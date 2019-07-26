
package gov.nih.tbi.repository.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Record
{

    @XmlElement(name = "repeatableGroup")
    List<RepeatableGroupData> repeatableGroup;

    public List<RepeatableGroupData> getRepeatableGroup()
    {

        return repeatableGroup;
    }

    public void setRepeatableGroup(List<RepeatableGroupData> repeatableGroup)
    {

        this.repeatableGroup = repeatableGroup;
    }
}
