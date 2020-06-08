package gov.nih.tbi.exceptions;

public class FilterEvaluatorException extends Exception {
	private static final long serialVersionUID = 5520423872099350802L;

	public FilterEvaluatorException(String msg) {
		super(msg);
	}

	public FilterEvaluatorException(String msg, Exception e) {
		super(msg, e);
	}
}
