
package gov.nih.tbi.repository.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.TherapeuticTargetData;

public interface TherapeuticTargetDataDao extends GenericDao<TherapeuticTargetData, Long>
{
	public int saveTherapeuticTargetData(StudyTherapeuticTarget studyTherapeuticTarget, String studyId);
	
	public int deleteTherapeuticTargetData(StudyTherapeuticTarget s, String studyId);
	
	public int saveTherapeuticTargetData(MetaStudyTherapeuticTarget metaStudyTherapeuticTarget, String metaStudyId);
	
	public int deleteTherapeuticTargetData(MetaStudyTherapeuticTarget s, String metaStudyId);

	public int saveTherapeuticTargetDataBulk(List <MetaStudyTherapeuticTarget> metaStudyTherapeuticTargets, String metaStudyId);
	
	public int deleteTherapeuticTargetDataBulk(List <MetaStudyTherapeuticTarget> metaStudyTherapeuticTargets, String metaStudyId);
}
