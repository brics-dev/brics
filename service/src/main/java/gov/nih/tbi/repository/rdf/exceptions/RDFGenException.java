package gov.nih.tbi.repository.rdf.exceptions;

public class RDFGenException extends RuntimeException {

	private static final long serialVersionUID = -2066951876152503699L;

	public RDFGenException() {
		super();
	}

	public RDFGenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RDFGenException(String message, Throwable cause) {
		super(message, cause);
	}

	public RDFGenException(String message) {
		super(message);
	}

	public RDFGenException(Throwable cause) {
		super(cause);
	}

}
