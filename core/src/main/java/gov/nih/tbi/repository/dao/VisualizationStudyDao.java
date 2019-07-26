
package gov.nih.tbi.repository.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.VisualizationStudy;

import java.util.List;

public interface VisualizationStudyDao extends GenericDao<VisualizationStudy, Long> {

	public List<VisualizationStudy> getAllVisualizationStudyData();

	public List<Study> getAllVisualization();

}
