package gov.nih.nichd.ctdb.security.common;

import gov.nih.nichd.ctdb.common.ObjectNotFoundException;

/**
 * Exception to be thrown when a user can not be found in the system
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class UserNotFoundException extends ObjectNotFoundException
{
	private static final long serialVersionUID = -4726642708693930701L;

	/**
     * Default Constructor for the UserNotFoundException Object
     */
    public UserNotFoundException()
    {
        super();
    }

    /**
     * Overloaded Constructor for the UserNotFoundException Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param   message Exception Message
     */
    public UserNotFoundException(String message)
    {
        super(message);
    }

    /**
     * Overloaded Constructor for the UserNotFoundException Object. This
     * constructor allows a detailed message and the original exception
     * to be passed into the Exception object.
     *
     * @param   message Exception Message
     * @param   originalException The original exception
     *
     */
    public UserNotFoundException(String message, Throwable originalException)
    {
        super(message, originalException);
    }
}
