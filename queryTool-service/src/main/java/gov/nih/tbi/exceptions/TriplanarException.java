package gov.nih.tbi.exceptions;

public class TriplanarException extends RuntimeException {

	private static final long serialVersionUID = 6948482212013763090L;

	public TriplanarException() {
        super();
    }

	public TriplanarException(String message) {
		super(message);
	}

	public TriplanarException(String message, Throwable t) {
		super(message, t);
	}
}
