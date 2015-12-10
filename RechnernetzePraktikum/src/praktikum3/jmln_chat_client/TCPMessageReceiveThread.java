package praktikum3.jmln_chat_client;

import java.io.BufferedReader;

import javax.swing.JTextArea;

import praktikum2.Message;

public class TCPMessageReceiveThread extends Thread{
	
	private BufferedReader _reader;
	private ChatClient _chatClient;
	private JTextArea _messageOutputArea;
	public boolean _receivesMessages;
	
	public TCPMessageReceiveThread(BufferedReader reader, ChatClient chatClient, boolean receivesMessages) {
		_reader = reader;
		_chatClient = chatClient;
		_messageOutputArea = chatClient.getMessageOutputArea();
		_receivesMessages = receivesMessages;
	}
	
	@Override
	public void run() {
		try {
			while(!isInterrupted()) {
				String serverResponse = "";
				serverResponse = _reader.readLine();
				if(serverResponse.startsWith("users ")) {
					_chatClient.updateUserListDisplay(serverResponse.substring(6));
				} else if(serverResponse.startsWith("rmsg ")) {
					String messageData = serverResponse.substring(5);
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
	private Message parseMessage(String messageData) {
		String[] messageDataArray = messageData.split(" ", 2);
		String userName = messageDataArray[0];
		String messageString = messageDataArray[1];
		return new Message(System.currentTimeMillis(), messageString, userName);
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
