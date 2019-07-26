package gov.nih.tbi.guid.ws.exception;

/**
 * Represents a problem logging in to the GUID server. Normally this is a response to a non-200 response from the GUID
 * server during an Auth call but could be used elsewhere if the response comes back as a 403 or similar.
 * 
 * @author jpark1
 *
 */
public class AuthenticationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 232887423005072399L;

	public AuthenticationException() {

	}

	public AuthenticationException(String arg0) {
		super(arg0);
	}

	public AuthenticationException(Throwable arg0) {
		super(arg0);
	}

	public AuthenticationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AuthenticationException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
