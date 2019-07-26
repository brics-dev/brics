
package gov.nih.tbi.service;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.RoleType;

/**
 * Base Manager
 * 
 * @author Nimesh Patel
 * 
 */
public interface BaseManager {

    /**
     * Indicates if the account has been granted the role requested (no database hit)
     * 
     * @param account
     * @param role
     * @return
     */
    public Boolean hasRole(Account account, RoleType role);


    /**
     * Escape the characters '_' '%' and '\' with '\' character for use in a hibernate Restrictions.iLike() statement.
     * 
     * @param input
     *            : The string to be escaped, can be null
     * @return
     */
    public String escapeForILike(String input);

}
