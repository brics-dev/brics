package gov.nih.nichd.ctdb.common.tag;

import javax.servlet.jsp.JspException;

import org.apache.struts2.ServletActionContext;

import gov.nih.nichd.ctdb.security.domain.Privilege;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.idt.ws.IdtDecorator;

/**
 * ActionDecorator enables a table to have a column with Action links (Edit/View/..). This
 * class works with the <code>display</code> tag library. <code>getWebRoot</code> will only work within a
 * web application using Struts with ApplicationResources properties defined in the configuration.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ActionIdtDecorator extends IdtDecorator {
    /**
     * Default Constructor
     */
    public ActionIdtDecorator() {
        // default constructor
        super();
    }

    /**
     * Retrieves the user actions allowed to be done on a Row of data.
     *
     * @return String HTML string displaying the actions that can be made on a Row
     */
    public String getActions() throws JspException {
        return "edit";
    }

    /**
     * Gets the application webroot from ApplicationResources
     *
     * @return Application Web Root (http://.....)
     */
    public String getWebRoot() throws JspException {
    	 return SysPropUtil.getProperty("app.webroot");
    }
    
    /**
     * Checks a user's privilege based on the code passed in
     *
     * @param privCode The privilege to check for
     * @return True if the user has the privilege, false otherwise
     */
    protected boolean checkPrivilege(String privCode) {

        User user = (User) ServletActionContext.getRequest().getAttribute("user");

        Privilege privilegeToCheck = new Privilege();
        privilegeToCheck.setCode(privCode);
        boolean hasPriv = user.hasPrivilege(privilegeToCheck);
        return hasPriv;
    }

    /**
     * Checks a user's privilege based on the code passed in and the current protocol
     *
     * @param privCode   The privilege to check for
     * @param protocolId The protocol ID that the user is in
     * @return True if the user has the privilege, false otherwise
     */
    protected boolean checkPrivilege(String privCode, int protocolId) {
    	
    	User user = (User) ServletActionContext.getRequest().getAttribute("user");

        Privilege privilegeToCheck = new Privilege();
        privilegeToCheck.setCode(privCode);
        boolean hasPriv = user.hasPrivilege(privilegeToCheck, protocolId);
        return hasPriv;
    }
}
