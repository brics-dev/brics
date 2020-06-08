package gov.nih.tbi.api.query.data.repository;

import org.springframework.data.repository.CrudRepository;

import gov.nih.tbi.api.query.data.repository.custom.DatasetRepoCustom;
import gov.nih.tbi.repository.model.hibernate.Dataset;

public interface DatasetRepository extends CrudRepository<Dataset, Long>, DatasetRepoCustom {

}
