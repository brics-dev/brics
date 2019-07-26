package gov.nih.nichd.ctdb.form.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.IIOException;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nih.nichd.ctdb.attachments.util.AttachmentIOUtil;
import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CasProxyTicketException;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.XslTransformer;
import gov.nih.nichd.ctdb.form.common.FormAssembler;
import gov.nih.nichd.ctdb.form.common.FormConstants;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.FormExportImport;
import gov.nih.nichd.ctdb.form.domain.FormGroup;
import gov.nih.nichd.ctdb.form.domain.FormInfoExportImport;
import gov.nih.nichd.ctdb.form.domain.ProformsDataElement;
import gov.nih.nichd.ctdb.form.domain.ProformsRepeatableGroup;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.form.form.FormForm;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.form.util.FormCache;
import gov.nih.nichd.ctdb.form.util.FormCacheThread;
import gov.nih.nichd.ctdb.form.util.FormDataStructureUtility;
import gov.nih.nichd.ctdb.form.util.HtmlFormCache;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.question.domain.AnswerType;
import gov.nih.nichd.ctdb.question.domain.Group;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionExportImport;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.domain.SkipRuleOperatorType;
import gov.nih.nichd.ctdb.question.domain.SkipRuleType;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.LookupResultControl;
import gov.nih.nichd.ctdb.util.common.Message;
import gov.nih.nichd.ctdb.util.common.MessageHandler;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.nichd.ctdb.util.domain.LookupType;
import gov.nih.nichd.ctdb.util.manager.LookupManager;
import gov.nih.tbi.commons.model.RepeatableType;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;


/**
 * FormAction will bring up add/edit form jsp page for user to start making new
 * forms or editing an existing form.
 * 
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class FormAction extends BaseAction {

	private static final long serialVersionUID = -8339688281300755020L;
	private static final Logger logger = Logger.getLogger(FormAction.class);
	
	public static final String ACTION_MESSAGES_KEY = "FormAction_ActionMessages";
	public static final String ACTION_ERRORS_KEY = "FormAction_ActionErrors";
	
	private FormForm formForm = new FormForm();
	private String id = null;
	private String action = null;
	private String jsonString = "{\"aaData\":[], \"aoColumns\":[]}";

	public void setupPage() throws Exception {
		
		buildLeftNav(LeftNavController.LEFTNAV_FORM_CREATE);

		LookupManager lookupMgr = new LookupManager();
		QuestionManager queMgr = new QuestionManager();
		List<CtdbLookup> nonPatientFormTypes = getNonPatientFormTypes(lookupMgr);
		
		session.put("formtypes", nonPatientFormTypes);
		request.setAttribute("webserviceException", "");

		// prepare to search question
		List<Group> questionGroups = queMgr.getGroups();
		Group all = new Group();
		all.setId(Integer.MIN_VALUE);
		all.setName("All Groups");
		questionGroups.add(0, all);
		
		session.put("groups", questionGroups);
		List<QuestionType> questionTypes = QuestionType.getDisplayTypes();
		session.put("questionTypes", questionTypes);
		session.put("types", questionTypes);
		session.put("medicalCodingStatusOptions", getOptionsFromMessageResources("app.options.search.questionmedicalcodingstatuses", ","));
		
		// prepare question attribute
		request.setAttribute("answers", new ArrayList());  
		request.setAttribute("answerTypes", AnswerType.toArray());
		request.setAttribute("rangeOptions", lookupMgr.getLookups(LookupType.QUESTIONRANGEOPERATOR));
		request.setAttribute("skipRuleOperatorTypes", SkipRuleOperatorType.toArray());
		request.setAttribute("skipRuleTypes", SkipRuleType.toArray());
		request.setAttribute("availableQuestionGroups", queMgr.getGroups());
		request.setAttribute("questionGroups", new ArrayList());
	}
	
	// Add form
	public String showAddForm() throws Exception {
		this.setupPage();
		
		List<CtdbLookup> formStatusList = getFormStatusList(false);
		session.put(FormConstants.XFORMSTATUS, formStatusList);
		request.setAttribute("formMode", "create");
		return SUCCESS;
	}
	
	// Edit form
	public String showEditForm() throws Exception {
		this.setupPage();
		
		// Check for any errors from the session to be displayed on the page
		if ( session.get(FormAction.ACTION_ERRORS_KEY) != null ) {
			this.retrieveActionErrors(FormAction.ACTION_ERRORS_KEY);
		}

		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		FormManager formMgr = new FormManager();
		
		Form form = formMgr.getForm(Integer.parseInt(id), true);
		
		if(form.isLegacy()) {
			addActionError(getText(StrutsConstants.ERROR_EDIT_LEGACY, new String[]{form.getName()}));
			session.put(ACTION_ERRORS_KEY, getActionErrors());
			return StrutsConstants.EXCEPTION;
			
		}
		
		if (form.getFormType() != 10) {
			request.setAttribute("_form_nonPatientForm", "true");
			session.put("formTypeToDisplay", "nonSubject");
		} else {
			request.setAttribute("_form_nonPatientForm", "false");
			session.put("formTypeToDisplay", "Subject");
		}

		// remove from cache if its going to get edited
		FormCache.getInstance().removeForm(form.getId());
		HtmlFormCache.getInstance().removeHtml(form.getId());
		
		FormAssembler.domainToForm(form, formForm);
		if (form.isAdministered()) {
			formForm.setIsAdministered(1);
		} else {
			formForm.setIsAdministered(0);
		}
		formForm.setId(form.getId());
		formForm.setFormid(form.getId());
		formForm.setFormtypeid(form.getFormType());
		if(form.getFormType() != 10) {
			formForm.setNonpatientformtypeid(form.getFormType());
		}
		List<CtdbLookup> formStatusList = null;
		if (form.getStatus().getId() == FormConstants.STATUS_INPROGRESS) {
			formStatusList = getInProgressFormStatusList();
		} else {
			formStatusList = getFormStatusList(formMgr.getHasQuestionsAttached(form));
		}
		session.put(FormConstants.XFORMSTATUS, formStatusList);
		session.put("formGroups", formMgr.getAssociatedFormGroups(form.getId()));
		session.put("availiableFormGroups", formMgr.getAvailiableFormGroups(form.getId(), p.getId()));
		session.put("orgCopyright", formForm.isCopyRight());
		request.setAttribute("formForm", formForm);
		request.setAttribute("formMode", "edit");

		String saveAs = (String) request.getParameter("comingFromSaveAs");
		if (saveAs != null && saveAs.equals("comingFromSaveAs")) {
			request.setAttribute("comingFromSaveAs","comingFromSaveAs");
		}
		return SUCCESS;
	}
	
	// Save as Form
	public String showSaveAsForm() throws Exception {
		this.setupPage();
		
		// Get a list of form structures form the Data Dictionary web service.
		/*try {
			List<FormStructure> dsList = FormDataStructureUtility.getDataStructureListFromWebService(request);
			request.setAttribute("dsList", dsList);
		}
		catch (Exception e) {
			logger.error("An error occurred while setting up the page.", e);
			request.setAttribute("webserviceException", "webserviceException");
		}*/
		
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		FormManager formMgr = new FormManager();

		Form form = formMgr.getFormAndSetofQuestions(Integer.parseInt(id));
		
		if(form.isLegacy()) {
			addActionError(getText(StrutsConstants.ERROR_SAVEAS_LEGACY, new String[]{form.getName()}));
			session.put(ACTION_ERRORS_KEY, getActionErrors());
			return StrutsConstants.EXCEPTION;
			
		}
		
		if (form.getFormType() == 10) {
			request.setAttribute("_form_nonPatientForm", "false");
			session.put("formTypeToDisplay", "Subject");
		} else {
			request.setAttribute("_form_nonPatientForm", "true");
			session.put("formTypeToDisplay","nonSubject");
		}

		// default form status to "Inactive" and default Single Key Data Entry -- CR#25262
		form.setStatus(new CtdbLookup(1, "Inactive"));
		FormAssembler.domainToForm(form, formForm);
		form.setSingleDoubleKeyFlag(1);
		form.setAccessFlag(1);
		formForm.setId(form.getId());
		formForm.setName("");

		List<CtdbLookup> formStatusList = getFormStatusList(formMgr.getHasQuestionsAttached(form));
		session.put(FormConstants.XFORMSTATUS, formStatusList);
		session.put("availiableFormGroups", formMgr.getFormGroups(p.getId()));
		session.put("formGroups", new ArrayList<FormGroup>());
		request.setAttribute("isCopyright", String.valueOf(form.isCopyRight()));
		
		FormDataStructureUtility fsUtil = new FormDataStructureUtility();
		String dsName = form.getDataStructureName();
		FormStructure fs = fsUtil.getDataStructureFromWebService(dsName, request);
		boolean doesFSContainDeprecatedOrRetiredDEs = fsUtil.doesFSContainDeprecatedOrRetiredDEs(fs);

		if(doesFSContainDeprecatedOrRetiredDEs) {
			MessageHandler messageHandler = new MessageHandler(request);
			messageHandler.addMessage(new Message(getText(StrutsConstants.CONTAINS_DEPRECATED_OR_RETIRED_DES, Arrays.asList("")),  MessageHandler.MESSAGE_TYPE_WARNING));
			messageHandler.save(request);
		}
		
		
		return SUCCESS;
	}
	
	// Copy form
	public String showCopyForm() throws Exception {
		this.setupPage();
		
		// Get a list of form structures form the Data Dictionary web service.
		/*try {
			List<FormStructure> dsList = FormDataStructureUtility.getDataStructureListFromWebService(request);
			request.setAttribute("dsList", dsList);
		}
		catch (Exception e) {
			logger.error("An error occurred while setting up the page.", e);
			request.setAttribute("webserviceException", "webserviceException");
		}*/
		
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		FormManager formMgr = new FormManager();
		Form form = formMgr.getFormAndSetofQuestions(Integer.parseInt(id));
		if (form.getFormType() != 10) {
			request.setAttribute("_form_nonPatientForm", "true");
		} else {
			request.setAttribute("_form_nonPatientForm", "false");
		}

		form.setStatus(new CtdbLookup(1, "Inactive"));  // default form status to "Inactive"
		FormAssembler.domainToForm(form, formForm);
		formForm.setId(form.getId());
		
		List<CtdbLookup> formStatusList = getFormStatusList(formMgr.getHasQuestionsAttached(form));
		session.put(FormConstants.XFORMSTATUS, formStatusList);
		session.put("availiableFormGroups", formMgr.getFormGroups(p.getId()));
		session.put("formGroups", new ArrayList<FormGroup>());
		
		FormDataStructureUtility fsUtil = new FormDataStructureUtility();
		String dsName = form.getDataStructureName();
		FormStructure fs = fsUtil.getDataStructureFromWebService(dsName, request);
		boolean doesFSContainDeprecatedOrRetiredDEs = fsUtil.doesFSContainDeprecatedOrRetiredDEs(fs);

		if(doesFSContainDeprecatedOrRetiredDEs) {
			MessageHandler messageHandler = new MessageHandler(request);
			messageHandler.addMessage(new Message(getText(StrutsConstants.CONTAINS_DEPRECATED_OR_RETIRED_DES, Arrays.asList("")),  MessageHandler.MESSAGE_TYPE_WARNING));
			messageHandler.save(request);
		}

		return SUCCESS;
	}
	
	
	public String saveNewForm() throws Exception {
		
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		User user = getUser();
		FormManager formMgr = new FormManager();

		// Save the form data user has generated so far
		Form form = (Form) FormAssembler.formToDomain(formForm);
		form.setProtocolId(p.getId());
		form.setCreatedBy(user.getId());
		form.setUpdatedBy(user.getId());
		form.setCreatedDate(new Date());
		form.setUpdatedDate(new Date());
		
		String dsName = formForm.getDataStructureName();
		// TODO Replace the empty string below once form structure versioning is supported again.
		String dsVersion = "";
		
		HttpServletResponse response = ServletActionContext.getResponse();
		String formAction = getAction();
		MessageHandler messageHandler = new MessageHandler(request);
		messageHandler.clearAll();
		try {
			if (formAction.equalsIgnoreCase(FormConstants.ACTION_PROCESS_ADD_FORMINFO)) {
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				session.put("nowCopyright", formForm.isCopyRight());
				
				
				response.setContentType("text/text");
				PrintWriter out = response.getWriter();
				JSONObject dataStructObj = getDataStructJson(dsName, dsVersion);

				if (dataStructObj == null) {
					out.print("webserviceException");
				} else {
					
					formMgr.createForm(form);

					int formID = formMgr.getFormID(p.getId(), form.getName());
					session.put(FormConstants.FORMID, formID);
					
					session.put("orgCopyright", formForm.isCopyRight()); 
				
					int formStatusId = Integer.parseInt(formForm.getStatus());
					StringBuilder sb = new StringBuilder();
					sb.append(formForm.getName()).append("::");
					sb.append(formMgr.getStatusName(formStatusId)).append("::");
					sb.append(formID).append("::");
					sb.append(formForm.isDataSpring()).append("::");
					sb.append(dsName).append("(").append(dsVersion).append(")").append("::");
					sb.append(dataStructObj);
					out.print(sb.toString());
				}
				out.flush();
				return null;
			
			} else if (formAction.equalsIgnoreCase(FormConstants.ACTION_PROCESS_SAVEAS) ||
					formAction.equalsIgnoreCase(FormConstants.ACTION_PROCESS_COPY)) {
			
				Form originalForm = formMgr.getFormAndSetofQuestions(formForm.getId());
				String originalDSNameAndVersion = originalForm.getDataStructureName() + 
						originalForm.getDataStructureVersion();
				String formDSNameAndVersion = dsName + dsVersion;
				FormDataStructureUtility fsUtil = new FormDataStructureUtility();
			
				if (originalDSNameAndVersion.equals(formDSNameAndVersion)) {
					formMgr.createSaveAsForm(form, originalForm, true);
				
					Form origForm = formMgr.getFormAndSections(formForm.getId());
					FormStructure ds = fsUtil.getDataStructureFromWebService(form.getDataStructureName(),request);
					if (!fsUtil.validateRequiredDataElements(origForm, ds)) {
						//addActionMessage(getText(StrutsConstants.VALIDATE_REQUIRED_DE_WARNING, Arrays.asList("")));
						messageHandler.addMessage(new Message(getText(StrutsConstants.VALIDATE_REQUIRED_DE_WARNING, Arrays.asList("")),  MessageHandler.MESSAGE_TYPE_WARNING));
					}
				} else {
					formMgr.createSaveAsForm(form, originalForm, false);

					int formID = formMgr.getFormID(p.getId(), form.getName());
					response.setContentType("text/text");
					PrintWriter out = response.getWriter();
					out.print(formID);
					out.flush();
					return null;
				}
			}
		
			//addActionMessage(getText(StrutsConstants.SUCCESS_ADD_KEY, Arrays.asList("Form " + form.getName())));
			messageHandler.addMessage(new Message(
					getText(StrutsConstants.SUCCESS_ADD_KEY, Arrays.asList("form " + form.getName())), 
					MessageHandler.MESSAGE_TYPE_MESSAGE));
			
			session.put(FormConstants.FORMID, form.getId());
			session.put(FormConstants.FORMNAME, form.getName());
			session.put(FormConstants.FORMSTATUS, form.getStatus());

			// update forms in the protocol in the session for header.jsp etc.
			p.setForms(formMgr.getActiveForms(p.getId()));

		}
		catch ( DuplicateObjectException doe ) {
			logger.error("Duplicate form found.", doe);
			FormAssembler.domainToForm(form, formForm);
			
			response.setContentType("text/text");
			PrintWriter out = response.getWriter();
			out.print(StrutsConstants.ERROR_DUPLICATE_FORM);
			out.flush();
			return null;
		}
		catch ( Exception e ) {
			logger.error("An error occurred while saving the form.", e);
			
			// Send the error message to the user's browser.
			response.setContentType("text/text");
			PrintWriter out = response.getWriter();
			out.print(StrutsConstants.ERROR_DATA_SAVE);
			out.flush();
			return null;
		}
		
		messageHandler.save(request);
		//session.put(FormAction.ACTION_MESSAGES_KEY, getActionMessages());
		return null;
	}
	
	
	public String saveEditForm() throws Exception {
		logger.info("FormAction: process_edit_form_info");
		
		MessageHandler messageHandler = new MessageHandler(request);
		messageHandler.clearAll();

		HttpServletResponse response = ServletActionContext.getResponse();

		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		User user = getUser();
		
		QuestionManager queMgr = new QuestionManager();
		FormManager formMgr = new FormManager();
		int formId = formForm.getId();
		String formName = formForm.getName();

		session.put("nowCopyright", formForm.isCopyRight());

		// if user has tried to set the form to active, then we have to
		// make sure that all required data elements are set
		Form origForm = formMgr.getFormAndSections(formId);

		// if user changed data structure, then we need to clear all the data  
		// elements of the original form no need to clear the data structure 
		// here since the update form should handle it!
		String dsName = formForm.getDataStructureName();
		// TODO Replace the empty string once form structure versioning is supported
		String dsVersion = "";
		String originalDSNameAndVersion = origForm.getDataStructureName() + 
				origForm.getDataStructureVersion();
		if (!originalDSNameAndVersion.equals(dsName + dsVersion)) {
			formMgr.clearDataElementAndGroupNameAssociationsForForm(origForm);
		}

		Form form = (Form) FormAssembler.formToDomain(formForm);
		form.setProtocolId(p.getId());
		form.setId(formId);
		form.setUpdatedBy(user.getId());
		session.put(FormConstants.FORMID, formId);
		session.put(FormConstants.FORMNAME, formName);

		try {
			formMgr.updateFormInfo(form);
		} catch (DuplicateObjectException doe) {
			logger.error("Found duplicate data when saving " + dsName, doe);
			response.setContentType("text/text");
			PrintWriter out = response.getWriter();
			out.print(StrutsConstants.ERROR_DUPLICATE_FORM);
			out.flush();
			return null;
		}
		catch ( Exception e ) {
			logger.error("An error occurred while save changes to the " + form.getName() + " form.", e);
			response.setContentType("text/text");
			PrintWriter out = response.getWriter();
			out.print(StrutsConstants.ERROR_DATABASE_SAVE);
			out.flush();
			return null;
		}
		
		FormCache.getInstance().removeForm(form.getId());
		if (form.getStatus().getId() == FormConstants.STATUS_ACTIVE) {
			FormCacheThread thread = new FormCacheThread(form);
			thread.start();
		}
		
		// update forms in the protocol in the session for header.jsp etc.
		p.setForms(formMgr.getActiveForms(p.getId()));
		
		JSONObject dataStructObj = null;
		
		try {
			 dataStructObj = getDataStructJson(dsName, dsVersion);
		}
		catch ( WebApplicationException we ) {
			//do nothing
			//this means the form structure was not found in dd, but we should continue to build the form
			logger.warn("Form structure not found in the Data Dictionary web service: " + we.getLocalizedMessage());
		}
		catch ( Exception e ) {
			response.setContentType("text/text");
			PrintWriter out = response.getWriter();

			messageHandler.save(request);
			out.print("webserviceException");
			out.flush();
			return null;
		}
		
		String sectionsJSONArrJSONString = "";
		String questionsJSONArrJSONString = "";

		// edit form info can occur while creating a form from scratch (create) or in edit mode (Edit)
		// if we are in edit mode, we need to get all the sections and questions so we can build the form!!
		if (request.getParameter("formMode").equals("edit")) {
			Form f = formMgr.getForm(formId, true);
			List<List<Section>> sections = formMgr.getSections(formId);

			if (sections != null && sections.size() > 0) {
				JSONArray sectionsJSONArr = new JSONArray();
				JSONArray questionsJSONArr = new JSONArray();
				Map<Integer, Integer> qustionMap = new HashMap<Integer, Integer>();
				
				for (List<Section> sectionRow : sections) {
					for ( Section sec : sectionRow ) {
						if (sec != null) {
							JSONObject secJSON = new JSONObject();
							String sectionId = FormAssembler.sectionDomainToSectionJson(secJSON, sec);
							sectionsJSONArr.put(secJSON);

							// now for each section, get all questions
							List<Question> questions = sec.getQuestionList();
							if (!questions.isEmpty()) {
								for (Question q : questions) {
									JSONObject quesJSON = new JSONObject();
									Set<Integer> attachedFormIds = queMgr.getAttachedFormIds(q.getId(), 
											q.getVersion().getVersionNumber());
									
									attachedFormIds.remove(new Integer(f.getId())); // don't include the current form Id.
									FormAssembler.questionDomainToQuestionJson(quesJSON,q,sectionId);
									quesJSON.put("questionOrder", q.getQuestionOrder());
									quesJSON.put("questionOrder_col", q.getQuestionOrderCol());
									quesJSON.put("attachedFormIds", attachedFormIds);
									quesJSON.put("attachedFormNames", queMgr.getCalDependentFormNames(q.getId()));
									quesJSON.put("hasDecimalPrecision", q.getHasDecimalPrecision());
									quesJSON.put("hasCalDependent", q.getHasCalDependent());
									quesJSON.put("prepopulation", q.getPrepopulation());
									questionsJSONArr.put(quesJSON);

									Boolean nowcopyright = (Boolean) session.get("nowCopyright");
									Boolean orgCopyright = (Boolean) session.get("orgCopyright");
									if (orgCopyright != null && orgCopyright && nowcopyright != null && nowcopyright) {
										qustionMap.put(q.getId(), q.getCopyfrom());
									}
								}
							}
						}
					}
				}
				
				if (qustionMap.size() > 0) {
					session.put("qustionMap", qustionMap);
				}
				sectionsJSONArrJSONString = sectionsJSONArr.toString();
				if (questionsJSONArr.length() > 0) {
					questionsJSONArrJSONString = questionsJSONArr.toString();
				}
			}
		}
		
		response.setContentType("text/text");
		PrintWriter out = response.getWriter();
		
		int formStatusId;
		if (formForm.getStatus() != null) {
			formStatusId = Integer.parseInt(formForm.getStatus());
		} else {
			formStatusId = Integer.parseInt(formForm.getStatusHidden());
		}

		Boolean nowcopyright = (Boolean) session.get("nowCopyright");
		Boolean orgCopyright = (Boolean) session.get("orgCopyright");

		StringBuilder sb = new StringBuilder();
		sb.append(formName).append("::");
		sb.append(formMgr.getStatusName(formStatusId)).append("::");
		sb.append(formId).append("::");
		sb.append(formForm.isDataSpring()).append("::");
		sb.append(dsName).append("(").append(dsVersion).append(")").append("::");
		sb.append(dataStructObj).append("::");
		sb.append(sectionsJSONArrJSONString).append("::").append(questionsJSONArrJSONString).append("::");
		sb.append(nowcopyright).append("::").append(orgCopyright);
		out.print(sb.toString());
		out.flush();

		messageHandler.save(request);
		return null;
	}
	
	/*added by Ching-Heng*/
	public String viewPromisFormInfo() {
		FormDataStructureUtility fsUtil = new FormDataStructureUtility();
		String shortName = request.getParameter("shortName");
		Form form;
		try {
			form = fsUtil.getEformFromBrics(request, shortName);			
			request.setAttribute("description", form.getDescription());
			request.setAttribute("title", form.getName());			
			if(form.getMeasurementType().equalsIgnoreCase(StrutsConstants.ADAPTIVE)) {
				request.setAttribute("type",StrutsConstants.ADAPTIVE_FULL);
			}else if(form.getMeasurementType().equalsIgnoreCase(StrutsConstants.AUTO_SCORING)) {
				request.setAttribute("type",StrutsConstants.AUTO_SCORING_FULL);
			}else if(form.getMeasurementType().equalsIgnoreCase(StrutsConstants.SHORT_FORM)) {
				request.setAttribute("type",StrutsConstants.SHORT_FORM_FULL);
			}else {
				request.setAttribute("type", "");
			}			
			return SUCCESS;
		} catch (WebApplicationException | CasProxyTicketException e) {
			e.printStackTrace();
		}
		
		return StrutsConstants.EXCEPTION;
	}
	
	public String viewFormDetail()  {
		try {
			FormDataStructureUtility fsUtil = new FormDataStructureUtility();
			FormManager formMgr = new FormManager();

			String formVersion = request.getParameter("formVersion");
			String shortName = request.getParameter("shortName");
			
			int eformId = Integer.parseInt(id);
			if(shortName == null || shortName.equals("")){
				shortName = formMgr.getEFormShortNameByEFormId(eformId);
			}
			Form form = fsUtil.getEformFromBrics(request, shortName);
			form.setId(eformId);
			Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			form.setProtocolId(protocol.getId());
			
			String formDetail = null;
			
			String xsl = SysPropUtil.getProperty("form.xsl.display");
			
			InputStream stream = request.getServletContext().getResourceAsStream(xsl);
			formDetail = XslTransformer.transform(form.getFormXml(), stream, CtdbConstants.GLOBAL_XSL_PARAMETER_MAP);
			
			
			String source = request.getParameter("source");
			if (source != null && (source.equals("sectionhome") || 
				source.equals("formAudit") || source.equals("adminForm"))) {
				request.setAttribute("source", "sectionhome");
			}

			request.setAttribute(FormConstants.FORMDETAIL, formDetail);
			request.setAttribute(FormConstants.FORMNAME, form.getName());
			if (request.getParameter("showVersion") != null) {
				request.setAttribute("showVersion", "true");
				request.setAttribute("formVersion", request.getParameter("formVersion"));
			}

			return SUCCESS;
		}catch (RuntimeException e) {
			logger.error("Error in retrieving form", e);
			addActionError(getText(StrutsConstants.ERROR_RESPONSE_LOADINGFORM));
			session.put(FormAction.ACTION_ERRORS_KEY, getActionErrors());
			return StrutsConstants.SUCCESS;
		} catch (Exception e) {
			logger.error("Error in retrieving form", e);
			addActionError(getText(StrutsConstants.ERROR_RESPONSE_LOADINGFORM));
			session.put(FormAction.ACTION_ERRORS_KEY, getActionErrors());
			return StrutsConstants.SUCCESS;
		}

		
	}
	
	
	public String activateForm() throws Exception {
		FormDataStructureUtility fsUtil = new FormDataStructureUtility();
		FormManager formMgr = new FormManager();

		int formId = Integer.parseInt(request.getParameter("formId"));
		Form form = formMgr.getFormAndSections(formId);

		MessageHandler messageHandler = new MessageHandler(request);
		messageHandler.clearAll();
		
		if (form.getFormType() == 10) {
			session.put("formTypeToDisplay","Subject");
		} else {
			session.put("formTypeToDisplay","nonSubject");
		}

		String nameVersion = form.getName();
		
		try {
			if (form.getDataStructureName().equals("")) {
				messageHandler.addMessage(new Message("The form, " + nameVersion + ", can not be activated because there is no form structure associated to it",  MessageHandler.MESSAGE_TYPE_ERROR));
			}
			else {
				FormStructure ds = fsUtil.getDataStructureFromWebService(form.getDataStructureName(), request);

				if (!fsUtil.validateRequiredDataElements(form, ds)) {
					messageHandler.addMessage(new Message("The form, " + nameVersion + ", can not be activated because not all required data elements are associated to questions on the form",  MessageHandler.MESSAGE_TYPE_ERROR));
				}
				else {
					form.setStatus(new CtdbLookup(3, "active"));
					formMgr.updateFormInfo(form);
					messageHandler.addMessage(new Message(getText("app.success.activate", Arrays.asList("form  " + form.getName())),  MessageHandler.MESSAGE_TYPE_MESSAGE));
				}
			}
		}
		catch (Exception e) {
			logger.error("Error occurred while activating form " + formId + ".", e);
			messageHandler.addMessage(new Message("The form, " + nameVersion + ", can not be activated because there was " + 
					"a problem connecting to the web service to check for proper form structure",  MessageHandler.MESSAGE_TYPE_ERROR));
			messageHandler.save(request);
		}
		
		messageHandler.save(request);
		return SUCCESS;
	}
	
	
	public String exportForm() throws Exception {
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		int formId = Integer.parseInt(request.getParameter(CtdbConstants.ID_REQUEST_ATTR));
		String questionImagePath = SysPropUtil.getProperty("filesystem.directory.questionimagepath");
		String imageFolderPath = request.getServletContext().getRealPath(questionImagePath);
		
		try {
			FormManager fm = new FormManager();
			boolean isLegacy = fm.isFormLegacy(formId);
			if(isLegacy) {
				String formName = fm.getFormNameForEFormId(p.getId(), formId);
				addActionError(getText(StrutsConstants.ERROR_EXPORT_LEGACY, new String[]{formName}));
				session.put(ACTION_ERRORS_KEY, getActionErrors());
				return StrutsConstants.EXCEPTION;
			}

			List<QuestionExportImport> qeList = FormExportHelper.exportQuestionToXML(formId, p.getId(), imageFolderPath);
			FormInfoExportImport formInfo = FormExportHelper.exportFormToXML(formId);
			FormExportImport fe = new FormExportImport();
			fe.setQuestion(qeList);
			fe.setFormInfo(formInfo);
		
			// Save file in this location
			File file = File.createTempFile(formInfo.getName(), ".xml");
			
			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(FormExportImport.class);
				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				jaxbMarshaller.marshal(fe, file);
				
				AttachmentIOUtil attUtil = new AttachmentIOUtil();
				attUtil.sendToBrowser(file, ServletActionContext.getResponse());
			}
			finally {
				logger.debug("Deleting temp export file: " + file.getCanonicalPath());
				boolean isDeleted = file.delete();
				
				// If the file can't be deleted now, mark the file to be deleted when the JVM exits.
				if ( !isDeleted ) {
					logger.debug("Marking the following temp export file to be deleted later: " + file.getCanonicalPath());
					file.deleteOnExit();
				}
			}
		}
		catch (JAXBException e) {
			logger.error("XML parsing error.", e);
			throw e;
		}
		catch (IIOException ie) {
			logger.error("Image IO exception occurred.", ie);
			addActionError(getText("errors.form.exportXML.imageio", new String[]{ie.getMessage()}));
			return StrutsConstants.EXCEPTION;
		}
		catch (Exception e) {
			logger.error("Error occurred while exporting the form " + formId + ".", e);
			throw e;
		}
		 
		return null;
	}
	
	/**
	 * Gets a listing of form structures from the data dictionary web service. The form structure listing is then 
	 * translated into a DataTables JSON configuration object, which will be sent back to the requester as a JSON string.
	 * 
	 * @return	Returns "SUCCESS" when the JSON configuration object is created, or one of the error conditions if 
	 * an error occurs.
	 */
	public String getFormStructureTableAsJSON() {
		String strutsResult = BaseAction.SUCCESS;
		FormDataStructureUtility fsUtil = new FormDataStructureUtility();
		
		try {
			logger.info("Requesting form structure list from the data dictionary web service...");
			List<FormStructure> fsList = fsUtil.getDataStructureListFromWebService(request);
			logger.info("Converting the form sturcture list to a DataTables JSON object...");
			JSONArray table = new JSONArray();
			
			// Construct the JSON table data.
			for ( FormStructure fs : fsList ) {
				// Add JSON row to the JSON table
				JSONArray row = new JSONArray();
				String shortName = fs.getShortName();
				
				row.put("<input type=\"radio\" value=\"" + shortName + "\" name=\"dataStructureRadio\">");
				row.put(shortName);
				row.put(fs.getVersion());
				row.put(fs.getDescription());
				row.put(fs.getIsCopyrighted() ? getText("button.yes") : getText("button.no"));
				
				table.put(row);
			}
			
			// Construct the table column names.
			JSONArray columns = new JSONArray();
			JSONObject columnDef = new JSONObject();
			
			//// Radio button column
			columnDef.put("sTitle", "");
			columnDef.put("sClass", "center");
			columns.put(columnDef);
			
			//// "Short Name" column
			columnDef = new JSONObject();
			columnDef.put("sTitle", getText("form.fsTable.columnTitle.shortName"));
			columns.put(columnDef);
			
			//// "Version" column
			columnDef = new JSONObject();
			columnDef.put("sTitle", getText("form.fsTable.columnTitle.version"));
			columns.put(columnDef);
			
			//// "Description" column
			columnDef = new JSONObject();
			columnDef.put("sTitle", getText("form.fsTable.columnTitle.description"));
			columns.put(columnDef);
			
			//// "Copyrighted?" column
			columnDef = new JSONObject();
			columnDef.put("sTitle", getText("form.fsTable.columnTitle.copyRighted"));
			columns.put(columnDef);
			
			// Bring table and column definitions together in the final JSON object
			JSONObject fsDataTablesObj = new JSONObject();
			
			fsDataTablesObj.put("aaData", table);
			fsDataTablesObj.put("aoColumns", columns);
			
			logger.info("Data structure DataTables JSON data has been generated.");
			jsonString = fsDataTablesObj.toString();
		}
		catch ( NoRouteToHostException | UnknownHostException | WebApplicationException e ) {
			logger.error("Error occurred while getting a connection to the DD web service.", e);
			strutsResult = StrutsConstants.BAD_GATEWAY;
		}
		catch ( JSONException je ) {
			logger.error("Could not construct the JSON table for the list of form structures.", je);
			strutsResult = BaseAction.ERROR;
		}
		catch ( RuntimeException re ) {
			logger.error("Error occurred while building a list of form structures from the dictionary web service.", re);
			strutsResult = BaseAction.ERROR;
		}
		
		// Verify that the JSON string was generated
		if ( jsonString == null ) {
			logger.error("The form structure table JSON table did not generate correctly.");
			strutsResult = BaseAction.ERROR;
		}
		
		return strutsResult;
	}
	
	private List<CtdbLookup> getFormStatusList(boolean includeActive) {
		
		List<CtdbLookup> formStatus = null;
		try {
			LookupManager lookupMgr = new LookupManager();
			formStatus = lookupMgr.getLookups(LookupType.FORM_STATUS, new LookupResultControl());
			formStatus.remove(new CtdbLookup(2, "Checked Out"));
			formStatus.remove(new CtdbLookup(4, "In Progress"));
			formStatus.remove(new CtdbLookup(5, "External"));
			
			if (!includeActive) {
				formStatus.remove(new CtdbLookup(3, "Active"));
			}
		}
		catch (CtdbException ce) {
			logger.error("Database error occurred while getting a listing of form status.", ce);
		} 		
		return formStatus;
	}


	private List<CtdbLookup> getInProgressFormStatusList() {
		List<CtdbLookup> formStatus = null;
		
		try {
			LookupManager lookupMgr = new LookupManager();
			formStatus = lookupMgr.getLookups(LookupType.FORM_STATUS, new LookupResultControl());
			formStatus.remove(new CtdbLookup(1, "Inactive"));
			formStatus.remove(new CtdbLookup(2, "Checked Out"));
			formStatus.remove(new CtdbLookup(3, "Active"));
			formStatus.remove(new CtdbLookup(5, "External"));
		}
		catch (CtdbException ce) {
			logger.error("Database error occurred while getting the status list for in progress forms.", ce);
		}
		
		return formStatus;
	}

	/**
	 * Gets the imported form from a file.
	 * 
	 * @param filename - The name of the imported form file
	 * @return The imported form html
	 * @throws CtdbException 
	 */
	private String getImportedHtml(String filename) throws CtdbException {
		StringBuffer sb = new StringBuffer();
		BufferedReader bf = null;
		
		try {
			String formFileUploadPath = SysPropUtil.getProperty("app.formfilepath");
			String filePath = request.getServletContext().getRealPath(formFileUploadPath);

			if (!filePath.endsWith(new Character(File.separatorChar).toString())) {
				filePath += File.separatorChar;
			}
			
			filePath += filename;
			
			FileInputStream in = new FileInputStream(filePath);
			bf = new BufferedReader(new InputStreamReader(in));

			String readLine;
			readLine = bf.readLine();
			while (readLine != null) {
				sb.append(readLine);
				sb.append("\r\n");
				readLine = bf.readLine();
			}
			return sb.toString();
		}
		catch (Exception e) {
			throw new CtdbException("Unable to read from the imported form file: " + e.getMessage(), e);
		}
		finally {
			try {
				bf.close();
			}
			catch (IOException e) {
				logger.error("Could not close BufferedReader for the file.", e);
			}
		}
	}

	// added by Ching Heng for data structure
	private JSONObject getDataStructJson(String dsName, String dsVersion) 
			throws CtdbException, UnknownHostException, NoRouteToHostException, WebApplicationException, RuntimeException {
		JSONObject dataStructObj = new JSONObject();
		List<JSONObject> dataElementList = new ArrayList<JSONObject>();
		List<JSONObject> repeatableGroupList = new ArrayList<JSONObject>();
		JSONArray repeatableGroupNamesArr = new JSONArray();
		List<JSONObject> allGroupsList = new ArrayList<JSONObject>();
		FormDataStructureUtility fsUtil = new FormDataStructureUtility();

		try {
			FormStructure choosedDataStructure = fsUtil.getDataStructureFromWebService(dsName,request);
			boolean doesFSContainDeprecatedOrRetiredDEs = fsUtil.doesFSContainDeprecatedOrRetiredDEs(choosedDataStructure);
			Set<RepeatableGroup> rgSet = choosedDataStructure.getRepeatableGroups();
			Map<String,DataElement> dataElementsMap =  choosedDataStructure.getDataElements();
			
			// need to sort the repeatable groups
			class RGComparator implements Comparator<RepeatableGroup> {
				@Override
				public int compare(RepeatableGroup rg1, RepeatableGroup rg2) {
					return rg1.getPosition().compareTo(rg2.getPosition());
				}
			}
			class MapElementComparator implements Comparator<MapElement> {
				@Override
				public int compare(MapElement vr1, MapElement vr2) {
					return vr1.getPosition().compareTo(vr2.getPosition());
				}
			}
			ArrayList<RepeatableGroup> rgList = new ArrayList<RepeatableGroup>(rgSet);
			try {
				Collections.sort(rgList, new RGComparator());
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			for (RepeatableGroup rg : rgList) {
				String rgName = rg.getName();
				RepeatableType rt = rg.getType();
				int threshold = rg.getThreshold();
				boolean isRepeatable = false;
				
				if (rt == RepeatableType.EXACTLY && threshold != 1) {
					ProformsRepeatableGroup PRG = wrapRepeatableGroup(rgName,
							threshold, ProformsRepeatableGroup.REPEATABLE_TYPE_EXACTLY);
					JSONObject repeatableGroupjson = new JSONObject(PRG);
					repeatableGroupList.add(repeatableGroupjson);
					allGroupsList.add(repeatableGroupjson);
					repeatableGroupNamesArr.put(rgName);
					isRepeatable = true;
				}
				else if (rt == RepeatableType.LESSTHAN && threshold != 1) {
					ProformsRepeatableGroup PRG = wrapRepeatableGroup(rgName,
							threshold, ProformsRepeatableGroup.REPEATABLE_TYPE_LESS_THAN);
					JSONObject repeatableGroupjson = new JSONObject(PRG);
					repeatableGroupList.add(repeatableGroupjson);
					allGroupsList.add(repeatableGroupjson);
					repeatableGroupNamesArr.put(rgName);
					isRepeatable = true;
				}
				else if (rt == RepeatableType.MORETHAN) {
					ProformsRepeatableGroup PRG = wrapRepeatableGroup(rgName, threshold, ProformsRepeatableGroup.REPEATABLE_TYPE_MORE_THAN);
					JSONObject repeatableGroupjson = new JSONObject(PRG);
					repeatableGroupList.add(repeatableGroupjson);
					allGroupsList.add(repeatableGroupjson);
					repeatableGroupNamesArr.put(rgName);					
					isRepeatable = true;
				}
				else {
					ProformsRepeatableGroup PRG;
					JSONObject repeatableGroupjson = new JSONObject();
					if (rt == RepeatableType.EXACTLY) {
						PRG = wrapRepeatableGroup(rgName, threshold, ProformsRepeatableGroup.REPEATABLE_TYPE_EXACTLY);
						repeatableGroupjson = new JSONObject(PRG);
					}
					else if (rt == RepeatableType.LESSTHAN) {
						PRG = wrapRepeatableGroup(rgName, threshold, ProformsRepeatableGroup.REPEATABLE_TYPE_LESS_THAN);
						repeatableGroupjson = new JSONObject(PRG);
					}
					else if (rt == RepeatableType.MORETHAN) {
						PRG = wrapRepeatableGroup(rgName, threshold, ProformsRepeatableGroup.REPEATABLE_TYPE_MORE_THAN);
						repeatableGroupjson = new JSONObject(PRG);
					}
					allGroupsList.add(repeatableGroupjson);					
					isRepeatable = false;
				}

				// translate the set of map elements into a list so it can be sorted.  This ONLY sorts the questions
				// NOT the permissable values.
				List<MapElement> mapElements = new ArrayList<MapElement>();
				mapElements.addAll(rg.getDataElements());
				
				try {
					Collections.sort(mapElements, new MapElementComparator());
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				
				for (MapElement me : mapElements) {
					DataElement de = dataElementsMap.get(me.getStructuralDataElement().getNameAndVersion());
					
					ProformsDataElement DE = wrapDataElement(de,rgName,isRepeatable,me.getPosition());
					JSONObject dataElementJson = new JSONObject(DE);
					dataElementJson.put("requiredType", me.getRequiredType().name());
					dataElementList.add(dataElementJson);
				}
			}

			dataStructObj.put("dataStructName", dsName);
			dataStructObj.put("dataStructVersion", dsVersion);
			dataStructObj.put("dataElements", dataElementList);
			dataStructObj.put("repeatableGroupNames", repeatableGroupNamesArr);
			dataStructObj.put("repeatableGroupList", repeatableGroupList);
			dataStructObj.put("allGroupsList", allGroupsList);
			dataStructObj.put("doesFSContainDeprecatedOrRetiredDEs", doesFSContainDeprecatedOrRetiredDEs);
			
		} catch (Exception e) {
			logger.error("Error occurred while constructing the data structure JSON.", e);
			return null;
		}
		
		return dataStructObj;
	}
	
	private ProformsRepeatableGroup wrapRepeatableGroup(String name, int threshold, String type) {
		ProformsRepeatableGroup RG = new ProformsRepeatableGroup();
		RG.setName(name);
		RG.setThreshold(threshold);
		RG.setType(type);
		return RG;
	}

	private ProformsDataElement wrapDataElement(DataElement de, String rgName, boolean isGroupRepeatable, int position) {
		ProformsDataElement DE = new ProformsDataElement();
		
		DE.setName(rgName + "." + de.getName());
		DE.setTitle(de.getTitle());
		DE.setGroupRepeatable(new Boolean(isGroupRepeatable));
		DE.setType(de.getType());
		DE.setShortDescription(de.getShortDescription());
		DE.setSuggestedQuestion(de.getSuggestedQuestion());
		DE.setRestrictions(de.getRestrictions());
		DE.setValueRangeList(de.getValueRangeList());
		DE.setSize(de.getSize());
		if(de.getMaximumValue() == null) {
			DE.setMaximumValue(new BigDecimal(-99999));
		}else {
			DE.setMaximumValue(de.getMaximumValue());
		}
		
		if(de.getMinimumValue() == null) {
			DE.setMinimumValue(new BigDecimal(-99999));
		}else {
			DE.setMinimumValue(de.getMinimumValue());
		}
		
		DE.setOrder(position);
		
		return DE;
	}

	private List<CtdbLookup> getNonPatientFormTypes(LookupManager lookUp)  throws Exception {
		List<CtdbLookup> myNonPatientFormTypes = new ArrayList<CtdbLookup>();
		
		for (CtdbLookup fType : lookUp.getLookups(LookupType.FORMTYPES)) {
			int fid = fType.getId();
			
			if (fid == 12 || fid == 14 || fid == 15) { // skip all others
				CtdbLookup npfType = new CtdbLookup(fid, fType.getShortName(), fType.getLongName());
				myNonPatientFormTypes.add(npfType);
			}
		}
		
		return myNonPatientFormTypes;
	}
	
	public FormForm getFormForm() {
		return formForm;
	}
	
	public void setFormForm(FormForm formForm) {
		this.formForm = formForm;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getAction() {
		return action;
	}
	
	public void setAction(String action) {
		this.action = action;
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
