package praktikum1.smtp_client;

import java.io.IOException;
import java.util.Properties;

public class MailFile {

	public static void main(String[] args){
		try {
			Properties properties = new Properties();
			properties.load(ClassLoader.getSystemResourceAsStream(args[1]));
			properties.setProperty("recipient", args[0]);
			new MailClient(properties);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}