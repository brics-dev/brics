package gov.nih.nichd.ctdb.patient.action;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.patient.common.PatientResultControl;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;
import gov.nih.nichd.ctdb.patient.tag.PatienHomeIdtDecorator;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

/**
 * The Struts Action class responsible for listing patients to the user and
 * giving the user the ability to search for a patient, sort the listing, edit a
 * patient, audit a patient, and add a new patient.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class PatientHomeAction extends BaseAction {
	private static final long serialVersionUID = 2154400619552095708L;
	private static final Logger logger = Logger.getLogger(PatientHomeAction.class);

	private String lastNameSearch = null;
	private String firstNameSearch = null;
	private String recordNumberSearch = null;
	private String mrnSearch = null;
	private String otherNameSearch = null;
	private String maidenNameSearch = null;
	private int protocolIdSearch = Integer.MIN_VALUE;
	private String clicked = "submit";
	private String sortBy = PatientResultControl.SORT_BY_SUBJECTID;
	private String sortedBy = null;
	private String sortOrder = PatientResultControl.SORT_ASC;
	private String enrollmentStatus = "this";
	private Map<String, String> enrollmentStatusList = null;
	private String inProtocol = "yes";
    private String activeInProtocol = null;
	private String patientGroupNameSearch = null;
    private String numResults = null;
    private String numResultsPerPage = null;
    private String subjectNumberSearch = null;
    private String guidSearch = null;
    private String searchSubmitted = "NO";
    private List<Patient> patients;
    private boolean hasAnyPatInRandomization = false;
    
    private List<Integer> siteIds = new ArrayList<Integer>();

	public String execute() throws Exception {

		buildLeftNav(LeftNavController.LEFTNAV_SUBJECTS_MYSUBJECTS);
		if (this.getCurrentProtocol() == null) {
			return StrutsConstants.SELECTPROTOCOL;
		}

		String non_pii_property = SysPropUtil.getProperty("guid_with_non_pii");
        if (non_pii_property != null && non_pii_property.equals("0") && !isValidNotInProtocolSearch()) {
        	addActionError("When searching outside of your study, you must enter all " + 
        		"following fields: first name, last name, and Medical Record Number.");
            return ERROR;
        }

		boolean protocolClosed = (Boolean) session.get(CtdbConstants.PROTOCOL_CLOSED_SESSION_KEY) == null ? false
				: ((Boolean) session.get(CtdbConstants.PROTOCOL_CLOSED_SESSION_KEY)).booleanValue();
		if (protocolClosed) {
			disableLinksForProtocolCloseout(LeftNavController.LEFTNAV_SUBJECTS_MYSUBJECTS,
					new int[] { LeftNavController.LEFTNAV_SUBJECTS_ADD, LeftNavController.LEFTNAV_SUBJECTS_VISITS });
		}

		// Display any action messages or errors from the add/edit subject page, and
		// clear any session data
		// that is associated with that page.
		this.retrieveActionMessages(PatientAction.ACTION_MESSAGES_KEY);
		this.retrieveActionErrors(PatientAction.ACTION_ERRORS_KEY);

		if (session.get(CtdbConstants.ATTACHMENT_HASH_MAP) != null) {
			PatientAction.clearSessionVariables(session);
		}

		try {
			this.setupPage();
		} catch (CtdbException ce) {
			ce.printStackTrace();
			return StrutsConstants.FAILURE;
		}

		return SUCCESS;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8082/ibis/getPatientList.action
	public String getPatientList() throws Exception {
		try {
			@SuppressWarnings("unchecked")
			PatientManager pm = new PatientManager();

			List<Patient> patientList = null;
			
			if(!isUserSiteCheckNeeded()) {
				patientList = (List<Patient>) session.get("patientList");
			} else {
				Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
				int protocolId = protocol.getId();
				siteIds = getUserAssignedSites();
				patientList = pm.getPatientListByProtocolIdAndSiteIds(protocolId, siteIds);
			}			
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<Patient> outputList = new ArrayList<Patient>(patientList);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new PatienHomeIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("Failed get a list of patients.", e);
			return StrutsConstants.FAILURE;
		}

		return null;
	}

	public String deletePatient() {
		buildLeftNav(LeftNavController.LEFTNAV_SUBJECTS_MYSUBJECTS);

		User user = getUser();

		try {
			PatientManager pm = new PatientManager();

			List<String> deletedList = new ArrayList<String>();

			if (!Utils.isBlank(request.getParameter("selectedIds"))) {
				String selectedIds = request.getParameter("selectedIds");
				String[] selectedPatientIds = selectedIds.split(",");
				;
				String patientVisitErrorList = "", patientAdminErrorList = "", idUsedInAnotherList = "";

				for (String selectedPatientId : selectedPatientIds) {

					if (pm.hasFutureVisits(selectedPatientId)) {
						patientVisitErrorList += selectedPatientId + "  ";
					}
					if (pm.isAdministered(selectedPatientId)) {
						patientAdminErrorList += selectedPatientId + "  ";
					}
					if (pm.isSubjectReusedInOtherProtocolWithDifferentIdinPP(this.getCurrentProtocol().getId(),
							selectedPatientId)) {
						idUsedInAnotherList += selectedPatientId + "  ";
					}
				}

				if (patientVisitErrorList != "" || patientAdminErrorList != "") {
					String errMessage = "";
					if (patientVisitErrorList != "") {
						errMessage += "The selected subject(s) can not be deleted due to the coming scheduled visit(s).<br>";
					}
					if (patientAdminErrorList != "") {
						errMessage += "The selected subject(s) can not be deleted due to the existing administered data collection.";
					}
					if (idUsedInAnotherList != "") {
						errMessage += "The selected subject(s) can not be deleted because it's used in other protocol with different ID.";
					}
					addActionError(errMessage);

				} else {
					if (selectedPatientIds != null && selectedPatientIds.length > 0) {
						deletedList = pm.deletePatients(selectedPatientIds, user.getId());

						if (deletedList != null && !deletedList.isEmpty()) {
							StringBuffer deletePatients = new StringBuffer(1000);

							for (String deletedName : deletedList) {
								deletePatients.append(deletedName + " ");
							}
							addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_KEY,
									new String[] { "selected subject(s)" }));
						}
	    			}
    			}
    		}    			

   			this.setupPage();
   			
        } 
        catch(CtdbException ce) {
        	ce.printStackTrace();
            return StrutsConstants.FAILURE;
        }

        return SUCCESS;
    }

    
    private void setupPage() throws CtdbException {
    	
    	enrollmentStatusList = new LinkedHashMap<String, String>();
    	enrollmentStatusList.put("this", getText("patient.tab.protocol.currentStudy"));
    	enrollmentStatusList.put("all", getText("patient.tab.protocol.allStudies"));
    	enrollmentStatusList.put("none", getText("patient.tab.protocol.notEnrolled"));
    	session.put("enrollmentStatusList", enrollmentStatusList);
    	
    	User user = getUser();
        
        PatientResultControl prc = new PatientResultControl();
        setProtocolIdSearch(this.getCurrentProtocol().getId());
        
        session.put("numResultsPerPageOptions", getOptionsFromMessageResources(
        		"app.options.search.numresultsperpage.responsedata"));
        session.put("numResultsOptions", getOptionsFromMessageResources(
        		"app.options.search.numresults.responsedata"));
        
        if (getNumResults() == null) {
            setNumResults(getText("app.options.search.numresults.default"));
        }
        if (getNumResultsPerPage() == null) {
            setNumResultsPerPage(getText("app.options.search.numresultsperpage.default"));
            session.put("resultsPerPageChoosen", "");
        } else {
        	session.put("resultsPerPageChoosen", getNumResultsPerPage());
        }

        // get the user from the session and only return appropriate patients
        this.updateResultControl(prc);
        if (user.isStaff()) {
            prc.setSiteId(user.getProtocolSiteMap().get(this.getCurrentProtocol().getId()));
        }

        PatientManager pm = new PatientManager();
            
        patients = pm.getMinimalPatients(prc);
       
        session.put("patientList", patients);
        
        ProtocolManager protocolManager = new ProtocolManager();
        List<Protocol> protocolList = protocolManager.getProtocols();

        Protocol all = new Protocol();
        all.setId(Integer.MIN_VALUE);
        all.setName("All Protocols");
        protocolList.add(0, all);
           
        request.setAttribute("protocolList", protocolList);
        request.setAttribute("prc", prc);
        
        /*check if any of patient has been assigned to a randomization group*/
    	int protocolId = this.getCurrentProtocol().getId();
    	boolean hasRandom = pm.checkIfAnyPatProtoHasRandomization(protocolId);
    	this.setHasAnyPatInRandomization(hasRandom);
    }
    
    
    private Protocol getCurrentProtocol() {
    		return  (Protocol)session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
    }
    
  

	/** updateResultControl updates a result control object to facilitate searching and sorting.
     *   
     * @param prc : the PatientResultControl
     */
    private void updateResultControl(PatientResultControl prc) {

        if (numResults != null && (!numResults.equalsIgnoreCase("all"))) {
            prc.setRowNumMax(Integer.parseInt(getNumResults()));
        }
        prc.setFirstName(getFirstNameSearch());
        prc.setLastName(getLastNameSearch());
        prc.setSubjectId(getRecordNumberSearch());
        prc.setMrn(getMrnSearch());
        prc.setProtocolId(getProtocolIdSearch());
        prc.setOtherName(getOtherNameSearch());
        prc.setMaidenName(getMaidenNameSearch());
        prc.setSubjectNumber(getSubjectNumberSearch());
        prc.setPatientGroupName(getPatientGroupNameSearch());
        prc.setActiveInProtocol(getActiveInProtocol());
        prc.setEnrollmentStatus(getEnrollmentStatus());
        prc.setGuid(getGuidSearch());
    }

    public boolean isValidNotInProtocolSearch() {
        if (enrollmentStatus != null && !enrollmentStatus.equalsIgnoreCase("this")) { 
        	// outside of current study
        	if (Utils.isBlank(lastNameSearch) && Utils.isBlank(firstNameSearch) && Utils.isBlank(mrnSearch)) {
        		return true;
        	} else {
        		return false;
        	}
        } else { // within protocol
            return true;
        }
    }

	public List<Patient> getPatients() {
		return patients;
	}

	public void setPatients(List<Patient> patients) {
		this.patients = patients;
	}

	public String getLastNameSearch() {
		return lastNameSearch;
	}

	public void setLastNameSearch(String lastNameSearch) {
		this.lastNameSearch = lastNameSearch;
	}

	public String getFirstNameSearch() {
		return firstNameSearch;
	}

	public void setFirstNameSearch(String firstNameSearch) {
		this.firstNameSearch = firstNameSearch;
	}

	public String getRecordNumberSearch() {
		return recordNumberSearch;
	}

	public void setRecordNumberSearch(String recordNumberSearch) {
		this.recordNumberSearch = recordNumberSearch;
	}

	public String getMrnSearch() {
		return mrnSearch;
	}

	public void setMrnSearch(String mrnSearch) {
		this.mrnSearch = mrnSearch;
	}

	public String getOtherNameSearch() {
		return otherNameSearch;
	}

	public void setOtherNameSearch(String otherNameSearch) {
		this.otherNameSearch = otherNameSearch;
	}

	public String getMaidenNameSearch() {
		return maidenNameSearch;
	}

	public void setMaidenNameSearch(String maidenNameSearch) {
		this.maidenNameSearch = maidenNameSearch;
	}

	public int getProtocolIdSearch() {
		return protocolIdSearch;
	}

	public void setProtocolIdSearch(int protocolIdSearch) {
		this.protocolIdSearch = protocolIdSearch;
	}

	public String getClicked() {
		return clicked;
	}

	public void setClicked(String clicked) {
		this.clicked = clicked;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public String getSortedBy() {
		return sortedBy;
	}

	public void setSortedBy(String sortedBy) {
		this.sortedBy = sortedBy;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getEnrollmentStatus() {
		return enrollmentStatus;
	}

	public void setEnrollmentStatus(String enrollmentStatus) {
		this.enrollmentStatus = enrollmentStatus;
	}

	public Map<String, String> getEnrollmentStatusList() {
		return enrollmentStatusList;
	}

	public void setEnrollmentStatusList(Map<String, String> enrollmentStatusList) {
		this.enrollmentStatusList = enrollmentStatusList;
	}

	public String getInProtocol() {
		return inProtocol;
	}

	public void setInProtocol(String inProtocol) {
		this.inProtocol = inProtocol;
	}

	public String getActiveInProtocol() {
		return activeInProtocol;
	}

	public void setActiveInProtocol(String activeInProtocol) {
		this.activeInProtocol = activeInProtocol;
	}

	public String getPatientGroupNameSearch() {
		return patientGroupNameSearch;
	}

	public void setPatientGroupNameSearch(String patientGroupNameSearch) {
		this.patientGroupNameSearch = patientGroupNameSearch;
	}

	public String getNumResults() {
		return numResults;
	}

	public void setNumResults(String numResults) {
		this.numResults = numResults;
	}

	public String getNumResultsPerPage() {
		return numResultsPerPage;
	}

	public void setNumResultsPerPage(String numResultsPerPage) {
		this.numResultsPerPage = numResultsPerPage;
	}

	public String getSubjectNumberSearch() {
		return subjectNumberSearch;
	}

	public void setSubjectNumberSearch(String subjectNumberSearch) {
		this.subjectNumberSearch = subjectNumberSearch;
	}

	public String getGuidSearch() {
		return guidSearch;
	}

	public void setGuidSearch(String guidSearch) {
		this.guidSearch = guidSearch;
	}

	public void setSearchSubmitted(String searchSubmitted) {
		this.searchSubmitted = searchSubmitted;
	}

	public String getSearchSubmitted() {
		return searchSubmitted;
	}
    public boolean getHasAnyPatInRandomization() {   	
    	return hasAnyPatInRandomization;
    }
    public void setHasAnyPatInRandomization(boolean hasAnyPatInRandomization) {
    	this.hasAnyPatInRandomization = hasAnyPatInRandomization;
    }
}
