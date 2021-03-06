package praktikum1.smtp_client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
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
	private final boolean _logging;
	private final String _logfile;
	
	private final static String EHLO = "EHLO ";
	private final static String AUTH_LOGIN = "AUTH LOGIN";
	private final static String MAIL_FROM = "MAIL FROM: ";
	private final static String RECIPIENT_TO = "RCPT TO: ";
	private final static String DATA = "DATA";
	private final static String QUIT = "QUIT";
	private final static String MESSAGE_TERMINATOR = ".";
	
	
	
	public MailClient(Properties properties, List<String> mailAttachments) throws IOException {
		_properties = properties;
		_logging = Boolean.parseBoolean(_properties.getProperty(MailProperties.LOGGING));
		_logfile = _properties.getProperty(MailProperties.LOGFILE);
		try {
			
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
			sendMail(mailAttachments);
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	private void sendMail(List<String> mailAttachments) throws UnknownHostException {

		String localHostString = _socket.getLocalAddress().getHostName();
		
		String username = _properties.getProperty(MailProperties.USERNAME);
		String password = _properties.getProperty(MailProperties.PASSWORD);
		
		String base64EncodedUsername = new String(Base64.getEncoder().encode(username.getBytes()));
		String base64EncodedPassword = new String(Base64.getEncoder().encode(password.getBytes()));

		receiveMessage();
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
		sendMailBody(_properties.getProperty(MailProperties.MAILBODY), mailAttachments);
		receiveMessage();
		sendMessage(QUIT);
		receiveMessage();
	}
	
	private void sendMailBody(String mailFile, List<String> mailAttachments) {
		try(BufferedReader mailBodyReader = new BufferedReader(new FileReader(mailFile))){
			String currentLine = "";
			while((currentLine = mailBodyReader.readLine()) != null ) {
				if(currentLine.startsWith(MESSAGE_TERMINATOR)) //Transparency
					currentLine = MESSAGE_TERMINATOR + currentLine;
				sendMessage(currentLine);
			}
			sendMailAttachment(mailAttachments);
			sendMessage(MESSAGE_TERMINATOR);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendMailAttachment(List<String> mailAttachments) {
		for (String mailAttatchmentFile : mailAttachments) {
			sendMessage("--=__=");
			try(BufferedReader mailAttatchmentReader = new BufferedReader(new FileReader(mailAttatchmentFile))) {
				sendMessage("Content-Transfer-Encoding: base64");
				sendMessage("Content-Type: image/png");
				sendMessage("Content-Disposition: attachment; filename=3.png");
				sendMessage("");
				byte[] byteFile = Files.readAllBytes(Paths.get(mailAttatchmentFile));
				String encodedString = Base64.getMimeEncoder().encodeToString(byteFile);
				sendMessage(encodedString);
			} catch(IOException e) {
				
			}
		}
	}
	
	private void sendMessage(String message) {
		_writer.println(message);
		String logmessage = "[SEND]    : " + message;
		System.err.println(logmessage);
		if(_logging)
			log(logmessage);
	}
	
	private void receiveMessage() {
			try {
				do {
					String message = _reader.readLine();
					String logmessage = "[RECEIVE] : " + message;
					System.err.println(logmessage);
					if(_logging)
						log(logmessage);
				} while(_reader.ready());
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	private void log(String message) {
		try (FileWriter logWriter = new FileWriter(_logfile, true)){
			logWriter.write(message + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
