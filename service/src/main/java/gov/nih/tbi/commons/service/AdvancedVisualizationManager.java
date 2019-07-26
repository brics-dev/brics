package gov.nih.tbi.commons.service;

import java.io.Serializable;
import java.util.List;

import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.VisualizationStudy;

public interface AdvancedVisualizationManager extends Serializable {
	
	public void buildFile();
	
	/**
	 * Returns a complete list of all studies with joins set to eager.  This is for the
	 * advanced visualization service so the chosen join fields are driven by that.
	 * 
	 * @return
	 */
	public List<VisualizationStudy> getAllStudiesVisualization();

}
