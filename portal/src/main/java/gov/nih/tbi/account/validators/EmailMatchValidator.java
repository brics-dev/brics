
package gov.nih.tbi.account.validators;

import gov.nih.tbi.commons.service.AccountManager;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

/**
 * This class validates that the email matches the email of an account in the system
 * 
 * @author Francis Chen
 */
public class EmailMatchValidator extends FieldValidatorSupport
{

    @Autowired
    AccountManager accountManager;

    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

        if (fieldValue != null && !accountManager.validateEmailMatch(fieldValue))
        {
            // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
            // is displayed. However a field error must be added to indicate that something has failed.
            addFieldError(fieldName, "Email does not match any email in the system.");
        }
    }
}
