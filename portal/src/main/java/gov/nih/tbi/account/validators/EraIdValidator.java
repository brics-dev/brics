
package gov.nih.tbi.account.validators;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.service.AccountManager;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class EraIdValidator extends FieldValidatorSupport
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
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

        Long id = (Long) this.getFieldValue(PortalConstants.ACCOUNT_ID, object);

        // If the user name is not unique, return an error
        if (fieldValue == null || !accountManager.checkEraIdAvailability(fieldValue, id))
        {
            // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
            // is displayed. However a field error must be added to indicate that something has failed.
            addFieldError(fieldName, "Invalid Era Commons Id");
        }

    }

}
