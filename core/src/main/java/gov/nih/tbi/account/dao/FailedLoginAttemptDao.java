
package gov.nih.tbi.account.dao;

import java.util.Date;
import java.util.List;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.FailedLoginAttempt;
import gov.nih.tbi.commons.dao.GenericDao;

/**
 * 
 * Dao abstraction for the FailedLoginAttemp ORM
 * 
 * @author Andrew Johnson
 * 
 */
public interface FailedLoginAttemptDao extends GenericDao<FailedLoginAttempt, Long>
{

    /**
     * Gets all failed login attempts between the date given and today for a given account
     * 
     * @param account
     *            (Account) - only get the results for this account
     * @param startDate
     *            (Date) - only get results after this date
     * 
     * @return returns a list of the failed attempts
     */
    public List<FailedLoginAttempt> getAll(Account account, Date startDate);

    /**
     * Remove all previous failed login attempts from the list of failed login attempts
     * 
     * @param account
     *            (Account) - account of failed attempts to remove
     */
    public void removeAll(Account account);

}
