package gov.nih.tbi.exceptions;

import java.io.IOException;

public class CSVGenerationException extends IOException {

	private static final long serialVersionUID = 5528259376158948378L;

	public CSVGenerationException() {

		super();
	}

	public CSVGenerationException(String message, Throwable cause) {

		super(message, cause);
	}

	public CSVGenerationException(String message) {

		super(message);
	}

	public CSVGenerationException(Throwable cause) {

		super(cause);
	}

}
