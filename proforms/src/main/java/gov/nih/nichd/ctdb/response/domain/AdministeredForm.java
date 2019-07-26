package gov.nih.nichd.ctdb.response.domain;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.attachments.domain.Attachment;
import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.tbi.commons.model.StudyType;

/**
 * AdministeredForm DomainObject for the NICHD CTDB Application
 * 
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class AdministeredForm extends CtdbDomainObject {
	private static final long serialVersionUID = 2955962442255393014L;
	
	private Form form;
	private int eformid;
	private Patient patient;
	private Interval interval;
	private int numPatients;
	private List<String> administeredIntervals = new ArrayList<String>();
	private List<Response> responses = new ArrayList<Response>();
	private boolean discrepancyFlag = false;
	private int certifiedBy = Integer.MIN_VALUE;
	private Date certifiedDate;
	private String certifiedByUsername;
	private int finalLockBy = Integer.MIN_VALUE;
	private Date finalLockDate;
	private String finalLockByUsername;
	private Date lockDate;
	private Date lock2Date;
	private boolean dataEntry2NotStarted = true;
	private int dataEntrySession = 1;
	private int dataEntryDraftId = Integer.MIN_VALUE;
	private String dataEnteredByName1 = "";
	private String dataEnteredByName2 = "";
	private int numQuestionsAnswered;
	private int lockedBy = Integer.MIN_VALUE; // userId for locked by used in
												// response data module
	private int locked2By = Integer.MIN_VALUE; // userId for locked by used in
												// response data module
	private int eventId = Integer.MIN_VALUE;
	private boolean dataOutOfRangeFlag = false;
	private int formVersion;
	private boolean resolveStarted = false;
	private boolean qaLocked = false;
	private boolean qaReviewed = false;
	// added for IBIS project by yogi
	private int userOneEntryId;
	private int userTwoEntryId;
	private String user1;
	private String user2;
	private Date visitDate;
	private Date visitDate2;
	private Date scheduledVisitDate;
	private int keySingleDouble;
	private String pVisitDate;
	private String sVisitDate;
	private String summaryGuid;
	private int loggedInUserId;
	// to determine start data collection from editing data collection
	private boolean startMode = false;
	// to set locked date from dataentryDraft to adminForm
	private Date dataEntryDraft_LockedDate;
	
	//to track if there are answers in form
	private boolean areThereAnswers;
	
	//To track if there are file attachment links in form
	private boolean anyFileAttachementsInForm = false;
	

	// to determine whether we need to skip required validation(false will skip
	// required validation)
	private boolean markAsCompleted;
	// first data entry status stored in dataenteydraft table
	private String entryOneStatus;
	// second data entry status stored in dataenteydraft table
	private String entryTwoStatus;

	private String patientRecordNo;

	// To detect if save was executed by auto saver or not
	private boolean comingFromAutoSaver = false;

	private String user1LastNameFirstNameDisplay;
	private String user2LastNameFirstNameDisplay;
	private List<String> assocFileQuestionIds;
	
	private String siteName;
	
	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	// added by Ching-Heng
	private boolean isCAT;

	public int getEformid() {
		return eformid;
	}

	public void setEformid(int eformid) {
		this.eformid = eformid;
	}

	public boolean isComingFromAutoSaver() {
		return comingFromAutoSaver;
	}

	public void setComingFromAutoSaver(boolean comingFromAutoSaver) {
		this.comingFromAutoSaver = comingFromAutoSaver;
	}

	public String getPatientRecordNo() {
		return patientRecordNo;
	}

	public String getUser1LastNameFirstNameDisplay() {
		return user1LastNameFirstNameDisplay;
	}

	public void setUser1LastNameFirstNameDisplay(
			String user1LastNameFirstNameDisplay) {
		this.user1LastNameFirstNameDisplay = user1LastNameFirstNameDisplay;
	}

	public String getUser2LastNameFirstNameDisplay() {
		return user2LastNameFirstNameDisplay;
	}

	public void setUser2LastNameFirstNameDisplay(
			String user2LastNameFirstNameDisplay) {
		this.user2LastNameFirstNameDisplay = user2LastNameFirstNameDisplay;
	}

	public void setPatientRecordNo(String patientRecordNo) {
		this.patientRecordNo = patientRecordNo;
	}

	public boolean isStartMode() {
		return startMode;
	}

	public void setStartMode(boolean startMode) {
		this.startMode = startMode;
	}

	// added for File type Question by Ching Heng
	private Map<String, Attachment> files;
	private boolean nonPatient = false;

	public boolean isNonPatient() {
		return nonPatient;
	}

	public void setNonPatient(boolean nonPatient) {
		this.nonPatient = nonPatient;
	}

	public Map<String, Attachment> getFiles() {
		return files;
	}

	public void setFiles(Map<String, Attachment> files) {
		this.files = files;
	}

	
	public void clone(AdministeredForm aform) {
		this.form = new Form();
		this.form.cloneMetaData(aform.getForm());
		this.patient = aform.getPatient();
		this.interval = aform.getInterval();
		this.numPatients = aform.getNumPatients();
		this.administeredIntervals = new ArrayList<String>();
		this.administeredIntervals.addAll(aform.getAdministeredIntervals());
		this.responses = new ArrayList<Response>();
		this.responses.addAll(aform.getResponses());
		this.discrepancyFlag = aform.getDiscrepancyFlag();
		this.certifiedBy = aform.getCertifiedBy();
		this.certifiedDate = aform.getCertifiedDate();
		this.finalLockBy = aform.getFinalLockBy();
		this.finalLockDate = aform.getFinalLockDate();
		this.lockDate = aform.getLockDate();
		this.lock2Date = aform.getLock2Date();
		this.dataEntry2NotStarted = aform.getDataEntry2NotStarted();
		this.dataEntrySession = aform.getDataEntrySession();
		this.dataEntryDraftId = aform.getDataEntryDraftId();
		this.dataEnteredByName1 = aform.getDataEnteredByName1();
		this.dataEnteredByName2 = aform.getDataEnteredByName2();
		this.numQuestionsAnswered = aform.getNumQuestionsAnswered();
		this.lockedBy = aform.getLockedBy();
		this.locked2By = aform.getLocked2By();
		this.eventId = aform.getEventId();
		this.dataOutOfRangeFlag = aform.isDataOutOfRangeFlag();
		this.formVersion = aform.getFormVersion();
		this.certifiedByUsername = aform.getCertifiedByUsername();
		this.finalLockByUsername = aform.getFinalLockByUsername();
		this.resolveStarted = aform.getResolveStarted();
		this.visitDate = aform.getVisitDate();
		this.scheduledVisitDate = aform.getScheduledVisitDate();
		this.qaLocked = aform.isQaLocked();
		this.qaReviewed = aform.isQaReviewed();
	}

	public void cloneWithId(AdministeredForm af) {
		this.clone(af);
		this.setId(af.getId());
	}

	/**
	 * Default Constructor for the AdministeredForm Domain Object
	 */
	public AdministeredForm() {
		// default constructor
	}

	public boolean isQaLocked() {
		return qaLocked;
	}

	public void setQaLocked(boolean qaLocked) {
		this.qaLocked = qaLocked;
	}

	public boolean isQaReviewed() {
		return qaReviewed;
	}

	public void setQaReviewed(boolean qaReviewed) {
		this.qaReviewed = qaReviewed;
	}

	/**
	 * Get the Locked2By value.
	 * 
	 * @return The Locked2By value.
	 */
	public int getLocked2By() {
		return locked2By;
	}

	/**
	 * Set the Locked2By value.
	 * 
	 * @param newLocked2By
	 *            The new Locked2By value.
	 */
	public void setLocked2By(int newLocked2By) {
		this.locked2By = newLocked2By;
	}

	/**
	 * Get the LockedBy value.
	 * 
	 * @return The LockedBy value.
	 */
	public int getLockedBy() {
		return lockedBy;
	}

	/**
	 * Set the LockedBy value.
	 * 
	 * @param newLockedBy
	 *            The new LockedBy value.
	 */
	public void setLockedBy(int newLockedBy) {
		this.lockedBy = newLockedBy;
	}

	public List<String> getAssocFileQuestionIds() {
		return assocFileQuestionIds;
	}

	public void setAssocFileQuestionIds(List<String> assocFileQuestionIds) {
		this.assocFileQuestionIds = assocFileQuestionIds;
	}

	/**
	 * Gets the form domain object
	 * 
	 * @return Form the form domain object
	 */
	public Form getForm() {
		return form;
	}

	/**
	 * Sets the form domain object
	 * 
	 * @param form
	 *            the form domain object
	 */
	public void setForm(Form form) {
		this.form = form;
	}

	/**
	 * Gets the interval domain object
	 * 
	 * @return Interval
	 */
	public Interval getInterval() {
		return interval;
	}

	/**
	 * Sets the interval domain object
	 * 
	 * @param interval
	 *            The Interval domain object
	 */
	public void setInterval(Interval interval) {
		this.interval = interval;
	}

	public boolean isAreThereAnswers() {
		return areThereAnswers;
	}

	public void setAreThereAnswers(boolean areThereAnswers) {
		this.areThereAnswers = areThereAnswers;
	}

	/**
	 * Gets the patient domain object
	 * 
	 * @return patient
	 */
	public Patient getPatient() {
		return patient;
	}

	/**
	 * Sets the patient domain object
	 * 
	 * @param patient
	 *            The patient domain object
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	/**
	 * Gets administered intervals for the form
	 * 
	 * @return List the administered intervals
	 */
	public List<String> getAdministeredIntervals() {
		return administeredIntervals;
	}

	public Date getDataEntryDraft_LockedDate() {
		return dataEntryDraft_LockedDate;
	}

	public String getEntryTwoStatus() {
		return entryTwoStatus;
	}

	public void setEntryTwoStatus(String entryTwoStatus) {
		this.entryTwoStatus = entryTwoStatus;
	}

	public boolean isMarkAsCompleted() {
		return markAsCompleted;
	}

	public void setMarkAsCompleted(boolean markAsCompleted) {
		this.markAsCompleted = markAsCompleted;
	}

	public void setDataEntryDraft_LockedDate(Date dataEntryDraft_LockedDate) {
		this.dataEntryDraft_LockedDate = dataEntryDraft_LockedDate;
	}

	/**
	 * Sets the administered intervals
	 * 
	 * @param admIntervals
	 *            The administered intervals
	 */
	public void setAdministeredIntervals(List<String> admIntervals) {
		this.administeredIntervals = admIntervals;
	}

	/**
	 * Gets the total number of patients completed
	 * 
	 * @return Total number of patients completed
	 */
	public int getNumPatients() {
		return numPatients;
	}

	/**
	 * Sets the total number of patients completed
	 * 
	 * @param numPatients
	 *            Total number of patients completed
	 */
	public void setNumPatients(int numPatients) {
		this.numPatients = numPatients;
	}

	/**
	 * Gets the list of Response
	 * 
	 * @return List the list of responses for the administered form
	 */
	public List<Response> getResponses() {
		return responses;
	}

	/**
	 * Sets the list of responses
	 * 
	 * @param responses
	 *            the list of responses for the administered form
	 */
	public void setResponses(List<Response> responses) {
		this.responses = responses;
	}

	public String getSummaryGuid() {
		return summaryGuid;
	}

	public void setSummaryGuid(String summaryGuid) {
		this.summaryGuid = summaryGuid;
	}

	/**
	 * Gets the discrepancy flag for the data entry.
	 * 
	 * @return boolean the discrepancy flag for the data entry; false: no
	 *         discrepancy; true: has discrepancy
	 */
	public boolean getDiscrepancyFlag() {
		return discrepancyFlag;
	}

	/**
	 * Sets the discrepancy flag for the data entry.
	 * 
	 * @param discrepancyFlag
	 *            the discrepancy flag for the data entry
	 */
	public void setDiscrepancyFlag(boolean discrepancyFlag) {
		this.discrepancyFlag = discrepancyFlag;
	}

	public String getEntryOneStatus() {
		return entryOneStatus;
	}

	public void setEntryOneStatus(String entryOneStatus) {
		this.entryOneStatus = entryOneStatus;
	}

	/**
	 * Gets the administer form final lock by user's ID
	 * 
	 * @return int The administer form final lock by user's ID
	 */
	public int getFinalLockBy() {
		return finalLockBy;
	}

	/**
	 * Sets the administer form's final lock by user's ID
	 * 
	 * @param finalLockBy
	 *            The administer form final lock by user's ID
	 */
	public void setFinalLockBy(int finalLockBy) {
		this.finalLockBy = finalLockBy;
	}

	/**
	 * Gets administer form final lock date
	 * 
	 * @return Date the administer form final lock date
	 */
	public Date getFinalLockDate() {
		return finalLockDate;
	}

	/**
	 * Sets the administer form final lock date
	 * 
	 * @param finalLockDate
	 *            The administer form's final lock date
	 */
	public void setFinalLockDate(Date finalLockDate) {
		this.finalLockDate = finalLockDate;
	}

	/**
	 * Gets the username who final locks the administer form.
	 * 
	 * @return The final locker's username
	 */
	public String getFinalLockByUsername() {
		return finalLockByUsername;
	}

	/**
	 * Sets the username who final locks the administer form
	 * 
	 * @param finalLockByUsername
	 *            The final locker's username
	 */
	public void setFinalLockByUsername(String finalLockByUsername) {
		this.finalLockByUsername = finalLockByUsername;
	}

	/**
	 * Gets administer form certified by user's ID
	 * 
	 * @return int The administer form certified by user's ID
	 */
	public int getCertifiedBy() {
		return certifiedBy;
	}

	/**
	 * Sets the administer form's certified by user's ID
	 * 
	 * @param certifiedBy
	 *            The administer form certified by user's ID
	 */
	public void setCertifiedBy(int certifiedBy) {
		this.certifiedBy = certifiedBy;
	}

	/**
	 * Gets the username who certifies the administer form.
	 * 
	 * @return The certified's username
	 */
	public String getCertifiedByUsername() {
		return certifiedByUsername;
	}

	/**
	 * Sets the username who certified the administer form
	 * 
	 * @param certifiedByUsername
	 *            The certified's username
	 */
	public void setCertifiedByUsername(String certifiedByUsername) {
		this.certifiedByUsername = certifiedByUsername;
	}

	public boolean isAnyFileAttachementsInForm() {
		return anyFileAttachementsInForm;
	}

	public void setAnyFileAttachementsInForm(boolean anyFileAttachementsInForm) {
		this.anyFileAttachementsInForm = anyFileAttachementsInForm;
	}

	/**
	 * Gets the administer form's certified date
	 * 
	 * @return Date the administer form's certified date
	 */
	public Date getCertifiedDate() {
		return certifiedDate;
	}

	/**
	 * Sets the administer form certified date
	 * 
	 * @param certifiedDate
	 *            The administer form's certified date
	 */
	public void setCertifiedDate(Date certifiedDate) {
		this.certifiedDate = certifiedDate;
	}

	/**
	 * Gets the data entry 1's lock date
	 * 
	 * @return Date the data entry 1 lock date
	 */
	public Date getLockDate() {
		return lockDate;
	}

	/**
	 * Sets the data entry 1's lock date
	 * 
	 * @param lockDate
	 *            The data entry 1 lock date
	 */
	public void setLockDate(Date lockDate) {
		this.lockDate = lockDate;
	}

	/**
	 * Gets the data entry 2's lock date
	 * 
	 * @return Date the data entry 2 lock date
	 */
	public Date getLock2Date() {
		return lock2Date;
	}

	/**
	 * Sets the data entry 2's lock date
	 * 
	 * @param lock2Date
	 *            The data entry 2 lock date
	 */
	public void setLock2Date(Date lock2Date) {
		this.lock2Date = lock2Date;
	}

	/**
	 * Gets the number 1 or 2 for data entry
	 * 
	 * @return int the data entry number. 1 for data entry 1; 2 for data entry 2
	 */
	public int getDataEntrySession() {
		return dataEntrySession;
	}

	/**
	 * Sets the data entry flag as 1 or 2
	 * 
	 * @param dataEntrySession
	 *            the number of 1 or 2 for data entry
	 */
	public void setDataEntrySession(int dataEntrySession) {
		this.dataEntrySession = dataEntrySession;
	}

	/**
	 * Gets the data entry draft's ID
	 * 
	 * @return int data entry draft's ID
	 */
	public int getDataEntryDraftId() {
		return dataEntryDraftId;
	}

	/**
	 * Sets the data entry draft's ID
	 * 
	 * @param dataEntryDraftId
	 *            The data entry draft ID
	 */
	public void setDataEntryDraftId(int dataEntryDraftId) {
		this.dataEntryDraftId = dataEntryDraftId;
	}

	/**
	 * Gets the total number of questions answered
	 * 
	 * @return Total number of questions answered
	 */
	public int getNumQuestionsAnswered() {
		return numQuestionsAnswered;
	}

	/**
	 * Sets the total number of questions answered
	 * 
	 * @param numQuestionsAnswered
	 *            Total number of questions answered
	 */
	public void setNumQuestionsAnswered(int numQuestionsAnswered) {
		this.numQuestionsAnswered = numQuestionsAnswered;
	}

	/**
	 * Gets the flag for if the data entry 2 has started.
	 * 
	 * @return boolean the flag for if the data entry 2; false: not started;
	 *         true: started
	 */
	public boolean getDataEntry2NotStarted() {
		return dataEntry2NotStarted;
	}

	/**
	 * Sets the flag for if the data entry 2 has started.
	 * 
	 * @param dataEntry2NotStarted
	 *            the flag for if the data entry 2 has started
	 */
	public void setDataEntry2NotStarted(boolean dataEntry2NotStarted) {
		this.dataEntry2NotStarted = dataEntry2NotStarted;
	}

	/**
	 * Get the dataEntered 1 By name value.
	 * 
	 * @return The dataEntered 1 By name value.
	 */
	public String getDataEnteredByName1() {
		return dataEnteredByName1;
	}

	/**
	 * Set the dataEntered 1 By name value.
	 * 
	 * @param dataEnteredByName1
	 *            The dataEntered 1 By name value.
	 */
	public void setDataEnteredByName1(String dataEnteredByName1) {
		this.dataEnteredByName1 = dataEnteredByName1;
	}

	/**
	 * Get the dataEntered 2 By name value.
	 * 
	 * @return The dataEntered 2 By name value.
	 */
	public String getDataEnteredByName2() {
		return dataEnteredByName2;
	}

	/**
	 * Set the dataEntered 2 By name value.
	 * 
	 * @param dataEnteredByName2
	 *            By Name The dataEntered 2 By name value.
	 */
	public void setDataEnteredByName2(String dataEnteredByName2) {
		this.dataEnteredByName2 = dataEnteredByName2;
	}

	/**
	 * Gets the event ID for this administered form.
	 * 
	 * @return int event ID
	 */
	public int getEventId() {
		return eventId;
	}

	/**
	 * Sets the event ID for this administered form.
	 * 
	 * @param eventId
	 *            the event ID
	 */
	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	/**
	 * is the data out of range flag set?
	 * 
	 * @return
	 */
	public boolean isDataOutOfRangeFlag() {
		return dataOutOfRangeFlag;
	}

	/**
	 * set the data out of range flag
	 * 
	 * @param dataOutOfRangeFlag
	 */
	public void setDataOutOfRangeFlag(boolean dataOutOfRangeFlag) {
		this.dataOutOfRangeFlag = dataOutOfRangeFlag;
	}

	public int getFormVersion() {
		return formVersion;
	}

	public void setFormVersion(int formVersion) {
		this.formVersion = formVersion;
	}

	public Date getVisitDate() {
		return visitDate;
	}

	public void setVisitDate(Date visitDate) {
		this.visitDate = visitDate;
	}
	
	public Date getScheduledVisitDate() {
		return scheduledVisitDate;
	}

	public void setScheduledVisitDate(Date scheduledVisitDate) {
		this.scheduledVisitDate = scheduledVisitDate;
	}

	/**
	 * get the resolve started flag
	 * 
	 * @return
	 */
	public boolean getResolveStarted() {
		return resolveStarted;
	}

	/**
	 * set the resolve started flag
	 * 
	 * @param resolveStarted
	 */
	public void setResolveStarted(boolean resolveStarted) {
		this.resolveStarted = resolveStarted;
	}

	public String getTimePoint(HttpServletRequest request) {
		return this.getTimePoint(request.getSession());
	}

	public String getTimePoint(HttpSession session) {
		return this
				.getTimePoint(this.form.getProtocol().getStudyType() == StudyType.NATURAL_HISTORY);
	}

	public String getTimePoint(boolean isNaturalHistory) {
		if (isNaturalHistory) {
			SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
			return df.format(this.getVisitDate());
		} else {
			return this.getInterval().getName();
		}
	}

	public String getVisitDateStringMMddyyyy() {
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		if (this.getVisitDate() != null) {
			return df.format(this.getVisitDate());
		} else {
			return "N/A";
		}
	}

	public String getVisitDateStringyyyyMMddHHmm() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if (this.getVisitDate() != null) {
			return df.format(this.getVisitDate());
		} else {
			return "";
		}
	}

	public String getVisitDate2StringyyyyMMddHHmm() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if (this.getVisitDate2() != null) {
			return df.format(this.getVisitDate2());
		} else {
			return "";
		}
	}
	
	
	public String getScheduledVisitDateStringMMddyyyy() {
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		if (this.getScheduledVisitDate() != null) {
			return df.format(this.getScheduledVisitDate());
		} else {
			return "";
		}
	}

	public String getScheduledVisitDateStringyyyyMMddHHmm() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if (this.getScheduledVisitDate() != null) {
			return df.format(this.getScheduledVisitDate());
		} else {
			return "";
		}
	}

	/**
	 * This method allows the transformation of a AdministeredForm into an XML
	 * Document. If no implementation is available at this time, an
	 * UnsupportedOperationException will be thrown.
	 * 
	 * @return XML Document
	 * @throws TransformationException
	 *             is thrown if there is an error during the XML tranformation.
	 */
	public Document toXML() throws TransformationException {
		try {
			Document document = super.newDocument();
			Element root = super.initXML(document, "administeredform");

			Element responsesNode = document.createElement("responses");
			for (Iterator<Response> it = this.responses.iterator(); it.hasNext();) {
				Response response = it.next();
				Document responseDom = response.toXML();
				responsesNode.appendChild(document.importNode(
						responseDom.getDocumentElement(), true));
			}
			root.appendChild(responsesNode);

			return document;
		} catch (Exception ex) {
			throw new TransformationException(
					"Unable to transform object " + this.getClass().getName()
							+ " with id = " + this.getId());
		}
	}

	public int getUserOneEntryId() {
		return userOneEntryId;
	}

	public void setUserOneEntryId(int userOneEntryId) {
		this.userOneEntryId = userOneEntryId;
	}

	public int getUserTwoEntryId() {
		return userTwoEntryId;
	}

	public void setUserTwoEntryId(int userTwoEntryId) {
		this.userTwoEntryId = userTwoEntryId;
	}

	public int getKeySingleDouble() {
		return keySingleDouble;
	}

	public void setKeySingleDouble(int keySingleDouble) {
		this.keySingleDouble = keySingleDouble;
	}

	public String getUser1() {
		return user1;
	}

	public void setUser1(String user1) {
		this.user1 = user1;
	}

	public String getUser2() {
		return user2;
	}

	public void setUser2(String user2) {
		this.user2 = user2;
	}

	public String getpVisitDate() {
		return pVisitDate;
	}

	public void setpVisitDate(String pVisitDate) {
		this.pVisitDate = pVisitDate;
	}
	
	public String getsVisitDate() {
		return sVisitDate;
	}

	public void setsVisitDate(String sVisitDate) {
		this.sVisitDate = sVisitDate;
	}

	public Date getVisitDate2() {
		return visitDate2;
	}

	public void setVisitDate2(Date visitDate2) {
		this.visitDate2 = visitDate2;
	}

	public int getLoggedInUserId() {
		return loggedInUserId;
	}

	public void setLoggedInUserId(int loggedInUserId) {
		this.loggedInUserId = loggedInUserId;
	}

	public boolean isCAT() {
		return isCAT;
	}

	public void setCAT(boolean isCAT) {
		this.isCAT = isCAT;
	}
	
	

}
