package gov.nih.nichd.ctdb.response.form;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbForm;

/**
 * The DataEntrySetupForm represents the Java class behind the HTML for nichd
 * ctdb data entry setup page
 * 
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class DataEntrySetupForm extends CtdbForm {
	private static final long serialVersionUID = 4055223432857892457L;
	private int patientId = Integer.MIN_VALUE;
	private int intervalId = Integer.MIN_VALUE;
	private int intervalIdHidden = Integer.MIN_VALUE;
	private String visitDate;
	private String scheduledVisitDate;

	private int formVersion = Integer.MIN_VALUE;
	private boolean isMobile = false;

	private String formName;

	private int formId;

	private String formType;

	private String formTypeString;

	private String formStatus;

	private String mode;

	private boolean attachFiles;

	private String valuesOutsideRange;
	private String rangeViolators;
	
	//Subject labels (patientprotocol)
	private String subjectId;
	///Subject labels (patient  )
	private String guid;
	private String mrn;

	private String memo;
	private int sampleId;
	private String intervalName;
	private String clickedSectionFields;
	private List sectionListForAdminForm;

	// added by Ching Heng for file type
	// question============================================
	private Map files;
	// added by Yogi to disable/enable Next and previous button
	private boolean nextButtonDisable;
	private boolean previousButtonDisable;
	private boolean editHideNextPrevious;
	private boolean showSavedMessage;
	//digital signature password validation in final lock confirmation page
	private String userPassword;
	private String userFullName;

	public String getMrn() {
		return mrn;
	}

	public void setMrn(String mrn) {
		this.mrn = mrn;
	}

	/**
	 * Return the value stored in the Map with key.
	 * 
	 * @return Object The value retrieved with key.
	 * @throws CtdbException
	 *             Thrown if any other errors occur while processing.
	 */
	public Object getValue(String key) throws CtdbException {
	
		return null;
	}

	/**
	 * Add the value to the Map with key key.
	 * 
	 * @param key
	 *            The key for the value Object.
	 * @param value
	 *            The value to be stored in the map with key key.
	 * @throws CtdbException
	 *             Thrown if any other errors occur while processing.
	 */
	public void setValue(String key, Object value) throws CtdbException {
	}

	/**
	 * Return the map for image files.
	 * 
	 * @return The map which holds image files.
	 */
	public Map getFiles() {
		return files;
	}

	public void setFiles(Map files) {
		this.files = files;
	}
	
	// ==============================================================================

	public boolean isAttachFiles() {
		return attachFiles;
	}

	public void setAttachFiles(boolean attachFiles) {
		this.attachFiles = attachFiles;
	}

	/**
	 * Get the IntervalId value.
	 * 
	 * @return The IntervalId value.
	 */
	public int getIntervalId() {
		return intervalId;
	}

	/**
	 * Set the IntervalId value.
	 * 
	 * @param newIntervalId
	 *            The new IntervalId value.
	 */
	public void setIntervalId(int newIntervalId) {
		this.intervalId = newIntervalId;
	}

	public int getIntervalIdHidden() {
		return intervalIdHidden;
	}

	public void setIntervalIdHidden(int intervalIdHidden) {
		this.intervalIdHidden = intervalIdHidden;
	}

	/**
	 * Get the PatientId value.
	 * 
	 * @return The PatientId value.
	 */
	public int getPatientId() {
		return patientId;
	}

	/**
	 * Set the PatientId value.
	 * 
	 * @param newPatientId
	 *            The new PatientId value.
	 */
	public void setPatientId(int newPatientId) {
		this.patientId = newPatientId;
	}

	public int getFormVersion() {
		return formVersion;
	}

	public void setFormVersion(int formVersion) {
		this.formVersion = formVersion;
	}

	public String getVisitDate() {
		return visitDate;
	}

	public void setVisitDate(String visitDate) {
		this.visitDate = visitDate;
	}
	
	public String getScheduledVisitDate() {
		return scheduledVisitDate;
	}

	public void setScheduledVisitDate(String scheduledVisitDate) {
		this.scheduledVisitDate = scheduledVisitDate;
	}

	public boolean isMobile() {
		return isMobile;
	}

	public void setMobile(boolean mobile) {
		isMobile = mobile;
	}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public int getFormId() {
		return formId;
	}

	public void setFormId(int formId) {
		this.formId = formId;
	}

	public boolean isShowSavedMessage() {
		return showSavedMessage;
	}

	public void setShowSavedMessage(boolean showSavedMessage) {
		this.showSavedMessage = showSavedMessage;
	}

	public String getFormType() {
		return formType;
	}

	public boolean isEditHideNextPrevious() {
		return editHideNextPrevious;
	}

	public void setEditHideNextPrevious(boolean editHideNextPrevious) {
		this.editHideNextPrevious = editHideNextPrevious;
	}

	public void setFormType(String formType) {
		this.formType = formType;
	}

	public String getFormStatus() {
		return formStatus;
	}

	public void setFormStatus(String formStatus) {
		this.formStatus = formStatus;
	}

	public String getMode() {
		return mode;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getIntervalName() {
		return intervalName;
	}

	public void setIntervalName(String intervalName) {
		this.intervalName = intervalName;
	}

	public String getValuesOutsideRange() {
		return valuesOutsideRange;
	}

	public void setValuesOutsideRange(String valuesOutsideRange) {
		this.valuesOutsideRange = valuesOutsideRange;
	}

	public String getRangeViolators() {
		return rangeViolators;
	}

	public void setRangeViolators(String rangeViolators) {
		this.rangeViolators = rangeViolators;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String patientRecordId) {
		this.subjectId = patientRecordId;
	}

	public String getFormTypeString() {
		return formTypeString;
	}

	public void setFormTypeString(String formTypeString) {
		this.formTypeString = formTypeString;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public int getSampleId() {
		return sampleId;
	}

	public void setSampleId(int sampleId) {
		this.sampleId = sampleId;
	}

	public String getClickedSectionFields() {
		return clickedSectionFields;
	}

	public void setClickedSectionFields(String clickedSectionFields) {
		this.clickedSectionFields = clickedSectionFields;
	}

	public List getSectionListForAdminForm() {
		return sectionListForAdminForm;
	}

	public void setSectionListForAdminForm(List sectionListForAdminForm) {
		this.sectionListForAdminForm = sectionListForAdminForm;
	}

	/**
	 * Reset all properties to their default values.
	 */
	public void reset() {
		this.patientId = Integer.MIN_VALUE;
		this.intervalId = Integer.MIN_VALUE;
		this.intervalIdHidden = Integer.MIN_VALUE;
		this.formVersion = Integer.MIN_VALUE;
		this.visitDate = null;
		this.clickedSectionFields = "";
		isMobile = false;
		// added by Ching Heng for file question type
		files = new HashMap();
	}

	public boolean isNextButtonDisable() {
		return nextButtonDisable;
	}

	public void setNextButtonDisable(boolean nextButtonDisable) {
		this.nextButtonDisable = nextButtonDisable;
	}

	public boolean isPreviousButtonDisable() {
		return previousButtonDisable;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public void setPreviousButtonDisable(boolean previousButtonDisable) {
		this.previousButtonDisable = previousButtonDisable;
	}

	public String getUserFullName() {
		return userFullName;
	}
	
	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}
}
