package gov.nih.nichd.ctdb.common;


/**
 * Exception class for the NICHD CTDB file uploads,
 * primarly intended to catch invalid file types and file does not exist exceptions.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class FileUploadException extends CtdbException {
	private static final long serialVersionUID = 3079764903504577349L;
	private int type;

    /**
     * Default Constructor for the FileUploadException Object
     */
    public FileUploadException() {
        super();
    }

    /**
     * Overloaded Constructor for the FileUploadException Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param message Exception Message
     */
    public FileUploadException(String message) {
        super(message);
    }

    /**
     * Overloaded Constructor for the FileUploadException Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param message Exception Message
     * @param type    type of file upload exception
     */
    public FileUploadException(String message, int type) {
        super(message);
        this.type = type;
    }

    /**
     * Overloaded Constructor for the FileUploadException Object. This
     * constructor allows a detailed message and the original exception
     * to be passed into the Exception object.
     *
     * @param message           Exception Message
     * @param originalException The original exception
     */
    public FileUploadException(String message, Throwable originalException) {
        super(message, originalException);
    }

    /**
     * Get the type of file upload exception.
     *
     * @return int exception type.
     */
    public int getType() {
        return type;
    }

    /**
     * Set the type of file upload exception.
     *
     * @param type exception type.
     */
    public void setType(int type) {
        this.type = type;
    }

}
