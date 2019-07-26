
package gov.nih.tbi.repository.dao.hibernate;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.repository.dao.GrantDao;
import gov.nih.tbi.repository.model.hibernate.Grant;

@Transactional("metaTransactionManager")
@Repository
public class GrantDaoImpl extends GenericDaoImpl<Grant, Long> implements GrantDao
{

    @Autowired
    public GrantDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory)
    {

        super(Grant.class, sessionFactory);
    }
}
