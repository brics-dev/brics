package gov.nih.tbi.account.validators;

import org.apache.commons.lang3.StringUtils;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class MiddleNameMatchValidator extends FieldValidatorSupport {
	
    @Override
    public void validate(Object object) throws ValidationException {
    	
        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(fieldName, object);
        
        String originalMiddleName = (String) this.getFieldValue("currentAccount.user.middleName", object);
        
        if (StringUtils.isEmpty(fieldValue) && !StringUtils.isEmpty(originalMiddleName)) {
        	addFieldError(fieldName, "Incorrect Middle Name.");
        } else if (!StringUtils.isEmpty(fieldValue) && !fieldValue.equalsIgnoreCase(originalMiddleName)) {
        	addFieldError(fieldName, "Incorrect Middle Name.");
        }
    }

}
