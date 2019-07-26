package gov.nih.nichd.ctdb.common;

/**
 * Exception class for the NICHD CTDB Assembler interface and children
 * primarly intended to catch cast class exceptions.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class AssemblerException extends CtdbException {
	private static final long serialVersionUID = -4181764314067408492L;

	/**
     * Creates a new instance of AssemblerException
     */
    public AssemblerException() {
        super();
    }

    /**
     * Overloaded Constructor for the AssemblerException Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param message Exception Message
     */
    public AssemblerException(String message) {
        super(message);
    }

    /**
     * Overloaded Constructor for the AssemblerException Object. This
     * constructor allows a detailed message and the original exception
     * to be passed into the Exception object.
     *
     * @param message           Exception Message
     * @param originalException The original exception
     */
    public AssemblerException(String message, Throwable originalException) {
        super(message, originalException);
    }
}
