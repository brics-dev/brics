package gov.nih.tbi.api.query.data.repository;

import org.springframework.data.repository.CrudRepository;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.api.query.data.repository.custom.EntityMapRepoCustom;

public interface EntityMapRepository extends CrudRepository<EntityMap, Long>, EntityMapRepoCustom {

	
}
