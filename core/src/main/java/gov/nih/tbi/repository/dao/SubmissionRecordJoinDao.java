package gov.nih.tbi.repository.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.hibernate.SubmissionRecordJoin;

import java.util.List;

public interface SubmissionRecordJoinDao extends GenericDao<SubmissionRecordJoin, Long> {

	/**
	 * Gets a list of submission records by dataset ID
	 * 
	 * @param datasetId
	 * @return
	 */
	public List<SubmissionRecordJoin> getByDatasetId(Long datasetId);

	/**
	 * Returns the number of entries in the submissionRecordJoin table with the given dataset id.
	 * This is the number of total records in a dataset.
	 * 
	 * @param datasetId
	 * @return
	 */
	public Long getNumRecordsInDataset(Long datasetId);
}
