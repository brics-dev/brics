package gov.nih.tbi.guid.exception;

/**
 * Represents when a JWT (anywhere) is invalid for some reason. Reason should be specified in the message.
 * 
 * @author jpark1
 *
 */
public class InvalidJwtException extends Exception {
	private static final long serialVersionUID = 4573292678040442215L;

	public InvalidJwtException(String message) {
		super(message);
	}

}
