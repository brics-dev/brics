package gov.nih.cit.brics.file.exception;

import javax.servlet.ServletException;

public class HttpRangeOutOfBoundsException extends ServletException {
	private static final long serialVersionUID = 7071231515466788011L;

	public HttpRangeOutOfBoundsException() {
		super();
	}

	public HttpRangeOutOfBoundsException(String msg) {
		super(msg);
	}

	public HttpRangeOutOfBoundsException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public HttpRangeOutOfBoundsException(Throwable cause) {
		super(cause);
	}

}
