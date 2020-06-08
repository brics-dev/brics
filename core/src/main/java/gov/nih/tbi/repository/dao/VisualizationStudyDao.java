
package gov.nih.tbi.repository.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.VisualizationStudy;

import java.util.List;
import java.util.Map;

public interface VisualizationStudyDao extends GenericDao<VisualizationStudy, Long> {

	public Map<Long, VisualizationStudy> getAllVisualizationStudyData();

	public List<Study> getAllVisualization();

}
