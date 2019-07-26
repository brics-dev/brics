
package gov.nih.tbi.dictionary.validators;

import java.util.List;

import gov.nih.tbi.commons.service.DictionaryToolManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

/**
 * This method will validate if a repeatable group is unique to a particular Form Structure
 * 
 * @author Francis Chen
 * 
 */
public class RepeatableGroupNameValidator extends FieldValidatorSupport
{

    @Autowired
    DictionaryToolManager dictionaryToolManager;

    /**
     * Method called by struts2 validation process
     */
    @SuppressWarnings("unchecked")
    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

        // Gets current data element from the action
        List<String> rgNames = (List<String>) this.getFieldValue("sessionRepeatableGroupNames", object);

        for (String rgName : rgNames)
        {
            if (rgName.equals(fieldValue))
            {
                // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
                // is displayed. However a field error must be added to indicate that something has failed.
                addFieldError(fieldName, "Invalid Group Name");
            }
        }
    }
}
