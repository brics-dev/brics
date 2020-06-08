package gov.nih.nichd.ctdb.response.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;

import org.json.JSONArray;

import gov.nih.nichd.ctdb.common.CasProxyTicketException;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.util.DateFormatter;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.FormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.form.util.FormDataStructureUtility;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.protocol.dao.ProtocolManagerDao;
import gov.nih.nichd.ctdb.protocol.domain.ClinicalLocation;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.protocol.domain.PointOfContact;
import gov.nih.nichd.ctdb.protocol.domain.Procedure;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.response.domain.AdverseEvent;
import gov.nih.nichd.ctdb.response.domain.ScheduleReport;
import gov.nih.nichd.ctdb.response.domain.ScheduleReportFilter;
import gov.nih.nichd.ctdb.response.domain.SubmissionSummaryReport;
import gov.nih.nichd.ctdb.response.domain.ViewAuditorComment;
import gov.nih.nichd.ctdb.response.form.FormVisitTypeStatusSubjectMatrix;
import gov.nih.nichd.ctdb.response.form.ReportingForm;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.dictionary.model.EformPfCategory;


/**
 * ReportingManagerDao interacts with the Data Layer for the ReportingManager. The only job of the DAO is to manipulate
 * the data layer.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ReportingManagerDao extends CtdbDao {
	/**
	 * Private Constructor to hide the instance creation implementation of the ReportingManagerDao object in memory.
	 * This will provide a flexible architecture to use a different pattern in the future without refactoring the
	 * ReportingManager.
	 */
	private ReportingManagerDao() {}

	/**
	 * Method to retrieve the instance of the ReportingManagerDao.
	 *
	 * @return ReportingManagerDao data object
	 */
	public static synchronized ReportingManagerDao getInstance() {
		return new ReportingManagerDao();
	}

	/**
	 * Method to retrieve the instance of the ReportingManagerDao. This method accepts a Database Connection to be used
	 * internally by the DAO. All transaction management will be handled at the BusinessManager level.
	 *
	 * @param conn Database connection to be used within this data object
	 * @return ReportingManagerDao data object
	 */
	public static synchronized ReportingManagerDao getInstance(Connection conn) {
		ReportingManagerDao dao = new ReportingManagerDao();
		dao.setConnection(conn);
		return dao;
	}

	/**
	 * Method to get interval list for the selected study
	 * 
	 * @param studyId
	 * @return list of interval names
	 * @throws CtdbException
	 */
	public List<Interval> getIntervalListForSelectedStudy(long studyId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Interval> list = new ArrayList<Interval>();

		try {
			String sql = "select * from interval where protocolid = ? order by orderval, name ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, studyId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Interval interval = new Interval();
				interval.setName(rs.getString("name"));
				list.add(interval);
			}
		} catch (SQLException sqle) {
			throw new CtdbException("Unable to get \"interval_list_for_study\" from DAO: " + sqle.getLocalizedMessage(),
					sqle);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return list;
	}



	/**
	 * Method to get list of String GUID for selected study (protocol)
	 * 
	 * @param studyId
	 * @return list of guid
	 * @throws CtdbException
	 */
	public List<Patient> getGUIDListForSelectedStudy(long studyId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Patient> list = new ArrayList<Patient>();

		try {
			String sql = " select p.patientid id,p.guid guid_p, " + getDecryptionFunc("p.mrn")
					+ " mrn_p,pp.subjectid nrn_pp from patient p,patientprotocol pp where p.patientid = pp.patientid and pp.protocolid = ? and p.deleteflag = false";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, studyId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Patient patient = new Patient();
				patient.setId(rs.getInt("id"));
				patient.setGuid(rs.getString("guid_p"));
				patient.setMrn(rs.getString("mrn_p"));
				patient.setSubjectId(rs.getString("nrn_pp"));
				list.add(patient);
			}
		} catch (SQLException sqle) {
			throw new CtdbException("Unable to get \"guid_mrn_list_for_study\" from DAO: " + sqle.getLocalizedMessage(),
					sqle);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return list;
	}


	/**
	 * Method to return list of collection status for selected guid for form and visit type in a study
	 * 
	 * @param studyId
	 * @param subjectId
	 * @return
	 * @throws CtdbException
	 */
	public List<FormVisitTypeStatusSubjectMatrix> getSubjectMatrixForFormVsVisitTypeWithCollectionStatusFilteredableByGuid(
			long studyId, String subjectId) throws CtdbException {
		PreparedStatement fromsInStudyStmt = null;
		PreparedStatement vistTypesStmt = null;
		PreparedStatement collectionStatusStmt = null;
		PreparedStatement formVtAssocStmt = null;
		PreparedStatement patientGuidStmt = null;
		PreparedStatement patientSubjectIdStmt = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		ResultSet rs4 = null;
		ResultSet rsPatientGuid = null;
		ResultSet rsPatientSubjectId = null;
		List<FormVisitTypeStatusSubjectMatrix> matrix = new ArrayList<FormVisitTypeStatusSubjectMatrix>();
		long patientId = -1;

		try {
			String sqlPatient = "select pp.patientid from patient pat, patientprotocol pp where pp.patientid = pat.patientid and pp.protocolid = ? and pat.guid = ?";
			patientGuidStmt = this.conn.prepareStatement(sqlPatient);
			patientGuidStmt.setLong(1, studyId);
			patientGuidStmt.setString(2, subjectId);
			rsPatientGuid = patientGuidStmt.executeQuery();

			if (rsPatientGuid.next()) {
				patientId = Long.valueOf(rsPatientGuid.getInt("patientid"));
				rsPatientGuid.close();
			} else {
				sqlPatient = "select patientid from patientprotocol where protocolid = ? and subjectid = ?";
				patientSubjectIdStmt = this.conn.prepareStatement(sqlPatient);
				patientSubjectIdStmt.setLong(1, studyId);
				patientSubjectIdStmt.setString(2, subjectId);
				rsPatientSubjectId = patientSubjectIdStmt.executeQuery();
				if (rsPatientSubjectId.next()) {
					patientId = Long.valueOf(rsPatientSubjectId.getInt("patientid"));
				}
				rsPatientSubjectId.close();
			}

			/* Get the all of the forms associated to the study */
			String sql = "select distinct ef.eformid, ef.name fname from eform ef,protocol pr,form_interval fi "
					+ " where pr.protocolid = ef.protocolid  and fi.eformid = ef.eformid and ef.protocolid = ? order by fname  ";
			fromsInStudyStmt = this.conn.prepareStatement(sql);
			fromsInStudyStmt.setLong(1, studyId);
			rs = fromsInStudyStmt.executeQuery();

			// Iterate through all forms in Study
			while (rs.next()) {
				FormVisitTypeStatusSubjectMatrix form = new FormVisitTypeStatusSubjectMatrix();
				Map<String, String> visitTypeStatusMap = new HashMap<String, String>();
				form.setFormName(rs.getString("fname"));
				long formid = rs.getLong("eformid");

				// Filled in details for each form only when guid or subjectid is selected
				if (patientId > 0) {
					/* Get all the intervals for given eform id in this study */
					sql = "select i.intervalid, i.name iname, fi.mandatory required from eform ef, form_interval fi, interval i where ef.eformid = fi.eformid and  "
							+ "i.intervalid = fi.intervalid and ef.eformid = ? and ef.protocolid = ? ";
					vistTypesStmt = this.conn.prepareStatement(sql);
					vistTypesStmt.setLong(1, formid);
					vistTypesStmt.setLong(2, studyId);
					rs2 = vistTypesStmt.executeQuery();

					// Iterate through all visit types for each form
					while (rs2.next()) {
						long intervalid = rs2.getLong("intervalid");
						String intervalName = rs2.getString("iname");
						boolean isRequired = rs2.getBoolean("required");

						// Check if form and visit type are associated or not
						sql = "select * from form_interval where eformid = ? and intervalid = ? ";
						formVtAssocStmt = this.conn.prepareStatement(sql);
						formVtAssocStmt.setLong(1, formid);
						formVtAssocStmt.setLong(2, intervalid);
						rs4 = formVtAssocStmt.executeQuery();

						if (rs4.next()) {
							// The form and visit type are associated with each other
							visitTypeStatusMap.put(intervalName, CtdbConstants.DATACOLLECTION_STATUS_NOTSTARTED);
							form.setFormRequiredInVisitType(isRequired);
							form.getIsRequiredPerVisitType().put(intervalName, isRequired);
						}
						// The form and visit type are NOT associated.
						else {
							visitTypeStatusMap.put(intervalName, "-");
						}

						rs4.close();
						form.setVisitType(intervalName);

						/* Get the column status for given eform id, interval id, patientid */
						StringBuffer sql2 = new StringBuffer(100);
						sql2.append(
								"select coll_status, i.name iname, ef.name fname, fi.mandatory required from administeredform af, dataentrydraft ded, form_interval fi, "
										+ "eform ef, interval i where af.administeredformid = ded.administeredformid and ef.eformid = af.eformid and af.intervalid = i.intervalid and "
										+ "af.eformid = ? and af.intervalid = ? and fi.intervalid = i.intervalid and fi.eformid = ef.eformid and (af.patientid = ? or af.patientid is null)");

						collectionStatusStmt = this.conn.prepareStatement(sql2.toString());

						collectionStatusStmt.setLong(1, formid);
						collectionStatusStmt.setLong(2, intervalid);
						collectionStatusStmt.setLong(3, patientId);
						rs3 = collectionStatusStmt.executeQuery();

						if (rs3.next()) {
							visitTypeStatusMap.put(intervalName, rs3.getString("coll_status"));
							form.setFormRequiredInVisitType(isRequired);
							form.getIsRequiredPerVisitType().put(intervalName, isRequired);
						}
						rs3.close();
					}
					rs2.close();
				}

				form.setVisitTypeStatusMap(visitTypeStatusMap);
				matrix.add(form);
			}
		} catch (SQLException sqle) {
			throw new CtdbException(
					"Unable to get \"subjectvisitcollstatusmatrix\" from DAO: " + sqle.getLocalizedMessage(), sqle);
		} finally {
			this.close(rs);
			this.close(rs2);
			this.close(rs3);
			this.close(rs4);
			this.close(rsPatientGuid);
			this.close(rsPatientSubjectId);
			this.close(patientGuidStmt);
			this.close(patientSubjectIdStmt);
			this.close(fromsInStudyStmt);
			this.close(vistTypesStmt);
			this.close(collectionStatusStmt);
			this.close(formVtAssocStmt);
		}

		return matrix;
	}

	/**
	 * Method to get the "Forms Requiring Lock" study report from the "forms_requiring_lock_view" view
	 * 
	 * @param studyId - The ID of the study (or protocol)
	 * @return A list of ReportingForm objects from the query
	 * @throws CtdbException If an error occurred while querying the view.
	 */
	public List<ReportingForm> getFormsRequiringLockReport(long studyId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<ReportingForm> list = new ArrayList<ReportingForm>();

		try {
			String sql = "select *," + getDecryptionFunc("mrn")
					+ " d_mrn,subjectid d_nrn from forms_requiring_lock_view where study_id = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, studyId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				ReportingForm form = new ReportingForm();
				// commented since we don't need this after we implement json data table
				// form.setFrl_formName(rs.getString("form_name").replaceAll("[(]", "&#40;").replaceAll("[)]",
				// "&#41;"));
				form.setFrl_formName(rs.getString("form_name"));
				form.setFrl_intervalName(rs.getString("visit_type"));
				form.setFrl_guid(rs.getString("subject_guid"));
				form.setFrl_mrn(rs.getString("d_mrn"));
				form.setFrl_nrn(rs.getString("d_nrn"));
				form.setFrl_createdDate(DateFormatter.getFormattedDateWithTime(rs.getTimestamp("createddate")));

				if (rs.getTimestamp("updateddate") != null) {
					form.setFrl_updateDate(DateFormatter.getFormattedDateWithTime(rs.getTimestamp("updateddate")));
				} else {
					form.setFrl_updateDate("");
				}

				// Set the required value
				if (rs.getBoolean("is_required")) {
					form.setFrl_requiredOptional("Required");
				} else {
					form.setFrl_requiredOptional("Optional");
				}

				list.add(form);
			}
		} catch (SQLException sqle) {
			throw new CtdbException(
					"Unable to get \"forms_requiring_lock_view\" from DAO: " + sqle.getLocalizedMessage(), sqle);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return list;
	}

	/**
	 * Method to get the study report from study view
	 * 
	 * @return list of studyforms
	 * @throws CtdbException
	 */
	public List<ReportingForm> getStudyReport() throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<ReportingForm> list = new ArrayList<ReportingForm>();
		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append(" select sv.studyname studyName,sv.bricsstudyid sbsi, sv.pi pi,sv.studytype st,sv.studystatus ss,sv.patientcount spc,sv.adminformcount safc, sv.eformcount sefc, sv.lockedadminformcount slafc, sv.notlockedadminformcount snafc, sv.studyid studyid  from study_view sv order by studyName ASC");
			stmt = this.conn.prepareStatement(sql.toString());
			rs = stmt.executeQuery();
			while (rs.next()) {
				ReportingForm form = new ReportingForm();
				form.setStudyName(rs.getString("studyName"));
				form.setStudyPI(rs.getString("pi"));
				form.setStudyType(rs.getString("st"));
				form.setStudyStatus(rs.getString("ss"));
				form.setStudySubjectCount(rs.getString("spc"));
				form.setStudyAdminFormsCount(rs.getString("safc"));
				form.setBricsStudyId(rs.getString("sbsi"));
				form.setStudyEformCount(rs.getString("sefc"));
				form.setStudyLockedAdminFormsCount(rs.getString("slafc"));
				form.setStudyNotLockedAdminFormsCount(rs.getString("snafc"));
				form.setProtocolId(rs.getInt("studyid"));
				list.add(form);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get StudyList from DAO", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		
    	return list;
    }
    
    
    
    
    
    /**
     * Method to get the study report from study view
     * @return list of studyforms
     * @throws CtdbException
     */
    public List<ReportingForm> getDetailedStudyReport() throws CtdbException
    {
    	PreparedStatement stmt = null;
    	PreparedStatement stmt2 = null;
    	PreparedStatement stmt3 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		List<ReportingForm> list = new ArrayList<ReportingForm>();
		try{
			StringBuffer sql = new StringBuffer(200);
			sql.append(" select sv.studyname studyName,sv.bricsstudyid sbsi, sv.pi pi,sv.studytype st,sv.studystatus ss,sv.patientcount spc,sv.adminformcount safc, sv.eformcount sefc, sv.lockedadminformcount slafc, sv.notlockedadminformcount snafc, sv.studyid studyid  from study_view sv order by studyName ASC");
			stmt = this.conn.prepareStatement(sql.toString());
			rs = stmt.executeQuery();
			while (rs.next()) {
				ReportingForm form = new ReportingForm();
				form.setStudyName(rs.getString("studyName"));
				form.setStudyPI(rs.getString("pi"));
				form.setStudyType(rs.getString("st"));
				form.setStudyStatus(rs.getString("ss"));
				form.setStudySubjectCount(rs.getString("spc"));
				form.setStudyAdminFormsCount(rs.getString("safc"));
				form.setBricsStudyId(rs.getString("sbsi"));
				form.setStudyEformCount(rs.getString("sefc"));
				form.setStudyLockedAdminFormsCount(rs.getString("slafc"));
				form.setStudyNotLockedAdminFormsCount(rs.getString("snafc"));
				form.setProtocolId(rs.getInt("studyid"));
				list.add(form);
			}
			
			//for each protocol, get eforms assoc with the protocol and get the number of admin form records for each eform
			for ( ReportingForm form : list ) {
				//get eforms assoc with the protocol
				int protocolId = form.getProtocolId();
				String sql2 = "select * from eform where protocolid = ? order by name ASC";
				stmt2 = this.conn.prepareStatement(sql2);
				stmt2.setLong(1, protocolId);
				rs2 = stmt2.executeQuery();
				String protocolEformsAndCount = "";
				//ArrayList<String> protocolEformsAndCountArr = new ArrayList<String>();
				while (rs2.next()) {
					int eformId = rs2.getInt("eformid");
					String eformName = rs2.getString("name");
					//now get the number of admin form records for each eformid
					String sql3 = "select count(*) from administeredform where eformid = ?";
					stmt3 = this.conn.prepareStatement(sql3);
					stmt3.setLong(1, eformId);
					rs3 = stmt3.executeQuery();
					int count = 0;
					while (rs3.next()) {
						count = rs3.getInt("count");
					}
					if(protocolEformsAndCount.equals("")) {
						protocolEformsAndCount = eformName + " (" + count + ")";
					}else {
						protocolEformsAndCount = protocolEformsAndCount + "<br>" + eformName + " (" + count + ")";
					}
					//protocolEformsAndCountArr.add(eformName + " (" + count + ")");
				}
				form.setProtocolEformsAndAdminFormCount(protocolEformsAndCount);
				//form.setProtocolEformsAndAdminFormCountArr(protocolEformsAndCountArr);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get StudyList from DAO", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		
    	return list;
    }
    
    
    
    
    public List<ReportingForm> getDetailedStudyReportExport() throws CtdbException
    {
    	PreparedStatement stmt = null;
    	PreparedStatement stmt2 = null;
    	PreparedStatement stmt3 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		List<ReportingForm> list = new ArrayList<ReportingForm>();
		try{
			StringBuffer sql = new StringBuffer(200);
			sql.append(" select sv.studyname studyName,sv.bricsstudyid sbsi, sv.pi pi,sv.studytype st,sv.studystatus ss,sv.patientcount spc,sv.adminformcount safc, sv.eformcount sefc, sv.lockedadminformcount slafc, sv.notlockedadminformcount snafc, sv.studyid studyid  from study_view sv order by studyName ASC");
			stmt = this.conn.prepareStatement(sql.toString());
			rs = stmt.executeQuery();
			while (rs.next()) {
				int protocolId = rs.getInt("studyid");
				//get eforms assoc with the protocol
				String sql2 = "select * from eform where protocolid = ? order by name ASC";
				stmt2 = this.conn.prepareStatement(sql2);
				stmt2.setLong(1, protocolId);
				rs2 = stmt2.executeQuery();
				while (rs2.next()) {
					int eformId = rs2.getInt("eformid");
					String eformName = rs2.getString("name");
					//now get the number of admin form records for each eformid
					String sql3 = "select count(*) from administeredform where eformid = ?";
					stmt3 = this.conn.prepareStatement(sql3);
					stmt3.setLong(1, eformId);
					rs3 = stmt3.executeQuery();
					int count = 0;
					while (rs3.next()) {
						count = rs3.getInt("count");
					}

					ReportingForm form = new ReportingForm();
					form.setStudyName(rs.getString("studyName"));
					form.setStudyPI(rs.getString("pi"));
					form.setStudyType(rs.getString("st"));
					form.setStudyStatus(rs.getString("ss"));
					form.setStudySubjectCount(rs.getString("spc"));
					form.setStudyAdminFormsCount(rs.getString("safc"));
					form.setBricsStudyId(rs.getString("sbsi"));
					form.setStudyEformCount(rs.getString("sefc"));
					form.setStudyLockedAdminFormsCount(rs.getString("slafc"));
					form.setStudyNotLockedAdminFormsCount(rs.getString("snafc"));
					form.setProtocolId(rs.getInt("studyid"));
					form.setEformName(eformName);
					form.setNumAforms(count);
					list.add(form);
				}


				
			}
			
			
		} catch (SQLException e) {
			throw new CtdbException("Unable to get StudyList from DAO", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		
    	return list;
    }
    
    
    
    public List<ReportingForm> getDetailedStudyReportForAStudyExport(long protocolId) throws CtdbException
    {
    	PreparedStatement stmt = null;
    	PreparedStatement stmt2 = null;
    	PreparedStatement stmt3 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		List<ReportingForm> list = new ArrayList<ReportingForm>();
		try{
			StringBuffer sql = new StringBuffer(200);
			sql.append(" select sv.studyname studyName,sv.bricsstudyid sbsi, sv.pi pi,sv.studytype st,sv.studystatus ss,sv.patientcount spc,sv.adminformcount safc, sv.eformcount sefc, sv.lockedadminformcount slafc, sv.notlockedadminformcount snafc, sv.studyid studyid  from study_view sv");
			sql.append(" where sv.studyid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				//get eforms assoc with the protocol
				String sql2 = "select * from eform where protocolid = ? order by name ASC";
				stmt2 = this.conn.prepareStatement(sql2);
				stmt2.setLong(1, protocolId);
				rs2 = stmt2.executeQuery();
				while (rs2.next()) {
					int eformId = rs2.getInt("eformid");
					String eformName = rs2.getString("name");
					//now get the number of admin form records for each eformid
					String sql3 = "select count(*) from administeredform where eformid = ?";
					stmt3 = this.conn.prepareStatement(sql3);
					stmt3.setLong(1, eformId);
					rs3 = stmt3.executeQuery();
					int count = 0;
					while (rs3.next()) {
						count = rs3.getInt("count");
					}

					ReportingForm form = new ReportingForm();
					form.setStudyName(rs.getString("studyName"));
					form.setStudyPI(rs.getString("pi"));
					form.setStudyType(rs.getString("st"));
					form.setStudyStatus(rs.getString("ss"));
					form.setStudySubjectCount(rs.getString("spc"));
					form.setStudyAdminFormsCount(rs.getString("safc"));
					form.setBricsStudyId(rs.getString("sbsi"));
					form.setStudyEformCount(rs.getString("sefc"));
					form.setStudyLockedAdminFormsCount(rs.getString("slafc"));
					form.setStudyNotLockedAdminFormsCount(rs.getString("snafc"));
					form.setProtocolId(rs.getInt("studyid"));
					form.setEformName(eformName);
					form.setNumAforms(count);
					list.add(form);
				}


				
			}
			
			
		} catch (SQLException e) {
			throw new CtdbException("Unable to get StudyList from DAO", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		
    	return list;
    }
    
    /**
     * Method to get the study report from study view
     * @return list of studyforms for specific study
     * @throws CtdbException
     */
    public List<ReportingForm> getStudyReportForAStudy(long protocolId) throws CtdbException {
    	PreparedStatement stmt = null;
		ResultSet rs = null;
		List<ReportingForm> list = new ArrayList<ReportingForm>();
		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append(" select sv.studyname studyName, sv.bricsstudyid sbsi, sv.pi pi,sv.studytype st,sv.studystatus ss,sv.patientcount spc,sv.adminformcount safc, sv.eformcount sefc, sv.lockedadminformcount slafc, sv.notlockedadminformcount snafc, sv.studyid studyid   from study_view sv");
			sql.append(" where sv.studyid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				ReportingForm form = new ReportingForm();
				form.setStudyName(rs.getString("studyName"));
				form.setStudyPI(rs.getString("pi"));
				form.setStudyType(rs.getString("st"));
				form.setStudyStatus(rs.getString("ss"));
				form.setStudySubjectCount(rs.getString("spc"));
				form.setStudyAdminFormsCount(rs.getString("safc"));
				form.setBricsStudyId(rs.getString("sbsi"));
				form.setStudyEformCount(rs.getString("sefc"));
				form.setStudyLockedAdminFormsCount(rs.getString("slafc"));
				form.setStudyNotLockedAdminFormsCount(rs.getString("snafc")); 
				form.setProtocolId(rs.getInt("studyid"));
				list.add(form);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get StudyList from DAO.", e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
		
    	return list;
    }
    
    
    /**
     * Method to get the study report from study view
     * @return list of studyforms for specific study
     * @throws CtdbException
     */
    public List<ReportingForm> getDetailedStudyReportForAStudy(long protocolId) throws CtdbException
    {
    	PreparedStatement stmt = null;
    	PreparedStatement stmt2 = null;
    	PreparedStatement stmt3 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		List<ReportingForm> list = new ArrayList<ReportingForm>();
		ReportingForm form = null;
		try{
			StringBuffer sql = new StringBuffer(200);
			sql.append(" select sv.studyname studyName, sv.bricsstudyid sbsi, sv.pi pi,sv.studytype st,sv.studystatus ss,sv.patientcount spc,sv.adminformcount safc, sv.eformcount sefc, sv.lockedadminformcount slafc, sv.notlockedadminformcount snafc, sv.studyid studyid   from study_view sv");
			sql.append(" where sv.studyid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				form = new ReportingForm();
				form.setStudyName(rs.getString("studyName"));
				form.setStudyPI(rs.getString("pi"));
				form.setStudyType(rs.getString("st"));
				form.setStudyStatus(rs.getString("ss"));
				form.setStudySubjectCount(rs.getString("spc"));
				form.setStudyAdminFormsCount(rs.getString("safc"));
				form.setBricsStudyId(rs.getString("sbsi"));
				form.setStudyEformCount(rs.getString("sefc"));
				form.setStudyLockedAdminFormsCount(rs.getString("slafc"));
				form.setStudyNotLockedAdminFormsCount(rs.getString("snafc")); 
				form.setProtocolId(rs.getInt("studyid"));
				list.add(form);
			}
			//for each protocol, get eforms assoc with the protocol and get the number of admin form records for each eform
			
			//get eforms assoc with the protocol
			String sql2 = "select * from eform where protocolid = ? order by name ASC";
			stmt2 = this.conn.prepareStatement(sql2);
			stmt2.setLong(1, protocolId);
			rs2 = stmt2.executeQuery();
			String protocolEformsAndCount = "";
			while (rs2.next()) {
				int eformId = rs2.getInt("eformid");
				String eformName = rs2.getString("name");
				//now get the number of admin form records for each eformid
				String sql3 = "select count(*) from administeredform where eformid = ?";
				stmt3 = this.conn.prepareStatement(sql3);
				stmt3.setLong(1, eformId);
				rs3 = stmt3.executeQuery();
				int count = 0;
				while (rs3.next()) {
					count = rs3.getInt("count");
				}
				if(protocolEformsAndCount.equals("")) {
					protocolEformsAndCount = eformName + " (" + count + ")";
				}else {
					protocolEformsAndCount = protocolEformsAndCount + "<br>" + eformName + " (" + count + ")";
				}
			}
			form.setProtocolEformsAndAdminFormCount(protocolEformsAndCount);
			
		} catch (SQLException e) {
			throw new CtdbException("Unable to get  StudyList from DAO:" + e.getStackTrace(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return list;
	}

	/**
	 * MEthod to get the completed form report from the canned query created in the completed_forms_view
	 * 
	 * @return list of completed forms
	 * @throws CtdbException
	 */
	public List<ReportingForm> getCompletedFormsReport(long protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<ReportingForm> list = new ArrayList<ReportingForm>();
		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append(" select cfv.studyid,cfv.eformid fid,cfv.subjectguid guid," + getDecryptionFunc("mrn")
					+ " d_mrn,subjectid d_nrn,"
					+ " cfv.filledforms ff,cfv.visitdate vd,cfv.answerdquestioncount qa,cfv.intervalname iname from completed_forms_view cfv");
			sql.append(" where cfv.studyid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				ReportingForm form = new ReportingForm();
				form.setCfvSubjectGuid(rs.getString("guid"));
				form.setCfvFilledFormsName(rs.getString("ff"));
				form.setCfvVisitDate(DateFormatter.getFormattedDateWithTime(rs.getTimestamp("vd")));
				form.setAnsweredQuesCount(rs.getString("qa"));
				form.setCcvIntervalName(rs.getString("iname"));
				form.setCfvFormId(rs.getInt("fid"));
				form.setCfvSubjectMrn(rs.getString("d_mrn"));
				form.setCfvSubjectNrn(rs.getString("d_nrn"));
				list.add(form);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get  StudyList from DAO:" + e.getStackTrace(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return list;

	}

	/**
	 * Method to get the administrative form report from proForms
	 * 
	 * @return list of admin forms
	 * @throws CtdbException
	 */
	public List<ReportingForm> getAdministeredFormReport(long protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<ReportingForm> list = new ArrayList<ReportingForm>();
		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append(
					"  select afv.formname fName,afv.guid as guId,afv.formstatus fStatus,afv.singledobuleentry singleDoubleEntry,afv.updatedate uDate from admin_form_view afv");
			sql.append(" where afv.adminstudyid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				ReportingForm form = new ReportingForm();
				form.setAfName(rs.getString("fName"));
				form.setGuId(rs.getString("guId"));
				form.setAfStatus(rs.getString("fStatus"));
				form.setAsingleDoubleEntry(rs.getString("singleDoubleEntry"));
				form.setAuDate(DateFormatter.getFormattedDateWithTime(rs.getTimestamp("uDate")));
				list.add(form);
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get  AdminFormList from DAO:" + e.getLocalizedMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return list;
	}

	/**
	 * Retrieves the data for the "Performance Overview" report.
	 * 
	 * @param studyId - The ID of the study to query for
	 * @return A list of ReportingForm objects containing the results from the "performance_overview_view" view
	 * @throws SQLException If there are any errors from the database
	 */
	public List<ReportingForm> getPerformanceOverviewReport(long studyId) throws SQLException {
		PreparedStatement studyInfoStmt = null;
		PreparedStatement viewInfoStmt = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		List<ReportingForm> report = new ArrayList<ReportingForm>();
		ReportingForm form = null;

		try {
			String query =
					"select st.protocolnumber, s.name from protocol st inner join site s on st.protocolid = s.protocolid "
							+ "where s.primarysite = TRUE and st.protocolid = ? ";
			studyInfoStmt = conn.prepareStatement(query);
			studyInfoStmt.setLong(1, studyId);
			rs = studyInfoStmt.executeQuery();

			// Save the results
			if (rs.next()) {
				form = new ReportingForm();
				form.setPrimarySiteName(rs.getString("name"));
				form.setStudyNumber(rs.getString("protocolnumber"));

				query = "select * from performance_overview_view where protocolid = ? ";
				viewInfoStmt = conn.prepareStatement(query);
				viewInfoStmt.setLong(1, studyId);
				rs2 = viewInfoStmt.executeQuery();

				if (rs2.next()) {
					form.setInformedConsentEnrollmentCount(rs2.getInt("informed_consent_enrollment"));
					form.setCsfCollectionDataCount(rs2.getInt("csf_collection_data"));
					form.setProtocolDeviationsCount(rs2.getInt("protocol_deviations"));
					form.setAdverseEventsCount(rs2.getInt("adverse_events"));
					form.setEarlyTermination(rs2.getInt("early_termination"));
				}

				report.add(form);
			}
		} finally {
			close(rs);
			close(rs2);
			close(studyInfoStmt);
			close(viewInfoStmt);
		}

		return report;
	}

	/**
	 * Retrieves the data for the "Performance Overview" report.
	 * 
	 * @param studyId - The ID of the study to query for
	 * @return A list of ReportingForm objects containing the results from the "performance_overview_view" view
	 * @throws SQLException If there are any errors from the database
	 */
	public List<ReportingForm> getGuidsWithoutCollectionsReport(long studyId) throws SQLException {
		PreparedStatement studyInfoStmt = null;
		PreparedStatement displayTypeQueryPrepStmt = null;
		ResultSet rs = null;
		ResultSet rsType = null;
		List<ReportingForm> report = new ArrayList<ReportingForm>();
		ReportingForm form = null;

		try {

			// Get subject display type
			String displayTypeQuery = "select patientdisplaytype from protocol where protocolid = ?";
			displayTypeQueryPrepStmt = conn.prepareStatement(displayTypeQuery);
			displayTypeQueryPrepStmt.setLong(1, studyId);
			rsType = displayTypeQueryPrepStmt.executeQuery();
			int subjectDisplayType = 0;
			while (rsType.next()) {
				subjectDisplayType = rsType.getInt("patientdisplaytype");
			}


			String query = "select p.guid," + getDecryptionFunc("mrn")
					+ " d_mrn,pr.subjectid d_nrn, p.patientid from patient p, patientprotocol pr "
					+ " where  p.patientid = pr.patientid and pr.protocolid = ? "
					+ " and p.patientid not in (select af.patientid from administeredform af, eform ef where af.eformid = ef.eformid and ef.protocolid = ? ) "
					+ " and p.deleteflag=false";
			studyInfoStmt = conn.prepareStatement(query);
			studyInfoStmt.setLong(1, studyId);
			studyInfoStmt.setLong(2, studyId);
			rs = studyInfoStmt.executeQuery();
			String guid;
			String mrn;
			String nrn;
			int patientid;
			String webroot = SysPropUtil.getProperty("app.webroot");
			// Save the results
			while (rs.next()) {

				form = new ReportingForm();
				guid = rs.getString("guid");
				mrn = rs.getString("d_mrn");
				nrn = rs.getString("d_nrn");
				patientid = rs.getInt("patientid");
				if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_ID) {
					String url = "<a href=\"" + webroot + "/patient/patientHome.action?selectGuidWithoutCollection="
							+ patientid + "\">" + nrn + "</a>";
					form.setnRN(url);
				}
				if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_GUID) {
					String url = "<a href=\"" + webroot + "/patient/patientHome.action?selectGuidWithoutCollection="
							+ patientid + "\">" + guid + "</a>";
					form.setGuId(url);

				}
				if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_MRN) {
					String url = "<a href=\"" + webroot + "/patient/patientHome.action?selectGuidWithoutCollection="
							+ patientid + "\">" + mrn + "</a>";
					form.setmRN(url);

				}



				report.add(form);
			}
		} finally {
			close(rs);
			close(studyInfoStmt);
		}

		return report;
	}

	/**
	 * Retrieves the data for the "Completed Visits" report. If a protocol is selected, then only the results of that
	 * protocol are returned. If no protocol is selected, results from all applicable protocols are returned.
	 * 
	 * @param protocolId - The ID of the currently selected protocol or -1.
	 * @return A list of ReportingForm objects containing the results from the "completed_visits_view" view
	 * @throws SQLException If an error occurs during the query.
	 */
	public List<ReportingForm> getCompletedVisitsReport(long protocolId) throws SQLException {
		String baselineVTName = SysPropUtil.getProperty("reports.completedVisits.baseline.name");
		String sixMonthVTName = SysPropUtil.getProperty("reports.completedVisits.sixMonths.name");
		String twelveMonthVTName = SysPropUtil.getProperty("reports.completedVisits.twelveMonths.name");
		String eighteenMonthVTName = SysPropUtil.getProperty("reports.completedVisits.eighteenMonths.name");
		String twentyFourMonthVTName = SysPropUtil.getProperty("reports.completedVisits.twentyfourMonths.name");
		String thirtyMonthVTName = SysPropUtil.getProperty("reports.completedVisits.thirtyMonths.name");
		String thirtySixMonthVTName = SysPropUtil.getProperty("reports.completedVisits.thirtysixMonths.name");
		String fortyTwoMonthVTName = SysPropUtil.getProperty("reports.completedVisits.fortytwoMonths.name");
		String fortyEightMonthVTName = SysPropUtil.getProperty("reports.completedVisits.fortyeightMonths.name");
		String fiftyFourMonthVTName = SysPropUtil.getProperty("reports.completedVisits.fiftyfourMonths.name");
		String sixtyMonthVTName = SysPropUtil.getProperty("reports.completedVisits.sixtyMonths.name");
		PreparedStatement getStudyStmt = null;
		PreparedStatement getPatCountVtStmt = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		List<ReportingForm> report = new ArrayList<ReportingForm>();
		ArrayList<String> visitTypeNames = new ArrayList<String>();

		// Build the list of visit type names used to gather information
		visitTypeNames.add(baselineVTName);
		visitTypeNames.add(sixMonthVTName);
		visitTypeNames.add(twelveMonthVTName);
		visitTypeNames.add(eighteenMonthVTName);
		visitTypeNames.add(twentyFourMonthVTName);
		visitTypeNames.add(thirtyMonthVTName);
		visitTypeNames.add(thirtySixMonthVTName);
		visitTypeNames.add(fortyTwoMonthVTName);
		visitTypeNames.add(fortyEightMonthVTName);
		visitTypeNames.add(fiftyFourMonthVTName);
		visitTypeNames.add(sixtyMonthVTName);

		try {
			// Prepare a SQL statement to get the number of subjects, get the number of subjects in a protocol that are
			// associated
			// with collected data and site name by protocol.
			StringBuffer queryBuff = new StringBuffer();

			queryBuff.append("select * from protocol_patient_counts_view ");

			if (protocolId > 0) {
				queryBuff.append("where protocolid = ? ");
			}

			getStudyStmt = conn.prepareStatement(queryBuff.toString());

			// Prepare a SQL statement to get the patient count by visit types if the total number of required forms
			// equals the number
			// required forms with data collected by protocol.
			queryBuff = new StringBuffer(50);
			queryBuff.append("select * from mandatory_form_counts_view ")
					.append("where protocolid = ? and lower(name) in (");

			//// Add in place holders for all of the visit type names.
			for (Iterator<String> it = visitTypeNames.iterator(); it.hasNext();) {
				it.next();
				queryBuff.append("?");

				if (it.hasNext()) {
					queryBuff.append(", ");
				}
			}

			//// End the "in" clause.
			queryBuff.append(") ");

			getPatCountVtStmt = conn.prepareStatement(queryBuff.toString());

			// Set the query parameter for the get the protocol listing, if needed.
			if (protocolId > 0) {
				getStudyStmt.setLong(1, protocolId);
			}

			// Set the visit type names for the "in" clause of the get patient count query.
			for (int i = 0; i < visitTypeNames.size(); i++) {
				getPatCountVtStmt.setString(2 + i, visitTypeNames.get(i).toLowerCase());
			}

			rs = getStudyStmt.executeQuery();

			// Build the ReportingForm list consisting of records for the "Completed Visits" report
			while (rs.next()) {
				long rowProtocolId = rs.getLong("protocolid");
				ReportingForm form = new ReportingForm();
				form.setStudyNumber(rs.getString("protocolnumber"));
				form.setPrimarySiteName(rs.getString("name"));
				form.setTotalNumSubjects(rs.getInt("total_subjects"));
				form.setNumSubjectsDataCollect(rs.getInt("collected_subjects"));

				// Query for the patient count for the current result set protocol.
				getPatCountVtStmt.setLong(1, rowProtocolId);
				rs2 = getPatCountVtStmt.executeQuery();

				while (rs2.next()) {
					int patCount = rs2.getInt("patientCount");
					String vtName = rs2.getString("name").trim();

					// Set the visit type count
					if (vtName.equalsIgnoreCase(baselineVTName)) {
						form.setBaseline(patCount);
					} else if (vtName.equalsIgnoreCase(sixMonthVTName)) {
						form.setSixMonths(patCount);
					} else if (vtName.equalsIgnoreCase(twelveMonthVTName)) {
						form.setTwelveMonths(patCount);
					} else if (vtName.equalsIgnoreCase(eighteenMonthVTName)) {
						form.setEighteenMonths(patCount);
					} else if (vtName.equalsIgnoreCase(twentyFourMonthVTName)) {
						form.setTwentyFourMonths(patCount);
					} else if (vtName.equalsIgnoreCase(thirtyMonthVTName)) {
						form.setThirtyMonths(patCount);
					} else if (vtName.equalsIgnoreCase(thirtySixMonthVTName)) {
						form.setThirtySixMonths(patCount);
					} else if (vtName.equalsIgnoreCase(fortyTwoMonthVTName)) {
						form.setFortyTwoMonths(patCount);
					} else if (vtName.equalsIgnoreCase(fortyEightMonthVTName)) {
						form.setFortyEightMonths(patCount);
					} else if (vtName.equalsIgnoreCase(fiftyFourMonthVTName)) {
						form.setFiftyFourMonths(patCount);
					} else if (vtName.equalsIgnoreCase(sixtyMonthVTName)) {
						form.setSixtyMonths(patCount);
					}
				}

				// Add the report record to the list
				report.add(form);
				rs2.close();
			}
		} finally {
			close(rs);
			close(rs2);
			close(getStudyStmt);
			close(getPatCountVtStmt);
		}

		return report;
	}

	/**
	 * Retrieves the data for the "Submission Summary" report.
	 * 
	 * @param studyId - The ID of the current study
	 * @return A list of records (SubmissionSummaryReport objects) for the "Submission Summary" report
	 * @throws SQLException If an error occurs while getting the data from the database.
	 */
	public List<SubmissionSummaryReport> getSubmissionSummaryReport(long studyId) throws SQLException {
		List<SubmissionSummaryReport> reportList = new ArrayList<SubmissionSummaryReport>();
		SubmissionSummaryReport report = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		String query = "select *," + getDecryptionFunc("mrn")
				+ " d_mrn,nrn d_nrn from submission_summary_view where study_id = ? ";

		try {
			stmt = conn.prepareStatement(query);
			stmt.setLong(1, studyId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				report = new SubmissionSummaryReport();

				// Create a report record from the queried row
				report.setGuid(rs.getString("guid"));
				report.setFormName(rs.getString("form_name"));
				report.setVisitTypeName(rs.getString("visit_type_name"));
				report.setVisitDate(new Date(rs.getTimestamp("visit_date").getTime()));
				report.setSubmissionStatus(rs.getString("submission_status"));
				report.setMrn(rs.getString("d_mrn"));
				report.setNrn(rs.getString("d_nrn"));

				reportList.add(report);
			}
		} finally {
			close(rs);
			close(stmt);
		}

		return reportList;
	}

	public List<ScheduleReport> getScheduleReportByFilters(ScheduleReportFilter schReportFilter)
			throws SQLException, CtdbException {
		List<ScheduleReport> reportList = new ArrayList<ScheduleReport>();

		PreparedStatement stmt = null;
		ResultSet rs = null;

		Integer protocolId = schReportFilter.getProtocolId();
		Integer clinicalLocId = schReportFilter.getClinicalLocId();
		Integer patientId = schReportFilter.getPatientId();
		Date startDate = schReportFilter.getStartDate();
		Date endDate = schReportFilter.getEndDate();

		StringBuffer sql = new StringBuffer(200);
		String select_sql =
				"select pv.visitdate visitdate, proto.protocolid, proto.protocolnumber protocolnumber, proto.name protocolname, pp.subjectid, "
						+ " pat.patientid, pat.guid, " + getDecryptionFunc("pat.mrn") + " mrn, "
						+ getDecryptionFunc("pat.firstname") + " firstname, " + getDecryptionFunc("pat.middlename")
						+ " middlename, " + getDecryptionFunc("pat.lastname") + " lastname, "
						+ " v.intervalid, v.name intervalname, pv.comments, "
						+ " icp.clinicallocationid, icp.procedureid, icp.pointofcontactid "
						+ " from patientvisit pv inner join interval v on pv.intervalid = v.intervalid "
						+ " inner join patientprotocol pp on pp.patientid = pv.patientid and pp.protocolid = pv.protocolid "
						+ " inner join protocol proto on proto.protocolid = pv.protocolid and proto.protocolid = pp.protocolid "
						+ " inner join patient pat on pv.patientid = pat.patientid "
						+ " inner join interval_clinicalpoint icp on pv.intervalid = icp.intervalid and pv.intervalclinicalpointid = icp.id and v.intervalid = icp.intervalid ";
		String where_sql = " where ";
		String orderBy_sql = " order by pat.patientid ";
		Map<String, Integer> stmtParamMap = new HashMap<String, Integer>();

		if (startDate != null || endDate != null || protocolId != null || clinicalLocId != null || patientId != null) {
			StringBuffer cond_sql = new StringBuffer();
			String andStr = " and ";
			int counter = 1;

			if (startDate != null) {
				cond_sql.append(" pv.visitdate >= ? ");
				stmtParamMap.put("startDate", counter++);
			}

			if (endDate != null) {
				if (cond_sql.length() == 0) {
					cond_sql.append(" pv.visitdate <= ? ");
				} else {
					cond_sql.append(andStr).append(" pv.visitdate <= ? ");
				}

				stmtParamMap.put("endDate", counter++);
			}

			if (protocolId != null) {
				if (cond_sql.length() == 0) {
					cond_sql.append(" proto.protocolid = ? ");
				} else {
					cond_sql.append(andStr).append(" proto.protocolid = ? ");
				}

				stmtParamMap.put("protocolId", counter++);
			}

			if (clinicalLocId != null) {
				if (cond_sql.length() == 0) {
					cond_sql.append(" icp.clinicallocationid = ? ");
				} else {
					cond_sql.append(andStr).append(" icp.clinicallocationid = ? ");
				}

				stmtParamMap.put("clinicalLocId", counter++);
			}

			if (patientId != null) {
				if (cond_sql.length() == 0) {
					cond_sql.append(" pat.patientid = ? ");
				} else {
					cond_sql.append(andStr).append(" pat.patientid = ? ");
				}

				stmtParamMap.put("patientId", counter++);
			}

			sql.append(select_sql).append(where_sql).append(cond_sql).append(orderBy_sql);
		} else {
			sql.append(select_sql).append(orderBy_sql);
		}

		try {
			stmt = conn.prepareStatement(sql.toString());

			for (Entry<String, Integer> entry : stmtParamMap.entrySet()) {
				switch (entry.getKey()) {
					case "startDate":
						stmt.setTimestamp(entry.getValue(), new Timestamp(startDate.getTime()));
						break;
					case "endDate":
						stmt.setTimestamp(entry.getValue(), new Timestamp(endDate.getTime()));
						break;
					case "protocolId":
						stmt.setInt(entry.getValue(), protocolId);
						break;
					case "clinicalLocId":
						stmt.setInt(entry.getValue(), clinicalLocId);
						break;
					case "patientId":
						stmt.setInt(entry.getValue(), patientId);
						break;
					default:
						throw new CtdbException(
								"No matching value found for key, " + entry.getKey() + ", in the 'stmtParamMap' map.");
				}
			}

			ProtocolManagerDao protoManDao = ProtocolManagerDao.getInstance(conn);
			rs = stmt.executeQuery();
			while (rs.next()) {
				ScheduleReport report = new ScheduleReport();
				report.setVisitDate(rs.getTimestamp("visitDate"));
				report.setProtocolId(rs.getInt("protocolid"));
				report.setProtocolNumber(rs.getString("protocolnumber"));
				report.setPatientId(rs.getInt("patientid"));
				report.setPatientFirstName(rs.getString("firstname"));
				report.setPatientMidName(rs.getString("middlename"));
				report.setPatientLastName(rs.getString("lastname"));
				report.setPatientMRN(rs.getString("mrn"));
				report.setPatientGuid(rs.getString("guid"));
				report.setPatientSubjectId(rs.getString("subjectid"));
				report.setVisitTypeId(rs.getInt("intervalid"));
				report.setVisitTypeName(rs.getString("intervalname"));
				report.setComments(rs.getString("comments"));

				int clinialLocationId = rs.getInt("clinicallocationid");
				ClinicalLocation clinicalLocation = protoManDao.getClinicalLocationById(clinialLocationId);
				report.setClinicalLocation(clinicalLocation);
				int procedureId = rs.getInt("procedureid");
				Procedure proc = protoManDao.getProcedureById(procedureId);
				report.setProcedure(proc);
				int pocId = rs.getInt("pointofcontactid");
				PointOfContact poc = protoManDao.getPointOfContactById(pocId);
				report.setPointOfContact(poc);

				reportList.add(report);
			}
		} finally {
			close(rs);
			close(stmt);
		}

		return reportList;
	}

	/* Adverse Events - AE */
	public List<AdverseEvent> getAEListBySelectedStudy(HttpServletRequest request, long studyId) throws CtdbException {
		PreparedStatement stmt = null;
		PreparedStatement displayTypeQueryPrepStmt = null;
		PreparedStatement eformStmt = null;
		PreparedStatement answerStmt = null;
		PreparedStatement ansUpdStmt = null;
		ResultSet rs = null;
		ResultSet rsType = null;
		ResultSet rsEform = null;
		ResultSet rsAnswer = null;
		ResultSet rsAnsUpd = null;

		List<AdverseEvent> list = new ArrayList<AdverseEvent>();

		try {
			// Get subject display type
			String displayTypeQuery = "select patientdisplaytype from protocol where protocolid = ?";
			displayTypeQueryPrepStmt = conn.prepareStatement(displayTypeQuery);
			displayTypeQueryPrepStmt.setLong(1, studyId);
			rsType = displayTypeQueryPrepStmt.executeQuery();
			int subjectDisplayType = 0;
			while (rsType.next()) {
				subjectDisplayType = rsType.getInt("patientdisplaytype");
			}
			
			FormDataStructureUtility fsUtil = new FormDataStructureUtility();
			String eformSql = "select distinct ef.shortname from administeredform af "  
					+ " inner join eform ef on af.eformid = ef.eformid "  
					+ " inner join patient p on p.patientid = af.patientid "  
					+ " inner join patientprotocol pp on pp.patientid = p.patientid and ef.protocolid = pp.protocolid "  
					+ " where p.deleteflag=false and ef.protocolid = ?";
			eformStmt = conn.prepareStatement(eformSql);
			eformStmt.setLong(1, studyId);
			rsEform = eformStmt.executeQuery();
			List<Form> aeFormList = new ArrayList<Form>();
			while (rsEform.next()) {
				String efShortName = rsEform.getString("shortname");
				Form form = fsUtil.getEformFromBrics(request, efShortName);
				if(form.getPfCategory() != null && form.getPfCategory().equals(EformPfCategory.ADVERSE_EVENT_REPORT)) {
					aeFormList.add(form);
				}
			}

			for(Form aeForm : aeFormList) {
				String aeShortName = aeForm.getShortName();
				int dataentrydraftid = 0;
				int afid = 0;
				String visitdate = "";
				String ansUpddate = "";
				String collstatus = "";
	
				/* subjectId */
				String guid;
				String mrn;
				String subjectId;
				int patientid;
				
				String sql = "select af.administeredformid as afid ,af.visitdate, "
						+ " 		 p.guid ,p.mrn as mrn ,pr.subjectid as subjectid ,p.patientid, "
						+ " 		 ef.data_structure_name ,ef.shortname, "
						+ "          def.dataentrydraftid ,def.completeddate ,def.coll_status "
						+ " from  eform ef, administeredform af, patient p, patientprotocol pr, dataentrydraft def "
						+ " where ef.eformid=af.eformid and af.patientid=p.patientid "
						+ "		and af.administeredformid=def.administeredformid and ef.protocolid = pr.protocolid "
						+ "		and ef.protocolid=? and ef.shortname = ? "
						+ "		and p.patientid = pr.patientid and p.deleteflag=false"
						+ " order by af.administeredformid;";
	
				stmt = this.conn.prepareStatement(sql);
				stmt.setLong(1, studyId);
				stmt.setString(2, aeShortName);
				rs = stmt.executeQuery();
	
				boolean noStartDateSection;
				while (rs.next()) {
					noStartDateSection = true;
					AdverseEvent ae = new AdverseEvent();
	
					afid = rs.getInt("afid");
					visitdate = rs.getString("visitdate");
					dataentrydraftid = rs.getInt("dataentrydraftid");
					collstatus = rs.getString("coll_status");
	
					/* subject */
					guid = rs.getString("guid");
					mrn = rs.getString("mrn");
					subjectId = rs.getString("subjectid");
					patientid = rs.getInt("patientid");
	
					/* get eform answer updated date */
					/* TODO: should add patientresponsedraft.updateddate */
					String ansUpdQuery =
							"select * from responseedit " + "where administeredformid=? and editauditcomment is null "
									+ "order by updateddate desc LIMIT 1;";
					ansUpdStmt = conn.prepareStatement(ansUpdQuery);
					ansUpdStmt.setLong(1, afid);
					rsAnsUpd = ansUpdStmt.executeQuery();
					ansUpddate = "";
					if (rsAnsUpd.next()) {
						ansUpddate = rsAnsUpd.getString("updateddate");
					}
	
					/* get all allQuestionsInForm in the form */
					for (List<Section> sections : aeForm.getRowList()) {
						for (Section section : sections) {
							if (section != null) {
								int sectionId = section.getId();
								String sName = section.getName();
								ae = new AdverseEvent();
								List<Question> secQuestionList = section.getQuestionList();
								int sdateqid = 0;
								int edateqid = 0;
								if (secQuestionList != null) {
									for (Question q : secQuestionList) {
										if (q != null) {
											FormQuestionAttributes fqAttr = q.getFormQuestionAttributes();
											String deName = fqAttr.getDataElementName();
											if (deName.toUpperCase()
													.contains(CtdbConstants.AE_START_DATE_DATA_ELEMENT.toUpperCase())) {
												sdateqid = q.getId();

											} else if (deName.toUpperCase()
													.contains(CtdbConstants.AE_END_DATE_DATA_ELEMENT.toUpperCase())) {
												edateqid = q.getId();
											}
										}
									}
								}

								/* query by startDate qid or endDate qid */
								String sql3 = "";
								if (CtdbConstants.DATACOLLECTION_STATUS_LOCKED.equalsIgnoreCase(collstatus)) {
									sql3 = " select * from patientresponse pr, response r "
											+ " where pr.responseid=r.responseid  and r.administeredformid=? "
											+ " 	  and r.dict_sectionid=? and r.dict_questionid=? ; ";
								} else {
									sql3 = " select * from patientresponsedraft prd, responsedraft rd "
											+ " where prd.responsedraftid=rd.responsedraftid "
											+ " 	  and rd.dataentrydraftid=?  and rd.dict_sectionid=? "
											+ " 	  and rd.dict_questionid=? ; ";
								}

								boolean hasStartDate = false;
								if (0 != sdateqid) {
									answerStmt = this.conn.prepareStatement(sql3);
									if (CtdbConstants.DATACOLLECTION_STATUS_LOCKED.equalsIgnoreCase(collstatus)) {
										answerStmt.setLong(1, afid);
									} else {
										answerStmt.setLong(1, dataentrydraftid);
									}
									answerStmt.setLong(2, sectionId);
									answerStmt.setLong(3, sdateqid);
									rsAnswer = answerStmt.executeQuery();
									if (rsAnswer.next()) {
										ae.setAeStartDate(rsAnswer.getString("answer"));
										hasStartDate = true;
										noStartDateSection = false;
									} else {
										ae.setAeStartDate(CtdbConstants.QUESTION_NO_ANSWER);
									}
									sdateqid = 0;
								} else {
									ae.setAeStartDate(null);
								}
								if (0 != edateqid && hasStartDate) {
									answerStmt = this.conn.prepareStatement(sql3);
									if (CtdbConstants.DATACOLLECTION_STATUS_LOCKED.equalsIgnoreCase(collstatus)) {
										answerStmt.setLong(1, afid);
									} else {
										answerStmt.setLong(1, dataentrydraftid);
									}
									answerStmt.setLong(2, sectionId);
									answerStmt.setLong(3, edateqid);
									rsAnswer = answerStmt.executeQuery();
									if (rsAnswer.next()) {
										ae.setAeEndDate(rsAnswer.getString("answer"));
									} else {
										ae.setAeEndDate(CtdbConstants.QUESTION_NO_ANSWER);
									}
									edateqid = 0;
								} else {
									ae.setAeEndDate(null);
								}

								/* VE records with startDate in repeatable section. */
								if (hasStartDate) {
									ae.setAdministeredformId(afid);
									ae.setAnsUpddate(ansUpddate);

									/* subject */
									if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_ID) {
										ae.setnRN(subjectId);
										ae.setSubject(subjectId);
									} else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_GUID) {
										ae.setGuId(guid);
										ae.setSubject(guid);
									} else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_MRN) {
										ae.setmRN(mrn);
										ae.setSubject(mrn);
									}

									list.add(ae);
								}
							}
						}
					}

	
					/* VE records with visitDate in required section, but without any startDate in repeatable section. */
					if (noStartDateSection) {
						ae.setAdministeredformId(afid);
						ae.setAnsUpddate(ansUpddate);
	
						/* subject */
						if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_ID) {
							ae.setnRN(subjectId);
							ae.setSubject(subjectId);
						} else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_GUID) {
							ae.setGuId(guid);
							ae.setSubject(guid);
						} else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_MRN) {
							ae.setmRN(mrn);
							ae.setSubject(mrn);
						}
	
						list.add(ae);
					}
				}
			} //end for loop
		} catch (SQLException sqle) {
			throw new CtdbException("Unable to get \"Adverse Event Log\".", sqle);
		} catch (Exception e) {
			// getting 401: unauthorized
			// e.printStackTrace();
			throw new CtdbException("Unable to get the response edit archive list for VQ: " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
			this.close(rsType);
			this.close(displayTypeQueryPrepStmt);
			this.close(rsAnswer);
			this.close(answerStmt);
			this.close(rsAnsUpd);
			this.close(ansUpdStmt);
			this.close(eformStmt);
			this.close(rsEform);
		}

		return list;
	}

	public List<ViewAuditorComment> getVQListBySelectedStudy(long studyId) throws CtdbException {
		PreparedStatement stmt = null;
		PreparedStatement displayTypeQueryPrepStmt = null;
		ResultSet rs = null;
		ResultSet rsType = null;

		List<ViewAuditorComment> list = new ArrayList<ViewAuditorComment>();

		try {
			// Get subject display type
			String displayTypeQuery = "select patientdisplaytype from protocol where protocolid = ?";
			displayTypeQueryPrepStmt = conn.prepareStatement(displayTypeQuery);
			displayTypeQueryPrepStmt.setLong(1, studyId);
			rsType = displayTypeQueryPrepStmt.executeQuery();
			int subjectDisplayType = 0;
			while (rsType.next()) {
				subjectDisplayType = rsType.getInt("patientdisplaytype");
			}


			String sql = "select af.administeredformid as afid, ef.name, "
					+ "		p.guid, p.mrn, pr.subjectid, p.patientid, "
					+ " 	count(re.editauditcomment) as comment_count, af.is_cat "
					+ " from eform ef, administeredform af, responseedit re, "
					+ "	   patient p ,patientprotocol pr where ef.eformid=af.eformid "
					+ "		and af.administeredformid=re.administeredformid and ef.protocolid=? "
					+ "		and p.patientid=af.patientid and p.patientid = pr.patientid "
					+ "		and p.deleteflag=false and pr.protocolid=? "
					+ " group by af.administeredformid, ef.name, "
					+ "		   p.guid, p.mrn, pr.subjectid, p.patientid "
					+ " having count(re.editauditcomment) > 0 ; ";


			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, studyId);
			stmt.setLong(2, studyId);
			rs = stmt.executeQuery();

			/* subjectId */
			String guid;
			String mrn;
			String subjectId;
			int patientid;

			while (rs.next()) {
				ViewAuditorComment vac = new ViewAuditorComment();
				vac.setAdministeredformId(rs.getInt("afid"));
				vac.setEformName(rs.getString("name"));
				vac.setCount(rs.getString("comment_count"));
				boolean isCat = rs.getBoolean("is_cat");
				vac.setIsCat(isCat);

				/* subject */
				guid = rs.getString("guid");
				mrn = rs.getString("mrn");
				subjectId = rs.getString("subjectid");
				patientid = rs.getInt("patientid");
				if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_ID) {
					vac.setSubjectId(subjectId);
					vac.setSubject(subjectId);
				}
				if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_GUID) {
					vac.setGuId(guid);
					vac.setSubject(guid);
				}
				if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_MRN) {
					vac.setMrn(mrn);
					vac.setSubject(mrn);
				}

				list.add(vac);
			}
		} catch (SQLException sqle) {
			throw new CtdbException("Unable to get \"View Audior Comment List\" from DAO: " + sqle.getLocalizedMessage(), sqle);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return list;
	}
	
	public HashMap<String, Form> getAuditorCommentEformMap(HttpServletRequest request, int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

        FormDataStructureUtility fsUtil = new FormDataStructureUtility();
        HashMap<String, Form> formMap = new HashMap<String, Form>();
        List<Form> formList = new ArrayList<Form>();
        JSONArray formNameJsonArr = new JSONArray();
        
        String sql = "	select distinct re.administeredformid as afid, ef.name , ef.shortname " + 
        			 "	from  eform ef, administeredform af, responseedit re ,patient p ,patientprotocol pr " + 
        			 "	where ef.eformid=af.eformid and af.administeredformid=re.administeredformid " + 
        			 "		  and ef.protocolid=? and re.editauditcomment is not null " + 
        			 "		  and p.patientid=af.patientid and p.patientid = pr.patientid " + 
        			 "		  and p.deleteflag=false and pr.protocolid=?";
        try {
	        stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			stmt.setLong(2, protocolId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				formNameJsonArr.put(rs.getString("shortname"));
			}
        } catch (SQLException e) {
			throw new CtdbException(
					"Unable to get the auditor comments form list for View Auditor Comments: "
							+ e.getMessage(), e);
		} catch(Exception e) {
			throw new CtdbException(
					"Unable to get the auditor comments form list for View Auditor Comments: "
							+ e.getMessage(), e);
        } finally {
			this.close(rs);
			this.close(stmt);
		}
        
        try {
			formList = fsUtil.getEformsForShortnamesFromBrics(request, formNameJsonArr.toString());
		} catch (WebApplicationException we) {
			throw new CtdbException("An error has occurred in retrieving the data from Dictionary when getting eforms by shortname list."
					+ we.getMessage(), we);	
		} catch (CasProxyTicketException cae) {
			throw new CtdbException("An error has occurred in retrieving the data from Dictionary when getting eforms by shortname list."
					+ cae.getMessage(), cae);
		} 
        
        for(Form form : formList) {
        	String shortName = form.getShortName();
        	formMap.put(shortName, form);
        }
        return formMap;
	}
	
	/* Auditor Comments on View Auditor Comments Details Table */
	public List<ViewAuditorComment> getAuditorCommentList(HttpServletRequest request, int protocolId) throws CtdbException {
																
		PreparedStatement stmt = null;
		PreparedStatement displayTypeQueryPrepStmt = null;
		ResultSet rs = null;
		ResultSet rsType = null;
        
        HashMap<String, Form> formMap = new HashMap<String, Form>();
        formMap = this.getAuditorCommentEformMap(request, protocolId);
        ArrayList<ViewAuditorComment> auditCommetList = new ArrayList<ViewAuditorComment>();

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
        	
			String sql = " select distinct resp.dict_sectionid, resp.dict_questionid, resp.updateddate, resp.auditstatus, resp.administeredformid, "  
			+ "		resp.responseeditversion, rv.afid, rv.afname, rv.afshortname, rv.guid, "
			+ "		rv.mrn, rv.subjectid, rv.patientid " 
			+ " from responseedit resp, " 
			+ "	(select max(re.responseeditversion) responseeditversion, "
			+ "			re.administeredformid as afid, ef.name as afname, "
			+ "			ef.shortname as afshortname, "
			+ "        	re.dict_sectionid, re.dict_questionid, "
			+ "			p.guid, p.mrn, pr.subjectid, p.patientid "
			+ "	from  eform ef, administeredform af, responseedit re, patient p, patientprotocol pr "
			+ "	where ef.eformid=af.eformid and af.administeredformid=re.administeredformid "
			+ "		  and ef.protocolid=? and re.editauditcomment is not null "
			+ "		  and p.patientid=af.patientid and p.patientid = pr.patientid "
			+ "		  and p.deleteflag=false and pr.protocolid=? "
			+ "	group by re.administeredformid, ef.name, ef.shortname, "
			+ "			 re.dict_sectionid, re.dict_questionid, "
			+ "		     p.guid, p.mrn, pr.subjectid, p.patientid ) as rv "			 
			+ " where resp.responseeditversion=rv.responseeditversion and resp.administeredformid=rv.afid "
			+ " 	   and resp.dict_sectionid=rv.dict_sectionid and resp.dict_questionid=rv.dict_questionid " 
			+ " order by rv.afid, rv.afname, rv.afshortname, resp.dict_sectionid, resp.dict_questionid, "
			+ "		     rv.guid, rv.mrn, rv.subjectid, rv.patientid; "; 

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			stmt.setLong(2, protocolId);
			rs = stmt.executeQuery();
		
			
			/*subject*/
        	String guid;
        	String mrn;
        	String subjectId;

        	while(rs.next()) {
        		String shortName = rs.getString("afshortname");
		        Form form = null;
		        HashMap<String,Question> questionMap = null;
		        String formName = "";
		        ViewAuditorComment vac = new ViewAuditorComment();
		        String text = "";
		        boolean isCat = false;
		        
				int reQuestionId = rs.getInt("dict_questionid");
				int reSectionId = rs.getInt("dict_sectionid");
				
		        if(shortName!=null && !shortName.isEmpty()) {
		        	form = formMap.get(shortName);
		        
		        	if(form != null) {
			        	formName = form.getName();
			        	isCat = form.isCAT();
			        	
			        	questionMap = form.getQuestionMap();						
						if(questionMap != null) {
							String questionMapKey = "S_" + reSectionId + "_Q_" + reQuestionId;
							Question question = questionMap.get(questionMapKey);			
							text = question.getText();							
						}												
		        	}
		        }	
		        
				vac.setEditDate(rs.getTimestamp("updateddate"));
				vac.setEformName(formName);
				vac.setQuestionId(reQuestionId);
				vac.setSectionId(reSectionId);
				vac.setIsCat(isCat);
				vac.setQuestionText(text);
				vac.setAuditStatus(rs.getString("auditstatus"));
				vac.setAdministeredformId(rs.getInt("administeredformid"));
				
				/* subject */
        		guid = rs.getString("guid");
        		mrn = rs.getString("mrn");
        		subjectId = rs.getString("subjectid");
        		String subject = "";
        		if(subjectDisplayType==CtdbConstants.PATIENT_DISPLAY_ID){
        			vac.setSubjectId(subjectId);
        			subject = subjectId;
        		}
				if(subjectDisplayType==CtdbConstants.PATIENT_DISPLAY_GUID){						
					vac.setGuId(guid);
					subject = guid;
			    }
				if(subjectDisplayType==CtdbConstants.PATIENT_DISPLAY_MRN){
					vac.setMrn(mrn);
					subject = mrn;
				}
				vac.setSubject(subject);
				auditCommetList.add(vac);		        
			}
			
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get the auditor comments list for View Auditor Comments: "
							+ e.getMessage(), e);
		} catch(Exception e) {
			throw new CtdbException(
					"Unable to get the auditor comments list for View Auditor Comments: "
							+ e.getMessage(), e);
        } finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return auditCommetList;
	}
}
