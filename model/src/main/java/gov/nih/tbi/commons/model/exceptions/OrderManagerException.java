package gov.nih.tbi.commons.model.exceptions;


public class OrderManagerException extends Exception
{
    private static final long serialVersionUID = -9178974468139591049L;
    
    public OrderManagerException()
    {
        super();
    }
    
    public OrderManagerException(String message)
    {
        super(message);
    }
}
