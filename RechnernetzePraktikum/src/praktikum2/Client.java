package praktikum2;

import java.net.InetAddress;

public class Client {
	private InetAddress _ipAddress;
	private int _portnumber;
	private String _nickname;
	
	public Client(String nickname, InetAddress ipAddress, int portnumber) {
		_ipAddress = ipAddress;
		_portnumber = portnumber;
		_nickname = nickname;
	}

	public InetAddress getIpAddress() {
		return _ipAddress;
	}

	public int getPortnumber() {
		return _portnumber;
	}

	public String getNickname() {
		return _nickname;
	}
}
