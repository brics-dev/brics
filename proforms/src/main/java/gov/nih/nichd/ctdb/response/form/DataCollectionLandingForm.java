package gov.nih.nichd.ctdb.response.form;

import gov.nih.nichd.ctdb.common.CtdbForm;
import gov.nih.nichd.ctdb.common.CtdbLookup;

public class DataCollectionLandingForm extends CtdbForm{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4626642641729164932L;
	private int protocolId;
	private String protocolNumber;
	private String protocolName;
	private String visitDate;
	private String intervalName;
	private String patientName;
	private int patientId;
	private String formName;
	private int formStatus;
	private String shortName;
	private String patientFormViewStatus;
	private int isStatus;
	private String npTypes;
	private int formId;
	private String formStatusName;
	private String subjectId;
	private String patientRecordNumber;
	private String formLastUpdatedDate;
	//Non patient From type
	 private CtdbLookup status;
	 private CtdbLookup xformtype;
	 private String formTypeName;
	 private String name;
	 private String updatedDate = null;
	 private int formTypeId;
	
	 //patientView specific 
	 private int pvPatientId;
	 private String pvVisitDate;
	 private String pvPatientName;
	 private String patientFirstName;
	 private String patientLastName;
	 private String pvGuid;
	 private String pvVisitDateId;
	 private String pvInterval;
	 private String pvIntervalName;
	 
	 private String mrn;
	 
	//serach type
	private String searchFormType ="patientData";
	
	public String getFormStatusName() {
		return formStatusName;
	}
	public void setFormStatusName(String formStatusName) {
		this.formStatusName = formStatusName;
	}

	public int getProtocolId() {
		return protocolId;
	}
	public void setProtocolId(int protocolId) {
		this.protocolId = protocolId;
	}
	public String getProtocolNumber() {
		return protocolNumber;
	}
	public void setProtocolNumber(String protocolNumber) {
		this.protocolNumber = protocolNumber;
	}
	public String getProtocolName() {
		return protocolName;
	}
	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
	}
	public String getVisitDate() {
		return visitDate;
	}
	public void setVisitDate(String visitDate) {
		this.visitDate = visitDate;
	}
	public String getIntervalName() {
		return intervalName;
	}
	public void setIntervalName(String intervalName) {
		this.intervalName = intervalName;
	}
	public String getPatientName() {
		return patientName;
	}
	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}
	public int getPatientId() {
		return patientId;
	}
	public void setPatientId(int patientId) {
		this.patientId = patientId;
	}
	public String getFormName() {
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}
	
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public int getFormStatus() {
		return formStatus;
	}
	public void setFormStatus(int formStatus) {
		this.formStatus = formStatus;
	}
	
	public int getFormId() {
		return formId;
	}
	public void setFormId(int formId) {
		this.formId = formId;
	}
	public String getPvGuid() {
		return pvGuid;
	}
	public void setPvGuid(String pvGuid) {
		this.pvGuid = pvGuid;
	}
	public String getSubjectId() {
		return subjectId;
	}
	public void setSubjectId(String nihRecordNo) {
		this.subjectId = nihRecordNo;
	}
	public String getPatientRecordNumber() {
		return patientRecordNumber;
	}
	public void setPatientRecordNumber(String patientRecordNumber) {
		this.patientRecordNumber = patientRecordNumber;
	}
	public String getFormLastUpdatedDate() {
		return formLastUpdatedDate;
	}
	public void setFormLastUpdatedDate(String formLastUpdatedDate) {
		this.formLastUpdatedDate = formLastUpdatedDate;
	}
	/*public int getFormType() {
		return formType;
	}
	public void setFormType(int formType) {
		this.formType = formType;
	}*/
	public CtdbLookup getStatus() {
		if (status == null){
			status = new CtdbLookup();
		}
		return status;
	}
	public void setStatus(CtdbLookup status) {
		this.status = status;
	}
	public CtdbLookup getXformtype() {
		if(xformtype == null){
			xformtype = new  CtdbLookup();
		}
		return xformtype;
	}
	public void setXformtype(CtdbLookup xformtype) {
		this.xformtype = xformtype;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSearchFormType() {
		return searchFormType;
	}
	public void setSearchFormType(String searchFormType) {
		this.searchFormType = searchFormType;
	}
	public String getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}
	public String getFormTypeName() {
		return formTypeName;
	}
	public void setFormTypeName(String formTypeName) {
		this.formTypeName = formTypeName;
	}
	public int getFormTypeId() {
		return formTypeId;
	}
	public void setFormTypeId(int formTypeId) {
		this.formTypeId = formTypeId;
	}
	public int getPvPatientId() {
		return pvPatientId;
	}
	public void setPvPatientId(int pvPatientId) {
		this.pvPatientId = pvPatientId;
	}
	public String getPvVisitDate() {
		return pvVisitDate;
	}
	public void setPvVisitDate(String pvVisitDate) {
		this.pvVisitDate = pvVisitDate;
	}
	public String getPvPatientName() {
		return pvPatientName;
	}
	public void setPvPatientName(String pvPatientName) {
		this.pvPatientName = pvPatientName;
	}
	public String getPatientFirstName() {
		return patientFirstName;
	}
	public void setPatientFirstName(String patientFirstName) {
		this.patientFirstName = patientFirstName;
	}
	public String getPatientLastName() {
		return patientLastName;
	}
	public void setPatientLastName(String patientLastName) {
		this.patientLastName = patientLastName;
	}
	public String getPatientFormViewStatus() {
		return patientFormViewStatus;
	}
	public void setPatientFormViewStatus(String patientFormViewStatus) {
		this.patientFormViewStatus = patientFormViewStatus;
	}
	public String getNpTypes() {
		return npTypes;
	}
	public void setNpTypes(String npTypes) {
		this.npTypes = npTypes;
	}
	public int getIsStatus() {
		return isStatus;
	}
	public void setIsStatus(int isStatus) {
		this.isStatus = isStatus;
	}
	public String getPvVisitDateId() {
		return pvVisitDateId;
	}
	public void setPvVisitDateId(String pvVisitDateId) {
		this.pvVisitDateId = pvVisitDateId;
	}
	public String getPvInterval() {
		return pvInterval;
	}
	public void setPvInterval(String pvInterval) {
		this.pvInterval = pvInterval;
	}
	public String getPvIntervalName() {
		return pvIntervalName;
	}
	public void setPvIntervalName(String pvIntervalName) {
		this.pvIntervalName = pvIntervalName;
	}
    public String getMrn() {
		return mrn;
	}
	public void setMrn(String mrn) {
		this.mrn = mrn;
	}
	
}
