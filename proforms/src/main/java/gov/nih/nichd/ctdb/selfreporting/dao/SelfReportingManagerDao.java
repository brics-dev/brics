package gov.nih.nichd.ctdb.selfreporting.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.selfreporting.form.SelfReportingLandingForm;
import gov.nih.nichd.ctdb.selfreporting.form.SelfReportingProperties;

public class SelfReportingManagerDao extends CtdbDao {

	private static Logger logger = Logger.getLogger(SelfReportingManagerDao.class);

	private static final String SQL_SELFREPORTING_LIST = "select pv.patientid, fi.intervalid, pv.visitdate, " +
			"f.eformid, f.name, af.administeredformid, ded.coll_status as status, ded.updateddate "+
			"from eform f " +
			"join form_interval fi on f.eformid = fi.eformid " +
			"join \"interval\" i on fi.intervalid = i.intervalid " +
			"join patientvisit pv on i.intervalid = pv.intervalid "+
			"left outer join administeredform af on (af.intervalid = i.intervalid and af.eformid = f.eformid and af.patientid = pv.patientid) " +
			"left outer join dataentrydraft ded on af.administeredformid = ded.administeredformid "+
			"where fi.selfreport = 'true' " +
			"and current_date >= date(pv.visitdate) - i.selfreportstart and current_date <= date(pv.visitdate) + i.selfreportend " +
			"and pv.token = ? ";


	/**
	 * Private Constructor to hide the instance creation implementation of the SelfReportingManagerDao object in memory.
	 * This will provide a flexible architecture to use a different pattern in the future without refactoring the
	 * SelfReportingManager.
	 */
	private SelfReportingManagerDao() {}

	/**
	 * Method to retrieve the instance of the SelfReportingManagerDao.
	 *
	 * @return SelfReportingManagerDao data object
	 */
	public static synchronized SelfReportingManagerDao getInstance() {
		return new SelfReportingManagerDao();
	}

	/**
	 * Method to retrieve the instance of the SelfReportingManagerDao. This method accepts a Database Connection to be
	 * used internally by the DAO. All transaction management will be handled at the BusinessManager level.
	 * 
	 * @param conn Database connection to be used within this data object
	 * @return SelfReportingManagerDao data object
	 */
	public static synchronized SelfReportingManagerDao getInstance(Connection conn) {
		SelfReportingManagerDao dao = new SelfReportingManagerDao();
		dao.setConnection(conn);
		return dao;
	}

	/**
	 * This method returns the list of self reporting forms associated with the token specified.
	 * 
	 * @param token - Defined token to retrieve the SelfReporting list.
	 * @return
	 * @throws CtdbException
	 */
	public List<SelfReportingLandingForm> getSelfReportingList(String token) throws CtdbException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<SelfReportingLandingForm> selfReportingList = new ArrayList<SelfReportingLandingForm>();

		try {
			stmt = conn.prepareStatement(SQL_SELFREPORTING_LIST);

			stmt.setString(1, token);
			rs = stmt.executeQuery();

			SelfReportingLandingForm srl = null;
			while (rs.next()) {
				srl = new SelfReportingLandingForm();
				srl.setPatientId(rs.getInt("patientId"));
				srl.setIntervalId(rs.getInt("intervalId"));
				srl.setFormId(rs.getInt("eformid"));

				Integer aformId = rs.getInt("administeredformid");
				srl.setAdministeredFormId(rs.wasNull() ? null : aformId);

				srl.setFormName(rs.getString("name"));
				srl.setStatus(rs.getString("status"));
				srl.setLastUpdated(rs.getTimestamp("updateddate"));
				srl.setToken(token);

				selfReportingList.add(srl);
			}
		} catch (SQLException e) {
			throw new CtdbException("Error occur while getting the SelfReportingList: " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		logger.info("SelfReportingManagerDao getSelfReportingList returns " + selfReportingList.size() + " results.");
		return selfReportingList;
	}
	
	public SelfReportingProperties getStartEndDates(String token) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String query = "select date(pv.visitdate) - i.selfreportstart as startDate, ";
			query += " date(pv.visitdate) + i.selfreportend as endDate ";
			query += " from interval i, patientvisit pv ";
			query += " where i.intervalid = pv.intervalid and pv.token = ?";
			
			SelfReportingProperties srp = new SelfReportingProperties();
			
			stmt = conn.prepareStatement(query);

			stmt.setString(1, token);
			rs = stmt.executeQuery();
			while (rs.next()) {
				srp.setEndDate(rs.getDate("endDate"));
				srp.setStartDate(rs.getDate("startDate"));
			}
			return srp;
		} catch (SQLException e) {
			throw new CtdbException("Error occur while getting the SelfReportingList: " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	public int getProtocolId(String token) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int protocolId = -1;
		try {
			String query = "SELECT protocolid FROM patientvisit WHERE token = ?";
			stmt = conn.prepareStatement(query);

			stmt.setString(1, token);
			rs = stmt.executeQuery();
			if (rs.next()) {
				protocolId = rs.getInt("protocolid");
			}
			
			return protocolId;
		} catch (SQLException e) {
			throw new CtdbException("Error occur while getting the SelfReportingList: " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	/**
	 * gets the scheduled visit date
	 * @param token
	 * @return
	 * @throws CtdbException
	 */
	public Date getScheduledVisitDate(String token) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Date scheduledVisitDate = null; 
		try {
			String query = "SELECT visitdate FROM patientvisit WHERE token = ?";
			stmt = conn.prepareStatement(query);

			stmt.setString(1, token);
			rs = stmt.executeQuery();
			if (rs.next()) {
				scheduledVisitDate = rs.getDate("visitdate");
			}
			
			return scheduledVisitDate;
		} catch (SQLException e) {
			throw new CtdbException("Error occur while getting the SelfReportingList: " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
}
