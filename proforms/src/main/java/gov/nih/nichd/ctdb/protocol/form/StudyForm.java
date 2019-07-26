package gov.nih.nichd.ctdb.protocol.form;

import java.util.ArrayList;
import java.util.List;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbForm;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.drugDevice.domain.DrugDevice;
import gov.nih.nichd.ctdb.site.domain.Site;

public class StudyForm extends CtdbForm
{
	private static final long serialVersionUID = -526423265867351990L;
	
	// Study input field properties
	private String studyNumber = "";
	private String name = "";
	private String principleInvestigator;
	private String studyUrl = "";
	private String description = "";
	private int status = 1;
	private int institute = 11;
	private int type = 1;
	private int patientDisplayType = CtdbConstants.PATIENT_DISPLAY_ID;
	private boolean useEBinder = false;
	private boolean lockVisitTypes = true;
	private boolean autoAssociatePatientRoles = false;
	private boolean usGovStudy = false;
	private boolean autoIncrementSubject = false;
	private String subjectNumberPrefix = "";
	private int subjectNumberStart = 0;
	private String subjectNumberSuffix = "";
	private List<Integer> studyDefaults = new ArrayList<Integer>();
	private Site site;
	private DrugDevice drugDevice;
	
	// Other properties not directly involved in any input fields
	private boolean showSites = false;
	private boolean showClinicalTrials = false;
	private String changeMode = StrutsConstants.ACTION_ADD_FORM;
	private boolean allowPii = false;
	
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the principleInvestigator
	 */
	public String getPrincipleInvestigator() {
		return principleInvestigator;
	}

	/**
	 * @param principleInvestigator the principleInvestigator to set
	 */
	public void setPrincipleInvestigator(String principleInvestigator) {
		this.principleInvestigator = principleInvestigator;
	}

	/**
	 * @return the studyUrl
	 */
	public String getStudyUrl() {
		return studyUrl;
	}

	/**
	 * @param studyUrl the studyUrl to set
	 */
	public void setStudyUrl(String studyUrl) {
		this.studyUrl = studyUrl;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the institute
	 */
	public int getInstitute() {
		return institute;
	}

	/**
	 * @param institute the institute to set
	 */
	public void setInstitute(int institute) {
		this.institute = institute;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the patientDisplayType
	 */
	public int getPatientDisplayType() {
		return patientDisplayType;
	}

	/**
	 * @param patientDisplayType the patientDisplayType to set
	 */
	public void setPatientDisplayType(int patientDisplayType) {
		this.patientDisplayType = patientDisplayType;
	}

	/**
	 * @return the useEBinder
	 */
	public boolean isUseEBinder() {
		return useEBinder;
	}

	/**
	 * @param useEBinder the useEBinder to set
	 */
	public void setUseEBinder(boolean useEBinder) {
		this.useEBinder = useEBinder;
	}

	/**
	 * @return the lockVisitTypes
	 */
	public boolean isLockVisitTypes() {
		return lockVisitTypes;
	}

	/**
	 * @param lockVisitTypes the lockVisitTypes to set
	 */
	public void setLockVisitTypes(boolean lockVisitTypes) {
		this.lockVisitTypes = lockVisitTypes;
	}

	/**
	 * @return the autoAssociatePatientRoles
	 */
	public boolean isAutoAssociatePatientRoles() {
		return autoAssociatePatientRoles;
	}

	/**
	 * @param autoAssociatePatientRoles the autoAssociatePatientRoles to set
	 */
	public void setAutoAssociatePatientRoles(boolean autoAssociatePatientRoles) {
		this.autoAssociatePatientRoles = autoAssociatePatientRoles;
	}

	/**
	 * @return the usGovStudy
	 */
	public boolean isUsGovStudy() {
		return usGovStudy;
	}

	/**
	 * @param usGovStudy the usGovStudy to set
	 */
	public void setUsGovStudy(boolean usGovStudy) {
		this.usGovStudy = usGovStudy;
	}

	/**
	 * @return the autoIncrementSubject
	 */
	public boolean isAutoIncrementSubject() {
		return autoIncrementSubject;
	}

	/**
	 * @param autoIncrementSubject the autoIncrementSubject to set
	 */
	public void setAutoIncrementSubject(boolean autoIncrementSubject) {
		this.autoIncrementSubject = autoIncrementSubject;
	}

	/**
	 * @return the subjectNumberPrefix
	 */
	public String getSubjectNumberPrefix() {
		return subjectNumberPrefix;
	}

	/**
	 * @param subjectNumberPrefix the subjectNumberPrefix to set
	 */
	public void setSubjectNumberPrefix(String subjectNumberPrefix) {
		this.subjectNumberPrefix = subjectNumberPrefix;
	}

	/**
	 * @return the subjectNumberStart
	 */
	public int getSubjectNumberStart() {
		return subjectNumberStart;
	}

	/**
	 * @param subjectNumberStart the subjectNumberStart to set
	 */
	public void setSubjectNumberStart(int subjectNumberStart) {
		this.subjectNumberStart = subjectNumberStart;
	}

	/**
	 * @return the subjectNumberSuffix
	 */
	public String getSubjectNumberSuffix() {
		return subjectNumberSuffix;
	}

	/**
	 * @param subjectNumberSuffix the subjectNumberSuffix to set
	 */
	public void setSubjectNumberSuffix(String subjectNumberSuffix) {
		this.subjectNumberSuffix = subjectNumberSuffix;
	}

	/**
	 * @return the studyDefaults
	 */
	public List<Integer> getStudyDefaults() {
		return studyDefaults;
	}

	/**
	 * @param studyDefaults the studyDefaults to set
	 */
	public void setStudyDefaults(List<Integer> studyDefaults) {
		this.studyDefaults = studyDefaults;
	}

	/**
	 * @return the showSites
	 */
	public boolean isShowSites() {
		return showSites;
	}

	/**
	 * @param showSites the showSites to set
	 */
	public void setShowSites(boolean showSites) {
		this.showSites = showSites;
	}

	/**
	 * @return the showClinicalTrials
	 */
	public boolean isShowClinicalTrials() {
		return showClinicalTrials;
	}

	/**
	 * @param showClinicalTrials the showClinicalTrials to set
	 */
	public void setShowClinicalTrials(boolean showClinicalTrials) {
		this.showClinicalTrials = showClinicalTrials;
	}

	/**
	 * @return the changeMode
	 */
	public String getChangeMode() {
		return changeMode;
	}

	/**
	 * @param changeMode the changeMode to set
	 */
	public void setChangeMode(String changeMode) {
		this.changeMode = changeMode;
	}

	/**
	 * @return the allowPii
	 */
	public boolean isAllowPii() {
		return allowPii;
	}

	/**
	 * @param allowPii the allowPii to set
	 */
	public void setAllowPii(boolean allowPii) {
		this.allowPii = allowPii;
	}

	/**
	 * @return the site
	 */
	public Site getSite() {
		return site;
	}

	/**
	 * @param site the site to set
	 */
	public void setSite(Site site) {
		this.site = site;
	}

	/**
	 * @return the drugDevice
	 */
	public DrugDevice getDrugDevice() {
		return drugDevice;
	}

	/**
	 * @param drugDevice the drugDevice to set
	 */
	public void setDrugDevice(DrugDevice drugDevice) {
		this.drugDevice = drugDevice;
	}
}