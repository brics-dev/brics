package gov.nih.nichd.ctdb.response.tag;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.Version;
import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.DataEntryDraft;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;

import java.util.List;

import javax.servlet.jsp.JspException;


/**
 * DataEntryInProgressDecorator enables a table to have a column with Action links
 * with information specific to data entry. This class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class DataEntryInProgressDecorator extends ActionDecorator
{
    /**
     * Default Constructor
     */
    public DataEntryInProgressDecorator()
    {
        super();
    }

    /**
     * Gets the link to the data entry page for the appropriate form
     *
     * @return  The HTML link
     */
    public String getNihRecordNumberDec() throws JspException
    {
        AdministeredForm domainObject = (AdministeredForm) this.getObject();
        Protocol proto = (Protocol) this.getPageContext().getSession().getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
        String title;
        if (proto != null && proto.isUsePatientName()) {
            title = domainObject.getPatient().getLastName() +", "+domainObject.getPatient().getFirstName();
        } else {
            title = domainObject.getPatient().getSubjectId();
        }

        String root = this.getWebRoot();
        return "<a href=\"" + root + "/response/dataEntry.do?action=edit_form&id=" + domainObject.getId() + "\">"
                + title + "</a>";
    }
    
    public String getPatientLink() throws JspException
    {
    	// http://localhost:8080/ibis/patient/viewPatient.do?&sectionDisplay=default&id=15
    	AdministeredForm form = (AdministeredForm) this.getObject();
    	Protocol proto = (Protocol) this.getPageContext().getSession().getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
        String title;
        if (proto != null && proto.isUsePatientName()) {
            title = form.getPatient().getLastName() +", "+form.getPatient().getFirstName();
        } else {
            title = form.getPatient().getSubjectId();
        }
    	
    	String type = form.getForm().getFormTypeName();
    	//return "<a href=\"" + this.getWebRoot()+"/response/dataCollection.do?action=editForm&mode=formPatient&aformId="+form.getId()+"&formId="+form.getForm().getId()+"\">" + title + "</a>";
    	// because the patient ID is not linked in the wireframe, I'm removing the "link" part of this
    	return title;
    }

    public String getActions () throws JspException {
        String actions = "";
        AdministeredForm af = (AdministeredForm) this.getObject();
        //CR#767: Edit link for Return to Previous Data Entry should always be showed up no matter what the privilege is
        actions += "<a href=\"" + this.getWebRoot() + "/response/dataEntry.do?action=edit_form&id=" + af.getId() + "\">"
                + "continue</a>&nbsp;&nbsp;";
        actions += "<a href=\""+this.getWebRoot() + "/response/editAdminFormMetaData.do?source=inProgress&action=edit_form&id="
                +af.getId()+"\">change&nbsp;patient/time&nbsp;point</a>";
        return actions;
    }

    public String getFormVersion () throws JspException
    {
        AdministeredForm domainObject = (AdministeredForm) this.getObject();
       return new Version(domainObject.getFormVersion()).toString();
    }

    public String getTimePointDec () throws JspException {
        AdministeredForm adminedForm = (AdministeredForm) this.getObject();
		return adminedForm.getTimePoint(this.getPageContext().getSession());
    }
    
    public String getFormName () throws JspException {
    	AdministeredForm adminedForm = (AdministeredForm) this.getObject();
    	return adminedForm.getForm().getName();
    }
    
    public String getAdminFormId()
    {
    	AdministeredForm adminedForm = (AdministeredForm) this.getObject();
        int aFormId =adminedForm.getForm().getId();
        return "<input type='checkbox' name='selectFormId' id=\"" + aFormId +"\" onclick=\"validateInProgressDataCollection()\"/>";
       // return "<input type='checkbox' name='selectProtocolId' id=\"" + protocolId + "\" onclick=\"validateSelectedProtocols()\"/>";
    }
    
    public String getStatus () throws JspException {
    	return "In Progress";
    }
    
    public int getDataeEntryFlag () throws JspException
    {
        AdministeredForm domainObject = (AdministeredForm) this.getObject();
       return domainObject.getForm().getSingleDoubleKeyFlag();
    }
    
    /**
     * Returns current user name 1 for this row
     *
     * @return The user name 1
     */
    public String getUserName1() throws JspException {
        AdministeredForm adminedForm = (AdministeredForm) this.getObject();
        String returnValue = adminedForm.getDataEnteredByName1();

        if (returnValue == null || returnValue.equals("")) {
            returnValue = "N/A";
        }
        return returnValue;
    }
    
    public String getResumeLink() throws JspException {
    	AdministeredForm form = (AdministeredForm) this.getObject();
    	String type = form.getForm().getFormTypeName();
    	return "<a href=\"" + this.getWebRoot()+"/response/dataCollection.do?action=editForm&mode=formPatient&aformId="+form.getId()+"&formId="+form.getForm().getId()+"\">" + form.getForm().getName() + "</a>";
    	//return "<a href=\""+this.getWebRoot()+"/response/dataEntry.do?action=edit_form&id="+form.getId()+"\">" + form.getForm().getName() + "</a>";
    }
    
    public String getNumQuestionsAnswered()
    {
    	AdministeredForm form = (AdministeredForm) this.getObject();
    	try {
    		ResponseManager rm = new ResponseManager();
    		List dataEntry1List = rm.getDataEntries(form.getForm().getId(), 1);
    		DataEntryDraft dataEntry = (DataEntryDraft)dataEntry1List.get(0);
	        //DataEntryDraft dataEntry = (DataEntryDraft)this.getObject();
	        if (dataEntry.getNumQuestionsAnswered() == Integer.MIN_VALUE)
	        {
	            return "0";
	        }
	        else
	        {
	            return dataEntry.getNumQuestionsAnswered() + "";
	        }
    	}
    	catch(Exception e) {
    		return "0";
    	}
    }
    
}
