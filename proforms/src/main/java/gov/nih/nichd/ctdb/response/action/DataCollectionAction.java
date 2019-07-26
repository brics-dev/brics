package gov.nih.nichd.ctdb.response.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.NoRouteToHostException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.attachments.domain.Attachment;
import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CasProxyTicketException;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.ResourceNotAvailableException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.common.util.XslTransformer;
import gov.nih.nichd.ctdb.form.common.FormConstants;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.form.util.FormDataStructureUtility;
import gov.nih.nichd.ctdb.patient.common.PatientResultControl;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.domain.PatientVisit;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;
import gov.nih.nichd.ctdb.response.common.DuplicateDataEntriesException;
import gov.nih.nichd.ctdb.response.common.InputHandler;
import gov.nih.nichd.ctdb.response.common.ResponseConstants;
import gov.nih.nichd.ctdb.response.common.VisitDateMismatchException;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.DataEntryDraft;
import gov.nih.nichd.ctdb.response.domain.DataEntryHeader;
import gov.nih.nichd.ctdb.response.domain.EditAssignment;
import gov.nih.nichd.ctdb.response.domain.FormInterval;
import gov.nih.nichd.ctdb.response.domain.Response;
import gov.nih.nichd.ctdb.response.form.DataEntrySetupForm;
import gov.nih.nichd.ctdb.response.manager.DataSubmissionManager;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;
import gov.nih.nichd.ctdb.response.util.DataCollectionUtils;
import gov.nih.nichd.ctdb.response.util.XMLManipulator;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.manager.SecurityManager;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.nichd.ctdb.site.manager.SiteManager;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.ws.HashMethods;


/**
 * This class is used for Data Collection
 * 
 * It will generate the pre-form for data collection, fetch the form, and begin
 * the data lock process It will handle Patient and
 * Non-Patient....however...Sample Non-Patient is not handled her
 * 
 * @author pandyan
 * 
 */
public class DataCollectionAction extends BaseAction {

	private static final long serialVersionUID = 5390466769911136426L;
	
	public static final String ACTION_MESSAGES_KEY = "DataCollectionAction_ActionMessages";
	public static final String ACTION_ERRORS_KEY = "DataCollectionAction_ActionErrors";
	
	private static final Logger logger = Logger.getLogger(DataCollectionAction.class);

	private String action;
	private String mode;
	private boolean nextPreviousJumpToFormTracker;
	private DataEntrySetupForm dataEntryForm = new DataEntrySetupForm();
	
    // Special handling for file type questions 
    private List<File> fileUpload = new ArrayList<File>();
    private List<String> fileUploadFileName = new ArrayList<String>();
    private List<String> fileUploadContentType = new ArrayList<String>();
    
    private String hiddenSectionsQuestionsElementIdsJSON = "[]";
    
    // For mapping each uploaded file to the corresponding question
    private List<String> fileUploadKey = new ArrayList<String>();    
	
	public String execute() throws Exception{


		action = request.getParameter(StrutsConstants.ACTION);
		nextPreviousJumpToFormTracker = false;
		
		HttpServletResponse response = ServletActionContext.getResponse();
		PrintWriter out = response.getWriter();
		
		int protocolId = -1;
		Protocol protocol = null;
		
		

		if(!action.equals(StrutsConstants.FETCHFORMPSR ) &&  !action.equals(StrutsConstants.EDITFORMPSR) && !action.equals(StrutsConstants.SAVEFORMPSR) ) {
		
			buildLeftNav(LeftNavController.LEFTNAV_COLLECT_COLLECT);
			
			User user = getUser();
			String userFullName = user.getFirstName() + " " + user.getLastName();
			dataEntryForm.setUserFullName(userFullName);
			
			protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
	
			if (protocol == null) {
				if (action.equals(StrutsConstants.SAVE) || action.equals(StrutsConstants.SAVEFORM) || 
					action.equals(StrutsConstants.LOCKFORM)) {
					out.print("<li style=\"margin-left:20px\">"+"The active study has been lost. Please select one to continue."+"</li>");
					out.flush();
					logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"Current Protocol is null in AJAX");
					return null;
					} else {
						logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"Current Protocol is null.");
						return StrutsConstants.SELECTPROTOCOL;
					}
			} else {
				 protocolId = protocol.getId();
			}
		}
		
		// Current formId form the request scope
		int currentFormId = -1;
		if (request.getParameter(CtdbConstants.FORM_ID_REQUEST_ATTR) != null) {
			currentFormId = Integer.parseInt(request.getParameter(CtdbConstants.FORM_ID_REQUEST_ATTR));
		} else {
			currentFormId = dataEntryForm.getFormId();
		}
		
		
		//Managers inAction
		PatientManager pm = new PatientManager();
		ResponseManager rm = new ResponseManager();
		FormManager fm = new FormManager();
		ProtocolManager protoMan = new ProtocolManager();
		QuestionManager qm = new QuestionManager();
		request.setAttribute(FormConstants.RANGE_VALIDATION_JS_OBJS, "");
		request.setAttribute(CtdbConstants.DATACOLLECTION_EDITMODE, false);

		if (action.equals(StrutsConstants.FETCHFORM)) {
			mode = getMode();
		} else {
			mode = request.getParameter(CtdbConstants.DATACOLLECTION_MODE);
		}
		
		int intervalId = dataEntryForm.getIntervalId();
		int patientId = dataEntryForm.getPatientId();
		
		String forward = SUCCESS;

		//new action to lock and load next or previous form
		if (action.equals(StrutsConstants.LOCKANDLOADNEXTFORM) || action.equals(StrutsConstants.LOCKANDLOADPREVIOUSFORM)) {
				try{
				logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"DataCollectionAction->" + action);
				this.lockFormStep2(rm);		
				dataEntryForm.setShowSavedMessage(true);
				
				String formName = fm.getFormNameForEFormId(protocolId, currentFormId);
				request.setAttribute(StrutsConstants.FORMNAMETOBESAVEDORLOCKED, formName + " is Locked");
				
				List<AdministeredForm> collectedAdminForms = rm.getAdminFormForSubjectIntervalAndStudy(
						intervalId, patientId, protocolId);
				
				// Sorted active Form List by formOrderVal from Interval module
				List<FormInterval> fiList = rm.getActiveFormsListInInterval(intervalId);
				int returnFormId = -1;
				if (action.equals(StrutsConstants.LOCKANDLOADNEXTFORM)) {
					returnFormId = DataCollectionUtils.returnNextFormId( 
							fiList, currentFormId, collectedAdminForms, request);
				} else {
					returnFormId = DataCollectionUtils.returnPreviousFormId(
							fiList, currentFormId, collectedAdminForms, request);
				}
				
				action = StrutsConstants.FETCHFORM;
				if (!collectedAdminForms.isEmpty()) {
					for (AdministeredForm adminForm : collectedAdminForms) {
						if (adminForm.getForm().getId() == returnFormId) {
							nextPreviousJumpToFormTracker = true;
							action = StrutsConstants.EDITFORM;   // ???
							mode = CtdbConstants.DATACOLLECTION_FORMPATIENT;
							break;
						}
					}
				}
				
				dataEntryForm.setFormId(returnFormId);
				dataEntryForm.setClickedSectionFields("");
				}
				catch ( CtdbException | SQLException e ) {
					logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"A database error occurred in save method in lock and load or previous method.", e);
					addActionError("Something went wrong in data collection process please retry your collection again.");
					session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
					return StrutsConstants.DATA_COLLECTION;
				}
				catch ( MessagingException me ) {
					logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"Error occurred while creating the notification email in lock and load or previous method.", me);
					addActionError("Something went wrong in data collection process please retry your collection again.");
					session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
					return StrutsConstants.DATA_COLLECTION;
				}
				catch ( IOException ie )
				{
					logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"Something went wrong whiling writing to JSP.See stack track for details in lock and load or previous method. ",ie);	
					addActionError("Something went wrong in data collection process please retry your collection again.");
					session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
					return StrutsConstants.DATA_COLLECTION;
				}
				catch ( Exception e) {
					logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"An error occurred in slock and load or previous method.", e);
					addActionError("Something went wrong in data collection process please retry your collection again.");
					session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
					return StrutsConstants.DATA_COLLECTION;
					
				}
			}
		
		// New action to load next/previous Form on click of the Next/Previous
		// button or jumpTo certain form onClick on jumpto link
		if (action.equals(StrutsConstants.NEXTFORM) || action.equals(StrutsConstants.PREVIOUSFORM) || 
			action.equals(StrutsConstants.JUMPTOFORM)) {
			
			String af = this.nextPreviousJumpToNavigation(rm, pm, fm, protoMan);
			// If there is forward this means there are errors in the form 
			// so go there for correction else continue with normal flow
			if (af != null) {
				
				return af;
			}
		}

		// //////////////////////////////SETTING UP PATIENT LISTS AND INTERVALS ETC.
		if (action.equals(StrutsConstants.FORMPARAMS)) {

			if (mode.equals(CtdbConstants.DATACOLLECTION_FORMPATIENT)) {
				String shortName = fm.getEFormShortNameByEFormId(currentFormId);

				setPatientIntervalsList(currentFormId, shortName , pm, protocol, protoMan, fm, currentFormId);

				Date todDate = new Date();
				SimpleDateFormat sf = new SimpleDateFormat(SysPropUtil.getProperty("default.system.datetimeformat"));
				String todDateS = sf.format(todDate);
				dataEntryForm.setVisitDate(todDateS);
				dataEntryForm.setScheduledVisitDate("");
			} else if (mode.equals(CtdbConstants.DATACOLLECTION_PATIENTMODE)) {

				session.put("searchFormType", "patientPVDiv");
				
				int subjectId = -1;
				Patient p = null;
				
				String pvIdStr = request.getParameter("patientVisitId");
				
				try {
					if (pvIdStr != null) {
						PatientVisit pv = pm.getPatientVisit(Integer.parseInt(pvIdStr));
						subjectId = pv.getPatientId();
						p = this.getPatientForCollection(rm, protocolId, subjectId);
						Date pvDate = pv.getVisitDate();
						SimpleDateFormat sf = new SimpleDateFormat(SysPropUtil.getProperty("default.system.datetimeformat"));
						String vDate = sf.format(pvDate);
						dataEntryForm.setVisitDate(vDate != null ? vDate : "");
						dataEntryForm.setScheduledVisitDate(vDate != null ? vDate : ""); //set the scheduled visit date
						
						request.setAttribute(StrutsConstants.SUBJECT_INTERVAL_NAME, 
								pv.getIntervalName() != null ? pv.getIntervalName() : "");
						this.populateActiveForms(protoMan, pv.getIntervalId());

					} else {
						subjectId = dataEntryForm.getPatientId();
						p = this.getPatientForCollection(rm, protocolId, patientId);
					}
					
					dataEntryForm.setPatientId(subjectId);
					this.setPatientDispalyValue(dataEntryForm, p,protocol);
					
					// get all intervals for protocol
					List<Interval> intervals = protoMan.getIntervals(protocolId);
					this.populateIntervalOptions(intervals);
				}
				catch ( CtdbException ce ) {
					logger.error("Database error occured while starting a collection.", ce);
					addActionError(getText(StrutsConstants.ERROR_DATABASE_GET, new String[] {"a patient visit"}));
					
					return StrutsConstants.FAILURE;
				}
				catch ( Exception e ) {
					logger.error("Error occured while starting a collection.", e);
					
					return StrutsConstants.FAILURE;
				}
			}
			
		}
		
		// ///////////////////////////////////////////////END SETTING UP PATIENT
		// LIST AND INTERVALS
		// //////////////////////////////////////////////////////////////////////////////////////////////

		if (action.equals(StrutsConstants.FORMPARAMS)) {
			session.remove(StrutsConstants.DELETEONCANCELFLAG);
			request.setAttribute(FormConstants.FORMDETAIL, null);
			request.setAttribute(StrutsConstants.FORMFETCHED, false);
			//added by Ching-Heng
			request.setAttribute(CtdbConstants.IS_CAT, false);
			return SUCCESS;
			
		} else if (action.equals(StrutsConstants.EDITFORM)) {
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"DataCollectinAction->editForm");
			if (!nextPreviousJumpToFormTracker) {
				request.setAttribute(CtdbConstants.DATACOLLECTION_EDITMODE, true);
			}
			
			if (mode.equals(CtdbConstants.DATACOLLECTION_FORMPATIENT)) {
				try {
					return this.editSubjectForm(rm, fm, qm, protoMan, pm);
				} catch (RuntimeException e) {
					logger.error("Error in retrieving form", e);
					addActionError(getText(StrutsConstants.ERROR_RESPONSE_LOADINGFORM));
					session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
					return StrutsConstants.MY_COLLECTIONS;
				} catch (JAXBException e) {
					logger.error("Error in retrieving form", e);
					addActionError(getText(StrutsConstants.ERROR_RESPONSE_LOADINGFORM));
					session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
					return StrutsConstants.MY_COLLECTIONS;
				} catch (Exception e) {
					logger.error("Error in retrieving form", e);
					addActionError(getText(StrutsConstants.ERROR_RESPONSE_LOADINGFORM));
					session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
					return StrutsConstants.MY_COLLECTIONS;
				}
			}
			
		} else if (action.equals(StrutsConstants.FETCHFORM)) {
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"Removing editUser from session->fetchForm");
			session.remove(StrutsConstants.EDITUSER);
			try {

				this.fetchSubjectForm(protoMan, fm, rm, pm);

			} catch (VisitDateMismatchException vdme) {
				logger.error("Visit date mismatch.", vdme);
				addActionError(getText(StrutsConstants.ERROR_VISITDATE_MISMATCH, 
						new String[]{vdme.getThedate()}));
				request.setAttribute("_des_visitDateMismatch", true);
			} catch (ResourceNotAvailableException rnae) {
				logger.error("Resource is not available.", rnae);
				addActionError(getText(StrutsConstants.ERROR_RESOURCE_NOT_AVAILABLE));
			} catch (DuplicateDataEntriesException ddee) {
				logger.error("Duplicate data entry found.", ddee);
				addActionError(getText(StrutsConstants.ERROR_RESPONSE_ALREADYSTARTED));
			} catch (DuplicateObjectException doe) {
				logger.error("Duplicate object found.", doe);
				addActionError(getText(StrutsConstants.ERROR_RESPONSE_ALREADYSTARTED));
			} catch (CtdbException ce) {
				logger.error("Database error ocurred.", ce);
				addActionError(getText(StrutsConstants.ERROR_RESPONSE_ALREADYSTARTED));
			} catch (ParseException pe) {
				logger.error("Parsing error ocurred.", pe);
				addActionError("Correct Date must be entered");
			} catch (CasProxyTicketException cste) {
				logger.error("A proxy ticket could not be created for a request to the Dictionary web service.", cste);
				addActionError(getText(StrutsConstants.ERROR_RESPONSE_LOADINGFORM));
			} catch (WebApplicationException wae) {
				logger.error("The request for an eForm from the Dictionary web service has failed.", wae);
				int status = wae.getResponse().getStatus();
				
				if ((status == 403) || (status == 401)) {
					addActionError(getText(StrutsConstants.ERROR_RESPONSE_LOADINGFORM_PERMISSION));
				} else {
					addActionError(getText(StrutsConstants.ERROR_RESPONSE_LOADINGFORM));
				}
			} catch (RuntimeException e) {
				logger.error("Error in retrieving form", e);
				addActionError(getText(StrutsConstants.ERROR_RESPONSE_LOADINGFORM));
			} catch (Exception e) {
				logger.error("Error in retrieving form", e);
				addActionError(getText(StrutsConstants.ERROR_RESPONSE_LOADINGFORM));
			}
			
			if (this.hasActionErrors()) {
				request.setAttribute(StrutsConstants.FORMFETCHED, false);
				//added by Ching-Heng
				request.setAttribute(CtdbConstants.IS_CAT, false);
				this.populateActiveForms(protoMan, intervalId);
				return StrutsConstants.STAY_IN_SAME_PAGE;
			}
			else {
				return SUCCESS;
			}
		}
		else if(action.equals(StrutsConstants.FETCHFORMPSR)) {
			//patient self reporting - fetch form
			if (request.getSession() == null || (boolean)request.getSession().getAttribute(CtdbConstants.USER_SESSION_KEY) != true) {
				return "sessionExpired";
			}
			
			SecurityManager sm = new SecurityManager();
			User patientUser = sm.getPatientUser();
			LeftNavController controller = new LeftNavController(request, patientUser);
			
			controller.clear();
			try {
			return this.fetchSubjectFormPSR(fm, rm, pm);
			}catch(IllegalStateException i) {
				addActionError("You don't have permission to do this. If you have please open new brower in private mode and paste link to start PSR.");
				
			}catch( Exception e){
				addActionError("You don't have permission to do this. If you have please open new browser in private mode and paste link to start PSR.");
			}
					
		}
		else if(action.equals(StrutsConstants.EDITFORMPSR)) {
			//patient self reporting - fetch form
			if (request.getSession() == null) {
				return "sessionExpired";
			}
			else {
				Boolean userKey = (Boolean) request.getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
				
				if (userKey == null || userKey != true) {
					return "sessionExpired";
				}
			}
			
			SecurityManager sm = new SecurityManager();
			User patientUser = sm.getPatientUser();
			LeftNavController controller = new LeftNavController(request, patientUser);
			controller.clear();
			return this.editSubjectFormPSR(rm, fm,  qm);
		}
		else if (action.equals(StrutsConstants.SAVEFORM)) {
			try {
				return this.saveSubjectForm(rm, protoMan, pm, fm, false,false);
			} catch (IOException io){
				logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"Something went wrong while writing to JSP.See stack track for details in save method. ", io);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return null;
			}
		} 
		else if (action.equals(StrutsConstants.SAVE)) {
			try {
				return this.saveSubjectForm(rm, protoMan, pm, fm, false,true);
			}
			catch (IOException io) {
				logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"Something went wrong while writing to JSP.See stack track for details in save method. ", io);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return null;
			}
			
		}
		else if (action.equals(StrutsConstants.SAVEFORMPSR)) {
			try{
				if (request.getSession() == null || (boolean)request.getSession().getAttribute(CtdbConstants.USER_SESSION_KEY) != true) {
					return "sessionExpired";
				}
				
				return this.saveSubjectFormPSR(rm, protoMan, pm, fm, false);
			}catch (IOException io){
				logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"Something went wrong while writing to JSP.See stack track for details in save method. ", io);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return null;
			}
		} 
		else if (action.equals(StrutsConstants.SAVEFORMFINALLOCKED)) {
			// Save Subject Form that are final locked after edits
			return this.saveEditsToFinalLockedSujectForm(rm, protoMan, pm, fm);
		}
		else if (action.equals(StrutsConstants.LOCKFORM)) {  
			try {
				// Lock Subject Form
				return this.lockFormStep1(rm, fm, protoMan, pm);
			} catch(IOException io){
				logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +
						"Something went wrong while writing to JSP.See stack track for details in lockstep1.", io);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return null;
			}
		}
		else if (action.equals(StrutsConstants.LOCKANDEXIT)){
			this.lockFormStep2(rm);
			return StrutsConstants.MY_COLLECTIONS;
		}
		else if (action.equals(StrutsConstants.LOCKANDEXITCAT)){
			//added by Ching-Heng
			return this.lockFormPSR(rm, fm, protoMan, pm);
		}else if(action.equals(StrutsConstants.VALIDATEMAINGROUP)) {
			//added by Ching-Heng
			return validateMainGroup(rm, fm, protoMan, pm);
		}
		else if (action.equals("digitalSignature")) {
			this.ajaxDigitalSignature();

			return null;
		} 
		else if (action.equals("getQuestionGraphic")) {
			
			this.getQuestionGraphic();
			return null;
		}
		else if (action.equals(StrutsConstants.PROCESS_POPULATE_FORMNAMES_AJAX)) {
			String iIdString = request.getParameter("optionIntervalId");
			int patId = Integer.parseInt(request.getParameter("patientId"));
			List<Form> forms = null;
			List<Integer> eformIds = new ArrayList<Integer>();
			
			if (iIdString.equals("pleaseSelect") || iIdString.equals("-1")) {
				forms = fm.getActiveAndInProgressForms(protocolId);
			} else {
				forms = protoMan.getActiveFormsForInterval(Integer.parseInt(iIdString));
				eformIds = rm.getEformIdsOfCollections(Integer.parseInt(iIdString),patId);
			}
			
			JSONArray jsonReturnArray = new JSONArray();
			JSONObject jsonObj = new JSONObject();
			
			if (forms != null && !forms.isEmpty()) {
				jsonObj.put("id", "pleaseSelect");
				jsonObj.put("name", "- Please Select");
				jsonObj.put("disabled", false);
				jsonReturnArray.put(jsonObj);
				
				for (Form f : forms) {
					jsonObj = new JSONObject();
					jsonObj.put("id", String.valueOf(f.getId()));
					jsonObj.put("name", f.getName());

					if ( eformIds.contains(Integer.valueOf(f.getId())) ) {
						jsonObj.put("disabled", true);
					} else {
						jsonObj.put("disabled", false);
					}
					jsonReturnArray.put(jsonObj);
				}
			} else {
				jsonObj.put("id", "noForms");
				jsonObj.put("name", "There are no forms this visit type is associated with yet");
				jsonObj.put("disabled", false);
				jsonReturnArray.put(jsonObj);
			}

			out.print(jsonReturnArray);
			out.flush();
			return null;

		} else {
			request.setAttribute(CtdbConstants.DATACOLLECTION_MODE, mode);
		}

		return forward;
	}
	
	private void setupPage(Patient patient, List<AdministeredForm> collectedAdminForms,
			AdministeredForm aform) throws CtdbException {
		
		ProtocolManager pm = new ProtocolManager();
		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		this.setPatientDispalyValue(dataEntryForm, patient, protocol);

		
		// Set interval name
		int intervalId = dataEntryForm.getIntervalId();
		String intervalName = "";
		
		if (intervalId == -1) {
			intervalName = StrutsConstants.OTHER;
		} else {
			intervalName = pm.getInterval(intervalId).getName();
		}
		dataEntryForm.setIntervalName(intervalName);

		// Populate form intervals
		List<Form> forms = pm.getActiveFormsForInterval(intervalId);
		List<FormInterval> formIntervals = DataCollectionUtils.returnFormsInInterval(
				collectedAdminForms, forms);
		request.setAttribute(StrutsConstants.FORMSININTERVAL, formIntervals);
		
		// Populate form fields from aform
		dataEntryForm.setFormName(aform.getForm().getName());
		//dataEntryForm.setVisitDate(aform.getVisitDateStringyyyyMMddHHmm());
		
		request.setAttribute(FormConstants.FORMDETAIL, getFormHtml(aform, true, false, null));
	}
	
	/**
	 * Gets the imported form from a file.
	 * 
	 * @param form The form to render
	 * @param pvPSR this PatientVisit is for PSR part only since in PSR administratedForm is getting the current datatime instead of the one stored in database
	 * 				which causes a corresponding patient visit can not be found in database.
	 * @return The form html
	 * @throws Exception Thrown if any errors occur while processing
	 */
	private String getFormHtml(AdministeredForm aform, boolean firstTime, boolean onMobile, PatientVisit pvPSR) throws CtdbException{
		logger.info("getFormHtml->populating form XSD with content");
		String xsl;
		if (onMobile) {
			if (firstTime) {
				xsl = SysPropUtil.getProperty("form.xsl.pda.display");
			} else {
				xsl = SysPropUtil.getProperty("form.xsl.pda.answerdisplay");
			}
		} else {
			String mType = aform.getForm().getMeasurementType();
			if (aform.getForm().isTabDisplay()) {
				xsl = SysPropUtil.getProperty("form.tab.xsl.display");
			} else {
				//Ching-Heng
				if(aform.isCAT() && !mType.equals(StrutsConstants.SHORT_FORM)) {
					xsl = SysPropUtil.getProperty("form.xsl.displayPROMIS");
				}else {
					xsl = SysPropUtil.getProperty("form.xsl.display");			
				}
			}
			
			if (!firstTime || aform.getResponses().size() > 0) {
				if (aform.getForm().isTabDisplay()) {
					xsl = SysPropUtil.getProperty("form.tab.xsl.tabAnswerDisplay");
				} else {
					if(aform.isCAT() && !mType.equals(StrutsConstants.SHORT_FORM)) {
						xsl = SysPropUtil.getProperty("form.xsl.answerdisplayPROMIS");
					}else {
						xsl = SysPropUtil.getProperty("form.xsl.answerdisplay");
					}
				}
			}
		}
		
		InputStream stream = request.getServletContext().getResourceAsStream(xsl);
		Document formDom = XMLManipulator.populateFormWithAnswers(aform, true, pvPSR);
		
		return XslTransformer.transform(formDom, stream, CtdbConstants.GLOBAL_XSL_PARAMETER_MAP);
	}

	
	/**
	 * Method to give the right data collection status modified form base CTDB
	 * version to address new status as: In Progress,Completed,Locked and Final
	 * Locked(Only for double data entry)
	 * 
	 * @param aform
	 * @param user
	 * @return
	 * @throws CtdbException 
	 */
	private String getAformFormStatus(AdministeredForm aform, int userId) throws CtdbException {
		String status = "";
		
		if (aform.getFinalLockDate() != null) {
			return CtdbConstants.DATACOLLECTION_STATUS_FINALLOCKED;
		}

		ResponseManager rm = new ResponseManager();
		//int dataEntryNumber = rm.getDataEntryFlag(aform.getId(), userId);
		
		//if (dataEntryNumber == 1) {
			status = aform.getEntryOneStatus();
		//} else if (dataEntryNumber == 2) {
			//status = aform.getEntryTwoStatus();
		//}
		
		return status;
	}
	
	/**
	 * Method to get JS responses
	 * @param responses
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private StringBuffer getJsResponses(List<Response> responses) throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer();
		sb.append(" var jsResponses = new HashMap();\n");

		for (Response r : responses) {
			String response1 = "";
			String response2 = "";
			List<String> answers1 = r.getAnswers();
			
			if (r.getQuestion().getType() == QuestionType.File) {
				if (answers1 != null && answers1.size() > 0) {
					String answer1 = answers1.get(0);
					if (answer1 != null && !answer1.equals("")) {
						if (answer1.contains(":")) {
							String fileName = answer1.substring(0, answer1.indexOf(":"));
							response1 = fileName;
						} else {
							response1 = answer1;
						}
					}
				}
			} else {
				if (r.getQuestion().isDisplayPV()) {
					response1 =  cleanUp(r.getSubmitAnswers());
				} else {
					response1 =  cleanUp(r.getAnswers());
				}
				 
			}

			sb.append("  var res = new JsResponse();\n"); 
			sb.append("    res.setAnswer1 (URLDecode(\""+ response1 + "\"));\n");
			sb.append("    res.setAnswer2 (URLDecode(\""+ response2 + "\"));\n");
			sb.append("    res.setFinalAnswer (URLDecode(\""+ cleanUp(r.getAnswers()) + "\"));\n");
			sb.append("    res.setQuestionId (" + r.getQuestion().getId()+ "); \n");
			sb.append("    res.setResponseId (" + r.getId() + ");\n");
			
			String text = URLEncoder.encode(r.getQuestion().getText(), "UTF-8");
			text = formatJsLineLenghts(text);
			sb.append("    res.setQText(URLDecode(\"" + text + "\")); \n\n");
			
			if (r.getEditReason() != null) {
				sb.append("res.setChangeReason(\"" + r.getEditReason() + "\");");
			}
			sb.append("  jsResponses.put (" + "\"S_" + r.getQuestion().getSectionId() + "_Q_" + 
					r.getQuestion().getId() + "\"" + ", res);\n\n");
		}
		return sb;
	}
	
	private JSONArray getJSONArrayResponses(List<Response> responses) throws UnsupportedEncodingException, JSONException{
		JSONArray jsonReturnArray = new JSONArray();
		
		for (Response r : responses) {
			String id = "S_" +  r.getQuestion().getSectionId() + "_Q_" + r.getQuestion().getId();
			String response1 = "";
			String response2 = "";
			List<String> answers1 = r.getAnswers();
			boolean isFile = false;
			String attachment1Id = "";
			String attachment2Id = "";
			
			if (r.getQuestion().getType() == QuestionType.File) {
				isFile =  true;
				
				if (answers1 != null && answers1.size() > 0) {
					String answer1 = answers1.get(0);
					
					if (answer1 != null && !answer1.equals("")) {
						String fileName = answer1.substring(0, answer1.indexOf(":"));
						response1 = fileName;
						attachment1Id = answer1.substring(answer1.indexOf(":") + 1, answer1.length());
					}
				}
			} else {
				response1 =  cleanUp(r.getAnswers());
			}
			
			JSONObject jsonObj = new JSONObject();
			
			jsonObj.put("id", id);
			jsonObj.put("response1", response1);
			jsonObj.put("response2", response2);
			jsonObj.put("attachment1Id", attachment1Id);
			jsonObj.put("attachment2Id", attachment2Id);
			jsonObj.put("isFile", isFile);
			
			jsonReturnArray.put(jsonObj);
		}
		
		return jsonReturnArray;
	}


	/**
	 * Method to cleanup answers
	 * @param answers
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String cleanUp(List<String> answers) throws UnsupportedEncodingException {
		String s = "";
		for (String answer : answers) {
			if (!s.isEmpty()) {
				s += " - ";
			}
			s += URLEncoder.encode(formatJsLineLenghts(answer), "UTF-8"); // for Chinese by Sunny
		}
		return s;
	}

	
	/**
	 * Method to formatJsLine lengths
	 * @param text
	 * @return
	 */
	private String formatJsLineLenghts(String text) {
		// line is too long for JS, get separate lines
		if (text.length() < 81) {
			return text;
		}
		
		StringBuffer sb = new StringBuffer();
		for (int j = 0; j < (text.length() / 80) + 1; j++) {
			if (sb.length() > 0) {
				sb.append("\" \n + \"");
			}
			sb.append(text.substring(j * 80, Math.min((j+1)*80, text.length())));
		}
		return sb.toString();
	}




	/**
	 * Method to set patient intervalList
	 * @param form
	 * @param protocolId
	 * @param pm
	 * @param protocol
	 * @param protoMan
	 * @param fm
	 * @param currentFormId
	 * @throws CtdbException
	 */
	private void setPatientIntervalsList(int eformid, String shortname , PatientManager pm, Protocol protocol, 
			ProtocolManager protoMan, FormManager fm, int currentFormId) throws CtdbException {
		int protocolId = protocol.getId();
		
		PatientResultControl prc = new PatientResultControl();
		prc.setProtocolId(protocolId);
		prc.setInProtocol(true);
		prc.setActiveInProtocol("");
		prc.setSortBy(PatientResultControl.SORT_BY_ORDERVAL);
		prc.setSortOrder(PatientResultControl.SORT_ASC);
		prc.setFormId(eformid);

		List<Patient> patients = pm.getMinimalPatients(prc);
		pm.updatePatientSubjectNumbers(patients, protocol);
		request.setAttribute("patientList", patients);

		// get list of intervals
		List<Interval> intervals = protoMan.getIntervalsForForm(protocolId, eformid);
		this.populateIntervalOptions(intervals);

		dataEntryForm.setFormName(shortname);
		//10 is Subject form
		dataEntryForm.setFormTypeString(CtdbConstants.FORM_TYPE_SUBJECT_STRING);
		request.setAttribute("formType", String.valueOf(CtdbConstants.FORM_TYPE_SUBJECT));
		request.setAttribute(CtdbConstants.FORM_ID_REQUEST_ATTR, currentFormId);
	}

	
	/**
	 * This is the method executed while saving the subject form
	 */
	private String saveSubjectForm(ResponseManager rm, ProtocolManager protoMan, PatientManager pm, FormManager fm, 
			boolean comingFromNextPreviousJump, boolean isSaveAndStay) throws IOException, JSONException {
		HttpServletResponse response = ServletActionContext.getResponse();
		PrintWriter out = response.getWriter();

		try {
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +
					"#DataCollectionAction->saveSubjectForm#---------------------------------------");

			// remove comma at the last of comma separated array arguments
			String assocFileQuestionIds = request.getParameter(CtdbConstants.ASSOCFILEQUESTIONIDS);
			if (!Utils.isBlank(assocFileQuestionIds)) {
				assocFileQuestionIds = assocFileQuestionIds.substring(0, assocFileQuestionIds.length()-1);
			}

			String[] assocFileQuestionIdsArray = assocFileQuestionIds.split(",");
			List<String> assocFileQuestionList = Arrays.asList(assocFileQuestionIdsArray);
			Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			String currentFormId = request.getParameter(CtdbConstants.FORM_ID_REQUEST_ATTR);

			request.setAttribute(CtdbConstants.DATACOLLECTION_MODE, request.getParameter(CtdbConstants.DATACOLLECTION_MODE));
			boolean eMode = Boolean.valueOf(request.getParameter(CtdbConstants.DATACOLLECTION_EDITMODE));
			request.setAttribute(CtdbConstants.DATACOLLECTION_EDITMODE, eMode);

			AdministeredForm aform = (AdministeredForm) session.get(ResponseConstants.AFORM_SESSION_KEY);
			int aformId = aform.getId();
			//User user = this.getEditUser(rm, aformId);
			int entryOneUserId = aform.getUserOneEntryId();
			User user = getUser(rm,entryOneUserId);
			
			User loggedInUser = getUser();

			//logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +
			//"#DataCollectionAction->saveSubjectForm#->Getting aform object for aformid:\t" + aformId + " and user:\t" + user.getId());

			String currentStatus = getAformFormStatus(aform, user.getId());

			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +
					"#DataCollectionAction->saveSubjectForm#->PatientId:\t" + aform.getPatient().getId());
			if (aform.getInterval() == null) {
				logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->saveSubjectForm#->Visit Type:\tOther");
			} else {
				logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->saveSubjectForm#->Visit Type:\t"+aform.getInterval().getId());
			}
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->saveSubjectForm#->Form:\t"+aform.getForm().getId());
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->saveSubjectForm#->UserId:\t"+loggedInUser.getId());

			aform.setAssocFileQuestionIds(assocFileQuestionList);
			aform.setComingFromAutoSaver(Boolean.valueOf(request.getParameter(
					StrutsConstants.COMINGFROMAUTOSAVER)));

			if (this.getFileUpload() != null && !this.getFileUpload().isEmpty() &&
					this.getFileUploadFileName() != null && !this.getFileUploadFileName().isEmpty()) {
				aform.setFiles(this.buildFileMap());
			} 

			boolean markCompleted = Boolean.valueOf(request.getParameter("markAsCompletedCheckBoxStatus"));
			aform.setMarkAsCompleted(markCompleted);

			boolean validate = markCompleted;
			List<String> inputErrors;

			if (currentStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_COMPLETED)) {
				inputErrors = InputHandler.getEditedAnswers(aform,request,dataEntryForm);
			} else {
				inputErrors = InputHandler.getResponses(aform, request, validate,false,dataEntryForm); // validation
			}

			request.setAttribute("jsResponseList", getJsResponses(aform.getResponses()));

			boolean fileExist = Boolean.valueOf(request.getParameter(CtdbConstants.FILE_EXIST));
			if (!fileExist) {
				if (!aform.isAreThereAnswers() && markCompleted) {
					//out.print(StrutsConstants.ERROR_ANSWER_REQUIRED_IN_SAVE);
					JSONObject jsonObj = new JSONObject();
					jsonObj.put("status", StrutsConstants.ERROR_ANSWER_REQUIRED_IN_SAVE);
					out.print(jsonObj);
					out.flush();
					return null;
				}
			}

			if (inputErrors.isEmpty() && !this.hasErrors()) { // NO ERRORS SAVE NOW
				//ok now check to see if aform has been created
				if(aform.getId() == Integer.MIN_VALUE) {
					rm.create(aform, user);
				}
				
				if (currentStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_COMPLETED)) {
					rm.saveCompletedEditAnswers(aform,loggedInUser);
				}
				else {
					rm.saveProgress2(aform, loggedInUser);
				}

				rm.saveVisibleRepeatableSection(aform, dataEntryForm, user);

				rm.updateAdministeredFormVisitDate(aform);
				
				if (action.equals(StrutsConstants.SAVEFORM)) {
					session.remove("currentVisitDate");
				}

				// prepare form
				aform.setLoggedInUserId(loggedInUser.getId());

				if (comingFromNextPreviousJump) {
					logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +
							"#DataCollectionAction->saveSubjectForm->#Success-comingFromNextPreviousJump#->aformId:\t" + aform.getId());
				}
				else {
					JSONArray returnArray = getJSONArrayResponses(aform.getResponses()); 
					JSONObject jsonObj = new JSONObject();

					jsonObj.put("status", "saveFormSuccess");
					jsonObj.put("returnArray", returnArray);

					out.print(jsonObj);
					out.flush();
					logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +
							"#DataCollectionAction->saveSubjectForm->#Success#->aformId:\t" + aform.getId());
				}

				//Don't remove from session if they do save and stay...only remove if they do normal save and exit or from next prev
				if(!isSaveAndStay) {
					session.remove(ResponseConstants.AFORM_SESSION_KEY);
				}else {
					session.put(ResponseConstants.AFORM_SESSION_KEY, aform);
				}
				return null;
			}
			else { // THERE ARE ERRORS	
				request.setAttribute(StrutsConstants.MARKASCOMPLETESTATUSINACTION, markCompleted);
				aform.setLoggedInUserId(user.getId());

				StringBuffer sb = new StringBuffer();

				for (String errorMsg : inputErrors) {
					addActionError(errorMsg);
					String s = "<li style=\"margin-left:20px\">" + errorMsg + "</li>";
					sb.append(s);	
				}

				JSONObject jsonObj = new JSONObject();
				jsonObj.put("status",sb.toString());
				JSONArray returnArray = getJSONArrayResponses(aform.getResponses()); 
				jsonObj.put("returnArray", returnArray);
				out.print(jsonObj);

				// need to set list of form intervals to display
				int intervalId = dataEntryForm.getIntervalId();
				int patientId = dataEntryForm.getPatientId();

				//Patient patient = pm.getPatient(patientId);
				List<AdministeredForm> collectedAdminForms = rm.getAdminFormForSubjectIntervalAndStudy(
						intervalId, patientId, protocol.getId());

				request.setAttribute(CtdbConstants.AFORM_ID_REQUEST_ATTR, String.valueOf(aform.getId()));
				request.setAttribute(StrutsConstants.FORMFETCHED, true);
				request.setAttribute(CtdbConstants.FORM_ID_REQUEST_ATTR, currentFormId);
				request.setAttribute(StrutsConstants.FORMSTATUS, getAformFormStatus(aform, user.getId()));
				
				// To enable disable next previous button on validation errors while saving the forms
				//since we are now allowing naviagation during editing, we need to remove the current
				//aform from collectedAdminForms so that the function setButtonDisable will disable correctly the
				//next and prev buttons
				if(aform.getId() != Integer.MIN_VALUE) {
					Iterator<AdministeredForm> iterAF = collectedAdminForms.iterator();
					while(iterAF.hasNext()) {
						AdministeredForm aff = iterAF.next();
						if(aff.getId() == aform.getId()) {
							iterAF.remove();
							break;
						}
					}
				}
				this.setButtonDisable(rm, intervalId, collectedAdminForms);

				if (eMode) {
					dataEntryForm.setEditHideNextPrevious(true);
				}

				logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +
						"#DataCollectionAction->saveSubjectForm-->errors in saving so returning to correct errors->aformId-->:\t" + aform.getId());
			}
		}
		catch( DuplicateObjectException doe ) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) + "An error occurred in lock step 1.", doe);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("status", "duplicateData");
		}
		catch ( CtdbException | SQLException e ) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) + "A database error occurred in save method.", e);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("status", "dataCollectionException");
			out.print(jsonObj);
		}
		catch ( MessagingException me ) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) + 
					"Error occurred while creating the notification email in save method.", me);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("status", "dataCollectionException");
			out.print(jsonObj);
		}
		catch ( IOException ie ) {	logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"A I/O error occurred in save method.", ie);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("status", "dataCollectionException");
			out.print(jsonObj);
		}
		catch (Exception e) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"An error occurred in save method.", e);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("status", "dataCollectionException");
			out.print(jsonObj);
		}

		out.flush();

		return null;	
	}
	
	/**
	 * This is the method executed while saving the subject form
	 */
	private String saveSubjectFormPSR(ResponseManager rm, ProtocolManager protoMan, 
			PatientManager pm, FormManager fm, boolean comingFromNextPreviousJump) throws IOException, JSONException {
		HttpServletResponse response = ServletActionContext.getResponse();
		PrintWriter out = response.getWriter();
		String sessionId = request.getSession().getId();
		
		try {
			logger.info("Subject:"+sessionId+"#DataCollectionAction->saveSubjectFormPSR#---------------------------------------");
					
			// remove comma at the last of comma separated array arguments
			String assocFileQuestionIds = request.getParameter(CtdbConstants.ASSOCFILEQUESTIONIDS);
			if (!Utils.isBlank(assocFileQuestionIds)) {
				assocFileQuestionIds = assocFileQuestionIds.substring(0, assocFileQuestionIds.length()-1);
			}
			
			String[] assocFileQuestionIdsArray = assocFileQuestionIds.split(",");
			List<String> assocFileQuestionList = Arrays.asList(assocFileQuestionIdsArray);
	
			request.setAttribute(CtdbConstants.DATACOLLECTION_MODE, request.getParameter(CtdbConstants.DATACOLLECTION_MODE));
			boolean eMode = Boolean.valueOf(request.getParameter(CtdbConstants.DATACOLLECTION_EDITMODE));
			request.setAttribute(CtdbConstants.DATACOLLECTION_EDITMODE, eMode);
			
			AdministeredForm aform = (AdministeredForm) session.get(ResponseConstants.AFORM_SESSION_KEY);
			int aformId = aform.getId();
			//SecurityManager sm = new SecurityManager();
			//User patientUser = sm.getPatientUser();
			
			int entryOneUserId = aform.getUserOneEntryId();
			User patientUser = getUser(rm,entryOneUserId);
			
			
	
			//logger.info("Subject:"+sessionId+"#DataCollectionAction->saveSubjectForm#->Getting aform object for aformid:\t"+aformId + " and user:\t" + patientUser.getId());
			logger.info("Subject:"+sessionId+"#DataCollectionAction->saveSubjectFormPSR#->PatientId:\t"+aform.getPatient().getId());
			
			if (aform.getInterval() == null) {
				logger.info("Subject:"+sessionId+"#DataCollectionAction->saveSubjectFormPSR#->Visit Type:\tOther");
			} else {
				logger.info("Subject:"+sessionId+"#DataCollectionAction->saveSubjectFormPSR#->Visit Type:\t"+aform.getInterval().getId());
			}
			
			logger.info("Subject:"+sessionId+"#DataCollectionAction->saveSubjectFormPSR#->Form:\t"+aform.getForm().getId());
			logger.info("Subject:"+sessionId+"#DataCollectionAction->saveSubjectFormPSR#->UserId:\t"+patientUser.getId());
			
			aform.setAssocFileQuestionIds(assocFileQuestionList);
					
			if (this.getFileUpload() != null && !this.getFileUpload().isEmpty() &&
				this.getFileUploadFileName() != null && !this.getFileUploadFileName().isEmpty()) {
				aform.setFiles(this.buildFileMap());
			} 
			
			boolean complete = Boolean.valueOf(request.getParameter("complete"));
			boolean validate = complete;
			
			aform.setMarkAsCompleted(validate);
			
			List<String> inputErrors;
			inputErrors = InputHandler.getResponses(aform, request, validate,true,dataEntryForm); // validation
	
			String formName = aform.getForm().getName();
			boolean fileExist = Boolean.valueOf(request.getParameter(CtdbConstants.FILE_EXIST));
			
			if (!fileExist) {
				if (!aform.isAreThereAnswers() && complete) {
					JSONObject jsonObj = new JSONObject();
					jsonObj.put("status", StrutsConstants.ERROR_ANSWER_REQUIRED_IN_SAVE);
	
					out.print(jsonObj);
					out.flush();
					return null;
				}
			}
		
			if (inputErrors.isEmpty() && !this.hasErrors()) { // NO ERRORS SAVE NOW
				//ok now check to see if aform has been created
				if(aform.getId() == Integer.MIN_VALUE) {
					rm.create(aform, patientUser);
				}
				
				
				rm.saveProgress2(aform, patientUser);
				rm.saveVisibleRepeatableSection(aform, dataEntryForm, patientUser);
				rm.updateAdministeredFormVisitDate(aform);
				aform.setLoggedInUserId(patientUser.getId());

				JSONArray returnArray = getJSONArrayResponses(aform.getResponses()); 
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("status", "saveFormSuccess");
				jsonObj.put("returnArray", returnArray);
				jsonObj.put("formName", formName);
				out.print(jsonObj);

				out.flush();
				logger.info("Subject:"+sessionId+"#DataCollectionAction->saveSubjectFormPSR->#Success#->aformId:\t"+aform.getId());
				
				session.remove(ResponseConstants.AFORM_SESSION_KEY);
				return null;
			}
			else { // THERE ARE ERRORS
				aform.setLoggedInUserId(patientUser.getId());
				aform.setUserOneEntryId(patientUser.getId());
				
				StringBuffer sb = new StringBuffer();
				for (String errorMsg : inputErrors) {
					addActionError(errorMsg);
					String s = "<li style=\"margin-left:20px\">"+errorMsg+"</li>";
					sb.append(s);	
				}
				
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("status",sb.toString());
				out.print(jsonObj);
	
				// need to set list of form intervals to display
				request.setAttribute(CtdbConstants.AFORM_ID_REQUEST_ATTR, String.valueOf(aform.getId()));
				request.setAttribute(StrutsConstants.FORMFETCHED, true);
				request.setAttribute(StrutsConstants.FORMSTATUS, getAformFormStatus(aform, patientUser.getId()));
				
				logger.info("Subject:" + sessionId + 
						"#DataCollectionAction->saveSubjectForm-->errors in saving so returning to correct errors->aformId-->:\t" + aform.getId());
			}
		}
		catch(DuplicateObjectException doe){
			doe.printStackTrace();
			logger.error("Subject:" + sessionId + "An error occurred in lock step 1.", doe);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("status", "duplicateData");
		}
		catch ( CtdbException | SQLException e ) {
			logger.error("Subject:" + sessionId + "A database error occurred in save method.", e);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("status", "dataCollectionException");
			out.print(jsonObj);
		}
		catch ( MessagingException me ) {
			me.printStackTrace();
			logger.error("Subject:" + sessionId + "Error occurred while creating the notification email in save method.", me);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("status", "dataCollectionException");
			out.print(jsonObj);
		}
		catch ( IOException ie ) {
			logger.error("Subject:" + sessionId + "A I/O error occurred in save method.", ie);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("status", "dataCollectionException");
			out.print(jsonObj);
		}
		catch (Exception e) {
			logger.error("Subject:" + sessionId + "An error occurred in save method.", e);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("status", "dataCollectionException");
			out.print(jsonObj);
		}
		
		out.flush();	
		return null;
	}
	
	private String getQuestionGraphic() throws IOException {
		HttpServletResponse response = ServletActionContext.getResponse();
		
		try{
			PrintWriter out = response.getWriter();
			String qId = request.getParameter("qId");
			String filename = request.getParameter("filename");
			FormDataStructureUtility fsUtil = new FormDataStructureUtility();

			String image = fsUtil.getQuestionGraphic(qId, filename,request);
			
			out.print(image);
			out.flush();
			
		} catch (Exception e) {
			logger.error("Error occured while getting question graphic.", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		
		return null;
	}
	
	/***
	 * Method to lock the subject form
	 */
	private String lockFormStep1(ResponseManager rm, FormManager fm,
			ProtocolManager protoMan, PatientManager pm) throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();

		try {
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +
					"#DataCollectionAction->lockFormStep1->---------------------------------------");	

			String assocFileQuestionIds = request.getParameter(CtdbConstants.ASSOCFILEQUESTIONIDS);

			//remove comma at the last of comma separated array arguments
			if (!Utils.isBlank(assocFileQuestionIds)) {
				assocFileQuestionIds = assocFileQuestionIds.substring(0, assocFileQuestionIds.length() - 1);
			}
			
			String[] assocFileQuestionIdsArray = assocFileQuestionIds.split(",");
			List<String> assocFileQuestionList = Arrays.asList(assocFileQuestionIdsArray);

			request.setAttribute(CtdbConstants.DATACOLLECTION_MODE, request.getParameter(CtdbConstants.DATACOLLECTION_MODE));

			boolean eMode = Boolean.valueOf(request.getParameter(CtdbConstants.DATACOLLECTION_EDITMODE));
			request.setAttribute(CtdbConstants.DATACOLLECTION_EDITMODE, eMode);

			AdministeredForm aform = (AdministeredForm) session.get(ResponseConstants.AFORM_SESSION_KEY);
			int aformId = aform.getId();
			
			
			//User user = this.getEditUser(rm, aformId);
			int entryOneUserId = aform.getUserOneEntryId();
			User user = getUser(rm,entryOneUserId);
			
			
			User loggedInUser = getUser();
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +
					"#DataCollectionAction->lockFormStep1#->Getting aform object for aformid:\t" + aformId + " and user:\t" + user.getId());

			aform.setAssocFileQuestionIds(assocFileQuestionList);

			boolean markCompleted = Boolean.valueOf(request.getParameter("markAsCompletedCheckBoxStatus"));
			aform.setMarkAsCompleted(markCompleted);
			request.setAttribute(StrutsConstants.MARKASCOMPLETESTATUSINACTION, markCompleted);
			request.setAttribute(StrutsConstants.FORMSTATUS, getAformFormStatus(aform, user.getId()));

			if (this.getFileUpload() != null && !this.getFileUpload().isEmpty() &&
					this.getFileUploadFileName() != null && !this.getFileUploadFileName().isEmpty()) {
				aform.setFiles(this.buildFileMap());
			} else {
				aform.setFiles(new HashMap<String, Attachment>());
			}

			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) + "#DataCollectionAction->lockFormStep1#->PatientId:\t" + 
					aform.getPatient().getId());

			if (aform.getInterval() == null) {
				logger.info(DataCollectionUtils.getUserIdSessionIdString(request) + "#DataCollectionAction->lockFormStep1#->Visit Type:\tOther");
			} else {
				logger.info(DataCollectionUtils.getUserIdSessionIdString(request) + "#DataCollectionAction->lockFormStep1#->Visit Type:\t" + 
						aform.getInterval().getId());
			}
			
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) + "#DataCollectionAction->lockFormStep1#->Form:\t" + aform.getForm().getId());
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) + "#DataCollectionAction->lockFormStep1#->UserId:\t" + user.getId());


			String currentStatus = getAformFormStatus(aform, user.getId());

			List<String> inputErrors;

			if (currentStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_COMPLETED)) {
				inputErrors = InputHandler.getEditedAnswers(aform,request,dataEntryForm);
			} else {
				inputErrors = InputHandler.getResponses(aform, request, true,false,dataEntryForm); // validation
			}

			boolean fileExist = Boolean.valueOf(request.getParameter(CtdbConstants.FILE_EXIST));
			if (!fileExist) {
				if (!aform.isAreThereAnswers() && markCompleted) {
					//There are no answer and mark as completed check box is checked
					out.print(StrutsConstants.ERROR_ANSWER_REQUIRED_IN_LOCK);
					out.flush();			
					return null;
				}
			}

			if (inputErrors.isEmpty()) {
				
				if(aform.getId() == Integer.MIN_VALUE) {
					rm.create(aform, user);
				}
				if (currentStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_COMPLETED)) {

					rm.saveCompletedEditAnswers(aform,loggedInUser);

				} else {
					rm.saveProgress2(aform, loggedInUser);
				}
				rm.saveVisibleRepeatableSection(aform, dataEntryForm, user);

				//check again if answers were saved to draft
				boolean areThereDraftAnswers = rm.areThereDraftAnswers(aform.getId());
				if (!areThereDraftAnswers) {
					logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->lockFormStep1->#Error Saving answers#-->aformId:\t"+aform.getId());
					out.print("<li style=\"margin-left:20px\">"+ "There was an error saving answers."+"</li>");
				} else {
					logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->lockFormStep1->#Success#-->aformId:\t"+aform.getId());
					out.print("lockForm1Success");
				}
				
				session.put(ResponseConstants.AFORM_SESSION_KEY, aform);

			} else {
				// prepare form
				aform.setLoggedInUserId(user.getId());

				for (String errorMsg : inputErrors) {
					out.print("<li style=\"margin-left:20px\">" + errorMsg + "</li>");
				}

				request.setAttribute(CtdbConstants.AFORM_ID_REQUEST_ATTR, String.valueOf(aform.getId()));
				request.setAttribute(StrutsConstants.FORMFETCHED, true);
				session.put("searchFormType", "patientPVDiv");

				if (eMode) {
					dataEntryForm.setEditHideNextPrevious(true);
				}
			}
		}
		catch(DuplicateObjectException doe){
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) + "An error occurred in lock step 1.", doe);
			out.print("duplicateData");			
		}
		catch ( CtdbException | SQLException e ) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) + "A database error occurred in lockStep1 method.", e);
			out.print("dataCollectionException");
		}
		catch ( MessagingException me ) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) + 
					"Error occurred while creating the notification email in lockStep1 method.", me);
			out.print("dataCollectionException");
		}
		catch ( IOException ie )
		{	
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) + "A I/O error occurred in lockStep1 method.", ie);
			out.print("dataCollectionException");
		}
		catch (Exception e) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"An error occurred in lockStep1 method.", e);
			out.print("dataCollectionException");
		}
		
		out.flush();
		return null;
	}
	
	
	/**
	 * Method to lock form after validation errors are passed that will actually 
	 * changed it to locked state with confirmation dialog.
	 */
	private String lockFormStep2(ResponseManager rm) throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		AdministeredForm aform = null;
		try{
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->lockFormStep2->------------------------------------");	
			
			aform = (AdministeredForm) session.get(ResponseConstants.AFORM_SESSION_KEY);
			int aformId = aform.getId();

			User user = this.getEditUser(rm, aformId);
			User loggedInUser = getUser();
			
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->lockFormStep2#->Getting aform object for aformid:\t"+aformId + " and user:\t" + user.getId());
	
		
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->lockFormStep2#->PatientId:\t"+aform.getPatient().getId());
			if (aform.getInterval() == null) {
				logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->lockFormStep2#->Visit Type:\tOther");
			} else {
				logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->lockFormStep2#->Visit Type:\t"+aform.getInterval().getId());
			}
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->lockFormStep2#->Form:\t"+aform.getForm().getId());
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->lockFormStep2#->UserId:\t"+user.getId());
			
			rm.performExpressDataEntry(aform, user, false, loggedInUser);  
			addActionMessage("The administered form "+aform.getForm().getName()+" has been Locked successfully");
			session.put(DataCollectionAction.ACTION_MESSAGES_KEY, getActionMessages());
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->lockFormStep2->#Success#--aformId-->:\t"+aform.getId());	
			
			//insert locked data into data submission table for transfer to mirth nightly process
			DataSubmissionManager dsm = new DataSubmissionManager();
			Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			dsm.submitDataForMirth(aform,protocol);
			
		} catch (DuplicateObjectException doe){
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"An error occurred in lock step 2-->DuplicateObjectException", doe);			
			addActionError(getText(StrutsConstants.DUPLICATE_AND_OTHER_EXCEPTIONS)+"<br/>Form Name : "+aform.getForm().getName()+"<br/>Visit Type : "+dataEntryForm.getIntervalName()+"<br/>Patient Id : "+dataEntryForm.getPatientId());
			session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
			session.remove(ResponseConstants.AFORM_SESSION_KEY);
			return StrutsConstants.MY_COLLECTIONS;
			//out.print("duplicateData");
		} catch (CtdbException e){			
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"An error occurred in lock step 2-->CtdbException", e);
			addActionError(getText(StrutsConstants.DUPLICATE_AND_OTHER_EXCEPTIONS)+"<br/>Form Name : "+aform.getForm().getName()+"<br/>Visit Type : "+dataEntryForm.getIntervalName()+"<br/>Patient Id : "+dataEntryForm.getPatientId());
			session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
			session.remove(ResponseConstants.AFORM_SESSION_KEY);
			return StrutsConstants.MY_COLLECTIONS;
		} catch (RuntimeException e){
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"An error occurred in lock step 2-->RuntimeException", e);
			addActionError(getText(StrutsConstants.DUPLICATE_AND_OTHER_EXCEPTIONS)+"<br/>Form Name : "+aform.getForm().getName()+"<br/>Visit Type : "+dataEntryForm.getIntervalName()+"<br/>Patient Id : "+dataEntryForm.getPatientId());
			session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
			session.remove(ResponseConstants.AFORM_SESSION_KEY);
			return StrutsConstants.MY_COLLECTIONS;
		} catch ( Exception e) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"An error occurred in lock step 2-->Exception", e);
			addActionError(getText(StrutsConstants.DUPLICATE_AND_OTHER_EXCEPTIONS)+"<br/>Form Name : "+aform.getForm().getName()+"<br/>Visit Type : "+dataEntryForm.getIntervalName()+"<br/>Patient Id : "+dataEntryForm.getPatientId());
			session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
			session.remove(ResponseConstants.AFORM_SESSION_KEY);
			return StrutsConstants.MY_COLLECTIONS;
			
		}
		
		session.remove(ResponseConstants.AFORM_SESSION_KEY);
		return StrutsConstants.MY_COLLECTIONS;
	}
	
	/**
	 * Method to lock CAT forms in PSR
	 * Ching-Heng
	 */
	private String lockFormPSR(ResponseManager rm, FormManager fm,
			ProtocolManager protoMan, PatientManager pm) throws Exception {
		
		HttpServletResponse response = ServletActionContext.getResponse();
		PrintWriter out = response.getWriter();
		//SecurityManager sm = new SecurityManager();
		//User user = sm.getPatientUser();
		
		
		
		
		
		//=================================================
		AdministeredForm aform = null;
		try{
			logger.info(getUser() + "#DataCollectionAction->lockFormPSR->------------------------------------");
			
			String assocFileQuestionIds = request.getParameter(CtdbConstants.ASSOCFILEQUESTIONIDS);
			//remove comma at the last of comma separated array arguments
			if (!Utils.isBlank(assocFileQuestionIds)) {
				assocFileQuestionIds = assocFileQuestionIds.substring(0, assocFileQuestionIds.length() - 1);
			}
			String[] assocFileQuestionIdsArray = assocFileQuestionIds.split(",");
			List<String> assocFileQuestionList = Arrays.asList(assocFileQuestionIdsArray);
			request.setAttribute(CtdbConstants.DATACOLLECTION_MODE, request.getParameter(CtdbConstants.DATACOLLECTION_MODE));
			boolean eMode = Boolean.valueOf(request.getParameter(CtdbConstants.DATACOLLECTION_EDITMODE));
			request.setAttribute(CtdbConstants.DATACOLLECTION_EDITMODE, eMode);
			aform = (AdministeredForm) session.get(ResponseConstants.AFORM_SESSION_KEY);
			
			int entryOneUserId = aform.getUserOneEntryId();
			User user = getUser(rm,entryOneUserId);
			
			User loggedInUser = user;
			
			aform.setAssocFileQuestionIds(assocFileQuestionList);
			aform.setMarkAsCompleted(true);
			request.setAttribute(StrutsConstants.MARKASCOMPLETESTATUSINACTION, true);
			request.setAttribute(StrutsConstants.FORMSTATUS, getAformFormStatus(aform, user.getId()));
			if (this.getFileUpload() != null && !this.getFileUpload().isEmpty() &&
					this.getFileUploadFileName() != null && !this.getFileUploadFileName().isEmpty()) {
				aform.setFiles(this.buildFileMap());
			} else {
				aform.setFiles(new HashMap<String, Attachment>());
			}
			if (aform.getInterval() == null) {
				logger.info(user +"#DataCollectionAction->lockFormPSR#->Visit Type:\tOther");
			} else {
				logger.info(user +"#DataCollectionAction->lockFormPSR#->Visit Type:\t"+aform.getInterval().getId());
			}
			String currentStatus = getAformFormStatus(aform, user.getId());

			List<String> inputErrors;

			if (currentStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_COMPLETED)) {
				inputErrors = InputHandler.getEditedAnswers(aform,request,dataEntryForm);
			} else {
				inputErrors = InputHandler.getResponses(aform, request, true, true, dataEntryForm); // validation
			}
			
			boolean fileExist = Boolean.valueOf(request.getParameter(CtdbConstants.FILE_EXIST));
			if (!fileExist) {
				if (!aform.isAreThereAnswers()) {
					//There are no answer and mark as completed check box is checked
					out.print(StrutsConstants.ERROR_ANSWER_REQUIRED_IN_LOCK);
					out.flush();			
					return null;
				}
			}

			if (inputErrors.isEmpty()) {
				
				if(aform.getId() == Integer.MIN_VALUE) {
					rm.create(aform, user);
				}
				
				if (currentStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_COMPLETED)) {
					rm.saveCompletedEditAnswers(aform,loggedInUser);
				} else {
					rm.saveProgress2(aform, loggedInUser);
				}
				rm.saveVisibleRepeatableSection(aform, dataEntryForm, user);

				//check again if answers were saved to draft
				boolean areThereDraftAnswers = rm.areThereDraftAnswers(aform.getId());
				if (!areThereDraftAnswers) {
					out.print("<li style=\"margin-left:20px\">"+ "There was an error saving answers."+"</li>");
				} 
			} else {
				// prepare form
				aform.setLoggedInUserId(user.getId());
				for (String errorMsg : inputErrors) {
					out.print("<li style=\"margin-left:20px\">" + errorMsg + "</li>");
				}
				request.setAttribute(CtdbConstants.AFORM_ID_REQUEST_ATTR, String.valueOf(aform.getId()));
				request.setAttribute(StrutsConstants.FORMFETCHED, true);
				session.put("searchFormType", "patientPVDiv");

				if (eMode) {
					dataEntryForm.setEditHideNextPrevious(true);
				}
			}
			
			//step 2 			
			rm.performExpressDataEntry(aform, user, false, loggedInUser);  
			addActionMessage("The administered form "+aform.getForm().getName()+" has been Locked successfully");
			session.put(DataCollectionAction.ACTION_MESSAGES_KEY, getActionMessages());	
			
			//insert locked data into data submission table for transfer to mirth nightly process
			DataSubmissionManager dsm = new DataSubmissionManager();
			Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			dsm.submitDataForMirth(aform,protocol);
			
		} catch (DuplicateObjectException doe){
			doe.printStackTrace();
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("status", "duplicateData");
		} catch (CtdbException e){			
			e.printStackTrace();
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("status", "ctdbException");
		} catch (RuntimeException e){
			e.printStackTrace();
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("status", "runtimeException");
		} catch ( Exception e) {
			e.printStackTrace();
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("status", "exception");		
		}		
		session.remove(ResponseConstants.AFORM_SESSION_KEY);
		//=================================================		
		JSONArray returnArray = getJSONArrayResponses(aform.getResponses()); 
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("status", "lockFormSuccess");
		jsonObj.put("returnArray", returnArray);
		jsonObj.put("formName", aform.getForm().getName());
		out.print(jsonObj);

		out.flush();	
		return null;
	}
	
	/**
	 * Method to validate main group in promis form
	 * Ching-Heng
	 */
	private String validateMainGroup(ResponseManager rm, FormManager fm,
			ProtocolManager protoMan, PatientManager pm) throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		List<String> inputErrors;
		boolean isSelfReporting = Boolean.valueOf(request.getParameter(CtdbConstants.isSelfReporting));
		JSONObject jsonObj = new JSONObject();
		StringBuffer sb = new StringBuffer();
		PrintWriter out = response.getWriter();
		AdministeredForm aform = (AdministeredForm) session.get(ResponseConstants.AFORM_SESSION_KEY);
		inputErrors = InputHandler.getResponses(aform, request, true, isSelfReporting,dataEntryForm); // validation

		if (inputErrors.isEmpty() && !this.hasErrors()) { // NO ERRORS SAVE NOW
			jsonObj.put("status",sb.toString());
			out.print(jsonObj);
			out.flush();
		}
		else { // THERE ARE ERRORS	

			for (String errorMsg : inputErrors) {
				addActionError(errorMsg);
				String s = "<li style=\"margin-left:20px\">" + errorMsg + "</li>";
				sb.append(s);	
			}			
			jsonObj.put("status",sb.toString());
			JSONArray returnArray = getJSONArrayResponses(aform.getResponses()); 
			jsonObj.put("returnArray", returnArray);
			out.print(jsonObj);
			out.flush();
		}	
		return null;	
	}
	
	/**
	 * Method to save Edits to final locked subject forms
	 */
	private String saveEditsToFinalLockedSujectForm(ResponseManager rm, ProtocolManager protoMan, 
			PatientManager pm, FormManager fm) throws Exception {
		
		logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"DataCollectionAction->saveEditsToFinalLockedSujectForm#--------------------------");
		String forward = null;
				
		//remove comma at the last of comma separated array arguments
		String assocFileQuestionIds = request.getParameter(CtdbConstants.ASSOCFILEQUESTIONIDS);
		if (!Utils.isBlank(assocFileQuestionIds)) {
			assocFileQuestionIds = assocFileQuestionIds.substring(0, assocFileQuestionIds.length() - 1);
		}
		
		String[] assocFileQuestionIdsArray = assocFileQuestionIds.split(",");
		List<String> assocFileQuestionList = Arrays.asList(assocFileQuestionIdsArray);

		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		String currentFormId = request.getParameter(CtdbConstants.FORM_ID_REQUEST_ATTR);
		
		request.setAttribute(CtdbConstants.DATACOLLECTION_MODE, request.getParameter(
				CtdbConstants.DATACOLLECTION_MODE));
		boolean eMode = Boolean.valueOf(request.getParameter(CtdbConstants.DATACOLLECTION_EDITMODE));
		request.setAttribute(CtdbConstants.DATACOLLECTION_EDITMODE, eMode);
		
		int aformId = Integer.parseInt(request.getParameter(CtdbConstants.AFORM_ID_REQUEST_ATTR));

		User user = this.getEditUser(rm, aformId);

		//AdministeredForm aform = rm.getAdministeredForm(aformId, false);
		AdministeredForm aform = (AdministeredForm) session.get(ResponseConstants.AFORM_SESSION_KEY);
		if(currentFormId==null){
			currentFormId= String.valueOf(aform.getForm().getId());
		}
		
		
		logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->saveEditsToFinalLockedSujectForm#->AFormId:\t"+aform.getId());
		logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->saveEditsToFinalLockedSujectForm#->PatientId:\t"+aform.getPatient().getId());
		if (aform.getInterval() == null) {
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->saveEditsToFinalLockedSujectForm#->Visit Type:\tOther");
		} else {
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->saveEditsToFinalLockedSujectForm#->Visit Type:\t"+aform.getInterval().getId());
		}
		logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->saveEditsToFinalLockedSujectForm#->Form:\t"+aform.getForm().getId());
		logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->saveEditsToFinalLockedSujectForm#->UserId:\t"+user.getId());
		
		aform.setAssocFileQuestionIds(assocFileQuestionList);
		aform.setMarkAsCompleted(true);
		request.setAttribute(StrutsConstants.MARKASCOMPLETESTATUSINACTION, true);

		int intervalId = dataEntryForm.getIntervalId();
		int patientId = dataEntryForm.getPatientId();
		
		HttpServletResponse response = ServletActionContext.getResponse();
		PrintWriter out = response.getWriter();

		try {
			if (this.getFileUpload() != null && !this.getFileUpload().isEmpty() &&
				this.getFileUploadFileName() != null && !this.getFileUploadFileName().isEmpty()) {
				aform.setFiles(this.buildFileMap());
			} 

			session.put(ResponseConstants.AFORM_SESSION_KEY, aform);
			
			List<String> errorList = InputHandler.getEditedAnswers(aform,request,dataEntryForm);
			
			// If there are invalid answers, return for correction.
			if (!errorList.isEmpty()) {
				//for (String err : errorList) {
					//addActionError(err);
				//}
				
				//request.setAttribute(ResponseConstants.ERROR_DETAILS, errorList);
				request.setAttribute("responseList", aform.getResponses());
				
				aform.setLoggedInUserId(user.getId());
				
				request.setAttribute("jsResponseList", getJsResponses(aform.getResponses()));
				
				
				StringBuffer sb = new StringBuffer();

				for (String errorMsg : errorList) {
					addActionError(errorMsg);
					String s = "<li style=\"margin-left:20px\">" + errorMsg + "</li>";
					sb.append(s);	
				}

				JSONObject jsonObj = new JSONObject();
				jsonObj.put("status",sb.toString());
				JSONArray returnArray = getJSONArrayResponses(aform.getResponses()); 
				jsonObj.put("returnArray", returnArray);
				out.print(jsonObj);
				
				

				request.setAttribute(StrutsConstants.FORMSTATUS, 
						CtdbConstants.DATACOLLECTION_STATUS_FINALLOCKED);
				request.setAttribute(CtdbConstants.DATACOLLECTION_MODE, 
						CtdbConstants.DATACOLLECTION_FORMPATIENT);
				request.setAttribute(CtdbConstants.DATACOLLECTION_EDITMODE, true);
				request.setAttribute(CtdbConstants.AFORM_ID_REQUEST_ATTR, String.valueOf(aform.getId()));
				request.setAttribute(StrutsConstants.FORMFETCHED, true);
				request.setAttribute(CtdbConstants.FORM_ID_REQUEST_ATTR, currentFormId);
				
				//Patient patient = pm.getPatient(patientId);
				//List<AdministeredForm> collectedAdminForms = rm.getAdminFormForSubjectIntervalAndStudy(
						//intervalId, patientId, protocol.getId());

				//this.setupPage(patient, collectedAdminForms, aform);
								
				if (eMode) {
					dataEntryForm.setEditHideNextPrevious(true);
				}
				
				//return SUCCESS;

			} else {
				// Check to see if any question has been edited.
				boolean empty = true;
				for (Response resp : (List<Response>) aform.getResponses()) {
					if (!resp.getEditAnswers().isEmpty()) {
						empty = false;
						break;
					}
				}
				
				// if no question is edited, then not going to bother the database
				if (empty) {
					//addActionError("No question was edited for the administered form " + aform.getForm().getName() + ".");
				} else {				
					User loggedInUser = getUser();
					Integer formStatus = fm.getFormXsubmissionStatus(Integer.valueOf(aform.getId()));
					rm.saveFinalEditAnswers(aform, loggedInUser);
					rm.saveVisibleRepeatableSection(aform, dataEntryForm, user);
					rm.updateAdministeredFormVisitDate(aform);
					//do not set the data for mirth if the data has already been submitted or the submission status
					//is null for some reason
					if(formStatus != -1 && formStatus != 2){
						DataSubmissionManager dsm = new DataSubmissionManager();
						dsm.deletePendingSubmissionData(aform);
						dsm.submitDataForMirth(aform, protocol);
					}
					//addActionMessage("The administered form "+ aform.getForm().getName()+ " was successfully edited.");
				}
				
				session.remove("tobeEditedForm");
				session.remove(StrutsConstants.FORMNAME);
				session.remove("formdescription");
				session.remove("forminterval");
				session.remove("formnihnumber");
				session.remove("patientListforedit");
				session.remove("formintervallist");
				session.remove("CancelToResponseHome");
				session.remove("_editAnswerSrc");

				session.put(ResponseConstants.FORM_SESSION_KEY, aform.getForm());
				session.remove(ResponseConstants.AFORM_SESSION_KEY);
				
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("status", "saveFormSuccess");
				jsonObj.put("fName", aform.getForm().getName());
				out.print(jsonObj);

			}

		} catch ( ObjectNotFoundException onfe) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"Could not find aform", onfe);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("status", "dataCollectionException");
			out.print(jsonObj);
			
		} catch (DuplicateObjectException doe) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) + "Duplicate Obj Error.", doe);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("status", "duplicateData");
			out.print(jsonObj);
		}
		catch (Exception e) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"An error occurred in save method.", e);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("status", "dataCollectionException");
			out.print(jsonObj);
		}
		
		logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->saveEditsToFinalLockedSujectForm->#Success#-->aformId:\t"+aform.getId());
		
		out.flush();
		return null;
	}
	
	
	public String editSubjectForm(ResponseManager rm, FormManager fm, QuestionManager qm, 
				ProtocolManager protoMan, PatientManager pm) throws RuntimeException, IOException, JAXBException,  Exception  {
		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		
		//dataEntryForm.setEditHideNextPrevious(true);
		int aformId = Integer.parseInt(request.getParameter(CtdbConstants.AFORM_ID_REQUEST_ATTR));
		
		if (request.getParameter(StrutsConstants.EDITUSER) != null) {
			session.put(StrutsConstants.EDITUSER, request.getParameter(StrutsConstants.EDITUSER));
		}
		
		
		int intervalId = dataEntryForm.getIntervalId();
		int patientId = dataEntryForm.getPatientId();
		List<AdministeredForm> collectedAdminForms = null;
		
		if (nextPreviousJumpToFormTracker) {
			dataEntryForm.setEditHideNextPrevious(false);
			AdministeredForm af = rm.getAdminFormForSubjectIntervalStudyForGivenForm(
					dataEntryForm.getFormId(), intervalId, patientId, protocol.getId());
			
			aformId = af.getId();
			request.setAttribute(StrutsConstants.MARKASCOMPLETESTATUSINACTION, af.isMarkAsCompleted());
		}
		
		User user = this.getEditUser(rm, aformId);

		AdministeredForm aform = null;
		List<Response> responseList = new ArrayList<Response>();
		FormDataStructureUtility fsUtil = new FormDataStructureUtility();
		
		try {
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->editSubjectForm#---------------------------------------------");
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->editSubjectForm#->Getting aform object for aformid:\t" +
					aformId + " and user:\t" + user.getId());
			
			String shortName = fm.getEFormShortNameByAFormId(aformId);
			
			aform = rm.getAdministeredForm(aformId, user);
			int eformid = aform.getEformid();
			Form form = fsUtil.getEformFromBrics(request, shortName);
			form.setId(eformid);
			form.setProtocolId(protocol.getId());
			aform.setForm(form);
			aform.setUserOneEntryId(user.getId());
			
			Date sDate = aform.getScheduledVisitDate();
			String sDateString = "";
			if(sDate != null) {
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				sDateString =  dateFormat.format(sDate);
			}
			
			dataEntryForm.setFormId(eformid);
			dataEntryForm.setVisitDate(sDateString);
			dataEntryForm.setScheduledVisitDate(sDateString);
			session.put("currentVisitDate", sDateString);
			session.put("scheduledVisitDate", sDateString);
			
			if(aform.isCAT()) {
				// The PROMIS forms can NOT be edited
				String mType = aform.getForm().getMeasurementType();
				if(StrutsConstants.ADAPTIVE.equals(mType) || StrutsConstants.AUTO_SCORING.equals(mType)) {
					addActionError(getText("The PROMIS Adaptive Instrument or Auto-Scoring forms can not be edited"));
					session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
					return StrutsConstants.MY_COLLECTIONS;
				}
			}
			
			DataCollectionUtils.completeAform(aform);
			responseList = aform.getResponses();
			
			if (intervalId == Integer.MIN_VALUE && patientId == Integer.MIN_VALUE) {
				intervalId = aform.getInterval().getId();
				patientId = aform.getPatient().getId();	
			}
			
			

			
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->editSubjectForm#->PatientId:\t"+aform.getPatient().getId());
			if (aform.getInterval() == null) {
				logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->editSubjectForm#->Visit Type:\tOther");
			} else {
				logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->editSubjectForm#->Visit Type:\t"+aform.getInterval().getId());
			}
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->editSubjectForm#->Form:\t"+aform.getForm().getId());
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->editSubjectForm#->UserId:\t"+user.getId());
			
			request.setAttribute(StrutsConstants.MARKASCOMPLETESTATUSINACTION, aform.isMarkAsCompleted());
			
		} catch (CtdbException e) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"editSubjectForm method has got error", e);
			addActionError(getText(StrutsConstants.ERROR_RESPONSE_NOTUSER));
			session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
			return StrutsConstants.MY_COLLECTIONS;
		} 

		// only do following if data entry is attempting to edit form the edit user
		// param only comes when a PI or AI is attempting to edit a users collection
		if (request.getParameter(StrutsConstants.EDITUSER) == null && 
			session.get(StrutsConstants.EDITUSER) == null) {
			
			EditAssignment ea = rm.getEditAssignment(aform.getId(), aform.getDataEntrySession());
			
			if (user.getId() == ea.getCurrentBy() && aform.getFinalLockDate() != null) {
				addActionError(getText(StrutsConstants.ERROR_RESPONSE_LOCKED));
				session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
				return StrutsConstants.MY_COLLECTIONS;
			}
		}

		//request.setAttribute("disableStr", qm.disableSkipRule(aform.getForm().getId()));

		DataEntryHeader dataEntryHeader = new DataEntryHeader();
		dataEntryHeader.setFormDisplay(aform.getForm().getName());
		dataEntryHeader.setDateDisplay(aform.getVisitDateStringyyyyMMddHHmm());
		dataEntryHeader.setScheduledVisitDateDisplay(aform.getScheduledVisitDateStringyyyyMMddHHmm());
		
		String intervalName = null;
		if (aform.getInterval() != null && aform.getInterval().getId() > 0) {
			Interval i = protoMan.getInterval(aform.getInterval().getId());
			intervalName = i.getName();
		} else {
			intervalName = StrutsConstants.OTHER;
		}
		dataEntryHeader.setIntervalDisplay(intervalName);
		Patient patient = this.getPatientForCollection(rm, protocol.getId(), aform.getPatient().getId());
		aform.setPatient(patient);
		dataEntryHeader.setPatientDisplay(patient.getDisplayLabel(
				protocol.getPatientDisplayType(), protocol.getId()));
		dataEntryHeader.setGuid(patient.getGuid());
		dataEntryHeader.setStudyName(protocol.getName());
		dataEntryHeader.setStudyNum(String.valueOf(protocol.getProtocolNumber()));
		dataEntryHeader.setSingleDoubleKeyFlag(CtdbConstants.SINGLE_ENTRY_FORM);

		session.put(ResponseConstants.DATAENTRYHEADER_SESSION_KEY, dataEntryHeader);
		
		request.setAttribute("dataEntry", String.valueOf(aform.getDataEntrySession()));
		if (aform.getDataEntrySession() == 1) {			
			dataEntryHeader.setEntry(user.getUsername());
		} else {
			dataEntryHeader.setEntry2(user.getUsername());
			dataEntryHeader.setDateDisplay2(aform.getVisitDate2StringyyyyMMddHHmm());
		}

		// prepare form
		aform.setLoggedInUserId(user.getId());

		String formStatus = getAformFormStatus(aform, user.getId());
		dataEntryForm.setFormStatus(formStatus);
		dataEntryForm.setIntervalId(aform.getInterval().getId());
		dataEntryForm.setPatientId(aform.getPatient().getId());
				
		/*List<AdministeredForm> collectedAdminForms = rm.getAdminFormForSubjectIntervalAndStudy(
				dataEntryForm.getIntervalId(), patId, protocol.getId());*/
		
		
		collectedAdminForms = rm.getAdminFormForSubjectIntervalAndStudy(
				intervalId, patientId, protocol.getId());
		
		this.setupPage(patient, collectedAdminForms, aform);
		

		//since we are now allowing naviagation during editing, we need to remove the current
		//aform from collectedAdminForms so that the function setButtonDisable will disable correctly the
		//next and prev buttons
		if(aform.getId() != Integer.MIN_VALUE) {
			Iterator<AdministeredForm> iterAF = collectedAdminForms.iterator();
			while(iterAF.hasNext()) {
				AdministeredForm aff = iterAF.next();
				if(aff.getId() == aform.getId()) {
					iterAF.remove();
					break;
				}
			}
		}
		this.setButtonDisable(rm, intervalId, collectedAdminForms);
		
		
		
		//rm.completeGetAdministeredForm(aform, false);
		Form f = aform.getForm();
		
		//if (formStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_FINALLOCKED) || formStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_COMPLETED)) {
			request.setAttribute("responseList", aform.getResponses());
			request.setAttribute("jsResponseList", getJsResponses(aform.getResponses()));
		//}
		
		aform.setResponses(responseList);
		
		
		request.setAttribute(CtdbConstants.FORM_TYPE_REQUEST_ATTR, String.valueOf(f.getFormType()));
		dataEntryForm.setFormType(String.valueOf(CtdbConstants.FORM_TYPE_SUBJECT));
		dataEntryForm.setFormTypeString(CtdbConstants.FORM_TYPE_SUBJECT_STRING);
		
		request.setAttribute(CtdbConstants.PATIENT_ID_REQUEST_ATTR, aform.getPatient().getId());
		request.setAttribute(CtdbConstants.FORM_ID_REQUEST_ATTR, f.getId());
		request.setAttribute(StrutsConstants.FORMSTATUS, formStatus);
		request.setAttribute(StrutsConstants.FORMFETCHED, true);
		request.setAttribute(CtdbConstants.AFORM_ID_REQUEST_ATTR, String.valueOf(aform.getId()));
		request.setAttribute(StrutsConstants.ATTACHFILES, f.isAttachFiles());
		//added by Ching-Heng
		request.setAttribute(CtdbConstants.IS_CAT, f.isCAT());
		request.setAttribute(CtdbConstants.MEASUREMENT_TYPE, f.getMeasurementType());
		//session variable to track that it is coming from dataCollection page
		//This gives primany key in patient table that maps to patient id
		session.put(StrutsConstants.SUBJECT_COLLECTING_DATA, patient.getId());
		
		request.setAttribute(CtdbConstants.DATACOLLECTION_EDITMODE, true);
		
		session.put(StrutsConstants.DELETEONCANCELFLAG, false);		
		session.put(ResponseConstants.AFORM_SESSION_KEY, aform);
		
		return SUCCESS;
	}
	
	/**
	 * Method to fetch Subject Form
	 * @throws JAXBException 
	 * @throws SQLException 
	 */
	public void fetchSubjectForm(ProtocolManager protoMan, FormManager fm, 
			ResponseManager rm, PatientManager pm)  throws ParseException, CtdbException, CasProxyTicketException, WebApplicationException, JAXBException, SQLException {
		
		Long time1 = System.nanoTime();
		logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->fetchSubjectForm#----------------------------");

		request.setAttribute(StrutsConstants.MARKASCOMPLETESTATUSINACTION, false);

		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		User user = getUser();
		
		//since we are disabling visit type selector when starting collection by selecting scheduled visit (not by selectin eform), 
		//there is now a hidden input to get the visit type id in this case. (if user starts collection by selecting scheduled visit, 
		//then we disable the visittype dropdown and formintervalhidden is used...if user starts collection by selecting eform
		//then visittype dropdown is enabled and formintervalhidden is not used...so it remains negative)
		int formIntervalId = -1;
		int formIntervalHidden = dataEntryForm.getIntervalIdHidden();
		if(formIntervalHidden > 0) {
			dataEntryForm.setIntervalId(formIntervalHidden);
		}
		formIntervalId = dataEntryForm.getIntervalId();
		
		int patientId = dataEntryForm.getPatientId();
		
		String visitDate = dataEntryForm.getVisitDate();
		if (request.getParameter(StrutsConstants.VISITDATE) != null) {
			visitDate = request.getParameter(StrutsConstants.VISITDATE);
			dataEntryForm.setVisitDate(visitDate);
		}
		
		Long time2 = System.nanoTime();
		logger.info("#		DataCollectionAction->fetchSubjectForm# 2------------------elapsed: " + String.valueOf(time2-time1));
		time1 = time2;
		
		Date desDate = null;
		Date startDate = null;
		
		if(visitDate != null && !(visitDate.equals(""))) {
			desDate = DataCollectionUtils.convertStringToDate(visitDate);
			startDate = new Date(Long.parseLong(SysPropUtil.getProperty("default.system.visitDateMinimum.milliseconds").trim()));
		}
		
		if (desDate != null && !dataEntryForm.isDateInRange(desDate, startDate, new Date(
				System.currentTimeMillis() + 13 * 3600000))) {
			addActionError("Visit date must be between Jan 1st 1900 and today");

		} else {
			time2 = System.nanoTime();
			logger.info("#		DataCollectionAction->fetchSubjectForm# 3------------------elapsed: " + String.valueOf(time2-time1));
			time1 = time2;
			FormDataStructureUtility fsUtil = new FormDataStructureUtility();
			int eformid = dataEntryForm.getFormId();
			String shortName = fm.getEFormShortNameByEFormId(eformid);
			Form form = fsUtil.getEformFromBrics(request, shortName);
			form.setId(eformid);
			form.setProtocolId(protocol.getId());

			session.put(ResponseConstants.FORM_SESSION_KEY, form);
			
			AdministeredForm aform = new AdministeredForm();
			aform.setForm(form);
			aform.setCAT(form.isCAT());
			
			if(visitDate == null || (visitDate.equals(""))) {
				//make visit date to todays current date
				Date cDate = new Date();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				visitDate =  dateFormat.format(cDate);
				
			}
			
			aform.setVisitDate(DataCollectionUtils.convertStringToDate(visitDate));
			if (Boolean.valueOf(request.getParameter("startVisitType"))) {
				session.put("currentVisitDate", visitDate);
			}
			
			String scheduledVisitDate = dataEntryForm.getScheduledVisitDate();
			if(scheduledVisitDate == null || scheduledVisitDate.equals("")) {
				aform.setScheduledVisitDate(null);
			}else {
				aform.setScheduledVisitDate(DataCollectionUtils.convertStringToDate(scheduledVisitDate));
			}
			
			
			
			
			
			
			session.put("scheduledVisitDate", scheduledVisitDate);
			
			if (formIntervalId > 0) {
				Interval interval = new Interval();
				interval.setId(formIntervalId);
				aform.setInterval(interval);
			} else {
				aform.setInterval(null);
			}
			
			time2 = System.nanoTime();
			logger.info("#		DataCollectionAction->fetchSubjectForm# 5------------------elapsed: " + String.valueOf(time2-time1));
			time1 = time2;
			
			Patient patient =this.getPatientForCollection(rm, protocol.getId(), patientId);
			aform.setPatient(patient);
			time2 = System.nanoTime();
			logger.info("#		DataCollectionAction->fetchSubjectForm# 6------------------elapsed: " + String.valueOf(time2-time1));
			time1 = time2;
			//rm.create(aform, user);
			if(rm.checkIsDuplicateAdminForm(aform)) {
				//if user started collection from selecting eform, we need to set few things
				if (mode.equals(CtdbConstants.DATACOLLECTION_FORMPATIENT)) {
					setPatientIntervalsList(eformid, shortName , pm, protocol, protoMan, fm, eformid);
					Date todDate = new Date();
					SimpleDateFormat sf = new SimpleDateFormat(SysPropUtil.getProperty("default.system.datetimeformat"));
					String todDateS = sf.format(todDate);
					dataEntryForm.setVisitDate(todDateS);
					dataEntryForm.setScheduledVisitDate("");
				}
				throw new DuplicateObjectException();
			}

			time2 = System.nanoTime();
			logger.info("#		DataCollectionAction->fetchSubjectForm# 7------------------elapsed: " + String.valueOf(time2-time1));
			time1 = time2;
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->fetchSubjectForm#->AFormId:\t"+aform.getId());
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->fetchSubjectForm#->PatientId:\t"+aform.getPatient().getId());
			if (aform.getInterval() == null) {
				logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->fetchSubjectForm#->Visit Type:\tOther");
			} else {
				logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->fetchSubjectForm#->Visit Type:\t"+aform.getInterval().getId());
			}
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->fetchSubjectForm#->Form:\t"+form.getId());
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->fetchSubjectForm#->User:\t"+user.getId());
			
			DataEntryHeader dataEntryHeader = new DataEntryHeader();
			dataEntryHeader.setFormDisplay(form.getName());
			dataEntryHeader.setDateDisplay(visitDate);
			dataEntryHeader.setScheduledVisitDateDisplay(scheduledVisitDate);
			
			String intervalDisplay = StrutsConstants.OTHER;
			if (aform.getInterval() != null && aform.getInterval().getId() > 0) {
				intervalDisplay = protoMan.getInterval(aform.getInterval().getId()).getName();
			} 
			dataEntryHeader.setIntervalDisplay(intervalDisplay);

			Patient p = this.getPatientForCollection(rm, protocol.getId(), patientId);
			//set site to the patient
			this.setSubjectSite(p, aform,protocol);
			//if (aform.getDataEntrySession() == 1) {
				dataEntryHeader.setEntry(user.getUsername());
			//} else {
				//dataEntryHeader.setEntry2(user.getUsername());
				//dataEntryHeader.setDateDisplay2(visitDate);
			//}
			
			dataEntryHeader.setPatientDisplay(p.getDisplayLabel(protocol.getPatientDisplayType(), protocol.getId()));
			dataEntryHeader.setGuid(p.getGuid());
			dataEntryHeader.setStudyName(protocol.getName());
			dataEntryHeader.setStudyNum(protocol.getProtocolNumber());
			dataEntryHeader.setSingleDoubleKeyFlag(1);
			
			request.setAttribute("dataEntry", "1");
			session.put(ResponseConstants.DATAENTRYHEADER_SESSION_KEY, dataEntryHeader);
			
			aform.setStartMode(true);
			aform.setLoggedInUserId(user.getId());
			aform.setUserOneEntryId(user.getId());
			
			
			request.setAttribute(CtdbConstants.AFORM_ID_REQUEST_ATTR, String.valueOf(aform.getId()));

			request.setAttribute(StrutsConstants.ATTACHFILES, form.isAttachFiles());
			request.setAttribute(StrutsConstants.FORMFETCHED, true);
			request.setAttribute(StrutsConstants.FORMSTATUS, CtdbConstants.DATACOLLECTION_STATUS_INPROGRESS);
			request.setAttribute(CtdbConstants.FORM_ID_REQUEST_ATTR, form.getId());
			//added by Ching-Heng
			request.setAttribute(CtdbConstants.IS_CAT, form.isCAT());
			request.setAttribute(CtdbConstants.MEASUREMENT_TYPE, form.getMeasurementType());
			
			time2 = System.nanoTime();
			logger.info("#		DataCollectionAction->fetchSubjectForm# 9------------------elapsed: " + String.valueOf(time2-time1));
			time1 = time2;
			List<AdministeredForm> collectedAdminForms = null;
			if (formIntervalId > 0) {
				collectedAdminForms = rm.getAdminFormForSubjectIntervalAndStudy(
						formIntervalId, patientId, protocol.getId());
			}
			
			//session variable to track that it is coming from dataCollection page
			//This gives primany key in patient table that maps to patient id
			session.put(StrutsConstants.SUBJECT_COLLECTING_DATA, p.getId());
			
			//in order to set up the jsresponses that we need if user does a mark as complete and a save and stay, we need to complete the aform create response objects...then set it back to what it should be when fetching form for first time...empthy
			this.setupPage(p, collectedAdminForms, aform);
			
			//aform = setAformFormStatus(aform, user.getId());
			aform.setEntryOneStatus(CtdbConstants.DATACOLLECTION_STATUS_STARTED);

			this.setButtonDisable(rm, formIntervalId, collectedAdminForms);
			
			session.put(ResponseConstants.AFORM_SESSION_KEY, aform);
			session.put(StrutsConstants.DELETEONCANCELFLAG, true);
			time2 = System.nanoTime();
			logger.info("#		DataCollectionAction->fetchSubjectForm# 10------------------elapsed: " + String.valueOf(time2-time1));
			time1 = time2;
			
			request.setAttribute(CtdbConstants.DATACOLLECTION_DISPLAY_VISITDATE_WARNING, true);
		}
	}
	
	
	
	
	/**
	 * Method to fetch Subject Form
	 */
	public String fetchSubjectFormPSR(FormManager fm, ResponseManager rm, PatientManager pm) throws Exception {
		
		try {
			// because PSR doesn't use a logged-in user, we can't use the session user
			String sessionId = request.getSession().getId();
			logger.info("Subject:"+sessionId+"#DataCollectionAction->fetchSubjectForm-PatientSelfReporting----------------------------");
	
	
			Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			
			SecurityManager sm = new SecurityManager();
			User patientUser = sm.getPatientUser();
			int formIntervalId   = Integer.parseInt(request.getParameter("visitTypeId"));
			int patientId = Integer.parseInt(request.getParameter("patientId"));
			int proformsFormId = Integer.parseInt(request.getParameter("formId"));
			String token = request.getParameter("token");
			
			PatientVisit pv = pm.getPatientVisit(token);
			
	
			if(pv == null) {
				System.out.println("no patient visit found for this token");
				return StrutsConstants.FAILURE;
			}else {
				int pvVisitTypeId = pv.getIntervalId();
				int pvPatientId = pv.getPatientId();
				if(pvVisitTypeId != formIntervalId || pvPatientId != patientId) {
					System.out.println("patient visit type assoc with this token does not have same visit type id or patient id of aform");
					return StrutsConstants.FAILURE;
				}
			}
	
	    	String formShortName = fm.getEFormShortNameByEFormId(proformsFormId);
	    	Form form = fm.getFormWithoutSession(formShortName);
			form.setId(proformsFormId);
			form.setProtocolId(protocol.getId());
			
			AdministeredForm aform = new AdministeredForm();
			aform.setForm(form);
			aform.setCAT(form.isCAT());
	
			aform.setVisitDate(new Date());
			
			String psrScheduledVisitDateString = (String)session.get(CtdbConstants.PSR_SCHEDULED_VISIT_DATE);
			Date psrScheduledVisitDate = null;
			if(psrScheduledVisitDateString != null && !psrScheduledVisitDateString.equals("")) {
				psrScheduledVisitDate = DataCollectionUtils.convertStringToDate(psrScheduledVisitDateString);
			}
			aform.setScheduledVisitDate(psrScheduledVisitDate);
			
			
	
			if (formIntervalId > 0) {
				Interval interval = new Interval();
				interval.setId(formIntervalId);
				aform.setInterval(interval);
			} else {
				aform.setInterval(null);
			}
				
			Patient patient = this.getPatientForCollection(rm, protocol.getId(), patientId);
			aform.setPatient(patient);
			this.setSubjectSite(patient, aform,protocol );
				
			//rm.create(aform, patientUser);
			if(rm.checkIsDuplicateAdminForm(aform)) {
				throw new DuplicateObjectException();
			}
				
	
			logger.info("Subject:"+sessionId +"#DataCollectionAction->fetchSubjectForm-PatientSelfReporting->AFormId:\t"+aform.getId());
			logger.info("Subject:"+sessionId +"#DataCollectionAction->fetchSubjectForm-PatientSelfReporting->PatientId:\t"+aform.getPatient().getId());
			if (aform.getInterval() == null) {
				logger.info("Subject:"+sessionId +"#DataCollectionAction->fetchSubjectForm-PatientSelfReporting->Visit Type:\tOther");
			} else {
				logger.info("Subject:"+sessionId +"#DataCollectionAction->fetchSubjectForm-PatientSelfReporting->Visit Type:\t"+aform.getInterval().getId());
			}
			logger.info("Subject:"+sessionId +"#DataCollectionAction->fetchSubjectForm-PatientSelfReporting->Form:\t"+aform.getForm().getId());
			logger.info("Subject:"+sessionId +"#DataCollectionAction->fetchSubjectForm-PatientSelfReporting->User:\t"+patientUser.getId());
				
	
	
			aform.setStartMode(true);
			aform.setLoggedInUserId(patientUser.getId());
			aform.setUserOneEntryId(patientUser.getId());
			//aform = setAformFormStatus(aform, patientUser.getId());
			aform.setEntryOneStatus(CtdbConstants.DATACOLLECTION_STATUS_STARTED);
				
			request.setAttribute(CtdbConstants.AFORM_ID_REQUEST_ATTR, String.valueOf(aform.getId()));
	
			request.setAttribute(StrutsConstants.ATTACHFILES, form.isAttachFiles());
			request.setAttribute(StrutsConstants.FORMFETCHED, true);
			request.setAttribute(StrutsConstants.FORMSTATUS, CtdbConstants.DATACOLLECTION_STATUS_INPROGRESS);
			request.setAttribute(CtdbConstants.FORM_ID_REQUEST_ATTR, form.getId());
			
			request.setAttribute(StrutsConstants.DELETEONCANCELFLAG, true);
			request.setAttribute(FormConstants.FORMDETAIL, getFormHtml(aform, true, false, pv));
			request.setAttribute("token", token);
			//added by Ching-Heng
			request.setAttribute(CtdbConstants.IS_CAT, form.isCAT());
			request.setAttribute(CtdbConstants.MEASUREMENT_TYPE, form.getMeasurementType());
			session.put(ResponseConstants.AFORM_SESSION_KEY, aform);
			
			
			//need to check to see if there are any sections or questions that need to be hidden
			List<String> hiddenSectionsQuestionsElementIdsList = rm.getHiddenSectionsQuestionsElementIds(protocol.getId(), proformsFormId);
			JSONArray jsonReturnArray = new JSONArray(hiddenSectionsQuestionsElementIdsList);
			hiddenSectionsQuestionsElementIdsJSON = jsonReturnArray.toString();

			return StrutsConstants.DATA_COLLECTION_PSR;
		}
		catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public String editSubjectFormPSR(ResponseManager rm, FormManager fm, QuestionManager qm) {
		String sessionId = request.getSession().getId();
		
		try {
			Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			int aformId = Integer.parseInt(request.getParameter(CtdbConstants.AFORM_ID_REQUEST_ATTR));
			String token = request.getParameter("token");
			SecurityManager sm = new SecurityManager();
			User patientUser = sm.getPatientUser();
			PatientManager pm = new PatientManager();
			PatientVisit pv = pm.getPatientVisit(token);
			AdministeredForm aform = null;
			List<Response> responseList = new ArrayList<Response>();
			
			logger.info("Subject:" + sessionId + "#DataCollectionAction->editSubjectFormPSR#---------------------------------------------");
			logger.info("Subject:" + sessionId + "#DataCollectionAction->editSubjectFormPSR#->Getting aform object for aformid:\t" + aformId + 
					" and user:\t" + patientUser.getId());
			
			FormDataStructureUtility fsUtil = new FormDataStructureUtility();
			String shortName = fm.getEFormShortNameByAFormId(aformId);
			aform = rm.getAdministeredForm(aformId, patientUser);
			int eformid = aform.getEformid();			
			Form form = fm.getFormWithoutSession(shortName);		
			form.setId(eformid);
			form.setProtocolId(protocol.getId());
			aform.setForm(form);
			DataCollectionUtils.completeAform(aform);
			
			responseList = aform.getResponses();
			
			if (pv == null) {
				System.out.println("no patient visit found for this token");
				return StrutsConstants.FAILURE;
			} else {
				int pvVisitTypeId = pv.getIntervalId();
				int pvPatientId = pv.getPatientId();
				int aformVisitTypeId = aform.getInterval().getId();
				int aformPatientId = aform.getPatient().getId();
				if(pvVisitTypeId != aformVisitTypeId || pvPatientId != aformPatientId) {
					System.out.println("patient visit type assoc with this token does not have same visit type id or patient id of aform");
					return StrutsConstants.FAILURE;
				}
			}
			
			logger.info("Subject:"+sessionId+"#DataCollectionAction->editSubjectFormPSR#->PatientId:\t"+aform.getPatient().getId());
			
			if (aform.getInterval() == null) {
				logger.info("Subject:"+sessionId+"#DataCollectionAction->editSubjectFormPSR#->Visit Type:\tOther");
			} else {
				logger.info("Subject:"+sessionId+"#DataCollectionAction->editSubjectFormPSR#->Visit Type:\t"+aform.getInterval().getId());
			}
			
			logger.info("Subject:"+sessionId+"#DataCollectionAction->editSubjectFormPSR#->Form:\t"+aform.getForm().getId());
			logger.info("Subject:"+sessionId+"#DataCollectionAction->editSubjectFormPSR#->UserId:\t"+patientUser.getId());
			
			request.setAttribute("disableStr", qm.disableSkipRule(aform.getForm().getId()));
			request.setAttribute("dataEntry", String.valueOf(aform.getDataEntrySession()));
			
			// prepare form
			aform.setLoggedInUserId(patientUser.getId());
			aform.setUserOneEntryId(patientUser.getId());
			request.setAttribute(FormConstants.FORMDETAIL, getFormHtml(aform, true, false, pv));
		
			Form f = aform.getForm();
			aform.setResponses(responseList);
			
			request.setAttribute(StrutsConstants.DELETEONCANCELFLAG, false);
			request.setAttribute(StrutsConstants.FORMFETCHED, true);
			request.setAttribute(CtdbConstants.AFORM_ID_REQUEST_ATTR, String.valueOf(aform.getId()));
			request.setAttribute(StrutsConstants.ATTACHFILES, f.isAttachFiles());
			request.setAttribute("token", token); 
			//added by Ching-Heng
			request.setAttribute(CtdbConstants.IS_CAT, f.isCAT());
			request.setAttribute(CtdbConstants.MEASUREMENT_TYPE, f.getMeasurementType());
			
			session.put(ResponseConstants.AFORM_SESSION_KEY, aform);
			
			//need to check to see if there are any sections or questions that need to be hidden
			List<String> hiddenSectionsQuestionsElementIdsList = rm.getHiddenSectionsQuestionsElementIds(protocol.getId(), eformid);
			JSONArray jsonReturnArray = new JSONArray(hiddenSectionsQuestionsElementIdsList);
			hiddenSectionsQuestionsElementIdsJSON = jsonReturnArray.toString();
			
		} catch (CtdbException ce) {
			logger.error("Subject:" + sessionId + " Database error occured.", ce);
			addActionError(getText(StrutsConstants.ERROR_RESPONSE_NOTUSER));
			return StrutsConstants.FAILURE;
		} catch (WebApplicationException e) {
			logger.error("Subject:" + sessionId + "editSubjectForm method has got error", e);
			addActionError(getText(StrutsConstants.ERROR_RESPONSE_LOADINGFORM));
			return StrutsConstants.FAILURE;
		}
		
		return StrutsConstants.DATA_COLLECTION_PSR;
	}

	public void ajaxDigitalSignature() throws IOException, CtdbException {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		User user = getUser();
		boolean webServiceReachAble = DataCollectionUtils.isWebServiceUp(request, user);
		
		if (!webServiceReachAble){
			out.print("bricsAccountWSnotReacheable");
			out.flush();
			return;
		}
		
		String bricsPassword;
		try {
			Account bricsAccount = SecuritySessionUtil.getBricsAccountInformation(request, user);
			//String saltedPassword = dataEntryForm.getUserPassword();
			String saltedPassword = SecuritySessionUtil.getBricsAccountSalt(bricsAccount) + dataEntryForm.getUserPassword();
			
			String signedPassword = HashMethods.convertFromByte(SecuritySessionUtil.sha256(saltedPassword));
			bricsPassword = SecuritySessionUtil.getBricsAccountPassword(bricsAccount);
			
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) + "Digital Signature Validation" + user.getId());
			
			if (signedPassword.equals(bricsPassword)) {
				logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"Digital Signature Validation passed valiadtion.");
				//response.setStatus(200);//send success code
				out.print("passwordValidationPassed");
				out.flush();
				return;
			} else if (Utils.isBlank(dataEntryForm.getUserPassword())){
				logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"Digital Signature Validation failed valiadtion.Password is blank");
				out.print("blankPassword");
				out.flush();
				return;
			} else {
				logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"Digital Signature Validation failed valiadtion.Password mismatch error");
				out.print("mismatchPassword");
				out.flush();
				return;
			}
		} catch(UnknownHostException ue) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"An error occurred in getting Brics account info.", ue);
			out.print("bricsAccountWSnotReacheable");
			out.flush();
			return;
		} catch(CtdbException ce) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"An error occurred in getting Brics account info.", ce);
			out.print("bricsAccountWSnotReacheable");
			out.flush();
			return;
		} catch(NoRouteToHostException ne) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"An error occurred in getting Brics account info.", ne);
			out.print("bricsAccountWSnotReacheable");
			out.flush();
			return;
		}
	}

	/**
	 * Method for next previous and jumpTo navigation
	 */
	private String nextPreviousJumpToNavigation(ResponseManager rm, PatientManager pm, FormManager fm, ProtocolManager protoMan) {
		// Get this parameters from request scope that will be same for all
		// the forms common elements
		// Putting visit date in session so that others form in the interval can
		// get the same date unless they save or cancel will be on start mode
		try {
			Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			String currentVisitDate = (String) session.get("currentVisitDate");
			String scheduledVisitDate = (String) session.get("scheduledVisitDate");
			String currentSubjectId = request.getParameter("currentSubjectId");
			int currentFormId = Integer.parseInt(request.getParameter(CtdbConstants.FORM_ID_REQUEST_ATTR));
			AdministeredForm aform = (AdministeredForm) session.get(ResponseConstants.AFORM_SESSION_KEY);
			int aformId = aform.getId();
			int currentIntervalId = Integer.valueOf(request.getParameter("currentIntervalId"));
			String currentIntervalName = request.getParameter("currentIntervalName");
			
			// To get user for edit mode navigation
			int entryOneUserId = aform.getUserOneEntryId();
			User user = getUser(rm,entryOneUserId);
			
			//logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"#DataCollectionAction->nextPreviousJumpToNavigation#->Getting aform object for aformid:\t"+aformId + " and user:\t" + user.getId());
			
			boolean markCompleted = Boolean.valueOf(request.getParameter("markAsCompletedCheckBoxStatus"));
			aform.setMarkAsCompleted(markCompleted);
			
			if (this.getFileUpload() != null && !this.getFileUpload().isEmpty() &&
				this.getFileUploadFileName() != null && !this.getFileUploadFileName().isEmpty()) {
				aform.setFiles(this.buildFileMap());
			}
			
			int intervalId = dataEntryForm.getIntervalId();
			int patientId = dataEntryForm.getPatientId();
			boolean validate = markCompleted;
			String currentStatus = getAformFormStatus(aform, user.getId());
			List<String> inputErrors;
			
			if (currentStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_COMPLETED)) {
				inputErrors = InputHandler.getEditedAnswers(aform,request,dataEntryForm);
			} else {
				inputErrors = InputHandler.getResponses(aform, request, validate,false,dataEntryForm); // validation
			}
			
			boolean fileExist = Boolean.valueOf(request.getParameter(CtdbConstants.FILE_EXIST));
			
			if (!fileExist) {
				if (!aform.isAreThereAnswers() && markCompleted) {
					addActionError(getText(StrutsConstants.ERROR_ANSWER_REQUIRED_IN_SAVE));
				}
			}
			
			if (!inputErrors.isEmpty() || this.hasActionErrors()) { 
				
				aform.setLoggedInUserId(user.getId());
	
				if (aform.isAreThereAnswers()) {
					for (String errorMsg : inputErrors) {
						addActionError(errorMsg);
					}
			    }
	
				request.setAttribute(CtdbConstants.AFORM_ID_REQUEST_ATTR, String.valueOf(aform.getId()));
				request.setAttribute(StrutsConstants.MARKASCOMPLETESTATUSINACTION, aform.isMarkAsCompleted());
	
				Patient patient = this.getPatientForCollection(rm, protocol.getId(), aform.getPatient().getId());
				List<AdministeredForm> collectedAdminForms = rm.getAdminFormForSubjectIntervalAndStudy(
						intervalId, patientId, protocol.getId());
	
				this.setupPage(patient, collectedAdminForms, aform);
				request.setAttribute(CtdbConstants.FORM_ID_REQUEST_ATTR, aform.getForm().getId());
				request.setAttribute(CtdbConstants.DATACOLLECTION_MODE, request.getParameter(CtdbConstants.DATACOLLECTION_MODE));
				request.setAttribute(CtdbConstants.DATACOLLECTION_EDITMODE, false);
				request.setAttribute(StrutsConstants.FORMSTATUS, getAformFormStatus(aform, user.getId()));
				request.setAttribute(StrutsConstants.FORMFETCHED, true);
				request.setAttribute(CtdbConstants.IS_CAT, aform.getForm().isCAT());
				dataEntryForm.setVisitDate(currentVisitDate);
				dataEntryForm.setScheduledVisitDate(scheduledVisitDate); 
	
				this.setPatientDispalyValue(dataEntryForm, patient, protocol);

				
				// For setting the next previous disable flag based on locked status and navigation steps
				//since we are now allowing naviagation during editing, we need to remove the current
				//aform from collectedAdminForms so that the function setButtonDisable will disable correctly the
				//next and prev buttons
				if(aform.getId() != Integer.MIN_VALUE) {
					Iterator<AdministeredForm> iterAF = collectedAdminForms.iterator();
					while(iterAF.hasNext()) {
						AdministeredForm aff = iterAF.next();
						if(aff.getId() == aform.getId()) {
							iterAF.remove();
							break;
						}
					}
				}
				this.setButtonDisable(rm, intervalId, collectedAdminForms);
				request.setAttribute("jsResponseList", getJsResponses(aform.getResponses()));
				return SUCCESS;
				
			} else { 
				// NO ERRORS SO SAVE FORM....BUT DELETE IT IF THERE ARE NO ANSWERS
				String mType = aform.getForm().getMeasurementType();
				
				
				
				
				
				
				
				
				if(aformId == Integer.MIN_VALUE) {
					if (aform.isAreThereAnswers()) {
						if(StrutsConstants.ADAPTIVE.equals(mType) || StrutsConstants.AUTO_SCORING.equals(mType)) {
							// if it is a adaptive form or a auto-scroing PROMIS form, still delete it
							logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"DataCollectionAction->nextPreviousJumpToNavigation->Deleting Form since there are no answers:\t" +
									aform.getId() + " User who deleted this:\t" + user.getId());
							rm.unadministerForm(aform, rm.getDataEntryFlag(aform.getId(), user.getId()));
						}else {
							this.saveSubjectForm(rm, protoMan, pm, fm, true,false);					
							currentFormId = aform.getForm().getId();
							dataEntryForm.setShowSavedMessage(true);
							request.setAttribute(StrutsConstants.FORMNAMETOBESAVEDORLOCKED, 
									aform.getForm().getName() + " is Saved");
						}									
					} else {
						logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"DataCollectionAction->nextPreviousJumpToNavigation->Deleting Form since there are no answers:\t" +
								aform.getId() + " User who deleted this:\t" + user.getId());
						rm.unadministerForm(aform, rm.getDataEntryFlag(aform.getId(), user.getId()));
					}
				}else {
					this.saveSubjectForm(rm, protoMan, pm, fm, true,false);					
					currentFormId = aform.getForm().getId();
					dataEntryForm.setShowSavedMessage(true);
					request.setAttribute(StrutsConstants.FORMNAMETOBESAVEDORLOCKED, 
							aform.getForm().getName() + " is Saved");
				}
				
				
				
				
				
			}
			
			List<AdministeredForm> collectedAdminForms = rm.getAdminFormForSubjectIntervalAndStudy(
					currentIntervalId, Integer.valueOf(currentSubjectId), protocol.getId());
			
			List<FormInterval> fiList = rm.getActiveFormsListInInterval(intervalId);
			// Will return the next formId to navigate based on current and collection status situation
			// (skip the locked collections) and by any coll by other users
			logger.info("collectedAdminForms size:\t"+collectedAdminForms.size()+"fiList size:\t"+fiList.size());
			logger.info("action in next previous jump2--->"+action);
			int nextFormId = -1;
			if (action.equals(StrutsConstants.NEXTFORM)) {
				nextFormId = DataCollectionUtils.returnNextFormId(
						fiList, currentFormId, collectedAdminForms, request);
			} else if (action.equals(StrutsConstants.PREVIOUSFORM)) {
				nextFormId = DataCollectionUtils.returnPreviousFormId(
						fiList, currentFormId, collectedAdminForms, request);
			} else if (action.equals(StrutsConstants.JUMPTOFORM)) {
				nextFormId = Integer.valueOf(request.getParameter("jumptoformId"));
			}
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"Next form Id is:\t" + nextFormId);
			
			if (collectedAdminForms.isEmpty()) {
				action = StrutsConstants.FETCHFORM;
			} else {
				for (AdministeredForm collectedAdminForm : collectedAdminForms) {
					if (collectedAdminForm.getForm().getId() != nextFormId) {
						action = StrutsConstants.FETCHFORM;
					} else {
						nextPreviousJumpToFormTracker = true;
						action = StrutsConstants.EDITFORM;
						mode = CtdbConstants.DATACOLLECTION_FORMPATIENT;
						break;
					}
				}
			}
			
			dataEntryForm.setFormId(nextFormId);
			dataEntryForm.setVisitDate(currentVisitDate);
			dataEntryForm.setScheduledVisitDate(scheduledVisitDate);

			Patient patient = this.getPatientForCollection(rm, protocol.getId(), patientId);
		
			
			this.setPatientDispalyValue(dataEntryForm, patient, protocol);

			
			dataEntryForm.setIntervalName(currentIntervalName);
			dataEntryForm.setIntervalId(currentIntervalId);
			dataEntryForm.setClickedSectionFields("");
			
			logger.info(DataCollectionUtils.getUserIdSessionIdString(request) +"New action in next previous and jumpTo:\t" + action);			
		}
		catch ( CtdbException e ) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"A database error occurred in save method in next previous jumpTo navigation.", e);
			addActionError("Something went wrong in data collection process please retry your collection again.");
			session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
			session.remove(ResponseConstants.AFORM_SESSION_KEY);
			return StrutsConstants.DATA_COLLECTION;
		}
		catch ( MessagingException me ) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"Error occurred while creating the notification email in next previous jumpTo navigation.", me);
			addActionError("Something went wrong in data collection process please retry your collection again.");
			session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
			session.remove(ResponseConstants.AFORM_SESSION_KEY);
			return StrutsConstants.DATA_COLLECTION;
		}
		catch ( IOException ie )
		{
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"Something went wrong whiling writing to JSP.See stack track for details in next previous jumpTo navigation. ",ie);	
			addActionError("Something went wrong in data collection process please retry your collection again.");
			session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
			session.remove(ResponseConstants.AFORM_SESSION_KEY);
			return StrutsConstants.DATA_COLLECTION;
		}
		catch ( Exception e) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request) +"An error occurred in save method next previous jumpTo navigation..", e);
			addActionError("Something went wrong in data collection process please retry your collection again.");
			session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
			session.remove(ResponseConstants.AFORM_SESSION_KEY);
			return StrutsConstants.DATA_COLLECTION;
			
		}
		
		session.remove(ResponseConstants.AFORM_SESSION_KEY);
		return null;
	}
	
	
	
	private User getEditUser(ResponseManager rm, int formId) throws CtdbException {
		
		User user = null;
		if (session.get(StrutsConstants.EDITUSER) == null) {
			user = getUser();
		} else {
			int editUserNum = 1;  //only handling single entry forms
			EditAssignment ea = rm.getEditAssignment(formId, editUserNum);
			
			SecurityManager sm = new SecurityManager();
			user = sm.getUser(ea.getCurrentBy());
		}
		
		return user;
	}
	
	
	
	
	private User getUser(ResponseManager rm, int userid) throws CtdbException {
		
		User user = null;
		

			SecurityManager sm = new SecurityManager();
			user = sm.getUser(userid);
		
		
		return user;
	}


	
	private void populateActiveForms(ProtocolManager protoMan, int intervalId) throws CtdbException {

		Map<String, String> activeFormMap = new LinkedHashMap<String, String>();
		List<Form> activeForms = protoMan.getActiveFormsForInterval(intervalId);
		
		for (Form f : activeForms) {
			activeFormMap.put(Integer.toString(f.getId()), f.getName());
		}
		if (activeForms.isEmpty()) {
			activeFormMap.put("noForms", "There are no active forms for this study yet");
		}
		session.put(FormConstants.ACTIVEFORMS, activeFormMap);
	}
	
	private void setButtonDisable(ResponseManager rm, int intervalId, 
			List<AdministeredForm> collectedAdminForms) throws CtdbException {
		
		List<FormInterval> fiList = rm.getActiveFormsListInInterval(intervalId);
		
		int previousFormId = DataCollectionUtils.returnPreviousFormId(fiList, 
				dataEntryForm.getFormId(), collectedAdminForms, request);
		if (previousFormId < 0) {
			dataEntryForm.setPreviousButtonDisable(true);
		}
		
		int nextFormId = DataCollectionUtils.returnNextFormId(fiList, 
				dataEntryForm.getFormId(), collectedAdminForms, request);
		if (nextFormId < 0) {
			dataEntryForm.setNextButtonDisable(true);
		}
	}

	
	private void populateIntervalOptions(List<Interval> intervals) {
		
		Map<String, String> intervalOptions = new LinkedHashMap<String, String>();
		for (Interval i : intervals) {
			intervalOptions.put( String.valueOf(i.getId()), i.getName());
		}
		
		//intervalOptions.put("-1", "Other");		
		request.setAttribute(CtdbConstants.VISIT_TYPE_OPTIONS, intervalOptions);
	}
	
	
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public DataEntrySetupForm getDataEntryForm() {
		return dataEntryForm;
	}

	public void setDataEntryForm(DataEntrySetupForm dataEntryForm) {
		this.dataEntryForm = dataEntryForm;
	}

	public List<File> getFileUpload() {
		return fileUpload;
	}

	public void setFileUpload(List<File> fileUpload) {
		this.fileUpload = fileUpload;
	}

	public List<String> getFileUploadFileName() {
		return fileUploadFileName;
	}

	public void setFileUploadFileName(List<String> fileUploadFileName) {
		this.fileUploadFileName = fileUploadFileName;
	}

	public List<String> getFileUploadKey() {
		return fileUploadKey;
	}

	public void setFileUploadKey(List<String> fileUploadKey) {
		this.fileUploadKey = fileUploadKey;
	}

	public List<String> getFileUploadContentType() {
		return fileUploadContentType;
	}


	public void setFileUploadContentType(List<String> fileUploadContentType) {
		this.fileUploadContentType = fileUploadContentType;
	}
	
	

	
	public String getHiddenSectionsQuestionsElementIdsJSON() {
		return hiddenSectionsQuestionsElementIdsJSON;
	}

	public void setHiddenSectionsQuestionsElementIdsJSON(String hiddenSectionsQuestionsElementIdsJSON) {
		this.hiddenSectionsQuestionsElementIdsJSON = hiddenSectionsQuestionsElementIdsJSON;
	}

	/**

	Common method to handle subject site setting for current protocol form patinet protocol table
	@param Patient p
	@param AdministeredForm af
	@param Protocol protocol
	*/
	private void setSubjectSite(Patient p, AdministeredForm af, Protocol protocol) {
		SiteManager sm = new SiteManager();
		ResponseManager rm = new ResponseManager();
		try {
			Integer siteId = rm.getSubjectSiteForStudy(p.getId(), protocol.getId());
			af.setSiteName(sm.getSite(siteId).getName());
		} catch (CtdbException y) {
			logger.error("Looks like you don't have site associated to subject for the study or protocol");
		}
	}
	
	
	/**
	 * Method to setup all patient identifier for data collection
	 * @param DataEntrySetupForm
	 * @param Patient
	 * @param Protocol
	 */
	private void setPatientDispalyValue(DataEntrySetupForm desf,Patient p,Protocol protocol) {
		dataEntryForm.setGuid(p.getGuid());
		dataEntryForm.setMrn(p.getMrn());
		dataEntryForm.setSubjectId(p.getPatientprotocol().getSubjectId());
	}
	
	/**
	 * Get Patient mapping for Collection 
	 * @param rm
	 * @param protocolId
	 * @param patientId
	 */
	private Patient getPatientForCollection(ResponseManager rm,int protocolId,int patientId) {
		Patient patient = null;
		try {
			patient = rm.getPatientForCollection(protocolId, patientId);
		} catch (CtdbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return patient;
	}
	


	public Map<String, Attachment> buildFileMap() {
		HttpServletResponse response = ServletActionContext.getResponse();
		PrintWriter out;
		Map<String, Attachment> fileMap = new HashMap<String, Attachment>();
		try {
			out = response.getWriter();
	
		
		
		List<String> fileUploadKeyList = this.getFileUploadKey();
		
		Iterator iter = fileUploadKeyList.iterator();
		while(iter.hasNext()) {
			String key = (String)iter.next();
			if(key.equals("")) {
				iter.remove();
			}
		}
		
		
		if (this.getFileUpload() != null && this.getFileUploadFileName() != null) {

			for (int i = 0; i < this.getFileUpload().size(); i++) {
				File file = this.getFileUpload().get(i);
				String fileName = this.getFileUploadFileName().get(i);
				
					if(CtdbConstants.IMAGE_FORMATS.contains(FilenameUtils.getExtension(fileName))){
						
					}
				
				String fileContentType = this.getFileUploadContentType().get(i);
				String key = this.getFileUploadKey().get(i);
					
				if (file != null && !Utils.isBlank(fileName) && !Utils.isBlank(key)) {
					Attachment a = new Attachment();
					a.setAttachFile(file);
					a.setAttachFileFileName(fileName);
					a.setAttachFileContentType(fileContentType);
					a.setFileName(fileName);
					fileMap.put(key, a);
				}
			}
		} 
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return fileMap;
	}
	
	public AdministeredForm setAformFormStatus(AdministeredForm aform, int userId) throws CtdbException {
		ResponseManager rm = new ResponseManager();
		AdministeredForm aformToReturn = aform;
		int dataEntryFlagNum;
		dataEntryFlagNum = rm.getDataEntryFlag(aform.getId(), userId);
		List<DataEntryDraft> dataEntryList = rm.getDataEntries(aform.getId(), dataEntryFlagNum);
		aformToReturn.setEntryOneStatus(dataEntryList.get(0).getStatus());
		
		return aformToReturn;
	}
}
