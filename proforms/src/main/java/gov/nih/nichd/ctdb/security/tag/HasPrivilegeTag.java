package gov.nih.nichd.ctdb.security.tag;

import gov.nih.nichd.ctdb.common.tag.CtdbTag;
import gov.nih.nichd.ctdb.security.domain.Privilege;
import gov.nih.nichd.ctdb.security.domain.User;

import javax.servlet.jsp.JspException;

/**
 * This tag performs system privilege check against a user to determine if the user
 * can view the nested html/data. It takes a String which contains the necessary privilege.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class HasPrivilegeTag extends CtdbTag
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1699670541074884390L;
	private String privilege = null;
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

        //get User object from session
        User user = (User) session.getAttribute("user");

        boolean hasPriv = false;

        if(this.privilege != null && !this.privilege.trim().equalsIgnoreCase(""))
        {
            Privilege privilegeToCheck = new Privilege();
            privilegeToCheck.setCode(this.privilege);
            hasPriv = user.hasPrivilege(privilegeToCheck);
        }
        else {
            if(this.privileges != null && !this.privileges.trim().equalsIgnoreCase("")) {
                String[] privilegeArray = this.privileges.split(",");
                if(!privilegeArray[0].equalsIgnoreCase(""))
                {
                    if(user.hasAnyPrivilege(privilegeArray))
                    {
                       hasPriv = true;
                    }
                }
            }
            else {
            	// josh park update - correct if wrong
            	// in the event that neither privilege NOR privileges are set, let 'em through
            	hasPriv = true;
            }
        }

        if(hasPriv)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    /**
     *  Reset all variables to the default value
     */
    public void release()
    {
        super.release();
        this.privilege = null;
        this.privileges = null;
    }

    /**
     * Set user's privilege required to view nested data
     *
     * @param privilege The user's privilege required to view nested data
     */
    public void setPrivilege(String privilege)
    {
        this.privilege = privilege;
    }

    /**
     * Get user's privilege required to see nested data
     *
     * @return User's privilege required to see nested data
     */
    public String getPrivilege()
    {
        return this.privilege;
    }

    /**
     * Gets user's privileges required to see nested data
     *
     * @return User's privilege required to see nested data
     */
    public String getPrivileges()
    {
        return privileges;
    }

    /**
     * Sets user's privileges required to see nested data
     *
     * @param privileges User's privileges required to see nested data
     */
    public void setPrivileges(String privileges)
    {
        this.privileges = privileges;
    }
}
