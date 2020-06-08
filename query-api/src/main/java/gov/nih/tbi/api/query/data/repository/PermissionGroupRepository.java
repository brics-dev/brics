package gov.nih.tbi.api.query.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import gov.nih.tbi.account.model.hibernate.PermissionGroup;

public interface PermissionGroupRepository extends CrudRepository<PermissionGroup, Long> {

	@Query("SELECT p FROM PermissionGroup p WHERE p.publicStatus = true")
	List<PermissionGroup> getPublicGroups();
	
	@Query(value="SELECT p.* FROM permission_group p JOIN permission_group_member pgm on p.id = pgm.permission_group_id " +
				"WHERE pgm.permission_group_status_id = 0 AND pgm.account_id = ?1", nativeQuery = true)
	List<PermissionGroup> getGrantedGroups(Long accountId);
	
}
