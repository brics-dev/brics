
package gov.nih.tbi.repository.validators;

import gov.nih.tbi.commons.service.RepositoryManager;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class ClinicalTrialValidator extends FieldValidatorSupport
{

    @Autowired
    RepositoryManager repositoryManager;

    /**
     * Method called by struts2 validation process
     */
    @SuppressWarnings("unchecked")
    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

        try
        {
            if (fieldValue != null && !repositoryManager.validateClinicalTrialId(fieldValue))
            {
                // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
                // is displayed. However a field error must be added to indicate that something has failed.
                addFieldError(fieldName, "Invalid Clinical Trial ID");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
