package gov.nih.nichd.ctdb.selfreporting.tag;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang3.StringUtils;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.selfreporting.form.SelfReportingLandingForm;

public class SelfReportingHomeDecorator extends ActionDecorator {

	public SelfReportingHomeDecorator() {
		super();
	}

	public String getFormName() throws JspException {

		SelfReportingLandingForm selfReporting = (SelfReportingLandingForm) this.getObject();
		Integer aformId = selfReporting.getAdministeredFormId();

		String status = selfReporting.getStatus();
		String formName = selfReporting.getFormTitle();

		if (CtdbConstants.DATACOLLECTION_STATUS_COMPLETED.equals(status)
				|| CtdbConstants.DATACOLLECTION_STATUS_LOCKED.equals(status)) {
			return formName;

		} else {
			String link;

			// aformId doesn't exist means Not Started
			if (aformId == null) {
				link = "<a href=\"" + this.getWebRoot()
								+ "/selfreporting/dataCollection?action=fetchFormPSR&patientId="
								+ selfReporting.getPatientId() + "&formId=" + selfReporting.getFormId()
								+ "&visitTypeId=" + selfReporting.getIntervalId() + "&token=" + selfReporting.getToken() +"\">" + formName + "</a>";
			} else {
				link = "<a href=\"" + this.getWebRoot()
								+ "/selfreporting/dataCollection?action=editFormPSR&aformId="
								+ selfReporting.getAdministeredFormId() + "&token=" + selfReporting.getToken() + "\">" + formName + "</a>";
			}
			
			return link;
		}
	}
	
	
	public String getStatus() {
		SelfReportingLandingForm selfReporting = (SelfReportingLandingForm) this.getObject();
		String status = selfReporting.getStatus();
		
		if (StringUtils.isBlank(status)) {
			return CtdbConstants.DATACOLLECTION_STATUS_NOTSTARTED;
			
		} else if (CtdbConstants.DATACOLLECTION_STATUS_LOCKED.equals(status)) {
			return CtdbConstants.DATACOLLECTION_STATUS_COMPLETED;
			
		} else {
			return status;
		}
	}

	public String getLastUpdated() {

		SelfReportingLandingForm selfReporting = (SelfReportingLandingForm) this.getObject();
		Date lastUpdated = selfReporting.getLastUpdated();

		if (lastUpdated == null) {
			return "Not Started";
		} else {
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
			return df.format(lastUpdated);
		}
	}
}
