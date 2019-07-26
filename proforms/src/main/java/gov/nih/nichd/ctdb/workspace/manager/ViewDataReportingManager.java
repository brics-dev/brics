package gov.nih.nichd.ctdb.workspace.manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.workspace.dao.ViewDataReportingDao;
import gov.nih.nichd.ctdb.workspace.domain.EformsSelected;
import gov.nih.nichd.ctdb.workspace.domain.ViewDataGraph;
import gov.nih.nichd.ctdb.workspace.domain.ViewDataGraphInput;

public class ViewDataReportingManager extends CtdbManager {
	/**
	 * 
	 * 
	 * @param protocolId
	 * @param vd
	 * @return
	 * @throws JSONException
	 * @throws CtdbException
	 */
	public List<ViewDataGraph> getGraphData(int protocolId, ViewDataGraphInput vd) throws JSONException, CtdbException {
		List<ViewDataGraph> viewDataGraphList = new ArrayList<ViewDataGraph>();
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			ViewDataReportingDao dao = ViewDataReportingDao.getInstance(conn);
			List<Integer> siteIds = vd.getSiteIds();
			Integer guidId = vd.getGuidId();
			List<Integer> intervalIds = vd.getIntervalIds();
			List<EformsSelected> eforms = vd.getEforms();

			if ((siteIds != null || guidId != null) && intervalIds != null && eforms != null) {
				// this count is to append guidid or siteid condition in the query once based on the which one is
				// selected
				for (EformsSelected eform : eforms) {
					if ((eform != null) && !Utils.isBlank(eform.getShortName())) {
						JSONArray dataelements = eform.getDeJson();

						for (int i = 0; i < dataelements.length(); i++) {
							JSONObject deObj = dataelements.getJSONObject(i);
							Integer qid = deObj.getInt("qid");
							String dename = deObj.getString("dename");
							ViewDataGraph vdgraph = dao.formatAndExecuteQuery(protocolId, eform.getShortName(), qid,
									dename, siteIds, guidId, intervalIds);

							if (vdgraph == null) {
								vdgraph = new ViewDataGraph();
								vdgraph.setDename(dename);
								vdgraph.setEformName(eform.getName());
							}

							viewDataGraphList.add(vdgraph);
						}
					}
				}
			}
		}
		finally {
			this.close(conn);
		}

		return viewDataGraphList;
	}


	public JSONArray geteFormsForInterval(int protocolId, List<Integer> intervalIds)
			throws CtdbException, SQLException {
		JSONArray jsonarray = null;
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			ViewDataReportingDao dao = ViewDataReportingDao.getInstance(conn);

			jsonarray = dao.getEformsForInterval(protocolId, intervalIds);
		} finally {
			this.close(conn);
		}

		return jsonarray;
	}

}
