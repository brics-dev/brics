
package gov.nih.tbi.account.validators;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class RequestPrivilegesReasonValidation extends FieldValidatorSupport
{

    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

        if (fieldValue != null && fieldValue.equals(""))
        {
            // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
            // is displayed. However a field error must be added to indicate that something has failed.
            addFieldError(fieldName, "this field is required");
        }

        // TODO Auto-generated method stub

    }

}
