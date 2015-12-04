package praktikum2.lmb_chat_client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.swing.JTextArea;

import praktikum2.Client;
import praktikum2.Message;

public class UDPMessageReceiveRunnable implements Runnable{
	
	private DatagramSocket _socket;
	private ChatClient _chatClient;
	private JTextArea _messageOutputArea;
	public boolean _receivesMessages;
	
	public UDPMessageReceiveRunnable(DatagramSocket socket, ChatClient chatClient, boolean receivesMessages) {
		_socket = socket;
		_chatClient = chatClient;
		_messageOutputArea = chatClient.getMessageOutputArea();
		_receivesMessages = receivesMessages;
	}
	
	@Override
	public void run() {
		try {
			while(_receivesMessages) {
				byte[] buffer = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				_socket.receive(packet); //TODO this might be executed after the ChatClient has been disposed off?
				byte[] data = packet.getData();
				String messageDataString = new String(data).trim();
				if(messageDataString.startsWith("CONN ")) {
					String messageData = messageDataString.substring(5);
					String[] clientData = messageData.split(";");
					String nickname = clientData[0];
					Client newClient = new Client(nickname, InetAddress.getByName(clientData[1]), Integer.parseInt(clientData[2]));
					_chatClient.registerNewClient(nickname, newClient);
					
				} else if(messageDataString.startsWith("QUIT ")) {
					String messageData = messageDataString.substring(5);
					String[] clientData = messageData.split(";");
					String nickname = clientData[0];
					_chatClient.unregisterClient(nickname);
					
				} else if(messageDataString.startsWith("MESSAGE ")) {
					String messageData = messageDataString.substring(9);
					Message parsedMessage = parseMessage(messageData);
					showMessage(parsedMessage);
					logMessage(parsedMessage);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses the received UDPDatagram and returns it as a {@code Message}
	 * @return the {@code Message} that has been transmitted
	 */
	private Message parseMessage(String messageData) { //TODO implement this after designing the MESSAGEDATA
		String timestamp = "";
		String nickname = "";
		if(messageData.startsWith("MESSAGE ")) {
			messageData = messageData.substring(8);
		}
		return new Message(Long.parseLong(timestamp.substring(10)), messageData, nickname);
	}
	
	/**
	 * Displays the {@code Message} in the output area
	 * @param message
	 */
	private void showMessage(Message message) { //TODO: test this
		String messageString = constructMessageString(message);
		_messageOutputArea.append(messageString);
	}
	
	/**
	 * Arranges the {@code Message}s data into a easily readable {@code String}.
	 * @param message the {@code Message} that is to be converted.
	 * @return A easily readable {@code String} representing the {@code Message}s data.
	 */
	private String constructMessageString(Message message) {
		String messageString = "";
		messageString += "[" + message.getTimeStampAsString() + "] ";
		messageString += message.getSender() + ": ";
		messageString += message.getMessageString();
		return messageString;
	}
	
	private void logMessage(Message message) {
		//TODO implement logging
	}
	
//	/**
//	 * Receives all required Message Data from the server  and returns it as a {@code Message}
//	 * @return the {@code Message} that is to be broadcasted
//	 * @throws IOException
//	 */
//	private Message receiveMessage() throws IOException {
//		sendRequestToServer("OK");
//		long timestamp = Long.parseLong(readResponseFromServer());
//		String request;
//		String messageData = "";
//		do {
//			request = readResponseFromServer();
//			if(request.startsWith("DATA ")) {
//				messageData += request.substring(5);
//			}
//		} while(!request.startsWith("FINISHED"));
//		sendRequestToServer("MESSAGE ACCEPTED");
//		return new Message(timestamp, messageData, _nickname);
//	}
}
