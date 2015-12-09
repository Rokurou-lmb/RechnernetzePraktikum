package praktikum2;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class Message implements Comparable<Message>{
	private long _timestamp;
	private String _messageString;
	private String _sender;
	
	public Message(String message, String sender) {
		this(System.currentTimeMillis(), message, sender);
	}
	
	public Message(long timestamp, String messageString, String sender) {
		_timestamp = timestamp;
		_messageString = messageString;
		_sender = sender;
	}
	
	/**
	 * Returns This {@code Message}s timestamp in a readable format with the format: "HH:mm:ss"
	 * @return String representing this {@code Message}s timestamp.
	 */
	public String getTimeStampAsString() {
		Date timestampDate = new Date(_timestamp);
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		return dateFormat.format(timestampDate);
	}
	
	/**
	 * @return This {@code Message}s timestamp as a {@code long}, representing milliseconds since UNIX-Timedings
	 */
	public long getTimeStamp() {
		return _timestamp;
	}
	
	/**
	 * @return This {@code Message}s messagedata as a {@code String}
	 */
	public String getMessageString() {
		return _messageString;
	}
	
	/**
	 * @return This {@code Message}s senders nickname as a {@code String}
	 */
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
