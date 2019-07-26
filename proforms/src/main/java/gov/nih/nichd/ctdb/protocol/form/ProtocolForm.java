package gov.nih.nichd.ctdb.protocol.form;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbForm;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.drugDevice.domain.DrugDevice;
import gov.nih.nichd.ctdb.protocol.domain.ClinicalLocation;
import gov.nih.nichd.ctdb.protocol.domain.MilesStone;
import gov.nih.nichd.ctdb.protocol.domain.PointOfContact;
import gov.nih.nichd.ctdb.protocol.domain.Procedure;
import gov.nih.nichd.ctdb.site.domain.Site;
import gov.nih.nichd.ctdb.util.domain.Address;

/**
 * The ProtocolForm represents the Java class behind the HTML
 * for nichd ctdb protocols
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ProtocolForm extends CtdbForm
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1219834267840643743L;
	private String protocolNumber;
    private String name;
    private String imageFileName;
    private String welcomeUrl = "";
    private String description;
    private int status = 1;
    private String bricsStudyId = "";
    private String ctdbLookupStringForStatusDisplay;
    
    private String organization = "";
    private String ctdbLookupStringForInstituteDisplay;
    private String[] roles;
    private File uploadedFile;
    private boolean remove = false;
    private String isEvent = "no";
    private String usePatientName = "no";

    private int protocolType;
    private String studyType;
    private String protocolTypeName;
    private String ctdbLookupStringForProtocolTypeDisplay;
    
    private String lockFormIntervals="yes";

    private boolean autoAssociatePatientRoles = false;
	private boolean enableEsignature = true;
	private String reasonForEsignature; //audit

	private int patientDisplayType;

    private boolean autoIncrementSubject = false;
    private int subjectNumberStart;
    private String subjectNumberPrefix;
    private String subjectNumberSuffix;
    private int[] selectedDefaults;
    private int[] btrisAccess;

    private String principleInvestigator;
    private String accountableInvestigator;
    private boolean useEbinder;
    //new fields added
    private String studyProject;
    private int principleInvestigatorId;
    private int accountableInvestigatorId;
    //site
    private List<Site> studySites;
    private Site site;
    private String protocolSiteActionFlag;
    private String selectedSiteIds = "[]";
    private String selectedSites = "[]";
    private int selectedAddressId;
    private int processSelectedSiteId;
    //private int sitePrincipleInvestigatorId;
    //Drug & Device
    private List<DrugDevice> drugDeviceList;
    private DrugDevice drugDevice;
    private String protocolDrugDeviceActionFlag;
    private String selectedDrugDeviceIds;
    private int selectedDrugDeviceId;
    private int processSelectedDrugDeviceId;
    //Check for Clinical Trial
    private boolean isClinicalTrial = false;
    
   //Hash Maps
	Map<String, Site> siteHashMap;
	Map<String, DrugDevice> drugDeviceHashMap;
	
    private String protocolNavigationFlag;
    //this used in the protocol inbox page
    private String selectedProtocolIds;
    
	private boolean deleteFlag;
	//used for expanding & collapsing the div
	private String sectionDisplay = "default";
	private String protoDetail="protolDetailInfoExpanded";
    private boolean addedFromDashboard = false;
    private boolean allowPii = false;
	private String locProcPOCDisplay = "default";

	//section procedure
	private List<ClinicalLocation> protoClinicLocList = new ArrayList<ClinicalLocation>();
	//section procedure
	private List<Procedure> protoProcedureList = new ArrayList<Procedure>();

	//section point of contact
	private List<PointOfContact> protoPOCList = new ArrayList<PointOfContact>();
	private List<MilesStone> protoMilesStoneList = new ArrayList<MilesStone>();
	
    /**
     * Gets the protocol's number. This number is unique
     * to NICHD and is an external number to the CTDB system.
     *
     * @return  The protocol's number
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
        this.protocolNumber = StringUtils.trim(protocolNumber);
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
        this.name = StringUtils.trim(name);//name.trim();
    }

    /**
     * Gets the protocol's image file name
     *
     * @return The protocol's image file name
     */
    public String getImageFileName()
    {
        return imageFileName;
    }

    /**
     * Sets the Protocol's image file name
     *
     * @param imageFileName The protocol's image file name
     */
    public void setImageFileName(String imageFileName)
    {
        this.imageFileName = StringUtils.trim(imageFileName);
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
        this.welcomeUrl = StringUtils.trim(welcomeUrl);
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
        this.description = StringUtils.trim(description);
    }

    /**
     * Gets the protocol's status
     *
     * @return The protocol's status
     */
    public int getStatus()
    {
        return status;
    }

    /**
     * Sets the Protocol's status (active, completed)
     *
     * @param status The protocol's status
     */
    public void setStatus(int status)
    {
        this.status = status;
    }

    /**
     * Gets the protocol's roles in the system
     *
     * @return The list of Protocol Roles which are ProtocolRole objects
     */
    public String[] getRoles()
    {
        return roles;
    }

    /**
     * Sets the protocol's roles in the system
     *
     * @param  roleList The list of Protocol Roles for this Protocol
     */
    public void setRoles(String[] roleList)
    {
        this.roles = roleList;
    }

    /**
     * Gets the protocol's image file
     *
     * @return The image file
     */
    public File getUploadedFile()
    {
        return uploadedFile;
    }

    /**
     * Sets the protocol's image file
     *
     * @param  theFile An image file
     */
    public void setUploadedFile(File theFile)
    {
        this.uploadedFile = theFile;
    }

    /**
     * Gets the flag whether to remove image file from the protocol
     *
     * @return The flag value of remove
     */
    public boolean getRemove()
    {
        return remove;
    }

    /**
     * Sets the flag remove value
     *
     * @param remove The flag to indicate whether to remove the image file
     */
    public void setRemove(boolean remove)
    {
        this.remove = remove;
    }

    /**
     * Gets the value the variable isEvent
     *
     * @return The value of isEvent
     */
    public String getIsEvent()
    {
        return isEvent;
    }

    /**
     * Sets the value of inEvent
     *
     * @param value The value of isEvent
     */
    public void setIsEvent(String value)
    {
        this.isEvent = StringUtils.trim(value);
    }

    public String getUsePatientName() {
        return usePatientName;
        
    }

    public void setUsePatientName(String usePatientName) {
        this.usePatientName = StringUtils.trim(usePatientName);
    }

    public int getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(int protocolType) {
        this.protocolType = protocolType;
    }
    
    

    public String getStudyType() {
		return studyType;
	}

	public void setStudyType(String studyType) {
		this.studyType = studyType;
	}

	public String getLockFormIntervals() {
        return lockFormIntervals;
    }

    public void setLockFormIntervals(String lockFormIntervals) {
        this.lockFormIntervals = StringUtils.trim(lockFormIntervals);
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

    public String getSubjectNumberPrefix()
    {
        return subjectNumberPrefix;
    }

    public void setSubjectNumberPrefix(String subjectNumberPrefix)
    {
        this.subjectNumberPrefix = StringUtils.trim(subjectNumberPrefix);
    }

    public String getSubjectNumberSuffix()
    {
        return subjectNumberSuffix;
    }

    public void setSubjectNumberSuffix(String subjectNumberSuffix)
    {
        this.subjectNumberSuffix = StringUtils.trim(subjectNumberSuffix);
    }

    public int getSubjectNumberStart()
    {
        return this.subjectNumberStart;
    }

    public void setSubjectNumberStart(int subjectNumberStart)
    {
        this.subjectNumberStart = subjectNumberStart;
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
 	

    public String getReasonForEsignature() {
		return reasonForEsignature;
	}

	public void setReasonForEsignature(String reasonForEsignature) {
		this.reasonForEsignature = reasonForEsignature;
	}
	
    public int[] getSelectedDefaults() {
        return selectedDefaults;
    }

    public void setSelectedDefaults(int[] selectedDefaults) {
        this.selectedDefaults = selectedDefaults;
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
        this.principleInvestigator = StringUtils.trim(principleInvestigator);
    }

    public String getAccountableInvestigator() {
        return accountableInvestigator;
    }

    public void setAccountableInvestigator(String accountableInvestigator) {
        this.accountableInvestigator = StringUtils.trim(accountableInvestigator);
    }

    public boolean isUseEbinder() {
        return useEbinder;
    }

    public void setUseEbinder(boolean useEbinder) {
        this.useEbinder = useEbinder;
    }
    
	public String getProtocolTypeName() {
		return protocolTypeName;
	}

	public void setProtocolTypeName(String protocolTypeName) {
		this.protocolTypeName = StringUtils.trim(protocolTypeName);
	}

	public String getStudyProject() {
		return studyProject;
	}

	public void setStudyProject(String studyProject) {
		this.studyProject = StringUtils.trim(studyProject);
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
		if(site == null){//We are not getting the site from database
			site = new Site();
			site.setAddress(new Address());
			site.getAddress().setState(new CtdbLookup());
			site.getAddress().setCountry(new CtdbLookup());
		}
		if(site.getAddress() == null)
			site.setAddress(new Address());
		if(site.getAddress().getState() == null)
			site.getAddress().setState(new CtdbLookup());
		if(site.getAddress().getCountry() == null)
			site.getAddress().setCountry(new CtdbLookup());		
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public String getProtocolSiteActionFlag() {
		return protocolSiteActionFlag;
	}

	public void setProtocolSiteActionFlag(String protocolSiteActionFlag) {
		this.protocolSiteActionFlag = StringUtils.trim(protocolSiteActionFlag);
	}

	public String getSelectedSiteIds() {
		return selectedSiteIds;
	}

	public void setSelectedSiteIds(String selectedSiteIds) {
		this.selectedSiteIds = StringUtils.trim(selectedSiteIds);
	}

	public int getSelectedAddressId() {
		return selectedAddressId;
	}

	public void setSelectedAddressId(int selectedAddressId) {
		this.selectedAddressId = selectedAddressId;
	}

	public int getProcessSelectedSiteId() {
		return processSelectedSiteId;
	}

	public void setProcessSelectedSiteId(int processSelectedSiteId) {
		this.processSelectedSiteId = processSelectedSiteId;
	}

	public DrugDevice getDrugDevice() {		
		if(drugDevice == null){
			drugDevice = new DrugDevice();
		}
		return drugDevice;
	}

	public void setDrugDevice(DrugDevice drugDevice) {
		this.drugDevice = drugDevice;
	}

	public List<DrugDevice> getDrugDeviceList() {
		return drugDeviceList;
	}

	public void setDrugDeviceList(List<DrugDevice> drugDeviceList) {
		this.drugDeviceList = drugDeviceList;
	}

	public String getProtocolDrugDeviceActionFlag() {
		return protocolDrugDeviceActionFlag;
	}

	public void setProtocolDrugDeviceActionFlag(String protocolDrugDeviceActionFlag) {
		this.protocolDrugDeviceActionFlag = StringUtils.trim(protocolDrugDeviceActionFlag);
	}

	public String getSelectedDrugDeviceIds() {
		return selectedDrugDeviceIds;
	}

	public void setSelectedDrugDeviceIds(String selectedDrugDeviceIds) {
		this.selectedDrugDeviceIds = selectedDrugDeviceIds;
	}

	public int getSelectedDrugDeviceId() {
		return selectedDrugDeviceId;
	}

	public void setSelectedDrugDeviceId(int selectedDrugDeviceId) {
		this.selectedDrugDeviceId = selectedDrugDeviceId;
	}

	public int getProcessSelectedDrugDeviceId() {
		return processSelectedDrugDeviceId;
	}

	public void setProcessSelectedDrugDeviceId(int processSelectedDrugDeviceId) {
		this.processSelectedDrugDeviceId = processSelectedDrugDeviceId;
	}

	public boolean isClinicalTrial() {
		if (this.protocolType == CtdbConstants.CLINICAL_TRIAL_5)
			isClinicalTrial = true;
		else if (this.protocolType == CtdbConstants.CLINICAL_TRIAL_6)
			isClinicalTrial = true;
		else if (this.protocolType == CtdbConstants.CLINICAL_TRIAL_7)
			isClinicalTrial = true;
		else if (this.protocolType == CtdbConstants.CLINICAL_TRIAL_8)
			isClinicalTrial = true;
		else if (this.protocolType == CtdbConstants.CLINICAL_TRIAL_9)
			isClinicalTrial = true;
		else if (this.protocolType == CtdbConstants.CLINICAL_TRIAL_10)
			isClinicalTrial = true;
		else if (this.protocolType == CtdbConstants.CLINICAL_TRIAL_11)
			isClinicalTrial = true;		
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

	public String getProtocolNavigationFlag() {
		return protocolNavigationFlag;
	}

	public void setProtocolNavigationFlag(String protocolNavigationFlag) {
		this.protocolNavigationFlag = protocolNavigationFlag;
	}

	public String getSelectedProtocolIds() {
		return selectedProtocolIds;
	}

	public void setSelectedProtocolIds(String selectedProtocolIds) {
		this.selectedProtocolIds = selectedProtocolIds;
	}

	public boolean getDeleteFlag() {
		return deleteFlag;
	}

	public String getSelectedSites() {
		return selectedSites;
	}

	public void setSelectedSites(String selectedSites) {
		this.selectedSites = selectedSites;
	}

	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public String getCtdbLookupStringForStatusDisplay() {
		return ctdbLookupStringForStatusDisplay;
	}

	public void setCtdbLookupStringForStatusDisplay(
			String ctdbLookupStringForStatusDisplay) {
		this.ctdbLookupStringForStatusDisplay = ctdbLookupStringForStatusDisplay;
	}

	public String getCtdbLookupStringForInstituteDisplay() {
		return ctdbLookupStringForInstituteDisplay;
	}

	public void setCtdbLookupStringForInstituteDisplay(
			String ctdbLookupStringForInstituteDisplay) {
		this.ctdbLookupStringForInstituteDisplay = ctdbLookupStringForInstituteDisplay;
	}

	public String getCtdbLookupStringForProtocolTypeDisplay() {
		return ctdbLookupStringForProtocolTypeDisplay;
	}

	public void setCtdbLookupStringForProtocolTypeDisplay(
			String ctdbLookupStringForProtocolTypeDisplay) {
		this.ctdbLookupStringForProtocolTypeDisplay = ctdbLookupStringForProtocolTypeDisplay;
	}

	public String getSectionDisplay() {
		return sectionDisplay;
	}

	public void setSectionDisplay(String sectionDisplay) {
		this.sectionDisplay = sectionDisplay;
	}

	/**
	 * @return the addedFromDashboard
	 */
	public boolean isAddedFromDashboard() {
		return addedFromDashboard;
	}

	/**
	 * @param addedFromDashboard the addedFromDashboard to set
	 */
	public void setAddedFromDashboard(boolean addedFromDashboard) {
		this.addedFromDashboard = addedFromDashboard;
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
	 * @return the otherOrganization
	 */
	public String getOrganization() {
		return organization;
	}

	/**
	 * @param organization the otherOrganization to set
	 */
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getProtoDetail() {
		return protoDetail;
	}

	public void setProtoDetail(String protoDetail) {
		this.protoDetail = protoDetail;
	}
	
	public String getLocProcPOCDisplay() {
		return this.locProcPOCDisplay;
	}
	
	public void setLocProcPOCDisplay(String locProcPOCDisplay) {
		this.locProcPOCDisplay = locProcPOCDisplay;
	}
	
	public List<ClinicalLocation> getProtoClinicLocList() {
		return this.protoClinicLocList;
	}
	
	public void setProtoClinicLocList(List<ClinicalLocation> clinicLocList){
		this.protoClinicLocList.clear();

		if (clinicLocList != null) {
			this.protoClinicLocList.addAll(clinicLocList);
		}
	}
	
	public List<Procedure> getProtoProcedureList(){
		return this.protoProcedureList;
	}
	
	public void setProtoProcedureList(List<Procedure> protopPocedureList){
		this.protoProcedureList.clear();

		if (protopPocedureList != null) {
			this.protoProcedureList.addAll(protopPocedureList);
		}
	}
	
	public List<PointOfContact> getProtoPOCList() {
		return this.protoPOCList;
	}
	
	public void setProtoPOCList(List<PointOfContact> pocList){
		this.protoPOCList.clear();

		if (pocList != null) {
			this.protoPOCList.addAll(pocList);
		}
	}
	
	public List<MilesStone> getProtoMilesStoneList() {
		return this.protoMilesStoneList;
	}
	
	public void setProtoMilesStoneList(List<MilesStone> milesStoneList){
		this.protoMilesStoneList.clear();

		if (milesStoneList != null) {
			this.protoMilesStoneList.addAll(milesStoneList);
		}
	}
}
