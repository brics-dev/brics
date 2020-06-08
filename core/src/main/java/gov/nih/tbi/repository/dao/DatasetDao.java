package gov.nih.tbi.repository.dao;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Set;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.repository.model.SubmissionType;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.VisualizationDataset;

public interface DatasetDao extends GenericDao<Dataset, Long> {

	/**
	 * Querys the database for a list of results based on the search string and the
	 * pagination data. Does not load lazy loaded properties of dataset.
	 * 
	 * @param key
	 * @param datasetStatus
	 * @param requested     : true if we want to filter by the requested status
	 *                      instead of the current status
	 * @param pageData
	 * @return
	 */
	public List<Dataset> search(Set<Long> ids, String key, DatasetStatus datasetStatus, boolean requested,
			PaginationData pageData);

	/**
	 * Querys the database for a list of results based on the search string and the
	 * pagination data. Does not load lazy loaded properties of dataset.
	 * 
	 * @param key
	 * @param searchColumns
	 * @param datasetStatus
	 * @param requested     : true if we want to filter by the requested status
	 *                      instead of the current status
	 * @param pageData
	 * @return
	 */
	public List<Dataset> search(Set<Long> ids, String key, List<String> searchColumns, DatasetStatus currentStatus,
			DatasetStatus requestStatus, PaginationData pageData);

	/**
	 * Counts the total number of datasets.
	 * 
	 * @return Long count of datasets in the table
	 */
	public int countAll();

	/**
	 * Gets all the datasets with the given ids. EAGERly gets all one-to-many
	 * dataset properties.
	 * 
	 * @param ids
	 * @return
	 */
	public List<Dataset> getByIds(Set<Long> ids);

	/**
	 * Get all the datasets of status UPLOADING with the given user. EAGERly gets
	 * all the one-to-many relationships of Dataset.
	 * 
	 * @param user
	 * @return
	 */
	public List<Dataset> getUploadingDataset(User user);

	/**
	 * returns the number of datasets with the given status as either the status or
	 * requested status as specified by requested.
	 * 
	 * @param status
	 * @param requested
	 * @return
	 */
	public Long getStatusCount(DatasetStatus status, boolean requested);

	/**
	 * Returns the dataset with the specified name and study. EAGERly gets all
	 * one-to-many properties of dataset.
	 * 
	 * @param study
	 * @param name
	 * @return
	 */
	public Dataset getDatasetByName(String studyName, String datasetName);

	/**
	 * Returns the dataset by the prefix id
	 * 
	 * @param datasetId
	 * @return
	 */
	public Dataset getByPrefixedId(String datasetId);

	/**
	 * Gets the datasets belonging to the given study. Eager loads the dataset files
	 * 
	 * @param study
	 * @return
	 */
	public List<Dataset> getDatasetsByStudy(Study study);

	public List<Long> getDatasetIdsByStudyIds(Set<Long> studyIds, Set<DatasetStatus> statuses);

	public List<Dataset> getByStatuses(Set<DatasetStatus> statuses);

	/**
	 * Gets the dataset by prefixed ID, but will not join the datasetSubject table.
	 * Joinining it causes problems in dataset retriever when there are too many
	 * subjects joined.
	 * 
	 * @param datasetId
	 * @return
	 */
	public Dataset getByPrefixedIdWithoutSubjects(String datasetId);

	public String getDatasetFilePath(String studyPrefixedId, String datasetName, String fileName);

	public List<Dataset> getByStatusesAndDate(Date cutOffDate, DatasetStatus datasetStatus);

	/**
	 * Gets the dataset by its prefixed ID, this version of the get will include
	 * researchManagement info, which is usually lazy loaded.
	 * 
	 * @param datasetId
	 * @return
	 */
	public Dataset getByPrefixedIdWithStudyInfo(String datasetId);

	public Dataset getDatasetWithChildren(Long id);

	public List<Long> getDatasetIdsByStudyIds(Set<Long> studyIds);

	public Dataset getDatasetWithFiles(Long id);

	public Dataset getDatasetExcludingDatasetFiles(Long datasetId);

	public Dataset getDataset(Long id);

	public int updateDatasetStatus(DatasetStatus status, Long datasetId);

	public List<Long> getDatasetIdsByStatus(Set<DatasetStatus> statuses);

	public List<Dataset> getDatasetByStatuses(Set<DatasetStatus> statuses);

	public BigInteger getDatasetUserFiles(Long userFileId);

	List<VisualizationDataset> getVisualizationStudyDatasetByStudy(Study study);

	public List<Integer> getSubmissionTypeseByDatasetId(Long datasetId);
}
