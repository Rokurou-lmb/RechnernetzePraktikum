package praktikum2.lmb_chat_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;
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
		_nickNamePattern = Pattern.compile("\\w*");
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
		//TODO: let the client notify all other clients
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
			} else if(request.equals("MESSAGE")) {
				Message incomingMessage = receiveMessage();
				log(incomingMessage);
			} else if(request.equals("USER?")) {
				sendUserList();
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
	 * Checks whether the given Nickname is of the correct format.
	 * @param timestamp
	 * @return {@code true} if the timestamp was accepted, {@code false} otherwise
	 */
	private boolean timestampAccepted(String timestamp) {
		return _timestampPattern.matcher(timestamp).matches();
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
	 * Uebertraegt die Liste der aktiven Nutzer an den Client
	 */
	private void sendUserList() {
		String response = "USER " + _nickname;
		Set<String> userSet = _server.getRegisteredNicknames();
		for (String nickname : userSet) {
			if(!nickname.equals(_nickname)) {
				response += ", " + nickname;
			}
		}
		sendResponseToClient(response);
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

	private void log(Message incomingMessage) {
		// TODO implement logging
	}
}
