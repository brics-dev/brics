
package gov.nih.tbi.dictionary.ws.validation;

import gov.nih.tbi.dictionary.model.FormTranslation;
import gov.nih.tbi.dictionary.model.TranslationRule;
import gov.nih.tbi.dictionary.model.Translations;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * This class handles the normalization/translation of form data
 * 
 * @author Francis Chen
 */
public class Normalizer extends DictionaryAccessor
{

    private RangeValidator ranger;
    private Map<StructuralFormStructure, HashMap<StructuralDataElement, HashMap<String, String>>> elementRules;
    private Translations translations;

    public Normalizer()
    {

        super();
    }

    public Normalizer(List<StructuralFormStructure> dictionary)
    {

        super(dictionary);
        ranger = new RangeValidator(dictionary);
    }

    public Normalizer(List<StructuralFormStructure> dictionary, RangeValidator validator, Translations translations)
    {

        super(dictionary);
        ranger = validator;
        this.translations = translations;
        elementRules = transformTranslations(translations);
    }

    public Normalizer(List<StructuralFormStructure> dictionary, Translations translations)
    {

        super(dictionary);
        this.translations = translations;
        elementRules = transformTranslations(translations);
    }

    /**
     * Checks if all the data structures referenced in the translation is in the dictionary
     * 
     * @return true if valid, false otherwise
     */
    public String validate()
    {

        if (translations != null)
        {
            for (FormTranslation form : translations.getFormTranslations())
            {
                StructuralFormStructure newStruct = Normalizer.getDataStructureByName(dictionary, form.getName());
                if (newStruct == null)
                {
                    return form.getName();
                }
            }
        }
        else
        {
            throw new NullPointerException("translation rule should not be null here");
        }

        return null;
    }

    /**
     * Transforms JAXB translations object into HashMap that is more useful for accessing translations.
     * 
     * @param translations
     * @return
     */
    public Map<StructuralFormStructure, HashMap<StructuralDataElement, HashMap<String, String>>> transformTranslations(
            Translations translations)
    {

        Map<StructuralFormStructure, HashMap<StructuralDataElement, HashMap<String, String>>> elementRules = new HashMap<StructuralFormStructure, HashMap<StructuralDataElement, HashMap<String, String>>>();

        if (translations != null)
        {
            for (FormTranslation form : translations.getFormTranslations())
            {
                StructuralFormStructure newStruct = Normalizer.getDataStructureByName(dictionary, form.getName());

                if (newStruct != null)
                {
                    for (TranslationRule rule : form.getTranslationRules())
                    {

                        HashMap<StructuralDataElement, HashMap<String, String>> newElementMap;
                        HashMap<String, String> newRuleMap;
                        StructuralDataElement newElement = Normalizer.getDataElementByName(dictionary,
                                newStruct.getShortName(), rule.getDataElementName());

                        if (elementRules.get(newStruct) == null)
                        {
                            newElementMap = new HashMap<StructuralDataElement, HashMap<String, String>>();
                        }
                        else
                        {
                            newElementMap = elementRules.get(newStruct);
                        }

                        if (newElementMap.get(newElement) == null)
                        {
                            newRuleMap = new HashMap<String, String>();
                        }
                        else
                        {
                            newRuleMap = newElementMap.get(newElement);
                        }

                        newRuleMap.put(rule.getUserValue(), rule.getPermissibleValue());
                        newElementMap.put(newElement, newRuleMap);
                        elementRules.put(newStruct, newElementMap);
                    }
                }
            }
        }

        return elementRules;
    }

    /**
     * The purpose is to return the translated/normalized data given the data element and data input
     * 
     * @param iElement
     * @param dataInput
     * @return
     */
    public String normalize(StructuralFormStructure dataStructure, StructuralDataElement dataElement, String dataInput)
    {

        if (elementRules != null && elementRules.get(dataStructure) != null
                && elementRules.get(dataStructure).get(dataElement) != null
                && elementRules.get(dataStructure).get(dataElement).get(dataInput) != null)
        {
            return elementRules.get(dataStructure).get(dataElement).get(dataInput);
        }
        else
        {
            return dataInput;
        }
    }

    public String translate(HashMap<HashSet<String>, String> translations, String data, MapElement iElement)
    {

        for (HashSet<String> values : translations.keySet())
        {
            if (ranger.inRange(data, iElement))
            {
                return translations.get(values);
            }
        }
        return data;
    }

}
