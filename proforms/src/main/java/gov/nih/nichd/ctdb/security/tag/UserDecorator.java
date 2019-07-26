package gov.nih.nichd.ctdb.security.tag;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.security.domain.User;
import javax.servlet.jsp.JspException;

/**
 * UserDecorator enables a table to have a column with Action links (Edit/View/..). This
 * class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class UserDecorator extends ActionDecorator
{
    /**
     * Default Constructor
     */
    public UserDecorator()
    {
        // default constructor
        super();
    }

    /**
     * Retrieves the user actions allowed to be done on a Row of data.
     *
     * @return  String HTML string displaying the actions that can be made on a Row
     */
    public String getActions() throws JspException
    {
        User domainObject = (User) this.getObject();
        User currentUser = (User) this.getPageContext().getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
        String display;
        if( domainObject.isSysAdmin() )
        {
            display = "Yes";
        }
        else
        {
            display = "No";
        }

        String actionText;

        if(currentUser.getUsername().equalsIgnoreCase(domainObject.getUsername()))
        {
            actionText = "&nbsp;" + display + "&nbsp;";
        }
        else
        {
            String username = domainObject.getUsername();
            String displayName = domainObject.getFirstName() + " " + domainObject.getLastName();
            String root = this.getWebRoot();
            boolean newFlag = !domainObject.isSysAdmin();
            actionText = "&nbsp;<a href=\"javascript:confirmAdmin('" + root + "/admin/user.do?action=process_sysadmin&id=" + username + "&sysadmin=" + newFlag + "','" + displayName + "','" + newFlag + "');\">" + display + "</a>&nbsp;";
        }

        return actionText;
    }
}
