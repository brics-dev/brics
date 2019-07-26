
package gov.nih.tbi.dictionary.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.Domain;

public interface DomainDao extends GenericDao<Domain, Long>
{

    /**
     * Overrides the default getAll so it is sorted asending alphabetical order, and also does not return row with
     * isActive=False.
     */
    public List<Domain> getAll();
}
