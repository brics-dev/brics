
package gov.nih.tbi.dictionary.validators;

import gov.nih.tbi.commons.service.DictionaryToolManager;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.dictionary.portal.DataElementAction;
import gov.nih.tbi.dictionary.portal.MapElementAction;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

/**
 * Makes sure there is at least one permissible value in the table
 * 
 * @author mvalei
 * 
 */
public class MinPermissibleValuesValidator extends FieldValidatorSupport
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
        Set<ValueRange> valueRanges = null;

        if (object instanceof DataElementAction)
        {
            valueRanges = ((DataElementAction) object).getSessionDataElement().getDataElement().getValueRangeList();
        }
        else
            if (object instanceof MapElementAction)
            {
                valueRanges = ((MapElementAction) object).getCurrentMapElement().getStructuralDataElement()
                        .getValueRangeList();
            }

        Boolean defined = (Boolean) this.getFieldValue("valueRangeForm.defined", object);

        if (defined && valueRanges.isEmpty())
        {
            // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
            // is displayed. However a field error must be added to indicate that something has failed.
            addFieldError(fieldName, "No permissable values.");
        }
    }
}
