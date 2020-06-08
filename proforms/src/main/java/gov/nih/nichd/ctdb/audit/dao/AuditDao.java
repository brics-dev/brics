package gov.nih.nichd.ctdb.audit.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import gov.nih.nichd.ctdb.audit.domain.Audit;
import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;

public class AuditDao extends CtdbDao {

	/**
	 * Private Constructor to hide the instance creation implementation of the AuditDao object in memory. This will
	 * provide a flexible architecture to use a different pattern in the future without re-factoring the AuditDao.
	 */
	private AuditDao() {}

	/**
	 * Method to retrieve the instance of the AuditDao.
	 *
	 * @return AuditDao data object
	 */
	public static synchronized AuditDao getInstance() {
		return new AuditDao();
	}

	/**
	 * Method to retrieve the instance of the AuditDao. This method accepts a Database Connection to be used internally
	 * by the DAO. All transaction management will be handled at the BusinessManager level.
	 *
	 * @param conn Database connection to be used within this data object
	 * @return AuditDao data object
	 */
	public static synchronized AuditDao getInstance(Connection conn) {
		AuditDao dao = new AuditDao();
		dao.setConnection(conn);
		
		return dao;
	}

	public void createAudit(Audit a) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String insertStmt = "insert into audittable (id, protocolid, reason, updatedby, updateddate) values "
					+ "(DEFAULT, ?, ?, ?, CURRENT_TIMESTAMP) ";

			stmt = this.conn.prepareStatement(insertStmt);
			stmt.setLong(1, a.getProtocolId());
			stmt.setString(2, a.getReason());
			stmt.setLong(3, a.getUpdatedBy());

			////////////////////////////////////////////////////////////////
			// System.out.println("a.getId()--------------------------" + a.getId());
			// System.out.println("a.getProtocolId()------------------" + a.getProtocolId());
			// System.out.println("a.getReason()-----------------" + a.getReason());
			// System.out.println("a.getUpdatedBy()-------------------" + a.getUpdatedBy());
			////////////////////////////////////////////////////////////

			stmt.executeUpdate();
			a.setId(this.getInsertId(conn, "audit_seq"));
		}
		catch (SQLException sqle) {
			throw new CtdbException("Couldn't create a protocol audit record.", sqle);
		}
		finally {
			close(stmt);
		}
	}

	public void updateAudit(Audit a) throws CtdbException {
		PreparedStatement stmt = null;
		
		try {
			String sql = "update audittable set reason = ?, updatedby = ?, updateddate = CURRENT_TIMESTAMP where id = ? ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, a.getReason());
			stmt.setLong(2, a.getUpdatedBy());
			stmt.setLong(3, a.getId());
			stmt.executeUpdate();
		}
		catch (SQLException sqle) {
			throw new CtdbException("Failure updating audit : " + sqle.getMessage() + sqle);
		}
		finally {
			close(stmt);
		}
	}

	public void updateAuditWithReason(Audit a) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "update audittable set reason = ? where id = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, a.getReason());
			stmt.setLong(2, a.getId());
			stmt.executeUpdate();
		}
		catch (SQLException sqle) {
			throw new CtdbException("Failure updating audit : " + sqle.getMessage() + sqle);
		}
		finally {
			close(stmt);
		}
	}

	public Audit getAuditLast(long protocolId) throws CtdbException {
		Audit audit = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String query = "select a.id, a.reason, a.protocolid, a.updatedby, a.updateddate "
					+ " from audittable a where a.protocolid = ? order by id DESC limit 1";

			stmt = this.conn.prepareStatement(query);
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();

			if (rs.next()) {
				audit =  rsToAudit(rs);
			}
		}
		catch (SQLException sqle) {
			throw new CtdbException("Failure getting audit.", sqle);
		}
		finally {
			close(stmt);
			close(rs);
		}
		
		return audit;
	}

	private Audit rsToAudit(ResultSet rs) throws SQLException {
		Audit a = new Audit();

		a.setId(rs.getInt("id"));
		a.setReason(rs.getString("reason"));
		a.setProtocolId(rs.getInt("protocolid"));
		a.setUpdatedDate(rs.getTimestamp("updateddate"));
		a.setUpdatedBy(rs.getInt("updatedby"));

		return a;
	}
}
