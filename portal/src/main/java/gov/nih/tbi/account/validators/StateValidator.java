
package gov.nih.tbi.account.validators;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.model.hibernate.Country;
import gov.nih.tbi.commons.model.hibernate.State;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

/**
 * Custom Validator for State. State should only be required if Country is USA
 * 
 * @author Francis Chen
 */
public class StateValidator extends FieldValidatorSupport
{

    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        State fieldValue = (State) this.getFieldValue(this.getFieldName(), object);

        Country country = (Country) this.getFieldValue("accountDetailsForm.country", object);

        if (fieldValue == null && (country == null || (CoreConstants.UNITED_STATES.equals(country.getName()))))
        {
            addFieldError(fieldName, "State is Required");
        }
    }
}
