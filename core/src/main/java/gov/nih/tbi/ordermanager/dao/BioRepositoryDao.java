
package gov.nih.tbi.ordermanager.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.ordermanager.model.BioRepository;

public interface BioRepositoryDao extends GenericDao<BioRepository, Long>
{

    public BioRepository findByName(String name);

}
