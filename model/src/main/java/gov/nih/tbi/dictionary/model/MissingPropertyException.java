package gov.nih.tbi.dictionary.model;

/**
 * Created by amakar on 7/4/2017.
 */
public class MissingPropertyException extends RuntimeException {


    private static final long serialVersionUID = -2225238860390928120L;

    public MissingPropertyException()
    {
        super();
    }

    public MissingPropertyException(String message) {
        super(message);
    }
}
