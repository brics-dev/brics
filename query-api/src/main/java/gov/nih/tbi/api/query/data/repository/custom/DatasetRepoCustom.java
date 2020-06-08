package gov.nih.tbi.api.query.data.repository.custom;

import java.util.List;
import java.util.Set;

import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.repository.model.hibernate.Dataset;

public interface DatasetRepoCustom {

	List<Dataset> getByStatuses(Set<DatasetStatus> statuses);
	
	List<Long> getDatasetIdsByStudyIds(Set<Long> studyIds, Set<DatasetStatus> statuses);
	
	List<Dataset> getByIds(Set<Long> ids);
}
