package gov.nih.nichd.ctdb.workspace.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.response.domain.AdverseEvent;
import gov.nih.nichd.ctdb.workspace.domain.DashboardChartFilter;
import gov.nih.nichd.ctdb.workspace.domain.DashboardDataCollectionStatus;
import gov.nih.nichd.ctdb.workspace.domain.DashboardOverallStatus;
import gov.nih.nichd.ctdb.workspace.domain.ProtocolCollectionStatusVsEformCount;
import gov.nih.nichd.ctdb.workspace.domain.ProtocolVisitNameVsSubjectCountCharts;
import gov.nih.nichd.ctdb.workspace.domain.StudyInformation;

public class DashboardReportingDao extends CtdbDao {
	private static final String SQL_PROTO_VISITS =
			" SELECT v.name visitName,count(pp.patientid) subjectCount FROM patientprotocol pp, patientvisit pv, interval v "
					+ "WHERE pp.patientid = pv.patientid and pv.intervalid = v.intervalid and pp.protocolid = ? "
					+ "GROUP BY v.name ORDER BY v.name ";

	private static final String SQL_ALL_PROTO_VISITS =
			" SELECT v.name visitName,count(pp.patientid) subjectCount FROM patientprotocol pp, patientvisit pv, interval v "
					+ "WHERE pp.patientid = pv.patientid and pv.intervalid = v.intervalid "
					+ "GROUP BY v.name ORDER BY v.name ";

	private static final String SQL_SITE_VISITS =
			"  SELECT v.name visitName, count(pp.patientid) subjectCount FROM patientprotocol pp "
					+ "INNER JOIN patientvisit pv ON pp.patientid = pv.patientid "
					+ "INNER JOIN interval v ON pv.intervalid = v.intervalid "
					+ "WHERE pp.protocolid = ? and pp.siteid = ? GROUP BY v.name ORDER BY v.name ";

	/**
	 * Private Constructor to hide the instance creation implementation of the DashbaordReportingDao object in memory.
	 * This will provide a flexible architecture to use a different pattern in the future without refactoring the
	 * DashbaordReporting.
	 */
	private DashboardReportingDao() {}

	/**
	 * Method to retrieve the instance of the DashbaordReportingDao.
	 *
	 * @return DashbaordReportingDao data object
	 */
	public static synchronized DashboardReportingDao getInstance() {
		return new DashboardReportingDao();
	}

	/**
	 * Method to retrieve the instance of the DashbaordReportingDao. This method accepts a Database Connection to be
	 * used internally by the DAO. All transaction management will be handled at the BusinessManager level.
	 *
	 * @param conn Database connection to be used within this data object
	 * @return DashbaordReportingDao data object
	 */
	public static synchronized DashboardReportingDao getInstance(Connection conn) {
		DashboardReportingDao dao = new DashboardReportingDao();
		dao.setConnection(conn);
		return dao;
	}

	/**
	 * method to create json data for subject count vs visit type in protocol
	 * 
	 * @param protocolId
	 * @return
	 * @throws CtdbException
	 */
	public List<ProtocolVisitNameVsSubjectCountCharts> getPatinetVisitCount(Integer protocolId) throws CtdbException {

		List<ProtocolVisitNameVsSubjectCountCharts> protcoSubjectVisitList =
				new ArrayList<ProtocolVisitNameVsSubjectCountCharts>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			if (protocolId == null) {
				stmt = this.conn.prepareStatement(DashboardReportingDao.SQL_ALL_PROTO_VISITS);
			} else {
				stmt = this.conn.prepareStatement(DashboardReportingDao.SQL_PROTO_VISITS);
				stmt.setInt(1, protocolId);
			}

			rs = stmt.executeQuery();

			while (rs.next()) {
				ProtocolVisitNameVsSubjectCountCharts protocolChart = new ProtocolVisitNameVsSubjectCountCharts();
				protocolChart.setVisitType(rs.getString("visitName"));
				protocolChart.setSubjectCount(rs.getInt("subjectCount"));
				protcoSubjectVisitList.add(protocolChart);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get subject visit list for protocol: " + protocolId, e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return protcoSubjectVisitList;
	}


	public List<ProtocolVisitNameVsSubjectCountCharts> getPatinetVisitCountBySite(Integer protocolId, Integer siteId)
			throws CtdbException {
		List<ProtocolVisitNameVsSubjectCountCharts> protcoSubjectVisitList =
				new ArrayList<ProtocolVisitNameVsSubjectCountCharts>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			if (protocolId == null) {
				stmt = this.conn.prepareStatement(DashboardReportingDao.SQL_ALL_PROTO_VISITS);
			} else if (siteId == 0) {
				stmt = this.conn.prepareStatement(DashboardReportingDao.SQL_PROTO_VISITS);
				stmt.setInt(1, protocolId);
			} else {
				stmt = this.conn.prepareStatement(DashboardReportingDao.SQL_SITE_VISITS);
				stmt.setInt(1, protocolId);
				stmt.setInt(2, siteId);
			}

			rs = stmt.executeQuery();

			while (rs.next()) {
				ProtocolVisitNameVsSubjectCountCharts protocolChart = new ProtocolVisitNameVsSubjectCountCharts();

				protocolChart.setVisitType(rs.getString("visitName"));
				protocolChart.setSubjectCount(rs.getInt("subjectCount"));
				protcoSubjectVisitList.add(protocolChart);
			}
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get subject visit list for protocol and site: (" + protocolId + ", " + siteId + ").", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return protcoSubjectVisitList;
	}

	public List<ProtocolVisitNameVsSubjectCountCharts> getOverallPVCountByChartFilter(DashboardChartFilter chartFilter)
			throws CtdbException {
		List<ProtocolVisitNameVsSubjectCountCharts> protcoSubjectVisitList =
				new ArrayList<ProtocolVisitNameVsSubjectCountCharts>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			Integer protocolId = chartFilter.getCurrentStudyId();
			String sql_visits_select = "SELECT v.name visitName,count(pp.patientid) subjectCount "
					+ "FROM patientvisit pv "
					+ " INNER JOIN patientprotocol pp ON pv.patientid = pp.patientid AND pp.protocolid = pv.protocolid "
					+ " INNER JOIN interval v  ON  pv.intervalid = v.intervalid "
					+ " INNER JOIN form_interval fi on pv.intervalid = fi.intervalid "
					+ " INNER JOIN eform ef ON fi.eformid = ef.eformid "
					+ " INNER JOIN patient p ON p.patientid = pp.patientid ";

			String sql_visits_groupBy = " GROUP BY v.name ORDER BY v.name  ";

			if (protocolId != null) {
				int startingParamNum = 1;
				Map<String, Integer> stmtParamMap = new HashMap<String, Integer>();
				String sql_status_where =
						getWhereClauseForChartFilters(chartFilter, null, null, startingParamNum, stmtParamMap);

				String sql = sql_visits_select + sql_status_where + sql_visits_groupBy;
				stmt = this.conn.prepareStatement(sql);
				addChartFiltersToStatment(chartFilter, null, null, stmt, stmtParamMap);
			} else {
				String sql = sql_visits_select + sql_visits_groupBy;
				stmt = this.conn.prepareStatement(sql);
			}

			rs = stmt.executeQuery();

			while (rs.next()) {
				ProtocolVisitNameVsSubjectCountCharts protocolChart = new ProtocolVisitNameVsSubjectCountCharts();
				protocolChart.setVisitType(rs.getString("visitName"));
				protocolChart.setSubjectCount(rs.getInt("subjectCount"));
				protcoSubjectVisitList.add(protocolChart);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get filitered list of subject visits.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return protcoSubjectVisitList;
	}

	public List<ProtocolCollectionStatusVsEformCount> getEFormCountByFilterAndVisitType(
			DashboardChartFilter chartFilter, String visitTypeName) throws CtdbException {
		List<ProtocolCollectionStatusVsEformCount> protocolEformCollStatusList =
				new ArrayList<ProtocolCollectionStatusVsEformCount>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql_status_select = "SELECT ded.coll_status collectionStatus, count(distinct ef.name) eformCount "
					+ "FROM patientprotocol pp INNER JOIN administeredform af ON af.patientid = pp.patientid "
					+ "INNER JOIN interval v  ON  af.intervalid=v.intervalid INNER JOIN dataentrydraft ded "
					+ "ON af.administeredformid=ded.administeredformid INNER JOIN eform ef ON af.eformid=ef.eformid "
					+ "INNER JOIN patient p ON p.patientid = pp.patientid ";

			String sql_status_groupBy = " GROUP BY ded.coll_status ORDER BY ded.coll_status  ";
			Integer protocolId = chartFilter.getCurrentStudyId();

			if (protocolId != null) {
				int startingParamNum = 1;
				Map<String, Integer> stmtParamMap = new HashMap<String, Integer>();
				String sql_status_where =
						getWhereClauseForChartFilters(chartFilter, visitTypeName, null, startingParamNum, stmtParamMap);

				// Getting eform count from the "administeredform" table with match collection status in the
				// "dataentrydraft" table.
				String sql = sql_status_select + sql_status_where + sql_status_groupBy;
				stmt = this.conn.prepareStatement(sql);
				addChartFiltersToStatment(chartFilter, visitTypeName, null, stmt, stmtParamMap);
			} else {
				String sql = sql_status_select + sql_status_groupBy;
				stmt = this.conn.prepareStatement(sql);
			}

			rs = stmt.executeQuery();

			while (rs.next()) {
				ProtocolCollectionStatusVsEformCount drillDownChart = new ProtocolCollectionStatusVsEformCount();
				drillDownChart.setCollectionStatus(rs.getString("collectionStatus"));
				drillDownChart.setEFormCount(rs.getInt("eformCount"));
				List<String> eFormNameList = getEFormNameListByCollectionStatus(chartFilter, visitTypeName,
						drillDownChart.getCollectionStatus());
				drillDownChart.setEFormNameList(eFormNameList);
				protocolEformCollStatusList.add(drillDownChart);
			}

			ProtocolCollectionStatusVsEformCount notStartedEformsCount =
					this.getNotStartedEFormCountByFilterAndVisitType(chartFilter, visitTypeName);
			protocolEformCollStatusList.add(notStartedEformsCount);
		} catch (SQLException e) {
			throw new CtdbException("Unable to get eForm count.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return protocolEformCollStatusList;
	}

	public List<AdverseEvent> getAEListBySelectedStudy(long studyId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<AdverseEvent> list = new ArrayList<AdverseEvent>();

		try {
			String sql = "select af.administeredformid as afid, p.guid, def.completeddate, ef.data_structure_name \r\n"
					+ "from eform ef, administeredform af, patient p, dataentrydraft def \r\n"
					+ "where ef.eformid=af.eformid \r\n" + "and af.patientid=p.patientid\r\n"
					+ "and af.administeredformid=def.administeredformid\r\n" + "and def.coll_status='Completed' \r\n"
					+ "--and def.coll_status='Locked' \r\n" + "and ef.protocolid=? \r\n"
					+ "and upper(ef.data_structure_name) like upper('%AdverseEvents%')\r\n"
					+ "order by af.administeredformid;";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, studyId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				AdverseEvent ae = new AdverseEvent();
				ae.setAdministeredformId(rs.getInt("afid"));
				ae.setSubject(rs.getString("guid"));
				ae.setFormCompleteddate(rs.getString("completeddate"));

				list.add(ae);
			}
		} catch (SQLException sqle) {
			throw new CtdbException("Unable to get \"Adverse Event Log\".", sqle);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return list;
	}

	public List<String> getEFormNameListByCollectionStatus(DashboardChartFilter chartFilter, String visitTypeName,
			String collStatus) throws CtdbException {
		List<String> eFormNameList = new ArrayList<String>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql_status_select = "SELECT ef.name eformName FROM patientprotocol pp "
					+ "INNER JOIN administeredform af ON af.patientid = pp.patientid "
					+ "INNER JOIN interval v  ON  af.intervalid=v.intervalid "
					+ "INNER JOIN dataentrydraft ded ON af.administeredformid=ded.administeredformid "
					+ "INNER JOIN eform ef ON af.eformid=ef.eformid "
					+ "INNER JOIN patient p ON p.patientid = pp.patientid ";

			String sql_status_groupBy = " ORDER BY ef.name  ";
			Integer protocolId = chartFilter.getCurrentStudyId();

			if (protocolId != null) {
				int startingParamNum = 0;
				Map<String, Integer> stmtParamMap = new HashMap<String, Integer>();
				String sql_status_where = getWhereClauseForChartFilters(chartFilter, visitTypeName, collStatus,
						startingParamNum, stmtParamMap);

				String sql = sql_status_select + sql_status_where + sql_status_groupBy;
				stmt = this.conn.prepareStatement(sql);
				addChartFiltersToStatment(chartFilter, visitTypeName, collStatus, stmt, stmtParamMap);
			} else {
				String sql = sql_status_select + sql_status_groupBy;
				stmt = this.conn.prepareStatement(sql);
			}

			rs = stmt.executeQuery();

			while (rs.next()) {
				eFormNameList.add(rs.getString("eformName"));
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get eForm name list by collection status.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return eFormNameList;
	}

	/*
	 * Getting eform name list from form_interval table but not in administered_from table. These are the eforms
	 * associated with the given interval and protocol, but not being started in data collections
	 */
	public ProtocolCollectionStatusVsEformCount getNotStartedEFormCountByFilterAndVisitType(
			DashboardChartFilter chartFilter, String visitTypeName) throws CtdbException {
		ProtocolCollectionStatusVsEformCount notStartedEformCount = new ProtocolCollectionStatusVsEformCount();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql_status_select = "SELECT ef.name eformName FROM form_interval fi  "
					+ "INNER JOIN interval v  ON  fi.intervalid = v.intervalid "
					+ "INNER JOIN eform ef ON ef.eformid = fi.eformid "
					+ "WHERE v.name = ? AND v.protocolid = ? AND ef.name NOT IN ("
					+ "SELECT ef.name FROM patientprotocol pp "
					+ "INNER JOIN administeredform af ON af.patientid = pp.patientid "
					+ "INNER JOIN interval v  ON  af.intervalid=v.intervalid "
					+ "INNER JOIN dataentrydraft ded ON af.administeredformid=ded.administeredformid"
					+ "INNER JOIN eform ef ON af.eformid=ef.eformid "
					+ "INNER JOIN patient p ON p.patientid = pp.patientid ";

			Integer protocolId = chartFilter.getCurrentStudyId();
			List<String> notStartedEformNameList = new ArrayList<String>();

			if (protocolId != null) {
				int startingParamNum = 3;
				Map<String, Integer> stmtParamMap = new HashMap<String, Integer>();
				String sql_status_where =
						getWhereClauseForChartFilters(chartFilter, visitTypeName, null, startingParamNum, stmtParamMap);

				String sql = sql_status_select + sql_status_where + ") ";
				stmt = this.conn.prepareStatement(sql);
				stmt.setString(1, visitTypeName);
				stmt.setInt(2, protocolId);
				addChartFiltersToStatment(chartFilter, visitTypeName, null, stmt, stmtParamMap);

			}

			rs = stmt.executeQuery();

			while (rs.next()) {
				notStartedEformNameList.add(rs.getString("eformName"));
			}

			notStartedEformCount.setCollectionStatus(CtdbConstants.DATACOLLECTION_STATUS_NOTSTARTED);
			notStartedEformCount.setEFormCount(notStartedEformNameList.size());
			notStartedEformCount.setEFormNameList(notStartedEformNameList);

		} catch (SQLException e) {
			throw new CtdbException("Unable to find not started eForm count.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		return notStartedEformCount;
	}

	public List<String> getCategorySetByChartFilter(DashboardChartFilter chartFilter) throws CtdbException {
		List<String> siteCateSet = new ArrayList<String>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql_visits_select = "SELECT DISTINCT v.name visitName , v.orderval FROM "
					+ "patientprotocol pp INNER JOIN patientvisit pv on pv.patientid = pp.patientid and "
					+ "pv.protocolid = pp.protocolid INNER JOIN interval v ON pv.intervalid = v.intervalid "
					+ "INNER JOIN patient p ON p.patientid = pp.patientid INNER JOIN administeredform af "
					+ "ON af.patientid = pp.patientid INNER JOIN dataentrydraft ded ON "
					+ "af.administeredformid = ded.administeredformid ";

			String sql_visits_orderBy = " ORDER BY v.orderval ";
			Integer protocolId = chartFilter.getCurrentStudyId();

			if (protocolId != null) {
				int startingParamNum = 1;
				Map<String, Integer> stmtParamMap = new HashMap<String, Integer>();
				String sql_status_where =
						getWhereClauseForChartFilters(chartFilter, null, null, startingParamNum, stmtParamMap);
				String sql = sql_visits_select + sql_status_where + sql_visits_orderBy;

				stmt = this.conn.prepareStatement(sql);
				addChartFiltersToStatment(chartFilter, null, null, stmt, stmtParamMap);
			} else {
				String sql = sql_visits_select + sql_visits_orderBy;
				stmt = this.conn.prepareStatement(sql);
			}

			rs = stmt.executeQuery();

			while (rs.next()) {
				siteCateSet.add(rs.getString("visitName"));
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get site category set list.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return siteCateSet;
	}

	public List<ProtocolVisitNameVsSubjectCountCharts> getStartedPVCountByChartFilter(DashboardChartFilter chartFilter)
			throws CtdbException {
		List<ProtocolVisitNameVsSubjectCountCharts> protcoSubjectVisitList =
				new ArrayList<ProtocolVisitNameVsSubjectCountCharts>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql_visits_select = "SELECT v.name visitName,count(pp.patientid) subjectCount FROM "
					+ "patientprotocol pp INNER JOIN patientvisit pv ON pp.patientid = pv.patientid "
					+ "AND pp.protocolid = pv.protocolid INNER JOIN administeredform af ON "
					+ "af.patientid = pp.patientid INNER JOIN interval v ON af.intervalid = v.intervalid "
					+ "AND pv.intervalid = af.intervalid INNER JOIN dataentrydraft ded ON "
					+ "af.administeredformid = ded.administeredformid INNER JOIN eform ef ON af.eformid = ef.eformid "
					+ " INNER JOIN patient p ON p.patientid = pp.patientid ";

			String sql_visits_groupBy = " GROUP BY v.name ORDER BY v.name ";
			Integer protocolId = chartFilter.getCurrentStudyId();

			if (protocolId != null) {
				int startingParamNum = 1;
				Map<String, Integer> stmtParamMap = new HashMap<String, Integer>();
				String sql_status_where =
						getWhereClauseForChartFilters(chartFilter, null, null, startingParamNum, stmtParamMap);

				String sql = sql_visits_select + sql_status_where + sql_visits_groupBy;
				stmt = this.conn.prepareStatement(sql);
				addChartFiltersToStatment(chartFilter, null, null, stmt, stmtParamMap);
			} else {
				String sql = sql_visits_select + sql_visits_groupBy;
				stmt = this.conn.prepareStatement(sql);
			}

			rs = stmt.executeQuery();

			while (rs.next()) {
				ProtocolVisitNameVsSubjectCountCharts protocolChart = new ProtocolVisitNameVsSubjectCountCharts();
				protocolChart.setVisitType(rs.getString("visitName"));
				protocolChart.setSubjectCount(rs.getInt("subjectCount"));
				protcoSubjectVisitList.add(protocolChart);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get subject visit list.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return protcoSubjectVisitList;
	}

	private String getWhereClauseForChartFilters(DashboardChartFilter chartFilter, String visitTypeName,
			String collStatus, int startingParamNum, Map<String, Integer> stmtParamMap) {
		StringBuffer sql_status_where = new StringBuffer();
		Integer siteId = chartFilter.getSelectedSiteId();
		Integer guidId = chartFilter.getSelectedGuidId();
		Integer statusId = chartFilter.getSelectedStatusId();

		String status = collStatus;
		if (statusId != null && Utils.isBlank(status)) {
			status = DashboardDataCollectionStatus.getById(statusId).getName();
		}

		int counter = startingParamNum;

		sql_status_where.append(" WHERE pp.protocolid = ? ");
		stmtParamMap.put("protocol", counter++);

		if (!Utils.isBlank(visitTypeName)) {
			sql_status_where.append(" and v.name = ? ");
			stmtParamMap.put("visitType", counter++);
		}

		if (siteId != null && siteId > 0) {
			sql_status_where.append(" and pp.siteid = ? ");
			stmtParamMap.put("site", counter++);
		}

		if (guidId != null && guidId > 0) {
			sql_status_where.append(" and p.patientid = ? ");
			stmtParamMap.put("guid", counter++);
		}

		if (!Utils.isBlank(status)) {
			sql_status_where.append(" and ded.coll_status = ? ");
			stmtParamMap.put("collectionStatus", counter++);
		}

		return sql_status_where.toString();
	}

	private void addChartFiltersToStatment(DashboardChartFilter chartFilter, String visitTypeName, String collStatus,
			PreparedStatement stmt, Map<String, Integer> stmtParamMap) throws SQLException, CtdbException {
		Integer protocolId = chartFilter.getCurrentStudyId();
		Integer siteId = chartFilter.getSelectedSiteId();
		Integer guidId = chartFilter.getSelectedGuidId();
		Integer statusId = chartFilter.getSelectedStatusId();

		String status = collStatus;
		if (statusId != null && Utils.isBlank(status)) {
			status = DashboardDataCollectionStatus.getById(statusId).getName();
		}

		for (Entry<String, Integer> entry : stmtParamMap.entrySet()) {
			switch (entry.getKey()) {
				case "protocol":
					stmt.setInt(entry.getValue(), protocolId);
					break;
				case "visitType":
					if (Utils.isBlank(visitTypeName)) {
						stmt.setString(entry.getValue(), "");
					} else {
						stmt.setString(entry.getValue(), visitTypeName);
					}

					break;
				case "site":
					stmt.setInt(entry.getValue(), siteId);
					break;
				case "guid":
					stmt.setInt(entry.getValue(), guidId);
					break;
				case "collectionStatus":
					if (Utils.isBlank(status)) {
						stmt.setString(entry.getValue(), "");
					} else {
						stmt.setString(entry.getValue(), status);
					}

					break;
				default:
					throw new CtdbException("Unkown key value, " + entry.getKey()
							+ ", encountered in the 'stmtParamMap' method parameter.");
			}
		}
	}

	/**
	 * Method to get the visit status of the proforms dashboard
	 * 
	 * @return list
	 * 
	 */
	public List<DashboardOverallStatus> getAssessmentTypeList(Integer protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<DashboardOverallStatus> list = new ArrayList<DashboardOverallStatus>();

		try {
			String sql = "select i.name Visittype, sum(CASE WHEN ded.coll_status = 'Completed' "
					+ "THEN 1 ELSE 0 END) Completed, sum(CASE WHEN i.intervalid is not null and "
					+ "ded.administeredformid is null THEN 1 ELSE 0 END) Incomplete, sum(CASE WHEN "
					+ "ded.coll_status = 'In Progress' THEN 1 ELSE 0 END) Inprogress, sum(CASE WHEN "
					+ "ef.data_structure_name in ('AdverseEvents', 'ProtocolDeviations') THEN 1 ELSE 0 "
					+ "END) Deviation from administeredform a JOIN interval i on a.intervalid = i.intervalid "
					+ "LEFT OUTER JOIN  dataentrydraft ded on  a.administeredformid = ded.administeredformid "
					+ "LEFT OUTER JOIN eform ef on a.eformid = ef.eformid JOIN patient p on a.patientid = p.patientid "
					+ "JOIN protocol pr on ef.protocolid = pr.protocolid JOIN patientprotocol pp on "
					+ "p.patientid = pp.patientid and pr.protocolid = pp.protocolid JOIN site s on pp.siteid = s.siteid "
					+ "where pr.protocolid = ? group by i.name ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, protocolId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				DashboardOverallStatus status = new DashboardOverallStatus();
				status.setAssessmentType(rs.getString("visitType"));
				status.setCompleteVal(rs.getString("completed"));
				status.setInCompleteVal(rs.getString("inComplete"));
				status.setInProgressVal(rs.getString("inProgress"));
				status.setDeviationsVal(rs.getString("deviation"));

				list.add(status);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get assessment type list.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return list;
	}

	/**
	 * Method to get the Study Information of the proforms dashboard
	 * 
	 * @return StudyInformation or null if none can be found.
	 * 
	 */
	public StudyInformation getStudyInformation(Integer protocolId) throws CtdbException {
		StudyInformation sInfo = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "select sv.studyname studyName,sv.bricsstudyid sbsi, sv.pi pi,sv.studytype st,"
					+ "sv.studystatus ss,sv.patientcount spc,sv.adminformcount safc from study_view sv "
					+ "where sv.studyid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, protocolId);
			rs = stmt.executeQuery();

			if (rs.next()) {
				sInfo = new StudyInformation();
				sInfo.setEnrolledSubjects(rs.getString("spc"));
				sInfo.setTotSubjects(rs.getString("safc"));
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get study information.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return sInfo;
	}

	/**
	 * Method to get the Miles Stones of the protocol dashboard
	 * 
	 * @return list
	 * 
	 */
	public List<String> getDashboardMilesStone(Integer protocolId, Integer mYear) throws CtdbException, ParseException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> list = new ArrayList<String>();

		try {
			String sql =
					"select milesstonename from milesstone where protocolid = ? and milesstonedate BETWEEN ? AND ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, protocolId);

			String fDate = mYear.toString().concat("-01-01 00:00:00");
			String eDate = mYear.toString().concat("-12-31 00:00:00");
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date startDate = df.parse(fDate);
			Date endDate = df.parse(eDate);

			stmt.setTimestamp(2, new Timestamp(startDate.getTime()));
			stmt.setTimestamp(3, new Timestamp(endDate.getTime()));
			rs = stmt.executeQuery();

			while (rs.next()) {
				list.add(rs.getString("milesstonename"));
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get milestone list for dashboad.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return list;
	}
}
