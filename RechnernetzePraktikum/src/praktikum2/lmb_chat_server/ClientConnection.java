package praktikum2.lmb_chat_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Set;
import java.util.regex.Pattern;

class ClientConnection implements Runnable{
	
	private String _nickname;

	private Socket _socket;
	private ChatServer _server;
	private BufferedReader _inFromClient;
	private PrintWriter _outToClient;

	private Pattern _nickNamePattern;
	
	public ClientConnection(Socket socket, ChatServer server) {
		_nickNamePattern = Pattern.compile("\\w*");
		_socket = socket;
		_server = server;
		try {
			_inFromClient = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
			_outToClient = new PrintWriter(_socket.getOutputStream(), true);
		} catch(Exception e) {
			System.err.println("There was a problem with instantiating the In/Outputstreams");
		}
	}
	
	public void run() {
		try {
			initiateConnection();
			receiveIncomingRequests();
			_socket.close();
		} catch(IOException e) {
			System.err.println("Connection aborted by client!");
			_server.unregisterNickname(_nickname);
		} finally {
			_server._workerThreadsSem.release();
		}
	}

	/**
	 * Initiates the Connection with the Client, including nickname negotiation
	 * 
	 * @throws IOException
	 */
	private void initiateConnection() throws IOException {
		sendResponseToClient("NICKNAME?");
		while(!nickNameRegisteredSuccessfully(readRequestFromClient().substring(9))) {}
		_server.registerClientConnection(this);
		sendResponseToClient("ACCEPTED, CONNECTED AS " + _nickname);
	}
	
	/**
	 * Receives incoming requests from the client until
	 * the client sends "QUIT", then it terminates this Thread
	 * @throws IOException
	 */
	private void receiveIncomingRequests() throws IOException {
		boolean connectionRequired = true;
		while(connectionRequired) {
			String request = readRequestFromClient();
			if(request.equals("QUIT")) {
				connectionRequired = false;
				sendResponseToClient("GOODBYE, RELEASING " + _nickname);
				_server.unregisterNickname(_nickname);
			} else if(request.equals("USER?")) {
				sendUserList();
				sendResponseToClient("FINISHED");
				while(readRequestFromClient() != "USERLIST ACCEPTED"); //TODO Rethink if this part of the protocol is needed
			}
		}
	}
	
	/**
	 * Checks whether the given Nickname is allowed and not taken.
	 * Registers the nickname if it is accepted
	 * 
	 * @param nickname the Clients chosen nickname
	 * @return {@code true} if the nickname was accepted, {@code false} otherwise
	 */
	private boolean nickNameRegisteredSuccessfully(String nickname) { 
		if(!nickname.isEmpty() && _nickNamePattern.matcher(nickname).matches()) {
			if(_server.registerNickname(nickname)) {
				_nickname = nickname;
				return true;
			} else {
				sendResponseToClient("REFUSED ERRORCODE 42 - Name already taken");
			}
		} else {
			sendResponseToClient("REFUSED ERRORCODE 1337 - Name not allowed");
		}
		return false;
	}
	
	/**
	 * Sends a list of all currently registered Users to the Client
	 * @throws IOException 
	 */
	private void sendUserList() throws IOException {
		Set<ClientConnection> clientConnections = _server.getRegisteredConnections();
		
		String nickname;
		InetAddress address;
		Integer port;
		String response = "USER ";
		
		for (ClientConnection clientConnection : clientConnections) {
			address = clientConnection.getRemoteAddress();
			port = clientConnection.getRemotePort();
			nickname = clientConnection.getNickname();
			response += nickname + ";" + address.getHostAddress() + ";" + port.toString() + ",";
		}
		response = response.substring(0, response.length()-1); //Remove trailing ","
		sendResponseToClient(response);
	}
	
	/**
	 * Reads the incoming request from this Connections Client
	 * 
	 * @return {@code String} the incoming request
	 * @throws IOException in case of errors while reading the request
	 */
	private String readRequestFromClient() throws IOException {
		String request = _inFromClient.readLine();
		System.err.println("CLIENT: " + request);
		return request;
	}
	
	/**
	 * Sends the specified Response to this Connections Client
	 * 
	 * @param message the {@code String} to send
	 */
	private void sendResponseToClient(String request) {
		System.err.println("Server: " + request);
		_outToClient.println(request);
	}

	public InetAddress getRemoteAddress() {
		return _socket.getInetAddress();
	}

	public Integer getRemotePort() {
		return _socket.getPort();
	}
	
	public String getNickname() {
		return _nickname;
	}
}
