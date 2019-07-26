
package gov.nih.tbi.dictionary.validators;

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.service.DictionaryToolManager;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.dictionary.portal.DataElementAction;
import gov.nih.tbi.dictionary.portal.MapElementAction;
import gov.nih.tbi.dictionary.portal.ValueRangeAction;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

/**
 * Validates numeric permissible values
 * 
 */
public class PermissibleValueNumericValidator extends FieldValidatorSupport
{

    @Autowired
    DictionaryToolManager dictionaryToolManager;

    /**
     * Method called by struts2 validation process
     */
    @Override
    public void validate(Object object) throws ValidationException
    {

        if (object.getClass().equals(ValueRangeAction.class))
        {
            ValueRangeAction action = (ValueRangeAction) object;

            String fieldName = this.getFieldName();
            String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

            if (action.getNumeric() && !isNumeric(fieldValue))
            {
                addFieldError(fieldName, "Permissible Value must be numeric.");
            }
        }
        else
        {
            if (object.getClass().equals(DataElementAction.class))
            {
                DataElementAction action = (DataElementAction) object;

                if (action.getValueRangeForm() != null && action.getValueRangeForm().getType() == DataType.NUMERIC)
                {
                    for (ValueRange value : action.getDataElementSession().getValueRangeList())
                    {
                        if (!isNumeric(value.getValueRange()))
                        {
                            addActionError(action);
                        }
                    }
                }
           }
           else if (object instanceof MapElementAction)
           {
                MapElementAction action = (MapElementAction) object;
    
                if (action.getValueRangeForm() != null && action.getValueRangeForm().getType() == DataType.NUMERIC)
                {
                    for (ValueRange value : action.getSessionDataElement().getDataElement().getValueRangeList())
                    {
                        if (!isNumeric(value.getValueRange()))
                        {
                            addActionError(action);
                        }
                    }
                }
           }
        }
    }

    private boolean isNumeric(String value)
    {

        try
        {
            Double.parseDouble(value);
            return true;
        }
        catch (NumberFormatException nfe)
        {
            return false;
        }
    }
}
