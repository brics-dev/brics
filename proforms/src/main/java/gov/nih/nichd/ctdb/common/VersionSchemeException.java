package gov.nih.nichd.ctdb.common;


/**
 * Exception to be thrown when trying to create a Version with a malformed String reprentation.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class VersionSchemeException extends CtdbException {
    /**
     * Default Constructor for the VersionSchemeObject
     */
    public VersionSchemeException() {
        super();
    }

    /**
     * Overloaded Constructor for the VersionSchemeObject Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param message Exception Message
     */
    public VersionSchemeException(String message) {
        super(message);
    }

    /**
     * Overloaded Constructor for the VersionSchemeObject. This
     * constructor allows a detailed message and the original exception
     * to be passed into the Exception object.
     *
     * @param message           Exception Message
     * @param originalException The original exception
     */
    public VersionSchemeException(String message, Throwable originalException) {
        super(message, originalException);
    }
}