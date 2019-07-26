package gov.nih.nichd.ctdb.security.tag;

import gov.nih.nichd.ctdb.common.tag.CtdbTag;
import gov.nih.nichd.ctdb.security.domain.User;

import javax.servlet.jsp.JspException;

/**
 * This tag performs system privilege check against a user to determine if the user
 * can view the nested html/data for a ctdb object. It takes a Object(such as Form domain object)
 * which contains the necessary privilege and the protocol ID for the protocol that is in question.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class HasObjectPrivilegeTag extends CtdbTag
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 259546146676945907L;
	private String privileges = null;
    private Object ctdbObject = null;

    /**
     * Process the start tag for this instance. This method is invoked by the JSP page implementation object
     *
     * @return SKIP_BODY
     * @throws JspException thrown if any error occurs while processing
     */
    public int doStartTag() throws JspException
    {
        try
        {
            //call method in super class to get session, request and response.
            doInitTag();

            String accessDenied = this.getWebRoot() + "/common/accessDenied.jsp";
            String[] privilegeArray = getPrivileges().split(",");

            //get User object from session
            User user = (User) session.getAttribute("user");

            boolean hasPriv = user.hasPrivilege(privilegeArray, this.ctdbObject);
            if( !hasPriv )
            {
                session.setAttribute("accessdenied", "1");
                response.sendRedirect(accessDenied);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return SKIP_BODY;
    }

    /**
     *  Reset all variables to the default value
     *
     */

    public void release()
    {
        super.release();
        this.privileges = null;
        this.ctdbObject = null;
    }

    /**
     * Set user's privilege required to view nested data
     *
     * @param privileges The user's privileges required to view nested data
     *
     */
    public void setPrivileges(String privileges)
    {
        this.privileges = privileges;
    }

    /**
     * Get user's privilege required to see nested data
     *
     * @return User's privilege required to see nested data
     *
     */
    public String getPrivileges()
    {
        return this.privileges;
    }

    /**
     * Set ctdb object
     *
     * @param o The ctdb object
     *
     */
    public void setCtdbObject(Object o)
    {
        this.ctdbObject = o;
    }

    /**
     * Get CtdbObject
     *
     * @return Object in CTDB
     *
     */
    public Object getCtdbObject()
    {
        return this.ctdbObject;
    }


}
