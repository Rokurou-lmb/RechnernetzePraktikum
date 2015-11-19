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
import java.net.Socket;

import javax.net.SocketFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import praktikum2.Message;

//TODO: Umbau auf UDP und broadcasten von Clients an Clients.
public class ChatClient extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private String _remoteHost;
	public final int _remotePort = 50000;
	private String _nickname;
	private Socket _socket;
	private PrintWriter _writer;
	private BufferedReader _reader;
	private boolean _clientRunning;
	private ClientBroadcastThread _clientBroadcastThread; //TODO: implement these Threads and initialize them
	private ClientReceiveThread _clientReceiveThread;
	
	//TODO Add a second JSplitpane to encapsulate a UserList on the Left side
	private Container _container;
	private JSplitPane _ioContainer;
	public JScrollPane _output;
	private JPanel _inputContainer;
	private JTextField _input;
	private JButton _sendButton;
	private JButton _userButton;

	
	public ChatClient() throws IOException {
		super("LMB Chat Client");
		_clientRunning = true;
		
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
		
		_remoteHost = (String)JOptionPane.showInputDialog(_container, "Please enter the Chat Servers IP Adress: ", "Setup", JOptionPane.OK_OPTION); 
		
		_inputContainer = new JPanel();
		_inputContainer.setLayout(new GridBagLayout());
		
		_userButton = new JButton("Benutzer");
		_userButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { 
				showUserList();
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
				try {
					sendMessage(new Message(messageData, _nickname));
				} catch(IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		GridBagConstraints sendButtonConstraints = new GridBagConstraints();
		sendButtonConstraints.weightx = 0;
		sendButtonConstraints.weighty = 1;
		sendButtonConstraints.fill = GridBagConstraints.VERTICAL;
		_inputContainer.add(_sendButton,sendButtonConstraints);
		
		_output = new JScrollPane();
		_output.setMinimumSize(new Dimension(500, 350));
		_ioContainer = new JSplitPane(JSplitPane.VERTICAL_SPLIT, _output, _inputContainer);
		_container.add(_ioContainer, BorderLayout.CENTER);
		
		openConnection();
		setVisible(true);
		
		while(_clientRunning) {
			receiveIncomingRequests();
		}
	}
	
	private void openConnection() throws IOException {
		try {
			_socket =  SocketFactory.getDefault().createSocket(_remoteHost, _remotePort);
			OutputStream out = _socket.getOutputStream();
			InputStream in = _socket.getInputStream();
			_writer = new PrintWriter(out, true);
			_reader = new BufferedReader(new InputStreamReader(in));
		} catch(Exception e) {
			e.printStackTrace();
		}
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
	}
	
	/**
	 * Called when the window is closed, terminates the Connection with the Server and releases all resources.
	 * @throws IOException 
	 */
	private void closeConnection() {
		sendRequestToServer("QUIT");
		try {
			while(!(readResponseFromServer().matches("GOODBYE, RELEASING \\w*")));
		} catch(IOException e) {
		} finally {
			dispose();
			System.exit(0);
		}
	}
	
	private void receiveIncomingRequests() throws IOException {
		boolean connectionRequired = true;
		while(connectionRequired) {
			String response = readResponseFromServer();
			if(response.equals("MESSAGE")) {
				
			}
		}
	}
	
	private void showUserList() { //TODO: transmit user Ip and port to the client and broadcast from the client.
		try {
			String users;
			users = getUserSet();
			String message = "These are the currently active Users: " + users;
			JOptionPane.showMessageDialog(_container, message, "Active Users", JOptionPane.INFORMATION_MESSAGE);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns a set of the currently registered ChatClients represented with their Nicknames
	 * @return Nicknames of currently registered ChatClients
	 * @throws IOException 
	 */
	private String getUserSet() throws IOException {
		String users = "";
		sendRequestToServer("USER?");
		String response;
		do {
			response = readResponseFromServer();
			if(response.startsWith("USER ")) {
				 users += response.substring(5);
			}
		} while(!response.startsWith("FINISHED"));
		return users;
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
	 * Sends the given {@code Message} to the Server.
	 * @param message the message to send
	 * @throws IOException 
	 */
	private void sendMessage(Message message) throws IOException {
		sendRequestToServer("MESSAGE");
		while(!(readResponseFromServer().equals("OK")));
		sendRequestToServer("TIMESTAMP " + message.getTimeStamp());
		sendRequestToServer("DATA " + message.getMessageData());
		sendRequestToServer("FINISHED");
		while(!(readResponseFromServer().equals("MESSAGE ACCEPTED")));
	}
	
	/**
	 * Receives all required Message Data from the server  and returns it as a {@code Message}
	 * @return the {@code Message} that is to be broadcasted
	 * @throws IOException
	 */
	private Message receiveMessage() throws IOException { //TODO need a second Thread running this method
		sendRequestToServer("OK");
		long timestamp = Long.parseLong(readResponseFromServer());
		String request;
		String messageData = "";
		do {
			request = readResponseFromServer();
			if(request.startsWith("DATA ")) {
				messageData += request.substring(5);
			}
		} while(!request.startsWith("FINISHED"));
		sendRequestToServer("MESSAGE ACCEPTED");
		return new Message(timestamp, messageData, _nickname);
	}
	
	public static void main(String args[]) {
		try {
			new ChatClient();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
