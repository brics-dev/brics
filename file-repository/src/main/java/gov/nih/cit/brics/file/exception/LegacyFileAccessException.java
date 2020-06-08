package gov.nih.cit.brics.file.exception;

import javax.servlet.ServletException;

public class LegacyFileAccessException extends ServletException {
	private static final long serialVersionUID = 3644435881920980510L;

	public LegacyFileAccessException(String message) {
		super(message);
	}

	public LegacyFileAccessException(String message, Throwable rootCause) {
		super(message, rootCause);
	}
}
