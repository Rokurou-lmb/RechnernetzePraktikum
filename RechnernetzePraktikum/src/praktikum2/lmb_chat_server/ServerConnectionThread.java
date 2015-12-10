package praktikum2.lmb_chat_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnectionThread implements Runnable{

	private ChatServer _chatServer;
	private int _portnumber;
	public boolean _acceptsNewConnections;
	
	
	public ServerConnectionThread(ChatServer chatServer) throws IOException {
		_acceptsNewConnections = true;
		_chatServer = chatServer;
		_portnumber = chatServer._serverPort;
		System.err.println("SERVER CONNECTION THREAD IS RUNNING");
	}
	
	@Override
	public void run() {
		try(ServerSocket serverSocket = new ServerSocket(_portnumber)) {
			Socket newClientSocket;
			while(_acceptsNewConnections) {
				System.err.println("SERVER CONNECTION THREAD IS WAITING FOR CONNECTIONS");
				_chatServer._workerThreadsSem.acquire();
				newClientSocket = serverSocket.accept();
				System.err.println("A NEW CONNECTION HAS BEEN ESTABLISHED");
				ClientConnection newClientConnection = new ClientConnection(newClientSocket, _chatServer);
				new Thread(newClientConnection, "ClientConnectionThread for Client " + newClientConnection.getNickname()).start();
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
