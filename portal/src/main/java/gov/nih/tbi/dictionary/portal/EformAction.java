package gov.nih.tbi.dictionary.portal;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.jcraft.jsch.JSchException;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.common.util.ProformsWsProvider;
import gov.nih.tbi.commons.model.AnswerType;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.QuestionType;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.service.BtrisMappingManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.service.WebServiceManager;
import gov.nih.tbi.commons.util.EformFormViewXmlUtil;
import gov.nih.tbi.commons.util.FormBuilderQuestionJSONToEform;
import gov.nih.tbi.commons.util.FormBuilderSearchJSONUtils;
import gov.nih.tbi.commons.util.FormStructureJSONUtil;
import gov.nih.tbi.commons.util.SectionJsonToSectionSet;
import gov.nih.tbi.dictionary.model.EformPfCategory;
import gov.nih.tbi.dictionary.model.FormStructureFacet;
import gov.nih.tbi.dictionary.model.hibernate.BtrisMapping;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;
import gov.nih.tbi.dictionary.model.hibernate.eform.CalculationQuestion;
import gov.nih.tbi.dictionary.model.hibernate.eform.CalculationQuestionPk;
import gov.nih.tbi.dictionary.model.hibernate.eform.CountQuestion;
import gov.nih.tbi.dictionary.model.hibernate.eform.CountQuestionPk;
import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;
import gov.nih.tbi.dictionary.model.hibernate.eform.EmailTrigger;
import gov.nih.tbi.dictionary.model.hibernate.eform.EmailTriggerValue;
import gov.nih.tbi.dictionary.model.hibernate.eform.Question;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAnswerOption;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAttribute;
import gov.nih.tbi.dictionary.model.hibernate.eform.Section;
import gov.nih.tbi.dictionary.model.hibernate.eform.SectionQuestion;
import gov.nih.tbi.dictionary.model.hibernate.eform.SkipRuleQuestion;
import gov.nih.tbi.dictionary.model.hibernate.eform.SkipRuleQuestionPk;
import gov.nih.tbi.dictionary.model.hibernate.eform.VisualScale;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;
import gov.nih.tbi.dictionary.utils.XslTransformer;
import gov.nih.tbi.portal.PortalUtils;

/**
 * Class to handle eForm Creation/Edit user actions
 * 
 * @author khanaly
 *
 */
public class EformAction extends BaseEformAction {

	private static final long serialVersionUID = -790220915927063189L;

	static Logger logger = Logger.getLogger(EformAction.class);

	@Autowired
	WebServiceManager webServiceManager;

	@Autowired
	BtrisMappingManager btrisMappingManager;

	// Action to JSP
	private String jsonString = "{\"aaData\":[], \"aoColumns\":[]}";
	private String jsonFsDe = "";
	private String questionJson;
	// JSP to Action
	private String sectionsJSON;
	private String questionsJSON;

	private BasicEform basicEform;

	// passed from UI to action for edit/delete action
	private Long eformId;

	// copy mode
	// private Boolean copyMode = true;

	// Edit Eform params
	private String formMode;
	// private String formStructure;
	private String sectionsJSONArrJSONString;
	private String questionsJSONArrJSONString;

	// delete on cancel of eFrom
	static List<Long> quesitonIdList = new ArrayList<Long>();

	// validate shortName from UI
	private String shortName;
	private String validationJson;
	private String eFormAction;

	// JSON data back for edit eFrom rendering
	private String json;

	private long publicationId;

	// Check wether it's eform moudle or sth else for d
	public boolean eFormModule = false;

	private String reason;

	private String ownerName;
	
	@Autowired
	protected ProformsWsProvider proformsWsProvider;

	public String view() throws UserPermissionException, UnsupportedEncodingException, MalformedURLException,
			UserAccessDeniedException {
		sessionEform.clear();

		if (Long.valueOf(getEformId()) != null) {
			// will need to add permissions checks here
			BasicEform eform = eformManager.getBasicEform(eformId);
			setBasicEform(eform);
			sessionEform.setBasicEform(eform);

			if (eform == null) {
				sessionEform.clear();
				throw new NullPointerException("The dao search came up empty. Try again. Eform ID: " + getEformId());
			}

			if (getPublicArea() == null || !getPublicArea()) {
				Account account =
						webServiceManager.getEntityOwnerAccountRestful(getAccount(), eform.getId(), EntityType.EFORM);

				String ownerName = account.getUser().getFullName();
				setOwnerName(ownerName);
			}


			if (getIsDictionaryAdmin() || eform.getStatus().equals(StatusType.PUBLISHED)
					|| (!eform.getStatus().equals(StatusType.PUBLISHED) && getHasReadPermission())) {
				return PortalConstants.ACTION_BASIC_VIEW;
			} else {
				sessionEform.clear();
				throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
			}

		} else {
			sessionEform.clear();
			logger.error("There was a request to view an eform and the eform ID was null.");
			throw new NullPointerException("There was a request to view an eform and the eform ID was null.");
		}
	}

	public String viewFormDetail() throws Exception {

		if (Long.valueOf(getEformId()) != null) {
			Eform viewEform = eformManager.getEformNoLazyLoad(eformId);
			sessionEform.setEform(viewEform);
			sessionEform.setSessionEformUserPermissionType(null);
			if (viewEform == null) {
				sessionEform.clear();
				throw new NullPointerException("The dao search came up empty. Try again. Eform ID: " + getEformId());
			}

			// throws a permission exception if not satisfied
			if (getIsDictionaryAdmin() || viewEform.getStatus().equals(StatusType.PUBLISHED)
					|| (!viewEform.getStatus().equals(StatusType.PUBLISHED) && getHasReadPermission())) {

				/*
				 * if (getRequest().getParameter("displayQids") != null) {
				 * viewEform.setDisplayQids(getRequest().getParameter("displayQids") != null); }
				 */

				String formDetail = null;
				if (viewEform.getUpdatedDate() != null) {
					// formDetail = getImportedHtml(form.getImportFileName());
				} else {
					String xsl = "/WEB-INF/xsl/formDisplay.xsl"; // need to move
																 // this out of
																 // the class
					if (("pda").equals(getRequest().getParameter("viewOption"))) {
						xsl = "/WEB-INF/xsl/formPdaDisplay.xsl"; // need to move
																 // this out of
																 // the class
					}
					/*
					 * if (viewEform.isTabDisplay()) { xsl = "/WEB-INF/xsl/formTabDisplay.xsl"; //need to move this out
					 * of the class }
					 */

					EformFormViewXmlUtil xslView = new EformFormViewXmlUtil(viewEform);
					InputStream stream = getRequest().getServletContext().getResourceAsStream(xsl);

					formDetail = XslTransformer.transform(xslView.convertEformToFormViewXML(), stream,
							modulesConstants.getGlobalXslParameterMap());
				}

				String source = getRequest().getParameter("source");
				if (source != null
						&& (source.equals("sectionhome") || source.equals("formAudit") || source.equals("adminForm"))) {
					getRequest().setAttribute("source", "sectionhome");
				}

				getRequest().setAttribute("formdetail", formDetail);
				getRequest().setAttribute("formname", viewEform.getTitle());
				if (getRequest().getParameter("showVersion") != null) {
					getRequest().setAttribute("showVersion", "true");
					getRequest().setAttribute("formVersion", getRequest().getParameter("formVersion"));
				}

				return SUCCESS;
			} else {
				sessionEform.clear();
				throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
			}
		} else {
			sessionEform.clear();
			logger.error("There was a request to view an eform and the eform ID was null.");
			throw new NullPointerException("There was a request to view an eform and the eform ID was null.");
		}
	}

	public String createEform() {
		sessionEform.clear();
		seteFormModule(true);
		setFormMode(PortalConstants.ACTION_CREATE);
		this.getSessionEform().setFormMode(this.getFormMode());
		Eform newEform = new Eform();
		newEform.setStatus(StatusType.DRAFT);
		newEform.setCreateBy(getAccount().getUserName());
		newEform.setId(-1l);
		newEform.setTitle(getParamValueForParamKeyFromRequest("formForm.name"));
		newEform.setDescription(getParamValueForParamKeyFromRequest("formForm.description"));
		newEform.setFormStructureShortName(getParamValueForParamKeyFromRequest("formForm.dataStructureName"));
		newEform.setShortName(getParamValueForParamKeyFromRequest("formForm.shortName"));
		String allowMultipleCollectionInstances =
				this.getParamValueForParamKeyFromRequest("formForm.allowMultipleCollectionInstances");
		newEform.setAllowMultipleCollectionInstances(Boolean.valueOf(allowMultipleCollectionInstances));
		newEform.setSectionNameColor(getParamValueForParamKeyFromRequest("formForm.sectioncolor"));
		newEform.setSectionNameFont(getParamValueForParamKeyFromRequest("formForm.sectionfont"));
		newEform.setEnableDataSpring(getParamValueForParamKeyFromRequest("formForm.isDataSpring"));
		String sectionboarder = getParamValueForParamKeyFromRequest("formForm.sectionborder");
		if (sectionboarder != null) {
			newEform.setSectionBorder(Boolean.valueOf(getParamValueForParamKeyFromRequest("formForm.sectionborder")));
		} else {
			newEform.setSectionBorder(false);
		}
		String cellPadding = getParamValueForParamKeyFromRequest("formForm.cellpadding");
		if (cellPadding != null) {
			newEform.setCellPadding(Integer.valueOf(getParamValueForParamKeyFromRequest("formForm.cellpadding")));
		} else {
			newEform.setCellPadding(2);
		}
		String formcolor = getParamValueForParamKeyFromRequest("formForm.formcolor");
		if (formcolor != null) {
			newEform.setFormNameColor(getParamValueForParamKeyFromRequest("formForm.formcolor"));
		}
		String fontSize = getParamValueForParamKeyFromRequest("formForm.fontSize");
		if (fontSize != null) {
			newEform.setFontSize(Integer.valueOf(getParamValueForParamKeyFromRequest("formForm.fontSize")));
		} else {
			newEform.setFontSize(10);
		}
		newEform.setFormNameFont(getParamValueForParamKeyFromRequest("formForm.formfont"));
		newEform.setFormBorder(Boolean.valueOf(getParamValueForParamKeyFromRequest("formForm.formborder")));
		newEform.setFooter(getParamValueForParamKeyFromRequest("formForm.formFooter"));
		newEform.setHeader(getParamValueForParamKeyFromRequest("formForm.formHeader"));
		newEform.setCreateDate(new Date());
		newEform.setOrderVal(7);
		newEform.setCreateBy(getAccount().getUserName());
		newEform.setPfCategory(EformPfCategory.NORMAL);
		createNewOwnership();

		sessionEform.setEform(newEform);
		return PortalConstants.ACTION_SUCCESS;
	}

	/**
	 * Method to get the list of published FS
	 * 
	 * @return JSON string
	 */
	public String getPublishedFS() {
		setFormMode(PortalConstants.ACTION_CREATE);
		this.getSessionEform().setFormMode(this.getFormMode());
		logger.info("EformAction->getPublishedFS()");

		Map<FormStructureFacet, Set<String>> selectedFacets = new HashMap<FormStructureFacet, Set<String>>();

		// set the search to only look for published FS
		Set<String> statusSet = new HashSet<String>();
		statusSet.add(StatusType.PUBLISHED.getType());
		selectedFacets.put(FormStructureFacet.STATUS, statusSet);

		List<SemanticFormStructure> pFS = dictionaryService.semanticFormStructureSearch(getAccount(), selectedFacets,
				null, Boolean.FALSE, Boolean.FALSE, null,
				PortalUtils.getProxyTicket(modulesConstants.getModulesAccountURL(getDiseaseId())));

		if (pFS == null || pFS.isEmpty()) {
			sessionEform.clear();
			throw new NullPointerException();
		}

		FormBuilderSearchJSONUtils tableJSON = new FormBuilderSearchJSONUtils(pFS,
				getText("form.fsTable.columnTitle.shortName"), getText("form.fsTable.columnTitle.version"),
				getText("form.fsTable.columnTitle.description"), getText("form.fsTable.columnTitle.title"));

		jsonString = tableJSON.convertFormStructureToJSON();
		return PortalConstants.ACTION_LIST;
	}

	/**
	 * Method to get the details of selected FS
	 * 
	 * @return JSON string data
	 */
	public String getSelectedFS() {
		setFormMode(PortalConstants.ACTION_CREATE);
		this.getSessionEform().setFormMode(this.getFormMode());

		String selectedFS = this.getParamValueForParamKeyFromRequest("formForm.dataStructureName");
		String allowMultipleCollectionInstances =
				this.getParamValueForParamKeyFromRequest("formForm.allowMultipleCollectionInstances");
		logger.info("selectedFS name:\t" + getParamValueForParamKeyFromRequest("formForm.dataStructureName"));

		Eform createNewEform = new Eform();
		createNewEform.setTitle(getParamValueForParamKeyFromRequest("formForm.name"));
		createNewEform.setDescription(getParamValueForParamKeyFromRequest("formForm.description"));
		createNewEform.setFormStructureShortName(getParamValueForParamKeyFromRequest("formForm.dataStructureName"));
		createNewEform.setShortName(getParamValueForParamKeyFromRequest("formForm.shortName"));
		createNewEform.setStatus(StatusType.DRAFT);
		createNewEform.setAllowMultipleCollectionInstances(Boolean.valueOf(allowMultipleCollectionInstances));
		createNewEform.setSectionNameColor(getParamValueForParamKeyFromRequest("formForm.sectioncolor"));
		createNewEform.setSectionNameFont(getParamValueForParamKeyFromRequest("formForm.sectionfont"));
		createNewEform.setEnableDataSpring(getParamValueForParamKeyFromRequest("formForm.isDataSpring"));
		createNewEform.setSectionBorder(Boolean.valueOf(getParamValueForParamKeyFromRequest("formForm.sectionborder")));
		createNewEform.setCellPadding(Integer.valueOf(getParamValueForParamKeyFromRequest("formForm.cellpadding")));
		createNewEform.setFormNameColor(getParamValueForParamKeyFromRequest("formForm.formcolor"));
		createNewEform.setFontSize(Integer.valueOf(getParamValueForParamKeyFromRequest("formForm.fontSize")));
		createNewEform.setFormNameFont(getParamValueForParamKeyFromRequest("formForm.formfont"));
		createNewEform.setFormBorder(Boolean.valueOf(getParamValueForParamKeyFromRequest("formForm.formborder")));
		createNewEform.setFooter(getParamValueForParamKeyFromRequest("formForm.formFooter"));
		createNewEform.setHeader(getParamValueForParamKeyFromRequest("formForm.formHeader"));
		createNewEform.setCreateDate(new Date());
		createNewEform.setOrderVal(7);
		createNewEform.setCreateBy(getAccount().getUserName());
		Long pfCatId = Long.valueOf(getParamValueForParamKeyFromRequest("formForm.pfCategory"));
		EformPfCategory pfCat = EformPfCategory.getById(pfCatId);
		createNewEform.setPfCategory(pfCat);
		FormStructureJSONUtil conversionUtil =
				new FormStructureJSONUtil(dictionaryManager.getDataStructureLatestVersion(selectedFS));
		setJsonFsDe(conversionUtil.getDataStructJson().toString());

		sessionEform.setEform(createNewEform);

		return PortalConstants.ACTION_VIEW_DETAILS;
	}

	/**
	 * Method to save Question object to pass Question Id to UI for further processing
	 * 
	 * @return
	 */
	public String saveQuestionAjax() {
		// setFormMode(PortalConstants.ACTION_CREATE);
		// getSessionEform().setFormMode(this.getFormMode());

		Question q = new Question();
		q.setText(getParamValueForParamKeyFromRequest("questionForm.text"));
		q.setName(getParamValueForParamKeyFromRequest("questionForm.questionName"));
		q.setDescriptionUp(getParamValueForParamKeyFromRequest("questionForm.descriptionUp"));
		q.setDescriptionDown(getParamValueForParamKeyFromRequest("questionForm.descriptionDown"));
		q.setDefaultValue(getParamValueForParamKeyFromRequest("questionForm.defaultValue"));


		Question savedQ = eformManager.saveQuestion(q);
		// Send persisted question back to Question.js for further processing
		JSONObject questionJsonObj = new JSONObject();
		questionJsonObj.put("questionId", savedQ.getId());
		questionJson = questionJsonObj.toString();
		if (!quesitonIdList.contains(savedQ.getId())) {
			quesitonIdList.add(savedQ.getId());
			sessionEform.setQuesitonList(quesitonIdList);
		}
		logger.info("questionJson\n/n" + questionJson);
		return PortalConstants.ACTION_ADVANCED;
	}

	/**
	 * Method to save eForm
	 * 
	 * @throws UnsupportedEncodingException
	 * @throws JSONException
	 * @throws UserPermissionException
	 * @throws MalformedURLException
	 */
	public String save() throws JSONException, UnsupportedEncodingException, UserPermissionException,
			MalformedURLException, UserAccessDeniedException {

		if (sessionEform.getEform().getId() != null && sessionEform.getEform().getId() > 1) {
			if (!getIsDictionaryAdmin() && !getHasWritePermission()) {
				sessionEform.clear();
				throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
			}
		} else {
			sessionEform.getEform().setId(null);
		}

		if (sessionEform.getEform().getDescription() == null) {
			sessionEform.getEform().setDescription("");
		}

		// Per REQ-642 set the eform back to draft if it's awaiting publication and the user made changes.
		if (sessionEform.getEform().getStatus().equals(StatusType.AWAITING_PUBLICATION)) {
			sessionEform.getEform().setStatus(StatusType.DRAFT);
		}

		logger.debug("sectionsJSON " + sectionsJSON);
		logger.debug("questionsJSON " + questionsJSON);

		SectionJsonToSectionSet sectionJsonUtil =
				new SectionJsonToSectionSet(new JSONArray(URLDecoder.decode(sectionsJSON, "UTF-8")));
		HashMap<Long, Section> sectionMap = sectionJsonUtil.parseSectionJSONToSectionMap();

		for (Entry<Long, Section> section : sectionMap.entrySet()) {
			if (section.getKey() <= 0l) {
				Section sectionWithDBId = eformManager.saveSection(section.getValue());
				section.setValue(sectionWithDBId);
			}
		}

		// loop over the section list to add the real parent ID based off the
		// psuedo id
		if (!sectionMap.isEmpty()) {
			for (Entry<Long, Section> sectionEntry : sectionMap.entrySet()) {
				Section section = sectionEntry.getValue();
				if (section.getRepeatedSectionParent() != null) {
					section.setRepeatedSectionParent(sectionMap.get(section.getRepeatedSectionParent()).getId());
				}
			}
		}

		FormBuilderQuestionJSONToEform formBuilder = new FormBuilderQuestionJSONToEform(sessionEform.getEform(),
				sectionMap, new JSONArray(questionsJSON), this.getSessionEform().getFormMode());

		HashMap<Long, BtrisMapping> questionBtrisMappingMap = this.generateQuestionBtrisMappingMap();
		if (!questionBtrisMappingMap.isEmpty()) {
			formBuilder.setQuestionBtrisMappingMap(questionBtrisMappingMap);
		}

		sessionEform.setEform(formBuilder.parseQuestionJSONToEform());


		Eform eForm = eformManager.saveEform(sessionEform.getEform());
		sessionEform.setEform(eForm);

		try {
			saveEformPermissions();
		} catch (Exception e) {
			sessionEform.clear();
			logger.error("We were not able to remove or save the permissions associated with the eform " + eForm.getId()
					+ " " + eForm.getTitle(), e);
		}

		this.basicEform = eformManager.getBasicEform(eForm.getId());
		sessionEform.setBasicEform(this.basicEform);

		if (getPublicArea() == null || !getPublicArea()) {
			Account account =
					webServiceManager.getEntityOwnerAccountRestful(getAccount(), basicEform.getId(), EntityType.EFORM);

			String ownerName = account.getUser().getFullName();
			setOwnerName(ownerName);
		}
		quesitonIdList = new ArrayList<Long>();
		return PortalConstants.ACTION_BASIC_VIEW;
	}

	private void saveEformPermissions()
			throws UserPermissionException, MalformedURLException, HttpException, IOException {
		List<EntityMap> entitiesToRemove = getSessionEform().getRemovedMapList();
		if (entitiesToRemove != null && !entitiesToRemove.isEmpty()) {
			webServiceManager.unregisterEntityListRestful(getAccount(), entitiesToRemove);
		}

		List<EntityMap> entitiesToAdd = getSessionEform().getEntityMapList();
		if (entitiesToAdd != null && !entitiesToAdd.isEmpty()) {
			for (EntityMap mapPermission : entitiesToAdd) {
				if (mapPermission.getEntityId() == null || mapPermission.getEntityId() == -1l) {
					mapPermission.setEntityId(sessionEform.getEform().getId());
				}
			}
			String[] proxyTicketArr = PortalUtils.getMultipleProxyTickets(
					modulesConstants.getModulesAccountURL(getDiseaseId()), entitiesToAdd.size());
			webServiceManager.registerEntityListRestful(getAccount(), entitiesToAdd, proxyTicketArr);
		}
	}

	/**
	 * Method to handle Eform edit
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws UserPermissionException
	 */
	public String editEform() throws UnsupportedEncodingException, UserPermissionException, UserAccessDeniedException {
		if (!sessionEform.getCopyMode()) {
			sessionEform.clear();
		}
		seteFormModule(true);

		Eform eFormFromDb = eformManager.getEformNoLazyLoad(eformId);
		sessionEform.setEform(eFormFromDb);
		setFormMode(PortalConstants.FORMTYPE_EDIT);
		this.sessionEform.setFormMode(this.getFormMode());

		if (!getIsDictionaryAdmin() && !getHasWritePermission()) {
			sessionEform.clear();
			throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
		} else if (sessionEform.getEform().getIsLegacy() != null && sessionEform.getEform().getIsLegacy()) {
			sessionEform.clear();
			throw new UserPermissionException(getAccount().getUserName() + " was trying to copy legacy eform "
					+ Long.toString(sessionEform.getEform().getId()));
		}

		return PortalConstants.ACTION_EDIT;
	}

	/**
	 * Method to pass JSON data back to JSP on edit mode
	 * 
	 * @return eFromJsonObj
	 */
	public String createEformJSONForEdit() {
		this.getSessionEform().setCopyMode(false);
		setFormMode(PortalConstants.FORMTYPE_EDIT);


		this.sessionEform.setFormMode(this.getFormMode());
		this.getUpdateEformValues();


		JSONArray sectionsJSONArr = new JSONArray();
		JSONArray questionsJSONArr = new JSONArray();
		FormStructure assocaitedFormStructure =
				dictionaryManager.getDataStructureLatestVersion(sessionEform.getEform().getFormStructureShortName());
		FormStructureJSONUtil conversionUtil = new FormStructureJSONUtil(assocaitedFormStructure);
		JSONObject dataStructObj = conversionUtil.getDataStructJson();
		Set<Section> secList = sessionEform.getEform().getSectionList();
		for (Section sec : secList) {
			RepeatableGroup rg = assocaitedFormStructure.getRepeatableGroupByName(sec.getGroupName());
			sectionsJSONArr.put(this.createSectionJSON(sec, rg));
			Set<SectionQuestion> secQuectionList = sec.getSectionQuestion();
			for (SectionQuestion sq : secQuectionList) {

				// question json
				JSONObject quesJSON = new JSONObject();
				Question q = sq.getQuestion();
				QuestionAttribute qa = q.getQuestionAttribute();
				this.questionJSONforEdit(quesJSON, q, sec, sq, qa);

				// attributeObject part of questionJson
				JSONObject attributeObjJson = new JSONObject();
				// QuestionAnswerOption to set redio/select/redio/multiselect to
				// UI
				JSONArray questionOptionsObjectArray = new JSONArray();
				attributeObjJson.put("answerType", qa.getAnswerType().getValue());
				Set<QuestionAnswerOption> qaOptionsSet = q.getQuestionAnswerOption();
				List<QuestionAnswerOption> questionAnswerOptionsList = new ArrayList(qaOptionsSet);

				if (qaOptionsSet != null) {
					Collections.sort(questionAnswerOptionsList, new Comparator<QuestionAnswerOption>() {

						@Override
						public int compare(QuestionAnswerOption qao1, QuestionAnswerOption qao2) {
							if (qao1.getOrderVal() != null || qao2.getOrderVal() != null) {
								return qao1.getOrderVal().compareTo(qao2.getOrderVal());
							} else {
								return -1;
							}
						}

					});
				}

				for (QuestionAnswerOption qaOpt : questionAnswerOptionsList) {
					qaOpt.getQuestionAnswerDataType();
					String opt = qaOpt.getDisplay();
					String score = "";
					String itemResponseOid = "";
					String elementOid = "";
					if (qaOpt.getScore() != Integer.MIN_VALUE) {
						score = String.valueOf(qaOpt.getScore());
					}

					JSONObject questionOption = new JSONObject();
					questionOption.put("option", opt);
					questionOption.put("score", score);
					questionOption.put("submittedValue", qaOpt.getSubmittedValue());
					// added by Ching-Heng
					if (qaOpt.getItemResponseOid() != null) {
						itemResponseOid = qaOpt.getItemResponseOid();
					}
					questionOption.put("itemResponseOid", itemResponseOid);
					if (qaOpt.getElementOid() != null) {
						elementOid = qaOpt.getElementOid();
					}
					questionOption.put("elementOid", elementOid);

					questionOption.put("orderVal", qaOpt.getOrderVal());

					questionOptionsObjectArray.put(questionOption);
				}
				quesJSON.put("questionOptionsObjectArray", questionOptionsObjectArray);

				QuestionType questionType = q.getType();

				if (questionType != null) {
					attributeObjJson.put("qType", q.getType().getValue());
				}

				this.attributeJSONforEdit(sq, qa, attributeObjJson);

				// conversion factor
				if (qa.getHasConversionFactor()) {
					attributeObjJson.put("hasUnitConversionFactor", qa.getHasConversionFactor());
					attributeObjJson.put("unitConversionFactor", qa.getConversionFactor());
				} else {
					attributeObjJson.put("hasUnitConversionFactor", qa.getHasConversionFactor());
					attributeObjJson.put("unitConversionFactor", "");
				}



				// range data from question attributes object that UI needs
				attributeObjJson.put("rangeOperator", qa.getRangeOperator());
				attributeObjJson.put("rangeValue1", qa.getRangeValue1());
				attributeObjJson.put("rangeValue2", qa.getRangeValue2());

				// set clac rule
				this.calcRuleJSONForEdit(attributeObjJson, qa, sq);
				// set skip rule
				this.skipRuleJSONforEdit(attributeObjJson, qa, sq);
				// set email trigger
				this.emailTriggerJSONforEdit(qa, attributeObjJson, quesJSON);
				// set visual scale
				JSONObject visualScaleEditJSONObj = this.visualScaleEdit(q);
				// set count rules
				this.countJSONForEdit(attributeObjJson, qa, sq);
				quesJSON.put("visualScaleInfo", visualScaleEditJSONObj.toString());
				// set btris mapping
				this.btrisMappingJSONforEdit(q, attributeObjJson, quesJSON);
				quesJSON.put("attributeObject", attributeObjJson);
				questionsJSONArr.put(quesJSON);
			}
		}

		sectionsJSONArrJSONString = sectionsJSONArr.toString();
		String sectionsJSONArrJSONString = sectionsJSONArr.toString();
		String questionsJSONArrJSONString = questionsJSONArr.toString();

		JSONObject eFromJsonObj = new JSONObject();
		eFromJsonObj.put("dataStructObj", dataStructObj);
		eFromJsonObj.put("sectionsJSONArrJSONString", sectionsJSONArrJSONString);
		eFromJsonObj.put("questionsJSONArrJSONString", questionsJSONArrJSONString);
		this.json = eFromJsonObj.toString();
		return PortalConstants.ACTION_EDIT;
	}

	public String validateEform() throws UserAccessDeniedException {
		this.getResponse().setContentType(MediaType.APPLICATION_JSON);

		if (PortalConstants.EFORM_SAVE.equals(geteFormAction())) {
			// when saving eform, need to validate that PROIMIS eforms with Data Elements prefixed with 'de_' have
			// non-empty OIDs in both fs and eform
			FormStructure fs = dictionaryManager.getDataStructureLatestVersion(sessionEform.getEform().getFormStructureShortName());
			sessionEform.getEform().setIsCAT(fs.isCAT());
			sessionEform.getEform().setCatOid(fs.getCatOid());
			sessionEform.getEform().setMeasurementType(fs.getMeasurementType());
			if (fs.isCAT()) {
				// check fs
				boolean isEmptyOID_FS = false;
				String message = "";

				outer: for (DataElement de : fs.getDataElements().values()) {
					if(de.getName().startsWith(PortalConstants.PROMIS_OID_PREFIX)) {
						Set<ValueRange> valueRangeList = de.getValueRangeList();
						for (ValueRange value : de.getValueRangeList()) {
	                        String itemResponseOid = value.getItemResponseOid();
	                        String elementOid = value.getElementOid();
	                        if(itemResponseOid == null || itemResponseOid.equals("") || elementOid == null || elementOid.equals("")) {
								isEmptyOID_FS = true;
								break outer;
							}
	                    }
					}
				}


				// check eform
				boolean isEmptyOID_EF = false;
				JSONArray qArray = new JSONArray(questionsJSON);
				outer2: for (int i = 0; i < qArray.length(); i++) {
					JSONObject qJson = qArray.getJSONObject(i);
					JSONObject attributeObjJSON = qJson.getJSONObject("attributeObject");
					String groupDataElementName = attributeObjJSON.getString("dataElementName");
					JSONArray options = qJson.getJSONArray("questionOptionsObjectArray");
					String dataElementName = groupDataElementName;

					if(groupDataElementName.contains(".")){
						dataElementName = groupDataElementName.substring(groupDataElementName.indexOf(".") + 1, groupDataElementName.length());
					} 
					if(dataElementName.startsWith(PortalConstants.PROMIS_OID_PREFIX)) {
						if(options != null) {
							for(int j=0;j<options.length();j++) {
								JSONObject option = options.getJSONObject(j);
								String itemResponseOid = option.getString("itemResponseOid");
								String elementOid = option.getString("elementOid");
								if(itemResponseOid == null || itemResponseOid.equals("") || elementOid == null || elementOid.equals("")) {
									isEmptyOID_EF = true;
									break outer2;
								}
							}
						}
					}
				}

				if (isEmptyOID_FS && isEmptyOID_EF) {
					message = PortalConstants.PROMIS_EFORM_EMPTY_OIDS_FS_EF_MSG;
				} else if (isEmptyOID_FS) {
					message = PortalConstants.PROMIS_EFORM_EMPTY_OIDS_FS_MSG;
				} else if (isEmptyOID_EF) {
					message = PortalConstants.PROMIS_EFORM_EMPTY_OIDS_EF_MSG;
				}
				if (isEmptyOID_FS || isEmptyOID_EF) {
					JSONObject jsonObj = new JSONObject();
					jsonObj.put("msgType", message);
					JSONArray promisErrArray = new JSONArray();
					promisErrArray.put(jsonObj);
					validationJson = promisErrArray.toString();
					return SUCCESS;
				}
			}
		} else if (PortalConstants.EFORM_CONTINUE.equals(geteFormAction())) {
			// when doing save and continue on eform, validdate shortname
			//validate if creating for first time or if entered short name is differnt from the session eform one
			JSONArray shortNameArray = new JSONArray();
			if(sessionEform.getEform().getShortName() == null || !sessionEform.getEform().getShortName().equals(getShortName())) {
				List<String> validationErrors = eformManager.validateEformShortName(getShortName());
				if (validationErrors != null && !validationErrors.isEmpty()) {
					for (String errorMessage : validationErrors) {
						JSONObject jsonObj = new JSONObject();
						jsonObj.put("msgType", errorMessage);
						shortNameArray.put(jsonObj);
					}
				}
			}
			
			
			validationJson = shortNameArray.toString();
			return SUCCESS;
		}
		return SUCCESS;


	}

	public String deleteEformQuestions() {
		ArrayList<Question> questionsToDelete = eformManager.getQuestionByQuestionIds(quesitonIdList);

		eformManager.deleteQuestions(questionsToDelete);
		return PortalConstants.ACTION_LIST;
	}
	
	
	/**
	 * Checks to see if there are any data collections by making ws call to proforms
	 * @return
	 * @throws JsonParseException
	 * @throws WebApplicationException
	 * @throws IOException
	 */
	public String checkForCollectionsAndEformsInVT() throws JsonParseException, WebApplicationException, IOException {
		String proformsWsUrl = modulesConstants.getModulesPFURL(getDiseaseId());
		String eformShortName = sessionEform.getBasicEform().getShortName();
		boolean areAnyDataCollectionsOrEformsInVT = false;
		String message = "";
		areAnyDataCollectionsOrEformsInVT = proformsWsProvider.areThereDataCollections(proformsWsUrl, eformShortName);
		if(areAnyDataCollectionsOrEformsInVT == true) {
			message = ServiceConstants.UNABLE_TO_DELETE_EFORM_BC_OF_COLLECTIONS;
		}else {
			areAnyDataCollectionsOrEformsInVT = proformsWsProvider.areThereEformsAttachedToAnyVisitType(proformsWsUrl, eformShortName);
			if(areAnyDataCollectionsOrEformsInVT == true) {
				message = ServiceConstants.UNABLE_TO_DELETE_EFORM_BC_OF_EFORMS_IN_VT;
			}
		}
		
		
		HttpServletResponse response = ServletActionContext.getResponse();
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("areAnyDataCollectionsOrEformsInVT", areAnyDataCollectionsOrEformsInVT);
		jsonObj.put("email", modulesConstants.getModulesOrgEmail(getDiseaseId()));
		jsonObj.put("message", message);
		PrintWriter out = response.getWriter();
		out.print(jsonObj);
		return null;
	}

	public String delete() throws HttpException, IOException, UserPermissionException, UserAccessDeniedException {

		if (!getIsDictionaryAdmin() && !getHasWritePermission()) {
			sessionEform.clear();
			throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
		}
		

		Eform eformToDelete = eformManager.getEformNoLazyLoad(sessionEform.getBasicEform().getId());
		eformManager.deleteEform(eformToDelete);
		removeEformFromStandardGroup();
		unregisterEformEntityPermissions();
		sessionEform.clear();
		return PortalConstants.ACTION_LIST;
	}

	public String editPermissions() throws UserPermissionException, MalformedURLException, UnsupportedEncodingException,
			UserAccessDeniedException {

		if (!getIsDictionaryAdmin() && !getHasAdminPermission()) {
			sessionEform.clear();
			throw new UserPermissionException(ServiceConstants.ADMIN_ACCESS_DENIED);
		}
		sessionEform.clear();

		return PortalConstants.ACTION_PERMISSIONS;
	}

	public String submitPermissions() throws UserPermissionException, MalformedURLException,
			UnsupportedEncodingException, UserAccessDeniedException {

		if (!getIsDictionaryAdmin() && !getHasAdminPermission()) {
			sessionEform.clear();
			throw new UserPermissionException(ServiceConstants.ADMIN_ACCESS_DENIED);
		}

		List<EntityMap> entitiesToRemove = getSessionEform().getRemovedMapList();
		if (entitiesToRemove != null && !entitiesToRemove.isEmpty()) {
			accountManager.unregisterEntity(entitiesToRemove);
		}

		List<EntityMap> entitiesToAdd = getSessionEform().getEntityMapList();
		if (entitiesToAdd != null && !entitiesToAdd.isEmpty()) {
			accountManager.saveUpdateEntity(entitiesToAdd);
		}

		return PortalConstants.ACTION_REDIRECT_TO_VIEW;
	}

	public String approvePublication()
			throws HttpException, IOException, UserPermissionException, UserAccessDeniedException {
		changePublication();

		if (getPublicArea() == null || !getPublicArea()) {
			Account account = webServiceManager.getEntityOwnerAccountRestful(getAccount(),
					sessionEform.getBasicEform().getId(), EntityType.EFORM);

			String ownerName = account.getUser().getFullName();
			setOwnerName(ownerName);
		}

		return PortalConstants.ACTION_BASIC_VIEW;
	}


	public String publicationDecision()
			throws HttpException, IOException, UserPermissionException, UserAccessDeniedException {
		changePublication();

		if (getPublicArea() == null || !getPublicArea()) {
			Account account = webServiceManager.getEntityOwnerAccountRestful(getAccount(),
					sessionEform.getBasicEform().getId(), EntityType.EFORM);

			String ownerName = account.getUser().getFullName();
			setOwnerName(ownerName);
		}

		return PortalConstants.ACTION_BASIC_VIEW;
	}

	/**
	 * Changes the publication state
	 * 
	 * @param publicationId
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws UserPermissionException
	 */
	public String changePublication()
			throws HttpException, IOException, UserPermissionException, UserAccessDeniedException {

		if (!getPublicationIdAsStatusType().equals(StatusType.AWAITING_PUBLICATION) && !getHasAdminPermission()
				&& !getIsDictionaryAdmin()) {
			sessionEform.clear();
			throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
		}

		switch (getPublicationIdAsStatusType()) {
			case DRAFT:
				sessionEform.getBasicEform().setIsShared(Boolean.FALSE);
				sessionEform.getBasicEform().setPublicationDate(null); // set back to null when you revert from archived
				removeEformFromStandardGroup();
				updateEformStatus(getPublicationIdAsStatusType());
				break;
			case AWAITING_PUBLICATION:
				updateEformStatus(getPublicationIdAsStatusType());
				break;
			case PUBLISHED:
				sessionEform.getBasicEform().setPublicationDate(new Date());
				updateEformStatus(getPublicationIdAsStatusType());
				break;
			case ARCHIVED:
				updateEformStatus(getPublicationIdAsStatusType());
				break;
			default:
				break;
		}

		sessionEform.setBasicEform(eformManager.saveBasicEform(sessionEform.getBasicEform()));
		setBasicEform(sessionEform.getBasicEform());
		return PortalConstants.ACTION_PUBLICATION;
	}

	public String standardize() throws HttpException, IOException, UserPermissionException {

		if (!getIsDictionaryAdmin()) {
			sessionEform.clear();
			throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
		}

		BasicEform sessionBasicEform = sessionEform.getBasicEform();
		sessionBasicEform.setIsShared(Boolean.TRUE);
		sessionBasicEform = eformManager.saveBasicEform(sessionBasicEform);
		setBasicEform(sessionBasicEform);
		sessionEform.setBasicEform(sessionBasicEform);
		addEformToSharedGroup();

		if (getPublicArea() == null || !getPublicArea()) {
			Account account = webServiceManager.getEntityOwnerAccountRestful(getAccount(),
					sessionEform.getBasicEform().getId(), EntityType.EFORM);

			String ownerName = account.getUser().getFullName();
			setOwnerName(ownerName);
		}

		return PortalConstants.ACTION_BASIC_VIEW;
	}

	public String copyEform() throws JSchException, UserPermissionException {

		if (sessionEform.getBasicEform() == null) {
			this.basicEform = eformManager.getBasicEform(eformId);
			sessionEform.setBasicEform(this.basicEform);
		}
		if (sessionEform.getBasicEform().getIsLegacy() != null && sessionEform.getBasicEform().getIsLegacy()) {
			sessionEform.clear();
			throw new UserPermissionException(getAccount().getUserName() + " was trying to copy legacy eform "
					+ Long.toString(sessionEform.getEform().getId()));
		}

		Eform copiedEform = eformManager
				.copyEform(eformManager.getEformNoLazyLoad(sessionEform.getBasicEform().getId()), getAccount());
		copiedEform.setShortName(PortalConstants.EFROM_SHORT_NAME_TEMP);
		copiedEform = eformManager.saveEform(copiedEform);
		sessionEform.clear();
		this.sessionEform.setCopyMode(Boolean.TRUE);
		this.sessionEform.setEform(copiedEform);

		createNewOwnership();
		return PortalConstants.ACTION_EDIT_COPY;
	}

	private void addEformToSharedGroup() throws HttpException, IOException {
		webServiceManager.registerEntityToPermissionGroup(getAccount(), ServiceConstants.SHARED_EFORMS,
				EntityType.EFORM, sessionEform.getBasicEform().getId(), PermissionType.READ);
	}

	private void removeEformFromStandardGroup() throws HttpException, IOException {
		webServiceManager.unregisterEntityToPermissionGroup(ServiceConstants.SHARED_EFORMS, EntityType.EFORM,
				sessionEform.getBasicEform().getId(), PermissionType.READ);
	}

	private void unregisterEformEntityPermissions() throws HttpException, IOException {
		RestAccountProvider accountProvider = getNewAccountRestProvider();
		accountProvider.unregisterEntity(sessionEform.getBasicEform().getId(), EntityType.EFORM);
	}

	private void updateEformStatus(StatusType changeStatus) {
		BasicEform basicEform = sessionEform.getBasicEform();
		basicEform.setStatus(changeStatus);
		basicEform = eformManager.saveBasicEform(basicEform);
		sessionEform.setBasicEform(basicEform);
		setBasicEform(basicEform);
	}

	/**
	 * Attribute JSON for edit
	 * 
	 * @param sq
	 * @param qa
	 * @param attributeObjJson
	 */
	public void attributeJSONforEdit(SectionQuestion sq, QuestionAttribute qa, JSONObject attributeObjJson) {
		if (sq.getSection().getGroupName().equalsIgnoreCase("None") || sq.getSection().getGroupName().isEmpty()) {
			attributeObjJson.put("dataElementName", qa.getDataElementName());
		} else {
			attributeObjJson.put("dataElementName", sq.getSection().getGroupName() + "." + qa.getDataElementName());

		}
		// attributeObjJson.put("dataElementName", qa.getDataElementName());
		attributeObjJson.put("decimalPrecision", String.valueOf(qa.getDecimalPrecision()));
		attributeObjJson.put("tableHeaderType", qa.getTableHeaderType());
		attributeObjJson.put("required", qa.getRequiredFlag());
		// prepopulation
		attributeObjJson.put("prepopulation", qa.getPrepopulation());
		attributeObjJson.put("prepopulationValue", qa.getPrepopulationValue());
		// html attributes
		attributeObjJson.put("vAlign", qa.getvAlign());
		attributeObjJson.put("align", qa.gethAlign());
		attributeObjJson.put("color", qa.getTextColor());
		attributeObjJson.put("fontSize", qa.getFontSize());
		attributeObjJson.put("fontFace", qa.getFontFace());
		attributeObjJson.put("indent", qa.getIndent());
		attributeObjJson.put("minCharacters", qa.getMinCharacters());
		attributeObjJson.put("maxCharacters", qa.getMaxCharacters());
		attributeObjJson.put("horizontalDisplay", qa.getHorizontalDisplay());
		attributeObjJson.put("horizDisplayBreak", qa.getHorizontalDisplayBreak());
		attributeObjJson.put("textareaWidth", qa.getTextBoxWidth());
		attributeObjJson.put("textareaHeight", qa.getTextBoxHeight());
		attributeObjJson.put("textboxLength", qa.getTextBoxLength());
		attributeObjJson.put("dataSpring", qa.getDataSpring());
		attributeObjJson.put("showText", qa.getShowText());
	}

	/**
	 * QuestionJSONforEdit
	 * 
	 * @param quesJSON
	 * @param q
	 * @param sec
	 * @param sq
	 * @param qa
	 */
	public void questionJSONforEdit(JSONObject quesJSON, Question q, Section sec, SectionQuestion sq,
			QuestionAttribute qa) {
		quesJSON.put("questionId", q.getId());
		quesJSON.put("questionName", q.getName());
		quesJSON.put("graphicNames", eformManager.getFileNameListByQuestion(q));
		quesJSON.put("questionText", q.getText());
		quesJSON.put("htmlText", q.getHtmltext());
		quesJSON.put("sectionId", sec.getId());
		quesJSON.put("newQuestionDivId", sec.getId() + "_" + q.getId());
		quesJSON.put("questionType", q.getType().getName());
		// added by Ching-Heng
		quesJSON.put("catOid", q.getCatOid());
		quesJSON.put("formItemOid", q.getFormItemOid());
		// eForm specifc sectionQuestionId we are tracking
		quesJSON.put("sqId", sq.getId());
		// eForm specifc questionAttributeId we are tracking
		quesJSON.put("qaId", qa.getId());
		// question order perservation during save edit
		quesJSON.put("questionOrder", sq.getQuestionOrder());
		quesJSON.put("questionOrder_col", sq.getQuestionOrderColumn());
		quesJSON.put("defaultValue", q.getDefaultValue());
		quesJSON.put("descriptionUp", q.getDescriptionUp());
		quesJSON.put("descriptionDown", q.getDescriptionDown());
		quesJSON.put("displayPV", q.getDisplayPV());
	}

	/**
	 * Calculation rule JSON for edit method
	 * 
	 * @param attributeObjJson
	 * @param qa
	 * @param sq
	 */
	public void calcRuleJSONForEdit(JSONObject attributeObjJson, QuestionAttribute qa, SectionQuestion sq) {
		// calculation
		boolean calQuestion = false;
		boolean calculatedFlag = false;
		if (qa.getCalculatedFlag() != null) {
			calculatedFlag = qa.getCalculatedFlag().booleanValue();
		}
		if (calculatedFlag == true) {
			JSONArray questionToCalculateJson = new JSONArray();
			List<CalculationQuestion> calculatedQuestions = sq.getCalculatedQuestion();
			for (CalculationQuestion calcQ : calculatedQuestions) {
				CalculationQuestionPk cqPk = calcQ.getCalculationQuestionCompositePk();
				questionToCalculateJson.put(PortalConstants.EFROM_SECTION_PATTERN + cqPk.getCalculationSection().getId()
						+ PortalConstants.EFORM_QUESTION_PATTERN + cqPk.getCalculationQuestion().getId());
			}
			attributeObjJson.put("calculationType", Integer.MIN_VALUE);
			attributeObjJson.put("questionsToCalculate", questionToCalculateJson);
			String calc = sq.getCalculation();
			attributeObjJson.put("calculation", calc);
			attributeObjJson.put("calculatedQuestion", true);
			int dtConversionFactor = qa.getDtConversionFactor().intValue();
			attributeObjJson.put("conversionFactor", dtConversionFactor);
			Boolean conditionalForCalc = qa.getConditionalForCalc();
			if (conditionalForCalc == null) {
				conditionalForCalc = new Boolean(false);
			}

			attributeObjJson.put("conditionalForCalc", conditionalForCalc.booleanValue());

		} else {
			attributeObjJson.put("calculationType", Integer.MIN_VALUE);
			attributeObjJson.put("questionsToCalculate", new JSONArray());
			attributeObjJson.put("calculation", "");
			attributeObjJson.put("calculatedQuestion", false);
			attributeObjJson.put("conversionFactor", Integer.MIN_VALUE);
			attributeObjJson.put("conditionalForCalc", false);
		}

		attributeObjJson.put("calDependent", true); // need to fix this
	}
	
	/**
	 * Translates the count information here on the server/database into the format expected by the front end
	 * formbuilder.
	 * 
	 * @param attributeObjJson output element modified by this method
	 * @param qa QuestionAttribute holding the count flag
	 * @param sq SectionQuestion holding the count formula and list of count questions
	 */
	public void countJSONForEdit(JSONObject attributeObjJson, QuestionAttribute qa, SectionQuestion sq) {
		if (qa.getCountFlag() != null && qa.getCountFlag().booleanValue()) {
			JSONArray questionsToCountJson = new JSONArray();
			List<CountQuestion> countQuestions = sq.getCountQuestion();
			for (CountQuestion countQ : countQuestions) {
				CountQuestionPk cPk = countQ.getCountQuestionCompositePk();
				questionsToCountJson.put(PortalConstants.EFROM_SECTION_PATTERN + cPk.getCountSection().getId()
						+ PortalConstants.EFORM_QUESTION_PATTERN + cPk.getCountQuestion().getId());
			}
			attributeObjJson.put("countFlag", true);
			attributeObjJson.put("questionsInCount", questionsToCountJson);
			attributeObjJson.put("countFormula", sq.getCountFormula());
		}
		else {
			attributeObjJson.put("countFlag", false);
			attributeObjJson.put("questionsInCount", new JSONArray());
			attributeObjJson.put("countFormula", "");
		}
	}

	/**
	 * Skip rule JSON for edit
	 * 
	 * @param attributeObjJson
	 * @param qa
	 * @param sq
	 */
	public void skipRuleJSONforEdit(JSONObject attributeObjJson, QuestionAttribute qa, SectionQuestion sq) {
		boolean skipRule = false;
		if (qa.getSkipRuleFlag() != null) {
			skipRule = qa.getSkipRuleFlag().booleanValue();
		}
		if (skipRule) {
			attributeObjJson.put("skipRuleType", qa.getSkipRuleType().getValue());
			attributeObjJson.put("skipRuleOperatorType", qa.getSkipRuleOperatorType().getValue());
			attributeObjJson.put("skipRuleEquals", qa.getSkipRuleEquals());

			JSONArray questionsToSkipJsonArr = new JSONArray();
			List<SkipRuleQuestion> skipRuleQuestions = sq.getSkipRuleQuestion();
			for (SkipRuleQuestion sqQ : skipRuleQuestions) {

				SkipRuleQuestionPk sqPk = sqQ.getSkipRuleQuestionCompositePk();
				String questionToSkipString = PortalConstants.EFROM_SECTION_PATTERN + sqPk.getSkipRuleSection().getId()
						+ PortalConstants.EFORM_QUESTION_PATTERN + sqPk.getSkipRuleQuestion().getId();
				questionsToSkipJsonArr.put(questionToSkipString);
			}
			attributeObjJson.put("questionsToSkip", questionsToSkipJsonArr);
		} else {
			attributeObjJson.put("skipRuleType", Integer.MIN_VALUE);
			attributeObjJson.put("skipRuleOperatorType", Integer.MIN_VALUE);
			attributeObjJson.put("skipRuleEquals", "");
			attributeObjJson.put("questionsToSkip", new JSONArray());
		}

		attributeObjJson.put("skipRuleDependent", true);
	}

	public JSONObject visualScaleEdit(Question q) {
		VisualScale vs = q.getVisualScale();
		JSONObject vsJSONObj = new JSONObject();

		vsJSONObj.put("vscaleRangeStart", String.valueOf(vs.getStartRange()));
		vsJSONObj.put("vscaleRangeEnd", String.valueOf(vs.getEndRange()));
		vsJSONObj.put("vscaleRightText", vs.getRightText());
		vsJSONObj.put("vscaleLeftText", vs.getLeftText());
		vsJSONObj.put("vscaleCenterText", vs.getCenterText());
		vsJSONObj.put("vscaleWidth", String.valueOf(vs.getWidthMM()));
		vsJSONObj.put("vscaleShowHandle", vs.getShowHandle());
		return vsJSONObj;

	}

	/**
	 * Email Trigger JSON for edit
	 * 
	 * @param qa
	 * @param attributeObjJson
	 * @param quesJSON
	 */
	public void emailTriggerJSONforEdit(QuestionAttribute qa, JSONObject attributeObjJson, JSONObject quesJSON) {
		EmailTrigger et = qa.getEmailTrigger();
		QuestionType qType = QuestionType.getByValue(attributeObjJson.getInt("qType"));
		AnswerType ansType = qa.getAnswerType();
		if (et != null && et.getId() != Integer.MIN_VALUE) {
			JSONArray triggerAnswersJsonArr = new JSONArray();
			JSONArray triggerConditionsJsonArr = new JSONArray();
			JSONArray triggerValuesJsonArr = new JSONArray();
			attributeObjJson.put("etId", et.getId());
			attributeObjJson.put("eMailTriggerId", et.getId());
			attributeObjJson.put("toEmailAddress", et.getToEmailAddress());
			attributeObjJson.put("ccEmailAddress", et.getCcEmailAddress());
			attributeObjJson.put("body", et.getBody());
			attributeObjJson.put("subject", et.getSubject());

			for (EmailTriggerValue etv : et.getTriggerValues()) {
				JSONObject etValue = new JSONObject();
				etValue.put("etValId", etv.getId());
				etValue.put("etAnswer", etv.getAnswer());
				etValue.put("etCondition", (etv.getTriggerCondition() == null ? "" : etv.getTriggerCondition()));
				triggerValuesJsonArr.put(etValue);
			}
			attributeObjJson.put("triggerValues", triggerValuesJsonArr);

			quesJSON.put("emailTrigger", true);
		} else {
			attributeObjJson.put("etId", -7);
			attributeObjJson.put("eMailTriggerId", Integer.MIN_VALUE);
			attributeObjJson.put("toEmailAddress", "");
			attributeObjJson.put("ccEmailAddress", "");
			attributeObjJson.put("body", "");
			attributeObjJson.put("subject", "");
			attributeObjJson.put("triggerValues", new JSONArray());
			quesJSON.put("emailTrigger", false);

		}
	}

	/**
	 * Btris Mapping JSON for edit
	 * 
	 * @param qa
	 * @param attributeObjJson
	 * @param quesJSON
	 */
	public void btrisMappingJSONforEdit(Question q, JSONObject attributeObjJson, JSONObject quesJSON) {
		String dataElementName = q.getQuestionAttribute().getDataElementName();
		BtrisMapping existingBM = btrisMappingManager.getBtrisMappingByDEName(dataElementName);
		if (existingBM != null) {
			quesJSON.put("hasBtrisMapping", true);
			BtrisMapping associatedBM = q.getBtrisMapping();
			if (associatedBM != null) {
				quesJSON.put("isGettingBtrisVal", true);
				attributeObjJson.put("btrisObservationName", associatedBM.getBtrisObservationName());
				attributeObjJson.put("btrisRedCode", associatedBM.getBtrisRedCode());
				attributeObjJson.put("btrisSpecimenType", associatedBM.getBtrisSpecimenType());
			} else {
				quesJSON.put("isGettingBtrisVal", false);
				attributeObjJson.put("btrisObservationName", existingBM.getBtrisObservationName());
				attributeObjJson.put("btrisRedCode", existingBM.getBtrisRedCode());
				attributeObjJson.put("btrisSpecimenType", existingBM.getBtrisSpecimenType());
			}
		} else {
			quesJSON.put("hasBtrisMapping", false);
			quesJSON.put("isGettingBtrisVal", false);
			attributeObjJson.put("btrisObservationName", "");
			attributeObjJson.put("btrisRedCode", "");
			attributeObjJson.put("btrisSpecimenType", "");
		}
	}

	/**
	 * Section JSON for edit mode
	 * 
	 * @param sec
	 * @return
	 */
	public JSONObject createSectionJSON(Section sec, RepeatableGroup rg) {
		// section json
		JSONObject secJSON = new JSONObject();
		secJSON.put("id", sec.getId());
		secJSON.put("name", sec.getName());
		secJSON.put("description", sec.getDescription());
		secJSON.put("isRepeatable", sec.getIsRepeatable());
		secJSON.put("isCollapsable", sec.getCollapsable());
		secJSON.put("initRepeatedSecs", sec.getInitialRepeatedSections());
		if (rg != null) {
			secJSON.put("maxRepeatedSecs", sec.getMaxRepeatedSections());
		}
		Long repeatedSectionParent = sec.getRepeatedSectionParent();
		if (repeatedSectionParent == null) {
			repeatedSectionParent = -1L;
		}
		String repeatedSectionParentString = String.valueOf(repeatedSectionParent);
		secJSON.put("repeatedSectionParent", repeatedSectionParentString);
		secJSON.put("repeatableGroupName", sec.getGroupName());
		// section layout order preservation during save edit
		secJSON.put("row", sec.getFormRow());
		secJSON.put("col", sec.getFormCol());
		secJSON.put("isManuallyAdded", sec.getIsManuallyAdded());
		return secJSON;

	}

	/**
	 * set updated to for eFrom attributes
	 */
	public void getUpdateEformValues() {
		String name = getParamValueForParamKeyFromRequest("formForm.name");
		if (name != null) {
			sessionEform.getEform().setTitle(name);
		}
		String description = getParamValueForParamKeyFromRequest("formForm.description");
		// if (description != null) {
		sessionEform.getEform().setDescription(description);
		// }
		String dataStructureName = getParamValueForParamKeyFromRequest("formForm.dataStructureName");
		if (dataStructureName != null) {
			sessionEform.getEform().setFormStructureShortName(dataStructureName);
		}
		String shortName = getParamValueForParamKeyFromRequest("formForm.shortName");
		if (shortName != null) {
			sessionEform.getEform().setShortName(shortName);
		}
		String allowMultipleCollectionInstances =
				this.getParamValueForParamKeyFromRequest("formForm.allowMultipleCollectionInstances");
		if (allowMultipleCollectionInstances != null) {
			sessionEform.getEform()
					.setAllowMultipleCollectionInstances(Boolean.valueOf(allowMultipleCollectionInstances));
		}
		String sectionColor = getParamValueForParamKeyFromRequest("formForm.sectioncolor");
		if (sectionColor != null) {
			sessionEform.getEform().setSectionNameColor(sectionColor);
		}
		String sectionFont = getParamValueForParamKeyFromRequest("formForm.sectionfont");
		if (sectionFont != null) {
			sessionEform.getEform().setSectionNameFont(sectionFont);
		}
		String isDataSpring = getParamValueForParamKeyFromRequest("formForm.isDataSpring");
		if (isDataSpring != null) {
			sessionEform.getEform().setEnableDataSpring(isDataSpring);
		}
		String sectionBorder = getParamValueForParamKeyFromRequest("formForm.sectionborder");
		if (sectionBorder != null) {
			sessionEform.getEform().setSectionBorder(Boolean.valueOf(sectionBorder));
		}
		String cellPadding = getParamValueForParamKeyFromRequest("formForm.cellpadding");
		if (cellPadding != null) {
			sessionEform.getEform().setCellPadding(Integer.valueOf(cellPadding));
		} else {
			sessionEform.getEform().setCellPadding(2);
		}
		String formColor = getParamValueForParamKeyFromRequest("formForm.formcolor");
		if (formColor != null) {
			sessionEform.getEform().setFormNameColor(formColor);
		}
		String fontSize = getParamValueForParamKeyFromRequest("formForm.fontSize");
		if (fontSize != null) {
			sessionEform.getEform().setFontSize(Integer.valueOf(fontSize));
		}
		String formFont = getParamValueForParamKeyFromRequest("formForm.formfont");
		if (formFont != null) {
			sessionEform.getEform().setFormNameFont(formFont);
		}
		String formBorder = getParamValueForParamKeyFromRequest("formForm.formborder");
		if (formBorder != null) {
			sessionEform.getEform().setFormBorder(Boolean.valueOf(formBorder));
		}
		String formFooter = getParamValueForParamKeyFromRequest("formForm.formFooter");
		if (formFooter != null) {
			sessionEform.getEform().setFooter(formFooter);
		}
		String formHeader = getParamValueForParamKeyFromRequest("formForm.formHeader");
		if (formHeader != null) {
			sessionEform.getEform().setHeader(formHeader);
		}
		String pfCatIdStr = getParamValueForParamKeyFromRequest("formForm.pfCategory");
		if (pfCatIdStr != null) {
			Long pfCatId = Long.valueOf(pfCatIdStr);
			EformPfCategory pfCat = EformPfCategory.getById(pfCatId);
			sessionEform.getEform().setPfCategory(pfCat);
		}

	}

	/**
	 * Helper function to get request param value for param key given from the request scope
	 * 
	 * @param paramKey
	 * @return paramValue
	 */
	private String getParamValueForParamKeyFromRequest(String paramKey) {
		String paramValue = null;
		Map parameterMap = getRequest().getParameterMap();
		Set parameterKeys = parameterMap.keySet();
		Iterator requestIter = parameterKeys.iterator();
		while (requestIter.hasNext()) {
			String key = (String) requestIter.next();
			if (key.equalsIgnoreCase(paramKey)) {
				String values[] = getRequest().getParameterValues(key);
				if (values != null && values.length != 0) {
					for (int i = 0; i < values.length; i++) {
						if (values[i] != null && !values[i].equals(PortalConstants.EMPTY_STRING)) {
							paramValue = values[i];
						}
					}
				}
			}
		}

		return paramValue;
	}

	private void createNewOwnership() {
		// Add New Owner Permission
		EntityMap em = new EntityMap();
		em.setAccount(getAccount());
		// em.setEntityId( null );
		em.setPermission(PermissionType.OWNER);
		em.setType(EntityType.EFORM);

		getSessionEform().setEntityMapList(new ArrayList<EntityMap>());
		getSessionEform().getEntityMapList().add(em);
		getSessionEform().getEntityMapAuthNameList().add(em.getAccount().getDisplayName());
		getSessionEform().setSessionEformUserPermissionType(PermissionType.OWNER);
	}

	private HashMap<Long, BtrisMapping> generateQuestionBtrisMappingMap() {
		HashMap<Long, BtrisMapping> questionBtrisMappingMap = new HashMap<Long, BtrisMapping>();
		JSONArray questionJSONArr = new JSONArray(this.questionsJSON);
		if (questionJSONArr.length() > 0) {
			for (int i = 0; i < questionJSONArr.length(); i++) {
				JSONObject questionJson = questionJSONArr.getJSONObject(i);
				Boolean hasBtrisMapping = questionJson.getBoolean("hasBtrisMapping");
				if(hasBtrisMapping) {
					Long questionId = questionJson.getLong("questionId");
					JSONObject attributeObjJSON = questionJson.getJSONObject("attributeObject");
					String geDotGrpName = attributeObjJSON.getString("dataElementName");
					String dataElementName = "none";
					if (geDotGrpName != null && geDotGrpName.contains(".")) {
						dataElementName = geDotGrpName.substring(geDotGrpName.indexOf(".") + 1, geDotGrpName.length());
					} else if (geDotGrpName != null) {
						dataElementName = geDotGrpName;
					}
					Boolean isGettingBtrisVal = questionJson.getBoolean("isGettingBtrisVal");
	
					if (dataElementName != null && !dataElementName.equals("") && isGettingBtrisVal != null
							&& isGettingBtrisVal) {
						BtrisMapping bm = btrisMappingManager.getBtrisMappingByDEName(dataElementName);
						questionBtrisMappingMap.put(questionId, bm);
	
					}
				}
			}
		}
		return questionBtrisMappingMap;
	}

	public String getQuestionsJSON() {
		return questionsJSON;
	}

	public void setQuestionsJSON(String questionsJSON) {
		this.questionsJSON = questionsJSON;
	}

	public String getSectionsJSON() {
		return sectionsJSON;
	}

	public void setSectionsJSON(String sectionsJSON) {
		this.sectionsJSON = sectionsJSON;
	}

	public String getQuestionJson() {
		return questionJson;
	}

	public void setQuestionJson(String questionJson) {
		this.questionJson = questionJson;
	}

	public String getJsonFsDe() {
		return jsonFsDe;
	}

	public void setJsonFsDe(String jsonFsDe) {
		this.jsonFsDe = jsonFsDe;
	}

	public String getJsonString() {
		return jsonString;
	}

	public String getSectionsJSONArrJSONString() {
		return sectionsJSONArrJSONString;
	}

	public void setSectionsJSONArrJSONString(String sectionsJSONArrJSONString) {
		this.sectionsJSONArrJSONString = sectionsJSONArrJSONString;
	}

	public String getQuestionsJSONArrJSONString() {
		return questionsJSONArrJSONString;
	}

	public void setQuestionsJSONArrJSONString(String questionsJSONArrJSONString) {
		this.questionsJSONArrJSONString = questionsJSONArrJSONString;
	}

	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}

	public void setBasicEform(BasicEform basicEform) {
		this.basicEform = basicEform;
	}

	public BasicEform getBasicEform() {
		return this.basicEform;
	}

	public void setEformId(Long eformId) {
		this.eformId = eformId;
	}

	public Long getEformId() {
		return this.eformId;
	}

	public String getFormMode() {
		return formMode;
	}

	public void setFormMode(String formMode) {
		this.formMode = formMode;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getValidationJson() {
		return validationJson;
	}

	public void setValidationJson(String validationJson) {
		this.validationJson = validationJson;
	}

	public boolean iseFormModule() {
		return eFormModule;
	}

	public void seteFormModule(boolean eFormModule) {
		this.eFormModule = eFormModule;
	}

	public void setPublicationId(long publicationId) {
		this.publicationId = publicationId;
	}

	public long getPublicationId() {
		return this.publicationId;
	}

	public StatusType getPublicationIdAsStatusType() {
		return StatusType.statusOf(this.publicationId);
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getReason() {
		return this.reason;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getOwnerName() {
		return this.ownerName;
	}

	public String geteFormAction() {
		return eFormAction;
	}

	public void seteFormAction(String eFormAction) {
		this.eFormAction = eFormAction;
	}


}
