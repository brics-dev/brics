package gov.nih.nichd.ctdb.security.tag;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.tag.CtdbTag;
import gov.nih.nichd.ctdb.security.domain.Privilege;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;

import javax.servlet.jsp.JspException;

/**
 * This tag performs system privilege check against a user to determine if the user
 * can view the nested html/data for a protocol. It takes a String which contains the necessary privilege and
 * the protocol ID for the protocol that is in question.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class HasProtocolPrivilegeTag extends CtdbTag
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 2802845573375637771L;
	private String privilege = null;
    private String protocolId = null;
    private String privileges = null;

    /**
     * Process the start tag for this instance. This method is invoked by the JSP page implementation object
     *
     * @return 1 if the user has the privilege required<br>
     *         0 if the user does not have the privilege required
     * @throws JspException thrown if any error occurs while processing
     */
    public int doStartTag() throws JspException
    {
        //call method in super class to get session, request and response.
        doInitTag();
        
        boolean hasPriv = false;
        
        //get User object from session
        User user = (User) session.getAttribute("user");
        Protocol sessionProtocol = (Protocol)session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
        
    	if (sessionProtocol != null) {
    		this.protocolId = String.valueOf(sessionProtocol.getId());
    	}
        
        // correct for just a comma in privileges
        String[] privilegeArray = new String[0];
        if (!this.isStringNullEmpty(this.privileges)) {
        	privilegeArray = this.privileges.trim().split(",");
        	// reset back to empty if we had just a comma
        	if (privilegeArray[0].trim().isEmpty()) {
        		privilegeArray = new String[0];
        	}
        }
        
        // decide how to proceed
        // do we have a privilege in privilege?
        if (!this.isStringNullEmpty(this.privilege)) {
        	if (!this.isStringNullEmpty(this.protocolId)) {
        		// we have a listing in privilege and a protocol
        		hasPriv = this.checkPriv(user, this.privilege, Integer.parseInt(this.protocolId));
        	}
        	else {
        		// we have a listing in privilege but no protocol
        		hasPriv = this.checkPriv(user, this.privilege);
        	}
        }
        else if (privilegeArray.length != 0) {
        	if (!this.isStringNullEmpty(this.protocolId)) {
        		// we have a listing in privileges and a protocol
        		hasPriv = this.checkPriv(user, privilegeArray, Integer.parseInt(this.protocolId));
        	}
        	else {
        		// we have a listing in privileges but no protocol
        		hasPriv = this.checkPriv(user, privilegeArray);
        	}
        }
        else {
        	// in the case of not having a listing in privilege or privileges, let them through
        	hasPriv = true;
        }

        // convert and get out of here
        if( hasPriv ) {
            return 1;
        }
        else {
            return 0;
        }
    }
    
    /**
     * Checks if a string is empty in either the null or empty case
     * 
     * @param str the string to check
     * @return true if the string is null or empty; otherwise false
     */
    private boolean isStringNullEmpty(String str) {
    	if (str == null || str.trim().length() == 0) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * Checks if the user has the specified privilege on the specified protocol
     * 
     * @param user the user to check
     * @param priv the privilege to check
     * @param protocolId the protocol to compare against
     * @return true if the user has the specified privilege in the specified protocol; otherwise false
     */
    private boolean checkPriv(User user, String priv, int protocolId) {
    	Privilege privilegeToCheck = new Privilege();
        privilegeToCheck.setCode(priv);
        return user.hasPrivilege(privilegeToCheck, protocolId);
    }
    
    /**
     * Checks if the user has the specified privilege in the entire system
     * 
     * @param user user the user to check
     * @param priv the privilege to look for
     * @return true if the user has the specified privilege; otherwise false
     */
    private boolean checkPriv(User user, String priv) {
    	Privilege privilegeToCheck = new Privilege();
        privilegeToCheck.setCode(priv);
        return user.hasPrivilege(privilegeToCheck);
    }
    
    /**
     * Checks if the user has one of the specified privileges on the specified protocol
     * 
     * @param user the user to check
     * @param privs the list of privileges to look for
     * @param protocolId the protocol in which to look for those privileges
     * @return true if the user has one of the specified privileges in the specified protocol; otherwise false
     */
    private boolean checkPriv(User user, String[] privs, int protocolId) {
    	return user.hasAnyPrivilege(privs, protocolId);
    }
    
    /**
     * Checks if the user has one of the specified privileges in the entire system
     * 
     * @param user the user to check
     * @param privs the list of privileges to look for
     * @return true if hte user has one of the specified privileges in the entire system; otherwise false
     */
    private boolean checkPriv(User user, String[] privs) {
    	return user.hasAnyPrivilege(privs);
    }
    
    
    

    /**
     *  Reset all variables to the default value
     *
     */

    public void release()
    {
        super.release();
        this.privilege = null;
        this.protocolId = null;
    }

    /**
     * Set user's privilege required to view nested data
     *
     * @param privilege The user's privilege required to view nested data
     *
     */
    public void setPrivilege(String privilege)
    {
        this.privilege = privilege;
    }

    /**
     * Get user's privilege required to see nested data
     *
     * @return User's privilege required to see nested data
     *
     */
    public String getPrivilege()
    {
        return this.privilege;
    }

    /**
     * Set protocol ID that is current
     *
     * @param privilege The current protocol ID
     *
     */
    public void setProtocolId(String protocolId)
    {
        this.protocolId = protocolId;
    }

    /**
     * Get protocol ID for the current protocol
     *
     * @return current protocol ID
     *
     */
    public String getProtocolId()
    {
        return this.protocolId;
    }

	public String getPrivileges() {
		return privileges;
	}

	public void setPrivileges(String privileges) {
		this.privileges = privileges;
	}

}
