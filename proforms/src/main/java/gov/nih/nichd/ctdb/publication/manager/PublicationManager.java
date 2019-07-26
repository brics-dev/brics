package gov.nih.nichd.ctdb.publication.manager;

import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.publication.domain.Publication;
import gov.nih.nichd.ctdb.publication.dao.PublicationManagerDao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: matt
 * Date: Apr 18, 2011
 * Time: 10:00:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationManager extends CtdbManager {

    public void createPublication (Publication p) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
            PublicationManagerDao.getInstance(conn).createPublication(p);
        } finally {
            this.close(conn);
        }
    }

    public void updatePublication (Publication p) throws CtdbException, SQLException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
            PublicationManagerDao.getInstance(conn).updatePublication(p);
        } finally {
            this.close(conn);
        }
    }

    public List<Publication> getPublications(int protocolId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return PublicationManagerDao.getInstance(conn).getPublications(protocolId);
        } finally {
            this.close(conn);
        }
    }


    public Publication getPublication(int id) throws CtdbException {
          Connection conn = null;
          try {
              conn = CtdbManager.getConnection();
              return PublicationManagerDao.getInstance(conn).getPublication(id);
          }  finally {
              this.close(conn);
          }
      }

    public void deletepublication (int pubId) throws CtdbException {
        Connection conn = null;
     try {
         conn= CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
         PublicationManagerDao.getInstance(conn).deletePublication(pubId);
         conn.commit();
     } catch (SQLException sqle) {
               throw new CtdbException("Failure deleting publication : " + sqle, sqle);

     } finally{
         this.close(conn);
     }
  }
}
