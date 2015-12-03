package praktikum2.lmb_chat_client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import praktikum2.Message;

public class UDPMessageReceiveRunnable implements Runnable{
	
	private DatagramSocket _socket;
	private ChatClient _chatClient;
	public boolean _receivesMessages;
	
	public UDPMessageReceiveRunnable(ChatClient chatClient, boolean receivesMessages) {
		_chatClient = chatClient;
		_receivesMessages = receivesMessages;
	}
	
	@Override
	public void run() {
		try {
			_socket = new DatagramSocket(_chatClient._serverPort);
			while(_receivesMessages) {
				byte[] buffer = new byte[10000];
				DatagramPacket packet = new DatagramPacket(buffer, 1000); //TODO check which length to use?
				_socket.receive(packet); //TODO this might be executed after the ChatClient has been disposed off?
				byte[] data = packet.getData();
				String messageData = new String(data);
				Message parsedMessage = parseMessage(messageData);
				showMessage(parsedMessage);
				logMessage(parsedMessage);
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
		if(messageData.startsWith("DATA ")) {
			messageData = messageData.substring(5);
		}
		return new Message(Long.parseLong(timestamp.substring(10)), messageData, nickname);
	}
	
	/**
	 * Displays the {@code Message}
	 * @param message
	 */
	private void showMessage(Message message) { //TODO: test this
		String messageString = constructMessageString(message);
		_chatClient._output.append(messageString);
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
		messageString += message.getMessageData();
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
