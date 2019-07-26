package gov.nih.tbi.metastudy.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyData;

public interface MetaStudyDataDao extends GenericDao<MetaStudyData, Long> {

	
	public boolean isMetaStudyDataTitleUnique(String fileName, long metaStudyId);
	
}
