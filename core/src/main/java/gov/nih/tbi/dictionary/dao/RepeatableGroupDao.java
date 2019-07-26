
package gov.nih.tbi.dictionary.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;

/**
 * Abstract Hibernate class used for making database querys for Repeatable Groups
 * 
 * @author mvalei
 */
public interface RepeatableGroupDao extends GenericDao<RepeatableGroup, Long>
{

    /**
     * Identifies a repeatableGroup based on the name of the group and the Data Structure to which it belongs.
     * 
     * @return the ids of repeatable groups matching the criteria
     */
    public List<Long> getIdsByNameAndDS(String repeatableGroupName, Long dataStructureId);
    
    public Map<Long, RepeatableGroup> getByIds(Set<Long> ids);
}
