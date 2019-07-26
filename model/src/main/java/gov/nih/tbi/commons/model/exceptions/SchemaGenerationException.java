
package gov.nih.tbi.commons.model.exceptions;

public class SchemaGenerationException extends Exception
{

    private static final long serialVersionUID = 2345774102021232684L;

    public SchemaGenerationException()
    {

        super();
    }

    public SchemaGenerationException(String message, Throwable cause)
    {

        super(message, cause);
    }

    public SchemaGenerationException(String message)
    {

        super(message);
    }

    public SchemaGenerationException(Throwable cause)
    {

        super(cause);
    }

}
