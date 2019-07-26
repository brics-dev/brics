
package gov.nih.tbi.repository.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class DataStructureData
{

    @XmlAttribute
    String shortName;

    @XmlAttribute
    String version;

    @XmlElement(name = "record")
    List<Record> record;

    public String getShortName()
    {

        return shortName;
    }

    public void setShortName(String shortName)
    {

        this.shortName = shortName;
    }

    public String getVersion()
    {

        return version;
    }

    public void setVersion(String version)
    {

        this.version = version;
    }

    public List<Record> getRecord()
    {

        return record;
    }

    public void setRecord(List<Record> record)
    {

        this.record = record;
    }
}
