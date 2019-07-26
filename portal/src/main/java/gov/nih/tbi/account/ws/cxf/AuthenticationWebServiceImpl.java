
package gov.nih.tbi.account.ws.cxf;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.AuthenticationWebService;
import gov.nih.tbi.account.ws.model.UserLogin;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.ws.HashMethods;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class AuthenticationWebServiceImpl implements AuthenticationWebService
{

    /***************************************************************************************************/

    private static Logger logger = Logger.getLogger(AuthenticationWebServiceImpl.class);

    /***************************************************************************************************/

    @Autowired
    protected AccountManager accountManager;

    /***************************************************************************************************/

    public Boolean authenticate(UserLogin user)
    {

        if (user.getUserName().equals("anonymous"))
        {
            return true;
        }

        if (getAccount(user) != null)
        {
            return true;
        }

        return false;
    }

    /***************************************************************************************************/

    protected Account getAccount(UserLogin user)
    {
        String username = user.getUserName();

        if(username.equals("anonymous")) {
            return null;
        }

        Account account = accountManager.getAccountByUserName(username);

        if (account == null)
        {
            throw new RuntimeException("User with name " + user.getUserName() + " does not exist.");
        }

        byte[] passwdBytes = account.getPassword();

        String passwd = HashMethods.convertFromByte(passwdBytes);
        
        logger.debug("Password from DB: " + passwd);

        String userHash1 = user.getHash1();
        boolean clientHash1Valid = HashMethods.validateClientHash(userHash1, username);

        if(clientHash1Valid) {
            String userHash2 = user.getHash2();
            String usernameServerHash = HashMethods.getServerHash(username);
            String userPasswdHash = HashMethods.getServerHash(username, passwd);
            boolean clientHash2Valid = HashMethods.validateClientHash(userHash2, usernameServerHash, userPasswdHash);

            if(clientHash2Valid) {
                return account;
            }
        }

        return null;
    }

}
