package gov.nih.tbi.dictionary.exceptions;

public class DictionaryVersioningException extends Exception {

	private static final long serialVersionUID = -8389090103791482276L;

	public DictionaryVersioningException() {
		super();
	}

	public DictionaryVersioningException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DictionaryVersioningException(String message, Throwable cause) {
		super(message, cause);
	}

	public DictionaryVersioningException(String message) {
		super(message);
	}

	public DictionaryVersioningException(Throwable cause) {
		super(cause);
	}
}
