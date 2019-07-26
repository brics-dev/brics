package gov.nih.tbi.metastudy.dao;

import java.util.Date;
import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyAccessRecord;


public interface MetaStudyAccessRecordDao extends GenericDao<MetaStudyAccessRecord, Long> {
	
	public List<MetaStudyAccessRecord> searchAccessRecords(Long metaStudyId, String searchText, Date queueDate,
			Date oldestDate,
			PaginationData pageData);
	
	public List<MetaStudyAccessRecord> getAccessRecordByMetaStudyId(Long id);
	
	public int countAccessRecords(Long datasetId);
	
}