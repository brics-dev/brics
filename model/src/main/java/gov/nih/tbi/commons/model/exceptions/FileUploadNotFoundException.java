package gov.nih.tbi.commons.model.exceptions;

public class FileUploadNotFoundException extends Exception {

	private static final long serialVersionUID = 8495273076898259416L;
	
	private String fileName = "";

    /**
     * Default Constructor for the FileUploadNotFoundException Object
     */
    public FileUploadNotFoundException() {
        super();
    }

    /**
     * Overloaded Constructor for the FileUploadNotFoundException Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param message Exception Message
     */
    public FileUploadNotFoundException(String message) {
        super(message);
    }

    /**
     * Overloaded Constructor for the FileUploadNotFoundException Object. This
     * constructor allows a detailed message and the original exception
     * to be passed into the Exception object.
     *
     * @param message           Exception Message
     * @param originalException The original exception
     */
    public FileUploadNotFoundException(String message, Throwable originalException) {
        super(message, originalException);
    }

    /**
     * Gets the filename not found
     *
     * @return The filename not found
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the filename not found
     *
     * @param fileName The filename not found
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
