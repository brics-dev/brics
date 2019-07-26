package gov.nih.nichd.ctdb.protocol.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.postgresql.util.PSQLException;

import gov.nih.nichd.ctdb.attachments.domain.Attachment;
import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.DuplicateArchiveObjectException;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.InvalidRemovalException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.Version;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.patient.domain.PatientVisitPrepopValue;
import gov.nih.nichd.ctdb.protocol.domain.ClinicalLocation;
import gov.nih.nichd.ctdb.protocol.domain.ConfigureEformAuditDetail;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.protocol.domain.IntervalClinicalPoint;
import gov.nih.nichd.ctdb.protocol.domain.MilesStone;
import gov.nih.nichd.ctdb.protocol.domain.PointOfContact;
import gov.nih.nichd.ctdb.protocol.domain.PrepopDataElement;
import gov.nih.nichd.ctdb.protocol.domain.Procedure;
import gov.nih.nichd.ctdb.protocol.domain.ProcedureType;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.domain.ProtocolClosingOut;
import gov.nih.nichd.ctdb.protocol.domain.ProtocolDefaults;
import gov.nih.nichd.ctdb.protocol.domain.ProtocolLink;
import gov.nih.nichd.ctdb.protocol.domain.ProtocolUser;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.nichd.ctdb.util.dao.AddressDao;
import gov.nih.nichd.ctdb.util.domain.Address;
import gov.nih.tbi.commons.model.StudyType;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;

/**
 * ProtocolManagerDao interacts with the Data Layer for the ProtocolManager. The only job of the DAO is to manipulate
 * the data layer.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ProtocolManagerDao extends CtdbDao {
	private static final Logger logger = Logger.getLogger(ProtocolManagerDao.class);

	/**
	 * Private Constructor to hide the instance creation implementation of the ProtocolManagerDao object in memory. This
	 * will provide a flexible architecture to use a different pattern in the future without refactoring the
	 * ProtocolManager.
	 */
	private ProtocolManagerDao() {}

	/**
	 * Method to retrieve the instance of the ProtocolManagerDao.
	 *
	 * @return ProtocolManagerDao data object
	 */
	public static synchronized ProtocolManagerDao getInstance() {
		return new ProtocolManagerDao();
	}

	/**
	 * Method to retrieve the instance of the ProtocolManagerDao. This method accepts a Database Connection to be used
	 * internally by the DAO. All transaction management will be handled at the BusinessManager level.
	 *
	 * @param conn Database connection to be used within this data object
	 * @return ProtocolManagerDao data object
	 */
	public static synchronized ProtocolManagerDao getInstance(Connection conn) {
		ProtocolManagerDao dao = new ProtocolManagerDao();
		dao.setConnection(conn);
		return dao;
	}

	/**
	 * Creates a Protocol.
	 *
	 * @param protocol The Protocol Object to create and merge data with.
	 * @throws DuplicateObjectException if the protocol already exists in the system
	 * @throws CtdbException if any other errors occur while processing
	 */
	public Protocol createProtocol(Protocol protocol) throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;
		Attachment file = protocol.getDataSubmissionFile();

		try {
			String sqlStmt = "insert into protocol (protocolid, protocolnumber, name, imagefilename, welcomeurl, "
					+ "description, funding_organization, xprotocolstatusid, createddate, createdby, updateddate, "
					+ "updatedby, version, isevent, usepatientid, typename, lockformintervals, patientdisplaytype, "
					+ "subjectautoincrement, subjectnumberprefix, subjectnumbersuffix, subjectnumberstart, "
					+ "autoAssignPatientRoles, principalInvestigator, accountableInvestigator, useEbinder, "
					+ "studyproject, enableEsignature,brics_studyid) values (DEFAULT, ?, ?, ?, ?, ?, ?, ?, "
					+ "CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

			stmt = this.conn.prepareStatement(sqlStmt);
			stmt.setString(1, protocol.getProtocolNumber().toUpperCase());
			stmt.setString(2, protocol.getName());

			if ((file.getFile() != null) && (file.getFile().exists())) {
				String imageFileName = this.getImageFileName(protocol.getId(), file.getFileName());
				file.setFileName(imageFileName);
				stmt.setString(3, file.getFileName());
			} else {
				stmt.setString(3, "");
			}

			stmt.setString(4, protocol.getWelcomeUrl());
			stmt.setString(5, protocol.getDescription());
			stmt.setString(6, protocol.getOrginization());
			stmt.setLong(7, protocol.getStatus().getId());
			stmt.setLong(8, protocol.getCreatedBy());
			stmt.setLong(9, protocol.getUpdatedBy());
			stmt.setInt(10, 1);
			stmt.setBoolean(11, false);  // is event always 0
			stmt.setBoolean(12, protocol.isUsePatientName());

			// Handle if the study type is null.
			if (protocol.getStudyType() != null) {
				stmt.setString(13, protocol.getStudyType().getName());
			} else {
				stmt.setNull(13, Types.VARCHAR);
			}

			stmt.setString(14, Boolean.toString(protocol.isLockFormIntervals()));
			stmt.setInt(15, protocol.getPatientDisplayType());
			stmt.setBoolean(16, protocol.isAutoIncrementSubject());
			stmt.setString(17, protocol.getSubjectNumberPrefix());
			stmt.setString(18, protocol.getSubjectNumberSuffix());
			stmt.setLong(19, protocol.getSubjectNumberStart());
			stmt.setBoolean(20, protocol.isAutoAssociatePatientRoles());
			stmt.setString(21, protocol.getPrincipleInvestigator());
			stmt.setString(22, protocol.getAccountableInvestigator());
			stmt.setString(23, Boolean.toString(protocol.isUseEbinder()));
			stmt.setString(24, protocol.getStudyProject());
			stmt.setBoolean(25, protocol.isEnableEsignature());
			stmt.setString(26, protocol.getBricsStudyId());

			stmt.executeUpdate();
			protocol.setId(getInsertId(conn, "protocol_seq"));
		} catch (PSQLException e) {
			if (e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A study with the number " + protocol.getProtocolNumber() + " already exists in the system.",
						e);
			} else {
				throw new CtdbException("Unable to create new study.", e);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == Integer
					.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A sudy with the number " + protocol.getProtocolNumber() + " already exists in the system.", e);
			} else {
				throw new CtdbException("Unable to create new study.", e);
			}
		} finally {
			this.close(stmt);
		}

		return protocol;
	}

	/**
	 * Updates a protocol in the system.
	 *
	 * @param newStudy The protocol to update
	 * @throws ObjectNotFoundException if the role does not exist in the system
	 * @throws DuplicateObjectException if the role already exists in the system
	 * @throws CtdbException if any other errors occur while processing
	 */
	public void updateProtocol(Protocol newStudy)
			throws ObjectNotFoundException, DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;

		try {
			String sqlStmt = "update protocol set protocolnumber = ?, name = ?, version = version + 1, "
					+ "welcomeurl = ?, description = ?, xprotocolstatusid = ?, updatedby = ?, updateddate = CURRENT_TIMESTAMP, "
					+ "usepatientid = ?, lockformintervals = ?, patientdisplaytype = ? ,subjectautoincrement = ?, "
					+ "subjectnumberstart = ?, subjectnumberprefix = ?, subjectnumbersuffix = ?, autoAssignPatientRoles = ?, "
					+ "principalInvestigator = ?, accountableInvestigator = ?, useEbinder = ?, "
					+ "studyproject = ?, brics_studyid = ?, funding_organization = ?, enableEsignature = ?, typename = ? "
					+ "where protocolid = ? ";

			stmt = this.conn.prepareStatement(sqlStmt);

			int n = 1;
			stmt.setString(n++, newStudy.getProtocolNumber().toUpperCase());
			stmt.setString(n++, newStudy.getName());
			stmt.setString(n++, newStudy.getWelcomeUrl());
			stmt.setString(n++, newStudy.getDescription());
			stmt.setLong(n++, newStudy.getStatus().getId());
			stmt.setLong(n++, newStudy.getUpdatedBy());
			stmt.setBoolean(n++, newStudy.isUsePatientName());
			stmt.setString(n++, Boolean.toString(newStudy.isLockFormIntervals()));
			stmt.setInt(n++, newStudy.getPatientDisplayType());
			stmt.setBoolean(n++, newStudy.isAutoIncrementSubject());
			stmt.setLong(n++, newStudy.getSubjectNumberStart());
			stmt.setString(n++, newStudy.getSubjectNumberPrefix());
			stmt.setString(n++, newStudy.getSubjectNumberSuffix());
			stmt.setBoolean(n++, newStudy.isAutoAssociatePatientRoles());
			stmt.setString(n++, newStudy.getPrincipleInvestigator());
			stmt.setString(n++, newStudy.getAccountableInvestigator());
			stmt.setBoolean(n++, newStudy.isUseEbinder());
			stmt.setString(n++, newStudy.getStudyProject());

			// Set the BRICS study ID
			if (newStudy.getBricsStudyId().length() > 0) {
				stmt.setString(n++, newStudy.getBricsStudyId());
			} else {
				stmt.setNull(n++, Types.VARCHAR);
			}

			stmt.setString(n++, newStudy.getOrginization());
			stmt.setBoolean(n++, newStudy.isEnableEsignature());

			// Check if the study type is null.
			if (newStudy.getStudyType() != null) {
				stmt.setString(n++, newStudy.getStudyType().getName());
			} else {
				stmt.setNull(n++, Types.VARCHAR);
			}

			stmt.setLong(n++, newStudy.getId());

			int recordsUpdated = stmt.executeUpdate();

			if (recordsUpdated == 0) {
				throw new ObjectNotFoundException(
						"The Protocol with ID: " + newStudy.getId() + " does not exist in the system.");
			}
		} catch (PSQLException e) {
			if (e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A duplicate protocol number or BRICS study ID already exists in the system.", e);
			} else {
				throw new CtdbException("Unable to update Protocol with ID " + newStudy.getId() + ".", e);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == Integer
					.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A duplicate protocol number or BRICS study ID already exists in the system.", e);
			} else {
				throw new CtdbException("Unable to update Study with ID " + newStudy.getId() + ".", e);
			}
		} finally {
			this.close(stmt);
		}

	}

	/**
	 * Soft delete (update) a protocol in the system.
	 *
	 * @param protocol The protocol to soft deleted (update)
	 * @throws ObjectNotFoundException if the role does not exist in the system
	 * @throws CtdbException if any other errors occur while processing
	 */
	public void softDeleteProtocol(Protocol protocol)
			throws ObjectNotFoundException, DuplicateObjectException, CtdbException {

		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(100);
			sql.append(
					"update protocol set protocolnumber = ?, name = ?, deleteFlag = ?, updatedby = ?, updateddate = CURRENT_TIMESTAMP ");
			sql.append(" where protocolid = ?");

			stmt = this.conn.prepareStatement(sql.toString());

			stmt.setString(1, protocol.getProtocolNumber().toUpperCase());
			stmt.setString(2, protocol.getName());
			stmt.setBoolean(3, protocol.getDeleteFlag());
			stmt.setLong(4, protocol.getUpdatedBy());
			stmt.setLong(5, protocol.getId());

			int recordsUpdated = stmt.executeUpdate();

			if (recordsUpdated == 0) {
				throw new ObjectNotFoundException(
						"The Study with ID: " + protocol.getId() + " does not exist in the system.");
			}
		} catch (PSQLException e) {
			if (e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A protocol with the number " + protocol.getProtocolNumber() + " already exists in the system.",
						e);
			} else {
				throw new CtdbException("Unable to update Study with ID " + protocol.getId() + ".", e);
			}
		}

		catch (SQLException e) {
			if (e.getErrorCode() == Integer
					.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A protocol with the number " + protocol.getProtocolNumber() + " already exists in the system.",
						e);
			} else {
				throw new CtdbException("Unable to update Study with ID " + protocol.getId() + ".", e);
			}
		} finally {
			this.close(stmt);
		}

	}

	/**
	 * Versions a Protocol in the CTDB System.
	 *
	 * @param oldStudy - The older version of the study that is about to be updated
	 * @throws DuplicateObjectException thrown if the protocol already exists in the system based on the unique
	 *         constraints
	 * @throws CtdbException thrown if any other errors occur while processing
	 */
	public void versionProtocol(Protocol oldStudy) throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;

		try {
			String sqlStmt = "insert into protocolarchive (protocolid, version, protocolnumber, name, "
					+ "welcomeurl, description, funding_organization, xprotocolstatusid, createddate, "
					+ "createdby, updateddate, updatedby, isevent, usepatientid, typename, lockformintervals, "
					+ "patientdisplaytype, subjectautoincrement, subjectnumberprefix, subjectnumbersuffix, "
					+ "subjectnumberstart, autoAssignPatientRoles, principalInvestigator, accountableInvestigator, "
					+ "useEbinder, studyproject, imagefilename, brics_studyid, enableEsignature) "
					+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

			stmt = this.conn.prepareStatement(sqlStmt);

			stmt.setLong(1, oldStudy.getId());
			stmt.setInt(2, oldStudy.getVersion().getVersionNumber());
			stmt.setString(3, oldStudy.getProtocolNumber().toUpperCase());
			stmt.setString(4, oldStudy.getName());
			stmt.setString(5, oldStudy.getWelcomeUrl());
			stmt.setString(6, oldStudy.getDescription());
			stmt.setString(7, oldStudy.getOrginization());
			stmt.setLong(8, oldStudy.getStatus().getId());
			stmt.setTimestamp(9, new Timestamp(oldStudy.getCreatedDate().getTime()));
			stmt.setLong(10, oldStudy.getCreatedBy());
			stmt.setTimestamp(11, new Timestamp(oldStudy.getUpdatedDate().getTime()));
			stmt.setLong(12, oldStudy.getUpdatedBy());
			stmt.setBoolean(13, oldStudy.isEvent());
			stmt.setBoolean(14, oldStudy.isUsePatientName());

			// Check if the study type is null.
			if (oldStudy.getStudyType() != null) {
				stmt.setString(15, oldStudy.getStudyType().getName());
			} else {
				stmt.setNull(15, Types.VARCHAR);
			}

			stmt.setString(16, Boolean.toString(oldStudy.isLockFormIntervals()));
			stmt.setInt(17, oldStudy.getPatientDisplayType());
			stmt.setBoolean(18, oldStudy.isAutoIncrementSubject());
			stmt.setString(19, oldStudy.getSubjectNumberPrefix());
			stmt.setString(20, oldStudy.getSubjectNumberSuffix());
			stmt.setLong(21, oldStudy.getSubjectNumberStart());
			stmt.setBoolean(22, oldStudy.isAutoAssociatePatientRoles());
			stmt.setString(23, oldStudy.getPrincipleInvestigator());
			stmt.setString(24, oldStudy.getAccountableInvestigator());
			stmt.setString(25, Boolean.toString(oldStudy.isUseEbinder()));
			stmt.setString(26, oldStudy.getStudyProject());
			stmt.setString(27, oldStudy.getDataSubmissionFile().getFileName());
			stmt.setString(28, oldStudy.getBricsStudyId());
			stmt.setBoolean(29, oldStudy.isEnableEsignature());

			stmt.executeUpdate();
		} catch (PSQLException e) {
			if (e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A protocol with the same version already exists in the system archive.", e);
			} else {
				throw new CtdbException("Unable to version protocol: " + e.getMessage(), e);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == Integer
					.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A protocol with the same version already exists in the system archive.", e);
			} else {
				throw new CtdbException("Unable to version protocol.", e);
			}
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Retrieves a protocol from the system based on the unique identifier: protocol ID.
	 *
	 * @param protocolId The protocol ID
	 * @throws ObjectNotFoundException if the protocol cannot be found
	 * @throws CtdbException if any other errors occur while processing
	 */
	public Protocol getProtocol(int protocolId) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(25);
			sql.append("select protocol.*, xprotocolstatus.name xprotocolstatusname, ");
			sql.append(" usr.username updated_username  ");
			sql.append("from protocol, xprotocolstatus, usr ");
			sql.append("where coalesce(PROTOCOL.DELETEFLAG, false) != true and ");
			sql.append(" protocol.xprotocolstatusid = xprotocolstatus.xprotocolstatusid and ");
			sql.append(" protocolid = ? and protocol.updatedby = usr.usrid ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);

			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new ObjectNotFoundException("The protocol with the ID: " + protocolId + " could not be found.");
			}
			Protocol protocol;
			protocol = this.rsToProtocol(rs);
			protocol.setUpdatedByUsername(rs.getString("updated_username"));

			return protocol;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get Protocol.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves all Protocols in the CTDB System
	 *
	 * @return A list of all Protocols in the CTDB System.
	 * @throws CtdbException if any errors occur while processing
	 */
	public List<Protocol> getProtocols() throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			List<Protocol> protocols = new ArrayList<Protocol>();

			StringBuffer sql = new StringBuffer(800);
			sql.append("select protocol.*, xprotocolstatus.name xprotocolstatusname, ");
			sql.append(" updated.username updated_username  ");
			sql.append("from protocol, xprotocolstatus, usr updated ");
			sql.append("where coalesce(PROTOCOL.DELETEFLAG, false) != true and protocol.protocolid > 0 and ");
			sql.append(" protocol.xprotocolstatusid = xprotocolstatus.xprotocolstatusid and ");
			sql.append(" protocol.updatedby = updated.usrid ");

			stmt = this.conn.prepareStatement(sql.toString());

			rs = stmt.executeQuery();

			Protocol protocol;
			while (rs.next()) {
				protocol = this.rsToProtocol(rs);
				protocol.setUpdatedByUsername(rs.getString("updated_username"));
				protocols.add(protocol);
			}
			return protocols;
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve protocols.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public Map<Integer, String> getProtocolIds() throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Map<Integer, String> protocolIdAndNamesMap = new HashMap<Integer, String>();

		try {
			String sql = "select protocolid, name from protocol ";

			stmt = this.conn.prepareStatement(sql);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Integer protocolId = rs.getInt("protocolid");
				String protocolName = rs.getString("name");

				protocolIdAndNamesMap.put(protocolId, protocolName);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve study/name map.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return protocolIdAndNamesMap;
	}

	/**
	 * Retrieves all versions of a protocol stored. A version consists of modifications to the metadata
	 * (number,name,description,status) about a protocol.
	 *
	 * @param protocolId The unique identifier of the Protocol to retrieve
	 * @return An list of all versions for a single protocol. The array will ordered by versions such that index 0 will
	 *         be the first protocol version. If the protocol does not exist an empty array will be returned.
	 * @throws CtdbException if any errors occur
	 */

	public List<Protocol> getProtocolVersions(int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(25);
			sql.append(
					"select protocol_view.*, xprotocolstatus.name xprotocolstatusname, usr.username updated_username ");
			sql.append("from protocol_view, xprotocolstatus, usr ");
			sql.append("where protocol_view.xprotocolstatusid = xprotocolstatus.xprotocolstatusid ");
			sql.append("and protocolid = ? and protocol_view.updatedby = usr.usrid ");
			sql.append("order by protocol_view.version desc ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);

			rs = stmt.executeQuery();

			List<Protocol> protocols = new ArrayList<Protocol>();
			Protocol protocol;
			while (rs.next()) {
				protocol = this.rsToProtocol(rs);
				protocol.setUpdatedByUsername(rs.getString("updated_username"));
				protocols.add(protocol);
			}

			return protocols;
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve protocols.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves a protocol user list based on the protocol ID. If no user is associated with the protocol, the list is
	 * empty.
	 *
	 * @param protocolId The Protocol ID to retrieve
	 * @return List of ProtocolUser data object
	 * @throws CtdbException if any other errors occur while processing
	 */
	public List<ProtocolUser> getProtocolUsers(int protocolId, int siteId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(500);
			sql.append("select protocolusrrole.*, usr.*  ");
			sql.append("from protocolusrrole, usr ");
			sql.append("where protocolid = ? and ");
			sql.append(" protocolusrrole.usrid = usr.usrid ");
			if (siteId != Integer.MIN_VALUE) {
				sql.append(" and protocolusrrole.siteid = ? ");
			}

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			if (siteId != Integer.MIN_VALUE) {
				stmt.setLong(2, siteId);
			}
			rs = stmt.executeQuery();

			List<ProtocolUser> users = new ArrayList<ProtocolUser>();
			while (rs.next())
				users.add(this.rsToProtocolUser(rs));

			return users;
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve user associated with protocol.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves a list of role IDs for a given protocol user. If no role is assigned to the protocol user the list is
	 * empty.
	 *
	 * @param protocolId The Protocol ID to retrieve
	 * @param userId The User ID to retrieve
	 * @return List of roleIds which are Integers
	 * @throws CtdbException if any other errors occur while processing
	 */
	public List<Integer> getProtocolRoles(int userId, int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(500);
			sql.append("select * ");
			sql.append("from protocolusrrole ");
			sql.append("where protocolid = ? and usrid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			stmt.setLong(2, userId);
			rs = stmt.executeQuery();

			List<Integer> roleIds = new ArrayList<Integer>();
			while (rs.next()) {
				Integer roleid = new Integer(rs.getInt("roleid"));
				roleIds.add(roleid);
			}

			return roleIds;
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve roles for a protocol user.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves a protocol list based on the user ID. If no protocol is associated with the user, the list is empty.
	 *
	 * @param userId The User ID to retrieve roles for
	 * @return List of ProtocolUser data object
	 * @throws CtdbException if any other errors occur while processing
	 */
	public List<Protocol> getUserProtocols(int userId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(1000);
			sql.append("select distinct protocol.protocolid, protocol.*, xprotocolstatus.name xprotocolstatusname, ");
			sql.append(" updated.username updated_username   ");
			sql.append("from protocol, xprotocolstatus, usr updated, protocolusrrole ");
			sql.append("where coalesce(PROTOCOL.DELETEFLAG, false) != true AND protocol.protocolid > 0 and ");
			sql.append(" protocol.xprotocolstatusid = xprotocolstatus.xprotocolstatusid and ");
			sql.append(" protocol.updatedby = updated.usrid and ");
			sql.append(" (protocolusrrole.protocolid = protocol.protocolid and ");
			sql.append("  protocolusrrole.usrid = ?) ");
			sql.append(" and protocol.protocolid != -1 ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, userId);
			rs = stmt.executeQuery();

			List<Protocol> protocols = new ArrayList<Protocol>();
			Protocol protocol;
			while (rs.next()) {
				protocol = this.rsToProtocol(rs);
				// protocol.setProtocolTypeName(rs.getString("typename"));
				protocol.setUpdatedByUsername(rs.getString("updated_username"));
				protocols.add(protocol);
			}

			return protocols;
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve protocols associated with user.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Associates a Protocol with users. Each user has an assigned role for the protocol.
	 *
	 * @param protocolId The protocol ID
	 * @param protocolUsers The list of all Protocol users to update. Only username and roleId need to be set in each
	 *        protocolUser object in the list.
	 * @throws CtdbException if any other errors occur while processing
	 */
	public void associateProtocolUsers(int protocolId, int siteId, List<ProtocolUser> protocolUsers)
			throws CtdbException {
		PreparedStatement dStmt = null;
		PreparedStatement iStmt = null;

		try {
			StringBuffer dSql = new StringBuffer(50);
			dSql.append("delete FROM protocolusrrole where protocolid = ? ");
			if (siteId == Integer.MIN_VALUE) {
				dSql.append(" and siteid is null ");
			} else {
				dSql.append(" and siteid = ? ");
			}

			StringBuffer iSql = new StringBuffer(100);
			iSql.append("insert into protocolusrrole(protocolid,usrid, roleid, siteid) ");
			iSql.append("values(?,?,?, ?)");

			dStmt = this.conn.prepareStatement(dSql.toString());
			dStmt.setLong(1, protocolId);
			if (siteId != Integer.MIN_VALUE) {

				dStmt.setLong(2, siteId);
			}
			dStmt.executeUpdate();

			iStmt = this.conn.prepareStatement(iSql.toString());
			iStmt.setLong(1, protocolId);

			for (ProtocolUser protocolUser : protocolUsers) {
				iStmt.setLong(2, protocolUser.getId());
				iStmt.setLong(3, protocolUser.getRoleId());
				if (siteId != Integer.MIN_VALUE) {
					iStmt.setLong(4, siteId);
				} else {
					iStmt.setNull(4, java.sql.Types.NUMERIC);
				}
				iStmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to associate users with Protocol ID: " + protocolId + ".", e);
		} finally {
			this.close(dStmt);
			this.close(iStmt);
		}
	}

	public void associateProtocolUser(int protocolId, int siteId, int roleId, int userId) throws CtdbException {
		PreparedStatement searchStmt = null;
		StringBuffer searchSql = new StringBuffer();
		searchSql.append("select protocolid FROM protocolusrrole WHERE protocolid = ? AND usrid = ?");

		PreparedStatement deleteStmt = null;
		StringBuffer deleteSql = new StringBuffer();
		deleteSql.append("delete FROM protocolusrrole where protocolid = ? and usrid = ?");

		PreparedStatement insertStmt = null;
		StringBuffer insertSql = new StringBuffer();
		insertSql.append("insert into protocolusrrole(protocolid, usrid, roleid, siteid)");
		insertSql.append("values(?, ?, ?, ?)");

		ResultSet rs = null;

		try {
			// search for user/protocol pairing

			searchStmt = this.conn.prepareStatement(searchSql.toString());
			searchStmt.setLong(1, protocolId);
			searchStmt.setLong(2, userId);
			rs = searchStmt.executeQuery();
			if (rs.next()) {
				// if exists, delete current listing
				deleteStmt = this.conn.prepareStatement(deleteSql.toString());
				deleteStmt.setLong(1, protocolId);
				deleteStmt.setLong(2, userId);
				deleteStmt.executeUpdate();
			}

			// if we are adding a role in, add it
			if (roleId != 0) {
				// insert new listing
				insertStmt = this.conn.prepareStatement(insertSql.toString());
				insertStmt.setLong(1, protocolId);
				insertStmt.setLong(2, userId);
				insertStmt.setLong(3, roleId);
				if (siteId != Integer.MIN_VALUE && siteId != 0) {
					insertStmt.setLong(4, siteId);
				} else {
					insertStmt.setNull(4, java.sql.Types.NUMERIC);
				}
				insertStmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to associate users with Protocol ID: " + protocolId + ".", e);
		} finally {
			this.close(rs);
			this.close(searchStmt);
			this.close(deleteStmt);
		}
	}

	/**
	 * Creates a protocol site link in the system. A site link is a URL that can be accessed through the CTDB system.
	 *
	 * @param protocolLink The protocol site link to create
	 * @throws DuplicateObjectException if a protocol site link with the same name already exists in the system
	 * @throws CtdbException if any other errors occur while processing
	 */
	public void createProtocolLink(ProtocolLink protocolLink) throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(400);
			sql.append("insert into protocollink(protocollinkid, protocolid, name, description, address, orderval) ");
			sql.append("select ?, ?, ?, ?, ?, coalesce(max(orderval)+1, 1) from protocollink ");

			stmt = this.conn.prepareStatement(sql.toString());
			// int id = this.getSequenceValues(this.conn, "protocollink_seq", 1);
			// protocolLink.setId(id);
			// stmt.setInt(1, id);
			stmt.setLong(1, this.getNextSequenceValue(conn, "protocollink_seq"));
			stmt.setLong(2, protocolLink.getProtocolId());
			stmt.setString(3, protocolLink.getName());
			stmt.setString(4, protocolLink.getDescription());
			stmt.setString(5, protocolLink.getAddress());

			stmt.executeUpdate();
			protocolLink.setId(getInsertId(conn, "protocollink_seq"));
		} catch (PSQLException e) {
			if (e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A study site link with the name " + protocolLink.getName() + " already exists in the system.",
						e);
			} else {
				throw new CtdbException("Unable to create new study site link.", e);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == Integer
					.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A study site link with the name " + protocolLink.getName() + " already exists in the system.",
						e);
			} else {
				throw new CtdbException("Unable to create new study site link.", e);
			}
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Updates a protocol site link in the system. A site link is a URL that can be accessed through the CTDB system.
	 *
	 * @param protocolLink The protocolLink to update
	 * @throws ObjectNotFoundException if the site link does not exist in the system
	 * @throws DuplicateObjectException if a site link with the same name already exists in the system
	 * @throws CtdbException if any other errors occur while processing
	 */
	public void updateProtocolLink(ProtocolLink protocolLink)
			throws ObjectNotFoundException, DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("update protocollink set protocolid = ?, name = ?, description = ?, address = ? ");
			sql.append("where protocollinkid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolLink.getProtocolId());
			stmt.setString(2, protocolLink.getName());
			stmt.setString(3, protocolLink.getDescription());
			stmt.setString(4, protocolLink.getAddress());
			stmt.setLong(5, protocolLink.getId());

			int recordsUpdated = stmt.executeUpdate();

			if (recordsUpdated == 0) {
				throw new ObjectNotFoundException(
						"The protocol link with ID: " + protocolLink.getId() + " does not exist in the system.");
			}
		} catch (PSQLException e) {
			if (e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A study link with the name " + protocolLink.getName() + " already exists in the system.", e);
			} else {
				throw new CtdbException("Unable to update site link with ID " + protocolLink.getId() + ".", e);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == Integer
					.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A study link with the name " + protocolLink.getName() + " already exists in the system.", e);
			} else {
				throw new CtdbException("Unable to update study site link with ID " + protocolLink.getId() + ".", e);
			}
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Updates the ordering of protocol site links for a protocol. A site link is a URL that can be accessed through the
	 * CTDB system. This ordering will be used when displaying URLs to the end user. If a URL is not ordered, it will be
	 * ordered by name.
	 *
	 * @param protocolLinkId The CTDB ProtocolLink ID to update
	 * @param orderVal The CTDB ProtocolLink order
	 * @throws CtdbException if any other errors occur while processing
	 */
	public void updateProtocolLinkOrdering(int protocolLinkId, int orderVal) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("update protocollink set orderval = ? where protocollinkid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, orderVal);
			stmt.setLong(2, protocolLinkId);

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to update protocol site link order with ID " + protocolLinkId + ".", e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Retrieves a protocol site link from the system based on the unique identifier
	 *
	 * @param protocolLinkId The SiteLink ID to retrieve
	 * @return ProtocolLink data object
	 * @throws ObjectNotFoundException if the site link does not exist in the system
	 * @throws CtdbException if any other errors occur while processing
	 */
	public ProtocolLink getProtocolLink(int protocolLinkId) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(25);
			sql.append("select * from protocollink where protocollinkid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolLinkId);

			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new ObjectNotFoundException(
						"The protocol link with ID: " + protocolLinkId + " could not be found.");
			}

			return this.rsToProtocolLink(rs);
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve protocol site link.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves all Protocol Site Links for a protocol
	 *
	 * @return A list of all Protocol Site Links for a protocol.
	 * @throws CtdbException if any errors occur while processing
	 */
	public List<ProtocolLink> getProtocolLinks(int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			List<ProtocolLink> protocolLinks = new ArrayList<ProtocolLink>();

			StringBuffer sql = new StringBuffer(25);
			sql.append("select * from protocollink where protocolid = ? order by orderval, name");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);

			rs = stmt.executeQuery();

			while (rs.next()) {
				protocolLinks.add(this.rsToProtocolLink(rs));
			}

			return protocolLinks;
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve all protocol site links.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Deletes a protocol site link from the system based on the unique identifier
	 *
	 * @param protocolLinkId The ProtocolLink ID to delete
	 * @throws CtdbException if any other errors occur while processing
	 */
	public void deleteProtocolLink(int protocolLinkId) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(25);
			sql.append("delete FROM protocollink where protocollinkid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolLinkId);

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to delete protocol site link.", e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Retrieves the protocol associated with an interval
	 *
	 * @param intervalname Name of the interval to retrieve
	 * @return Protocol object
	 * @throws ObjectNotFoundException if the interval does not exist in the system
	 * @throws CtdbException if any other errors occur while processing
	 */
	public Protocol getIntervalProtocol(String intervalname) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(25);
			sql.append("select protocol.*, xprotocolstatus.name xprotocolstatusname, ");
			sql.append("usr.username updated_username ");
			sql.append("from protocol, xprotocolstatus, usr, interval ");
			sql.append("where (protocol.deleteflag != TRUE or protocol.deleteflag is null) and ");
			sql.append("protocol.xprotocolstatusid = xprotocolstatus.xprotocolstatusid ");
			sql.append("and protocol.updatedby = usr.usrid ");
			sql.append("and protocol.protocolid = interval.protocolid ");
			sql.append("and interval.name = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setString(1, intervalname);

			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new ObjectNotFoundException("The interval : " + intervalname + " could not be found.");
			}

			Protocol protocol;
			protocol = this.rsToProtocol(rs);
			protocol.setUpdatedByUsername(rs.getString("updated_username"));

			return protocol;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get Interval.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public void createEform(BasicEform basicEform, long studyId) throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlShortNameProtocolUniqueness = "select eformid from eform where shortname = ? and protocolid = ? ";

		try {
			stmt = this.conn.prepareStatement(sqlShortNameProtocolUniqueness);
			stmt.setString(1, basicEform.getShortName());
			stmt.setLong(2, studyId);
			rs = stmt.executeQuery();

			if (rs.next()) {
				basicEform.setId(Long.valueOf(rs.getLong("eformid")));

				return;
			}
		} catch (SQLException e) {
			throw new CtdbException("Error occurred while determining eForm to protocol uniqueness.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		try {
			String sql =
					"insert into eform (eformid, shortname, name, data_structure_name, protocolid, allow_multiple_collection_instances, "
							+ "updateddate, createddate) values (DEFAULT, ?, ?, ?, ?, ?, ?, ?) ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, basicEform.getShortName());
			stmt.setString(2, basicEform.getTitle());
			stmt.setString(3, basicEform.getFormStructureShortName());
			stmt.setLong(4, studyId);
			stmt.setBoolean(5, basicEform.isAllowMultipleCollectionInstances());
			stmt.setDate(6, new java.sql.Date(basicEform.getCreateDate().getTime()));
			stmt.setDate(7, new java.sql.Date(basicEform.getCreateDate().getTime()));
			stmt.executeUpdate();

			basicEform.setId(Long.valueOf(getInsertId(conn, "eform_seq")));

		} catch (PSQLException e) {
			if (e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A eForm with the name " + basicEform.getShortName() + " already exists in the system.", e);
			} else {
				throw new CtdbException("Unable to create a new eForm.", e);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == Integer
					.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A eForm with the name " + basicEform.getShortName() + " already exists in the system.", e);
			} else {
				throw new CtdbException("Unable to create a new Eform.", e);
			}
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Creates a visit type in the database.
	 *
	 * @param visitType - The visit type object to create
	 * @throws DuplicateObjectException If the visit type already exists in the system
	 * @throws CtdbException If any other database errors occur while creating the visit type
	 */
	public Interval createVisitType(Interval visitType) throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "insert into interval(intervalid, protocolid, name, description, CATEGORY, XINTERVALTYPEID, "
					+ "createddate, createdby, updateddate, updatedby, version, selfreportstart, selfreportend) "
					+ "values (DEFAULT, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, 1, ?, ?) ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, visitType.getProtocolId());
			stmt.setString(2, visitType.getName());
			stmt.setString(3, visitType.getDescription());
			stmt.setString(4, visitType.getCategory());
			stmt.setLong(5, visitType.getIntervalType());
			stmt.setLong(6, visitType.getCreatedBy());
			stmt.setLong(7, visitType.getUpdatedBy());
			stmt.setInt(8, visitType.getSelfReportStart());
			stmt.setInt(9, visitType.getSelfReportEnd());

			stmt.executeUpdate();
			visitType.setId(getInsertId(conn, "interval_seq"));
		} catch (PSQLException e) {
			if (e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A visit type with the name " + visitType.getName() + " already exists in the system.", e);
			} else {
				throw new CtdbException("Unable to create a new visit type: " + e.getMessage(), e);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == Integer
					.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A visit type with the name " + visitType.getName() + " already exists in the system.", e);
			} else {
				throw new CtdbException("Unable to create a new visit type.", e);
			}
		} finally {
			this.close(stmt);
		}

		return visitType;
	}

	/**
	 * Updates a visit type in the database.
	 *
	 * @param visitType - The visit type to update
	 * @param newVersionCreated - A boolean flag indicating if a new version of the visit type has been created
	 * @throws ObjectNotFoundException If the visit type does not exist in the system
	 * @throws DuplicateObjectException If the visit type already exists in the system
	 * @throws CtdbException If any other database errors occur while updating the visit type
	 */
	public void updateInterval(Interval visitType, boolean newVersionCreated)
			throws ObjectNotFoundException, DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer("update interval set protocolid = ?, ");

			if (newVersionCreated) {
				sql.append("version = version + 1, ");
			}

			sql.append("name = ?, description = ?, category = ?, xintervaltypeid = ?, ");
			sql.append("updatedby = ?, selfreportstart = ?, selfreportend = ?, ");
			sql.append("updateddate = CURRENT_TIMESTAMP where intervalid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, visitType.getProtocolId());
			stmt.setString(2, visitType.getName());
			stmt.setString(3, visitType.getDescription());
			stmt.setString(4, visitType.getCategory());
			stmt.setLong(5, visitType.getIntervalType());
			stmt.setLong(6, visitType.getUpdatedBy());
			stmt.setInt(7, visitType.getSelfReportStart());
			stmt.setInt(8, visitType.getSelfReportEnd());
			stmt.setLong(9, visitType.getId());

			int recordsUpdated = stmt.executeUpdate();

			if (recordsUpdated == 0) {
				throw new ObjectNotFoundException(
						"The visit type with ID: " + visitType.getId() + " does not exist in the system.");
			}
		} catch (PSQLException e) {
			if (e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A visit type with the name " + visitType.getName() + " already exists in the system.", e);
			} else {
				throw new CtdbException("Unable to update the visit type with ID " + visitType.getId() + ".", e);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == Integer
					.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A visit type with the name " + visitType.getName() + " already exists in the system.", e);
			} else {
				throw new CtdbException("Unable to update the visit type with ID " + visitType.getId() + ".", e);
			}
		} finally {
			this.close(stmt);
		}
	}

	public void updateIntervalOrder(String[] intervalIds) throws CtdbException {
		PreparedStatement stmt = null;
		String vtId = "";

		try {
			String sql = "update interval set orderval = ? where intervalid = ? ";
			stmt = this.conn.prepareStatement(sql);

			for (int i = 0; i < intervalIds.length; i++) {
				vtId = intervalIds[i];
				stmt.setInt(1, i + 1);
				stmt.setLong(2, Long.parseLong(vtId));
				stmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to update interval order.", e);
		} catch (NumberFormatException nfe) {
			throw new CtdbException("Unable to convert " + vtId + " to a number.", nfe);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Creates a new version of a visit type in the archive table.
	 *
	 * @param intervalId - The visit type ID to version
	 * @throws CtdbException If any other database errors occur while recording the new version
	 * @throws DuplicateArchiveObjectException If a duplicate entry attempt occurs while creating a new version in the
	 *         visit type (interval) archive table
	 */
	public void versionInterval(int intervalId) throws CtdbException, DuplicateArchiveObjectException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("insert into intervalarchive select * from interval where intervalid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, intervalId);

			stmt.executeUpdate();
		} catch (PSQLException e) {
			if (e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateArchiveObjectException(
						"A visit type with the same version already exists in the system archive.", e);
			} else {
				throw new CtdbException("Unable to create a version of the visit type.", e);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == Integer
					.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateArchiveObjectException(
						"A visit type with the same version already exists in the system archive.", e);
			} else {
				throw new CtdbException("Unable to create a version of the visit type.", e);
			}
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Retrieves a Interval from the system based on the unique identifier.
	 *
	 * @param intervalId The unique identifier of the Interval to retrieve
	 * @return Interval object
	 * @throws ObjectNotFoundException if the interval does not exist in the system
	 * @throws CtdbException if any other errors occur while processing
	 */
	public Interval getInterval(int intervalId) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(25);
			sql.append("select * from interval where intervalid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, intervalId);

			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new ObjectNotFoundException("The interval with the ID: " + intervalId + " could not be found.");
			}

			return this.rsToInterval(rs);
		} catch (SQLException e) {
			throw new CtdbException("Unable to get Interval.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves all intervals in the system associated with a protocol
	 *
	 * @param protocolId The protocol ID.
	 * @return The list of intervals in the system. The list will be empty if no intervals exist in the system.
	 * @throws CtdbException if any errors occur while processing
	 */
	public List<Interval> getIntervals(int protocolId) throws CtdbException {
		return getIntervals(protocolId, Interval.INTERVAL_ORDER_BY_NAME);
	}

	/**
	 * Retrieves all intervals in the system associated with a protocol
	 *
	 * @param protocolId The protocol ID.
	 * @return The list of intervals in the system. The list will be empty if no intervals exist in the system.
	 * @throws CtdbException if any errors occur while processing
	 */
	public List<Interval> getIntervals(int protocolId, String orderByStr) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Interval> intervals = new LinkedList<Interval>();

		try {
			String sql = "select * from interval where protocolId = ? order by " + orderByStr;
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				intervals.add(this.rsToInterval(rs));
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve intervals.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return intervals;
	}

	/**
	 * Retrieves all intervals in the system associated with a patient
	 *
	 * @param patientId The patient ID.
	 * @return The list of intervals in the system. The list will be empty if no intervals exist in the system.
	 * @throws CtdbException if any errors occur while processing
	 */
	public List<Interval> getIntervalsByPatient(int protocolId, int patientId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Interval> intervals = new LinkedList<Interval>();

		try {
			String sql = "select distinct v.* from interval v "
					+ " inner join patientvisit pv on pv.intervalid = v.intervalid "
					+ " where pv.protocolid = ? and pv.patientid = ? " + " order by orderval, v.name ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, protocolId);
			stmt.setLong(2, patientId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				intervals.add(this.rsToInterval(rs));
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve intervals.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return intervals;
	}

	/**
	 * Gets list of all unique eforms that are associated with visit types for a protocol and determines if eform is
	 * already psr configured
	 * 
	 * @param protocolId
	 * @return
	 * @throws CtdbException
	 */
	public ArrayList<BasicEform> getEformsForAllVisitTypes(int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		PreparedStatement stmt2 = null;
		ResultSet rs2 = null;
		PreparedStatement stmt3 = null;
		ResultSet rs3 = null;
		// HashSet will avoid duplicates using the equals() in BasicEform
		HashSet<BasicEform> eformSet = new HashSet<BasicEform>();
		try {
			// Gets all Visit Types in the protocol
			String sql = "select intervalid from interval where protocolId = ?";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();
			BasicEform eform = null;
			while (rs.next()) {
				int intervalid = rs.getInt("intervalid");

				// Gets all eforms associated to the Visit Type
				String sql2 = "select distinct e.eformid,e.shortname shortName, e.name eformName , fi.selfreport, "
						+ "e.description edescription from  form_interval fi,eform e where fi.eformid = "
						+ "e.eformid and fi.intervalid = ? ";

				stmt2 = this.conn.prepareStatement(sql2);
				stmt2.setLong(1, intervalid);
				rs2 = stmt2.executeQuery();

				while (rs2.next()) {
					long eformid = Long.valueOf(rs2.getString("eformid"));
					String shortname = rs2.getString("shortName");
					String title = rs2.getString("eformName");
					String description = rs2.getString("edescription");
					boolean isConfigured = false;
					// Determines if eForm is already configured checking to see if there are any
					// entries in this table
					String sql3 = "select * from psr_hidden_elements where eformid = ?";

					stmt3 = this.conn.prepareStatement(sql3);
					stmt3.setLong(1, eformid);
					rs3 = stmt3.executeQuery();

					if (rs3.next()) {
						isConfigured = true;
					}

					eform = new BasicEform();
					eform.setId(eformid);
					eform.setShortName(shortname);
					eform.setTitle(title);
					eform.setDescription(description);
					eform.setIsConfigured(isConfigured);
					eformSet.add(eform);
				}
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve intervals.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
			this.close(rs2);
			this.close(stmt2);
			this.close(rs3);
			this.close(stmt3);
		}

		// now just return the list of values as the BasicEform has the eform id in the object
		ArrayList<BasicEform> eformList = new ArrayList<BasicEform>(eformSet);
		Collections.sort(eformList, new Comparator<BasicEform>() {
			public int compare(BasicEform e1, BasicEform e2) {
				return e1.getShortName().compareTo(e2.getShortName());
			}
		});
		return eformList;
	}

	/**
	 * Function to get all the save hidden elememnts for an eform/protocolid
	 * 
	 * @param protocolId
	 * @param eformId
	 * @return
	 * @throws CtdbException
	 */
	public ArrayList<String> getPSRHiddenElements(int protocolId, int eformId) throws CtdbException {
		ArrayList<String> psrHiddenElementsList = new ArrayList<String>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "select * from psr_hidden_elements where protocolId = ? and eformid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, protocolId);
			stmt.setLong(2, eformId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				int sectionId = rs.getInt("dict_sectionid");
				boolean isQuestion = rs.getBoolean("is_question");
				String elementIdString = "";
				int questionId = -1;

				if (isQuestion) {
					questionId = rs.getInt("dict_questionid");
					elementIdString = "S_" + sectionId + "_Q_" + questionId;
				} else {
					elementIdString = "S_" + sectionId;
				}

				psrHiddenElementsList.add(elementIdString);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve intervals: " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return psrHiddenElementsList;
	}

	/**
	 * Function that saves all the hidden elements for an eform/protocolid
	 * 
	 * @param protocolId
	 * @param eformId
	 * @param sectionQuestionIds
	 * @throws CtdbException
	 */
	public void updatePSRHiddenElements(int protocolId, int eformId, List<String> sectionQuestionIds, User user)
			throws CtdbException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> currentHiddenElements = new ArrayList<String>();

		try {
			StringBuffer sql = new StringBuffer(25);
			// get current snapshot of table for auditing purposes
			sql.append("select * FROM psr_hidden_elements where protocolid = ? and eformid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			stmt.setLong(2, eformId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				String currentElement = this.rsToCurrentElement(rs);
				currentHiddenElements.add(currentElement);
			}
			stmt.close();
			// delete from psr_hidden_elements
			sql = new StringBuffer(25);
			sql.append("delete FROM psr_hidden_elements where protocolid = ? and eformid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			stmt.setLong(2, eformId);
			stmt.execute();
			stmt.close();
			// insert into psr_hidden_elements
			sql = new StringBuffer();
			sql.append(" insert into psr_hidden_elements values (?, ?, ?, ?, ?, ?)");
			stmt = this.conn.prepareStatement(sql.toString());
			for (String elementIdString : sectionQuestionIds) {
				boolean isQuestion = false;
				String sectionIdString = "";
				String questionIdString = "";
				int questionId = -1;
				int sectionId = -1;
				if (elementIdString.contains("Q_")) {
					isQuestion = true;
					sectionIdString = elementIdString.substring(elementIdString.indexOf("S_") + 2,
							elementIdString.indexOf("_Q_"));
					sectionId = Integer.valueOf(sectionIdString).intValue();
					questionIdString =
							elementIdString.substring(elementIdString.indexOf("Q_") + 2, elementIdString.length());
					questionId = Integer.valueOf(questionIdString).intValue();
				} else {
					sectionIdString =
							elementIdString.substring(elementIdString.indexOf("S_") + 2, elementIdString.length());
					sectionId = Integer.valueOf(sectionIdString).intValue();
				}

				stmt.setLong(1, this.getNextSequenceValue(conn, "psr_hidden_elements_seq"));
				stmt.setLong(2, protocolId);
				stmt.setLong(3, eformId);
				stmt.setLong(4, sectionId);
				if (questionId == -1) {
					stmt.setNull(5, java.sql.Types.BIGINT);
				} else {
					stmt.setLong(5, questionId);
				}

				stmt.setBoolean(6, isQuestion);

				stmt.addBatch();

			}
			stmt.executeBatch();

			// now do audit
			this.auditPSRHiddenElements(protocolId, eformId, currentHiddenElements, sectionQuestionIds, user);

		} catch (SQLException e) {
			throw new CtdbException("Unable to update protocolDefaults: " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

	}

	public void auditPSRHiddenElements(int protocolId, int eformId, List<String> currentHiddenElements,
			List<String> sectionQuestionIds, User user) throws CtdbException {
		PreparedStatement stmt = null;
		List<String> hideElements = new ArrayList<String>();
		List<String> showElements = new ArrayList<String>();
		// if currentHiddenElements is empty, then everthing goes into hideElements
		if (currentHiddenElements.size() == 0) {
			for (int i = 0; i < sectionQuestionIds.size(); i++) {
				String sectionQuestionId = sectionQuestionIds.get(i);
				hideElements.add(sectionQuestionId);
			}
		} else {
			// Iterate over currentHiddenElements....then iterate over
			// sectionQuestionIds....if its not in sectionQuestionIds, then this goes into
			// showElements
			for (String currentHiddenElement : currentHiddenElements) {
				boolean isShow = true;
				for (String sectionQuestionId : sectionQuestionIds) {
					if (currentHiddenElement.equals(sectionQuestionId)) {
						isShow = false;
						break;
					}
				}
				if (isShow) {
					showElements.add(currentHiddenElement);
				}
			}

			// Iterate over sectionQuestionIds...then iterate over
			// currentHiddenElements...if its not in currentHiddenElements, then this goes
			// into hideElements
			for (String sectionQuestionId : sectionQuestionIds) {
				boolean isHide = true;
				for (String currentHiddenElement : currentHiddenElements) {
					if (sectionQuestionId.contentEquals(currentHiddenElement)) {
						isHide = false;
						break;
					}
				}
				if (isHide) {
					hideElements.add(sectionQuestionId);
				}
			}
		}

		try {
			// now save hideElements and showElements to psr_hidden_elements_audit
			StringBuffer sql = new StringBuffer(25);
			sql.append(" insert into psr_hidden_elements_audit values (DEFAULT, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)");
			stmt = this.conn.prepareStatement(sql.toString());
			for (String elementIdString : showElements) {
				stmt.setLong(1, protocolId);
				stmt.setLong(2, eformId);
				String sectionIdString = "";
				String questionIdString = "";
				int questionId = -1;
				int sectionId = -1;
				if (elementIdString.contains("Q_")) {
					sectionIdString = elementIdString.substring(elementIdString.indexOf("S_") + 2,
							elementIdString.indexOf("_Q_"));
					sectionId = Integer.valueOf(sectionIdString).intValue();
					questionIdString =
							elementIdString.substring(elementIdString.indexOf("Q_") + 2, elementIdString.length());
					questionId = Integer.valueOf(questionIdString).intValue();
				} else {
					sectionIdString =
							elementIdString.substring(elementIdString.indexOf("S_") + 2, elementIdString.length());
					sectionId = Integer.valueOf(sectionIdString).intValue();
				}
				stmt.setLong(3, sectionId);
				if (questionId == -1) {
					stmt.setNull(4, java.sql.Types.BIGINT);
				} else {
					stmt.setLong(4, questionId);
				}
				stmt.setString(5, "Show");
				stmt.setLong(6, user.getId());
				stmt.addBatch();
			}
			for (String elementIdString : hideElements) {
				stmt.setLong(1, protocolId);
				stmt.setLong(2, eformId);
				String sectionIdString = "";
				String questionIdString = "";
				int questionId = -1;
				int sectionId = -1;
				if (elementIdString.contains("Q_")) {
					sectionIdString = elementIdString.substring(elementIdString.indexOf("S_") + 2,
							elementIdString.indexOf("_Q_"));
					sectionId = Integer.valueOf(sectionIdString).intValue();
					questionIdString =
							elementIdString.substring(elementIdString.indexOf("Q_") + 2, elementIdString.length());
					questionId = Integer.valueOf(questionIdString).intValue();
				} else {
					sectionIdString =
							elementIdString.substring(elementIdString.indexOf("S_") + 2, elementIdString.length());
					sectionId = Integer.valueOf(sectionIdString).intValue();
				}
				stmt.setLong(3, sectionId);
				if (questionId == -1) {
					stmt.setNull(4, java.sql.Types.BIGINT);
				} else {
					stmt.setLong(4, questionId);
				}
				stmt.setString(5, "Hide");
				stmt.setLong(6, user.getId());
				stmt.addBatch();
			}
			stmt.executeBatch();
		} catch (SQLException e) {
			throw new CtdbException("Unable to update protocolDefaults: " + e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Gets the audit trail for Configuring Eform
	 * 
	 * @param protocolId
	 * @param eformId
	 * @return
	 * @throws CtdbException
	 */
	public List<ConfigureEformAuditDetail> getConfigureEformAudit(int protocolId, int eformId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			List<ConfigureEformAuditDetail> auditList = new ArrayList<ConfigureEformAuditDetail>();
			String sql =
					"select * from psr_hidden_elements_audit au, usr us where au.usrid = us.usrid and protocolid = ? and eformid = ? order by au.updateddate";
			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, protocolId);
			stmt.setInt(2, eformId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				ConfigureEformAuditDetail audit = new ConfigureEformAuditDetail();
				audit.setSectionId(rs.getInt("dict_sectionid"));
				if (rs.getInt("dict_questionid") != 0) {
					audit.setQuestionId(rs.getInt("dict_questionid"));
				}
				audit.setAction(rs.getString("action"));
				audit.setUpdatedBy(rs.getInt("usrid"));
				audit.setUpdatedDate(rs.getTimestamp("updateddate"));
				String lastName = rs.getString("lastname");
				String firstName = rs.getString("firstname");
				String username = lastName + ", " + firstName;
				audit.setUsername(username);
				auditList.add(audit);
			}

			return auditList;

		} catch (SQLException e) {
			throw new CtdbException("Unable to get configure eform audit " + e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public String rsToCurrentElement(ResultSet rs) throws SQLException {
		int sectionId = rs.getInt("dict_sectionid");
		boolean isQuestion = rs.getBoolean("is_question");
		String elementIdString = "";
		int questionId = -1;
		if (isQuestion) {
			questionId = rs.getInt("dict_questionid");
			elementIdString = "S_" + sectionId + "_Q_" + questionId;
		} else {
			elementIdString = "S_" + sectionId;
		}
		return elementIdString;
	}

	/**
	 * Retrieves all intervals in the system associated with a study
	 *
	 * @param protocolId The study ID.
	 * @return The list of intervals in the system. The list will be empty if no intervals exist in the system.
	 * @throws CtdbException if any errors occur while processing
	 */
	public List<Interval> getStudyIntervals(int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			List<Interval> intervals = new LinkedList<Interval>();

			StringBuffer sql = new StringBuffer(25);
			sql.append("select interval.*, xintervaltype.INTERVALTYPENAME from interval, xintervaltype ");
			sql.append(" where interval.XINTERVALTYPEID = xintervaltype.XINTERVALTYPEID and protocolId = ? ");
			sql.append(" order by interval.name ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);

			rs = stmt.executeQuery();

			Interval interval;
			while (rs.next()) {
				interval = this.rsToInterval(rs);
				interval.setIntervalType(rs.getInt("XINTERVALTYPEID"));
				interval.setIntervalTypeName(rs.getString("INTERVALTYPENAME"));
				interval.setCategory(rs.getString("CATEGORY"));
				interval.setIntervalFormList(getFormsForInterval(interval.getId()));
				intervals.add(interval);
			}
			return intervals;
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve intervals.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * gets the intervals for the study that are associated to the form
	 * 
	 * @param formid
	 * @return
	 * @throws CtdbException
	 */
	public List<Interval> getIntervalsForForm(long protocolId, long eformid) throws CtdbException {
		List<Interval> intervals = new ArrayList<Interval>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "select * from interval where protocolId = ? and intervalid in "
					+ "(select intervalid from form_interval where eformid = ? ) order by orderval";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, protocolId);
			stmt.setLong(2, eformid);

			rs = stmt.executeQuery();

			while (rs.next()) {
				intervals.add(this.rsToInterval(rs));
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve intervals.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return intervals;
	}

	public List<BasicEform> getEformsForInterval(int intervalId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			List<BasicEform> forms = new ArrayList<BasicEform>();

			String sql = "select distinct e.eformid,e.shortname shortName, e.name eformName ,fi.mandatory, "
					+ "fi.selfreport, e.description edescription, fi.intervalformorder from form_interval fi, "
					+ "eform e where fi.eformid = e.eformid and fi.intervalid = ? order by intervalformorder";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, intervalId);

			rs = stmt.executeQuery();

			while (rs.next()) {
				BasicEform form = new BasicEform();

				form.setId(Long.valueOf((rs.getString("eformid"))));
				form.setShortName(rs.getString("shortName"));
				form.setTitle(rs.getString("eformName"));
				form.setDescription(rs.getString("edescription"));
				form.setIsMandatory(
						rs.getString("mandatory") != null && rs.getString("mandatory").equalsIgnoreCase("true"));
				form.setIsSelfReport(
						rs.getString("selfreport") != null && rs.getString("selfreport").equalsIgnoreCase("true"));
				form.setOrderValue(rs.getInt("intervalformorder"));

				forms.add(form);
			}

			return forms;
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve eforms associated with intervals.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * gets the forms for the study that are associated to the visit type
	 * 
	 * @param intervalId - The visit type ID
	 * @return
	 * @throws CtdbException
	 */
	public List<Form> getFormsForInterval(int intervalId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			List<Form> forms = new ArrayList<Form>();

			String sql = "select f.name, fi.formid, fi.mandatory, fi.selfreport, f.description, "
					+ "fi.intervalformorder from form f, form_interval fi where fi.formid = f.formid "
					+ "and fi.intervalid = ? order by intervalformorder ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, intervalId);

			rs = stmt.executeQuery();

			while (rs.next()) {
				Form form = new Form();

				form.setId(rs.getInt("formid"));
				form.setName(rs.getString("name"));
				form.setMandatory(
						rs.getString("mandatory") != null && rs.getString("mandatory").equalsIgnoreCase("true"));
				form.setSelfReport(
						rs.getString("selfreport") != null && rs.getString("selfreport").equalsIgnoreCase("true"));
				form.setDescription(rs.getString("description"));
				form.setOrderValue(rs.getInt("intervalformorder"));

				forms.add(form);
			}

			return forms;
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve forms associated with intervals.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * gets the intervals for the protoocl that are associated to the form
	 * 
	 * @param formid
	 * @return
	 * @throws CtdbException
	 */
	public List<Form> getActiveFormsForInterval(int intervalId) throws CtdbException {
		List<Form> forms = new ArrayList<Form>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "select ef.name, fi.eformid, fi.mandatory, fi.selfreport, fi.intervalformorder "
					+ "from eform ef, form_interval fi where fi.eformid = ef.eformid and fi.intervalid = ? "
					+ "order by intervalformorder";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, intervalId);

			rs = stmt.executeQuery();

			while (rs.next()) {
				Form form = new Form();
				form.setId(rs.getInt("eformid"));
				form.setName(rs.getString("name"));
				form.setMandatory(
						rs.getString("mandatory") != null && rs.getString("mandatory").equalsIgnoreCase("true"));
				form.setSelfReport(
						rs.getString("selfreport") != null && rs.getString("selfreport").equalsIgnoreCase("true"));
				form.setOrderValue(rs.getInt("intervalformorder"));
				forms.add(form);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve forms associated with intervals.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return forms;
	}

	/**
	 * Retrieves all versions of a interval stored. A version consists of modifications to the metadata about a
	 * interval.
	 *
	 * @param intervalId The unique identifier of the Interval to retrieve
	 * @return A list of all versions for a single interval. The array will ordered by versions such that index 0 will
	 *         be the first interval version. If the interval does not exist an empty array will be returned.
	 * @throws CtdbException if any errors occur
	 */
	public List<Interval> getIntervalVersions(int intervalId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(25);
			sql.append("select interval_view.*, usr.username ");
			sql.append(" from interval_view, usr ");
			sql.append("where intervalid = ? and interval_view.updatedby = usr.usrid ");
			sql.append("order by interval_view.version desc ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, intervalId);

			rs = stmt.executeQuery();

			List<Interval> intervals = new ArrayList<Interval>();
			while (rs.next()) {
				Interval interval = this.rsToInterval(rs);
				interval.setUpdatedByUsername(rs.getString("username"));
				intervals.add(interval);
			}

			return intervals;
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve intervals.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Checks if the interval is administered. Uses ID and versionNumber of the domain object to check to see if it is
	 * administered. If the versionNumber in the domain object is not set (equals to Integer.MIN_VALUE), then the
	 * current version of the patient in the data base is used for the check.
	 *
	 * @param interval the interval object to check
	 * @return if the interval with the version has been administered
	 * @throws CtdbException thrown if any errors occur while processing
	 */
	public boolean isAdministered(Interval interval) throws CtdbException {
		// currently interval version is not in the administeredform table
		// so check the interval id only.
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("select count(administeredformid) from administeredform where intervalid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, interval.getId());
			rs = stmt.executeQuery();
			int num = 0;

			if (rs.next()) {
				num = rs.getInt(1);
			}
			return num > 0;
		} catch (SQLException e) {
			throw new CtdbException("Unable to check is administered for a interval with id " + interval.getId() + ".",
					e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * 
	 */
	public boolean doesVisitTypeHaveAnySelfReportingForms(int visitTypeId) throws CtdbException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("select selfreport from form_interval where intervalid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, visitTypeId);
			rs = stmt.executeQuery();
			boolean isSelfReport = false;
			while (rs.next()) {
				if (rs.getString("selfreport") != null && rs.getString("selfreport").equalsIgnoreCase("true")) {
					isSelfReport = true;
					break;
				}
			}
			return isSelfReport;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to check for self reporting forms for visit type with id " + visitTypeId + ".", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public ProtocolDefaults getProtocolDefaults(int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(25);
			sql.append(
					" with theNum as (select count(defaultItem) as ber from protocoldefaults where protocolid = ? ) ");
			sql.append(" select theNum.ber, pd.defaultItem from protocoldefaults pd, theNum where protocolid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			stmt.setLong(2, protocolId);

			rs = stmt.executeQuery();
			int[] selected = new int[0];

			if (rs.next()) {
				int howMnay = rs.getInt("ber");
				selected = new int[howMnay];
				do {
					selected[--howMnay] = rs.getInt("defaultitem");
				} while (rs.next());
			}
			ProtocolDefaults pd = new ProtocolDefaults();
			pd.setSelectedDefaults(selected);
			return pd;

		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve protocolDefaults.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public void updateProtocolDefaults(Protocol p) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(25);
			sql.append("delete FROM protocoldefaults where protocolid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, p.getId());
			stmt.execute();
			stmt.close();

			sql = new StringBuffer("insert into protocoldefaults values (?, ?, true) ");
			stmt = this.conn.prepareStatement(sql.toString());

			for (int i = 0; i < p.getProtocolDefaults().getSelectedDefaults().length; i++) {
				stmt.setLong(1, p.getId());
				stmt.setLong(2, p.getProtocolDefaults().getSelectedDefaults()[i]);
				stmt.addBatch();
			}

			stmt.executeBatch();
		} catch (SQLException e) {
			throw new CtdbException("Unable to update protocolDefaults.", e);
		} finally {
			this.close(stmt);
		}
	}

	public void updateBtrisAccess(Protocol p) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(25);
			sql.append("delete FROM protocol_btrisaccess where protocolid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, p.getId());
			stmt.execute();
			stmt.close();

			sql = new StringBuffer("insert into protocol_btrisaccess values (?, ?) ");
			stmt = this.conn.prepareStatement(sql.toString());

			for (int i = 0; p.getBtrisAccess() != null && i < p.getBtrisAccess().length; i++) {
				stmt.setLong(1, p.getId());
				stmt.setLong(2, p.getBtrisAccess()[i]);
				stmt.addBatch();

			}

			stmt.executeBatch();
		} catch (SQLException e) {
			throw new CtdbException("Unable to update btris access.", e);
		} finally {
			this.close(stmt);
		}
	}

	public int[] getBtrisAccess(int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(25);
			sql.append(
					" with theNum as (select count(accessid) as ber from protocol_btrisaccess where protocolid = ? ) ");
			sql.append(" select theNum.ber, pd.accessid from protocol_btrisaccess pd, theNum where protocolid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			stmt.setLong(2, protocolId);

			rs = stmt.executeQuery();
			int[] selected = new int[0];

			if (rs.next()) {
				int howMnay = rs.getInt("ber");
				selected = new int[howMnay];
				do {
					selected[--howMnay] = rs.getInt("accessid");
				} while (rs.next());
			}
			return selected;

		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve btris access.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Transforms a ResulSet object into a ProtocolLink object
	 *
	 * @param rs ResultSet to transform to ProtocolLink object
	 * @return ProtocolLink object
	 * @throws SQLException if any errors occur while retrieving data from result set
	 */
	private ProtocolLink rsToProtocolLink(ResultSet rs) throws SQLException {
		ProtocolLink protocolLink = new ProtocolLink();
		protocolLink.setId(rs.getInt("protocollinkid"));
		protocolLink.setProtocolId(rs.getInt("protocolid"));
		protocolLink.setName(rs.getString("name"));
		protocolLink.setDescription(rs.getString("description"));
		protocolLink.setAddress(rs.getString("address"));
		return protocolLink;
	}

	/**
	 * Transforms a ResultSet object into a Protocol object
	 *
	 * @param rs ResultSet to transform to Protocol object
	 * @return Protocol object
	 * @throws SQLException if any errors occur while retrieving data from result set
	 */
	private Protocol rsToProtocol(ResultSet rs) throws SQLException {
		Protocol protocol = new Protocol();

		protocol.setDataSubmissionFile(new Attachment());
		protocol.setId(rs.getInt("protocolid"));
		protocol.setVersion(new Version(rs.getInt("version")));
		protocol.setProtocolNumber(rs.getString("protocolnumber"));
		protocol.setName(rs.getString("name"));
		protocol.getDataSubmissionFile().setFileName(rs.getString("imagefilename"));
		protocol.setWelcomeUrl(rs.getString("welcomeurl"));
		protocol.setDescription(rs.getString("description"));

		protocol.setStatus(new CtdbLookup(rs.getInt("xprotocolstatusid"), rs.getString("xprotocolstatusname")));

		protocol.setOrginization(rs.getString("funding_organization"));
		protocol.setCreatedBy(rs.getInt("createdby"));
		protocol.setCreatedDate(new Date(rs.getTimestamp("createddate").getTime()));
		protocol.setUpdatedBy(rs.getInt("updatedby"));
		protocol.setUpdatedDate(new Date(rs.getTimestamp("updateddate").getTime()));
		protocol.setIsEvent(rs.getBoolean("isevent"));
		protocol.setUsePatientName(rs.getBoolean("usepatientid"));

		// Check if the "typename" column is null.
		if (rs.getString("typename") != null) {
			protocol.setStudyType(StudyType.getStudyTypeFromName(rs.getString("typename")));
		}

		protocol.setLockFormIntervals(rs.getString("lockformintervals").trim().equalsIgnoreCase("true"));
		protocol.setPatientDisplayType(rs.getInt("patientdisplaytype"));
		protocol.setAutoIncrementSubject(rs.getBoolean("subjectautoincrement"));
		protocol.setSubjectNumberPrefix(rs.getString("subjectnumberprefix"));
		protocol.setSubjectNumberSuffix(rs.getString("subjectnumbersuffix"));
		protocol.setSubjectNumberStart(rs.getInt("subjectnumberstart"));
		protocol.setAutoAssociatePatientRoles(rs.getBoolean("autoAssignPatientRoles"));
		protocol.setPrincipleInvestigator(rs.getString("principalinvestigator"));
		protocol.setAccountableInvestigator(rs.getString("accountableinvestigator"));
		protocol.setUseEbinder(Boolean.parseBoolean(rs.getString("useEbinder")));
		protocol.setStudyProject(rs.getString("studyproject"));
		protocol.setPrincipleInvestigatorId(rs.getInt("principalinvid"));

		if (rs.getString("brics_studyid") != null) {
			protocol.setBricsStudyId(rs.getString("brics_studyid"));
		}

		protocol.setEnableEsignature(rs.getBoolean("enableEsignature"));

		return protocol;
	}

	/**
	 * Transforms a ResultSet object into a ProtocolUser"
	 *
	 * @param rs ResultSet to transform to ProtocolUser
	 * @return ProtocolUser object
	 * @throws SQLException if any errors occur while retrieving data from result set
	 */
	private ProtocolUser rsToProtocolUser(ResultSet rs) throws SQLException {
		ProtocolUser protocolUser = new ProtocolUser();
		protocolUser.setId(rs.getInt("usrid"));
		protocolUser.setRoleId(rs.getInt("roleid"));
		protocolUser.setUsername(rs.getString("username"));
		protocolUser.setSysAdmin(rs.getBoolean("sysadminflag"));
		protocolUser.setCreatedBy(rs.getInt("createdby"));
		protocolUser.setCreatedDate(rs.getDate("createddate"));
		protocolUser.setUpdatedBy(rs.getInt("updatedby"));
		protocolUser.setUpdatedDate(rs.getDate("updateddate"));

		protocolUser.setDisplayName(rs.getString("lastname") + ", " + rs.getString("firstname"));
		Map<Integer, Integer> protocolSites = new HashMap<Integer, Integer>();
		if (rs.getString("siteId") != null) {
			protocolSites.put(new Integer(rs.getInt("protocolid")), new Integer(rs.getInt("siteId")));
		}
		protocolUser.setProtocolSiteMap(protocolSites);
		return protocolUser;
	}

	/**
	 * Transforms a ResultSet object into an Interval object
	 *
	 * @param rs ResultSet to transform to Interval object
	 * @return Interval object
	 * @throws SQLException if any errors occur while retrieving data from result set
	 * @throws CtdbException if any errors occur while retrieving data from result set
	 */
	private Interval rsToInterval(ResultSet rs) throws SQLException, CtdbException {
		Interval interval = new Interval();
		interval.setVersion(new Version(rs.getInt("version")));
		interval.setId(rs.getInt("intervalid"));
		interval.setProtocolId(rs.getInt("protocolid"));
		interval.setName(rs.getString("name"));
		interval.setDescription(rs.getString("description"));
		interval.setIntervalType(rs.getInt("xintervaltypeid"));
		interval.setCategory(rs.getString("category"));
		interval.setIntervalEFormList(getEformsForInterval(interval.getId()));
		interval.setCreatedBy(rs.getInt("createdby"));
		interval.setCreatedDate(new Date(rs.getTimestamp("createddate").getTime()));
		interval.setUpdatedBy(rs.getInt("updatedby"));
		interval.setUpdatedDate(new Date(rs.getTimestamp("updateddate").getTime()));
		interval.setSelfReportStart(rs.getInt("selfreportstart"));
		interval.setSelfReportEnd(rs.getInt("selfreportend"));

		return interval;
	}

	/**
	 * Constructs image file name
	 *
	 * @param protocolId The protocolId
	 * @param name The current file name
	 * @return The new file name
	 *
	 */
	private String getImageFileName(int protocolId, String name) throws CtdbException {
		try {
			String nameHead = "protocol_" + protocolId + ".";
			String fileType;
			int lastIndex = name.lastIndexOf('.');
			if (lastIndex != -1) {
				// chanage file type to lower case. Image file end with .GIF or .JPG is not
				// displayed
				// in the IE browser. Image files from MAC often end with capitalized file type.

				fileType = name.substring(lastIndex + 1, name.length()).toLowerCase();
			} else {
				fileType = "";
			}
			return nameHead + fileType;
		} catch (IndexOutOfBoundsException e) {
			throw new CtdbException("Problem with uploaded file name: " + name + ".", e);
		}
	}


	public void createVistTypeEFormAssociation(long visitTypeId, BasicEform ef) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql =
					"insert into form_interval (eformid, intervalid, mandatory, intervalformorder, selfreport) values (?, ?, ?, ?, ?) ";
			stmt = conn.prepareStatement(sql);

			stmt.setLong(1, ef.getId());
			stmt.setLong(2, visitTypeId);
			stmt.setString(3, Boolean.toString(ef.getIsMandatory()));
			stmt.setInt(4, ef.getOrderValue());
			stmt.setString(5, Boolean.toString(ef.getIsSelfReport()));
			stmt.executeUpdate();

		} catch (SQLException e) {
			throw new CtdbException("Unable to associate new eform to all visit types.", e);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Method to associate visit type with eFrom
	 * 
	 * @param visitTypeId
	 * @param eformList
	 * @throws CtdbException
	 */
	public void createVistTypeEFormAssociations(long visitTypeId, BasicEform ef) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "insert into form_interval (eformid, intervalid, mandatory, intervalformorder, "
					+ "selfreport) values (?, ?, ?, ?, ?) ";
			stmt = conn.prepareStatement(sql);

			stmt.setLong(1, ef.getId());
			stmt.setLong(2, visitTypeId);
			stmt.setString(3, Boolean.toString(ef.getIsMandatory()));
			stmt.setInt(4, ef.getOrderValue());
			stmt.setString(5, Boolean.toString(ef.getIsSelfReport()));

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to associate new eform to all visit types.", e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Creates the form associations for the given visit type.
	 * 
	 * @param visitTypeId - The ID of the visit type to associate the forms to
	 * @param formList - A list of form objects to associate to the visit type
	 * @throws CtdbException If an error occurred while creating the associations.
	 */
	public void createVistTypeFormAssociations(long visitTypeId, List<Form> formList) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql =
					"insert into form_interval (formid, intervalid, mandatory, intervalformorder, selfreport) values (?, ?, ?, ?, ?) ";
			stmt = conn.prepareStatement(sql);

			for (Form f : formList) {
				stmt.setLong(1, f.getId());
				stmt.setLong(2, visitTypeId);
				stmt.setString(3, Boolean.toString(f.isMandatory()));
				stmt.setInt(4, f.getOrderValue());
				stmt.setString(5, Boolean.toString(f.isSelfReport()));
				stmt.addBatch();
			}

			stmt.executeBatch();
		} catch (SQLException e) {
			throw new CtdbException("Unable to associate new form to all visit types.", e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Updates the form associations of the given visit type
	 * 
	 * @param visitTypeId - The ID of the visit type to update the associations
	 * @param formList - A list of form objects associated to the visit type to update
	 * @throws CtdbException If an error occurred while updating the associations.
	 */
	public void updateVistTypeEFormAssociations(long visitTypeId, BasicEform f) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "update form_interval set mandatory = ?, intervalformorder = ?, "
					+ "selfreport = ? where eformid = ? and intervalid = ? ";
			stmt = conn.prepareStatement(sql);

			stmt.setString(1, Boolean.toString(f.getIsMandatory()));
			stmt.setInt(2, f.getOrderValue());
			stmt.setString(3, Boolean.toString(f.getIsSelfReport()));
			stmt.setLong(4, f.getId());
			stmt.setLong(5, visitTypeId);

			stmt.executeUpdate();
		} catch (SQLException sqle) {
			throw new CtdbException("Could not update the form association for the visit type: " + visitTypeId, sqle);
		} finally {
			close(stmt);
		}
	}

	/**
	 * Deletes the form associations of the given visit type
	 * 
	 * @param visitTypeId - The ID of the visit type to delete the associations
	 * @param formList - A list of form objects that need to be de-associated
	 * @throws CtdbException If an error occurred while deleting the associations.
	 */
	public void deleteVisitTypeEFormAssociations(long visitTypeId, List<BasicEform> formList, int studyId)
			throws CtdbException {
		PreparedStatement stmt = null;
		PreparedStatement delFormIntervalStmt = null;
		PreparedStatement queryFormIntervalStmt = null;
		PreparedStatement delEformStmt = null;
		PreparedStatement delHiddenPsrElmsStmt = null;
		PreparedStatement delHiddenPsrElmsStmtAudit = null;
		ResultSet rs = null;

		try {
			// First find the number of collections that are associated with this visit type.
			int numCollections = 0;
			String sql = "select count(administeredformid) from administeredform where intervalid = ? ";
			stmt = conn.prepareStatement(sql);

			stmt.setLong(1, visitTypeId);
			rs = stmt.executeQuery();

			if (rs.next()) {
				numCollections = rs.getInt(1);
			}

			rs.close();
			stmt.close();

			// Setup the statements for the visit type eForm deletions.
			sql = "delete from form_interval where intervalid = ? and eformid = ? ";
			delFormIntervalStmt = conn.prepareStatement(sql);

			sql = "select count(*) from form_interval where eformid = ? ";
			queryFormIntervalStmt = conn.prepareStatement(sql);

			sql = "delete from eform where eformid = ? ";
			delEformStmt = conn.prepareStatement(sql);

			sql = "delete from psr_hidden_elements where protocolid = ? and eformid = ? ";
			delHiddenPsrElmsStmt = conn.prepareStatement(sql);

			sql = "delete from psr_hidden_elements_audit where protocolid = ? and eformid = ?";
			delHiddenPsrElmsStmtAudit = conn.prepareStatement(sql);

			for (BasicEform f : formList) {
				// Delete the visit type eForm linkage from the "form_interval" table.
				delFormIntervalStmt.setLong(1, visitTypeId);
				delFormIntervalStmt.setLong(2, f.getId());
				delFormIntervalStmt.executeUpdate();

				// Check if there are another other visit types associated with this eForm.
				int numOtherIntervals = 0;

				queryFormIntervalStmt.setLong(1, f.getId());
				rs = queryFormIntervalStmt.executeQuery();

				if (rs.next()) {
					numOtherIntervals = rs.getInt(1);
				}

				rs.close();

				// Check if it is OK to delete this eFrom.
				if ((numOtherIntervals == 0) && (numCollections == 0)) {
					// Delete PSR hidden element associations for the eForm.
					delHiddenPsrElmsStmt.setLong(1, studyId);
					delHiddenPsrElmsStmt.setLong(2, f.getId());
					delHiddenPsrElmsStmt.executeUpdate();

					// Delete from PSR hidden element associations Audit
					delHiddenPsrElmsStmtAudit.setLong(1, studyId);
					delHiddenPsrElmsStmtAudit.setLong(2, f.getId());
					delHiddenPsrElmsStmtAudit.executeUpdate();

					// Delete the eForm.
					delEformStmt.setLong(1, f.getId());
					delEformStmt.executeUpdate();
				}
			}
		} catch (SQLException sqle) {
			throw new CtdbException("Could not delete the eform assoication with the visit type (" + visitTypeId + ").",
					sqle);
		} finally {
			close(rs);
			close(stmt);
			close(delFormIntervalStmt);
			close(queryFormIntervalStmt);
			close(delEformStmt);
			close(delHiddenPsrElmsStmt);
			close(delHiddenPsrElmsStmtAudit);
		}
	}

	public void deleteFormAssociations(int intervalId) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "delete from form_interval where INTERVALID = ? ";

			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, intervalId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to delete assocaited forms for interval with ID " + intervalId + ".", e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Deletes a visit type from the database that is not associated with any data collections. If the visit type is
	 * associated to any data collections, an exception will be thrown.
	 * 
	 * @param visitType - The visit type to be deleted
	 * @throws CtdbException If a database error occurred during the deletion
	 * @throws InvalidRemovalException If the visit type is associated with a data collection.
	 */
	public void deleteVisitType(Interval visitType) throws CtdbException {
		PreparedStatement stmt = null;
		PreparedStatement stmt1 = null;
		PreparedStatement stmt2 = null;
		PreparedStatement stmt3 = null;
		ResultSet rs = null;

		try {
			// Check if the interval is associated with a data collection
			String sql = "select administeredformid from administeredform where intervalid = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, visitType.getId());
			rs = stmt.executeQuery();

			if (rs.next()) {
				throw new InvalidRemovalException("Cannot delete the \"" + visitType.getName()
						+ "\" visit type because it is associated with a data collection ("
						+ rs.getString("administeredformid") + ")");
			}

			rs.close();
			stmt.close();

			sql = "delete from interval_clinicalpoint where intervalid = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, visitType.getId());
			stmt.executeUpdate();
			stmt.close();

			sql = "delete from prepopdataelement_interval where intervalid = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, visitType.getId());
			stmt.executeUpdate();
			stmt.close();

			sql = "delete from form_interval where intervalid = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, visitType.getId());
			stmt.executeUpdate();
			stmt.close();

			sql = "delete from intervalarchive where intervalid = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, visitType.getId());
			stmt.executeUpdate();
			stmt.close();

			sql = "delete from interval where intervalid = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, visitType.getId());
			stmt.executeUpdate();
			stmt.close();

			// Setup the prepared statements for deleting eForms that are not linked to any visit types.
			sql = "select * from form_interval where eformid = ? ";
			stmt = this.conn.prepareStatement(sql);

			sql = "delete from eform where eformid = ? ";
			stmt1 = this.conn.prepareStatement(sql);

			sql = "delete from psr_hidden_elements where protocolid = ? and eformid = ? ";
			stmt2 = this.conn.prepareStatement(sql);

			sql = "delete from psr_hidden_elements_audit where protocolid = ? and eformid = ?";
			stmt3 = conn.prepareStatement(sql);

			// First check if eForm exist in other protocols.
			for (BasicEform f : visitType.getIntervalEFormList()) {
				// Get all form_interval records for this eForm.
				stmt.setLong(1, f.getId());
				rs = stmt.executeQuery();

				// If there are no linked visit types, batch the eForm deletion.
				if (!rs.next()) {
					// Delete the linked PSR hidden elements.
					stmt2.setLong(1, visitType.getProtocolId());
					stmt2.setLong(2, f.getId());
					stmt2.executeUpdate();

					// Delete from PSR hidden elements audit.
					stmt3.setLong(1, visitType.getProtocolId());
					stmt3.setLong(2, f.getId());
					stmt3.executeUpdate();

					// Delete the eForm.
					stmt1.setLong(1, f.getId());
					stmt1.executeUpdate();
				}

				rs.close();
			}
		} catch (SQLException sqle) {
			throw new CtdbException("Failure deleting visit type.", sqle);
		} finally {
			close(rs);
			close(stmt);
			close(stmt1);
			close(stmt2);
			close(stmt3);
		}
	}

	public List<String> getPrepopDENamesForInterval(int intervalId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> prepopDEs = new ArrayList<String>();

		try {
			String sql = "select * from prepopdataelement_interval pdei "
					+ " inner join prepopdataelement pde on pdei.prepopdataelementid = pde.prepopdataelementid "
					+ " where pdei.intervalid = ? order by pde.elementshortname ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, intervalId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				prepopDEs.add(rs.getString("elementshortname"));
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve intervals.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return prepopDEs;
	}

	public List<String> getAllPrepopDENames() throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> prepopDEs = new ArrayList<String>();

		try {
			String sql = "select * from prepopdataelement";

			stmt = this.conn.prepareStatement(sql);
			rs = stmt.executeQuery();

			while (rs.next()) {
				prepopDEs.add(rs.getString("elementshortname"));

			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve intervals.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return prepopDEs;
	}

	public void createVistTypePrepopDEAssociation(long visitTypeId, String prepopDEShortName) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			long prepopDEId = -1L;
			String sql = "select prepopdataelementid from prepopdataelement where elementshortname = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, prepopDEShortName);
			rs = stmt.executeQuery();

			if (rs.next()) {
				prepopDEId = rs.getLong("prepopdataelementid");
			}

			rs.close();
			stmt.close();

			sql = "insert into prepopdataelement_interval (prepopdataelementid, intervalid) values (?, ?) ";
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, prepopDEId);
			stmt.setLong(2, visitTypeId);
			stmt.executeUpdate();

		} catch (SQLException e) {
			throw new CtdbException("Unable to associate new eform to all visit types.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Checks the database for a PrepopDataElement, which corresponds to the given data element short name.
	 * 
	 * @param deShortName - The data element short name to query for.
	 * @return The PrepopDataElement object that is associated with the given data element short name, or null if no
	 *         correlation exists.
	 * @throws CtdbException When there is a database error while the pre-population data element lookup is in progress.
	 */
	public PrepopDataElement getPrepopDEByShortName(String deShortName) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		PrepopDataElement prepopDE = null;

		try {
			String sql = "select * from prepopdataelement where elementshortname = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, deShortName);
			rs = stmt.executeQuery();

			if (rs.next()) {
				prepopDE = rsToPrepopDataElement(rs);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve prepop data element.", e);
		} finally {
			this.close(stmt);
			this.close(rs);
		}

		return prepopDE;
	}

	/**
	 * Queries the database for pre-population data element objects that corresponds to the given list of data element
	 * short names.
	 * 
	 * @param deShortNameList - A list of data element short names used to find data elements that support
	 *        pre-population values.
	 * @return A list of PrepopDataElement which are associated with the given list of data element short names.
	 * @throws CtdbException When there is a database error while querying for PrepopDataElement objects.
	 */
	public List<PrepopDataElement> getPrepopDEByShortNameList(List<String> deShortNameList) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<PrepopDataElement> prePopDeList = new ArrayList<PrepopDataElement>();

		try {
			// Construct the SQL query.
			StringBuffer sql = new StringBuffer(100);

			sql.append("select * from prepopdataelement where elementshortname in (");

			//// Add in all of the place holders for th pre-pop DE names.
			for (Iterator<String> it = deShortNameList.iterator(); it.hasNext();) {
				it.next();
				sql.append("?");

				if (it.hasNext()) {
					sql.append(", ");
				}
			}

			sql.append(") ");

			// Construct the prepared statement.
			stmt = conn.prepareStatement(sql.toString());
			int counter = 1;

			for (Iterator<String> it = deShortNameList.iterator(); it.hasNext();) {
				stmt.setString(counter, it.next());
				counter++;
			}

			rs = stmt.executeQuery();

			// Create the PrepopDataElement objects from the result set.
			while (rs.next()) {
				PrepopDataElement prePopDe = rsToPrepopDataElement(rs);
				prePopDeList.add(prePopDe);
			}
		} catch (SQLException sqle) {
			throw new CtdbException("Unable to retrieve prepop data elements.", sqle);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return prePopDeList;
	}

	public void deleteVistTypePrepopDEAssociation(long visitTypeId, String prepopDEShortName) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			long prepopDEId = -1L;
			String sql = "select prepopdataelementid from prepopdataelement where elementshortname = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, prepopDEShortName);
			rs = stmt.executeQuery();

			if (rs.next()) {
				prepopDEId = rs.getLong("prepopdataelementid");
			}

			rs.close();
			stmt.close();

			sql = "delete from prepopdataelement_interval where prepopdataelementid = ? and intervalid = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, prepopDEId);
			stmt.setLong(2, visitTypeId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to associate new eform to all visit types.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public void savePrepopDEValuesForInterval(int intervalId, List<PatientVisitPrepopValue> pvPrepopValueList)
			throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "update prepopdataelement_interval set prepopvalue = ? "
					+ "where prepopdataelementid = ? and intervalid = ? ";

			stmt = conn.prepareStatement(sql);

			for (PatientVisitPrepopValue pvPrepopValue : pvPrepopValueList) {
				stmt.setString(1, pvPrepopValue.getPrepopvalue());
				stmt.setLong(2, pvPrepopValue.getPrepopDataElementId());
				stmt.setLong(3, intervalId);
				stmt.addBatch();
			}

			stmt.executeBatch();
		} catch (SQLException e) {
			throw new CtdbException("Unable to associate new eform to all visit types.", e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Gets a list of PrepopDataElement objects from the database for the specified visit type (interval) ID. The list
	 * of objects returned from the database can be conditionally filtered by the second boolean argument. A value of
	 * "true" will cause this method to return <u>all</u> PrepopDataElement objects that the system has for the given
	 * visit type ID. A value of "false" will only return PrepopDataElement objects that allow a user to define its
	 * pre-population value (i.e. Age in Years, etc.). The returned list will also be ordered by the data element title
	 * (the "elementtitle" column of the "prepopdataelement" table).
	 * 
	 * @param intervalId - The interval or visit type ID used to restrict the query for PrepopDataElement objects.
	 * @param completeList - A boolean flag used to indicate whether or not to return all available PrepopDataElement
	 *        objects for the given visit type ID.
	 * @return All PrepopDataElement objects found in the system for the given visit type ID. The listed items may have
	 *         been filtered according to the supplied boolean flag.
	 * @throws CtdbException If there was a database error while getting the list of PrepopDataElement objects.
	 */
	public List<PrepopDataElement> getPrepopDEsForInterval(int intervalId, boolean completeList) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<PrepopDataElement> prepopDEList = new ArrayList<PrepopDataElement>();

		try {
			StringBuffer sql = new StringBuffer(100);

			sql.append("select * from prepopdataelement pde inner join prepopdataelement_interval pdei on ")
					.append("pdei.prepopdataelementid = pde.prepopdataelementid where pdei.intervalid = ? ");

			// Check if the results need to filter out the system pre-populated data
			// elements.
			if (!completeList) {
				sql.append("and pdei.prepopdataelementid not in (1, 2, 3, 4) ");
			}

			// Add order by title
			sql.append("order by pde.elementtitle ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, intervalId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				prepopDEList.add(rsToPrepopDataElement(rs));
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve intervals.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return prepopDEList;
	}

	/**
	 * Gets a list of available pre-population data elements in the system. If "true" is given for the parameter, then
	 * the list will only contain pre-population data elements whose pre-population value can be defined by a user.
	 * Otherwise, all pre-population data elements in the system will be returned.
	 * 
	 * @param userDefOnly - Used to indicate whether or not to restrict the listed PrepopDataElement object to only ones
	 *        that can be defined by the user.
	 * @return A list of PrepopDataElement objects that are found in the system, and is optionally filtered by the
	 *         passed in boolean flag.
	 * @throws CtdbException When there is a database error while retrieving the PrepopDataElement objects.
	 */
	public List<PrepopDataElement> getAllPrepopDEs(boolean userDefOnly) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<PrepopDataElement> prepopDEList = new ArrayList<PrepopDataElement>();

		try {
			StringBuffer sql = new StringBuffer("select * from prepopdataelement ");

			// Check if only the user definable pre-population data elements should be
			// returned.
			if (userDefOnly) {
				sql.append("where prepopdataelementid not in (1, 2, 3, 4) ");
			}

			stmt = this.conn.prepareStatement(sql.toString());
			rs = stmt.executeQuery();

			while (rs.next()) {
				prepopDEList.add(rsToPrepopDataElement(rs));
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve intervals.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return prepopDEList;
	}

	public List<PatientVisitPrepopValue> getPvPrepopValuesForInterval(int intervalId, int visitdateId)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<PatientVisitPrepopValue> pvPrepopValueList = new ArrayList<PatientVisitPrepopValue>();

		try {
			String sql = "select pdei.intervalid, pde.*, pvpv.id as patientvisit_prepopvalueid, pvpv.* "
					+ "from prepopdataelement_interval pdei inner join prepopdataelement pde on "
					+ "pdei.prepopdataelementid = pde.prepopdataelementid inner join "
					+ "patientvisit_prepopvalue pvpv on pvpv.prepopdataelement_interval_id = pdei.id "
					+ "where pdei.intervalid = ? and pvpv.patientvisitid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, intervalId);
			stmt.setLong(2, visitdateId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				PatientVisitPrepopValue pvPrepopValue = rsToPatientVisitPrepopValue(rs);
				PrepopDataElement prepopDE = rsToPrepopDataElement(rs);

				pvPrepopValue.setPrepopDataElement(prepopDE);
				pvPrepopValueList.add(pvPrepopValue);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve intervals.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return pvPrepopValueList;
	}

	private PatientVisitPrepopValue rsToPatientVisitPrepopValue(ResultSet rs) throws SQLException {
		PatientVisitPrepopValue pvPrepopValue = new PatientVisitPrepopValue();

		pvPrepopValue.setId(rs.getInt("patientvisit_prepopvalueid"));
		pvPrepopValue.setPrepopDataElementId(rs.getLong("prepopdataelementid"));
		pvPrepopValue.setIntervalId(rs.getLong("intervalid"));
		pvPrepopValue.setPrepopvalue(rs.getString("prepopvalue"));
		pvPrepopValue.setPatientVisitId(rs.getLong("patientvisitid"));
		pvPrepopValue.setPrepopDEIntervalId(rs.getLong("prepopdataelement_interval_id"));

		return pvPrepopValue;
	}

	private PrepopDataElement rsToPrepopDataElement(ResultSet rs) throws SQLException {
		PrepopDataElement prepopDE = new PrepopDataElement();

		prepopDE.setId(rs.getInt("prepopdataelementid"));
		prepopDE.setShortName(rs.getString("elementshortname"));
		prepopDE.setTitle(rs.getString("elementtitle"));
		prepopDE.setValueType(rs.getString("valuetype"));

		return prepopDE;
	}

	public List<ClinicalLocation> getProtocolClinicalLocs(int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<ClinicalLocation> protocolSiteIdList = new ArrayList<ClinicalLocation>();
		AddressDao addressDao = AddressDao.getInstance(conn);

		try {
			String sql = "select * from clinicallocation where protocolid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				ClinicalLocation clinicalLoc = new ClinicalLocation();

				clinicalLoc.setId(rs.getInt("clinicallocationid"));
				clinicalLoc.setName(rs.getString("clinicallocationname"));

				Address address = addressDao.getAddress(rs.getInt("addressid"));
				clinicalLoc.setAddress(address);
				protocolSiteIdList.add(clinicalLoc);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve clinical locations for protocol: " + protocolId, e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return protocolSiteIdList;
	}

	public List<Procedure> getProtocolProcedures(int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Procedure> protocolProcedureList = new ArrayList<Procedure>();

		try {
			String sql = "select proc.* from procedure proc inner join protocol_procedure protproc "
					+ " on proc.procedureid = protproc.procedureid" + " where protproc.protocolid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Procedure procedure = new Procedure();
				procedure.setId(rs.getInt("procedureid"));
				procedure.setName(rs.getString("name"));
				ProcedureType procedureType = ProcedureType.getById(rs.getLong("proceduretypeid"));
				procedure.setProcedureType(procedureType);
				protocolProcedureList.add(procedure);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve procedures for protocol: " + protocolId, e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return protocolProcedureList;
	}

	public List<PointOfContact> getProtocolPointOfContacts(int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		AddressDao addressDao = AddressDao.getInstance(conn);
		List<PointOfContact> protocolPOCList = new ArrayList<PointOfContact>();

		try {
			String sql = "select * from pointofcontact poc where poc.protocolid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				PointOfContact poc = new PointOfContact();
				poc.setId(rs.getInt("pointofcontactid"));
				poc.setFirstName(rs.getString("firstname"));
				poc.setMiddleName(rs.getString("middlename"));
				poc.setLastName(rs.getString("lastname"));
				poc.setEmail(rs.getString("email"));
				poc.setPhone(rs.getString("phone"));
				poc.setPosition(rs.getString("position"));

				Address address = addressDao.getAddress(rs.getInt("addressid"));
				poc.setAddress(address);
				protocolPOCList.add(poc);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve point of contancts for protocol: " + protocolId, e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return protocolPOCList;
	}

	public ClinicalLocation getClinicalLocationById(int clinicLocId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		AddressDao addressDao = AddressDao.getInstance(conn);
		ClinicalLocation clinicLoc = new ClinicalLocation();

		try {
			String sql = "select * from clinicallocation where clinicallocationid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, clinicLocId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				clinicLoc.setId(clinicLocId);
				clinicLoc.setName(rs.getString("clinicallocationname"));

				Address address = addressDao.getAddress(rs.getInt("addressid"));
				clinicLoc.setAddress(address);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve clinical location: " + clinicLocId, e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return clinicLoc;
	}

	public Procedure getProcedureById(int procId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Procedure proc = new Procedure();

		try {
			String sql = "select * from procedure where procedureid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, procId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				proc.setId(procId);
				proc.setName(rs.getString("name"));

				ProcedureType procedureType = ProcedureType.getById(rs.getLong("proceduretypeid"));
				proc.setProcedureType(procedureType);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve procedure: " + procId, e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return proc;
	}

	public PointOfContact getPointOfContactById(int pocId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		AddressDao addressDao = AddressDao.getInstance(conn);
		PointOfContact poc = new PointOfContact();

		try {
			String sql = "select * from pointofcontact poc where pointofcontactid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, pocId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				poc.setId(pocId);
				poc.setFirstName(rs.getString("firstname"));
				poc.setMiddleName(rs.getString("middlename"));
				poc.setLastName(rs.getString("lastname"));
				poc.setEmail(rs.getString("email"));
				poc.setPhone(rs.getString("phone"));
				poc.setPosition(rs.getString("position"));

				Address address = addressDao.getAddress(rs.getInt("addressid"));
				poc.setAddress(address);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve point of contact: " + pocId, e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return poc;
	}

	public void createVisitTypeClinicalPointAsso(List<IntervalClinicalPoint> intervalCPList, int visitTypeId)
			throws CtdbException {
		logger.info("ProtocolManagerDao: createVisitTypeClinicalPointAsso ");

		PreparedStatement stmt = null;

		try {
			String sql = "insert into interval_clinicalpoint (id, intervalid, clinicallocationid, "
					+ "procedureid, pointofcontactid) values (DEFAULT, ?, ?, ?, ? ) ";

			stmt = conn.prepareStatement(sql);
			int i = 0;

			for (IntervalClinicalPoint intervalCP : intervalCPList) {
				stmt.setInt(1, visitTypeId);
				stmt.setInt(2, intervalCP.getClinicalLoc().getId());
				stmt.setInt(3, intervalCP.getProcedure().getId());
				stmt.setInt(4, intervalCP.getPointOfContact().getId());
				stmt.addBatch();
				i++;

				if ((i % 1000 == 0) || (i == intervalCPList.size())) {
					stmt.executeBatch(); // Execute every 1000 items.
				}
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to associate new clinical points to the visit type.", e);
		} finally {
			this.close(stmt);
		}
	}

	public void updateIntervalClinicalPointAsso(IntervalClinicalPoint intervalCP, int visitTypeId)
			throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "update interval_clinicalpoint set clinicallocationid = ?, procedureid = ?, "
					+ "pointofcontactid = ? where id = ? and intervalid = ? ";

			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, intervalCP.getClinicalLoc().getId());
			stmt.setInt(2, intervalCP.getProcedure().getId());
			stmt.setInt(3, intervalCP.getPointOfContact().getId());
			stmt.setInt(4, intervalCP.getId());
			stmt.setInt(5, visitTypeId);

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to update the associates between interval and clinical points.", e);
		} finally {
			this.close(stmt);
		}
	}

	public void deleteVisitTypeClinicalPointAsso(List<Integer> intervalCPIdToKeepList, int visitTypeId)
			throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer("delete from interval_clinicalpoint where intervalid = ? ");

			if (!intervalCPIdToKeepList.isEmpty()) {
				String idListToKeepStr = StringUtils.join(intervalCPIdToKeepList, ", ");
				sql.append("and id not in ( " + idListToKeepStr + " ) ");
			}

			stmt = conn.prepareStatement(sql.toString());
			stmt.setLong(1, visitTypeId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to delete the associates between interval and clinical points.", e);
		} finally {
			this.close(stmt);
		}
	}

	public List<IntervalClinicalPoint> getIntervalClinicalPntsForInterval(int intervalId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<IntervalClinicalPoint> intervalSitePntList = new ArrayList<IntervalClinicalPoint>();

		try {
			String sql = "select * from interval_clinicalpoint where intervalid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, intervalId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				int id = rs.getInt("id");
				int clinicallocationid = rs.getInt("clinicallocationid");
				int procId = rs.getInt("procedureid");
				int pocId = rs.getInt("pointofcontactid");
				ClinicalLocation clinicalLoc = getClinicalLocationById(clinicallocationid);
				Procedure proc = getProcedureById(procId);
				PointOfContact poc = getPointOfContactById(pocId);
				IntervalClinicalPoint intervalSitePnt = new IntervalClinicalPoint(clinicalLoc, proc, poc);

				intervalSitePnt.setId(id);
				intervalSitePntList.add(intervalSitePnt);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve clinical point associations for interval: " + intervalId, e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return intervalSitePntList;
	}

	public List<Procedure> getAllProcedureList() throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Procedure> procedureList = new ArrayList<Procedure>();

		try {
			String sql = "select * from procedure ";
			stmt = this.conn.prepareStatement(sql);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Procedure procedure = new Procedure();

				procedure.setId(rs.getInt("procedureid"));
				procedure.setName(rs.getString("name"));
				ProcedureType procedureType = ProcedureType.getById(rs.getLong("proceduretypeid"));
				procedure.setProcedureType(procedureType);
				procedureList.add(procedure);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve all procedures.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return procedureList;
	}

	public void createProtocolClinicalLocationAsso(int protocolId, List<ClinicalLocation> protoClinicalLocationList)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		AddressDao addressDao = AddressDao.getInstance(conn);

		try {
			String sql = "insert into clinicallocation (clinicalLocationid, protocolid, clinicallocationname, "
					+ "addressid) values (DEFAULT, ?, ?, ? )";

			stmt = conn.prepareStatement(sql);
			int i = 0;

			for (ClinicalLocation clinicalLocation : protoClinicalLocationList) {
				Integer addressId = addressDao.createAddress(clinicalLocation.getAddress());
				String name = clinicalLocation.getName() != null ? clinicalLocation.getName() : null;
				stmt.setInt(1, protocolId);
				stmt.setString(2, name);
				stmt.setInt(3, addressId);
				stmt.addBatch();
				i++;

				if (i % 1000 == 0 || i == protoClinicalLocationList.size()) {
					stmt.executeBatch(); // Execute every 1000 items.
				}
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to associate new clinical points to the visit type.", e);
		} finally {
			this.close(stmt);
			this.close(rs);
		}
	}

	public void createProtocolProcedureAsso(int protocolId, List<Procedure> protoProcedureList) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "insert into protocol_procedure (id, protocolid, procedureid) values (DEFAULT, ?, ? ) ";

			stmt = conn.prepareStatement(sql);
			int i = 0;

			for (Procedure procedure : protoProcedureList) {
				stmt.setInt(1, protocolId);
				stmt.setInt(2, procedure.getId());
				stmt.addBatch();
				i++;

				if (i % 1000 == 0 || i == protoProcedureList.size()) {
					stmt.executeBatch(); // Execute every 1000 items.
				}
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to associate new procedure to the protocol: " + protocolId, e);
		} finally {
			this.close(stmt);
			this.close(rs);
		}
	}

	public void createProtocolPointOfContactAsso(int protocolId, List<PointOfContact> protoPOCList)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		AddressDao addressDao = AddressDao.getInstance(conn);

		try {
			String sql = "insert into pointofcontact (pointofcontactid, protocolid, firstname, middlename, "
					+ "lastname, email, phone, addressid, position) values (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?) ";

			stmt = conn.prepareStatement(sql);
			int i = 0;

			for (PointOfContact poc : protoPOCList) {
				String firstName = poc.getFirstName() != null ? poc.getFirstName() : null;
				String midName = poc.getMiddleName() != null ? poc.getMiddleName() : null;
				String lastName = poc.getLastName() != null ? poc.getLastName() : null;
				String phone = poc.getPhone() != null ? poc.getPhone() : null;
				String email = poc.getEmail() != null ? poc.getEmail() : null;
				Integer addressId = addressDao.createAddress(poc.getAddress());
				String position = poc.getPosition() != null ? poc.getPosition() : null;

				stmt.setInt(1, protocolId);
				stmt.setString(2, firstName);
				stmt.setString(3, midName);
				stmt.setString(4, lastName);
				stmt.setString(5, email);
				stmt.setString(6, phone);
				stmt.setInt(7, addressId);
				stmt.setString(8, position);
				stmt.addBatch();
				i++;

				if (i % 1000 == 0 || i == protoPOCList.size()) {
					stmt.executeBatch(); // Execute every 1000 items.
				}
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to add new point of contact to protocol: " + protocolId, e);
		} finally {
			this.close(stmt);
			this.close(rs);
		}
	}

	public void updateProtocolClinicalLocationAsso(ClinicalLocation clinicLoc) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			Address address = clinicLoc.getAddress();
			AddressDao addressDao = AddressDao.getInstance(conn);
			addressDao.updateAddress(address);

			String sql = "update clinicallocation set clinicallocationname = ? where clinicallocationid = ? ";

			stmt = conn.prepareStatement(sql);
			stmt.setString(1, clinicLoc.getName());
			stmt.setLong(2, clinicLoc.getId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to update the associates between protocol and clinical location points.",
					e);
		} finally {
			this.close(stmt);
		}
	}

	public void deleteProtocolClinicalLocationAsso(List<Integer> clinicalLocIdToKeepList, int protocolId)
			throws CtdbException {
		PreparedStatement stmt = null;
		PreparedStatement stmt1 = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer("select addressid from clinicallocation where protocolid = ? ");
			StringBuffer sql1 = new StringBuffer("delete from clinicallocation where protocolid = ? ");

			if (!clinicalLocIdToKeepList.isEmpty()) {
				String idListToKeepStr = StringUtils.join(clinicalLocIdToKeepList, ", ");
				String inClause = "and clinicallocationid not in (" + idListToKeepStr + ") ";
				sql.append(inClause);
				sql1.append(inClause);
			}

			// Search for any addresses linked to the clinical locations that will be deleted.
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();

			// Setup the Address deletion statement.
			String sql2 = "delete from address where addressid = ? ";
			stmt1 = conn.prepareStatement(sql2);

			// Batch up any address deletions.
			while (rs.next()) {
				stmt1.setLong(1, rs.getLong("addressid"));
				stmt1.addBatch();
			}

			rs.close();
			stmt.close();

			// Execute the clinical location deletions.
			stmt = conn.prepareStatement(sql1.toString());
			stmt.setLong(1, protocolId);
			stmt.executeUpdate();

			// Execute the batch of address deletions.
			stmt1.executeBatch();
		} catch (SQLException e) {
			throw new CtdbException("Unable to delete the associates between protocol (" + protocolId
					+ ") and clinicallocation points.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
			this.close(stmt1);
		}
	}

	public void deleteProtocolProcedureAsso(int protocolId) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "delete from protocol_procedure where protocolid = ? ";

			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, protocolId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to delete the associates between protocol (" + protocolId + ") and procedures.", e);
		} finally {
			this.close(stmt);
		}
	}

	public void updateProtocolPointOfContactAsso(PointOfContact poc) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			Address address = poc.getAddress();
			AddressDao addressDao = AddressDao.getInstance(conn);
			addressDao.updateAddress(address);

			String sql = "update pointofcontact set firstname = ?, middlename = ?, lastname = ?, "
					+ " position = ?, email = ?, phone = ? " + " where pointofcontactid = ? ";

			stmt = conn.prepareStatement(sql);
			stmt.setString(1, poc.getFirstName());
			stmt.setString(2, poc.getMiddleName());
			stmt.setString(3, poc.getLastName());
			stmt.setString(4, poc.getPosition());
			stmt.setString(5, poc.getEmail());
			stmt.setString(6, poc.getPhone());
			stmt.setInt(7, poc.getId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to update the associates between protocol and point of contacts.", e);
		} finally {
			this.close(stmt);
		}
	}

	public void deleteProtocolPointOfContactAsso(List<Integer> pocIdToKeepList, int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		PreparedStatement stmt1 = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer("select addressid from pointofcontact where protocolid = ? ");
			StringBuffer sql1 = new StringBuffer("delete from pointofcontact where protocolid = ? ");

			if (!pocIdToKeepList.isEmpty()) {
				String idListToKeepStr = StringUtils.join(pocIdToKeepList, ", ");
				String inClause = "and pointofcontactid not in (" + idListToKeepStr + ") ";

				sql.append(inClause);
				sql1.append(inClause);
			}

			// Find all addresses that are linked to the point of contacts that are about to be deleted.
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, protocolId);
			rs = stmt.executeQuery();

			// Setup the address deletion statement.
			String sql2 = "delete from address where addressid = ? ";
			stmt1 = conn.prepareStatement(sql2);

			while (rs.next()) {
				stmt1.setLong(1, rs.getLong("addressid"));
				stmt1.addBatch();
			}

			rs.close();
			stmt.close();

			// Execute the deletion of point of contacts.
			stmt = conn.prepareStatement(sql1.toString());
			stmt.setInt(1, protocolId);
			stmt.executeUpdate();

			// Execute the batched address deletions.
			stmt1.executeBatch();
		} catch (SQLException e) {
			throw new CtdbException("Unable to delete the associates between protocol and point of contacts.", e);
		} finally {
			this.close(rs);
			this.close(stmt1);
			this.close(stmt);
		}
	}

	public int createProcedure(Procedure proc) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "insert into procedure (procedureid, proceduretypeid, name) " + " values (DEFAULT, ?, ?); ";

			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, proc.getProcedureType().getId());
			stmt.setString(2, proc.getName());
			stmt.executeUpdate();
			proc.setId(getInsertId(conn, "procedure_seq"));
		} catch (SQLException e) {
			throw new CtdbException("Unable to create new procedure.", e);
		} finally {
			this.close(stmt);
			this.close(rs);
		}

		return proc.getId();
	}

	public void createProtocolMilesStoneAsso(int protocolId, List<MilesStone> protoMStoneList) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "insert into milesstone (milesstoneid, protocolid, milesstonename, milesstonedate) "
					+ "values (DEFAULT, ?, ?, ? )";

			stmt = conn.prepareStatement(sql);
			int i = 0;

			for (MilesStone mStone : protoMStoneList) {
				stmt.setInt(1, protocolId);
				stmt.setString(2, mStone.getName());
				stmt.setTimestamp(3, new Timestamp(mStone.getMilesStoneDate().getTime()));
				stmt.addBatch();
				i++;

				if (i % 1000 == 0 || i == protoMStoneList.size()) {
					stmt.executeBatch(); // Execute every 1000 items.
				}
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to associate new milesstone to protocol: " + protocolId, e);
		} finally {
			this.close(stmt);
			this.close(rs);
		}
	}

	public void deleteProtocolMilesStoneAsso(int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "delete from milesstone where protocolid = ? ";

			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, protocolId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to delete the associates between protocol (" + protocolId + ") and milesstone.", e);
		} finally {
			this.close(stmt);
			this.close(rs);
		}
	}

	public List<MilesStone> getProtocolMilesStone(int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<MilesStone> protocolMStoneList = new ArrayList<MilesStone>();

		try {
			String sql = "select * from milesstone where protocolid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				MilesStone mStone = new MilesStone();

				mStone.setId(rs.getInt("milesstoneid"));
				mStone.setName(rs.getString("milesstonename"));
				mStone.setMilesStoneDate(rs.getTimestamp("milesstonedate"));
				protocolMStoneList.add(mStone);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve milesstones for protocol: " + protocolId, e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return protocolMStoneList;
	}

	public List<ClinicalLocation> getAllClinicalLocs() throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<ClinicalLocation> protocolSiteIdList = new ArrayList<ClinicalLocation>();
		AddressDao addressDao = AddressDao.getInstance(conn);

		try {
			String sql = "select * from clinicallocation ";
			stmt = this.conn.prepareStatement(sql);
			rs = stmt.executeQuery();

			while (rs.next()) {
				ClinicalLocation clinicalLoc = new ClinicalLocation();
				clinicalLoc.setId(rs.getInt("clinicallocationid"));
				clinicalLoc.setName(rs.getString("clinicallocationname"));
				Address address = addressDao.getAddress(rs.getInt("addressid"));
				clinicalLoc.setAddress(address);
				protocolSiteIdList.add(clinicalLoc);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve all clinical locations.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return protocolSiteIdList;
	}

	public Interval getIntervalByClinicalPnt(int clinicalPntId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Interval interval = new Interval();

		try {
			String sql = "select v.* from interval v inner join interval_clinicalpoint icp "
					+ "on v.intervalid = icp.intervalid where icp.id = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, clinicalPntId);
			rs = stmt.executeQuery();

			if (rs.next()) {
				interval = this.rsToInterval(rs);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve interval by clinical point: " + clinicalPntId, e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return interval;
	}

	public List<ProtocolClosingOut> getClosingOutListByStudyId(String studyId) throws CtdbException {
		List<ProtocolClosingOut> closingOutList = new ArrayList<ProtocolClosingOut>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "select p.protocolid, p.protocolnumber, p.name as protocolname, p.brics_studyid, "
					+ "p.enableesignature, u.usrid, u.brics_userid, u.username, u.firstname, u.lastname, pco.closingoutdate "
					+ " from protocol p left join protocolclosingout pco on p.protocolid = pco.protocolid "
					+ " left join usr u on u.usrid = pco.usrid where p.brics_studyid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, studyId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				ProtocolClosingOut closingOut = new ProtocolClosingOut();

				closingOut.setProtocolId(rs.getInt("protocolid"));
				closingOut.setProtocolName(rs.getString("protocolname"));
				closingOut.setProtocolNumber(rs.getString("protocolnumber"));
				closingOut.setBricsStudyId(rs.getString("brics_studyid"));
				closingOut.setProtoEnableEsignature(rs.getBoolean("enableesignature"));

				int closingUserId = rs.getInt("usrid");
				if (rs.wasNull()) {
					closingOut.setClosingUserId(null);
				} else {
					closingOut.setClosingUserId(closingUserId);
				}

				long closingBricsUserId = rs.getLong("brics_userid");
				if (rs.wasNull()) {
					closingOut.setBricsStudyId(null);
				} else {
					closingOut.setClosingBricsUserId(closingBricsUserId);
				}

				String closingUsrname = rs.getString("username");
				if (rs.wasNull()) {
					closingOut.setClosingUserName("");
				} else {
					closingOut.setClosingUserName(closingUsrname);
				}

				StringBuffer closingUserFullName = new StringBuffer();
				String closingUsrLN = rs.getString("lastname");
				if (!rs.wasNull()) {
					closingUserFullName.append(closingUsrLN);
				}

				String closingUsrFN = rs.getString("firstname");
				if (!rs.wasNull()) {
					closingUserFullName.append(", " + closingUsrFN);
				}

				closingOut.setClosingUserFullName(closingUserFullName.toString());
				closingOut.setClosingOutDate(rs.getTimestamp("closingoutdate"));
				closingOutList.add(closingOut);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve closing out for study: " + studyId, e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return closingOutList;
	}

	public void saveProtocolClosingout(ProtocolClosingOut pco) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "insert into protocolclosingout (protocolclosingoutid, protocolid, "
					+ "brics_studyid, usrid, brics_userid, closingoutdate) values (DEFAULT, ?, ?, ?, ?, ?) ";

			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, pco.getProtocolId());
			stmt.setString(2, pco.getBricsStudyId());
			stmt.setInt(3, pco.getClosingUserId());
			stmt.setLong(4, pco.getClosingBricsUserId());
			stmt.setTimestamp(5, new Timestamp(pco.getClosingOutDate().getTime()));
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to save closing out data.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public boolean checkProtocolClosed(int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean closedout = false;

		try {
			String sql = "select * from protocolclosingout where protocolId = ? ";

			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, protocolId);
			rs = stmt.executeQuery();

			if (rs.next()) {
				closedout = true;
			}

		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve closing out status for protocol: " + protocolId, e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return closedout;
	}
}
