package gov.nih.tbi.repository.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.hibernate.BasicStudy;
import gov.nih.tbi.repository.model.hibernate.BasicStudySearch;

public interface BasicStudyDao extends GenericDao<BasicStudy, Long> {
	
	public BasicStudy getBasicStudyByPrefixId(String prefixId);
	
	/**
	 * This is a search that allows the public site to view study data. It returns a custom POJO
	 * since this search needs sub queries in the select clause. This allows for direct marshalling
	 * into JSON at the service layer and reduces clutter in the Study and BasicStudy Pojo
	 * since this is only used for the search view
	 * @return
	 */
	public List<BasicStudySearch> getPublicSiteSearchBasicStudies();
}
