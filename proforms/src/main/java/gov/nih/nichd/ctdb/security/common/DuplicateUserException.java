package gov.nih.nichd.ctdb.security.common;

import gov.nih.nichd.ctdb.common.DuplicateObjectException;

/**
 * Exception to be thrown when trying to insert a user that already exists
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class DuplicateUserException extends DuplicateObjectException
{
	private static final long serialVersionUID = 1106588691212561911L;

	/**
     * Default Constructor for the DuplicateUserException Object
     */
    public DuplicateUserException()
    {
        super();
    }

    /**
     * Overloaded Constructor for the DuplicateUserException Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param   message Exception Message
     */
    public DuplicateUserException(String message)
    {
        super(message);
    }

    /**
     * Overloaded Constructor for the DuplicateUserException Object. This
     * constructor allows a detailed message and the original exception
     * to be passed into the Exception object.
     *
     * @param   message Exception Message
     * @param   originalException The original exception
     *
     */
    public DuplicateUserException(String message, Throwable originalException)
    {
        super(message, originalException);
    }
}