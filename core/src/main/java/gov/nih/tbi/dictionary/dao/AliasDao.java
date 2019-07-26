
package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.Alias;

public interface AliasDao extends GenericDao<Alias, Long>
{

    public Alias getAliasByName(String aliasName);
}
