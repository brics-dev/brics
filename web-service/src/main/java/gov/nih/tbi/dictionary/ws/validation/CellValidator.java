
package gov.nih.tbi.dictionary.ws.validation;

import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;

import java.util.List;

abstract class CellValidator extends DictionaryAccessor
{

    // XXX: Why is there a dictionary here as well?
    // private List<AbstractDataStructure> dictionary;

    public CellValidator()
    {

        super();
    }

    public CellValidator(List<StructuralFormStructure> dictionary)
    {

        super(dictionary);
    }

    public boolean validate(String shortname, String element, String data) throws RuntimeException
    {

        MapElement current = getElement(shortname, element);
        return validate(current, data);
    }

    abstract public boolean validate(MapElement iElement, String data);
}
