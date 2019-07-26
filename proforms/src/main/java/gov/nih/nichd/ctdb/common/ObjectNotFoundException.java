package gov.nih.nichd.ctdb.common;


/**
 * Exception to be thrown when trying to retrieve an object from the system that does not exist
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ObjectNotFoundException extends CtdbException {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5436738718675701010L;

	/**
     * Default Constructor for the ObjectNotFoundException Object
     */
    public ObjectNotFoundException() {
        super();
    }

    /**
     * Overloaded Constructor for the ObjectNotFoundException Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param message Exception Message
     */
    public ObjectNotFoundException(String message) {
        super(message);
    }

    /**
     * Overloaded Constructor for the ObjectNotFoundException Object. This
     * constructor allows a detailed message and the original exception
     * to be passed into the Exception object.
     *
     * @param message           Exception Message
     * @param originalException The original exception
     */
    public ObjectNotFoundException(String message, Throwable originalException) {
        super(message, originalException);
    }
}