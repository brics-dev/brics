package gov.nih.nichd.ctdb.patient.manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import gov.nih.nichd.ctdb.attachments.manager.AttachmentManager;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.FileUploadException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.patient.common.PatientResultControl;
import gov.nih.nichd.ctdb.patient.common.PatientVisitResultControl;
import gov.nih.nichd.ctdb.patient.dao.PatientManagerDao;
import gov.nih.nichd.ctdb.patient.dao.PatientRoleDao;
import gov.nih.nichd.ctdb.patient.domain.AuditDetail;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.domain.PatientProtocol;
import gov.nih.nichd.ctdb.patient.domain.PatientVisit;
import gov.nih.nichd.ctdb.patient.domain.PatientVisitPrepopValue;
import gov.nih.nichd.ctdb.patient.util.PatientChangeTracker;
import gov.nih.nichd.ctdb.protocol.dao.ProtocolManagerDao;
import gov.nih.nichd.ctdb.protocol.domain.IntervalScheduleDisplay;
import gov.nih.nichd.ctdb.protocol.domain.PrepopDataElement;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.util.dao.AddressDao;

/**
 * PatientManager is a business layer object which interacts with the PatientManagerDao. The
 * role of the PatientManager is to enforce business rule logic and delegate data layer manipulation
 * to the PatientManagerDao.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */

public class PatientManager extends CtdbManager {
	
	/**
     * Creates a Patient in the CTDB System. All patient data that
     * is stored into the database will be encrypted.
     *
     * @param patient The patient to create
     * @throws DuplicateObjectException thrown if the patient already exists in the system
     *                                  based on the unique constraints
     * @throws CtdbException            thrown if any other errors occur while processing
     */
    public void createPatient(Patient patient,int protocolId) throws DuplicateObjectException, CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            PatientManagerDao dao = PatientManagerDao.getInstance(conn);

            AddressDao addressDao = AddressDao.getInstance(conn);
	        
            // create patient's address
            int addressId = addressDao.createAddress(patient.getHomeAddress());
            patient.getHomeAddress().setId(addressId);
            
            //check if subjectId already exist in current protocol then error out with exception
            if( dao.subjectIDexistInProtocol(patient, protocolId)) {
         	   throw new DuplicateObjectException("Subject with the Subject ID " + patient.getSubjectId() + " already exists in the system.");
         	
            }

            // create patient
			if (!dao.isGuidExistsProtocol(patient, protocolId)) {
				dao.createPatient(patient, protocolId);
				// assign patient to protocols
				for (PatientProtocol protocol : patient.getProtocols()) {
					dao.assignPatientToProtocol(patient, protocol);
				}
			}

            conn.commit();   
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to create Patient: " + e.getMessage(), e);
        }
        catch (DuplicateObjectException doe) {
            throw doe;
        }
        catch (CtdbException ce) {
            throw ce;
        }
        catch (Exception e) {
            throw new CtdbException("Unknown error occurred while creating patient: " + e.getMessage(), e);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }
    
    public boolean isGuidExists(Patient patient) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection();
            boolean isExists =  PatientManagerDao.getInstance(conn).isGuidExists(patient);
    
            return isExists;
        }
        finally {
            this.close(conn);
        }
    }
    
	public boolean isGuidExistsProtocol(Patient patient, int protocolId) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			boolean isExists = PatientManagerDao.getInstance(conn).isGuidExistsProtocol(patient, protocolId);

			return isExists;
		} finally {
			this.close(conn);
		}
	}


	/**
     * Creates a Patient visit 
     * 
     */
    public void createPatientVisit(PatientVisit pv) throws DuplicateObjectException, CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            PatientManagerDao dao = PatientManagerDao.getInstance(conn);
            dao.createPatientVisit(pv);
            conn.commit();
        }
        catch (SQLException e) {
            this.rollback(conn);
            throw new CtdbException("Unable to create Patient Visit: " + e.getMessage(), e);
        }
        catch (DuplicateObjectException doe) {
            this.rollback(conn);
            throw doe;
        }
        catch (CtdbException ce) {
            this.rollback(conn);
            throw ce;
        }
        catch (Exception e) {
            this.rollback(conn);
            throw new CtdbException("Unknown error occurred while creating patient Visit: " + e.getMessage(), e);
        }
        finally {
            this.close(conn);
        }
    }

	/**
     * Creates a Patient visit 
     * 
     */
    public void updatePatientVisit(PatientVisit pv) throws DuplicateObjectException, CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            PatientManagerDao dao = PatientManagerDao.getInstance(conn);
            dao.updatePatientVisit(pv);
            this.commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }

    public List<String> deletePatients(String[] subjectIds, int userId) throws ObjectNotFoundException, DuplicateObjectException, CtdbException {
        Connection conn = null;
    	List<String> deletedList = new ArrayList<String>();

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            PatientManagerDao dao = PatientManagerDao.getInstance(conn);
            
            for(String subjectId: subjectIds){
            		Patient p  =  dao.getPatient( subjectId);
            		deletedList.add(p.getFirstName() +  " " + p.getLastName());
            		p.setDeleteFlag(CtdbConstants.DATABASE_DELETE_FLAG_TRUE);
            		p.setUpdatedBy(userId);
            		dao.softDeletePatient(p);
        		conn.commit();
        	}
        }
        catch(SQLException e)
        {
        	this.rollback(conn);
        	throw new CtdbException("Unable to delete ptient: " + e.getMessage(), e);
        }
        catch(ObjectNotFoundException onfe)
        {
        	this.rollback(conn);
        	throw onfe;
        }
        catch(DuplicateObjectException doe)
        {
        	this.rollback(conn);
        	throw doe;
        }
        catch(FileUploadException fue)
        {
        	this.rollback(conn);
        	throw fue;
        }
        catch(CtdbException ce)
        {
        	this.rollback(conn);
        	throw ce;
        }
        catch(Exception e)
        {
        	this.rollback(conn);
        	throw new CtdbException("Unknown error occurred while deleting the patient: " + e.getMessage(), e);
        }
        finally
        {
        	this.close(conn);
        }
        return deletedList;
    }

    public List<Integer> deletePatientVisits(String[] visitDateIds) throws ObjectNotFoundException, DuplicateObjectException, CtdbException {
        Connection conn = null;
    	List<Integer> deletedList = new ArrayList<Integer>();

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            PatientManagerDao dao = PatientManagerDao.getInstance(conn);
            
            for( int i = 0; i < visitDateIds.length; i++ ) {
            	int visitDateId = Integer.parseInt(visitDateIds[i]);
            	
            	if ( visitDateId != 0 ) { System.out.println("PatientManager.java deletePatientVisits() visitDateId: "+visitDateId);	
            		dao.deletePatientVisit(visitDateId);
            		dao.deletePatientVisitPrepopValues(visitDateId);
            		deletedList.add(new Integer(visitDateId));
            	}
            	
        		conn.commit();
        	}
        }
        catch(SQLException e)
        {
        	this.rollback(conn);
        	throw new CtdbException("Unable to delete patient visit: " + e.getMessage(), e);
        }
        catch(ObjectNotFoundException onfe)
        {
        	this.rollback(conn);
        	throw onfe;
        }
        catch(DuplicateObjectException doe)
        {
        	this.rollback(conn);
        	throw doe;
        }
        catch(FileUploadException fue)
        {
        	this.rollback(conn);
        	throw fue;
        }
        catch(CtdbException ce)
        {
        	this.rollback(conn);
        	throw ce;
        }
        catch(Exception e)
        {
        	this.rollback(conn);
        	throw new CtdbException("Unknown error occurred while deleting the patient visit: " + e.getMessage(), e);
        }
        finally
        {
        	this.close(conn);
        }
        return deletedList;
    }

    /**
     * Updates a Patient in the CTDB System. All patient data that
     * is stored in the database will be encrypted. If the patient's
     * first name or last name is changed the patient will be versioned
     * for an audit trail. Versioning only begins once the patient
     * has been associated with a form being administered.
     *
     * @param patient The patient to update
     * @throws ObjectNotFoundException  thrown if the patient does not exist in the system
     * @throws DuplicateObjectException thrown if the patient already exists in the system
     *                                  based on the unique constraints
     * @throws CtdbException            thrown if any other errors occur while processing
     *                                  TODO Account for if a patient is part of an administered form first before versioning
     */
    public void updatePatient(Patient patient,int protocolId) throws ObjectNotFoundException, DuplicateObjectException, CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            PatientManagerDao dao = PatientManagerDao.getInstance(conn);
            AddressDao aDao = AddressDao.getInstance(conn);
            // Change the the default for versioning from false to true;
            boolean versionPatient = true;
            
            // update patient's address
            aDao.updateAddress(patient.getHomeAddress());
            // update patient
            dao.updatePatient(patient, versionPatient,protocolId);
            
            // assign patient to protocols
            // assign created by to current user for new inserts
            patient.setCreatedBy(patient.getUpdatedBy());
            //TODO: verify why this loop is involved here. Is the CURRENT Protocol is the only protocol target to handle? 
            for ( PatientProtocol protocol : patient.getProtocols() ) {
                /*
                 * XL: There are 3 predefined categories for patient role in CTDB, and it is never a null value when a patient is populated. 
                 * Now The patient role is consolidated into patient group, its value is always null. 
                 */
                
                if (protocol.isAssociated() == false) {
                    // remove record, it has been unassociated
                    dao.removePatientToProtocolAssignment(patient, protocol.getId());
                } 
                else if(dao.hasPatientProtocol(protocol.getId(), patient.getId()) ){
                     dao.updatePatientToProtocolAssignment(patient, protocol);
               	}
              	else{
               		dao.assignPatientToProtocol(patient, protocol);
                }
            }

            conn.commit();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to update Patient: " + e.getMessage(), e);
        }
        catch (ObjectNotFoundException onfe) {
            throw onfe;
        }
        catch (DuplicateObjectException doe) {
            throw doe;
        }
        catch (CtdbException ce) {
            throw ce;
        }
        catch (Exception e) {
            throw new CtdbException("Unknown error occurred while updating patient: " + e.getMessage(), e);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
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
	public Patient getPatient(String patientId, long protocolId) throws ObjectNotFoundException, CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection();
            Patient p =  PatientManagerDao.getInstance(conn).getPatient(patientId);
            AddressDao aDao = AddressDao.getInstance(conn);
            p.setHomeAddress(aDao.getAddress(p.getHomeAddress().getId()));
            AttachmentManager am = new AttachmentManager();
			p.setAttachments(am.getProtocolAttachments(AttachmentManager.FILE_PATIENT, p.getId(), protocolId));
            return p;
        }
        finally {
            this.close(conn);
        }
    }
    


    public void removePatientToProtocolAssignment(Patient p, long protooclId) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection();
            PatientManagerDao.getInstance(conn).removePatientToProtocolAssignment(p, protooclId);
        }
        finally {
            this.close(conn);
        }
    }
    
    public PatientProtocol getPatientProtocalByPatientAndProtocol(long patientId, long protocolId) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection();
            PatientProtocol patientProtocol = PatientManagerDao.getInstance(conn).getPatientProtocalByPatientAndProtocol(patientId, protocolId);
            return patientProtocol;
        }
        finally {
            this.close(conn);
        }
        
    }

        public void recordReasonsAudit( PatientChangeTracker pct, Patient p) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            PatientManagerDao.getInstance(conn).recordReasonsAudit(pct, p);
        }
        finally {
            this.close(conn);
        }
    }

    public List<AuditDetail> getPatientChanges(String subjectId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return PatientManagerDao.getInstance(conn).getPatientChanges(subjectId);
        }
        finally {
            this.close(conn);
        }
    }

    public void updatePatientDisplayValues(Patient p) throws ObjectNotFoundException, CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection();
            PatientManagerDao.getInstance(conn).updatePatientDisplayValues(p);
        }
        finally {
            this.close(conn);
        }
    }
    
    
    
    public boolean  doesPatientVisitExistWithThisToken(String token) throws  CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection();
            return PatientManagerDao.getInstance(conn).doesPatientVisitExistWithThisToken(token);
        }
        finally {
            this.close(conn);
        }
    }
    
    


    public Patient getPatientByNIHNumForAuditLog(String nihNum) throws ObjectNotFoundException, CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection();
            Patient p =  PatientManagerDao.getInstance(conn).getPatientBySubjectId(nihNum);
            AddressDao aDao = AddressDao.getInstance(conn);
            p.setHomeAddress(aDao.getAddress(p.getHomeAddress().getId()));
            return p;
        }
        finally {
            this.close(conn);
        }
    }
    
    public Patient getPatientBySubjectNumber(String subjectNumber) throws CtdbException {
    	Connection conn = null;
    	try {
    		conn = CtdbManager.getConnection();
    		return PatientManagerDao.getInstance(conn).getPatientBySubjectId(subjectNumber);
    	}
    	finally {
    		this.close(conn);
    	}
    }

    /**
     * Retrieves the Patient by patient last name and first name. The last and first names are encrypted.
     *
     * @param name patient's full name
     * @return The Patient object
     * @throws CtdbException Thrown if any errors occur during the process.
     */
    public Patient getPatientByNameForAuditLog(String name) throws ObjectNotFoundException, CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection();
            String lastName = null;
            String firstName = null;
            StringTokenizer names = new StringTokenizer(name, ",");
            if (names.hasMoreTokens()) {
                lastName = names.nextToken().trim();
            }
            if (names.hasMoreTokens()) {
                firstName = names.nextToken().trim();
            }
            return PatientManagerDao.getInstance(conn).getPatientByNameForAuditLog(lastName, firstName);
        }
        finally {
            this.close(conn);
        }
    }



    public List<Patient> getMinimalPatients(PatientResultControl prc) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection();
            
            return PatientManagerDao.getInstance(conn).getMinimalPatients(prc);
        }
        finally {
            this.close(conn);
        }
    }
    
    
    public List<Patient> getMinPatientsByProtocolIdSiteIds(long protocolid, List<Integer> siteIds) throws CtdbException {
    	Connection conn = null;
    	
    	try {
    		conn = CtdbManager.getConnection();
    		
    		return PatientManagerDao.getInstance(conn).getMinimalPatientsBySiteIds(protocolid,siteIds);
    	}
    	finally {
    		this.close(conn);
    	}
    }

    public  void updatePatientSubjectNumbers (List<Patient> patients, Protocol pro) {
        for ( Patient p : patients ) {
            p.setDisplayLabel(p.getDisplayLabel(pro.getPatientDisplayType(), pro.getId()));
        }
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
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection();
            return PatientManagerDao.getInstance(conn).getPatients(prc);
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Method to get patient visits 
     * @param formId
     * @return
     * @throws ObjectNotFoundException
     * @throws CtdbException
     */
    public List<PatientVisit> getPatientVisits(PatientVisitResultControl pvrc,long protocolId) throws ObjectNotFoundException, CtdbException {
    	Connection conn = null;
    	 try {
             conn = CtdbManager.getConnection();
             conn.setAutoCommit(false);
             PatientManagerDao dao = PatientManagerDao.getInstance(conn);
             List<PatientVisit> pvList = dao.getPatientVisits(pvrc,protocolId);
             return pvList;
         } catch (Exception e) {
             throw new CtdbException("Unable to get list of patient visits form for the ID: "
                     + pvrc.getPatientId()
                     + e.getMessage(),
                     e);
         } finally {
             this.close(conn);
         }
     }
    
    /**
     * Method to get patient visits 
     * @param siteIds 
     * @param roleId 
     * @param formId
     * @return
     * @throws ObjectNotFoundException
     * @throws CtdbException
     */
    public List<PatientVisit> getPatientVisitsByUserSiteIds(PatientVisitResultControl pvrc,long protocolId, List<Integer> siteIds) throws ObjectNotFoundException, CtdbException {
    	Connection conn = null;
    	 try {
             conn = CtdbManager.getConnection();
             conn.setAutoCommit(false);
             PatientManagerDao dao = PatientManagerDao.getInstance(conn);
             List<PatientVisit> pvList = dao.getPatientVisitsUserSiteIds(pvrc,protocolId,siteIds);
             return pvList;
         } catch (Exception e) {
             throw new CtdbException("Unable to get list of patient visits form for the ID: "
                     + pvrc.getPatientId()
                     + e.getMessage(),
                     e);
         } finally {
             this.close(conn);
         }
     }
    
    /**
     * Method to get patient visits 
     * @param formId
     * @return
     * @throws ObjectNotFoundException
     * @throws CtdbException
     */
    public List<PatientVisit> getMonthPatientVisits(PatientVisitResultControl pvrc) throws ObjectNotFoundException, CtdbException {
    	Connection conn = null;
    	 try {
             conn = CtdbManager.getConnection();
             PatientManagerDao dao = PatientManagerDao.getInstance(conn);
             List<PatientVisit> pvList = dao.getMonthPatientVisits(pvrc);
             return pvList;
         } catch (Exception e) {
             throw new CtdbException("Unable to get list of patient visits form for the ID: "
                     + pvrc.getPatientId()
                     + e.getMessage(),
                     e);
         } finally {
             this.close(conn);
         }
     }
    
    public List<PatientVisit> getMonthPatientVisitsBySites(PatientVisitResultControl pvrc, List<Integer> siteIds) throws ObjectNotFoundException, CtdbException {
    	Connection conn = null;
    	 try {
             conn = CtdbManager.getConnection();
             PatientManagerDao dao = PatientManagerDao.getInstance(conn);
             List<PatientVisit> pvList = dao.getMonthPatientVisitsBySites(pvrc, siteIds);
             return pvList;
         } catch (Exception e) {
             throw new CtdbException("Unable to get list of patient visits form for the ID: "
                     + pvrc.getPatientId()
                     + e.getMessage(),
                     e);
         } finally {
             this.close(conn);
         }
     }
    
    /**
     * Method to get patient visits 
     * @param formId
     * @return
     * @throws ObjectNotFoundException
     * @throws CtdbException
     */
    public PatientVisit getPatientVisit(long visitDateId) throws ObjectNotFoundException, CtdbException {
    	Connection conn = null;
    	 try {
             conn = CtdbManager.getConnection();
             PatientManagerDao dao = PatientManagerDao.getInstance(conn);
             
             PatientVisit pv = dao.getPatientVisit(visitDateId);
             return pv;
         } catch (Exception e) {
             throw new CtdbException("Unable to get list of patient visit form for the ID: "
                     + visitDateId
                     + e.getMessage(),
                     e);
         } finally {
             this.close(conn);
         }
     }
    
    
    
    
    
    /**
     * Method to get patient visits 
     * @param formId
     * @return
     * @throws ObjectNotFoundException
     * @throws CtdbException
     */
    public PatientVisit getPatientVisit(String token) throws ObjectNotFoundException, CtdbException {
    	Connection conn = null;
    	 try {
             conn = CtdbManager.getConnection();
             PatientManagerDao dao = PatientManagerDao.getInstance(conn);
             
             PatientVisit pv = dao.getPatientVisit(token);
             return pv;
         } catch (Exception e) {
             throw new CtdbException("Unable to get list of patient visit form for the token : "
                     + token
                     + e.getMessage(),
                     e);
         } finally {
             this.close(conn);
         }
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
    public List<Patient> getPatientVersions(String subjectId) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection();
            return PatientManagerDao.getInstance(conn).getPatientVersions(subjectId);
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * returns a list of administeredForms that the patient is currently involved in for Active Data Entry
     *
     * @param protocolId
     * @param patientId
     * @return
     * @throws CtdbException
     */

    public List<AdministeredForm> hasActiveDataEntry(long protocolId, long patientId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return PatientManagerDao.getInstance(conn).hasActiveDataEntry(protocolId, patientId);
        }
        finally {
            this.close(conn);
        }
    }
    
    public boolean isAdministered(String patientId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return PatientManagerDao.getInstance(conn).isAdministered(patientId);
        }
        finally {
            this.close(conn);
        }
    }

    public boolean hasProtocolData(long protocolId, long patientId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return PatientManagerDao.getInstance(conn).hasProtocolData(protocolId, patientId);
        }
        finally {
            this.close(conn);
        }
    }
    
    public boolean isSubjectReusedInOtherProtocolWithDifferentIdinPP(long protocolId, String patientId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return PatientManagerDao.getInstance(conn).isSubjectReusedInOtherProtocolWithDifferentIdinPP(protocolId, patientId);
        }
        finally {
            this.close(conn);
        }
    }


    
    public boolean hasFutureVisits(String subjectId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return PatientManagerDao.getInstance(conn).hasFutureVisits(subjectId);
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * isActiveForProtocol() test to determine if a given user is active for a given
     * protocol
     *
     * @param protocolId the id of the protocol
     * @param patientId  the id of the patient
     * @return true if the user is active false if not
     * @throws CtdbException
     */
    public boolean isActiveForProtocol(long protocolId, String subjectId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            Patient patient = PatientManagerDao.getInstance(conn).getPatient(subjectId);
            List<PatientProtocol> protocols = patient.getProtocols();
            
            if (protocols.size() > 0) {
                for ( PatientProtocol protocol : protocols ) {
                    if (protocol.getId() == protocolId) {
                        return protocol.isActive();
                    }
                }
            } else {
                return false;
            }
            return false;
        }
        finally {
            this.close(conn);
        }
    }

    public void updatePatientProtocolOrder(long protocolId, String[] patientIds) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            for (int i = 0; i < patientIds.length; i++) {
                PatientManagerDao.getInstance(conn).updatePatientProtocolOrder(protocolId, patientIds[i], i);
            }
        }
        finally {
            this.close(conn);
        }
    }

    public void autoSortPatients(Protocol p) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            if (p.getPatientDisplayType() == CtdbConstants.PATIENT_DISPLAY_NAME) {
                PatientManagerDao.getInstance(conn).autoSortPatients(p.getId(), true, false);
            } else if (p.getPatientDisplayType() == CtdbConstants.PATIENT_DISPLAY_SUBJECT) {
                PatientManagerDao.getInstance(conn).autoSortPatients(p.getId(), false, true);

            }else {
                PatientManagerDao.getInstance(conn).autoSortPatients(p.getId(), false, false);
            }
        }
        finally {
            this.close(conn);
        }
    }

    public void sortAddedPatient(int patientId, Protocol p) throws CtdbException {

        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            if (p.isUsePatientName()) {
                PatientManagerDao.getInstance(conn).sortAddedPatient(patientId, p.getId(), true);
            } else {
                PatientManagerDao.getInstance(conn).sortAddedPatient(patientId, p.getId(), false);
            }
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Gets the id of the protocol patient role 'N/A' or creates if it does not exist.
     * used when associating an existing patinet to a protocol
     *
     * @param protocolId
     * @return
     * @throws CtdbException
     */

    public int getDefaultPatientRole(int protocolId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return PatientRoleDao.getInstance(conn).getDefaultPatientRole(protocolId);
        }
        finally {
            this.close(conn);
        }

    }

    /**
     * Retrieves all patients for a given protocol that do not have a subject number assigned
     * @param protocol the protocol to search in
     * @return a map of patient id's and patient protocols
     * @throws CtdbException the exception
     */
    public Map<Integer, PatientProtocol> getPatientsWithoutSubjectNumberByProtocol(Protocol protocol) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return PatientManagerDao.getInstance(conn).getPatientsWithoutSubjectNumberByProtocol(protocol);
        }
        finally {
            this.close(conn);
        }
    }

   /**
     * Retrieves the next incremental subject number within a given protocol
     * @param protocol the protocol to search in
     * @return the next subject number value
     * @throws CtdbException the exception
     */
    public int findNextSubjectNumber(Protocol protocol) throws CtdbException
    {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return PatientManagerDao.getInstance(conn).findNextSubjectNumber(protocol);
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Updates the patient protocol, specifically the subject number
     * @param patientProtocols a map of the patient id's and patient protocols
     * @throws CtdbException the exception
     */
    public void updatePatientProtocol(Map<Integer, PatientProtocol> patientProtocols) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            PatientManagerDao.getInstance(conn).updatePatientProtocol(patientProtocols);
        }
        finally {
            this.close(conn);
        }
    }

    /**
     * Checks the system to see if the patient visit is already scheduled for a given patient, visit date, and visit type.
     * 
     * @param patientId - The ID of the patient.
     * @param visitDate - The date of the scheduled visit.
     * @param vType - The ID of the associated visit type.
     * @param visitId - The ID of the associated patient visit.
     * @return True if and only if there is already a patient visit scheduled in the system.
     * @throws CtdbException If a database error occurs while searching for a patient visit.
     */
	public boolean visitExists(long patientId, Date visitDate, long vType, long visitId)  throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
        	return PatientManagerDao.getInstance(conn).visitExists(patientId, visitDate, vType, visitId);
		}
    	finally {
    		this.close(conn);
    	}
	}

	/**
	 * Creates a Patient visit prepop values
	 * 
	 */
	public void createPatientVisitPrepopValues(PatientVisit pv, List<PatientVisitPrepopValue> pvPrepopValueList)
			throws DuplicateObjectException, CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			PatientManagerDao dao = PatientManagerDao.getInstance(conn);
			
			dao.createPatientVisitPrepopValues(pv, pvPrepopValueList);
			this.commit(conn);
		}
		finally {
			this.rollback(conn);
			this.close(conn);
		}
	}
    
	public List<PatientVisitPrepopValue> getPvPrepopValueByPvId(long visitDateId) throws CtdbException {
    	Connection conn = null;
    	List<PatientVisitPrepopValue> pvPrepopValueList = null;
    	
    	try {
            conn = CtdbManager.getConnection();
            PatientManagerDao dao = PatientManagerDao.getInstance(conn);
            pvPrepopValueList = dao.getPvPrepopValueByPvId(visitDateId);
        }
    	finally {
            this.close(conn);
        }
    	
    	return pvPrepopValueList;
	}
	
	/**
	 * Gets a list of pre-population value objects from the database for the given visit. The list of pre-population values will also be
	 * compared with the list of pre-population data elements. When the lengths of both lists are not equal, the missing pre-population
	 * value objects (with empty values) will be appended to the final pre-population value list. The returned list will also be sorted
	 * by the data element title.
	 * 
	 * @param pv - The patient visit object used to lookup the pre-population lists.
	 * @return A list of pre-population values that exists in the database along with any values that have been left out from a prior save.
	 * @throws CtdbException When there is an error looking up any of the pre-population lists.
	 */
	public List<PatientVisitPrepopValue> getCompletePrePopValList(PatientVisit pv) throws CtdbException {
		Connection conn = null;
    	List<PatientVisitPrepopValue> pvPrepopValueList = null;
    	
    	try {
    		conn = CtdbManager.getConnection();
            PatientManagerDao patDao = PatientManagerDao.getInstance(conn);
            ProtocolManagerDao protoDao = ProtocolManagerDao.getInstance(conn);
            List<PrepopDataElement> prePopDeList = protoDao.getPrepopDEsForInterval(pv.getIntervalId(), false);
            
            pvPrepopValueList = patDao.getPvPrepopValueByPvId(pv.getId());
            
            // Check if there needs to be additional pre-pop values to add to the list.
            if ( pvPrepopValueList.size() != prePopDeList.size() ) {
    			// Add in the missing pre-population fields.
    			List<PatientVisitPrepopValue> newPrePopVals = new ArrayList<PatientVisitPrepopValue>();
    			
    			for ( PrepopDataElement prePopDe : prePopDeList ) {
    				boolean found = false;
    				
    				for ( PatientVisitPrepopValue prePopVal : pvPrepopValueList ) {
    					if ( prePopDe.getShortName().equals(prePopVal.getPrepopDataElement().getShortName()) ) {
    						found = true;
    						break;
    					}
    				}
    				
    				// Create an empty pre-population value object, if not found in the pre-pop value list.
    				if ( !found ) {
    					PatientVisitPrepopValue newPrePopVal = new PatientVisitPrepopValue();
    					
    					newPrePopVal.setPrepopDataElement(prePopDe);
    					newPrePopVal.setPrepopDataElementId(prePopDe.getId());
    					newPrePopVal.setIntervalId(pv.getIntervalId());
    					newPrePopVal.setPatientVisitId(pv.getId());
    					
    					// Add new pre-pop value to the new object list.
    					newPrePopVals.add(newPrePopVal);
    				}
    			}
    			
    			// Append new pre-population values to the main list, if any.
    			if ( !newPrePopVals.isEmpty() ) {
    				pvPrepopValueList.addAll(newPrePopVals);
    			}
    		}
    	}
    	finally {
    		this.close(conn);
    	}
    	
    	// Sort the pre-population value list by data element title.
        Collections.sort(pvPrepopValueList, new Comparator<PatientVisitPrepopValue>() {
			@Override
			public int compare(PatientVisitPrepopValue o1, PatientVisitPrepopValue o2) {
				return o1.getPrepopDataElement().getTitle().compareToIgnoreCase(o2.getPrepopDataElement().getTitle());
			}
        });
    	
    	return pvPrepopValueList;
	}
	
	/**
     * Creates a Patient visit prepop values
     * 
     */
    public void updatePatientVisitPrepopValues(PatientVisit pv, List<PatientVisitPrepopValue> pvPrepopValueList) throws DuplicateObjectException, CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            PatientManagerDao dao = PatientManagerDao.getInstance(conn);
            
            dao.deletePatientVisitPrepopValues(pv.getId());
            dao.createPatientVisitPrepopValues(pv, pvPrepopValueList);
            this.commit(conn);
        }
        finally {
        	this.rollback(conn);
            this.close(conn);
        }
    }
    
	public PatientVisit getPatientVisit(long patientid, Date visitDate, long intervalid) throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		PatientVisit pv = null;

		try {
			conn = CtdbManager.getConnection();
			PatientManagerDao dao = PatientManagerDao.getInstance(conn);
			pv = dao.getPatientVisit(patientid, visitDate, intervalid);
		}
		finally {
			this.close(conn);
		}

		return pv;
	}
	
	/**
	 * Retrieve the list of intervals along with the scheduler status.
	 *
	 * @param protocolId
	 * @return  the list of IntervalScheduleDisplay 
	 * @throws  CtdbException if any errors occur while processing
	 */
	public List<IntervalScheduleDisplay> getIntervalsWithSchedulerStatus(long patientid, long protocolId) throws CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return PatientManagerDao.getInstance(conn).getIntervalsWithSchedulerStatus(patientid, protocolId);
		}
		finally
		{
			this.close(conn);
		}
	}


	
	public List<Patient> getPatientListByProtocol(Integer protocolId) throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		List<Patient> patList = new ArrayList<Patient>();

		try {
			conn = CtdbManager.getConnection();
			PatientManagerDao dao = PatientManagerDao.getInstance(conn);
			patList.addAll(dao.getPatientListByProtocol(protocolId));
		}
		finally {
			this.close(conn);
		}

		return patList;
	}
	
	public List<Patient> getPatientListForAllProtocol() throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		List<Patient> patList = new ArrayList<Patient>();

		try {
			conn = CtdbManager.getConnection();
			PatientManagerDao dao = PatientManagerDao.getInstance(conn);
			patList.addAll(dao.getPatientListForAllProtocol());
		}
		finally {
			this.close(conn);
		}

		return patList;
	}
	
	public List<Patient> getPatientsInDataCollectionByProtocol(Integer protocolId) throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		List<Patient> patList = new ArrayList<Patient>();

		try {
			conn = CtdbManager.getConnection();
			PatientManagerDao dao = PatientManagerDao.getInstance(conn);
			patList.addAll(dao.getPatientsInDataCollectionByProtocol(protocolId));
		}
		finally {
			this.close(conn);
		}

		return patList;
	}
	
	public List<Patient> getPatientListByProtocolAndSite(Integer protocolId, Integer siteId) throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		List<Patient> patList = new ArrayList<Patient>();
		
		try {
			conn = CtdbManager.getConnection();
			PatientManagerDao dao = PatientManagerDao.getInstance(conn);
			patList.addAll(dao.getPatientListByProtocolAndSite(protocolId, siteId));
		}
		finally {
			this.close(conn);
		}
		return patList;
	}

	public List<Patient> getPatientListByProtocolAndSites(Integer protocolId, Integer[] siteIds)
			throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		List<Patient> patList = new ArrayList<Patient>();

		try {
			conn = CtdbManager.getConnection();
			PatientManagerDao dao = PatientManagerDao.getInstance(conn);
			patList.addAll(dao.getPatientListByProtocolAndSites(protocolId, siteIds));
		} finally {
			this.close(conn);
		}

		return patList;
	}
	
	public List<Patient> getPatientListByProtocolIdAndSiteIds(Integer protocolId, List<Integer> siteIds) throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		List<Patient> patList = new ArrayList<Patient>();
		
		try {
			conn = CtdbManager.getConnection();
			PatientManagerDao dao = PatientManagerDao.getInstance(conn);
			patList.addAll(dao.getPatientListByProtocolIdAndSiteIds(protocolId, siteIds));
		}
		finally {
			this.close(conn);
		}
		return patList;
	}
	
	public boolean checkIfAnyPatProtoHasRandomization(int protocolId) throws CtdbException {
		Connection conn = null;
		boolean hasRandomization = false;

		try {
			conn = CtdbManager.getConnection();
			PatientManagerDao dao = PatientManagerDao.getInstance(conn);
			hasRandomization = dao.checkIfAnyPatProtoHasRandomization(protocolId);
		} finally {
			this.close(conn);
		}
		return hasRandomization;
	}

}
