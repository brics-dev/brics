package gov.nih.nichd.ctdb.workspace.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CasProxyTicketException;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.FormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.form.util.FormDataStructureUtility;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.site.domain.Site;
import gov.nih.nichd.ctdb.workspace.domain.EformsSelected;
import gov.nih.nichd.ctdb.workspace.domain.ViewDataGraph;
import gov.nih.nichd.ctdb.workspace.domain.ViewDataGraphInput;
import gov.nih.nichd.ctdb.workspace.manager.ViewDataReportingManager;

public class ViewDataDashboardAction extends BaseAction {

	private static final long serialVersionUID = 1957296955585448073L;
	private static final Logger logger = Logger.getLogger(ViewDataDashboardAction.class);

	public static final String VIEWDATA_KEY="viewDataAction";
	private String jsonData = "";
	private String errRespMsg;

	public String execute() throws Exception {
		buildLeftNav(LeftNavController.LEFTNAV_HOME);
		this.retrieveActionMessages(VIEWDATA_KEY);
		this.retrieveActionErrors(VIEWDATA_KEY);
		
		return "viewData";
	}
    public String populateViewData(){
    	
    	try {
	    	Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			if (protocol == null) {
				return StrutsConstants.SELECTPROTOCOL;
			}
			ProtocolManager pm = new ProtocolManager();
			
			List<Interval> intervals = pm.getIntervalsOrderByOrderval(protocol.getId());
			JSONArray intervalJsonArray = new JSONArray();
			for (Interval interv : intervals) {
				JSONObject intervalJSON = new JSONObject();
				intervalJSON.put("id", interv.getId());
				intervalJSON.put("name", interv.getName());				
				intervalJsonArray.put(intervalJSON);
			}

			JSONArray siteJsonArray = new JSONArray();
			for ( Site s : protocol.getStudySites() ) {
				JSONObject siteJsonObj = new JSONObject();
				siteJsonObj.put("id", s.getId());
				siteJsonObj.put("name", s.getName());
				siteJsonArray.put(siteJsonObj);
			}
						
			JSONObject viewData = new JSONObject();
			viewData.put("intervalOptions",intervalJsonArray);
			viewData.put("siteOptions", siteJsonArray);
			
			jsonData = viewData.toString();
			
    	}catch (CtdbException ce) {
    		logger.error(ce.getMessage(), ce);
    		addActionError(getText(StrutsConstants.ERROR_DATABASE_GET, new String[] {"View Data Chart"}));
    		JSONObject errorObj = new JSONObject();
			try {
				errorObj.put("backEndErrors", "An error occurred while retrieving data from the database.If the problem persists, please contact your system administrator.");
				errRespMsg = errorObj.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return ERROR;
		} catch (JSONException je) {
			logger.error(je.getMessage(), je);
			JSONObject errorObj = new JSONObject();
			try {
				errorObj.put("backEndErrors", "An error occurred while retrieving data.If the problem persists, please contact your system administrator.");
				errRespMsg = errorObj.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return ERROR;
		}
    	return "viewData";
    }
    
	public String geteFormsForInterval() {
	
		String intervalIds = request.getParameter("intervalIds");
		
		boolean calcRuleExists = false;
		JSONArray eformJSONArr = new JSONArray();
		List <Form> formList = new ArrayList<Form>();
		ViewDataReportingManager vdr = new ViewDataReportingManager();
		FormDataStructureUtility formUtil = new FormDataStructureUtility(); 
		
		try {
			Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			if (protocol == null) {
				return StrutsConstants.SELECTPROTOCOL;
			}
		
			JSONArray intervalIdsArr = new JSONArray(intervalIds);
			List<Integer> ids = convertJSONArrayToList(intervalIdsArr);
			
			JSONArray jsonarray = vdr.geteFormsForInterval(protocol.getId(), ids);
			formList = formUtil.getEformsForShortnamesFromBrics(request, jsonarray.toString());
			
			for(Form form: formList) {
				calcRuleExists = false;
				JSONArray deArray = new JSONArray();
				HashMap<Integer, Section>sectionMap = form.getSectionMap();
				for(Map.Entry<Integer, Section> entry: sectionMap.entrySet()) {
					Section section = entry.getValue();
					for(Question question : section.getQuestionList()) {
						FormQuestionAttributes qa = question.getFormQuestionAttributes();
						if(qa.isCalculatedQuestion()) {
							calcRuleExists = true;
							JSONObject deWithCalcRule = new JSONObject();
							deWithCalcRule.put("qid", question.getId());
							deWithCalcRule.put("dename", qa.getDataElementName());
							deArray.put(deWithCalcRule);
						}
					}
				}
				if(calcRuleExists) {
					JSONObject eformJson = new JSONObject();
					eformJson.put("name", form.getName());
					eformJson.put("shortName", form.getShortName());
				
					eformJson.put("deWithCalcRule", deArray);
					logger.debug("Eform details: "+eformJson);
					eformJSONArr.put(eformJson);
				}
			}
			jsonData = eformJSONArr.toString();
			
		}catch (WebApplicationException we) {
			logger.error(we.getMessage(),we);
			JSONObject errorObj = new JSONObject();
			try {
				errorObj.put("backEndErrors", "An error has occurred in retrieving the data from Dictionary. If the problem persists, please contact the system administrator.");
				errRespMsg = errorObj.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return ERROR;
		} catch (CasProxyTicketException cae) {
			logger.error("A proxy ticket could not be created for a request to the Dictionary web service.", cae);
			JSONObject errorObj = new JSONObject();
			try {
				errorObj.put("backEndErrors", "An error has occurred in retrieving data from Dictionary. If the problem persists, please contact the system administrator.");
				errRespMsg = errorObj.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return ERROR;
		} catch (JSONException je) {
			logger.error(je.getMessage(),je);
			JSONObject errorObj = new JSONObject();
			try {
				errorObj.put("backEndErrors", "An error has occurred in retrieving the eForms from Dictionary. If the problem persists, please contact the system administrator.");
				errRespMsg = errorObj.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return ERROR;
		} catch (CtdbException ce) {
			logger.error(ce.getMessage(), ce);
			JSONObject errorObj = new JSONObject();
			try {
				errorObj.put("backEndErrors", "An error occurred while retrieving data from the database. If the problem persists, please contact your system administrator.");
				errRespMsg = errorObj.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return ERROR;
		} catch ( Exception e) {
			logger.error(e.getMessage(),e);
			JSONObject errorObj = new JSONObject();
			try {
				errorObj.put("backEndErrors", "An error occurred while retrieving data. If the problem persists, please contact your system administrator.");
				errRespMsg = errorObj.toString();
			} catch (JSONException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
		return "viewData";
	}
	
	public String viewDataGraph() {
		
		String siteIds = request.getParameter("siteIds").replaceAll("^\"|\"$", "");
		String guidId = request.getParameter("guidId").replaceAll("^\"|\"$", "");
		String intervalIds = request.getParameter("intervalIds").replaceAll("^\"|\"$", "");
		String eforms = request.getParameter("eforms").replaceAll("^\"|\"$", "");
		
		JSONArray array = new JSONArray();
		JSONObject item = new JSONObject();
		
		try {
			Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			if (protocol == null) {
				return StrutsConstants.SELECTPROTOCOL;
			}
			ViewDataGraphInput viewDataGraphInput = convertToPojo(siteIds, guidId, intervalIds, eforms);
			ViewDataReportingManager viewDataReporting = new ViewDataReportingManager();
			List<ViewDataGraph> vdGraphList = viewDataReporting.getGraphData(protocol.getId(), viewDataGraphInput);
			/*if(vdGraphList.isEmpty()) {
				errRespMsg = "bad request";
				return StrutsConstants.BAD_REQUEST;
			}*/
			
			for(ViewDataGraph vd: vdGraphList) {
				item = new JSONObject();
				item.put("ename", vd.getEformName());
				item.put("dename", vd.getDename());
				item.put("inames", vd.getIntervalsJSON());
				item.put("scores", vd.getScoresJSON());
				array.put(item);
			}
			jsonData = array.toString();
		} catch (JSONException je) {
			logger.error(je.getMessage(), je);
			JSONObject errorObj = new JSONObject();
			try {
				errorObj.put("backEndErrors", "Error in getting data for the chart(s). If the problem persists, please contact the system administrator.");
				errRespMsg = errorObj.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return ERROR;
		} catch (CtdbException ce) {
			logger.error(ce.getMessage(), ce);
			JSONObject errorObj = new JSONObject();
			try {
				errorObj.put("backEndErrors", "An error ocurred in retrieving chart data from the database. If the problem persists, please contact the system administrator.");
				errRespMsg = errorObj.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return ERROR;
		}
		
		return "viewData";
	}
	private ViewDataGraphInput convertToPojo(String siteIds, String guidId, String intervalIds, String eforms) throws JSONException{
		
		ViewDataGraphInput viewDataGraphInput = new ViewDataGraphInput();
		if (StringUtils.isNotBlank(siteIds)){
			viewDataGraphInput.setSiteIds(convertJSONArrayToList(siteIds));
		} else {
			viewDataGraphInput.setSiteIds(new ArrayList<Integer>());
		}
		if(StringUtils.isNotBlank(guidId)){
			viewDataGraphInput.setGuidId(Integer.valueOf(guidId));
		}
		if (StringUtils.isNotBlank(intervalIds)) {
			viewDataGraphInput.setIntervalIds(convertJSONArrayToList(intervalIds));
		}
		if (StringUtils.isNotBlank(eforms)) {
			List<EformsSelected> selectedEforms = new ArrayList<EformsSelected>();
			JSONArray eformJSONArray = new JSONArray(eforms);
			
			for (int i=0; i<eformJSONArray.length(); i++) {
				
				JSONObject eformJSONObj = eformJSONArray.getJSONObject(i);
				EformsSelected eform = new EformsSelected();
				eform.setName(eformJSONObj.getString("name"));
				eform.setShortName(eformJSONObj.getString("shortName"));
				JSONArray dataElementsArr = eformJSONObj.getJSONArray("deQidArr");
				eform.setDeJson(dataElementsArr);
				selectedEforms.add(eform);
			}
			viewDataGraphInput.setEforms(selectedEforms);
			
		}
		return viewDataGraphInput;
	}
	private List<Integer> convertJSONArrayToList(String jsonArrayStr) throws JSONException{
		JSONArray jsonArray = new JSONArray(jsonArrayStr);
		return convertJSONArrayToList(jsonArray);
	}
	
	private List<Integer> convertJSONArrayToList(JSONArray jsonArray) throws JSONException{
		List<Integer> returnList = new ArrayList<>();
		for (int i=0; i<jsonArray.length(); i++) {
			returnList.add(jsonArray.getInt(i));
		}
		return returnList;
	}
	
	public String getVisitTypeListByGuid() {
		
    	try {
	    	Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			if (protocol == null) {
				return StrutsConstants.SELECTPROTOCOL;
			}
			String patientIdStr = request.getParameter("guidId");
			Integer patientId = Integer.valueOf(patientIdStr);
			
			JSONArray intervalJsonArray = new JSONArray();
			if(patientIdStr != null && patientId > 0){
				ProtocolManager pm = new ProtocolManager();			
				List<Interval> intervals = pm.getIntervalsByPatieint(protocol.getId(), patientId);
	
				
				for (Interval interv : intervals) {
					JSONObject intervalJSON = new JSONObject();
					intervalJSON.put("id", interv.getId());
					intervalJSON.put("name", interv.getName());
					
					intervalJsonArray.put(intervalJSON);
				}
			}
			jsonData = intervalJsonArray.toString();
			
    	}catch (JSONException je) {
			logger.error(je.getMessage(),je);
			errRespMsg = "Error in getting Visit Type by GUID";
			return ERROR;
		}catch (CtdbException ce){
    		logger.error(ce.getMessage(), ce);
    		errRespMsg = "Error in getting Visit Type by GUID from the database";
    		return ERROR;
		}
    	return "viewData";
	}
	
	public String getGuidForViewData() {
		
    	try {
	    	Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			if (protocol == null) {
				return StrutsConstants.SELECTPROTOCOL;
			}
			
	    	List<Patient> patList = new ArrayList<Patient>();
			PatientManager patMan = new PatientManager();			
			patList = patMan.getPatientsInDataCollectionByProtocol(protocol.getId());
			JSONArray guidsJsonArray = new JSONArray();
			for ( Patient p : patList ) {
				JSONObject guidJsonObj = new JSONObject();
				guidJsonObj.put("id", p.getId());
				guidJsonObj.put("guid", p.getGuid());				
				guidsJsonArray.put(guidJsonObj);
			}
			jsonData = guidsJsonArray.toString();
			
    	}catch (CtdbException ce){
    		logger.error(ce.getMessage(),ce);
    		errRespMsg = "Error in getting GUID from the database for View Data chart";
    		return ERROR;
		}catch(JSONException je) {
			logger.error(je.getMessage(), je);
			errRespMsg = "Error in getting GUID for View Data chart";
			return ERROR;
		}
    	
    	return "viewData";
	}

	public String getGuidsForSelectedSites() {

		try {
			String selectedSiteIds = request.getParameter("selectedSiteIds");
			JSONArray siteIdsJsonArr = new JSONArray(selectedSiteIds);
			JSONArray guidsJsonArray = new JSONArray();

			if (selectedSiteIds != null && siteIdsJsonArr.length() > 0) {
				Integer[] siteIds = new Integer[siteIdsJsonArr.length()];
				for (int i = 0; i < siteIdsJsonArr.length(); i++) {
					siteIds[i] = siteIdsJsonArr.getInt(i);
				}

				List<Patient> patList = new ArrayList<Patient>();

				Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
				if (protocol != null) {
					PatientManager patMan = new PatientManager();
					patList = patMan.getPatientListByProtocolAndSites(protocol.getId(), siteIds);
					for (Patient p : patList) {
						JSONObject guidJsonObj = new JSONObject();
						guidJsonObj.put("id", p.getId());
						guidJsonObj.put("guid", p.getGuid());
						guidsJsonArray.put(guidJsonObj);
					}
				} else {
					errRespMsg = "No Protocol";
					return ERROR;
				}

				jsonData = guidsJsonArray.toString();
			}
		} catch (CtdbException ce) {
			logger.error(ce.getMessage(), ce);
			errRespMsg = "Error in getting GUIDs for the selected sites in View Data chart";
			return ERROR;
		} catch (JSONException je) {
			logger.error(je.getMessage(), je);
			errRespMsg = "Error in getting GUIDs for the selected sites in View Data chart";
			return ERROR;
		}
		return "viewData";
	}

	public String getJsonData() {
		return jsonData;
	}

	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}
	public String getErrRespMsg() {
		return errRespMsg;
	}
	public void setErrRespMsg(String errRespMsg) {
		this.errRespMsg = errRespMsg;
	}

}
