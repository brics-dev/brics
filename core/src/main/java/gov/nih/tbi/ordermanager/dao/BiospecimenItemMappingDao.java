package gov.nih.tbi.ordermanager.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.ordermanager.model.BiospecimenItemMapping;


public interface BiospecimenItemMappingDao extends GenericDao<BiospecimenItemMapping, Long>
{

    public BiospecimenItemMapping findByFormName(String formName);
}
