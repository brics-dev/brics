package gov.nih.nichd.ctdb.workspace.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.workspace.domain.ViewDataGraph;

public class ViewDataReportingDao extends CtdbDao {
	private static final String SCORE_FOR_DATAELEMENTS_IN_EFORMS = "select e.name, i.name, pd.answer from "
			+ "patientprotocol pp, administeredform af, eform e, dataentrydraft de, responsedraft rd, "
			+ "patientresponsedraft pd, interval i where e.eformid = af.eformid and "
			+ "af.administeredformid = de.administeredformid and de.dataentrydraftid = rd.dataentrydraftid and "
			+ "rd.responsedraftid = pd.responsedraftid and af.intervalid = i.intervalid and af.patientid = pp.patientid "
			+ "and e.protocolid = pp.protocolid and e.shortname = ? and e.protocolid = ? and rd.dict_questionid = ? "
			+ "and i.intervalid in (%s) ";

	private static final String EFORMS_FOR_INTERVAL = "select e.shortname from eform e, interval i, protocol p, "
			+ "form_interval fi where p.protocolid = ? and i.intervalid in (%s) and i.protocolid = p.protocolid and "
			+ "fi.intervalid = i.intervalid and fi.eformid = e.eformid ";

	private ViewDataReportingDao() {}

	/**
	 * Method to retrieve the instance of the ViewDataReportingDao.
	 *
	 * @return ViewDataReportingDao data object
	 */
	public static synchronized ViewDataReportingDao getInstance() {
		return new ViewDataReportingDao();
	}

	/**
	 * Method to retrieve the instance of the ViewDataReportingDao. This method accepts a Database Connection to be used
	 * internally by the DAO. All transaction management will be handled at the BusinessManager level.
	 *
	 * @param conn Database connection to be used within this data object
	 * @return ViewDataReportingDao data object
	 */
	public static synchronized ViewDataReportingDao getInstance(Connection conn) {
		ViewDataReportingDao dao = new ViewDataReportingDao();
		dao.setConnection(conn);
		return dao;
	}

	public ViewDataGraph formatAndExecuteQuery(int protocolId, String eShortName, Integer qid, String dename,
			List<Integer> siteIds, Integer guidId, List<Integer> intervalIds) throws CtdbException {

		ViewDataGraph vdgraph = null;
		String queryToExecute = "";
		String sql = ViewDataReportingDao.SCORE_FOR_DATAELEMENTS_IN_EFORMS;
		String intervalIdArgs = getPreparedStatementPlaceholderforIn(intervalIds);
		String siteIdArgs = getPreparedStatementPlaceholderforIn(siteIds);

		if (siteIds.size() > 0) {
			sql += "and pp.siteid in (%s) ";
			queryToExecute = String.format(sql, intervalIdArgs, siteIdArgs);
		}

		if (guidId != null) {
			sql += "and pp.patientid = ? ";
			queryToExecute = String.format(sql, intervalIdArgs, siteIdArgs);
		}

		vdgraph = execQuery(queryToExecute, protocolId, eShortName, qid, dename, siteIds, intervalIds, guidId);

		return vdgraph;
	}

	public JSONArray getEformsForInterval(int protocolId, List<Integer> intervalIds) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray jsonarray = new JSONArray();

		try {
			String intervalIdArgs = getPreparedStatementPlaceholderforIn(intervalIds);

			if (StringUtils.isNotBlank(intervalIdArgs)) {
				String formattedQuery = String.format(ViewDataReportingDao.EFORMS_FOR_INTERVAL, intervalIdArgs);
				stmt = conn.prepareStatement(formattedQuery);
				stmt.setLong(1, protocolId);
				setIntParameters(stmt, intervalIds, 2);
				rs = stmt.executeQuery();

				while (rs.next()) {
					jsonarray.put(rs.getString("shortname"));
				}
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get eForm JSON array for intervals.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		return jsonarray;
	}

	private ViewDataGraph execQuery(String queryToExecute, int protocolId, String eShortName, Integer qid,
			String dename, List<Integer> siteIds, List<Integer> intervalIds, Integer guidId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		ViewDataGraph vdgraph = null;

		try {
			if (StringUtils.isNotEmpty(queryToExecute)) {
				stmt = conn.prepareStatement(queryToExecute);
				setParameters(stmt, protocolId, eShortName, qid, siteIds, intervalIds, guidId);
				rs = stmt.executeQuery();

				String ename = "";
				JSONArray intervals = new JSONArray();
				JSONArray scores = new JSONArray();
				boolean enameNotSet = true;

				while (rs.next()) {
					// Each result set has ename, so using a flag to only set it once. The SQL query is already complex
					// so I didn't add this logic in the query.
					if (enameNotSet) {
						ename = rs.getString(1);
						enameNotSet = false;
					}

					intervals.put(rs.getString(2));
					scores.put(rs.getBigDecimal(3));
				}

				// If at least one record in result set then creating vdgraph object.
				if (intervals.length() > 0) {
					vdgraph = new ViewDataGraph();
					vdgraph.setEformName(ename);
					vdgraph.setDename(dename);
					vdgraph.setIntervalsJSON(intervals);
					vdgraph.setScoresJSON(scores);
				}
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to contruct the ViewDataGraph.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return vdgraph;
	}

	private void setParameters(PreparedStatement stmt, int protocolId, String eShortName, Integer de,
			List<Integer> siteIds, List<Integer> intervalIds, Integer guidId) throws SQLException {
		int parameterIndex = 4;
		stmt.setString(1, eShortName);
		stmt.setInt(2, protocolId);
		stmt.setInt(3, de);
		parameterIndex = setIntParameters(stmt, intervalIds, parameterIndex);
		if (siteIds.size() > 0) {
			parameterIndex = setIntParameters(stmt, siteIds, parameterIndex);
		}
		if (guidId != null) {
			stmt.setInt(parameterIndex, guidId);
		}
	}

	private int setIntParameters(PreparedStatement stmt, List<Integer> values, int parameterIndex) throws SQLException {
		for (Integer val : values) {
			stmt.setInt(parameterIndex++, val);
		}

		return parameterIndex;
	}

	private String getPreparedStatementPlaceholderforIn(List<? extends Object> list) {
		if (list == null || list.isEmpty()) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		Integer count = list.size();
		for (int i = 0; i < count; i++) {
			if (count == i + 1) {
				sb.append("?");
				break;
			}
			sb.append("?,");

		}
		return sb.toString();
	}
}
