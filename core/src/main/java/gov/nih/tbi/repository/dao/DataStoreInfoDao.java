
package gov.nih.tbi.repository.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.repository.model.hibernate.DataStoreInfo;

import java.util.List;

public interface DataStoreInfoDao extends GenericDao<DataStoreInfo, Long>
{

    /**
     * Queries the DATASTORE_INFO table and sorts by the specified column in the specified order
     * 
     * @param sortColumn
     * @param sortOrder
     * @return
     */
    List<DataStoreInfo> getAllSorted(String sortColumn, boolean sortAsc);

    /**
     * Returns the DataStoreInfo with the matching Data_Structure_Id
     * 
     * @param id
     *            of the data structure
     * @return
     */
    DataStoreInfo getByDataStructureId(Long dataStructureId);

    /**
     * Querys the database for a page of DataStoreInfo entires based on the serach criteria
     * 
     * @param key
     * @param tabular
     * @param federated
     * @param archived
     * @param pageData
     * @return
     */
    public List<DataStoreInfo> search(String key, Boolean tabular, Boolean federated, Boolean archived,
            PaginationData pageData);
}
