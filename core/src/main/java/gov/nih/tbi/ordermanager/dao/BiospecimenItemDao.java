
package gov.nih.tbi.ordermanager.dao;

import java.util.Map;
import java.util.Set;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.ordermanager.model.BiospecimenItem;

public interface BiospecimenItemDao extends GenericDao<BiospecimenItem, Long>
{
    public Map <Long,BiospecimenItem> getBiospecimenItems(Set<Long> biospecimenIds);
}
