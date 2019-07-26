package gov.nih.nichd.ctdb.patient.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.form.domain.Form;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Nov 27, 2006
 * Time: 10:42:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class PatientGroupFormInclusionDao extends CtdbDao {


        /**
     * Method to retrieve the instance of the PatientManagerDao.
     *
     * @return PatientManagerDao data object
     */
    public static synchronized PatientGroupFormInclusionDao getInstance() {
        return new PatientGroupFormInclusionDao();
    }


    /**
     * Method to retrieve the instance of the PatientManagerDao. This method
     * accepts a Database Connection to be used internally by the DAO. All
     * transaction management will be handled at the BusinessManager level.
     *
     * @param conn Database connection to be used within this data object
     * @return PatientManagerDao data object
     */
    public static synchronized PatientGroupFormInclusionDao getInstance(Connection conn) {
        PatientGroupFormInclusionDao dao = new PatientGroupFormInclusionDao();
        dao.setConnection(conn);
        return dao;
    }

    public List getInclusionForms (int groupId) throws CtdbException {
        List forms = new ArrayList();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuffer sql = new StringBuffer("select f.formid, f.name from patientgroupinclusions pi, form f where f.formid = pi.formid and groupid = ? ");


            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, groupId);
            rs = stmt.executeQuery();
            while (rs.next()) {
            	Form form= new Form();
            	form.setId(rs.getInt("formid"));
                form.setName(rs.getString("name"));
                forms.add(form);
            }
            return forms;
        } catch (SQLException e) {
            throw new CtdbException("Unable to get groups for form" + e.getStackTrace(), e);
        } finally {
            this.close(stmt);
            this.close(rs);
        }    	
    }
    public void updateInclusionForms (int groupId, String[] formIds) throws CtdbException {

        PreparedStatement stmt = null;
        try {
            stmt = this.conn.prepareStatement("delete FROM patientgroupinclusions where groupid = ? ");
            stmt.setLong(1, groupId);
            stmt.executeUpdate();
            if (formIds != null && formIds.length > 0) {
	            for (int i = 0; i < formIds.length; i++) {
	                stmt=this.conn.prepareStatement("insert into patientgroupinclusions (formid, groupid) values ("+formIds[i]+ ", "+groupId+")");
	                stmt.executeUpdate();
	            }
            }
            //stmt.executeBatch();
        }
        catch(SQLException sqle) {
            throw new CtdbException("Failure update included forms for role " + groupId + " - " + sqle.getStackTrace());
        } finally{
            this.close(stmt);
        }
    }

    public void createInclusionForms(int groupId, String[] formIds) throws CtdbException {
        PreparedStatement stmt = null;
        try {
            if (formIds != null && formIds.length > 0) {
	            for (int i = 0; i < formIds.length; i++) {
	                stmt=this.conn.prepareStatement("insert into patientgroupinclusions (formid, groupid) values (?, ?)");
	                stmt.setLong(1, Long.valueOf(formIds[i]));
	                stmt.setLong(2, groupId);
	                stmt.executeUpdate();
	            }
            }
            //stmt.executeBatch();
        }
        catch(SQLException sqle) {
            throw new CtdbException("Failure create included forms for role " + groupId + " - " + sqle.getStackTrace());
        } finally{
            this.close(stmt);
        }

    }    
    
}
