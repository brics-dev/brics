
package gov.nih.tbi.repository.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class DataElementData
{

    @XmlAttribute
    String alias;

    @XmlAttribute
    String name;

    @XmlAttribute
    String value;

    public String getAlias()
    {

        return alias;
    }

    public void setAlias(String alias)
    {

        this.alias = alias;
    }

    public String getName()
    {

        return name;
    }

    public void setName(String name)
    {

        this.name = name;
    }

    public String getValue()
    {

        return value;
    }

    public void setValue(String value)
    {

        this.value = value;
    }
}
