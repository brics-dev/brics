
package gov.nih.tbi.account.dao.hibernate;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.dao.PermissionGroupMemberDao;
import gov.nih.tbi.account.model.hibernate.PermissionGroupMember;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;

@Transactional("metaTransactionManager")
@Repository
public class PermissionGroupMemberDaoImpl extends GenericDaoImpl<PermissionGroupMember, Long> implements
        PermissionGroupMemberDao
{

    @Autowired
    public PermissionGroupMemberDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory)
    {

        super(PermissionGroupMember.class, sessionFactory);
    }

}
