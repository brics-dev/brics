package gov.nih.nichd.ctdb.common;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Oct 7, 2005
 * Time: 11:05:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class InvalidRemovalException extends CtdbException {
	private static final long serialVersionUID = -8703569636590498628L;
	
	private String offendingRowIndicator;

    /**
     * Creates a new instance of AssemblerException
     */
    public InvalidRemovalException() {
        super();
    }

    /**
     * Overloaded Constructor for the AssemblerException Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param message Exception Message
     */
    public InvalidRemovalException(String message) {
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
    public InvalidRemovalException(String message, Throwable originalException) {
        super(message, originalException);
    }

    public String getOffendingRowIndicator() {
        return offendingRowIndicator;
    }

    public void setOffendingRowIndicator(String offendingRowIndicator) {
        this.offendingRowIndicator = offendingRowIndicator;
    }
}
