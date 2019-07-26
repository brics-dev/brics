
package gov.nih.tbi.dictionary.model.hibernate.formstructure.export;

import gov.nih.tbi.commons.model.ConditionalOperators;


public class ConditionExport
{

    private Long id;
    private ConditionalOperators operator;
    private MapElementExport mapElement;
    private String value;

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public MapElementExport getMapElement()
    {

        return mapElement;
    }

    public void setMapElement(MapElementExport mapElement)
    {

        this.mapElement = mapElement;
    }

    public String getValue()
    {

        return value;
    }

    public void setValue(String value)
    {

        this.value = value;
    }

    public ConditionalOperators getOperator()
    {

        return operator;
    }

    public void setOperator(ConditionalOperators operator)
    {

        this.operator = operator;
    }
}
