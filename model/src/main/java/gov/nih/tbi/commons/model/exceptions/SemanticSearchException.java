
package gov.nih.tbi.commons.model.exceptions;

public class SemanticSearchException extends RuntimeException
{

    private static final long serialVersionUID = 2345774102021232684L;

    public SemanticSearchException()
    {

        super();
    }

    public SemanticSearchException(String message, Throwable cause)
    {

        super(message, cause);
    }

    public SemanticSearchException(String message)
    {

        super(message);
    }

    public SemanticSearchException(Throwable cause)
    {

        super(cause);
    }

}
