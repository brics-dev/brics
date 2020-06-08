package gov.nih.cit.brics.file.exception;

import javax.servlet.ServletException;

public class InvalidJWTException extends ServletException {
	private static final long serialVersionUID = -8458125419518144973L;

	public InvalidJWTException(String message) {
		super(message);
	}

	public InvalidJWTException(String message, Throwable cause) {
		super(message, cause);
	}

}
