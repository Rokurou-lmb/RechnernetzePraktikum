package praktikum4.lmb_chat_server;

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
	
	/**
	 * Tries creating a Server on the given Port which can accept the given number of users,
	 * @param serverPort the Port on which to listen for incoming connections
	 * @param maxParallelUsers the maximum number of parallel users that can connect to the Server.
	 * @throws IOException
	 */
	public ChatServer(int serverPort, int maxParallelUsers) {
		_nicknames = Collections.newSetFromMap(new ConcurrentHashMap<>());
		_verteiler = Collections.newSetFromMap(new ConcurrentHashMap<>());
		_workerThreadsSem = new Semaphore(maxParallelUsers);
		_serverPort = serverPort;
	}
	
	public static void main(String[] args) throws IOException {
		ChatServer myServer = new ChatServer(9400, 100);
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