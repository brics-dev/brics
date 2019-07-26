
package gov.nih.tbi.dictionary.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

public interface ValueRangeDao extends GenericDao<ValueRange, Long>
{
	public ValueRange getByDeNameAndPv(DataElement de, String pvValue);
	public List<ValueRange> getByDeName(DataElement de);
}
