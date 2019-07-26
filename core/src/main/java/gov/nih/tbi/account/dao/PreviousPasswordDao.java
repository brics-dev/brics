
package gov.nih.tbi.account.dao;

import java.util.List;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.PreviousPassword;
import gov.nih.tbi.commons.dao.GenericDao;

/**
 * Dao abstaction for the PreviousPassword ORM
 * 
 * @author Andrew Johnson
 * 
 */
public interface PreviousPasswordDao extends GenericDao<PreviousPassword, Long>
{

    /**
     * Gets the last *count* passwords from the database for a given user
     * 
     * @param account
     * @param count
     * @return
     */
    public List<PreviousPassword> getLast(Account account, int count);

}
