package vn.infory.infory.data;

import java.util.ArrayList;

public class MessageBySender {

	private String sender;
	private ArrayList<messages> messages;

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public ArrayList<messages> getMessages() {
		return messages;
	}

	public void setMessages(ArrayList<messages> messages) {
		this.messages = messages;
	}

	public MessageBySender() {

	}

	public static class messages extends Message {
		
		public messages() {

		}
	}

}
