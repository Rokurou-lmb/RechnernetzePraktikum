package praktikum2.lmb_chat_client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
				_socket.receive(packet);
				byte[] data = packet.getData();
				String messageDataString = new String(data).trim();
				if(messageDataString.startsWith("CONN ")) {
					String nickname = messageDataString.substring(5);
					Client newClient = new Client(nickname, packet.getAddress(), packet.getPort());
					_chatClient.registerNewClient(newClient);
				} else if(messageDataString.startsWith("QUIT ")) {
					String nickname = messageDataString.substring(5);
					_chatClient.unregisterClient(nickname);
				} else if(messageDataString.startsWith("MESSAGE ")) {
					String messageData = messageDataString.substring(9);
					Message parsedMessage = parseMessage(messageData);
					_chatClient.registerNewClient(new Client(parsedMessage.getSender(), packet.getAddress(), packet.getPort())); //TODO: check if client is already registered before registering
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
	private Message parseMessage(String messageData) {
		if(messageData.startsWith("MESSAGE ")) {
			messageData = messageData.substring(8);
		}
		String[] splitMessageData = messageData.split(";", 3);
		return new Message(Long.parseLong(splitMessageData[0]), splitMessageData[2], splitMessageData[1]);
	}
	
	/**
	 * Displays the {@code Message} in the output area
	 * @param message
	 */
	private void showMessage(Message message) {
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
		messageString += message.getMessageString() + "\n";
		return messageString;
	}
	
	private void logMessage(Message message) {
		//TODO implement logging
	}
}
