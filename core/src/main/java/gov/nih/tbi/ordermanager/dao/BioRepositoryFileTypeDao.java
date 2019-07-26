
package gov.nih.tbi.ordermanager.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.ordermanager.model.BioRepositoryFileType;

public interface BioRepositoryFileTypeDao extends GenericDao<BioRepositoryFileType, Long>
{

    public BioRepositoryFileType get(String fileType);
}
