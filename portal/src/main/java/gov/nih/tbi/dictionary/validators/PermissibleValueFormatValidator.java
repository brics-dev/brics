
package gov.nih.tbi.dictionary.validators;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

/**
 * Validates that the permissible value does not contain a semi-colon
 * 
 * @author Francis Chen
 * 
 */
public class PermissibleValueFormatValidator extends FieldValidatorSupport
{

    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

        if (fieldValue != null && fieldValue.indexOf(";") > 0)
        {
            // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
            // is displayed. However a field error must be added to indicate that something has failed.
            addFieldError(fieldName, "Invalid Permissible Value");
        }
    }
}
