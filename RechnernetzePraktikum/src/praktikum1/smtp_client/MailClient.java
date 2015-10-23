package praktikum1.smtp_client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.Properties;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class MailClient {
	private Socket _socket;
	private InputStream _in;
	private BufferedReader _reader;
	private OutputStream _out;
	private PrintWriter _writer;
	private Properties _properties;
	private final static boolean loging = true;
	
	public MailClient(Properties properties) throws IOException {
		try {
			_properties = properties;
			
			String remoteHostString = properties.getProperty("hostname");
			int remotePort = Integer.parseInt(properties.getProperty("portnumber"));
			InetAddress remoteHost = InetAddress.getByName(remoteHostString);
			_socket = (remotePort == 465)
				? SSLSocketFactory.getDefault().createSocket(remoteHost, remotePort)
				: SocketFactory.getDefault().createSocket(remoteHost, remotePort);
			
			_out = _socket.getOutputStream();
			_in = _socket.getInputStream();
			_writer = new PrintWriter(_out, true);
			_reader = new BufferedReader(new InputStreamReader(_in));
			sendMail();
			
				
				
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	private void sendMail() throws UnknownHostException {

		String localHostString = _socket.getLocalAddress().getHostName();
		int localPort = Integer.parseInt(_properties.getProperty("portnumber"));
		InetAddress localHost = InetAddress.getByName(localHostString);
			


		String username = _properties.getProperty("username");
		String password = _properties.getProperty("password");
		
		String base64EncodedUsername = new String(Base64.getEncoder().encode(username.getBytes()));
		String base64EncodedPassword = new String(Base64.getEncoder().encode(password.getBytes()));
		
		sendMessage("EHLO " + localHostString);
		receiveMessage();
		sendMessage("AUTH LOGIN");
		receiveMessage();
		sendMessage(base64EncodedUsername);
		receiveMessage();
		sendMessage(base64EncodedPassword);
		receiveMessage();
		sendMessage("MAIL FROM: " + _properties.getProperty("mailaddress"));
		receiveMessage();
		sendMessage("RCPT TO: " + _properties.getProperty("recipient"));
		receiveMessage();
		sendMessage("DATA");
		receiveMessage();
		sendMessage("Subject: Testemail");
		receiveMessage();
		sendMessage("Hallo, was läuft?");
		receiveMessage();
		sendMessage(".");
		receiveMessage();
		sendMessage("QUIT");
		receiveMessage();
		
	}
	
	private void sendMessage(String message) {
		_writer.println(message);
		String logmessage = "[SEND]    : " + message;
		System.err.println(logmessage);
		if(loging)
			log(logmessage);
//		try {
//			Thread.currentThread().sleep(100); //TODO fix race condition and remove this.
//		} catch (InterruptedException e) {}
	}
	
	private void receiveMessage() {
			try {
				while(_reader.ready()) {
					String message = _reader.readLine();
					String logmessage = "[RECEIVE] : " + message;
					System.err.println(logmessage);
					if(loging)
						log(logmessage);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	private void log(String message) {
		
	}
}
