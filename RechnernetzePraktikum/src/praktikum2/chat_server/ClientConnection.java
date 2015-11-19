package praktikum2.chat_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Pattern;
import praktikum2.Message;

class ClientConnection implements Runnable{
	
	private String _nickname;
	private Socket _socket;
	private ChatServer _server;
	private BufferedReader _inFromClient;
	private PrintWriter _outToClient;

	private Pattern _nickNamePattern;
	private Pattern _timestampPattern;
	
	public ClientConnection(Socket socket, ChatServer server) {
		_nickNamePattern = Pattern.compile("NICKNAME \\w*");
		_timestampPattern = Pattern.compile("TIMESTAMP \\d*");
		_socket = socket;
		_server = server;
		try {
			_inFromClient = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
			_outToClient = new PrintWriter(_socket.getOutputStream(), true);
		} catch(Exception e) {
			System.err.println("There was a problem with instantiating In/Outputstreams");
		}
	}
	
	public void run() {
		try {
			initiateConnection();
			receiveIncomingRequests();
			_socket.close();
		} catch(IOException e) {
			System.err.println("Connection aborted by client!");
		} finally {
			_server._workerThreadsSem.release();
		}
	}
	
	private void receiveIncomingRequests() throws IOException {
		boolean connectionRequired = true;
		while(connectionRequired) {
			String request = readRequestFromClient();
			if(request.equals("QUIT")) {
				connectionRequired = false;
				sendResponseToClient("GOODBYE, RELEASING " + _nickname);
				_server.unregisterNickname(_nickname);
			} else if(request.equals("MESSAGE")) {
				broadcastMessage(receiveMessage());
			} else if(request.equals("USER?")) {
				//TODO: implement this
			}
		}
	}
	
	/**
	 * Receives all required Message Data from the client and returns it as a {@code Message}
	 * @return the {@code Message} that is to be broadcasted
	 * @throws IOException
	 */
	private Message receiveMessage() throws IOException {
		sendResponseToClient("OK");
		String timestamp;
		while(!timestampAccepted(timestamp = readRequestFromClient()));
		String request;
		String messageData = "";
		do {
			request = readRequestFromClient();
			if(request.startsWith("DATA ")) {
				messageData += request.substring(5);
			}
		} while(!request.equals("FINISHED"));
		sendResponseToClient("MESSAGE ACCEPTED");
		return new Message(Long.parseLong(timestamp.substring(10)), messageData, _nickname);
	}

	/**
	 * Checks wheter the given Nickname is of the correct format.
	 * @param timestamp
	 * @return {@code true} if the timestamp was accepted, {@code false} otherwise
	 */
	private boolean timestampAccepted(String timestamp) {
		return _timestampPattern.matcher(timestamp).matches();
	}

	/**
	 * Initiates the Connection with the Client, including nickname negotiation
	 * 
	 * @throws IOException
	 */
	private void initiateConnection() throws IOException {
		sendResponseToClient("NICKNAME?");
		while(!nickNameRegisteredSuccessfully(readRequestFromClient())) {
			sendResponseToClient("REFUSED ERRORCODE 01 - MESSAGE NOT IMPLEMENTED");
		}
		_server.registerClientConnection(this);
		sendResponseToClient("ACCEPTED, CONNECTED AS " + _nickname);
		broadcastMessage(new Message("Client connected under Nickname: " + _nickname, "SYSTEM"));
	}
	
	/**
	 * Checks wheter the given Nickname is allowed and not taken.
	 * Retgisters the nickname if it is accepted
	 * 
	 * @param nickname the Clients chosen nickname
	 * @return {@code true} if the nickname was accepted, {@code false} otherwise
	 */
	private boolean nickNameRegisteredSuccessfully(String nickname) {
		if(_nickNamePattern.matcher(nickname).matches() && _server.registerNickname(nickname)) { //shortcircuiting to prevent registering unallowed nicknames
			_nickname = nickname.substring(9);
			return true;
		}
		return false;
	}
	
	/**
	 * Reads the incoming request from this Connections Client
	 * 
	 * @return {@code String} the incoming request
	 * @throws IOException in case of errors while reading the request
	 */
	public String readRequestFromClient() throws IOException {
		String request = _inFromClient.readLine();
		System.err.println("CLIENT: " + request);
		return request;
	}
	
	/**
	 * Sends the specified Response to this Connections Client
	 * 
	 * @param message the String to send
	 */
	public void sendResponseToClient(String request) {
		System.err.println("Server: " + request);
		_outToClient.println(request);
	}
	
	/**
	 * Broadcasts the given {@code Message} to all connected Clients.
	 * @param message the message to broadcast
	 * @throws IOException 
	 */
	private void broadcastMessage(Message message) throws IOException {
		for(ClientConnection clientConnection : _server._verteiler) {
//			if(clientConnection != this) {
				sendMessage(message, clientConnection);
//			}
		}
	}
	
	/**
	 * Sends the given {@code Message} to the given Client.
	 * @param message the message to send
	 * @param client the client to send the message to
	 * @throws IOException 
	 */
	private void sendMessage(Message message, ClientConnection client) throws IOException {
		client.sendResponseToClient("MESSAGE");
		while(!(client.readRequestFromClient().equals("OK")));
		client.sendResponseToClient("NICKNAME " + message.getSender());
		client.sendResponseToClient("TIMESTAMP " + message.getTimeStamp());
		client.sendResponseToClient("DATA " + message.getMessageData());
		client.sendResponseToClient("FINISHED");
		while(!(client.readRequestFromClient().equals("MESSAGE ACCEPTED")));
	}
}
