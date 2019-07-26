
package gov.nih.tbi.dictionary.model;

import gov.nih.tbi.dictionary.model.hibernate.DataElement;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This is a wrapper class for a list of objects that extend AbstractDataElement. Its purpose is to generate an XML
 * export containing Data Elements using JAXB to marshal the objects to XML.
 * 
 * @author dhollo
 * 
 */
@XmlRootElement
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class DataElementsExport
{

    private List<DataElement> elementList;

    @XmlElementWrapper
    @XmlElement(name = "element")
    public List<DataElement> getElementList()
    {

        return elementList;
    }

    public void setElementList(List<DataElement> elementList)
    {

        this.elementList = elementList;
    }
}
