
package gov.nih.tbi.service.impl;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.pojo.authentication.AccountDetailService;
import gov.nih.tbi.service.BaseManager;

import java.io.Serializable;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * This is the base manager implemenation that uses hibernate
 * 
 * @author Nimesh Patel
 * 
 */
public class BaseManagerImpl implements BaseManager, Serializable
{

    private static final long serialVersionUID = -3856535484237089289L;

    static Logger logger = Logger.getLogger(BaseManagerImpl.class);

    @Autowired
    RoleHierarchy roleHierarchy;

    /**
     * @inheritDoc
     */
    public Boolean hasRole(Account account, RoleType role)
    {

        Collection<? extends GrantedAuthority> authorities = null;

        authorities = roleHierarchy.getReachableGrantedAuthorities(AccountDetailService.getAuthorities(account
                .getAccountRoleList()));

        if (authorities.contains(new SimpleGrantedAuthority(role.getName())))
        {
            return true;
        }

        return false;
    }

    /**
     * Escape the characters '_' '%' and '\' with '\' character for use in a hibernate Restrictions.iLike() statement.
     * 
     * @param input
     *            : The string to be escaped, can be null
     * @return
     */
    public String escapeForILike(String input)
    {

        // null check for this function.
        if (input == null)
            return null;

        for (int i = 0; i < input.length(); i++)
        {
            char c = input.charAt(i);
            if (c == '\\' || c == '%' || c == '_')
            {
                input = input.substring(0, i) + '\\' + input.substring(i, input.length());
                i++;
            }
        }
        return input;
    }

}
