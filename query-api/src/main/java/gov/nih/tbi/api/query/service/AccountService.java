package gov.nih.tbi.api.query.service;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.api.query.data.entity.UserToken;
import gov.nih.tbi.service.model.PermissionModel;

public interface AccountService {

    /**
     * Returns the Account object for the given user name.
     * 
     * @param username
     * @return
     */
    Account findByUserName(String username);

    /**
     * Returns the PermissionModel for the given user name. If not found in the
     * userPermissionCache, it will create the PermissionModel object and add to the
     * cache for the future use.
     * 
     * @param username
     * @return
     */
    PermissionModel getPermissionModel(UserToken userToken);
}
