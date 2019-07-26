
package gov.nih.tbi.dictionary.ws.cxf;

import gov.nih.tbi.dictionary.model.hibernate.FormStructure;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "formStructureList")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataDictionaryForm
{

    @XmlElement(name = "formStructure")
    private List<FormStructure> dataStructureList;

    public DataDictionaryForm()
    {

    }

    public DataDictionaryForm(List<FormStructure> dataStructureList)
    {

        this.dataStructureList = dataStructureList;
    }

    public List<FormStructure> getDataStructureList()
    {

        return dataStructureList;
    }

}
