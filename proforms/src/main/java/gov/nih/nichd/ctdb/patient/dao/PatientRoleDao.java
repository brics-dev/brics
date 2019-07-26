package gov.nih.nichd.ctdb.patient.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.patient.domain.PatientRole;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Nov 3, 2006
 * Time: 10:06:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class PatientRoleDao extends CtdbDao {


    /**
     * Method to retrieve the instance of the PatientManagerDao.
     *
     * @return PatientManagerDao data object
     */
    public static synchronized PatientRoleDao getInstance() {
        return new PatientRoleDao();
    }

    /**
     * Method to retrieve the instance of the PatientManagerDao. This method
     * accepts a Database Connection to be used internally by the DAO. All
     * transaction management will be handled at the BusinessManager level.
     *
     * @param conn Database connection to be used within this data object
     * @return PatientManagerDao data object
     */
    public static synchronized PatientRoleDao getInstance(Connection conn) {
        PatientRoleDao dao = new PatientRoleDao();
        dao.setConnection(conn);
        return dao;
    }


    public void createPatientRole(PatientRole pr) throws CtdbException {
        createRole(pr);
    }

    public void updatePatientRole(PatientRole pr) throws CtdbException {
        PreparedStatement stmt = null;
        try {

                StringBuffer sql = new StringBuffer("update protocolpatientrole set name = ? where xpatientroleId = ?");
                stmt = this.conn.prepareStatement(sql.toString());
                stmt.setString(1, pr.getName());
                stmt.setLong(2, pr.getId());
                stmt.executeUpdate();


        } catch (SQLException sqle) {
            throw new CtdbException("Failure creating protocol role association : " + sqle.getMessage(), sqle);
        } finally {
            this.close(stmt);
        }
    }



    private void createRole(PatientRole pr) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            //int roleId = this.getSequenceValue(conn, "patientrole_seq");
            StringBuffer sql = new StringBuffer("insert into protocolpatientrole (xpatientroleid, name, protocolid) values (DEFAULT, ?, ?)");
            stmt = this.conn.prepareStatement(sql.toString());
            //stmt.setInt(1, roleId);
            stmt.setString(1, pr.getName());
            stmt.setInt(2, pr.getProtocolId());
            stmt.executeUpdate();
            pr.setId(getInsertId(conn, "protocolpatientrole_seq"));

        } catch (SQLException sqle) {

                throw new CtdbException("Failure creating role via xpatinetRole : " + sqle.getMessage(), sqle);
        } finally {
            this.close(stmt);
            this.close(rs);
        }

    }

    public void orderRoles (int[] ids) throws CtdbException {
      PreparedStatement stmt = null;
        try {
            stmt = this.conn.prepareStatement("update protocolpatientrole set orderval = ? where xpatientroleid = ? ");
            for (int i = 0; i < ids.length; i++ ){
                stmt.setInt(1, i+1);
                stmt.setLong(2, ids[i]);
                stmt.addBatch();
            }
            stmt.executeBatch();

        } catch (SQLException sqle) {
            throw new CtdbException("Failure ordering patient roles : " + sqle.getMessage(), sqle);
        } finally {
            this.close(stmt);
        }
    }


    public List getAssociatedPatients(PatientRole pr) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList results = new ArrayList();
        try {
            StringBuffer sql = new StringBuffer("select nihrecordnumber");
            sql.append(" from patient p, patientprotocol pp ");
            sql.append(" where pp.xpatientroleid = ? ");
            sql.append(" and p.patientid = pp.patientid ");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, pr.getId());

            rs = stmt.executeQuery();
             while (rs.next()) {
                 results.add(rs.getString(1));
             }
            return results;


        } catch (SQLException sqle) {
            throw new CtdbException("Failure retreiving patinets associated to role : " + sqle.getMessage(), sqle);
        } finally {
            this.close(stmt);
            this.close(rs);
        }


    }

    public void removeRoleAssociation(PatientRole pr) throws CtdbException {
        PreparedStatement stmt = null;
        try {
            stmt = this.conn.prepareStatement("delete FROM patientgroupinclusions where groupid = ? ");
            stmt.setLong(1, pr.getId());
            stmt.executeUpdate();
            
            StringBuffer sql = new StringBuffer("delete FROM protocolpatientrole where xpatientroleid = ? and protocolid = ?");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, pr.getId());
            stmt.setLong(2, pr.getProtocolId());
            stmt.executeUpdate();

        } catch (SQLException sqle) {
            throw new CtdbException("Failure deleting protocol patient role : " + sqle.getMessage(), sqle);
        } finally {
            this.close(stmt);
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

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.conn.prepareStatement("select xpatientroleid from protocolpatientrole where protocolid = ? and upper (name) = 'N/A'");
            stmt.setLong(1, protocolId);
            rs = stmt.executeQuery();
            if (rs.next()){
                return rs.getInt(1);
            } else {
               PatientRole pr = new PatientRole();
                pr.setProtocolId(protocolId);
                pr.setName("N/A");
                this.createPatientRole(pr);
                return pr.getId();
            }


        } catch (SQLException sqle) {
            throw new CtdbException("Failure obtaining default  patient role : " + sqle.getMessage(), sqle);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
    }

    public List getPatientRoles (int protocolId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList results = new ArrayList();
        try {
            StringBuffer sql = new StringBuffer();
            sql.append("select xpatientroleid, name, protocolid from protocolpatientrole where  protocolid = ? order by orderval");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, protocolId);

            rs = stmt.executeQuery();
             while (rs.next()) {
                 PatientRole pg = new PatientRole();
                 pg.setId(rs.getInt("xpatientroleid"));
                 pg.setName(rs.getString("name"));
                 pg.setProtocolId(protocolId);
                 results.add(pg);
             }
            return results;


        } catch (SQLException sqle) {
            throw new CtdbException("Failure retreiving patientgroups: " + sqle.getMessage(), sqle);
        } finally {
            this.close(stmt);
            this.close(rs);
        }


    }

}
