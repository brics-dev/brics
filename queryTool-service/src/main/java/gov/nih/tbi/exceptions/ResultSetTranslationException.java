package gov.nih.tbi.exceptions;

public class ResultSetTranslationException extends Exception {
    private static final long serialVersionUID = -5778281379199467509L;

	public ResultSetTranslationException(String message) {
		super(message);
	}

	public ResultSetTranslationException(String message, Throwable t) {
		super(message, t);
    }
}
