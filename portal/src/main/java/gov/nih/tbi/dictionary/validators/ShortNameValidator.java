
package gov.nih.tbi.dictionary.validators;

import gov.nih.tbi.commons.service.DictionaryToolManager;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

/**
 * Short Name Validator for struts2 validation:
 * 
 * Checks the database to determine if the short name is valid.
 * 
 * @author Andrew Johnson
 * 
 */
public class ShortNameValidator extends FieldValidatorSupport
{

    @Autowired
    DictionaryToolManager dictionaryToolManager;

    /**
     * Method called by struts2 validation process
     */
    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

        // Gets current data structure from the action
        FormStructure dataStructure = (FormStructure) this
                .getFieldValue("currentDataStructure", object);

        // If the short name is not valid then throw an error
        if (fieldValue != null && !dictionaryToolManager.validateShortName(dataStructure, fieldValue))
        {

            // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
            // is displayed. However a field error must be added to indicate that something has failed.
            addFieldError(fieldName, "Invalid Short Name");
        }

    }

}
