package gov.nih.tbi.commons.model.exceptions;

public class DoiWsValidationException extends Exception {
	private static final long serialVersionUID = 3675831050286691660L;

	public DoiWsValidationException() {
		super();
	}

	public DoiWsValidationException(String message) {
		super(message);
	}

	public DoiWsValidationException(Throwable cause) {
		super(cause);
	}

	public DoiWsValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public DoiWsValidationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
