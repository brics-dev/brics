
package gov.nih.tbi.dictionary.model;


import gov.nih.tbi.dictionary.model.hibernate.formstructure.export.FormStructureExport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class for exporting Form Structure information to XML.
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DataStructureExport
{

    private FormStructureExport dataStructure;

    public FormStructureExport getDataStructure()
    {

        return dataStructure;
    }

    public void setDataStructure(FormStructureExport dataStructure)
    {

        this.dataStructure = dataStructure;
    }
}
