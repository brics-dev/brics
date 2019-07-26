
package gov.nih.tbi.dictionary.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class TranslationRule
{

    @XmlAttribute
    String dataElementName;

    @XmlAttribute
    String userValue;

    @XmlAttribute
    String permissibleValue;

    public String getDataElementName()
    {

        return dataElementName;
    }

    public void setDataElementName(String dataElementName)
    {

        this.dataElementName = dataElementName;
    }

    public String getUserValue()
    {

        return userValue;
    }

    public void setUserValue(String userValue)
    {

        this.userValue = userValue;
    }

    public String getPermissibleValue()
    {

        return permissibleValue;
    }

    public void setPermissibleValue(String permissibleValue)
    {

        this.permissibleValue = permissibleValue;
    }
}
