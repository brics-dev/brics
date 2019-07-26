package gov.nih.nichd.ctdb.workspace.action;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.eclipse.jetty.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;
import gov.nih.nichd.ctdb.protocol.action.ProtocolAdminAction;
import gov.nih.nichd.ctdb.protocol.common.ProtocolConstants;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.response.domain.AdverseEvent;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.nichd.ctdb.site.domain.Site;
import gov.nih.nichd.ctdb.workspace.domain.DashboardChartFilter;
import gov.nih.nichd.ctdb.workspace.domain.DashboardDataCollectionStatus;
import gov.nih.nichd.ctdb.workspace.domain.ProtocolCollectionStatusVsEformCount;
import gov.nih.nichd.ctdb.workspace.domain.ProtocolVisitNameVsSubjectCountCharts;
import gov.nih.nichd.ctdb.workspace.manager.DashboardReportingManager;
import gov.nih.nichd.ctdb.workspace.util.AdverseEventJson;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

public class MyWorkspaceAction extends BaseAction {
	private static final long serialVersionUID = 264237781763391123L;
	private static final Logger logger = Logger.getLogger(MyWorkspaceAction.class);
	private String viewDataJson = CtdbConstants.EMPTY_JSON_ARRAY_STR;
	
	private DashboardReportingManager dashboardReporting = new DashboardReportingManager();

	//Dashboard data
	private String populationList = CtdbConstants.EMPTY_JSON_ARRAY_STR;
	private String jsonList = CtdbConstants.EMPTY_JSON_ARRAY_STR;
	private String errRespMsg;
	
	//colors in dashboard
	public static final String DASHBOARD_ALL_COLUMN_BlUE="#79B4EC";
	public static final String DASHBOARD_GUID_COLUMN_DARDBlUE="#1D76C9";


	public String execute() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.addHeader("X-UA-Compatible", "IE=edge");
                
    	// Clear out some study session variables
    	session.remove(CtdbConstants.CURRENT_PROTOCOL_STUDY_SITES_SESSION_KEY);
		session.remove("xinstitutes");
		session.remove("xProtocolStatus");
		session.remove("xProtocolTypes");
		session.remove("xuserlist");
		session.remove("__SiteHome_states");
		session.remove("_availiableDefaults");
		session.remove("_btrisAccess");
		session.remove(CtdbConstants.SITE_HASH_MAP);
		session.remove(CtdbConstants.DRUG_DEVICE_HASH_MAP);
		session.remove(ProtocolConstants.BRICS_STUDY_HASHTABLE);
		
		this.retrieveActionMessages(ProtocolAdminAction.ACTION_MESSAGES_KEY);

		// get the list of user's protocols from the session
        SecuritySessionUtil securitySession = new SecuritySessionUtil(request);  // Constructor auto-refreshes the session's protocol list.
        
        try {
        	// handle switching protocols
        	Protocol protocol = securitySession.switchProtocol();
	    				
            if (protocol == null) {
				buildLeftNav(LeftNavController.LEFTNAV_PICKSTUDY,
						new int[] {LeftNavController.LEFTNAV_SUBJECTS_MANAGE, LeftNavController.LEFTNAV_COLLECT,
								LeftNavController.LEFTNAV_FORM_HOME, LeftNavController.LEFTNAV_STUDY_HOME});
          
            }
            else {
            	// we have a protocol selected, so show the protocol specific workspace
            	buildLeftNav(LeftNavController.LEFTNAV_HOME);
            }
                        
        }
        catch (Exception e) {
        	logger.error("Could not setup the dashboard page.", e);
        	return StrutsConstants.FAILURE;
        }
        
        return SUCCESS;
	}
	
	public String getQaAlertsList() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			 ArrayList outputList = new ArrayList();
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Method to send JSON chart data to JSP
	 * @return JSON list
	 */
	public String reportingDashboard()  throws JSONException, ObjectNotFoundException, CtdbException{
		String params = request.getParameter("params");
		DashboardChartFilter chartFilter = convertParamsToChartFilterObj(params);
		Integer studyId = chartFilter.getCurrentStudyId();
		Integer siteId = chartFilter.getSelectedSiteId();
		Integer guidId = chartFilter.getSelectedGuidId();
		Integer statusId = chartFilter.getSelectedStatusId();
	
		Protocol protocol = null;   	
    	if(studyId != null) {
    		if(studyId > 0){
    			ProtocolManager protoMan = new ProtocolManager();
    			protocol = protoMan.getProtocol(studyId);
    		}
    	}
		Integer protocolId = null;
		if(protocol!=null) {
			 protocolId = protocol.getId();
		}

		/*filtered by filters*/
		String dataName = "";
		String dataId = "";
		
		
		JSONArray chartArray = new JSONArray();
		List<String> vtBySiteCateSet = new ArrayList<String>();
		JSONArray vtBySiteCategoryArr = new JSONArray();
		if(siteId != null){
//			SiteManager siteMan = new SiteManager();
//			String siteName = "Site - " + siteMan.getSite(siteId).getName();
			dataId = "site";
			DashboardChartFilter protocolSiteFilter = new DashboardChartFilter(chartFilter.getCurrentStudyId(), chartFilter.getSelectedSiteId(), null, null);
			vtBySiteCateSet = dashboardReporting.getCategorySetByChartFilter(protocolSiteFilter);
			
			for(String cateStr : vtBySiteCateSet){
				vtBySiteCategoryArr.put(cateStr);
			}
			
			List<DashboardDataCollectionStatus> siteCollStatusList = this.getSiteCollStatusList();
			for(DashboardDataCollectionStatus siteCollStatus : siteCollStatusList){
				protocolSiteFilter.setSelectedStatusId(siteCollStatus.getId());
				List<ProtocolVisitNameVsSubjectCountCharts> repoList = new ArrayList<ProtocolVisitNameVsSubjectCountCharts>();
				if(!siteCollStatus.getName().equals(CtdbConstants.DATACOLLECTION_STATUS_NOTSTARTED) ) { 
					repoList = dashboardReporting.getStartedPVCountByChartFilter(protocolSiteFilter);
				} else {
					repoList = dashboardReporting.getNotStatedPVCountByChartFilter(protocolSiteFilter);
				}
				dataName = siteCollStatus.getName();
				JSONObject vTBySiteJsonObj = convertDataListToChartData(dataId, dataName, repoList, protocolSiteFilter, vtBySiteCategoryArr);
				chartArray.put(vTBySiteJsonObj);
			}
//			dataName = siteName;
		}
		

		JSONArray vtByGuidCategoryArr = new JSONArray();
		List<String> vtByGuidCateSet = new ArrayList<String>();
		if(guidId != null){ 
			/*site != null && guid != null*/
			PatientManager patMan = new PatientManager();
			String guid = patMan.getPatient(String.valueOf(guidId)).getGuid();
//			if(dataName.length() > 0){
//				dataName += " | " + guid + " ";
//			} else {
				dataName = guid;
//			}
			dataId = siteId == null ? "guid" : "siteGuid";
			DashboardChartFilter protoSiteGuidFilter = new DashboardChartFilter(chartFilter.getCurrentStudyId(), chartFilter.getSelectedSiteId(), chartFilter.getSelectedGuidId(), null);
			vtByGuidCateSet = dashboardReporting.getCategorySetByChartFilter(protoSiteGuidFilter);
			if(vtBySiteCategoryArr.length() > 0){
				vtByGuidCategoryArr = vtBySiteCategoryArr;
			} else {
				for(String cateStr : vtByGuidCateSet){
					vtByGuidCategoryArr.put(cateStr);
				}
			}
			
			List<ProtocolVisitNameVsSubjectCountCharts> repoList = dashboardReporting.getOverallPVCountByChartFilter(protoSiteGuidFilter);
			JSONObject vTByGuidJsonObj = new JSONObject();
			if(vtBySiteCategoryArr.length() > 0) {
				vTByGuidJsonObj = convertDataListToChartData(dataId,dataName, repoList, protoSiteGuidFilter, vtBySiteCategoryArr);
			} else {
				vTByGuidJsonObj = convertDataListToChartData(dataId,dataName, repoList, protoSiteGuidFilter, vtByGuidCategoryArr);
			}
			chartArray.put(vTByGuidJsonObj);
		}
		
		if(statusId != null){
			DashboardDataCollectionStatus collStatus = DashboardDataCollectionStatus.getById(statusId);
			String status = collStatus.getName();
//			if(dataName.length() > 0){
//				dataName += " | " + status;
//			} else {
				dataName = status;
//			}
			
			dataId = "";
			if (siteId == null && guidId == null){
				dataId = "status";
			} /*else if (siteId != null && guidId == null) {
				dataId = "siteStatus";
			}*/ else if (siteId == null && guidId != null) {
				dataId = "guidStatus";
			} /*else if (siteId != null && guidId != null) {
				dataId = "siteGuidStatus";
			}*/
			/*data from selected collection status will take the category from the data filtered by site or guid
			 * if the category from site or guid is longer that the one getting by collection status*/
			JSONArray vtByStatusCateArr = new JSONArray();
			List<String> vtByStatusCateSet = new ArrayList<String>();
			if(chartFilter.getSelectedStatusId() == 0) {
				/*if status id is 0, reset it to be null in order to get the visit type category in orders */
				DashboardChartFilter cateChartFilter = new DashboardChartFilter(chartFilter.getCurrentStudyId(), chartFilter.getSelectedSiteId(), chartFilter.getSelectedGuidId(), null);
				vtByStatusCateSet = dashboardReporting.getCategorySetByChartFilter(cateChartFilter);
			} else {
				vtByStatusCateSet = dashboardReporting.getCategorySetByChartFilter(chartFilter);
			}

			if(vtBySiteCategoryArr.length() > 0) {
				vtByStatusCateArr = vtBySiteCategoryArr;
			} else if (vtByGuidCategoryArr.length() > 0) {
				vtByStatusCateArr = vtByGuidCategoryArr;
			} else {
				for(String cateStr : vtByStatusCateSet){
					vtByStatusCateArr.put(cateStr);
				}
			}
			List<ProtocolVisitNameVsSubjectCountCharts> repoList = new ArrayList<ProtocolVisitNameVsSubjectCountCharts>();
			if(!collStatus.getName().equals(CtdbConstants.DATACOLLECTION_STATUS_NOTSTARTED)){
				repoList = dashboardReporting.getStartedPVCountByChartFilter(chartFilter);
			} else {
				repoList = dashboardReporting.getNotStatedPVCountByChartFilter(chartFilter);
			}
			JSONObject vTByStatusJsonObj = convertDataListToChartData(dataId,dataName, repoList, chartFilter, vtByStatusCateArr);
			chartArray.put(vTByStatusJsonObj);
		}
		if(siteId == null && guidId == null && statusId == null) {
			/*site == null && guid == null*/
			dataName = "All";
			dataId = "all";
			JSONArray vtByAllCateArr = new JSONArray();
			List<String> vtByAllCateSet = dashboardReporting.getCategorySetByChartFilter(chartFilter);

			for(String cateStr : vtByAllCateSet) {
				if(!vtByAllCateArr.toString().contains(cateStr)) {
					vtByAllCateArr.put(cateStr);
				}
			}
			List<ProtocolVisitNameVsSubjectCountCharts> repoList = dashboardReporting.getOverallPVCountByChartFilter(chartFilter);
			JSONObject allVTJsonObj = convertDataListToChartData(dataId, dataName, repoList, chartFilter, vtByAllCateArr);
			chartArray.put(allVTJsonObj);
		} 		

		this.populationList = chartArray.toString();

		return "jsonData";
	}
	
	public JSONObject convertDataListToChartData(String dataId, String dataName, List<ProtocolVisitNameVsSubjectCountCharts> repoDataList, 
			DashboardChartFilter chartFilter, JSONArray vtCategoryArr) 
			throws JSONException, ObjectNotFoundException, CtdbException{
		JSONObject visitTypeJsonObj = new JSONObject();

		
		int stack = 0;
		int yAxis = 0;
		if(dataId.equals("siteGuid") || dataId.equals("guidStatus")) {
			stack = 1;
		}

		if(dataId.equals("site")){
			dataId += String.valueOf(chartFilter.getSelectedStatusId());
			String siteStatusColor = DashboardDataCollectionStatus.getById(chartFilter.getSelectedStatusId()).getAssoColor();
			visitTypeJsonObj.put("color", siteStatusColor);
		}
		if(dataId.toLowerCase().contains("guid")){
			visitTypeJsonObj.put("color", MyWorkspaceAction.DASHBOARD_GUID_COLUMN_DARDBlUE);
		}
		if(dataId.toLowerCase().contains("status")){
			String siteStatusColor = DashboardDataCollectionStatus.getById(chartFilter.getSelectedStatusId()).getAssoColor();
			visitTypeJsonObj.put("color", siteStatusColor);
		}
		if(dataId.equals("all")){
			visitTypeJsonObj.put("color", MyWorkspaceAction.DASHBOARD_ALL_COLUMN_BlUE);
		}
		visitTypeJsonObj.put("id", dataId);
		visitTypeJsonObj.put("name", dataName);
		visitTypeJsonObj.put("stack", stack);
		visitTypeJsonObj.put("yAxis", yAxis);
		
		JSONArray vtDataJsonArr = new JSONArray();
		JSONArray vtCurrentCateJsonArr = new JSONArray();

		//making the dataJsonArr to be the same length as the one filtered by site only
		for (ProtocolVisitNameVsSubjectCountCharts aModel:repoDataList) {
			vtCurrentCateJsonArr.put(aModel.getVisitType());
			//ensure this JSONArray contains all categories from both filtered lists
			if(!vtCategoryArr.toString().contains(aModel.getVisitType())) {
				vtCategoryArr.put(aModel.getVisitType());
			}
		}

		for(int i = 0; i < vtCategoryArr.length(); i++){
			String vtName = (String) vtCategoryArr.get(i);

//			Pattern sitePatt = Pattern.compile("^site[0-9]{1}$"); 
//			Matcher siteMatch = sitePatt.matcher(dataId);
			if(dataId.matches("^site[0-9]{1}$") && //do followings only for site column
					(vtCurrentCateJsonArr.length() == 0 || !vtCurrentCateJsonArr.toString().contains(vtName))){
				JSONObject vtEmptyJsonObj = new JSONObject();
				vtEmptyJsonObj.put("name", "");
				vtEmptyJsonObj.put("y", 0);
				vtDataJsonArr.put(vtEmptyJsonObj);
			} else {
				for (ProtocolVisitNameVsSubjectCountCharts aModel:repoDataList) {
					if(vtName.equals(aModel.getVisitType())){
						JSONObject vtCountJsonObj = new JSONObject();
			
						vtCountJsonObj.put("name", aModel.getVisitType());
						vtCountJsonObj.put("y", aModel.getSubjectCount());
						if(dataId.toLowerCase().contains("guid") && chartFilter.getSelectedGuidId() != null 
								&& chartFilter.getSelectedStatusId() == null){
							vtCountJsonObj.put("drilldown", true);
						} 
						vtDataJsonArr.put(vtCountJsonObj);
					}
				}
			}
		}

		visitTypeJsonObj.put("data", vtDataJsonArr);
		visitTypeJsonObj.put("categories", vtCategoryArr);	
		
		return visitTypeJsonObj;
	}
	
	public String showViewData() {
		buildLeftNav(LeftNavController.LEFTNAV_HOME);
    	return "viewData";
	}

    public String getDashBoardSitesList() throws JSONException, ObjectNotFoundException, CtdbException{
    	String studyIdStr = request.getParameter("studyId");
    	Protocol protocol = null;
    	if(!studyIdStr.equalsIgnoreCase("all")) {
    		Integer studyId = Integer.valueOf(studyIdStr);
    		if(studyId > 0){
    			ProtocolManager protoMan = new ProtocolManager();
    			protocol = protoMan.getProtocol(studyId);
    		}
    	}

		if(protocol != null){
			JSONArray sitesJsonArray = new JSONArray();
			
			for ( Site s : protocol.getStudySites() ) {
				JSONObject siteJsonObj = new JSONObject();
				siteJsonObj.put("id", s.getId());
				siteJsonObj.put("name", s.getName());
				
				sitesJsonArray.put(siteJsonObj);
			}
		
			jsonList = sitesJsonArray.toString();
			return BaseAction.SUCCESS;
		} else {
			errRespMsg = "No Protocol";
			return StrutsConstants.BAD_REQUEST;
		}
    }
    
    public String getDashBoardGuidsList() throws JSONException, ObjectNotFoundException, CtdbException{
    	String studyIdStr = request.getParameter("studyId");
    	Protocol protocol = null;
    	if(!studyIdStr.equalsIgnoreCase("all")) {
    		Integer studyId = Integer.valueOf(studyIdStr);
    		if(studyId > 0){
    			ProtocolManager protoMan = new ProtocolManager();
    			protocol = protoMan.getProtocol(studyId);
    		}
    	}
    	List<Patient> patList = new ArrayList<Patient>();
		if(protocol != null){
			PatientManager patMan = new PatientManager();
			JSONArray guidsJsonArray = new JSONArray();
			patList = patMan.getPatientListByProtocol(protocol.getId());
			
			for ( Patient p : patList ) {
				JSONObject guidJsonObj = new JSONObject();
				guidJsonObj.put("id", p.getId());
				guidJsonObj.put("guid", p.getGuid());				
				guidsJsonArray.put(guidJsonObj);
			}
		
			jsonList = guidsJsonArray.toString();
			return BaseAction.SUCCESS;
		} else {
			errRespMsg = "No Protocol";
			return StrutsConstants.BAD_REQUEST;
		}

    }
    
    public String getDashBoardCollStatusList() throws JSONException, ObjectNotFoundException, CtdbException{
			
		JSONArray collStatusesJsonArray = new JSONArray();
		EnumSet<DashboardDataCollectionStatus> allCollStatus = EnumSet.allOf( DashboardDataCollectionStatus.class );
		
		for ( DashboardDataCollectionStatus status : allCollStatus ) {
//			if(status.getId() != 3){ //not adding Not Started status to the front end filter
				JSONObject collStatusJsonObj = new JSONObject();
				collStatusJsonObj.put("id", status.getId());
				collStatusJsonObj.put("name", status.getName());				
				collStatusesJsonArray.put(collStatusJsonObj);
//			}
		}
		
		jsonList = collStatusesJsonArray.toString();
		
		if(collStatusesJsonArray.length() > 0) {
			return BaseAction.SUCCESS;
		} else {
			errRespMsg = "No Protocol";
			return StrutsConstants.BAD_REQUEST;
		}
    }
    
	private DashboardChartFilter convertParamsToChartFilterObj(String paramStr){
		DashboardChartFilter chartFilter = new DashboardChartFilter();
		JsonParser jsonParser = new JsonParser();
		JsonElement element = jsonParser.parse(paramStr.replaceAll("^\"+|\"+$", ""));
		JsonObject chartFilterJsonObj = element.getAsJsonObject();
		
		String currentStudyIdStr = chartFilterJsonObj.get("currentStudyId").toString().replaceAll("^\"|\"$", "");
		if(StringUtil.isBlank(currentStudyIdStr) || currentStudyIdStr.equalsIgnoreCase("all")) { 
			chartFilter.setCurrentStudyId(null);
		} else {
			chartFilter.setCurrentStudyId(Integer.valueOf(currentStudyIdStr));
		}
		
		String selectedSiteIdStr = chartFilterJsonObj.get("selectedSiteId").toString().replaceAll("^\"|\"$", "");
		if(StringUtil.isBlank(selectedSiteIdStr)){
			chartFilter.setSelectedSiteId(null);
		} else {
			chartFilter.setSelectedSiteId(Integer.valueOf(selectedSiteIdStr));
		}
		
		String selectedGuidIdStr = chartFilterJsonObj.get("selectedGuidId").toString().replaceAll("^\"|\"$", "");
		if(StringUtil.isBlank(selectedGuidIdStr)){
			chartFilter.setSelectedGuidId(null);
		} else {
			chartFilter.setSelectedGuidId(Integer.valueOf(selectedGuidIdStr));
		}
		
		String selectedStatusIdStr = chartFilterJsonObj.get("selectedStatusId").toString().replaceAll("^\"|\"$", "");
		if(StringUtil.isBlank(selectedStatusIdStr)) {
			chartFilter.setSelectedStatusId(null);
		} else {
			chartFilter.setSelectedStatusId(Integer.valueOf(selectedStatusIdStr));
		}
		return chartFilter;
	}
	
	public String getDrillDownForStatusEformJson() throws ObjectNotFoundException, JSONException, CtdbException {
		String params = request.getParameter("params");
		DashboardChartFilter chartFilter = convertParamsToChartFilterObj(params);
		String visitTypeName = request.getParameter("visitTypeName");
		String seriesName = request.getParameter("seriesName");
		String drilldownName = seriesName + "& Visit Type - " + visitTypeName;
		
		JSONArray drillDownArr = new JSONArray();
		JSONObject drillDownDataObj = getDrillDownDataJson(chartFilter, visitTypeName, drilldownName);
		
		drillDownArr.put(drillDownDataObj);
		this.populationList = drillDownArr.toString();

		return "jsonData";
	}
	
	public JSONObject getDrillDownDataJson(DashboardChartFilter chartFilter, String visitTypeName, String drilldownName) 
			throws JSONException, ObjectNotFoundException, CtdbException{

		List<ProtocolCollectionStatusVsEformCount> collStatusEformCountList = dashboardReporting.getEFormCountByFilterAndVisitType(chartFilter, visitTypeName);

		JSONObject ddSeriesJsonObj = new JSONObject();
		ddSeriesJsonObj.put("id", drilldownName);
		ddSeriesJsonObj.put("name", drilldownName);
		JSONArray drillDownDataArr = new JSONArray();
		JSONArray drillDownCatArr = new JSONArray();
		for(ProtocolCollectionStatusVsEformCount collStatusEformCount : collStatusEformCountList){

			JSONObject ddDataObj = new JSONObject();
			ddDataObj.put("name",collStatusEformCount.getCollectionStatus());
			ddDataObj.put("y",collStatusEformCount.getEFormCount());
			DashboardDataCollectionStatus ddcs = DashboardDataCollectionStatus.getByName(collStatusEformCount.getCollectionStatus());
			ddDataObj.put("color", ddcs.getAssoColor());

			ddDataObj.put("eforms", collStatusEformCount.getEFormNameList());
			drillDownDataArr.put(ddDataObj);
			
			drillDownCatArr.put(collStatusEformCount.getCollectionStatus());
		}
		ddSeriesJsonObj.put("data", drillDownDataArr);
		ddSeriesJsonObj.put("drilldownCategory", drillDownCatArr);
		return ddSeriesJsonObj;
	}
	
	public String getAEsData() throws Exception {
		
		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		List<AdverseEvent> aes = new ArrayList<AdverseEvent>();
		if (protocol != null) {
			aes = (List<AdverseEvent>)dashboardReporting.getAEListBySelectedStudy(protocol.getId());			
		} 		
		JSONArray jsonAEs = new JSONArray();
		for (AdverseEvent ae : aes) {
			jsonAEs.put(AdverseEventJson.fromAdverseEvent(ae));
		}
		
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/text");
    	PrintWriter out = response.getWriter();
    	out.print(jsonAEs.toString());
    	out.flush();
		
		return null;
	}
	
    public List<DashboardDataCollectionStatus> getSiteCollStatusList() {
    	List<DashboardDataCollectionStatus> siteCollStatusList = new ArrayList<DashboardDataCollectionStatus>();
		EnumSet<DashboardDataCollectionStatus> allCollStatus = EnumSet.allOf( DashboardDataCollectionStatus.class );
		
		for ( DashboardDataCollectionStatus status : allCollStatus ) {
			siteCollStatusList.add(status);
		}
		
		return siteCollStatusList;
    }

	public String getPopulationList() {
		return populationList;
	}

	public void setPopulationList(String populationList) {
		this.populationList = populationList;
	}
	
	public String getJsonList() {
		return jsonList;
	}

	public void setJsonList(String jsonList) {
		this.jsonList = jsonList;
	}

	public String getViewDataJson() {
		return viewDataJson;
	}

	public void setViewDataJson(String viewDataJson) {
		this.viewDataJson = viewDataJson;
	}
	
}
