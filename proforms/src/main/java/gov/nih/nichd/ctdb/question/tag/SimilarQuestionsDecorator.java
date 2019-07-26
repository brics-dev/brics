package gov.nih.nichd.ctdb.question.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.question.domain.Question;

import javax.servlet.jsp.JspException;

/**
 * Created by Booz Allen Hamilton
 * Date: Aug 19, 2004
 * 
 */
public class SimilarQuestionsDecorator extends ActionDecorator {

    /**
        * Retrieves the user actions allowed to be done on a Row of data.
        *
        * @return  HTML string displaying the actions that can be made on a Row
        */
       public String getActions() throws JspException
       {
           Question q = (Question) this.getObject();
           int id = q.getId();
           String root = this.getWebRoot();
           StringBuffer actions = new StringBuffer(100);
           actions.append("&nbsp;<a href=\"" + root + "/question/questionWizardStart.do?action=edit_form&id=" + id + "&version="+q.getVersion().getVersionNumber()+"\">edit</a>&nbsp;&nbsp;");
           return actions.toString();
       }

}
