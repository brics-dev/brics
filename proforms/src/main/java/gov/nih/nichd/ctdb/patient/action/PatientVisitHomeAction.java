package gov.nih.nichd.ctdb.patient.action;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.ModulesConstants;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.DictionaryUtil;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.patient.common.PatientVisitResultControl;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.domain.PatientVisit;
import gov.nih.nichd.ctdb.patient.domain.PatientVisitPrepopValue;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;
import gov.nih.nichd.ctdb.patient.tag.PatientVisitIdtDecorator;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.protocol.domain.IntervalClinicalPoint;
import gov.nih.nichd.ctdb.protocol.domain.IntervalScheduleDisplay;
import gov.nih.nichd.ctdb.protocol.domain.PrepopDataElement;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.selfreporting.util.SelfReportingAcessControlUtil;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.commons.service.util.MailEngine;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

public class PatientVisitHomeAction extends BaseAction {

	private static final long serialVersionUID = -1402793648963977693L;
	private static final Logger logger = Logger.getLogger(PatientVisitHomeAction.class);
	private static final Integer VISIT_COMMENTS_MAX_LENGTH = 1000;

    @Autowired
    MailEngine mailEngine;

	private String id;
    private int patientId;
	private String patientEmail;
	private String protocolId;
	//private String subjectId;
    private String visitTypeId;
	private String visitDateStr;
	private String visitComments;
	private List<IntervalClinicalPoint> intClinicalPntList = new ArrayList<IntervalClinicalPoint>();
	private Date vsDate;
    private String firstName;
	private String lastName;
	private String token;
	private String pvPrepopValues;
	private String jsonString;
	private String errRespMsg;
	private List<PatientVisit> pvList;
	private String selectedClinicPntId;
	
	private List<Integer> siteIds = new ArrayList<Integer>();

	public PatientVisitHomeAction() {
		patientId = -1;
		pvPrepopValues = CtdbConstants.EMPTY_JSON_ARRAY_STR;
		jsonString = CtdbConstants.EMPTY_JSON_OBJECT_STR;
	}
	
	private class PatientVisitComparator implements Comparator<PatientVisit> {
		public PatientVisitComparator() {}
		
		@Override
		public int compare(PatientVisit pv1, PatientVisit pv2) {
			if (pv1 != null && pv2 != null) {
				if (pv1.getVisitDate() != null && pv2.getVisitDate() != null) {
					int compare = pv1.getVisitDate().compareTo(pv2.getVisitDate());
					if (compare == 0) {
						return pv1.getGuid().compareToIgnoreCase(pv2.getGuid());
					} else {
						return -(pv1.getVisitDate().compareTo(pv2.getVisitDate()));
					}
				} else {
					return 0;
				}
			} else {
				return 0;
			}
		}
	}
	
	/**
	 * Sets up needed data that is required to show the patient visit page.
	 * 
	 * @throws CtdbException If there is a database error.
	 * @throws JSONException If their is a JSON parsing error.
	 */
	private void setupPage() throws CtdbException, JSONException {
		buildLeftNav(LeftNavController.LEFTNAV_SUBJECTS_VISITS);
		clearPatientVisitHomeSession();
		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		int protocolId = protocol.getId();
		PatientManager pm = new PatientManager();
		
		List<Patient> patients = new ArrayList<Patient>();
		
		if(!isUserSiteCheckNeeded()) {
			 patients  = pm.getMinPatientsByProtocolIdSiteIds(protocolId,null);
		}else {
			siteIds = getUserAssignedSites();
			patients  = pm.getMinPatientsByProtocolIdSiteIds(protocolId,siteIds);
		}
		
		pm.updatePatientSubjectNumbers(patients, protocol);
		session.put(CtdbConstants.PATIENT_LIST_KEY, patients);
		
		
		// Get patient visits.
		PatientVisitResultControl pvrc = new PatientVisitResultControl();
		
		pvrc.setProtocolId(protocol.getId());
		
		if (patientId > 0) {
			pvrc.setPatientId(patientId);
			request.setAttribute(CtdbConstants.VISIT_TYPE_SCHEDULER_LIST_KEY, pm.getIntervalsWithSchedulerStatus(patientId, protocolId));
		}

		if(!isUserSiteCheckNeeded()) {
			setPvList(pm.getPatientVisitsByUserSiteIds(pvrc,protocol.getId(),null));
		}
		else {
			siteIds = getUserAssignedSites();
			setPvList(pm.getPatientVisitsByUserSiteIds(pvrc,protocol.getId(),siteIds));
		}
		
		
		Collections.sort(getPvList(), new PatientVisitComparator());
		request.setAttribute(CtdbConstants.PATIENT_VISIT_DATE_LIST_KEY, getPvList());
		session.put("patientVisitList", this.getPvList());

		// Get the list of visit types for the current protocol.
		ProtocolManager protMan = new ProtocolManager();
		List<Interval> vTList = protMan.getIntervals(protocolId);
		//request.setAttribute(CtdbConstants.VISIT_TYPE_OPTIONS, protMan.getIntervals(protocolId));
		session.put(CtdbConstants.VISIT_TYPE_OPTIONS, vTList);
		
		for(Interval vt : vTList){
			List<IntervalClinicalPoint> vTClinicalPnts = protMan.getIntervalClinicalPntsForInterval(vt.getId());
			intClinicalPntList.addAll(vTClinicalPnts);
		}
	}

	/**
	 * The default action method of this action class. Used to setup the initial patient visit page.
	 * 
	 * @return The Struts result path for this invocation.
	 */
    public String execute() {
        String resultPath = BaseAction.SUCCESS;
        
    	try {
    		this.setupPage();
    		
    		// Check if a success message will need to be forwarded.
    		if ( session.containsKey(CtdbConstants.SUCCESS_MSG_SESSION_KEY) ) {
    			String successMsg = (String) session.get(CtdbConstants.SUCCESS_MSG_SESSION_KEY);
    			
    			addActionMessage(successMsg);
    			session.remove(CtdbConstants.SUCCESS_MSG_SESSION_KEY);
    		}
    		
    		// Check if the pre-population data element cache needs to be created.
    		if ( !session.containsKey(CtdbConstants.PRE_POP_DE_CACHE_KEY) ) {
    			DictionaryUtil dictUtil = new DictionaryUtil();
    			Map<String, DataElement> deCache = dictUtil.getDeMapForAllPrePopDes(request);
    			
    			logger.info("Saving the data element cache to session...");
    			session.put(CtdbConstants.PRE_POP_DE_CACHE_KEY, deCache);
    		}
    	}
    	catch ( JSONException je ) {
    		logger.error("A JSON error occured while generating the pre-population array.", je);
    		addActionError(getText(StrutsConstants.ERROR_PAGE_SETUP));
    		pvPrepopValues = CtdbConstants.EMPTY_JSON_ARRAY_STR;
    	}
    	catch ( CtdbException ce ) {
    		logger.error("A database error occurred while setting up the page.", ce);
    		addActionError(getText(StrutsConstants.ERROR_PAGE_SETUP));
    	}
    	catch ( Exception e ) {
    		logger.error("An error occurred while setting up the page.", e);
    		addActionError(getText(StrutsConstants.ERROR_PAGE_SETUP));
    	}
    	
    	// Check for action errors.
    	if ( hasActionErrors() ) {
    		// Rest all of the needed data for the page with empty lists.
    		if ( request.getAttribute(CtdbConstants.PATIENT_LIST_KEY) == null ) {
    			request.setAttribute(CtdbConstants.PATIENT_LIST_KEY, new ArrayList<Patient>());
    		}
    		
    		if ( request.getAttribute(CtdbConstants.PATIENT_VISIT_DATE_LIST_KEY) == null ) {
    			request.setAttribute(CtdbConstants.PATIENT_VISIT_DATE_LIST_KEY, new ArrayList<PatientVisit>());
    		}
    		
    		if ( request.getAttribute(CtdbConstants.VISIT_TYPE_OPTIONS) == null ) {
    			//request.setAttribute(CtdbConstants.VISIT_TYPE_OPTIONS, new ArrayList<Interval>());
    			session.put(CtdbConstants.VISIT_TYPE_OPTIONS, new ArrayList<Interval>());
    		}
    		
    		resultPath = StrutsConstants.EXCEPTION;
    	}
		
		return resultPath;
    }

	private void clearPatientVisitHomeSession() {
		session.remove("patientVisitList");
		session.remove(CtdbConstants.VISIT_TYPE_OPTIONS);
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8082/ibis/getPatientVisitList.action
	public String getPatientVisitList() throws Exception {
		List<PatientVisit> pVList =  (List<PatientVisit>) session.get("patientVisitList");

		try {
			IdtInterface idt = new Struts2IdtInterface();
			this.setupPage();
			ArrayList<PatientVisit> outputList = new ArrayList<PatientVisit>(pVList);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new PatientVisitIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			e.printStackTrace();
			return StrutsConstants.FAILURE;
		}
		return null;
	}
    
	public String getUpdateByPatient() throws Exception {
		this.patientId = this.getPatientId();
		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		int protocolId = protocol.getId();
		
		PatientVisitResultControl pvrc = new PatientVisitResultControl();		
		pvrc.setProtocolId(protocol.getId());
		PatientManager pm = new PatientManager();
		if (patientId > 0) {
			pvrc.setPatientId(patientId);
			/*Add visit type scheduler list key to enable vt scheduler */
			List<IntervalScheduleDisplay> intervalSchDisList = pm.getIntervalsWithSchedulerStatus(patientId, protocolId);
			request.setAttribute(CtdbConstants.VISIT_TYPE_SCHEDULER_LIST_KEY, intervalSchDisList);
		}
		
		/*Update the patient visit list by selected patient */
		
		if(!isUserSiteCheckNeeded()) {
			setPvList(pm.getPatientVisitsByUserSiteIds(pvrc,protocol.getId(),null));
		}
		else {
			siteIds = getUserAssignedSites();
			setPvList(pm.getPatientVisitsByUserSiteIds(pvrc,protocol.getId(),siteIds));
		}
		Collections.sort(getPvList(), new PatientVisitComparator());
		request.setAttribute(CtdbConstants.PATIENT_VISIT_DATE_LIST_KEY, getPvList());
		session.put("patientVisitList", this.getPvList());

		return BaseAction.SUCCESS;
	}
    
    /**
	 * The default action method of this action class. Used to load data of a target patient visit for editing.
	 * 
	 * @return The Struts result path for this invocation.
	 */
    public String getPatientVisit() {
    	String resultPath = BaseAction.SUCCESS;
    	
    	// Check if visit data needs to be loaded.
		if ( !Utils.isBlank(id) && Utils.isNumeric(id) ) {
			try {
				PatientManager pm = new PatientManager();
				PatientVisit pv = pm.getPatientVisit(Long.parseLong(id));
				
				// Load patient visit data.
				patientId = pv.getPatientId();
				visitDateStr = pv.getFormattedVisitDate();
			
				
				if ( pv.getIntervalId() > 0 ) {
					visitTypeId = Long.toString(pv.getIntervalId());
				}
				
				// Load pre-population values.
				List<PatientVisitPrepopValue> pvPrepopValueList = pm.getCompletePrePopValList(pv);
				
				pvPrepopValues = pvPrepopValueListToJsonArrayString(pvPrepopValueList);
				visitComments = pv.getComments();
				selectedClinicPntId = String.valueOf(pv.getIntervalClinicalPointId());	
			}
			catch (ObjectNotFoundException onfe) {
				logger.error("Could not find patient visit with ID of " + id + ".", onfe);
				addActionError(getText(StrutsConstants.ERROR_NOTFOUND, new String[]{getText("patient.scheduleVisit.singular.label")}));
			}
			catch (CtdbException ce) {
				logger.error("Database error occurred while getting the patient visit.", ce);
				addActionError(getText(StrutsConstants.ERROR_DATABASE_GET, new String[]{getText("patient.scheduleVisit.singular.label")}));
			}
			catch (JSONException je) {
				logger.error("Error occurred when converting the pre-population value list to a JSON array.", je);
				addActionError(getText(StrutsConstants.ERROR_DATA_GET, new String[]{getText("form.forms.prepopvalue.display")}));
			}
		}
		else {
			logger.error("The patient visit ID (" + id + ") is not valid.");
			addActionError(getText(StrutsConstants.ERROR_DATA_GET, new String[]{getText("patient.scheduleVisit.singular.label")}));
		}
		
		// Check for action errors.
		if ( hasActionErrors() ) {
			resultPath = StrutsConstants.EXCEPTION;
		}
		
		// Finish setting up data for the page.
		try {
			this.setupPage();
		}
		catch (Exception e) {
			logger.error("Couldn't setup the page, while getting the patient visit by ID (" + id + ").", e);
			resultPath = BaseAction.ERROR;
		}
		
		return resultPath;
    }
    
    /**
     * Action method that retrieves all patient visit pre-population data elements for the given visit type
     * ID. The values will be returned as a JSON array.
     * 
     * @return A Struts result path for the current invocation if this action method.
     */
    public String getDePrePopJsonArray() {
    	String resultPath = BaseAction.SUCCESS;
    	
    	try {
        	String vtId = request.getParameter("visitTypeId");
        	
        	// Validate the patient visit ID.
        	if ( !Utils.isBlank(vtId) && Utils.isNumeric(vtId) ) {
        		ProtocolManager pm = new ProtocolManager();
        		List<PrepopDataElement> prePopDeList = pm.getPrepopDEsForInterval(Integer.parseInt(vtId), false);
            	
        		logger.info(prePopDeList.size() + " pre-pop data elements retrieved for visit type ID " + vtId + ".");
            	jsonString = prePopDeListToJsonArrayString(prePopDeList);
        	}
        	else {
        		logger.error("The visit type ID " + vtId + " is not valid.");
        		errRespMsg = getText(StrutsConstants.ERROR_FIELD_INVALID, new String[]{getText("protocol.visitType.title.display")});
        		resultPath = StrutsConstants.BAD_REQUEST;
        	}
    	}
    	catch (CtdbException ce) {
			logger.error("Error occurred while getting the list of pre-pop data elements from the database.", ce);
			errRespMsg = getText(StrutsConstants.ERROR_DATABASE_GET, new String[]{getText("form.forms.prepopvalue.display")});
			resultPath = BaseAction.ERROR;
		}
		catch (JSONException je) {
			logger.error("Error occurred while getting the pre-pop data element array JSON.", je);
			errRespMsg = getText(StrutsConstants.ERROR_DATA_GET, new String[]{getText("form.forms.prepopvalue.display")});
			resultPath = BaseAction.ERROR;
		}
    	
    	return resultPath;
    }
    
    /**
     * Action method that handles the saving changes to the current patient visit.
     * 
     * @return The Struts result string for this invocation.
     * @throws Exception
     */
    public String savePatienVisit() {
    	String resultPath = BaseAction.SUCCESS;
		PatientManager pm = new PatientManager();
		ProtocolManager protMan = new ProtocolManager();
		
		try {
	
			
			/*add new or update existing the prepopValues*/
			List<PatientVisitPrepopValue> pvPrepopValueList = this.jsonStrArrayToPrepopValueList(getPvPrepopValues());
			
			// Validate the current patient visit data before continuing.
			if ( isPatientVisitValid(pm, pvPrepopValueList) ) {
				User user = getUser();
		        Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		    	PatientVisit pv = new PatientVisit();
		    	pv.setPatientId(getPatientId());
				
		    	pv.setProtocolId(protocol.getId());
		    	
		    	int vtId = Integer.parseInt(visitTypeId);
		    	
		    	pv.setIntervalId(vtId);
		        pv.setVisitDate(vsDate);
		        pv.setUpdatedBy(user.getId());
				// The instance with this property to be false, like pdbp and fitbir will not have these two fields set
				// to patient visit
				if (Boolean.valueOf(SysPropUtil.getProperty("display.protocol.clinicalPoint"))) {
					pv.setComments(visitComments);
					int pntId = Integer.parseInt(this.selectedClinicPntId);
					pv.setIntervalClinicalPointId(pntId);
				}
				// Is this a visit type other than "other" and does it have any forms marked as self reporting forms...if
				// so...then create token
				if ( (vtId > 0) && protMan.doesVisitTypeHaveAnySelfReportingForms(vtId) ) {
					String token;
					
					do {
						token = SelfReportingAcessControlUtil.createRandomToken();
					} while (pm.doesPatientVisitExistWithThisToken(token));

					pv.setToken(token);
				}
				
				if (Utils.isBlank(getId())) {
					pv.setId(Integer.MIN_VALUE);
					pv.setCreatedBy(user.getId());

					pm.createPatientVisit(pv);
					pm.createPatientVisitPrepopValues(pv, pvPrepopValueList);
				}
				else {
		        	pv.setId(Integer.parseInt(getId()));
		    		pm.updatePatientVisit(pv);
		    		pm.updatePatientVisitPrepopValues(pv, pvPrepopValueList);
		        }
				
				session.put(CtdbConstants.SUCCESS_MSG_SESSION_KEY, 
						getText(StrutsConstants.SUCCESS_EDIT_KEY, new String[]{getText("patient.scheduleVisit.singular.label")}));
			}
		}
		catch ( CtdbException ce ) {
			logger.error("Database error occurred while saving the patient visit.", ce);
			addActionError(getText(StrutsConstants.ERROR_DATABASE_SAVE, new String[]{getText("patient.scheduleVisit.singular.label")}));
		}
		catch ( JSONException je ) {
			logger.error("Could not parse the prepopulation JSON array.", je);
			addActionError(getText(StrutsConstants.ERROR_DATA_GET, new String[]{getText("form.forms.prepopvalue.display")}));
		}
    	
    	// Return to the JSP if there are any errors.
		if (this.hasActionErrors()) {
			try {
				this.setupPage();
				resultPath = StrutsConstants.EXCEPTION;
			}
			catch (Exception e) {
				logger.error("Couldn't setup the page.", e);
				resultPath = BaseAction.ERROR;
			}
    	}
		
		return resultPath;
    }

    public String deletePatientVisit() {
    	String resultPath = BaseAction.SUCCESS;
    	String selectedIdsStr = request.getParameter("selectedIds");
		
		try {
			if (selectedIdsStr != null) {
				JSONArray selectedIdArray = new JSONArray(selectedIdsStr);
				List<Integer> deletedList = new ArrayList<Integer>();
				PatientManager pm = new PatientManager();
				String[] selectedPatientIds = jsonArrayToStringArray(selectedIdArray);
				
				if (selectedPatientIds != null && selectedPatientIds.length > 0) {
					deletedList = pm.deletePatientVisits(selectedPatientIds);
					
					if (deletedList != null && !deletedList.isEmpty()) {
						addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_KEY, new String[]{getText("patient.visit.display")}));
					}
				}
			}
		}
		catch (Exception e) {
			logger.error("Could not delete the one or more of the following visits: " + request.getParameter("selectedIds"), e);
			addActionError("The selected subject visit date failed to be deleted.");
			resultPath = StrutsConstants.EXCEPTION;
		}
		
		// Refresh patient visit page data.
		try {
    		this.setupPage();
    	}
    	catch (Exception e) {
    		logger.error("An error occurred while setting up the page.", e);
    		resultPath = BaseAction.ERROR;
    	}
		
		return resultPath;
    }
    
    public String sendEmailPSR() 
    {
    	String resultPath = BaseAction.SUCCESS;

		// Finish setting up data for the page.
		try {
			this.setupPage();
		}
		catch (Exception e) {
			logger.error("Couldn't setup the page, while getting the patient visit by ID (" + id + ").", e);
			return StrutsConstants.EXCEPTION;
		}

		/*
		 * Validation to check patient email is not empty.
		 */
        if( Utils.isBlank( patientEmail ) )
        {
    		logger.error("The Patient's email address is missing. PSR Email has not been sent.");
        	addActionError("The Patient's email address is missing. PSR Email has not been sent.");
			return StrutsConstants.EXCEPTION;
        }
        
		/*
		 *  Need to use clinical point. Email is sent out to them. 
		 */
		if (Boolean.valueOf(SysPropUtil.getProperty("display.protocol.clinicalPoint"))) 
		{
			if (!Utils.isBlank(selectedClinicPntId) && !Utils.isNumeric(selectedClinicPntId)) 
			{
				logger.error("Interval clinical point has not been selected.");
				addActionError(getText(StrutsConstants.ERROR_FIELD_INVALID,
						new String[] {getText("protocol.intervalClinicalPoint.id.display")}));
				return StrutsConstants.EXCEPTION;
			} 
			else 
			{
				Integer clinicPntId = Integer.parseInt(selectedClinicPntId);
				IntervalClinicalPoint selectedIntervalClinicalPoint = null;
				
				for( IntervalClinicalPoint intervalClinicalPoint : intClinicalPntList )
				{
					if( intervalClinicalPoint != null && intervalClinicalPoint.getId() == clinicPntId )
					{
						selectedIntervalClinicalPoint = intervalClinicalPoint;
						break;
					}
				}
				
		    	Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		    	
		    	/*
		    	 * Retrieve the email template and send email.
		    	 */
		    	ResourceBundle mailProperties = ResourceBundle.getBundle("mail");
		    	String subject =
		    			MessageFormat.format(mailProperties.getString(ModulesConstants.MAIL_RESOURCE_EMAIL_PSR + ModulesConstants.MAIL_RESOURCE_SUBJECT),
								(protocol != null && protocol.getName() != null) ? "" + protocol.getName() : "");
		
		        String messageText = 
		        		MessageFormat.format(mailProperties.getString(ModulesConstants.MAIL_RESOURCE_EMAIL_PSR + ModulesConstants.MAIL_RESOURCE_BODY), 
		        			(protocol != null && protocol.getName() != null) ? "" + protocol.getName() : "", // {0}
			                token, // {1}
			                selectedIntervalClinicalPoint.getPointOfContact().getFullName(), // {2}
			                selectedIntervalClinicalPoint.getPointOfContact().getEmail(), // {3}
			                selectedIntervalClinicalPoint.getPointOfContact().getPhone(), // {4} 
			                selectedIntervalClinicalPoint.getPointOfContact().getAddress() // {5}
		        		);
		        
		        String toAddrStr = getPatientEmail();
		        String fromAddrStr = SysPropUtil.getProperty("admin.email.address.cistar");;
		
		        try
		        {
		            mailEngine.sendMail(subject, messageText, fromAddrStr, toAddrStr);
		    		addActionMessage("PSR Email has been sent successfully.");
		        }
		        catch (MessagingException e)
		        {
					logger.error("Error sending PSR email", e);
					addActionError("Error sending PSR email.");
					resultPath = StrutsConstants.EXCEPTION;
		        }
			}
		}
		
		return resultPath;
    }
    
    
    /**
     * Runs a suite of validation tests on the submitted patient visit.
     * 
     * @param pm - Used to get data from the database.
     * @param pvpvList - List of PatientVisitPrepopValue objects to validate.
     * @return True if and only if the submitted patient visit data passes all validation tests.
     */
	private boolean isPatientVisitValid(PatientManager pm, List<PatientVisitPrepopValue> pvpvList) {
    	// Validate the patient visit ID.
		long pvID = -1L;
		
    	if ( !Utils.isBlank(id) && !Utils.isNumeric(id) ) {
    		addActionError(getText(StrutsConstants.ERROR_FIELD_INVALID, new String[]{getText("patient.scheduleVisit.id.display")}));
    	}
    	else if ( !Utils.isBlank(id) ) {
    		pvID = Long.parseLong(id);
    	}

    	// Validate patient ID.
		if ( patientId <= 0 ) {
    		addActionError(getText(StrutsConstants.ERROR_REQUIRED_GUID_SUBJECT_ID));
    	}
		
		// Validate the visit type ID.
		long vsId = -1L;
		
		if ( !Utils.isBlank(visitTypeId) && !Utils.isNumeric(visitTypeId) ) {
			logger.error("The submitted visit type ID of " + visitTypeId + " is not valid.");
			addActionError(getText(StrutsConstants.ERROR_FIELD_INVALID, new String[]{getText("protocol.visittype.id.display")}));
		}
		else if ( !Utils.isBlank(visitTypeId) ) {
			vsId = Long.parseLong(visitTypeId);
		}
		
		
		// Validate visit type required
		if ( vsId <= 0 ) {
		    addActionError(getText(StrutsConstants.ERROR_REQUIRED_VISIT_TYPE));
		}
		
		// Validate the visit date.
		if ( !Utils.isBlank(visitDateStr) ) {
    		try {
                SimpleDateFormat df = new SimpleDateFormat(SysPropUtil.getProperty("default.system.datetimeformat"));
                vsDate = df.parse(visitDateStr);
                
                if ( pm.visitExists(patientId, vsDate, vsId, pvID) ) {
            		logger.error("The given visit date (" + visitDateStr + ") is already scheduled for patient " 
            				+ patientId + " and visit type " + visitTypeId + ".");
                	addActionError(getText(StrutsConstants.ERROR_EXISTS_VISIT_DATE));
            	}
            }
    		catch (Exception e) {
    			logger.error("Error while validating the visit date string: " + visitDateStr, e);
            	addActionError(getText(StrutsConstants.ERROR_FIELD_INVALID, new String[]{getText("patient.visitdate.display")}));
            }
    	}
    	else {
    		logger.error("The visit date is missing.");
    		addActionError(getText(StrutsConstants.ERROR_REQUIRED_VISIT_DATE));
    	}
		
		// The instance with this property to be false, like pdbp and fitbir will validate these two fields
		if (Boolean.valueOf(SysPropUtil.getProperty("display.protocol.clinicalPoint"))) {
			if (!Utils.isBlank(selectedClinicPntId) && !Utils.isNumeric(selectedClinicPntId)) {
				logger.error("The submitted visit type ID of " + visitTypeId + " is not valid.");
				addActionError(getText(StrutsConstants.ERROR_FIELD_INVALID,
						new String[] {getText("protocol.intervalClinicalPoint.id.display")}));
			} else {
				Integer clinicPntId = Integer.parseInt(selectedClinicPntId);
				if (clinicPntId <= 0) {
					logger.error("Missing to select an interval clinical point.");
					addActionError(getText(StrutsConstants.ERROR_REQUIRED_CLLINICAL_POINT,
							new String[] {getText("patient.vTClinicalPoint.display")}));
				}
			}
			if (!Utils.isBlank(visitComments) && visitComments.length() > VISIT_COMMENTS_MAX_LENGTH) {
				logger.error("Comments cannot have more than 1000 characters.");
				addActionError(getText(StrutsConstants.ERROR_MAX_LENGTH, new String[] {
						getText("patient.visitComments.display"), String.valueOf(VISIT_COMMENTS_MAX_LENGTH)}));
			}
		}
		
		// Validate the pre-population values, if needed.
		if ( vsId > 0 ) {
			@SuppressWarnings("unchecked")
			Map<String, DataElement> deCache = (Map<String, DataElement>) session.get(CtdbConstants.PRE_POP_DE_CACHE_KEY);
			
			for (PatientVisitPrepopValue pvPrepopValue : pvpvList) {
				PrepopDataElement prePopDe = pvPrepopValue.getPrepopDataElement();
				String fieldValue = pvPrepopValue.getPrepopvalue();
				String dataElementName = prePopDe.getShortName();
				String dataElementTitle = prePopDe.getTitle();
				
				try {
					DataElement de = deCache.get(dataElementName);
					
					// Validate prepop value field.
					if ( de != null && !Utils.isBlank(fieldValue) ) {
						// Validate the numeric values.
						if ( prePopDe.getValueType().equalsIgnoreCase("number") && Utils.isNumeric(fieldValue) ) {
							BigDecimal deMaxValue = de.getMaximumValue();
							BigDecimal deMinValue = de.getMinimumValue();
							BigDecimal prepopValue = new BigDecimal(fieldValue);
							if (deMaxValue != null && deMinValue != null 
									&& (prepopValue.compareTo(deMaxValue) == 1 || prepopValue.compareTo(deMinValue) == -1)) {
								String range = "from " + deMinValue + " to " + deMaxValue;
								addActionError(getText("errors.prepopvalue.exceededLimit", new String[]{ dataElementTitle, range }));
							}
						}
						else if(prePopDe.getValueType().equalsIgnoreCase("string")){
							if (de.getSize() != null && fieldValue.length() > de.getSize()){
								String restrict = "maximum "+ String.valueOf(de.getSize()) + " characters";
								addActionError(getText("errors.prepopvalue.exceededLimit", new String[]{ dataElementTitle, restrict }));
							}
						} else {
							addActionError(getText("errors.prepopvalue.notNumber", new String[]{ fieldValue,  dataElementTitle }));
						}
					}
					else if ( de == null ) {
						addActionError(getText("errors.dataelement.failToGet", dataElementName));
					}
					
					pvPrepopValue.setIntervalId(vsId);
				}
				catch ( NumberFormatException nfe )  {
					logger.error("Couldn't format field value " + fieldValue + " to a BigDecimal.", nfe);
					addActionError(getText("errors.prepopvalue.notNumber", new String[]{ fieldValue,  dataElementTitle }));
				}
			}
		}
		else if ( !pvpvList.isEmpty() ) {
			logger.error("Pre-population values given for a non-existent visit type (" + visitTypeId + ").");
			addActionError(getText("errors.prepopvalue.visittype.required"));
		}
    	
    	return !hasErrors();
    }
	
	/**
	 * Converts a list of PatientVisitPrepopValue objects to a JSON array string.
	 * 
	 * @param pvpvList - The list of PatientVisitPrepopValue objects to convert.
	 * @return A JSON array string version of the passed in PatientVisitPrepopValue list.
	 * @throws JSONException If there is a JSON parse or conversion error.
	 */
	private String pvPrepopValueListToJsonArrayString(List<PatientVisitPrepopValue> pvpvList) throws JSONException {
		JSONArray jsonArray = new JSONArray();

		for ( PatientVisitPrepopValue pvpv : pvpvList ) {
			JSONObject jsonObj = new JSONObject();
			PrepopDataElement prePopDE = pvpv.getPrepopDataElement();
			
			jsonObj.put("prePopValId", pvpv.getId());
			jsonObj.put("shortName", prePopDE.getShortName());
			jsonObj.put("title", prePopDE.getTitle());
			jsonObj.put("valueType", prePopDE.getValueType());
			jsonObj.put("isDateField", prePopDE.getValueType().equalsIgnoreCase("date"));
			jsonObj.put("prePopValue", pvpv.getPrepopvalue());
			
			jsonArray.put(jsonObj);
		}

		String jsonStr = jsonArray.toString();

		// Check if the JSON array string conversion was successful.
		if (jsonStr == null) {
			throw new JSONException("Could not generate the prepopulation JSON array.");
		}

		return jsonStr;
	}
	
	/**
	 * Converts a list of PrepopDataElement objects to a JSON array string.
	 * 
	 * @param prePopDeList - The PrepopDataElement list to be converted.
	 * @return A JSON array string version of the passed in PrepopDataElement list.
	 * @throws JSONException If there is a JSON parse or conversion error.
	 */
	private String prePopDeListToJsonArrayString(List<PrepopDataElement> prePopDeList) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		
		// Convert the list to a JSON array.
		for ( PrepopDataElement prePopDe : prePopDeList ) {
			JSONObject jsonObj = new JSONObject();
			
			jsonObj.put("shortName", prePopDe.getShortName());
			jsonObj.put("title", prePopDe.getTitle());
			jsonObj.put("valueType", prePopDe.getValueType());
			jsonObj.put("isDateField", prePopDe.getValueType().equalsIgnoreCase("date"));
			jsonObj.put("prePopValue", "");
			
			jsonArray.put(jsonObj);
		}
		
		String jsonStr = jsonArray.toString();

		// Check if the JSON array string conversion was successful.
		if (jsonStr == null) {
			throw new JSONException("Could not generate the pre-population data element JSON array.");
		}

		return jsonStr;
	}
	
	/**
	 * Converts the JSON string list to a list of PatientVisitPrepopValue for the current visit type
	 * 
	 * @param strList - A JSON string list of the PatientVisitPrepopValue to associate to the current visit type
	 * @return 	A listing of PatientVisitPrepopValue associated to the current interval
	 * @throws JSONException If an error occurred while parsing the JSON data
	 * @throws CtdbException If a database error occurs.
	 */
	private List<PatientVisitPrepopValue> jsonStrArrayToPrepopValueList(String strList) throws JSONException, CtdbException {
		List<PatientVisitPrepopValue> pvPrepopValues = new ArrayList<PatientVisitPrepopValue>();
		
		// First validate the JSON array string.
		if (!Utils.isBlank(strList)) {
			JSONArray inArray = new JSONArray(strList);
			ProtocolManager protMan = new ProtocolManager();
			
			for (int i = 0; i < inArray.length(); i++) {
				JSONObject inObj = inArray.getJSONObject(i);
				PatientVisitPrepopValue pvpv = new PatientVisitPrepopValue();
				
				pvpv.setPrepopDataElement(protMan.getPrepopDEByShortName(inObj.getString("shortName")));
				pvpv.setPrepopDataElementId(pvpv.getPrepopDataElement().getId());
				pvpv.setPrepopvalue(inObj.optString("prePopValue", ""));
				
				pvPrepopValues.add(pvpv);
			}
		}
		
		return pvPrepopValues;
	}
	
	private String[] jsonArrayToStringArray(JSONArray array) throws JSONException {
		if (array == null || array.length() == 0) {
			return null;
		}
		
		String[] strArray = new String[array.length()];
		
		for (int i = 0; i < array.length(); i++) {
			strArray[i] = array.getString(i);
		}
		
		return strArray;
	}
	
	public String getVTClinicalPntList() throws CtdbException, JSONException {
		this.visitTypeId = this.getVisitTypeId();
		Integer vTId = StringUtils.isBlank(visitTypeId) ? null : Integer.valueOf(visitTypeId);
		List<IntervalClinicalPoint> clinicPntList = new ArrayList<IntervalClinicalPoint>();
		ProtocolManager protoMan = new ProtocolManager();
		if(visitTypeId != null && vTId > 0){
			clinicPntList = protoMan.getIntervalClinicalPntsForInterval(vTId);
		} else {
			List<Interval> vTList = (List<Interval>) session.get(CtdbConstants.VISIT_TYPE_OPTIONS);
			ProtocolManager protMan = new ProtocolManager();
			for(Interval vt : vTList){
				clinicPntList.addAll(protMan.getIntervalClinicalPntsForInterval(vt.getId()));
			}
		}
		this.setIntClinicalPntList(clinicPntList);
		
		JSONArray clinialPntJsonArr = new JSONArray();
		
		for (IntervalClinicalPoint pnt : clinicPntList) {
			JSONObject clinicalJsonObj = new JSONObject();
			clinicalJsonObj.put("id", pnt.getId());
			String info = pnt.getClinicalLoc().getName() + " - " + pnt.getProcedure().getName() + " - " + pnt.getPointOfContact().getFullName();
			clinicalJsonObj.put("name", info);			
			clinialPntJsonArr.put(clinicalJsonObj);
		}
	
		jsonString = clinialPntJsonArr.toString();
		
		return BaseAction.SUCCESS;
	}
	
	public String getCorrespondingVT() throws CtdbException, JSONException {
		this.selectedClinicPntId = this.getSelectedClinicPntId();
		Integer clinicPntId = StringUtils.isBlank(selectedClinicPntId) ? null : Integer.valueOf(selectedClinicPntId);
		ProtocolManager protoMan = new ProtocolManager();
		Interval interval = new Interval();
		if(clinicPntId != null && clinicPntId > 0){
			interval = protoMan.getIntervalByClinicalPnt(clinicPntId);
		}
		try{
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("id", interval.getId());
			jsonObj.put("name", interval.getName());
	
			jsonString = jsonObj.toString();
		} catch(JSONException ex) {
			ex.printStackTrace();
		}
		
		return BaseAction.SUCCESS;
	}
	
	public List<PatientVisit> getPvList() {
		return pvList;
	}
	public void setPvList(List<PatientVisit> pvList) {
		this.pvList = pvList;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public int getPatientId() {
		return patientId;
	}
	public void setPatientId(int patientId) {
		this.patientId = patientId;
	}
	
	public String getProtocolId() {
		return protocolId;
	}
	public void setProtocolId(String protocolId) {
		this.protocolId = protocolId;
	}
	
	public String getVisitTypeId() {
		return visitTypeId;
	}
	public void setVisitTypeId(String visitTypeId) {
		this.visitTypeId = visitTypeId;
	}
	
	public String getVisitDateStr() {
		return visitDateStr;
	}

	public void setVisitDateStr(String visitDateStr) {
		this.visitDateStr = visitDateStr;
	}

	public String getVisitComments() {
		return this.visitComments;
	}

	public void setVisitComments(String comments) {
		this.visitComments = comments;
	}

	public List<IntervalClinicalPoint> getIntClinicalPntList() {
		return this.intClinicalPntList;
	}

	public void setIntClinicalPntList(List<IntervalClinicalPoint> intClinicalPnt){
		this.intClinicalPntList.clear();
		if(intClinicalPnt != null){
			this.intClinicalPntList.addAll(intClinicalPnt);
		}
	}

	public Date getVsDate() {
		return vsDate;
	}

	public void setVsDate(Date vsDate) {
		this.vsDate = vsDate;
	}

	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	public String getPvPrepopValues() {
		return pvPrepopValues;
	}
	
	public void setPvPrepopValues(String pvPrepopValues) {
		this.pvPrepopValues = pvPrepopValues;
	}

	public String getJsonString() {
		return jsonString;
	}

	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}

	public String getErrRespMsg() {
		return errRespMsg;
	}

	public void setErrRespMsg(String errRespMsg) {
		this.errRespMsg = errRespMsg;
	}

	public String getSelectedClinicPntId() {
		return this.selectedClinicPntId;
	}

	public void setSelectedClinicPntId(String clinicPntId){
		this.selectedClinicPntId = clinicPntId;
	}
	
	public String getPatientEmail() {
		return this.patientEmail;
	}

	public void setPatientEmail(String patientEmail){
		this.patientEmail = patientEmail;
	}
	
}