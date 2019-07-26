package gov.nih.tbi.repository.service.exception;

public class DataLoaderException extends Exception {

	private static final long serialVersionUID = -3794751182497461692L;

	public DataLoaderException(String msg, Exception e) {
		super(msg, e);
	}
}
