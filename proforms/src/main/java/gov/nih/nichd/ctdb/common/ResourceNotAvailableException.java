package gov.nih.nichd.ctdb.common;


/**
 * Exception to be thrown when trying to use database table object that is not available or locked by others.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ResourceNotAvailableException extends CtdbException {
    /**
     * Default Constructor for the ResourceNotAvailableException Object
     */
    public ResourceNotAvailableException() {
        super();
    }

    /**
     * Overloaded Constructor for the ResourceNotAvailableException Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param message Exception Message
     */
    public ResourceNotAvailableException(String message) {
        super(message);
    }

    /**
     * Overloaded Constructor for the ResourceNotAvailableException Object. This
     * constructor allows a detailed message and the original exception
     * to be passed into the Exception object.
     *
     * @param message           Exception Message
     * @param originalException The original exception
     */
    public ResourceNotAvailableException(String message, Throwable originalException) {
        super(message, originalException);
    }
}