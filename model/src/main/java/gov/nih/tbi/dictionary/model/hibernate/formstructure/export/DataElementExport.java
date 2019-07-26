
package gov.nih.tbi.dictionary.model.hibernate.formstructure.export;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;



@XmlRootElement(name = "dataElement")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataElementExport
{

    private String name;


    public String getName()
    {

        return name;
    }

    public void setName(String name)
    {

        this.name = name;
    }


}
