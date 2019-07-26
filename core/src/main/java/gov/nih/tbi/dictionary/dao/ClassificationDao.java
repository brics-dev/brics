
package gov.nih.tbi.dictionary.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.Classification;

public interface ClassificationDao extends GenericDao<Classification, Long>
{

    /**
     * Get the list of classifications that the user is permitted to create.
     * 
     * @return
     */
    public List<Classification> getUserList();
}
