package gov.nih.nichd.ctdb.common;


/**
 * Exception to be thrown when trying to insert a duplicate object in the system
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class DuplicateObjectException extends CtdbException {
	private static final long serialVersionUID = -4382537176527847305L;

	/**
     * Default Constructor for the DuplicateObjectException Object
     */
    public DuplicateObjectException() {
        super();
    }

    /**
     * Overloaded Constructor for the DuplicateObjectException Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param message Exception Message
     */
    public DuplicateObjectException(String message) {
        super(message);
    }

    /**
     * Overloaded Constructor for the DuplicateObjectException Object. This
     * constructor allows a detailed message and the original exception
     * to be passed into the Exception object.
     *
     * @param message           Exception Message
     * @param originalException The original exception
     */
    public DuplicateObjectException(String message, Throwable originalException) {
        super(message, originalException);
    }
}