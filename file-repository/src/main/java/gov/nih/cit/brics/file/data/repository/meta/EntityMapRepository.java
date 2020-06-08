package gov.nih.cit.brics.file.data.repository.meta;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.EntityType;

public interface EntityMapRepository extends CrudRepository<EntityMap, Long> {
	public List<EntityMap> findAllByEntityIdAndType(Long entityId, EntityType type);
}
