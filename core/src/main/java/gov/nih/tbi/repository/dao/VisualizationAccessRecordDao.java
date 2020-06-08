package gov.nih.tbi.repository.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.DataSource;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.repository.model.hibernate.AccessRecord;
import gov.nih.tbi.repository.model.hibernate.VisualizationAccessData;
import gov.nih.tbi.repository.model.hibernate.VisualizationAccessRecord;

public interface VisualizationAccessRecordDao extends GenericDao<VisualizationAccessRecord, Long> {

	/**
	 * Given the id of a study, this function will return all access records that are for datasets
	 * within the given study.
	 * 
	 * @param studyId
	 * @return
	 */
	public List<VisualizationAccessRecord> getAll(Long studyId, Date StartDate, Date endDate, PaginationData pageData);
	

	/**
	 * This function will return all access records that are for datasets
	 * 
	 * @return
	 */
	public List<VisualizationAccessData> getAllAccessRecords();
	
	
}
