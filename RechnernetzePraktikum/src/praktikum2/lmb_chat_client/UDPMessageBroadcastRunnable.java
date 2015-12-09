package praktikum2.lmb_chat_client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import praktikum2.Client;
import praktikum2.Message;

public class UDPMessageBroadcastRunnable implements Runnable{

	private Message _message;
	private Map<String, Client> _recipients;
	private DatagramSocket _socket;
	
	public UDPMessageBroadcastRunnable(Map<String, Client> recipients, Message messageToBroadcast, DatagramSocket socket) {
		_recipients = recipients;
		_message = messageToBroadcast;
		_socket = socket;
	}
	
	@Override
	public void run() {
		try {
			String message = packMessage(_message);
			byte[] messageBytes = new byte[1024];
			messageBytes = message.getBytes();
			int offset = 0;
			int length = messageBytes.length;
			for (Client recipient : _recipients.values()) {
				InetAddress address = recipient.getIpAddress();
				Integer portnumber = recipient.getPortnumber();
				DatagramPacket packet = new DatagramPacket(messageBytes, offset, length, address , portnumber);
				_socket.send(packet);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Packs the data of the given {@code message} into a {@code String} to be sent in a {@code DatagramPacket}.
	 * @param message the message to pack for sending
	 */
	private String packMessage(Message message) {
		String packedMessage = "";
		String messageString = message.getMessageString();
		if(messageString.startsWith("CONN ") || messageString.startsWith("QUIT ")) {
			packedMessage = messageString;
		} else if(messageString.startsWith("MESSAGE ")) {
			packedMessage = "MESSAGE " + message.getTimeStamp() + ";" + message.getSender() + ";" + messageString.substring(8);
		}
		return packedMessage;
	}
}