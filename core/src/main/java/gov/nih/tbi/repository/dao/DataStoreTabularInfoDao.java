
package gov.nih.tbi.repository.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.repository.model.hibernate.DataStoreInfo;
import gov.nih.tbi.repository.model.hibernate.DataStoreTabularInfo;

public interface DataStoreTabularInfoDao extends GenericDao<DataStoreTabularInfo, Long>
{

    public DataStoreTabularInfo get(DataStoreInfo storeInfo, RepeatableGroup repeatableGroup);

}