package gov.nih.tbi.account.validators;

import org.apache.commons.lang3.StringUtils;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class LastNameMatchValidator extends FieldValidatorSupport {
	
    @Override
    public void validate(Object object) throws ValidationException {
    	
        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(fieldName, object);
        
        String originalLastName = (String) this.getFieldValue("currentAccount.user.lastName", object);
        
        if (StringUtils.isEmpty(fieldValue) || !fieldValue.equalsIgnoreCase(originalLastName)) {
        	addFieldError(fieldName, "Incorrect Last Name.");
        }
    }
    
}
