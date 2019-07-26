
package gov.nih.tbi.dictionary.model;

public class MissingStructuralObjectException extends RuntimeException
{

    private static final long serialVersionUID = -9065028433127492097L;

    public MissingStructuralObjectException()
    {

        super();
    }

    public MissingStructuralObjectException(String message)
    {

        super(message);
    }
}
