
package gov.nih.tbi.dictionary.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.Disease;

public interface DiseaseDao extends GenericDao<Disease, Long>
{

    /**
     * Overrides the default getAll so it is sorted ascending alphabetical order, and also does not return row with
     * isActive=False.
     */
    public List<Disease> getAll();
    
    /**
     * Gets the disease prefix with disease id
     * @param diseaseId
     * @return
     */
    public String getPrefix(Long diseaseId);
}
