package gov.nih.nichd.ctdb.workspace.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.protocol.domain.BricsStudy;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.BricsRepoWsClient;
import gov.nih.nichd.ctdb.util.common.DashboardUtil;
import gov.nih.nichd.ctdb.workspace.domain.DashboardOverallStatus;
import gov.nih.nichd.ctdb.workspace.domain.StudyInformation;
import gov.nih.nichd.ctdb.workspace.manager.DashboardReportingManager;

/**
 * Action class to display overallstatus report in Dashboard.
 * 
 * @author Sireesha Kolla
 *
 */
public class DashboardOverAllStatusAction extends BaseAction {
	private static final long serialVersionUID = 264237781763391123L;
	private static final Logger logger = Logger.getLogger(DashboardOverAllStatusAction.class);
	private static final String YEAR_1 = "Year 1";
	private static final String YEAR_VAL = "Year ";
	private static final String YEARS_VAL = " years";

	// Dashboard data
	private String jsonList = CtdbConstants.EMPTY_JSON_ARRAY_STR;
	private String errRespMsg;

	/**
	 * @return the jsonList
	 */
	public String getJsonList() {
		return jsonList;
	}

	/**
	 * @param jsonList the jsonList to set
	 */
	public void setJsonList(String jsonList) {
		this.jsonList = jsonList;
	}

	public String getDashBoardOverAllStatusList() throws JSONException {
		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);

		if (protocol != null) {
			List<DashboardOverallStatus> overallStatusList = new ArrayList<DashboardOverallStatus>();

			try {
				DashboardReportingManager dashboardReporting = new DashboardReportingManager();
				overallStatusList = dashboardReporting.getAssessmentTypeList(protocol.getId());
			} catch (CtdbException e) {
				logger.error("Error while getting over all status data.", e);
			}

			JSONArray vtDataJsonArr = new JSONArray();

			for (DashboardOverallStatus aModel : overallStatusList) {
				JSONObject statusJsonObj = new JSONObject();
				statusJsonObj.put("assessmentType", aModel.getAssessmentType());
				statusJsonObj.put("compVal", aModel.getCompleteVal());
				statusJsonObj.put("inCompVal", aModel.getInCompleteVal());
				statusJsonObj.put("inProgVal", aModel.getInProgressVal());
				statusJsonObj.put("devVal", aModel.getDeviationsVal());

				vtDataJsonArr.put(statusJsonObj);
			}

			jsonList = vtDataJsonArr.toString();
			return BaseAction.SUCCESS;
		} else {
			setErrRespMsg("No Data");
			return StrutsConstants.BAD_REQUEST;
		}
	}

	public String getDashBoardStudyInformation() throws JSONException {
		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		JSONObject studyJsonObj = new JSONObject();
		BricsRepoWsClient client = new BricsRepoWsClient();
		User user = getUser();
		Hashtable<String, BricsStudy> bricsStudyTable = null;

		if (protocol != null) {
			List<String> msList = new ArrayList<String>();
			DashboardReportingManager dashboardReporting = new DashboardReportingManager();

			try {
				StudyInformation studyInfo = dashboardReporting.getStudyInformation(protocol.getId());

				studyJsonObj.put("enrollSubjects", studyInfo.getEnrolledSubjects());
				studyJsonObj.put("totSubjects", studyInfo.getTotSubjects());
				bricsStudyTable = client.getBricsStudiesForUser(user, request, protocol);

				if(!protocol.getBricsStudyId().isEmpty()) {
					BricsStudy bs = bricsStudyTable.get(protocol.getBricsStudyId());
					DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
					Date startDate = null;
					Date endDate = null;

					startDate = formatter.parse(bs.getStudyStartDate());
					endDate = formatter.parse(bs.getStudyEndDate());

					SimpleDateFormat df = new SimpleDateFormat("yyyy");

					String startIntDt = df.format(startDate);
					String endIntDt = df.format(endDate);
					int diff = Integer.parseInt(endIntDt) - Integer.parseInt(startIntDt);
					HashMap<String, List<String>> studyMap = new HashMap<>();

					if (diff == 0) {
						String year = YEAR_1;
						msList = dashboardReporting.getMilesStone(protocol.getId(), Integer.parseInt(startIntDt));
						studyMap.put(year, msList);
					} else {
						for (int i = 1, j = 0; i <= diff + 1; i++, j++) {
							String year = YEAR_VAL + i;
							msList = dashboardReporting.getMilesStone(protocol.getId(), Integer.parseInt(startIntDt) + j);
							studyMap.put(year, msList);
						}
					}

					studyJsonObj.put("studyMap", DashboardUtil.sortMap(studyMap));
					
					int totYears = diff + 1;
					studyJsonObj.put("studyDuration", DashboardUtil.diffDate(bs.getStudyStartDate()) + "/" + totYears + YEARS_VAL);
				} else {
					setErrRespMsg("No Data Found");
					return StrutsConstants.BAD_REQUEST;
				}
			} catch (Exception e) {
				logger.error("Error while getting study information for the dashboard.", e);
			}

			jsonList = studyJsonObj.toString();
			return BaseAction.SUCCESS;
			
		} else {
			setErrRespMsg("No Protocol");
			return StrutsConstants.BAD_REQUEST;
		}
	}

	public String getErrRespMsg() {
		return errRespMsg;
	}

	public void setErrRespMsg(String errRespMsg) {
		this.errRespMsg = errRespMsg;
	}
}
