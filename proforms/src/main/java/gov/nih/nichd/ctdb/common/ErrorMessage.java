package gov.nih.nichd.ctdb.common;


public class ErrorMessage
{
	
	public enum ChangeType
	{
		CREATE, UPDATE, DELETE
	}
	
	private long objId;
	private String objName;
	private ChangeType objChangeType;
	private Throwable exception;
	private String message;
	
	public ErrorMessage()
	{
		objId = Integer.MIN_VALUE;
		objName = "";
		objChangeType = null;
		exception = null;
		message = null;
	}

	/**
	 * @return the objId
	 */
	public long getObjId() {
		return objId;
	}

	/**
	 * @param objId the objId to set
	 */
	public void setObjId(long objId) {
		this.objId = objId;
	}

	/**
	 * @return the exception
	 */
	public Throwable getException() {
		return exception;
	}

	/**
	 * @param exception the exception to set
	 */
	public void setException(Throwable exception) {
		this.exception = exception;
	}

	/**
	 * @return the errorMessage
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setMessage(String errorMessage) {
		this.message = errorMessage;
	}

	/**
	 * @return the objName
	 */
	public String getObjName() {
		return objName;
	}

	/**
	 * @param objName the objName to set
	 */
	public void setObjName(String objName) {
		this.objName = objName;
	}

	/**
	 * @return the objChangeType
	 */
	public ChangeType getObjChangeType() {
		return objChangeType;
	}

	/**
	 * @param objChangeType the objChangeType to set
	 */
	public void setObjChangeType(ChangeType objChangeType) {
		this.objChangeType = objChangeType;
	}

}
