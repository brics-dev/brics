
package gov.nih.tbi.account.dao.hibernate;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.dao.PermissionMapDao;
import gov.nih.tbi.account.model.hibernate.PermissionMap;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;

@Transactional("metaTransactionManager")
@Repository
public class PermissionMapDaoImpl extends GenericDaoImpl<PermissionMap, Long> implements PermissionMapDao
{

    @Autowired
    public PermissionMapDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory)
    {

        super(PermissionMap.class, sessionFactory);
    }

}
