
package gov.nih.tbi.account.validators;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.dao.AccountDao;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.service.AccountManager;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class AccountUserOrEmailFoundValidator extends FieldValidatorSupport
{

    @Autowired
    AccountManager accountManager;

    /**
     * Method called by struts2 validation process This validator makes sure the given field is either a username or an
     * email address belonging to an account in the system.
     */
    @Override
    public void validate(Object object) throws ValidationException
    {

        String userOrEmail = (String) this.getFieldValue(this.getFieldName(), object);

        // Get the user's account based on the userOrEmail field
        Account account = accountManager.getAccountByEmail(userOrEmail);
        if (account == null)
        {
            account = accountManager.getAccountByUserName(userOrEmail);
        }
        if (account == null)
        {
            addFieldError(this.getFieldName(), "UserName or Email not found");
            return;
        }
        if(account.getAccountStatus() == AccountStatus.INACTIVE || account.getAccountStatus() == AccountStatus.WITHDRAWN) {
        	addFieldError(this.getFieldName(), "Account is inactive or withdrawn");
            return;
        }

        // All good
    }

}
