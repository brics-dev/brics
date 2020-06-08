package gov.nih.nichd.ctdb.patient.dao;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.postgresql.util.PSQLException;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.Version;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.patient.common.PatientResultControl;
import gov.nih.nichd.ctdb.patient.common.PatientVisitResultControl;
import gov.nih.nichd.ctdb.patient.domain.AuditDetail;
import gov.nih.nichd.ctdb.patient.domain.NextOfKin;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.domain.PatientExtraInfo;
import gov.nih.nichd.ctdb.patient.domain.PatientProtocol;
import gov.nih.nichd.ctdb.patient.domain.PatientVisit;
import gov.nih.nichd.ctdb.patient.domain.PatientVisitPrepopValue;
import gov.nih.nichd.ctdb.patient.domain.Phone;
import gov.nih.nichd.ctdb.patient.domain.PhoneType;
import gov.nih.nichd.ctdb.patient.util.PatientChangeTracker;
import gov.nih.nichd.ctdb.protocol.dao.ProtocolManagerDao;
import gov.nih.nichd.ctdb.protocol.domain.IntervalScheduleDisplay;
import gov.nih.nichd.ctdb.protocol.domain.PrepopDataElement;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.domain.ProtocolRandomization;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.nichd.ctdb.util.domain.Address;

/**
 * PatientManagerDao interacts with the Data Layer for the PatientManager.
 * The only job of the DAO is to manipulate the data layer.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class PatientManagerDao extends CtdbDao {
	
	private static final SimpleDateFormat enrollmentDateSDF = new SimpleDateFormat("yyyy-MM-dd");
	private static Logger logger = Logger.getLogger(PatientManagerDao.class);
	/**
     * Private Constructor to hide the instance
     * creation implementation of the PatientManagerDao object
     * in memory. This will provide a flexible architecture
     * to use a different pattern in the future without
     * refactoring the PatientManager.
     */
	private PatientManagerDao() {
		
	}
	
    private static final String selectStarFromPatient = "select distinct patient.patientid,patientprotocol.subjectid,patient.version, " + 
        getDecryptionFunc("patient.firstname") + " as firstname, " +
		getDecryptionFunc("patient.middlename") + " as middlename, " + getDecryptionFunc("patient.lastname") + " as lastname, " +
		getDecryptionFunc("patient.homephone") + " as homephone, " + getDecryptionFunc("patient.workphone") + " as workphone, " +
		getDecryptionFunc("patient.mobilephone") + " as mobilephone, " + "patient.homeaddressid, " +
		getDecryptionFunc("patient.email") + " as email, " +
		getDecryptionFunc("patient.dob") + " as dob, patient.createdby, patient.createddate, patient.updatedby, patient.updateddate, " +
		getDecryptionFunc("patient.sex") + " as sex, " + getDecryptionFunc("patient.birthcity") + " as birthcity, " +
		getDecryptionFunc("patient.xbirthcountryid") + " as xbirthcountryid, patient.guid, patient.deleteflag, " +
		getDecryptionFunc("hasmiddlename") + " as hasmiddlename, " + getDecryptionFunc("patient.mrn") + " as mrn ";
	
	private static final String selectStarFromPatientView = selectStarFromPatient.replaceAll("patient\\.", "patient_view.");
	
    /**
     * Method to retrieve the instance of the PatientManagerDao.
     *
     * @return PatientManagerDao data object
     */
    public static synchronized PatientManagerDao getInstance() {
        return new PatientManagerDao();
    }

    /**
     * Method to retrieve the instance of the PatientManagerDao. This method
     * accepts a Database Connection to be used internally by the DAO. All
     * transaction management will be handled at the BusinessManager level.
     *
     * @param conn Database connection to be used within this data object
     * @return PatientManagerDao data object
     */
    public static synchronized PatientManagerDao getInstance(Connection conn) {
        PatientManagerDao dao = new PatientManagerDao();
        dao.setConnection(conn);
        return dao;
    }
    


    /**
     * Creates a Patient in the CTDB System. All patient data that is stored into the database will be encrypted.
     *
     * @param patient The patient to create
     * @throws DuplicateObjectException thrown if the patient already exists in the system
     *                                  based on the unique constraints
     * @throws CtdbException            thrown if any other errors occur while processing
     */
    public void createPatient(Patient patient,int protocolId) throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;
        String query = "";

        try {
//           if (isSubjectIdExists(patient)) {
//                throw new DuplicateObjectException("Subject with the Subject ID " + patient.getSubjectId() + " already exists in the system.");
//           }
//            
//            if (doesSubjectIdExistsInCurrentProtocol(patient,protocolId)) {
//                throw new DuplicateObjectException("Subject with the Subject ID " + patient.getSubjectId() + " already exists in the system.");
//            }
//            
			if (this.isMrnExists(patient, protocolId)) {
                throw new DuplicateObjectException("Subject with the subject MRN " + patient.getMrn() + " already exists in the system.");
            }


			if (this.isGuidExistsProtocol(patient, protocolId)) {
				throw new DuplicateObjectException(
						"Subject with the subject GUID " + patient.getGuid() + " already exists in the system.");
			}

            
            query = "insert into patient (patientid, version, firstname, middlename, lastname, homephone, " +
            		"workphone, mobilephone, homeaddressid, nextofkin, email, sex, dob, createdby, createddate, updatedby, " +
            		"updateddate, birthcity, xbirthcountryid, guid, deleteflag, hasmiddlename, mrn) values " + 
            		" (DEFAULT,?," 
            		 + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " + 
            		CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", ?, ?, " +
            		CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", ?, " +
            		"CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " +
            		"?, FALSE, " + CtdbDao.STANDARD_ENCRYPTION_FUNCT +  ", " + CtdbDao.STANDARD_ENCRYPTION_FUNCT+ ") ";

            stmt = this.conn.prepareStatement(query);
     
            stmt.setInt(1, 1);
            stmt.setString(2, patient.getFirstName());
            stmt.setString(3, patient.getMiddleName());
            stmt.setString(4, patient.getLastName());

            Phone home = (Phone) patient.getPhoneNumbers().get(PhoneType.HOME);
            if (home != null) {
                stmt.setString(5, home.getNumber());
            } else {
                stmt.setString(5, "");
            }

            Phone work = (Phone) patient.getPhoneNumbers().get(PhoneType.WORK);
            if (work != null) {
                stmt.setString(6, work.getNumber());
            } else {
                stmt.setString(6, "");
            }

            Phone mobile = (Phone) patient.getPhoneNumbers().get(PhoneType.MOBILE);
            if (mobile != null) {
                stmt.setString(7, mobile.getNumber());
            } else {
                stmt.setString(7, "");
            }

            stmt.setLong(8, patient.getHomeAddress().getId());
            // leave the next of kin field & populate for legacy compatibility
            stmt.setString(9, "");
            
            String email = patient.getEmail();
            
            if ( (email != null) && (email.length() > 0) ) {
                stmt.setString(10, patient.getEmail());
            } else {
                stmt.setString(10, "");
            }
            
            stmt.setString(11, patient.getExtraInfo().getSex());
            stmt.setString(12, patient.getDateOfBirth());
            stmt.setLong(13, patient.getCreatedBy());
            stmt.setLong(14, patient.getUpdatedBy());
            stmt.setString(15, patient.getExtraInfo().getBirthCity());
            stmt.setString(16, Long.toString(patient.getExtraInfo().getBirthCountry().getId()));
            stmt.setString(17, patient.getGuid());
            stmt.setString(18, Boolean.toString(patient.isHasMiddleName()));
            stmt.setString(19, patient.getMrn());

            stmt.executeUpdate();
            patient.setId(getInsertId(conn, "patient_seq"));
        }
        catch (SQLException e) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column encryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to create new subject: " + e.getMessage(), e);
			}
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Checks to see if the NIH record number already exists in the system.
     *
     * @param patient The patient has the NIH record number to check for
     * @return boolean true the NIH record number is already in the system; false not
     * @throws CtdbException thrown if any errors occur during processing
     */
    public boolean subjectIDexistInProtocol(Patient patient,int protocolId) throws CtdbException {
    	
    	
       
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean subjectIdExistInCurrentProtocol = false;
        String subjectId  = patient.getSubjectId();
        
        // Proceed only if the subject ID is valid
        if( (subjectId == null) || (subjectId.length() == 0) ) {
        	return false;
        }
        
        try {
            String sql = "select count(subjectid) from patientprotocol " + 
            			 "where subjectid = ? and protocolid = ? ";
            
      
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setString(1, subjectId);
            stmt.setInt(2, protocolId);
 
           

            rs = stmt.executeQuery();
            int count = 0;
            
            if ( rs.next() ) {
                count = rs.getInt(1);
            }
            
            if ( count > 0 ) {
            	subjectIdExistInCurrentProtocol = true;
            }
            
            return subjectIdExistInCurrentProtocol;
        }
        catch (SQLException e) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to check if the subject ID exists in the system: " + e.getMessage(), e);
			}
        }
        finally {
        	this.close(rs);
            this.close(stmt);
        }
    	
    	
    }

    /**
     * Checks to see if the GUID already exists in the system.
     *
     */
    public boolean isGuidExists(Patient patient) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean isGuidDuplicated = false;
        String guid = patient.getGuid();
        // Proceed only if the subject ID is valid
        if( (guid == null) || (guid.length() == 0) ) {
        	return false;
        }

        try {
            String sql = "select count(guid) from patient " +
            			 "where patient.deleteflag = false and upper(guid) = upper(?) ";
            
            // If updating patient, need to exclude the patient himself
            if ( patient.getId() != Integer.MIN_VALUE ) {
                sql += "and patientid != ? ";
            }
            stmt = this.conn.prepareStatement(sql);
            stmt.setString(1, guid);
            
            if ( patient.getId() != Integer.MIN_VALUE ) {
            	stmt.setLong(2, patient.getId());
            }

            rs = stmt.executeQuery();
            int count = 0;
            
            if  (rs.next() ) {
                count = rs.getInt(1);
            }
            
            if ( count > 0 ) {
                isGuidDuplicated = true;
            }
            
            return isGuidDuplicated;
        }
        catch ( SQLException e ) {
            throw new CtdbException("Unable to check if the subject GUID exists in the system: " + e.getMessage(), e);
        }
        finally {
        	this.close(rs);
            this.close(stmt);
        }
    }
    
    /**
     * Checks to see if the GUID already exists in the system.
     *
     */
    public boolean isGuidExistsProtocol(Patient patient, int protocolId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean isGuidDuplicated = false;
        String guid = patient.getGuid();
        // Proceed only if the subject ID is valid
        if( (guid == null) || (guid.length() == 0) ) {
        	return false;
        }

        try {
            String sql = "select count(p.guid) from patient p, protocol pt, patientprotocol pp " +
					"where pp.patientid = p.patientid and pp.protocolid = pt.protocolid and "
					+ "p.deleteflag = false and upper(p.guid) = upper(?) and pp.protocolid = ? ";
            
            // If updating patient, need to exclude the patient himself
            if ( patient.getId() != Integer.MIN_VALUE ) {
                sql += "and pp.patientid != ? ";
            }
            stmt = this.conn.prepareStatement(sql);
            stmt.setString(1, guid);
            stmt.setLong(2, protocolId);
            if ( patient.getId() != Integer.MIN_VALUE ) {
            	stmt.setLong(3, patient.getId());
            }

            rs = stmt.executeQuery();
            int count = 0;
            
            if  (rs.next() ) {
                count = rs.getInt(1);
            }
            
            if ( count > 0 ) {
                isGuidDuplicated = true;
            }
            
            return isGuidDuplicated;
        }
        catch ( SQLException e ) {
            throw new CtdbException("Unable to check if the subject GUID exists in the system: " + e.getMessage(), e);
        }
        finally {
        	this.close(rs);
            this.close(stmt);
        }
    }

	/**
	 * Checks to see if the GUID already exists in the system.
	 *
	 */
	private boolean isMrnExists(Patient patient, int protocolId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean isDuplicated = false;
        String value  = patient.getMrn();
        
        // Proceed only if the subject ID is valid
        if( ( value == null) || (value.length() == 0) ) {
        	return false;
        }
        
        try {
			String sql = "select count(p.mrn) from patient p, protocol pt, patientprotocol pp "
					+ "where pp.patientid = p.patientid and pp.protocolid = pt.protocolid and "
					+ "p.deleteflag = false and upper(" + getDecryptionFunc("p.mrn")
					+ ") = upper(?) and pp.protocolid = ? ";

            //if updating patient, need to exclude the patient himself
            if ( patient.getId() != Integer.MIN_VALUE ) {
                sql += "and patientid != ? ";
            }
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setString(1, patient.getMrn());
			stmt.setLong(2, protocolId);
            
            if ( patient.getId() != Integer.MIN_VALUE ) {
				stmt.setLong(3, patient.getId());
            }

            rs = stmt.executeQuery();
            int count = 0;
            
            if ( rs.next() ) {
                count = rs.getInt(1);
            }
            
            if ( count > 0 ) {
                isDuplicated = true;
            }
            
            return isDuplicated;
        }
        catch (SQLException e) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed for subject MRN: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to check if the subject MRN exists in the system: " + e.getMessage(), e);
			}
        }
        finally {
        	this.close(rs);
            this.close(stmt);
        }
    }
    
    /**
     * Checks to see if the NIH record number already exists in the system.
     *
     * @param patient The patient has the NIH record number to check for
     * @return boolean true the NIH record number is already in the system; false not
     * @throws CtdbException thrown if any errors occur during processing
     */
    public boolean doesSubjectIdExistsInCurrentProtocol(Patient patient,int protocolId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean subjectIdExistInCurrentProtocol = false;
        String subjectId  = patient.getSubjectId();
        
        // Proceed only if the subject ID is valid
        if( (subjectId == null) || (subjectId.length() == 0) ) {
        	return false;
        }
        
        try {
            String sql = "select count(subjectid) from patientprotocol " + 
            			 "where upper(subjectid) = ? and protocolid = ? ";
            
       // if updating patient, need to exclude the patient himself
            if ( patient.getId() != Integer.MIN_VALUE ) {
                sql += "and patientid != ? ";
            }
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setString(1, subjectId.toUpperCase());
            stmt.setInt(2, protocolId);
            
            if ( patient.getId() != Integer.MIN_VALUE ) {
           	stmt.setLong(3, patient.getId());
           }
//            
           

            rs = stmt.executeQuery();
            int count = 0;
            
            if ( rs.next() ) {
                count = rs.getInt(1);
            }
            
            if ( count > 0 ) {
            	subjectIdExistInCurrentProtocol = true;
            }
            
            return subjectIdExistInCurrentProtocol;
        }
        catch (SQLException e) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to check if the subject ID exists in the system: " + e.getMessage(), e);
			}
        }
        finally {
        	this.close(rs);
            this.close(stmt);
        }
    }
    
    /**
     * Updates a Patient in the system. All patient data that is stored in the database will be encrypted.
     *
     * @param patient          The patient to update
     * @param isVersionPatient Boolean flag to determine if patient is a new version or not. Used to calculate
     *                         the version attribute in the patient object.
     * @throws ObjectNotFoundException  thrown if the patient does not exist in the system
     * @throws DuplicateObjectException thrown if the patient already exists in the system
     *                                  based on the unique constraints
     * @throws CtdbException            thrown if any other errors occur while processing
     */
    public void updatePatient(Patient patient, boolean isVersionPatient,int protocolId) throws ObjectNotFoundException, DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String query = "";
        
        try {
        	// If the NIH record number is a duplicate throw an exception
            
            if (doesSubjectIdExistsInCurrentProtocol(patient,protocolId)) {
                throw new DuplicateObjectException("Subject with the Subject ID " + patient.getSubjectId() + " already exists in the system.");
            }
//
//            if ( this.isMrnExists(patient) ) {
//                throw new DuplicateObjectException("Subject with the subject MRN " + patient.getMrn() + " already exists in the system.");
//            }
//
//            if ( this.isGuidExists(patient) ) {
//                throw new DuplicateObjectException("Subject with the subject GUID " + patient.getGuid() + " already exists in the system.");
//            }
            
            query = "update patient set nihrecordnumber = " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", ";
            
            if ( isVersionPatient ) {
            	query += "version = version + 1, ";
            }
            
            query += "firstname = " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", middlename = " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " +
            		 "lastname = " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", homephone = " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " +
            		 "workphone = " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", mobilephone = " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " +
            		 "homeaddressid = ?, nextofkin = ?, email = " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", sex = " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " +
            		 "dob = " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", updatedby = ?, updateddate = CURRENT_TIMESTAMP, birthcity = " + 
            		 CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", xbirthcountryid = " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", guid = ?, " +
            		 "hasmiddlename = " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", mrn = " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + " where patientid = ? ";

            stmt = this.conn.prepareStatement(query);
            
            stmt.setString(1, patient.getSubjectId());
            stmt.setString(2, patient.getFirstName());
            stmt.setString(3, patient.getMiddleName());
            stmt.setString(4, patient.getLastName());
            Phone home = (Phone) patient.getPhoneNumbers().get(PhoneType.HOME);
            
            if ( home != null ) {
                stmt.setString(5, home.getNumber());
            } else {
                stmt.setString(5, "");
            }

            Phone work = (Phone) patient.getPhoneNumbers().get(PhoneType.WORK);
            
            if ( work != null ) {
                stmt.setString(6, work.getNumber());
            } else {
                stmt.setString(6, "");
            }

            Phone mobile = (Phone) patient.getPhoneNumbers().get(PhoneType.MOBILE);
            
            if ( mobile != null ) {
                stmt.setString(7, mobile.getNumber());
            } else {
                stmt.setString(7, "");
            }

            stmt.setInt(8, patient.getHomeAddress().getId());
            stmt.setString(9, "");
            String email = patient.getEmail();
            
            if ( (email != null) && (email.length() > 0) ) {
                stmt.setString(10, patient.getEmail());
            } else {
                stmt.setString(10, "");
            }
            
            stmt.setString(11, patient.getExtraInfo().getSex());
            stmt.setString(12, patient.getDateOfBirth());
            stmt.setLong(13, patient.getUpdatedBy());
            stmt.setString(14, patient.getExtraInfo().getBirthCity());
            stmt.setString(15, Long.toString(patient.getExtraInfo().getBirthCountry().getId()));
            stmt.setString(16, patient.getGuid());
            stmt.setString(17, Boolean.toString(patient.isHasMiddleName()));
            stmt.setString(18, patient.getMrn());
            stmt.setLong(19, patient.getId());
            
            int recordsUpdated = stmt.executeUpdate();

            if ( recordsUpdated == 0 ) {
                throw new ObjectNotFoundException("The subject with ID: " + patient.getId() + " does not exist in the system.");
            }
            
            // Update the version number of the patient object
            stmt.close();
            query = "select version from patient where patientid = ? ";
            stmt = this.conn.prepareStatement(query);
            stmt.setLong(1, patient.getId());

            rs = stmt.executeQuery();

            if ( rs.next() ) {
                Version v = new Version(rs.getInt("version"));
                patient.setVersion(v);
            }
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column encryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to update subject with ID " + patient.getId() + ": " + e.getMessage(), e);
			}
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    
    /**
     * Logically deletes a patient by setting a flag in the database and appending a time stamp to the
     * NIH record number and GUID.
     * 
     * @param patient - The patient to be deleted
     * @throws ObjectNotFoundException	If the target patient cannot be found in the database.
     * @throws DuplicateObjectException	If the deletion cause a constraint violation.
     * @throws CtdbException	If any error has occurred during the deletion.
     */
    public void softDeletePatient(Patient patient)  throws ObjectNotFoundException, DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        
        try {
            String sql = "update patient set deleteFlag = TRUE, updatedby = ?, updateddate = CURRENT_TIMESTAMP, version = version + 1, " +
            			 " guid = ?,mrn = " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + " where patientid = ?";

            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, patient.getUpdatedBy());
            stmt.setString(2, patient.getGuid() + "_" + timeStamp);
            stmt.setString(3, patient.getMrn() + "_" + timeStamp);
            stmt.setLong(4, patient.getId());

            int recordsUpdated = stmt.executeUpdate();
            
            if ( recordsUpdated == 0 ) {
                throw new ObjectNotFoundException("Failed subject deletion: The subject with ID: " + patient.getId() + " does not exist in the system.");
            }
            
            String sql2 = "update patientprotocol set subjectid = subjectid || \'_\' || to_char(CURRENT_TIMESTAMP,\'YYYYMMDD_HHMISS\') where subjectid is not null and patientid = ?";
            stmt2 = this.conn.prepareStatement(sql2);
            stmt2.setLong(1, patient.getId());
            stmt2.executeUpdate();
   
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column encryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to soft delete subject with ID " + patient.getId() + ": " + e.getMessage(), e);
			}
        }
        finally {
            this.close(stmt);
            this.close(stmt2);
        }
    }
    
    /**
     * Delete a patient visit from the patientvisit table.
     * 
     * @param id
     * @throws ObjectNotFoundException
     * @throws DuplicateObjectException
     * @throws CtdbException
     */
    public void deletePatientVisit(long id)  throws ObjectNotFoundException, DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;
        
        try {
            String sql = "delete FROM patientvisit where visitdateid = ? ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, id);

            int recordDeleted = stmt.executeUpdate();

            if ( recordDeleted == 0 ) {
                throw new ObjectNotFoundException("Failed subject visit deletion: Visit date ID: " + id + " does not exist in the system.");
            }
        }
        catch ( SQLException e ) {
            throw new CtdbException("Unable to delete subject visit date with ID " + id + ": " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }
    
    /**
     * Deassociates all patient with a site.  This is most useful for deleting
     * sites.
     * 
     * @param id site ID to deassociate
     * @return (int) number of rows updated
     * @throws CtdbException if a database error occurs
     */
    public int deassociatePatientSite(long id) throws CtdbException
    {
        PreparedStatement stmt = null;
        int numUpdated = 0;
        
        try
        {
            String sql = "UPDATE patientprotocol SET siteid = null WHERE siteid = ?";
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, id);
            numUpdated = stmt.executeUpdate();
        }
        catch (SQLException sqle)
        {
        	throw new CtdbException("Failure removing site association to subject : " + sqle.getLocalizedMessage(), sqle);
        }
        finally
        {
            close(stmt);
        }
        
        return numUpdated;
    }

    /**
     * Assigns a Patient to Protocol in the CTDB System.
     *
     * @param patient  The patient to assign to protocol
     * @param protocol The protocol object to assign the patient to
     * @throws DuplicateObjectException thrown if the patient already exists in the system
     *                                  based on the unique constraints
     * @throws CtdbException            thrown if any other errors occur while processing
     */
    public void assignPatientToProtocol(Patient patient, PatientProtocol pp) throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "insert into patientprotocol(patientid, protocolid, active, xpatientroleid, createdby, createddate, updatedby, " +
            			 "updateddate, subjectnumber, enrollmentdate, siteid, groupid, cohortid, completiondate, associated, " +
            			 "recruited, futurestudy, validated, issubject, validatedby, validateddate,subjectid, protocol_randomization_id) " +
            		 	 "values(?, ?, ?, ?, ?, CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, patient.getId());
            stmt.setInt(2, pp.getId());
            stmt.setBoolean(3, pp.isActive());
            
            if ( (pp.getPatientRole() != null) && (pp.getPatientRole().getId() != Integer.MIN_VALUE) ) {
                stmt.setLong(4, pp.getPatientRole().getId());
            } else {
                stmt.setNull(4, java.sql.Types.NUMERIC);
            }
            
            stmt.setLong(5, patient.getCreatedBy());
            stmt.setLong(6, patient.getUpdatedBy());
            stmt.setString(7, pp.getSubjectNumber());
            if ( (pp.getEnrollmentDate() == null) || (pp.getEnrollmentDate().trim().length() == 0) ) {
                stmt.setNull(8, java.sql.Types.DATE);
            } else {
                stmt.setDate(8, new java.sql.Date(enrollmentDateSDF.parse(pp.getEnrollmentDate()).getTime()));
            }
            
            if ( pp.getSiteId() != Integer.MIN_VALUE ) {
                stmt.setLong(9, pp.getSiteId());
            } else {
                stmt.setNull(9, java.sql.Types.NUMERIC);
            }
            
            if ( pp.getGroupId() != Integer.MIN_VALUE ) {
                stmt.setLong(10, pp.getGroupId());
            } else {
                stmt.setNull(10, java.sql.Types.NUMERIC);
            }
            
            // cohortId, discontinued
            stmt.setNull(11, java.sql.Types.NUMERIC); 
            
            if ( (pp.getCompletionDate() == null) || (pp.getCompletionDate().trim().length() == 0) ) {
                stmt.setNull(12, java.sql.Types.DATE);
            } else {
                stmt.setDate(12, new java.sql.Date(enrollmentDateSDF.parse(pp.getCompletionDate()).getTime()));
            }
            
            stmt.setBoolean(13, pp.isAssociated());
            stmt.setBoolean(14, pp.isRecruited());
            stmt.setBoolean(15, pp.isFutureStudy());
            stmt.setBoolean(16, pp.isValidated());
            stmt.setBoolean(17, pp.isSubject());

            
            if ( pp.getValidatedBy() != Integer.MIN_VALUE ) {
                stmt.setLong(18, pp.getValidatedBy());
            } else {
                stmt.setNull(18, java.sql.Types.NUMERIC);
            }
            
            if ( pp.getValidatedDate() != null ) {
                stmt.setTimestamp(19, new Timestamp(pp.getValidatedDate().getTime()));
            } else {
                stmt.setNull(19, java.sql.Types.DATE);
            }
            
            stmt.setString(20, pp.getSubjectId());
            
            /*Randomization*/
            long protoRandomId = Integer.MIN_VALUE;
            int protocolId = pp.getId();
            ProtocolManagerDao protoManDao = ProtocolManagerDao.getInstance(conn);
            boolean hasRandomization = protoManDao.checkIfProtoHasRandomization(protocolId);
            if (hasRandomization) {
            	protoRandomId = getProtRandomizationIdForPat(protocolId);
            }
            if(protoRandomId != Integer.MIN_VALUE) {
            	stmt.setLong(21, protoRandomId);
            } else {
            	stmt.setNull(21, java.sql.Types.NUMERIC);
            }
            
            stmt.executeUpdate();
        }
        catch (PSQLException e) {
            if (e.getSQLState().equals( SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
                throw new DuplicateObjectException("The protocol_subject assignment already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to assign subject to study with ID " + pp.getId() + ": " + e.getMessage(), e);
            }
        }
        catch (SQLException e) {
            if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
                throw new DuplicateObjectException("The protocol/subject assignment already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to assign subject to study with ID " + pp.getId() + ": " + e.getMessage(), e);
            }
        }
        catch (ParseException pe) {
            throw new CtdbException(" Unable to assign subject to study : parse exception : " + pe.getMessage());
        }
        finally {
            this.close(stmt);
        }
    }
    
    public long getProtRandomizationIdForPat(int protocolId) throws CtdbException {
    	long protoRandomId = Integer.MIN_VALUE;
    	PreparedStatement stmt = null;
    	ResultSet rs = null;
    	
    	try {
        	String sql = "select * from patientprotocol where protocolid = ? "
        				+ " order by createddate desc nulls last "
        				+ " limit 1";
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, protocolId);
            rs = stmt.executeQuery();
            
            long lastSequence = Integer.MIN_VALUE;
            if ( rs.next() ) {
            	int lastProtoRandomId = rs.getInt("protocol_randomization_id");
            	if (!rs.wasNull()) {
            		ProtocolRandomization lastRandom = this.getProtocolRandomizationById(lastProtoRandomId);
            		lastSequence = lastRandom.getSequence();
            	}
            }
            
            long currSequence = Integer.MIN_VALUE;
            if(lastSequence == Integer.MIN_VALUE) {
            	currSequence = 1; 
            } else {
            	currSequence = lastSequence + 1;
            }
            ProtocolRandomization currRandom = this.getProtoRandomizationByProtoAndSeq(protocolId, currSequence);
            protoRandomId = currRandom.getId();
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to retrieve patient protocol by protocol id: " + protocolId + " " + e.getMessage(), e);
			}
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
           	
    	return protoRandomId;
    }

    /**
     * Updates assignment of a Patient to Protocol in the CTDB System.
     *
     * @param patient  The patient to update assignment to protocol
     * @param protocol The protocol object to update assignment
     * @throws CtdbException thrown if any other errors occur while processing
     */
    public void updatePatientToProtocolAssignment(Patient patient, PatientProtocol pp) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "update patientprotocol set updatedby = ?, updateddate = CURRENT_TIMESTAMP, active = ?, xpatientroleid = ?,  " +
            			 " subjectnumber=? , enrollmentdate = ?, siteid = ?, groupid = ?, cohortid= ?, completiondate = ?, associated = ?, " +
            			 " recruited=?, futurestudy=?, validated=?, issubject=?, validatedby=?,validateddate=?,subjectid= ?" +
            			 " where patientid = ? and protocolid = ? ";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, patient.getUpdatedBy());
            stmt.setBoolean(2, pp.isActive());
            stmt.setNull(3, java.sql.Types.NUMERIC);
            stmt.setString(4, pp.getSubjectNumber());
            
            if ( (pp.getEnrollmentDate() == null) || (pp.getEnrollmentDate().trim().length() == 0) ) {
                stmt.setNull(5, java.sql.Types.DATE);
            } else {
                stmt.setDate(5, new java.sql.Date(enrollmentDateSDF.parse(pp.getEnrollmentDate()).getTime()));
            }
            
            if ( pp.getSiteId() != Integer.MIN_VALUE ) {
                stmt.setLong(6, pp.getSiteId());
            } else {
                stmt.setNull(6, java.sql.Types.NUMERIC);
            }

            if ( (pp.getGroupId() != Integer.MIN_VALUE) && (pp.getGroupId() != 0) ) {
                stmt.setLong(7, pp.getGroupId());
            } else {
                stmt.setNull(7, java.sql.Types.NUMERIC);
            }
            
            if ( (pp.getCohortId() != Integer.MIN_VALUE) && (pp.getCohortId() != 0) ) {
                stmt.setLong(8, pp.getCohortId());
            } else {
                stmt.setNull(8, java.sql.Types.NUMERIC);
            }
            
            if ( (pp.getCompletionDate() == null) || (pp.getCompletionDate().trim().length() == 0) ) {
                stmt.setNull(9, java.sql.Types.TIMESTAMP);
            } else {
                stmt.setDate(9, new java.sql.Date(enrollmentDateSDF.parse(pp.getCompletionDate()).getTime()));
            }
            
            stmt.setBoolean(10, pp.isAssociated());
            stmt.setBoolean(11, pp.isRecruited());
            stmt.setBoolean(12, pp.isFutureStudy());
            stmt.setBoolean(13, pp.isValidated());
            stmt.setBoolean(14, pp.isSubject());
            
            if ( pp.getValidatedBy() != Integer.MIN_VALUE ) {
                stmt.setLong(15, pp.getValidatedBy());
            } else {
                stmt.setNull(15, java.sql.Types.NUMERIC);
            }
            
            if ( pp.isValidated() == false ) {
                stmt.setNull(16, java.sql.Types.TIMESTAMP);
            } else {
                stmt.setTimestamp(16, new Timestamp( new Date().getTime()));
            }
            stmt.setString(17, patient.getSubjectId());
            stmt.setLong(18, patient.getId());
            stmt.setLong(19, pp.getId());
          

            stmt.executeUpdate();
        }
        catch (PSQLException e) {
            throw new CtdbException("Unable to update assignment of subject to Study with ID " + pp.getId() + ": " + e.getMessage(), e);
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to update assignment of subject to study with ID " + pp.getId() + ": " + e.getMessage(), e);
        }
        catch (ParseException pe) {
            throw new CtdbException("Unable to update assignment of subject to study with ID " + pp.getId() + ": " + pe.getMessage(), pe);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Removes assignment of a Patient to Protocol in the CTDB System.
     *
     * @param patient    The patient to remove assignment to protocol
     * @param protocolId The protocol ID to remove assignment
     * @throws CtdbException thrown if any other errors occur while processing
     */ 
    public void removePatientToProtocolAssignment(Patient patient, long protocolId) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "delete FROM patientprotocol where patientid = ? and protocolid = ? ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, patient.getId());
            stmt.setLong(2, protocolId);

            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to remove assignment of subject to Study with ID " + protocolId + ": " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }
    
    /**
     * Get a patient protocol by a Patient Id and Protocol Id.
     *
     * @param patientId    The patient ID
     * @param protocolId The protocol ID to remove assignment
     * @throws CtdbException thrown if any other errors occur while processing
     */ 
    public PatientProtocol getPatientProtocalByPatientAndProtocol(long patientId, long protocolId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        PatientProtocol patientprotocol = new PatientProtocol();
        
        try {
            String sql = "select * FROM patientprotocol where patientid = ? and protocolid = ? ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, patientId);
            stmt.setLong(2, protocolId);

            rs= stmt.executeQuery();
            if(rs.next()){
            	patientprotocol = this.rsToPatientProtocol(rs);
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get patientProtocol with patient ID " + patientId + " and protocol ID " + protocolId + " : "+e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
        return patientprotocol;
    }

    public void recordReasonsAudit(PatientChangeTracker pct, Patient p) throws CtdbException {
     PreparedStatement stmt = null;
     String changedField = "";
     String[] values = null;
     
        try {
            String sql = "insert into patientedit (patientid, fieldChanged, fieldValue_old, fieldValue_new, changeDate, changedBy, patientversion) " +
            			 "values (?, ?, " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", CURRENT_TIMESTAMP, ?, ?) ";
            stmt = this.conn.prepareStatement(sql);
            
            for ( Entry<String, String[]> e : pct.getChangedFields().entrySet() ) {
                changedField = e.getKey();
         		values = e.getValue();
                stmt.setLong(1, p.getId());
                stmt.setString(2, changedField);
                stmt.setString(3, values[0]);
                stmt.setString(4, values[1]);
                stmt.setLong(5, p.getUpdatedBy());
                stmt.setInt(6, p.getVersion().getVersionNumber());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column encryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to record subject change reasons " + e.getMessage(), e);
			}
        }
        finally {
            this.close(stmt);
        }
    }

	public List<Map<String, String>> getPatientChangesInHashmap (long patientId)  throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
        try {
            List<Map<String, String>> l = new ArrayList<Map<String, String>>();
            
            String sql = "select pe.fieldChanged, pe.fieldValue, pe.reason, pe.changeDate, u.username " +
            			 "from patientedit pe, usr u where pe.patientid = ? and u.usrid = pe.changedby order by pe.changedate ";
			
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, patientId);
            rs = stmt.executeQuery();
            
            while ( rs.next() ) {
                Map<String, String> m = new HashMap<String, String>();
				
                m.put("User", rs.getString("username"));
                m.put("Field", rs.getString("fieldChanged"));
                m.put("Value", rs.getString("fieldValue"));
                m.put("Reason", rs.getString("reason"));
                m.put("Date", rs.getString("changeDate"));
                l.add(m);
            }
            
            return l;
        }
        catch ( SQLException e ) {
            throw new CtdbException("Unable to get subject change reasons " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

	public List<AuditDetail> getPatientChanges (String patientId)  throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			List<AuditDetail> l = new ArrayList<AuditDetail>();
			
			String sql = "select pe.fieldChanged, " + getDecryptionFunc("pe.fieldValue_old") + " as fieldValue_old, " +
						 getDecryptionFunc("pe.fieldValue_new") + " as fieldValue_new, pe.changeDate, pe.patientversion, u.username " +
						 "from patientedit pe, usr u,patientprotocol pp where pp.patientid = ? and pp.patientid=pe.patientid and u.usrid = pe.changedby order by pe.changeDate desc ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, Integer.valueOf(patientId));
			rs = stmt.executeQuery();
			
			while ( rs.next() ) {
				AuditDetail ad = new AuditDetail();
				ad.setUpdatedByUsername(rs.getString("username"));
				ad.setFieldName(rs.getString("fieldChanged"));
				if(rs.getString("fieldValue_old") == null || rs.getString("fieldValue_old").equals(Integer.toString(Integer.MIN_VALUE))) {
					ad.setFieldValueOriginal("");
				}else {
					ad.setFieldValueOriginal(rs.getString("fieldValue_old"));
				}
				if(rs.getString("fieldValue_new") == null || rs.getString("fieldValue_new").equals(Integer.toString(Integer.MIN_VALUE))) {
					ad.setFieldValueUpdated("");
				}else {
					ad.setFieldValueUpdated(rs.getString("fieldValue_new"));
				}
				ad.setUpdatedDate(rs.getTimestamp("changeDate"));
				ad.setUpdatedByUsername(rs.getString("username"));
				ad.setVersion(new Version(rs.getInt("patientversion")) );
				l.add(ad);
			}
			
			return l;
		}
		catch ( SQLException e ) {
			// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to get subject change reasons " + e.getMessage(), e);
			}
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
    /**
     * Versions a Patient in the CTDB System. All patient data that
     * is stored into the database will be encrypted.
     *
     * @param patientId The patient ID to version
     * @throws DuplicateObjectException thrown if the patient already exists in the system
     *                                  based on the unique constraints
     * @throws CtdbException            thrown if any other errors occur while processing
     */
    public void versionPatient(long patientId) throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "insert into patientarchive select * from patient where patientid = ?";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, patientId);

            stmt.executeUpdate();
        }
        catch ( SQLException e ) {
            if ( e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code")) ) {
                throw new DuplicateObjectException("A subject with the same version already exists in the system archive: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to version subject: " + e.getMessage(), e);
            }
        }
        finally {
            this.close(stmt);
        }
    }
    


    /**
     * Retrieves a Patient from the system based on the unique identifier.
     *
     * @param patientId The unique identifier of the Patient to retrieve
     * @return Patient data object
     * @throws ObjectNotFoundException thrown if the patient does not exist in the system
     * @throws CtdbException           thrown if any other errors occur while processing
     */
    public Patient getPatient(String patintId) throws ObjectNotFoundException, CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = selectStarFromPatient + " from patient , patientprotocol where patient.patientid=patientprotocol.patientid and  deleteflag = false and patientprotocol.patientid = ?";

            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, Integer.valueOf(patintId));
            rs = stmt.executeQuery();

            if ( !rs.next() ) {
                throw new ObjectNotFoundException("The subject with ID: " + patintId + " could not be found.");
            }

            Patient patient = this.rsToPatient(rs);
            patient.setProtocols(this.getProtocols(patient.getId()));

            return patient;
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to retrieve subject: " + e.getMessage(), e);
			}
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    
    
    public Patient getPatient(String subjectId,int protocolid) throws ObjectNotFoundException, CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = selectStarFromPatient + " from patient , patientprotocol "
            		+ "where patient.patientid=patientprotocol.patientid and  deleteflag = false and patientprotocol.subjectid = ? and protocolid =?";

            stmt = this.conn.prepareStatement(sql);
            stmt.setString(1, subjectId);
            stmt.setLong(2, protocolid);
            rs = stmt.executeQuery();

            if ( !rs.next() ) {
                throw new ObjectNotFoundException("The subject with ID: " + subjectId + " could not be found.");
            }

            Patient patient = this.rsToPatient(rs);
            patient.setProtocols(this.getProtocols(patient.getId()));

            return patient;
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to retrieve subject: " + e.getMessage(), e);
			}
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    
    

    
    /**
     * Retrieves a Patient from the system based on GUID
     *
     * @param guid the patient's GUID
     * @return Patient data object
     * @throws ObjectNotFoundException thrown if the patient does not exist in the system
     * @throws CtdbException           thrown if any other errors occur while processing
     */
    public Patient getPatientByGuid(String guid) throws ObjectNotFoundException, CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = selectStarFromPatient + " from patient , patientprotocol where patient.patientid=patientprotocol.patientid and  deleteflag = false and guid = ?";

            stmt = this.conn.prepareStatement(sql);
            stmt.setString(1, guid);
            rs = stmt.executeQuery();

            if ( !rs.next() ) {
                throw new ObjectNotFoundException("The subject with GUID: " + guid + " could not be found.");
            }

            Patient patient = this.rsToPatient(rs);
            patient.setProtocols(this.getProtocols(patient.getId()));

            return patient;
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to retrieve subject: " + e.getMessage(), e);
			}
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Updates a patient object's foreign key values with the normalized text
     * for display on the various 'select patient' screens
     *
     * @param p
     * @throws CtdbException
     */
    public void updatePatientDisplayValues(Patient p) throws CtdbException {

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "select p.guid, " + getDecryptionFunc("p.birthcity") + " as birthcity, xc.longname as birthCountryName, " +
            		"xs.name as sexName from patient p left join xcountry xc on cast(" + getDecryptionFunc("p.xbirthcountryid") + 
					" as bigint) = xc.xcountryid join xsex xs on cast(" + getDecryptionFunc("p.sex") + " as bigint) = xs.xsexid " +
            		"where p.deleteflag = false and p.patientid = ? ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, p.getId());

            rs = stmt.executeQuery();

            if ( !rs.next() ) {
                throw new ObjectNotFoundException("The subject with ID: " + p.getId() + " could not be updated.");
            }
            
            p.setGuid(rs.getString("guid"));
            p.getExtraInfo().setSex(rs.getString("sexName"));
            p.getExtraInfo().setBirthCity(rs.getString("birthcity"));
            p.getExtraInfo().setBirthCountryName(rs.getString("birthCountryName"));
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to retrieve subject: " + e.getMessage(), e);
			}
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }


    /**
     * Gets a patient by matching on the NIH record number
     *
     * @param nihNum
     * @return
     * @throws ObjectNotFoundException
     * @throws CtdbException
     */
    public Patient getPatientBySubjectId(String subjectId) throws ObjectNotFoundException, CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Patient patient = null;

        try {
            String sql = selectStarFromPatient + " from patient where deleteflag = false and subjectid = ? ";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setString(1, subjectId);
            
            rs = stmt.executeQuery();
            
            if ( !rs.next() ) {
                throw new ObjectNotFoundException("The subject with subject ID: " + subjectId + " could not be found.");
            }
            
            patient = this.rsToPatient(rs);
            patient.setProtocols(this.getProtocols(patient.getId()));
            
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to retrieve subject: " + e.getMessage(), e);
			}
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return patient;
    }
    
    /**
     * Retrieves the Patient by patient last name and first name. The last and first names are encrypted.
     *
     * @param lastName  Encrypted patient's last name.
     * @param firstName Encrypted patient's first name.
     * @return The Patient object
     * @throws CtdbException Thrown if any errors occur during the process.
     */
    public Patient getPatientByNameForAuditLog(String lastName, String firstName) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Patient patient = null;

        try {
            String sql = selectStarFromPatient + " from patient , patientprotocol where patient.patientid=patientprotocol.patientid and  " + getDecryptionFunc("lastname") + " = ? and " +
            			 getDecryptionFunc("firstname") + " = ? ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setString(1, lastName);
            stmt.setString(2, firstName);

            rs = stmt.executeQuery();
            
            if ( rs.next() ) {
                patient = this.rsToPatient(rs);
            }
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to retrieve subject by last name and first name: " + e.getMessage(), e);
			}
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return patient;
    }

    /**
     * Gets patients with little information for fast retrieval and processing
     * mainly used on list screens where only an display and patient id are needed
     *
     * @param prc
     * @return
     * @throws CtdbException
     */
    public List<Patient> getMinimalPatients(PatientResultControl prc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Patient> patients = new ArrayList<Patient>();

        try {
        	StringBuffer sql = new StringBuffer(200);
        	
        	sql.append("select distinct patientprotocol.subjectid, ")
        		.append(getDecryptionFunc("patient.mrn") + " as mrn, " + getDecryptionFunc("patient.lastname") + " as lastname, ")
        		.append(getDecryptionFunc("patient.firstname") + " as firstname, patient.version, patient.patientid, patient.guid, ")
        		.append("patientprotocol.recruited, patientprotocol.validated, patientprotocol.futurestudy, protocol.name as protocolname, ")
        		.append("protocol.patientdisplaytype, protocol.protocolnumber as protocolnumber, patientprotocol.orderval, ")
        		.append("patientprotocol.subjectnumber, patientprotocol.protocolid, patientprotocol.active, patientgroup.name as groupname, ")
        		.append("patientprotocol.protocol_randomization_id ")
        		.append("from patient LEFT OUTER JOIN patientprotocol ON patient.patientid = patientprotocol.patientid ")
        		.append("LEFT OUTER JOIN protocol ON patientprotocol.protocolid = protocol.protocolid ")
        		.append("LEFT OUTER JOIN patientgroup ON patientprotocol.groupid = patientgroup.groupid ")
        		.append("where patient.deleteflag = false and COALESCE(protocol.DELETEFLAG, false) != true ")
        		.append(prc.getSearchClause());
            
            if ( prc.isInProtocol() ) {
          	sql.append(prc.getSortString());
           } else {
           	sql.append(" order by guid " + prc.getSortOrder());
            }

            stmt = this.conn.prepareStatement(sql.toString());
            rs = stmt.executeQuery();
            
            while ( rs.next() ) {
                patients.add(this.rsToPatientMin(rs));
            }
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to retrieve subjects for subject home: " + e.getMessage(), e);
			}
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return patients;
    }
    
    public ProtocolRandomization getProtocolRandomizationById(int protRandomId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ProtocolRandomization randomization = new ProtocolRandomization();

        try {
        	String sql = "select * from protocol_randomization where protocol_randomization_id = ? ";
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, protRandomId);
            rs = stmt.executeQuery();
            
            if ( rs.next() ) {
            	randomization.setId(protRandomId);
            	randomization.setProtocolId(rs.getLong("protocolid"));
            	randomization.setSequence(rs.getLong("sequence"));
            	randomization.setGroupName(rs.getString("groupname"));
            	randomization.setGroupDescription(rs.getString("groupdescription"));
            }
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to retrieve protocol randomization by id: " + protRandomId + " " + e.getMessage(), e);
			}
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return randomization;
    }
    
    public ProtocolRandomization getProtoRandomizationByProtoAndSeq(int protocolId, long sequence) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ProtocolRandomization randomization = new ProtocolRandomization();

        try {
        	String sql = "select * from protocol_randomization where protocolid = ? and sequence = ? ";
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, protocolId);
            stmt.setLong(2, sequence);
            rs = stmt.executeQuery();
            
            if ( rs.next() ) {
            	randomization.setId(rs.getInt("protocol_randomization_id"));
            	randomization.setProtocolId(protocolId);
            	randomization.setSequence(sequence);
            	randomization.setGroupName(rs.getString("groupname"));
            	randomization.setGroupDescription(rs.getString("groupdescription"));
            }
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to retrieve protocol randomization by protocolId: " + protocolId 
						+ " and sequence " + sequence + " " + e.getMessage(), e);
			}
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return randomization;
    }
       
    /**
     * Gets patients with little information for fast retrieval and processing
     * mainly used on list screens where only an display and patient id are needed.
     * 
     * @param protocolId - The protocol ID used to restrict the resulting patients.
     * @return A list of patients for a given protocol ID.
     * @throws CtdbException If there are any database errors.
     */
    public List<Patient> getMinimalPatientsBySiteIds(long protocolId, List<Integer> siteIds) throws CtdbException {
    	List<Patient> patients = new ArrayList<Patient>();
    	 PreparedStatement stmt = null;
         ResultSet rs = null;
         Array siteIdsArr = null;
         
         try {
        	 StringBuffer sql = new StringBuffer(200);
         	
         	sql.append("select distinct patientprotocol.subjectid, ")
         		.append(getDecryptionFunc("patient.mrn") + " as mrn, " + getDecryptionFunc("patient.lastname") + " as lastname, ")
         		.append(getDecryptionFunc("patient.firstname") + " as firstname, patient.version, patient.patientid, patient.guid, ")
         		.append("patientprotocol.recruited, patientprotocol.validated, patientprotocol.futurestudy, protocol.name as protocolname, ")
         		.append("protocol.patientdisplaytype, protocol.protocolnumber as protocolnumber, patientprotocol.orderval, ")
         		.append("patientprotocol.subjectnumber, patientprotocol.protocolid, patientprotocol.active, patientgroup.name as groupname, ")
         		.append("patientprotocol.protocol_randomization_id ")
         		.append("from patient LEFT OUTER JOIN patientprotocol ON patient.patientid = patientprotocol.patientid ")
         		.append("LEFT OUTER JOIN protocol ON patientprotocol.protocolid = protocol.protocolid ")
         		.append("LEFT OUTER JOIN patientgroup ON patientprotocol.groupid = patientgroup.groupid ")
         		.append("where patient.deleteflag = false and COALESCE(protocol.DELETEFLAG, false) != true ");
         	
			if(siteIds != null) {
				Integer[] arrSiteIds = (Integer[]) siteIds.toArray(new Integer[siteIds.size()]);
				siteIdsArr = this.conn.createArrayOf("BIGINT", arrSiteIds);
				sql.append("and protocol.protocolid = ? and patientprotocol.siteid = ANY (?) order by subjectid asc ");
			}else {
				sql.append("and protocol.protocolid = ? order by subjectid asc ");
			}
  	         	stmt = this.conn.prepareStatement(sql.toString());
	         	stmt.setLong(1, protocolId);
	         	
	         	if(siteIds != null) {
	         		stmt.setArray(2, siteIdsArr);
	         	}
	         	rs = stmt.executeQuery();
	         	
	         	while ( rs.next() ) {
	         		patients.add(this.rsToPatientMin(rs));
	         	}
        	 
         }
         catch ( SQLException e ) {
         	// Check the sql state
 			if ( e.getSQLState().contains("39000") ) {
 				logger.error("Column decryption failed: " + e.getMessage(), e);
  			}
 			else {
 				logger.error("Unable to retrieve subject for the site : " + e.getMessage(), e);
 			}
         }
         finally {
        	 this.close(rs);
        	 this.close(stmt);
         }
    	
    	return patients;
    }
    
    /**
     * Retrieves all patients in the system.
     *
     * @param prc PatientResultControl object which determines searching and sorting of list
     * @return The list of patients in the system. The list will be empty if no
     *         patients exist in the system.
     * @throws CtdbException thrown if any errors occur while processing
     */
    public List<Patient> getPatients(PatientResultControl prc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Patient> patients = new ArrayList<Patient>();

        try {
            String sql = selectStarFromPatient + ", patientprotocol.orderval from patient LEFT OUTER JOIN patientprotocol ON " +
            			 "patient.patientid = patientprotocol.patientid where patient.deleteflag = false " + prc.getSearchClause();
            
            stmt = this.conn.prepareStatement(sql);
            
            rs = stmt.executeQuery();
            
            Patient patient = null;
            
            while ( rs.next() ) {
                patient = this.rsToPatient(rs);
                patient.setProtocols(this.getProtocols(patient.getId()));
                patients.add(patient);
            }
            
            if ( prc.getRowNumMax() != Integer.MIN_VALUE ) {
                patients = patients.subList(0, prc.getRowNumMax());
            }
            
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to retrieve subject: " + e.getMessage(), e);
			}
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return patients;
    }

    /**
     * Retrieves all versions of a patient stored. A version consists
     * of modifications to the metadata (demographic information) about
     * a patient.
     *
     * @param patientId The unique identifier of the Patient to retrieve
     * @return A list of all versions for a single patient. The list
     *         will be ordered by versions such that index 0 will be the first
     *         patient version. If the patient does not exist an empty
     *         list will be returned.
     * @throws CtdbException thrown if any errors occur
     */
    public List<Patient> getPatientVersions(String patientId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Patient> patients = new ArrayList<Patient>();
        
        try {
            String sql = selectStarFromPatientView + ", created.username created_username, updated.username updated_username, " +
            			 "to_char(patient_view.updateddate, 'MM-DD-YYYY HH24:MI') as updateddatetime, " +
            			 "to_char(patient_view.createddate, 'MM-DD-YYYY HH24:MI') as createddatetime " +
            			 "from patient_view,patientprotocol, usr created, usr updated " +
            			 "where patientprotocol.patientid = ? and patient_view.createdby = created.usrid and patientprotocol.patientid=patient_view.patientid and patient_view.updatedby = updated.usrid " +
            			 "order by version desc";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, Integer.valueOf(patientId));
            System.out.println("Patient audit sql for subject ID= :" + patientId);
            rs = stmt.executeQuery();
            
            Patient patient = null;
            
            while ( rs.next() ) {
                patient = this.rsToPatient(rs);
                patient.setCreatedByUsername(rs.getString("created_username"));
                patient.setUpdatedByUsername(rs.getString("updated_username"));
                patient.setProtocols(this.getProtocols(patient.getId()));
                SimpleDateFormat format = new SimpleDateFormat();
                
                try {
                    format.applyPattern("MM-dd-yyyy HH:mm");
                    patient.setUpdatedDate(format.parse(rs.getString("updateddatetime")));
                    patient.setCreatedDate(format.parse(rs.getString("createddatetime")));
                }
                catch ( Exception e ) { // if there is an exception, just use date w/ no time
                }
                
                patients.add(patient);
            }
            
        }
        catch (SQLException e) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to retrieve subjects: " + e.getMessage(), e);
			}
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return patients;
    }
    
    
    
    
    
    public PatientProtocol getPatientProtocol(String  subjectId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        PatientProtocol patientprotocol = new PatientProtocol();

        try {
            String sql = "select protocol.*, patientprotocol.active, patientprotocol.xpatientroleid, protocolpatientrole.name shortname, " +
            			 "patientprotocol.subjectnumber, patientProtocol.enrollmentdate, patientprotocol.siteid, patientprotocol.groupid, " +
            			 "patientprotocol.cohortid, patientprotocol.completiondate, patientprotocol.associated, patientprotocol.futurestudy, " +
            			 "patientprotocol.recruited, patientprotocol.validated, patientprotocol.validatedBy,patientprotocol.validatedDate, " +
            			 "patientprotocol.issubject, subjectid " +
            			 "from patientprotocol JOIN protocol ON protocol.protocolid = patientprotocol.protocolid LEFT OUTER JOIN protocolpatientrole " +
            			 "ON patientprotocol.xpatientroleid = protocolpatientrole.xpatientroleid " +
            			 "where coalesce(protocol.DELETEFLAG, false) != true  and subjectid = ?";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setString(1, subjectId);
            
            rs = stmt.executeQuery();
            
            while ( rs.next() ) {
            	
            	patientprotocol =this.rsToProtocol(rs);
            }
        }
        catch ( SQLException e ) {
            throw new CtdbException("Unable to retrieve subject: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return patientprotocol;
    }
	
    /**
     * Retrieves all protocols that this Patient is assigned to
     * in the system based on the unique identifier.
     *
     * @param patientId The unique identifier of the Patient to retrieve
     * @return List of protocols
     * @throws CtdbException thrown if any errors occur while processing
     */
    public List<PatientProtocol> getProtocols(long patientId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<PatientProtocol> protocols = new ArrayList<PatientProtocol>();

        try {
            String sql = "select protocol.*, patientprotocol.active, patientprotocol.xpatientroleid, protocolpatientrole.name shortname, " +
            			 "patientprotocol.subjectnumber, patientProtocol.enrollmentdate, patientprotocol.siteid, patientprotocol.groupid, " +
            			 "patientprotocol.cohortid, patientprotocol.completiondate, patientprotocol.associated, patientprotocol.futurestudy, " +
            			 "patientprotocol.recruited, patientprotocol.validated, patientprotocol.validatedBy,patientprotocol.validatedDate, " +
            			 "patientprotocol.issubject, subjectid " +
            			 "from patientprotocol JOIN protocol ON protocol.protocolid = patientprotocol.protocolid LEFT OUTER JOIN protocolpatientrole " +
            			 "ON patientprotocol.xpatientroleid = protocolpatientrole.xpatientroleid " +
            			 "where patientid = ? and coalesce(protocol.DELETEFLAG, false) != true ";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, patientId);
            
            rs = stmt.executeQuery();
            
            while ( rs.next() ) {
                protocols.add(this.rsToProtocol(rs));
            }
        }
        catch ( SQLException e ) {
            throw new CtdbException("Unable to retrieve subject: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return protocols;
    }

    /**
     * Determines if a patient has response data collected in a protocol
     *
     * @param protooclId
     * @param patientId
     * @return
     * @throws CtdbException
     */
    public boolean hasProtocolData(long protooclId, long patientId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean isSucessful = false;
        
        try {
            String sql = "select Distinct 1 as existing_data from administeredform af, form f " +
            			 "where af.formid = f.formid and f.protocolid = ? and af.patientid = ? ";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, protooclId);
            stmt.setLong(2, patientId);

            rs = stmt.executeQuery();
            
            if ( rs.next() ) {
            	isSucessful =  true;
            }
        }
        catch ( SQLException sqle ) {
            throw new CtdbException("Unable to determine subject data entry status " + sqle.getMessage() + sqle);

        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return isSucessful;
    }
    
    public boolean isSubjectReusedInOtherProtocolWithDifferentIdinPP(long protooclId, String patientId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean isSucessful = false;
        
        try {
            String sql = "select count(subjectid) from patientprotocol where protocolid = ? and patientid <> ? ";
            		
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, protooclId);
            stmt.setInt(2, Integer.valueOf(patientId));

            rs = stmt.executeQuery();
            
            if ( rs.next() ) {
            	isSucessful =  true;
            }
        }
        catch ( SQLException sqle ) {
            throw new CtdbException("Unable to determine subject data entry status " + sqle.getMessage() + sqle);

        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return isSucessful;
    }
    
    
    

 
	
	public boolean hasFutureVisits (String patientId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean foundVisits = false;
		
		try {
			String sql = "select count(visitdateid) rowcount from patientvisit pv, patientprotocol pp where pp.patientid=pv.patientid and pv.visitdate > CURRENT_TIMESTAMP AND pp.subjectid = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, patientId);
			rs = stmt.executeQuery();
			
			if ( rs.next() ) {
				if ( rs.getInt("rowcount") > 0 ) {
					foundVisits = true;
				}
			}
			else {
				foundVisits = true;
			}
		}
		catch ( SQLException sqle ) {
			throw new CtdbException("Unable to determine future subject visits " + sqle.getMessage() + sqle);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return foundVisits;
	}
	
	public boolean hasPatientProtocol (long protooclId, long patientId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean foundStudy = false;
		
		try {
			String sql = "select count(patientid) rowcount from patientprotocol where protocolid = ? and patientid = ? ";
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protooclId);
			stmt.setLong(2, patientId);
			rs = stmt.executeQuery();
			
			if ( rs.next() ) {
				if ( rs.getInt("rowcount") > 0 ) {
					foundStudy = true;
				}
			}
			else {
				foundStudy = true;
			}
		}
		catch ( SQLException sqle ) {
			throw new CtdbException("Unable to determine subject study status " + sqle.getMessage() + sqle);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return foundStudy;
	}

    /**
     * Checks to determine if a patient currently has data entry underway within a protocol
     *
     * @param protocolId
     * @param patientId
     * @return
     * @throws CtdbException
     */
    public List<AdministeredForm> hasActiveDataEntry(long protocolId, long patientId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<AdministeredForm> listOfActiveAdminsiteredForms = new ArrayList<AdministeredForm>();

        try {
            String sql = "select af.administeredformid, af.formid, ded.dataentrydraftid " +
            			 "from administeredform af, form f, dataentrydraft ded " +
            			 "where patientid = ? and ded.administeredformid = af.administeredformid and f.formid = af.formid and f.protocolid = ? " +
            			 "and ded.lockdate is null ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, patientId);
            stmt.setLong(2, protocolId);

            rs = stmt.executeQuery();
            
            if ( rs.next() ) {
                AdministeredForm af = new AdministeredForm();
                af.setId(rs.getInt("administeredformid"));
                Form f = new Form();
                f.setId(rs.getInt("formid"));
                af.setForm(f);
                af.setDataEntryDraftId(rs.getInt("dataentrydraftid"));
                listOfActiveAdminsiteredForms.add(af);
            }
        }
        catch ( SQLException e ) {
            throw new CtdbException("Unable to retrieve active data entry status: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return listOfActiveAdminsiteredForms;
    }

    /**
     * Checks if the patient is administered.  Uses ID and versionNumber of the
     * domain object to check to see if it is administered.  If the versionNumber
     * in the domain object is not set (equals to Integer.MIN_VALUE), then the
     * current version of the patient in the data base is used for the check.
     *
     * @param patient the patient object to check
     * @return if the patient with the version has been administered
     * @throws CtdbException thrown if any errors occur while processing
     */
    public boolean isAdministered(String patientId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int num = 0;

        try {
            String sql = "select count(administeredformid) from administeredform af,patientprotocol pp where pp.patientid = ?  and pp.patientid=af.patientid";
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, Integer.valueOf(patientId));
            rs = stmt.executeQuery();
            
            if ( rs.next() ) {
                num = rs.getInt(1);
            }
        }
        catch ( SQLException e ) {
            throw new CtdbException("Unable to check is administered for a subject with id " + patientId + " : " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return num > 0;
    }

    /**
     * Transforms a ResulSet object into a Patient object
     *
     * @param rs ResultSet to transform to Patient object
     * @return Patient data object
     * @throws SQLException thrown if any errors occur while retrieving data from result set
     */
    private Patient rsToPatient(ResultSet rs) throws SQLException, NumberFormatException {
        Patient patient = new Patient();
        patient.setId(rs.getInt("patientid"));
        patient.setSubjectId(notNull(rs.getString("subjectid")));
        patient.setMrn(notNull(rs.getString("mrn")));
        patient.setVersion(new Version(rs.getInt("version")));
        patient.setFirstName( notNull(rs.getString("firstname")));
        patient.setMiddleName(rs.getString("middlename"));
        patient.setLastName(notNull(rs.getString("lastname")));

        // aggregate phone numbers
        Map<PhoneType, Phone> phones = new HashMap<PhoneType, Phone>();
        phones.put(PhoneType.HOME, new Phone(rs.getString("homephone"), PhoneType.HOME));
        phones.put(PhoneType.WORK, new Phone(rs.getString("workphone"), PhoneType.WORK));
        phones.put(PhoneType.MOBILE, new Phone(rs.getString("mobilephone"), PhoneType.MOBILE));
        patient.setPhoneNumbers(phones);

        // Address
        Address address = new Address();
        address.setId(rs.getInt("homeaddressid"));
        patient.setHomeAddress(address);

        patient.setEmail(rs.getString("email"));
        patient.setDateOfBirth(rs.getString("dob"));
        patient.setCreatedBy(rs.getInt("createdby"));
        patient.setCreatedDate(rs.getDate("createddate"));
        patient.setUpdatedBy(rs.getInt("updatedby"));
        patient.setUpdatedDate(rs.getDate("updateddate"));
        patient.setGuid(rs.getString("guid"));
        patient.setHasMiddleName(rs.getString("hasmiddlename") != null && rs.getString("hasmiddlename").trim().equalsIgnoreCase("true") );

        PatientExtraInfo pei = new PatientExtraInfo();
        try {
            pei.setSex(rs.getString("sex"));
            pei.setBirthCity(rs.getString("birthcity"));
            pei.setBirthCountry(new CtdbLookup(Integer.parseInt(rs.getString("xbirthcountryid").trim())));
        }
        catch (java.lang.NullPointerException npe) {
        }
        catch (SQLException sle) {
            if (sle.getMessage().indexOf("Invalid column name") < 0) {
                // Coming from get versions for auditing
                // pei not in patient view.
                throw sle;
            }
        }
        
        patient.setExtraInfo(pei);


        return patient;
    }
    
    /**
     * Transforms a ResulSet object into a Patient object, which is a subset of the normal
     * patient object. Such objects are more suitable for being displayed in tables or drop downs.
     * 
     * @param rs - ResultSet to transform to Patient object
     * @return Patient data object
     * @throws SQLException If any errors occur while retrieving data from result set
     */
    private Patient rsToPatientMin(ResultSet rs) throws SQLException, CtdbException {
    	Patient patient = new Patient();
        patient.setId(rs.getInt("patientid"));
        patient.setPatientId(rs.getInt("patientid"));
        patient.setGuid(notNull(rs.getString("guid")));
        patient.setVersion(new Version(rs.getInt("version")));
        patient.setSubjectId(notNull( rs.getString("subjectid")));
        patient.setMrn(notNull( rs.getString("mrn")));
        patient.setFirstName(notNull(rs.getString("firstname")));
        patient.setLastName(notNull(rs.getString("lastname")));
        
        PatientProtocol pro = new PatientProtocol();
        pro.setSubjectNumber(notNull(rs.getString("subjectnumber")));
        pro.setId(rs.getInt("protocolid"));
        pro.setActive(rs.getBoolean("active"));
        pro.setName(rs.getString("protocolname"));
        pro.setProtocolNumber(rs.getString("protocolnumber"));
        pro.setGroupName(notNull(rs.getString("groupname")));
        pro.setValidated(rs.getBoolean("validated"));
        pro.setRecruited(rs.getBoolean("recruited"));
        pro.setFutureStudy(rs.getBoolean("futurestudy"));
        
        int protRandomizationId = rs.getInt("protocol_randomization_id");
        ProtocolRandomization randomization = new ProtocolRandomization();
        if(!rs.wasNull()) {
        	randomization = this.getProtocolRandomizationById(protRandomizationId);
        }        
        pro.setProtocolRandomization(randomization);
        
        List<PatientProtocol> protocols = new ArrayList<PatientProtocol>();
        protocols.add(pro);
        patient.setProtocols(protocols);
        
        return patient;
    }

    /**
     * Transforms a ResulSet object into a minimal (id, number, name) Protocl object
     *
     * @param rs ResultSet to transform to Protocol object
     * @return Protocol data object
     * @throws SQLException thrown if any errors occur while retrieving data from result set
     */
    private PatientProtocol rsToProtocol(ResultSet rs) throws SQLException {
        PatientProtocol protocol = new PatientProtocol();
        protocol.setSubjectId(rs.getString("subjectid"));
        protocol.setId(rs.getInt("protocolid"));
        protocol.setName(rs.getString("name"));
        protocol.setProtocolNumber(rs.getString("protocolnumber"));
        protocol.setActive(rs.getBoolean("active"));
        
        CtdbLookup patientRole = new CtdbLookup(rs.getInt("xpatientroleid"), rs.getString("shortname"), null);
        protocol.setPatientRole(patientRole);
        protocol.setSubjectNumber(rs.getString("subjectnumber"));
        
        if ( rs.getDate("enrollmentDate") != null ) {
            protocol.setEnrollmentDate(enrollmentDateSDF.format(rs.getDate("enrollmentdate")));
        } else {
            protocol.setEnrollmentDate("");
        }
        
        if ( rs.getString("siteId") != null ) {
            protocol.setSiteId(rs.getInt("siteId"));
        }
        
        if ( rs.getString("groupid") != null ) {
            protocol.setGroupId(rs.getInt("groupid"));
        }
        
        if ( rs.getString("cohortId") != null ) {
            protocol.setCohortId(rs.getInt("cohortid"));
        }
        
        if ( rs.getDate("completiondate" ) != null) {
            protocol.setCompletionDate(enrollmentDateSDF.format(rs.getDate("completiondate")));
        } else {
            protocol.setCompletionDate("");
        }
        
        protocol.setFutureStudy(rs.getBoolean("futurestudy"));
        protocol.setRecruited(rs.getBoolean("recruited"));
        protocol.setValidated(rs.getBoolean("validated") );
        
        if ( rs.getString("validatedby") != null ) {
            protocol.setValidatedBy(rs.getInt("validatedby"));
        }
        
        if ( rs.getDate("validateddate") != null ) {
            protocol.setValidatedDate(rs.getDate("validateddate"));
        }
        
        return protocol;
    }
    
    /**
     * Transforms a ResulSet object into a PatientProtocol object
     *
     * @param rs ResultSet to transform to PatientProtocol object
     * @return PatientProtocol data object
     * @throws SQLException thrown if any errors occur while retrieving data from result set
     */
    private PatientProtocol rsToPatientProtocol(ResultSet rs) throws SQLException {
        PatientProtocol protocol = new PatientProtocol();
        protocol.setSubjectId(rs.getString("subjectid"));
        protocol.setId(rs.getInt("protocolid"));
        protocol.setActive(rs.getBoolean("active"));
        
        if ( rs.getString("subjectnumber") != null ) {
        	protocol.setSubjectNumber(rs.getString("subjectnumber"));
        }
        if ( rs.getDate("enrollmentDate") != null ) {
            protocol.setEnrollmentDate(enrollmentDateSDF.format(rs.getDate("enrollmentdate")));
        } else {
            protocol.setEnrollmentDate("");
        }
        
        if ( rs.getString("siteId") != null ) {
            protocol.setSiteId(rs.getInt("siteId"));
        }
        
        if ( rs.getString("groupid") != null ) {
            protocol.setGroupId(rs.getInt("groupid"));
        }
        
        if ( rs.getString("cohortId") != null ) {
            protocol.setCohortId(rs.getInt("cohortid"));
        }
        
        if ( rs.getDate("completiondate" ) != null) {
            protocol.setCompletionDate(enrollmentDateSDF.format(rs.getDate("completiondate")));
        } else {
            protocol.setCompletionDate("");
        }
        
        protocol.setFutureStudy(rs.getBoolean("futurestudy"));
        protocol.setRecruited(rs.getBoolean("recruited"));
        protocol.setValidated(rs.getBoolean("validated") );
        
        if ( rs.getString("validatedby") != null ) {
            protocol.setValidatedBy(rs.getInt("validatedby"));
        }
        
        if ( rs.getDate("validateddate") != null ) {
            protocol.setValidatedDate(rs.getDate("validateddate"));
        }
        
        return protocol;
    }

    /**
     * Result set to next of kin, that is what it does
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    private NextOfKin rsToNextOfKin(ResultSet rs) throws SQLException {
        NextOfKin nok = new NextOfKin();
        nok.setId(rs.getInt("nextofkinid"));
        nok.setRelationship(rs.getString("relationship"));
        nok.setFirstName(rs.getString("firstname"));
        nok.setLastName(rs.getString("lastname"));

        // aggregate phone numbers
        Map<PhoneType, Phone> phones = new HashMap<PhoneType, Phone>();
        phones.put(PhoneType.HOME, new Phone(rs.getString("homephone"), PhoneType.HOME));
        phones.put(PhoneType.WORK, new Phone(rs.getString("workphone"), PhoneType.WORK));
        nok.setPhoneNumbers(phones);

        // Address
        Address address = new Address();
        address.setId(rs.getInt("addressid"));
        nok.setHomeAddress(address);
        return nok;
    }

    /**
     * Updates the patient order value based upon usr input on the change patient order page
     *
     * @param protocolId
     * @param patientId
     * @param order
     * @throws CtdbException
     */
    public void updatePatientProtocolOrder(long protocolId, String patientId, int order) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "update patientprotocol set updateddate = CURRENT_TIMESTAMP, orderval = ? where patientid = ? and protocolid = ? ";
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, order);
            stmt.setLong(2, Long.valueOf(patientId));
            stmt.setLong(3, protocolId);
            stmt.executeUpdate();
        }
        catch ( SQLException e ) {
            throw new CtdbException("Unable to update subject order with ID " + patientId + ": " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Auto sort the patients in a protocol
     *
     * @param protocolId
     * @param usePatientName - sort by name or nih record number
     * @throws CtdbException
     */
    public void autoSortPatients(long protocolId, boolean usePatientName, boolean useSubjectNumber) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "MERGE INTO PATIENTPROTOCOL PP " +
            			 "	USING (SELECT ? AS PROTOCOLID, PatientID, ROWNUM as OrderVal " +
            			 "	FROM  (SELECT p.patientid, subjectid, " +
            			 "			lower(" + getDecryptionFunc("p.lastname") + " || " + getDecryptionFunc("p.firstname") + " ) as PatientName, " +
            			 "			coalesce (lower(pp.subjectnumber), lower(" + getDecryptionFunc("p.lastname") + 
            			 "				|| " + getDecryptionFunc("p.firstname") + " )) as tehSubjectNum " +             
            			 "			FROM PATIENT p, PATIENTPROTOCOL pp " +
            			 "			WHERE P.PATIENTID = PP.PATIENTID AND PP.PROTOCOLID = ? ";
            
            if (usePatientName) {
                sql += "			ORDER BY PatientName ";
            }
            else if (useSubjectNumber) {
                 sql += " ORDER by tehSubjectNum ";
            }
            else{
                sql += " order by subjectid ";
            }
            
            sql += "	)) SortedList ON (PP.PATIENTID = SortedList.PATIENTID AND PP.PROTOCOLID = SortedList.PROTOCOLID) " +
            	   "WHEN MATCHED THEN UPDATE SET PP.ORDERVAL = SortedList.OrderVal " +
            	   "WHEN NOT MATCHED THEN INSERT (PATIENTID, PROTOCOLID, CREATEDBY, CREATEDDATE, UPDATEDBY, UPDATEDDATE, ACTIVE, XPATIENTROLEID, " +
            	   "ORDERVAL, SUBJECTNUMBER, ENROLLMENTDATE) VALUES (NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL) ";
            
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, protocolId);
            stmt.setLong(2, protocolId);
            stmt.executeUpdate();
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to update subject order with ID " + protocolId + ": " + e.getMessage(), e);
			}
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Updates the patientprotocol.orderval column for a subject that has been added
     * to a protocol or edited so the paitent appears in the correct order
     *
     * @param patientId
     * @param protocolId
     * @param usePatientName
     * @throws CtdbException
     */
    public void sortAddedPatient(long patientId, long protocolId, boolean usePatientName) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String orderNumQuery = "(select count(p.patientid) from patient p, patientprotocol pp " +
            					   "where LOWER('subjectid') < " +
            					   "(select LOWER('subjectid') " +
            					   "from patient p where p.patientid = ?) and pp.patientid = p.patientid and pp.protocolid = ?) ";
            
            if ( usePatientName ) {
                orderNumQuery = orderNumQuery.replaceAll("pp\\.subjectid", " p.lastname");
            }
            
            stmt = this.conn.prepareStatement(orderNumQuery);
            stmt.setLong(1, patientId);
            stmt.setLong(2, protocolId);
            rs = stmt.executeQuery();
            int orderNumber = 0;
            
            if ( rs.next() ) {
                orderNumber = rs.getInt(1);
            }
            
            orderNumber++; // the correct order is num less than + 1
            stmt.close();

            String sql = "update patientprotocol set orderval = orderval + 1 where orderval >= ? and protocolid = ? ";
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, orderNumber - 1);
            stmt.setLong(2, protocolId);
            stmt.executeUpdate();
            stmt.close();
            
            sql = "update patientprotocol pp set orderval = ? where patientid = ? and protocolid = ? ";
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, orderNumber);
            stmt.setLong(2, patientId);
            stmt.setLong(3, protocolId);
            stmt.executeUpdate();
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to update subject order with ID " + protocolId + ": " + e.getMessage(), e);
			}
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Retrieves all patients within a protocol that do not have a subject number
     * @param protocol the protocol to search in
     * @return list of patients
     * @throws CtdbException
     */
    public Map<Integer, PatientProtocol> getPatientsWithoutSubjectNumberByProtocol(Protocol protocol) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<Integer, PatientProtocol> patientProtocols = new HashMap<Integer, PatientProtocol>();

        try {
            String sql = "select pp.* from patient p, patientprotocol pp where p.patientid = pp.patientid and " +
            			 "p.deleteflag = false and pp.subjectnumber is null and pp.protocolid = ? order by pp.enrollmentdate, p.lastname ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, protocol.getId());
            rs = stmt.executeQuery();
            
            while ( rs.next() ) {
                PatientProtocol patientProtocol = new PatientProtocol();
                patientProtocol.setId(rs.getInt("protocolid"));
                patientProtocols.put(rs.getInt("patientid"), patientProtocol);
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to retrieve subjects by protocol: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return patientProtocols;
    }

    /**
     * Updates the patient protocol subject number
     * @param patientProtocols the map of patient protocol relationship
     * @throws CtdbException
     */
    public void updatePatientProtocol(Map<Integer, PatientProtocol> patientProtocols) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "update patientprotocol set updateddate = CURRENT_TIMESTAMP, updatedby = ?, subjectnumber = ?,subjectid =? " +
            			 "where patientid = ? and protocolid = ? ";

            stmt = this.conn.prepareStatement(sql);

            for ( Entry<Integer, PatientProtocol> entry : patientProtocols.entrySet() )
            {
                PatientProtocol patientProtocol = entry.getValue();
                stmt.setLong(1, patientProtocol.getUpdatedBy());
                stmt.setString(2, patientProtocol.getSubjectNumber());
                stmt.setString(3, patientProtocol.getSubjectId());
                stmt.setLong(4, entry.getKey());
                stmt.setLong(5, patientProtocol.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to batch update subject protocol subject numbers: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Finds the next incremental subject number
     * @param protocol the protocol to search in
     * @return the next subject number value
     * @throws CtdbException the exception
     */
    public int findNextSubjectNumber(Protocol protocol) throws CtdbException
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String subjectNumber = "";
        
        try
        {
            String sql = "select max(substr(pp.subjectnumber, ?, 5)) from protocol p, patientprotocol pp " +
            			 " where p.protocolid = pp.protocolid and pp.protocolid = ? ";

            int subjectNumberStartLocation = 0;
            int suffixStartLocation = 6;

            if ( StringUtils.isNotEmpty(protocol.getSubjectNumberPrefix()) )
            {
                //Match prefix
                sql += "and substr(pp.subjectnumber, 0, ?) = ? ";
                subjectNumberStartLocation = protocol.getSubjectNumberPrefix().length() + 1;
                suffixStartLocation = subjectNumberStartLocation + 5;
            }

            if ( StringUtils.isNotEmpty(protocol.getSubjectNumberSuffix()) )
            {
                //Match suffix
                sql += "and substr(pp.subjectnumber, ?) = ? ";
            }

            stmt = this.conn.prepareStatement(sql);

            int paramIndex = 1;

            stmt.setInt(paramIndex++, subjectNumberStartLocation);
            stmt.setLong(paramIndex++, protocol.getId());

            if ( StringUtils.isNotEmpty(protocol.getSubjectNumberPrefix()) )
            {
                stmt.setInt(paramIndex++, subjectNumberStartLocation - 1);
                stmt.setString(paramIndex++, protocol.getSubjectNumberPrefix());
            }

            if ( StringUtils.isNotEmpty(protocol.getSubjectNumberSuffix()) )
            {
                stmt.setInt(paramIndex++, suffixStartLocation);
                stmt.setString(paramIndex++, protocol.getSubjectNumberSuffix());
            }

            rs = stmt.executeQuery();
            
            if ( rs.next() )
            {
                subjectNumber = rs.getString(1);
            }
        }
        catch ( Exception e )
        {
        	throw new CtdbException("Unable to get max subject number for protocol " + protocol.getId() + ": " + e.getMessage(), e);
        }
        finally
        {
            this.close(rs);
            this.close(stmt);
        }
        
        return StringUtils.isNotEmpty(subjectNumber) ? Integer.parseInt(subjectNumber) + 1 : protocol.getSubjectNumberStart();
    }

    private String notNull(String theStr) {
        if ( theStr == null ) {
            return "";
        }
        
        return theStr;
    }

    /**
     * Create a new patient visit record in database
     */
	public void createPatientVisit(PatientVisit pv) throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "insert into patientvisit(visitdateid, patientid, protocolid, intervalId, visitdate, createdby, createddate, "
            			+ "	updatedby, updateddate,token, comments, intervalclinicalpointid) " 
            			+ " values(DEFAULT, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?, ?) ";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, pv.getPatientId());
            stmt.setLong(2, pv.getProtocolId());
            
            if ( pv.getIntervalId() > 0 ) {
            	stmt.setLong(3, pv.getIntervalId());
            }
            else {
            	stmt.setNull(3, java.sql.Types.NUMERIC);
            }
            
            stmt.setTimestamp(4, new java.sql.Timestamp(pv.getVisitDate().getTime()));
            stmt.setLong(5, pv.getCreatedBy());
            stmt.setLong(6, pv.getUpdatedBy());
            
            String token = pv.getToken();
            
            if ( !Utils.isBlank(token) ) {
            	stmt.setString(7, pv.getToken());
            }
            else {
            	stmt.setNull(7, java.sql.Types.VARCHAR);	
            }
            stmt.setString(8, pv.getComments());
            stmt.setLong(9, pv.getIntervalClinicalPointId());
            stmt.executeUpdate();
            
            // Set the ID of the newly created patient visit.
            pv.setId(getInsertId(conn, "patientvisit_seq"));
        }
        catch ( SQLException e ) {
            throw new CtdbException("Unable to create new subject visit: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
	}

    /**
     * Create a new patient visit record in database
     */
	public void updatePatientVisit(PatientVisit pv) throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {
            String sql = "update patientvisit set visitdate = ?, updatedby = ?, intervalId = ?, updateddate = CURRENT_TIMESTAMP, token = ?, comments = ?, "
            			+ " intervalclinicalpointid = ? where visitdateid = ? ";
            
            stmt = this.conn.prepareStatement(sql);

            stmt.setTimestamp(1, new java.sql.Timestamp(pv.getVisitDate().getTime()));
            stmt.setLong(2, pv.getUpdatedBy());
            stmt.setLong(3, pv.getIntervalId());
            stmt.setString(4, pv.getToken());
            stmt.setString(5, pv.getComments());
            stmt.setLong(6, pv.getIntervalClinicalPointId());
            stmt.setLong(7, pv.getId());
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to update subject visit: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
	
	}
	
	
	/**
     * This function determines whether a user alreadyexists in the system with this username
     * @param username
     * @return
     * @throws CtdbException
     */
    public boolean doesPatientVisitExistWithThisToken(String token) throws CtdbException{
    	
    	 PreparedStatement stmt = null;
         ResultSet rs = null;

         try {
             stmt = this.conn.prepareStatement("select * from patientvisit where token = ? ");
             stmt.setString(1, token);
             rs = stmt.executeQuery();
             if(rs.next()) {
            	 return true;
             }else {
            	 return false;
             }


         }
         catch (SQLException e) {

             throw new CtdbException("Failure checking user patient visit token " + e.getMessage(), e);
         }
         finally {
             this.close(stmt);
             this.close(rs);
         }
    	
    	
    }
	
    /**
     * Retrieve the list of patient visits 
     * @param Patient result control
     * @return the list of visits 
     * @throws CtdbException the exception
     */

	public PatientVisit getPatientVisit(long visitDateId) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		PatientVisit visit = new PatientVisit();
		
		try {
			String sql = "select " + getDecryptionFunc("p.mrn") + " as mrn,p.guid,pp.subjectid, " +
						 getDecryptionFunc("p.firstname") + " as firstname, " + getDecryptionFunc("p.lastname") + " as lastname, " +
						 "pv.visitdateid, pv.patientid, pv.intervalid, pv.protocolid, pv.visitdate, pv.token, pv.comments, pv.intervalclinicalpointid, i.name as visitType " +
						 "from patientvisit pv join patient p on p.patientid = pv.patientid join patientprotocol pp on p.patientid=pp.patientid left outer join interval i on " +
						 "pv.intervalid = i.intervalid where p.deleteflag = false and pv.visitdateid = ? ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, visitDateId);
			rs = stmt.executeQuery();

			while ( rs.next() ) {
				visit = new PatientVisit();
				visit.setSubjectId(rs.getString("subjectid"));
				visit.setMrn(rs.getString("mrn"));
				visit.setGuid(rs.getString("guid"));
				visit.setPatientFirstName(rs.getString("firstname"));
				visit.setPatientLastName(rs.getString("lastname"));
				visit.setId(rs.getInt("visitDateId"));
				visit.setPatientId(rs.getInt("patientId"));
				visit.setProtocolId(rs.getInt("protocolId"));
				visit.setIntervalId(rs.getInt("intervalid"));
				visit.setVisitDate(rs.getTimestamp("visitDate"));
				visit.setIntervalName(notNull(rs.getString("visitType")) );
				visit.setToken(rs.getString("token"));
				visit.setComments(rs.getString("comments"));
				visit.setIntervalClinicalPointId(rs.getInt("intervalclinicalpointid"));
			}
		}
		catch ( SQLException e ) {
			// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Error occur while getting the subject visit list : " + e.getMessage(), e);
			}
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return visit;
	}
	
	
	
	
	
	
	/**
     * Retrieve the list of patient visits 
     * @param Patient result control
     * @return the list of visits 
     * @throws CtdbException the exception
     */

	public PatientVisit getPatientVisit(String token) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		PatientVisit visit = null;
		
		try {
			String sql = "select " +  getDecryptionFunc("p.mrn") + " as mrn,p.guid,pp.subjectid, " +
						 getDecryptionFunc("p.firstname") + " as firstname, " + getDecryptionFunc("p.lastname") + " as lastname, " +
						 "pv.visitdateid, pv.patientid, pv.intervalid, pv.protocolid, pv.visitdate, pv.token, pv.comments, pv.intervalclinicalpointid, i.name as visitType " +
						 "from patientvisit pv join patient p on p.patientid = pv.patientid join patientprotocol pp on p.patientid=pp.patientid left outer join interval i on " +
						 "pv.intervalid = i.intervalid where p.deleteflag = false and pv.token = ? ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, token);
			rs = stmt.executeQuery();

			while ( rs.next() ) {
				
				visit = new PatientVisit();
				visit.setSubjectId(rs.getString("subjectid"));
				visit.setMrn(rs.getString("mrn"));
				visit.setGuid(rs.getString("guid"));
				visit.setPatientFirstName(rs.getString("firstname"));
				visit.setPatientLastName(rs.getString("lastname"));
				visit.setId(rs.getInt("visitDateId"));
				visit.setPatientId(rs.getInt("patientId"));
				visit.setProtocolId(rs.getInt("protocolId"));
				visit.setIntervalId(rs.getInt("intervalid"));
				visit.setVisitDate(rs.getTimestamp("visitDate"));
				visit.setIntervalName(notNull(rs.getString("visitType")) );
				visit.setToken(rs.getString("token"));
				visit.setComments(rs.getString("comments"));
				visit.setIntervalClinicalPointId(rs.getInt("intervalclinicalpointid"));
			}
		}
		catch ( SQLException e ) {
			// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Error occur while getting the subject visit list : " + e.getMessage(), e);
			}
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return visit;
	}

    /**
     * Retrieve the list of patient visits 
     * @param siteIds 
     * @param roleId 
     * @param Patient result control
     * @return the list of visits 
     * @throws CtdbException the exception
     */
	public List<PatientVisit> getPatientVisitsUserSiteIds(PatientVisitResultControl pvrc,long protocolId, List<Integer> siteIds) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<PatientVisit> visitList = new ArrayList<PatientVisit>();
		PatientVisit visit = new PatientVisit();
		Array siteIdsArr = null;
		try {
			 StringBuffer sql = new StringBuffer(200);
        	 sql.append("select " +  getDecryptionFunc("p.mrn") + " as mrn, p.guid, " +  getDecryptionFunc("p.email") + " as email, pp.subjectid, " +
						 getDecryptionFunc("p.firstname") + " as firstname, " + getDecryptionFunc("p.lastname") + " as lastname, " +
						 "pv.visitdateid, pv.patientid, pv.intervalid, pv.token, pv.protocolid, pv.visitdate, pv.comments, pv.intervalclinicalpointid, i.name as visitType " +
						 "from patientvisit pv join patient p on p.patientid = pv.patientid join patientprotocol pp on p.patientid=pp.patientid " + pvrc.getSearchClause() +
						 "left outer join interval as i on pv.intervalid = i.intervalid where p.deleteflag = false and pp.protocolid=? ");
			
			if(siteIds != null) {
				Integer[] arrSiteIds = (Integer[]) siteIds.toArray(new Integer[siteIds.size()]);
				siteIdsArr = this.conn.createArrayOf("BIGINT", arrSiteIds);
				sql.append(" and pp.siteid = ANY (?) order by visitdate");
			}else {
				sql.append(" order by visitdate");
			}
			
				stmt = this.conn.prepareStatement(sql.toString());
				stmt.setLong(1, protocolId);
				if(siteIds != null) {
					stmt.setArray(2, siteIdsArr);
				}
			rs = stmt.executeQuery();
			
			while ( rs.next() ) {
				visit = rsToPatientVisit(rs);
				visit.setEmail(rs.getString("email"));
				visitList.add(visit);
			}
			
		}
		catch ( SQLException e ) {
			// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				logger.error("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				logger.error("Error occured while getting the subject visit list: " + e.getMessage(), e);
			}
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return visitList;
	}
	
	   /**
     * Retrieve the list of patient visits 
     * @param Patient result control
     * @return the list of visits 
     * @throws CtdbException the exception
     */
	public List<PatientVisit> getPatientVisits(PatientVisitResultControl pvrc,long protocolId) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<PatientVisit> visitList = new ArrayList<PatientVisit>();
		PatientVisit visit = new PatientVisit();

		try {
			String sql = "select " +  getDecryptionFunc("p.mrn") + " as mrn, p.guid, " +  getDecryptionFunc("p.email") + " as email, pp.subjectid, " +
						 getDecryptionFunc("p.firstname") + " as firstname, " + getDecryptionFunc("p.lastname") + " as lastname, " +
						 "pv.visitdateid, pv.patientid, pv.intervalid, pv.token, pv.protocolid, pv.visitdate, pv.comments, pv.intervalclinicalpointid, i.name as visitType " +
						 "from patientvisit pv join patient p on p.patientid = pv.patientid join patientprotocol pp on p.patientid=pp.patientid " + pvrc.getSearchClause() +
						 "left outer join interval as i on pv.intervalid = i.intervalid where p.deleteflag = false and pp.protocolid=? order by visitdate ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();
			
			while ( rs.next() ) {
				visit = new PatientVisit();
				visit.setSubjectId(rs.getString("subjectid"));
				visit.setMrn(rs.getString("mrn"));
				visit.setGuid(rs.getString("guid"));
		        visit.setEmail(rs.getString("email"));
				visit.setPatientFirstName(rs.getString("firstname"));
				visit.setPatientLastName(rs.getString("lastname"));
				visit.setId(rs.getInt("visitDateId"));
				visit.setPatientId(rs.getInt("patientId"));
				visit.setProtocolId(rs.getInt("protocolId"));
				visit.setIntervalId(rs.getInt("intervalid"));
				visit.setVisitDate(rs.getTimestamp("visitDate"));
				visit.setIntervalName(notNull(rs.getString("visitType")) );
				visit.setToken(rs.getString("token"));
				visit.setComments(rs.getString("comments"));
				visit.setIntervalClinicalPointId(rs.getInt("intervalclinicalpointid"));
				visitList.add(visit);
			}
		}
		catch ( SQLException e ) {
			// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Error occur while getting the subject visit list : " + e.getMessage(), e);
			}
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return visitList;
	}

	/**
     * Retrieve the list of patient visits 
     * @param Patient result control
     * @return the list of visits 
     * @throws CtdbException the exception
     */
	public List<PatientVisit> getMonthPatientVisits(PatientVisitResultControl pvrc) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<PatientVisit> visitList = new ArrayList<PatientVisit>();
		PatientVisit visit = new PatientVisit();
		
		try {
			String sql = "select distinct " + getDecryptionFunc("p.mrn") + " as mrn,p.guid,pp.subjectid, " +
						 getDecryptionFunc("p.firstname") + " as firstname, " + getDecryptionFunc("p.lastname") + " as lastname, " +
						 "pv.visitdateid, pv.patientid, pv.intervalid, pv.protocolid, pv.visitdate, pv.token, pv.comments, pv.intervalclinicalpointid, i.name as visitType, pro.protocolnumber " +
						 "from patientvisit pv join patient p on p.patientid = pv.patientid  join patientprotocol pp on pp.patientid=p.patientid and pv.protocolid = pp.protocolid join protocol pro on " +
						 "pro.protocolid = pp.protocolid left outer join interval i on pv.intervalid = i.intervalid WHERE pv.visitdate > ? AND " +
						 "pv.visitdate < ? and p.deleteflag = false and pro.deleteFlag = false ";
			
			if ( pvrc.getProtocolId() > Integer.MIN_VALUE ) {
				sql += "AND pp.protocolid = ? ";
			}
			
			sql += "order by pv.visitdate ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setTimestamp(1, new Timestamp(pvrc.getStartDate().getTime()));
			stmt.setTimestamp(2, new Timestamp(pvrc.getEndDate().getTime()));
			
			if ( pvrc.getProtocolId() > Integer.MIN_VALUE ) {
				stmt.setLong(3, pvrc.getProtocolId());
			}
			
			rs = stmt.executeQuery();
			
			while ( rs.next() ) {
				visit = new PatientVisit();
				visit.setSubjectId(rs.getString("subjectid"));
				visit.setMrn(rs.getString("mrn"));
				visit.setGuid(rs.getString("guid"));
				visit.setPatientFirstName(rs.getString("firstname"));
				visit.setPatientLastName(rs.getString("lastname"));
				visit.setId(rs.getInt("visitDateId"));
				visit.setPatientId(rs.getInt("patientId"));
				visit.setProtocolId(rs.getInt("protocolId"));
				visit.setIntervalId(rs.getInt("intervalid"));
				visit.setVisitDate(rs.getTimestamp("visitDate"));
				visit.setIntervalName(notNull(rs.getString("visitType")));
				visit.setProtocolNumber(rs.getString("protocolnumber"));
				visit.setToken(rs.getString("token"));
				visit.setComments(rs.getString("comments"));
				visit.setIntervalClinicalPointId(rs.getInt("intervalclinicalpointid"));
				visitList.add(visit);
			}
		}
		catch (SQLException e) {
			// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Error occur while getting the subject visit list : " + e.getMessage(), e);
			}
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return visitList;
	}
	
	public List<PatientVisit> getMonthPatientVisitsBySites(PatientVisitResultControl pvrc, List<Integer> siteIds) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<PatientVisit> visitList = new ArrayList<PatientVisit>();
		PatientVisit visit = new PatientVisit();
		
		String inClause = "";
		for (int i = 0; i < siteIds.size(); i++) {
		  inClause += "?";
		  if (i < siteIds.size() - 1) {
			  inClause += ",";
		  }
		}
		
		try {
			String sql = "select distinct " + getDecryptionFunc("p.mrn") + " as mrn,p.guid,pp.subjectid, " 
						 + getDecryptionFunc("p.firstname") + " as firstname, " + getDecryptionFunc("p.lastname") + " as lastname, " 
						 + " pv.visitdateid, pv.patientid, pv.intervalid, pv.protocolid, pv.visitdate, pv.token, pv.comments, "
						 + " pv.intervalclinicalpointid, i.name as visitType, pro.protocolnumber " 
						 + " from patientvisit pv join patient p on p.patientid = pv.patientid  "
						 + " join patientprotocol pp on pp.patientid=p.patientid and pv.protocolid = pp.protocolid "
						 + " join protocol pro on pro.protocolid = pp.protocolid "
						 + " left outer join interval i on pv.intervalid = i.intervalid "
						 + " WHERE pv.visitdate > ? AND pv.visitdate < ? and p.deleteflag = false and pro.deleteFlag = false ";
			
			if ( pvrc.getProtocolId() > Integer.MIN_VALUE ) {
				sql += "AND pp.protocolid = ? AND pp.siteid in (" + inClause + ") ";
			}
			
			sql += "order by pv.visitdate ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setTimestamp(1, new Timestamp(pvrc.getStartDate().getTime()));
			stmt.setTimestamp(2, new Timestamp(pvrc.getEndDate().getTime()));
			
			if ( pvrc.getProtocolId() > Integer.MIN_VALUE ) {
				stmt.setLong(3, pvrc.getProtocolId());
			}
			
			for (int i = 0; i < siteIds.size(); i++) {
				 stmt.setInt(4 + i, siteIds.get(i));
			}
			
			rs = stmt.executeQuery();
			
			while ( rs.next() ) {
				visit = new PatientVisit();
				visit.setSubjectId(rs.getString("subjectid"));
				visit.setMrn(rs.getString("mrn"));
				visit.setGuid(rs.getString("guid"));
				visit.setPatientFirstName(rs.getString("firstname"));
				visit.setPatientLastName(rs.getString("lastname"));
				visit.setId(rs.getInt("visitDateId"));
				visit.setPatientId(rs.getInt("patientId"));
				visit.setProtocolId(rs.getInt("protocolId"));
				visit.setIntervalId(rs.getInt("intervalid"));
				visit.setVisitDate(rs.getTimestamp("visitDate"));
				visit.setIntervalName(notNull(rs.getString("visitType")));
				visit.setProtocolNumber(rs.getString("protocolnumber"));
				visit.setToken(rs.getString("token"));
				visit.setComments(rs.getString("comments"));
				visit.setIntervalClinicalPointId(rs.getInt("intervalclinicalpointid"));
				visitList.add(visit);
			}
		}
		catch (SQLException e) {
			// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Error occur while getting the subject visit list : " + e.getMessage(), e);
			}
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return visitList;
	}

	/**
	 * Checks the database to see if the patient visit is already scheduled for a given patient, visit date, and visit type.
	 * 
	 * @param patientId - The ID of the patient.
	 * @param visitDate - The date of the scheduled visit.
	 * @param vTypeId - The ID of the associated visit type.
	 * @param visitId - The ID of the associated patient visit.
	 * @return True if and only if there is already a patient visit scheduled in the database.
	 * @throws CtdbException If a database error occurs while searching for a patient visit.
	 */
	public boolean visitExists(long patientId, Date visitDate, long vTypeId, long visitId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean foundVisit = false;
		StringBuffer sql = new StringBuffer(100);
		int count = 1;
		
		sql.append("select count(visitdateid) rowcount from patientvisit where patientid = ? AND visitdate = ? ");
		
		// Add in visit type ID condition.
		if ( vTypeId > 0 ) {
			sql.append("AND intervalId = ? ");
		}
		else {
			 sql.append("AND intervalId is null ");
		}
		
		// Check if the patient visit ID condition is needed.
		if ( visitId > 0 ) {
			sql.append("AND visitdateid != ? ");
		}
		
		try {
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(count++, patientId);
			stmt.setTimestamp(count++, new Timestamp(visitDate.getTime()));
			
			// Add in visit type ID, if needed.
			if ( vTypeId > 0 ) {
				stmt.setLong(count++, vTypeId);
			}
			
			// Add in patient visit ID, if needed.
			if ( visitId > 0 ) {
				stmt.setLong(count++, visitId);
			}
			
			rs = stmt.executeQuery();
			
			if ( rs.next() ) {
				if ( rs.getInt("rowcount") > 0 ) {
					foundVisit = true;
				}
			}
		}
        catch ( SQLException e ) {
        	throw new CtdbException("Unable to determine if the subject visit is already scheduled.", e);
        }
		finally {
			this.close(rs);
			this.close(stmt);
		}

		return foundVisit;
	}
	
    /**
     * Creates a Patient visit prepop values in database. The patient visit prepop value object ID, 
     * interval ID, and patient visit ID will be set as well.
     */
	public void createPatientVisitPrepopValues(PatientVisit pv, List<PatientVisitPrepopValue> pvPrepopValueList) throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        ResultSet rs = null;

        try {
        	String sql = "select * from prepopdataelement_interval where prepopdataelementid = ? and intervalid = ? ";
        	String sql2 = "insert into patientvisit_prepopvalue (id, prepopdataelement_interval_id, patientvisitid, prepopvalue) "
					+ " values(DEFAULT, ?, ?, ?) ";
        	
        	stmt = this.conn.prepareStatement(sql);
        	stmt2 = this.conn.prepareStatement(sql2);
        	
        	for (PatientVisitPrepopValue pvPrepopValue : pvPrepopValueList) {
        		stmt.setLong(1, pvPrepopValue.getPrepopDataElementId());
    			stmt.setLong(2, pv.getIntervalId());
    		
    			rs = stmt.executeQuery();
    			
    			long prepopDEIntervalId = -1L;
    			
    			if ( rs.next() ) {
    				prepopDEIntervalId = rs.getLong("id");
    			}

    			rs.close();
    			
    			long patientVisitId = pv.getId();
    			
    			if ( (prepopDEIntervalId > 0) && (patientVisitId > 0) ) {
    				// Update the patient and prepop DE interval ID in the pre-pop value object.
    				pvPrepopValue.setPrepopDEIntervalId(prepopDEIntervalId);
    				pvPrepopValue.setPatientVisitId(patientVisitId);
    				
    				stmt2.setLong(1, prepopDEIntervalId);
    				stmt2.setLong(2, patientVisitId);
    				stmt2.setString(3, pvPrepopValue.getPrepopvalue());
    				stmt2.executeUpdate();
    				
    				// Set PatientVisitPrepopValue ID.
    				pvPrepopValue.setId(getInsertId(conn, "patientvisit_prepopvalue_seq"));
    			}
        	}
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to update subject visit: " + e.getMessage(), e);
        }
        finally {
        	this.close(rs);
        	this.close(stmt);
        	this.close(stmt2);
        }
	
	}
	
    /**
     * Delete a list of prepopulated values associated with patient visit  from the prepopdataelement_interval table.
     * 
     * @param id
     * @throws ObjectNotFoundException
     * @throws DuplicateObjectException
     * @throws CtdbException
     */
    public void deletePatientVisitPrepopValues(long visitDateId)  throws ObjectNotFoundException, DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;
        String sql = "";
        
        try {
            sql = "delete from patientvisit_prepopvalue where patientvisitid = ? ";
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, visitDateId);
            stmt.executeUpdate();
        }
        catch ( SQLException e ) {
            throw new CtdbException("Unable to delete subject visit date with ID " + visitDateId + ": " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }
    
	public List<PatientVisitPrepopValue> getPvPrepopValueByPvId(long visitDateId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<PatientVisitPrepopValue> pvPrepopValueList = new ArrayList<PatientVisitPrepopValue>();

		try {
			String sql = "select pdei.intervalid, pde.*, pvpv.id as patientvisit_prepopvalueid, pvpv.* " +
					"from patientvisit pv join patientvisit_prepopvalue pvpv on pv.visitdateid = pvpv.patientvisitid " +
					"join prepopdataelement_interval pdei on pvpv.prepopdataelement_interval_id = pdei.id join " +
					"prepopdataelement pde on pdei.prepopdataelementid = pde.prepopdataelementid where pv.visitdateid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, visitDateId);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				PatientVisitPrepopValue pvPrepopValue = rsToPatientVisitPrepopValue(rs);
				PrepopDataElement prepopDE = rsToPrepopDataElement(rs);
				
				pvPrepopValue.setPrepopDataElement(prepopDE);
				pvPrepopValueList.add(pvPrepopValue);				
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to retrieve intervals: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return pvPrepopValueList;
	}
	
	public PatientVisit getPatientVisit(long patientid, Date visitDate, long intervalid) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		PatientVisit visit = null;
		
		try {
			String sql = "select " +  getDecryptionFunc("p.mrn") + " as mrn, p.guid, pp.subjectid ," +
						 getDecryptionFunc("p.firstname") + " as firstname, " + getDecryptionFunc("p.lastname") + " as lastname, " +
						 "pv.visitdateid, pv.patientid, pv.intervalid, pv.protocolid, pv.visitdate, pv.token, pv.comments, pv.intervalclinicalpointid, i.name as visitType " +
						 "from patientvisit pv join patient p on p.patientid = pv.patientid join patientprotocol pp on p.patientid=pp.patientid left outer join interval i on " +
						 "pv.intervalid = i.intervalid where p.deleteflag = false and pv.patientid = ? and pv.intervalid = ? and pv.visitdate = ?";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, patientid);
			stmt.setLong(2, intervalid);
			stmt.setTimestamp(3, new java.sql.Timestamp(visitDate.getTime()));
			rs = stmt.executeQuery();

			if ( rs.next() ) {
				visit = rsToPatientVisit(rs);
			}
			else {
				throw new ObjectNotFoundException("Could not find a patient visit entry in the database.May be there is no scheduled visit for this patient.");
			}
		}
		catch ( SQLException e ) {
			// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Error occur while getting the subject visit : " + e.getMessage(), e);
			}
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return visit;
	}
	
	private PatientVisit rsToPatientVisit(ResultSet rs) throws SQLException {
		PatientVisit visit = new PatientVisit();
		
		visit.setSubjectId(rs.getString("subjectid"));
		visit.setMrn(rs.getString("mrn"));
		visit.setGuid(rs.getString("guid"));
		visit.setPatientFirstName(rs.getString("firstname"));
		visit.setPatientLastName(rs.getString("lastname"));
		visit.setId(rs.getInt("visitDateId"));
		visit.setPatientId(rs.getInt("patientId"));
		visit.setProtocolId(rs.getInt("protocolId"));
		visit.setIntervalId(rs.getInt("intervalid"));
		visit.setVisitDate(rs.getTimestamp("visitDate"));
		visit.setIntervalName(notNull(rs.getString("visitType")) );
		visit.setToken(rs.getString("token"));
		visit.setComments(rs.getString("comments"));
		visit.setIntervalClinicalPointId(rs.getInt("intervalclinicalpointid"));
		
		return visit;
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
	
	/**
     * Retrieve the list of intervals along with the scheduler status.
     * @param protocolId
     * @return the list of IntervalScheduleDisplay 
     * @throws CtdbException the exception
     */
	public List<IntervalScheduleDisplay> getIntervalsWithSchedulerStatus(long patientId, long protocolId) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<IntervalScheduleDisplay> intervalScheduleList = new ArrayList<IntervalScheduleDisplay>();
		IntervalScheduleDisplay intervalScheduleDisplay = null;
		
		try {

			String sql = "select i.name, pv.visitdate from interval i left join (select * from patientvisit where patientvisit.patientid = ? ) pv on i.intervalid = pv.intervalid " +
						 "where i.protocolid = ? order by i.orderval";
			
			stmt = this.conn.prepareStatement(sql);
			
			stmt.setLong(1, patientId);
			stmt.setLong(2, protocolId);

			rs = stmt.executeQuery();
			
			while ( rs.next() ) 
			{
				intervalScheduleDisplay = new IntervalScheduleDisplay();
				intervalScheduleDisplay.setIntervalName(rs.getString("name"));
				
				Date visitDate = rs.getTimestamp("visitDate");
				if( visitDate != null )
				{
					intervalScheduleDisplay.setSchedulerStatus( IntervalScheduleDisplay.SCHEDULER_STATUS_SCHEDULED );
				}
				else
				{
					intervalScheduleDisplay.setSchedulerStatus( IntervalScheduleDisplay.SCHEDULER_STATUS_NOT_SCHEDULED );
				}
				intervalScheduleList.add(intervalScheduleDisplay);
			}
		}
		catch (SQLException e) {
			// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Error occur while getting the subject visit list : " + e.getMessage(), e);
			}
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return intervalScheduleList;
	}


    public List<Patient> getPatientListByProtocol(Integer protocolId) throws ObjectNotFoundException, CtdbException {
    	List<Patient> patList = new ArrayList<Patient>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql_patient_protocol = selectStarFromPatient + " from patient "
 										+ " INNER JOIN patientvisit on patientvisit.patientid = patient.patientid " 
 										+ " INNER JOIN patientprotocol patientprotocol ON patientvisit.patientid = patientprotocol.patientid and patientvisit.protocolid = patientprotocol.protocolid "
            		 					+ " WHERE patientprotocol.protocolid = ? "
            		 					+ " ORDER BY patient.guid  ";
            stmt = this.conn.prepareStatement(sql_patient_protocol);
            stmt.setInt(1, protocolId);

            rs = stmt.executeQuery();
            while(rs.next()){
	            Patient patient = this.rsToPatient(rs);
	            patList.add(patient);
            }
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to retrieve subject: " + e.getMessage(), e);
			}
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return patList;
    }
    
    public List<Patient> getPatientListForAllProtocol() throws ObjectNotFoundException, CtdbException {
    	List<Patient> patList = new ArrayList<Patient>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql_patient_all_protocol = selectStarFromPatient + " from patient "
 											+ " INNER JOIN patientvisit on patientvisit.patientid = patient.patientid " 
 											+ " INNER JOIN patientprotocol patientprotocol ON patientvisit.patientid = patientprotocol.patientid and patientvisit.protocolid = patientprotocol.protocolid "
 											+ " ORDER BY patient.guid  ";

            stmt = this.conn.prepareStatement(sql_patient_all_protocol);

            rs = stmt.executeQuery();
            while(rs.next()){
	            Patient patient = this.rsToPatient(rs);
	            patList.add(patient);
            }
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to retrieve subject: " + e.getMessage(), e);
			}
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return patList;
    }
    
    public List<Patient> getPatientsInDataCollectionByProtocol(Integer protocolId) throws ObjectNotFoundException, CtdbException {
    	List<Patient> patList = new ArrayList<Patient>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql_patient_visit = selectStarFromPatient + " from patient "
            		 					+ " INNER JOIN patientvisit on patientvisit.patientid = patient.patientid " 
            		 					+ " INNER JOIN patientprotocol patientprotocol ON patientvisit.patientid = patientprotocol.patientid and patientvisit.protocolid = patientprotocol.protocolid "
            		 					+ " INNER JOIN administeredform ON administeredform.patientid = patientvisit.patientid and patientvisit.intervalid = administeredform.intervalid "
            		 					+ " WHERE patientvisit.protocolid = ? "
            		 					+ " ORDER BY patient.guid  ";
            stmt = this.conn.prepareStatement(sql_patient_visit);
            stmt.setInt(1, protocolId);

            rs = stmt.executeQuery();
            while(rs.next()){
	            Patient patient = this.rsToPatient(rs);
	            patList.add(patient);
            }
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to retrieve subject: " + e.getMessage(), e);
			}
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return patList;
    }
    
    public List<Patient> getPatientListByProtocolAndSite(Integer protocolId, Integer siteId) throws ObjectNotFoundException, CtdbException {
    	List<Patient> patList = new ArrayList<Patient>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String sql_patient_protocol_site = selectStarFromPatient + " from patient "
 										+ " INNER JOIN patientvisit on patientvisit.patientid = patient.patientid " 
 										+ " INNER JOIN patientprotocol patientprotocol ON patientvisit.patientid = patientprotocol.patientid and patientvisit.protocolid = patientprotocol.protocolid "
            		 					+ " WHERE patientprotocol.protocolid = ? and patientprotocol.siteid = ? "
            		 					+ " ORDER BY patient.guid  ";
            stmt = this.conn.prepareStatement(sql_patient_protocol_site);
            stmt.setInt(1, protocolId);
            stmt.setInt(2, siteId);

            rs = stmt.executeQuery();
            while(rs.next()){
	            Patient patient = this.rsToPatient(rs);
	            patList.add(patient);
            }
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to retrieve subject: " + e.getMessage(), e);
			}
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return patList;
    }   

	public List<Patient> getPatientListByProtocolAndSites(Integer protocolId, Integer[] siteIds)
			throws ObjectNotFoundException, CtdbException {
		List<Patient> patList = new ArrayList<Patient>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer();
			sql.append(selectStarFromPatient);
			sql.append(" from patient "
					+ " INNER JOIN patientvisit on patientvisit.patientid = patient.patientid "
					+ " INNER JOIN patientprotocol patientprotocol ON patientvisit.patientid = patientprotocol.patientid and patientvisit.protocolid = patientprotocol.protocolid "
					+ " WHERE patientprotocol.protocolid = ? and patientprotocol.siteid = ANY (?) ORDER BY patient.guid");


			Array siteIdsArr = this.conn.createArrayOf("BIGINT", siteIds);

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, protocolId);
			stmt.setArray(2, siteIdsArr);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Patient patient = this.rsToPatient(rs);
				patList.add(patient);
			}
		} catch (SQLException e) {
			// Check the sql state
			if (e.getSQLState().contains("39000")) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to retrieve subject: " + e.getMessage(), e);
			}
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return patList;
	}
	
	
	@SuppressWarnings("deprecation")
	public List<Patient> getPatientListByProtocolIdAndSiteIds(Integer protocolId, List<Integer> siteIds)
			throws ObjectNotFoundException, CtdbException{
		
		List<Patient> patList = new ArrayList<Patient>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		PatientResultControl prc = new PatientResultControl();

		try {
				for (int siteId:siteIds) {
					String query = "select distinct patientprotocol.subjectid, " + 
							getDecryptionFunc("patient.mrn") + " as mrn, " + getDecryptionFunc("patient.lastname") + " as lastname, " + 
							getDecryptionFunc("patient.firstname") + " as firstname, patient.version, patient.patientid, patient.guid, " + 
							" patientprotocol.recruited, patientprotocol.validated, patientprotocol.futurestudy, protocol.name as protocolname, " + 
							" protocol.patientdisplaytype, protocol.protocolnumber as protocolnumber, patientprotocol.orderval, " + 
							" patientprotocol.subjectnumber, patientprotocol.protocolid, patientprotocol.active, patientgroup.name as groupname, " + 
							" patientprotocol.protocol_randomization_id " + 
							" from patient LEFT OUTER JOIN patientprotocol ON patient.patientid = patientprotocol.patientid " + 
							" LEFT OUTER JOIN protocol ON patientprotocol.protocolid = protocol.protocolid " + 
							" LEFT OUTER JOIN patientgroup ON patientprotocol.groupid = patientgroup.groupid " + 
							" where patient.deleteflag = false and COALESCE(protocol.DELETEFLAG, false) != true " + 
							"       and patientprotocol.protocolid = ? and patientprotocol.siteid = ? " +
							prc.getSearchClause();
		            if ( prc.isInProtocol() ) {
		            	query += prc.getSortString();
		            } else {
		            	query += " order by guid " + prc.getSortOrder();
		            }
		            stmt = this.conn.prepareStatement(query);
					stmt.setLong(1, protocolId);
					stmt.setInt(2, siteId);
			
					rs = stmt.executeQuery();
	
					while (rs.next()) {
						Patient patient = this.rsToPatientMin(rs);
						patList.add(patient);
					
					}
				}
			
			
		} catch (SQLException e) {
			// Check the sql state
			if (e.getSQLState().contains("39000")) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to retrieve subject: " + e.getMessage(), e);
			}
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return patList;
	}
	
	/*Check if any patient in a protocol has been assigned to a randomized group*/
    public boolean checkIfAnyPatProtoHasRandomization(int protocolId) throws CtdbException {
    	boolean hasRandomization = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
        	String sql = "select * from patientprotocol where protocolid = ? and protocol_randomization_id is not null limit 1";
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, protocolId);
            rs = stmt.executeQuery();
            
            if ( rs.next() ) {
                hasRandomization = true;           	
            }
        }
        catch ( SQLException e ) {
        	// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to retrieve randomization group by protocolId: " + protocolId
						+ e.getMessage(), e);
			}
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
        
        return hasRandomization;
    }
}
