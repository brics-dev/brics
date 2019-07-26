package gov.nih.nichd.ctdb.protocol.action;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.result.StreamResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CasProxyTicketException;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.DuplicateArchiveObjectException;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.DictionaryUtil;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.form.common.FormConstants;
import gov.nih.nichd.ctdb.protocol.domain.ClinicalLocation;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.protocol.domain.IntervalClinicalPoint;
import gov.nih.nichd.ctdb.protocol.domain.PointOfContact;
import gov.nih.nichd.ctdb.protocol.domain.PrepopDataElement;
import gov.nih.nichd.ctdb.protocol.domain.Procedure;
import gov.nih.nichd.ctdb.protocol.domain.ProcedureType;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.protocol.tag.IntervalClinicalPointIdtDecorator;
import gov.nih.nichd.ctdb.protocol.util.BasicEformTitleComparator;
import gov.nih.nichd.ctdb.protocol.util.PrePopDataElementComparator;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.nichd.ctdb.util.common.UnknownLookupException;
import gov.nih.nichd.ctdb.util.domain.LookupType;
import gov.nih.nichd.ctdb.util.manager.LookupManager;
import gov.nih.nichd.ctdb.ws.clients.DictionaryWSProvider;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

/**
 * The Struts Action class responsible for adding and
 * editing & deleting interval information for ProFoRMS
 *
 * @author CIT
 */
public class IntervalAction extends BaseAction {
	private static final long serialVersionUID = -2790743303068472991L;
	private static final Logger logger = Logger.getLogger(IntervalAction.class);
	
	public static final String INTERVALACTION_MESSAGES_KEY = "IntervalAction_ActionMessages";
	public static final String INTERVALACTION_ERROR_KEY = "IntervalAction_ActionErrors";
	
	private int id = -1;
	private String name;
	private String description = "";
	private int studyId = Integer.MIN_VALUE;
	private int intervalType = 0;
	private String category = "";
	private String selectedForms = CtdbConstants.EMPTY_JSON_ARRAY_STR;
	private int visitTypeSelfReportStart = 15;
	private int visitTypeSelfReportEnd = 15;
	private String prepopDataElements = CtdbConstants.EMPTY_JSON_ARRAY_STR;
	private String jsonString = CtdbConstants.EMPTY_JSON_ARRAY_STR;
	private String errRespMsg = "";
	private String eFormShortNames = CtdbConstants.EMPTY_JSON_ARRAY_STR;
	private List<CtdbLookup> intervalTypeList = new ArrayList<CtdbLookup>();
	private String intervalClinicals = CtdbConstants.EMPTY_JSON_ARRAY_STR;
	private String intervalProcedures = CtdbConstants.EMPTY_JSON_ARRAY_STR;
	private String intervalPointOfContacts = CtdbConstants.EMPTY_JSON_ARRAY_STR;
	private List<IntervalClinicalPoint> intervalClinicalPointList = new ArrayList<IntervalClinicalPoint>();
	private String intervalClinicalPoints = CtdbConstants.EMPTY_JSON_ARRAY_STR;
	private String intervalClinicalPOCStr;
		
	public String execute() {
		buildLeftNav(LeftNavController.LEFTNAV_STUDY_CREATEINTERVAL);
		String strutsResult = BaseAction.SUCCESS;
		Interval visitType = null;
		Locale userLocale = request.getLocale();
		clearIntevalSession();
		
		try {
			Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			this.setStudyId(p.getId());
			ProtocolManager protoMan = new ProtocolManager();
			DictionaryWSProvider dictionaryWSProvider = new DictionaryWSProvider(request);
			
			// Get published eFrom from Dictionary.
			Map<String, BasicEform> eFormMap = dictionaryWSProvider.getPublishedEformMapFromRestWs();
			List<BasicEform> eFormList = new ArrayList<BasicEform>(eFormMap.values());
			Collections.sort(eFormList, new BasicEformTitleComparator());
			request.setAttribute(FormConstants.PROTOCOLEFORMS, eFormList);
			session.put(FormConstants.PUBLISHED_EFORM_MAP, eFormMap);
			
			LookupManager luMan = new LookupManager();
			intervalTypeList = luMan.getLookups(LookupType.INTERVAL_TYPE);
			
			// Check if the pre-population data element cache needs to be created.
    		if ( !session.containsKey(CtdbConstants.PRE_POP_DE_CACHE_KEY) ) {
    			DictionaryUtil dictUtil = new DictionaryUtil();
    			Map<String, DataElement> deCache = dictUtil.getDeMapForAllPrePopDes(request);
    			
    			logger.info("Saving the data element cache to session...");
    			session.put(CtdbConstants.PRE_POP_DE_CACHE_KEY, deCache);
    		}
			
			// Get all available pre-population data elements.
			List<PrepopDataElement> prepopDEList = protoMan.getAllPrepopDEs(false);
			Collections.sort(prepopDEList, new PrePopDataElementComparator());
			JSONArray prePopDeArray = prepopDEListToJSONArray(prepopDEList, false);
			
			List<ClinicalLocation> clinicalLocList = protoMan.getProtocolClinicalLocs(p.getId());
			JSONArray ClinicalArray = clinicalLocListToJSONArray(clinicalLocList);
			intervalClinicals = ClinicalArray.toString();
			
			List<Procedure> procedureList = protoMan.getProtocolProcdeure(p.getId());
			JSONArray procedureArray = procedureListTOJSONArray(procedureList);
			intervalProcedures = procedureArray.toString();
			
			List<PointOfContact> pointOfContactList = protoMan.getProtocolPointOfContact(p.getId());
			JSONArray pointOfContactArray = pocListTOJSONArray(pointOfContactList);
			intervalPointOfContacts = pointOfContactArray.toString();
			
			// Check if the page will be editing an existing visit type.
			if (this.getId() > 0) {
				int visitTypeId = this.getId();
				
				visitType = protoMan.getInterval(visitTypeId);
				visitType.setId(visitTypeId);
				this.setName(visitType.getName());
				this.setDescription(visitType.getDescription());
				this.setIntervalType(visitType.getIntervalType());
				this.setCategory(visitType.getCategory());
				this.setVisitTypeSelfReportStart(visitType.getSelfReportStart());
				this.setVisitTypeSelfReportEnd(visitType.getSelfReportEnd());
				List<BasicEform> eformsInterval = protoMan.getEformsForInterval(visitTypeId);
				this.setSelectedForms(eformListToJSONString(eformsInterval));
				
				// Mark the existing pre-pop data elements as selected.
				List<PrepopDataElement> selectedPrePopDEs = protoMan.getPrepopDEsForInterval(visitTypeId, false);
				
				for ( PrepopDataElement pde : selectedPrePopDEs ) {
					for ( int i = 0; i < prePopDeArray.length(); i++ ) {
						JSONObject prePopJson = prePopDeArray.getJSONObject(i);
						
						if ( prePopJson.getString("shortName").equals(pde.getShortName()) ) {
							prePopJson.put("isSelected", true);
						}
					}
				}
				
				List<IntervalClinicalPoint> intLocPointList = protoMan.getIntervalClinicalPntsForInterval(visitTypeId);
				session.put("intervalClinicalPointList", intLocPointList);
			}
			
			// Convert the JSON array of available pre-population data elements to a string.
			prepopDataElements = prePopDeArray.toString();
		}
		catch (CtdbException ce) {
			logger.error("Database error occurred while setting up the add/edit visit type page.", ce);
			
			// Set action error.
			String vt = visitType != null ? visitType.getName() : getText("protocol.visitType.title.display").toLowerCase(userLocale);
			addActionError(getText(StrutsConstants.ERROR_DATABASE_GET, new String[]{vt}));
		}
		catch (JSONException je) {
			logger.error("Could not translate the forms to JSON.", je);
			
			// Set action error.
			String vt = visitType != null ? visitType.getName() : getText("protocol.visitType.title.display").toLowerCase(userLocale);
            addActionError(getText(StrutsConstants.ERROR_ADD_EDIT_PAGE_SETUP, new String[]{vt}));
        }
		catch (CasProxyTicketException | WebApplicationException e) {
			logger.error("Could not get eForms from the Dictionary web service.", e);
			
			// Set action error.
			addActionError(getText(StrutsConstants.ERROR_WEB_SERVICE_GET, 
					new String[] {getText("response.collect.eform.plural.display"), getText("webservice.dictionary.label")}));
		}
		catch(Exception e) {
			logger.error("An error occurred while setting up the add/edit visit type page.", e);
			
			// Set action error.
			String vt = visitType != null ? visitType.getName() : getText("protocol.visitType.title.display").toLowerCase(userLocale);
            addActionError(getText(StrutsConstants.ERROR_ADD_EDIT_PAGE_SETUP, new String[]{vt}));
        }
		
		// Check for any errors
		if ( hasActionErrors() ) {
			strutsResult = StrutsConstants.EXCEPTION;
			session.put(IntervalAction.INTERVALACTION_ERROR_KEY, getActionErrors());
		}
		
		return strutsResult;
	}
	
	private void clearIntevalSession() {
		session.remove("intervalClinicalPointList");
	}
	
	public String getIntervalClinicalPointDTList() throws CtdbException {
		List<IntervalClinicalPoint> intLocPointList = new ArrayList<IntervalClinicalPoint>();

		//add new visit type, get intervalClinicalPoint list from session adding from method addIntervalClinicalPoint()
		List<IntervalClinicalPoint> intervalLPList = (List<IntervalClinicalPoint>) session.get("intervalClinicalPointList");	
		if (intervalLPList != null) {
			intLocPointList = intervalLPList;
		}
		
		try {
			IdtInterface idt = new Struts2IdtInterface();			
			ArrayList<IntervalClinicalPoint> outputList = new ArrayList<IntervalClinicalPoint>(intLocPointList);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new IntervalClinicalPointIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	public StreamResult addIntervalClinicalPoint() throws JSONException, CtdbException {
		String intervalLPJson = this.getIntervalClinicalPOCStr();
		JsonParser jsonParser = new JsonParser();
		JsonElement element = jsonParser.parse(intervalLPJson.replaceAll("^\"+|\"+$", ""));
		JsonObject intervalCPJsonObj = element.getAsJsonObject();
		
		List<IntervalClinicalPoint> intervalCPList = (List<IntervalClinicalPoint>) session.get("intervalClinicalPointList");
		if (intervalCPList == null) {
			intervalCPList = new ArrayList<IntervalClinicalPoint>();
		}

		try {
			IntervalClinicalPoint intervalCP = convertJsonStrToIntClinicPnt(intervalCPJsonObj);
			intervalCPList.add(intervalCP);
			session.put("intervalClinicalPointList", intervalCPList);
		
		} catch (JsonSyntaxException jse) {
			jse.printStackTrace();
		}

		
		return new StreamResult(new ByteArrayInputStream((BaseAction.SUCCESS).getBytes()));
	}
	
	public StreamResult editIntervalClinicalPoint() throws JSONException, CtdbException {
		String intervalLPJson = this.getIntervalClinicalPOCStr();
		JsonParser jsonParser = new JsonParser();
		JsonElement element = jsonParser.parse(intervalLPJson.replaceAll("^\"+|\"+$", ""));
		JsonObject intervalCPJsonObj = element.getAsJsonObject();
		
		List<IntervalClinicalPoint> sessionIntervalCPList = (List<IntervalClinicalPoint>) session.get("intervalClinicalPointList");
		if (sessionIntervalCPList == null) {
			sessionIntervalCPList = new ArrayList<IntervalClinicalPoint>();
		}

		try {
			IntervalClinicalPoint intervalCP = convertJsonStrToIntClinicPnt(intervalCPJsonObj);
			for(IntervalClinicalPoint icp : sessionIntervalCPList){
				if(icp.getId() == intervalCP.getId()) {
					icp.setClinicalLoc(intervalCP.getClinicalLoc());
					icp.setProcedure(intervalCP.getProcedure());
					icp.setPointOfContact(intervalCP.getPointOfContact());
					icp.setStatus(intervalCP.getStatus());
				}
			}
			session.put("intervalClinicalPointList", sessionIntervalCPList);
		
		} catch (JsonSyntaxException jse) {
			jse.printStackTrace();
		}
		
		return new StreamResult(new ByteArrayInputStream((BaseAction.SUCCESS).getBytes()));
	}

	
	public StreamResult deleteIntervalClinicalPoint() throws CtdbException {
		String intClinicalPntJson = this.getIntervalClinicalPOCStr();
		JsonParser jsonParser = new JsonParser();
		JsonElement element = jsonParser.parse(intClinicalPntJson.replaceAll("^\"+|\"+$", ""));
		JsonArray intClinicalPntJsonArr = element.getAsJsonArray();
		
		List<IntervalClinicalPoint> intClinicalPntToRemoveList = new ArrayList<IntervalClinicalPoint>();
		if (intClinicalPntJsonArr != null && intClinicalPntJsonArr.size() > 0) {
			for (int i = 0; i < intClinicalPntJsonArr.size(); i++) {
				JsonObject intClinicalPntJsonObj = intClinicalPntJsonArr.get(i).getAsJsonObject();
				IntervalClinicalPoint intervalSP = convertJsonStrToIntClinicPnt(intClinicalPntJsonObj);
				intClinicalPntToRemoveList.add(intervalSP);
			}			
		}
		
		List<IntervalClinicalPoint> intClinicalPntList = (List<IntervalClinicalPoint>) session.get("intervalClinicalPointList");
		if (intClinicalPntList != null && intClinicalPntList.size() > intClinicalPntToRemoveList.size()){
			intClinicalPntList.removeAll(intClinicalPntToRemoveList);
		}
		session.put("intervalClinicalPointList", intClinicalPntList);
		
		return new StreamResult(new ByteArrayInputStream((BaseAction.SUCCESS).getBytes()));
	}
	
	public IntervalClinicalPoint convertJsonStrToIntClinicPnt(JsonObject jsonObj) throws CtdbException{
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		ProtocolManager protoMan = new ProtocolManager();
		
		JsonObject clinicalLocJsonObj = jsonObj.get("clinicalLocation").getAsJsonObject();
		String clinicalLocIdStr = clinicalLocJsonObj.get("clinicalLocationId").toString().replaceAll("^\"|\"$", "");
		ClinicalLocation clinicalLoc = new ClinicalLocation();
		if (StringUtils.isNotBlank(clinicalLocIdStr)) {
			int  clinicalLocId = Integer.parseInt(clinicalLocIdStr);
			if(clinicalLocId > 0) {
				clinicalLoc = protoMan.getClinicalLocationById(clinicalLocId);
			}
		}
		
		JsonObject procJsonObj = jsonObj.get("procedure").getAsJsonObject();
		String procIdStr = procJsonObj.get("procedureId").toString().replaceAll("^\"|\"$", "");
		Procedure proc = new Procedure();
		if(StringUtils.isNotBlank(procIdStr)) {		
			int procedureId = Integer.parseInt(procIdStr);
			if(procedureId > 0){
				proc = protoMan.getProcedureById(procedureId);
			}
		}
		
		JsonObject pocJsonObj = jsonObj.get("pointOfContact").getAsJsonObject();
		String podIdStr = pocJsonObj.get("pocId").toString().replaceAll("^\"|\"$", "");
		PointOfContact poc = new PointOfContact();
		if (StringUtils.isNotBlank(procIdStr)) {
			int pocId = Integer.parseInt(podIdStr);
			if (pocId > 0) {
				poc = protoMan.getPointOfContactById(pocId);
			}
		}
		
		IntervalClinicalPoint intervalLocPnt = new IntervalClinicalPoint(clinicalLoc, proc, poc);
		String icpIdStr = jsonObj.get("id").toString().replaceAll("^\"|\"$", "");
		if (!icpIdStr.equals("") &&
				Long.valueOf(icpIdStr) > 0) {
			intervalLocPnt.setId(Integer.parseInt(icpIdStr));
		}
		intervalLocPnt.setStatus(jsonObj.get("status").toString().replaceAll("^\"|\"$", ""));
		
		return intervalLocPnt;
	}
	
	public String saveVisitType() {
		buildLeftNav(LeftNavController.LEFTNAV_STUDY_CREATEINTERVAL);
		
		String strutsResult = BaseAction.SUCCESS;
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		Locale userLocale = request.getLocale();
		List<BasicEform> eFromList = null;
		List<PrepopDataElement> selectedPrePopDeList = null;
		ProtocolManager protoMan = new ProtocolManager();
		
		try {
			LookupManager luMan = new LookupManager();
			intervalTypeList = luMan.getLookups(LookupType.INTERVAL_TYPE);
			
			// Get published eForms from session.
			@SuppressWarnings("unchecked")
			Map<String, BasicEform> eFormMap = (Map<String, BasicEform>) session.get(FormConstants.PUBLISHED_EFORM_MAP);
			
			if ( eFormMap == null ) {
				DictionaryWSProvider dictionaryWSProvider = new DictionaryWSProvider(request);
				eFormMap = dictionaryWSProvider.getPublishedEformMapFromRestWs();
				session.put(FormConstants.PUBLISHED_EFORM_MAP, eFormMap);
			}
			
			eFromList = new ArrayList<BasicEform>(eFormMap.values());
			Collections.sort(eFromList, new BasicEformTitleComparator());
			request.setAttribute(FormConstants.PROTOCOLEFORMS, eFromList);
		}
		catch ( Exception e ) {
			logger.error("Error occured while setting up the visit type page.", e);
			addActionError(getText(StrutsConstants.ERROR_DATA_GET, 
					new String[]{getText("repoonse.collectDataLandingHome.subtitle").toLowerCase(userLocale)}));
			
			return StrutsConstants.EXCEPTION;
		}
		
		try {
			// Convert the selected pre-pop JSON array to a Java list.
			if ( !Utils.isBlank(prepopDataElements) ) {
				selectedPrePopDeList = this.buildAssociatedPrepopDEList(prepopDataElements);
			}
			
			//convert string to a JSON array, then convert the JSON array to IntervalClinicalPoint object list
			JsonParser jsonParser = new JsonParser();
			JsonElement element = jsonParser.parse(intervalClinicalPoints.replaceAll("^\"+|\"+$", ""));
			JsonArray intervalLPJsonArr = element.getAsJsonArray();
			
			if (intervalLPJsonArr != null && intervalLPJsonArr.size() > 0) {
				for (int i = 0; i < intervalLPJsonArr.size(); i++) {
					JsonObject intervalLPJsonObj = intervalLPJsonArr.get(i).getAsJsonObject();
					try {
						IntervalClinicalPoint intervalCP = convertJsonStrToIntClinicPnt(intervalLPJsonObj);
						intervalClinicalPointList.add(intervalCP);					
					} catch (JsonSyntaxException jse) {
						jse.printStackTrace();
					}
				}
			}

			// Check if the visit type data is valid.
			if ( validateVisitType(selectedPrePopDeList) ) {
				this.setStudyId(p.getId());
				User user = getUser();
				Interval visitType = new Interval();
				
				// Build a visit type object from the user input.
				visitType.setProtocolId(this.getStudyId());
				visitType.setName(this.getName());
				visitType.setDescription(this.getDescription());
				visitType.setIntervalType(this.getIntervalType());
				visitType.setCategory(this.getCategory());
				visitType.setUpdatedBy(user.getId());
				visitType.setSelfReportStart(this.getVisitTypeSelfReportStart());
				visitType.setSelfReportEnd(this.getVisitTypeSelfReportEnd());
				visitType.setPrepopulateDEList(selectedPrePopDeList);
				visitType.setIntervalClinicalPointList(intervalClinicalPointList);

				if ( !Utils.isBlank(this.getSelectedForms()) ) {
					visitType.setIntervalEFormList(this.buildAssociatedEformList(this.getSelectedForms(), this.eFromListToMap(eFromList)));
				} else {
					visitType.setIntervalEFormList(new ArrayList<BasicEform>());
				}
				
				if ( this.getId() < 0 ) {   // Add
					visitType.setCreatedBy(user.getId());
					protoMan.createVisitType(visitType, p.getId());
					addActionMessage(getText(StrutsConstants.SUCCESS_ADD_KEY, 
							new String[]{"\"" + visitType.getName() + "\" " + getText("protocol.visitType.title.display").toLowerCase(userLocale)}));
				}
				else {          // Edit       
					visitType.setId(this.getId());
					protoMan.updateVisitType(visitType, p.getId());
					addActionMessage(getText(StrutsConstants.SUCCESS_EDIT_KEY, 
							new String[]{"\"" + visitType.getName() + "\" " + getText("protocol.visitType.title.display").toLowerCase(userLocale)}));
				}
				
	            session.put(IntervalAction.INTERVALACTION_MESSAGES_KEY, this.getActionMessages());
			}
		}
		catch (UnknownLookupException ubme) {
			logger.error("Could not get a look up object.", ubme);
            return StrutsConstants.FAILURE;
        }
		catch (DuplicateObjectException doe){
			logger.error("Duplicate visit type found while saving changes.", doe);
			addActionError(getText(StrutsConstants.ERROR_DUPLICATE, 
					new String[]{"\"" + this.getName() + "\" " + getText("protocol.visitType.title.display").toLowerCase(userLocale)}));
        }
		catch (DuplicateArchiveObjectException daoe) {
        	logger.error("Duplicate archive entry found while saving changes.", daoe);
			addActionError(getText(StrutsConstants.ERROR_DUPLICATE_ARCHIVE, 
					new String[]{"\"" + this.getName() + "\" " + getText("protocol.visitType.title.display").toLowerCase(userLocale)}));
		}
		catch (NumberFormatException nfe) {
			logger.error("Could not convert the string to a number.", nfe);
			addActionError(getText(StrutsConstants.ERROR_DATA_SAVE_RETRY, 
					new String[]{getText("form.forms.formInformationDisplay").toLowerCase(userLocale)}));
		}
		catch (JSONException je) {
			logger.error("Could not construct JSON object.", je);
			addActionError(getText(StrutsConstants.ERROR_DATA_SAVE, 
					new String[]{getText("form.FormAssociation.display.plural").toLowerCase(userLocale)}));
		}
		catch (CtdbException ce) {
			logger.error("Database error occurred.", ce);
			addActionError(getText(StrutsConstants.ERROR_DATABASE_SAVE_RETRY, 
					new String[]{"\"" + this.getName() + "\" " + getText("protocol.visitType.title.display").toLowerCase(userLocale)}));
		}
		catch (Exception e) {
			logger.error("An error occured while saving the visit type.", e);
			addActionError(getText(StrutsConstants.ERROR_DATA_SAVE, 
					new String[]{"\"" + this.getName() + "\" " + getText("protocol.visitType.title.display").toLowerCase(userLocale)}));
		}
		
		// Check for any action errors
		if ( hasActionErrors() || hasFieldErrors() ) {
			strutsResult = StrutsConstants.EXCEPTION;
		}
		clearIntevalSession();

		return strutsResult;
	}

	public String queryPrePopDeJsArrayByEformNameArray() {
		String strutsResult = BaseAction.SUCCESS;
		JSONArray eFormNameArray = null;
		
		// Validate the eForm short name JSON array string.
		try {
			eFormNameArray = new JSONArray(eFormShortNames);
		}
		catch (JSONException je) {
			errRespMsg = "The given JSON array is invalid: " + eFormShortNames;
			logger.error(errRespMsg, je);
			return BaseAction.ERROR;
		}
		
		// Get the list of pre-population data element short names from the system.
		try {
			DictionaryWSProvider dictionaryWSProvider = new DictionaryWSProvider(request);
			List<String> deNameList = dictionaryWSProvider.getDeNamesByEformShortNameJsArray(eFormNameArray);
			ProtocolManager protoMan = new ProtocolManager();
			List<PrepopDataElement> prePopDeList = protoMan.getPrepopDEByShortNameList(deNameList);
			
			// Build the JSON response.
			jsonString = prepopDEListToJSONArray(prePopDeList, true).toString();
		}
		catch (JSONException je) {
			errRespMsg = getText(StrutsConstants.ERROR_DATA_GET, new String[] {getText("form.forms.prepopvalue.dataelement.display")});
			logger.error("An error occurred while converting the pre-pop DE list to JSON.", je);
			strutsResult = BaseAction.ERROR;
		}
		catch (CasProxyTicketException | WebApplicationException e) {
			errRespMsg = getText(StrutsConstants.ERROR_WEB_SERVICE_GET, 
					new String[] {getText("question.dataElement.name.display"), getText("webservice.dictionary.label")});
			logger.error("Couldn't generate a proxy ticket or a web service error occured while getting the list of data element shortnames.", e);
			strutsResult = BaseAction.ERROR;
		}
		catch (CtdbException ce) {
			errRespMsg = getText(StrutsConstants.ERROR_DATABASE_GET, new String[] {getText("form.forms.prepopvalue.dataelement.display")});
			logger.error("A database error occurred while getting the pre-pop DE objects.", ce);
			strutsResult = BaseAction.ERROR;
		}
		catch (Exception e) {
			errRespMsg = getText(StrutsConstants.ERROR_DATA_GET, new String[] {getText("form.forms.prepopvalue.dataelement.display")});
			logger.error("An unexpected error occured while getting a listing of pre-pop DE objects.", e);
			strutsResult = BaseAction.ERROR;
		}
		
		return strutsResult;
	}
	
	public String queryPrePopDeJsArrayByEformName() {
		String strutsResult = BaseAction.SUCCESS;
		String eFormShortName = request.getParameter("eformShortName");
		
		// Validate the eForm short name.
		if ( Utils.isBlank(eFormShortName) ) {
			errRespMsg = "Invalid eForm short name given.";
			logger.error(errRespMsg);
			return BaseAction.ERROR;
		}
		
		try {
			DictionaryWSProvider dictionaryWSProvider = new DictionaryWSProvider(request);
			List<String> deNameList = dictionaryWSProvider.getDeNamesByEformShortName(eFormShortName);
			ProtocolManager protoMan = new ProtocolManager();
			List<PrepopDataElement> prePopDeList = protoMan.getPrepopDEByShortNameList(deNameList);
			
			// Build the JSON response.
			jsonString = prepopDEListToJSONArray(prePopDeList, true).toString();
		}
		catch (CasProxyTicketException | WebApplicationException e) {
			errRespMsg = getText(StrutsConstants.ERROR_WEB_SERVICE_GET, 
					new String[] {getText("question.dataElement.name.display"), getText("webservice.dictionary.label")});
			logger.error("Couldn't generate a proxy ticket or a web service error occured while getting the list of data element shortnames.", e);
			strutsResult = BaseAction.ERROR;
		}
		catch (CtdbException ce) {
			errRespMsg = getText(StrutsConstants.ERROR_DATABASE_GET, new String[] {getText("form.forms.prepopvalue.dataelement.display")});
			logger.error("A database error occurred while getting the pre-pop DE objects.", ce);
			strutsResult = BaseAction.ERROR;
		}
		catch (JSONException je) {
			errRespMsg = getText(StrutsConstants.ERROR_DATA_GET, new String[] {getText("form.forms.prepopvalue.dataelement.display")});
			logger.error("Error occurred while converting the pre-pop DE list to JSON.", je);
			strutsResult = BaseAction.ERROR;
		}
		catch (Exception e) {
			errRespMsg = getText(StrutsConstants.ERROR_DATA_GET, new String[] {getText("form.forms.prepopvalue.dataelement.display")});
			logger.error("An unexpected error occured while getting a listing of pre-pop DE objects.", e);
			strutsResult = BaseAction.ERROR;
		}
		
		return strutsResult;
	}
	
	private boolean validateVisitType(List<PrepopDataElement> prePopDeList) {
		int originalCount = getFieldErrors().size();
		
		// Checking the "Visit Type Name" field
		if (Utils.isBlank(getName())) {
			addFieldError(getText("protocol.intervalForm.create.intervalName"), getText(
					StrutsConstants.ERROR_FIELD_REQUIRED, 
					new String[]{getText("protocol.intervalForm.create.intervalName"), "is"}));
		} else if (getName().length() > 50) {
			addFieldError(getText("protocol.intervalForm.create.intervalName"), getText(
					StrutsConstants.ERROR_MAX_LENGTH, 
					new String[]{getText("protocol.intervalForm.create.intervalName"), "50"}));
		} 
		
		// Checking the "Description" field
		if (Utils.isBlank(getDescription())) { 
			addFieldError(getText("protocol.intervalForm.create.description"), getText(
					StrutsConstants.ERROR_FIELD_REQUIRED, 
					new String[]{getText("protocol.intervalForm.create.description"), "is"}));
		} else if (getDescription().length() > 4000) {
			addFieldError(getText("protocol.intervalForm.create.description"), getText(
					StrutsConstants.ERROR_MAX_LENGTH, 
					new String[]{getText("protocol.intervalForm.create.description"), "4000"}));
		}
		
		// Checking the "Category" field
		if (!Utils.isBlank(getCategory()) && getCategory().length() > 50) {
			addFieldError(getText("protocol.intervalForm.create.category"), getText(
					StrutsConstants.ERROR_MAX_LENGTH, 
					new String[]{getText("protocol.intervalForm.create.category"), "50"}));
		}
		
		// Check if all of the selected pre-pop DEs are in the data element cache.
		/*if ( (prePopDeList != null) && !prePopDeList.isEmpty() ) {
			@SuppressWarnings("unchecked")
			Map<String, DataElement> deCache = (Map<String, DataElement>) session.get(CtdbConstants.PRE_POP_DE_CACHE_KEY);
			
			for ( PrepopDataElement pde : prePopDeList ) {
				DataElement de = deCache.get(pde.getShortName());
				
				// Check if the data element was found.
				if ( de == null ) {
					addFieldError(getText("protocol.intervalForm.fieldAutopopulationList.title"), 
							getText(StrutsConstants.ERROR_NOTFOUND, new String[] {"\"" + pde.getTitle() + "\" data element"}));
				}
			}
		}*/
		
		//check if Clinical points list is empty or not
		if (Boolean.valueOf(SysPropUtil.getProperty("display.protocol.clinicalPoint"))) {
			if (intervalClinicalPointList != null && intervalClinicalPointList.isEmpty()) {
				addFieldError(getText("protocol.intervalForm.create.clinicalPoints"),
						getText(StrutsConstants.ERROR_FIELD_REQUIRED,
								new String[] {getText("protocol.intervalForm.create.clinicalPoints"), "is"}));
			}
		}
		
		return (getFieldErrors().size() == originalCount);
	}
	
	/**
	 * Converts a list of associated eforms to the current visit type to a JSON string list version.
	 * 
	 * @param asscosiatedFroms - A list of forms associated with the current visit type
	 * @return	A string representation of the associated form list.
	 * @throws JSONException	An error occurred while creating JSON objects
	 */
	private String eformListToJSONString(List<BasicEform> asscosiatedFroms) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObj = null;
		
		for (BasicEform f : asscosiatedFroms) {
			 jsonObj = new JSONObject();
			 jsonObj.put("shortName", f.getShortName());
			 jsonObj.put("name", f.getTitle());
			 jsonObj.put("isManitory", Boolean.toString(f.getIsMandatory()));
			 jsonObj.put("orderVal", f.getOrderValue());
			 jsonObj.put("isSelfReport", Boolean.toString(f.getIsSelfReport()));
			 
			 jsonArray.put(jsonObj);
		}
		
		return jsonArray.toString() != null ? jsonArray.toString() : "[]";
	}

	/**
	 * Converts the JSON string list to a list of associated eForms for the current visit type
	 * 
	 * @param strList - A JSON string list of the forms to associate to the current visit type.
	 * @param publishedEformMap - A map of published eForms from the Dictionary.
	 * @return A listing of forms associated to the current interval
	 * @throws JSONException If an error occurred while parsing the JSON data
	 * @throws CasProxyTicketException When there is an error generating the proxy ticket for the Dictionary request for
	 *         a list of published eForms.
	 * @throws WebApplicationException When there is an error getting the list of eForms from the Dictionary web
	 *         service.
	 */
	private List<BasicEform> buildAssociatedEformList(String strList, Map<String, BasicEform> publishedEformMap) 
			throws JSONException, WebApplicationException, CasProxyTicketException {
		List<BasicEform> associatedEForms = new ArrayList<BasicEform>();
		
		// Check if string list is empty
		if ( !Utils.isBlank(strList) && !strList.equals(CtdbConstants.EMPTY_JSON_ARRAY_STR) ) {
			JSONArray input = new JSONArray(strList);
			
			for ( int i = 0; i < input.length(); i++ ) {
				JSONObject inputObj = input.getJSONObject(i);
				String shortName = inputObj.getString("shortName"); // There should always be a short name present.
				BasicEform anEform = new BasicEform(publishedEformMap.get(shortName));
				
				// Set the JSON data to a form and add it to the associatedForms list
				anEform.setTitle(inputObj.optString("name", anEform.getTitle()));
				anEform.setIsMandatory(Boolean.valueOf(inputObj.optBoolean("isManitory", false)));
				anEform.setIsSelfReport(Boolean.valueOf(inputObj.optBoolean("isSelfReport", false)));
				anEform.setOrderValue(Integer.valueOf(inputObj.optInt("orderVal", 0)));
				
				associatedEForms.add(anEform);
			}
		}
		
		return associatedEForms;
	}
	
	/**
	 * Converts a list of BasicEform objects to a map with the short name of each BasicEform as the key.
	 * 
	 * @param basicEforms - The list of BasicEform objects to convert.
	 * @return A map of BasicEform objects contained in the given list with the short name of each object being the key.
	 */
	private Map<String, BasicEform> eFromListToMap(List<BasicEform> basicEforms) {
		Map<String, BasicEform> eFormMap = new HashMap<String, BasicEform>(basicEforms.size());
		
		// Add all of the eForms to the map with the short name as the key.
		for ( BasicEform bef : basicEforms ) {
			eFormMap.put(bef.getShortName(), bef);
		}
		
		return eFormMap;
	}
	
	/**
	 * Converts the JSON string list to a list of associated pre-populate data element names for the current visit type
	 * 
	 * @param strList - A JSON string list of the pre-populate data element names to associate to the current visit type
	 * @return 	A listing of forms associated to the current interval
	 * @throws NumberFormatException	If there is an invalid form ID in the string list
	 * @throws JSONException If an error occurred while parsing the JSON data
	 * @throws CtdbException When there is in error while getting the PrepopDataElement object from the database.
	 */
	private List<PrepopDataElement> buildAssociatedPrepopDEList(String strList) 
			throws NumberFormatException, JSONException, CtdbException {
		List<PrepopDataElement> prepopDEList = new ArrayList<PrepopDataElement>();
		
		// Check if string list is empty
		if (!Utils.isBlank(strList) && !strList.equals(CtdbConstants.EMPTY_JSON_ARRAY_STR)) {
			JSONArray jsArray = new JSONArray(strList);
			ProtocolManager protoMan = new ProtocolManager();
			
			for (int i = 0; i < jsArray.length(); i++) {
				JSONObject prePopJson = jsArray.getJSONObject(i);
				
				// Add only the selected pre-pop data elements.
				if ( prePopJson.getBoolean("isSelected") ) {
					String shortName = prePopJson.getString("shortName");
					PrepopDataElement prepopDE = protoMan.getPrepopDEByShortName(shortName);
					
					prepopDEList.add(prepopDE);
				}
			}
			
			Collections.sort(prepopDEList, new PrePopDataElementComparator());
		}
		
		return prepopDEList;
	}
	
	/**
	 * Converts a list of PrepopDataElement objects to a JSON array string representation that will be usable by the visit type
	 * create/edit page.
	 * 
	 * @param prepopDEList - A list of PrepopDataElement objects to convert.
	 * @param defaultSelect - The default selection state for changeable pre-population data elements.
	 * @return The JSON array string representation of the passed in PrepopDataElement list.
	 * @throws JSONException When there is an error while converting the list.
	 */
	private JSONArray prepopDEListToJSONArray(List<PrepopDataElement> prepopDEList, boolean defaultSelect) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		
		for (PrepopDataElement prepopDE : prepopDEList) {
			JSONObject jsonObj = new JSONObject();
			 
			jsonObj.put("id", prepopDE.getId());
			jsonObj.put("shortName", prepopDE.getShortName());
			jsonObj.put("title", prepopDE.getTitle());
			
			if ( isPrePopDeUnchangeable(prepopDE.getShortName()) ) {
				jsonObj.put("isUnchangeable", true);
				jsonObj.put("isSelected", true);
			}
			else {
				jsonObj.put("isUnchangeable", false);
				jsonObj.put("isSelected", defaultSelect);
			}
			 
			jsonArray.put(jsonObj);
		}
		
		return jsonArray;
	}
	
	/**
	 * Determines whether or not a pre-population data element value can be changed by the user.
	 * 
	 * @param deName - The short name of the pre-population data element.
	 * @return True if and only if the given short name matches pre-population data elements that cannot be changed by the user.
	 */
	private boolean isPrePopDeUnchangeable(String deName) {
		return deName.equals("GUID") || deName.equals("VisitType") || deName.equals("ClinicalName") || deName.equals("VisitDate");
	}
	
	/**
	 * Converts a list of Clinical objects to a JSON array string representation that will be usable by the visit type
	 * create/edit page.
	 * 
	 * @param ClinicalList - A list of Clinical objects to convert.
	 * @return The JSON array string representation of the passed in PrepopDataElement list.
	 * @throws JSONException When there is an error while converting the list.
	 */
	private JSONArray clinicalLocListToJSONArray(List<ClinicalLocation> clinicalLocList) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		
		for (ClinicalLocation Clinical : clinicalLocList) {
			JSONObject jsonObj = new JSONObject();
			 
			jsonObj.put("id", Clinical.getId());
			jsonObj.put("name", Clinical.getName());
			 
			jsonArray.put(jsonObj);
		}
	
		return jsonArray;
	}
	
	private JSONArray procedureListTOJSONArray(List<Procedure> procedureList) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		List<ProcedureType> procTypeList = new ArrayList<ProcedureType>();

		for (Procedure procedure : procedureList) {
			if(!procTypeList.contains(procedure.getProcedureType())) {
				procTypeList.add(procedure.getProcedureType());		
			}
		}

		Collections.sort(procTypeList, new Comparator<ProcedureType>() {
			@Override
			public int compare(ProcedureType procT1, ProcedureType procT2) {
				return procT1.getName().compareTo(procT2.getName());
			}
		});
		
		for(ProcedureType procType : procTypeList){
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("proceduretype", procType.getName());
			
			JSONArray innerJsonArr = new JSONArray();
			for (Procedure procedure : procedureList) {
				if(procedure.getProcedureType() == procType){
					JSONObject innerJsonObj = new JSONObject();
					innerJsonObj.put("id", procedure.getId());
					innerJsonObj.put("name", procedure.getName());
					innerJsonArr.put(innerJsonObj);
				}
			}
			
			jsonObj.put("procedureList", innerJsonArr);
			jsonArray.put(jsonObj);
		}
		
		return jsonArray;
	}
	
	private JSONArray pocListTOJSONArray(List<PointOfContact> pocList) throws JSONException {
		JSONArray jsonArr = new JSONArray();
		
		for(PointOfContact poc : pocList){
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("id", poc.getId());
			jsonObj.put("fullname", poc.getFullName());
			jsonObj.put("phone", poc.getPhone());
			
			jsonArr.put(jsonObj);
		}
		
		return jsonArr;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getStudyId() {
		return studyId;
	}

	public void setStudyId(int studyId) {
		this.studyId = studyId;
	}

	public int getIntervalType() {
		return intervalType;
	}

	public void setIntervalType(int intervalType) {
		this.intervalType = intervalType;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSelectedForms() {
		return selectedForms;
	}

	public void setSelectedForms(String selectedForms) {
		this.selectedForms = selectedForms;
	}

	public List<CtdbLookup> getIntervalTypeList() {
		return intervalTypeList;
	}

	public void setIntervalTypeList(List<CtdbLookup> intervalTypeList) {
		this.intervalTypeList = intervalTypeList;
	}

	public int getVisitTypeSelfReportStart() {
		return visitTypeSelfReportStart;
	}

	public void setVisitTypeSelfReportStart(int visitTypeSelfReportStart) {
		this.visitTypeSelfReportStart = visitTypeSelfReportStart;
	}

	public int getVisitTypeSelfReportEnd() {
		return visitTypeSelfReportEnd;
	}

	public void setVisitTypeSelfReportEnd(int visitTypeSelfReportEnd) {
		this.visitTypeSelfReportEnd = visitTypeSelfReportEnd;
	}
	
	public String getPrepopDataElements() {
		return prepopDataElements;
	}
	
	public void setPrepopDataElements(String prepopDataElements) {
		this.prepopDataElements = prepopDataElements;
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


	public String geteFormShortNames() {
		return eFormShortNames;
	}

	public void seteFormShortNames(String eFormShortNames) {
		this.eFormShortNames = eFormShortNames;
	}

	public String getIntervalClinicals() {
		return intervalClinicals;
	}
	
	public void setIntervalClinicals(String intervalClinicals) {
		this.intervalClinicals = intervalClinicals;
	}
	
	public String getIntervalProcedures() {
		return intervalProcedures;
	}
	
	public void setIntervalProcedures(String intervalProcedures) {
		this.intervalProcedures = intervalProcedures;
	}
	
	public String getIntervalPointOfContacts() {
		return intervalPointOfContacts;
	}
	
	public void setIntervalPointOfContacts(String intervalPointOfContacts) {
		this.intervalPointOfContacts = intervalPointOfContacts;
	}
	
	public List<IntervalClinicalPoint> getIntervalClinicalPointList() {
		return intervalClinicalPointList;
	}
	
	public void setIntervalClinicalPointList(List<IntervalClinicalPoint> intervalClinicalPointList) {
		this.intervalClinicalPointList.clear();
		
		if (intervalClinicalPointList != null) {
			this.intervalClinicalPointList.addAll(intervalClinicalPointList);
		}
	}
	
	public String getIntervalClinicalPoints() {
		return intervalClinicalPoints;
	}
	
	public void setIntervalClinicalPoints(String intervalClinicalPoints) {
		this.intervalClinicalPoints = intervalClinicalPoints;
	}
	
	public String getIntervalClinicalPOCStr() {
		return intervalClinicalPOCStr;
	}
	
	public void setIntervalClinicalPOCStr(String intervalClinicalPOCStr) {
		this.intervalClinicalPOCStr = intervalClinicalPOCStr;
	}
}