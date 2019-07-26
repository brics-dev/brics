package gov.nih.nichd.ctdb.response.tag;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.DataEntryWorkflowType;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;


/**
 * ResponseHomeDecorator enables sorting on the response home table.
 * This class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ResponseHomeDecorator extends ActionDecorator
{

    /**
     * Default Constructor
     */
    public ResponseHomeDecorator()
    {
        super();
    }

    public String getPatientDec () {
        AdministeredForm aform = (AdministeredForm) this.getObject();
        Protocol protocol = (Protocol) this.getPageContext().getSession().getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
        if (protocol.isUsePatientName()) {
            return aform.getPatient().getLastName() + ", " + aform.getPatient().getFirstName();
        } else {
            return aform.getPatient().getSubjectId();
        }

    }

    /**
     * Gets the link to the response data page for the appropriate form
     *
     * @return  The HTML for the form name
     */
    public String getFormNameDec()
    {
        AdministeredForm domainObject = (AdministeredForm) this.getObject();
        String formName = domainObject.getForm().getName();
        return formName;
    }

    /**
     * Gets the formatted certified date/time
     *
     * @return  The Certified Date/Time
     */
    public String getCertifiedDateDec()
    {
        AdministeredForm domainObject = (AdministeredForm) this.getObject();
        Date date = domainObject.getCertifiedDate();
        if(date != null)
        {
            SimpleDateFormat localFormat = new SimpleDateFormat(SysPropUtil.getProperty("default.system.dateformat"));
            SimpleDateFormat timeFormat = new SimpleDateFormat(SysPropUtil.getProperty("default.system.timeformat"));

            return localFormat.format(date) + "  " + timeFormat.format(date);
        }
        else
        {
            return null;
        }
    }


    /**
     * Retrieves the user actions allowed to be done on a Row of data.
     *
     * @return  HTML string displaying the actions that can be made on a Row
     */
    public String getActions() throws JspException
    {
        AdministeredForm domainObject = (AdministeredForm) this.getObject();
        int id = domainObject.getId();
        String root = this.getWebRoot();
        StringBuffer actions = new StringBuffer(100);
        actions.append("");
        //get User object from session
        User user = (User) this.getPageContext().getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);

        if(domainObject.getLockDate() != null &&
                (this.checkPrivilege("dataentryoversight") || (domainObject.getLockedBy() == user.getId())))
        {
            actions.append("&nbsp;<a href=\"" + root + "/response/viewForm.do?action=view_draft_form_withheader&source=response_home&id=" + id + "&userid=" + domainObject.getLockedBy() + "\">view&nbsp;entry1</a>&nbsp;&nbsp;");
        }
        if(domainObject.getLock2Date() != null &&
                (this.checkPrivilege("dataentryoversight") || (domainObject.getLocked2By() == user.getId())))
        {
            actions.append("&nbsp;<a href=\"" + root + "/response/viewForm.do?action=view_draft_form_withheader&source=response_home&id=" + id + "&userid=" + domainObject.getLocked2By() + "\">view&nbsp;entry2</a>&nbsp;&nbsp;");
        }
        actions.append("<br>");
        if(this.checkPrivilege("dataentryoversight"))
        {
            if(domainObject.getCertifiedDate() != null)
            {
                if (domainObject.getForm().getDataEntryWorkflow().equals(DataEntryWorkflowType.STANDARD)) {
                    actions.append("&nbsp;<a href=\"" + root + "/response/viewForm.do?action=view_certified_form_withheader&source=response_home&id=" + id + "\">view&nbsp;certified</a>&nbsp;&nbsp;");
                }

                if(domainObject.getFinalLockDate() != null)
                {
                    actions.append("&nbsp;<a href=\"" + root + "/response/viewForm.do?action=view_final_form_withheader&source=response_home&id=" + id + "\">view&nbsp;final</a>&nbsp;&nbsp;");
                }
            }
        }

        if (this.checkPrivilege("createquery") && ! domainObject.isQaLocked()) {
            actions.append("&nbsp;<a href=\""+root+"/qa/qaQuery.do?action=add_form&_associatedId="+id+"&_subId=0&_ocId=18\">query</a>&nbsp;");
        }
        if (this.checkPrivilege("qaReview") && domainObject.getFinalLockDate() != null) {
            if (domainObject.isQaReviewed()) {
                actions.append( "&nbsp;&nbsp;<a href=\""+root+"/response/qaReview.do?id="+id+"&_qaReviewed=false\">undo&nbsp;review</a>&nbsp;");                
            } else {
                actions.append( "&nbsp;&nbsp;<a href=\""+root+"/response/qaReview.do?id="+id+"&_qaReviewed=true\">review&nbsp;complete</a>&nbsp;");
            }
        }
         if (this.checkPrivilege("qaLock") && domainObject.getFinalLockDate() != null) {
             if(domainObject.isQaLocked()) {
                 actions.append( "&nbsp;&nbsp;<a href=\""+root+"/response/qaReview.do?id="+id+"&_qaLocked=false\">undo&nbsp;qa&nbsp;lock</a>&nbsp;");
             } else {
                actions.append( "&nbsp;&nbsp;<a href=\""+root+"/response/qaReview.do?id="+id+"&_qaLocked=true\">qa&nbsp;lock</a>&nbsp;");
             }
        }
        return actions.toString();
    }

    public String getTimePointDec () throws JspException {
        AdministeredForm af = (AdministeredForm) this.getObject();
        //return "test";
        return af.getTimePoint(this.getPageContext().getSession());
    }
}
