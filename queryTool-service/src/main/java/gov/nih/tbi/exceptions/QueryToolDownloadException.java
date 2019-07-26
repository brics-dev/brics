package gov.nih.tbi.exceptions;

public class QueryToolDownloadException extends RuntimeException {
	private static final long serialVersionUID = 8633404794315427530L;

	public QueryToolDownloadException() {

		super();
	}

	public QueryToolDownloadException(String message) {
		super(message);
	}
}
