package praktikum1.smtp_client;

import java.io.BufferedReader;
import java.io.FileReader;
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

import praktikum1.MailProperties;

public class MailClient {
	private Socket _socket;
	private InputStream _in;
	private BufferedReader _reader;
	private OutputStream _out;
	private PrintWriter _writer;
	private Properties _properties;
	private final static boolean loging = true;
	
	private final static String EHLO = "EHLO ";
	private final static String AUTH_LOGIN = "AUTH LOGIN";
	private final static String MAIL_FROM = "MAIL FROM: ";
	private final static String RECIPIENT_TO = "RCPT TO: ";
	private final static String DATA = "DATA";
	private final static String QUIT = "QUIT";
	private final static String MESSAGE_TERMINATOR = ".";
	
	
	
	public MailClient(Properties properties) throws IOException {
		try {
			_properties = properties;
			
			String remoteHostString = properties.getProperty(MailProperties.HOSTNAME);
			int remotePort = Integer.parseInt(properties.getProperty(MailProperties.PORTNUMBER));
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
		int localPort = Integer.parseInt(_properties.getProperty(MailProperties.PORTNUMBER));
		InetAddress localHost = InetAddress.getByName(localHostString);
		
		String username = _properties.getProperty(MailProperties.USERNAME);
		String password = _properties.getProperty(MailProperties.PASSWORD);
		
		String base64EncodedUsername = new String(Base64.getEncoder().encode(username.getBytes()));
		String base64EncodedPassword = new String(Base64.getEncoder().encode(password.getBytes()));
		
		sendMessage(EHLO + localHostString);
		receiveMessage();
		sendMessage(AUTH_LOGIN);
		receiveMessage();
		sendMessage(base64EncodedUsername);
		receiveMessage();
		sendMessage(base64EncodedPassword);
		receiveMessage();
		sendMessage(MAIL_FROM + _properties.getProperty(MailProperties.MAILADDRESS));
		receiveMessage();
		sendMessage(RECIPIENT_TO + _properties.getProperty(MailProperties.RECIPIENT));
		receiveMessage();
		sendMessage(DATA);
		receiveMessage();
		sendMailBody(_properties.getProperty(MailProperties.MAILBODY));
		receiveMessage();
		sendMessage(QUIT);
		receiveMessage();
	}
	
	private void sendMailBody(String mailFile) {
		try(BufferedReader mailBodyReader = new BufferedReader(new FileReader(mailFile))){
			String currentLine = "";
			while((currentLine = mailBodyReader.readLine()) != null ) {
				sendMessage(currentLine);
			}
			sendMessage(MESSAGE_TERMINATOR);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendMessage(String message) {
		_writer.println(message);
		String logmessage = "[SEND]    : " + message;
		System.err.println(logmessage);
		if(loging)
			log(logmessage);
	}
	
	private void receiveMessage() {
			try {
				do {
					String message = _reader.readLine();
					String logmessage = "[RECEIVE] : " + message;
					System.err.println(logmessage);
					if(loging)
						log(logmessage);
				} while(_reader.ready());
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	private void log(String message) {
		//TODO: implement this.
	}
}
