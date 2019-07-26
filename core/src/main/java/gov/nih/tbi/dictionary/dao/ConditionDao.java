
package gov.nih.tbi.dictionary.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.Condition;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;

public interface ConditionDao extends GenericDao<Condition, Long>
{

    /**
     * Gets the condition by data element
     * 
     * @param dataElement
     * @return
     */
    public List<Condition> getByDataElement(DataElement dataElement);
}
