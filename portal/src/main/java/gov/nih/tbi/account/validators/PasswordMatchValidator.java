package gov.nih.tbi.account.validators;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

import gov.nih.tbi.commons.service.AccountManager;

public class PasswordMatchValidator extends FieldValidatorSupport {
	
    @Autowired
    AccountManager accountManager;
    
    
    @Override
    public void validate(Object object) throws ValidationException {
    	
        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(fieldName, object);
        
        if (StringUtils.isEmpty(fieldValue)) {
        	addFieldError(fieldName, "Password cannot be empty.");
        }
        
        byte[] originalPassword = (byte[]) this.getFieldValue("currentAccount.password", object);
        String salt = (String) this.getFieldValue("currentAccount.salt", object);
        
        if (!Arrays.equals(originalPassword, accountManager.hashPassword(salt + fieldValue))) {
        	addFieldError(fieldName, "Invalid Password.");
        }

    }

}
