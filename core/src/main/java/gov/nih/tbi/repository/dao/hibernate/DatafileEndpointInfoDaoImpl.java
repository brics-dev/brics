
package gov.nih.tbi.repository.dao.hibernate;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.repository.dao.DatafileEndpointInfoDao;
import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;

@Transactional("metaTransactionManager")
@Repository
public class DatafileEndpointInfoDaoImpl extends GenericDaoImpl<DatafileEndpointInfo, Long> implements
        DatafileEndpointInfoDao
{

    @Autowired
    public DatafileEndpointInfoDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory)
    {

        super(DatafileEndpointInfo.class, sessionFactory);
    }

}
