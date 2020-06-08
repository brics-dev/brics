package gov.nih.cit.brics.file.exception;

import javax.servlet.ServletException;

public class JWTRenewalException extends ServletException {
	private static final long serialVersionUID = -7612782639231996413L;

	public JWTRenewalException(String msg) {
		super(msg);
	}

	public JWTRenewalException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
