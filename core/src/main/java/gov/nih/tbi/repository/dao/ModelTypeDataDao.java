
package gov.nih.tbi.repository.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.alzped.MetaStudyModelType;
import gov.nih.tbi.repository.model.alzped.ModelTypeData;
import gov.nih.tbi.repository.model.alzped.StudyModelType;

public interface ModelTypeDataDao extends GenericDao<ModelTypeData, Long>
{
	public int saveModelTypeData(StudyModelType studyModelType, String studyId);
	
	public int deleteModelTypeData(StudyModelType s, String studyId);

	public int saveModelTypeData(MetaStudyModelType metaStudyModelType, String metaStudyId);
	
	public int deleteModelTypeData(MetaStudyModelType s, String metaStudyId);
	
	public int saveModelTypeDataBulk(List<MetaStudyModelType> metaStudyModelTypes, String metaStudyId);
	
	public int deleteModelTypeDataBulk(List<MetaStudyModelType> metaStudyModelTypes, String metaStudyId);
}
