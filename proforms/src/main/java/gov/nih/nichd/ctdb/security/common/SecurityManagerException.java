package gov.nih.nichd.ctdb.security.common;

import gov.nih.nichd.ctdb.common.CtdbException;

/**
 * Exception to be thrown when there is a SecurityManager error
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class SecurityManagerException extends CtdbException
{
	private static final long serialVersionUID = 3937410309533583067L;

	/**
     * Default Constructor for the SecurityManagerException Object
     */
    public SecurityManagerException()
    {
        super();
    }

    /**
     * Overloaded Constructor for the SecurityManagerException Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param   message Exception Message
     */
    public SecurityManagerException(String message)
    {
        super(message);
    }

    /**
     * Overloaded Constructor for the SecurityManagerException Object. This
     * constructor allows a detailed message and the original exception
     * to be passed into the Exception object.
     *
     * @param   message Exception Message
     * @param   originalException The original exception
     *
     */
    public SecurityManagerException(String message, Throwable originalException)
    {
        super(message, originalException);
    }
}