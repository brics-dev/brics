package gov.nih.nichd.ctdb.patient.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.patient.domain.PatientCohort;

/**
 * Created by IntelliJ IDEA.
 * User: matt
 * Date: Nov 5, 2009
 * Time: 3:28:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class PatientCohortDao extends CtdbDao {


    /**
     * Method to retrieve the instance of the PatientManagerDao.
     *
     * @return PatientManagerDao data object
     */
    public static synchronized PatientCohortDao getInstance() {
        return new PatientCohortDao();
    }

    /**
     * Method to retrieve the instance of the PatientManagerDao. This method
     * accepts a Database Connection to be used internally by the DAO. All
     * transaction management will be handled at the BusinessManager level.
     *
     * @param conn Database connection to be used within this data object
     * @return PatientManagerDao data object
     */
    public static synchronized PatientCohortDao getInstance(Connection conn) {
        PatientCohortDao dao = new PatientCohortDao();
        dao.setConnection(conn);
        return dao;
    }


    public void createCohort(PatientCohort pg) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            //int id = this.getSequenceValue(conn, "patientrole_seq");
            StringBuffer sql = new StringBuffer("insert into patientcohort values (DEFAULT, ?, ?,?,0)");
            //stmt = this.conn.prepareStatement(sql.toString());
            //stmt.setInt(1, id);
            stmt.setInt(1, pg.getProtocolId());
            stmt.setString(2, pg.getName());
            stmt.setString(3, pg.getDescription());
            stmt.executeUpdate();
            pg.setId(getInsertId(conn, "patientrole_seq"));

        } catch (SQLException sqle) {

                throw new CtdbException("Failure creating patient cohort : " + sqle.getMessage(), sqle);
        } finally {
            this.close(stmt);
            this.close(rs);
        }

    }


    public void updatePatientCohort(PatientCohort p) throws CtdbException {
        PreparedStatement stmt = null;
        try {

                StringBuffer sql = new StringBuffer("update patientcohort set name = ?, description=?  where cohortid = ?");
                stmt = this.conn.prepareStatement(sql.toString());
                stmt.setString(1, p.getName());
            stmt.setString(2, p.getDescription());
                stmt.setLong(3, p.getId());
                stmt.executeUpdate();


        } catch (SQLException sqle) {
            throw new CtdbException("Failure updating patient cohort: " + sqle.getMessage(), sqle);
        } finally {
            this.close(stmt);
        }
    }


    public List getAssociatedPatients (PatientCohort pg) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList results = new ArrayList();
        try {
            StringBuffer sql = new StringBuffer("select nihrecordnumber");
            sql.append(" from patient p, patientprotocol pp ");
            sql.append(" where pp.cohortid = ? ");
            sql.append(" and p.patientid = pp.patientid ");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, pg.getId());

            rs = stmt.executeQuery();
             while (rs.next()) {
                 results.add(rs.getString(1));
             }
            return results;


        } catch (SQLException sqle) {
            throw new CtdbException("Failure retreiving patinets associated togroup : " + sqle.getMessage(), sqle);
        } finally {
            this.close(stmt);
            this.close(rs);
        }


    }

    public void orderCohorts (int[] ids) throws CtdbException {
         PreparedStatement stmt = null;
           try {
               stmt = this.conn.prepareStatement("update patientCohort set orderval = ? where cohortid = ? ");
               for (int i = 0; i < ids.length; i++ ){
                   stmt.setInt(1, i+1);
                   stmt.setLong(2, ids[i]);
                   stmt.addBatch();
               }
               stmt.executeBatch();

           } catch (SQLException sqle) {
               throw new CtdbException("Failure ordering patient cohorts : " + sqle.getMessage(), sqle);
           } finally {
               this.close(stmt);
           }
       }


       public void deleteCohort(PatientCohort pr) throws CtdbException {
        PreparedStatement stmt = null;
        try {

            StringBuffer sql = new StringBuffer("delete FROM patientCohort where cohortid = ? and protocolid = ?");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, pr.getId());
            stmt.setLong(2, pr.getProtocolId());
            stmt.executeUpdate();

        } catch (SQLException sqle) {
            throw new CtdbException("Failure deleting  patient Cohort : " + sqle.getMessage(), sqle);
        } finally {
            this.close(stmt);
        }

    }

      public List<PatientCohort> getPatientCohorts (int protocolId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<PatientCohort> results = new ArrayList<PatientCohort>();
        try {
            StringBuffer sql = new StringBuffer("select * from patientCohort where protocolid= ? order by orderval");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, protocolId);

            rs = stmt.executeQuery();
             while (rs.next()) {
                 PatientCohort pg = new PatientCohort();
                 pg.setId(rs.getInt("cohortid"));
                 pg.setName(rs.getString("name"));
                 pg.setDescription(rs.getString("description"));
                 pg.setProtocolId(protocolId);
                 results.add(pg);
             }
            return results;


        } catch (SQLException sqle) {
            throw new CtdbException("Failure retreiving patientcohorts: " + sqle.getMessage(), sqle);
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

    public int getDefaultPatientCohort (int protocolId) throws CtdbException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.conn.prepareStatement("select cohortid from patientcohort where protocolid = ? and upper (name) = 'N/A'");
            stmt.setLong(1, protocolId);
            rs = stmt.executeQuery();
            if (rs.next()){
                return rs.getInt(1);
            } else {
               PatientCohort pr = new PatientCohort();
                pr.setProtocolId(protocolId);
                pr.setName("N/A");
                this.createCohort(pr);
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
