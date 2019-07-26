package gov.nih.nichd.ctdb.patient.manager;

import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.patient.dao.PatientGroupFormInclusionDao;
import gov.nih.nichd.ctdb.patient.dao.PatientRoleFormExclusionDao;


import java.util.List;

import java.sql.Connection;


public class PatientGroupFormInclusionManager extends CtdbManager {

    public List getInclusionForms (int groupId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return PatientGroupFormInclusionDao.getInstance(conn).getInclusionForms(groupId);
        }
        catch (Exception e) {
        	 throw new CtdbException("Failure including forms : " + e.getStackTrace(), e);
        }
        finally {
            this.close(conn);
        }
        
    }


    public void updateInclusionForms (int groupId, String[] formIds) throws CtdbException {
        Connection conn = null;

       try {
           conn = CtdbManager.getConnection();
           PatientGroupFormInclusionDao.getInstance(conn).updateInclusionForms(groupId, formIds );

       } catch(CtdbException ce) {
    	   ce.printStackTrace();
           throw new CtdbException("Failure including forms : " + groupId + " -- " + ce.getStackTrace());
       }
       finally{
           this.close(conn);
       }
   }

    public void createInclusionForms (int groupId, String[] formIds) throws CtdbException {
        Connection conn = null;

       try {
           conn = CtdbManager.getConnection();
           PatientGroupFormInclusionDao.getInstance(conn).createInclusionForms(groupId, formIds );

       } catch(CtdbException ce) {
    	   ce.printStackTrace();
           throw new CtdbException("Failure including forms : " + groupId + " -- " + ce.getMessage());
       }
       finally{
           this.close(conn);
       }
   }

}
