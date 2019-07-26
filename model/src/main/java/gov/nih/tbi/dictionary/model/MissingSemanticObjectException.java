package gov.nih.tbi.dictionary.model;


public class MissingSemanticObjectException extends RuntimeException
{
    private static final long serialVersionUID = -6326526668322773561L;
    
    public MissingSemanticObjectException()
    {
        super();
    }
    
    public MissingSemanticObjectException(String message)
    {
        super(message);
    }
}
