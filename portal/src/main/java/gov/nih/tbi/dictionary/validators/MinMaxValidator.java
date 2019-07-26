
package gov.nih.tbi.dictionary.validators;

import java.math.BigDecimal;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class MinMaxValidator extends FieldValidatorSupport
{

    /**
     * Validator that guarantees an elements minValue is less than its maxValue
     */
    @SuppressWarnings("unchecked")
    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        BigDecimal minimumValue = (BigDecimal) this.getFieldValue("valueRangeForm.minimumValue", object);
        BigDecimal maximumValue = (BigDecimal) this.getFieldValue("valueRangeForm.maximumValue", object);

        if (minimumValue != null && maximumValue != null && minimumValue.compareTo(maximumValue) > 0)
        {
            // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
            // is displayed. However a field error must be added to indicate that something has failed.
            addFieldError(fieldName, "Invalid Permissible Value");
        }
    }

}
