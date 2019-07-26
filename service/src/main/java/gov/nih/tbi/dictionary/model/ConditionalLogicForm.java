
package gov.nih.tbi.dictionary.model;

import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.commons.model.ConditionalOperators;
import gov.nih.tbi.commons.service.DictionaryToolManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.dictionary.model.hibernate.Condition;

/**
 * This form is for storing information about conditional logic
 * 
 * @author Francis Chen
 */
public class ConditionalLogicForm
{

    @Autowired
    DictionaryToolManager dictionaryManager;

    protected Long mapElementId;
    protected ConditionalOperators operator;
    protected String value;

    public ConditionalLogicForm()
    {

    }

    public ConditionalLogicForm(Condition condition)
    {

        this.mapElementId = condition.getMapElement() == null ? null : condition.getMapElement().getId();
        this.operator = condition.getOperator();
        this.value = condition.getValue();
    }

    public void setMapElementId(String id)
    {

        if (id != null && !ServiceConstants.EMPTY_STRING.equals(id))
        {
            this.mapElementId = Long.valueOf(id);
        }
    }

    public Long getMapElementId()
    {

        return mapElementId;
    }

    public ConditionalOperators getOperator()
    {

        return operator;
    }

    public void setOperator(String operator)
    {

        if (operator != null && !ServiceConstants.EMPTY_STRING.equals(operator))
        {
            for (ConditionalOperators op : ConditionalOperators.values())
            {
                if (Long.valueOf(operator).equals(op.getId()))
                {
                    this.operator = op;
                }
            }
        }
    }

    public String getValue()
    {

        return value;
    }

    public void setValue(String value)
    {

        this.value = value;
    }

    public void adapt(Condition condition)
    {

        condition.setOperator(operator);
        condition.setValue(value);
    }
}
