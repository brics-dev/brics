package gov.nih.cit.brics.file.exception;

public class MultiTenantSetupException extends RuntimeException {
	private static final long serialVersionUID = -3240644647303705461L;

	public MultiTenantSetupException(String message) {
		super(message);
	}

	public MultiTenantSetupException(String message, Throwable cause) {
		super(message, cause);
	}
}
