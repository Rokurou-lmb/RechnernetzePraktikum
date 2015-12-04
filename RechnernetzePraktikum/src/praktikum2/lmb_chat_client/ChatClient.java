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
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.SocketFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import praktikum2.Client;
import praktikum2.Message;

public class ChatClient extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private InetAddress _serverAddress;
	public final int _serverPort = 50000;
	private InetAddress _localAddress;
	private int _localPort;
	private Map<String, Client> _connectedClients;
	private String _nickname;
	private Socket _socket;
	private PrintWriter _writer;
	private BufferedReader _reader;
	private Thread _messageReceiveThread;
	
	private JTextArea _userListOutputArea;
	private JTextArea _messageOutputArea; //TODO make private if not needed elsewhere
	
	public ChatClient() throws Exception {
		super("LMB Chat Client");
		_connectedClients = new ConcurrentHashMap<>();
		_serverAddress = InetAddress.getByName((String)JOptionPane.showInputDialog(this, "Please enter the Chat Servers IP Adress: ", "Setup", JOptionPane.OK_OPTION)); 
		
		setSize(500, 500);
		initializeUI();
		openConnection();
		_messageReceiveThread = new Thread(new UDPMessageReceiveRunnable(this, true), "MessageReceiveThread for Client " + _nickname);
		_messageReceiveThread.start();
		pack();
		setVisible(true);
	}
	
	private void initializeUI() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				closeConnection();
			}
		});
		
		Container container = getContentPane();
		container.setLayout(new BorderLayout());
		
		//initialize inputfield and buttons
		JPanel inputContainer = new JPanel();
		inputContainer.setLayout(new GridBagLayout());
		
		JButton userButton = new JButton("Force Userlist Update");
		userButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new UpdateUserListRunnable());
			}
		});
		GridBagConstraints userButtonConstraints = new GridBagConstraints();
		userButtonConstraints.weightx = 0;
		userButtonConstraints.weighty = 1;
		userButtonConstraints.fill = GridBagConstraints.VERTICAL;
		inputContainer.add(userButton,userButtonConstraints);
		
		JTextField input = new JTextField();
		input.setHorizontalAlignment(JTextField.LEFT);
		input.setPreferredSize(new Dimension(100, 50));
		GridBagConstraints inputConstraints = new GridBagConstraints();
		inputConstraints.weightx = 1;
		inputConstraints.weighty = 1;
		inputConstraints.fill = GridBagConstraints.BOTH;
		inputContainer.add(input, inputConstraints);
		
		JButton sendButton = new JButton("Senden");
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { 
				String messageData = input.getText();
				input.setText("");
				broadcastMessage(new Message(messageData, _nickname));
			}
		});
		GridBagConstraints sendButtonConstraints = new GridBagConstraints();
		sendButtonConstraints.weightx = 0;
		sendButtonConstraints.weighty = 1;
		sendButtonConstraints.fill = GridBagConstraints.VERTICAL;
		inputContainer.add(sendButton,sendButtonConstraints);
		
		//initialize outputfield and vertical Splitpane
		_messageOutputArea = new JTextArea();
		_messageOutputArea.setEditable(false);
		JScrollPane outputScrollPane = new JScrollPane(_messageOutputArea);
		outputScrollPane.setPreferredSize(new Dimension(500, 350));
		JSplitPane ioContainer = new JSplitPane(JSplitPane.VERTICAL_SPLIT, outputScrollPane, inputContainer);
		
		//initialize userList and horizontal Splitpane
		_userListOutputArea = new JTextArea();
		_userListOutputArea.setEditable(false);
		JScrollPane userListScrollPane = new JScrollPane(_userListOutputArea);
		userListScrollPane.setPreferredSize(new Dimension(100, 500));
		JSplitPane userListIOContainer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userListScrollPane, ioContainer);
		
		//add the nested components to the window container
		container.add(userListIOContainer, BorderLayout.CENTER);
	}
	
	/**
	 * Tries opening a connection with the server under the given address 
	 * and broadcasts an introductory message to all connected clients.
	 * @throws IOException
	 * @throws InvocationTargetException 
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
			_nickname = (String)JOptionPane.showInputDialog(this, dialogMessage, "Setup", JOptionPane.OK_OPTION);
			sendRequestToServer("NICKNAME " + _nickname);
			response = readResponseFromServer();
		} while(!response.matches("ACCEPTED, CONNECTED AS \\w*"));
		
		//introduce this client to all other connected clients
		Message introductionMessage = new Message("CONN " + _nickname + ";" + _localAddress.getHostAddress() + ";" + _localPort, "SYSTEM");//TODO: check why this doesn't get send or doesn't get parsed
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
	 * Updates the internal {@code Set} _connectedClients which holds all currently connected chat clients and refreshes the userlist ui component
	 * @throws InvocationTargetException 
	 */
	private void updateUserList(){
		try {
			SwingUtilities.invokeAndWait(new UpdateUserListRunnable());
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	public Map<String, Client> getConnectedClients() {
		return _connectedClients;
	}
	
	public JTextArea getUserListOutputArea() {
		return _userListOutputArea;
	}
	
	public JTextArea getMessageOutputArea() {
		return _messageOutputArea;
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
				sendRequestToServer("USERLIST ACCEPTED");
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
			_connectedClients.clear();
			for (String userDataString : users) {
				String userData[] = userDataString.split(";");
				
				String nickname = userData[0];
				InetAddress address = InetAddress.getByName(userData[1]);
				int port = Integer.parseInt(userData[2]);
				Client client = new Client(nickname, address, port);
				_connectedClients.put(nickname, client);
			}
		}
	}
	
	/**
	 * Updates the {@code JTextArea} which holds the nicknames of currently connected clients.
	 */
	public void updateUserListDisplay() {
		String users = "";
		for(Client client : _connectedClients.values()) {
			users += client.getNickname() + "\n";
		}
		_userListOutputArea.setText(users);
	}
}