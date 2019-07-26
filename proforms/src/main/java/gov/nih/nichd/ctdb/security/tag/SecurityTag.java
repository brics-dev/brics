package gov.nih.nichd.ctdb.security.tag;

import gov.nih.nichd.ctdb.common.tag.CtdbTag;
import gov.nih.nichd.ctdb.security.domain.User;

import javax.servlet.jsp.JspException;

/**
 * This tag performs system privileges check against a user. It takes a String
 * which contains the user's privilegies. The String uses "," to separate privileges.
 * If the user doesn't have the privilege, the tag redirects the user
 * to an unauthorized JSP. If the user has any of the privileges, the tag
 * does no action. This tag must be used at the beginning of the JSP in order to redirect
 * the user to other page.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class SecurityTag extends CtdbTag
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 6633376586263167316L;
	private String privileges = null;

    /**
     * Process the start tag for this instance. This method is invoked by the JSP page implementation object
     *
     * @return SKIP_BODY
     * @throws JspException
     */
    public int doStartTag() throws JspException
    {
        try
        {
            //call method in super class to get session, request and response.
            doInitTag();

            //get User object from session
            User u = (User) session.getAttribute("user");

            String accessDenied = this.getWebRoot() + "/common/accessDenied.jsp";
            String[] privilegeArray = getPrivileges().split(",");

            // user does not exist, throw to access denied
            // or a user has been inadvertantly created via useBean
            // so check for a username.  cr24666
            if(u == null || u.getUsername() == null)
            {
                session.setAttribute("accessdenied", "1");
                response.sendRedirect(accessDenied);
            }

            else if(u != null && privilegeArray.length != 0)
            {
                if(!privilegeArray[0].equalsIgnoreCase(""))
                {
                    if(!u.hasAnyPrivilege(privilegeArray))
                    {
                        session.setAttribute("accessdenied", "1");
                        response.sendRedirect(accessDenied);
                    }
                }
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
    }

    /**
     * Set user's privileges
     *
     * @param   privileges  user's privileges
     *
     */
    public void setPrivileges(String privileges)
    {
        this.privileges = privileges;
    }

    /**
     * Get user's privileges
     *
     * @return  String  user's privileges which uses "," as a separator
     *
     */
    public String getPrivileges()
    {
        return this.privileges;
    }
}