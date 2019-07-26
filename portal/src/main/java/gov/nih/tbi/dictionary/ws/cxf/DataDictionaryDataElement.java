
package gov.nih.tbi.dictionary.ws.cxf;

import gov.nih.tbi.dictionary.model.hibernate.DataElement;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "dataElementList")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataDictionaryDataElement
{

    @XmlElement(name = "dataElement")
    private List<DataElement> dataElementList;

    public DataDictionaryDataElement()
    {

    }

    public DataDictionaryDataElement(List<DataElement> dataElementList)
    {

        this.dataElementList = dataElementList;
    }

    public List<DataElement> getDataElementList()
    {

        return dataElementList;
    }

}
