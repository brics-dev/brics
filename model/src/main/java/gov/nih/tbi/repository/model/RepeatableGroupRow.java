
package gov.nih.tbi.repository.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class RepeatableGroupRow
{

    @XmlElement(name = "data")
    List<DataElementData> data;

    public List<DataElementData> getData()
    {

        return data;
    }

    public void setData(List<DataElementData> data)
    {

        this.data = data;
    }
}
