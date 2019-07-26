
package gov.nih.tbi.repository.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.hibernate.DatasetSubject;

public interface DatasetSubjectDao extends GenericDao<DatasetSubject, Long>
{

    /**
     * Gets the DatasetSubject by SecureSubject
     * 
     * @param subject
     * @return
     */
    public List<DatasetSubject> getDatasetSubjectListByGuid(String guid);
    
}
