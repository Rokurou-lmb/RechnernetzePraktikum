package praktikum2;

import java.util.concurrent.TimeUnit;

public class Message implements Comparable<Message>{
	private long _timestamp; //TODO: Save Timestamp as String instead of long
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
	
	public String getTimeStampAsString() {//TODO: doesn't seem to work as intended
		String timestamp = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(_timestamp),
				TimeUnit.MILLISECONDS.toMinutes(_timestamp) % TimeUnit.HOURS.toMinutes(1),
				TimeUnit.MILLISECONDS.toSeconds(_timestamp) % TimeUnit.MINUTES.toSeconds(1));
		return timestamp;
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
