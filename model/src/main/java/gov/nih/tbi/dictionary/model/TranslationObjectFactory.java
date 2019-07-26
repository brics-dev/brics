
package gov.nih.tbi.dictionary.model;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class TranslationObjectFactory
{

    public FormTranslation createFormTranslation()
    {

        return new FormTranslation();
    }

    public TranslationRule createTranslationRule()
    {

        return new TranslationRule();
    }

    public Translations createTranslations()
    {

        return new Translations();
    }
}
