package gov.nih.nichd.ctdb.response.common;

import java.util.ArrayList;
import java.util.List;

import gov.nih.nichd.ctdb.common.CtdbException;

/**
 * InvalidInputException handles all input exceptions for the system during data entry.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class InvalidInputException extends CtdbException
{
	private static final long serialVersionUID = -1895201505370189668L;
	private List<String> errors;

    /**
     * Default Constructor for the InvalidInputException Object
     */
    public InvalidInputException()
    {
        super();
        errors = new ArrayList<String>();
    }

    /**
     * Overloaded Constructor for the InvalidInputException Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param   message Exception Message
     */
    public InvalidInputException(String message)
    {
        super(message);
    }

    /**
     * Overloaded Constructor for the InvalidInputException Object. This
     * constructor allows a detailed message and the original exception
     * to be passed into the Exception object.
     *
     * @param   message Exception Message
     * @param   originalException The original exception
     *
     */
    public InvalidInputException(String message, Throwable originalException)
    {
        super(message, originalException);
    }

    /**
     * Returns the list of errors that caused this exception
     *
     * @return List of generated errors
     */
    public List<String> getErrors()
    {
        return errors;
    }

    /**
     * Sets the list of errors that caused this exception
     *
     * @param errors List of generated errors
     */
    public void setErrors(List<String> errors)
    {
    	if ( errors != null )
    	{
    		this.errors = errors;
    	}
    	else
    	{
    		this.errors = new ArrayList<String>();
    	}
    }
}
