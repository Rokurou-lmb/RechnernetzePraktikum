package praktikum2.lmb_chat_client;

import java.io.IOException;

import javax.swing.JScrollPane;

public class ClientBroadcastThread implements Runnable{

	private ChatClient _chatClient;
	private int _portnumber;
	public boolean _acceptsNewConnections;
	private JScrollPane _output;
	
	
	public ClientBroadcastThread(ChatClient chatClient) throws IOException {
		_acceptsNewConnections = true;
		_chatClient = chatClient;
		_output = chatClient._output;
	}
	
	@Override
	public void run() {

	}
}