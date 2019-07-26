
package gov.nih.tbi.dictionary.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Translations
{

    @XmlElement(name = "formTranslation")
    List<FormTranslation> formTranslations;

    public List<FormTranslation> getFormTranslations()
    {

        return formTranslations;
    }

    public void setFormTranslations(List<FormTranslation> formTranslations)
    {

        this.formTranslations = formTranslations;
    }
}
