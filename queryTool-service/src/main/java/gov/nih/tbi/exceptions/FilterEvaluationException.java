package gov.nih.tbi.exceptions;

public class FilterEvaluationException extends RuntimeException {

	private static final long serialVersionUID = -5449987717834102753L;

	public FilterEvaluationException(String msg) {
		super(msg);
	}

	public FilterEvaluationException(String msg, Exception e) {
		super(msg, e);
	}
}
