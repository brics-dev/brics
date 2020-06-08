package gov.nih.nichd.ctdb.response.dao;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.postgresql.util.PSQLException;

import gov.nih.nichd.ctdb.attachments.manager.AttachmentManager;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.PaginationData;
import gov.nih.nichd.ctdb.common.ResourceNotAvailableException;
import gov.nih.nichd.ctdb.common.ServerFileSystemException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.Version;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.form.domain.DataCollectionExport;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.FormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.form.util.FormDataStructureUtility;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.domain.PatientProtocol;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.question.dao.QuestionManagerDao;
import gov.nih.nichd.ctdb.question.domain.PatientCalendarQuestion;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.form.QuestionForm;
import gov.nih.nichd.ctdb.response.common.DuplicateDataEntriesException;
import gov.nih.nichd.ctdb.response.common.VisitDateMismatchException;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.CalendarResponse;
import gov.nih.nichd.ctdb.response.domain.DataEntryAssArch;
import gov.nih.nichd.ctdb.response.domain.DataEntryDraft;
import gov.nih.nichd.ctdb.response.domain.EditAnswerDisplay;
import gov.nih.nichd.ctdb.response.domain.EditAssignment;
import gov.nih.nichd.ctdb.response.domain.FormCollectionDisplay;
import gov.nih.nichd.ctdb.response.domain.FormInterval;
import gov.nih.nichd.ctdb.response.domain.PatientCalendarCellResponse;
import gov.nih.nichd.ctdb.response.domain.Response;
import gov.nih.nichd.ctdb.response.domain.SummaryQueryPF;
import gov.nih.nichd.ctdb.response.form.DataCollectionLandingForm;
import gov.nih.nichd.ctdb.response.form.DataEntryInProgressForm;
import gov.nih.nichd.ctdb.response.form.DataEntrySetupForm;
import gov.nih.nichd.ctdb.response.util.AdminFormMetaDataEdit;
import gov.nih.nichd.ctdb.response.util.DataCollectionSummary;
import gov.nih.nichd.ctdb.response.util.MetaDataHistory;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * ResponseManagerDao interacts with the Data Layer for the ResponseManager. The
 * only job of the DAO is to manipulate the data layer.
 * 
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ResponseManagerDao extends CtdbDao {

	private static final StringBuffer SQL_RETRIEV_REASS_ARCHIVE;
	private static final StringBuffer SQL_RETRIEV_ALL_USER_MAP;
	private static final StringBuffer SQL_RETRIEV_PROTOCOL_USER_MAP;
	private static final StringBuffer SQL_RETRIEV_PROTOCOL_USER_MAP2;
	private static final StringBuffer SQL_UPDATE_DATA_ENTRY_ASSIGNMENT;
	private static final StringBuffer SQL_NEW_DATA_ENTRY_ASSIGNMENT;
	private static final StringBuffer SQL_RETRIEV_DATA_ENTRY_DRAFT;
	private static final StringBuffer SQL_RETRIEV_EFORMID;
	private static final StringBuffer SQL_RETRIEV_AFORM_COUNT;
	private static final StringBuffer SQL_RETRIEV_EFORM_INTERVAL_COUNT;
	private static final String SQL_RETRIEV_SUMMARY_QUERY;

	private static Logger logger = Logger.getLogger(ResponseManagerDao.class);
	
	static {
		SQL_RETRIEV_REASS_ARCHIVE = new StringBuffer();
		SQL_RETRIEV_REASS_ARCHIVE
				.append("SELECT previousby, currentby, reassignby, assigneddate, dataentryflag ");
		SQL_RETRIEV_REASS_ARCHIVE.append("FROM dataentryreassignarchive ");
		SQL_RETRIEV_REASS_ARCHIVE.append("WHERE dataentrydraftid IN ");
		SQL_RETRIEV_REASS_ARCHIVE
				.append("(SELECT dataentrydraftid FROM dataentrydraft WHERE administeredformid=?) ");
		SQL_RETRIEV_REASS_ARCHIVE
				.append("ORDER BY dataentryflag, assigneddate");

		SQL_RETRIEV_ALL_USER_MAP = new StringBuffer();
		SQL_RETRIEV_ALL_USER_MAP
				.append("SELECT usrid, username FROM usr ORDER BY usrid");

		SQL_RETRIEV_PROTOCOL_USER_MAP = new StringBuffer();
		SQL_RETRIEV_PROTOCOL_USER_MAP
				.append("SELECT distinct ur.usrid, ur.username ");
		SQL_RETRIEV_PROTOCOL_USER_MAP
				.append("FROM usr ur, protocolusrrole pr ");
		SQL_RETRIEV_PROTOCOL_USER_MAP
				.append("WHERE ur.usrid = pr.usrid AND pr.protocolid = ? ");
		SQL_RETRIEV_PROTOCOL_USER_MAP
				.append("AND ur.usrid <> ? ORDER BY usrid");
		
		SQL_RETRIEV_PROTOCOL_USER_MAP2 = new StringBuffer();
		SQL_RETRIEV_PROTOCOL_USER_MAP2
				.append("SELECT distinct ur.usrid, ur.username, ur.lastname, ur.firstname ");
		SQL_RETRIEV_PROTOCOL_USER_MAP2
				.append("FROM usr ur, protocolusrrole pr ");
		SQL_RETRIEV_PROTOCOL_USER_MAP2
				.append("WHERE ur.usrid = pr.usrid AND pr.protocolid = ? ");
		SQL_RETRIEV_PROTOCOL_USER_MAP2
				.append("ORDER BY usrid");
		

		SQL_UPDATE_DATA_ENTRY_ASSIGNMENT = new StringBuffer();
		SQL_UPDATE_DATA_ENTRY_ASSIGNMENT
				.append("UPDATE dataentrydraft SET dataenteredby = ?, updateddate = CURRENT_TIMESTAMP ");
		SQL_UPDATE_DATA_ENTRY_ASSIGNMENT.append("WHERE dataentrydraftid = ? ");

		SQL_NEW_DATA_ENTRY_ASSIGNMENT = new StringBuffer();
		SQL_NEW_DATA_ENTRY_ASSIGNMENT
				.append("INSERT INTO dataentryreassignarchive ");
		SQL_NEW_DATA_ENTRY_ASSIGNMENT
				.append("VALUES(DEFAULT,?,?,?,?,CURRENT_TIMESTAMP,?)");

		SQL_RETRIEV_DATA_ENTRY_DRAFT = new StringBuffer();
		SQL_RETRIEV_DATA_ENTRY_DRAFT
				.append("SELECT af.formid, dd.administeredformid, dd.dataentrydraftid, dd.dataenteredby, ");
		SQL_RETRIEV_DATA_ENTRY_DRAFT.append("ur.username, dd.dataentryflag ");
		SQL_RETRIEV_DATA_ENTRY_DRAFT
				.append("FROM dataentrydraft dd, usr ur, administeredform af ");
		SQL_RETRIEV_DATA_ENTRY_DRAFT
				.append("WHERE ur.usrid = dd.dataenteredby AND af.formid = ? ");
		SQL_RETRIEV_DATA_ENTRY_DRAFT
				.append("AND af.administeredformid = dd.administeredformid");
		
		SQL_RETRIEV_EFORMID = new StringBuffer();
		SQL_RETRIEV_EFORMID.append("select eformid from eform where shortname = ?");
		
		
		SQL_RETRIEV_AFORM_COUNT = new StringBuffer();
		SQL_RETRIEV_AFORM_COUNT.append("select count(*) as count from administeredform where eformid  = ?");
		
		SQL_RETRIEV_EFORM_INTERVAL_COUNT = new StringBuffer();
		SQL_RETRIEV_EFORM_INTERVAL_COUNT.append("select count(*) as count from form_interval where eformid  = ?");
		
		SQL_RETRIEV_SUMMARY_QUERY = "select * from summary_query where shortname = ? ";
		

	}

	/**
	 * Private Constructor to hide the instance creation implementation of the
	 * ResponseManagerDao object in memory. This will provide a flexible
	 * architecture to use a different pattern in the future without refactoring
	 * the ResponseManager.
	 */
	private ResponseManagerDao() {

	}

	/**
	 * Method to retrieve the instance of the ResponseManagerDao.
	 * 
	 * @return ResponseManagerDao data object
	 */
	public static synchronized ResponseManagerDao getInstance() {
		return new ResponseManagerDao();
	}

	/**
	 * Method to retrieve the instance of the ResponseManagerDao. This method
	 * accepts a Database Connection to be used internally by the DAO. All
	 * transaction management will be handled at the BusinessManager level.
	 * 
	 * @param conn
	 *            Database connection to be used within this data object
	 * @return ResponseManagerDao data object
	 */
	public static synchronized ResponseManagerDao getInstance(Connection conn) {
		ResponseManagerDao dao = new ResponseManagerDao();
		dao.setConnection(conn);
		return dao;
	}

	/**
	 * Creates the administered form. It checks if two data entries have been
	 * done already before creating a new one.
	 * 
	 * @param administeredForm
	 *            the Administered form object to create
	 * @param isClinicalTrial
	 *            boolean value for if a protocol is a clinicla trial
	 * @throws DuplicateDataEntriesException
	 *             thrown if two data entries exist already
	 * @throws DuplicateObjectException
	 *             thrown if the Administered form exists in the system based on
	 *             the unique constraints
	 * @throws VisitDateMismatchException
	 *             thrown if the visit date does not match first key
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public void create(AdministeredForm administeredForm) throws DuplicateDataEntriesException,
			DuplicateObjectException, VisitDateMismatchException, CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			if (!administeredForm.getForm().isAllowMultipleCollectionInstances()){
				checkDataEntryDuplication(administeredForm);
			}
				administeredForm.setDataEntrySession(CtdbConstants.SINGLE_ENTRY_FORM);
				sql.append("insert into administeredform(administeredformid, eformid, intervalid, ");
				sql.append("patientid, patientversion, discrepancyflag, visitdate,  is_cat, scheduledVisitDate) ");
				sql.append("values(DEFAULT,?,?,?,?,false, ?, ?, ?)");

				stmt = this.conn.prepareStatement(sql.toString());
				stmt.setLong(1, administeredForm.getForm().getId());
				
				if (administeredForm.getInterval() == null) {
					stmt.setNull(2, java.sql.Types.NUMERIC);
				} else {
					stmt.setInt(2, administeredForm.getInterval().getId());
				}

				stmt.setLong(3, administeredForm.getPatient().getId());
				stmt.setInt(4, administeredForm.getPatient().getVersion().getVersionNumber());
				
				if (administeredForm.getVisitDate() == null) {
					stmt.setNull(5, java.sql.Types.DATE);
				} else {
					stmt.setTimestamp(5, new Timestamp(administeredForm
							.getVisitDate().getTime()));
				}
				stmt.setBoolean(6, administeredForm.isCAT());
				
				if (administeredForm.getScheduledVisitDate() == null) {
					stmt.setNull(7, java.sql.Types.DATE);
				} else {
					stmt.setTimestamp(7, new Timestamp(administeredForm.getScheduledVisitDate().getTime()));
				}
				
				stmt.executeUpdate();
				administeredForm.setId(this.getInsertId(conn,
						"administeredform_seq"));
		} catch (PSQLException e) {
			if (e.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A administered form already exists: " + e.getMessage(),
						e);
			} else {
				throw new CtdbException("Unable to create administered form: "
						+ e.getMessage(), e);
			}
		} catch (SQLException e) {
			//System.out.println("unique constraint erroe");
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A administered form already exists: " + e.getMessage(),
						e);
			} else {
				throw new CtdbException("Unable to create administered form: "
						+ e.getMessage(), e);
			}
		} finally {
			this.close(stmt);
		}
	}



	/**
	 * Checks if an attempt to start administering a form will result in
	 * duplicate data entries
	 * If no results from query, all data entry sessions have begun
	 * If results & administeredformid, second data entry session has not begun on double key
	 * if results & no administeredformid, no data entry has begun
	 * @param af
	 *            teh administered form
	 * @param isClinicalTrial
	 *            boolean value for if the protocol is a clinical trial.
	 * @return the number of data entries?
	 * @throws DuplicateDataEntriesException
	 *             if the data entry is duplicate
	 * @throws DuplicateObjectException
	 *             if there is a duplicate object
	 * @throws SQLException
	 *             who knows?
	 */
	private void checkDataEntryDuplication(AdministeredForm af) throws DuplicateDataEntriesException, DuplicateObjectException, SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select count(*) cnt from administeredform a where a.eformid = ? ");
					if (af.getInterval() == null) {
						sql.append("    and a.intervalid is null ");
					} else {
						sql.append("    and a.intervalid = ? ");
					}
			sql.append("    and a.patientid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, af.getForm().getId());
			if (af.getInterval() == null) {
				stmt.setInt(2, af.getPatient().getId());

			} else {
				stmt.setInt(2, af.getInterval().getId());
				stmt.setInt(3, af.getPatient().getId());
			}
			rs = stmt.executeQuery();
			if (rs.next()) {
				int count = rs.getInt("cnt");
				if(count > 0) {
					throw new DuplicateDataEntriesException("Maxmium data entries exist");
				}
			}

		} finally {
			this.close(stmt);
			this.close(rs);
		}

	}

	/**
	 * Creates the data entry draft record.
	 * 
	 * @param administeredForm
	 *            the administered form object
	 * @param user
	 *            the user object
	 * @throws DuplicateObjectException
	 *             thrown if dataentrydraft record exists in the system
	 * @throws CtdbException
	 *             thrown if any errors occur
	 */
	public void createDataEntryDraft(AdministeredForm administeredForm, User user) throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;
		
		try {
			StringBuffer sql = new StringBuffer(50);
			if(administeredForm.isMarkAsCompleted()){
			sql.append("insert into dataentrydraft(dataentrydraftid, administeredformid, dataenteredby, createddate, updateddate, dataentryflag,coll_status,completeddate,completedby) ");
			sql.append("values(DEFAULT,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,?,"+"'"+CtdbConstants.DATACOLLECTION_STATUS_COMPLETED+"'"+",CURRENT_TIMESTAMP,?" +")");
			}else{
				sql.append("insert into dataentrydraft(dataentrydraftid, administeredformid, dataenteredby, createddate, updateddate, dataentryflag,coll_status) ");
				sql.append("values(DEFAULT,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,?,"+"'"+CtdbConstants.DATACOLLECTION_STATUS_INPROGRESS+"'"+")");
			}

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredForm.getId());
			stmt.setLong(2, user.getId());
			stmt.setInt(3, administeredForm.getDataEntrySession());
			if(administeredForm.isMarkAsCompleted()){
				stmt.setLong(4, user.getId());
				
			}

			stmt.executeUpdate();
			administeredForm.setDataEntryDraftId(this.getInsertId(conn,
					"dataentrydraft_seq"));

			// this.createResponseDraftAll(administeredForm);
		} catch (PSQLException e) {
			if (e.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A data entry draft for the administered form already exists for the user id "
								+ user.getId() + ": " + e.getMessage(), e);
			} else if (e.getSQLState().equals(
					SysPropUtil.getProperty("postgres.deadlock_error.code"))) {
				throw new CtdbException(
						"Database resource is not available now: "
								+ e.getMessage(), e);
			} else if (e
					.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.foreignkey_constraint_sqlstate"))) {
				throw new CtdbException("Integirty violated: : "
						+ e.getMessage(), e);
			} else {
				throw new CtdbException(
						"Unable to create data entry draft for the administered form: "
								+ e.getMessage(), e);
			}
		}

		catch (SQLException e) {
			//System.out.println("sql excpeiton");
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A data entry draft for the administered form already exists for the user id "
								+ user.getId() + " : " + e.getMessage(), e);
			} else if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.deadlock_error.code"))) {
				throw new ResourceNotAvailableException(
						"Database resource is not available now: "
								+ e.getMessage(), e);
			} else if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.integrity_error.code"))) {
				throw new ResourceNotAvailableException(
						"Database resource is not available now: "
								+ e.getMessage(), e);
			} else {
				throw new CtdbException(
						"Unable to create data entry draft for the administered form: "
								+ e.getMessage(), e);
			}
		} finally {
			this.close(stmt);
		}
	}


	/**
	 * Get data entry flag for the form.
	 * 
	 * @param admForm
	 *            the AdministeredForm object
	 * @return int data entry flag; 1 for single key, and 2 for double key
	 * @throws CtdbException
	 *             thrown if any errors occur
	 */
	private int getFormDataEntryFlag(AdministeredForm admForm)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select dataentryflag from form where formid = ?");
			// sql.append(admForm.getForm().getId());
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admForm.getForm().getId());
			rs = stmt.executeQuery();
			int flag = 1;
			if (rs.next()) {
				flag = rs.getInt(1);
			}
			return flag;
		} catch (SQLException e) {
			throw new CtdbException(
					"Error occur while getting form data entry flag : "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	private int getDataEntryNum(AdministeredForm admForm) throws CtdbException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		int num = Integer.MIN_VALUE;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select p.typeid from protocol p, eform ef where ef.eformid = ? and p.protocolid = ef.protocolid ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admForm.getForm().getId());
			rs = stmt.executeQuery();
			if (rs.next()) {
				if (rs.getInt("typeid") == CtdbConstants.PROTOCOL_TYPE_NATURAL_HISTORY) {
					num = getDataEntryNum(admForm, false);
				} else {
					num = getDataEntryNum(admForm, true);
				}
			}
		} catch (SQLException e) {
			throw new CtdbException(
					"Error occur while getting number of data entries : "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		if (num == Integer.MIN_VALUE) {
			throw new CtdbException(
					"Error occur while getting number of data entries : ");
		} else {
			return num;
		}

	}

	/**
	 * Gets the data entry number exists(0, 1 or 2) in the current system for
	 * the administer form.
	 * 
	 * @param admForm
	 *            the administered form object
	 * @param isClinicalTrial
	 *            if the protocol is a clinical trial or not
	 * @return int data entry number(0, 1, or 2) exist in the system.
	 * @throws CtdbException
	 *             thrown if any errors occur
	 */
	private int getDataEntryNum(AdministeredForm admForm,
			boolean isClinicalTrial) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select count(*) from administeredform a, dataentrydraft b ");
			sql.append("where a.administeredformid = b.administeredformid ");
			sql.append("and a.eformid = ? ");
			if (isClinicalTrial) {
				if ( admForm.getInterval().getId() > 0 ) {
					sql.append("and a.intervalid = ? ");
				}
				else {
					sql.append("and a.intervalid is null ");
				}
			} else {
				sql.append(" and a.visitdate = ? ");
			}
			sql.append("and a.patientid = ? ");
			// sql.append("and a.patientversion = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admForm.getForm().getId());
			if (isClinicalTrial) {
				if ( admForm.getInterval().getId() > 0 ) {
					stmt.setLong(2, admForm.getInterval().getId());
					stmt.setLong(3, admForm.getPatient().getId());
				}
				else {
					stmt.setLong(2, admForm.getPatient().getId());
				}
			} else {
				stmt.setTimestamp(2, new Timestamp(admForm.getVisitDate()
						.getTime()));
				stmt.setLong(3, admForm.getPatient().getId());
			}

			rs = stmt.executeQuery();

			int numDataEntry = 0;
			if (rs.next()) {
				numDataEntry = rs.getInt(1);
			}

			return numDataEntry;
		} catch (SQLException e) {
			throw new CtdbException(
					"Error occur while getting number of data entries : "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}



	public void saveVisibleRepeatableSection(AdministeredForm aform,
			DataEntrySetupForm desf, User usr) throws ObjectNotFoundException,
			CtdbException, SQLException, JSONException {
		List sectionListToStore = new ArrayList();
		JSONArray jsonReturnArray;
		
		if(desf.getClickedSectionFields()==null||desf.getClickedSectionFields().length()==0){
			 jsonReturnArray = new JSONArray();
		}else{
		 jsonReturnArray = new JSONArray(desf.getClickedSectionFields());
		}
		
		for (int y = 0; y < jsonReturnArray.length(); y++) {
			sectionListToStore.add(jsonReturnArray.getInt(y));
		}
		
		boolean newSectionExist = false;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		logger.info("ResponseManagerDao->saveVisibleRepeatableSection->sectionListToStore:\t"+sectionListToStore.size());
		
		for (int y = 0; y < sectionListToStore.size(); y++) {
			int sectIdoStore = (Integer) new ArrayList(sectionListToStore).get(y);
			try {
				//Query to find out if the row to be inserted exist in db and if exist set a flag to run insert script
				StringBuffer sql = new StringBuffer(200);
				//sql.append("select count(1) from visibleadminsteredsection where  administeredformid =?  and sectionid=?  and userid= ? " );
				sql.append("select count(1) from visibleadminsteredsection where  administeredformid =?  and dict_sectionid=?  and userid= ? " );
				stmt = this.conn.prepareStatement(sql.toString());
				stmt.setLong(1, aform.getId());
				stmt.setLong(2, sectIdoStore);
				stmt.setLong(3, usr.getId());
				rs = stmt.executeQuery();

				while (rs.next()) {
					int count = rs.getInt(1);
					if (count > 0) {
						newSectionExist = true;
					} else{
						newSectionExist = false;
					}
				}
				
				rs.close();
				stmt.close();

				if (!newSectionExist) {
					sql = new StringBuffer(200);
					//sql.append("insert into visibleadminsteredsection(administeredformid, sectionid, userid) ");
					sql.append("insert into visibleadminsteredsection(administeredformid, dict_sectionid, userid) ");
					sql.append("values(?, ?, ?)");
					stmt = this.conn.prepareStatement(sql.toString());
					stmt.setLong(1, aform.getId());
					stmt.setLong(2, sectIdoStore);
					stmt.setLong(3, usr.getId());
					stmt.executeUpdate();
				}
		
				logger.info("ResponseManagerDao->saveVisibleRepeatableSection->aformId:\t"+aform.getId()+":\t sectionid:\t"+sectIdoStore+":\t userid:\t"+usr.getId());
			} catch (PSQLException e) {
			 if (e.getSQLState()
						.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
					throw new DuplicateObjectException(
							"A section entry  already exists: " + e.getMessage(), e);
				} else if (e.getSQLState()
							.equals(SysPropUtil.getProperty("postgres_database.foreignkey_constraint_sqlstate"))) {
						throw new DuplicateObjectException(
								"A section entry  already exists: " + e.getMessage(), e);
				} else {
					throw new CtdbException(
							"Unable to insert  data visibleadminsteredsection: "
									+ e.getMessage(), e);
				}
			} catch (SQLException e) {
				throw new CtdbException(
						"Error occur while getting the data entry draft ID : "
								+ e.getMessage(), e);
			} finally {
				this.close(rs);
				this.close(stmt);
			}
		}
	}

	/**
	 * Method to get patient visit dates from patient module to data collection by site module
	 * 
	 * @param dataForm
	 * @param dcrc
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws CtdbException
	 * @throws SQLException
	 */
	public List getPatientViewDataForLandingPageBySites(DataCollectionLandingForm dataForm, int protocolId,
			Map<String, String> searchOptions, List<Integer> siteIds)
			throws ObjectNotFoundException, CtdbException, SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List dataFormList = new ArrayList();
		DataCollectionLandingForm dataFormResult = new DataCollectionLandingForm();
		Array siteIdsArr = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append(
					"select i.name iname,pv.intervalid intervalName,pv.visitdateid visitDateId,p.guid guid,pv.patientid as pvPatientId,to_char (pv.visitdate, 'YYYY-MM-DD HH24:MI' ) as pvVisitDate, ("
							+ getDecryptionFunc("p.firstname") + "   ||' ' ||" + getDecryptionFunc("p.lastname")
							+ ") as  pvPatientName,p.firstname as patientFirstName, p.lastname as   patientLastName,pp.subjectid as subjectid, "
							+ getDecryptionFunc("p.mrn") + " as mrn, pp.subjectnumber subjectNumber ");
			sql.append("from patientvisit pv ");
			sql.append("join patient p on p.patientid = pv.patientid ");
			sql.append(
					"left outer join patientprotocol pp on pv.patientid = pp.patientid and pv.protocolid = pp.protocolid ");
			sql.append("left outer join interval i on pv.intervalid = i.intervalid ");
			sql.append("where p.deleteflag = false and pv.protocolid = ?");

			if (siteIds != null) {
				Integer[] arrSiteIds = (Integer[]) siteIds.toArray(new Integer[siteIds.size()]);
				siteIdsArr = this.conn.createArrayOf("BIGINT", arrSiteIds);
				sql.append(" and pp.siteid = ANY (?)");
			}

			if ((searchOptions.get(CtdbConstants.DATA_COLLECTION_VISIT_DATE) != null)) {
				sql.append(" and cast(pv.visitdate as date)  = " + "'"
						+ searchOptions.get(CtdbConstants.DATA_COLLECTION_VISIT_DATE) + "'");
			}

			if ((searchOptions.get(CtdbConstants.DATA_COLLECTION_SUBJECT_GUID) != null)) {
				sql.append(
						" and p.guid  = " + "'" + searchOptions.get(CtdbConstants.DATA_COLLECTION_SUBJECT_GUID) + "'");
			}

			// sql.append(dcrc.getPatientViewSearchClause());
			sql.append("order by pv.visitdate ASC");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);

			if (siteIds != null) {
				stmt.setArray(2, siteIdsArr);
			}

			rs = stmt.executeQuery();

			while (rs.next()) {
				dataFormResult = new DataCollectionLandingForm();

				if (rs.getString("pvPatientName") != null) {
					dataFormResult.setPvPatientName(rs.getString("pvPatientName"));
				}

				if (rs.getString("pvPatientId") != null) {
					dataFormResult.setPvPatientId(rs.getInt("pvPatientId"));
				}

				if (rs.getString("patientFirstName") != null) {
					dataFormResult.setPatientFirstName(rs.getString("patientFirstName"));
				}

				if (rs.getString("patientLastName") != null) {
					dataFormResult.setPatientLastName(rs.getString("patientLastName"));
				}

				if (rs.getString("pvVisitDate") != null) {
					dataFormResult.setPvVisitDate(rs.getString("pvVisitDate"));
				}

				if (rs.getString("subjectid") != null) {
					dataFormResult.setSubjectId(rs.getString("subjectid"));
				}

				if (rs.getString("mrn") != null) {
					dataFormResult.setMrn(rs.getString("mrn"));
				}

				if (rs.getString("guid") != null) {
					dataFormResult.setPvGuid(rs.getString("guid"));
				}

				if (rs.getString("visitDateId") != null) {
					dataFormResult.setPvVisitDateId(rs.getString("visitDateId"));
				}

				if (rs.getString("intervalName") != null) {
					dataFormResult.setPvInterval(rs.getString("intervalName"));
				}

				if (rs.getString("iname") != null) {
					dataFormResult.setPvIntervalName(rs.getString("iname"));
				}

				dataFormList.add(dataFormResult);
			}

		} catch (SQLException e) {
			throw new CtdbException("Error occur while getting the data entry draft ID : ", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return dataFormList;
	}

	/**
	 * Method to data collection landing table and search by yogi
	 * 
	 * @param administeredFormId
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws CtdbException
	 */
	public List<DataCollectionLandingForm> getCollectDataSearchResult(DataCollectionLandingForm dataForm, int protocolId,Map<String,String> searchOptions)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<DataCollectionLandingForm> dataFormList = new ArrayList<DataCollectionLandingForm>();
		DataCollectionLandingForm dataFormResult = new DataCollectionLandingForm();

		try {
			StringBuffer sql = new StringBuffer(2000);

			
			
			sql.append("select distinct ef.eformid, ef.name as formName, ef.shortname as shortName, to_char(ef.updateddate, 'YYYY-MM-DD HH24:MI') as updateDate, pr.protocolid ");
			sql.append("from eform ef,protocol pr,form_interval fi ");
			sql.append("where pr.protocolid = ef.protocolid  and fi.eformid = ef.eformid");
			sql.append(" and pr.protocolid = ?");
			
			if((searchOptions.get(CtdbConstants.DATA_COLLECTION_EFORMS_NAME)!= null)){
				sql.append(" and ef.shortName = " + "'" + searchOptions.get(CtdbConstants.DATA_COLLECTION_EFORMS_NAME) + "'");
			}
			
			if((searchOptions.get(CtdbConstants.DATA_COLLECTION_FORM_LAST_UPDATED)!= null)){
				sql.append(" and cast(ef.updateddate as date) = " + "'" + searchOptions.get(CtdbConstants.DATA_COLLECTION_FORM_LAST_UPDATED) + "'");
			}
			
			/*if((searchOptions.get(CtdbConstants.DATA_COLLECTION_FORM_STATUS)!= null)){
				sql.append(" and = " + "'" + searchOptions.get(CtdbConstants.DATA_COLLECTION_FORM_STATUS) + "'");
			}*/
			

			//sql.append(dcrc.getSearchClause());
			sql.append(" order by formName ASC");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				dataFormResult = new DataCollectionLandingForm();
				if (rs.getString("eformid") != null) {
					dataFormResult.setFormId(rs.getInt("eformid"));
				}

				if (rs.getString("formName") != null) {
					dataFormResult.setFormName(rs.getString("formName"));
				}
				
				
				if (rs.getString("shortName") != null) {
					dataFormResult.setShortName(rs.getString("shortName"));

				}
				//if (rs.getString("formStatusName") != null) {
					dataFormResult.setFormStatusName("Active");
				//}

				if (rs.getString("updateDate") != null) {
					dataFormResult.setFormLastUpdatedDate(rs.getString("updateDate"));
				}
				dataFormList.add(dataFormResult);
			}
			return dataFormList;
		} catch (SQLException e) {
			throw new CtdbException(
					"Error occur while getting the data entry draft ID : "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Gets list of in progress forms
	 * 
	 * @param administeredFormId
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws CtdbException
	 */
	public List<DataCollectionLandingForm> getInProgressSearchResult(
			DataCollectionLandingForm dataForm, int protocolId)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<DataCollectionLandingForm> dataFormList = new ArrayList<DataCollectionLandingForm>();
		DataCollectionLandingForm dataFormResult = new DataCollectionLandingForm();

		try {
			StringBuffer sql = new StringBuffer(2000);
			sql.append("select distinct f.formid, f.name as formName, xf.shortname as formStatusName, f.updateddate, pr.protocolid ");
			sql.append("from form f, xformstatus xf, patient p,protocol pr,xformtype  ft ");
			sql.append("where pr.protocolid = f.protocolid  ");
			sql.append(" and f.xstatusid = xf.xstatusid ");
			sql.append("and f.xstatusid = 4 ");
			sql.append("and  ft.xformtypeid = 10 ");
			sql.append(" and f.formtypeid = ft.xformtypeid ");
			sql.append(" and pr.protocolid = " + protocolId);

			//sql.append(dcrc.getSearchClause());

			stmt = this.conn.prepareStatement(sql.toString());
			rs = stmt.executeQuery();

			while (rs.next()) {
				dataFormResult = new DataCollectionLandingForm();
				if (rs.getString("formid") != null) {
					dataFormResult.setFormId(rs.getInt("formid"));
				}

				if (rs.getString("formName") != null) {
					dataFormResult.setFormName(rs.getString("formName"));

				}
				if (rs.getString("formStatusName") != null) {
					dataFormResult.setFormStatusName(rs
							.getString("formStatusName"));
				}
				if (rs.getString("updateddate") != null) {
					dataFormResult.setFormLastUpdatedDate(rs
							.getString("updateddate"));
				}
				dataFormList.add(dataFormResult);
			}
			return dataFormList;
		} catch (SQLException e) {
			throw new CtdbException(
					"Error occur while getting the data entry draft ID : "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Gets the list of data entry draft ID from a administeredform
	 * 
	 * @param administeredFormId
	 *            The administered form ID
	 * @return list of DataEntryDraftIds for the administeredFormId.
	 * @throws ObjectNotFoundException
	 *             if the object is not found, this exception is trhown
	 * @throws CtdbException
	 *             thrown if any errors occur
	 */
	public List getDataEntryDraftId(int administeredFormId)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List idList = new ArrayList();

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select dataentrydraftid ");
			sql.append("from dataentrydraft ");
			sql.append("where administeredformid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredFormId);

			rs = stmt.executeQuery();

			while (rs.next()) {
				int id = rs.getInt("dataentrydraftid");
				idList.add(Integer.toString(id));
			}
			return idList;
		} catch (SQLException e) {
			throw new CtdbException(
					"Error occur while getting the data entry draft ID : "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Gets the data entry draft ID
	 * 
	 * @param administeredFormId
	 *            The administered form ID
	 * @param userId
	 *            The user ID doing data entry
	 * @return int data entry number(0, 1, or 2) exist in the system.
	 * @throws CtdbException
	 *             thrown if any errors occur
	 * @throws ObjectNotFoundException
	 *             if not found
	 */
	public int getDataEntryDraftId(int administeredFormId, int userId)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select dataentrydraftid ");
			sql.append("from dataentrydraft ");
			sql.append("where dataentrydraft.administeredformid = ? ");
			sql.append("and dataenteredby = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredFormId);
			stmt.setLong(2, userId);

			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new ObjectNotFoundException(
						"The dataentrydraft for this user/administeredform combination does not exist.");
			}

			return rs.getInt(1);
		} catch (SQLException e) {
			throw new CtdbException(
					"Error occur while getting the data entry draft ID : "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	
	/**
	 * Gets the administeredformid ID based on dataentrydraftyid
	 * 
	 * @throws CtdbException
	 *             thrown if any errors occur
	 * @throws ObjectNotFoundException
	 *             if not found
	 */
	public int getAdministeredFormId(int dataEntryDraft)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select administeredformid ");
			sql.append("from dataentrydraft ");
			sql.append("where dataentrydraft.dataentrydraftid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, dataEntryDraft);


			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new ObjectNotFoundException(
						"The administeredformid for this user/administeredform combination does not exist.");
			}

			return rs.getInt(1);
		} catch (SQLException e) {
			throw new CtdbException(
					"Error occur while getting the data entry draft ID : "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Gets the data entry draft ID
	 * 
	 * @param administeredFormId
	 *            The administered form ID
	 * @param userId
	 *            The user ID doing data entry
	 * @return int data entry number(0, 1, or 2) exist in the system.
	 * @throws CtdbException
	 *             thrown if any errors occur
	 * @throws ObjectNotFoundException
	 *             if not found
	 */
	public int getDataEntryFlag(int administeredFormId, int userId)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select dataentryflag ");
			sql.append("from dataentrydraft ");
			sql.append("where dataentrydraft.administeredformid = ? ");
			sql.append("and dataenteredby = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredFormId);
			stmt.setLong(2, userId);

			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new ObjectNotFoundException("The dataentrydraft for this user/administeredform combination does not exist.");
			}

			return rs.getInt(1);
		} catch (ObjectNotFoundException onfe) {
			return -1;
		} catch (SQLException e) {
			throw new CtdbException("Error occur while getting the data entry draft ID : "+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	/**
	 * Method to get userid from dataentry draft table based on the admin from and flag which will be 1 or 2 based on first entry or second entry
	 * @param administeredFormId
	 * @param dataEntryFlag
	 * @return
	 * @throws CtdbException
	 */
	public int getDataEntryUser(int administeredFormId, int dataEntryFlag) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select dataenteredby from dataentrydraft where dataentrydraft.administeredformid = ? and dataentryflag = ? ");
			

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredFormId);
			stmt.setLong(2, dataEntryFlag);

			rs = stmt.executeQuery();

			if (!rs.next()) {
				return -1;
			}

			return rs.getInt(1);
		}
		catch (SQLException e) {
			throw new CtdbException("Error occur while getting the data entry draft ID : "+ e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	
	
	
	
	
	
	/**
	 * Method to get userid from dataentry draft table based on the admin from and flag which will be 1 or 2 based on first entry or second entry
	 * @param administeredFormId
	 * @param dataEntryFlag
	 * @return
	 * @throws CtdbException
	 */
	public String getDataEntryUserName(int administeredFormId, int dataEntryFlag)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select username from dataentrydraft ded, usr u where ded.administeredformid = ? and dataentryflag = ? and ded.dataenteredby = u.usrid");
			

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredFormId);
			stmt.setLong(2, dataEntryFlag);

			rs = stmt.executeQuery();
			
			if (!rs.next()) {
				return null;
			}


			return rs.getString("username");
			
		}catch (SQLException e) {
			throw new CtdbException("Error occur while getting the data entry draft ID : "+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	

	
	/***
	 * Method to get Locked data based on admin from and user from dataentrydraft table
	 * @param administeredFormId
	 * @param userId
	 * @return
	 * @throws CtdbException
	 */
	public Date getDataEntryLockDate(int administeredFormId, int userId)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select lockdate ");
			sql.append("from dataentrydraft ");
			sql.append("where dataentrydraft.administeredformid = ? ");
			sql.append("and dataenteredby = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredFormId);
			stmt.setLong(2, userId);

			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new ObjectNotFoundException("The dataentrydraft for this user/administeredform combination does not exist.");
			}

			return rs.getTimestamp(1);
		} catch (ObjectNotFoundException onfe) {
			logger.error("No data entry draft found.", onfe);
			return null;
		} catch (SQLException e) {
			throw new CtdbException("Error occur while getting the data entry draft ID : "+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	/**
	 * Method to get dataentryflag based on dataEntryDraftId
	 * @param dataEntryDraftId
	 * @return dataEntyFlag will be 1 or 2
	 * @throws CtdbException
	 */
	public int getDataEntryFlag(int dataEntryDraftId)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select dataentryflag ");
			sql.append("from dataentrydraft ");
			sql.append("where dataentrydraftid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, dataEntryDraftId);
			

			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new ObjectNotFoundException("The dataentrydraft for this user/administeredform combination does not exist.");
			}

			return rs.getInt(1);
		} catch (ObjectNotFoundException onfe) {
			return -1;
		} catch (SQLException e) {
			throw new CtdbException("Error occur while getting the data entry draft ID : "+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Get data entry flag for the form.
	 * 
	 * @param formId
	 *            the Form id
	 * @return int data entry flag; 1 for single key, and 2 for double key
	 * @throws CtdbException
	 *             thrown if any errors occur
	 */
	public int getFormDataEntryFlag(int formId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select dataentryflag from form where formid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);
			rs = stmt.executeQuery();
			int flag = 1;
			if (rs.next()) {
				flag = rs.getInt(1);
			}
			return flag;
		} catch (SQLException e) {
			throw new CtdbException(
					"Error occur while getting form data entry flag : "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Gets the list of AdministeredForm based on the administered form id and
	 * user object
	 * 
	 * @param afrc - The administered form result control
	 * @param searchOptions - Some search options.
	 * 
	 * @return List the list of administered form objects
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */

	public List<AdministeredForm> getDataEntrySummary(Map<String, Set<String>> searchOptions) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<AdministeredForm> admForms = new ArrayList<AdministeredForm>();
		StringBuffer sql = new StringBuffer(50);

		try {
			sql.append(" select af.*, tmp.*, "
					+ CtdbDao.getDecryptionFunc("pat.subjectid") + " as subjectid, ");
			sql.append(CtdbDao.getDecryptionFunc("pat.lastname") + " as lastname , "
					+ CtdbDao.getDecryptionFunc("pat.firstname") + " as firstname, ");
			sql.append(" i.name intervalname, i.orderval intervalorder , f.attachfiles  ");
			sql.append(" , pp.orderval patientorder from administeredform af, patient pat, interval i, patientprotocol pp, form f, ");
			sql.append(" (select temp1.administeredformid, dataenteredby1, username1, lockdate1, ded1,   dataenteredby2, username2, lockdate2, ded2 from ");
			sql.append(" (select administeredformid, dataenteredby dataenteredby1, lockdate lockdate1, dataentrydraftid ded1, username username1 from dataentrydraft, usr u where dataentryflag=1 and dataenteredby = u.usrid) temp1");
			sql.append(" LEFT OUTER JOIN (select administeredformid, dataenteredby dataenteredby2, lockdate lockdate2, dataentrydraftid ded2, username username2 from dataentrydraft, usr u where dataentryflag=2 and dataenteredby = u.usrid) temp2 ");
			sql.append("  ON temp1.administeredformid = temp2.administeredformid) tmp ");
			sql.append(" where tmp.administeredformid = af.administeredformid");
			sql.append(" and f.formid = af.formid ");
			sql.append(" and pat.patientid = af.patientid ");
			sql.append(" and pat.patientid = pp.patientid ");
			sql.append(" and pp.protocolid = ? ");
			sql.append(" and af.intervalid = i.intervalid ");
			sql.append(" and f.formid = ? ");
//			sql.append(afrc.getSiteIdSearch());
//			sql.append(afrc.getDeSummaryClause());
//			
//			if (afrc != null) {
//				sql.append(afrc.getSortString());
//			}
//
//			stmt = this.conn.prepareStatement(sql.toString());
//			stmt.setLong(2, afrc.getFormId());
//			stmt.setLong(1, afrc.getProtocolId());
			rs = stmt.executeQuery();
			
			AdministeredForm admForm;

			searchOptions.put("assign1", new TreeSet<String>());
			searchOptions.put("assign2", new TreeSet<String>());
			searchOptions.put("intervals", new TreeSet<String>());

			while (rs.next()) {
				admForm = this.rsToAdministeredFormInProgress(rs);
				searchOptions.get("intervals").add(rs.getString("intervalname"));
				admForm.setDataEnteredByName1(rs.getString("username1"));
				searchOptions.get("assign1").add(rs.getString("username1"));
				admForm.setLockDate(rs.getTimestamp("lockdate1"));
				admForm.setDataEnteredByName2(rs.getString("username2"));
				
				if (rs.getString("username2") != null) {
					admForm.setDataEntry2NotStarted(false);
					searchOptions.get("assign2").add(rs.getString("username2"));
				}
				
				if (rs.getTimestamp("lockdate2") != null) {
					admForm.setLock2Date(rs.getTimestamp("lockdate2"));
				}
				
				if (rs.getString("attachfiles").equals("true")) {
					admForm.getForm().setAttachFiles(true);
				}
				
				admForms.add(admForm);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get administered forms in progress: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
			sql.delete(0, sql.length());
		}
		
		return admForms;
	}

	// ****************added by yogi for IBIS MyCollections page
	/**
	 * method to get the patient previous collection data that will be shown in MyCollections page
	 * 
	 * @param afrc
	 * @param protocolId
	 * @param searchOptions
	 * @return adminForm List
	 * @throws CtdbException
	 */
		public List<AdministeredForm> myCollectionsListBySiteIds(int protocolId, int userId,Map<String,String> searchOptions, PaginationData pageData, String key,List<Integer> siteIds) throws CtdbException {
			PreparedStatement stmt = null;
			ResultSet rs = null;
			StringBuffer sql = new StringBuffer(200);
			Array siteIdsArr = null;
			
			try {

				sql.append(" select distinct af.administeredformid administeredformid,af.finallockby finallockby,af.finallockdate finallockdate,af.visitdate visitdate,"
						+ "to_char (af.visitdate , 'YYYY-MM-DD HH24:MI') as pVisitDate, to_char(af.scheduledvisitdate , 'YYYY-MM-DD HH24:MI') as sVisitDate, ef.eformid eformId,pat.version patientversion,pat.patientid patientid,pat.guid, " 
						+CtdbDao.getDecryptionFunc("pat.mrn") + " as mrn, ef.name formName,pp.subjectid, ");
						sql.append(" ef.shortname shortName, i.name as intervalname,ded.coll_status coll_status,ded.lockdate,ded.updateddate,"
								+ "ded.dataenteredby dataenteredby,u.lastname lastname,u.firstname firstname,u.username username,i.name as visitType,"
								+ "i.intervalid intervalid ");
						sql.append("  from administeredform af LEFT OUTER JOIN interval i ON (af.intervalid = i.intervalid)  "
								+ "JOIN  patient pat on pat.patientid=af.patientid JOIN patientprotocol pp ON pat.patientid=pp.patientid ");
						sql.append(" JOIN eform ef ON ef.eformid=af.eformid JOIN protocol p  ON p.protocolid=pp.protocolid "
								+ "JOIN dataentrydraft ded ON ded.administeredformid=af.administeredformid JOIN usr u ON ded.dataenteredby = u.usrid ");
						sql.append("   WHERE   pat.deleteflag = false and dataentryflag=1 ");
						sql.append(" and pp.protocolid =" + protocolId);
						
						if(siteIds != null) {
							Integer[] arrSiteIds = (Integer[]) siteIds.toArray(new Integer[siteIds.size()]);
							siteIdsArr = this.conn.createArrayOf("BIGINT", arrSiteIds);
							sql.append(" and pp.siteid = ANY (?)");
						}
						
						sql.append(" and ef.protocolid =" + protocolId);
						if(searchOptions.get(StrutsConstants.SUBJECT_COLLECTING_DATA)!=null&& !searchOptions.get(StrutsConstants.SUBJECT_COLLECTING_DATA).isEmpty()){
							sql.append(" and pat.patientid = " + searchOptions.get(StrutsConstants.SUBJECT_COLLECTING_DATA));
						}
						if((searchOptions.get("formId")!= null)){
						sql.append(" and ef.eformid = " + searchOptions.get("formId"));
						}
						
						if((searchOptions.get(CtdbConstants.DATA_COLLECTION_EFORMS_NAME)!= null)){
							sql.append(" and ef.name = " + "'" + searchOptions.get(CtdbConstants.DATA_COLLECTION_EFORMS_NAME) + "'");
						}
						
						
						if((searchOptions.get(CtdbConstants.SUBJECT_GUID_DISPLAY)!= null)){
						sql.append(" and pat.guid = " + "'" + searchOptions.get(CtdbConstants.SUBJECT_GUID_DISPLAY) + "'");
						}
						if((searchOptions.get(CtdbConstants.SUBJECT_VISIT_DATE)!= null)){
						sql.append(" and to_char (af.visitdate , 'YYYY-MM-DD') = " + "'" + searchOptions.get(CtdbConstants.SUBJECT_VISIT_DATE) + "'");
						//sql.append(" and to_char (af.visitdate , 'YYYY-MM-DD HH24:MI') < " + "'" + searchOptions.get("pVisitDate") +  "'");
						}	
						
						if((searchOptions.get(CtdbConstants.SUBJECT_INTERVAL_NAME)!= null)){
							if(!(searchOptions.get(CtdbConstants.SUBJECT_INTERVAL_NAME).equals("all"))) {
								sql.append(" and i.name = " + "'" + searchOptions.get(CtdbConstants.SUBJECT_INTERVAL_NAME) + "'");
							}
						}
						
						if((searchOptions.get(CtdbConstants.SEARCH_STATUS_MyCollection)!= null)){
							
							if(searchOptions.get(CtdbConstants.SEARCH_STATUS_MyCollection).equals("1")) {
								sql.append(" and ded.coll_status = '"+CtdbConstants.COLLECTION_STATUS_LOCKED+ "'");
							}else if(searchOptions.get(CtdbConstants.SEARCH_STATUS_MyCollection).equals("2")) {
								sql.append(" and ded.coll_status = '"+CtdbConstants.COLLECTION_STATUS_INPROGRESS+ "'");
							}else if(searchOptions.get(CtdbConstants.SEARCH_STATUS_MyCollection).equals("3")) {
								sql.append(" and ded.coll_status <> '"+CtdbConstants.COLLECTION_STATUS_LOCKED+ "' and ded.coll_status <>'"+CtdbConstants.COLLECTION_STATUS_INPROGRESS+ "'");
								
							}else {
								//Show all result don't filer anything on status
							}
							
						}
				
						//Search on Subject identifier
						if((searchOptions.get(CtdbConstants.SUBJECT_GUID_DISPLAY)!= null)){
							sql.append(" and pat.guid = " + "'" + searchOptions.get(CtdbConstants.SUBJECT_GUID_DISPLAY) + "'");
						}
						
						if((searchOptions.get(CtdbConstants.SUBJECT_MRN_DISPLAY)!= null)){
							
							sql.append(" and "+CtdbDao.getDecryptionFunc("pat.mrn")+" = " + "'" + searchOptions.get(CtdbConstants.SUBJECT_MRN_DISPLAY) + "'");
						}
						
						if((searchOptions.get(CtdbConstants.PATIENTID_DISPLAY)!= null)){
							sql.append(" and pp.subjectid = " + "'" + searchOptions.get(CtdbConstants.PATIENTID_DISPLAY) + "'");
						}
						if(key != null && !key.isEmpty()) {
			
							String[] SORT_COLUMNS = { "guid", "to_char (af.visitdate , 'YYYY-MM-DD HH24:MI')", "subjectid", "i.name", "ef.name", "ded.coll_status", "u.lastname", "u.firstname", CtdbDao.getDecryptionFunc("pat.mrn"), "to_char (af.finallockdate , 'YYYY-MM-DD HH24:MI')" };
							StringBuffer filterQueryBuffer = new StringBuffer(" AND (");
							
							for(String currentColumn:SORT_COLUMNS) {
								filterQueryBuffer.append(currentColumn).append(" ilike '%").append(key).append("%' OR ");
							}
							
							filterQueryBuffer = filterQueryBuffer.replace(filterQueryBuffer.length() - 4, filterQueryBuffer.length(), "");
							filterQueryBuffer.append(")");
							
							if(SORT_COLUMNS.length > 0) {
								sql.append(filterQueryBuffer.toString());
							}
						}
						
						if(pageData != null) {
							if(pageData.getAscending() != null) {
								sql.append(" ORDER BY ").append(pageData.getSort()).append(" ").append(pageData.getAscending()?"asc":"desc");
							}
							sql.append(" LIMIT ").append(pageData.getPageSize()).append(" OFFSET ").append((pageData.getPage() - 1) * pageData.getPageSize());
							
						}
			
				stmt = this.conn.prepareStatement(sql.toString());
				if(siteIds != null) {
					stmt.setArray(1, siteIdsArr);
				}

				rs = stmt.executeQuery();
				List<AdministeredForm> admForms = new ArrayList<AdministeredForm>();		
				AdministeredForm admForm;
				while (rs.next()) {
					//admForm = this.rsToAdministeredFormInProgress(rs);
					admForm = new AdministeredForm();
					if(Integer.valueOf(rs.getInt("administeredformid"))!=null){
						admForm.setId(rs.getInt("administeredformid"));
					}
					
					//set form id and form name
					Form form = new Form();
					if (Integer.valueOf(rs.getInt("eformId")) != null) {
						form.setId(rs.getInt("eformId"));
					}
					if (rs.getString("formName") != null) {
						form.setName(rs.getString("formName"));
					}
					if (rs.getString("shortName") != null) {
						form.setShortName(rs.getString("shortName"));
					}
					admForm.setForm(form);
							
					//Set subject related fields
					Patient patient = new Patient();
					if(Integer.valueOf(rs.getInt("patientid"))!=null){
						patient.setId(rs.getInt("patientid"));
					}
					
					if(Integer.valueOf(rs.getInt("patientversion"))!=null){
						patient.setVersion(new Version(rs.getInt("patientversion")));
					}
					admForm.setPatient(patient);
					
					if(rs.getString("subjectid")!=null){
						admForm.getPatient().setSubjectId(rs.getString("subjectid"));
					}
					
					if(rs.getString("mrn")!=null){
						admForm.getPatient().setMrn(rs.getString("mrn"));
					}
					if(rs.getString("lastname")!=null){
						admForm.getPatient().setLastName(rs.getString("lastname"));
					}
					if(rs.getString("firstname")!=null){
						admForm.getPatient().setFirstName(rs.getString("firstname"));
					}
					if(rs.getString("guid")!=null){
						admForm.getPatient().setGuid(rs.getString("guid"));
					}

					
					//set visitType related fields
					Interval interval = new Interval();
					if (rs.getInt("intervalid") == 0) {
						interval.setId(-1);
					} else {
						interval.setId(rs.getInt("intervalid"));
					}
					
					admForm.setInterval(interval);
					if(rs.getString("visitType") != null){
					admForm.getInterval().setName(rs.getString("visitType"));
					}

					int finalLockBy = rs.getInt("finallockby");
					if (finalLockBy != 0) {
						admForm.setFinalLockBy(finalLockBy);
					}
					Timestamp finalLockDate = rs.getTimestamp("finallockdate");
					if (finalLockDate != null) {
						admForm.setFinalLockDate(finalLockDate);
					}
					Timestamp visitDate = rs.getTimestamp("visitdate");
					if (visitDate != null) {
						admForm.setVisitDate(visitDate);
					}

					admForm.getForm().setAttachFiles(false);

					if (rs.getString("dataenteredby") != null) {
						admForm.setUserOneEntryId(rs.getInt("dataenteredby"));
					}

					admForm.setKeySingleDouble(CtdbConstants.SINGLE_ENTRY_FORM);

					if (rs.getString("username") != null) {
						admForm.setUser1(rs.getString("username"));
						if(rs.getString("dataenteredby").equals("-1")) {
							admForm.setUser1LastNameFirstNameDisplay(rs.getString("username"));
						}else {
							admForm.setUser1LastNameFirstNameDisplay(rs.getString("lastname") + ", " + rs.getString("firstname"));
						}
						
					}

					if (rs.getString("pVisitDate") != null) {
						admForm.setpVisitDate(rs.getString("pVisitDate"));
					}
					
					if (rs.getString("sVisitDate") != null) {
						admForm.setsVisitDate(rs.getString("sVisitDate"));
					} else {
						admForm.setsVisitDate("");
					}
					
					if (rs.getString("guid") != null) {
						admForm.setSummaryGuid(rs.getString("guid"));
					}
					
					if (rs.getString("coll_status") != null){
						admForm.setEntryOneStatus(rs.getString("coll_status"));
					}

					admForms.add(admForm);
				}
				
				return admForms;
			}
			catch (SQLException e) {
	         	// Check the sql state
	 			if ( e.getSQLState().contains("39000") ) {
	 				logger.error("Column decryption failed: " + e.getMessage(), e);
	 				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
	 			}
	 			else {
	 				logger.error("Unable to retrieve subjects for subject home: " + e.getMessage(), e);
	 				throw new CtdbException("Unable to retrieve subjects for subject home: " + e.getMessage(), e);
	 			}
	         }
	         finally {
	        	 this.close(rs);
	        	 this.close(stmt);
	         }
		}

	public int countMyCollectionsList(int protocolId, int userId,Map<String,String> searchOptions, String key) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer(200);

		try {
					sql.append("select count (distinct af.*) as total_count");					
					sql.append("  from administeredform af LEFT OUTER JOIN interval i ON (af.intervalid = i.intervalid)  "
							+ "JOIN  patient pat on pat.patientid=af.patientid JOIN patientprotocol pp ON pat.patientid=pp.patientid ");
					sql.append(" JOIN eform ef ON ef.eformid=af.eformid JOIN protocol p  ON p.protocolid=pp.protocolid "
							+ "JOIN dataentrydraft ded ON ded.administeredformid=af.administeredformid JOIN usr u ON ded.dataenteredby = u.usrid ");
					sql.append("   WHERE   pat.deleteflag = false and dataentryflag=1 ");
					sql.append(" and pp.protocolid =" + protocolId);
					sql.append(" and ef.protocolid =" + protocolId);
					if(searchOptions.get(StrutsConstants.SUBJECT_COLLECTING_DATA)!=null&& !searchOptions.get(StrutsConstants.SUBJECT_COLLECTING_DATA).isEmpty()){
						sql.append(" and pat.patientid = " + searchOptions.get(StrutsConstants.SUBJECT_COLLECTING_DATA));
					}
					if((searchOptions.get("formId")!= null)){
					sql.append(" and ef.eformid = " + searchOptions.get("formId"));
					}
					if((searchOptions.get(CtdbConstants.SUBJECT_GUID_DISPLAY)!= null)){
					sql.append(" and pat.guid = " + "'" + searchOptions.get(CtdbConstants.SUBJECT_GUID_DISPLAY) + "'");
					}
					if((searchOptions.get(CtdbConstants.SUBJECT_VISIT_DATE)!= null)){
					sql.append(" and to_char (af.visitdate , 'YYYY-MM-DD') = " + "'" + searchOptions.get(CtdbConstants.SUBJECT_VISIT_DATE) + "'");
					//sql.append(" and to_char (af.visitdate , 'YYYY-MM-DD HH24:MI') < " + "'" + searchOptions.get("pVisitDate") +  "'");
					}				
					
					
					if((searchOptions.get(CtdbConstants.SEARCH_STATUS_MyCollection)!= null)){
						
						if(searchOptions.get(CtdbConstants.SEARCH_STATUS_MyCollection).equals("1")) {
							sql.append(" and ded.coll_status = '"+CtdbConstants.COLLECTION_STATUS_LOCKED+ "'");
						}else if(searchOptions.get(CtdbConstants.SEARCH_STATUS_MyCollection).equals("2")) {
							sql.append(" and ded.coll_status = '"+CtdbConstants.COLLECTION_STATUS_INPROGRESS+ "'");
						}else if(searchOptions.get(CtdbConstants.SEARCH_STATUS_MyCollection).equals("3")) {
							sql.append(" and ded.coll_status <> '"+CtdbConstants.COLLECTION_STATUS_LOCKED+ "' and ded.coll_status <>'"+CtdbConstants.COLLECTION_STATUS_INPROGRESS+ "'");
							
						}else {
							//Show all result don't filer anything on status
						}
						
					}
					

					//Search on Subject identifier
					if((searchOptions.get(CtdbConstants.SUBJECT_GUID_DISPLAY)!= null)){
						sql.append(" and pat.guid = " + "'" + searchOptions.get(CtdbConstants.SUBJECT_GUID_DISPLAY) + "'");
					}
					
					if((searchOptions.get(CtdbConstants.SUBJECT_INTERVAL_NAME)!= null)){
						if(!(searchOptions.get(CtdbConstants.SUBJECT_INTERVAL_NAME).equals("all"))) {
							sql.append(" and i.name = " + "'" + searchOptions.get(CtdbConstants.SUBJECT_INTERVAL_NAME) + "'");
						}
					}
					
					if((searchOptions.get(CtdbConstants.DATA_COLLECTION_EFORMS_NAME)!= null)){
						sql.append(" and ef.name = " + "'" + searchOptions.get(CtdbConstants.DATA_COLLECTION_EFORMS_NAME) + "'");
					}
					
					if((searchOptions.get(CtdbConstants.SUBJECT_MRN_DISPLAY)!= null)){
						
						sql.append(" and "+CtdbDao.getDecryptionFunc("pat.mrn")+" = " + "'" + searchOptions.get(CtdbConstants.SUBJECT_MRN_DISPLAY) + "'");
					}
					
					if((searchOptions.get(CtdbConstants.PATIENTID_DISPLAY)!= null)){
						sql.append(" and pp.subjectid = " + "'" + searchOptions.get(CtdbConstants.PATIENTID_DISPLAY) + "'");
					}
					
					if(key != null && !key.isEmpty()) {
						String[] SORT_COLUMNS = { "guid", "to_char (af.visitdate , 'YYYY-MM-DD HH24:MI')", "subjectid", "i.name", "ef.name", "ded.coll_status", "u.lastname", "u.firstname", CtdbDao.getDecryptionFunc("pat.mrn"), "to_char (af.finallockdate , 'YYYY-MM-DD HH24:MI')" };
						StringBuffer filterQueryBuffer = new StringBuffer(" AND (");
						
						for(String currentColumn:SORT_COLUMNS) {
							filterQueryBuffer.append(currentColumn).append(" ilike '%").append(key).append("%' OR ");
						}
						
						filterQueryBuffer = filterQueryBuffer.replace(filterQueryBuffer.length() - 4, filterQueryBuffer.length(), "");
						filterQueryBuffer.append(")");
						
						if(SORT_COLUMNS.length > 0) {
							sql.append(filterQueryBuffer.toString());
						}
					}				
					
			stmt = this.conn.prepareStatement(sql.toString());

			rs = stmt.executeQuery();

			int count = 0;
			while (rs.next()) {
				count = rs.getInt("total_count");
			}
			
			return count;
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new CtdbException("Unable to get administered forms for My Collections page: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
			sql.delete(0, sql.length());
		}
	}
	
	/**
	 * gets the locked by and date info for a list of administered form domain
	 * objects.
	 * 
	 * @param list
	 *            the list of administered form domain object ids whose
	 *            lockedBy, locked2By, lockedDate, and locked2Date will be set.
	 * @return Map the hashMap of AdministeredForms whose data members have been
	 *         set as wanted.
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public Map<String, AdministeredForm> getLockedByAndDate(List<String> list)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select * from dataentrydraft a ");
			sql.append("where a.administeredformid in (");

			for (int i = 0; i < list.size(); i++) {
				if (i > 0)
					sql.append(",");
				sql.append(Integer.parseInt(list.get(i)));
			}
			sql.append(")");

			stmt = this.conn.prepareStatement(sql.toString());
			rs = stmt.executeQuery();

			Timestamp lockDate = null;
			Map<String, AdministeredForm> map = new HashMap<String, AdministeredForm>();
			while (rs.next()) {
				int admFormId = rs.getInt("administeredformid");

				AdministeredForm admForm = (AdministeredForm) map.get(Integer
						.toString(admFormId));
				if (admForm != null) {
					admForm.setDataEntry2NotStarted(false);
				} else {
					admForm = new AdministeredForm();
					admForm.setId(admFormId);
					map.put(Integer.toString(admFormId), admForm);
				}

				lockDate = rs.getTimestamp("lockdate");
				if (rs.getInt("dataentryflag") == 1) {
					admForm.setDataEntrySession(1);
					if (lockDate != null) {
						admForm.setLockDate(lockDate);
						admForm.setLockedBy(rs.getInt("dataenteredby"));
					}
				} else {
					admForm.setDataEntrySession(2);
					if (lockDate != null) {
						admForm.setLock2Date(lockDate);
						admForm.setLocked2By(rs.getInt("dataenteredby"));
					}
				}

			}
			return map;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get administered form data from the dataentrydraft table: "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * gets the locked by and date info for a given administered form domain
	 * object.
	 * 
	 * @param admForm
	 *            the administered form domain object where lockedBy, locked2By,
	 *            lockedDate, and locked2Date will be set.
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public void getLockedByAndDate(AdministeredForm admForm)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select * from dataentrydraft a ");
			sql.append("where a.administeredformid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admForm.getId());
			rs = stmt.executeQuery();
			int count = 0;
			Timestamp lockDate = null;
			while (rs.next()) {
				count++;
				lockDate = rs.getTimestamp("lockdate");
				if (rs.getInt("dataentryflag") == 1) {
					admForm.setDataEntrySession(1);
					if (lockDate != null) {
						admForm.setLockDate(lockDate);
						admForm.setLockedBy(rs.getInt("dataenteredby"));
					}
				} else {
					admForm.setDataEntrySession(2);
					if (lockDate != null) {
						admForm.setLock2Date(lockDate);
						admForm.setLocked2By(rs.getInt("dataenteredby"));
					}
				}
			}
			if (count == 2) {
				admForm.setDataEntry2NotStarted(false);
			}
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get administered form data from the dataentrydraft table: "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}


	public int getFormIdByResponse(Response response) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int formId = Integer.MIN_VALUE;
		try {

			StringBuffer sql = new StringBuffer(200);
			sql.append("select formid from administeredform where administeredformid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, response.getAdministeredForm().getId());
			rs = stmt.executeQuery();
			while (rs.next()) {
				formId = rs.getInt("formid");
			}
			return formId;

		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get form ID with AdministeredForm ID: "
							+ response.getAdministeredForm().getId());
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Deletes the response draft answers
	 * 
	 * @param response
	 *            the response draft object
	 * @throws CtdbException
	 *             thrown if any errors occur
	 */
	public void deleteDraftAnswers(Response response) throws CtdbException {
		PreparedStatement stmt = null;
		try {
			/*
			 * added by Ching Heng for File question type, we not only delete
			 * the DraftAnswer but also the attachment file
			 */
			//boolean isDraft = true;
			boolean isDraft = (response.getAdministeredForm().getFinalLockDate() == null); //set draft value by checking if form has been locked or not
			if (isFileQuestion(response)) {
				int attId = getAttachmentIdFromResponse(response.getId(), false);
				int formId = getFormIdByResponse(response);
				//AttachmentDao.getInstance(conn).deleteAttachment(attId);
				// delete real file in computer
				AttachmentManager am = new AttachmentManager();
				try {
					if (attId != Integer.MIN_VALUE) {
						am.deleteAttachment(attId, formId, AttachmentManager.FILE_COLLECTION);
					}
				} catch (ServerFileSystemException e) {
					logger.error("File system error.", e);
				}
			}
			// -------
			StringBuffer sql = new StringBuffer(200);
			//System.out.println("deleteDraftAnswers()->responseId"+ response.getId());
			sql.append("delete from patientresponsedraft ");
			sql.append("where responsedraftid = ?");
			stmt = this.conn.prepareStatement(sql.toString());

			stmt.setInt(1, response.getId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException(
					"Error occur while deleting the answers for the response draft "
							+ response.getId() + e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}
	
	public void deleteDraftAnswers(List<Response> responses) throws CtdbException {
		PreparedStatement stmt = null;
		try {
			
			StringBuffer sql = new StringBuffer(200);
			
			sql.append("delete from patientresponsedraft ");
			sql.append("where responsedraftid in (");
			
			int i = 0;
			for(Response response : responses){
				
				if (isFileQuestion(response)) {
					int attId = getAttachmentIdFromResponse(response.getId(), false);
					int formId = getFormIdByResponse(response);
					AttachmentManager am = new AttachmentManager();
					try {
						if (attId != Integer.MIN_VALUE) {
							am.deleteAttachment(attId, formId, AttachmentManager.FILE_COLLECTION);
						}
					} catch (ServerFileSystemException e) {
						logger.error("File system error.", e);
					}
				}
				

				sql.append(response.getId());
					
				if(!(i == (responses.size() -1))){
					sql.append(", ");
				}
				else{
					sql.append(")");
				}

				i++;
			}
			
			stmt = this.conn.prepareStatement(sql.toString());
			
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			throw new CtdbException(
					"Error occur while deleting the answers for the response draft "
							+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	
	
public void deleteVisibleAdministeredSection (int sectionId) throws CtdbException {
	PreparedStatement stmt = null;
	
	try {
		StringBuffer sql = new StringBuffer(200);
		//sql.append("delete from visibleadminsteredsection where sectionid = ? ");
		sql.append("delete from visibleadminsteredsection where dict_sectionid = ? ");
		stmt = this.conn.prepareStatement(sql.toString());
		stmt.setInt(1, sectionId);
		stmt.executeUpdate();
	} catch (SQLException e) {
		throw new CtdbException("Error occur while deleting visibleadministeredsection for section id : " + sectionId + e.getMessage(), e);
	} finally {
		this.close(stmt);
	}

		
		
	}
	
	
	/*
	 * created by Ching Heng for checking is the response belong to a File type
	 * question
	 */
	public boolean isFileQuestion(Response response)
			throws CtdbException {
		QuestionType type = response.getQuestion().getType();
		if(type == QuestionType.File) {
			return true;
		}else {
			return false;
		}
		
		

	}

	public int getAttachmentIdFromResponse(Response response, AdministeredForm adm)
			throws CtdbException {
		int attachId = Integer.MIN_VALUE;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String answer = "";
		try {
			StringBuffer sql = new StringBuffer(200);
			//boolean isLocked = adm.getLockDate() != null;
			//Lock Date will handle single entry but Final Lock date should handle both single and double entry
			boolean isLocked = adm.getFinalLockDate() != null;
			//boolean isPatientForm = (adm.getForm().getFormType() == 10 ) ? true : false;
			//if (isPatientForm){
				if (!isLocked){
					sql.append("select attachmentid from patientresponsedraft ");
					sql.append("where responsedraftid = ?");
					stmt = this.conn.prepareStatement(sql.toString());
					// stmt.setInt(1, response.getId());
					stmt.setLong(1, response.getId());
					rs = stmt.executeQuery();
					while (rs.next()) {
						answer = rs.getString("attachmentid");
					}
					if ((!answer.equals(""))) {
						attachId = Integer.parseInt(answer);
					}
				} else { // form is patient and it's not locked
					// if we can't find the attachment id from "patientresponsedraft"
					// table,
					// it may at "patientresponse" table...
					if (attachId == Integer.MIN_VALUE) {
						sql = new StringBuffer(200);
						sql.append("select attachmentid from patientresponse ");
						sql.append("where responseid = ?");
						stmt = this.conn.prepareStatement(sql.toString());
						// stmt.setInt(1, response.getId());
						stmt.setLong(1, response.getId());
						rs = stmt.executeQuery();
						while (rs.next()) {
							answer = rs.getString("attachmentid");
						}
						if ((!answer.equals(""))) {
							attachId = Integer.parseInt(answer);
						}
					}
				}
		} catch (SQLException e) {
			throw new CtdbException(
					"Error occur while getting the attachment ID "
							+ response.getId() + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		return attachId;
	}

	public String getAttachmentNameFromResponse(Response response, AdministeredForm adm)
			throws CtdbException {
		String attachName = "";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sql = new StringBuffer(200);
			//boolean isLocked = adm.getLockDate() != null;
			//Lock date will handle single entry but Final Lock date should handle double data entry as well
			boolean isLocked = adm.getFinalLockDate() != null;
			//boolean isPatientForm = (adm.getForm().getFormType() == 10 ) ? true : false;
			//if (isPatientForm){
				if (!isLocked){
					sql.append("select answer from patientresponsedraft  ");
					sql.append("where responsedraftid = ?");
					stmt = this.conn.prepareStatement(sql.toString());
					// stmt.setInt(1, response.getId());
					stmt.setLong(1, response.getId());
					rs = stmt.executeQuery();
					while (rs.next()) {
						attachName = rs.getString("answer");
					}
				} else { // must be patient form that's locked
					// if we can't find the attachment id from "patientresponsedraft"
					// table, it may at "patientresponse" table...
					if (attachName.equals("")) {
						sql = new StringBuffer(200);
						sql.append("select answer from patientresponse ");
						sql.append("where responseid = ?");
						stmt = this.conn.prepareStatement(sql.toString());
						// stmt.setInt(1, response.getId());
						stmt.setLong(1, response.getId());
						rs = stmt.executeQuery();
						while (rs.next()) {
							attachName = rs.getString("answer");
						}
					}
				}

		} catch (SQLException e) {
			throw new CtdbException(
					"Error occur while getting the attachment Name "
							+ response.getId() + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		return attachName;
	}

	/**
	 * Deletes the response final answers
	 * 
	 * @param response
	 *            the response object
	 * @throws CtdbException
	 *             thrown if any errors occur
	 */
	public void deleteResponseAnswers(Response response) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			if (isFileQuestion(response)) {
			
				int attId = getAttachmentIdFromResponse(response.getId(),true);
				int formId = getFormIdByResponse(response);
		
				AttachmentManager am = new AttachmentManager();
				try {
					if (attId != Integer.MIN_VALUE) {
						if(response.getAdministeredForm().getFinalLockDate() != null) {
							am.deleteAttachment(attId, formId, AttachmentManager.FILE_COLLECTION);
						}
						
					}
				} catch (ServerFileSystemException e) {
					logger.error("File system error.", e);
				}
			}
			// -------
			StringBuffer sql = new StringBuffer(200);
			sql.append("delete from patientresponse ");
			sql.append("where responseid = ?");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, response.getId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException(
					"Error occur while deleting the final answers for the response "
							+ response.getId() + e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Creates the response draft answers
	 * 
	 * @param response
	 *            the response draft object
	 * @throws DuplicateObjectException
	 *             thrown if the answer for the question already exists in the
	 *             system
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public void createResponseDraftAnswers(Response response,boolean usingEditAnswers) throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;

		try {
			List<String> answers;
			
			if(usingEditAnswers) {
				answers = response.getEditAnswers();
			}else {
				answers = response.getAnswers();
			}
			
			List<String> submitAnswers = response.getSubmitAnswers();
			StringBuffer sql = new StringBuffer(200);

			// delete the answers first
			this.deleteDraftAnswers(response);

			// create answers
			// added by Ching Heng for file type question
			boolean isDraft = true;
			sql = new StringBuffer(200);
			
			if (isFileQuestion(response)) {
				sql.append("insert into patientresponsedraft(patientresponsedraftid, responsedraftid, answer, attachmentid, submitanswer) ");
				sql.append("values(DEFAULT, ?, ?, ?, ?)");
			} else {
				sql.append("insert into patientresponsedraft(patientresponsedraftid, responsedraftid, answer, submitanswer) ");
				sql.append("values(DEFAULT, ?, ?, ?)");
			}
			
			stmt = this.conn.prepareStatement(sql.toString());
			
			String answer = null;
			String submitAnswer = null;
			Iterator<String> it2 = submitAnswers.iterator();

			for (Iterator<String> it = answers.iterator(); it.hasNext();) {
				answer = it.next();

				if (isFileQuestion(response)) {
					stmt.setLong(1, response.getId());
					String[] fileAnswer = answer.split(":"); // it would be
					stmt.setString(2, fileAnswer[0]);
					stmt.setInt(3, Integer.valueOf(fileAnswer[1]));
					stmt.setString(4, fileAnswer[0]);
				} else {
					stmt.setLong(1, response.getId());
					submitAnswer = it2.next();
					stmt.setString(2, answer);
					stmt.setString(3, submitAnswer);
				}

				stmt.executeUpdate();
			}
		}
		catch (PSQLException e) {
			if (e.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A response draft answer already exists: " + e.getMessage(), e);
			} else {
				throw new CtdbException(
						"Unable to create response draft answer: "
								+ e.getMessage(), e);
			}
		}
		catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A response draft answer already exists: " + e.getMessage(), e);
			} else {
				throw new CtdbException(
						"Unable to create response draft answer: " + e.getMessage(), e);
			}
		}
		finally {
			this.close(stmt);
		}
	}
	
	public void createResponsesDraftAnswers(List<Response> responses, boolean usingEditAnswers) throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;
		StringBuffer sql = new StringBuffer(200);

		try {
			
			this.deleteDraftAnswers(responses);
			
			sql.append("insert into patientresponsedraft(patientresponsedraftid, responsedraftid, answer, attachmentid, submitanswer) ");
			sql.append("values ");
			
			int i = 0;
			for(Response response : responses){
				int j = 0;
				for(String answer : response.getAnswers()){
					sql.append("(DEFAULT, ?, ?, ?, ?)");
					
					if(!(i == (responses.size() -1) && j == (response.getAnswers().size() -1))){
						sql.append(", ");
					}
					j++;
				}
				i++;
			}
			
			stmt = this.conn.prepareStatement(sql.toString());
			
			int index= 0;
			for(Response response : responses){
				List<String> answers = new ArrayList<String>();
				
				if(usingEditAnswers) {
					answers = response.getEditAnswers();
				}else {
					answers = response.getAnswers();
				}
				List<String> submitAnswers = response.getSubmitAnswers();
				//List<String> deComments = response.getDeComments();
				
				if (isFileQuestion(response)){
					
					for (String answer : answers) {
						String[] fileAnswer = answer.split(":"); // it would be fileNme:ID
						stmt.setInt(index+1, response.getId());
						stmt.setString(index+2, fileAnswer[0]);
						stmt.setInt(index+3, Integer.valueOf(fileAnswer[1]));
						stmt.setString(index+4, fileAnswer[0]);
						
						index += 4;
					}
				}
				
				else {
					
					String answer = null;
					String submitAnswer = null;
					//String deComment = null;
					Iterator<String> it2 = submitAnswers.iterator();
					//Iterator<String> it3 = deComments.iterator();
					
					for (Iterator<String> it = answers.iterator(); it.hasNext();) {
						answer = it.next();
						submitAnswer = it2.next();
//						if(deComments.size()>0) {
//						deComment = it3.next();
//						}else {
//							deComment = null;
//						}
						
						if (answer.length() > 0) {
							stmt.setInt(index+1, response.getId());
							stmt.setString(index+2, answer);
							stmt.setNull(index+3, Types.INTEGER);
							stmt.setString(index+4, submitAnswer);
							//stmt.setString(index+5, deComment);
						}
						
						//index += 5;
						index += 4;
					}
				}
			}
			
			stmt.executeUpdate();
			
		}
		catch (PSQLException e) {
			if (e.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A response draft answer already exists: " + e.getMessage(), e);
			} else {
				throw new CtdbException(
						"Unable to create response draft answer: "
								+ e.getMessage(), e);
			}
		}
		catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A response draft answer already exists: " + e.getMessage(), e);
			} else {
				throw new CtdbException(
						"Unable to create response draft answer: " + e.getMessage(), e);
			}
		}
		finally {
			this.close(stmt);
		}
	}

	/**
	 * Creates the response answers
	 * 
	 * @param response
	 *            the response object
	 * @throws DuplicateObjectException
	 *             thrown if the answer for the question already exists in the
	 *             system
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public void createResponseAnswers(Response response) throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;
		
		try {
			List<String> answers = response.getAnswers();
			List<String> submitAnswers = response.getSubmitAnswers();
			StringBuffer sql = new StringBuffer(200);

			// delete the answers first
			this.deleteResponseAnswers(response);

			// create answers
			// added by Ching Heng for file type question
			if (response.getQuestion().getType().getValue() == QuestionType.File.getValue()) {
				sql.append("insert into patientresponse(patientresponseid, responseid, answer, attachmentid, submitanswer) ");
				sql.append("values(DEFAULT, ?, ?, ?, ?)");
				stmt = this.conn.prepareStatement(sql.toString());
				stmt.setInt(1, response.getId());
				
				for (String answer : answers) {
					String[] fileAnswer = answer.split(":"); // it would be fileNme:ID
					stmt.setString(2, fileAnswer[0]);
					stmt.setInt(3, Integer.valueOf(fileAnswer[1]));
					stmt.setString(4, fileAnswer[0]);
					stmt.executeUpdate();
				}
			}
			else {
				sql.append("insert into patientresponse(patientresponseid, responseid, answer, submitanswer) ");
				sql.append("values(DEFAULT, ?, ?, ?)");
				stmt = this.conn.prepareStatement(sql.toString());
				stmt.setInt(1, response.getId());
				
				String answer = null;
				String submitAnswer = null;
				Iterator<String> it2 = submitAnswers.iterator();
				
				for (Iterator<String> it = answers.iterator(); it.hasNext();) {
					answer = it.next();
					submitAnswer = it2.next();
					
					if (answer.length() > 0) {
						stmt.setString(2, answer);
						stmt.setString(3, submitAnswer);
						stmt.executeUpdate();
					}
				}
			}
			
			stmt.close();

			// update comment
			String uptString = "update response set resolvecomment ='" + response.getComment() + "'" 
					+ " where responseid = ? ";
			
			stmt = this.conn.prepareStatement(uptString);
			stmt.setLong(1, response.getId());
			stmt.executeUpdate();
		}
		catch (PSQLException e) {
			if (e.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException("A response answer already exists: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to create response draft answer: " + e.getMessage(), e);
			}
		}
		catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException("A response answer already exists: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to create response answer: " + e.getMessage(), e);
			}
		}
		finally {
			this.close(stmt);
		}
	}
	
	/**
	 * Alternate createResponseAnswers method for locking performance optimization.
	 * Handles list of responses rather than single response object.
	 * 
	 * @param responses
	 * @throws DuplicateObjectException
	 * @throws CtdbException
	 */
	public void createResponseAnswers2(List<Response> responses) throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;
		PreparedStatement stmt2 = null;
		
		try {
			
			StringBuffer sql = new StringBuffer(200);
			StringBuffer sql2 = new StringBuffer(200);

			// create answers
			// added by Ching Heng for file type question
			sql.append("insert into patientresponse(patientresponseid, responseid, answer, attachmentid, submitanswer) ");
			sql.append("values (DEFAULT, ?, ?, ?, ?) ");
			
			sql2.append("delete from patientresponse ");
			sql2.append("where responseid in (");
			
			
			
			for(Iterator<Response> itr=responses.iterator();itr.hasNext();){
				Response response = itr.next() ;
				sql2.append(response.getId());
				if(itr.hasNext()){
					sql2.append(", ");
				}else{
					sql2.append(")");
				}
				
		
				
			}
			
			
			
		
			
			stmt = this.conn.prepareStatement(sql.toString());
			stmt2 = this.conn.prepareStatement(sql2.toString());
			
			
			
			for(Response response : responses){
				List<String> answers = response.getAnswers();
				List<String> submitAnswers = response.getSubmitAnswers();
				
				//XXX will need to aggregate this as well
//				this.deleteResponseAnswers(response);
				
				
				if (response.getQuestion().getType().getValue() == QuestionType.File.getValue()){
					
					for (String answer : answers) {
						String[] fileAnswer = answer.split(":"); // it would be fileNme:ID
						stmt.setInt(1, response.getId());
						stmt.setString(2, fileAnswer[0]);
						stmt.setInt(3, Integer.valueOf(fileAnswer[1]));
						stmt.setString(4, fileAnswer[0]);
						
						
						stmt.addBatch();
					}
				}
				
				else {
					
					String answer = null;
					String submitAnswer = null;
					Iterator<String> it2 = submitAnswers.iterator();
					
					for (Iterator<String> it = answers.iterator(); it.hasNext();) {
						answer = it.next();
						submitAnswer = it2.next();
						
						if (answer.length() > 0) {
							stmt.setInt(1, response.getId());
							stmt.setString(2, answer);
							stmt.setNull(3, Types.INTEGER);
							stmt.setString(4, submitAnswer);
							stmt.addBatch();
						}
						
						
					}
				}
			}
			
			stmt2.executeUpdate();
			stmt.executeBatch();
			//stmt.close();

		}
		catch (PSQLException e) {
			if (e.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException("A response answer already exists: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to create response draft answer: " + e.getMessage(), e);
			}
		}
		catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException("A response answer already exists: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to create response answer: " + e.getMessage(), e);
			}
		}
		finally {
			this.close(stmt);
			this.close(stmt2);
		}
	}

	/**
	 * Creates the response answers using edit answers
	 * 
	 * @param response
	 *            the response object
	 * @param usingEditAnswers
	 *            true for using edit answers
	 * @throws DuplicateObjectException
	 *             thrown if the answer for the question already exists in the
	 *             system
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public void createResponseAnswers(Response response, boolean usingEditAnswers) 
			throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;

		try {
			List<String> answers = response.getEditAnswers();
			List<String> submitAnswers = response.getSubmitAnswers();
			StringBuffer sql = new StringBuffer(200);

			// delete the answers first
			this.deleteResponseAnswers(response);

			// create answers
			// added by Ching Heng for file type question
			boolean isDraft = false;
			if (isFileQuestion(response)) {
				sql.append("insert into patientresponse(patientresponseid, responseid, answer, attachmentid, submitanswer) ");
				sql.append("values(DEFAULT, ?, ?, ?, ?)");
				stmt = this.conn.prepareStatement(sql.toString());
				stmt.setLong(1, response.getId());
				
				for (String answer : answers) {
					String[] fileAnswer = answer.split(":"); // it would be fileNme:ID
					stmt.setString(2, fileAnswer[0]);
					stmt.setInt(3, Integer.valueOf(fileAnswer[1]));
					stmt.setString(4, fileAnswer[0]);
					stmt.executeUpdate();
				}
			}
			else {
				sql.append("insert into patientresponse(patientresponseid, responseid, answer, submitanswer) ");
				sql.append("values(DEFAULT, ?, ?, ?)");
				stmt = this.conn.prepareStatement(sql.toString());
				stmt.setLong(1, response.getId());
				
				String answer = null;
				String submitAnswer = null;
				Iterator<String> it2 = submitAnswers.iterator();
				
				for (Iterator<String> it = answers.iterator(); it.hasNext();) {
					answer = it.next();
					submitAnswer = it2.next();
					
					if (answer.length() > 0) {
						stmt.setString(2, answer);
						stmt.setString(3, submitAnswer);
						stmt.executeUpdate();
					}
				}
			}
		}
		catch (PSQLException e) {
			if (e.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException("A response answer already exists: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to create response answer: " + e.getMessage(), e);
			}
		}
		catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException("A response answer already exists: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to create response answer: " + e.getMessage(), e);
			}
		}
		finally {
			this.close(stmt);
		}
	}

	/**
	 * Creates the edit answers.
	 * 
	 * @param response
	 *            the response object
	 * @throws DuplicateObjectException
	 *             thrown if the answer for the question already exists in the
	 *             system
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public void createEditAnswersArchive(int admFormId, Response response, boolean useFinalAnswers)
			throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;

		try {
			int responseId = response.getId();
			List<String> previousAnswers;
			if(useFinalAnswers) {
				if(response.getQuestion().isDisplayPV()) {
					previousAnswers = this.getFinalSubmitAnswers(responseId);
				}else {
					previousAnswers = this.getFinalAnswers(responseId);
				}
			} else {
				if(response.getQuestion().isDisplayPV()) {
					previousAnswers = this.getDraftCompletedSubmitAnswers(responseId);
				} else {
					previousAnswers = this.getDraftCompletedAnswers(responseId);
				}
			}
			
			int version = this.getLastAnswerVersion(admFormId,response.getQuestion());

			StringBuffer sql = new StringBuffer(200);

			// create answers archive
			sql = new StringBuffer(200);
			sql.append("insert into RESPONSEEDIT (administeredformid, dict_questionid, questionversion, responseeditversion, ");
			sql.append("previousanswer, editreason, updateddate, updatedby, dict_sectionId,coll_status) ");
			sql.append("values (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?) ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admFormId);
			stmt.setLong(2, response.getQuestion().getId());
			stmt.setInt(3, response.getQuestion().getVersion().getVersionNumber());
			stmt.setInt(4, ++version);
			stmt.setString(6, response.getEditReason());
			stmt.setLong(7, response.getEditedBy());
			stmt.setLong(8, response.getQuestion().getSectionId());
			if(useFinalAnswers) {
				stmt.setString(9, CtdbConstants.COLLECTION_STATUS_LOCKED);
			}else {
				stmt.setString(9, CtdbConstants.COLLECTION_STATUS_COMPLETED);
			}

			if (!previousAnswers.isEmpty()) {
				for (String previousAnswer : previousAnswers) {
					stmt.setString(5, previousAnswer);
					stmt.executeUpdate();
				}
			}
			else {
				stmt.setString(5, null);
				stmt.executeUpdate();
			}
		}
		catch (PSQLException e) {
			if (e.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException("An answer already exists: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to create answer archive: " + e.getMessage(), e);
			}
		}
		catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException("An answer already exists: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to create answer archive: " + e.getMessage(), e);
			}
		}
		finally {
			this.close(stmt);
		}
	}
	
	/* Audit Comment */
	public void createAuditCommentArchive(int admFormId, Response response, boolean useFinalAnswers, String inprog)
			throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;

		try {
			int responseId = response.getId();
			List<String> previousAnswers;
			if(useFinalAnswers) {
				if(response.getQuestion().isDisplayPV()) {
					previousAnswers = this.getFinalSubmitAnswers(responseId);
				}else {
					previousAnswers = this.getFinalAnswers(responseId);
				}
			} else {
				if(response.getQuestion().isDisplayPV()) {
					previousAnswers = this.getDraftCompletedSubmitAnswers(responseId);
				} else {
					previousAnswers = this.getDraftCompletedAnswers(responseId);
				}
			}
			
			int version = this.getLastAnswerVersion(admFormId,response.getQuestion());

			StringBuffer sql = new StringBuffer(200);

			// create answers archive
			sql = new StringBuffer(200);
			sql.append("insert into RESPONSEEDIT (administeredformid, dict_questionid, questionversion, responseeditversion, ");
			sql.append("previousanswer, editauditcomment, auditstatus, updateddate, updatedby, dict_sectionId, in_progress_data_flag ) ");
			sql.append("values (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?) ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admFormId);
			stmt.setLong(2, response.getQuestion().getId());
			stmt.setInt(3, response.getQuestion().getVersion().getVersionNumber());
			stmt.setInt(4, ++version);
			stmt.setString(6, response.getEditAuditComment());
			stmt.setString(7, response.getAuditStatus());
			stmt.setLong(8, response.getEditedBy());
			stmt.setLong(9, response.getQuestion().getSectionId());
			stmt.setString(10, inprog);

			if (!previousAnswers.isEmpty()) {
				for (String previousAnswer : previousAnswers) {
					stmt.setString(5, previousAnswer);
					stmt.executeUpdate();
				}
			}
			else {
				stmt.setString(5, null);
				stmt.executeUpdate();
			}
		}
		catch (PSQLException e) {
			if (e.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException("An audit comment already exists: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to create audit comment archive: " + e.getMessage(), e);
			}
		}
		catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException("An audit comment already exists: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to create audit comment archive: " + e.getMessage(), e);
			}
		}
		finally {
			this.close(stmt);
		}
	}
	
	/**
	 * Creates the audit records in RESPONSEEDIT.
	 * 
	 * @param response
	 *            the response object
	 * @throws DuplicateObjectException
	 *             thrown if the answer for the question already exists in the
	 *             system
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public void createEditAnswersArchiveInProgress(int admFormId, Response response, boolean useFinalAnswers)
			throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;

		try {
			int responseId = response.getId();
			List<String> previousAnswers;
			
			if(useFinalAnswers) {
				if(response.getQuestion().isDisplayPV()) {
					previousAnswers = this.getFinalSubmitAnswers(responseId);
				}else {
					previousAnswers = this.getFinalAnswers(responseId);
				}
			} else {
				if(response.getQuestion().isDisplayPV()) {
					previousAnswers = this.getDraftCompletedSubmitAnswers(responseId);
				} else {
					previousAnswers = this.getDraftCompletedAnswers(responseId);
				}
			}
			
			int version = this.getLastAnswerVersion(admFormId, response.getQuestion());

			// create answers archive
			String sql = "insert into RESPONSEEDIT (administeredformid, dict_questionid, questionversion, "
					+ "responseeditversion, previousanswer, editreason, updateddate, updatedby, "
					+ "dict_sectionId, in_progress_data_flag ) "
					+ "values (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?) ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, admFormId);
			stmt.setLong(2, response.getQuestion().getId());
			stmt.setInt(3, response.getQuestion().getVersion().getVersionNumber());
			stmt.setInt(4, ++version);
			stmt.setString(6, response.getEditReason());
			stmt.setLong(7, response.getEditedBy());
			stmt.setLong(8, response.getQuestion().getSectionId());
			stmt.setString(9, CtdbConstants.YES);

			if (!previousAnswers.isEmpty()) {
				for (String previousAnswer : previousAnswers) {
					stmt.setString(5, previousAnswer);
					stmt.addBatch();
				}

				stmt.executeBatch();
			}
			else {
				stmt.setNull(5, Types.VARCHAR);
				stmt.executeUpdate();
			}
		}
		catch (PSQLException e) {
			if (e.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException("An answer already exists.", e);
			}
			else {
				throw new CtdbException("Unable to create answer archive.", e);
			}
		}
		catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException("An answer already exists.", e);
			}
			else {
				throw new CtdbException("Unable to create answer archive.", e);
			}
		}
		finally {
			this.close(stmt);
		}
	}
	
	public List<String> getFinalAnswers(int responseId) throws CtdbException {
		List<String> answers = new ArrayList<String>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("select answer from patientresponse ");
			sql.append("where responseid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, responseId);
			stmt.executeQuery();

			rs = stmt.executeQuery();
			
			while (rs.next()) {
				answers.add(rs.getString("answer"));
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get the response final answers: " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return answers;
	}
	
	public List<String> getDraftCompletedAnswers(int responseDraftId) throws CtdbException {
		List<String> answers = new ArrayList<String>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("select answer from patientresponsedraft ");
			sql.append("where responsedraftid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, responseDraftId);
			stmt.executeQuery();

			rs = stmt.executeQuery();
			
			while (rs.next()) {
				answers.add(rs.getString("answer"));
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get the response final answers: " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return answers;
	}
	
	
	public String getIntervalName(int intervalId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String intervalName = "";
		try {

			StringBuffer sql = new StringBuffer(200);
			sql.append("select name from Interval where intervalid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, intervalId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				intervalName = rs.getString("name");
			}
			return intervalName;

		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get interval name");
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		
	}
	
	public Map<String, Map<String, List<Integer>>> getPatientIdLockedAformIdsHashMap(int protocolId) throws CtdbException {
		Map<String, Map<String, List<Integer>>> outerHashMap = new HashMap<String, Map<String, List<Integer>>>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Integer aformid = null;
		Integer intervalid = null;
		String intervalName = "Other";
		Integer patientid = null;
		String guid = "";
		Map<String, List<Integer>> innerHashMap = null;
		List<Integer> aformidList = null;
		String patientKey = "";
		
		try {
			String sql = "select af.administeredformid,af.intervalid, af.patientid, p.guid from administeredform af, form " +
					"f, Patient p where f.protocolid = ? and f.formid = af.formid and af.patientid = p.patientid and " +
					"af.finallockdate is not null and af.xsubmissionstatusid <> 2 and af.xsubmissionstatusid <> 4";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				aformid = rs.getInt("administeredformid");
				intervalid = rs.getInt("intervalid");
				
				if (intervalid != 0) {
					intervalName = getIntervalName(intervalid.intValue());
				}
				else {
					intervalName = "Other";
				}

				patientid = rs.getInt("patientid");
				guid = rs.getString("guid");
				
				if (guid != null) {
					patientKey = guid;
				}
				else {
					patientKey = String.valueOf(patientid);
				}

				if (outerHashMap.get(patientKey) == null) {
					innerHashMap = new HashMap<String, List<Integer>>();
					aformidList = new ArrayList<Integer>();
					aformidList.add(aformid);
					innerHashMap.put(intervalName, aformidList);
					outerHashMap.put(patientKey, innerHashMap);		
				}
				else {
					innerHashMap = outerHashMap.get(patientKey);
					
					if (innerHashMap.get(intervalName) == null) {
						aformidList = new ArrayList<Integer>();
						aformidList.add(aformid);
						innerHashMap.put(intervalName, aformidList);
					}
					else {
						aformidList = innerHashMap.get(intervalName);
						aformidList.add(aformid);	
					}
				}	
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get the last response final answer version.", e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return outerHashMap;
	}

	private int getLastAnswerVersion(int admFormId, Question q) throws CtdbException {
		int version = 0;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "select max(responseeditversion) from responseedit where administeredformid = ? "
					+ " and dict_questionid = ?  and questionversion = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, admFormId);
			stmt.setLong(2, q.getId());
			stmt.setInt(3, q.getVersion().getVersionNumber());
			stmt.executeQuery();

			rs = stmt.executeQuery();

			if (rs.next()) {
				version = rs.getInt(1);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get the last response final answer version.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return version;
	}

	public List<String> getLatestAnswerInProgress(AdministeredForm admForm, Response response) throws CtdbException {
		List<String> latestAnswerInProgress = new ArrayList<String>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "select answer from patientresponsedraft " +
					"where responsedraftid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, response.getId());
			stmt.executeQuery();
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				latestAnswerInProgress.add(rs.getString("answer"));
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get the response latest answers in progress status.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		return latestAnswerInProgress;
	}

	/**
	 * Deletes the response draft answers
	 * 
	 * @param response
	 *            the response draft object
	 * @throws CtdbException
	 *             thrown if any errors occur
	 */
	public void deletePreviousAuditLogInProgress(AdministeredForm admForm, Response response) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "delete from responseedit where administeredformid = ? " +
					"and questionversion = ? and dict_sectionid = ? and " +
					"dict_questionid = ? and in_progress_data_flag = ? ";

			stmt = this.conn.prepareStatement(sql);

			stmt.setLong(1, admForm.getId());
			stmt.setLong(2, response.getQuestion().getVersion().getVersionNumber() );
			stmt.setLong(3, response.getQuestion().getSectionId());
			stmt.setLong(4, response.getQuestion().getId());
			stmt.setString(5, CtdbConstants.YES);			

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException(
					"Error occur while deleting the in progress records from administeredformid: "
							+ response.getId(), e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Updates the editby and editdate information for edit answers
	 * 
	 * @param response
	 *            the Response object to update
	 * @param user
	 *            the User object contains the user id who edits the answers
	 * @throws ObjectNotFoundException
	 *             is thrown if the Response does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public void updateResponseEdit(Response response, User user)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("update response set editedby = ?, editeddate = CURRENT_TIMESTAMP, editreason = ? ");
			sql.append("where responseid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, user.getId());
			stmt.setString(2, response.getEditReason());
			stmt.setLong(3, response.getId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to update the response.", e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Gets the response draft for the administered form by data entry flag
	 * 
	 * @param admForm
	 *            the AdministeredForm object
	 * @param dataEntryFlag
	 *            1: for the first data entry; 2: for the second data entry
	 * @return List the list of responses draft
	 * @throws ObjectNotFoundException
	 *             thrown if response draft does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur
	 */
	public List getResponsesDraft(AdministeredForm admForm, int dataEntryFlag)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(200);

			// get the first data entry response info
			sql.append("select a.*, q.* ");
			sql.append("from responsedraft a, administeredform b, dataentrydraft c, question q ");
			sql.append("where b.formid = ? "); // 1
			sql.append("and b.patientid = ? "); // 2
			sql.append("and b.patientversion = ? ");// 3
			sql.append("and b.administeredformid = c.administeredformid ");
			sql.append("and c.dataentryflag = ? ");// 4
			sql.append("and c.dataentrydraftid = a.dataentrydraftid ");
			sql.append("and a.questionid = q.questionid ");
			if (admForm.getVisitDate() == null) {
				sql.append(" and visitdate is null ");
			} else {
				sql.append("and b.visitdate = ? "); // 5
			}

			if (admForm.getInterval().getId() == -1) {
				sql.append(" and b.intervalid is null ");
			} else {
				sql.append("and b.intervalid = ? "); // 5 or 6
			}

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admForm.getForm().getId());
			stmt.setLong(2, admForm.getPatient().getId());
			stmt.setInt(3, admForm.getPatient().getVersion().getVersionNumber());
			stmt.setInt(4, dataEntryFlag);
			int num = 5;
			if (admForm.getVisitDate() != null) {
				stmt.setTimestamp(num, new Timestamp(admForm.getVisitDate()
						.getTime()));
				num++;
			}
			if (admForm.getInterval().getId() != -1) {
				stmt.setLong(num, admForm.getInterval().getId());
			}

			rs = stmt.executeQuery();

			List<Response> responses = new ArrayList<Response>();
			Response response;
			while (rs.next()) {
				response = this.rsToResponseDraft(rs);
				List<String> submitAnswers = new ArrayList<String>();
				//List<String> deComments = new ArrayList<String>();
				//List<String> answers = this.getAnswersDraft(response, response.getId(),submitAnswers,deComments);
				List<String> answers = this.getAnswersDraft(response, response.getId(),submitAnswers);
				response.setAnswers(answers);
				response.setSubmitAnswers(submitAnswers);
				//response.setDeComments(deComments);
				responses.add(response);
			}
			return responses;
		} catch (SQLException e) {
			throw new CtdbException("Unable to update form status with ID "
					+ admForm.getForm().getId() + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves the administered form's certified information based on the
	 * administered ID.
	 * 
	 * @param administeredFormId
	 *            The administered form ID for retrieving the AdministeredForm
	 * @return AdministeredForm domain object
	 * @throws ObjectNotFoundException
	 *             Thrown if the administered form does not exist
	 * @throws CtdbException
	 *             Thrown if any other errors occur while processing
	 */
	public AdministeredForm getCertifiedInfo(int administeredFormId)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {

			StringBuffer sql = new StringBuffer(50);
			sql.append("select c.username certifiedbyusername, a.certifieddate ");
			sql.append("from administeredform a, usr c ");
			sql.append("where administeredformid = ? ");
			sql.append("and a.certifiedby = c.usrid ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredFormId);

			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new ObjectNotFoundException(
						"The administered form with ID: " + administeredFormId
								+ " could not be found.");
			}

			AdministeredForm admForm = new AdministeredForm();
			admForm.setCertifiedByUsername(rs.getString("certifiedbyusername"));
			admForm.setCertifiedDate(rs.getTimestamp("certifieddate"));

			return admForm;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get administered form with ID "
					+ administeredFormId + "  : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves the administered form's final locked information based on the
	 * administered ID.
	 * 
	 * @param administeredFormId
	 *            The administered form ID for retrieving the AdministeredForm
	 * @return AdministeredForm domain object
	 * @throws ObjectNotFoundException
	 *             Thrown if the administered form does not exist
	 * @throws CtdbException
	 *             Thrown if any other errors occur while processing
	 */
	public AdministeredForm getFinalLockedInfo(int administeredFormId)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {

			StringBuffer sql = new StringBuffer(50);
			sql.append("select c.firstname, c.lastname, a.finallockdate ");
			sql.append("from administeredform a, usr c ");
			sql.append("where administeredformid = ? ");
			sql.append("and a.finallockby = c.usrid ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredFormId);

			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new ObjectNotFoundException(
						"The administered form with ID: " + administeredFormId
								+ " could not be found.");
			}

			AdministeredForm admForm = new AdministeredForm();
			String finalLockByUsername = rs.getString("lastname")+", "+rs.getString("firstname");
			admForm.setFinalLockByUsername(finalLockByUsername);
			admForm.setFinalLockDate(rs.getTimestamp("finallockdate"));

			return admForm;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get administered form with ID "
					+ administeredFormId + "  : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves the administered form based on the administered ID only.
	 * 
	 * @param administeredFormId
	 *            the administered form ID for retrieving the AdministeredForm
	 * @return AdministeredForm domain objects
	 * @throws ObjectNotFoundException
	 *             thrown if the administered form does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public   AdministeredForm getAdministeredForm(int administeredFormId)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {

			StringBuffer sql = new StringBuffer(50);
			sql.append("select * from administeredform ");
			sql.append("where administeredformid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredFormId);

			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new ObjectNotFoundException(
						"The administered form with ID: " + administeredFormId
								+ " could not be found.");
			}

			AdministeredForm admForm = this.rsToAdministeredForm(rs);

			sql.delete(0, sql.length());
			stmt.close();

			sql.append("select max(dataentryflag) entryflag from dataentrydraft b ");
			sql.append("where b.administeredformid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredFormId);

			rs = stmt.executeQuery();

			if (!rs.next())
				admForm.setDataEntrySession(Integer.MIN_VALUE);
			else {
				int entryFlag = rs.getInt("entryflag");
				admForm.setDataEntrySession(entryFlag);
				if (entryFlag == 1) {
					admForm.setDataEntry2NotStarted(true);
				} else {
					admForm.setDataEntry2NotStarted(false);
				}
			}

			return admForm;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get administered form with ID "
					+ administeredFormId + "  : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves the administered form based on the administered ID and user
	 * object.
	 * 
	 * @param administeredFormId
	 *            the administered form ID for retrieving the AdministeredForm
	 * @param user
	 *            the user object
	 * @return AdministeredForm domain object
	 * @throws ObjectNotFoundException
	 *             thrown if the administered form does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public AdministeredForm getAdministeredForm(int administeredFormId, User user) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		AdministeredForm admForm = null;
		
		try {
			StringBuffer sql = new StringBuffer(100);
			sql.append("select b.coll_status status,a.*,b.* from administeredform a, dataentrydraft b ");
			sql.append("where a.administeredformid = b.administeredformid ");
			sql.append("and a.administeredformid = ? ");
			sql.append("and b.dataenteredby = ? ");
			
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredFormId);
			stmt.setLong(2, user.getId());
			rs = stmt.executeQuery();
			
			if ( !rs.next() ) {
				throw new ObjectNotFoundException("Either  administered form with ID: " + administeredFormId + " could not be found or userId: "+user.getId()+"  not found.");
			}
			
			admForm = this.rsToAdministeredForm(rs);
			admForm.setDataEntryDraftId(rs.getInt("dataentrydraftid"));
			admForm.setCreatedBy(rs.getInt("dataenteredby"));
			admForm.setCreatedDate(rs.getTimestamp("createddate"));
			admForm.setDataEntrySession(rs.getInt("dataentryflag"));
			if (admForm.getDataEntrySession() == 1) {
				admForm.setEntryOneStatus(rs.getString("status"));
			}
			else {
				admForm.setEntryTwoStatus(rs.getString("status"));
			}
			
			//To set status on admin object 
			String status = rs.getString("status");
			
			if( !status.isEmpty()&& (status!=null) &&(status.equalsIgnoreCase(CtdbConstants.DATACOLLECTION_STATUS_COMPLETED) 
					|| status.equalsIgnoreCase(CtdbConstants.DATACOLLECTION_STATUS_LOCKED) 
					|| status.equalsIgnoreCase(CtdbConstants.DATACOLLECTION_STATUS_FINALLOCKED)) ) {
				admForm.setMarkAsCompleted(true);
			}
			
			Timestamp lockDate = rs.getTimestamp("lockdate");

				if (lockDate != null) {
					admForm.setLockDate(lockDate);
					admForm.setResponses(this.getResponsesLocked(admForm.getId()));
				}
				else {
					admForm.setResponses(this.getResponsesDraft(admForm.getDataEntryDraftId()));
				}

			sql.delete(0, sql.length());
			stmt.close();

			sql.append("select max(dataentryflag) entryflag from dataentrydraft b ");
			sql.append("where b.administeredformid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredFormId);

			rs = stmt.executeQuery();

			if (!rs.next())
				admForm.setDataEntrySession(Integer.MIN_VALUE);
			else {
				int entryFlag = rs.getInt("entryflag");
				if (entryFlag == 1) {
					admForm.setDataEntry2NotStarted(true);
				} else {
					admForm.setDataEntry2NotStarted(false);
				}
			}
			
			//We need to set lockdate 1 and lockdate2 based on first data entry or second data entry added by yogi						
			int user1Id = this.getDataEntryUser(administeredFormId, 1);
			admForm.setLockDate(this.getDataEntryLockDate(administeredFormId, user1Id));
		
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get administered form with ID " + administeredFormId + " and user ID " + user.getId()
				+ "  : " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return admForm;
	}
	
	/**
	 * Gets the number of collections for a given eform shortname and visit type id
	 * 
	 * @param eformShortName
	 * @param visistTypeId
	 * @return
	 * @throws CtdbException
	 */
	public int getNumAdminFormsForEformAndVisitType(String eformShortName, int visistTypeId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select count(af.*) from administeredform af, eform ef where ");
			sql.append("af.eformid = ef.eformid and ");
			sql.append("ef.shortname = ? and ");
			sql.append("af.intervalid = ?");
			stmt = this.conn.prepareStatement(sql.toString());

			stmt.setString(1, eformShortName);
			stmt.setLong(2, visistTypeId);

			rs = stmt.executeQuery();
			int count = 0;
			if (rs.next()) {
				count = rs.getInt(1);
			}
			return count;

		} catch (SQLException e) {
			throw new CtdbException("Unable to get count of administered forms: " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}


	public int getAdministeredForm(int formId, int intervalId,
			String subjectid) throws ObjectNotFoundException,
			CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);			
			sql.append("select a.administeredformid from administeredform a, patient b ");
			sql.append("where a.intervalid = ? ");
			sql.append("and a.eformid = ? ");
			sql.append("and a.patientid = b.patientid ");
			sql.append("and b.subjectid = "
					+ CtdbDao.getDecryptionFunc(subjectid) + " ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, intervalId);
			stmt.setLong(2, formId);

			rs = stmt.executeQuery();

			if (!rs.next()) {
				return Integer.MIN_VALUE;
			}

			return rs.getInt(1);
		} catch (SQLException e) {
			throw new CtdbException("Unable to get administered form: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public int getFormVersion(int formId) throws ObjectNotFoundException,
			CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select version from form ");
			sql.append("where formid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);

			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new ObjectNotFoundException(
						"Form not found in the system");
			}

			return rs.getInt(1);
		} catch (SQLException e) {
			throw new CtdbException("Unable to get administered form: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	public int getFormId(int aformId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int formId = Integer.MIN_VALUE;
		try {

			StringBuffer sql = new StringBuffer(200);			
			sql.append("select eformid from administeredform where administeredformid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, aformId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				formId = rs.getInt("eformid");
			}
			return formId;

		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get form ID with AdministeredForm ID: "
							+ aformId);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		
	}
	
	
	
	


	public int[] getPatientData(String subjectidEncrypted)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select patientid, version from patient ");
			sql.append("where subjectid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setString(1, subjectidEncrypted);

			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new ObjectNotFoundException(
						"Patient not found in the system");
			}

			int[] patients = new int[2];
			patients[0] = rs.getInt("patientId");
			patients[1] = rs.getInt("version");

			return patients;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get administered form: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves the administered form based on the administered ID and user
	 * object. It contains the list of final answers and edit answers for the
	 * administered form. It also contains the list of answers for the questions
	 * for data entry 1 and data entry 2.
	 * 
	 * @param administeredFormId
	 *            the administered form ID for retrieving the AdministeredForm
	 * @param onlyDiscrepancy
	 *            the flag to indicate if it gets all questions or only
	 *            questions are discrepancy true gets only discrepancy
	 *            questions; false gets all questions.
	 * @return AdministeredForm domain object
	 * @throws ObjectNotFoundException
	 *             thrown if the administered form does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public AdministeredForm getAdministeredForm(int administeredFormId,boolean onlyDiscrepancy) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append(" select ded.coll_status status,a.*, ded.lockdate, ded.dataentryflag from administeredform a ");
			sql.append(" LEFT OUTER JOIN dataentrydraft ded ON a.administeredformid = ded.administeredformid ");
			sql.append(" where a.administeredformid = ? ");
			sql.append(" order by ded.dataentryflag ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredFormId);

			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new ObjectNotFoundException("The administered form with ID: " + administeredFormId+ " could not be found.");
			}

			// get AdministeredForm object
			AdministeredForm admForm = null;
			admForm = this.rsToAdministeredForm(rs);
			
			//Keep the check box checked in JSP if mark as completed is checked in start mode
			String status= rs.getString("status");
			if(!status.isEmpty()&& (status!=null) &&status.equalsIgnoreCase(CtdbConstants.DATACOLLECTION_STATUS_COMPLETED)){
				admForm.setMarkAsCompleted(true);
			}
			
			if (rs.getTimestamp("lockdate") != null) {
				admForm.setDataEntrySession(1);
				admForm.setLockDate(rs.getTimestamp("lockdate"));
			}
			if (rs.next()) {
				// its double key
				if (rs.getTimestamp("lockdate") != null) {
					admForm.setDataEntrySession(2);
					admForm.setLock2Date(rs.getTimestamp("lockdate"));
					admForm.setDataEntry2NotStarted(false);
				}
				

			}

	
			return admForm;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get administered form with ID "
					+ administeredFormId + "  : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves the administered form based on the administered ID and user
	 * object. It contains the list of final answers and edit answers for the
	 * administered form. It also contains the list of answers for the questions
	 * for data entry 1 and data entry 2.
	 * 
	 * @param administeredFormId
	 *            the administered form ID for retrieving the AdministeredForm
	 * @return AdministeredForm domain object
	 * @throws ObjectNotFoundException
	 *             thrown if the administered form does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public AdministeredForm getDescrepanciesNotResolved(int administeredFormId)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select * from administeredform ");
			sql.append("where administeredformid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredFormId);

			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new ObjectNotFoundException(
						"The administered form with ID: " + administeredFormId
								+ " could not be found.");
			}

			// get AdministeredForm object
			AdministeredForm admForm = null;
			admForm = this.rsToAdministeredForm(rs);

			sql = new StringBuffer(50);
			sql.append("select * from dataentrydraft a ");
			sql.append("where a.administeredformid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admForm.getId());
			rs = stmt.executeQuery();
			int count = 0;
			Timestamp lockDate = null;
			while (rs.next()) {
				count++;
				lockDate = rs.getTimestamp("lockdate");
				if (rs.getInt("dataentryflag") == 1) {
					admForm.setDataEntrySession(1);
					if (lockDate != null) {
						admForm.setLockDate(lockDate);
					}
				} else {
					admForm.setDataEntrySession(2);
					if (lockDate != null) {
						admForm.setLock2Date(lockDate);
					}
				}
			}
			if (count == 2) {
				admForm.setDataEntry2NotStarted(false);
			}

			// get all questions for form
			List questions = new ArrayList();
			sql = new StringBuffer(50);
			sql.append("select * from section a, sectionquestion b ");
			sql.append("where a.formid = ? ");
			sql.append("and a.sectionid = b.sectionid ");
			sql.append("order by a.orderval, b.questionorder");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admForm.getForm().getId());

			rs = stmt.executeQuery();

			QuestionManagerDao qDao = QuestionManagerDao.getInstance(this.conn);

			while (rs.next()) {
				questions.add(qDao.getQuestion(rs.getInt("questionid"),
						rs.getInt("questionversion")));
			}

			Question question;
			Response response;
			Response response1;
			Response response2;
			List responses = new ArrayList();
			for (Iterator it = questions.iterator(); it.hasNext();) {
				question = (Question) it.next();
				response = new Response();
				response.setQuestion(question);

				response1 = new Response();
				response1.setQuestion(question);
				response.setResponse1(response1);

				response2 = new Response();
				response2.setQuestion(question);
				response.setResponse2(response2);

				responses.add(response);
			}
			admForm.setResponses(responses);

			List answers = new ArrayList();
			List discrepancyResponses = new ArrayList();
			// populate the final answers, edit answers and draft answers
			for (Iterator it = responses.iterator(); it.hasNext();) {
				response = (Response) it.next();
				answers = this.getFinalAnswers(admForm, response);
				response.setAnswers(answers);

				int responseId = this.getResponseId(admForm, response);
				response.setId(responseId);

				if (responseId != Integer.MIN_VALUE)
					continue;

				else if (answers.isEmpty() || answers == null) {
					// final answer not provided check for child discrepancies
					answers = this.getEditAnswers(admForm, response);
					response.setEditAnswers(answers);

					// populate the draft answers for data entry 1
					response1 = response.getResponse1();
					List submitAnswers = new ArrayList();
					answers = this.getDraftAnswers(admForm, response1, 1,submitAnswers);
					response1.setAnswers(answers);
					response1.setSubmitAnswers(submitAnswers);

					int response1Id = this.getResponseDraftId(admForm,
							response1, 1);
					response1.setId(response1Id);

					// populate the draft answers for data entry 2
					response2 = response.getResponse2();
					submitAnswers = new ArrayList();
					answers = this.getDraftAnswers(admForm, response2, 2,submitAnswers);
					response2.setAnswers(answers);
					response2.setSubmitAnswers(submitAnswers);

					int response2Id = this.getResponseDraftId(admForm,
							response2, 2);
					response2.setId(response2Id);

					if (!response1.equals(response2)) {
						discrepancyResponses.add(response);
					}
				}
			}

			// set only discrepancy responses
			admForm.setResponses(discrepancyResponses);

			return admForm;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get administered form with ID "
					+ administeredFormId + "  : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public List<String> getFinalAnswers(AdministeredForm admForm, Response response)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean pCalendar = response.getQuestion().getType() == QuestionType.PATIENT_CALENDAR;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select answer ");
			if (pCalendar) {
				sql.append(", rownumber, colnumber, b.responseid ");
			}
			sql.append("from administeredform a, response b, patientresponse c ");
			if (pCalendar) {
				sql.append(", patientcalendar d ");
			}
			sql.append("where a.administeredformid = b.administeredformid ");
			sql.append("and b.responseid = c.responseid ");
			sql.append("and a.administeredformid = ? ");
			sql.append("and b.dict_questionid = ? ");
			sql.append("and b.dict_sectionid = ? ");
			// sql.append("and b.questionversion = ? ");
			if (pCalendar) {
				sql.append("and d.responseid = b.responseid ");
			}

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admForm.getId());
			stmt.setLong(2, response.getQuestion().getId());
			stmt.setLong(3, response.getQuestion().getSectionId());
			// stmt.setInt(3,
			// response.getQuestion().getVersion().getVersionNumber());

			rs = stmt.executeQuery();

			List answers = new ArrayList();
			while (rs.next()) {
				answers.add(rs.getString("answer"));

				if (pCalendar) {
					((PatientCalendarQuestion) response.getQuestion())
							.setCalendarData(rs.getInt("colnumber"),
									rs.getInt("rownumber"),
									rs.getString("answer"));

					((PatientCalendarQuestion) response.getQuestion())
							.setResponseId(rs.getInt("colnumber"),
									rs.getInt("rownumber"),
									rs.getInt("responseid"));
				}
			}
			return answers;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get final answers for administered form with id "
							+ admForm.getId() + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}



	public int getResponseId(AdministeredForm admForm, Response response)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);

			
			
			sql.append("select responseid ");
			sql.append("from response r ");
			sql.append("where r.administeredformid = ? ");
			sql.append("and r.dict_questionid = ? and r.dict_sectionid = ? ");
			// sql.append("and r.questionversion = ? ");

			stmt = this.conn.prepareStatement(sql.toString());

			stmt.setLong(1, admForm.getId());
			stmt.setLong(2, response.getQuestion().getId());
			stmt.setLong(3, response.getQuestion().getSectionId());
			// stmt.setInt(3,
			// response.getQuestion().getVersion().getVersionNumber());

			rs = stmt.executeQuery();
	
			int responseId = Integer.MIN_VALUE;
			while (rs.next()) {
				responseId = rs.getInt("responseid");
			}
			return responseId;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get responseId for administered form with id "
							+ admForm.getId() + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	
	public int getResponseId(int aformId, int sectionId, int questionId)throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
		
			
			sql.append("select responseid ");
			sql.append("from response r ");
			sql.append("where r.administeredformid = ? ");
			sql.append("and r.dict_questionid = ? and r.dict_sectionid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());

			stmt.setLong(1, aformId);
			stmt.setLong(2, questionId);
			stmt.setLong(3, sectionId);
		

			rs = stmt.executeQuery();
		
			int responseId = Integer.MIN_VALUE;
			while (rs.next()) {
				responseId = rs.getInt("responseid");
			}
			return responseId;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get responseId for administered form with id "
							+ aformId + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	
	

	public String getResponseCommentById(int responseId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("select resolvecomment ");
			sql.append("from response  ");
			sql.append("where responseId = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, responseId);
			rs = stmt.executeQuery();
			String comment = "";
			if (rs.next()) {
				comment = rs.getString("resolvecomment") == null ? "" : rs
						.getString("resolvecomment");
			}
			return comment;

		} catch (SQLException e) {
			throw new CtdbException("Unable to get response"
					+ e.getStackTrace());
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public int getResponseDraftId(AdministeredForm admForm, Response response,
			int dataEntryNum) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select responsedraftid ");
			sql.append("from administeredform a, dataentrydraft b, responsedraft c ");
			sql.append("where a.administeredformid = b.administeredformid ");
			sql.append("and b.dataentryflag = ? ");
			sql.append("and b.dataentrydraftid = c.dataentrydraftid ");
			sql.append("and a.administeredformid = ? ");
			sql.append("and c.questionid = ? ");
			sql.append("and c.questionversion = ? ");
			sql.append("and c.sectionid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			// stmt.setBoolean(1,
			// Boolean.getBoolean(Integer.toString(dataEntryNum)));
			stmt.setInt(1, dataEntryNum);
			stmt.setLong(2, admForm.getId());
			stmt.setLong(3, response.getQuestion().getId());
			stmt.setInt(4, response.getQuestion().getVersion()
					.getVersionNumber());
			stmt.setLong(5, response.getQuestion().getSectionId());

			rs = stmt.executeQuery();

			int responseId = Integer.MIN_VALUE;
			while (rs.next()) {
				responseId = rs.getInt("responsedraftid");
			}
			return responseId;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get responseDraftId for administered form with id "
							+ admForm.getId() + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public List getDraftAnswers(AdministeredForm admForm, Response response, int dataEntryNum, List submitAnswers) throws CtdbException {
		return getDraftAnswers(admForm, response, dataEntryNum, false, submitAnswers);
	}

	public List<String> getDraftAnswers(AdministeredForm admForm, Response response, int dataEntryNum, boolean CalendarResponseMethod, 
			List<String> submitAnswers) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> answers = new ArrayList<String>();


		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select answer,submitanswer,  d.attachmentid ");

			sql.append("from administeredform a, dataentrydraft b, responsedraft c, patientresponsedraft d ");

		
			sql.append("where a.administeredformid = b.administeredformid ");
			sql.append("and b.dataentryflag = ? ");
			sql.append("and b.dataentrydraftid = c.dataentrydraftid ");
			sql.append("and a.administeredformid = ? ");
			sql.append("and c.responsedraftid = d.responsedraftid ");
			sql.append("and c.dict_questionid = ? ");
			sql.append("and c.questionversion = ? ");
			sql.append("and c.dict_sectionid = ? ");
			
		

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, dataEntryNum);
			int aformid = admForm.getId();
			stmt.setLong(2, aformid);
			int questionid = response.getQuestion().getId();
			stmt.setLong(3, questionid);
			stmt.setInt(4, 1);
			int sectionid = response.getQuestion().getSectionId();
			stmt.setLong(5, sectionid);

			
			rs = stmt.executeQuery();
			
			// modified by Ching Heng for file type question
			boolean isDraft = true;
			
			while ( rs.next() ) {
				if ( response.getQuestion().getType().getValue() == QuestionType.File.getValue() ) {
					answers.add(rs.getString("answer") + ":" + rs.getString("attachmentid"));
					submitAnswers.add(rs.getString("submitanswer") + ":" + rs.getString("attachmentid"));
				}
				else {
					answers.add(rs.getString("answer"));
					submitAnswers.add(rs.getString("submitanswer"));
				}
				
			
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get draft answers for administered form with id " + admForm.getId() + 
				" : " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return answers;
	}
	
	/**
	 * Alternate getDraftAnswers method for locking performance optimization
	 * Handles list of responses rather than single response object
	 * 
	 * @param admForm
	 * @param responses
	 * @param dataEntryNum
	 * @return
	 * @throws CtdbException
	 */
	public List getAllDraftAnswers(AdministeredForm admForm, List<Response> responses, int dataEntryNum) throws CtdbException {
		return getAllDraftAnswers(admForm, responses, dataEntryNum, false);
	}
	
	public List<String> getAllDraftAnswers(AdministeredForm admForm, List<Response> responses, int dataEntryNum, boolean CalendarResponseMethod) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> answers = new ArrayList<String>();
		//boolean pCal = false;


		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select answer,submitanswer,  d.attachmentid, c.responsedraftid, c.dataentrydraftid ");
			
			sql.append("from administeredform a, dataentrydraft b, responsedraft c, patientresponsedraft d ");
			sql.append("where ");


			for(int i = 0; i<responses.size(); i++){
				sql.append("(a.administeredformid = b.administeredformid ");
				sql.append("and b.dataentryflag = ? ");
				sql.append("and b.dataentrydraftid = c.dataentrydraftid ");
				sql.append("and a.administeredformid = ? ");
				sql.append("and c.responsedraftid = d.responsedraftid ");
				sql.append("and c.dict_questionid = ? ");
				sql.append("and c.questionversion = ? ");
				sql.append("and c.dict_sectionid = ?) ");
				
				if(i != (responses.size() -1)){
					sql.append(" or ");
				}
			}
			

			stmt = this.conn.prepareStatement(sql.toString());
			
			int index = 0;
			for(Response response : responses){
				stmt.setInt(index+1, dataEntryNum);
				int aformid = admForm.getId();
				stmt.setLong(index+2, aformid);
				int questionid = response.getQuestion().getId();
				stmt.setLong(index+3, questionid);
				stmt.setInt(index+4, 1);
				int sectionid = response.getQuestion().getSectionId();
				stmt.setLong(index+5, sectionid);
				
				index += 5;
				
				System.out.println("dataENtryNum:" + dataEntryNum + " aformid:" + aformid + " questionid:" + questionid + " sectionid:" + sectionid);

			}
			

			rs = stmt.executeQuery();
			
			// modified by Ching Heng for file type question
			boolean isDraft = true;
			int currentResponse = 0;
			
			while ( rs.next() ) {
				
				for(Response response : responses){
					
//					int nextResponse = rs.getInt("responsedraftid");
					
					if(rs.getInt("responsedraftid") == response.getId()){
						if ( response.getQuestion().getType().getValue() == QuestionType.File.getValue() ) {
							response.getAnswers().add(rs.getString("answer") + ":" + rs.getString("attachmentid"));
							response.getSubmitAnswers().add(rs.getString("submitanswer") + ":" + rs.getString("attachmentid"));
						}
						else {
							response.getAnswers().add(rs.getString("answer"));
							response.getSubmitAnswers().add(rs.getString("submitanswer"));
						}
					}
				}
				
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get draft answers for administered form with id " + admForm.getId() + 
				" : " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return answers;
	}

	public List getEditAnswers(AdministeredForm admForm, Response response)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select previousanswer ");
			sql.append("from administeredform a, responseedit re ");
			sql.append("where a.administeredformid = re.administeredformid ");
			sql.append("and a.administeredformid = ? ");
			sql.append("and re.dict_questionid = ? ");
			sql.append("and re.questionversion = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admForm.getId());
			stmt.setLong(2, response.getQuestion().getId());
			stmt.setInt(3, response.getQuestion().getVersion().getVersionNumber());

			rs = stmt.executeQuery();

			List answers = new ArrayList();
			while (rs.next()) {
				answers.add(rs.getString("previousanswer"));
			}
			return answers;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get edit answers for administered form with id "
							+ admForm.getId() + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Updates the data entry draft lock date.
	 * 
	 * @param admForm
	 *            the AdministeredForm object
	 * @throws ObjectNotFoundException
	 *             thrown if the data entry draft does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur
	 */
	public void updateDataEntryDraft(AdministeredForm admForm,User loggedInUser) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		logger.info("Updating Response draft to ResponseManagerDao->updateDataEntryDraft(AdministeredForm admForm)->AdministeredFormId:\t"+admForm.getId());
		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("update dataentrydraft set lockdate = CURRENT_TIMESTAMP, updateddate = CURRENT_TIMESTAMP,  coll_status = "+"'"+CtdbConstants.DATACOLLECTION_STATUS_LOCKED+"'"+", dataoutsiderangeflag =?, lockedby=? ");
			sql.append("where dataentrydraftid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());

			stmt.setBoolean(1, admForm.isDataOutOfRangeFlag());
			stmt.setLong(2, loggedInUser.getId());
			stmt.setLong(3, admForm.getDataEntryDraftId());
			int rowsUpdated = stmt.executeUpdate();
			logger.info("CtdbConstants.DATACOLLECTION_STATUS_LOCKED"+CtdbConstants.DATACOLLECTION_STATUS_LOCKED);
			if (rowsUpdated == 0) {
				throw new ObjectNotFoundException("Data Entry Draft with ID = "
						+ admForm.getDataEntryDraftId()
						+ " does not exist in the system.");
			}
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to update data entry draft with ID "
							+ admForm.getDataEntryDraftId() + " : "
							+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Updates the data entry draft lock date.
	 * 
	 * @param administeredFormId
	 *            the AdministeredForm object ID
	 * @throws ObjectNotFoundException
	 *             thrown if the data entry draft does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur
	 */
	public void updateDiscrepancyFlag(int administeredFormId, boolean flag)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("update administeredform set discrepancyflag = ? ");
			sql.append("where administeredformid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());

			stmt.setBoolean(1, flag);
			stmt.setLong(2, administeredFormId);

			int rowsUpdated = stmt.executeUpdate();

			if (rowsUpdated == 0) {
				throw new ObjectNotFoundException(
						"The administered form  with ID = "
								+ administeredFormId
								+ " does not exist in the system.");
			}
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to update discrepancy flag for administeredform with ID "
							+ administeredFormId + " : " + e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Checks for discrepancies between the two data entry sessions of an
	 * administeredform Updates the database to reflect such Administered form's
	 * discrepancy flag is modified in discrepancyEsixts
	 * 
	 * @param aForm
	 *            The administered form to check for discrepancieys
	 * @return weather or not a descrapancy exists
	 * @throws ObjectNotFoundException
	 *             if the administered for is not found when updating
	 * @throws CtdbException
	 *             when a SQL excpetion occours, may arise from method
	 *             discrepancyExists(AForm)
	 */
	public boolean updateAndReturnDiscrepancyFlag(AdministeredForm aForm)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("update administeredform set discrepancyflag = ? ");
			sql.append("where administeredformid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setBoolean(1, discrepanciesExist(aForm));
			stmt.setLong(2, aForm.getId());
			int rowsUpdated = stmt.executeUpdate();
			if (rowsUpdated == 0) {
				throw new ObjectNotFoundException(
						"The administered form  with ID = " + aForm.getId()
								+ " does not exist in the system.");
			}
			return aForm.getDiscrepancyFlag();
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to update discrepancy flag for administeredform with ID "
							+ aForm.getId() + " : " + e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Checks for discrepancies between data entry one and two and modifies the
	 * administered forms discrepancy flag
	 * 
	 * @param af
	 *            The administered form to check for discrepancies
	 * @return weahter or not a discrepancy exists
	 * @throws CtdbException
	 *             when a sql excpetion occours.
	 */
	private boolean discrepanciesExist(AdministeredForm af) // NISH
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select count(ilv.no_answers),case when count(ilv.no_answers)=0 then false else true end as discrepancy from  "
					+ "  (select rd.questionid, upper(prd.answer), count(*) no_answers "
					+ " from patientresponsedraft prd, responsedraft rd, dataentrydraft ded "
					+ " where ded.administeredformid = ?"
					+ " and ded.dataentrydraftid = rd.dataentrydraftid "
					+ " and rd.responsedraftid = prd.responsedraftid "
					+ " group by rd.questionid, upper(prd.answer) "
					+ "  order by rd.questionid "
					+ ") ilv where ilv.no_answers = 1");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, af.getId());

			rs = stmt.executeQuery();
			while (rs.next()) {
				if (rs.getString("discrepancy").equalsIgnoreCase("t")) {
					af.setDiscrepancyFlag(true);
				} else {
					af.setDiscrepancyFlag(false);
				}
			}
			return af.getDiscrepancyFlag();
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable determine discrepancy status for aForm : "
							+ af.getId() + " : " + e.getMessage(), e);
		} finally {
			this.close(stmt);
			this.close(rs);
		}
	}

	public LinkedHashMap getDiscrepancyQuestions(int admFormId)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append(" select distinct ilv.questionid, ilv.questionversion,ilv.sectionid  from  ");
			sql.append(" (select rd.questionid, rd.questionversion, rd.sectionid, prd.answer, count(*) no_answers ");
			sql.append(" from patientresponsedraft prd, responsedraft rd, dataentrydraft ded ");
			sql.append(" where ded.administeredformid = ? ");
			sql.append("  and ded.dataentrydraftid = rd.dataentrydraftid ");
			sql.append("  and rd.responsedraftid = prd.responsedraftid ");
			sql.append(" group by rd.questionid, rd.questionversion,  rd.sectionid, prd.answer ");
			sql.append(" order by rd.questionid ");
			sql.append(") ilv where ilv.no_answers = 1");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admFormId);

			rs = stmt.executeQuery();
			LinkedHashMap qIds = new LinkedHashMap();
			while (rs.next()) {
				qIds.put(
						rs.getString("sectionid") + "_"
								+ rs.getString("questionid"),
						rs.getString("questionversion"));
			}
			return qIds;

		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get disparat question ids for form : "
							+ admFormId + " : " + e.getMessage(), e);
		} finally {
			this.close(stmt);
			this.close(rs);
		}
	}

	public Response getResolveInfoForLog(int admFormId, int questionId,
			int sectionId, int questionVersion) throws CtdbException {
		Question q = new Question();
		q.setId(questionId);
		q.setVersion(new Version(questionVersion));
		// get the final response and metaData
		Response r = getResolveAnswerData(admFormId, q);
		r.setQuestion(q);
		// get the data entry drafts
		r.setResponse1(getPatientResponseDraft(admFormId, questionId,
				sectionId, questionVersion, 1));
		r.setResponse2(getPatientResponseDraft(admFormId, questionId,
				sectionId, questionVersion, 2));
		return r;
	}



	/**
	 * Updates the administer form's information, such as certified info and
	 * final lock info.
	 * 
	 * @param admForm
	 *            the administered form object to update
	 * @throws ObjectNotFoundException
	 *             thrown if the administer form does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur
	 */
	public void updateAdministeredForm(AdministeredForm admForm)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuilder sql = new StringBuilder();

			if (admForm.getCertifiedBy() != Integer.MIN_VALUE && 
					admForm.getFinalLockBy() == Integer.MIN_VALUE) {
				sql.append("update administeredform set certifiedby = ?, certifieddate = CURRENT_TIMESTAMP ");
				sql.append("where administeredformid = ? ");
				stmt = this.conn.prepareStatement(sql.toString());
				stmt.setLong(1, admForm.getCertifiedBy());
				stmt.setLong(2, admForm.getId());
			} else if (admForm.getFinalLockBy() != Integer.MIN_VALUE) {
				sql.append("update administeredform set finallockby = ?, finallockdate = CURRENT_TIMESTAMP ");
				sql.append("where administeredformid = ? ");
				stmt = this.conn.prepareStatement(sql.toString());
				stmt.setLong(1, admForm.getFinalLockBy());
				stmt.setLong(2, admForm.getId());
			}

			int rowsUpdated = stmt.executeUpdate();

			if (rowsUpdated == 0) {
				throw new ObjectNotFoundException(
						"administered form with ID = " + admForm.getId()
								+ " does not exist in the system.");
			}

		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to update administered form with ID "
							+ admForm.getForm().getId() + " : "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	/**
	 * Updates the visit date for the admisistered form object
	 * @param aform
	 * @throws ObjectNotFoundException
	 * @throws CtdbException
	 */
	public void updateAdministeredFormVisitDate(AdministeredForm aform) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("update administeredform set visitdate = ? where administeredformid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setTimestamp(1, new Timestamp(aform.getVisitDate().getTime()));
			stmt.setLong(2, aform.getId());
			stmt.executeUpdate();

		} catch (SQLException e) {
			throw new CtdbException("Failure updating visit date for aformid: "
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	public void setDoubleKeyBypass(int aFormId) throws CtdbException {

		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("update administeredform set doublekeybypass = true where administeredformid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, aFormId);
			stmt.executeUpdate();

		} catch (SQLException e) {
			throw new CtdbException("Failure updating double key bypass flag"
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}

	}

	/**
	 * Check to see if the administered form is final locked.
	 * 
	 * @param admForm
	 *            the administered form object
	 * @return true if the administered form is final locked; false if not.
	 * @throws CtdbException
	 *             thrown if any errors occur during processing
	 */
	public boolean isFormFinalLocked(AdministeredForm admForm)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select count(formid) from administeredform ");
			sql.append("where formid = ? ");
			sql.append("and finallockby is null ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admForm.getForm().getId());

			rs = stmt.executeQuery();

			int count = 0;
			if (rs.next()) {
				count = rs.getInt(1);
			}
			if (count == 0) {
				return false;
			}
			return true;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to check if the administered form with id "
							+ admForm.getId() + " is final locked " + " : "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	
	
	
	/**
	 * Check to see if the administered form has answers in draft tables
	 * 
	 * @param admFormid 
	 * @return true if there are answers in draft
	 * @throws CtdbException
	 *             thrown if any errors occur during processing
	 */
	public boolean areThereDraftAnswers(int aformid)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select count(prd.*)  ");
			sql.append("from patientresponsedraft prd join responsedraft rd on prd.responsedraftid = rd.responsedraftid join dataentrydraft ded on ded.dataentrydraftid = rd.dataentrydraftid join administeredform af on af.administeredformid = ded.administeredformid ");
			sql.append("where af.administeredformid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, aformid);

			rs = stmt.executeQuery();

			int count = 0;
			if (rs.next()) {
				count = rs.getInt(1);
			}
			if (count == 0) {
				return false;
			}
			return true;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to check if the administered form with id "
							+ aformid + "has draft answers");
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves the resolved answers for the question of administered form.
	 * 
	 * @param admFormId
	 *            The administered form ID
	 * @param q
	 *            The question object
	 * @return The list of resolved answers
	 * @throws CtdbException
	 *             Throws if any errors occur
	 */
	public Response getResolveAnswerData(int admFormId, Question q)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select a.responseid, a.updateddate, a.questionid, b.name, c.username ");
			sql.append("from response a, question_view b, usr c ");
			sql.append(" where a.administeredformid = ? ");
			sql.append(" and a.questionid = ? ");
			sql.append(" and a.questionid = b.questionid ");
			sql.append(" and a.questionversion = b.version ");
			sql.append("and a.questionversion = ? ");
			sql.append("and a.updatedby = c.usrid");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admFormId);
			stmt.setLong(2, q.getId());
			stmt.setInt(3, q.getVersion().getVersionNumber());

			rs = stmt.executeQuery();
			Response response = new Response();
			if (rs.next()) {

				response.setId(rs.getInt("responseid"));
				response.setUpdatedDate(rs.getTimestamp("updateddate"));
				response.setUpdatedByUsername(rs.getString("username"));
				q.setName(rs.getString("name"));
				response.setQuestion(q);
				List answers = this.getResolveAnswersForAudit(admFormId, q);
				if (answers.isEmpty()) {
					response.setAnswers(this.getAnswers(response.getId()));
				} else {
					response.setAnswers(answers);
				}
			}
			return response;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get resolve answers for the administered form with id "
							+ admFormId + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves the patient response draft information for the question of the
	 * administered form
	 * 
	 * @param admFormId
	 *            the data entry draft id
	 * @return The list of the patient response draft info for the question on
	 *         the administered form
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public Response getPatientResponseDraft(int admFormId, int questionId,
			int questionVersion, int sectionId, int dataEntrySession)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select distinct rd.responsedraftid, rd.questionid, rd.questionversion, rd.sectionid ");
			sql.append("from responsedraft rd, administeredform af, dataentrydraft ded ");
			sql.append(" where af.administeredformid = ? ");
			sql.append(" and af.administeredformid = ded.administeredformid ");
			sql.append(" and ded.dataentryflag = ? ");
			sql.append(" and rd.dataentrydraftid = ded.dataentrydraftid ");
			sql.append(" and rd.dict_questionid = ? ");
			sql.append(" and rd.questionversion = ? ");
			sql.append(" and rd.dict_sectionid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admFormId);
			stmt.setInt(2, dataEntrySession);
			stmt.setLong(3, questionId);
			stmt.setInt(4, questionVersion);
			stmt.setInt(5, sectionId);
			rs = stmt.executeQuery();
			Response response = new Response();
			if (rs.next()) {
				response.setId(rs.getInt("responsedraftid"));
				Question q = new Question();
				q.setId(rs.getInt("questionid"));
				q.setVersion(new Version(rs.getInt("questionversion")));
				q.setSectionId(rs.getInt("sectionid"));
				response.setQuestion(q);
				List<String> answers = new ArrayList<String>();
				List<String> submitAnswers = new ArrayList<String>();
				List<String> deComments = new ArrayList<String>();
				//answers = this.getAnswersDraft(response, response.getId(),submitAnswers,deComments);
				answers = this.getAnswersDraft(response, response.getId(),submitAnswers);
				response.setAnswers(answers);
				response.setSubmitAnswers(submitAnswers);
			}

			return response;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get patient response drafts for the administeredoform "
							+ admFormId + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves the responses for the administered form
	 * 
	 * @param dataEntryDraftId
	 *            the data entry draft id
	 * @return List the list of the responses for the administered form
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public List<Response> getResponsesDraft(int dataEntryDraftId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Response> responses = new ArrayList<Response>();
		
		try {
			StringBuffer sql = new StringBuffer(50);
			/*sql.append("select rd.*, q.* from responsedraft rd, question_view q ");
			sql.append("where dataentrydraftId = ? ");
			sql.append("and rd.questionid = q.questionid ");
			sql.append(" and rd.questionversion = q.version ");*/
			
			
			sql.append("select rd.* from responsedraft rd");
			sql.append(" where dataentrydraftId = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, dataEntryDraftId);
			
			rs = stmt.executeQuery();
			
			while ( rs.next() ) {
				Response response = this.rsToResponseDraft(rs);
				List<String> submitAnswers = new ArrayList<String>();
				List<String> deComments = new ArrayList<String>();
				//List<String> answers = this.getAnswersDraft(response, response.getId(), submitAnswers,deComments);
				List<String> answers = this.getAnswersDraft(response, response.getId(), submitAnswers);
				response.setAnswers(answers);
				response.setSubmitAnswers(submitAnswers);
				//response.setDeComments(deComments);
				
				//new Nish
				response.getResponse1().setAnswers(answers);
				response.getResponse1().setSubmitAnswers(submitAnswers); 
				//response.getResponse1().setDeComments(deComments);
				
				
				
				
				responses.add(response);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get responses draft with id " + dataEntryDraftId + " : " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return responses;
	}

	/**
	 * Retrieves the responses for the administered form
	 * 
	 * @param dataEntryDraftId
	 *            the data entry draft id
	 * @return List the list of the responses for the administered form
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public List<Response> getResponsesLocked(int administeredFormId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Response> responses = new ArrayList<Response>();
		
		try {
			StringBuffer sql = new StringBuffer(100);
			sql.append("select rd.* from response rd");
			sql.append(" where administeredformid = ? ");

			
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredFormId);
			
			rs = stmt.executeQuery();
			
			while ( rs.next() ) {
				Response response = this.rsToResponseLocked(rs);
				List<String> submitAnswers = this.getSubmitAnswers(response.getId());
				List<String> answers = this.getAnswers(response.getId());
				response.setAnswers(answers);
				response.setSubmitAnswers(submitAnswers);
				responses.add(response);
				
				//new Nish
				response.getResponse1().setAnswers(answers);
				response.getResponse1().setSubmitAnswers(submitAnswers); 
				
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get responses with id " + administeredFormId + " : " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return responses;
	}

	public void copyDraftToResponse(AdministeredForm admForm, User user)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs2 = null;
		try {
			//List draftResponses1 = this.getResponsesDraft(admForm, 1);
			List draftResponses1 = admForm.getResponses();
			Response draftResponse1 = null;
			Response draftResponse2 = null;
			Response response = null;

			boolean inResponse = false;

			AdministeredForm newAdmForm = null;
			List answers = new ArrayList();

			int iter = 0;
			for (Iterator it = draftResponses1.iterator(); it.hasNext();) {

				draftResponse1 = (Response) it.next();

				inResponse = this.isInResponse(draftResponse1.getQuestion(),
						admForm.getId());
				if (!inResponse) {

					newAdmForm = new AdministeredForm();
					newAdmForm.setId(admForm.getId());
					response = new Response();

					response.setAdministeredForm(newAdmForm);
					Question question = new Question();
					question.setId(draftResponse1.getQuestion().getId());
					question.setSectionId(draftResponse1.getQuestion()
							.getSectionId());
					question.setVersion(new Version(draftResponse1
							.getQuestion().getVersion().getVersionNumber()));

					question.setType(draftResponse1.getQuestion().getType());
					
					question.setAnswers(draftResponse1.getQuestion().getAnswers());

					response.setQuestion(question);
					response.setResponse1(draftResponse1);
					/*draftResponse2 = this.getResponseDraft2(admForm.getId(),
							draftResponse1);
					response.setResponse2(draftResponse2);*/

					this.createResponse(response, user);
					List submitAnswers = new ArrayList();
					answers = this.getDraftAnswers(admForm, response, 1,submitAnswers);

					if (!answers.isEmpty()) {
						response.setAnswers(answers);
						response.setSubmitAnswers(submitAnswers);
						this.createResponseAnswers(response);
					}

				
				}
			}
		} 
		catch(DuplicateObjectException doe){
			doe.printStackTrace();
			throw new DuplicateObjectException("Duplicate data detected while copying from draft to response. Please consult with your vendor for data cleanup.");
		}
		
		
		catch (Exception e) {
			throw new CtdbException(
					"Unable to copy draft responses to response with administered form id "
							+ admForm.getId() + " : " + e.getMessage(), e);
		} finally {
			this.close(rs2);
			this.close(stmt);
		}
	}
	
	/**
	 * This is an alternate copyDraftToResponse method to optimize the form locking performance
	 * This method executes single queries on the list of responses, rather than
	 * iterating through each response to execute an individual query for each.
	 * 
	 * @param admForm
	 * @param user
	 * @throws CtdbException
	 */
	public void copyDraftToResponse2(AdministeredForm admForm, User user)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs2 = null;
		try {
			List<Response> draftResponses1 = admForm.getResponses();
			List<Response> newResponses = new ArrayList<Response>();
			Response response = null;

			AdministeredForm newAdmForm = null;

			draftResponses1 = this.areInResponse(draftResponses1, admForm.getId());
			List<String> answers = new ArrayList<String>();
			for(Response draftResponse : draftResponses1){
				newAdmForm = new AdministeredForm();
				newAdmForm.setId(admForm.getId());
				response = new Response();

				response.setAdministeredForm(newAdmForm);
				Question question = new Question();
				question.setId(draftResponse.getQuestion().getId());
				question.setSectionId(draftResponse.getQuestion()
						.getSectionId());
				question.setVersion(new Version(draftResponse
						.getQuestion().getVersion().getVersionNumber()));

				question.setType(draftResponse.getQuestion().getType());
				
				question.setAnswers(draftResponse.getQuestion().getAnswers());

				response.setQuestion(question);
				response.setResponse1(draftResponse);
				
				List<String> submitAnswers = new ArrayList<String>();
				answers = this.getDraftAnswers(admForm, response, 1, submitAnswers);
				response.setAnswers(answers);
				response.setSubmitAnswers(submitAnswers);
				
				newResponses.add(response);
			}
			
			this.createResponses(newResponses, user);

			this.createResponseAnswers2(newResponses);
			
		} 
		catch(DuplicateObjectException doe){
			doe.printStackTrace();
			throw new DuplicateObjectException("Duplicate data detected while copying from draft to response. Please consult with your vendor for data cleanup.");
		}
		
		catch (Exception e) {
			throw new CtdbException(
					"Unable to copy draft responses to response with administered form id "
							+ admForm.getId() + " : " + e.getMessage(), e);
		} finally {
			this.close(rs2);
			this.close(stmt);
		}
	}



	private boolean isInResponse(Question q, int admFormId)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select count(a.responseid) from response a ");
			sql.append("where a.administeredformid = ? ");
			sql.append("and a.dict_questionid = ? and a.questionversion = ? and a.dict_sectionid = ?");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admFormId);
			stmt.setLong(2, q.getId());
			stmt.setInt(3, q.getVersion().getVersionNumber());
			stmt.setInt(4, q.getSectionId());
			rs = stmt.executeQuery();
			int count = 0;
			if (rs.next()) {
				count = rs.getInt(1);
			}
			if (count == 0) {
				return false;
			}
			return true;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to check if responses draft is in response : "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	private List<Response> areInResponse(List<Response> responses, int admFormId)
			throws CtdbException {
		
		List<Long> responsesThatExist = new ArrayList();
		
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select a.responseid from response a ");
			sql.append("where ");
			
			for(int i = 0; i < responses.size(); i++){
				sql.append("(a.administeredformid = ? and a.dict_questionid = ? and a.questionversion = ? and a.dict_sectionid = ?)");
				if(i != (responses.size() -1)){
					sql.append(" or ");
				}
			}
			
			stmt = this.conn.prepareStatement(sql.toString());
			
			int index = 0;
			
			for(Response response : responses){
				Question q = response.getQuestion();
				
				stmt.setLong(index+1, admFormId);
				stmt.setLong(index+2, q.getId());
				stmt.setInt(index+3, q.getVersion().getVersionNumber());
				stmt.setInt(index+4, q.getSectionId());
				
				index+=4;
			}
			rs = stmt.executeQuery();
			while (rs.next()) {
				Long responseid = rs.getLong("responseid");
				
				responsesThatExist.add(responseid);
			}
			
			List<Response> responsesToRemove = new ArrayList<Response>();
			
			for(Response response : responses){
				for(Long id : responsesThatExist){
					if(id == response.getId()){
						responsesToRemove.add(response);
					}
				}
			}
			
			for(Response responseToRemove : responsesToRemove){
				responses.remove(responseToRemove);
			}
			
			return responses;
			
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to check if responses draft is in response : "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}



	/**
	 * Retrieves the number of questions from an administered form which have
	 * answers in the patientresponsedraft table.
	 * 
	 * @param admFormId
	 *            the administered form id
	 * @param user
	 *            The user who is the data entry person for this admin form.
	 * @return int the number of questions with answers received.
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public int getNumQuestionsWithAnswers(int admFormId, User user)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select count(distinct pd.responsedraftid) from patientresponsedraft pd ");
			sql.append("where exists (select rd.responsedraftid from responsedraft rd, dataentrydraft dd ");
			sql.append("where rd.dataentrydraftid = dd.dataentrydraftid ");
			sql.append("and pd.responsedraftid = rd.responsedraftid ");
			sql.append("and dd.administeredformid = ? and dd.dataenteredby = ? )");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admFormId);
			stmt.setLong(2, user.getId());
			rs = stmt.executeQuery();
			int numQuestions = 0;
			if (rs.next()) {
				numQuestions = rs.getInt(1);
			}
			return numQuestions;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get number of questions answered for the administered form "
							+ admFormId + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public boolean checkGuid(int aFormId) {
		CallableStatement stmt = null;
		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("{ call CHECKGUIDLOCK (?) }");
			stmt = conn.prepareCall(sql.toString());
			stmt.setLong(1, aFormId);
			stmt.execute();

			return true;
		} catch (PSQLException e) {
			// error state in Postgres?
			return false;
		} catch (SQLException e) {
			if (e.getErrorCode() == -20010) {
				return false;
			}
		} finally {
			this.close(stmt);
		}
		return true;
	}

	public void updateClinicalTrialAdmFormVisitDate(AdministeredForm aform)
			throws CtdbException {
		PreparedStatement stmt = null;
		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("update administeredform set visitdate = ? where patientid = ? and intervalid = ? and formid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setTimestamp(1, new Timestamp(aform.getVisitDate().getTime()));
			stmt.setLong(2, aform.getPatient().getId());
			stmt.setLong(3, aform.getInterval().getId());
			stmt.setLong(4, aform.getForm().getId());
			stmt.executeUpdate();

		} catch (SQLException e) {
			throw new CtdbException(" Failure updating visit date "
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	public void updateQaReviewStatus(int aformId, boolean reviewed,
			boolean qaLocked) throws CtdbException {
		PreparedStatement stmt = null;
		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("update administeredform set qaReviewed = ?, qaLocked = ? where administeredformid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setBoolean(1, reviewed);
			stmt.setBoolean(2, qaLocked);
			stmt.setLong(3, aformId);
			stmt.executeUpdate();

		} catch (SQLException e) {
			throw new CtdbException(" Failure updating form qa status "
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}



	public void deleteAfRecordIfNeeded(AdministeredForm af)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		PreparedStatement stmt2 = null;

		try {
			boolean deleteRecord = true;
			StringBuffer sql = new StringBuffer(100);
			sql.append("select dataEntryDraftId from dataEntrydraft ");
			sql.append("where administeredformid = ?");
			stmt = this.conn.prepareStatement(sql.toString());

			stmt.setLong(1, af.getId());
			rs = stmt.executeQuery();
			if (rs.next()) {
				deleteRecord = false;
			}
			stmt.close();
			rs.close();
			if (deleteRecord) {
				// there are no dataentrydrafts associated with the af id
				// anymroe
				sql = null;
				sql = new StringBuffer(100);
				sql.append("delete from administeredform where administeredformid = ?");
				stmt2 = this.conn.prepareStatement(sql.toString());
				stmt2.setLong(1, af.getId());
				stmt2.executeUpdate();

			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to update administered form "
					+ af.getId() + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
			this.close(stmt2);			
		}
	}

	public void updateAdministeredFormMetaData(AdministeredForm af,
			int intervalId, int patientId, boolean isClinicalTrial)
			throws CtdbException {
		PreparedStatement stmt = null;

		try {
			int origAdminFormId = af.getId();
			int dedId = af.getDataEntryDraftId();
			// add a new admin form record
			af.getInterval().setId(intervalId);
			af.getPatient().setId(patientId);
			this.create(af);

			// now update data entry draft
			// RX: Also make sure this is the first data entry for the new adm
			// form since this maybe edited
			// from DE2 of another adm form.
			StringBuffer sql = new StringBuffer(100);
			sql.append("update dataentrydraft set administeredformid = ?, dataentryflag = ?  where dataentrydraftid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());

			stmt.setLong(1, af.getId());
			stmt.setInt(2, this.getDataEntryNum(af, isClinicalTrial) + 1);
			stmt.setLong(3, dedId);
			stmt.executeUpdate();
			stmt.close();
			stmt = null;

			// make sure dataentry draft indicates only one record
			sql = new StringBuffer(100);
			sql.append("update dataentrydraft set dataentryflag = 1 where administeredformid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());

			stmt.setLong(1, origAdminFormId);
			stmt.executeUpdate();

			// now update AdministeredFormEdit table to set new
			// AdministeredFormId
			sql = new StringBuffer(100);
			sql.append("update administeredformedit set administeredformid = ? where administeredformid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, af.getId());
			stmt.setLong(2, origAdminFormId);
			stmt.executeUpdate();

		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to update administered form Meta Data "
							+ af.getId() + " : " + e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}



	public void recordAdminFormMetaDataEdit(AdminFormMetaDataEdit data)
			throws CtdbException {

		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(100);
			sql.append("insert into administeredformedit(editid, administeredformid, colname, colvaluebefore, "
					+ "colvalueafter, editreason, editdate, editby) ");
			sql.append("values(DEFAULT,?,?,?,?,?, CURRENT_TIMESTAMP,?)");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, data.getAdminFormId());
			stmt.setString(2, data.getColumn());
			stmt.setString(3, data.getBeforeValue());
			stmt.setString(4, data.getAfterValue());
			stmt.setString(5, data.getReason());
			stmt.setInt(6, data.getUserId());

			stmt.executeUpdate();

		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to create administeredformedit table entry for the administered form: "
							+ data.getAdminFormId(), e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * check if the administered for edited by the user is already present in
	 * the system. Note: A Form Id and an interval Id and a Patient Id make a
	 * unique administered form.
	 * 
	 * @param adminForm
	 *            the administered form that contains new meta data.
	 * @return boolean true if the adminForm is already in the system, otherwise
	 *         false.
	 * @throws CtdbException
	 *             Thrown if any errors occur while processing
	 */
	public boolean checkIsDuplicateAdminForm(AdministeredForm adminForm)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select count(*) from administeredform ");
			sql.append("where eformid = ? ");
			sql.append("and intervalid = ? ");
			sql.append("and patientid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, adminForm.getForm().getId());
			stmt.setLong(2, adminForm.getInterval().getId());
			stmt.setLong(3, adminForm.getPatient().getId());

			rs = stmt.executeQuery();
			int count = 0;

			if (rs.next()) {
				count = rs.getInt(1);
			}

			if (count == 0)
				return false;
			else
				return true;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get administered for "
					+ adminForm.getForm().getName());
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public boolean isDuplicateAdminForm(User u, AdministeredForm af,
			int intervalId, int patientId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			// check to see if one data entry exists for single key and if two
			// data entries exist for double key
			AdministeredForm afTry = new AdministeredForm();
			afTry.clone(af);
			Patient p = new Patient();
			p.setId(patientId);
			Interval i = new Interval();
			i.setId(intervalId);

			afTry.setPatient(p);
			afTry.setInterval(i);
			int dataEntryNum = this.getDataEntryNum(afTry);
			if (af.getForm().getSingleDoubleKeyFlag() == 1 && dataEntryNum >= 1) {
				return true;
			} else if (af.getForm().getSingleDoubleKeyFlag() == 2
					&& dataEntryNum >= 2) {
				return true;
			}
			// make sure same user is not trying to reassign to something else
			// they have already entered

			StringBuffer sb = new StringBuffer();
			sb.append("select dataentrydraftid from dataentrydraft where administeredformid  in ");
			sb.append(" (select administeredformid from administeredform where ");
			sb.append("     formid = ? and patientid=? ");
			if (af.getVisitDate() != null) {
				// this is a natural history protocol
				// hopefully the only time a visit date is set on this form is
				// in the answeresEditAction calling this method.
				sb.append(" and visitdate = ? ");
			} else {
				sb.append(" and intervalid = ? ");
			}
			sb.append(" ) and dataenteredby = ?");
			stmt = this.conn.prepareStatement(sb.toString());
			stmt.setLong(1, af.getForm().getId());
			stmt.setLong(2, afTry.getPatient().getId());
			if (af.getVisitDate() != null) {
				stmt.setTimestamp(3, new Timestamp(af.getVisitDate().getTime()));
			} else {
				stmt.setLong(3, afTry.getInterval().getId());
			}
			stmt.setLong(4, u.getId());

			rs = stmt.executeQuery();
			if (rs.next()) {
				// this user has entered data for what they are tryign to
				// reassign
				return true;
			}
			return false;
		} catch (SQLException e) {
			throw new CtdbException("Unable to find duplicate status", e);
		} catch (CtdbException e) {
			throw new CtdbException("Unable to find duplicate status", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves the answers for the response draft ID
	 * 
	 * @param responseDraftId
	 *            the response draft id
	 * @return List the list of the answers for the response draft id
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	private List<String> getAnswersDraft(Response response, int responseDraftId, List<String> submitAnswers) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> answers = new ArrayList<String>();

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select * from patientresponsedraft ");
			sql.append("where responsedraftid = ? ");
			
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, responseDraftId);
			
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				boolean isFile = false;
				int attachmentId = rs.getInt("attachmentid");
				if(attachmentId != 0) {
					isFile = true;
				}
				
				if (isFile) {
					answers.add(rs.getString("answer") + ":" + rs.getString("attachmentid"));
					submitAnswers.add(rs.getString("submitanswer") + ":" + rs.getString("attachmentid"));
				}
				else {
					answers.add(rs.getString("answer"));
					submitAnswers.add(rs.getString("submitanswer"));
					//deComments.add(rs.getString("de_comment"));
				}
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get answers for response draft with id " + responseDraftId + 
				" : " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return answers;
	}

	/**
	 * 
	 * 
	 * @param patientId
	 * @param protocolId
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws CtdbException
	 */
	public List getCompletedPatientForms(Integer patientId, int protocolId)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List completedForms = new ArrayList();
		DataCollectionLandingForm dataFormResult = new DataCollectionLandingForm();
		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append(" select distinct ef.name as eformname, ef.eformid as eformid,  to_char(a.FINALLOCKDATE, 'YYYY-MM-DD HH24:MI') as FINALLOCKDATE ");
			sql.append(" from administeredform a, eform ef ");
			sql.append(" where a.eformid = ef.eformid ");
			sql.append(" and a.finallockdate is not null ");
			sql.append(" and  a.patientid = ?  and ef.protocolid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, patientId);
			stmt.setLong(2, protocolId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				dataFormResult = new DataCollectionLandingForm();
				dataFormResult.setFormName(rs.getString("eformname"));
				dataFormResult.setFormId(rs.getInt("eformid"));
				dataFormResult.setFormLastUpdatedDate(rs
						.getString("finallockdate"));
				completedForms.add(dataFormResult);
			}
			return completedForms;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get completed administered form: for patient id "
							+ patientId + ", protocol id " + protocolId
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}




	/**
	 * Method to display getAllAdministeredFormInProgress and search on that
	 * list for IBIS new screen added by yogi
	 * 
	 * @param user
	 * @param protocolId
	 * @param afrc_pc
	 * @return
	 * @throws CtdbException
	 */
	public List<AdministeredForm> getAllAdministeredFormInProgress(User user,int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List dataEntryInProgressList = new ArrayList();
		DataEntryInProgressForm deipf = new DataEntryInProgressForm();
		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append(" select a.*, b.*, "
					+ CtdbDao.getDecryptionFunc("patient.subjectid")
					+ " as subjectid, interval.name as intervalname ");
			sql.append(" , " + CtdbDao.getDecryptionFunc("patient.lastname")
					+ " as lastname, "
					+ CtdbDao.getDecryptionFunc("patient.firstname")
					+ " as firstname ");
			sql.append(" ,  f.name formname, f.protocolid, p.patientdisplaytype, p.protocolnumber ");
			sql.append(" from administeredform a, dataentrydraft b, patient , interval, form f, protocol p");
			sql.append(" where  a.administeredformid = b.administeredformid ");
			sql.append(" and b.dataenteredby = ? ");
			sql.append(" and b.lockdate is null ");
			sql.append(" and a.patientid = patient.patientid ");
			sql.append(" and a.intervalid = interval.intervalid ");
			sql.append(" and a.formid = f.formid ");
			sql.append(" and p.protocolid = f.protocolid ");
			sql.append(" and p.protocolid = ? ");
			//sql.append(afrc_pc.getPreviousCollectionsSearchClause());
			sql.append(" order by f.protocolid, formname, lastname ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, user.getId());
			stmt.setLong(2, protocolId);
			rs = stmt.executeQuery();

			List<AdministeredForm> admForms = new ArrayList<AdministeredForm>();
			AdministeredForm admForm;
			while (rs.next()) {
				admForm = this.rsToAdministeredFormToResume(rs);
				admForms.add(admForm);
			}
			return admForms;

		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get administered forms in progress: "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}



	private int getNumQuestionsAnswered(int admFormId, User user)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select distinct c.responsedraftid from administeredform a, dataentrydraft b, ");
			sql.append("responsedraft c, patientresponsedraft d ");
			sql.append("where a.administeredformid = ? ");
			sql.append("and a.administeredformid = b.administeredformid ");
			sql.append("and b.dataentrydraftid = c.dataentrydraftid ");
			sql.append("and c.responsedraftid = d.responsedraftid ");
			sql.append("and b.lockdate is null ");
			sql.append("and b.dataenteredby = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admFormId);
			stmt.setLong(2, user.getId());
			rs = stmt.executeQuery();
			int numQuestions = 0;
			while (rs.next()) {
				numQuestions++;
			}

			return numQuestions;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get number of questions answered for the administered form "
							+ admFormId + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Creates the response draft and its answer.
	 * 
	 * @param response
	 *            the Response object to create
	 * @param user
	 *            the User object
	 * @throws DuplicateObjectException
	 *             thrown if the ResponseDraft exists in the system based on the
	 *             unique constraints
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public void createResponseDraft(Response response, User loggedInUser) throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;
		PreparedStatement stmt1 = null;
		try {
			StringBuffer sql = new StringBuffer(200);

			
			sql.append("insert into responsedraft(responsedraftid, dataentrydraftid, dict_questionid, questionversion, ");
			sql.append("createddate, updateddate, dict_sectionid) ");
			sql.append("values(DEFAULT,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,?)");
			stmt = this.conn.prepareStatement(sql.toString());


			Response response1 = new Response();

			stmt.setLong(1, response.getAdministeredForm()
					.getDataEntryDraftId());
			stmt.setLong(2, response.getQuestion().getId());
			stmt.setInt(3, response.getQuestion().getVersion()
					.getVersionNumber());
			stmt.setLong(4, response.getQuestion().getSectionId());

			stmt.executeUpdate();

			// added by yogi
			StringBuffer sb = new StringBuffer();
			if(response.getAdministeredForm().isMarkAsCompleted()){

			sb.append("update  dataentrydraft set updateddate = CURRENT_TIMESTAMP, coll_status ="+"'"+CtdbConstants.DATACOLLECTION_STATUS_COMPLETED+"', completeddate = CURRENT_TIMESTAMP, completedby=?"+" where dataentrydraftid = ?");
			}else{
				sb.append("update  dataentrydraft set updateddate = CURRENT_TIMESTAMP,coll_status ="+"'"+CtdbConstants.DATACOLLECTION_STATUS_INPROGRESS+"'"+" where dataentrydraftid = ?");
			}

			stmt1 = this.conn.prepareStatement(sb.toString());
			if(response.getAdministeredForm().isMarkAsCompleted()){
				stmt1.setLong(1, loggedInUser.getId()); 
				stmt1.setLong(2, response.getAdministeredForm()
						.getDataEntryDraftId());
			}else {
				stmt1.setLong(1, response.getAdministeredForm()
						.getDataEntryDraftId());
			}
			
			stmt1.executeUpdate();
			// to update date based on update to form

			int id = this.getInsertId(conn, "responsedraft_seq");
			response.setId(id);
			response1.setId(id);
			response.setResponse1(response1);

		} catch (PSQLException e) {
			if (e.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A response draft for the administered form already exists: "
								+ e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to create response draft: "
						+ e.getMessage(), e);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A response draft for the administered form already exists: "
								+ e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to create response draft: "
						+ e.getMessage(), e);
			}
		} finally {
			this.close(stmt);
			this.close(stmt1);
		}
	}
	
	public void createResponsesDraft(List<Response> responses, User loggedInUser) throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;
		
		try {
			StringBuffer sql = new StringBuffer(200);

			sql.append("insert into responsedraft(responsedraftid, dataentrydraftid, dict_questionid, questionversion, ");
			sql.append("createddate, updateddate, dict_sectionid) ");
			sql.append("values ");
			
			for(int i=0; i < responses.size(); i++){
				sql.append("(DEFAULT,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,?)");
				
				if(i != (responses.size() -1)){
					sql.append(", ");
				}
			}
			
			
			stmt = this.conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);

			int index = 0;
			for(Response response : responses){
				stmt.setLong(index+1, response.getAdministeredForm()
						.getDataEntryDraftId());
				stmt.setLong(index+2, response.getQuestion().getId());
				stmt.setInt(index+3, response.getQuestion().getVersion()
						.getVersionNumber());
				stmt.setLong(index+4, response.getQuestion().getSectionId());
				
				index += 4;
			}
			
			stmt.executeUpdate();
			
			ResultSet rs = stmt.getGeneratedKeys();
			
			int j = 0;
			while (rs.next()) {
				int id = rs.getInt(1);
			 
				responses.get(j).setId(id);
			 
				j++;
			}
			
			//or do this instead of all following method
			this.updateDataEntryDraft(responses, loggedInUser);
			
	
			
		} catch (PSQLException e) {
			if (e.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A response draft for the administered form already exists: "
								+ e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to create response draft: "
						+ e.getMessage(), e);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A response draft for the administered form already exists: "
								+ e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to create response draft: "
						+ e.getMessage(), e);
			}
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Creates the all response draft corresponding to all questions in an administered form.
	 * 
	 * @param admForm - The AdministeredForm object containing all questions.
	 * @throws SQLException	Thrown if any sql errors occur while processing.
	 * @throws CtdbException	Thrown if any other errors occur while processing from
	 * calling other dao methods.
	 */
	public void createResponseDraftAll(AdministeredForm admForm) throws CtdbException {
		PreparedStatement stmt = null;
		StringBuffer sql = new StringBuffer(200);
		
		try {

			sql.append("insert into responsedraft(responsedraftid, dataentrydraftid, dict_questionid, questionversion, ");
			sql.append("createddate, updateddate, dict_sectionid) ");
			sql.append("values(?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?) ");
			stmt = this.conn.prepareStatement(sql.toString());
	
			// get all questions for a form
			List<Question> questions = new ArrayList<Question>();
			
			for ( List<Section> row : admForm.getForm().getRowList() ) {
				for ( Section s : row ) {
					if (s != null) {
						// questions.addAll(qDao.getQuestions(s));
						questions.addAll(s.getQuestionList());
					}
				}
			}
	
			// create responsedraft entries
			for ( Question question : questions ) {
				// I don't know how the rest of this code works, so I'm just going
				// to translate the CTDB Oracle version into our postgres version
				int id = this.getNextSequenceValue(this.conn, "responsedraft_seq");
				
				for ( Response aResponse : admForm.getResponses() ) {
					if ( aResponse.getQuestion().getId() == question.getId() ) {
						aResponse.setId(id);
					}
				}
				
				stmt.setLong(1, id);
				stmt.setLong(2, admForm.getDataEntryDraftId());
				stmt.setLong(3, question.getId());
				stmt.setInt(4, question.getVersion().getVersionNumber());
				stmt.setLong(5, question.getSectionId());
				stmt.executeUpdate();
			}
		}
		catch ( SQLException sqle ) {
			throw new CtdbException("Could not create a reponse draft entry because of a database error.", sqle);
		}
		finally {
			this.close(stmt);
		}
	}

	/**
	 * Creates the final response for the data entries.
	 * 
	 * @param response
	 *            the Response object to create
	 * @param user
	 *            the User object
	 * @throws DuplicateObjectException
	 *             thrown if the Response exists in the system based on the
	 *             unique constraints
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public void createResponse(Response response, User user)
			throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;
		try {
			StringBuffer sql = new StringBuffer(200);

			sql.append("insert into response(responseid, administeredformid, "
					+ "dict_questionid, questionversion,");
			sql.append(" updatedby, updateddate,resolvecomment, dict_sectionid ) ");
			sql.append("values( DEFAULT, ?, ?, ?, ?,CURRENT_TIMESTAMP,?,?)");
			stmt = this.conn.prepareStatement(sql.toString());


			stmt.setLong(1, response.getAdministeredForm().getId());
			stmt.setLong(2, response.getQuestion().getId());
			stmt.setInt(3, response.getQuestion().getVersion()
					.getVersionNumber());

			stmt.setInt(4, user.getId());
			stmt.setString(5, response.getComment());
			//System.out.println(response.getQuestion().getSectionId());
			stmt.setLong(6, response.getQuestion().getSectionId());

			stmt.executeUpdate();
			response.setId(getInsertId(conn, "response_seq"));


		} catch (PSQLException e) {
			if (e.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A response for the administered form already exists: "
								+ e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to create response: "
						+ e.getMessage(), e);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A response for the administered form already exists: "
								+ e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to create response: "
						+ e.getMessage(), e);
			}
		} finally {
			this.close(stmt);
		}
	}
	
	/**
	 * Alternate method to optimize form locking performance
	 * Handles a list of responses for single query, rather than a single response object
	 * 
	 * @param responses
	 * @param user
	 * @throws DuplicateObjectException
	 * @throws CtdbException
	 */
	public void createResponses(List<Response> responses, User user)
			throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;
		try {
			StringBuffer sql = new StringBuffer(200);

			sql.append("insert into response(responseid, administeredformid, "
					+ "dict_questionid, questionversion,");
			sql.append(" updatedby, updateddate,resolvecomment, dict_sectionid ) ");
			sql.append(" values ");
			
			for(int i=0; i < responses.size(); i++){
				sql.append("( DEFAULT, ?, ?, ?, ?,CURRENT_TIMESTAMP,?,?)");
				
				if(i != (responses.size() -1)){
					sql.append(", ");
				}
			}
			
			stmt = this.conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);

			int index = 0;
			
			for(Response response : responses){
				stmt.setLong(index+1, response.getAdministeredForm().getId());
				stmt.setLong(index+2, response.getQuestion().getId());
				stmt.setInt(index+3, response.getQuestion().getVersion()
						.getVersionNumber());
				stmt.setInt(index+4, user.getId());
				stmt.setString(index+5, response.getComment());
				stmt.setLong(index+6, response.getQuestion().getSectionId());
				
				index += 6;
			}

			stmt.executeUpdate();
			
			ResultSet rs = stmt.getGeneratedKeys();
			
			int j = 0;
			while (rs.next()) {
			 int id = rs.getInt(1);
			 
			 responses.get(j).setId(id);
			 
			 j++;
			}


		} catch (PSQLException e) {
			if (e.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A response for the administered form already exists: "
								+ e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to create response: "
						+ e.getMessage(), e);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A response for the administered form already exists: "
								+ e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to create response: "
						+ e.getMessage(), e);
			}
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Gets the number of patients completed with the certify for the form id
	 * 
	 * @param formId
	 *            the form id
	 * @param user
	 *            the User object contains the user info
	 * @return int the number of patient completed the certify
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public int getNumOfPatientsCompleted(int formId, User user)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			int numPatients = 0;

			StringBuffer sql = new StringBuffer(50);
			sql.append("select count(distinct patientid) from administeredform a, dataentrydraft b ");
			sql.append("where a.administeredformid = b.administeredformid ");
			sql.append("and a.formid = ? ");
			sql.append("and b.dataenteredby = ? ");
			sql.append("and a.certifieddate is not null ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);
			stmt.setLong(2, user.getId());
			rs = stmt.executeQuery();

			if (rs.next()) {
				numPatients = rs.getInt(1);
			}
			return numPatients;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get number of patients certified with form ID "
							+ formId + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Gets the number of patients completed with the certify for the form id
	 * 
	 * @param formId
	 *            the form id
	 * @return int the number of patient completed the certify
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public int getNumOfPatientsCompleted(int formId, int version)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			int numPatients = 0;

			StringBuffer sql = new StringBuffer(50);
			sql.append("select count(patientid) from administeredform a  ");
			sql.append("where a.formid = ? ");
			sql.append(" and a.formversion = ? ");
			sql.append("and a.certifieddate is not null ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);
			stmt.setInt(2, version);
			rs = stmt.executeQuery();

			if (rs.next()) {
				numPatients = rs.getInt(1);
			}
			return numPatients;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get number of patients certified with form ID "
							+ formId + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}



	/**
	 * Updates the response draft info.
	 * 
	 * @param response
	 *            the Response object to update
	 * @throws ObjectNotFoundException
	 *             is thrown if the ResponseDraft does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public void updateResponseDraft(Response response,User loggedInUser)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		PreparedStatement stmt3 = null;
		logger.info("Updating Response draft to ResponseManagerDao->updateResponseDraft(Response response)->AdministeredFormId:\t"+response.getAdministeredForm().getId());
		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("update responsedraft set updateddate = CURRENT_TIMESTAMP ");
			sql.append("where dataentrydraftid = ? ");
			sql.append("and questionid = ? ");
			sql.append("and questionversion = ? ");
			sql.append("and sectionid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, response.getAdministeredForm()
					.getDataEntryDraftId());
			stmt.setLong(2, response.getQuestion().getId());
			stmt.setInt(3, response.getQuestion().getVersion()
					.getVersionNumber());
			stmt.setLong(4, response.getQuestion().getSectionId());

			stmt.executeUpdate();
			
			int dataEntryFlag = this.getDataEntryFlag(response.getAdministeredForm().getDataEntryDraftId());
			String entryStatus = null;
			
			if(dataEntryFlag == 1){
				 entryStatus = response.getAdministeredForm().getEntryOneStatus();
			}else{
				entryStatus = response.getAdministeredForm().getEntryTwoStatus();
			}
			
			//added by yogi
			StringBuffer sb = new StringBuffer();
			if(response.getAdministeredForm().isMarkAsCompleted()){
				if(entryStatus == null || !entryStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_LOCKED)){
					sb.append("update  dataentrydraft set updateddate = CURRENT_TIMESTAMP, coll_status = "+"'"+CtdbConstants.DATACOLLECTION_STATUS_COMPLETED+"', completeddate = CURRENT_TIMESTAMP, completedby=?"+" where dataentrydraftid = ?");
					 stmt3 = this.conn.prepareStatement(sb.toString());
					 stmt3.setLong(1,loggedInUser.getId());
					 	stmt3.setLong(2, response.getAdministeredForm().getDataEntryDraftId());
						stmt3.executeUpdate();
						logger.info("Updating  dataentrydraft status to:\t"+CtdbConstants.DATACOLLECTION_STATUS_COMPLETED);
				}
			}else{
				sb.append("update  dataentrydraft set updateddate = CURRENT_TIMESTAMP, coll_status = "+"'"+CtdbConstants.DATACOLLECTION_STATUS_INPROGRESS+"'"+" where dataentrydraftid = ?");	
				 stmt3 = this.conn.prepareStatement(sb.toString());
					stmt3.setLong(1, response.getAdministeredForm().getDataEntryDraftId());
					stmt3.executeUpdate();
					logger.info("Updating  dataentrydraft status to:\t"+CtdbConstants.DATACOLLECTION_STATUS_INPROGRESS);
			}
			
			//stmt.executeUpdate();
		
			//to update date based on update to form
			
		} catch (SQLException e) {
			throw new CtdbException("Unable to update the response draft: "
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
			this.close(stmt3);
		}
	}
	
	public void updateResponsesDraft(List<Response> responses, User loggedInUser)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		PreparedStatement stmt2 = null;
		
		String entryStatus = null;
		
		
		try {
			StringBuffer sql = new StringBuffer(200);
			
			sql.append("update responsedraft as rd ");
			sql.append("set ");
			sql.append("updateddate = c.updateddate ");
			sql.append("from (values ");
			
			for(int i=0; i < responses.size(); i++){
				sql.append("(CURRENT_TIMESTAMP, ?, ?, ?, ?)");
				
				if(i != (responses.size() -1)){
					sql.append(", ");
				}
				else{
					sql.append(")");
				}
			}
			
			sql.append(" as c(updateddate, dataentrydraftid, questionid, questionversion, sectionid) ");
			sql.append("where c.dataentrydraftid = rd.dataentrydraftid ");
			sql.append("and c.questionid = rd.questionid ");
			sql.append("and c.questionversion = rd.questionversion ");
			sql.append("and c.sectionid = rd.sectionid");
			
			
			stmt = this.conn.prepareStatement(sql.toString());
			
			int index = 0;
			for(Response response : responses){
				
				stmt.setLong(index+1, response.getAdministeredForm()
						.getDataEntryDraftId());
				stmt.setLong(index+2, response.getQuestion().getId());
				stmt.setInt(index+3, response.getQuestion().getVersion()
						.getVersionNumber());
				stmt.setLong(index+4, response.getQuestion().getSectionId());
				
				index += 4;
				
			}
			
			stmt.executeUpdate();
			
			//or do this instead of all following method
			this.updateDataEntryDraft(responses, loggedInUser);

			
		} catch (SQLException e) {
			throw new CtdbException("Unable to update the response draft: "
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
			this.close(stmt2);
		}
	}
	
	private void updateDataEntryDraft(List<Response> responses, User loggedInUser) throws CtdbException{
		PreparedStatement stmt = null;
		
		String entryStatus = null;
		
		try{
			
			StringBuffer sql2 = new StringBuffer();
			
			sql2.append("update dataentrydraft as ded ");
			sql2.append("set ");
			sql2.append("updateddate = c.updateddate, ");
			sql2.append("coll_status = c.coll_status, ");
			sql2.append("completeddate = c.completeddate, ");
			sql2.append("completedby = c.completedby ");
			sql2.append("from (values ");
			
			for(int i=0; i < responses.size(); i++){
				sql2.append("(CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?)");
				
				if(i != (responses.size() -1)){
					sql2.append(", ");
				}
				else{
					sql2.append(")");
				}
			}
			
			sql2.append(" as c(updateddate, coll_status, completeddate, completedby, dataentrydraftid) ");
			sql2.append("where c.dataentrydraftid = ded.dataentrydraftid");
			
			stmt = this.conn.prepareStatement(sql2.toString());
			
			int index = 0;
			for(Response response : responses){
				
				int dataEntryFlag = this.getDataEntryFlag(response.getAdministeredForm().getDataEntryDraftId());
				
				if(dataEntryFlag == 1){
					 entryStatus = response.getAdministeredForm().getEntryOneStatus();
				}else{
					entryStatus = response.getAdministeredForm().getEntryTwoStatus();
				}
				
				if(response.getAdministeredForm().isMarkAsCompleted()){
					if(entryStatus == null || !entryStatus.equals(CtdbConstants.DATACOLLECTION_STATUS_LOCKED)){
						stmt.setString(index+1, CtdbConstants.DATACOLLECTION_STATUS_COMPLETED);
						stmt.setLong(index+2, loggedInUser.getId()); 
						stmt.setLong(index+3, response.getAdministeredForm()
								.getDataEntryDraftId());
					}
				}
				else{
					stmt.setString(index+1, CtdbConstants.DATACOLLECTION_STATUS_INPROGRESS);
					stmt.setNull(index+2, Types.INTEGER);
					stmt.setLong(index+3, response.getAdministeredForm()
							.getDataEntryDraftId());
				}
				
				index += 3;
			}
			
			stmt.executeUpdate();
			
		
		} catch (SQLException e) {
			throw new CtdbException("Unable to update the response draft: "
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Updates the final response info.
	 * 
	 * @param response
	 *            the Response object to update
	 * @param user
	 *            the User object contains the person who updates the final
	 *            response
	 * @throws ObjectNotFoundException
	 *             is thrown if the ResponseDraft does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public void updateResponse(Response response, User user)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("update response set updatedby = ?, updateddate = CURRENT_TIMESTAMP ");
			sql.append("where responseid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, user.getId());
			stmt.setLong(2, response.getId());

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to update the response: "
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	public boolean intervalExistsInProtocol(int protocolId, int intervalId)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean found = false;

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("select coalesce(count(*), 0) count from interval ");
			sql.append("where interval.protocolid = ? ");
			sql.append("and interval.intervalid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			stmt.setLong(2, intervalId);

			rs = stmt.executeQuery();

			rs.next();

			int count = rs.getInt(1);

			if (count == 1) {
				found = true;
			}

			return found;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to check if patient exists in protocol: "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public int getResponseDraftId(int dataEntryDraftId, int questionId,
			int sectionId) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);

			
			sql.append("select responsedraftid from responsedraft ");
			sql.append("where dataentrydraftid = ? ");
			sql.append("and dict_questionid = ? ");
			sql.append("and dict_sectionid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, dataEntryDraftId);
			stmt.setLong(2, questionId);
			stmt.setLong(2, sectionId);

			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new ObjectNotFoundException(
						"The response draft entry does not exist.");
			}

			return rs.getInt(1);
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to check if patient exists in protocol: "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public boolean patientExistsInProtocol(int protocolId,
			String subjectid) throws ObjectNotFoundException,
			CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean found = false;

		try {

			StringBuffer sql = new StringBuffer(50);

			sql.append("select coalesce(count(*), 0) count from patientprotocol, patient ");
			sql.append("where patientprotocol.protocolid = ? ");
			sql.append("and patientprotocol.patientid = patient.patientid ");
			sql.append("and patient.subjectid = "
					+ CtdbDao.getDecryptionFunc(subjectid) + " ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);

			rs = stmt.executeQuery();

			rs.next();

			int count = rs.getInt(1);

			if (count == 1) {
				found = true;
			}

			return found;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to check if patient exists in protocol: "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public boolean questionExistsInForm(int formId, int questionId)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean found = false;

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("select coalesce(count(*), 0) count ");
			sql.append("from section, sectionquestion ");
			sql.append("where section.formid = ? ");
			sql.append("and section.sectionid = sectionquestion.sectionid ");
			sql.append("and sectionquestion.questionid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);
			stmt.setLong(2, questionId);

			rs = stmt.executeQuery();

			rs.next();

			int count = rs.getInt(1);

			if (count == 1) {
				found = true;
			}

			return found;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to check if question exists in form: "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	

	/**
	 * Transforms a ResulSet object into a AdministeredForm object
	 * 
	 * @param rs
	 *            ResultSet to transform to AdministeredForm object
	 * @return AdministeredForm data object
	 * @throws SQLException
	 *             thrown if any errors occur while retrieving data from result
	 *             set
	 */
	private AdministeredForm rsToAdministeredForm(ResultSet rs)
			throws SQLException {
		AdministeredForm admForm = new AdministeredForm();
		admForm.setId(rs.getInt("administeredformid"));

		Patient patient = new Patient();
		patient.setId(rs.getInt("patientid"));
		patient.setVersion(new Version(rs.getInt("patientversion")));
		admForm.setPatient(patient);

		Interval interval = new Interval();
		
		// modified by Ching Heng for fixing "Other" visit type
		// In database, the id of "Other" is null, so getInt() will be "0"
		// For consistence, we transform 0 to -1
		if (rs.getInt("intervalid") == 0) {
			interval.setId(-1);
		} else {
			interval.setId(rs.getInt("intervalid"));
		}

		admForm.setInterval(interval);

		
		int eformid = rs.getInt("eformid");
		admForm.setEformid(eformid); 
		

		int finalLockBy = rs.getInt("finallockby");
		
		if (finalLockBy != 0) {
			admForm.setFinalLockBy(finalLockBy);
		}
		
		Timestamp finalLockDate = rs.getTimestamp("finallockdate");
		
		if (finalLockDate != null) {
			admForm.setFinalLockDate(finalLockDate);
		}
		
		Timestamp visitDate = rs.getTimestamp("visitdate");
		
		if (visitDate != null) {
			admForm.setVisitDate(visitDate);
		}

		admForm.setCAT(Boolean.valueOf(rs.getBoolean("is_cat")));
		
		
		Timestamp scheduledVisitDate = rs.getTimestamp("scheduledvisitdate");
		
		if (scheduledVisitDate != null) {
			admForm.setScheduledVisitDate(scheduledVisitDate);
		}
		
		return admForm;
	}

	/**
	 * Transforms a ResulSet object into a Response object
	 * 
	 * @param rs
	 *            ResultSet to transform to Response object
	 * @return Response data object
	 * @throws SQLException
	 *             thrown if any errors occur while retrieving data from result
	 *             set
	 */
	private Response rsToResponseDraft(ResultSet rs) throws SQLException {

		Question question = new Question();

		question.setId(rs.getInt("dict_questionid"));
		//question.setVersion(new Version(rs.getInt("questionversion")));
		question.setSectionId(rs.getInt("dict_sectionid"));
	
			Response response = new Response();
			int responseDraftId = rs.getInt("responsedraftid");
			response.setId(responseDraftId);
			response.setCreatedDate(rs.getTimestamp("createddate"));
			response.setUpdatedDate(rs.getTimestamp("updateddate"));
			response.setQuestion(question);
			
			//new  NISH
			Response response1 = new Response();
			response1.setId(responseDraftId);
			response1.setQuestion(question);
			response.setResponse1(response1);
			
			return response;
		//}

	}

	/**
	 * Transforms a ResulSet object into a Response object
	 * 
	 * @param rs
	 *            ResultSet to transform to Response object
	 * @return Response data object
	 * @throws SQLException
	 *             thrown if any errors occur while retrieving data from result
	 *             set
	 */
	private Response rsToResponseLocked(ResultSet rs) throws SQLException {

		Question question = new Question();

		question.setId(rs.getInt("dict_questionid"));
		
		question.setSectionId(rs.getInt("dict_sectionid"));
		
			Response response = new Response();
			int responseId = rs.getInt("responseid");
			response.setId(responseId);
	
			Response response1 = new Response();
			response1.setId(responseId);
			response1.setQuestion(question);
			response.setResponse1(response1);

			return response;
	}

	/**
	 * Transforms a ResulSet object into a Response object
	 * 
	 * @param rs
	 *            ResultSet to transform to Response object
	 * @return Response data object
	 * @throws SQLException
	 *             thrown if any errors occur while retrieving data from result
	 *             set
	 */
	private Response rsToResponseEdit(ResultSet rs) throws SQLException {
		Response response = new Response();

		response.setId(rs.getInt("responseid"));

		String editReason = rs.getString("editreason");
		if (editReason != null) {
			response.setEditReason(editReason);
		} else
			response.setEditReason("");

	

		response.setUpdatedByUsername(rs.getString("username"));

		Timestamp editedDate = rs.getTimestamp("editeddate");
		if (editedDate != null) {
			response.setEditedDate(editedDate);
		}

		int version = rs.getInt("version");
		if (version != 0) {
			response.setVersion(new Version(version));
		}

		String answer = null;
		answer = rs.getString("edit");

		if (answer == null)
			answer = "";

		List answers = new ArrayList();
		answers.add(answer);
		response.setAnswers(answers);

		return response;
	}

	private AdministeredForm rsToAdministeredFormToResume(ResultSet rs)
			throws SQLException {
		AdministeredForm admForm = this.rsToAdministeredFormInProgress(rs);
		// request is for all inprogress forms to dispaly on protocol inbox
		admForm.getForm().setName(rs.getString("formname"));
		admForm.getForm().setProtocolId(rs.getInt("protocolid"));
		admForm.getForm().getProtocol()
				.setPatientDisplayType(rs.getInt("patientdisplaytype"));
		admForm.getForm().getProtocol()
				.setProtocolNumber(rs.getString("protocolnumber"));
	

		return admForm;
	}

	/**
	 * Transforms a ResulSet object into a AdministeredForm object, with more
	 * patient and interval information.
	 * 
	 * @param rs
	 *            ResultSet to transform to AdministeredForm object
	 * @return AdministeredForm data object
	 * @throws SQLException
	 *             thrown if any errors occur while retrieving data from result
	 *             set
	 */
	private AdministeredForm rsToAdministeredFormInProgress(ResultSet rs)
			throws SQLException {
		AdministeredForm admForm = this.rsToAdministeredForm(rs);
		admForm.getPatient().setSubjectId(rs.getString("subjectid"));
		admForm.getPatient().setLastName(rs.getString("lastname"));
		admForm.getPatient().setFirstName(rs.getString("firstname"));
		admForm.getInterval().setName(rs.getString("intervalname"));

		return admForm;
	}

	/**
	 * Retrieves the administered form meta data. Responses are not included.
	 * 
	 * @param administeredFormId
	 *            the administered form ID for retrieving the AdministeredForm
	 * @return AdministeredForm domain object which contains responses
	 * @throws ObjectNotFoundException
	 *             thrown if the administered form does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public AdministeredForm getAdministeredFormMetaData(int administeredFormId)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select administeredform.*, response.responseid as response_responseid from administeredform, response ");
			sql.append("where administeredform.administeredformid = response.administeredformid ");
			sql.append("and administeredform.administeredformid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredFormId);

			rs = stmt.executeQuery();
			if (!rs.next()) {
				throw new ObjectNotFoundException(
						"The administered form with ID: " + administeredFormId
								+ " could not be found.");
			}

			AdministeredForm admForm = this.rsToAdministeredForm(rs);

			return admForm;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get administered form with ID "
					+ administeredFormId);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves the administered form meta data. Responses are not included.
	 * 
	 * @param administeredFormId
	 *            the administered form ID for retrieving the AdministeredForm
	 * @return AdministeredForm domain object which contains responses
	 * @throws ObjectNotFoundException
	 *             thrown if the administered form does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public AdministeredForm getAdministeredFormMetaDataNoResponses(
			int administeredFormId) throws ObjectNotFoundException,
			CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select * from administeredform where administeredformid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredFormId);

			rs = stmt.executeQuery();
			if (!rs.next()) {
				throw new ObjectNotFoundException(
						"The administered form with ID: " + administeredFormId
								+ " could not be found.");
			}

			AdministeredForm admForm = this.rsToAdministeredForm(rs);

			return admForm;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get administered form with ID "
					+ administeredFormId);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves the most updated responses from the response table.
	 * 
	 * @param af
	 *            The administered form
	 * @param isGettingEdit
	 *            if get the edited data
	 * @return List the list of the answers for the response draft id
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public List<Response> getUpdatedResponses(AdministeredForm af, boolean isGettingEdit)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("select a.* from response a ");
			sql.append("where administeredformid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, af.getId());

			rs = stmt.executeQuery();
			List<Response> responses = new ArrayList<Response>();

			while (rs.next()) {

				Response response;

				response = this.rsToResponseSimple(rs);
				response.setQuestion(af.getForm().getQuestion(
						rs.getInt("questionid")));
				List<String> answers = new ArrayList<String>();
				if (!isGettingEdit) {
					answers = this.getFirstVersionAnswers(af.getId(), response);
					if (answers.isEmpty()) {
						// no edit is done yet
						answers = this.getAnswers(response.getId());
					}
				} else {
					// getting the most current answer
					answers = this.getAnswers(response.getId());
				}
				if (!answers.isEmpty()) {
					response.setAnswers(answers);
				}

				responses.add(response);

			}

			return responses;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get responses with administeredformid "
							+ af.getId() + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves the answers from the patientresponse table
	 * 
	 * @param responseId
	 *            the response id
	 * @return List the list of the answers
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	private List<String> getAnswers(int responseId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> answers = new ArrayList<String>();

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select * from patientresponse ");
			sql.append("where responseid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, responseId);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				answers.add(rs.getString("answer"));
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get answers for response with id " + responseId + " : " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return answers;
	}

	/**
	 * Retrieves the answers from the patientresponse table
	 * 
	 * @param administeredformid
	 *            the response id
	 * @return List the list of the answers
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	private List<String> getAnswers(int administeredformid, int questionid,
			int questionversion) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select pr.answer from patientresponse pr, response r ");
			sql.append("where pr.responseid = r.responseid ");
			sql.append(" and r.administeredformid = ?  and r.dict_questionid = ? and r.questionversion = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredformid);
			stmt.setLong(2, questionid);
			stmt.setInt(3, questionversion);

			rs = stmt.executeQuery();
			List<String> answers = new ArrayList<String>();
			while (rs.next()) {
				answers.add(rs.getString("answer"));
			}
			return answers;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get answers for response "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	
	
	
	
	
	/**
	 * Retrieves the answers from the patientresponse table
	 * 
	 * @param administeredformid
	 *            the response id
	 * @return List the list of the answers
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	private List<String> getDraftAnswers(int administeredformid, int questionid,
			int questionversion) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select prd.answer from patientresponsedraft prd, responsedraft rd, dataentrydraft ded ");
			sql.append(" where prd.responsedraftid = rd.responsedraftid ");
			sql.append(" and rd.dataentrydraftid = ded.dataentrydraftid ");
			sql.append(" and ded.administeredformid = ?  and rd.dict_questionid = ? and rd.questionversion = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredformid);
			stmt.setLong(2, questionid);
			stmt.setInt(3, questionversion);

			rs = stmt.executeQuery();
			List<String> answers = new ArrayList<String>();
			while (rs.next()) {
				answers.add(rs.getString("answer"));
			}
			return answers;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get answers for response "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	private List<String> getResolveAnswersForAudit(int aFormId, Question q)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
	
			
			sql.append("select * from RESPONSEEDIT ");
			sql.append("where administeredformid = ? ");
			sql.append(" and dict_questionid = ? ");
			sql.append(" and questionversion = ? ");
			sql.append("and responseeditversion = 1 ");
			sql.append("order by updateddate ");
			
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, aFormId);
			stmt.setLong(2, q.getId());
			stmt.setInt(3, q.getVersion().getVersionNumber());

			rs = stmt.executeQuery();
			List<String> answers = new ArrayList<String>();
			while (rs.next()) {
				answers.add(rs.getString("previousanswer"));
			}
			return answers;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get answers for response with adminFormid "
							+ aFormId + " for resolve answer : "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves the answers for one respones from the patientadminresponse
	 * table where version equals 1.
	 * 
	 * @param admFormId
	 *            the responseId set by this function
	 * @return a string List of answers if there is any. May be null.
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	private List<String> getFirstVersionAnswers(int admFormId, Response r)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(200);
		
			
			sql.append("select previousanswer ");
			sql.append("from responseedit ");
			sql.append("where administeredformid = ? and responseeditversion = 1");
			sql.append(" and dict_questionid = ? and questionversion = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admFormId);
			stmt.setLong(2, r.getQuestion().getId());
			stmt.setInt(3, r.getQuestion().getVersion().getVersionNumber());

			rs = stmt.executeQuery();

			List<String> answers = new ArrayList<String>();
			while (rs.next()) {
				answers.add(rs.getString("previousanswer"));
			}
			return answers;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get version one edit answers for administered form with id "
							+ admFormId + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves all data entries for the administered form.
	 * 
	 * @param admFormId
	 *            The administered form ID for retrieving the data entry.
	 * @return List the list of data entry information including the reassign
	 *         info and lock info.
	 * @throws CtdbException
	 *             thrown if any errors occur
	 */
	public List<DataEntryDraft> getDataEntryDrafts(int admFormId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			List<DataEntryDraft> dataEntries = new ArrayList<DataEntryDraft>();

			StringBuffer sql = new StringBuffer(200);
			sql.append("select * from dataentrydraft where administeredformid = ?");
			// + admFormId);
			sql.append(" order by dataentryflag ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admFormId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				DataEntryDraft de = new DataEntryDraft();
				de.setId(rs.getInt("dataentrydraftid"));
				de.setAdministeredFormId(rs.getInt("administeredformid"));
				de.setDataEnteredFlag(rs.getInt("dataentryflag"));
				de.setCreatedDate(rs.getTimestamp("createddate"));
				de.setDataEnteredBy(rs.getInt("dataenteredby"));
				dataEntries.add(de);
			}
			return dataEntries;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get the data entry draft list for the administered form id "
							+ admFormId + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	
	
	
	
	
	/**
     * Retrieves all data entries for the data entry 1 or 2. The list includes
     * the reassign information and data entry lock information. This is for
     * View Audit for who has the View Data Entry Audit Trail privilege.
     * 
      * @param admFormId
     *            The administered form ID for retrieving the data entry.
     * @param dataEntryFlag
     *            data entry 1 or 2
     * @return List the list of data entry information including the reassign
     *         info and lock info.
     * @throws CtdbException
     *             thrown if any errors occur
     */
     public List<DataEntryDraft> getDataEntries(int admFormId, int dataEntryFlag) throws CtdbException {
                    PreparedStatement stmt = null;
                    PreparedStatement stmt2 = null;
                    PreparedStatement stmt3 = null;
                    ResultSet rs = null;
                    ResultSet rs2 = null;
                    ResultSet rs3 = null;

                    try {

                    	// check for Reassign info
                    	List<DataEntryDraft> dataEntries = this.getReassignInfo(admFormId, dataEntryFlag);

                    	StringBuffer sql = new StringBuffer(200);
                    	sql.append("select d.*, u.firstname, u.lastname from dataentrydraft d, usr u ");
                    	sql.append("where d.administeredformid = ?");
                    	sql.append(" and d.dataentryflag = ?");
                    	sql.append(" and d.dataenteredby = u.usrid ");

                    	stmt = this.conn.prepareStatement(sql.toString());
                    	stmt.setLong(1, admFormId);
                    	stmt.setInt(2, dataEntryFlag);
                    	rs = stmt.executeQuery();

                    	DataEntryDraft dataEntry = new DataEntryDraft();
                    	//String lockByUsername = null;
                    	if (rs.next()) {

                    		int dataEnteredBy = rs.getInt("dataenteredby");
                    		String userFullName = "";
                    		if(dataEnteredBy == -1) {
                    			userFullName = CtdbConstants.SUBJECT_TITLE_DISPLAY;
                    		}else {
                    			userFullName = rs.getString("lastname") + ", "+ rs.getString("firstname");
                    		}
                    		
                    		dataEntry.setDataEnteredByName(userFullName);
                    		//dataEntry.setStatus(CtdbConstants.DATACOLLECTION_STATUS_STARTED);
                    		dataEntry.setCreatedDate(rs.getTimestamp("createddate"));
                    		//lockByUsername = rs.getString("username");
                    		dataEntries.add(dataEntry);

                    		// if there are some reassign info, need to get the correct
                    		// start date/time
                    		if (!dataEntries.isEmpty() && dataEntries.size() > 1) {
                    			int i = 0;
                    			Date deDraftDate = null;
                    			for (Iterator it = dataEntries.iterator(); it.hasNext();) {
                    				i++;
                    				DataEntryDraft deDraft = (DataEntryDraft) it.next();
                    				if (i == 1) {
                    					deDraftDate = deDraft.getCreatedDate();
                    					deDraft.setCreatedDate(rs.getTimestamp("createddate"));
                    					deDraft.setStatus(CtdbConstants.DATACOLLECTION_STATUS_STARTED);
                    				} else {
                    					Date temp = deDraft.getCreatedDate();
                    					deDraft.setCreatedDate(deDraftDate);
                    					deDraft.setStatus(CtdbConstants.DATACOLLECTION_STATUS_REASSIGNED);
                    					deDraftDate = temp;
                    				}
                    			}
                    		}else if(!dataEntries.isEmpty()) { //only 1 entry
                    			dataEntries.get(0).setStatus(CtdbConstants.DATACOLLECTION_STATUS_STARTED); 
                    		}

                    		



                    		// for Lock info
                    		if (rs.getDate("completeddate") != null && rs.getInt("completedby")!=0) {
                    			dataEntry = new DataEntryDraft();
                    			int completedById = rs.getInt("completedby");
                    			StringBuffer sql2 = new StringBuffer(200);
                    			sql2.append("select firstname, lastname from usr where usrid = ?");
                    			stmt2 = this.conn.prepareStatement(sql2.toString());
                    			stmt2.setLong(1, completedById);
                    			rs2 = stmt2.executeQuery();

                    			if (rs2.next()) {

                    				String completedByUsername = "";
                    				if(completedById == -1) {
                    					completedByUsername = CtdbConstants.SUBJECT_TITLE_DISPLAY;
                            		}else {
                            			completedByUsername = rs2.getString("lastname") + ", "+ rs2.getString("firstname");
                            		}
                    				dataEntry.setDataEnteredByName(completedByUsername);
                    				dataEntry.setCreatedDate(rs.getTimestamp("completeddate"));
                    				dataEntry.setStatus(CtdbConstants.DATACOLLECTION_STATUS_COMPLETED);
                    				//dataEntry.setNumQuestionsAnswered(this.getNumOfQuestionsAnsweredForDataEntry(admFormId,dataEntryFlag));
                    				dataEntries.add(dataEntry);
                    			}
                    		}

                    		// for Lock info
                    		if (rs.getDate("lockdate") != null) {
                    			dataEntry = new DataEntryDraft();
                    			int lockedById = rs.getInt("lockedby");
                    			StringBuffer sql3 = new StringBuffer(200);
                    			sql3.append("select firstname, lastname from usr where usrid = ?");
                    			stmt3 = this.conn.prepareStatement(sql3.toString());
                    			stmt3.setLong(1, lockedById);
                    			rs3 = stmt3.executeQuery();

                    			if (rs3.next()) {
                    				String lockedByUsername = "";
                    				if(lockedById == -1) {
                    					lockedByUsername = CtdbConstants.SUBJECT_TITLE_DISPLAY;
                            		}else {
                            			lockedByUsername = rs3.getString("lastname") + ", "+ rs3.getString("firstname");
                            		}
                    				dataEntry.setDataEnteredByName(lockedByUsername);
                    				dataEntry.setCreatedDate(rs.getTimestamp("lockdate"));
                    				dataEntry.setStatus(CtdbConstants.DATACOLLECTION_STATUS_LOCKED);
                    				dataEntry.setNumQuestionsAnswered(this.getNumOfQuestionsAnsweredLocked(admFormId));
                    				dataEntries.add(dataEntry);
                    			}
                    		}
                    	}
                    	return dataEntries;
                    } catch (SQLException e) {
                    	throw new CtdbException("Unable to get the data entry list: "
                    			+ e.getMessage(), e);
                    } finally {
                    	this.close(rs);
                    	this.close(rs2);
                    	this.close(rs3);
                    	this.close(stmt);
                    	this.close(stmt2);
                    	this.close(stmt3);
                    }
     }


	/**
	 * Gets the list of reassign data entry info for the data entry 1 or 2.
	 * 
	 * @param admFormId
	 *            The administered form ID for retrieving the reassign info
	 * @param dataEntryFlag
	 *            data entry 1 or 2
	 * @return List of reassign info
	 * @throws CtdbException
	 *             thrown if any errors occur
	 */
	private List<DataEntryDraft> getReassignInfo(int admFormId, int dataEntryFlag) throws CtdbException {
		List<DataEntryDraft> reassigns = new ArrayList<DataEntryDraft>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("select a.previousby, a.currentby, a.reassignby, a.assigneddate, c.username currentbyuser ");
			sql.append("from dataentryreassignarchive a, dataentrydraft b, usr c ");
			sql.append("where a.dataentrydraftid = b.dataentrydraftid ");
			sql.append("and b.administeredformid = ?");
			sql.append(" and b.dataentryflag = ?");
			sql.append(" and c.usrid = a.currentby ");
			sql.append("order by assigneddate ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admFormId);
			stmt.setInt(2, dataEntryFlag);
			rs = stmt.executeQuery();

			while (rs.next()) {
				DataEntryDraft dataEntry = new DataEntryDraft();
				dataEntry.setCreatedDate(rs.getTimestamp("assigneddate"));
				dataEntry.setDataEnteredByName(this.getUserLastNameFirstName(rs.getInt("previousby")));
				reassigns.add(dataEntry);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get the reassign info list: "+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return reassigns;
	}

	private String getUsername(int userId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String name = "";

			StringBuffer sql = new StringBuffer(200);
			sql.append("select username from usr ");
			sql.append("where usrid = ?");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, userId);
			rs = stmt.executeQuery();

			if (rs.next()) {
				name = rs.getString(1);
			}
			return name;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get username for the userid "
					+ userId + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	
	public String getUserLastNameFirstName(int userId) throws CtdbException {
		if(userId == -1) {
			return CtdbConstants.SUBJECT_TITLE_DISPLAY;
		}
		
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String name = "";

			StringBuffer sql = new StringBuffer(200);
			sql.append("select lastname, firstname from usr ");
			sql.append("where usrid = ?");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, userId);
			rs = stmt.executeQuery();

			if (rs.next()) {
				name = rs.getString("lastname") + ", " + rs.getString("firstname");
			}
			return name;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get username for the userid "
					+ userId + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Gets the number of questions answered in draft/completed
	 * 
	 * @param formId
	 *            the administered form id
	 * @return int the number of question completed after the lock
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public int getNumOfQuestionsAnsweredForDataEntry(int formId, int dataEntry)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			int numQuestions = 0;

			StringBuffer sql = new StringBuffer(200);
			sql.append("select count(distinct c.responsedraftid) ");
			sql.append("from administeredform a, dataentrydraft b, responsedraft c, patientresponsedraft d ");
			sql.append("where a.administeredformid = ? ");
			sql.append("and a.administeredformid = b.administeredformid ");
			sql.append("and b.dataentrydraftid = c.dataentrydraftid ");
			sql.append("and b.dataentryflag = ? ");
			sql.append("and c.responsedraftid = d.responsedraftid ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);
			stmt.setInt(2, dataEntry);
			rs = stmt.executeQuery();

			if (rs.next()) {
				numQuestions = rs.getInt(1);
			}
			return numQuestions;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get number of questions answered for data entry "
							+ dataEntry + " with administered form ID "
							+ formId + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	
	
	
	
	/**
	 * Gets the number of questions answered in draft/completed
	 * 
	 * @param formId
	 *            the administered form id
	 * @return int the number of question completed after the lock
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public int getNumOfQuestionsAnsweredLocked(int aformId)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			int numQuestions = 0;

			StringBuffer sql = new StringBuffer(200);
			sql.append("select count(distinct c.responseid) ");
			sql.append("from administeredform a, response c, patientresponse d ");
			sql.append("where a.administeredformid = ? ");
			sql.append("and a.administeredformid = c.administeredformid ");
			sql.append("and c.responseid = d.responseid ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, aformId);
			rs = stmt.executeQuery();

			if (rs.next()) {
				numQuestions = rs.getInt(1);
			}
			return numQuestions;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get number of questions loccked");
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves all responses for an administered form.
	 * 
	 * @param admFormId
	 *            The administered form ID for retrieving all the responses
	 * @return Map the map of Response objects for the admFormId with its id as
	 *         key.
	 * @throws CtdbException
	 *             thrown if any errors occur
	 */
	public Map<String, Response> getResponses(int admFormId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			
			sql.append("select responseid, dict_questionid, questionversion from response ");
			sql.append("where administeredformid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admFormId);
			rs = stmt.executeQuery();

			Response response = null;
			QuestionManagerDao qDao = QuestionManagerDao.getInstance(this.conn);
			Question question = null;
			int responseId = Integer.MIN_VALUE;
			Map<String, Response> respMap = new HashMap<String, Response>();

			while (rs.next()) {
				response = new Response();
				responseId = rs.getInt("responseid");
				response.setId(responseId);

				question = qDao.getQuestion(rs.getInt("questionid"),
						rs.getInt("questionversion"));
				response.setQuestion(question);
				respMap.put(Integer.toString(responseId), response);
			}

			return respMap;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get the response list: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public PatientCalendarCellResponse getFinalCalendarResponse(int id)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		PatientCalendarCellResponse pccr = new PatientCalendarCellResponse();

		try {
			StringBuffer sql = new StringBuffer(100);

			sql.append("select a.*, b.*, c.answer ");
			sql.append("from response a, patientcalendar b, patientresponse c ");
			sql.append("where a.responseid = ? ");
			sql.append("and a.responseid = b.responseid ");
			sql.append("and a.responseid = c.responseid ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, id);
			rs = stmt.executeQuery();

			if (rs.next()) {
				pccr.setId(rs.getInt("responseid"));
				Question question = new Question();
				question.setId(rs.getInt("questionid"));
				question.setVersion(new Version(rs.getInt("questionversion")));
				pccr.setQuestion(question);
				PatientCalendarCellResponse dataEntry1 = new PatientCalendarCellResponse();
				dataEntry1.setId(rs.getInt("responsedraftid1"));
				pccr.setResponse1(dataEntry1);
				PatientCalendarCellResponse dataEntry2 = new PatientCalendarCellResponse();
				dataEntry2.setId(rs.getInt("responsedraftid2"));
				pccr.setResponse2(dataEntry2);
				pccr.setRow(rs.getInt("rownumber"));
				pccr.setCol(rs.getInt("colnumber"));
				ArrayList answers = new ArrayList();
				answers.add(rs.getString("answer"));
				pccr.setAnswers(answers);
			} else {
				throw new SQLException();
			}

			return pccr;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get the response: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public PatientCalendarCellResponse getDraftCalendarResponse(int id)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		PatientCalendarCellResponse pccr = new PatientCalendarCellResponse();

		try {
			StringBuffer sql = new StringBuffer(100);

			sql.append("select a.*, b.answer, c.* ");
			sql.append("from responsedraft a, patientresponsedraft b, question c ");
			sql.append("where a.responsedraftid = ? ");
			sql.append("and a.responsedraftid = b.responsedraftid ");
			sql.append("and a.questionid = c.questionid ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, id);
			rs = stmt.executeQuery();

			if (rs.next()) {
				pccr = (PatientCalendarCellResponse) this.rsToResponseDraft(rs);
				ArrayList answers = new ArrayList();
				answers.add(rs.getString("answer"));
				pccr.setAnswers(answers);
			} else {
				return new PatientCalendarCellResponse();
			}

			return pccr;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get the response draft: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves edit archives for a list of response ids.
	 * 
	 * @param admFormId
	 *            The administered form id for retrieving the edit archives for
	 *            the answers.
	 * @return Map the hashMap of Response objects from all editing history.
	 * @throws CtdbException
	 *             thrown if any errors occur
	 */
	public List<EditAnswerDisplay> getEditArchives(int admFormId, boolean isFinalLocked) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(100);

			sql.append("select re.*, q.name, q.text, q.type, u.username, s.name as sectionname, qa.data_element_name  ");
			sql.append(" from responseedit re, question q, usr u, section s, sectionquestion sq, questionattributes qa ");
			sql.append(" where re.administeredformid = ? ");
			sql.append(" and re.questionid = q.questionid ");
			
			sql.append(" and re.sectionid = s.sectionid ");
			sql.append(" and s.sectionid = sq.sectionid ");
			sql.append(" and re.questionid = sq.questionid ");
			sql.append(" and sq.questionattributesid = qa.questionattributesid ");
			sql.append(" and re.updatedby = u.usrid ");
			sql.append(" order by  re.questionid, re.questionversion, re.responseeditversion desc, re.updateddate ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admFormId);
			rs = stmt.executeQuery();
			List<EditAnswerDisplay> edits = new ArrayList<EditAnswerDisplay>();
			int questionid = Integer.MIN_VALUE;
			int questionVersion = Integer.MIN_VALUE;
			int editversion = Integer.MIN_VALUE;
			for (int i = 0; rs.next();) {
				if (rs.getInt("questionid") == questionid
						&& rs.getInt("questionversion") == questionVersion
						&& editversion == rs.getInt("responseeditversion")) {
					// this row is the same as the last, meaning its a multiple
					// choice question
					edits.get(i - 1).getPreviousAnswer().add(rs.getString("previousanswer"));
				} else {
					// it's a new edit
					EditAnswerDisplay ead = new EditAnswerDisplay();
					ead.setEditDate(rs.getTimestamp("updateddate"));
					ead.getPreviousAnswer().add(rs.getString("previousAnswer"));
					ead.setQuestionId(rs.getInt("questionid"));
					ead.setQuestionName(rs.getString("name"));
					ead.setQuestionText(rs.getString("text"));
					ead.setSectionId(rs.getInt("sectionid"));
					
					ead.setSectionName(rs.getString("sectionname"));
					ead.setDataElementName(rs.getString("data_element_name")); 
					ead.setQuestionType(QuestionType.getByValue(
							rs.getInt("type")).toString());
					ead.setReasonForEdit(rs.getString("editreason"));
					ead.setUsername(rs.getString("username"));
					ead.setAdministeredformId(rs.getInt("administeredformid"));
					if (rs.getInt("questionid") == questionid
							&& rs.getInt("questionversion") == questionVersion) {
						// its the same question / version but different edit
						ead.setEditedAnswer(edits.get(i - 1).getPreviousAnswer());
					} else {
						// its the last edit of this question
						if (isFinalLocked) {
							ead.setEditedAnswer(
									getAnswers(admFormId, rs.getInt("questionid"), rs.getInt("questionVersion")));
						} else {
							ead.setEditedAnswer(
									getDraftAnswers(admFormId, rs.getInt("questionid"), rs.getInt("questionVersion")));
						}
					}
					i++;
					questionid = rs.getInt("questionid");
					questionVersion = rs.getInt("questionversion");
					editversion = rs.getInt("responseeditversion");
					edits.add(ead);
				}
			}

			return edits;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get the response edit archive list.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	
	
	
	
	
	/**
	 * Retrieves edit archives for a list of response ids.
	 * 
	 * @param admFormId
	 *            The administered form id for retrieving the edit archives for
	 *            the answers.
	 * @return Map the hashMap of Response objects from all editing history.
	 * @throws CtdbException
	 *             thrown if any errors occur
	 */
	public List<EditAnswerDisplay> getEditArchives(Form form, int admFormId, boolean isFinalLocked)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<EditAnswerDisplay> edits = new ArrayList<EditAnswerDisplay>();

		try {
			//note: "and editreason is not null"
			if(isFinalLocked) {
				String sql = 
						"select * 																																						 "
								+ "from (select userfullname, username, updateddate, administeredformid,dict_sectionid,dict_questionid,responseeditversion, prevanswer, editreason,  coll_status,         			"
								+ "				LEAD(prevanswer, 1, null) OVER (PARTITION by administeredformid,dict_sectionid,dict_questionid ORDER BY responseeditversion NULLS LAST) AS curranswer,  "
								+ "             LEAD(prevsubmitanswer, 1, null) OVER (PARTITION by administeredformid,dict_sectionid,dict_questionid ORDER BY responseeditversion NULLS LAST) AS currentsubmitanswer "
								+ "		 from (select u.lastname||', '||u.firstname as userfullname, u.username, re.updateddate, re.administeredformid,re.dict_questionid,                        		"
								+ "					 re.dict_sectionid, re.editreason, STRING_AGG(re.previousanswer, ', '  ) as prevanswer, STRING_AGG(re.previousanswer, ', '  ) as prevsubmitanswer,  "
								+ "					 re.responseeditversion, re.coll_status                                 																			"
								+ "			   from responseedit re, usr u                                                                                                                        		"
								+ "			   where re.in_progress_data_flag is null and re.updatedby = u.usrid and re.auditstatus is null 															"
								+ "			   group by u.lastname, u.firstname, u.username, re.updateddate,                    																		"
								+ "					re.administeredformid,re.dict_questionid, re.dict_sectionid, re.editreason, re.responseeditversion, re.coll_status                                  "
								+ "	           union all                                                                                                                                          		"
								+ "	           select  u.lastname||', '||u.firstname as userfullname, u.username, r.updateddate, r.administeredformid, r.dict_questionid, r.dict_sectionid, null, 		"
								+ "					STRING_AGG(pr.answer, ', ' ) as currentanswer, STRING_AGG(pr.submitanswer, ', ' ) as currentsubmitanswer, null, null 								"
								+ "            from patientresponse pr, response r, usr u                                                   															"
								+ "			   where pr.responseid = r.responseid and r.updatedby = u.usrid                                                                                       		"
								+ "			   group by u.lastname, u.firstname, u.username, r.updateddate, r.administeredformid,r.dict_questionid, r.dict_sectionid                              		"
								+ "			  ) b                                                                                                                                                		"
								+ "	    ) a                                                                                                                                                         	"
								+ "where administeredformid = ? and responseeditversion is not null  and editreason is not null                                                                         "
								+ "order by administeredformid,dict_sectionid,dict_questionid, responseeditversion                                                                               		";
				stmt = this.conn.prepareStatement(sql);
				stmt.setLong(1, admFormId);
				
				rs = stmt.executeQuery();
				int questionid = Integer.MIN_VALUE;
				int questionVersion = Integer.MIN_VALUE;
				int editversion = Integer.MIN_VALUE;
				HashMap<String,Question> questionMap = form.getQuestionMap();
				HashMap<Integer,Section> sectionMap = form.getSectionMap();
				
				while(rs.next()) {

					EditAnswerDisplay ead = new EditAnswerDisplay();
					ead.setEditDate(rs.getTimestamp("updateddate"));
					if (rs.getString("userfullname") != null && !rs.getString("userfullname").equals(""))
						ead.setUsername(rs.getString("userfullname"));
					else 
						ead.setUsername(rs.getString("username"));
					
					ead.setAdministeredformId(rs.getInt("administeredformid"));
					
					int reQuestionId = rs.getInt("dict_questionid");
					int reSectionId = rs.getInt("dict_sectionid");
					String questionMapKey = "S_" + reSectionId + "_Q_" + reQuestionId;
					Question question = questionMap.get(questionMapKey);
					Section section = sectionMap.get(reSectionId);
					
					ead.setQuestionId(reQuestionId);
					ead.setSectionId(reSectionId);
					
					String name = question.getName();
					ead.setQuestionName(name);
					
					String text = question.getText();
					ead.setQuestionText(text);
					
					FormQuestionAttributes questionAttribute = question.getFormQuestionAttributes();
					
					String sectionName = section.getName();
					ead.setSectionName(sectionName);
					
					String dataElementName = questionAttribute.getDataElementName();
					ead.setDataElementName(dataElementName); 
					ead.setReasonForEdit(rs.getString("editreason"));
					
					int type = question.getType().getValue();
					
					ead.setQuestionType(QuestionType.getByValue(type).toString());
					ead.setPrevAnswer(rs.getString("prevanswer"));
					// if the question have the setting to show PV, get the submitanswer instead of answer from
					// patientresponse table
					if (question.isDisplayPV()) {
						ead.setEditAnswer(rs.getString("currentsubmitanswer"));
					} else {
						ead.setEditAnswer(rs.getString("curranswer"));
					}
					if(rs.getString("coll_status") != null) {
						ead.setCollStatus(rs.getString("coll_status"));
					}else {
						ead.setCollStatus("");
					}
					
					
					edits.add(ead);
				}
			} else {
				String sql =
						"select * 																																						"
								+ "from (select userfullname, username, updateddate, administeredformid,dict_sectionid,dict_questionid,responseeditversion, prevanswer, editreason, coll_status,            	  		"
								+ "				LEAD(prevanswer, 1, null) OVER (PARTITION by administeredformid,dict_sectionid,dict_questionid ORDER BY responseeditversion NULLS LAST) AS curranswer, 					"
								+ "             LEAD(prevsubmitanswer, 1, null) OVER (PARTITION by administeredformid,dict_sectionid,dict_questionid ORDER BY responseeditversion NULLS LAST) AS currentsubmitanswer 	"
								+ "	  	 from ( select u.lastname||', '||u.firstname as userfullname, u.username, re.updateddate, re.administeredformid,re.dict_questionid,                        						"
								+ "   				re.dict_sectionid, re.editreason, STRING_AGG(re.previousanswer, ', '  ) as prevanswer, STRING_AGG(re.previousanswer, ', '  ) as prevsubmitanswer,   				"
								+ "					re.responseeditversion, re.coll_status  from responseedit re, usr u    																								"
								+ "	  			where re.in_progress_data_flag is null and re.updatedby = u.usrid and re.auditstatus is null                                                                            "
								+ "	  			group by u.lastname, u.firstname, u.username, re.updateddate, re.administeredformid,                                                                    				"
								+ "						re.dict_questionid, re.dict_sectionid, re.editreason, re.responseeditversion, re.coll_status                                                                    "
								+ "	  			union all                                                                                                                                               "
								+ "	  			select  u.lastname||', '||u.firstname as userfullname, u.username, d.updateddate, d.administeredformid, rd.dict_questionid,                             "
								+ "					rd.dict_sectionid, null, STRING_AGG(pd.answer, ', ' ) as currentanswer, STRING_AGG(pd.submitanswer, ', ' ) as currentsubmitanswer, null, null       "
								+ "	  			from patientresponsedraft pd, responsedraft rd, dataentrydraft d, usr u                                                                                 "
								+ "	  			where pd.responsedraftid = rd.responsedraftid and d.dataentrydraftid = rd.dataentrydraftid                                                              "
								+ "					and d.dataenteredby = u.usrid                                                                                                                       "
								+ "	  			group by u.lastname, u.firstname, u.username, d.updateddate, d.administeredformid,rd.dict_questionid, rd.dict_sectionid                                 "
								+ "	 		 ) b                                                                                                                                                        "
								+ "     ) a                                                                                                                                                		        "
								+ "where administeredformid = ? and responseeditversion is not null                                                                                         		    "
								+ "order by administeredformid,dict_sectionid,dict_questionid, responseeditversion                                                                             			";
				
				stmt = this.conn.prepareStatement(sql);
				stmt.setLong(1, admFormId);
				
				rs = stmt.executeQuery();
				int questionid = Integer.MIN_VALUE;
				int questionVersion = Integer.MIN_VALUE;
				int editversion = Integer.MIN_VALUE;
				HashMap<String,Question> questionMap = form.getQuestionMap();
				HashMap<Integer,Section> sectionMap = form.getSectionMap();
				
				while(rs.next()) {
					EditAnswerDisplay ead = new EditAnswerDisplay();
					ead.setEditDate(rs.getTimestamp("updateddate"));
					if (rs.getString("userfullname") != null && !rs.getString("userfullname").equals(""))
						ead.setUsername(rs.getString("userfullname"));
					else 
						ead.setUsername(rs.getString("username"));
					ead.setAdministeredformId(rs.getInt("administeredformid"));
					
					int reQuestionId = rs.getInt("dict_questionid");
					int reSectionId = rs.getInt("dict_sectionid");
					String questionMapKey = "S_" + reSectionId + "_Q_" + reQuestionId;
					Question question = questionMap.get(questionMapKey);
					Section section = sectionMap.get(reSectionId);
					
					ead.setQuestionId(reQuestionId);
					ead.setSectionId(reSectionId);
					
					String name = question.getName();
					ead.setQuestionName(name);
					
					String text = question.getText();
					ead.setQuestionText(text);
					
					FormQuestionAttributes questionAttribute = question.getFormQuestionAttributes();
					
					String sectionName = section.getName();
					ead.setSectionName(sectionName);
					
					String dataElementName = questionAttribute.getDataElementName();
					ead.setDataElementName(dataElementName); 
					ead.setReasonForEdit(rs.getString("editreason"));
					
					int type = question.getType().getValue();
					
					ead.setQuestionType(QuestionType.getByValue(type).toString());
					ead.setPrevAnswer(rs.getString("prevanswer"));
					// if the question have the setting to show PV, get the submitanswer instead of answer from
					// patientresponsedraft table
					if (question.isDisplayPV()) {
						ead.setEditAnswer(rs.getString("currentsubmitanswer"));
					} else {
						ead.setEditAnswer(rs.getString("curranswer"));
					}
					if(rs.getString("coll_status") != null) {
						ead.setCollStatus(rs.getString("coll_status"));
					}else {
						ead.setCollStatus("");
					}
					edits.add(ead);
				}
			}
			return edits;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get the response edit archive list.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
    /** Audit Comment on "view audit" page.
     */
	public List<EditAnswerDisplay> getEditArchivesForAuditComment(Form form, int admFormId,boolean isFinalLocked) 
																					throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(100);

			sql.append("select re.*,  u.username, u.lastname||', '||u.firstname as userfullname  ");
			sql.append(" from responseedit re, usr u ");
			sql.append(" where re.administeredformid = ? ");
			sql.append(" and re.editauditcomment is not null ");
			sql.append(" and re.updatedby = u.usrid ");
			sql.append(" order by re.administeredformid, re.dict_sectionid, re.dict_questionid, re.responseeditversion  ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admFormId);
			rs = stmt.executeQuery();
			ArrayList<EditAnswerDisplay> edits = new ArrayList<EditAnswerDisplay>();
			int questionid = Integer.MIN_VALUE;
			int questionVersion = Integer.MIN_VALUE;
			int editversion = Integer.MIN_VALUE;
			
			HashMap<String,Question> questionMap = form.getQuestionMap();
			HashMap<Integer,Section> sectionMap = form.getSectionMap();
			
			
			
			
			
			for (int i = 0; rs.next();) {
				if (rs.getInt("dict_questionid") == questionid && editversion == rs.getInt("responseeditversion")) {
					// this row is the same as the last, meaning its a multiple
					// choice question
					((EditAnswerDisplay) edits.get(i - 1)).getPreviousAnswer().add(rs.getString("previousanswer"));

				} else {
					// it's a new edit
					EditAnswerDisplay ead = new EditAnswerDisplay();
					ead.setEditDate(rs.getTimestamp("updateddate"));
//					
//					if(useFinalAnswers) { //FL
//						previousAnswers = this.getFinalAnswers(responseId);
//					}else { //Completed or In Progress
//						previousAnswers = this.getDraftCompletedAnswers(responseId);
//					}
					String inprog=rs.getString("in_progress_data_flag");
					String lastPreAnswer ="";
					if(CtdbConstants.YES.equalsIgnoreCase(inprog)) {
						lastPreAnswer = getLastPreAnswerForInprogress(admFormId, rs.getInt("dict_sectionid"), rs.getInt("dict_questionid"), rs.getInt("responseeditversion"));
					}else { //FL or Completed
						lastPreAnswer = getLastPreAnswer(admFormId, rs.getInt("dict_sectionid"), rs.getInt("dict_questionid"), rs.getInt("responseeditversion"));
					}
					
					if (lastPreAnswer==null) {lastPreAnswer="";}
					ead.getPreviousAnswer().add(lastPreAnswer);					
					String previousAnswer = rs.getString("previousAnswer");
					if (previousAnswer==null) {previousAnswer="";}
					//ead.getPreviousAnswer().add(previousAnswer);
					
					int reQuestionId = rs.getInt("dict_questionid");
					int reSectionId = rs.getInt("dict_sectionid");
					String questionMapKey = "S_" + reSectionId + "_Q_" + reQuestionId;
					Question question = questionMap.get(questionMapKey);
					Section section = sectionMap.get(reSectionId);
					
					
					
					ead.setQuestionId(reQuestionId);
					ead.setSectionId(reSectionId);
					
					
					String name = question.getName();
					ead.setQuestionName(name);
					
					String text = question.getText();
					ead.setQuestionText(text);
					
					FormQuestionAttributes questionAttribute = question.getFormQuestionAttributes();
					
					String sectionName = section.getName();
					ead.setSectionName(sectionName);
					
					
					String dataElementName = questionAttribute.getDataElementName();
					ead.setDataElementName(dataElementName); 
					
					
					int type = question.getType().getValue();
					
					ead.setQuestionType(QuestionType.getByValue(type).toString());
					//ead.setReasonForEdit(rs.getString("editreason"));
					ead.setAuditcommentForEdit(rs.getString("editauditcomment"));
					ead.setAuditStatus(rs.getString("auditstatus"));
					ead.setUsername(rs.getString("userfullname"));
					ead.setAdministeredformId(rs.getInt("administeredformid"));
					ead.getEditedAnswer().add(previousAnswer);  

					i++;
					questionid = rs.getInt("dict_questionid");
					questionVersion = rs.getInt("questionversion");
					editversion = rs.getInt("responseeditversion");
					edits.add(ead);

				}
			}
			return edits;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get the response edit archive list: "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	/* Audit Comment on VQ-view queries */
	public List<EditAnswerDisplay> getEditArchivesForVq(HttpServletRequest request, int protocolId,boolean isFinalLocked) //int admFormId
																throws CtdbException {
		PreparedStatement stmt = null;
		PreparedStatement displayTypeQueryPrepStmt = null;
		ResultSet rs = null;
		ResultSet rsType = null;
        FormDataStructureUtility fsUtil = new FormDataStructureUtility();
        Form form = null;

		try {
        	//Get subject display type
        	String displayTypeQuery = "select patientdisplaytype from protocol where protocolid = ?";
        	displayTypeQueryPrepStmt = conn.prepareStatement(displayTypeQuery);
        	displayTypeQueryPrepStmt.setLong(1, protocolId);
        	rsType = displayTypeQueryPrepStmt.executeQuery();
        	int subjectDisplayType = 0;
        	while ( rsType.next() ){
        		subjectDisplayType = rsType.getInt("patientdisplaytype");
        	}
        	
			StringBuffer sql = new StringBuffer(100);
            /*max responseeditversion by dict_questionid at a form; responseeditversion is inserted by qid without sid. */
			sql.append(" select resp.*, rv.afid, rv.afname, rv.afshortname ,rv.guid ");
			sql.append("		,rv.d_mrn ,rv.d_nrn ,rv.patientid "); //, u.username, resp.responseeditversion
			sql.append(" from responseedit resp, "); // usr u,
			sql.append("	(select max(re.responseeditversion) responseeditversion ");
			sql.append("			,re.administeredformid as afid, ef.name as afname ");
			sql.append("			,ef.shortname as afshortname ");
			sql.append("        	,re.dict_sectionid, re.dict_questionid ");
			sql.append("			,p.guid ,p.mrn as d_mrn ,pr.subjectid as d_nrn ,p.patientid ");
			sql.append("	from  eform ef, administeredform af, responseedit re ,patient p ,patientprotocol pr ");
			sql.append("	where ef.eformid=af.eformid and af.administeredformid=re.administeredformid "); //and re.administeredformid = 26 --28
			sql.append("		  and ef.protocolid=? and re.editauditcomment is not null ");
			sql.append("		  and p.patientid=af.patientid and p.patientid = pr.patientid ");
			sql.append("		  and p.deleteflag=false and pr.protocolid=? ");
			sql.append("	group by re.administeredformid, ef.name, ef.shortname ");
			sql.append("			 ,re.dict_sectionid,  re.dict_questionid ");
			sql.append("		     ,p.guid, p.mrn ,pr.subjectid ,p.patientid ) as rv ");			 
			sql.append(" where resp.responseeditversion=rv.responseeditversion and resp.administeredformid=rv.afid ");
			sql.append(" 	   and resp.dict_sectionid=rv.dict_sectionid and resp.dict_questionid=rv.dict_questionid "); // and resp.updatedby = u.usrid
			sql.append(" order by rv.afid, rv.afname, rv.afshortname, resp.dict_sectionid, resp.dict_questionid ");
			sql.append("		 ,rv.guid ,rv.d_mrn ,rv.d_nrn ,rv.patientid; "); //,resp.responseeditversion desc; ,resp.updateddate

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId); //stmt.setLong(1, admFormId);
			stmt.setLong(2, protocolId);
			rs = stmt.executeQuery();
			ArrayList<EditAnswerDisplay> edits = new ArrayList<EditAnswerDisplay>();
			int questionid = Integer.MIN_VALUE;
			int questionVersion = Integer.MIN_VALUE;
			int editversion = Integer.MIN_VALUE;
			
	        String shortName = "";
	        String shortNameTemp = "";
			HashMap<String,Question> questionMap = null;
			HashMap<Integer,Section> sectionMap  = null;
			
			/*subjectId*/
        	String guid;
        	String mrn;
        	String nrn;
        	int patientid;
        	
			for (int i = 0; rs.next();) {
		        shortName = rs.getString("afshortname");
		        if(shortName!=null && !shortName.isEmpty() && !shortName.equalsIgnoreCase(shortNameTemp))
		        {
		        	form = fsUtil.getEformFromBrics(request, shortName);
		        	questionMap = form.getQuestionMap();
		        	sectionMap = form.getSectionMap();
		        	shortNameTemp=shortName;
		        }

				if (rs.getInt("dict_questionid") == questionid && editversion == rs.getInt("responseeditversion")) {
					// this row is the same as the last, meaning its a multiple
					// choice question
					((EditAnswerDisplay) edits.get(i - 1)).getPreviousAnswer().add(rs.getString("previousanswer"));

				} else {
					// it's a new edit
					EditAnswerDisplay ead = new EditAnswerDisplay();
					ead.setEditDate(rs.getTimestamp("updateddate"));
					String previousAnswer = rs.getString("previousAnswer");
					ead.getPreviousAnswer().add(previousAnswer);
					int reQuestionId = rs.getInt("dict_questionid");
					int reSectionId = rs.getInt("dict_sectionid");
					String questionMapKey = "S_" + reSectionId + "_Q_" + reQuestionId;
					Question question = questionMap.get(questionMapKey);
					Section section = sectionMap.get(reSectionId);
					
					
					
					ead.setQuestionId(reQuestionId);
					ead.setSectionId(reSectionId);
					
					
					String name = question.getName();
					ead.setQuestionName(name);
					
					String text = question.getText();
					ead.setQuestionText(text);
					
					FormQuestionAttributes questionAttribute = question.getFormQuestionAttributes();
					
					String sectionName = section.getName();
					ead.setSectionName(sectionName);
					
					
					String dataElementName = questionAttribute.getDataElementName();
					ead.setDataElementName(dataElementName); 
					
					
					int type = question.getType().getValue();
					
					ead.setQuestionType(QuestionType.getByValue(type).toString());
					ead.setAuditcommentForEdit(rs.getString("editauditcomment"));
					ead.setAuditStatus(rs.getString("auditstatus"));
					//ead.setUsername(rs.getString("username")); //TODO
					ead.setAdministeredformId(rs.getInt("administeredformid"));
					ead.setAdministeredformName(rs.getString("afname"));
					ead.getEditedAnswer().add(previousAnswer);

					i++;
					questionid = rs.getInt("dict_questionid");
					questionVersion = rs.getInt("questionversion");
					editversion = rs.getInt("responseeditversion");
					
					/* subject */
	        		guid = rs.getString("guid");
	        		mrn = rs.getString("d_mrn");
	        		nrn = rs.getString("d_nrn");
	        		patientid = rs.getInt("patientid");
	        		if(subjectDisplayType==CtdbConstants.PATIENT_DISPLAY_ID){
	        			ead.setnRN(nrn);
	        			ead.setSubject(nrn);
	        		}
					if(subjectDisplayType==CtdbConstants.PATIENT_DISPLAY_GUID){						
						ead.setGuId(guid);
						ead.setSubject(guid);
				    }
					if(subjectDisplayType==CtdbConstants.PATIENT_DISPLAY_MRN){
						ead.setmRN(mrn);
						ead.setSubject(mrn);
					}
					
					edits.add(ead);

				}
			}
			return edits;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get the response edit archive list for VQ: "
							+ e.getMessage(), e);
		} catch(Exception e) {
        	// getting 401: unauthorized
        	//e.printStackTrace();
			throw new CtdbException(
					"Unable to get the response edit archive list for VQ: "
							+ e.getMessage(), e);
        } finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
    /* Audit Comment  */
	public List<EditAnswerDisplay> getEditArchivesByQId(Form form, int admFormId,
									 int dict_sectionId, int dict_questionId,
									 boolean isFinalLocked) 
														throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(100);

			sql.append("select re.*,  u.username, u.lastname||', '||u.firstname as userfullname  ");
			sql.append(" from responseedit re, usr u ");
			sql.append(" where re.administeredformid = ? ");
			sql.append(" and re.dict_sectionid=? and dict_questionid= ? ");
			sql.append(" and re.editauditcomment is not null ");
			sql.append(" and re.updatedby = u.usrid ");
			sql.append(" order by re.responseeditversion desc; ");
			//sql.append(" order by  re.updateddate ");


			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admFormId);
			stmt.setLong(2, dict_sectionId);
			stmt.setLong(3, dict_questionId);
			rs = stmt.executeQuery();
			ArrayList<EditAnswerDisplay> edits = new ArrayList<EditAnswerDisplay>();
			int questionid = Integer.MIN_VALUE;
			int questionVersion = Integer.MIN_VALUE;
			int editversion = Integer.MIN_VALUE;
			
			HashMap<String,Question> questionMap = form.getQuestionMap();
			HashMap<Integer,Section> sectionMap = form.getSectionMap();
			
			
			
			
			
			for (int i = 0; rs.next();) {
				if (rs.getInt("dict_questionid") == questionid && editversion == rs.getInt("responseeditversion")) {
					// this row is the same as the last, meaning its a multiple
					// choice question
					((EditAnswerDisplay) edits.get(i - 1)).getPreviousAnswer().add(rs.getString("previousanswer"));

				} else {
					// it's a new edit
					EditAnswerDisplay ead = new EditAnswerDisplay();
					
					String lastPreAnswer = getLastPreAnswer(admFormId, rs.getInt("dict_sectionid"), rs.getInt("dict_questionid"), rs.getInt("responseeditversion"));
					if (lastPreAnswer==null) {lastPreAnswer="";}
					ead.getPreviousAnswer().add(lastPreAnswer);					
					ead.setEditDate(rs.getTimestamp("updateddate"));
					String previousAnswer = rs.getString("previousAnswer");
					if (previousAnswer==null) {previousAnswer="";}
					//ead.getPreviousAnswer().add(previousAnswer);
					
					int reQuestionId = rs.getInt("dict_questionid");
					int reSectionId = rs.getInt("dict_sectionid");
					String questionMapKey = "S_" + reSectionId + "_Q_" + reQuestionId;
					Question question = questionMap.get(questionMapKey);
					Section section = sectionMap.get(reSectionId);
					
					
					
					ead.setQuestionId(reQuestionId);
					ead.setSectionId(reSectionId);
					
					
					String name = question.getName();
					ead.setQuestionName(name);
					
					String text = question.getText();
					ead.setQuestionText(text);
					
					FormQuestionAttributes questionAttribute = question.getFormQuestionAttributes();
					
					String sectionName = section.getName();
					ead.setSectionName(sectionName);
					
					
					String dataElementName = questionAttribute.getDataElementName();
					ead.setDataElementName(dataElementName); 
					
					
					int type = question.getType().getValue();
					
					ead.setQuestionType(QuestionType.getByValue(type).toString());      
					ead.setAuditcommentForEdit(rs.getString("editauditcomment"));
					ead.setAuditStatus(rs.getString("auditstatus"));
					ead.setUsername(rs.getString("userfullname"));
					ead.setAdministeredformId(rs.getInt("administeredformid"));
					ead.getEditedAnswer().add(previousAnswer);

					i++;
					questionid = rs.getInt("dict_questionid");
					questionVersion = rs.getInt("questionversion");
					editversion = rs.getInt("responseeditversion");
					edits.add(ead);

				}
			}
			return edits;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get the response edit archive list: "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	/* Audit Comment */
	public List getLastAuditComment(int admFormId, int sectionId,int questionId) //User loggedInUser)
												 throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String editauditcomment = "";
		String auditstatus = "";
		ArrayList auditpair = new ArrayList();
		
		try {
			StringBuffer sql = new StringBuffer(50);
			
			sql.append("select editauditcomment, auditstatus from responseedit ");
			sql.append("where administeredformid = ? ");
			sql.append(" and dict_sectionid = ? and dict_questionid = ? and editauditcomment is not null");
			sql.append(" order by responseeditversion desc LIMIT 1 ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admFormId);
			stmt.setLong(2, sectionId);
			stmt.setLong(3, questionId);
			stmt.executeQuery();

			rs = stmt.executeQuery();

			while (rs.next()) {
				editauditcomment = rs.getString("editauditcomment");
				auditstatus = rs.getString("auditstatus");
				//ArrayList auditpair = new ArrayList();
				auditpair.add(editauditcomment);
				auditpair.add(auditstatus);
			}
			return auditpair;

		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get the last response final editauditcomment: "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	/* Audit Comment table at "audit view" for FL and Completed eform status */ 
	public String getLastPreAnswer(int admFormId, int sectionId,int questionId, int responseeditversion) //User loggedInUser)
												 throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String previousAnswer = "";
		
		try {
			StringBuffer sql = new StringBuffer(50);
			
			sql.append("select * from responseedit ");
			sql.append("where administeredformid = ? ");
			sql.append(" and dict_sectionid = ? and dict_questionid = ? and editreason is not null ");
			sql.append(" and responseeditversion <? ");
			sql.append(" order by responseeditversion desc LIMIT 1 ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admFormId);
			stmt.setLong(2, sectionId);
			stmt.setLong(3, questionId);
			stmt.setLong(4, responseeditversion);
			stmt.executeQuery();

			rs = stmt.executeQuery();

			while (rs.next()) {
				previousAnswer = rs.getString("previousAnswer");
			}
			return previousAnswer;

		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get the last response final editreason: "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	/* Audit Comment table at "audit view" for In Progress eform status */ 
	public String getLastPreAnswerForInprogress(int admFormId, int sectionId,int questionId, int responseeditversion) //User loggedInUser)
												 throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String previousAnswer = "";
		
		try {
			StringBuffer sql = new StringBuffer(50);
			
			sql.append("select * from responseedit ");
			sql.append("where administeredformid = ? ");
			sql.append(" and dict_sectionid = ? and dict_questionid = ? ");
			//sql.append(" and editreason is null and editauditcomment is null  ");
			sql.append(" and editauditcomment is null and in_progress_data_flag=? ");
			sql.append(" and responseeditversion <? ");
			sql.append(" order by responseeditversion desc LIMIT 1 ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admFormId);
			stmt.setLong(2, sectionId);
			stmt.setLong(3, questionId);
			stmt.setString(4, CtdbConstants.YES);
			stmt.setLong(5, responseeditversion);
			stmt.executeQuery();

			rs = stmt.executeQuery();

			while (rs.next()) {
				previousAnswer = rs.getString("previousAnswer");
			}
			return previousAnswer;

		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get the last response final editreason: "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	/**
	 * Retrieves all final answers.
	 * 
	 * @param responseIdList
	 *            The list of response ids from an administered form for
	 *            retrieving the final answers.
	 * @return Map the hashMap of answers for the passed response id list.
	 * @throws CtdbException
	 *             thrown if any errors occur
	 */
	public Map getFinalAnswers(List responseIdList) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(100);
			sql.append("select answer, responseid from patientresponse ");
			sql.append("where responseid in(");
			int count = 0;
			for (Iterator it = responseIdList.iterator(); it.hasNext();) {
				String id = (String) it.next();
				if (count > 0)
					sql.append(",");

				sql.append(Integer.parseInt(id));
				count++;
			}
			sql.append(") order by responseid ");
			stmt = this.conn.prepareStatement(sql.toString());
			rs = stmt.executeQuery();

			Map answerMap = new HashMap();
			while (rs.next()) {
				String answer = rs.getString("answer");
				String responseId = Integer.toString(rs.getInt("responseid"));
				List answers = (List) answerMap.get(responseId);

				if (answers == null) {
					answers = new ArrayList();
					answers.add(answer);
					answerMap.put(responseId, answers);
				} else
					answers.add(answer);
			}

			return answerMap;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get the edit archive list: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves adminisered form meta data editing history.
	 * 
	 * @param admFormId
	 *            The administered form ID for retrieving the editing
	 *            administered form meta data history.
	 * @return List the list of MetaDataHistory objects for the admFormId.
	 * @throws CtdbException
	 *             thrown if any errors occur
	 */
	public List getMetaDataHistory(int admFormId, boolean finalLocked)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			Date finalLockDate = null;
			if (finalLocked) {
				AdministeredForm admForm = this.getFinalLockedInfo(admFormId);
				finalLockDate = admForm.getFinalLockDate();
			}
			StringBuffer sql = new StringBuffer(200);
			sql.append("select colname, colvaluebefore, colvalueafter, editreason, ");
			sql.append("editdate, u.username from usr u, administeredformedit a ");
			sql.append("where administeredformid = ? and u.usrid = a.editby order by editid");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admFormId);
			rs = stmt.executeQuery();

			List history = new ArrayList();

			while (rs.next()) {
				MetaDataHistory metaData = new MetaDataHistory();
				if (rs.getString("colname").equalsIgnoreCase("patient"))
					metaData.setColname("Patient ID");
				else if (rs.getString("colname").equalsIgnoreCase("interval"))
					metaData.setColname("Interval Name");
				else if (rs.getString("colname").startsWith("visit")) {
					metaData.setColname("Visit Date");
				}

				metaData.setColvaluebefore(rs.getString("colvaluebefore"));
				metaData.setColvalueafter(rs.getString("colvalueafter"));
				metaData.setEditreason(rs.getString("editreason"));
				Date editDate = rs.getTimestamp("editdate");
				metaData.setEditdate(editDate);
				metaData.setEditby(rs.getString("username"));
				if (finalLocked && editDate.compareTo(finalLockDate) >= 0) {
					metaData.setStatus(CtdbConstants.DATACOLLECTION_STATUS_FINALLOCKED);
				} else {
					metaData.setStatus(CtdbConstants.DATACOLLECTION_STATUS_INPROGRESS);
				}
				history.add(metaData);
			}
			return history;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get the meta data edit history list: "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Transforms a ResulSet object into a Response object with only data in the
	 * response table
	 * 
	 * @param rs
	 *            ResultSet to transform to Response object
	 * @return Response data object
	 * @throws SQLException
	 *             thrown if any errors occur while retrieving data from result
	 *             set
	 */
	private Response rsToResponseSimple(ResultSet rs) throws SQLException {
		Response response = new Response();
		response.setId(rs.getInt("responseid"));
		return response;
	}

	// ================ 505951 added ==========================
	/**
	 * Retrieves Data entry assignment archives.
	 * 
	 * @param admFormId
	 *            The administered form ID for retrieving the Data entry
	 *            assignment history
	 * @return List the list of Data entry assignment objects for the admFormId
	 * @throws CtdbException
	 *             thrown if any errors occur
	 */
	public List getAssignArchives(int admFormId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			HashMap userIdNameMap = getUserIdNameMap();
			stmt = this.conn.prepareStatement(SQL_RETRIEV_REASS_ARCHIVE
					.toString());
			stmt.setInt(1, admFormId);
			rs = stmt.executeQuery();

			List dataEntryAssginHistoryList = new ArrayList();
			List userIdList = new ArrayList();
			String tempUserIdString = "";
			DataEntryAssArch dataEntryAssArch;

			while (rs.next()) {
				dataEntryAssArch = new DataEntryAssArch();

				dataEntryAssArch.setPreviousBy(rs.getInt("previousby"));
				tempUserIdString = Integer.toString(rs.getInt("previousby"));
				dataEntryAssArch.setPreviousByName((String) userIdNameMap
						.get(tempUserIdString));

				dataEntryAssArch.setCurrentBy(rs.getInt("currentby"));
				tempUserIdString = Integer.toString(rs.getInt("currentby"));
				dataEntryAssArch.setCurrentByName((String) userIdNameMap
						.get(tempUserIdString));

				dataEntryAssArch.setAssignedBy(rs.getInt("reassignby"));
				tempUserIdString = Integer.toString(rs.getInt("reassignby"));
				dataEntryAssArch.setAssignedByName((String) userIdNameMap
						.get(tempUserIdString));

				dataEntryAssArch.setAssignedDate(rs
						.getTimestamp("assigneddate"));
				dataEntryAssArch.setDataEntryFlag(rs.getInt("dataentryflag"));

				dataEntryAssginHistoryList.add(dataEntryAssArch);
			}

			return dataEntryAssginHistoryList;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get the edit archive list: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	

	/**
	 * Gets the list of eform ids of collections that are in-progress/completed or locked for 
	 * a particular visit type and patient
	 * 	
	 * @param visitTypeid
	 * @param patientId
	 * @return
	 * @throws CtdbException
	 */
	public List<Integer> getEformIdsOfCollections(int visitTypeid, int patientId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Integer> eformIds = new ArrayList<Integer>();

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select eformid from administeredform ");
			sql.append("where patientid = ? and intervalid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, patientId);
			stmt.setLong(2, visitTypeid);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				eformIds.add(rs.getInt("eformid"));
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get eformids " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return eformIds;

	}


	/**
	 * Retrieves Current Data Entry Assignment info.
	 * 
	 * @param admFormId
	 *            the Adninisteredform id
	 * @param dataEntryFlag
	 *            1 indicates for the first data entry; 2 indicates for the
	 *            second data entry
	 * @return EditAssignment the Object contains Current Data Entry Assignment
	 *         info
	 * @throws CtdbException
	 *             thrown if any errors occur
	 */
	public EditAssignment getEditAssignment(int admFormId, int dataEntryFlag)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		EditAssignment editAssignment = null;

		try {
			StringBuffer sb = new StringBuffer();
		
			
			
			
			
			sb.append("SELECT af.visitdate, af.formid, ef.name as form_name, af.intervalid, il.name as interval_name, ur.usrid, af.patientid,pp.subjectid, "
					+ CtdbDao.getDecryptionFunc("pa.lastname") + " as lastname, "
					+ CtdbDao.getDecryptionFunc("pa.firstname") + " as firstname, ");
			sb.append(" dd.dataenteredby, ur.username as user_name, dd.createddate, ef.protocolid, dd.dataentrydraftid ");
			sb.append(" FROM eform ef, patient pa,patientprotocol pp, dataentrydraft dd, usr ur, administeredform af left outer join interval il on af.intervalid = il.intervalid ");
			sb.append(" WHERE af.administeredformid = ? AND af.eformid = ef.eformid and pp.patientid=pa.patientid");

			sb.append(" AND af.patientid = pa.patientid AND af.administeredformid = dd.administeredformid AND dd.dataentryflag = ? AND ur.usrid = dd.dataenteredby ");
			stmt = this.conn.prepareStatement(sb.toString());
			stmt.setLong(1, admFormId);
			stmt.setInt(2, 1);
			rs = stmt.executeQuery();

			SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
			if (rs.next()) {
				editAssignment = new EditAssignment();
				if (rs.getDate("visitdate") != null) {
					editAssignment.setVisitDate(df.format(rs
							.getDate("visitdate")));
				} else {
					editAssignment.setVisitDate("N/A");
				}
				editAssignment.setFormId(rs.getInt("formid"));
				editAssignment.setFormName(rs.getString("form_name"));
				String intervalName = rs.getString("interval_name");
				if (intervalName == null || intervalName.equals("")) {
					intervalName = "Other";
				}
				editAssignment.setIntervalName(intervalName);
				editAssignment.setSubjectId(rs
						.getString("subjectid"));
				editAssignment.setDataEntryFlag(CtdbConstants.SINGLE_ENTRY_FORM);
				//editAssignment.setDataEntryFlag(dataEntryFlag);
				editAssignment.setStartDate(rs.getTimestamp("createddate"));
				editAssignment.setCurrentByName(rs.getString("user_name"));
				editAssignment.setCurrentBy(rs.getInt("usrid"));
				editAssignment.setProtocolId(rs.getInt("protocolid"));
				editAssignment.setDataEntryDraftId(rs
						.getInt("dataentrydraftid"));
				editAssignment.setPatientNameLabel(rs.getString("lastname")
						+ ", " + rs.getString("firstname"));
			}
			return editAssignment;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get the Edit Assign Form: " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Save Data Re-Assign infor
	 * 
	 * @param editAssignment
	 *            the Object contains Data Entry Assignment info to be saved
	 * @throws DuplicateObjectException
	 *             thrown if the user assigned to has started another data entry
	 * @throws CtdbException
	 *             thrown if any errors occur
	 */
	public void saveDataEntryReAssign(EditAssignment editAssignment)
			throws DuplicateObjectException, CtdbException {
		logger.info("ResponseManagerDAO->saveDataEntryReAssign->editAssignment->aformId:\t"+editAssignment.getAdministeredFormId()+"eaForm.getPreviousBy()"+editAssignment.getPreviousBy()+"getCurrentBy():\t"+editAssignment.getCurrentBy());
		logger.info("eaForm.getAssignedBy():\t"+editAssignment.getAssignedBy()+"eaForm.getDataEntryFlag():\t"+editAssignment.getDataEntryFlag());
		PreparedStatement stmt = null;
		int effectedRowCount = 0;
		EditAssignment eaForm = editAssignment;

		try {
			stmt = this.conn.prepareStatement(SQL_UPDATE_DATA_ENTRY_ASSIGNMENT
					.toString());
			stmt.setLong(1, eaForm.getCurrentBy());
			stmt.setLong(2, eaForm.getDataEntryDraftId());
			effectedRowCount = stmt.executeUpdate();
			this.close(stmt);
			stmt = null;

			// int newKey = getSequenceValue(conn,
			// "dataentryreassignarchive_seq");
			stmt = this.conn.prepareStatement(SQL_NEW_DATA_ENTRY_ASSIGNMENT
					.toString());
			// stmt.setInt(1, newKey);
			stmt.setInt(1, eaForm.getDataEntryDraftId());
			stmt.setInt(2, eaForm.getPreviousBy());
			stmt.setInt(3, eaForm.getCurrentBy());
			stmt.setInt(4, eaForm.getAssignedBy());
			stmt.setInt(5, eaForm.getDataEntryFlag());
			effectedRowCount = stmt.executeUpdate();
			
			
			//need to also update the visibleadministeredsection for collections that have been done
			int aformid = getAdministeredFormId(eaForm.getDataEntryDraftId());
			
			StringBuffer sql = new StringBuffer(50);
			sql.append("update visibleadminsteredsection set userid = ? ");
			sql.append("where userid = ? and administeredformid = ?");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, eaForm.getCurrentBy());
			stmt.setLong(2, eaForm.getPreviousBy());
			stmt.setLong(3, aformid);
			stmt.executeUpdate();
			this.close(stmt);
			
		} catch (PSQLException e) {
			if (e.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"Can't reassign data entry to this user with ID "
								+ eaForm.getCurrentBy()
								+ "since he/she has already started one data entry : "
								+ e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to reassign data entry: "
						+ e.getMessage(), e);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"Can't reassign data entry to this user with ID "
								+ eaForm.getCurrentBy()
								+ "since he/she has already started one data entry : "
								+ e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to reassign data entry: "
						+ e.getMessage(), e);
			}
		} finally {
			this.close(stmt);
		}
	}
	
	
	public Map<String, String> getProtocolUsers(int pId) throws CtdbException {
		Map<String, String> userMap = new LinkedHashMap<String, String>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.prepareStatement(SQL_RETRIEV_PROTOCOL_USER_MAP2.toString());
			stmt.setLong(1, pId);

			rs = stmt.executeQuery();

			while (rs.next()) {
				userMap.put(rs.getInt("usrid") + "", rs.getString("lastname") + 
						", " + rs.getString("firstname"));
			}
			
		} catch (SQLException e) {
			throw new CtdbException("Unable to get the User id and name map: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return userMap;
	}

	// ====================

	private HashMap getUserIdNameMap() throws CtdbException {
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = this.conn.createStatement();

			rs = stmt.executeQuery(SQL_RETRIEV_ALL_USER_MAP.toString());

			HashMap idNameMap = new HashMap();

			while (rs.next()) {
				idNameMap.put(Integer.toString(rs.getInt("usrid")),
						rs.getString("username"));
			}
			return idNameMap;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get the User id and name map: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public ArrayList getDataEntryDraft(int formId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList dataEntryList = new ArrayList();

		try {
			stmt = this.conn.prepareStatement(SQL_RETRIEV_DATA_ENTRY_DRAFT
					.toString());
			stmt.setLong(1, formId);

			rs = stmt.executeQuery();

			while (rs.next()) {
				DataEntryDraft dataEntryDraft = new DataEntryDraft();
				dataEntryDraft.setFormId(rs.getInt("formid"));
				dataEntryDraft.setAdministeredFormId(rs
						.getInt("administeredformid"));
				dataEntryDraft.setDataEntryDraftId(rs
						.getInt("dataentrydraftid"));
				dataEntryDraft.setDataEnteredBy(rs.getInt("dataenteredby"));
				dataEntryDraft.setDataEnteredByName(rs.getString("username"));
				dataEntryDraft.setDataEnteredFlag(rs.getInt("dataentryflag"));
				dataEntryList.add(dataEntryDraft);
			}
			return dataEntryList;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get Data Entry Draft: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	// ================ add end 505951 ========================

	/**
	 * Unadminister the form with different status. Database cascade delete will
	 * be performed on other tables, such as DATAENTRYDRAFT, RESPONSEDRAFT,
	 * PATIENTRESPONSEDRAFT, DATAENTRYREASSIGNARCHIVE, RESPONSE, PATIENTRESPONSE
	 * PATIENTADMINRESPONSE AND ADMINSTEREDFORMEDIT.
	 * 
	 * @param administeredForm
	 *            The form to be unadministered
	 * @param dataEntryFlag
	 *            the flag indicates the data entry 1 or 2
	 * @throws CtdbException
	 *             thrown if any errors occur when processing
	 */
	public void unadministerForm(AdministeredForm administeredForm,
			int dataEntryFlag) throws CtdbException {
		logger.info("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$unadministerForm$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"+administeredForm.getId());
		PreparedStatement stmt = null;
		try {
	
			
			List<Response> responses = administeredForm.getResponses();
			Iterator responsesIter = responses.iterator();
			while (responsesIter.hasNext()) {
				Response response  = (Response) responsesIter.next();
				QuestionType questionType = response.getQuestion().getType();
				boolean isLocked = (administeredForm.getFinalLockDate() != null);
				
				if (questionType.getValue() == QuestionType.File.getValue()) {
					int responseID = response.getId();
					int attID = getAttachmentIdFromResponse(responseID, isLocked);
					// delete real file in computer
					int formId = administeredForm.getForm().getId();
					AttachmentManager am = new AttachmentManager();
					try {
						am.deleteAttachment(attID, formId, AttachmentManager.FILE_COLLECTION);
					} catch (ServerFileSystemException e) {
						logger.error("File system error.", e);
					}
				}
			}
			
			
			// -------
			StringBuffer sql = new StringBuffer(25);
			sql = new StringBuffer(50);
			//delete section id for that user only new modification 3/15/2013 by yogi
			sql.append("delete from visibleadminsteredsection  where administeredformid ="
					+ administeredForm.getId()
					+ " and userid in (select  dataenteredby from dataentrydraft  where administeredformid ="
					+ administeredForm.getId()
					+ " and dataentryflag ="
					+ dataEntryFlag + ")");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.executeUpdate();
			this.close(stmt);

			if (dataEntryFlag == 0) {
				//delete any rows from sent email that this collection has sent.					
				if(administeredForm.getInterval() == null || administeredForm.getInterval().getId() == -1) {
					sql = new StringBuffer(50);
					sql.append("delete from sentemail where patientid = ? and intervalid is null and eformid = ?");
					stmt = this.conn.prepareStatement(sql.toString());
					stmt.setLong(1, administeredForm.getPatient().getId());
					stmt.setLong(2, administeredForm.getForm().getId());
					stmt.executeUpdate();
					this.close(stmt);
				}else {
					sql = new StringBuffer(50);
					sql.append("delete from sentemail where patientid = ? and intervalid = ? and eformid = ?");
					stmt = this.conn.prepareStatement(sql.toString());
					stmt.setLong(1, administeredForm.getPatient().getId());
					stmt.setLong(2, administeredForm.getInterval().getId());
					stmt.setLong(3, administeredForm.getForm().getId());
					stmt.executeUpdate();
					this.close(stmt);
				}
				
				sql = new StringBuffer(50);
				sql.append("delete from administeredform where administeredformid = ?");
				stmt = this.conn.prepareStatement(sql.toString());
				stmt.setLong(1, administeredForm.getId());
				stmt.executeUpdate();
				this.close(stmt);
			} else if (dataEntryFlag == 1) {
		
					
					//delete any rows from sent email that this collection has sent.					
					if(administeredForm.getInterval() == null || administeredForm.getInterval().getId() == -1) {
						sql = new StringBuffer(50);
						sql.append("delete from sentemail where patientid = ? and intervalid is null and eformid = ?");
						stmt = this.conn.prepareStatement(sql.toString());
						stmt.setLong(1, administeredForm.getPatient().getId());
						stmt.setLong(2, administeredForm.getForm().getId());
						stmt.executeUpdate();
						this.close(stmt);
					}else {
						sql = new StringBuffer(50);
						sql.append("delete from sentemail where patientid = ? and intervalid = ? and eformid = ?");
						stmt = this.conn.prepareStatement(sql.toString());
						stmt.setLong(1, administeredForm.getPatient().getId());
						stmt.setLong(2, administeredForm.getInterval().getId());
						stmt.setLong(3, administeredForm.getForm().getId());
						stmt.executeUpdate();
						this.close(stmt);
					}
					
					
					
					
					
					sql = new StringBuffer(50);
					sql.append("delete from administeredform where administeredformid = ?");
					stmt = this.conn.prepareStatement(sql.toString());
					stmt.setLong(1, administeredForm.getId());
					stmt.executeUpdate();
					this.close(stmt);
				
			} else {
				sql = new StringBuffer(50);
				sql.append("delete from dataentrydraft where administeredformid = ? and dataentryflag = 2");
				stmt = this.conn.prepareStatement(sql.toString());
				stmt.setLong(1, administeredForm.getId());
				stmt.executeUpdate();
				this.close(stmt);

				// set discrepancyflag to 0 in the administeredform
				if (administeredForm.getDiscrepancyFlag()) {
					sql = new StringBuffer(50);
					sql.append("update administeredform set discrepancyflag = false ");
					sql.append("where administeredformid = ?");
					stmt = this.conn.prepareStatement(sql.toString());
					stmt.setLong(1, administeredForm.getId());
					stmt.executeUpdate();
					this.close(stmt);
				}

				// If we are deleting second entry we have to delete the
				// corresponding entry from respose & patientresponse tables
				sql = new StringBuffer(50);
				sql.append("delete from patientresponse  where responseid in (select responseid from response where administeredformid=?)");
				stmt = this.conn.prepareStatement(sql.toString());
				stmt.setLong(1, administeredForm.getId());
				stmt.executeUpdate();
				this.close(stmt);

				sql = new StringBuffer(50);
				sql.append("delete from response  where administeredformid = ?");
				stmt = this.conn.prepareStatement(sql.toString());
				stmt.setLong(1, administeredForm.getId());
				stmt.executeUpdate();
				this.close(stmt);

				sql = new StringBuffer(50);
				sql.append("update administeredform set finallockdate = null,certifiedby = null,finallockby = null,certifieddate = null  ");
				sql.append("where administeredformid = ?");
				stmt = this.conn.prepareStatement(sql.toString());
				stmt.setLong(1, administeredForm.getId());
				stmt.executeUpdate();
				this.close(stmt);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new CtdbException("Unable to unadminister the form with ID "
					+ administeredForm.getId() + " : " + e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}
	
	
	
	/* created by Ching Heng for File type question */
	public List getAllResponse(AdministeredForm administeredForm,
			int dataEntryFlag) throws CtdbException {
		/* need to modify for deleting specific attachment!!!!!!!!!!! */
		List responses = new ArrayList();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sql = new StringBuffer(200);
			
			if (administeredForm.getFinalLockDate() != null){
				sql.append("select responseid from response where administeredformid = ? ");
				stmt = this.conn.prepareStatement(sql.toString());
				stmt.setLong(1, administeredForm.getId());
			} else{
				sql.append("select responsedraftid from responsedraft where dataentrydraftid=(select dataentrydraftid from dataentrydraft ");
				sql.append("where administeredformid = ? and dataentryflag=?)");
				stmt = this.conn.prepareStatement(sql.toString());
				stmt.setLong(1, administeredForm.getId());
				stmt.setLong(2, dataEntryFlag);
			}
			
			rs = stmt.executeQuery();
			String qType = String.valueOf(QuestionType.File.getValue());
			while (rs.next()) {
				if (administeredForm.getFinalLockDate() != null){
					responses.add(rs.getInt("responseid"));
				}else{
					responses.add(rs.getInt("responsedraftid"));
				}
			}
		} catch (SQLException e) {
			throw new CtdbException(
					"Error occur while getting the Data Entry IDs "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		return responses;
	}



	public int getAttachmentIdFromResponse(long responseID, boolean isLocked)
			throws CtdbException {
		int attachId = Integer.MIN_VALUE;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String answer = "";
		try {
			StringBuffer sql = new StringBuffer(200);
			
			if (isLocked){
				sql.append("select attachmentid from patientresponse ");
				sql.append("where responseid = ?");
			}else{
				sql.append("select attachmentid from patientresponsedraft ");
				sql.append("where responsedraftid = ?");
			}
			
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, responseID);
			rs = stmt.executeQuery();
			while (rs.next()) {
				answer = rs.getString("attachmentid");
			}
			if (!Utils.isBlank(answer)) {
				attachId = Integer.parseInt(answer);
			}
		} catch (SQLException e) {
			throw new CtdbException(
					"Error occur while getting the attachment ID "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		return attachId;
	}

	// ===============================

	public void setResponseCalendarDataEntryDraft(int administeredFormId,
			CalendarResponse cResponse) throws CtdbException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer(200);

		try {

			sql.append("select rownumber, colnumber, answer, dataentryflag, d.responsedraftid ");
			sql.append("from administeredform a, dataentrydraft b, responsedraft c, patientresponsedraft d ");
			sql.append(", patientcalendardraft e ");
			sql.append("where a.administeredformid = b.administeredformid ");
			sql.append("and b.dataentrydraftid = c.dataentrydraftid ");
			sql.append("and a.administeredformid = ? ");
			sql.append("and c.responsedraftid = d.responsedraftid ");
			sql.append("and c.questionid = ? ");
			sql.append("and c.questionversion = ? ");
			sql.append("and c.sectionid = ? ");
			sql.append("and e.responsedraftid = c.responsedraftid ");

			stmt = this.conn.prepareStatement(sql.toString());

			stmt.setLong(1, administeredFormId);
			stmt.setLong(2, cResponse.getQuestion().getId());
			stmt.setInt(3, cResponse.getQuestion().getVersion()
					.getVersionNumber());
			stmt.setLong(2, cResponse.getQuestion().getSectionId());

			rs = stmt.executeQuery();
			while (rs.next()) {
				((PatientCalendarQuestion) cResponse.getResponse(
						rs.getInt("dataentryflag")).getQuestion())
						.setCalendarData(rs.getInt("colnumber"),
								rs.getInt("rownumber"), rs.getString("answer"));

				((PatientCalendarQuestion) cResponse.getResponse(
						rs.getInt("dataentryflag")).getQuestion())
						.setResponseId(rs.getInt("colnumber"),
								rs.getInt("rownumber"),
								rs.getInt("responsedraftid"));
			}
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to build data entry data for the patient calendar for admin id "
							+ administeredFormId + " and Qid : "
							+ cResponse.getQuestion().getId() + " : "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

	}

	public PatientCalendarCellResponse getCalendarDataEntryDraft(
			PatientCalendarCellResponse pccr, int adminFormId)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer(200);

		try {

			sql.append("select rownumber, colnumber, answer, dataentryflag, d.responsedraftid ");
			sql.append("from administeredform a, dataentrydraft b, responsedraft c, patientresponsedraft d ");
			sql.append(", patientcalendardraft e ");
			sql.append("where a.administeredformid = b.administeredformid ");
			sql.append("and b.dataentrydraftid = c.dataentrydraftid ");
			sql.append("and a.administeredformid = ? ");
			sql.append("and c.responsedraftid = d.responsedraftid ");
			sql.append("and c.questionid = ? ");
			sql.append("and e.responsedraftid = c.responsedraftid ");
			sql.append("and e.rownumber = ? ");
			sql.append("and e.colnumber = ? ");

			stmt = this.conn.prepareStatement(sql.toString());

			stmt.setLong(1, adminFormId);
			stmt.setLong(2, pccr.getQuestion().getId());
			stmt.setInt(3, pccr.getRow());
			stmt.setInt(4, pccr.getCol());

			rs = stmt.executeQuery();
			while (rs.next()) {
				ArrayList answers = new ArrayList();
				answers.add(rs.getString("answer"));
				pccr.getResponse(rs.getInt("dataentryflag"))
						.setAnswers(answers);
				pccr.getResponse(rs.getInt("dataentryflag")).setId(
						rs.getInt("responsedraftid"));

			}
			return pccr;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to build data entry data for the patient calendar for admin id "
							+ adminFormId + " and Qid : "
							+ pccr.getQuestion().getId() + " : "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

	}

	public void setResponseCalendarDataEntryFinal(int administeredFormId,
			CalendarResponse cResponse) throws CtdbException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer(200);

		try {
			sql.append("select answer, rownumber, colnumber, b.responseid ");
			sql.append("from administeredform a, response b, patientresponse c ");
			sql.append(", patientcalendar d ");
			sql.append("where a.administeredformid = b.administeredformid ");
			sql.append("and b.responseid = c.responseid ");
			sql.append("and a.administeredformid = ? ");
			sql.append("and b.questionid = ? ");
			sql.append("and b.questionversion = ? ");
			sql.append("and d.responseid = b.responseid ");

			stmt = this.conn.prepareStatement(sql.toString());

			stmt.setLong(1, administeredFormId);
			stmt.setLong(2, cResponse.getQuestion().getId());
			stmt.setInt(3, cResponse.getQuestion().getVersion()
					.getVersionNumber());

			rs = stmt.executeQuery();
			while (rs.next()) {

				((PatientCalendarQuestion) cResponse.getQuestion())
						.setCalendarData(rs.getInt("colnumber"),
								rs.getInt("rownumber"), rs.getString("answer"));

				((PatientCalendarQuestion) cResponse.getQuestion())
						.setResponseId(rs.getInt("colnumber"),
								rs.getInt("rownumber"), rs.getInt("responseid"));
			}
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to build data entry data for the patient calendar for admin id "
							+ administeredFormId + " and Qid : "
							+ cResponse.getQuestion().getId() + " : "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

	}

	/**
	 * Takes a given PatientCalendarCellResponse and uses its responseid to set
	 * its x and y coordinate
	 * 
	 * @param pccr
	 *            the response to be updated
	 * @throws CtdbException
	 */
	public void addCoordinatesToCalendarResponse(
			PatientCalendarCellResponse pccr) throws CtdbException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer(200);

		try {
			sql.append("select * from patientcalendar ");
			sql.append("where responseid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());

			stmt.setLong(1, pccr.getId());
			rs = stmt.executeQuery();

			if (rs.next()) {
				pccr.setCol(rs.getInt("colnumber"));
				pccr.setRow(rs.getInt("rownumber"));
			}

		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to complete the patientcalendarcellresponse :"
							+ pccr.getId() + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public Collection getDataCollectionFormInfo(int formId) throws CtdbException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer(200);
		try {
			sql.append("select form_view.version, form_view.description, tmp.questioncount from form_view, ");
			sql.append("    ( select count(questionid) questioncount, formversion ");
			sql.append("        from sectionquestion_view ");
			sql.append("       where formid = ?  group by formversion order by formversion ) tmp ");
			sql.append(" where formid = ? and form_view.version = tmp.formversion order by  version");

			stmt = this.conn.prepareStatement(sql.toString());

			stmt.setLong(1, formId);
			stmt.setLong(2, formId);

			rs = stmt.executeQuery();
			Collection results = new ArrayList();
			while (rs.next()) {
				DataCollectionSummary dcs = new DataCollectionSummary();
				dcs.setFormDescription(rs.getString("description"));
				dcs.setFormVersion(new Version(rs.getInt("version"))
						.getToString());
				dcs.setNumQuestions(rs.getInt("questioncount"));
				dcs.setNumCertifiedPatients(this.getNumOfPatientsCompleted(
						formId, rs.getInt("version")));
				dcs.setIntervalsAdministered(this.getIntervalsAdministered(
						formId, rs.getInt("version")));
				results.add(dcs);
			}
			return results;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to complete get collect home summary info :"
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	


	public List getDiscrepancyQustionByAdminId(int adminId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List list = new ArrayList();
		try {
			String sql = "select distinct qname from  "
					+ " (select rd.questionid, rd.questionversion,q.name qname, prd.answer, count(*) no_answers "
					+ " from patientresponsedraft prd, responsedraft rd, dataentrydraft ded  , question q "
					+ " where ded.administeredformid = ?"
					+ "  and ded.dataentrydraftid = rd.dataentrydraftid "
					+ "  and rd.responsedraftid = prd.responsedraftid "
					+ "  and rd.questionid= q.questionid "
					+ " group by rd.questionid, rd.questionversion, prd.answer ,q.name "
					+ " order by rd.questionid "
					+ " ) ilv where ilv.no_answers = 1";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, adminId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				QuestionForm q = new QuestionForm();
				q.setName(rs.getString("qname"));
				list.add(q);
			}
			return list;
		} catch (SQLException e) {
			throw new CtdbException("Unable to create discrepancyQustion:"
					+ e.getStackTrace(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		
	}

	public List getIntervalsAdministered(int formId, int version)
			throws CtdbException {

		PreparedStatement stmt = null;
		PreparedStatement stmt2 = null;
		ResultSet rs2 = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer(200);
		try {
			sql.append(" select protocol.typeid from protocol, form where protocol.protocolid = form.protocolid and form.formid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);
			rs = stmt.executeQuery();
			rs.next();
			int type = rs.getInt("typeid");
			if (type == CtdbConstants.PROTOCOL_TYPE_NATURAL_HISTORY) {
				sql = new StringBuffer();
				sql.append("select distinct administeredform.visitdate as name from administeredform ");
				sql.append(" where administeredform.formid = ? and administeredform.formversion = ? ");

				// get dates
			} else {
				// get intervals
				sql = new StringBuffer();
				sql.append("select distinct interval.name from interval, administeredform ");
				sql.append(" where administeredform.formid = ? and administeredform.formversion = ? ");
				sql.append(" and interval.intervalid = administeredform.intervalid ");

			}

			stmt2 = this.conn.prepareStatement(sql.toString());

			stmt2.setLong(1, formId);
			stmt2.setInt(2, version);

			rs2 = stmt2.executeQuery();
			List results = new ArrayList();
			while (rs2.next()) {
				if (type == CtdbConstants.PROTOCOL_TYPE_NATURAL_HISTORY) {
					SimpleDateFormat df = new SimpleDateFormat(
							"MM/dd/yyyy HH:mm");
					results.add(df.format(rs2.getDate("name")));
				} else {
					results.add(rs2.getString("name").trim());
				}
			}

			return results;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to complete get collect home summary info :"
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
			this.close(rs2);
			this.close(stmt2);
		}
	}


	/**
	 * 
	 * Method to get the getVisibleRepeableSectionsList for adminFormId
	 * 
	 * @param adminFormId
	 * @return visibleSectionList
	 * @throws CtdbException
	 */
	public List<Integer> getVisibleRepeableSectionsList(int adminFormId, int loggedInUserId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<Integer> results = new ArrayList<Integer>();
		
		try {
			/*stmt = conn .prepareStatement("select sectionid from visibleadminsteredsection where administeredformid = " + 
				adminFormId + " and userid = " + loggedInUserId);
			*/
			stmt = conn .prepareStatement("select dict_sectionid from visibleadminsteredsection where administeredformid = " + 
					adminFormId + " and userid = " + loggedInUserId);
			
			
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				results.add(rs.getInt("dict_sectionid"));
			}
		}
		catch (SQLException sqle) {
			throw new CtdbException(" Failure getting section group list " + sqle.getMessage(), sqle);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return results;
	}
	
	
	
	
	
	/**
	 * gets the hidden section ids
	 * @param protocolId
	 * @param eformId
	 * @return
	 * @throws CtdbException
	 */
	public List<Integer> getHiddenSectionsList(int protocolId, int eformId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<Integer> results = new ArrayList<Integer>();
		
		try {
			stmt = conn.prepareStatement("select dict_sectionid from psr_hidden_elements where protocolid = ? and eformId = ? and is_question = false and is_pv = false");

			stmt.setLong(1, protocolId);
			stmt.setLong(2, eformId);
			
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				results.add(rs.getInt("dict_sectionid"));
			}
		}
		catch (SQLException sqle) {
			throw new CtdbException(" Failure getting hidden section list" + sqle.getMessage(), sqle);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return results;
	}
	
	
	
	
	
	/**
	 * Gets the hidden question ids in secid_quesid format
	 * @param protocolId
	 * @param eformId
	 * @return
	 * @throws CtdbException
	 */
	public List<String> getHiddenQuestionsList(int protocolId, int eformId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<String> results = new ArrayList<String>();
		
		try {
			stmt = conn .prepareStatement("select dict_sectionid, dict_questionid from psr_hidden_elements where protocolid = ? and eformId = ? and is_question = true");
			
			stmt.setLong(1, protocolId);
			stmt.setLong(2, eformId);
			
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				int dict_section_id = rs.getInt("dict_sectionid");
				int dict_question_id = rs.getInt("dict_questionid");
				String sectionQuestion = dict_section_id + "_" + dict_question_id;
				results.add(sectionQuestion);
			}
		}
		catch (SQLException sqle) {
			throw new CtdbException(" Failure getting hidden question list " + sqle.getMessage(), sqle);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return results;
	}
	
	/**
	 * Gets the hidden pv ids in secid_quesid_pvid format
	 * @param protocolId
	 * @param eformId
	 * @return
	 * @throws CtdbException
	 */
	public List<String> getHiddenPVList(int protocolId, int eformId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<String> results = new ArrayList<String>();
		
		try {
			stmt = conn.prepareStatement("select dict_sectionid, dict_questionid, dict_pvid from psr_hidden_elements where protocolid = ? and eformId = ? and is_pv = true");
			
			stmt.setLong(1, protocolId);
			stmt.setLong(2, eformId);
			
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				int dict_section_id = rs.getInt("dict_sectionid");
				int dict_question_id = rs.getInt("dict_questionid");
				int dict_pv_id = rs.getInt("dict_pvid");
				String sectionQuestionPv = dict_section_id + "_" + dict_question_id + "_" + dict_pv_id;
				results.add(sectionQuestionPv);
			}
		}
		catch (SQLException sqle) {
			throw new CtdbException(" Failure getting hidden question list " + sqle.getMessage(), sqle);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return results;
	}
	
	/**
	 * method that gets the max repeated number of sections open for list of aforms for a particular repeatable group
	 * @param adminFormIds
	 * @param formId
	 * @param repeatedSectionParentId
	 * @return
	 * @throws CtdbException
	 */
	public int getMaxRepeatedSectionsForAdminForms(Form form, int[] adminFormIds, int repeatedSectionParentId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		int max = 1;
		String adminFormListString = "";
		for(int i=0;i<adminFormIds.length;i++) {
			adminFormListString = adminFormListString + adminFormIds[i];
			if(i != adminFormIds.length -1) {
				adminFormListString = adminFormListString + ",";
			}
			
		}
		
		try {
			StringBuffer sb = new StringBuffer();
			FormDataStructureUtility fsUtil = new FormDataStructureUtility();
			List<Integer> childSectionIds = fsUtil.getChildSectionIdsForParentSectionId(form, repeatedSectionParentId);
			for(Iterator<Integer> iter = childSectionIds.iterator(); iter.hasNext(); ) {
				sb.append(iter.next().toString());
				if(iter.hasNext()) 
					sb.append(",");
			}
			
			
			stmt = conn .prepareStatement("select count(*) as count from visibleadminsteredsection where" +
					" administeredformid in (" + adminFormListString +
					") and dict_sectionid in (" + sb.toString() + ")" +
					" group by administeredformid" +
					" order by count desc");
			

			
			
			rs = stmt.executeQuery();

			if(rs.next()) {
				max = rs.getInt("count") + 1;
			}
			
			return max;
		}
		catch (SQLException sqle) {
			throw new CtdbException(" Failure max repeated section  " + sqle.getMessage(), sqle);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		
	}
	
	
	
	public boolean isAdministeredFormInFinalLockStatus(int aformId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean isFinalLocked = false;
		
		
		
		
		try {
			String sql = "select finallockdate from administeredform where administeredformid = ?";
		
		
			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, aformId);
			rs = stmt.executeQuery();
		
		
		
		
			if (rs.next()) {
				Date date  = rs.getDate("finallockdate");
				if(date != null ) {
					isFinalLocked = true;
				}else {
					isFinalLocked = false;
				}
			}
		
		
		
		}
		catch ( SQLException sqle )
		{
			throw new CtdbException("Unable to get \"final lock status\" from DAO: " + sqle.getLocalizedMessage(), sqle);
		}
		finally
		{
			this.close(rs);
			this.close(stmt);
		}
		return isFinalLocked;
		
	}
	
	
	
	
	
	/**
	 * Method that gets the collection data for list of aformids
	 * @param aformId
	 * @param formId
	 * @return
	 * @throws CtdbException
	 */
    public HashMap<String,DataCollectionExport> getDataCollectionExport_DRAFT(int aformId, Form form) throws CtdbException {
    	PreparedStatement stmt = null;
		ResultSet rs = null;
		HashMap<String,DataCollectionExport> dceMap = new HashMap<String,DataCollectionExport>();

		try
		{

			
			
			
			String sql = "select rd.*, prd.*, prd.submitanswer " +
					 " from responsedraft rd, patientresponsedraft prd, dataentrydraft ded " +
					 " where prd.responsedraftid = rd.responsedraftid " +
					 " and rd.dataentrydraftid = ded.dataentrydraftid" + 
					 " and  ded.administeredformid = ? ";
						 
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, aformId);
			rs = stmt.executeQuery();

			HashMap<String,Question> questionMap = form.getQuestionMap();
			HashMap<Integer,Section> sectionMap = form.getSectionMap();

			int order = 0;
			int repeatableGroupCounter = 1;
			while ( rs.next() )
			{
				int sectionId = rs.getInt("dict_sectionid");
				int questionId = rs.getInt("dict_questionid");

				String answer = rs.getString("answer");
				String submitAnswer = rs.getString("submitanswer");
			
				String sectionIdQuestionIdKey = sectionId + "_" + questionId;
		

				if(dceMap.containsKey(sectionIdQuestionIdKey)) {
					//this means this is part of a multiple choice question...so get the object and append the answer to the 
					DataCollectionExport dce = dceMap.get(sectionIdQuestionIdKey);
					String currentAnswer = dce.getAnswer();
					String currentSubmitAnswer = dce.getSubmitAnswer();
					dce.setAnswer(currentAnswer + ";" + answer);
					dce.setSubmitAnswer(currentSubmitAnswer + ";" + submitAnswer); 
					
				}else {
					DataCollectionExport dce = new DataCollectionExport();
					//dce.setOrder(order);
					dce.setSectionId(sectionId);
					dce.setQuestionId(questionId);
				
					dce.setAnswer(answer);
					dce.setSubmitAnswer(submitAnswer);
		
					dceMap.put(sectionIdQuestionIdKey, dce);
				}
			}
			
			
			
			
			for ( List<Section> row : form.getRowList() ) {
				for ( Section section : row ) {
					if ( section != null ) {
						List<Question> questionsList = section.getQuestionList();
						int questionOrder = 1;
						for (Question question : questionsList) {
							int formSectionId = section.getId();
							int formQuestionId = question.getId();
							String key =  formSectionId + "_" + formQuestionId;
							DataCollectionExport dce = dceMap.get(key);
							if (dce == null) {
								dce = new DataCollectionExport();
								dce.setSectionId(formSectionId);
								dce.setQuestionId(formQuestionId);
								dceMap.put(key, dce);
							}
						/*if(dce != null) {*/
							
							boolean isRepeatable = section.isRepeatable();
							int repeatedSectionParent = section.getRepeatedSectionParent();
							String groupName = section.getRepeatableGroupName();
							FormQuestionAttributes qa = question.getFormQuestionAttributes();
							String dataElementName = qa.getDataElementName();
							String sectionName = section.getName();
							String questionName = question.getName();
							String questionText = question.getText();
							String columnLabel = "";
							
							//need to add integer suffix to repeatables
							if(isRepeatable && repeatedSectionParent == -1) {
								//this means its the parent repeatable section
								repeatableGroupCounter = 1;
							}else if(isRepeatable && repeatedSectionParent != -1 && questionOrder == 1) {
								repeatableGroupCounter ++;
							}

							if(isRepeatable) {
								
								columnLabel = columnLabel + "_" + repeatableGroupCounter;
								
								if(groupName != null && !groupName.trim().equals("") && dataElementName != null && !dataElementName.trim().equals("")) {
									columnLabel = groupName + "_" + repeatableGroupCounter + "." + dataElementName;
								}else {
									columnLabel = sectionName + "_" + repeatableGroupCounter + "." + questionName;
								}
							}else {
								
								if(groupName != null && !groupName.trim().equals("") && dataElementName != null && !dataElementName.trim().equals("")) {
									columnLabel = groupName + "." + dataElementName;
								}else {
									columnLabel = sectionName + "." + questionName;
								}
							}
							dce.setOrder(order);
							dce.setGroupName(groupName);
							dce.setDataElementName(dataElementName);
							
							
							dce.setSectionName(sectionName);
							dce.setQuestionName(questionName);
							dce.setQuestionText(questionText);
							dce.setColumnLabel(columnLabel);
							dce.setRepeatedSectionParent(repeatedSectionParent); 
							dce.setRepeatable(isRepeatable); 
							dce.setQuestionOrder(questionOrder); 
							order++;
						/*}*/
							questionOrder++;
						}
					}
					
				}
			}
			
			
			
			
			
			
			
			
		}
		catch ( SQLException sqle )
		{
			throw new CtdbException("Unable to get \"data collection export\" from DAO: " + sqle.getLocalizedMessage(), sqle);
		}
		finally
		{
			this.close(rs);
			this.close(stmt);
		}

    	return dceMap;
    }
	
	
	
	
	
	
	/**
	 * Method that gets the collection data for list of aformids
	 * @param aformId
	 * @param formId
	 * @return
	 * @throws CtdbException
	 */
    public HashMap<String,DataCollectionExport> getDataCollectionExport_LOCKED(int aformId, Form form) throws CtdbException {
    	PreparedStatement stmt = null;
		ResultSet rs = null;
		HashMap<String,DataCollectionExport> dceMap = new HashMap<String,DataCollectionExport>();

		try
		{

			String sql = "select r.*, pr.*, pr.submitanswer " +
					 " from response r, patientresponse pr " +
					 " where r.responseid = pr.responseid " +
					 " and  r.administeredformid = ? ";
					 
						 
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, aformId);
			rs = stmt.executeQuery();
			
			HashMap<String,Question> questionMap = form.getQuestionMap();
			HashMap<Integer,Section> sectionMap = form.getSectionMap();

			int order = 0;
			int repeatableGroupCounter = 1;
			while ( rs.next() )
			{
				int sectionId = rs.getInt("dict_sectionid");
				int questionId = rs.getInt("dict_questionid");

				String answer = rs.getString("answer");
				String submitAnswer = rs.getString("submitanswer");
		
				String sectionIdQuestionIdKey = sectionId + "_" + questionId;
	

				if(dceMap.containsKey(sectionIdQuestionIdKey)) {
					//this means this is part of a multiple choice question...so get the object and append the answer to the 
					DataCollectionExport dce = dceMap.get(sectionIdQuestionIdKey);
					String currentAnswer = dce.getAnswer();
					String currentSubmitAnswer = dce.getSubmitAnswer();
					dce.setAnswer(currentAnswer + ";" + answer);
					dce.setSubmitAnswer(currentSubmitAnswer + ";" + submitAnswer); 
					
				}else {
					DataCollectionExport dce = new DataCollectionExport();
					//dce.setOrder(order);
					dce.setSectionId(sectionId);
					dce.setQuestionId(questionId);

					dce.setAnswer(answer);
					dce.setSubmitAnswer(submitAnswer);

					dceMap.put(sectionIdQuestionIdKey, dce);
				}
			}
			
			
			
			for ( List<Section> row : form.getRowList() ) {
				for ( Section section : row ) {
					if ( section != null ) {
						List<Question> questionsList = section.getQuestionList();
						int questionOrder = 1;
						for (Question question : questionsList) {
							int formSectionId = section.getId();
							int formQuestionId = question.getId();
							String key =  formSectionId + "_" + formQuestionId;
							DataCollectionExport dce = dceMap.get(key);
							if (dce == null) {
								dce = new DataCollectionExport();
								dce.setSectionId(formSectionId);
								dce.setQuestionId(formQuestionId);
								dceMap.put(key, dce);
							}
						/*if(dce != null) {*/
							
							boolean isRepeatable = section.isRepeatable();
							int repeatedSectionParent = section.getRepeatedSectionParent();
							String groupName = section.getRepeatableGroupName();
							FormQuestionAttributes qa = question.getFormQuestionAttributes();
							String dataElementName = qa.getDataElementName();
							String sectionName = section.getName();
							String questionName = question.getName();
							String questionText = question.getText();
							String columnLabel = "";
							
							//need to add integer suffix to repeatables
							if(isRepeatable && repeatedSectionParent == -1) {
								//this means its the parent repeatable section
								repeatableGroupCounter = 1;
							}else if(isRepeatable && repeatedSectionParent != -1 && questionOrder == 1) {
								repeatableGroupCounter ++;
							}

							if(isRepeatable) {
								
								columnLabel = columnLabel + "_" + repeatableGroupCounter;
								
								if(groupName != null && !groupName.trim().equals("") && dataElementName != null && !dataElementName.trim().equals("")) {
									columnLabel = groupName + "_" + repeatableGroupCounter + "." + dataElementName;
								}else {
									columnLabel = sectionName + "_" + repeatableGroupCounter + "." + questionName;
								}
							}else {
								
								if(groupName != null && !groupName.trim().equals("") && dataElementName != null && !dataElementName.trim().equals("")) {
									columnLabel = groupName + "." + dataElementName;
								}else {
									columnLabel = sectionName + "." + questionName;
								}
							}

							dce.setOrder(order);
							dce.setGroupName(groupName);
							dce.setDataElementName(dataElementName);
							
							
							dce.setSectionName(sectionName);
							dce.setQuestionName(questionName);
							dce.setQuestionText(questionText);
							dce.setColumnLabel(columnLabel);
							dce.setRepeatedSectionParent(repeatedSectionParent); 
							dce.setRepeatable(isRepeatable); 
							dce.setQuestionOrder(questionOrder); 
							order++;
						/*}*/
							questionOrder++;
						}
					}
					
				}
			}
			
			
			
			
			
			
			
			
			
			
		}
		catch ( SQLException sqle )
		{
			throw new CtdbException("Unable to get \"data collection export\" from DAO: " + sqle.getLocalizedMessage(), sqle);
		}
		finally
		{
			this.close(rs);
			this.close(stmt);
		}

    	return dceMap;
    }
	
	
	
	
	
	
	
	
	
	
	
    
	/**
	 * Method to return unique responsedraftid based on adminId, sectionId and questionId combination		
	 * @param aformId
	 * @param questionId
	 * @param sectionId
	 * @return
	 * @throws CtdbException
	 */
	public Integer getDataEntryDraftIdForGivenResponse(int aformId, int questionId, int sectionId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Integer responseDraftId = 0;
		try {
			stmt = conn
					.prepareStatement(" select  rd.responsedraftid responseDraftId from  dataentrydraft ded, responsedraft rd where  ded.dataentrydraftid = rd.dataentrydraftid"
							+ " and ded.administeredformid ="
							+ aformId
							+ " and rd.questionid ="
							+ questionId
							+ " and rd.sectionid =" + sectionId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				responseDraftId = Integer.valueOf(rs
						.getString("responseDraftId"));
			}

		} catch (SQLException sqle) {
			throw new CtdbException(" Failure getting responseDraftId "
					+ sqle.getMessage(), sqle);
		}
		return responseDraftId;

	}





	/**
	 * Method to get the list of active data collected from in seleted order
	 * 
	 * @param intervalId
	 * @param subjectId
	 * @param studyId
	 * @return forms
	 * @throws CtdbException
	 */
	public List<AdministeredForm> getAdmininisteredFormForSubjectInterval(
			String intervalId, String subjectId, int userId, int studyId)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<AdministeredForm> afs = new ArrayList<AdministeredForm>();
		try {
			stmt = conn.prepareStatement(" select af.*,af.formid formid,af.finallockby finalLockDate,ded.lockdate ded_lockedDate  from administeredform af,dataentrydraft ded,form f "
							+ " where  af.administeredformid = ded.administeredformid and	f.formid = af.formid "
							+ " and af.intervalid="
							+ intervalId
							+ " and patientid = "
							+ subjectId
							+ " and ded.dataenteredby = "
							+ userId
							+ " and f.protocolid = " + studyId);

			rs = stmt.executeQuery();
			AdministeredForm af;
			Form form;
			while (rs.next()) {
				af = new AdministeredForm();
				form = new Form();
				form.setId(rs.getInt("formid"));
				af.setForm(form);
				af.setFinalLockDate(rs.getDate("finalLockDate"));
				af.setDataEntryDraft_LockedDate(rs.getDate("ded_lockedDate"));
				afs.add(af);
			}
		return afs;
		} catch (SQLException sqle) {
			throw new CtdbException(" Failure getting admin from  list "
					+ sqle.getMessage(), sqle);
		}finally{
			this.close(rs);
			this.close(stmt);
		}
	

	}
	
	/**
	 * Method to get Admin forms for interval subject and study ids combination used for next previous and jumpto iteration
	 * @param intervalId
	 * @param subjectId
	 * @param studyId
	 * @return
	 * @throws CtdbException
	 */
	public List<AdministeredForm> getAdminFormForSubjectIntervalAndStudy(int intervalId, int subjectId, int studyId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<AdministeredForm> afs = new ArrayList<AdministeredForm>();
		try {
	
			
			stmt = conn.prepareStatement(" select af.administeredformid, af.eformid eformid,af.finallockdate finalLockDate,ded.lockdate ded_lockedDate,ded.dataenteredby userId,ded.coll_status status, af.is_cat is_cat from administeredform af, dataentrydraft ded, eform ef "
					+ " where af.administeredformid = ded.administeredformid and ef.eformid = af.eformid and intervalid="+intervalId+" and patientid ="+subjectId+" and ef.protocolid="+studyId);
	
			
			rs = stmt.executeQuery();
			AdministeredForm af;
			Form form;
			while (rs.next()) {
				af = new AdministeredForm();
				af.setId(rs.getInt("administeredformid"));
				form = new Form();
				form.setId(rs.getInt("eformid"));
				af.setForm(form);
				af.setFinalLockDate(rs.getDate("finalLockDate"));
				af.setDataEntryDraft_LockedDate(rs.getDate("ded_lockedDate"));
				af.setUserOneEntryId(rs.getInt("userId"));
				af.setEntryOneStatus(rs.getString("status"));
				af.setCAT(Boolean.valueOf(rs.getString("is_cat")));
				String status= rs.getString("status");
				if(!status.isEmpty()&& (status!=null) &&status.equalsIgnoreCase(CtdbConstants.DATACOLLECTION_STATUS_COMPLETED)){
						af.setMarkAsCompleted(true);
				}
				afs.add(af);
			}
			return afs;
		} catch (SQLException sqle) {
			throw new CtdbException(" Failure getting admin from  list "
					+ sqle.getMessage(), sqle);
		}finally{
			this.close(rs);
			this.close(stmt);
		}
	

	}
	
	
	/***
	 * Method to get AdministeredForm object for given formId,intervalId, userId,  subjectId and studyId combination
	 * @param formId
	 * @param intervalId
	 * @param userId
	 * @param subjectId
	 * @param studyId
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws CtdbException
	 */
	public AdministeredForm getAdminFormForSubjectIntervalStudyForGivenForm(int formId,int intervalId,int subjectId, int studyId)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		AdministeredForm adminForms = new AdministeredForm();
		boolean moreThanOneRecord = false;
		try {
			StringBuffer sql = new StringBuffer(50);
	
			sql.append("select af.administeredformid as aFormId,ded.coll_status status  from administeredform af,eform ef,dataentrydraft ded where af.eformid = ef.eformid and  af.administeredformid = ded.administeredformid  ");
			sql.append(" and  ef.eformid = "+ formId+" and  af.intervalid = "+intervalId+"  and af.patientid = "+subjectId+" and ef.protocolid ="+studyId);
			stmt = this.conn.prepareStatement(sql.toString());
			rs = stmt.executeQuery();
			int rsCount = 0;
			while (rs.next()) {
					adminForms.setId(rs.getInt("aFormId"));
					String status= rs.getString("status");
					if(!status.isEmpty()&& (status!=null) &&status.equalsIgnoreCase(CtdbConstants.DATACOLLECTION_STATUS_COMPLETED)){
							adminForms.setMarkAsCompleted(true);
					}
					rsCount = rsCount + 1;
			}
			
			if(rsCount>1){
				moreThanOneRecord = true;
			}

		} catch (SQLException e) {
			throw new CtdbException("ResponseManagerDao->getAdminFormForSubjectIntervalStudyUserForGivenForm:Unable to get AdministeredForm form for given parameters combination of  the IDs: " +formId+":"+ intervalId +":"+subjectId+":"+studyId+":"+ e.getMessage(), e);
		} catch (Exception e){
			if(moreThanOneRecord){
			throw new CtdbException("More than one record exist");
			}
		}finally {
			this.close(rs);
			this.close(stmt);
		}
		return adminForms;
	}

	public List<FormCollectionDisplay> getCollectedPatientDataByFormName(long studyId, long subjectId, String formName)	throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		FormCollectionDisplay  summary = null;
		boolean moreThanOneRecord = false;
		List<FormCollectionDisplay> collectedForms = new ArrayList<FormCollectionDisplay>();
		
		try {
			String sql = "SELECT fm.name as formname, fm.formid as formid, af.administeredformid as administeredformid, af.intervalid, af.patientid, " +
				"to_char(af.visitdate, 'MM/DD/YYYY HH24:MI') as pvisitdate, " +
				"dd.dataentrydraftid, dd.coll_status, dd.dataentryflag, il.name as intervalname, dd.dataenteredby " +
				"FROM form fm, dataentrydraft dd, administeredform af, interval il " + 
				"WHERE af.formid = fm.formid AND af.administeredformid = dd.administeredformid " +  
				"AND af.intervalid = il.intervalid AND af.patientid = ? " +   
				"AND il.protocolid = ? " +  
				"AND UPPER(fm.name) = upper(?) ORDER BY dd.createddate ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, subjectId);
			stmt.setLong(2, studyId);
			stmt.setString(3, formName);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				summary = new FormCollectionDisplay();
				summary.setFormName(rs.getString("formname"));
				summary.setIntervalName(rs.getString("intervalname"));
				summary.setpVisitDate(rs.getString("pvisitdate"));
				summary.setCollectionStatus(rs.getString("coll_status"));
				summary.setFormId(rs.getInt("formid"));
				summary.setAdministeredFormId(rs.getInt("administeredformid"));
				summary.setDataEntryByUserId(rs.getInt("dataenteredby"));
				summary.setPatientId(rs.getInt("patientid"));
				summary.setIntervalId(rs.getInt("intervalid"));
				collectedForms.add(summary);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("ResponseManagerDao->getCollectedPatientDataByFormName ERROR: Subject ID: "+ subjectId + 
				", Study ID: "  + studyId + ":" +  e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return collectedForms;
	}
	
	public List<FormInterval> getActiveFormsListInInterval(int intervalId)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			List<FormInterval> forms = new ArrayList<FormInterval>();

			StringBuffer sql = new StringBuffer(200);
			sql.append("select fi.eformid fid,fi.intervalformorder fio from eform ef, form_interval fi  ");
			sql.append(" where fi.eformid = ef.eformid and fi.intervalid = ? ");
			sql.append(" order by intervalformorder");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, intervalId);

			rs = stmt.executeQuery();
			FormInterval form;
			while (rs.next()) {
				form = new FormInterval();
				form.setId(rs.getInt("fid"));
				form.setIntervalOrder(rs.getInt("fio"));
				forms.add(form);
			}
			return forms;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to retrieve forms associated with intervals: "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public Patient getPatientForCollection(int protocolId, int patientid) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		PatientProtocol patientprotocol;
		Patient patient = null;

		try {
			String sql = "select pp.patientid patientid,pp.subjectid,p.guid," + getDecryptionFunc("mrn")
					+ "as mrn,p.patientid,p.version from patient p,patientprotocol pp where p.patientid=pp.patientid \r\n"
					+ "and pp.protocolid=? and p.patientid = ? and p.deleteflag = false";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, protocolId);
			stmt.setLong(2, patientid);
			rs = stmt.executeQuery();

			while (rs.next()) {
				patient = new Patient();
				patient.setId(rs.getInt("patientid"));
				patient.setGuid(rs.getString("guid"));
				patient.setSubjectId(rs.getString("subjectid"));
				patient.setMrn(rs.getString("mrn"));
				patient.setVersion(new Version(rs.getInt("version")));
				patientprotocol = new PatientProtocol();
				patientprotocol.setSubjectId(rs.getString("subjectid"));
				patient.setPatientprotocol(patientprotocol);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to retrieve subject.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return patient;
	}

	/**
	 * Method to get site for given subject and protocol should be only set to only one
	 * 
	 * @param patientId
	 * @param protocolId
	 * @return siteId
	 * @throws CtdbException
	 */
	public Integer getSubjectSiteForStudy(Integer patientId, Integer protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Integer siteID = Integer.MIN_VALUE;

		try {
			String sql = "select siteid from patientprotocol where patientid = ? and protocolid= ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, patientId);
			stmt.setLong(2, protocolId);
			rs = stmt.executeQuery();

			if (rs.next()) {
				siteID = rs.getInt("siteid");
			}
		} catch (SQLException e) {
			throw new CtdbException("Site ID Not found.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return siteID;
	}

	/**
	 * Retrieves the answers from the patientresponse table
	 * 
	 * @param administeredformid
	 *            the response id
	 * @return List the list of the answers
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	private List getSubmitAnswers(int administeredformid, int questionid,
			int questionversion) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select pr.submitanswer from patientresponse pr, response r ");
			sql.append("where pr.responseid = r.responseid ");
			sql.append(" and r.administeredformid = ?  and r.dict_questionid = ? and r.questionversion = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredformid);
			stmt.setLong(2, questionid);
			stmt.setInt(3, questionversion);

			rs = stmt.executeQuery();
			List answers = new ArrayList();
			while (rs.next()) {
				answers.add(rs.getString("submitanswer"));
			}
			return answers;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get submit answers for response "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves the answers from the patientresponse table
	 * 
	 * @param administeredformid the response id
	 * @return List the list of the answers
	 * @throws CtdbException thrown if any errors occur while processing
	 */
	private List getDraftSubmitAnswers(int administeredformid, int questionid,
			int questionversion) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select prd.submitanswer from patientresponsedraft prd, responsedraft rd, dataentrydraft ded ");
			sql.append(" where prd.responsedraftid = rd.responsedraftid ");
			sql.append(" and rd.dataentrydraftid = ded.dataentrydraftid ");
			sql.append(" and ded.administeredformid = ?  and rd.dict_questionid = ? and rd.questionversion = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, administeredformid);
			stmt.setLong(2, questionid);
			stmt.setInt(3, questionversion);

			rs = stmt.executeQuery();
			List answers = new ArrayList();
			while (rs.next()) {
				answers.add(rs.getString("submitanswer"));
			}
			return answers;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get submit answers for response "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	public List<String> getFinalSubmitAnswers(int responseId) throws CtdbException {
		List<String> answers = new ArrayList<String>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("select submitanswer from patientresponse ");
			sql.append("where responseid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, responseId);
			stmt.executeQuery();

			rs = stmt.executeQuery();
			
			while (rs.next()) {
				answers.add(rs.getString("submitanswer"));
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get the response final submit answers: " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return answers;
	}
	
	public List<String> getDraftCompletedSubmitAnswers(int responseDraftId) throws CtdbException {
		List<String> answers = new ArrayList<String>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("select submitanswer from patientresponsedraft ");
			sql.append("where responsedraftid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, responseDraftId);
			stmt.executeQuery();

			rs = stmt.executeQuery();
			
			while (rs.next()) {
				answers.add(rs.getString("submitanswer"));
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get the draft competed submit  answers: " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return answers;
	}
	
	/**
	 * Retrieves the answers from the patientresponse table
	 * 
	 * @param responseId the response id
	 * @return List the list of the answers
	 * @throws CtdbException thrown if any errors occur while processing
	 */
	private List<String> getSubmitAnswers(int responseId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> answers = new ArrayList<String>();

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select * from patientresponse ");
			sql.append("where responseid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, responseId);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				answers.add(rs.getString("submitanswer"));
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get answers for response with id " + responseId + " : " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return answers;
	}
	
	/**
	 * Retrieves all data entries for the data entry 1 or 2. The list includes
	 * the reassign information and data entry lock information. This is for
	 * View Audit for who has the View Data Entry Audit Trail privilege.
	 * 
	 * @param admFormId
	 *            The administered form ID for retrieving the data entry.
	 * @param dataEntryFlag
	 *            data entry 1 or 2
	 * @return List the list of data entry information including the reassign
	 *         info and lock info.
	 * @throws CtdbException
	 *             thrown if any errors occur
	 */
	public List<EditAnswerDisplay> getInitialDataEntries(int admFormId, AdministeredForm admForm) throws CtdbException {
		List<EditAnswerDisplay> edits = new ArrayList<EditAnswerDisplay>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
    	
		try {
			final String sql =
					"select * 																																						"
							+ "from (select userfullname, username, updateddate, administeredformid,dict_sectionid,dict_questionid,responseeditversion, prevanswer,                            "
							+ "			LEAD(prevanswer, 1, null) OVER (PARTITION by administeredformid,dict_sectionid,dict_questionid ORDER BY responseeditversion NULLS LAST) AS curranswer, "
							+ "         LEAD(prevsubmitanswer, 1, null) OVER (PARTITION by administeredformid,dict_sectionid,dict_questionid ORDER BY responseeditversion NULLS LAST) AS currentsubmitanswer "
							+ "	  	 from ( select u.lastname||', '||u.firstname as userfullname, u.username, re.updateddate, re.administeredformid,                                           "
							+ "   				re.dict_questionid, re.dict_sectionid, STRING_AGG(re.previousanswer, ', '  ) as prevanswer, 												   "
							+ "					STRING_AGG(re.previousanswer, ', '  ) as prevsubmitanswer, re.responseeditversion                              								   "
							+ "			    from responseedit re, usr u                                                                                                                        "
							+ "	     		where re.in_progress_data_flag = 'Y' and re.updatedby = u.usrid and re.auditstatus is null                         								   "
							+ "		 		group by u.lastname, u.firstname, u.username, re.updateddate, re.administeredformid,re.dict_questionid, re.dict_sectionid, re.responseeditversion  "
							+ "		 		union all                                                                                                                                          "
							+ "		 		select  u.lastname||', '||u.firstname as userfullname, u.username, d.updateddate, d.administeredformid,                                            "
							+ "					rd.dict_questionid, rd.dict_sectionid,STRING_AGG(pd.answer, ', ' ) as currentanswer, STRING_AGG(pd.submitanswer, ', ' ) as currentsubmitanswer, null "
							+ "		 		from patientresponsedraft pd, responsedraft rd, dataentrydraft d, usr u                                                                            "
							+ "		 		where pd.responsedraftid = rd.responsedraftid and d.dataentrydraftid = rd.dataentrydraftid                                                         "
							+ "					and d.dataenteredby = u.usrid                                                                                                                  "
							+ "		 		group by u.lastname, u.firstname, u.username, d.updateddate, d.administeredformid,rd.dict_questionid, rd.dict_sectionid                            "
							+ "			  ) b                                                                                                                                                  "
							+ "      ) a                                                                                                                                                       "
							+ "where administeredformid = ? and responseeditversion is not null                                                                                                "
							+ "order by administeredformid,dict_sectionid,dict_questionid                                                                                                      ";


			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, admFormId);
			
			rs = stmt.executeQuery();

			int questionid = Integer.MIN_VALUE;
			int questionVersion = Integer.MIN_VALUE;
			int editversion = Integer.MIN_VALUE;
			Form form = admForm.getForm();
			HashMap<String,Question> questionMap = form.getQuestionMap();
			HashMap<Integer,Section> sectionMap = form.getSectionMap();
			
			while(rs.next()) {
				EditAnswerDisplay ead = new EditAnswerDisplay();
				ead.setEditDate(rs.getTimestamp("updateddate"));
				if (rs.getString("userfullname") != null && !rs.getString("userfullname").equals(""))
					ead.setUsername(rs.getString("userfullname"));
				else 
					ead.setUsername(rs.getString("username"));
				ead.setAdministeredformId(rs.getInt("administeredformid"));
				
				int reQuestionId = rs.getInt("dict_questionid");
				int reSectionId = rs.getInt("dict_sectionid");
				String questionMapKey = "S_" + reSectionId + "_Q_" + reQuestionId;
				Question question = questionMap.get(questionMapKey);
				Section section = sectionMap.get(reSectionId);
				
				ead.setQuestionId(reQuestionId);
				ead.setSectionId(reSectionId);
				
				String name = question.getName();
				ead.setQuestionName(name);
				
				String text = question.getText();
				ead.setQuestionText(text);
				
				FormQuestionAttributes questionAttribute = question.getFormQuestionAttributes();
				
				String sectionName = section.getName();
				ead.setSectionName(sectionName);
				
				String dataElementName = questionAttribute.getDataElementName();
				ead.setDataElementName(dataElementName); 
				
				int type = question.getType().getValue();
				
				ead.setQuestionType(QuestionType.getByValue(type).toString());
				ead.setPrevAnswer(rs.getString("prevanswer"));

				// if the question have the setting to show PV, get the submitanswer instead of answer from
				// patientresponsedraft table
				if (question.isDisplayPV()) {
					ead.setEditAnswer(rs.getString("currentsubmitanswer"));
				} else {
					ead.setEditAnswer(rs.getString("curranswer"));
				}
				edits.add(ead);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get the data entry list.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return edits;
	}
	
	
	
		/**
		 * Checks to see if there are any data collections for a given eform shortname in all protocols
		 * @param eformShortName
		 * @return
		 * @throws CtdbException
		 */
		public boolean areThereDataCollectionsInAnyProtocol(String eformShortName) throws CtdbException {
			
			PreparedStatement stmt = null;
			ResultSet rs = null;

			PreparedStatement stmt2 = null;
			ResultSet rs2 = null;

			
			boolean areThereDataCollection = false;

			try {

				stmt = this.conn.prepareStatement(SQL_RETRIEV_EFORMID.toString());
				stmt.setString(1, eformShortName);

				rs = stmt.executeQuery();

				while(rs.next()) {

					
					int eformid = rs.getInt("eformid");
					
					stmt2 = this.conn.prepareStatement(SQL_RETRIEV_AFORM_COUNT.toString());
					stmt2.setInt(1, eformid);
					
					rs2 = stmt2.executeQuery();
					
					int count = 0;
					if (rs2.next()) {
						count = rs2.getInt("count");
					}
					
					if(count > 0) {
						areThereDataCollection = true;
						break;
					}
					
				}

				return areThereDataCollection;
				
			}catch (SQLException e) {
				throw new CtdbException("unable to determine if there are any collections",e);
			} finally {
				this.close(rs);
				this.close(stmt);
				this.close(rs2);
				this.close(stmt2);
			}
			
			
		}
		
		/**
		 * Get the query from summary_query table for the given chartName
		 * 
		 * @param chartName
		 * @return SummaryQuery
		 * @throws CtdbException thrown if any errors occur while processing
		 */
		
		public SummaryQueryPF getSummaryQuery(String chartName) throws CtdbException {
			//get the query from summaryQuery Table
			SummaryQueryPF query = new SummaryQueryPF();
			PreparedStatement stmt = null;
			ResultSet rs = null;

			try {
				stmt = this.conn.prepareStatement(SQL_RETRIEV_SUMMARY_QUERY);
				stmt.setString(1, chartName);
				rs = stmt.executeQuery();
				while(rs.next()) {
					query.setId(rs.getInt("id"));
					query.setQuery(rs.getString("query"));
					query.setShortname(rs.getString("shortname"));
					query.setRequiresNumber(rs.getBoolean("requires_number"));
					query.setRequiresPostgres(rs.getBoolean("requires_postgres"));
					query.setRequiresSparql(rs.getBoolean("requires_sparql"));
					query.setRequiresProforms(rs.getBoolean("requires_proforms"));
					query.setRequiresText(rs.getBoolean("requires_text"));
					query.setRequiresSite(rs.getBoolean("requires_site"));
					query.setRequiresStudy(rs.getBoolean("requires_study"));
				}
				
			}
			catch (SQLException e) {
				throw new CtdbException("Unable to get query for chartName " + chartName + " : " + e.getMessage(), e);
			}
			finally {
				this.close(rs);
				this.close(stmt);
			}
			
			return query;
		}
		
		/**
		 * Returns collection for the given query from summary_query table 
		 * 
		 * @param chartName
		 * @return HashMap
		 * @throws CtdbException thrown if any errors occur while processing
		 */
		public HashMap<String, String> getSummaryDataResult(String chartName) throws CtdbException {
			//get the query from SummaryQuery
			SummaryQueryPF query = getSummaryQuery(chartName);
			HashMap<String, String> summaryResults = new HashMap<String, String>();
			PreparedStatement stmt = null;
			ResultSet rs = null;
			//Run the query and return the result
			try {
				
				stmt = this.conn.prepareStatement(query.getQuery());
				rs = stmt.executeQuery();
				while(rs.next()) {
					String category = ((String) rs.getString("category"));
					String count =  ((String) rs.getString("count"));
					summaryResults.put(category,count);

				}
				
			}
			catch (SQLException e) {
				throw new CtdbException("Unable to get query for chartName " + chartName + " : " + e.getMessage(), e);
			}
			finally {
				this.close(rs);
				this.close(stmt);
			}
			
			
			return summaryResults;
		}
		
		
		
		
		
		
		
		/**
		 * Checks to see if for a givien eform shortname if it is attached to any visittype in all protocols 
		 * @param eformShortName
		 * @return
		 * @throws CtdbException
		 */
		public boolean areThereEformsAttachedToAnyVisitType(String eformShortName) throws CtdbException {
			
			PreparedStatement stmt = null;
			ResultSet rs = null;

			PreparedStatement stmt2 = null;
			ResultSet rs2 = null;

			
			boolean areThereEforms = false;

			try {

				stmt = this.conn.prepareStatement(SQL_RETRIEV_EFORMID.toString());
				stmt.setString(1, eformShortName);

				rs = stmt.executeQuery();

				while(rs.next()) {

					
					int eformid = rs.getInt("eformid");
					
					stmt2 = this.conn.prepareStatement(SQL_RETRIEV_EFORM_INTERVAL_COUNT.toString());
					stmt2.setInt(1, eformid);
					
					rs2 = stmt2.executeQuery();
					
					int count = 0;
					if (rs2.next()) {
						count = rs2.getInt("count");
					}
					
					if(count > 0) {
						areThereEforms = true;
						break;
					}
					
				}

				return areThereEforms;
				
			}catch (SQLException e) {
				throw new CtdbException("unable to determine if there are any collections",e);
			} finally {
				this.close(rs);
				this.close(stmt);
				this.close(rs2);
				this.close(stmt2);
			}
			
			
		}
}
