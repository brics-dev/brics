
package gov.nih.tbi.account.validators;

import gov.nih.tbi.commons.service.AccountManager;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class PasswordFormatValidator extends FieldValidatorSupport
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
       //Remove leading, trailing and in between white/blank space from password entry from notorious bug(PS#158)
        String fieldValue = StringUtils.deleteWhitespace((String) this.getFieldValue(this.getFieldName(), object));  
        
        if( fieldValue != null )
        {
        	if( !accountManager.validatePasswordFormat(fieldValue) )
        	{
        		// Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
                // is displayed. However a field error must be added to indicate that something has failed.
                addFieldError(fieldName, "Invalid Password");
        		
        	}
        }
    }
}
