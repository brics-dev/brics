
package gov.nih.tbi.account.validators;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.portal.BaseAction;
import gov.nih.tbi.commons.service.AccountManager;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class PasswordRecoveryValidator extends FieldValidatorSupport
{

    @Autowired
    AccountManager accountManager;
    static Logger logger = Logger.getLogger(PasswordRecoveryValidator.class);

    /**
     * Method called by struts2 validation process This validator validates the entire change password though password
     * recovery form because any errors on the page should return a generic password error.
     */
    @Override
    public void validate(Object object) throws ValidationException
    {

        String userOrEmail = (String) this.getFieldValue("userOrEmail", object);
        String casToken = (String) this.getFieldValue("casToken", object);

        Account account = accountManager.getAccountByEmail(userOrEmail);
        if (account == null){
            account = accountManager.getAccountByUserName(userOrEmail);
        }

        Date date = null;
        if(account != null) {
        	date = account.getRecoveryDate();
        }
        if(date != null){ logger.info("casToken: "+casToken);logger.info("account username: "+account.getUserName());
	        // Validate the token
	        if (!accountManager.validateRecoveryTokenForAccount(account, casToken))
	        {
	            addFieldError(this.getFieldName(), "Invalid token");
	            logger.error("The token passed in the request is not a valid token.");
	            return;
	        }
	        
	        // Use a calendar object to increment the date
	        Calendar cal = new GregorianCalendar();
	        cal.setTime(date);
	        cal.add(Calendar.MINUTE, CoreConstants.RECOVERY_TIME);
	        if (new Date().after(cal.getTime()))
	        {
	            addFieldError(this.getFieldName(), "Recovery Expired");
	            logger.error("The token passed in the request has expired.");
	            return;
	        }
        }
        
    }

}
