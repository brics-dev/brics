
package gov.nih.tbi.dictionary.ws.validation;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class DictionaryAccessor
{

    private static Logger logger = Logger.getLogger(DictionaryAccessor.class);

    protected List<StructuralFormStructure> dictionary;

    public DictionaryAccessor()
    {

    }

    public DictionaryAccessor(List<StructuralFormStructure> dictionary)
    {

        this.dictionary = dictionary;
    }

    public void setDictionary(List<StructuralFormStructure> list)
    {

        this.dictionary = list;
    }

    public void addWorkingStructure(StructuralFormStructure struct)
    {

        if (dictionary == null)
        {
            dictionary = new ArrayList<StructuralFormStructure>();
        }
        dictionary.add(struct);
    }

    public static StructuralFormStructure getDataStructureByName(List<StructuralFormStructure> dictionary, String name)
    {

        try
        {

            for (StructuralFormStructure ads : dictionary)
            {
                // TODO: this will need to be changed to account for the version
                if (ads.getShortName().equalsIgnoreCase(name))
                {
                    return ads;
                }
            }
        }
        catch (NullPointerException e)
        {
            logger.debug("Form Structure not found!");
        }

        return null;
    }

    public static MapElement getDataElementByName(StructuralFormStructure ds, String elementName)
    {

        String repeatableGroup = "main";

        for (RepeatableGroup rg : ds.getRepeatableGroups())
        {
            if (rg.getName().equalsIgnoreCase(repeatableGroup))
            {
                for (MapElement me : rg.getMapElements())
                {
                    if (me.getStructuralDataElement().getName().equalsIgnoreCase(elementName))
                    {
                        return me;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Overloaded getDataElementByName that uses short name in the argument instead of a data structure
     * 
     * @param dictionary
     * @param structureName
     * @param elementName
     * @return
     */
    public static StructuralDataElement getDataElementByName(List<StructuralFormStructure> dictionary,
            String structureName, String elementName)
    {

        for (StructuralFormStructure dataStructure : dictionary)
        {
            if (structureName.equalsIgnoreCase(dataStructure.getShortName()))
            {
                for (MapElement me : dataStructure.getDataElements())
                {
                    if (me.getStructuralDataElement().getName().equalsIgnoreCase(elementName))
                    {
                        return me.getStructuralDataElement();
                    }
                }
            }
        }
        return null;
    }

    public static MapElement getDataElementByName(StructuralFormStructure ds, String elementName, String repeatableGroup)
    {

        if (repeatableGroup == null || ModelConstants.EMPTY_STRING.equals(repeatableGroup.trim()))
        {
            repeatableGroup = "main";
        }

        for (RepeatableGroup rg : ds.getRepeatableGroups())
        {
            if (rg.getName().equalsIgnoreCase(repeatableGroup))
            {
                for (MapElement me : rg.getMapElements())
                {
                    if (me.getStructuralDataElement().getName().equalsIgnoreCase(elementName))
                    {
                        return me;
                    }
                }
            }
        }

        return null;
    }

    protected MapElement getElement(String shortname, String element) throws RuntimeException
    {

        if (dictionary == null)
        {
            throw new RuntimeException("dictionary has not be initialized");
        }
        StructuralFormStructure struct = getDataStructureByName(dictionary, shortname.toLowerCase());
        if (struct == null)
        {
            throw new RuntimeException(shortname + " - No such structure exists");
        }
        MapElement el = getDataElementByName(struct, element.toLowerCase(), null); // struct.getDataElementByName(element.toLowerCase());
        if (el == null)
        {
            throw new RuntimeException(element + " - No such element exists in " + shortname);
        }
        return el;
    }

}
