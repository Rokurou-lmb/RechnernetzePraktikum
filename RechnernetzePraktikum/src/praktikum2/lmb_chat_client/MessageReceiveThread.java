package praktikum2.lmb_chat_client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import praktikum2.Message;

public class MessageReceiveThread implements Runnable{

	private DatagramSocket _socket;
	private ChatClient _chatClient;
	public boolean _receivesMessages;
	
	public MessageReceiveThread(ChatClient chatClient, boolean receivesMessages) {
		_chatClient = chatClient;
		_receivesMessages = receivesMessages;
	}
	
	@Override
	public void run() {
		try {
			_socket = new DatagramSocket(_chatClient._remotePort);
			while(_receivesMessages) {
				byte[] buffer; //TODO initialise buffer 
				DatagramPacket packet = new DatagramPacket(buffer, 10); //TODO check which length to use?
				_socket.receive(packet);
				byte[] data = packet.getData();
				String messageData = new String(data);
				//TODO: Do something with the message data
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Message parseMessage(String messageData) {
		//TODO: implement Message parsing.
		return null;
	}
	
	private void showMessage(Message message) {
		//TODO implement this
	}
	
	private void log(Message message) {
		//TODO implement this
	}

}
