/**
 * 
 */
package gov.nih.nichd.ctdb.common;

/**
 * @author jeng
 *
 */
public class BinderNotFoundException extends Exception {
	private static final long serialVersionUID = 1851533170754643559L;

	/**
	 * 
	 */
	public BinderNotFoundException() {
	}

	/**
	 * @param message
	 */
	public BinderNotFoundException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param originalException
	 */
	public BinderNotFoundException(String message, Throwable originalException) {
		super(message, originalException);
	}
}
