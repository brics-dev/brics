
package gov.nih.tbi.repository.validators;

import gov.nih.tbi.PortalConstants;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class StudyUrlValidator extends FieldValidatorSupport
{

    /**
     * Method called by struts2 validation process
     */
    @SuppressWarnings("unchecked")
    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

        Boolean urlRadio = (Boolean) this.getFieldValue(PortalConstants.URL_DOCUMENTATION_RADIO, object);

        if (urlRadio != null)
        {
            if ((fieldValue == null || PortalConstants.EMPTY_STRING.equals(fieldValue)) && urlRadio)
            {
                // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
                // is displayed. However a field error must be added to indicate that something has failed.
                addFieldError(fieldName, "URL is required");
            }
        }
    }
}
