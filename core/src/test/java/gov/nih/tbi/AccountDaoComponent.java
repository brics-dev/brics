
package gov.nih.tbi;

import gov.nih.tbi.account.dao.AccountDao;
import gov.nih.tbi.account.dao.EntityMapDao;
import gov.nih.tbi.account.dao.PermissionGroupDao;
import gov.nih.tbi.account.dao.PermissionGroupMemberDao;
import gov.nih.tbi.account.dao.PermissionMapDao;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class AccountDaoComponent extends DaoComponent
{

    static Logger logger = Logger.getLogger(AccountDaoComponent.class);

    /**********************************************************************************/

    @Autowired
    EntityMapDao entityMapDao;

    @Autowired
    PermissionMapDao permissionMapDao;

    @Autowired
    AccountDao accountDao;

    @Autowired
    PermissionGroupDao permissionGroupDao;

    @Autowired
    PermissionGroupMemberDao permissionGroupMemberDao;

    /**********************************************************************************/

}
