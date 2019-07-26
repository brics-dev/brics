package gov.nih.nichd.ctdb.common;

/**
 * Base Exception class for the NICHD CTDB application
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class CtdbException extends Exception {
	private static final long serialVersionUID = -7993194872844358541L;

	/**
     * Default Constructor for the CtdbException Object
     */
    public CtdbException() {
        super();
    }

    /**
     * Overloaded Constructor for the CtdbException Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param message Exception Message
     */
    public CtdbException(String message) {
        super(message);
    }

    /**
     * Overloaded Constructor for the CtdbException Object. This
     * constructor allows a detailed message and the original exception
     * to be passed into the Exception object.
     *
     * @param message           Exception Message
     * @param originalException The original exception
     */
    public CtdbException(String message, Throwable originalException) {
        super(message, originalException);
    }
}
