package praktikum2.chat_server;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import praktikum2.Message;

public class ChatServer {
	private Set<String> _nicknames;
	public Set<ClientConnection> _verteiler;
	public Semaphore _workerThreadsSem;
	public final int _serverPort;
	
	
	public ChatServer(int serverPort, int maxParallelUsers) throws IOException {
		_nicknames = Collections.newSetFromMap(new ConcurrentHashMap<>());
		_verteiler = Collections.newSetFromMap(new ConcurrentHashMap<>());
		_workerThreadsSem = new Semaphore(maxParallelUsers);
		_serverPort = serverPort;
	}
	
	public static void main(String[] args) throws IOException {
		ChatServer myServer = new ChatServer(50000, 5);
		myServer.startServer();
	}
	
	public void startServer() throws IOException {
		new Thread(new ServerConnectionThread(this)).start();
		
	}
	
	//TODO: broadcasting Messages should happen in this context rather than inside the individual Connection
	public void broadcastMessage(Message message) {
//		for(ClientConnection clientConnectionThread : _verteiler) {
//			
//		}
	}
	
	public void registerClientConnection(ClientConnection newClientConnection) {
		_verteiler.add(newClientConnection);
	}
	
	public Set<ClientConnection> getRegisteredConnections() {
		return Collections.unmodifiableSet(_verteiler);
	}
	
	public void unregisterNickname(String nickname) {
		_nicknames.remove(nickname);
	}

	public boolean registerNickname(String nickname) {
		return _nicknames.add(nickname);
	}
}
