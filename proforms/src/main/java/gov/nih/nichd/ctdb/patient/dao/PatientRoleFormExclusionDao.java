package gov.nih.nichd.ctdb.patient.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Nov 27, 2006
 * Time: 10:42:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class PatientRoleFormExclusionDao extends CtdbDao {


        /**
     * Method to retrieve the instance of the PatientManagerDao.
     *
     * @return PatientManagerDao data object
     */
    public static synchronized PatientRoleFormExclusionDao getInstance() {
        return new PatientRoleFormExclusionDao();
    }


    /**
     * Method to retrieve the instance of the PatientManagerDao. This method
     * accepts a Database Connection to be used internally by the DAO. All
     * transaction management will be handled at the BusinessManager level.
     *
     * @param conn Database connection to be used within this data object
     * @return PatientManagerDao data object
     */
    public static synchronized PatientRoleFormExclusionDao getInstance(Connection conn) {
        PatientRoleFormExclusionDao dao = new PatientRoleFormExclusionDao();
        dao.setConnection(conn);
        return dao;
    }

    public Map getExcludedForms (int roleId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.conn.prepareStatement("select f.formid, f.name from patientroleformexclusions pe, form f where f.formid = pe.formid and xpatientroleid = ? ");
            stmt.setLong(1, roleId);
            rs = stmt.executeQuery();
            HashMap results = new HashMap();
            while (rs.next()){
                results.put(rs.getString(1), rs.getString(2));
            }
            return results;
        }
        catch(SQLException sqle) {
            throw new CtdbException("Failure getting excluded forms for role " + roleId + " - " + sqle.getMessage());
        } finally{
            this.close(rs);
            this.close(stmt);
        }
    }

    public void excludeForms (int roleId, String[] formIds) throws CtdbException {

        PreparedStatement stmt = null;
        try {
            stmt = this.conn.prepareStatement("delete FROM patientroleformexclusions where xpatientroleid = ? ");
            stmt.setLong(1, roleId);
            stmt.executeUpdate();
            if (formIds != null && formIds.length > 0) {
            for (int i = 0; i < formIds.length; i++) {
                stmt=this.conn.prepareStatement("insert into patientroleformexclusions (formid, xpatientroleid) values ("+formIds[i]+ ", "+roleId+")");
                stmt.executeUpdate();
            }
            }
            //stmt.executeBatch();
        }
        catch(SQLException sqle) {
            throw new CtdbException("Failure getting excluded forms for role " + roleId + " - " + sqle.getMessage());
        } finally{
            this.close(stmt);
        }
    }



}
