package gov.nih.tbi.dictionary.model;

/**
 * Created by amakar on 12/20/2016.
 */
public class MissingDataException extends RuntimeException {

    private static final long serialVersionUID = -837191022017791579L;

    public MissingDataException()
    {
        super();
    }

    public MissingDataException(String message) {
        super(message);
    }
}
