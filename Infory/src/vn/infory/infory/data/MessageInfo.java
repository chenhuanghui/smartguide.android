package vn.infory.infory.data;


public class MessageInfo {
	private int idSender;
	private String sender;
	private count count;
	private int status;
	private newestMessage newestMessage;
	
	public static class count {
		private int[] number;
		private String[] string;
		public count() {
			
		}
		public int[] getNumber() {
			return number;
		}
		public void setNumber(int[] number) {
			this.number = number;
		}
		public String[] getString() {
			return string;
		}
		public void setString(String[] string) {
			this.string = string;
		}	
	}
	
	public static class newestMessage extends Message {

		public newestMessage() {
			
		}
	}

	public MessageInfo() {
		
	}

	public int getIdSender() {
		return idSender;
	}

	public void setIdSender(int idSender) {
		this.idSender = idSender;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public count getCount() {
		return count;
	}

	public void setCount(count count) {
		this.count = count;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public newestMessage getNewestMessage() {
		return newestMessage;
	}

	public void setNewestMessage(newestMessage newestMessage) {
		this.newestMessage = newestMessage;
	}
}
