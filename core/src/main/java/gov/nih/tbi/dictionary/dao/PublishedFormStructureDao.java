package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.PublishedFormStructure;

public interface PublishedFormStructureDao extends GenericDao<PublishedFormStructure, Long>{
	
	public PublishedFormStructure getFormStructurePublished(Long formStructureId,Long diseaseid);

}
