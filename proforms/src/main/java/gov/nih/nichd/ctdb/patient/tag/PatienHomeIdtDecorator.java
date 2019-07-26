package gov.nih.nichd.ctdb.patient.tag;

import java.util.Iterator;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.tag.ActionIdtDecorator;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.domain.PatientProtocol;
import gov.nih.nichd.ctdb.patient.form.PatientSearchForm;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;

public class PatienHomeIdtDecorator extends ActionIdtDecorator {

	public PatienHomeIdtDecorator() {
		super();
	}

	public String getProtocolConfiguredDisplay() throws JspException {
		Protocol protocol = (Protocol) ServletActionContext.getRequest().getSession()
				.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		Patient patient = (Patient) this.getObject();
		String protocolConfiguredDisplay = null;

		// Get the protocol configured patient display type either GUID/MRN/SubjectId
		if (protocol.getPatientDisplayType() == CtdbConstants.PATIENT_DISPLAY_ID) {
			protocolConfiguredDisplay = patient.getSubjectId();
		}
		else if (protocol.getPatientDisplayType() == CtdbConstants.PATIENT_DISPLAY_GUID) {
			protocolConfiguredDisplay = patient.getGuid();
		}
		else if (protocol.getPatientDisplayType() == CtdbConstants.PATIENT_DISPLAY_MRN) {
			protocolConfiguredDisplay = patient.getMrn();
		}

		// Build the anchor tag.
		String tag = "";

		if (!StringUtils.isEmpty(protocolConfiguredDisplay)) {
			tag = "<a href=\"" + this.getWebRoot() + "/patient/viewPatient.action?patientId=" + patient.getId() + "\">"
					+ protocolConfiguredDisplay + "</a>";
		}

		return tag;
	}

	public String getLastName() {
		Patient patient = (Patient) this.getObject();
		return patient.getLastName();
	}

	public String getFirstName() {
		return ((Patient) this.getObject()).getFirstName();
	}

	public String getPatientProtocolStatus() {
		String message = "";
		Patient patient = (Patient) this.getObject();
		for (PatientProtocol pp : patient.getProtocols()) {
			// no protocol associate with current subject, i.e. no study specific value
			if (pp.getId() <= 0) {
				continue;
			}
			
			if (pp.isActive()) {
				message = "Active";
			}
			else {
				message = "Inactive";
			}
		}
		return message;
	}

	public String getPatientGroup() {
		String message = "";
		Patient patient = (Patient) this.getObject();
		Protocol protocol = (Protocol) ServletActionContext.getRequest().getSession()
				.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		
		for (PatientProtocol pp : patient.getProtocols()) {
			if (pp.getId() == protocol.getId()) {
				message = pp.getGroupName();
				break;
			}
		}
		return message;
	}


	/**
	 * Returns a string of all protocol numbers/names associated with the patient. In the following format:<br>
	 * <li>protocol number (protocol name)
	 *
	 * @return The string representation of all protocol numbers/names associated with the patient.
	 */

	public String getProtocols() throws JspException {
		Patient patient = (Patient) this.getObject();
		StringBuffer result = new StringBuffer();
		
		for (Iterator<PatientProtocol> iterator = patient.getProtocols().iterator(); iterator.hasNext();) {
			PatientProtocol pp = iterator.next();
			
			if (pp != null && pp.getProtocolNumber() != null) {
				result.append(pp.getProtocolNumber());
			}
			
			if (iterator.hasNext()) {
				result.append(", ");
			}
		}
		
		return result.toString();
	}

	public String getFutureStudy() throws JspException {
		Patient patient = (Patient) this.getObject();
		StringBuffer result = new StringBuffer();
		
		for (PatientProtocol pp : patient.getProtocols()) {
			if (pp != null) {
				result.append((pp.isFutureStudy()) ? " TRUE" : " FALSE");
			}
		}
		
		return result.toString();
	}

	/**
	 * Returns edit and audit action links for a patient
	 *
	 * @return The action links
	 */
	public String getActions() throws JspException {
		StringBuffer action = new StringBuffer();
		Patient patient = (Patient) this.getObject();
		String rootUrl = this.getWebRoot();
		
		if (this.checkPrivilege("addeditpatients")) {
			action.append("&nbsp;<a href=\"" + rootUrl + "/patient/showEditPatient.action?patientId=")
				.append(patient.getPatientId() + "\">edit</a>&nbsp;&nbsp;&nbsp;");
		}
		
		if (this.checkPrivilege("viewaudittrails")) {
			action.append("<a href=\"Javascript:popupWindow ('" + rootUrl + "/patient/patientAudit.action?id=")
				.append(patient.getPatientId() + "');\">view&nbsp;audit</a>");
		}

		PatientSearchForm form =
				(PatientSearchForm) ServletActionContext.getRequest().getAttribute("patientSearchForm");

		if (this.checkPrivilege("addeditpatients") && form.getInProtocol().equalsIgnoreCase("no")) {
			action.append("&nbsp;&nbsp;<a href=\"" + rootUrl)
				.append("/patient/showEditPatient.action?action=add_to_protocol&patientId=" + patient.getPatientId())
				.append("\">associate&nbsp;to&nbsp;protocol</a>");
		}
		
		if (this.checkPrivilege("manageAttachments")) {
			action.append("&nbsp;&nbsp;<a href=\"" + rootUrl)
				.append("/attachments/attachmentHome.do?highlightLeftNav=patientHome.do&hideNav=false&typeId=2&associatedId=")
				.append(patient.getPatientId() + "\">attachments</a> ");
		}

		if (this.checkPrivilege("createquery")) {
			action.append("&nbsp;<a href=\"" + rootUrl + "/qa/qaQuery.do?action=add_form&_associatedId=")
				.append(patient.getPatientId() + "&_subId=0&_ocId=9\">query</a>&nbsp;");
		}

		return action.toString();
	}

	public String getAdminFormActions() throws JspException {
		Patient patient = (Patient) this.getObject();
		int id = patient.getId();
		String rootUrl = this.getWebRoot();
		StringBuffer actions = new StringBuffer();
		
		actions.append("<a href='" + rootUrl + "/response/dataEntrySetup.do?action=add_form&selectedPatient=" + id)
			.append("' title='Select Patient' class='tableCell'>select patient</a>&nbsp;&nbsp;&nbsp;")
			.append("<a href='" + rootUrl + "/patient/viewPatient.action?id=" + id)
			.append("' class='tableCell' title='View Details'>")
			.append("view details</a>");

		return actions.toString();
	}

	public String getFormatValidated() throws JspException {
		Patient patient = (Patient) this.getObject();
		Protocol protocol = (Protocol) ServletActionContext.getRequest().getSession()
				.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		String result = "";
		
		for (PatientProtocol pp : patient.getProtocols()) {
			if (pp != null && pp.getId() == protocol.getId() && pp.isValidated()) {
				result = "<img src=\"" + this.getWebRoot()
						+ "/images/checkMark.png\" alt='validated' width='15' height='15' />";
				break;
			}
		}
		
		return result;
	}
}
