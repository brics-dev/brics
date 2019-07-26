
package gov.nih.tbi.dictionary.ws.validation;

import gov.nih.tbi.dictionary.model.hibernate.MapElement;

import java.util.Vector;

public interface ConditionalValidator
{

    abstract public String getConstraintType(String rowRef) throws RuntimeException;

    abstract public boolean validateConstraint(String operator, String rowRef, Vector<String> valueRange, String type,
            MapElement iElement) throws RuntimeException;

}