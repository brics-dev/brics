package gov.nih.nichd.ctdb.response.common;

import gov.nih.nichd.ctdb.common.DuplicateObjectException;

/**
 * Exception to be thrown when trying to insert administered form that already has two data entries exist
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class DuplicateDataEntriesException extends DuplicateObjectException
{
	private static final long serialVersionUID = -7192172974403721407L;

	/**
     * Default Constructor for the DuplicateDataEntriesException Object
     */
    public DuplicateDataEntriesException()
    {
        super();
    }

    /**
     * Overloaded Constructor for the DuplicateDataEntriesException Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param   message Exception Message
     */
    public DuplicateDataEntriesException(String message)
    {
        super(message);
    }

    /**
     * Overloaded Constructor for the DuplicateDataEntriesException Object. This
     * constructor allows a detailed message and the original exception
     * to be passed into the Exception object.
     *
     * @param   message Exception Message
     * @param   originalException The original exception
     *
     */
    public DuplicateDataEntriesException(String message, Throwable originalException)
    {
        super(message, originalException);
    }
}