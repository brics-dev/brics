
package gov.nih.tbi.repository.dao.hibernate;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.repository.dao.DataStoreBinaryInfoDao;
import gov.nih.tbi.repository.model.hibernate.DataStoreBinaryInfo;

/**
 * Hibernate implementation of the DAO to create and access information for binary data stores.
 * 
 * @author dhollo
 * 
 */
@Transactional("metaTransactionManager")
@Repository
public class DataStoreBinaryInfoDaoImpl extends GenericDaoImpl<DataStoreBinaryInfo, Long> implements
        DataStoreBinaryInfoDao
{

    @Autowired
    public DataStoreBinaryInfoDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory)
    {

        super(DataStoreBinaryInfo.class, sessionFactory);
    }
}
