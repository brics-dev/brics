package gov.nih.tbi.account.validators;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.service.AccountManager;

public class ActiveAccountValidator extends FieldValidatorSupport {

	
	@Autowired
    AccountManager accountManager;
	
	@Override
	public void validate(Object object) throws ValidationException {
		String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);
        
        Account account = accountManager.getAccountByEmail(fieldValue);
        
        if (account == null || account.getAccountStatus() == AccountStatus.INACTIVE || account.getAccountStatus() == AccountStatus.WITHDRAWN) {
        	addFieldError(fieldName, "Account is inactive or withdrawn");
        }

	}

}
