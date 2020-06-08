package gov.nih.nichd.ctdb.patient.tag;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang3.StringUtils;

import gov.nih.nichd.ctdb.common.tag.ActionIdtDecorator;
import gov.nih.nichd.ctdb.patient.domain.PatientVisit;

/**
 * PatientHomeDecorator enables a table to have a column with Action links (Edit/View/..). This class works with the
 * <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class PatientVisitIdtDecorator extends ActionIdtDecorator {

	/**
	 * Default Constructor
	 */
	public PatientVisitIdtDecorator() {
		super();
	}

	public String getIdDisplay() throws JspException {
		PatientVisit pv = (PatientVisit) this.getObject();
		String tag = "";

		if (!StringUtils.isEmpty(pv.getSubjectId())) {
			tag = "<a href=\"" + this.getWebRoot()
					+ "/patient/viewPatient.action?highlightLeftNav=patientHome.action&patientId=" + pv.getPatientId()
					+ "\">" + pv.getSubjectId() + "</a>";
		}

		return tag;
	}

	public String getGuidDisplay() throws JspException {
		PatientVisit pv = (PatientVisit) this.getObject();

		return "<a href=\"" + this.getWebRoot()
				+ "/patient/viewPatient.action?highlightLeftNav=patientHome.action&patientId=" + pv.getPatientId()
				+ "\">" + pv.getGuid() + "</a>";
	}

	// TODO: pv.getId() or pv.getPatientId() ???
	public String getMrn() throws JspException {
		PatientVisit pv = (PatientVisit) this.getObject();

		return "<a href=\"" + this.getWebRoot()
				+ "/patient/viewPatient.action?highlightLeftNav=patientHome.action&patientId=" + pv.getPatientId()
				+ "\">" + (pv.getMrn() == null ? "" : pv.getMrn()) + "</a>";
	}

	public String getTokenLink() throws JspException {
		PatientVisit pv = (PatientVisit) this.getObject();
		String token = pv.getToken();
		String tokenLink = null;
		
		if (token == null) {
			tokenLink = " ";
		}
		else {
			tokenLink = "<a href=\"javascript:showTokenLink('" + pv.getGuid() + "','" + pv.getEmail() + "','" + pv.getIntervalClinicalPointId() + "','" + this.getWebRoot()
					+ "/selfreporting/list?token=" + pv.getToken() + "')\">" + pv.getToken() + "</a>";
		}
		
		return tokenLink;
	}
	
	public String getComments() {
		PatientVisit pv = (PatientVisit) this.getObject();
		return pv.getComments();
	}
}
