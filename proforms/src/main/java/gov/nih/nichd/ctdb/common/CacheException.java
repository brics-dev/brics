package gov.nih.nichd.ctdb.common;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Jun 15, 2007
 * Time: 11:44:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class CacheException extends Exception {

        /**
     * Default Constructor for the CacheException Object
     */
    public CacheException() {
        super();
    }

    /**
     * Overloaded Constructor for the CacheException Object. This
     * constructor allows a detailed message to be passed into the Exception object.
     *
     * @param message Exception Message
     */
    public CacheException(String message) {
        super(message);
    }

    /**
     * Overloaded Constructor for the CacheException Object. This
     * constructor allows a detailed message and the original exception
     * to be passed into the Exception object.
     *
     * @param message           Exception Message
     * @param originalException The original exception
     */
    public CacheException(String message, Throwable originalException) {
        super(message, originalException);
    }



    
}
