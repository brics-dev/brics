package gov.nih.tbi.commons.model.exceptions;

public class TransformationException extends RuntimeException {
	private static final long serialVersionUID = 5413840313951263194L;
	
	public TransformationException() {
		super();
	}
	
	public TransformationException(String message) {
		super(message);
	}
	
	public TransformationException(String message, Throwable t) {
		super(message, t);
	}
}
