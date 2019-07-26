package gov.nih.nichd.ctdb.security.domain;

import java.util.Date;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;

/**
 * The AccessAuditLog domain object. This object contains all information used
 * for auditing a login/logout attempt in the system.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class AccessAuditLog extends CtdbDomainObject
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -6344926298699049178L;
	private String username;
    private String attemptType;
    private boolean success;
    private Date accessDate;
    private String failureReason = null;

    private String remoteAddress;    //request.getRemoteAddr();
    private String xForwardedFor; // request header x forwarded for
    private String remoteHost;   // request.getRemoteHost
    private String requestHeaders; 
    
    

    /**
     *  AccessAuditLog LOGIN Attempt Type static string.
     */
    public static final String LOGIN_ATTEMPT_TYPE = "LOGIN";

    /**
     *  AccessAuditLog LOGOUT Attempt Type static string.
     */
    public static final String LOGOUT_ATTEMPT_TYPE = "LOGOUT";

    /**
     * Default Constructor for the User Domain Object
     */
    public AccessAuditLog()
    {
        // default constructor
    }

    /**
     * Gets the attempted username
     *
     * @return  String username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Sets the attempted username
     *
     * @param   username The username
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * Gets the attempted access type
     *
     * @return  String attempted access type, either LOGIN or LOGOUT
     */
    public String getAttemptType()
    {
        return attemptType;
    }

    /**
     * Sets the attempted access type, either LOGIN or LOGOUT
     *
     * @param   attemptType The attempted type.
     */
    public void setAttemptType(String attemptType)
    {
        this.attemptType = attemptType;
    }

    /**
     * Gets the attempted access success flag. True if the attempt was successful, false otherwise
     *
     * @return  boolean True if the attempt was successful, false otherwise.
     */
    public boolean isSuccess()
    {
        return success;
    }

    /**
     * Sets the attempted success flag, either true if the attempt was successful, false otherwise.
     *
     * @param   success The attempted success flag.
     */
    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    /**
     * Gets the attempted access date
     *
     * @return  Date The attempted access date
     */
    public Date getAccessDate()
    {
        return accessDate;
    }

    /**
     * Sets the attempted access date.
     *
     * @param   accessDate The attempted access date.
     */
    public void setAccessDate(Date accessDate)
    {
        this.accessDate = accessDate;
    }

    /**
     * Gets the attempted access reason for failure
     *
     * @return  String The attempted access reason for failure if it exists.
     */
    public String getFailureReason()
    {
        return failureReason;
    }

    /**
     * Sets the attempted access reason for failure.
     *
     * @param   failureReason The attempted access reason for failure.
     */
    public void setFailureReason(String failureReason)
    {
        this.failureReason = failureReason;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getXForwardedFor() {
        return xForwardedFor;
    }

    public void setXForwardedFor(String xForwardedFor) {
        this.xForwardedFor = xForwardedFor;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(String requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    /**
     * This method allows the transformation of a AccessAuditLog into an XML Document.
     * If no implementation is available at this time,
     * an UnsupportedOperationException will be thrown.
     *
     * @return      XML Document
     * @throws   UnsupportedOperationException is thrown if this method
     *              is currently unsupported and not implemented.
     */
    public Document toXML() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("No Implementation at this time for the method toXML() in AccessAuditLog.");
    }
}