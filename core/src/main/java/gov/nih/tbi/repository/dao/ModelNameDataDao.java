
package gov.nih.tbi.repository.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.alzped.MetaStudyModelName;
import gov.nih.tbi.repository.model.alzped.ModelNameData;
import gov.nih.tbi.repository.model.alzped.StudyModelName;

public interface ModelNameDataDao extends GenericDao<ModelNameData, Long>
{
	public int saveModelNameData(StudyModelName studyModelName, String studyId);
	
	public int deleteModelNameData(StudyModelName s, String studyId);
	
	public int saveModelNameData(MetaStudyModelName studyModelName, String metaStudyId);
	
	public int deleteModelNameData(MetaStudyModelName s, String metaStudyId);
	
	public int saveModelNameDataBulk(List<MetaStudyModelName> studyModelNames, String metaStudyId);
	
	public int deleteModelNameDataBulk(List<MetaStudyModelName> studyModelNames, String metaStudyId);
}
