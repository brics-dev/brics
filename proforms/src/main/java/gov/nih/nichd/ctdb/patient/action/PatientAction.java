package gov.nih.nichd.ctdb.patient.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.jasig.cas.client.validation.Assertion;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.gson.JsonObject;

import gov.nih.nichd.ctdb.attachments.domain.Attachment;
import gov.nih.nichd.ctdb.attachments.domain.AttachmentCategory;
import gov.nih.nichd.ctdb.attachments.manager.AttachmentCategoryManager;
import gov.nih.nichd.ctdb.attachments.manager.AttachmentManager;
import gov.nih.nichd.ctdb.attachments.tag.AttachmentPatientDecorator;
import gov.nih.nichd.ctdb.common.AssemblerException;
import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.ServerFileSystemException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.patient.common.PatientAssembler;
import gov.nih.nichd.ctdb.patient.common.PatientVisitResultControl;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.domain.PatientGroup;
import gov.nih.nichd.ctdb.patient.domain.PatientProtocol;
import gov.nih.nichd.ctdb.patient.form.PatientForm;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;
import gov.nih.nichd.ctdb.patient.manager.PatientRoleManager;
import gov.nih.nichd.ctdb.patient.util.PatientChangeTracker;
import gov.nih.nichd.ctdb.patient.util.PatientEditValidator;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.FormCollectionDisplay;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.nichd.ctdb.site.domain.Site;
import gov.nih.nichd.ctdb.site.manager.SiteManager;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.nichd.ctdb.util.domain.LookupType;
import gov.nih.nichd.ctdb.util.manager.LookupManager;
import gov.nih.tbi.guid.exception.InvalidJwtException;
import gov.nih.tbi.guid.model.GuidJwt;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

/**
 * The Struts Action class responsable for adding and editing patient
 * information for the nichd ctdb
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class PatientAction extends BaseAction {

	private static final long serialVersionUID = -8314011045768436410L;
	private static final Logger logger = Logger.getLogger(PatientAction.class);

	/**
     * Struts Constant used to set/get ActionMessages for this action from session.
     * Used for the redirect to the main listing page for this functionality.
     */
    public static final String ACTION_MESSAGES_KEY = "PatientAction_ActionMessages";
    public static final String ACTION_ERRORS_KEY = "PatientAction_ActionErrors";
	public static final int PUB_TYPE_OTHER = 14;

    private int patientId = -1;
  
    private PatientForm patientForm = new PatientForm();
    private String guidToValidate = null;
    private String skipcheckcodematch = null;
    private String associatedWithStudy = "true";
    private List<Long> selectedAttachmentIds = new ArrayList<Long>();
    private String attachmentListJson = "[]";
    private String jsonString = "{}";
    
    // This method will be called by all action methods for the general page initialization.
    public void setupPage() {
		buildLeftNav(LeftNavController.LEFTNAV_SUBJECTS_ADD);
    	
        setDefaultPatientInfo(SysPropUtil.getProperty("guid_with_non_pii").equals("1"));
        
        String expandedDisplayOption = request.getParameter("sectionDisplay");
        if (expandedDisplayOption != null) {
        	patientForm.setSectionDisplay(expandedDisplayOption);
        }
        
        if (request.getAttribute(CtdbConstants.PATIENT_VISIT_DATE_LIST_KEY) == null) {
        	List<Date> visitDates  = new ArrayList<Date>();
            request.setAttribute(CtdbConstants.PATIENT_VISIT_DATE_LIST_KEY, visitDates);
        } 
        
        Protocol protocol = (Protocol)session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
        patientForm.setCurrentProtocolId(Long.toString(protocol.getId()));
    }
    
    /**
     * Default action method that sets up the Add/Edit subject page.
     */
    public String showPage() {
    	
    	//clear session patientId before you add or edit
    	session.remove(CtdbConstants.PATIENT_ID_REQUEST_ATTR);
		String nonPII = SysPropUtil.getProperty("guid_with_non_pii");
		if (nonPII == null || !(nonPII.equals("0") || nonPII.equals("1"))) {
			addActionError("GUID_WITH_NON_PII must be defined in the property file as either 0 or 1");
			return StrutsConstants.FAILURE;
		}
        
		this.setupPage();
		
		String strutsResult = BaseAction.SUCCESS;
        Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
        int pid = protocol.getId();
        Locale userLocale = request.getLocale();
        
        try {
        	setJwtCookie(getJwtString());
        	PatientManager pm = new PatientManager();
            PatientRoleManager roleManager = new PatientRoleManager();
            
            boolean isEditingPatient = false;
        	Patient patient = null;
        	
			if ( (request.getParameter("action") != null) && 
					request.getParameter("action").equalsIgnoreCase("add_to_protocol") ) {
				this.addToProtocol();
			}
			
			
			//
			if(session.get(CtdbConstants.PATIENT_ID_REQUEST_ATTR)!=null) {
				patientId = Integer.valueOf((String) session.get(CtdbConstants.PATIENT_ID_REQUEST_ATTR));
			}
			
            // Edit patient
			  if ( patientId > 0 ) {
            	isEditingPatient = true;
            	if (session.get("_patient_newlyAddedPatient") != null) {
            		patient = (Patient) session.get("_patient_newlyAddedPatient");
            		patientForm.setId(patient.getId());
            		//patientForm.setSubjectId(patientId);
            	} else {
            		
            		patientForm.setId(patientId);
            		//patientForm.setSubjectId(patientId);
            		
            		patient = pm.getPatient(String.valueOf(patientId));
            		patient.setUsingNonPII(nonPII);
            	}
            	
                PatientAssembler.domainToForm(patient, patientForm);
                for (Protocol pp : patient.getProtocols()) {
                	if (pp.getId() == pid) {
                        patientForm.setActive(String.valueOf(pp.isActive()));
                        break;
                	}
                }
                
                ResponseManager rm = new ResponseManager();
                List<FormCollectionDisplay> demoForms = rm.getCollectedPatientDataByFormName(pid, patient.getId(),
                		getText("subject.demographics.form.name"));

                request.setAttribute("demoForms",  demoForms);
                session.put("_editingPatient", patient);
                session.put("wasActiveInProtocol", patientForm.getActive());
            }
            else {
            	patientForm.setActive("true");
            	//patientForm.setSiteId(protocol.getDefaultStudySiteId());
            }
            
            AttachmentCategoryManager acm = new AttachmentCategoryManager();
            int attachTypeId = AttachmentManager.FILE_PATIENT;
            session.put("_attachments_categories", acm.getAttachmentCategories(pid, attachTypeId));

            LookupManager lookUp = new LookupManager();
            session.put("xstates", lookUp.getLookups(LookupType.STATE));
            session.put("countryOptions", lookUp.getLookups(LookupType.COUNTRY));
            session.put("sexOptions", lookUp.getLookups(LookupType.PATIENT_SEX));
            session.put("_patient_patientGroups", roleManager.getPatientGroups(protocol.getId()));
            session.put(CtdbConstants.ATTACHMENT_TYPE_ATTR, attachTypeId);

            SiteManager sm = new SiteManager();
            List<Site> protocolSites = new ArrayList<Site>();
            
            protocolSites.addAll(sm.getSites(protocol.getId()));
            session.put("__patientAction_sites", protocolSites);

            ProtocolManager protoMan = new ProtocolManager();
            session.put("protocolList", protoMan.getProtocols());

            List<Attachment> attFiles = new ArrayList<Attachment>();
            
            if ( isEditingPatient ) {
            	session.put("_attachments_associatedId", patient.getId());
            	attFiles = patient.getAttachments();
                
                // get list of patient visit dates
        		PatientVisitResultControl pvrc = new PatientVisitResultControl();
        		pvrc.setPatientId(patient.getId());
        		pvrc.setProtocolId(protocol.getId());
                request.setAttribute(CtdbConstants.PATIENT_VISIT_DATE_LIST_KEY, pm.getPatientVisits(pvrc,protocol.getId()));
            }

            Map<Long, Attachment> attachmentHashMap = generateAttachmentMap(attFiles);
            session.put(CtdbConstants.ATTACHMENT_HASH_MAP, attachmentHashMap);
            setupAttachmentList(attachmentHashMap);
        }
        catch ( JSONException je ) {
			logger.error("Could not setup attachment JSON array.", je);
			return StrutsConstants.FAILURE;
		}
        catch ( ObjectNotFoundException onfe ) {
        	logger.error("Cannot find the subject in the database.", onfe);
        	addActionError(getText(StrutsConstants.ERROR_NOTFOUND, new String[]{getText("app.neededInfo.display")}));
        }
        catch ( DuplicateObjectException doe ) {
        	logger.error("Duplicate subject found in the database when setting its study information.", doe);
        	addActionError(getText(StrutsConstants.ERROR_DATABASE_GET, new String[]{"a " + getText("subject.title.display").toLowerCase(userLocale)}));
        }
        catch ( AssemblerException ae ) {
        	logger.error("Could not correctly assemble the Patient object for the form object.", ae);
        	addActionError(getText(StrutsConstants.ERROR_FIELD_INVALID, new String[]{getText("subject.title.display")}));
        }
        catch ( CtdbException ce ) {
        	logger.error("Database error occurred while setting up the add/edit subject page.", ce);
        	addActionError(getText(StrutsConstants.ERROR_DATABASE_GET, new String[]{"a " + getText("subject.title.display").toLowerCase(userLocale)}));
        } 
        catch (IOException | RuntimeException e) {
        	logger.error("Unable to request JWT because of incorrect proxy ticket", e);
        	addActionError("Authenticating with the GUID server failed");
		} 
        catch (InvalidJwtException e) {
			logger.error("Unable to request JWT because the server JWT was invalid", e);
			addActionError("Authenticating with the GUID server failed.");
		}
        
        // Check if there were any errors.
        if ( hasActionErrors() ) {
        	session.put(PatientAction.ACTION_ERRORS_KEY, getActionErrors());
        	strutsResult = BaseAction.ERROR;
        }
        
        return strutsResult;
    }
    
    /**
     * Clear all session data that was created for the sole use of creating or editing subjects.
     * 
     * @param userSession - The user's session.
     */
	public static synchronized void clearSessionVariables(Map<String, Object> userSession) {
    	userSession.remove(CtdbConstants.ATTACHMENT_HASH_MAP);
    	userSession.remove("_attachments_associatedId");
    	userSession.remove("protocolList");
    	userSession.remove("__patientAction_sites");
    	userSession.remove("_patient_patientGroups");
    	userSession.remove("sexOptions");
    	userSession.remove("countryOptions");
    	userSession.remove("xstates");
    	userSession.remove("_attachments_categories");
    	userSession.remove("wasActiveInProtocol");
    	userSession.remove("_editingPatient");
    	userSession.remove(CtdbConstants.PATIENT_ID_REQUEST_ATTR);
		userSession.remove(CtdbConstants.ATTACHMENT_TYPE_ATTR);

		// Clear session attached file.
		Attachment sessionFile = (Attachment) userSession.get(CtdbConstants.ATTACHED_FILE);

		try {
			if ((sessionFile != null) && (sessionFile.getAttachFileContent() != null)) {
				sessionFile.getAttachFileContent().close();
			}
		} catch (IOException e) {
			logger.warn("Couldn't close handle to the attachment file.", e);
		}

    	userSession.remove(CtdbConstants.ATTACHED_FILE);
    }
    
    public void addToProtocol() throws ObjectNotFoundException, DuplicateObjectException, CtdbException {
    	
        Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
        PatientManager pm = new PatientManager();
    	PatientProtocol pp = new PatientProtocol(protocol);
    	pp.setSiteId(pp.getDefaultStudySiteId());
    	pp.setGroupId(Integer.MIN_VALUE);
    	pp.setCohortId(Integer.MIN_VALUE);
    	
    	// Set subject number if auto incrementing is active
    	if (protocol.isAutoIncrementSubject()) {
    		String fullSubjNumber = getFormattedNextSubjectNumber(pm, protocol);
    		pp.setSubjectNumber(fullSubjNumber);
    	}
    	
    	Patient patient = pm.getPatient(request.getParameter("id"));
    	patient.setUsingNonPII(SysPropUtil.getProperty("guid_with_non_pii"));
		patient.updateUpdatedByInfo(getUser());
    	patient.getProtocols().add(pp);
    	
    	pm.updatePatient(patient,protocol.getId());
    	pm.sortAddedPatient(patient.getId(), protocol);
    	session.put("_patient_newlyAddedPatient", patient);
    }
	
    
    public String addPatient() throws Exception {
    	 // String subjId  = null;
        if (patientForm.hasInvalidDateOfBirth()) {
        	addActionError(getText(StrutsConstants.ERROR_FIELD_INVALID, new String[]{getText("patient.dateofbirth.display")}));
    	}
        
		this.setupPage();
	
		
		String strutsResult = BaseAction.SUCCESS;
		User user = getUser();
        Protocol protocol = (Protocol)session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
        Patient patient = (Patient) PatientAssembler.formToDomain(patientForm);
        patient.setUsingNonPII(SysPropUtil.getProperty("guid_with_non_pii"));
        
        PatientProtocol firstProtocol = patient.getProtocols().get(0);
        
        if (protocol.isAutoAssociatePatientRoles() && Integer.parseInt(patientForm.getCurrentProtocolId()) > 0) {
            @SuppressWarnings("unchecked")
			List<PatientGroup> patientGroups = (List<PatientGroup>) session.get("_patient_patientGroups");
            if (patientGroups != null && !patientGroups.isEmpty()) {
            	Random generator = new Random(System.currentTimeMillis());
            	int randomGroupId = patientGroups.get(generator.nextInt(patientGroups.size())).getId();
            	firstProtocol.setGroupId(randomGroupId);
            }
        }
        
        patient.updateUpdatedByInfo(user);
        patient.updateValidatedByInfo(user, protocol.getId(), patientForm.isValidated());
        patient.updateCreatedByInfo(user);
        //patient.setSubjectId(this.getSubjectId());
        //patient.setSubjectId(subjId);
        
        @SuppressWarnings("unchecked")
		Map<Long, Attachment> attachmentMap = (Map<Long, Attachment>) session.get(CtdbConstants.ATTACHMENT_HASH_MAP);
        Locale userLocale = request.getLocale();
        boolean patIsExists = false;

        try {
        	PatientManager pm = new PatientManager();
        	patIsExists = pm.isGuidExists(patient);
        	pm.createPatient(patient,protocol.getId());
        	int id = patient.getId();
        	
            // Set subject number if auto-incrementing is active                 (
            if (protocol.isAutoIncrementSubject() && Utils.isBlank(firstProtocol.getSubjectNumber())) {
                String fullSubjNumber = getFormattedNextSubjectNumber(pm, protocol);
                PatientProtocol patientProtocol = new PatientProtocol(protocol);
                patientProtocol.setSubjectNumber(fullSubjNumber);
                patientProtocol.setSubjectId(patient.getSubjectId());
                Map<Integer, PatientProtocol> patientProtocolMap = new HashMap<Integer, PatientProtocol>();
                patientProtocolMap.put(new Integer(id), patientProtocol);
                patientProtocol.setUpdatedBy(user.getId());
                pm.updatePatientProtocol(patientProtocolMap);
            }
            
            // Set the associated ID to the new patient ID for all attachments in the map
			for (Attachment a : attachmentMap.values()) {
				a.setAssociatedId(id);
			}
            
            // Save the attachments
            AttachmentManager am = new AttachmentManager();
            am.saveAttachments(attachmentMap);
        }
        catch ( DuplicateObjectException e ) {
        	logger.error("Duplicate subject found in the database while creating a new subject.", e);
        	e.printStackTrace();
        	addActionError(getText(StrutsConstants.ERROR_DUPLICATE, new String[]{getText("patient.label.SubjectID").toLowerCase(userLocale)}));
        }
        catch ( ServerFileSystemException sfse ) {
        	logger.error("Error occurred while saving attachments to the server's file system.", sfse);
        	sfse.printStackTrace();
        	addActionError(getText(StrutsConstants.ERROR_DATA_SAVE, new String[]{getText("attachment.label.lowerCase.display")}));
        }
        catch ( CtdbException ce ) {
        	logger.error("Database error occurred while creating a new subject.", ce);
        	ce.printStackTrace();
        	addActionError(getText(StrutsConstants.ERROR_DATABASE_SAVE, new String[]{getText("subject.title.display").toLowerCase(userLocale)}));
        }
        
        // Check for any errors
        if ( hasActionErrors() ) {
        	strutsResult = BaseAction.ERROR;
        	session.put(ACTION_ERRORS_KEY, getActionErrors());

        	try {
        		setupAttachmentList(attachmentMap);
        	}
        	catch ( JSONException je ) {
    			logger.error("Could not setup attachment JSON array.", je);
    			return StrutsConstants.FAILURE;
    		}
        }
        else {
			if (Integer.parseInt(SysPropUtil.getProperty("guid_with_non_pii")) == 0 && patIsExists) {
            	addActionMessage(getText(StrutsConstants.SUBJECT_EXISTING_WARNING, new String[]{getText("subject.title.display").toLowerCase(userLocale) + 
                		" " + patient.getDisplayLabel(protocol.getPatientDisplayType(), protocol.getId())}));
                session.put(ACTION_MESSAGES_KEY, getActionMessages());
        	}
			else {
        		addActionMessage(getText(StrutsConstants.SUCCESS_ADD_KEY, new String[]{getText("subject.title.display").toLowerCase(userLocale) + 
            		" " + patient.getDisplayLabel(protocol.getPatientDisplayType(), protocol.getId())}));
            	session.put(ACTION_MESSAGES_KEY, getActionMessages());
        	}

            this.retrieveActionErrors(PatientAction.ACTION_ERRORS_KEY);
            PatientAction.clearSessionVariables(session);
        }

        return strutsResult;
    }
    
    public String updatePatient() {
    	
    	// Check the date format for the date of birth
        if (patientForm.hasInvalidDateOfBirth()) {
        	addActionError(getText(StrutsConstants.ERROR_FIELD_INVALID, new String[]{getText("patient.dateofbirth.display")}));
    	}
        
        this.setupPage();
        
        String strutsResult = BaseAction.SUCCESS;
		User user = getUser();
        Protocol protocol = (Protocol)session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
        int pid = protocol.getId();
        String patientDisplayLabel = "";
        String subjId  = null;
        @SuppressWarnings("unchecked")
		Map<Long, Attachment> attachmentMap = (Map<Long, Attachment>) session.get(CtdbConstants.ATTACHMENT_HASH_MAP);
        Locale userLocale = request.getLocale();
        
        try {
        	PatientManager pm = new PatientManager();
        	
        	//patientForm.setSubjectId(this.getSubjectId());
        	if(patientForm.getSubjectId()!=null) {
        		subjId = patientForm.getSubjectId();
        	}else {
        		  throw new RuntimeException("Subject Id is required to create patient.");
        	}
            Patient patient = (Patient)PatientAssembler.formToDomain(patientForm);
            patient.setUsingNonPII(SysPropUtil.getProperty("guid_with_non_pii"));
            patientDisplayLabel = patient.getDisplayLabel(protocol.getPatientDisplayType(), pid);
           // patient.setSubjectId(this.getSubjectId());
            boolean inProtocol = false;
            boolean activeInProtocol = false;
            for (PatientProtocol pp : patient.getProtocols()) {
                if (pp.getId() == pid) { // patient is not being dis-associated
                    inProtocol = true;
                    if (pp.isActive()) {
                        activeInProtocol = true;
                    }
                    if (patientForm.isValidated()) {
                    	pp.setValidatedBy(user.getId());
                    	pp.setValidatedDate(new java.util.Date());
                    }
                    break;
                }
            }
        	
            List<AdministeredForm> activeAdminForms = pm.hasActiveDataEntry(pid, patient.getId());
            boolean hasActiveDataEntry = !activeAdminForms.isEmpty();
            boolean hasDataInProtocol = pm.hasProtocolData(pid, patient.getId());
            
            // Validate patient data with any related collections
            if (!inProtocol && hasDataInProtocol) {
                patientForm.setActive(session.get("wasActiveInProtocol").toString());
                patientForm.setCurrentProtocolId(pid + "");
                addActionError("The system cannot remove " + patientDisplayLabel + 
                		" from the current study.  Please remove all response data first.");
            }
            else if (!activeInProtocol && hasActiveDataEntry) { 
                addActionError(getText(StrutsConstants.ERROR_DATAENTRY_PATIENT_INACTIVATE, 
                		new String[]{"Subject " + patientDisplayLabel}));
            }
            
            if ( hasActionErrors() ) {
            	request.setAttribute(CtdbConstants.PATIENT_ATTACHMENTS_SESSION_KEY, new ArrayList<Attachment>(attachmentMap.values()));
                return BaseAction.ERROR;
            }
            
            patient.updateUpdatedByInfo(user);
            patient.updateCreatedByInfo(user);
            patient.setSubjectId(subjId);
            PatientChangeTracker pct = new PatientChangeTracker();
            pct.setEditingUser(user);
            pm.updatePatient(patient,protocol.getId());
            
            Patient oldP = (Patient)session.get("_editingPatient");
            pct = PatientEditValidator.validateEditWithoutReasons(oldP, patient, request);
            if (pct.getChangedFields().size() > 0) {
            	pm.recordReasonsAudit(pct, patient);
            }
            
            if (!inProtocol) {
            	pm.removePatientToProtocolAssignment(patient, pid);
            } 
            
            if (hasActiveDataEntry && activeInProtocol) {
            	FormManager fm = new FormManager();
            	fm.updateAdministeredForms(patient, activeAdminForms);
            }
            
       
            
            // Update attachments
            AttachmentManager am = new AttachmentManager();
            am.saveAttachments(attachmentMap);
        }
        catch ( AssemblerException ae ) {
        	logger.error("Could not correctly assemble the Patient object for the form object.", ae);
        	addActionError(getText(StrutsConstants.ERROR_FIELD_INVALID, new String[]{getText("subject.title.display")}));
        }
        catch ( DuplicateObjectException e ) {
        	logger.error("Duplicate subject found in the database while updating the current subject.", e);
        	addActionError(getText(StrutsConstants.ERROR_DUPLICATE, new String[]{getText("patient.label.SubjectID").toLowerCase(userLocale)}));
        }
        catch ( ServerFileSystemException sfse ) {
        	logger.error("Error occurred while saving attachments to the server's file system.", sfse);
        	addActionError(getText(StrutsConstants.ERROR_DATA_SAVE, new String[]{getText("attachment.label.lowerCase.display")}));
        }
        catch ( CtdbException ce ) {
        	logger.error("Database error occurred while creating a new subject.", ce);
        	addActionError(getText(StrutsConstants.ERROR_DATABASE_SAVE, new String[]{getText("subject.title.display").toLowerCase(userLocale)}));
        }
        
        // Check if there were any errors
        if ( hasActionErrors() ) {
        	strutsResult = BaseAction.ERROR;
        	
        	try {
        		setupAttachmentList(attachmentMap);
        	}
        	catch ( JSONException je ) {
    			logger.error("Could not setup attachment JSON array.", je);
    			return StrutsConstants.FAILURE;
    		}
        }
        else {
        	addActionMessage(getText(StrutsConstants.SUCCESS_EDIT_KEY, new String[]{getText("subject.title.display").toLowerCase(userLocale) + 
        			" " + patientDisplayLabel}));
            session.put(ACTION_MESSAGES_KEY, getActionMessages());
            PatientAction.clearSessionVariables(session);
        }
        
        return strutsResult;
    }
    
    public String cancelPatientChanges() {
    	@SuppressWarnings("unchecked")
		Map<Long, Attachment> attachmentMap = (Map<Long, Attachment>) session.get(CtdbConstants.ATTACHMENT_HASH_MAP);
    	
    	if ( attachmentMap != null ) {
    		// Try to close any opened input streams
    		for ( Attachment a : attachmentMap.values() ) {
    			InputStream input = a.getAttachFileContent();
    			if ( input != null ) {
    				try {
    					input.close();
    				}
    				catch ( IOException ie ) {
    					logger.warn("Could not close an input stream while clearing out the attachment map.", ie);
    				}
    			}
    		}
    	}
    	
    	PatientAction.clearSessionVariables(session);
    	
    	return BaseAction.SUCCESS;
    }
    
    public String addAttachment() {
    	String strutsResult = BaseAction.SUCCESS;
		User currUser = getUser();
    	@SuppressWarnings("unchecked")
		Map<Long, Attachment> attachmentMap = (Map<Long, Attachment>) session.get(CtdbConstants.ATTACHMENT_HASH_MAP);
    	
    	// Initialize the page.
    	setupPage();
    	
    	// Check if the hash map needs to be created
    	if ( attachmentMap == null ) {
    		logger.error("There was no attachment map in the session.");
    		return StrutsConstants.FAILURE;
    	}
    	
		// Check if a file was upload or one is in session.
		File attachFile = patientForm.getAttachment().getAttachFile();

		if ((attachFile == null) || (attachFile.length() == 0)) {
			Attachment a = (Attachment) session.get(CtdbConstants.ATTACHED_FILE);

			if (a != null) {
				patientForm.setAttachment(a);
			}

			session.remove(CtdbConstants.ATTACHED_FILE);
		}

    	// Validate the attachment before continuing
    	if ( isAttachmentValid() ) {
    		Attachment attach = patientForm.getAttachment();
    		
    		try {
    			// Set the attachment data
    			int newId = -1;
    			
    			// Ensure that the generated ID is truly unique within the attachment map
    			do {
    				newId = getNegativeUniqueId();
				} while (attachmentMap.containsKey(Long.valueOf(newId)));
    			
        		attach.setId(newId);
        		attach.setUpdated(true);
        		attach.updateCreatedByInfo(currUser);
        		setCommonAttachmentProperties(attach);
        		
        		// Save the new attachment in the hash table, save the new hash table to the session, and reset the form
        		attachmentMap.put(new Long(attach.getId()), attach);
        		patientForm.setAttachment(new Attachment());
    		}
    		catch ( FileNotFoundException fnfe ) {
    			logger.error("Could not find the attachment data file on the server.", fnfe);
    			addActionError(getText(StrutsConstants.ERROR_FILEUPLOAD_NOTFOUND, new String[]{attach.getAttachFileFileName()}));
    		}
    		catch ( CtdbException ce) {
    			logger.error("Database error occured while adding a file for a patient.", ce);
    			addActionError(getText(StrutsConstants.ERROR_DATABASE_SAVE, new String[]{getText("patient.Attachment")}));
    		}
    	}
    	
    	// Check for any action errors
    	if ( hasActionErrors() ) {
    		strutsResult = BaseAction.ERROR;
    	}
    	
    	// Set the data for the attachment table
    	try {
    		setupAttachmentList(attachmentMap);
    	}
    	catch ( JSONException je ) {
			logger.error("Could not setup attachment JSON array.", je);
			return StrutsConstants.FAILURE;
		}
    	
    	return strutsResult;
    }
    
	public String updateAttachment() {
    	String strutsResult = BaseAction.SUCCESS;
    	@SuppressWarnings("unchecked")
		Map<Long, Attachment> attachmentMap = (Map<Long, Attachment>) session.get(CtdbConstants.ATTACHMENT_HASH_MAP);
    	
    	// Initialize the page.
    	setupPage();
    	
    	// Validate the map object 
    	if ( attachmentMap == null ) {
    		logger.error("There was no attachment map in the session.");
    		return StrutsConstants.FAILURE;
    	}
    	
    	if ( isAttachmentValid() ) {
    		Attachment updatedAttach = patientForm.getAttachment();
			Attachment origAttach = attachmentMap.get(Long.valueOf(updatedAttach.getId()));
    		
    		try {
    			// If the attachment is not in the map throw an error
    			if ( origAttach == null ) {
    				throw new ObjectNotFoundException("Could not find the \"" + updatedAttach.getName() + "\" in the attachment map.");
    			}
    			
    			setCommonAttachmentProperties(updatedAttach);
    			
    			// Check for any changes
    			if ( hasAttachmentChanged(updatedAttach, origAttach) ) {
    				updatedAttach.setUpdated(true);
					attachmentMap.put(Long.valueOf(updatedAttach.getId()), updatedAttach);
    			}
    			
    			patientForm.setAttachment(new Attachment());
    		}
    		catch ( ObjectNotFoundException onfe ) {
    			logger.error("Updated attachment was not found in the attachment map.", onfe);
    			addActionError(getText(StrutsConstants.ERROR_NOTFOUND, new String[]{updatedAttach.getName()}));
    		}
    		catch ( FileNotFoundException fnfe ) {
    			logger.error("Could not find the uploaded file.", fnfe);
    			addActionError(getText(StrutsConstants.ERROR_FILEUPLOAD_NOTFOUND, new String[]{updatedAttach.getAttachFileFileName()}));
    		}
    		catch ( CtdbException ce ) {
    			logger.error("Database error occurred while updating an attachment.", ce);
    			addActionError(getText(StrutsConstants.ERROR_DATABASE_SAVE, new String[]{getText("patient.Attachment")}));
    		}
    	}
    	
    	// Check for any action errors
    	if ( hasActionErrors() ) {
    		strutsResult = BaseAction.ERROR;
    	}
    	
    	// Set the data for the attachment table
    	try {
    		setupAttachmentList(attachmentMap);
    	}
    	catch ( JSONException je ) {
			logger.error("Could not setup attachment JSON array.", je);
			return StrutsConstants.FAILURE;
		}
    	
    	return strutsResult;
    }
    
    public String deleteAttachments() {
    	String strutsResult = BaseAction.SUCCESS;
    	@SuppressWarnings("unchecked")
		Map<Long, Attachment> attachmentMap = (Map<Long, Attachment>) session.get(CtdbConstants.ATTACHMENT_HASH_MAP);
    	
    	List<Long> delIdList = this.getSelectedAttachmentIds();
		
		for ( Long id : delIdList ) {
			Attachment attach = attachmentMap.get(id);
			
			if ( attach != null ) {
				if ( attach.getId() <= 0 ) {
					// Remove a new attachment from the map
					try {
						if (attach.getAttachFileContent() != null) {
							attach.getAttachFileContent().close();
						}
					}
					catch ( IOException ie ) {
	    				logger.warn(id + " does not have any data associated to it.", ie);
	    			}
					
					attachmentMap.remove(Long.valueOf(attach.getId()));
				}
				else {
					// Set flags for an existing attachment that will be deleted when the subject is saved.
					attach.setUpdated(false);
					attach.setDeleted(true);
				}
			}
		}
    	
    	// Set the data for the attachment table
    	try {
    		setupAttachmentList(attachmentMap);
    	}
    	catch ( JSONException je ) {
			logger.error("Could not setup attachment JSON array.", je);
			return StrutsConstants.FAILURE;
		}

    	this.setupPage();
    	
    	return strutsResult;
    }
    

    public String getGuidToolUrl() {
    	String homeUrl = SysPropUtil.getProperty("brics.modules.home.url");
    	return homeUrl + "portal/guid/guidAction!launch.action";
    }
    
	public String getJwt() {
		String result = BaseAction.SUCCESS;
		String jwt = null;

		try {
			jwt = getJwtString();
			// Put the JWT in a JSON object.
			JsonObject resultObj = new JsonObject();

			resultObj.addProperty("jwt", jwt);
			jsonString = resultObj.toString();
		}
		catch (WebApplicationException wae) {
			logger.error("Error while getting the JWT. Cause: " + wae.getMessage());
			result = BaseAction.ERROR;
		}
		catch (InvalidJwtException e) {
			logger.error("The JWT string is not valid. Cause: " + e.getMessage());
			result = BaseAction.ERROR;
		}
		catch (IOException | RuntimeException e) {
			logger.error("Couldn't get the proxy ticket.", e);
			result = BaseAction.ERROR;
		}

		return result;
	}
	
	private String getJwtString() throws InvalidJwtException, NoRouteToHostException, UnknownHostException, RuntimeException {
		GuidJwt guidJwt = (GuidJwt) session.get(CtdbConstants.GUID_JWT_SESSION_KEY);
		String jwt = null;

		// Validate the GUID JWT stored in session.
		if ((guidJwt == null) || guidJwt.isAlmostExpired()) {
			String domainUrl = SysPropUtil.getProperty("webservice.usermgmt.url");
			String url = domainUrl + "portal/guid/guidAction!getJwt.action";

			// Get proxy ticket.
			SecuritySessionUtil ssu = new SecuritySessionUtil(request);
			String proxyTicket = ssu.getProxyTicket(domainUrl);

			if (!StringUtils.isEmpty(proxyTicket)) {
				url = ssu.compileProxiedWebserviceUrl(url, proxyTicket);
			}
			else {
				logger.error("Couldn't generate a proxy ticket for: " + url);
				return BaseAction.ERROR;
			}

			// Get the JWT
			Client client = ClientBuilder.newClient();
			WebTarget wt = client.target(url);
			jwt = wt.request(MediaType.TEXT_PLAIN).get(String.class);
			
			// Put the new object in session.
			guidJwt = new GuidJwt(jwt);
			session.put(CtdbConstants.GUID_JWT_SESSION_KEY, guidJwt);
		}
		else {
			jwt = guidJwt.toString();
		}
		
		return jwt;
	}
	
	private void setJwtCookie(String jwtString) {
		Cookie jwtCookie = new Cookie("sessToken", jwtString);
		jwtCookie.setMaxAge(-1);
		ServletActionContext.getResponse().addCookie(jwtCookie);
	}
    
    /**
     * Perform validation tests on attachment related inputs made by the user. Any resulting action
     * error messages will also be generated.
     * 
     * @return	True if and only if all input fields pass all validation tests.
     */
    private boolean isAttachmentValid() {
    	boolean isValid = true;
    	Attachment uploadedFile = patientForm.getAttachment();
    	
    	// Check the attachment name field
    	String attachName = uploadedFile.getName();
    	
    	if ( !Utils.isBlank(attachName) ) {
    		if ( attachName.length() > 256 ) {
    			addActionError(getText(StrutsConstants.ERROR_MAX_LENGTH, new String[]{getText("patient.AttchName"), "256"}));
    			isValid = false;
    		}
    	}
    	else {
    		addActionError(getText(StrutsConstants.ERROR_FIELD_REQUIRED, 
    			new String[]{getText("patient.AttchName"), getText("errors.singular")}));
    		isValid = false;
    	}
    	  	
    	// Check the attachment description field
    	String description = uploadedFile.getDescription();
    	
    	if ( !Utils.isBlank(description) && (description.length() > 4000) ) {
    		addActionError(getText(StrutsConstants.ERROR_MAX_LENGTH, 
    				new String[]{getText("patient.AttchDescription"), "4000"}));
			isValid = false;
    	}
    	
    	// Check the change reason field
    	String changeReason = uploadedFile.getChangeReason();
    	
    	if ( !Utils.isBlank(changeReason) && (changeReason.length() > 4000) ) {
    		addActionError(getText(StrutsConstants.ERROR_MAX_LENGTH, 
    				new String[]{getText("patient.AttchReasonForChange"), "4000"}));
			isValid = false;
    	}
    	
    	// Check for the binary file
    	File attachFile = uploadedFile.getAttachFile();

    	if ( ((attachFile == null) || (attachFile.length() == 0)) && Utils.isBlank(uploadedFile.getFileName()) ) {
    		addActionError(getText(StrutsConstants.ERROR_FIELD_REQUIRED, 
        			new String[]{getText("patient.Attachment"), getText("errors.singular")}));
        	isValid = false;
    	} else {
			// Save the binary file to session when there is an error on the page and upload file is valid.
			if (!isValid) {
				// Create a file handle to point to the tmp file Struts2 created, this way it won't be
				// deleted after the response is sent.
				try {
					if (uploadedFile.getAttachFileContent() == null) {
						uploadedFile.setAttachFileContent(new FileInputStream(attachFile));
					}

					// Add the copied file into session.
					session.put(CtdbConstants.ATTACHED_FILE, uploadedFile);
				} catch (IOException e) {
					logger.error("Failed to open the upload file for input in validation.", e);
				}
    		}
    	}
    	
    	return isValid;
    }
    

    
    /**
     * Set Attachment object properties that are needed by both the add and edit attachment actions. The more
     * important properties set are the attachment's category and file data association.
     * 
     * @param attachment - The target Attachment object to update
     * @throws CtdbException	If there any database errors.
     * @throws FileNotFoundException	If the uploaded file can not be found in the server's file system.
     */
    private void setCommonAttachmentProperties(Attachment attachment) throws CtdbException, FileNotFoundException {
		User u = getUser();
    	AttachmentCategoryManager acMan = new AttachmentCategoryManager();
    	
    	// MIN_INTERGER ATT ID bug
        if ( attachment.getCategory().getId() <= 0 ) {
        	int protocolId = ((Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY)).getId();
        	Integer attType = (Integer) session.get(CtdbConstants.ATTACHMENT_TYPE_ATTR);
        	AttachmentCategory attCate = acMan.getDefaultCategoryId(protocolId, attType);
        	attachment.setCategory(attCate);
        } else {
			attachment.setCategory(acMan.getAttachmentCategory(attachment.getCategory().getId()));
        }
        
        File binaryFile = attachment.getAttachFile();
        
        if ( binaryFile != null ) {
			if (attachment.getAttachFileContent() == null) {
				// Create a file handle to point to the tmp file Struts2 created, this way it won't be
				// deleted after page submission.
				attachment.setAttachFileContent(new FileInputStream(binaryFile));
			}

    		attachment.setFileName(attachment.getAttachFileFileName());
        } 
		
		attachment.setAssociatedId(this.getPatientId());
		attachment.setType(new CtdbLookup(AttachmentManager.FILE_PATIENT));
		attachment.updateUpdatedByInfo(u);
		attachment.setPublicationType(new CtdbLookup(PatientAction.PUB_TYPE_OTHER));
    }
    
    /**
     * Compares the user supplied Attachment object with the original Attachment object to determine
     * if there are any changes. Only the Attachment properties that are accessible on the add/edit
     * subject page will be considered.
     * 
     * @param newAttachment - The Attachment object that contains potential changes to the attachment.
     * @param oldAttachment - The original Attachment object from the attachment map.
     * @return	True if any changes are detected in the attachment name, file name, or description.
     */
    private boolean hasAttachmentChanged(Attachment newAttachment, Attachment oldAttachment) {
    	boolean changed = false;
    	
    	changed = !oldAttachment.getName().equals(newAttachment.getName()) || 
    			!oldAttachment.getFileName().equals(newAttachment.getFileName()) ||
    			!oldAttachment.getDescription().equals(newAttachment.getDescription()) ||
    			(oldAttachment.getCategory().getId() != newAttachment.getCategory().getId());
    	
    	return changed;
    }
    
    /**
     * Setup the attachment list and the attachment JSON array to be used by the JSP.
     * 
     * @param attachmentMap - The Map object containing attachments added or edited by the user.
     * @throws JSONException	If the JSON array could not be constructed.
     */
    private void setupAttachmentList(Map<Long, Attachment> attachmentMap) throws JSONException {
    	List<Attachment> displayableList = new ArrayList<Attachment>(attachmentMap.size());
    	JSONArray attachListJSON = new JSONArray();
    	
    	// Create a JSON version of the attachment list
    	for ( Attachment a : attachmentMap.values() ) {
    		if ( !a.isDeleted() ) {
	    		JSONObject attachJSON = new JSONObject();
	    		
	    		// Create the attachment JSON object
	    		attachJSON.put("id", a.getId());
	    		attachJSON.put("name", a.getName());
	    		attachJSON.put("fileName", a.getFileName());
	    		attachJSON.put("associatedId", a.getAssociatedId());
	    		attachJSON.put("description", a.getDescription());
	    		attachJSON.put("categoryId", a.getCategory().getId());
	    		
	    		// Add new object to the JSON array, and add the attachment to the displayable list.
	    		attachListJSON.put(attachJSON);
	    		displayableList.add(a);
    		}
    	}
    	
    	// Add the resulting JSON array to member variable to be sent to in the response.
    	attachmentListJson = attachListJSON.toString();
    	
    	// Add the attachment list to the request.
    	request.setAttribute(CtdbConstants.PATIENT_ATTACHMENTS_SESSION_KEY, displayableList);
    }
    
	// url: http://fitbir-portal-local.cit.nih.gov:8082/ibis/patient/getAttachmentsListForDT.action
	public String getAttachmentsListForDT() {
		Map<Long, Attachment> attachmentMap = (Map<Long, Attachment>) session.get(CtdbConstants.ATTACHMENT_HASH_MAP);
		List<Attachment> patientAttachmentsList = new ArrayList<Attachment>();
    	for ( Attachment a : attachmentMap.values() ) {
    		patientAttachmentsList.add(a);
    	}
		
		try {
			IdtInterface idt = new Struts2IdtInterface();			
			ArrayList<Attachment> outputList = new ArrayList<Attachment>(patientAttachmentsList);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new AttachmentPatientDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}
    
    public String autoIncrement() {
    	
		User user = getUser();
        Protocol protocol = (Protocol)session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
        try {
        	PatientManager pm = new PatientManager();
        	
            // Get list of patients for this protocol that have no subject number 
        	// (order by enrollment date then last name)
        	Map<Integer, PatientProtocol> patientProtocols = pm.getPatientsWithoutSubjectNumberByProtocol(protocol);
            int subjectNumber = pm.findNextSubjectNumber(protocol);
            
            //For each patient, increment the subject number
            for (PatientProtocol patientProtocol : patientProtocols.values()) {
                patientProtocol.setSubjectNumber(StringUtils.defaultString(
                		protocol.getSubjectNumberPrefix()) + 
                		padSubjectNumber(subjectNumber) +
                		StringUtils.defaultString(protocol.getSubjectNumberSuffix()));
                patientProtocol.setUpdatedBy(user.getId());
                subjectNumber++;
            }
            
            pm.updatePatientProtocol(patientProtocols);
            
    	} catch (CtdbException ce) {
        	ce.printStackTrace();
            return StrutsConstants.EXCEPTION;
        }
        return SUCCESS;
    }

	/**
     * Calculates the next subject number for a protocol
     *
     * @param pm - the patient manager delegate
     * @param protocol - the protocol of the patient
     * @return string value of the next subject number
     * @throws CtdbException the exception
     */
    private String getFormattedNextSubjectNumber(PatientManager pm, Protocol protocol) 
    		throws CtdbException {
        int subjNumValue = pm.findNextSubjectNumber(protocol);
        return StringUtils.defaultString(
        		protocol.getSubjectNumberPrefix()) + 
        		padSubjectNumber(subjNumValue) +
        		StringUtils.defaultString(protocol.getSubjectNumberSuffix());
    }

    /**
     * Pads the subject number start with zeros
     *
     * @param startNumber the starting value of the subject number
     * @return the padded subject number start value
     */
    private String padSubjectNumber(int startNumber) {
        StringBuffer sb = new StringBuffer();
        //Increment subject number
        String incrementedSubjectNumber = String.valueOf(startNumber);
        //Left pad with zeroes
        for (int i = 0; i < 5 - incrementedSubjectNumber.length(); i++) {
            sb.append("0");
        }
        sb.append(incrementedSubjectNumber);
        return sb.toString();
    }
    
    /**
     * Creates a new attachment map based on the given list of attachments. The key will be that ID of the attachment.
     * 
     * @param attachmentList - A list of Attachment objects the map will be based on.
     * @return	A map of Attachment objects from the given list, or an empty map if the list is also empty.
     */
    private Map<Long, Attachment> generateAttachmentMap(List<Attachment> attachmentList) {
    	Map<Long, Attachment> attachmentMap = new Hashtable<Long, Attachment>();
    	
    	for ( Attachment a : attachmentList ) {
    		attachmentMap.put(new Long(a.getId()), a);
    	}
    	
    	return attachmentMap;
    }
	
    // TODO: a better fix should be within JSP and set these value as hidden default below.
    private void setDefaultPatientInfo(boolean nonPII) {

    	if (!nonPII) {
    		return;
    	}
    	// following fields are set only when PII is disallowed.
    	patientForm.setState("0");
    	patientForm.setCountry("0");
    	patientForm.setBirthCountryId(0);
    	patientForm.setSex("1"); // unknown;
    }

	public int getPatientId() {
		return patientId;
	}
	
	public void setPatientId(int patientId) {
		this.patientId = patientId;
	}

	public PatientForm getPatientForm() {
		return patientForm;
	}
	public void setPatientForm(PatientForm patientForm) {
		this.patientForm = patientForm;
	}

	public String getGuidToValidate() {
		return guidToValidate;
	}

	public void setGuidToValidate(String guidToValidate) {
		this.guidToValidate = guidToValidate;
	}

	public String getSkipcheckcodematch() {
		return skipcheckcodematch;
	}
	public void setSkipcheckcodematch(String skipcheckcodematch) {
		this.skipcheckcodematch = skipcheckcodematch;
	}

	public String getAssociatedWithStudy() {
		return associatedWithStudy;
	}
	public void setAssociatedWithStudy(String associatedWithStudy) {
		this.associatedWithStudy = associatedWithStudy;
	}

	public List<Long> getSelectedAttachmentIds() {
		return selectedAttachmentIds;
	}

	public void setSelectedAttachmentIds(List<Long> selectedAttachmentIds) {
		this.selectedAttachmentIds = selectedAttachmentIds;
	}

	/**
	 * @return the attachmentListJson
	 */
	public String getAttachmentListJson() {
		return attachmentListJson;
	}

	/**
	 * @param attachmentListJson the attachmentListJson to set
	 */
	public void setAttachmentListJson(String attachmentListJson) {
		this.attachmentListJson = attachmentListJson;
	}

	public String validateGuid() throws IOException, JSONException {
    	
    	Boolean isValid = this.doesGuidExist(guidToValidate);
    	logger.debug("Guid " + guidToValidate + " is " + isValid);
    	
    	JsonObject respObj = new JsonObject();
    	respObj.addProperty("guidValid", isValid.toString());
        
    	HttpServletResponse response = ServletActionContext.getResponse();
    	response.setContentType(MediaType.TEXT_PLAIN);
    	response.getWriter().write(respObj.toString());
    	
		return null;		
	}

	
	public boolean doesGuidExist(String guid) {
		
		boolean isValid = false;
		
		String restfulDomain = SysPropUtil.getProperty("brics.modules.home.url")
				+ SysPropUtil.getProperty("webservice.guidservice.url");
		String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.guidservice.validate.url");
		
		try {
			Authentication auth = (SecurityContextHolder.getContext().getAuthentication());
			Assertion assertion = ((CasAuthenticationToken) auth).getAssertion();
			String url = restfulDomain + "j_spring_cas_security_check";
			String proxyTicket = assertion.getPrincipal().getProxyTicketFor(url);

			if (proxyTicket != null) {
				Client client = ClientBuilder.newClient();
				SecuritySessionUtil ssu = new SecuritySessionUtil(request);
				restfulUrl= ssu.compileProxiedWebserviceUrl(restfulUrl + guid, proxyTicket);
				
		    	logger.info("Calling Guid restfulUrl " + restfulUrl);

				WebTarget wt = client.target(restfulUrl);
				isValid = wt.request(MediaType.TEXT_PLAIN).get(Boolean.class);
			}
		} catch(Exception e){
			e.printStackTrace();
		}

		return isValid;  
	}

	/**
	 * @return the jsonString
	 */
	public String getJsonString() {
		return jsonString;
	}

	/**
	 * @param jsonString the jsonString to set
	 */
	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}

}
