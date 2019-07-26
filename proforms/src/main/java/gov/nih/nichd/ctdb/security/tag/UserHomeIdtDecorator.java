package gov.nih.nichd.ctdb.security.tag;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.tag.ActionIdtDecorator;
import gov.nih.nichd.ctdb.security.domain.User;


public class UserHomeIdtDecorator extends ActionIdtDecorator {

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
    
}
