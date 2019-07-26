
package gov.nih.tbi.account.validators;

import java.util.Arrays;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.service.AccountManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class CorrectPasswordValidator extends FieldValidatorSupport
{

    @Autowired
    AccountManager accountManager;

    /**
     * Method called by struts2 validation process This validator verifies the user has entered the correct password.
     */
    @Override
    public void validate(Object object) throws ValidationException
    {

        String password = (String) this.getFieldValue(this.getFieldName(), object);
        String username = (String) this.getFieldValue("username", object);
        if (password == null || username == null)
        {
            addFieldError(this.getFieldName(), "Null Parameter.");
            return;
        }

        // Verify that the password matches the username
        Account account = accountManager.getAccountByUserName(username);
        if (account == null)
        {
            addFieldError(this.getFieldName(), "Could not locate account for given username.");
            return;
        }
        if (!Arrays.equals(account.getPassword(), accountManager.hashPassword(account.getSalt() + password)))
        {
            addFieldError(this.getFieldName(), "Passwords to not match.");
            return;
        }

        // All good
    }

}
