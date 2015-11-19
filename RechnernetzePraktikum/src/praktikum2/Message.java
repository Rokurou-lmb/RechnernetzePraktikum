package praktikum2;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class Message implements Comparable<Message>{
	private long _timestamp;
	private String _message;
	private String _sender;
	
	public Message(String message, String sender) {
		this(System.currentTimeMillis(), message, sender);
	}
	
	public Message(long timestamp, String message, String sender) {
		_timestamp = timestamp;
		_message = message;
		_sender = sender;
	}
	
	public String getTimeStampAsString() {//TODO test this
		Date timestampDate = new Date(_timestamp);
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		return dateFormat.format(timestampDate);
	}
	
	public long getTimeStamp() {
		return _timestamp;
	}
	
	public String getMessageData() {
		return _message;
	}
	public String getSender() {
		return _sender;
	}

	@Override
	public int compareTo(Message otherMessage) {
		if(this._timestamp > otherMessage.getTimeStamp()) {
			return 1;
		} else if(this._timestamp < otherMessage.getTimeStamp()) {
			return -1;
		} else {
			return 0;
		}
	}
}
