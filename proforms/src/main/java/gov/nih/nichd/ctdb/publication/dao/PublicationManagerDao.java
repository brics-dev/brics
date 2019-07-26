package gov.nih.nichd.ctdb.publication.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.InvalidRemovalException;
import gov.nih.nichd.ctdb.publication.domain.Publication;

/**
 * Created by IntelliJ IDEA.
 * User: matt
 * Date: Apr 18, 2011
 * Time: 10:01:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationManagerDao extends CtdbDao {
    private PublicationManagerDao() {

       }

       /**
        * Method to retrieve the instance of the AttachmentDao.
        *
        * @return AttachmentDao data object
        */
       public static synchronized PublicationManagerDao getInstance() {
           return new PublicationManagerDao();
       }

       /**
        * Method to retrieve the instance of the AttachmentDao. This method
        * accepts a Database Connection to be used internally by the DAO. All
        * transaction management will be handled at the BusinessManager level.
        *
        * @param conn Database connection to be used within this data object
        * @return AttachmentDao data object
        */
       public static synchronized PublicationManagerDao getInstance(Connection conn) {
           PublicationManagerDao dao = new PublicationManagerDao();
           dao.setConnection(conn);
           return dao;
       }


    public void createPublication (Publication p) throws CtdbException {
          PreparedStatement stmt = null;
        try {

            StringBuffer sb = new StringBuffer(100);

            sb.append("insert into publication (publicationid, protocolid, publicationtypeid, title, authors, description, ");
            sb.append("createdby, createddate, updatedby, updateddate, documentid, url, pubmedid) ");
            sb.append("values (DEFAULT, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?, ?) ");
            stmt = this.conn.prepareStatement(sb.toString());
            stmt.setLong(1, p.getProtocolId());
            stmt.setLong(2, p.getPublicationType().getId());
            stmt.setString(3, p.getTitle());
            stmt.setString(4, p.getAuthors());
            stmt.setString(5, p.getDescription());
            stmt.setLong(6, p.getCreatedBy());
            stmt.setLong(7, p.getUpdatedBy());
            stmt.setLong(8, p.getDocumentId());
            stmt.setString(9, p.getUrl());
            stmt.setString(10, p.getPubmedId());
            stmt.executeUpdate();
            p.setId(getInsertId(conn, "publication_seq"));

        } catch (SQLException sqle) {
            throw new CtdbException("Failure creating publication : " + sqle.getMessage() + sqle);
        } finally {
            close(stmt);
        }
    
    }
    
    // modified by Ching Heng
    public void updatePublication (Publication p) throws CtdbException {
        PreparedStatement stmt = null;
              try {

                  StringBuffer sb = new StringBuffer(100);

                  sb.append("update publication set publicationtypeid = ?, title = ?, authors = ?, description = ?, ");
                  sb.append("updatedby = ?, updateddate = CURRENT_TIMESTAMP, documentid = ?, url = ?, pubmedId = ? ");
                  sb.append("where publicationid = ? ");
                  stmt = this.conn.prepareStatement(sb.toString());
                  stmt.setLong(1, p.getPublicationType().getId());
                  stmt.setString(2, p.getTitle());
                  stmt.setString(3, p.getAuthors());
                  stmt.setString(4, p.getDescription());
                  stmt.setLong(5, p.getUpdatedBy());
                  stmt.setLong(6, p.getDocumentId());
                  stmt.setString(7, p.getUrl());
                  stmt.setString(8, p.getPubmedId());
                  stmt.setLong(9, p.getId());
                  stmt.executeUpdate();
              } catch (SQLException sqle) {
                  throw new CtdbException("Failure updating publication : " + sqle.getMessage() + sqle);
              } finally {
                  close(stmt);
              }

    }


    public List<Publication> getPublications(int protocolId) throws CtdbException {
         PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Publication> l = new ArrayList<Publication>();
        try {
            StringBuffer sb = new StringBuffer(100);
            sb.append("select * from  publication where protocolid = ? ");
            stmt = this.conn.prepareStatement(sb.toString());
            stmt.setLong(1, protocolId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                l.add(this.rsToPublication(rs));
            }
            return l;
        } catch (SQLException sqle) {
            throw new CtdbException("Failure getting publications : " + sqle.getMessage() + sqle);
        } finally {
            close(stmt);
            close(rs);
        }

    }

    public Publication getPublication(int id) throws CtdbException {
         PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuffer sb = new StringBuffer(100);
            sb.append("select * from  publication where publicationid = ? ");
            stmt = this.conn.prepareStatement(sb.toString());
            stmt.setLong(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return this.rsToPublication(rs);
            } else {
                throw new CtdbException("Failure getting publication, does not exist");
            }
        } catch (SQLException sqle) {
            throw new CtdbException("Failure getting publication : " + sqle.getMessage() + sqle);
        } finally {
            close(stmt);
            close(rs);
        }
    }
    
    //added by Ching Heng 
    public void deletePublication(int pId) throws CtdbException {
        PreparedStatement stmt = null;
        try {
            StringBuffer sb = new StringBuffer(50);
            sb = new StringBuffer("delete FROM publication where publicationid=?");
            stmt = this.conn.prepareStatement(sb.toString());
            stmt.setLong(1, pId);
            stmt.executeUpdate();

        } catch (SQLException sqle) {
            if (sqle.getMessage().toUpperCase().indexOf("FK") > -1) {
                throw new InvalidRemovalException("Constraint violated deleting publication");
            } else {
                throw new CtdbException("faliure deleting publication : " + sqle.getMessage(), sqle);
            }
        } finally{
            this.close(stmt);
        }
    }
    //

    private Publication rsToPublication (ResultSet rs) throws SQLException {
        Publication p = new Publication();
        p.setId(rs.getInt("publicationid"));
        p.setProtocolId(rs.getInt("protocolid"));
        p.setPublicationType(new CtdbLookup(rs.getInt("publicationtypeid")));
        p.setAuthors(rs.getString("authors"));
        p.setTitle(rs.getString("title"));
        p.setDescription(rs.getString("description"));
        p.setCreatedBy(rs.getInt("createdby"));
        p.setCreatedDate(rs.getDate("createddate"));
        p.setUpdatedBy(rs.getInt("updatedby"));
        p.setUpdatedDate(rs.getDate("updateddate"));
        p.setDocumentId(rs.getInt("documentid"));
        p.setUrl(rs.getString("url"));
        p.setPubmedId(rs.getString("pubmedid"));
        return p;

    }

}
