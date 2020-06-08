package gov.nih.tbi.api.query.data.repository.custom;

import java.util.List;
import java.util.Set;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.commons.model.EntityType;

public interface EntityMapRepoCustom {
	
	List<EntityMap> listUserAccess(Account account, Set<PermissionGroup> currentGroups, EntityType type,
			boolean isAdmin);

}
