package gov.nih.nichd.ctdb.selfreporting.form;

import java.util.Date;

import gov.nih.nichd.ctdb.common.CtdbForm;

public class SelfReportingLandingForm extends CtdbForm {

	private static final long serialVersionUID = 2340107426436008531L;
	
	private int patientId;
	private int intervalId;
	private Date visitDate;
	private int formId;
	private Integer administeredFormId;
	private String shortName;
	private Date lastUpdated;
	private String status;
	private String token;
	private String formTitle;
	
	public int getPatientId() {
		return patientId;
	}
	
	public void setPatientId(int patientId) {
		this.patientId = patientId;
	}
	
	public int getIntervalId() {
		return intervalId;
	}
	
	public void setIntervalId(int intervalId) {
		this.intervalId = intervalId;
	}
	
	public Date getVisitDate() {
		return visitDate;
	}
	
	public void setVisitDate(Date visitDate) {
		this.visitDate = visitDate;
	}
	
	public int getFormId() {
		return formId;
	}
	
	public void setFormId(int formId) {
		this.formId = formId;
	}
	
	public Integer getAdministeredFormId() {
		return administeredFormId;
	}

	public void setAdministeredFormId(Integer administeredFormId) {
		this.administeredFormId = administeredFormId;
	}

	public String getShortName() {
		return shortName;
	}
	
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	public String getFormTitle() {
		return formTitle;
	}

	public void setFormTitle(String formTitle) {
		this.formTitle = formTitle;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}
	
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	
	
}
