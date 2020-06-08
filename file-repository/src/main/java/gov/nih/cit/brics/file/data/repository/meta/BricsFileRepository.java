package gov.nih.cit.brics.file.data.repository.meta;

import org.springframework.data.repository.CrudRepository;

import gov.nih.tbi.file.model.hibernate.BricsFile;

public interface BricsFileRepository extends CrudRepository<BricsFile, String> {

}
