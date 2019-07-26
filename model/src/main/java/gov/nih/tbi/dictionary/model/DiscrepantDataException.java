package gov.nih.tbi.dictionary.model;

/**
 * Created by amakar on 12/20/2016.
 */
public class DiscrepantDataException extends RuntimeException {

    private static final long serialVersionUID = 8899385833183486780L;

    public DiscrepantDataException()
    {
        super();
    }

    public DiscrepantDataException(String message) {
        super(message);
    }
}

