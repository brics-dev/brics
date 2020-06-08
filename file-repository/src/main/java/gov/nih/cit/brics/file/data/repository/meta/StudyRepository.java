package gov.nih.cit.brics.file.data.repository.meta;

import org.springframework.data.repository.CrudRepository;

import gov.nih.tbi.repository.model.hibernate.Study;

public interface StudyRepository extends CrudRepository<Study, Long> {

}
