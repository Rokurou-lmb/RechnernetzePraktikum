package praktikum2.chat_server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChatServer implements Runnable{
	Map<Object, String> _nicknameMapping;
	Set<Object> _verteiler;
	
	public ChatServer() {
		_nicknameMapping = new HashMap<>();
		_verteiler = new HashSet<>();
	}

	@Override
	public void run() {
		while(true) {
			
		}
	}
}
