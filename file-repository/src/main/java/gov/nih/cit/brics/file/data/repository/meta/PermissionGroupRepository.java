package gov.nih.cit.brics.file.data.repository.meta;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;

public interface PermissionGroupRepository extends CrudRepository<PermissionGroup, Long> {
	public List<PermissionGroup> findAllByMemberSet_Account(Account account);
}
