package gov.nih.brics.downloadtool.exception;

import org.springframework.security.core.AuthenticationException;

public class JWTAuthenticationException extends AuthenticationException {

	private static final long serialVersionUID = 8700150500735875876L;

	public JWTAuthenticationException(String msg) {
		super(msg);
	}
	
	public JWTAuthenticationException(String msg, Throwable t) {
		super(msg, t);
	}

}
