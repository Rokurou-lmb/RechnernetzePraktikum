package praktikum2.lmb_chat_server;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class ChatServer {
	private Set<String> _nicknames;
	private Set<ClientConnection> _verteiler;
	public Semaphore _workerThreadsSem;
	public final int _serverPort;
	
	public ChatServer(int serverPort, int maxParallelUsers) throws IOException {
		_nicknames = Collections.newSetFromMap(new ConcurrentHashMap<>());
		_verteiler = Collections.newSetFromMap(new ConcurrentHashMap<>());
		_workerThreadsSem = new Semaphore(maxParallelUsers);
		_serverPort = serverPort;
	}
	
	public static void main(String[] args) throws IOException {
		ChatServer myServer = new ChatServer(50001, 100);
		myServer.startServer();
	}
	
	public void startServer() throws IOException {
		new Thread(new ServerConnectionThread(this), "ServerConnectionThread").start();
	}
	
	public void registerClientConnection(ClientConnection newClientConnection) {
		_verteiler.add(newClientConnection);
	}
	
	public Set<ClientConnection> getRegisteredConnections() {
		return Collections.unmodifiableSet(_verteiler);
	}
	
	public void unregisterClient(ClientConnection clientConnection) {
		String nickname = clientConnection.getNickname();
		_nicknames.remove(nickname);
		_verteiler.remove(clientConnection);
	}

	public boolean registerNickname(String nickname) {
		return _nicknames.add(nickname);
	}
}