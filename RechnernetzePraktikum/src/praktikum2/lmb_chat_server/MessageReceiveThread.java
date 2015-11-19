package praktikum2.lmb_chat_server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import praktikum2.Message;

public class MessageReceiveThread implements Runnable{

	private DatagramSocket _socket;
	private ChatServer _chatServer;
	public boolean _receivesMessages;
	
	public MessageReceiveThread(ChatServer chatServer, boolean receivesMessages) {
		_chatServer = chatServer;
		_receivesMessages = receivesMessages;
	}
	
	@Override
	public void run() {
		try {
			_socket = new DatagramSocket(_chatServer._serverPort);
			while(_receivesMessages) {
				byte[] buffer; //TODO initialize buffer 
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
	
	private void log(Message message) {
		
	}

}
