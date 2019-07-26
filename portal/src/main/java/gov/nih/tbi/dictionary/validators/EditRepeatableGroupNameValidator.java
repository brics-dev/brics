
package gov.nih.tbi.dictionary.validators;

import gov.nih.tbi.commons.service.DictionaryToolManager;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

/**
 * This method will validate the name of the repeatable group that is being edited is the same as another preexisting
 * repeatable group
 * 
 * @author mgree1
 * 
 */
public class EditRepeatableGroupNameValidator extends FieldValidatorSupport
{

    @Autowired
    DictionaryToolManager dictionaryToolManager;

    @SuppressWarnings("unchecked")
    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

        List<RepeatableGroup> curRepeatableGroups = (List<RepeatableGroup>) this.getFieldValue(
                "currentRepeatableGroups", object);
        Long editedRgId = (Long) this.getFieldValue("groupElementId", object);

        for (RepeatableGroup rg : curRepeatableGroups)
        {
            if (rg.getName().equals(fieldValue))
            {
                if (!rg.getId().equals(Long.valueOf(editedRgId)))
                {
                    addFieldError(fieldName, "Invalid Group Name");
                }
            }
        }
    }
}
