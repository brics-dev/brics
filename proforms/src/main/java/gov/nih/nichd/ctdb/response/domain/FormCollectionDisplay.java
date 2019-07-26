package gov.nih.nichd.ctdb.response.domain;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

public class FormCollectionDisplay extends CtdbDomainObject {
	private static final long serialVersionUID = -776782831199568430L;
	
	private String formName = "";
	private String  intervalName = "";
    private String pVisitDate = "";
    private String collectionStatus = "";
    private int administeredFormId;
    private int formId;
    private int intervalId;
    private int patientId;
    private int dataEntryDraftId;
    private int dataEntryByUserId;
    private int dataEntryFlag;
    
    	public String getFormName() {
		return formName;
	}




	public void setFormName(String formName) {
		this.formName = formName;
	}




	public String getIntervalName() {
		return intervalName;
	}




	public void setIntervalName(String intervalName) {
		this.intervalName = intervalName;
	}




	public String getpVisitDate() {
		return pVisitDate;
	}




	public void setpVisitDate(String pVisitDate) {
		this.pVisitDate = pVisitDate;
	}




	public String getCollectionStatus() {
		return collectionStatus;
	}




	public void setCollectionStatus(String collectionStatus) {
		this.collectionStatus = collectionStatus;
	}




	public int getAdministeredFormId() {
		return administeredFormId;
	}




	public void setAdministeredFormId(int administeredFormId) {
		this.administeredFormId = administeredFormId;
	}




	public int getFormId() {
		return formId;
	}


	public void setFormId(int formId) {
		this.formId = formId;
	}




	public int getIntervalId() {
		return intervalId;
	}




	public void setIntervalId(int intervalId) {
		this.intervalId = intervalId;
	}




	public int getPatientId() {
		return patientId;
	}




	public void setPatientId(int patientId) {
		this.patientId = patientId;
	}




	public int getDataEntryDraftId() {
		return dataEntryDraftId;
	}




	public void setDataEntryDraftId(int dataEntryDraftId) {
		this.dataEntryDraftId = dataEntryDraftId;
	}




	public int getDataEntryFlag() {
		return dataEntryFlag;
	}




	public void setDataEntryByUserId(int a) {
		this.dataEntryByUserId = a;
	}


	public int  getDataEntryByUserId() {
		return this.dataEntryByUserId;
	}
	public void setDataEntryFlag(int dataEntryFlag) {
		this.dataEntryFlag = dataEntryFlag;
	}

    public Document toXML() throws TransformationException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("No Implementation at this time for the method toXML() in Event.");
    }
}
