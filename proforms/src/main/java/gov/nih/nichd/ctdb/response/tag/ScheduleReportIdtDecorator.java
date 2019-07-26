package gov.nih.nichd.ctdb.response.tag;

import org.eclipse.jetty.util.StringUtil;

import gov.nih.nichd.ctdb.response.domain.ScheduleReport;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class ScheduleReportIdtDecorator extends IdtDecorator {

	/**
	 * Default Constructor
	 */
	public ScheduleReportIdtDecorator() {
		super();
	}
	
	public String getProtocolNumber() {
		ScheduleReport repo = (ScheduleReport) getObject();
		return repo.getProtocolNumber();
	}
	
	public String getPatientId() {
		ScheduleReport repo = (ScheduleReport) getObject();
		String patientId = "";
		String mrn = repo.getPatientMRN();
		String guid = repo.getPatientGuid();
		String subjectId = repo.getPatientSubjectId();
		if(StringUtil.isBlank(mrn)){
			mrn = "N/A";
		}
		if(StringUtil.isBlank(guid)){
			guid = "N/A";
		}
		if(StringUtil.isBlank(subjectId)){
			subjectId = "N/A";
		}
		patientId = mrn + " / <br/>" + guid + " / <br/>" + subjectId;
		return patientId;
	}
	
	public String getPatientName() {
		ScheduleReport repo = (ScheduleReport) getObject();
		return repo.getPatientFullName();
	}
	
	public String getVisitTypeName() {
		ScheduleReport repo = (ScheduleReport) getObject();
		return repo.getVisitTypeName();
	}
	
	public String getVisitDate() {
		ScheduleReport repo = (ScheduleReport) getObject();
		String visitDate = BRICSTimeDateUtil.dateToDateTimeString(repo.getVisitDate());
		String[] visitDataTime = visitDate.split("T");
		String vTDate = visitDataTime[0];
		String vtTime = visitDataTime[1].split("-")[0];
		return vTDate + " <br/>" + vtTime;
	}
	public String getProcedure() {
		ScheduleReport repo = (ScheduleReport) getObject();
		return repo.getProcedure().getName();
	}
	
	public String getClinicalLocationInfo() {
		ScheduleReport repo = (ScheduleReport) getObject();
		String locName = repo.getClinicalLocation().getName();
//		String address = repo.getClinicalLocation().getAddress().toString();
//		String info = locName + "<br/>" + address;
		return locName;
	}
	
	public String getPointOfContactInfo() {
		ScheduleReport repo = (ScheduleReport) getObject();
		String pocName = repo.getPointOfContact().getFullName();
//		String phone = repo.getPointOfContact().getPhone();
//		String email = repo.getPointOfContact().getEmail();
//		String info = pocName + "<br/>" + phone + " / " + email;
		return pocName ;
	}
	public String getComments() {
		ScheduleReport repo = (ScheduleReport) getObject();
		String comments = repo.getComments();
		return comments;
	}

}

