package gov.nih.nichd.ctdb.response.form;

import gov.nih.nichd.ctdb.common.CtdbForm;
import gov.nih.nichd.ctdb.common.CtdbLookup;

/**
 * The DataEntryInProgressForm represents the form class behind the table
 * displayed on the DataEntryInProgress page.
 * 
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class DataEntryInProgressForm extends CtdbForm {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2477355544497720175L;
	// patient fields
	private String formName;
	private String intervalName;
	private String nihRecordNumber;
	private String visitDatePreColl;
	private String assignedToUserOne;
	private String assignedToUserTwo;

	// non patient fields
	private String npFormName;
	private String npVisitDate;
	private String dataEntryStatus1;
	private String dataEntryStatus2;

	// serach type
	private String searchFormType = "patientPVDiv";
	private String selectedFormId;
	private int isStatus;
	private CtdbLookup status;
	private CtdbLookup xformtype;

	
	
	
private String reassignableAformsJSON;
	
	public String getReassignableAformsJSON() {
		return reassignableAformsJSON;
	}

	public void setReassignableAformsJSON(String reassignableAformsJSON) {
		this.reassignableAformsJSON = reassignableAformsJSON;
	}
	
	





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

	public String getNihRecordNumber() {
		return nihRecordNumber;
	}

	public void setNihRecordNumber(String nihRecordNumber) {
		this.nihRecordNumber = nihRecordNumber;
	}

	public String getVisitDatePreColl() {
		return visitDatePreColl;
	}

	public void setVisitDatePreColl(String visitDatePreColl) {
		this.visitDatePreColl = visitDatePreColl;
	}

	public CtdbLookup getStatus() {
		if (status == null) {
			status = new CtdbLookup();
		}
		return status;
	}

	public void setStatus(CtdbLookup status) {
		this.status = status;
	}

	public CtdbLookup getXformtype() {
		if (xformtype == null) {
			xformtype = new CtdbLookup();
		}
		return xformtype;
	}

	public void setXformtype(CtdbLookup xformtype) {
		this.xformtype = xformtype;
	}

	public String getNpFormName() {
		return npFormName;
	}

	public void setNpFormName(String npFormName) {
		this.npFormName = npFormName;
	}

	public String getSearchFormType() {
		return searchFormType;
	}

	public void setSearchFormType(String searchFormType) {
		this.searchFormType = searchFormType;
	}

	public String getNpVisitDate() {
		return npVisitDate;
	}

	public void setNpVisitDate(String npVisitDate) {
		this.npVisitDate = npVisitDate;
	}

	public String getAssignedToUserOne() {
		return assignedToUserOne;
	}

	public void setAssignedToUserOne(String assignedToUserOne) {
		this.assignedToUserOne = assignedToUserOne;
	}

	public String getAssignedToUserTwo() {
		return assignedToUserTwo;
	}

	public void setAssignedToUserTwo(String assignedToUserTwo) {
		this.assignedToUserTwo = assignedToUserTwo;
	}

	public String getDataEntryStatus1() {
		return dataEntryStatus1;
	}

	public void setDataEntryStatus1(String dataEntryStatus1) {
		this.dataEntryStatus1 = dataEntryStatus1;
	}

	public String getDataEntryStatus2() {
		return dataEntryStatus2;
	}

	public void setDataEntryStatus2(String dataEntryStatus2) {
		this.dataEntryStatus2 = dataEntryStatus2;
	}

	public String getSelectedFormId() {
		return selectedFormId;
	}

	public void setSelectedFormId(String selectedFormId) {
		this.selectedFormId = selectedFormId;
	}

	public int getIsStatus() {
		return isStatus;
	}

	public void setIsStatus(int isStatus) {
		this.isStatus = isStatus;
	}

}
