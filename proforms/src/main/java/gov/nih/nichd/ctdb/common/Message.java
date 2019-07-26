package gov.nih.nichd.ctdb.common;

import java.io.Serializable;

import gov.nih.nichd.ctdb.util.common.MessageHandler;

public class Message implements Serializable {

	private static final long serialVersionUID = 6496517184205361224L;
	protected String message;
	protected String type;
	
	public Message(String msg, String type) {
		this.message = msg;
		this.type = type;
	}
	
	public Message() {
		message = null;
		type = MessageHandler.MESSAGE_TYPE_MESSAGE;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * Compares this message against another message
	 * 
	 * @param msg the Message to compare against this one
	 * @return boolean true if the Messages match; otherwise false
	 */
	public boolean equals(Message msg) {
		return type.equals(msg.type) && message.equalsIgnoreCase(msg.getMessage());
	}
}
