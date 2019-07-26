package gov.nih.tbi.metastudy.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.metastudy.model.hibernate.ResearchManagementMeta;

public interface ResearchManagementMetaDao extends GenericDao<ResearchManagementMeta, Long> {

	public ResearchManagementMeta getMetastudyPrimaryInvestigator(Long metastudyId);
	
	public ResearchManagementMeta getMetaStudyManagementImage(Long studyId, Long rmId);
}
