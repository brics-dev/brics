
package gov.nih.tbi.dictionary.service.rulesengine;

import gov.nih.tbi.commons.model.SeverityLevel;
import gov.nih.tbi.dictionary.model.SeverityRecord;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.service.rulesengine.model.InvalidOperationException;
import gov.nih.tbi.dictionary.service.rulesengine.model.RulesEngineException;

import java.util.List;

public interface RulesEngineInterface
{

    /**
     * Compares two data elements and returns a list of changes made to each field. Returns an empty list when no
     * changes are found.
     */
    public List<SeverityRecord> evaluateDataElementChangeSeverity(DataElement originalDataElement,
            DataElement alteredDataElement) throws InvalidOperationException, RulesEngineException;

    /**
     * Compares two form structures and returns a list of changes made to each field. Returns an empty list when no
     * changes are found.
     */
    public List<SeverityRecord> evaluateFormStructureChangeSeverity(FormStructure originalDataDataStructure,
            FormStructure alteredDataDataStructure) throws InvalidOperationException, RulesEngineException,
            NoSuchMethodException;

    /**
     * Compares two data elements and returns the highest severity level found in the altered data element. Returns NULL
     * when no changes are found.
     */
    public SeverityLevel highestSeverityLevel(DataElement originalDataElement, DataElement alteredDataElement)
            throws InvalidOperationException, RulesEngineException;

    /**
     * Compares two form structures and returns the highest severity level found in the altered form structure. Returns
     * NULL when no changes are found.
     */
    public SeverityLevel highestSeverityLevel(FormStructure originalDataDataStructure,
            FormStructure alteredDataDataStructure) throws InvalidOperationException, RulesEngineException,
            NoSuchMethodException;
}