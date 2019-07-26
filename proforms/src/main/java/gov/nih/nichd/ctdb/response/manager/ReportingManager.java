package gov.nih.nichd.ctdb.response.manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.response.dao.ReportingManagerDao;
import gov.nih.nichd.ctdb.response.domain.ScheduleReport;
import gov.nih.nichd.ctdb.response.domain.ScheduleReportFilter;
import gov.nih.nichd.ctdb.response.domain.SubmissionSummaryReport;
import gov.nih.nichd.ctdb.response.form.FormVisitTypeStatusSubjectMatrix;
import gov.nih.nichd.ctdb.response.form.ReportingForm;

/**
 * ReportingManager
 * 
 */
public class ReportingManager extends CtdbManager {
	public List<Interval> getIntervalListForSelectedStudy(long studyId) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			return ReportingManagerDao.getInstance(conn).getIntervalListForSelectedStudy(studyId);
		} finally {
			this.close(conn);
		}
	}

	/**
	 * Method to return the list of guid for selected study
	 * 
	 * @param studyId
	 * @return
	 * @throws CtdbException
	 */
	public List<Patient> getGUIDListForSubject(long studyId) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			return ReportingManagerDao.getInstance(conn).getGUIDListForSelectedStudy(studyId);
		} finally {
			this.close(conn);
		}
	}

	/**
	 * This is the suject matrix dashboard that produces form vs visit type color coded data collection status
	 * filterable by subject identifier
	 * 
	 * @param studyId
	 * @return list of FormVisitTypeStatusSubjectMatrix->Object
	 * @throws CtdbException
	 */
	public List<FormVisitTypeStatusSubjectMatrix> getSubjectMatrixForFormVsVisitTypeWithCollectionStatusFilteredableByGuid(
			long studyId, String subjectId) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			return ReportingManagerDao.getInstance(conn)
					.getSubjectMatrixForFormVsVisitTypeWithCollectionStatusFilteredableByGuid(studyId, subjectId);
		} finally {
			this.close(conn);
		}
	}

	/**
	 * Retrieves the data for the "Forms Requiring Lock" report.
	 * 
	 * @param studyId - The ID of the currently selected study
	 * @return A list of ReportingForm objects containing the data for the report
	 * @throws CtdbException If an error occurred while querying the view.
	 */
	public List<ReportingForm> getFormsRequiringLockReport(long studyId) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			return ReportingManagerDao.getInstance(conn).getFormsRequiringLockReport(studyId);
		} finally {
			this.close(conn);
		}
	}

	/**
	 * Method to get the list of completed forms report
	 * 
	 * @return list of completed forms
	 * @throws CtdbException
	 */
	public List<ReportingForm> getCompletedFormsReport(long protocolId) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			return ReportingManagerDao.getInstance(conn).getCompletedFormsReport(protocolId);
		} finally {
			this.close(conn);
		}

	}

	/**
	 * Method to get the administrative forms list form the DAO
	 * 
	 * @return list of admin form
	 * @throws CtdbException
	 */
	public List<ReportingForm> getAdministeredFormReport(long protocolId) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			return ReportingManagerDao.getInstance(conn).getAdministeredFormReport(protocolId);
		} finally {
			this.close(conn);
		}
	}

	/**
	 * Method to get the study report from DAO
	 * 
	 * @return
	 * @throws CtdbException
	 */
	public List<ReportingForm> getStudyReport() throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			return ReportingManagerDao.getInstance(conn).getStudyReport();
		} finally {
			this.close(conn);
		}
	}

	/**
	 * Method to get the study report from DAO
	 * 
	 * @return
	 * @throws CtdbException
	 */
	public List<ReportingForm> getStudyReportForAStudy(long protocolId) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			return ReportingManagerDao.getInstance(conn).getStudyReportForAStudy(protocolId);
		} finally {
			this.close(conn);
		}
	}

	/**
	 * Retrieves the data for the "Performance Overview" report.
	 * 
	 * @param studyId - The ID of the study to query for
	 * @return A list of ReportingForm objects containing the results from the "performance_overview_view" view
	 * @throws CtdbException When a database error occurred
	 */
	public List<ReportingForm> getPerformanceOverviewReport(long studyId) throws CtdbException {
		List<ReportingForm> report = null;
		Connection conn = null;

		try {
			conn = getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			report = ReportingManagerDao.getInstance(conn).getPerformanceOverviewReport(studyId);
		} catch (SQLException sqle) {
			throw new CtdbException("Unable to get the Performance Overview report.", sqle);
		} finally {
			close(conn);
		}

		return report;
	}

	/**
	 * Retrieves the data for the "Performance Overview" report.
	 * 
	 * @param studyId - The ID of the study to query for
	 * @return A list of ReportingForm objects containing the results from the "performance_overview_view" view
	 * @throws CtdbException When a database error occurred
	 */
	public List<ReportingForm> getGuidsWithoutCollectionsReport(long studyId) throws CtdbException {
		List<ReportingForm> report = null;
		Connection conn = null;

		try {
			conn = getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			report = ReportingManagerDao.getInstance(conn).getGuidsWithoutCollectionsReport(studyId);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new CtdbException("Unable to get the GUIDs Without Collections report.", sqle);
		} finally {
			close(conn);
		}

		return report;
	}

	/**
	 * Retrieves the data for the "Performance Overview" report. If a protocol is selected, then only the results of
	 * that protocol are returned. If no protocol is select, results from all applicable protocols are returned.
	 * 
	 * @param protocolId - The ID of the currently selected protocol or -1.
	 * @return A list of ReportingForm objects containing the results from the database queries.
	 * @throws CtdbException If an error occurs while querying the database.
	 */
	public List<ReportingForm> getCompletedVisitsReport(long protocolId) throws CtdbException {
		List<ReportingForm> report = null;
		Connection conn = null;

		try {
			conn = getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			report = ReportingManagerDao.getInstance(conn).getCompletedVisitsReport(protocolId);
		} catch (SQLException sqle) {
			throw new CtdbException("Unable to get the \"Completed Visits\" report.", sqle);
		} finally {
			close(conn);
		}

		return report;
	}

	public List<SubmissionSummaryReport> getSubmissionSummaryReport(long studyId) throws CtdbException {
		List<SubmissionSummaryReport> reportList = null;
		Connection conn = null;

		try {
			conn = getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			reportList = ReportingManagerDao.getInstance(conn).getSubmissionSummaryReport(studyId);
		} catch (SQLException sqle) {
			throw new CtdbException("Unable to get the \"Submission Summary\" report.", sqle);
		} finally {
			close(conn);
		}

		return reportList;
	}

	public List<ScheduleReport> getScheduleReportByFilters(ScheduleReportFilter schReportFilter) throws CtdbException {
		List<ScheduleReport> reportList = new ArrayList<ScheduleReport>();
		Connection conn = null;

		try {
			conn = getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ReportingManagerDao dao = ReportingManagerDao.getInstance(conn);
			reportList.addAll(dao.getScheduleReportByFilters(schReportFilter));
		} catch (SQLException sqle) {
			throw new CtdbException("Unable to get the Schedule Report.", sqle);
		} finally {
			close(conn);
		}

		return reportList;
	}
}
