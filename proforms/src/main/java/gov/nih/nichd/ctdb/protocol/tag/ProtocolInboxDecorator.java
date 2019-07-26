package gov.nih.nichd.ctdb.protocol.tag;

import java.util.List;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.patient.common.PatientResultControl;
import gov.nih.nichd.ctdb.patient.form.PatientSearchForm;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.tbi.commons.model.StudyType;


/**
 * ProtocolInboxDecorator enables a table to have a column with Action links
 * with information specific to protocols. This class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ProtocolInboxDecorator extends ActionDecorator
{

	/** Creates a new instance of ProtocolListDecorator */
	public ProtocolInboxDecorator()
	{
		super();
	}

	/** get protocol number decorator returns the link to the protocols home page
	 * @return  String, the html
	 */
	public String getProtocolNumberDec() throws JspException
	{
		Protocol domainObject = (Protocol) this.getObject();
		if(domainObject.getWelcomeUrl() != null && !domainObject.getWelcomeUrl().equals(""))
		{
			int id = domainObject.getId();
			String root = this.getWebRoot();
			return "<a href=\"" + root + "/protocol/protocolView.do?action=view_form&id=" + id + "\">"
			+ domainObject.getProtocolNumber() + "</a>";
		}
		else
		{
			return domainObject.getProtocolNumber();
		}
	}

	/**
	 * Retrieves the description of a protocol in a way that the description length
	 *			is limited to about 90 characters on display.
	 *
	 * @return  String HTML string displaying the description on a Row
	 */
	public String getDescr()
	{
		Protocol protocol = (Protocol) this.getObject();

		String description = protocol.getDescription();

		if(description == null)
			return description;

		int index = description.indexOf(" ", 90);

		if(index != -1)
			return description.substring(0, index) + "...";
		else
			return description;
	}
	
	public String getStudyTypeName() {
		StudyType st = ((Protocol) this.getObject()).getStudyType();
		
		return st != null ? st.getName() : "";
	}

	/**
	 * returns the protocol number display string
	 * @return  String, the status
	 */
	public String getProtocolIdCheckbox()
	{
		Protocol domainObject = (Protocol) this.getObject();
		int protocolId = domainObject.getId();
		return "<input type='checkbox' name='selectProtocolId' id=\"" + protocolId + "\" />";
	}

	/**
	 * returns the status display string
	 * @return  String, the status
	 */
	public String getStatusDec()
	{
		Protocol domainObject = (Protocol) this.getObject();
		return domainObject.getStatus().getShortName();
	}


	/**
	 * returns dhtml to invoke the edit and audit actions on a patient
	 * @return  String, the actions
	 */
	public String getActionDec() throws JspException
	{
		String actionStr = "";
		User user = (User) this.getPageContext().getSession().getAttribute("user");
		Protocol domainObject = (Protocol) this.getObject();
		int id = domainObject.getId();
		String root = this.getWebRoot();
		if(this.checkPrivilege("addeditprotocols", id) && ( ! user.isStaff()))
		{
			actionStr += "<a href=\"" + root + "/protocol/protocol.do?action=edit_form&id=" + id + "\">edit</a>&nbsp;&nbsp;";
		}
		
		if( this.checkPrivilege("assignuserstoprotocol", id) && user.isSysAdmin() )
		{
			actionStr += "<a href=\"" + root + "/protocol/protocolUser.do?action=edit_form&id=" + id + "\">assign&nbsp;users</a>&nbsp;&nbsp;";
		}



		if(this.checkPrivilege("viewaudittrails", id))
		{
			actionStr += "<a href=\"Javascript:popupWindow ('" + root + "/protocol/protocolAudit.do?id=" + id + "');\">view&nbsp;audit</a><br>";
		}
		// if they can see a protocol, they can see the links
		actionStr += "<a href=\"" + root + "/protocol/protocolLinkHome.do?protocolId=" + id + "\">web&nbsp;sites</a>&nbsp;&nbsp;";
		if (this.checkPrivilege("manageAttachments")) {
			actionStr += "<a href=\""+root +"/attachments/attachmentHome.do?hideNav=false&typeId=1&associatedId="+id+"\">attachments</a> ";
		}
		return actionStr;
	}
	
	public String getStudyNumberLink() throws JspException {
		String linkText = "";
		String root = this.getWebRoot();
		Protocol domainObject = (Protocol) this.getObject();
		String protocolId = Integer.toString(domainObject.getId());
		linkText = "<a href=\"" + root + "/protocol/protocolView.do?action=view_form&id="+protocolId+"\">"+domainObject.getProtocolNumber()+"</a>";
		return linkText;
	}
	
	public String getSwitchStudyLink() throws JspException {
		String linkText = "";
		String root = this.getWebRoot();
		Protocol domainObject = (Protocol) this.getObject();
		String protocolId = Integer.toString(domainObject.getId());
		linkText = "<a href=\"" + root + "/dashboard.action?id="+protocolId+"\">"+domainObject.getProtocolNumber()+"</a>";
		return linkText;
	}
	
	public String getDetails() throws JspException {
		Protocol domainObject = (Protocol) this.getObject();
		return "<div class=\"details\">" + domainObject.getName() + "</div>";
	}
	
	public String getProtocolPatientsCount() throws JspException {
		try {
			User user = (User) this.getPageContext().getSession().getAttribute("user");
			PatientManager pm = new PatientManager();
			Protocol domainObject = (Protocol) this.getObject();
			PatientResultControl prc = new PatientResultControl();
			prc.setProtocolId(domainObject.getId());
			PatientSearchForm patientSearchForm = new PatientSearchForm();
			patientSearchForm.setProtocolIdSearch(domainObject.getId());
			this.updateResultControl(prc, patientSearchForm);
			List patients = pm.getMinimalPatients(prc);
			return Integer.toString(patients.size());
		}
		catch(Exception e) {
			return "0";
		}
	}
	
	/** updateResultControl updates a result control ojbect to facillitate searching
     *   and sorting.
     * @param prc : the PatientResultControl
     * @param form  : the ActionForm that has the info needed
     */
    private void updateResultControl(PatientResultControl prc, PatientSearchForm form)
    {

        if( form.getNumResults() != null && (! (form.getNumResults()).equalsIgnoreCase ("all")))
        {
            prc.setRowNumMax(Integer.parseInt(form.getNumResults()));
        }

        prc.setFirstName(form.getFirstNameSearch());
        prc.setLastName(form.getLastNameSearch());
        prc.setSubjectId(form.getRecordNumberSearch());
        prc.setProtocolId(form.getProtocolIdSearch());
        prc.setSubjectNumber(form.getSubjectNumberSearch());
        prc.setPatientGroupName(form.getPatientGroupNameSearch());
        prc.setActiveInProtocol(form.getActiveInProtocol());
        prc.setEnrollmentStatus(form.getEnrollmentStatus());

    }
}