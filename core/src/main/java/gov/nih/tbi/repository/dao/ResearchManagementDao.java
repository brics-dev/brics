
package gov.nih.tbi.repository.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;

public interface ResearchManagementDao extends GenericDao<ResearchManagement, Long> {

	public ResearchManagement getStudyPrimaryInvestigator(Long studyId);
	
	public ResearchManagement getStudyManagementImage(Long studyId, Long rmId);
	
	public ResearchManagement getMetastudyPrimaryInvestigator(Long metastudyId);

	public ResearchManagement getMetaStudyManagementImage(Long metastudyId, Long rmId);
	
}
