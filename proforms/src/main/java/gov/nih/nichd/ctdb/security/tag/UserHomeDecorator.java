package gov.nih.nichd.ctdb.security.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.security.domain.User;

import javax.servlet.jsp.JspException;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: May 2, 2006
 * Time: 9:38:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class UserHomeDecorator extends ActionDecorator {

    public String getActions () throws JspException {
        User domainObject = (User) this.getObject();
        String root = this.getWebRoot();
        return "<a href='"+root+"/admin/manageUsers.do?action=edit_form&id="+domainObject.getId()+"'>edit</a>";
    }

    public String getClassification () throws JspException {
        User domainObject = (User) this.getObject();
        if (domainObject.isStaff()) {
            return "site";
        } else {
            return "system";
        }
    }
    
    public String getCheckbox() throws JspException {
    	User domainObject = (User) this.getObject();
    	return "<input type=\"checkbox\" name=\"userId\" value=\"" + String.valueOf(domainObject.getId()) + "\" />";
    }
}
