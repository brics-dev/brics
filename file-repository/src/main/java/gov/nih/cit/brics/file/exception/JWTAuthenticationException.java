package gov.nih.cit.brics.file.exception;

import org.springframework.security.core.AuthenticationException;

public class JWTAuthenticationException extends AuthenticationException {
	private static final long serialVersionUID = -8267635742572698931L;

	public JWTAuthenticationException(String msg) {
		super(msg);
	}
	
	public JWTAuthenticationException(String msg, Throwable t) {
		super(msg, t);
	}

}
