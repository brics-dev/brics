
package gov.nih.tbi.account.validators;

import gov.nih.tbi.PortalConstants;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class AdminNoteValidator extends FieldValidatorSupport
{

    /**
     * Method called by struts2 validation process
     */
    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

        String namespace = (String) this.getFieldValue(PortalConstants.NAME_SPACE, object);
        Boolean isRequest = (Boolean) this.getFieldValue(PortalConstants.IS_REQUEST, object);

        if (!isRequest && PortalConstants.NAMESPACE_ADMIN.equals(namespace)
                && (fieldValue == null || PortalConstants.EMPTY_STRING.equals(fieldValue)))
        {
            // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
            // is displayed. However a field error must be added to indicate that something has failed.
            addFieldError(fieldName, "this field is required");
        }

    }
}
