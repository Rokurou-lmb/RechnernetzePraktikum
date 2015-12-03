package praktikum2.lmb_chat_client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import javax.net.SocketFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import praktikum2.Client;
import praktikum2.Message;

//TODO: Umbau auf UDP und broadcasten von Clients an Clients.
public class ChatClient extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private InetAddress _serverAddress;
	public final int _serverPort = 50000;
	private InetAddress _localAddress;
	private int _localPort;
	private Set<Client> _connectedClients;
	private String _nickname;
	private Socket _socket;
	private PrintWriter _writer;
	private BufferedReader _reader;
	private Thread _messageReceiveThread;
	private ClientReceiveThread _clientReceiveThread; //TODO: implement this Thread and initialize it
	
	private Container _container;
	private JSplitPane _userListIOContainer;
	private JSplitPane _ioContainer;
	private JScrollPane _userListScrollPane;
	public JTextArea _userListOutput;//TODO Show the own Clients nickname?
	private JScrollPane _outputScrollPane;
	public JTextArea _output; //TODO make private if not needed elsewhere
	private JPanel _inputContainer;
	private JTextField _input;
	private JButton _sendButton;
	private JButton _userButton;

	
	public ChatClient() throws Exception {
		super("LMB Chat Client");
		_connectedClients = new HashSet<>();
		_messageReceiveThread = new Thread(new UDPMessageReceiveRunnable(this, true));
		_clientReceiveThread = new ClientReceiveThread();
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				closeConnection();
			}
		});
		
		setSize(500, 500);
		_container = getContentPane();
		_container.setLayout(new BorderLayout());
		
		_serverAddress = InetAddress.getByName((String)JOptionPane.showInputDialog(_container, "Please enter the Chat Servers IP Adress: ", "Setup", JOptionPane.OK_OPTION)); 
		
		//initialize inputfield and buttons
		_inputContainer = new JPanel();
		_inputContainer.setLayout(new GridBagLayout());
		
		_userButton = new JButton("Force Userlist Update");
		_userButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { 
				updateUserList();
			}
		});
		GridBagConstraints userButtonConstraints = new GridBagConstraints();
		userButtonConstraints.weightx = 0;
		userButtonConstraints.weighty = 1;
		userButtonConstraints.fill = GridBagConstraints.VERTICAL;
		_inputContainer.add(_userButton,userButtonConstraints);
		
		_input = new JTextField();
		_input.setHorizontalAlignment(JTextField.LEFT);
		_input.setPreferredSize(new Dimension(100, 50));
		GridBagConstraints inputConstraints = new GridBagConstraints();
		inputConstraints.weightx = 1;
		inputConstraints.weighty = 1;
		inputConstraints.fill = GridBagConstraints.BOTH;
		_inputContainer.add(_input, inputConstraints);
		
		_sendButton = new JButton("Senden");
		_sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { 
				String messageData = _input.getText();
				_input.setText("");
				broadcastMessage(new Message(messageData, _nickname));
			}
		});
		GridBagConstraints sendButtonConstraints = new GridBagConstraints();
		sendButtonConstraints.weightx = 0;
		sendButtonConstraints.weighty = 1;
		sendButtonConstraints.fill = GridBagConstraints.VERTICAL;
		_inputContainer.add(_sendButton,sendButtonConstraints);
		
		//initialize outputfield and vertical Splitpane
		_outputScrollPane = new JScrollPane();
		_outputScrollPane.setPreferredSize(new Dimension(500, 350));
		_output = new JTextArea();
		_outputScrollPane.add(_output);
		_ioContainer = new JSplitPane(JSplitPane.VERTICAL_SPLIT, _outputScrollPane, _inputContainer);
		
		//initialize userList and horizontal Splitpane
		_userListScrollPane = new JScrollPane();
		_userListScrollPane.setPreferredSize(new Dimension(100, 500));
		_userListOutput = new JTextArea();
		_userListScrollPane.add(_userListOutput);
		_userListIOContainer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, _userListScrollPane, _ioContainer);
		
		//add the nested components to the window container
		_container.add(_userListIOContainer, BorderLayout.CENTER);
		
		openConnection();
		_messageReceiveThread.start();
		
		pack();
		setVisible(true);
	}
	
	/**
	 * Tries opening a connection with the server under the given address 
	 * and broadcasts an introductory message to all connected clients.
	 * @throws IOException
	 */
	private void openConnection() throws IOException {
		//try opening a connection with the server
		_socket =  SocketFactory.getDefault().createSocket(_serverAddress, _serverPort);
		_localAddress = _socket.getInetAddress();
		_localPort = _socket.getLocalPort();
		OutputStream out = _socket.getOutputStream();
		InputStream in = _socket.getInputStream();
		_writer = new PrintWriter(out, true);
		_reader = new BufferedReader(new InputStreamReader(in));
		
		//negotiate a nickname with the server
		while(!(readResponseFromServer().equals("NICKNAME?")));
		String dialogMessage = "Please enter your preferred nickname: ";
		String response = "";
		do {
			if(!response.isEmpty()) {
				dialogMessage = response.substring(8);
			}
			_nickname = (String)JOptionPane.showInputDialog(_ioContainer, dialogMessage, "Setup", JOptionPane.OK_OPTION);
			sendRequestToServer("NICKNAME " + _nickname);
			response = readResponseFromServer();
		} while(!response.matches("ACCEPTED, CONNECTED AS \\w*"));
		
		//introduce this client to all other connected clients
		Message introductionMessage = new Message("CONN " + _nickname + ";" + _localAddress.getHostAddress() + ";" + _localPort, "SYSTEM");
		updateUserList();
		broadcastMessage(introductionMessage);
	}
	
	/**
	 * Called when the window is closed.
	 * Terminates the Connection with the Server and releases all resources.
	 * @throws IOException 
	 */
	private void closeConnection() {
		broadcastMessage(new Message("QUIT " + _nickname + ";" + _localAddress + ";" + _localPort, "SYSTEM"));
		sendRequestToServer("QUIT");
		try {
			while(!(readResponseFromServer().matches("GOODBYE, RELEASING \\w*")));
		} catch(IOException e) {
		} finally {
			dispose();
			System.exit(0);
		}
	}
	
	/**
	 * Updates the internal {@code Set} _connectedClients which holds all currently connected chat clients
	 */
	private void updateUserList(){
		try {
			Thread userUpdateThread = new Thread(new UpdateUserListRunnable());
			userUpdateThread.start();
			userUpdateThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Updates the {@code JTextArea} which holds the nicknames of currently connected clients.
	 */
	private void updateUserListDisplay() {
		
	}
	
	/**
	 * Reads the incoming response from the Server
	 * 
	 * @return {@code String} the incoming response
	 * @throws IOException in case of errors while reading the response
	 */
	public String readResponseFromServer() throws IOException {
		String response = _reader.readLine();
		System.err.println("SERVER: " + response);
		return response;
	}
	
	/**
	 * Sends the specified request to the Server
	 * 
	 * @param request the String to send
	 */
	public void sendRequestToServer(String request) {
		System.err.println("CLIENT: " + request);
		_writer.println(request);
	}
	
	/**
	 * Sends the given {@code Message} to all currently connected Clients.
	 * @param message the message to send
	 * @throws IOException 
	 */
	private void broadcastMessage(Message message) {
		new Thread(new UDPMessageBroadcastRunnable(_connectedClients, message)).start();
	}
	
	public static void main(String args[]) {
		try {
			new ChatClient();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public class UpdateUserListRunnable implements Runnable{
		
		@Override
		public void run() {
			try {
				sendRequestToServer("USER?");
				String response;
				do {
					response = readResponseFromServer();
					if(response.startsWith("USER ")) {
						parseResponse(response);
					}
				} while(!response.startsWith("FINISHED"));
				updateUserListDisplay();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * Parses the response and updates the {@code ChatClient}s {@code Set} _connectedClients
		 * @param response the responseString to parse
		 * @throws UnknownHostException 
		 */
		public void parseResponse(String response) throws UnknownHostException {
			response = response.substring(5);
			String[] users = response.split(",");
			for (String userDataString : users) {
				String userData[] = userDataString.split(";");
				
				String nickname = userData[0];
				InetAddress address = InetAddress.getByName(userData[1]);
				int port = Integer.parseInt(userData[2]);
				Client client = new Client(nickname, address, port);
				_connectedClients.add(client);
			}
		}
	}
}
