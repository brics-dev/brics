package gov.nih.nichd.ctdb.security.common;

import gov.nih.nichd.ctdb.common.CtdbException;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Jan 17, 2008
 * Time: 8:49:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class InvalidAssociationException extends CtdbException {
	private static final long serialVersionUID = 1040883846876905304L;

		/**
     * Default Constructor for the DuplicateUserException Object
     */
    public InvalidAssociationException()
    {
        super();
    }

    /**
     * Overloaded Constructor for the DuplicateUserException Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param   message Exception Message
     */
    public InvalidAssociationException(String message)
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
    public InvalidAssociationException(String message, Throwable originalException)
    {
        super(message, originalException);
    }
}
