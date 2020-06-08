package gov.nih.nichd.ctdb.protocol.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.attachments.domain.Attachment;
import gov.nih.nichd.ctdb.audit.domain.Audit;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.drugDevice.domain.DrugDevice;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.site.domain.Site;
import gov.nih.nichd.ctdb.util.domain.Address;
import gov.nih.tbi.commons.model.StudyType;

/**
 * Protocol DomainObject for the NICHD CTDB Application
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class Protocol extends CtdbDomainObject {
	private static final long serialVersionUID = 5225220641786872193L;
	
	private String protocolNumber;
	private String bricsStudyId = "";
    private String name = "";
    private String welcomeUrl = "";
    private String description = "";
    private CtdbLookup status;
    private String orginization = "";
    private List<Form> forms;
    private Attachment dataSubmissionFile;
    private boolean isEvent = false;
    private boolean autoAssociatePatientRoles = false;
	private boolean enableEsignature = true;
    private Audit audit;
    private StudyType studyType;
    private boolean lockFormIntervals = true; // allow the users to edit form interval associations for the protocol.
    
    private int patientDisplayType = CtdbConstants.PATIENT_DISPLAY_ID;
    
    private boolean autoIncrementSubject = false;
    private int subjectNumberStart;
    private String subjectNumberPrefix;
    private String subjectNumberSuffix;
    
    private ProtocolDefaults protocolDefaults = new ProtocolDefaults();
    private int[] btrisAccess;
    
    private String principleInvestigator = "";
    private String accountableInvestigator = "";
    
    private boolean useEbinder;
    private String studyProject = "";
    private int principleInvestigatorId;
    private int accountableInvestigatorId;
    private List<Site> studySites = new ArrayList<Site>();
    private List<String> selectedBricsStudySiteIds = new ArrayList<String>();
    private Site site;
    //Drug & Devices
    private List<DrugDevice> drugDeviceList = new ArrayList<DrugDevice>();
    private DrugDevice drugDevice;
    //Check for Clinical Trial
    private boolean isClinicalTrial = false;
    
    //Hash Maps
	Map<String, Site> siteHashMap;
	Map<String, DrugDevice> drugDeviceHashMap;
	
	private boolean deleteFlag;

	private String psrHeader = "";
    
	private List<ClinicalLocation> clinicalLocationList = new ArrayList<ClinicalLocation>();
	private List<Procedure> procedureList = new ArrayList<Procedure>();
	private List<PointOfContact> pointOfContactList = new ArrayList<PointOfContact>();
	private List<MilesStone> milesStoneList = new ArrayList<MilesStone>();
	
	private boolean hasRandomization = false;
    
    /**
     * Default Constructor for the Protocol Domain Object
     */
    public Protocol() {}

    public Protocol(int id) {
        this.setId(id);
    }
    /**
     * Gets the protocol's number. This number is unique
     * to NICHD and is an external number to the CTDB system.
     *
     * @return  String The protocol's number
     */
    public String getProtocolNumber()
    {
        return protocolNumber;
    }

    /**
     * Sets the Protocol's number. This number is unique
     * to NICHD and is an external number to the CTDB system.
     *
     * @param protocolNumber The protocol's number
     */

    public void setProtocolNumber(String protocolNumber)
    {
        this.protocolNumber = protocolNumber;
    }
    
    /** get the default protocol study site
     * If a primary site exists, use one of them as default (there may be multiple primary sites). If not, then use integer.min_value
     */
    public int getDefaultStudySiteId(){
        if(studySites != null && studySites.size() >0){
	        for (Site s: studySites) {
	        	if(s.isPrimarySite()){
	        		return s.getId();
	        	}
	        }
        }
    	return Integer.MIN_VALUE; 
    }

    /**
     * Gets the protocol's name
     *
     * @return The protocol's name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the protocol's name
     *
     * @param name The protocol's name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the protocol's image file
     *
     * @return The protocol's image file.
     */
    public Attachment getDataSubmissionFile()
    {
        return dataSubmissionFile;
    }

    /**
     * Sets the Protocol's image file
     *
     * @param dataSubmissionFile The studies submission file
     */
    public void setDataSubmissionFile(Attachment dataSubmissionFile)
    {
        this.dataSubmissionFile = dataSubmissionFile;
    }

    /**
     * Gets the flag for the protocol to see if it is Event-Driven protocol.
     *
     * @return The protocol event flag
     */
    public boolean getIsEvent()
    {
        return isEvent;
    }

    /**
     * Sets the protocol event flag.
     *
     * @param  isEvent  the protocol event flag
     */
    public void setIsEvent(boolean isEvent)
    {
        this.isEvent = isEvent;
    }

    /**
     * Gets the protocol's welcome URL
     *
     * @return The protocol's welcome URL
     */
    public String getWelcomeUrl()
    {
        return welcomeUrl;
    }

    /**
     * Sets the Protocol's welcome URL
     *
     * @param welcomeUrl The protocol's welcome URL
     */
    public void setWelcomeUrl(String welcomeUrl)
    {
        this.welcomeUrl = welcomeUrl;
    }

    /**
     * Gets the protocol's description
     *
     * @return The protocol's description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets the Protocol's description
     *
     * @param description The protocol's description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Gets the protocol's status
     *
     * @return The protocol's status
     */
    public CtdbLookup getStatus()
    {
        return status;
    }

    /**
     * Sets the Protocol's status (active, completed)
     *
     * @param status The protocol's status
     */
    public void setStatus(CtdbLookup status)
    {
        this.status = status;
    }

    /**
     * Gets the forms associated with a protocol
     *
     * @return The forms
     */
    public List<Form> getForms()
    {
        return forms;
    }

    /**
     * Sets the forms associated with a protocol
     *
     * @param forms The forms
     */
    public void setForms(List<Form> forms)
    {
        this.forms = forms;
    }

    public boolean isUsePatientName() {
        if (this.getPatientDisplayType() == CtdbConstants.PATIENT_DISPLAY_ID) {
            return false;
        } else {
            return true;
        }
    }

    public void setUsePatientName(boolean usePatientName) {
    }

	public boolean isLockFormIntervals() {
        return lockFormIntervals;
    }

    public void setLockFormIntervals(boolean lockFormIntervals) {
        this.lockFormIntervals = lockFormIntervals;
    }


    public int getPatientDisplayType() {
        return patientDisplayType;
    }

    public void setPatientDisplayType(int patientDisplayType) {
        this.patientDisplayType = patientDisplayType;
    }


    public boolean isAutoIncrementSubject()
    {
        return autoIncrementSubject;
    }

    public void setAutoIncrementSubject(boolean autoIncrementSubject)
    {
        this.autoIncrementSubject = autoIncrementSubject;
    }

    public int getSubjectNumberStart()
    {
        return this.subjectNumberStart;
    }

    public void setSubjectNumberStart(int subjectNumberStart)
    {
        this.subjectNumberStart = subjectNumberStart;
    }

    public String getSubjectNumberPrefix()
    {
        return subjectNumberPrefix;
    }

    public void setSubjectNumberPrefix(String subjectNumberPrefix)
    {
        this.subjectNumberPrefix = subjectNumberPrefix;
    }

    public String getSubjectNumberSuffix()
    {
        return subjectNumberSuffix;
    }

    public void setSubjectNumberSuffix(String subjectNumberSuffix)
    {
        this.subjectNumberSuffix = subjectNumberSuffix;
    }
    public boolean isAutoAssociatePatientRoles() {
           return autoAssociatePatientRoles;
       }

    public void setAutoAssociatePatientRoles(boolean autoAssociatePatientRoles) {
        this.autoAssociatePatientRoles = autoAssociatePatientRoles;
    }
    
    public boolean isEnableEsignature() {
		return enableEsignature;
	}
	public void setEnableEsignature(boolean enableEsignature) {
		this.enableEsignature = enableEsignature;
	}

	public Audit getAudit() {
		if(audit == null){//We not getting the audit from database
			audit = new Audit();
		}
		return audit;
	}

	public void setAudit(Audit audit) {
		this.audit = audit;
	}
	
    public StudyType getStudyType() {
		return studyType;
	}

	public void setStudyType(StudyType studyType) {
		this.studyType = studyType;
	}

	public ProtocolDefaults getProtocolDefaults() {
        return protocolDefaults;
    }

    public void setProtocolDefaults(ProtocolDefaults protocolDefaults) {
        this.protocolDefaults = protocolDefaults;
    }

    public List<String> getSelectedBricsStudySiteIds() {
		return selectedBricsStudySiteIds;
	}

	public void setSelectedBricsStudySiteIds(List<String> selectedBricsStudySiteIds) {
		this.selectedBricsStudySiteIds = selectedBricsStudySiteIds;
	}

	public boolean isEvent() {
        return isEvent;
    }

    public void setEvent(boolean event) {
        isEvent = event;
    }

    public int[] getBtrisAccess() {
        return btrisAccess;
    }

    public void setBtrisAccess(int[] btrisAccess) {
        this.btrisAccess = btrisAccess;
    }


    public String getPrincipleInvestigator() {
        return principleInvestigator;
    }

    public void setPrincipleInvestigator(String principleInvestigator) {
        this.principleInvestigator = principleInvestigator;
    }

    public String getAccountableInvestigator() {
        return accountableInvestigator;
    }

    public void setAccountableInvestigator(String accountableInvestigator) {
        this.accountableInvestigator = accountableInvestigator;
    }

    public boolean isUseEbinder() {
        return useEbinder;
    }

    public void setUseEbinder(boolean useEbinder) {
        this.useEbinder = useEbinder;
    }

    /**
     * Determines if an object is equal to the current Protocol Object.
     * Equal is based on if the protocol number and protocol name are equal.
     *
     * @param   o The object to determine if it is equal to the current Protocol
     * @return  True if the current study is equal to the other study.
     *          False if the current study is not equal to the other study.
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        
        if ((o == null) || !(o instanceof Protocol)) {
            return false;
        }
        
        final Protocol otherStudy = (Protocol) o;
        
        // Perform equality checks
        return protocolNumber.equals(otherStudy.protocolNumber) && name.equals(otherStudy.name) && 
        	   orginization.equals(otherStudy.orginization) &&
        	   (patientDisplayType == otherStudy.patientDisplayType) && (useEbinder == otherStudy.useEbinder) && 
        	   (lockFormIntervals == otherStudy.lockFormIntervals) && (autoAssociatePatientRoles == otherStudy.autoAssociatePatientRoles) && 
        	   (enableEsignature == otherStudy.enableEsignature) &&
        	   (autoIncrementSubject ? ((autoIncrementSubject == otherStudy.autoIncrementSubject) && 
        			   subjectNumberPrefix.equals(otherStudy.subjectNumberPrefix) && (subjectNumberStart == otherStudy.subjectNumberStart) &&
        			   subjectNumberSuffix.equals(otherStudy.subjectNumberSuffix)) : autoIncrementSubject == otherStudy.autoIncrementSubject) &&
				bricsStudyId.equals(otherStudy.bricsStudyId) && studyType == otherStudy.studyType
				&& psrHeader.equals(otherStudy.psrHeader);
    }

    public String getPatientDisplayLabel () {
        switch(patientDisplayType) {
            case CtdbConstants.PATIENT_DISPLAY_NAME :
                return CtdbConstants.PATIENTNAME_DISPLAY;
            case CtdbConstants.PATIENT_DISPLAY_SUBJECT :
                return CtdbConstants.PATIENTSUBJECT_DISPLAY;
            case CtdbConstants.PATIENT_DISPLAY_MRN :
                return CtdbConstants.SUBJECT_MRN_DISPLAY;
            case CtdbConstants.PATIENT_DISPLAY_GUID :
                return CtdbConstants.SUBJECT_GUID_DISPLAY;    
            default :
                return CtdbConstants.PATIENTID_DISPLAY;
        }
    }


	/**
	 * This method allows the transformation of a Protocol into an XML Document.
	 * If no implementation is available at this time, an
	 * UnsupportedOperationException will be thrown.
	 *
	 * @return XML Document
	 * @throws TransformationException
	 *             is thrown if there is an error during the XML tranformation
	 * @throws UnsupportedOperationException
	 *             is thrown if this method is currently unsupported and not
	 *             implemented.
	 */
	public Document toXML() throws TransformationException, UnsupportedOperationException {
		throw new UnsupportedOperationException("No Implementation at this time for the method toXML() in Protocol.");
	}

	public String getStudyProject() {
		return studyProject;
	}

	public void setStudyProject(String studyProject) {
		this.studyProject = studyProject;
	}

	public int getPrincipleInvestigatorId() {
		return principleInvestigatorId;
	}

	public void setPrincipleInvestigatorId(int principleInvestigatorId) {
		this.principleInvestigatorId = principleInvestigatorId;
	}

	public int getAccountableInvestigatorId() {
		return accountableInvestigatorId;
	}

	public void setAccountableInvestigatorId(int accountableInvestigatorId) {
		this.accountableInvestigatorId = accountableInvestigatorId;
	}

	public List<Site> getStudySites() {
		return studySites;
	}

	public void setStudySites(List<Site> studySites) {
		this.studySites = studySites;
	}

	public Site getSite() {
		if(site == null){//We not getting the site from database
			site = new Site();
			site.setAddress(new Address());
		}
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public List<DrugDevice> getDrugDeviceList() {
		return drugDeviceList;
	}

	public void setDrugDeviceList(List<DrugDevice> drugDeviceList) {
		this.drugDeviceList = drugDeviceList;
	}

	public DrugDevice getDrugDevice() {
		if(drugDevice == null){//We not getting the site from database
			drugDevice = new DrugDevice();
		}
		return drugDevice;
	}

	public void setDrugDevice(DrugDevice drugDevice) {
		this.drugDevice = drugDevice;
	}

	public boolean isClinicalTrial() {
		return isClinicalTrial;
	}

	public void setClinicalTrial(boolean isClinicalTrial) {
		this.isClinicalTrial = isClinicalTrial;
	}

	public Map<String, Site> getSiteHashMap() {
		return siteHashMap;
	}

	public void setSiteHashMap(Map<String, Site> siteHashMap) {
		this.siteHashMap = siteHashMap;
	}

	public Map<String, DrugDevice> getDrugDeviceHashMap() {
		return drugDeviceHashMap;
	}

	public void setDrugDeviceHashMap(Map<String, DrugDevice> drugDeviceHashMap) {
		this.drugDeviceHashMap = drugDeviceHashMap;
	}

	public boolean getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	/**
	 * @return the bricsStudyId
	 */
	public String getBricsStudyId() {
		return bricsStudyId;
	}

	/**
	 * @param bricsStudyId the bricsStudyId to set
	 */
	public void setBricsStudyId(String bricsStudyId) {
		this.bricsStudyId = bricsStudyId;
	}

	/**
	 * @return the otherOrginization
	 */
	public String getOrginization() {
		return orginization;
	}

	/**
	 * @param orginization the otherOrginization to set
	 */
	public void setOrginization(String orginization) {
		this.orginization = orginization;
	}
	
	/**
	 * @return the psrHeader
	 */
	public String getPsrHeader() {
		return psrHeader;
	}

	/**
	 * @param psrHeader the psrHeader to set
	 */
	public void setPsrHeader(String psrHeader) {
		this.psrHeader = psrHeader;
	}

	public List<ClinicalLocation> getClinicalLocationList() {
		return this.clinicalLocationList;
	}
	
	public void setClinicalLocationList(List<ClinicalLocation> clinicalLocationList){
		this.clinicalLocationList.clear();

		if (clinicalLocationList != null) {
			this.clinicalLocationList.addAll(clinicalLocationList);
		}
	}
	
	public List<Procedure> getProcedureList() {
		return this.procedureList;
	}
	
	public void setProcedureList(List<Procedure> procedureList){
		this.procedureList.clear();
		
		if ( procedureList != null ) {
			this.procedureList.addAll(procedureList);
		}
	}
	
	public List<PointOfContact> getPointOfContactList() {
		return this.pointOfContactList;
	}
	
	public void setPointOfContact(List<PointOfContact> pocList){
		this.pointOfContactList.clear();

		if (pocList != null) {
			this.pointOfContactList.addAll(pocList);
		}
	}
	
	public List<MilesStone> getMilesStoneList() {
		return this.milesStoneList;
	}
	
	public void setMilesStoneList(List<MilesStone> milesStoneList){
		this.milesStoneList.clear();

		if (milesStoneList != null) {
			this.milesStoneList.addAll(milesStoneList);
		}
	}
	
	public boolean getHasRandomization() {
		return hasRandomization;
	}

	public void setHasRandomizationg(boolean hasRandomization) {
		this.hasRandomization = hasRandomization;
	}
}
