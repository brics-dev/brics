package gov.nih.tbi.repository.dao;

import java.util.Date;
import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.DataSource;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.repository.model.hibernate.AccessRecord;

public interface AccessRecordDao extends GenericDao<AccessRecord, Long> {

	/**
	 * Given the id of a study, this function will return all access records that are for datasets
	 * within the given study.
	 * 
	 * @param studyId
	 * @return
	 */
	public List<AccessRecord> getAll(Long studyId, Date StartDate, Date endDate, PaginationData pageData);
	
	/**
	 * Given list of study ids, this function will return all access records that are for datasets
	 * within the given studies.
	 * 
	 * @param studyIdList
	 * @return
	 */
	public List<AccessRecord> getByStudyIdList(List<Long> studyIdList, Date StartDate, Date endDate, PaginationData pageData);
	
	//@formatter:off
	/**
	 * Performs a search across the accessRecord table based on a variety of paramters. (Does not
	 * support pagination. The pageData object is used only for sorting and ascending currently).
	 * 
	 * The SQL query:	SELECT * FROM access_record AS ac 
	 * 						LEFT JOIN account a on ac.account_id = a.id
	 * 						LEFT JOIN dataset d on a.dataset_id = d.id
	 * 						LEFT JOIN study s on d.study=s.id
	 * 						WHERE (
	 * 							d.prefix_id ilike %[datasetId]% OR
	 * 							d.name ilike %[dsName]% OR
	 * 							d.dataset_status_id=[status.getId()] OR
	 * 							data_source = [dataSource.getId()] OR
	 * 							a.user_name ilike %[uesrname]% OR
	 * 							date_trunc('day', queue_date=TIMESTAMP [queueDate.isoFormat()] OR
	 * 							ac.record_count=[numRec]
	 * 							
	 * 						)
	 * 						AND date_trunc('day', queue_date)>=TIMESTAMP [oldestDate.isoFormat()]
	 * 						AND s.id=[studyId]
	 * 						ORDER BY [ ASC | DESC ] [pageData.getSort()];
	 * 			
	 * 
	 * @param studyId
	 * @param dsId : If non-null then adds (accessRecord.dataset.id == dsId) logic to hibernate query
	 * @param recCount : If non-null then adds (accessRecord.recordCount == numRec) logic to hibernate query
	 * @param dsName : If non-null then adds (accessRecord.dataset.name contains dsName) logic to hibernate query
	 * @param status : If non-null then adds (accessRecord.dataset.status is status) logic to hibernate query
	 * @param dataSource : If non-null then adds (accessRecord.dataSource is dataSource) logic to hibernate query
	 * @param username : If non-null then adds (accessRecord.account.username contains username) logic to hibernate query
	 * @param queueDate : If non-null then adds (accessRecords.queueDate == queueDate) logic to hibernate query
	 * @param oldestDate : If non-null then adds (accessRecords.queueDate <= queueDate) logic to hibernate query
	 * @param pageData
	 * @return
	 */
	public List<AccessRecord> searchAccessRecords(Long studyId, String dsId, Long recCount, String dsName,DatasetStatus status,
			String searchText, Long dataSource, String username, Date queueDate, Date oldestDate,
			PaginationData pageData);

	//@formatter:on

	/**
	 * Given a study, returns the number of access records for that study.
	 * 
	 * @param datasetId
	 * @return
	 */
	public int countAccessRecordsStudy(Long studyId);

	/**
	 * Given a datasetId, returns the number of access records for that dataset.
	 * 
	 * @param datasetId
	 * @return
	 */
	public int countAccessRecords(Long datasetId);

	/**
	 * Fetch access records based in datasetid
	 * @param datasetId
	 * @return List of AccessRecord
	 */
	public List<AccessRecord> getAccessRecordsByDataset(Long datasetId);

}
