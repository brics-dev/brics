package gov.nih.nichd.ctdb.contacts.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PSQLException;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.InvalidRemovalException;
import gov.nih.nichd.ctdb.contacts.domain.ExternalContact;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.nichd.ctdb.util.domain.Address;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Jun 26, 2007
 * Time: 9:03:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExternalContactDao extends CtdbDao {

    private ExternalContactDao () {}

    public static synchronized ExternalContactDao getInstance () {
        return new ExternalContactDao();
    }

    public static synchronized ExternalContactDao getInstance(Connection conn)  {
        ExternalContactDao dao = new ExternalContactDao();
        dao.setConnection(conn);
        return dao;
    }


    public void createExternalContact (ExternalContact ec) throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;
        try {
            StringBuffer sb = new StringBuffer(100);
            sb.append("insert into externalcontact ");
            sb.append (" ( externalcontactid,  xexternalcontacttypeid, protocolid, name, organization, ");
            sb.append(" phone1, phone2, email, addressid, xinstituteid, createdby, createddate, updatedby, updateddate,studysiteid )");
            sb.append(" values ( ");
            sb.append(" DEFAULT, ?, ?, ?, ?, ");
            sb.append(" ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)");

            stmt = this.conn.prepareStatement(sb.toString());
            stmt.setLong(1, Long.valueOf(ec.getContactType().getId()));
            stmt.setLong(2, Long.valueOf(ec.getProtocolId()));
            stmt.setString(3, ec.getName().trim());
            stmt.setString(4, ec.getOrganization());
            stmt.setString(5, ec.getPhone1());
            stmt.setString(6, ec.getPhone2());
            stmt.setString(7, ec.getEmailAddress());
            stmt.setLong(8, Long.valueOf(ec.getAddress().getId()));
            stmt.setLong(9, Long.valueOf(ec.getInstitute().getId()));
            stmt.setLong(10, Long.valueOf(ec.getCreatedBy()));
            stmt.setLong(11, Long.valueOf(ec.getUpdatedBy()));
            stmt.setLong(12, Long.valueOf(ec.getStudySiteId()));
            stmt.executeUpdate();
            
            ec.setId(this.getInsertId(conn, "externalcontact_seq"));
        }
        catch ( PSQLException psqle )
        {
        	if ( psqle.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate")) )
        	{
        		throw new DuplicateObjectException("A contact with the same name already exists: " + psqle.getMessage(), psqle);
        	}
        	else
        	{
        		throw new CtdbException("Unable to create a contact: " + psqle.getMessage(), psqle);
        	}
        }
        catch (SQLException sqle)
        {
            if (sqle.getMessage().toUpperCase().indexOf("UK") > -1) {
                throw new DuplicateObjectException ("Unique key name violated creating external contact ");
            } else {
                throw new CtdbException("faliure creating external contact : " + sqle.getMessage(), sqle);
            }
        }
        finally
        {
            this.close(stmt);
        }
    }

    public void updateExternalContact (ExternalContact ec) throws DuplicateObjectException, CtdbException {
            PreparedStatement stmt = null;
        try {
            StringBuffer sb = new StringBuffer(100);
            sb.append("update externalcontact set ");
            sb.append (" xexternalcontacttypeid = ?, name = ?, organization =?, ");
            sb.append(" phone1 = ?, phone2 =?, email =?, xinstituteid = ?, updatedby = ?, updateddate = CURRENT_TIMESTAMP,studysiteid=? ");
            sb.append(" where externalcontactid = ? ");

            stmt = this.conn.prepareStatement(sb.toString());
            stmt.setLong(1, Long.valueOf(ec.getContactType().getId()));
            stmt.setString(2, ec.getName());
            stmt.setString(3, ec.getOrganization());
            stmt.setString(4, ec.getPhone1());
            stmt.setString(5, ec.getPhone2());
            stmt.setString(6, ec.getEmailAddress());
            stmt.setLong(7, Long.valueOf(ec.getInstitute().getId()));
            stmt.setLong(8, Long.valueOf(ec.getUpdatedBy()));
            stmt.setLong(9, Long.valueOf(ec.getStudySiteId()));
            stmt.setLong(10, Long.valueOf(ec.getId()));
            stmt.executeUpdate();

        }
        catch ( PSQLException psqle )
        {
        	if ( psqle.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate")) )
        	{
        		throw new DuplicateObjectException("A contact with the same name already exists: " + psqle.getMessage(), psqle);
        	}
        	else
        	{
        		throw new CtdbException("Unable to update a contact: " + psqle.getMessage(), psqle);
        	}
        }
        catch (SQLException sqle)
        {
            throw new CtdbException("faliure updating external contact : " + sqle.getMessage(), sqle);
        }
        finally
        {
            this.close(stmt);
        }
    }

    public void deleteExternalContact(long ecId)  throws CtdbException {
            PreparedStatement stmt = null;
        try {
            StringBuffer sb = new StringBuffer(50);
            sb = new StringBuffer("delete FROM externalcontact where externalcontactid = ? ");
            stmt = this.conn.prepareStatement(sb.toString());
            stmt.setLong(1, ecId);
            stmt.executeUpdate();

        } catch (SQLException sqle) {
            if (sqle.getMessage().toUpperCase().indexOf("FK") > -1) {
                throw new InvalidRemovalException("Constraint violated deleting exteranl contact");
            } else {
                throw new CtdbException("faliure deleting external contact : " + sqle.getMessage(), sqle);
            }
        } finally{
            this.close(stmt);
        }
    }
    
    public ExternalContact getExternalContact(long contactId) throws CtdbException
    {
    	PreparedStatement stmt = null;
    	ResultSet rs = null;
    	ExternalContact ec = null;
    	
    	try
    	{
    		StringBuffer sb = new StringBuffer(100);
            sb.append("select ec.*, xinstitute.shortname institutename, xexternalcontacttype.name contactTypeName ");
            sb.append( " from  externalcontact ec, xinstitute, xexternalcontacttype where ec.externalcontactid = ? ");
            sb.append (" and ec.xinstituteid = xinstitute.xinstituteid ");
            sb.append (" and ec.xexternalcontacttypeid = xexternalcontacttype.xexternalcontacttypeid ");
            stmt = this.conn.prepareStatement(sb.toString());
            stmt.setLong(1, contactId);
            rs = stmt.executeQuery();
            rs.next();
            ec = rsToExternalContact(rs);
    	}
    	catch (SQLException sqle)
    	{
            throw new CtdbException("faliure getting external contact : " + sqle.getMessage(), sqle);
        } 
    	finally
    	{
            this.close(stmt);
            this.close(rs);
        }
    	
    	return ec;
    }

    public List<ExternalContact> getExternalContacts(long protocolId) throws CtdbException {
            PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuffer sb = new StringBuffer(100);
            sb.append("select ec.*, xinstitute.shortname institutename, xexternalcontacttype.name contactTypeName  ");
            sb.append( " from  externalcontact ec, xinstitute, xexternalcontacttype where ec.protocolid = ? ");
            sb.append (" and ec.xinstituteid = xinstitute.xinstituteid ");
            sb.append (" and ec.xexternalcontacttypeid = xexternalcontacttype.xexternalcontacttypeid ");

            stmt = this.conn.prepareStatement(sb.toString());
            stmt.setLong(1, protocolId);
            rs = stmt.executeQuery();
            List<ExternalContact> lst = new ArrayList<ExternalContact>();
            while(rs.next()) {
                lst.add(this.rsToExternalContact(rs));
            }
            return lst;
        } catch (SQLException sqle) {
            throw new CtdbException("faliure getting external contacts : " + sqle.getMessage(), sqle);
        } finally{
            this.close(stmt);
            this.close(rs);
        }
    }


    private final ExternalContact rsToExternalContact (ResultSet rs) throws SQLException {
        ExternalContact ec = new ExternalContact ();
        ec.setId(rs.getInt("externalcontactid"));
        ec.setContactType(new CtdbLookup(rs.getInt("xexternalcontacttypeid"), rs.getString("contactTypeName")));
        ec.setProtocolId(rs.getInt("protocolId"));
        ec.setName(rs.getString("name"));
        ec.setOrganization(rs.getString("organization"));
        ec.setPhone1(rs.getString("phone1"));
        ec.setPhone2(rs.getString("phone2"));
        ec.setEmailAddress(rs.getString("email"));
        ec.setInstitute(new CtdbLookup(rs.getInt("xinstituteId"), rs.getString("institutename")));
        ec.setCreatedBy(rs.getInt("createdby"));
        ec.setCreatedDate(rs.getDate("createddate"));
        ec.setUpdatedBy(rs.getInt("updatedby"));
        ec.setUpdatedDate(rs.getDate("updateddate"));
        ec.setStudySiteId(rs.getInt("studysiteid"));

        ec.setAddress(new Address(rs.getInt("addressid")));
        return ec;
    }
}
