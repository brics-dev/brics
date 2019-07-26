
package gov.nih.tbi.repository.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class RepeatableGroupData
{

    @XmlAttribute
    String name;

    @XmlElement(name = "group")
    List<RepeatableGroupRow> group;

    public String getName()
    {

        return name;
    }

    public void setName(String name)
    {

        this.name = name;
    }

    public List<RepeatableGroupRow> getGroup()
    {

        return group;
    }

    public void setGroup(List<RepeatableGroupRow> group)
    {

        this.group = group;
    }
}
