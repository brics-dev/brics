package gov.nih.nichd.ctdb.response.tag;


import javax.servlet.jsp.JspException;

import org.apache.struts2.ServletActionContext;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.tag.ActionIdtDecorator;
import gov.nih.nichd.ctdb.response.domain.ViewAuditorComment;
import gov.nih.nichd.ctdb.security.common.SecurityConstants;
import gov.nih.nichd.ctdb.security.domain.Privilege;
import gov.nih.nichd.ctdb.security.domain.User;


/**
 * ViewAuditorCommentIdtDecorator enables a table to have a column with Action links This class works with the
 * <code>display</code> tag library.
 *
 * @author wangla
 * 
 */
public class ViewAuditorCommentIdtDecorator extends ActionIdtDecorator
{
    public ViewAuditorCommentIdtDecorator()
    {
        super();
    }
    
    
    public String getCountLk() throws JspException
	{
    	ViewAuditorComment  vac = (ViewAuditorComment) this.getObject();
    	
        User currentUser = (User) ServletActionContext.getRequest().getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
        Privilege rspdAdtCmmtPrivlege = new Privilege(SecurityConstants.RESPOND_TO_AUDIT_COMMENTS_PRIV);
    	
    	String count = vac.getCount();
        if(count!=null && !count.trim().isEmpty()) {
        	String countTm = count.trim();
    		int selected_Form_Ids = vac.getAdministeredformId(); 
    		//link to edit form page if this eform is not CAT form && current user is sysAdmin or has respond to auditor comments privilege
        	if (!vac.getIsCat() && currentUser != null 
        			&& (currentUser.isSysAdmin() || currentUser.hasPrivilege(rspdAdtCmmtPrivlege)) ) {
    			count = "<a href=\"" + this.getWebRoot()
					+ "/response/dataCollection.action?action=editForm&mode=formPatient&aformId="
					+ selected_Form_Ids + "&editUser=1\">" + countTm + "</a>";
        	} else {
        		//link to audit comment page if this eform is a CAT form or current user is not sysAdmin and does not have respond to auditor comments privilege
        		count = "<a href=\"" + this.getWebRoot()
					+ "/response/dataCollection.action?action=auditComments&mode=formPatient&aformId="
					+ selected_Form_Ids + "&audit=true\">" + countTm + "</a>";
        	}
        }
        return count;
    }
}
