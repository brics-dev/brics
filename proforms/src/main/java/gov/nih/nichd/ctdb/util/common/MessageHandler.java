package gov.nih.nichd.ctdb.util.common;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;


public class MessageHandler {
	private ArrayList<Message> messages;
	
	public static final String MESSAGE_TYPE_ERROR = "message_error";
	public static final String MESSAGE_TYPE_MESSAGE = "message_message";
	public static final String MESSAGE_TYPE_WARNING = "message_warning";
	public static final String MESSAGE_TYPE_INFO = "message_info";
	public static final String SESSION_KEY_MESSAGEHANDLER = "messageHandler";
	
	/**
	 * Default constructor that creates a empty list of messages.
	 */
	public MessageHandler() {
		messages = new ArrayList<Message>();
	}
	
	/**
	 * A constructor that initializes the internal message list with the version stored in session.
	 * 
	 * @param request - The HTTP request object
	 */
	public MessageHandler(HttpServletRequest request) {
		this();
		load(request);
	}
	
	/**
	 * Resets the internal message list with the given array list
	 * 
	 * @param msgs - A list of messages
	 */
	public void init(ArrayList<Message> msgs) {
		messages = msgs;
	}
	
	/**
	 * Translates a list of ActionMessage objects (contained in a ActionMessages container) to the local
	 * message format. The message translation will convert each ActionMessage into its string version
	 * with any replacements ({num}) made. Then the translated message will be converted to the local Message
	 * object and finally stored in the internal messages list.
	 * 
	 * @param messageList - A list of ActionMessage formated messages
	 */
//	@SuppressWarnings("unchecked")
//	public void init(ActionMessages messageList) {
//		ActionMessage aMessage = null;
//		MessageResources mr = MessageResources.getMessageResources("ApplicationResources");
//		String msgText = "";
//		
//		for ( Iterator<ActionMessage> it = (Iterator<ActionMessage>) messageList.get(); it.hasNext(); ) {
//			aMessage = it.next();
//			msgText = mr.getMessage(aMessage.getKey(), aMessage.getValues());
//			messages.add(new Message(msgText, MessageHandler.MESSAGE_TYPE_MESSAGE));
//		}
//	}
	
	/**
	 * Translates a list of ActionMessage objects (contained in a ActionErrors container) to the local
	 * message format. The message translation will convert each ActionMessage into its string version
	 * with any replacements ({num}) made. Then the translated message will be converted to the local Message
	 * object and finally stored in the internal messages list.
	 * 
	 * @param errorList
	 */
	//@SuppressWarnings("unchecked")
//	public void init(ActionErrors errorList) {
//		ActionMessage aMessage = null;
//		MessageResources mr = MessageResources.getMessageResources("ApplicationResources");
//		String msgText = "";
//		
//		for ( Iterator<ActionMessage> it = (Iterator<ActionMessage>) errorList.get(); it.hasNext(); ) {
//			aMessage = it.next();
//			msgText = mr.getMessage(aMessage.getKey(), aMessage.getValues());
//			messages.add(new Message(msgText, MessageHandler.MESSAGE_TYPE_ERROR));
//		}
//	}
	
	/**
	 * Add a message to the internal message list with just a String and message type.
	 * 
	 * @param message - The text of the message that will be displayed
	 * @param type - The type of the message, which is either a regular message, an
	 * 				 error message, or a message handler message
	 */
	public void addMessage(String message, String type) {
		Message temp = new Message(message, type);
		messages.add(temp);
	}
	
	/**
	 * Add a message to the internal message list in its native Message object.
	 * 
	 * @param message - The message as the lists native Message object
	 */
	public void addMessage(Message message) {
		messages.add(message);
	}
	
	/**
	 * Converts an ActionMessage object to the native Message object, and adds it to the internal list.
	 * During the conversion, a String representation is extracted from the ActionMessage object with
	 * any replacements ({num}) made.
	 * 
	 * @param aMessage - The message as an ActionMessage object
	 * @param type - Either a regular message, error message, or MessageHandler message
	 */
//	public void addMessage(ActionMessage aMessage, String type) {
//		MessageResources mr = MessageResources.getMessageResources("ApplicationResources");
//		String msgText = mr.getMessage(aMessage.getKey(), aMessage.getValues());
//		
//		messages.add(new Message(msgText, type));
//	}
	
	/**
	 * Removes the message from the internal list that matches the native Message object that is passed in.
	 * 
	 * @param message - The message to find and remove
	 */
	public void removeMessage(Message message) {
		messages.remove(message);
	}
	
	/**
	 * Removes a message from the internal list that is located at the given array index.
	 * 
	 * @param index - The array index of the message to be deleted
	 */
	public void removeMessage(int index) {
		messages.remove(index);
	}
	
	/**
	 * Removes all messages from the list
	 */
	public void clearAll() {
		messages.clear();
	}
	
	/**
	 * Retrieves a list of messages of the given type from the internal list.
	 * 
	 * @param type - Either a regular message, error message, or MessageHandler message
	 * @return	A list of messages of the given type or an empty list if no messages can
	 * 			be found.
	 */
	protected ArrayList<Message> getAllOfType(String type) {
		ArrayList<Message> tmpMessages = new ArrayList<Message>();
		if (messages != null) {
			for (Message message : messages) {
				if (message.getType().equals(type)) {
					tmpMessages.add(message);
				}
			}
		}
		return tmpMessages;
	}
	
	/**
	 * Retrieves a list of info messages from the internal list.
	 * 
	 * @return	A list of info messages or an empty list if no error messages can be found.
	 */
	public ArrayList<Message> getAllInfoMessages() {
		return getAllOfType(MessageHandler.MESSAGE_TYPE_INFO);
	}
	
	/**
	 * Retrieves a list of warning messages from the internal list.
	 * 
	 * @return	A list of warning messages or an empty list if no error messages can be found.
	 */
	public ArrayList<Message> getAllWarningMessages() {
		return getAllOfType(MessageHandler.MESSAGE_TYPE_WARNING);
	}
	
	/**
	 * Retrieves a list of error messages from the internal list.
	 * 
	 * @return	A list of error messages or an empty list if no error messages can be found.
	 */
	public ArrayList<Message> getAllErrorMessages() {
		return getAllOfType(MessageHandler.MESSAGE_TYPE_ERROR);
	}
	
	/**
	 * Retrieves a list of regular or success messages from the internal list.
	 * 
	 * @return	A list of regular messages or an empty list if no messages can be found.
	 */
	public ArrayList<Message> getAllMessageMessages() {
		return getAllOfType(MessageHandler.MESSAGE_TYPE_MESSAGE);
	}
	
	/**
	 * Saves the internal list of messages to the session to be displayed on a JSP page.
	 * 
	 * @param request - The HTTP request object
	 */
	public void save(HttpServletRequest request) {
		request.getSession().setAttribute(SESSION_KEY_MESSAGEHANDLER, messages);
	}
	
	/**
	 * Resets the internal message list with the one stored in the session.
	 * 
	 * @param request - The HTTP request object
	 */
	@SuppressWarnings("unchecked")
	public void load(HttpServletRequest request) {
		ArrayList<Message> tmpMessages = (ArrayList<Message>) request.getSession().getAttribute(SESSION_KEY_MESSAGEHANDLER);
		if (tmpMessages != null) {
			messages = tmpMessages;
		}
	}
	
	/**
	 * Checks if the internal list is empty.
	 * 
	 * @return	True if the list is empty or false otherwise.
	 */
	public boolean isEmpty() {
		return messages.isEmpty();
	}
}
