package gov.nih.nichd.ctdb.workspace.manager;

import java.sql.Connection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.response.domain.AdverseEvent;
import gov.nih.nichd.ctdb.workspace.dao.DashboardReportingDao;
import gov.nih.nichd.ctdb.workspace.domain.DashboardChartFilter;
import gov.nih.nichd.ctdb.workspace.domain.DashboardOverallStatus;
import gov.nih.nichd.ctdb.workspace.domain.ProtocolCollectionStatusVsEformCount;
import gov.nih.nichd.ctdb.workspace.domain.ProtocolVisitNameVsSubjectCountCharts;
import gov.nih.nichd.ctdb.workspace.domain.StudyInformation;

/**
 * Class to build JSON data for the charts and graphs
 * 
 * @author khanaly
 *
 */
public class DashboardReportingManager extends CtdbManager {

	public List<ProtocolVisitNameVsSubjectCountCharts> getPatinetVisitCount(Integer protocolId) throws CtdbException {

		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			return DashboardReportingDao.getInstance(conn).getPatinetVisitCount(protocolId);
		} finally {
			this.close(conn);
		}
	}

	public List<ProtocolVisitNameVsSubjectCountCharts> getPatinetVisitCountBySite(Integer protocolId, Integer siteId)
			throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			return DashboardReportingDao.getInstance(conn).getPatinetVisitCountBySite(protocolId, siteId);
		} finally {
			this.close(conn);
		}
	}

	public List<ProtocolVisitNameVsSubjectCountCharts> getOverallPVCountByChartFilter(DashboardChartFilter chartFilter)
			throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			return DashboardReportingDao.getInstance(conn).getOverallPVCountByChartFilter(chartFilter);
		} finally {
			this.close(conn);
		}
	}

	public List<ProtocolCollectionStatusVsEformCount> getEFormCountByFilterAndVisitType(
			DashboardChartFilter chartFilter, String visitTypeName) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			return DashboardReportingDao.getInstance(conn).getEFormCountByFilterAndVisitType(chartFilter,
					visitTypeName);
		} finally {
			this.close(conn);
		}
	}


	public List<AdverseEvent> getAEListBySelectedStudy(Integer protocolId) throws CtdbException {
		Connection conn = null;
		try

		{
			conn = CtdbManager.getConnection();
			return DashboardReportingDao.getInstance(conn).getAEListBySelectedStudy(protocolId);
		} finally

		{
			this.close(conn);
		}
	}

	public List<String> getCategorySetByChartFilter(DashboardChartFilter chartFilter) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			return DashboardReportingDao.getInstance(conn).getCategorySetByChartFilter(chartFilter);
		} finally {
			this.close(conn);
		}
	}

	public List<ProtocolVisitNameVsSubjectCountCharts> getStartedPVCountByChartFilter(DashboardChartFilter chartFilter)
			throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			DashboardReportingDao dao = DashboardReportingDao.getInstance(conn);
			return dao.getStartedPVCountByChartFilter(chartFilter);
		} finally {
			this.close(conn);
		}
	}

	public List<DashboardOverallStatus> getAssessmentTypeList(Integer protocolId) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			return DashboardReportingDao.getInstance(conn).getAssessmentTypeList(protocolId);
		} finally {
			this.close(conn);
		}
	}

	public List<ProtocolVisitNameVsSubjectCountCharts> getNotStatedPVCountByChartFilter(
			DashboardChartFilter chartFilter) throws CtdbException {

		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			DashboardReportingDao dao = DashboardReportingDao.getInstance(conn);
			// set data collection status to be null
			DashboardChartFilter resetChartFilter = new DashboardChartFilter(chartFilter.getCurrentStudyId(),
					chartFilter.getSelectedSiteId(), chartFilter.getSelectedGuidId(), null);

			List<ProtocolVisitNameVsSubjectCountCharts> notStartedPVSubjectList =
					new ArrayList<ProtocolVisitNameVsSubjectCountCharts>();

			List<ProtocolVisitNameVsSubjectCountCharts> allPVSubjectList =
					dao.getOverallPVCountByChartFilter(resetChartFilter);

			List<ProtocolVisitNameVsSubjectCountCharts> startedPVSubjectList =
					dao.getStartedPVCountByChartFilter(resetChartFilter);

			for (ProtocolVisitNameVsSubjectCountCharts pv : allPVSubjectList) {
				for (ProtocolVisitNameVsSubjectCountCharts spv : startedPVSubjectList) {
					if (pv.getVisitType().equals(spv.getVisitType())) {
						ProtocolVisitNameVsSubjectCountCharts nspv = new ProtocolVisitNameVsSubjectCountCharts();
						nspv.setVisitType(pv.getVisitType());
						int subCount = pv.getSubjectCount() - spv.getSubjectCount();
						if (subCount >= 0) {
							nspv.setSubjectCount(subCount);
						}
						notStartedPVSubjectList.add(nspv);
					}
				}
			}

			return notStartedPVSubjectList;
		} finally {
			this.close(conn);
		}
	}

	public StudyInformation getStudyInformation(Integer protocolId) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			return DashboardReportingDao.getInstance(conn).getStudyInformation(protocolId);
		} finally {
			this.close(conn);
		}
	}

	public List<String> getMilesStone(Integer protocolId, Integer mYear) throws CtdbException, ParseException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			return DashboardReportingDao.getInstance(conn).getDashboardMilesStone(protocolId, mYear);
		} finally {
			this.close(conn);
		}
	}

}
