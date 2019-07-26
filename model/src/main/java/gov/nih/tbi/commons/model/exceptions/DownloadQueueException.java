package gov.nih.tbi.commons.model.exceptions;

public class DownloadQueueException extends NullPointerException {
	private static final long serialVersionUID = 3287212456840501462L;

	public DownloadQueueException(String message) {
		super(message);
	}
}
