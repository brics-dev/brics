package gov.nih.tbi.account.dao;

import java.util.List;

import gov.nih.tbi.account.model.hibernate.VisualizationEntityMap;
import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.EntityType;

public interface VisualizationEntityMapDao extends GenericDao< VisualizationEntityMap, Long > {

	public List<VisualizationEntityMap> getUserGrantedEntities(Long accountId, Long typeId);

}
