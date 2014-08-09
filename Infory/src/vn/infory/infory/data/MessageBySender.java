package vn.infory.infory.data;

import java.util.List;

public class MessageBySender {
	
	private String sender;
	private List<messages> messages;
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public List<messages> getMessages() {
		return messages;
	}
	public void setMessages(List<messages> messages) {
		this.messages = messages;
	}
	public MessageBySender() {
		
	}
	
	public static class messages extends Message {

		public messages() {
			
		}
		
	}
	

}
