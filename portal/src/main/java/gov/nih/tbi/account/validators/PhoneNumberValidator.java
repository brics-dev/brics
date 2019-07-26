
package gov.nih.tbi.account.validators;

import org.apache.cxf.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.commons.service.AccountManager;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

/**
 * This class checks if a phone number's format is valid
 * 
 * @author Francis Chen
 */
public class PhoneNumberValidator extends FieldValidatorSupport
{

    @Autowired
    AccountManager accountManager;

    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

        // If the phone number is not valid, return an error
        if (!StringUtils.isEmpty(fieldValue) && !accountManager.validatePhoneNumber(fieldValue))
        {
            // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
            // is displayed. However a field error must be added to indicate that something has failed.
            addFieldError(fieldName, "Invalid Phone Number");
        }
    }

}
