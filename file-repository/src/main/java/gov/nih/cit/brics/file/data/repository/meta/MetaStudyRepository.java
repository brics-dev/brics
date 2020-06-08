package gov.nih.cit.brics.file.data.repository.meta;

import org.springframework.data.repository.CrudRepository;

import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;

public interface MetaStudyRepository extends CrudRepository<MetaStudy, Long> {

}
