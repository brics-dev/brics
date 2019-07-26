
package gov.nih.tbi.repository.xml.exception;

public class XmlValidationException extends Exception {

	private static final long serialVersionUID = 8907555388629591201L;

	public XmlValidationException() {

		super();
	}

	public XmlValidationException(String message, Throwable cause) {

		super(message, cause);
	}

	public XmlValidationException(String message) {

		super(message);
	}

	public XmlValidationException(Throwable cause) {

		super(cause);
	}

}