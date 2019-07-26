
package gov.nih.tbi.dictionary.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class FormTranslation
{

    @XmlAttribute
    String name;

    @XmlAttribute
    String version;

    @XmlElement(name = "translationRule")
    List<TranslationRule> translationRules;

    public String getName()
    {

        return name;
    }

    public void setName(String name)
    {

        this.name = name;
    }

    public String getVersion()
    {

        return version;
    }

    public void setVersion(String version)
    {

        this.version = version;
    }

    public List<TranslationRule> getTranslationRules()
    {

        return translationRules;
    }

    public void setTranslationRules(List<TranslationRule> translationRules)
    {

        this.translationRules = translationRules;
    }
}
