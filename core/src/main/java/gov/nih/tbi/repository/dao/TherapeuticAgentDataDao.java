
package gov.nih.tbi.repository.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.TherapeuticAgentData;

public interface TherapeuticAgentDataDao extends GenericDao<TherapeuticAgentData, Long>
{
	public int saveTherapeuticAgentData(StudyTherapeuticAgent studyTherapeuticAgent, String studyId);
	
	public int saveTherapeuticAgentDataBulk(List<MetaStudyTherapeuticAgent>studyTherapeuticAgent,String studyId );

	public int deleteTherapeuticAgentData(StudyTherapeuticAgent s, String studyId);
	
	public int saveTherapeuticAgentData(MetaStudyTherapeuticAgent studyTherapeuticAgent, String metaStudyId);

	public int deleteTherapeuticAgentData(MetaStudyTherapeuticAgent s, String metaStudyId);
	
	public int deleteTherapeuticAgentDataBulk(List<MetaStudyTherapeuticAgent>studyTherapeuticAgent,String studyId);
}
