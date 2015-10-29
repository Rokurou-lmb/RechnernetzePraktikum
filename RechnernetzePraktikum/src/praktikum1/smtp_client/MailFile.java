package praktikum1.smtp_client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MailFile {

	public static void main(String[] args){
		try {
			Properties properties = new Properties();
			properties.load(ClassLoader.getSystemResourceAsStream(args[1]));
			properties.setProperty("recipient", args[0]);
			List<String> mailAttachments = new ArrayList<>();
			for(int i = 2;i < args.length; i++) {
				mailAttachments.add(args[i]);
			}
			new MailClient(properties, mailAttachments);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}