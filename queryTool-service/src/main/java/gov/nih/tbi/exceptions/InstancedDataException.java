package gov.nih.tbi.exceptions;

public class InstancedDataException extends RuntimeException
{
	private static final long serialVersionUID = -828734418280139913L;

	public InstancedDataException(String msg) {
		super(msg);
	}

	public InstancedDataException(String msg, Throwable t) {
		super(msg, t);
    }
}
