
package gov.nih.tbi.repository.dao.hibernate;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.repository.dao.DataStoreTabularColumnInfoDao;
import gov.nih.tbi.repository.model.hibernate.DataStoreTabularColumnInfo;

/**
 * Dao implementation to store information about columns in data store tables.
 * 
 * @author dhollo
 * 
 */
@Transactional("metaTransactionManager")
@Repository
public class DataStoreTabularColumnInfoDaoImpl extends GenericDaoImpl<DataStoreTabularColumnInfo, Long> implements
        DataStoreTabularColumnInfoDao
{

    @Autowired
    public DataStoreTabularColumnInfoDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory)
    {

        super(DataStoreTabularColumnInfo.class, sessionFactory);
    }
}
