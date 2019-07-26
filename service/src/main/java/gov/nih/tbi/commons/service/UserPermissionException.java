
package gov.nih.tbi.commons.service;

/**
 * Custom Exception for User Permission handling
 * 
 * @author Francis Chen
 * 
 */
public class UserPermissionException extends Exception
{

    private static final long serialVersionUID = -6863704248381348823L;

    String exceptionMessage;

    public UserPermissionException(String e)
    {

        super(e);
        exceptionMessage = e;
    }

    public UserPermissionException(UserPermissionException e)
    {

        super(e);
        exceptionMessage = e.getExceptionMessage();
    }

    public String getExceptionMessage()
    {

        return exceptionMessage;
    }
}
