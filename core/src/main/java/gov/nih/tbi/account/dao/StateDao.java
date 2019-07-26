
package gov.nih.tbi.account.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.hibernate.State;

public interface StateDao extends GenericDao<State, Long>
{

    /**
     * Overrides the original getAll to sort states alphabetically
     */
    public List<State> getAll();
}
