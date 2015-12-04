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
	
	public UDPMessageBroadcastRunnable(Map<String, Client> recipients, Message messageToBroadcast) {
		_recipients = recipients;
		_message = messageToBroadcast;
	}
	
	@Override
	public void run() {
		try (DatagramSocket udpSocket = new DatagramSocket()){
			String message = packMessage(_message);
			byte[] messageBytes = new byte[1024];
			messageBytes = message.getBytes();
			int offset = 0;
			int length = messageBytes.length;
			InetAddress address;
			Integer portnumber;
			for (Client recipient : _recipients.values()) {
				address = recipient.getIpAddress();
				portnumber = recipient.getPortnumber();
				DatagramPacket packet = new DatagramPacket(messageBytes, offset, length, address , portnumber);
				udpSocket.send(packet);
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
		String packedMessage = ""; //TODO implement this after designing the MESSAGEDATA
		String messageString = message.getMessageString();
		if(messageString.startsWith("CONN ")) {
			packedMessage = messageString;
		} else if(messageString.startsWith("QUIT ")) {
			packedMessage = messageString;
		} else if(messageString.startsWith("MESSAGE ")) {
			
		}
		return packedMessage;
	}
}