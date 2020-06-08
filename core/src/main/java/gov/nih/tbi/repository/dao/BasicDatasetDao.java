package gov.nih.tbi.repository.dao;

import java.util.List;
import java.util.Set;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;

public interface BasicDatasetDao extends GenericDao<BasicDataset, Long> {

	/**
	 * 
	 * @param datasets
	 * @return
	 */
	List<BasicDataset> getByIds(Set<Long> datasets);

	/**
	 * Returns a projection of ids of all basic dataset objects that are in Private or Shared status
	 * 
	 * @return
	 */
	public List<Long> getPrivateSharedDatasetIds();

	public List<BasicDataset> getPrivateSharedDatasets();

	public Long getDatasetIdByDatasetFileId(Long datasetFileId);
}
