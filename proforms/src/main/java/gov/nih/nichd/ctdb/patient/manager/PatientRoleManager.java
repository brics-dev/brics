package gov.nih.nichd.ctdb.patient.manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.patient.dao.PatientCohortDao;
import gov.nih.nichd.ctdb.patient.dao.PatientGroupDao;
import gov.nih.nichd.ctdb.patient.dao.PatientRoleDao;
import gov.nih.nichd.ctdb.patient.domain.PatientCategory;
import gov.nih.nichd.ctdb.patient.domain.PatientCohort;
import gov.nih.nichd.ctdb.patient.domain.PatientGroup;
import gov.nih.nichd.ctdb.patient.domain.PatientRole;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Nov 3, 2006
 * Time: 10:08:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class PatientRoleManager extends CtdbManager {


    public void update (PatientCategory pc) throws CtdbException {
        if (pc instanceof PatientRole ) {
            updatePatientRole((PatientRole) pc);
        } else if (pc instanceof PatientGroup ) {
            updatePatientGroup((PatientGroup) pc);
        } else if (pc instanceof PatientCohort ) {
            updatePatientCohort((PatientCohort) pc);
        }
    }


    public void add (PatientCategory pc) throws CtdbException {
        if (pc instanceof PatientRole ) {
            createPatientRole((PatientRole) pc);
        } else if (pc instanceof PatientGroup ) {
            createGroup((PatientGroup) pc);
        } else if (pc instanceof PatientCohort ) {
            createCohort((PatientCohort) pc);
        }
    }

    public List delete  (PatientCategory pc) throws CtdbException {
        if (pc instanceof PatientRole ) {
            return this.deleteRoleAssociation((PatientRole) pc);
        } else if (pc instanceof PatientGroup ) {
          return deleteGroup((PatientGroup) pc);
        } else if (pc instanceof PatientCohort ) {
            return deleteCohort((PatientCohort) pc);
        } else { throw new CtdbException("failure deleteing patient category "); }

    }

    public void order (PatientCategory pc, int[] ids) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            if (pc instanceof PatientRole ) {
                PatientRoleDao.getInstance(conn).orderRoles(ids);
            } else if (pc instanceof PatientGroup ) {
                PatientGroupDao.getInstance(conn).orderGroups(ids);
            } else if (pc instanceof PatientCohort ) {
                PatientCohortDao.getInstance(conn).orderCohorts (ids);
            }
        } finally {
            this.close(conn);
        }
    }

    public void createPatientRole (PatientRole pr) throws CtdbException{
        Connection conn = null;
        try {
         conn = CtdbManager.getConnection();
            conn.setAutoCommit(false);
            PatientRoleDao.getInstance(conn).createPatientRole(pr);
            conn.commit();
        } catch(SQLException  sqle) {
            throw new CtdbException("Failure creating patinet role : " + sqle.getMessage(), sqle);

        }
        finally{
            this.close(conn);
        }

    }

    public void updatePatientRole (PatientRole pr) throws CtdbException{
        Connection conn = null;
        try {
         conn = CtdbManager.getConnection();
            conn.setAutoCommit(false);
            PatientRoleDao.getInstance(conn).updatePatientRole(pr);
            conn.commit();
        } catch(SQLException  sqle) {
            throw new CtdbException("Failure updating patinet role : " + sqle.getMessage(), sqle);

        }
        finally{
            this.close(conn);
        }

    }

    public List deleteRoleAssociation (PatientRole pr) throws CtdbException{
        Connection conn = null;
        try {
         conn = CtdbManager.getConnection();
            conn.setAutoCommit(false);
            PatientRoleDao dao = PatientRoleDao.getInstance(conn);
            List lst =  new ArrayList();
            List associatedPatients = dao.getAssociatedPatients (pr);
            if (associatedPatients.size() > 0) {
                lst = associatedPatients;
            } else {
                dao.removeRoleAssociation(pr);
            }
            conn.commit();
            return lst;
        } catch(SQLException  sqle) {
            throw new CtdbException("Failure deleting role association : " + sqle.getMessage(), sqle);

        }
        finally{
            this.close(conn);
        }

    }

      public List deleteGroup (PatientGroup pr) throws CtdbException{
        Connection conn = null;
        try {
         conn = CtdbManager.getConnection();
            conn.setAutoCommit(false);
            PatientGroupDao dao = PatientGroupDao.getInstance(conn);
            List lst =  new ArrayList();
            List associatedPatients = dao.getAssociatedPatients (pr);
            if (associatedPatients.size() > 0) {
                lst = associatedPatients;
            } else {
                dao.deleteGroup(pr);
            }
            conn.commit();
            return lst;
        } catch(SQLException  sqle) {
            throw new CtdbException("Failure deleting patientGroup: " + sqle.getMessage(), sqle);

        }
        finally{
            this.close(conn);
        }
    }


      public List deleteCohort (PatientCohort pr) throws CtdbException{
        Connection conn = null;
        try {
         conn = CtdbManager.getConnection();
            conn.setAutoCommit(false);
            PatientCohortDao dao = PatientCohortDao.getInstance(conn);
            List lst =  new ArrayList();
            List associatedPatients = dao.getAssociatedPatients (pr);
            if (associatedPatients.size() > 0) {
                lst = associatedPatients;
            } else {
                dao.deleteCohort(pr);
            }
            conn.commit();
            return lst;
        } catch(SQLException  sqle) {
            throw new CtdbException("Failure deleting patientcohort: " + sqle.getMessage(), sqle);

        }
        finally{
            this.close(conn);
        }
    }
    


    public List getPatientRoles (int protocolId) throws CtdbException {

        Connection conn = null;
        try {
         conn = CtdbManager.getConnection();
            return PatientRoleDao.getInstance(conn).getPatientRoles(protocolId);
        } finally{
            this.close(conn);
        }
    }
    /**
     *  Gets the id of the protocol patient role 'N/A' or creates if it does not exist.
     * used when associating an existing patinet to a protocol
     * @param protocolId
     * @return
     * @throws CtdbException
     */

    public int getDefaultPatientRole (int protocolId) throws CtdbException {
    Connection conn = null;
        try {
         conn = CtdbManager.getConnection();
            return PatientRoleDao.getInstance(conn).getDefaultPatientRole(protocolId);
        }
        finally{
            this.close(conn);
        }

    }






    public void createGroup(PatientGroup pg) throws CtdbException {

        Connection conn = null;
        try {
         conn = CtdbManager.getConnection();
            conn.setAutoCommit(false);
            PatientGroupDao.getInstance(conn).createGroup(pg);
            conn.commit();
        } catch(SQLException  sqle) {
            throw new CtdbException("Failure creating patinet group : " + sqle.getMessage(), sqle);
        }
        finally{
            this.close(conn);
        }

    }

    public void updatePatientGroup (PatientGroup pg) throws CtdbException{
        Connection conn = null;
        try {
         conn = CtdbManager.getConnection();
            conn.setAutoCommit(false);
            PatientGroupDao.getInstance(conn).updatePatientGroup(pg);
            conn.commit();
        } catch(SQLException  sqle) {
            throw new CtdbException("Failure updating patinet role : " + sqle.getMessage(), sqle);

        }
        finally{
            this.close(conn);
        }

    }
    public PatientGroup getPatientGroup (int groupId,int protocolId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return PatientGroupDao.getInstance(conn).PatientGroup(groupId,protocolId);
        }
        catch (Exception e) {
            throw new CtdbException("Unable to create form group" + e.getMessage(), e);
        }
        finally {
            this.close(conn);
        }
    }
       
    

    public List<PatientGroup> getPatientGroups (int protocolId) throws CtdbException {

        Connection conn = null;
        try {
         conn = CtdbManager.getConnection();
            return PatientGroupDao.getInstance(conn).getPatientGroups(protocolId);
        } finally {
            this.close(conn);
        }
    }

    public List<PatientCohort> getPatientCohorts (int protocolId) throws CtdbException {

        Connection conn = null;
        try {
         conn = CtdbManager.getConnection();
            return PatientCohortDao.getInstance(conn).getPatientCohorts(protocolId);
        } finally {
            this.close(conn);
        }
    }


    public void createCohort(PatientCohort pg) throws CtdbException {

        Connection conn = null;
        try {
         conn = CtdbManager.getConnection();
            conn.setAutoCommit(false);
            PatientCohortDao.getInstance(conn).createCohort(pg);
            conn.commit();
        } catch(SQLException  sqle) {
            throw new CtdbException("Failure creating patinet cohort : " + sqle.getMessage(), sqle);
        }
        finally{
            this.close(conn);
        }

    }

    public void updatePatientCohort (PatientCohort pg) throws CtdbException{
        Connection conn = null;
        try {
         conn = CtdbManager.getConnection();
            conn.setAutoCommit(false);
            PatientCohortDao.getInstance(conn).updatePatientCohort(pg);
            conn.commit();
        } catch(SQLException  sqle) {
            throw new CtdbException("Failure updating patinet cohort : " + sqle.getMessage(), sqle);

        }
        finally{
            this.close(conn);
        }

    }


}
