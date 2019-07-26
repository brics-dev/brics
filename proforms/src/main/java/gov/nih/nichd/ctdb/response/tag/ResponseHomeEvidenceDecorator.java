package gov.nih.nichd.ctdb.response.tag;

import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.DataEntryWorkflowType;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.common.CtdbConstants;

import javax.servlet.jsp.JspException;

/**
 * Created by IntelliJ IDEA.
 * User: matt
 * Date: Jun 29, 2011
 * Time: 9:46:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class ResponseHomeEvidenceDecorator extends ResponseHomeDecorator {

        public String getActions() throws JspException
    {
        AdministeredForm domainObject = (AdministeredForm) this.getObject();
        int id = domainObject.getId();
        String root = this.getWebRoot();
        StringBuffer actions = new StringBuffer(100);
        actions.append("");
        //get User object from session
        User user = (User) this.getPageContext().getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
        actions.append(" <a href='Javascript:void(0);' onClick=\"parent.addFormEvidence('"+id+"');\">associate</a>");
        return actions.toString();
    }
}
