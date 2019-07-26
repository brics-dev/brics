
package gov.nih.tbi.commons.exceptions;

/**
 * Custom Exception for User Permission handling
 * 
 * @author Francis Chen
 * 
 */
public class PermissionException extends Exception
{

    private static final long serialVersionUID = -6863704248381348823L;

    String exceptionMessage;

    public PermissionException(String e)
    {

        super(e);
        exceptionMessage = e;
    }

    public PermissionException(PermissionException e)
    {

        super(e);
        exceptionMessage = e.getExceptionMessage();
    }

    public String getExceptionMessage()
    {

        return exceptionMessage;
    }
}
