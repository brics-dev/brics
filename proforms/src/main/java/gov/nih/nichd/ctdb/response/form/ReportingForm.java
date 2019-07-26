package gov.nih.nichd.ctdb.response.form;

import gov.nih.nichd.ctdb.common.CtdbForm;

public class ReportingForm extends CtdbForm {
	private static final long serialVersionUID = -5609472112683644942L;
	
	// discrepancy report fields
	private String userName;
	private String intervalName;
	private String updateDate;
	private String patientNo;
	private String ddvGuId;
	private String ddvFormName;
	private String resolveCount;

	// study related fields
	private String studyName;
	private String studyPI;
	private String studyType;
	private String studyStatus;
	private String studySubjectCount;
	private String studyAdminFormsCount;
	private String bricsStudyId;
	private String studyStartDate;
	private String studyEndDate;
	private int studyNumberSubjects;

	// administrative forms report fields & form without guid
	private String afName;
	private String guId;
	private String mRN;
	private String nRN;
	private String afStatus;
	private String asingleDoubleEntry;
	private String auDate;
	
	//completed form view fields
	private String cfvSubjectGuid;
	private String cfvSubjectMrn;
	private String cfvSubjectNrn;
	private String cfvFilledFormsName;
	private String cfvVisitDate;
	private String answeredQuesCount;
	private String ccvIntervalName;
	private int cfvFormId;
	
	//forms requiring lock fields
	private String frl_formName = "";
	private String frl_intervalName = "";
	private String frl_guid = "";
	private String frl_mrn = "";
	private String frl_nrn = "";
	private String frl_createdDate = "";
	private String frl_updateDate = "";
	private String frl_requiredOptional = "";
	
	// "Performance Overview" and some "Completed Visits" report fields
	private String primarySiteName = "";
	private String studyNumber = "";
	private int informedConsentEnrollmentCount = 0;
	private int csfCollectionDataCount = 0;
	private int protocolDeviationsCount = 0;
	private int adverseEventsCount = 0;
	private int earlyTermination = 0;
	
	// "Completed Visits" report fields
	private int totalNumSubjects = 0;
	private int numSubjectsDataCollect = 0;
	private int baseline = -1;
	private int sixMonths = -1;
	private int twelveMonths = -1;
	private int eighteenMonths = -1;
	private int twentyFourMonths = -1;
	private int thirtyMonths = -1;
	private int thirtySixMonths = -1;
	private int fortyTwoMonths = -1;
	private int fortyEightMonths = -1;
	private int fiftyFourMonths = -1;
	private int sixtyMonths = -1;

	public String getStudySubjectCount() {
		return studySubjectCount;
	}

	public void setStudySubjectCount(String studySubjectCount) {
		this.studySubjectCount = studySubjectCount;
	}

	public String getStudyAdminFormsCount() {
		return studyAdminFormsCount;
	}

	public void setStudyAdminFormsCount(String studyAdminFormsCount) {
		this.studyAdminFormsCount = studyAdminFormsCount;
	}

	public String getAfName() {
		return afName;
	}

	public void setAfName(String afName) {
		this.afName = afName;
	}

	public String getGuId() {
		return guId;
	}

	public void setGuId(String guId) {
		this.guId = guId;
	}

	public String getAfStatus() {
		return afStatus;
	}

	public void setAfStatus(String afStatus) {
		this.afStatus = afStatus;
	}

	public String getAsingleDoubleEntry() {
		return asingleDoubleEntry;
	}

	public void setAsingleDoubleEntry(String asingleDoubleEntry) {
		this.asingleDoubleEntry = asingleDoubleEntry;
	}

	public String getAuDate() {
		return auDate;
	}

	public void setAuDate(String auDate) {
		this.auDate = auDate;
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	public String getFrl_formName() {
		return frl_formName;
	}

	public void setFrl_formName(String frl_formName) {
		this.frl_formName = frl_formName;
	}

	public String getFrl_intervalName() {
		return frl_intervalName;
	}

	public void setFrl_intervalName(String frl_intervalName) {
		this.frl_intervalName = frl_intervalName;
	}

	public String getFrl_guid() {
		return frl_guid;
	}

	public void setFrl_guid(String frl_guid) {
		this.frl_guid = frl_guid;
	}

	public String getFrl_createdDate() {
		return frl_createdDate;
	}

	public void setFrl_createdDate(String frl_createdDate) {
		this.frl_createdDate = frl_createdDate;
	}

	public String getFrl_updateDate() {
		return frl_updateDate;
	}

	public void setFrl_updateDate(String frl_updateDate) {
		this.frl_updateDate = frl_updateDate;
	}

	public String getFrl_requiredOptional() {
		return frl_requiredOptional;
	}

	public void setFrl_requiredOptional(String frl_requiredOptional) {
		this.frl_requiredOptional = frl_requiredOptional;
	}

	public String getStudyPI() {
		return studyPI;
	}

	public void setStudyPI(String studyPI) {
		this.studyPI = studyPI;
	}

	public String getStudyType() {
		return studyType;
	}

	public void setStudyType(String studyType) {
		this.studyType = studyType;
	}

	public String getStudyStatus() {
		return studyStatus;
	}

	public void setStudyStatus(String studyStatus) {
		this.studyStatus = studyStatus;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getIntervalName() {
		return intervalName;
	}

	public void setIntervalName(String intervalName) {
		this.intervalName = intervalName;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getPatientNo() {
		return patientNo;
	}

	public void setPatientNo(String patientNo) {
		this.patientNo = patientNo;
	}

	public String getDdvGuId() {
		return ddvGuId;
	}

	public void setDdvGuId(String ddvGuId) {
		this.ddvGuId = ddvGuId;
	}

	public String getResolveCount() {
		return resolveCount;
	}

	public void setResolveCount(String resolveCount) {
		this.resolveCount = resolveCount;
	}

	public String getDdvFormName() {
		return ddvFormName;
	}

	public void setDdvFormName(String ddvFormName) {
		this.ddvFormName = ddvFormName;
	}

	public String getCfvSubjectGuid() {
		return cfvSubjectGuid;
	}

	public void setCfvSubjectGuid(String cfvSubjectGuid) {
		this.cfvSubjectGuid = cfvSubjectGuid;
	}

	public String getCfvFilledFormsName() {
		return cfvFilledFormsName;
	}

	public void setCfvFilledFormsName(String cfvFilledFormsName) {
		this.cfvFilledFormsName = cfvFilledFormsName;
	}

	public String getCfvVisitDate() {
		return cfvVisitDate;
	}

	public void setCfvVisitDate(String cfvVisitDate) {
		this.cfvVisitDate = cfvVisitDate;
	}

	public String getAnsweredQuesCount() {
		return answeredQuesCount;
	}

	public void setAnsweredQuesCount(String answeredQuesCount) {
		this.answeredQuesCount = answeredQuesCount;
	}

	public String getCcvIntervalName() {
		return ccvIntervalName;
	}

	public void setCcvIntervalName(String ccvIntervalName) {
		this.ccvIntervalName = ccvIntervalName;
	}

	public int getCfvFormId() {
		return cfvFormId;
	}

	public void setCfvFormId(int cfvFormId) {
		this.cfvFormId = cfvFormId;
	}

	/**
	 * @return the primarySiteName
	 */
	public String getPrimarySiteName() {
		return primarySiteName;
	}

	/**
	 * @param primarySiteName the primarySiteName to set
	 */
	public void setPrimarySiteName(String primarySiteName) {
		this.primarySiteName = primarySiteName;
	}

	/**
	 * @return the studyNumber
	 */
	public String getStudyNumber() {
		return studyNumber;
	}

	/**
	 * @param studyNumber the studyNumber to set
	 */
	public void setStudyNumber(String studyNumber) {
		this.studyNumber = studyNumber;
	}

	/**
	 * @return the informedConsentEnrollmentCount
	 */
	public int getInformedConsentEnrollmentCount() {
		return informedConsentEnrollmentCount;
	}

	/**
	 * @param informedConsentEnrollmentCount the informedConsentEnrollmentCount to set
	 */
	public void setInformedConsentEnrollmentCount(
			int informedConsentEnrollmentCount) {
		this.informedConsentEnrollmentCount = informedConsentEnrollmentCount;
	}

	/**
	 * @return the csfCollectionDataCount
	 */
	public int getCsfCollectionDataCount() {
		return csfCollectionDataCount;
	}

	/**
	 * @param csfCollectionDataCount the csfCollectionDataCount to set
	 */
	public void setCsfCollectionDataCount(int csfCollectionDataCount) {
		this.csfCollectionDataCount = csfCollectionDataCount;
	}

	/**
	 * @return the protocolDeviationsCount
	 */
	public int getProtocolDeviationsCount() {
		return protocolDeviationsCount;
	}

	/**
	 * @param protocolDeviationsCount the protocolDeviationsCount to set
	 */
	public void setProtocolDeviationsCount(int protocolDeviationsCount) {
		this.protocolDeviationsCount = protocolDeviationsCount;
	}

	/**
	 * @return the adverseEventsCount
	 */
	public int getAdverseEventsCount() {
		return adverseEventsCount;
	}

	/**
	 * @param adverseEventsCount the adverseEventsCount to set
	 */
	public void setAdverseEventsCount(int adverseEventsCount) {
		this.adverseEventsCount = adverseEventsCount;
	}

	/**
	 * @return the earlyTermination
	 */
	public int getEarlyTermination() {
		return earlyTermination;
	}

	/**
	 * @param earlyTermination the earlyTermination to set
	 */
	public void setEarlyTermination(int earlyTermination) {
		this.earlyTermination = earlyTermination;
	}

	/**
	 * @return the totalNumSubjects
	 */
	public int getTotalNumSubjects() {
		return totalNumSubjects;
	}

	/**
	 * @param totalNumSubjects the totalNumSubjects to set
	 */
	public void setTotalNumSubjects(int totalNumSubjects) {
		this.totalNumSubjects = totalNumSubjects;
	}

	/**
	 * @return the numSubjectsDataCollect
	 */
	public int getNumSubjectsDataCollect() {
		return numSubjectsDataCollect;
	}

	/**
	 * @param numSubjectsDataCollect the numSubjectsDataCollect to set
	 */
	public void setNumSubjectsDataCollect(int numSubjectsDataCollect) {
		this.numSubjectsDataCollect = numSubjectsDataCollect;
	}

	/**
	 * @return the baseline
	 */
	public int getBaseline() {
		return baseline;
	}

	/**
	 * @param baseline the baseline to set
	 */
	public void setBaseline(int baseline) {
		this.baseline = baseline;
	}

	/**
	 * @return the sixMonths
	 */
	public int getSixMonths() {
		return sixMonths;
	}

	/**
	 * @param sixMonths the sixMonths to set
	 */
	public void setSixMonths(int sixMonths) {
		this.sixMonths = sixMonths;
	}

	/**
	 * @return the twelveMonths
	 */
	public int getTwelveMonths() {
		return twelveMonths;
	}

	/**
	 * @param twelveMonths the twelveMonths to set
	 */
	public void setTwelveMonths(int twelveMonths) {
		this.twelveMonths = twelveMonths;
	}

	/**
	 * @return the eighteenMonths
	 */
	public int getEighteenMonths() {
		return eighteenMonths;
	}

	/**
	 * @param eighteenMonths the eighteenMonths to set
	 */
	public void setEighteenMonths(int eighteenMonths) {
		this.eighteenMonths = eighteenMonths;
	}

	/**
	 * @return the twentyFourMonths
	 */
	public int getTwentyFourMonths() {
		return twentyFourMonths;
	}

	/**
	 * @param twentyFourMonths the twentyfourMonths to set
	 */
	public void setTwentyFourMonths(int twentyfourMonths) {
		this.twentyFourMonths = twentyfourMonths;
	}

	/**
	 * @return the thirtyMonths
	 */
	public int getThirtyMonths() {
		return thirtyMonths;
	}

	/**
	 * @param thirtyMonths the thirtyMonths to set
	 */
	public void setThirtyMonths(int thirtyMonths) {
		this.thirtyMonths = thirtyMonths;
	}

	/**
	 * @return the thirtySixMonths
	 */
	public int getThirtySixMonths() {
		return thirtySixMonths;
	}

	/**
	 * @param thirtySixMonths the thirtysixMonths to set
	 */
	public void setThirtySixMonths(int thirtysixMonths) {
		this.thirtySixMonths = thirtysixMonths;
	}

	public int getFortyTwoMonths() {
		return fortyTwoMonths;
	}

	public void setFortyTwoMonths(int fortyTwoMonths) {
		this.fortyTwoMonths = fortyTwoMonths;
	}

	public int getFortyEightMonths() {
		return fortyEightMonths;
	}

	public void setFortyEightMonths(int fortyEightMonths) {
		this.fortyEightMonths = fortyEightMonths;
	}

	public int getFiftyFourMonths() {
		return fiftyFourMonths;
	}

	public void setFiftyFourMonths(int fiftyFourMonths) {
		this.fiftyFourMonths = fiftyFourMonths;
	}

	public int getSixtyMonths() {
		return sixtyMonths;
	}

	public void setSixtyMonths(int sixtyMonths) {
		this.sixtyMonths = sixtyMonths;
	}

	public String getCfvSubjectMrn() {
		return cfvSubjectMrn;
	}

	public void setCfvSubjectMrn(String cfvSubjectMrn) {
		this.cfvSubjectMrn = cfvSubjectMrn;
	}

	public String getFrl_mrn() {
		return frl_mrn;
	}

	public void setFrl_mrn(String frl_mrn) {
		this.frl_mrn = frl_mrn;
	}

	public String getmRN() {
		return mRN;
	}

	public void setmRN(String mRN) {
		this.mRN = mRN;
	}

	public String getCfvSubjectNrn() {
		return cfvSubjectNrn;
	}

	public void setCfvSubjectNrn(String cfvSubjectNrn) {
		this.cfvSubjectNrn = cfvSubjectNrn;
	}

	public String getFrl_nrn() {
		return frl_nrn;
	}

	public void setFrl_nrn(String frl_nrn) {
		this.frl_nrn = frl_nrn;
	}

	public String getnRN() {
		return nRN;
	}

	public void setnRN(String nRN) {
		this.nRN = nRN;
	}

	public String getBricsStudyId() {
		return bricsStudyId;
	}

	public void setBricsStudyId(String bricsStudyId) {
		this.bricsStudyId = bricsStudyId;
	}

	public String getStudyStartDate() {
		return studyStartDate;
	}

	public void setStudyStartDate(String studyStartDate) {
		this.studyStartDate = studyStartDate;
	}

	public String getStudyEndDate() {
		return studyEndDate;
	}

	public void setStudyEndDate(String studyEndDate) {
		this.studyEndDate = studyEndDate;
	}

	public int getStudyNumberSubjects() {
		return studyNumberSubjects;
	}

	public void setStudyNumberSubjects(int studyNumberSubjects) {
		this.studyNumberSubjects = studyNumberSubjects;
	}

	
	
	
}
