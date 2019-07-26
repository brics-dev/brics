
package gov.nih.tbi.account.validators;

import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.service.AccountManager;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

/**
 * This validator prevents the reuse of passwords via rules described in the account manager
 * 
 * @author anjohn
 * 
 */
public class PasswordReuseValidator extends FieldValidatorSupport
{

    @Autowired
    AccountManager accountManager;

    /**
     * Method called by struts2 validation process
     */
    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

        Boolean isRequest = (Boolean) this.getFieldValue(PortalConstants.IS_REQUEST, object);

        // if this is a new request, then there will not be any previous passwords
        if (isRequest == null || !isRequest)
        {
            // track down the username if this is edit my profile page
            String userName = (String) this.getFieldValue("sessionAccountEdit.account.userName", object);

            Account account = null;

            if (userName == null)
            {
                // track down the username/email if this is password recovery
                userName = (String) this.getFieldValue("userOrEmail", object);

                account = accountManager.getAccountByUserName(userName);

                if (account == null)
                {
                    account = accountManager.getAccountByEmail(userName);
                }
            }
            else
            {
                account = accountManager.getAccountByUserName(userName);
            }

            if (accountManager.checkPassword(account, fieldValue))
            {
                // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
                // is displayed. However a field error must be added to indicate that something has failed.
                addFieldError(fieldName, "Password and confirmPassword do not match.");
            }
        }

    }
}
