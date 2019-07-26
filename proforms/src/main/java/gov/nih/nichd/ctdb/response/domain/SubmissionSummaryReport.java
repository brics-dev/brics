package gov.nih.nichd.ctdb.response.domain;

import java.util.Date;

public class SubmissionSummaryReport
{
	private String guid;
	private String formName;
	private String visitTypeName;
	private Date visitDate;
	private String submissionStatus;
	private String mrn;
	private String nrn;
	
	public SubmissionSummaryReport()
	{
		guid = "";
		formName = "";
		visitTypeName = "";
		visitDate = null;
		submissionStatus = "";
		mrn ="";
		nrn ="";
	}

	/**
	 * @return the guid
	 */
	public String getGuid() {
		return guid;
	}

	/**
	 * @param guid the guid to set
	 */
	public void setGuid(String guid) {
		this.guid = guid;
	}

	/**
	 * @return the formName
	 */
	public String getFormName() {
		return formName;
	}

	/**
	 * @param formName the formName to set
	 */
	public void setFormName(String formName) {
		this.formName = formName;
	}

	/**
	 * @return the visitTypeName
	 */
	public String getVisitTypeName() {
		return visitTypeName;
	}

	/**
	 * @param visitTypeName the visitTypeName to set
	 */
	public void setVisitTypeName(String visitTypeName) {
		this.visitTypeName = visitTypeName;
	}

	/**
	 * @return the visitDate
	 */
	public Date getVisitDate() {
		return visitDate;
	}

	/**
	 * @param visitDate the visitDate to set
	 */
	public void setVisitDate(Date visitDate) {
		this.visitDate = visitDate;
	}

	/**
	 * @return the submissionStatus
	 */
	public String getSubmissionStatus() {
		return submissionStatus;
	}

	/**
	 * @param submissionStatus the submissionStatus to set
	 */
	public void setSubmissionStatus(String submissionStatus) {
		this.submissionStatus = submissionStatus;
	}

	public String getMrn() {
		return mrn;
	}

	public void setMrn(String mrn) {
		this.mrn = mrn;
	}

	public String getNrn() {
		return nrn;
	}

	public void setNrn(String nrn) {
		this.nrn = nrn;
	}
}
