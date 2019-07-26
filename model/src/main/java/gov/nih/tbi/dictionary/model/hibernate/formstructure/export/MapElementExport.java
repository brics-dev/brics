
package gov.nih.tbi.dictionary.model.hibernate.formstructure.export;

import gov.nih.tbi.commons.model.RequiredType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;



@XmlRootElement(name = "mapElement")
@XmlAccessorType(XmlAccessType.FIELD)
public class MapElementExport
{

    private Long id;
    private String repeatableGroup;
    private DataElementExport dataElement;
    private String requiredType;
    private Integer position;
    private RequiredType requiredTypeId;
    @XmlElement
    private ConditionExport condition;

    /**********************************************************************/


    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public String getRepeatableGroup()
    {

        return repeatableGroup;
    }

    public void setRepeatableGroup(String repeatableGroup)
    {

        this.repeatableGroup = repeatableGroup;
    }

    public DataElementExport getDataElement()
    {

        return dataElement;
    }

    public void setDataElement(DataElementExport dataElement)
    {

        this.dataElement = dataElement;
    }

    public Integer getPosition()
    {

        return position;
    }

    public void setPosition(Integer position)
    {

        this.position = position;
    }

    public RequiredType getRequiredType()
    {

        return requiredTypeId;
    }

    public void setRequiredType(RequiredType requiredType)
    {

        this.requiredTypeId = requiredType;
    }

    public String getName()
    {

        return dataElement.getName();
    }

    public void setName(String name)
    {

        dataElement.setName(name);
    }


    public ConditionExport getCondition()
    {

        return condition;
    }

    public void setCondition(ConditionExport condition)
    {

        this.condition = condition;
    }

	public String getRequiredTypeString() {
		return requiredType;
	}

	public void setRequiredTypeString(String requiredType) {
		this.requiredType = requiredType;
	}


}
