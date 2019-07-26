package gov.nih.tbi.commons.model.exceptions;


/**
 * Invalid file type Exception class for the NICHD CTDB file uploads.
 */
public class FileUploadInvalidTypeException extends Exception {
	private static final long serialVersionUID = 8256633119914792337L;

	/**
     * Default Constructor for the FileUploadInvalidTypeException Object
     */
    public FileUploadInvalidTypeException() {
        super();
    }

    /**
     * Overloaded Constructor for the FileUploadInvalidTypeException Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param message Exception Message
     */
    public FileUploadInvalidTypeException(String message) {
        super(message);
    }

    /**
     * Overloaded Constructor for the FileUploadInvalidTypeException Object. This
     * constructor allows a detailed message and the original exception
     * to be passed into the Exception object.
     *
     * @param message           Exception Message
     * @param originalException The original exception
     */
    public FileUploadInvalidTypeException(String message, Throwable originalException) {
        super(message, originalException);
    }
}
