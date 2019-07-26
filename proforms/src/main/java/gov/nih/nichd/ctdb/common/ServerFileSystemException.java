package gov.nih.nichd.ctdb.common;

/**
 * Any errors that will occur when interacting with files on the server's file system.
 * 
 * @author CIT
 *
 */
public class ServerFileSystemException extends Exception
{
	private static final long serialVersionUID = -3767879127858811183L;

	/**
	 * 
	 */
	public ServerFileSystemException() {
		
	}

	/**
	 * @param arg0
	 */
	public ServerFileSystemException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ServerFileSystemException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
