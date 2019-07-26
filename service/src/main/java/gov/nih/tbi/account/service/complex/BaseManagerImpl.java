
package gov.nih.tbi.account.service.complex;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.service.BaseManager;
import gov.nih.tbi.commons.service.util.MailEngine;
import gov.nih.tbi.commons.ws.HashMethods;

import java.io.Serializable;
import java.util.Collection;

import javax.mail.MessagingException;

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

    private static final long serialVersionUID = -3856535486237089289L;

    static Logger logger = Logger.getLogger(BaseManagerImpl.class);

    @Autowired
    MailEngine mailEngine;

    @Autowired
    RoleHierarchy roleHierarchy;
    
	@Autowired
	protected ModulesConstants modulesConstants;

    /**
     * @inheritDoc
     */
    public Boolean hasRole(Account account, RoleType role)
    {

        Collection<? extends GrantedAuthority> authorities = null;

        authorities = roleHierarchy.getReachableGrantedAuthorities(AccountDetailService.getAuthorities(account));

        if (authorities.contains(new SimpleGrantedAuthority(role.getName())))
        {
            return true;
        }

        return false;
    }

    /**
     * @inheritDoc
     */
    public void sendEmail(Account account, String subject, String messageText) throws MessagingException
    {

        sendEmail(account, subject, messageText, null);

    }

    /**
     * @inheritDoc
     */
    public void sendEmail(Account account, String subject, String messageText, String from) throws MessagingException
    {

        try
        {
            mailEngine.sendMail(subject, messageText, from, account.getUser().getEmail());
        }
        catch (MessagingException e)
        {
            logger.error("There was an exception in the baseManagerImpl sendEmail(): " + e.getLocalizedMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void sendMail(String subject, String htmlMessage, String from, String... to) throws MessagingException
    {

        try
        {
            mailEngine.sendMail(subject, htmlMessage, from, to);
        }
        catch (MessagingException e)
        {
            logger.error("There was an exception in the baseManagerImpl sendEmail(): " + e.getLocalizedMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public String getHash2(Account account)
    {

        String userName = "anonymous";
        String password = "";

        if (account != null && account.getUserName() != null && !(account.getUserName().equals("anonymous")))
        {
            userName = account.getUserName();
            password = HashMethods.convertFromByte(account.getPassword());
        }

        return HashMethods.getServerHash(userName, password);
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
