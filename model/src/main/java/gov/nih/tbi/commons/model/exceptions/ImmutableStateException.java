package gov.nih.tbi.commons.model.exceptions;

/**
 * Created by amakar on 7/12/2017.
 */
public class ImmutableStateException extends RuntimeException {

    private static final long serialVersionUID = 2456110410142255382L;

    public ImmutableStateException() {
        super();
    }

    public ImmutableStateException(String message) {
        super(message);
    }
}
