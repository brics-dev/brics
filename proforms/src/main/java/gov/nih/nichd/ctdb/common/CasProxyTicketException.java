package gov.nih.nichd.ctdb.common;

public class CasProxyTicketException extends Exception {
	private static final long serialVersionUID = -580411001651777850L;

	public CasProxyTicketException() {
		super();
	}

	public CasProxyTicketException(String message) {
		super(message);
	}

	public CasProxyTicketException(Throwable cause) {
		super(cause);
	}

	public CasProxyTicketException(String message, Throwable cause) {
		super(message, cause);
	}

	public CasProxyTicketException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
