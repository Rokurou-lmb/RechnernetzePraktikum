package praktikum3.jmln_chat_client;

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
import java.util.ArrayList;
import java.util.List;

import javax.net.SocketFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatClient extends JFrame {

	private static final long serialVersionUID = 1L;

	private InetAddress _serverAddress;
	public final int _serverPort = 56789;
	private List<String> _connectedClients;
	private String _nickname;
	private Socket _tcpSocket;
	private PrintWriter _writer;
	private BufferedReader _reader;
	private Thread _messageReceiveThread;
	
	private JTextArea _userListOutputArea;
	private JTextArea _messageOutputArea;
	
	public ChatClient() throws Exception {
		super("LMB Chat Client");
		_connectedClients = new ArrayList<>();
		_serverAddress = InetAddress.getByName((String)JOptionPane.showInputDialog(this, "Please enter the Chat Servers IP Adress: ", "Setup", JOptionPane.OK_OPTION)); 
		
		initializeUI();
		openConnection();
		_messageReceiveThread = new TCPMessageReceiveThread(_reader, this, true);
		_messageReceiveThread.start();
		pack();
		setVisible(true); 
	}
	
	private void initializeUI() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setMinimumSize(new Dimension(400,200));
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
		inputContainer.setPreferredSize(new Dimension(550, 30));
		
		JButton userButton = new JButton("Force Userlist Update");
		userButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendRequestToServer("list_users");
			}
		});
		GridBagConstraints userButtonConstraints = new GridBagConstraints();
		userButtonConstraints.weightx = 0;
		userButtonConstraints.weighty = 1;
		userButtonConstraints.fill = GridBagConstraints.VERTICAL;
		inputContainer.add(userButton,userButtonConstraints);
		
		JTextField input = new JTextField();
		input.setHorizontalAlignment(JTextField.LEFT);
		GridBagConstraints inputConstraints = new GridBagConstraints();
		inputConstraints.weightx = 1;
		inputConstraints.weighty = 1;
		inputConstraints.fill = GridBagConstraints.BOTH;
		inputContainer.add(input, inputConstraints);
		inputContainer.setMinimumSize(new Dimension(300,20));
		
		JButton sendButton = new JButton("Senden");
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { 
				String messageData = input.getText();
				input.setText("");
				sendMessage(messageData);
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
		outputScrollPane.setPreferredSize(new Dimension(450,250));
		JSplitPane ioContainer = new JSplitPane(JSplitPane.VERTICAL_SPLIT, outputScrollPane, inputContainer);
		ioContainer.setResizeWeight(1);
		
		//initialize userList and horizontal Splitpane
		_userListOutputArea = new JTextArea();
		_userListOutputArea.setEditable(false);
		JScrollPane userListScrollPane = new JScrollPane(_userListOutputArea);
		userListScrollPane.setPreferredSize(new Dimension(50, 300));
		userListScrollPane.setMinimumSize(new Dimension(50, 10));
		userListScrollPane.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
		JSplitPane userListIOContainer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userListScrollPane, ioContainer);
		userListIOContainer.setResizeWeight(0.1);
		
		//add the nested components to the window container
		container.add(userListIOContainer, BorderLayout.CENTER);
		input.requestFocusInWindow();
		this.getRootPane().setDefaultButton(sendButton);
	}
	
	/**
	 * Tries opening a connection with the server under the given address 
	 * and broadcasts an introductory message to all connected clients.
	 * @throws IOException
	 * @throws InvocationTargetException 
	 */
	private void openConnection() throws IOException {
		//try opening a connection with the server
		_tcpSocket =  SocketFactory.getDefault().createSocket(_serverAddress, _serverPort);
		OutputStream out = _tcpSocket.getOutputStream();
		InputStream in = _tcpSocket.getInputStream();
		_writer = new PrintWriter(out, true);
		_reader = new BufferedReader(new InputStreamReader(in));
		
		//negotiate a nickname with the server
		String dialogMessage = "Please enter your preferred nickname: ";
		String response = "";
		do {
			if(!response.isEmpty()) {
				dialogMessage = "Nickname has been declined. Please enter a new one";
			}
			_nickname = (String)JOptionPane.showInputDialog(this, dialogMessage, "Setup", JOptionPane.OK_OPTION);
			sendRequestToServer("auth " + _nickname);
			response = readResponseFromServer();
		} while(!response.matches("accept"));

		setTitle(getTitle() + " - Connected as " + _nickname);
		
		updateUserList();
	}
	
	/**
	 * Called when the window is closed.
	 * Terminates the Connection with the Server and releases all resources.
	 * @throws IOException 
	 */
	private void closeConnection() {
		_messageReceiveThread.interrupt();
		sendRequestToServer("quit");
		try {
			while(!readResponseFromServer().equals("bye"));
		} catch(IOException e) {
			e.printStackTrace();
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
		sendRequestToServer("list_users");
	}
	
	/**
	 * Updates the {@code JTextArea} which holds the nicknames of currently connected clients.
	 */
	public void updateUserListDisplay(String newUsers) {
		String[] userArray = newUsers.split(" ");
		String users = "";
		for(String user : userArray) {
			users += user + "\n";
		}
		_userListOutputArea.setText(users);
	}
	
	/**
	 * Sends the given {@code Message} to all currently connected Clients.
	 * @param message the message to send
	 * @throws IOException 
	 */
	private void sendMessage(String message) {
		sendRequestToServer("smsg " + message);
	}
	
	/**
	 * Reads the incoming response from the Server
	 * 
	 * @return {@code String} the incoming response
	 * @throws IOException in case of errors while reading the response
	 */
	private synchronized String readResponseFromServer() throws IOException {
		String response = _reader.readLine();
		System.err.println("SERVER: " + response);
		return response;
	}
	
	/**
	 * Sends the specified request to the Server
	 * 
	 * @param request the String to send
	 */
	private synchronized void sendRequestToServer(String request) {
		System.err.println("CLIENT: " + request);
		_writer.println(request);
	}
	
	public static void main(String args[]) {
		try {
			new ChatClient();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<String> getConnectedClients() {
		return _connectedClients;
	}
	
	public JTextArea getUserListOutputArea() {
		return _userListOutputArea;
	}
	
	public JTextArea getMessageOutputArea() {
		return _messageOutputArea;
	}
}