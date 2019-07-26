package gov.nih.nichd.ctdb.security.common;

import gov.nih.nichd.ctdb.common.CtdbException;

/**
 * Exception to be thrown when a user cannot be authenticated to the system.
 *
 * @author Booz Allen Hamilton
 * @version 1.0      
 */
public class AuthenticationFailedException extends CtdbException
{
	private static final long serialVersionUID = -5766804043044838060L;

	/**
     * Default Constructor for the AuthenticationFailedException Object
     */
    public AuthenticationFailedException()
    {
        super();
    }

    /**
     * Overloaded Constructor for the AuthenticationFailedException Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param   message Exception Message
     */
    public AuthenticationFailedException(String message)
    {
        super(message);
    }

    /**
     * Overloaded Constructor for the AuthenticationFailedException Object. This
     * constructor allows a detailed message and the original exception
     * to be passed into the Exception object.
     *
     * @param   message Exception Message
     * @param   originalException The original exception
     *
     */
    public AuthenticationFailedException(String message, Throwable originalException)
    {
        super(message, originalException);
    }
}
