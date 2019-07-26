package gov.nih.nichd.ctdb.patient.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.patient.domain.PatientGroup;

/**
 * Created by IntelliJ IDEA.
 * User: matt
 * Date: Nov 5, 2009
 * Time: 2:38:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class PatientGroupDao extends CtdbDao {


    /**
     * Method to retrieve the instance of the PatientManagerDao.
     *
     * @return PatientManagerDao data object
     */
    public static synchronized PatientGroupDao getInstance() {
        return new PatientGroupDao();
    }

    /**
     * Method to retrieve the instance of the PatientManagerDao. This method
     * accepts a Database Connection to be used internally by the DAO. All
     * transaction management will be handled at the BusinessManager level.
     *
     * @param conn Database connection to be used within this data object
     * @return PatientManagerDao data object
     */
    public static synchronized PatientGroupDao getInstance(Connection conn) {
        PatientGroupDao dao = new PatientGroupDao();
        dao.setConnection(conn);
        return dao;
    }


    public void createGroup(PatientGroup pg) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            //int id = this.getSequenceValue(conn, "patientrole_seq");
            StringBuffer sql = new StringBuffer("insert into patientgroup values (DEFAULT, ?, ?,?,0)");
            stmt = this.conn.prepareStatement(sql.toString());
            //stmt.setInt(1, id);
            stmt.setLong(1, pg.getProtocolId());
            stmt.setString(2, pg.getName());
            stmt.setString(3, pg.getDescription());
            stmt.executeUpdate();
            pg.setId(getInsertId(conn, "patientgroup_seq"));

        } catch (SQLException sqle) {

                throw new CtdbException("Failure creating patient group : " + sqle.getMessage(), sqle);
        } finally {
            this.close(stmt);
            this.close(rs);
        }

    }


    public void updatePatientGroup(PatientGroup p) throws CtdbException {
        PreparedStatement stmt = null;
        try {

                StringBuffer sql = new StringBuffer("update patientgroup set name = ?, description=?  where groupid = ?");
                stmt = this.conn.prepareStatement(sql.toString());
                stmt.setString(1, p.getName());
                stmt.setString(2, p.getDescription());
                stmt.setLong(3, p.getId());
                stmt.executeUpdate();


        } catch (SQLException sqle) {
            throw new CtdbException("Failure updating patient group: " + sqle.getMessage(), sqle);
        } finally {
            this.close(stmt);
        }
    }

    public List<String> getAssociatedPatients(PatientGroup pg) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<String> results = new ArrayList<String>();
        
        try {
            String sql = "select " + getDecryptionFunc("p.nihrecordnumber") + " as nihrecordnumber from patient p, patientprotocol pp " +
            			 "where pp.groupid = ? and p.patientid = pp.patientid ";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, pg.getId());
            rs = stmt.executeQuery();
            
            while (rs.next()) {
            	results.add(rs.getString("nihrecordnumber"));
            }
        }
        catch (SQLException sqle) {
        	// Check the sql state
			if ( sqle.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + sqle.getMessage(), sqle);
			}
			else {
				throw new CtdbException("Failure retreiving patinets associated togroup : " + sqle.getMessage(), sqle);
			}
        }
        finally {
            this.close(stmt);
            this.close(rs);
        }
        
        return results;
    }
    
    public void orderGroups (int[] ids) throws CtdbException {
         PreparedStatement stmt = null;
           try {
               stmt = this.conn.prepareStatement("update patientgroup set orderval = ? where groupid = ? ");
               for (int i = 0; i < ids.length; i++ ){
                   stmt.setInt(1, i+1);
                   stmt.setLong(2, ids[i]);
                   stmt.addBatch();
               }
               stmt.executeBatch();

           } catch (SQLException sqle) {
               throw new CtdbException("Failure ordering patient groups : " + sqle.getMessage(), sqle);
           } finally {
               this.close(stmt);
           }
       }

    public void deleteGroup(PatientGroup pr) throws CtdbException {
        PreparedStatement stmt = null;
        try {
            stmt = this.conn.prepareStatement("delete FROM patientgroupinclusions where groupid = ? ");
            stmt.setLong(1, pr.getId());
            stmt.executeUpdate();
            
            StringBuffer sql = new StringBuffer("delete FROM patientgroup where groupid = ? and protocolid = ?");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, pr.getId());
            stmt.setLong(2, pr.getProtocolId());
            stmt.executeUpdate();

        } catch (SQLException sqle) {
            throw new CtdbException("Failure deleting  patient group : " + sqle.getStackTrace(), sqle);
        } finally {
            this.close(stmt);
        }

    }
    
    public PatientGroup PatientGroup(int groupId,int protocolId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;   	
    	
    	PatientGroup pg = new PatientGroup();
    	
        try {
            StringBuffer sql = new StringBuffer("select * from patientgroup where groupid = ?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, groupId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                pg.setName(rs.getString("name"));
                pg.setDescription(rs.getString("description"));
                pg.setProtocolId(protocolId);
            } else {
                throw new CtdbException("Unable to get form group, no record foundzord");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new CtdbException("Unable to get form group" + e.getMessage(), e);
        } finally {
            this.close(stmt);
            this.close(rs);
        }   	
    	return pg;
    	
    }

     public List<PatientGroup> getPatientGroups (int protocolId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<PatientGroup> results = new ArrayList<PatientGroup>();
        try {
            StringBuffer sql = new StringBuffer("select * from patientgroup where protocolid = ? order by orderval");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, protocolId);

            rs = stmt.executeQuery();
             while (rs.next()) {
                 PatientGroup pg = new PatientGroup();
                 pg.setId(rs.getInt("groupid"));
                 pg.setName(rs.getString("name"));
                 pg.setDescription(rs.getString("description"));
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

    /**
     *  Gets the id of the protocol patient role 'N/A' or creates if it does not exist.
     * used when associating an existing patinet to a protocol
     * @param protocolId
     * @return
     * @throws CtdbException
     */

    public int getDefaultPatientGroup (int protocolId) throws CtdbException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.conn.prepareStatement("select groupid from patientgroup where protocolid = ? and upper (name) = 'N/A'");
            stmt.setLong(1, protocolId);
            rs = stmt.executeQuery();
            if (rs.next()){
                return rs.getInt(1);
            } else {
               PatientGroup pr = new PatientGroup();
                pr.setProtocolId(protocolId);
                pr.setName("N/A");
                this.createGroup(pr);
                return pr.getId();
            }


        } catch (SQLException sqle) {
            throw new CtdbException("Failure obtaining default  patient role : " + sqle.getMessage(), sqle);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
    }
}
