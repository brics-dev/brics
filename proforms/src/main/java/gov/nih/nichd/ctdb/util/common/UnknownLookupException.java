package gov.nih.nichd.ctdb.util.common;

import gov.nih.nichd.ctdb.common.CtdbException;

/**
 * UnknownLookupException is thrown when a lookup type is trying to be accessed that does not exist.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class UnknownLookupException extends CtdbException
{
	private static final long serialVersionUID = -1477687124011346423L;

	/**
     * Default Constructor for the UnknownLookupException Object
     */
    public UnknownLookupException()
    {
        super();
    }

    /**
     * Overloaded Constructor for the UnknownLookupException Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param   message Exception Message
     */
    public UnknownLookupException(String message)
    {
        super(message);
    }

    /**
     * Overloaded Constructor for the UnknownLookupException Object. This
     * constructor allows a detailed message and the original exception
     * to be passed into the Exception object.
     *
     * @param   message Exception Message
     * @param   originalException The original exception
     *
     */
    public UnknownLookupException(String message, Throwable originalException)
    {
        super(message, originalException);
    }
}
