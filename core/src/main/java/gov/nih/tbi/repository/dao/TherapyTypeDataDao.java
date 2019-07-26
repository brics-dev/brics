
package gov.nih.tbi.repository.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapyType;
import gov.nih.tbi.repository.model.alzped.StudyTherapyType;
import gov.nih.tbi.repository.model.alzped.TherapyTypeData;

public interface TherapyTypeDataDao extends GenericDao<TherapyTypeData, Long>
{
	public int saveTherapyTypeData(StudyTherapyType studyTherapyType, String studyId);
	
	public int deleteTherapyTypeData(StudyTherapyType s, String studyId);
	
    public int saveTherapyTypeData(MetaStudyTherapyType metaStudyTherapyType, String metaStudyId);
	
	public int deleteTherapyTypeData(MetaStudyTherapyType s, String metaStudyId);
	
	public int saveTherapyTypeDataBulk(List <MetaStudyTherapyType> metaStudyTherapyTypes, String metaStudyId);
	
	public int deleteTherapyTypeDataBulk(List <MetaStudyTherapyType> s, String metaStudyId);
}
