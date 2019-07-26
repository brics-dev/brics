package gov.nih.nichd.ctdb.common;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Jun 15, 2007
 * Time: 11:45:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class CacheDirtyException extends CacheException {
    /**
  * Default Constructor for the CacheException Object
  */
 public CacheDirtyException() {
     super();
 }

 /**
  * Overloaded Constructor for the CacheException Object. This
  * constructor allows a detailed message to be passed into the Exception object.
  *
  * @param message Exception Message
  */
 public CacheDirtyException(String message) {
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
 public CacheDirtyException(String message, Throwable originalException) {
     super(message, originalException);
 }




}
