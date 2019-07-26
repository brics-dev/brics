package gov.nih.tbi.account.dao;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.commons.dao.GenericDao;

import java.util.List;

public interface PermissionGroupDao extends GenericDao<PermissionGroup, Long> {

	public List<PermissionGroup> getPublicGroups();

	public List<PermissionGroup> getGrantedGroups(Account account);

	public List<PermissionGroup> getPrivateGroups();

	public PermissionGroup getByName(String groupName);

}
