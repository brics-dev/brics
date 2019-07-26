
package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.ValidationPlugin;

public interface ValidationPluginDao extends GenericDao<ValidationPlugin, Long>
{

    /**
     * Returns the validator with name of param
     * 
     * @param name
     * @return
     */
    public ValidationPlugin getValidatorByName(String name);
}
