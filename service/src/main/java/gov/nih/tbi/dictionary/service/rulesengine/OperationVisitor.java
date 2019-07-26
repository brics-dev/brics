
package gov.nih.tbi.dictionary.service.rulesengine;

import gov.nih.tbi.dictionary.model.SeverityRecord;
import gov.nih.tbi.dictionary.service.rulesengine.model.FieldList;
import gov.nih.tbi.dictionary.service.rulesengine.model.InvalidOperationException;
import gov.nih.tbi.dictionary.service.rulesengine.model.Rule;
import gov.nih.tbi.dictionary.service.rulesengine.model.RulesEngineException;

import java.lang.reflect.InvocationTargetException;

public interface OperationVisitor
{

    /**
     * The evaluate method is responsible for calling the other evaluate methods in the Rules Engine Operation Handler.
     * 
     * @param original
     * @param incoming
     * @param rule
     * @param fieldList
     * @param dataDictionaryObject TODO
     * @param Field
     * @return
     * @throws RulesEngineException
     * @throws InvalidOperationException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public SeverityRecord evaluate(Object original, Object incoming, StringBuilder FieldName, Rule rule,
            FieldList fieldList, String dataDictionaryObject) throws InvalidOperationException, RulesEngineException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException;
}
