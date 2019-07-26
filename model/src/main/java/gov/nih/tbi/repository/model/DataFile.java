
package gov.nih.tbi.repository.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DataFile
{

    @XmlElement(name = "dataStructure")
    List<DataStructureData> dataStructure;

    public List<DataStructureData> getDataStructure()
    {

        return dataStructure;
    }

    public void setDataStructure(List<DataStructureData> dataStructure)
    {

        this.dataStructure = dataStructure;
    }
}
