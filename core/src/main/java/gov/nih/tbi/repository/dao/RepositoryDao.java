
package gov.nih.tbi.repository.dao;

import java.util.List;
import java.util.Map;

import gov.nih.tbi.repository.model.GenericTable;
import gov.nih.tbi.repository.model.hibernate.DataStoreInfo;
import gov.nih.tbi.repository.model.hibernate.DataStoreTabularInfo;
import gov.nih.tbi.repository.model.hibernate.SubmissionRecordJoin;

public interface RepositoryDao
{

    public SubmissionRecordJoin getSubmissionRecordJoin(Long id);
    
    public Map<String, GenericTable> queryByDataStoreInfo(DataStoreInfo dsInfo);
    
    /**
     * Queries all the tables in the data store info (sans joins) and return the data as a hash map of table names to a list of maps of
     * column names to column value
     * 
     * @param dsInfo
     * @return
     */
    public Map<String, GenericTable> queryByDataStoreInfo(DataStoreInfo dsInfo, List<Long> nonArchivedDatasetIds);

    @Deprecated
    public List joinDataStoreInfo(DataStoreInfo dsInfo, Integer limit);

    /**
     * Generate a SQL query that will pull all data for all repeatable groups for a given dataset. If datasetId is null,
     * it will pull all data for all datasets (this was the original behavior). The logic was originally part of
     * joinDataStoreInfo(DataStoreInfo, Integer) but has been pulled out, since that method actually executes against
     * the database. This just returns the SQL that will be executed. (joinDataStoreInfo does call this method to
     * generate its own SQL/results)
     * 
     * @param dsInfo
     * @param limit
     * @param datasetId
     * @return
     */
    @Deprecated
    public String generateJoinDataStoreInfoQuery(DataStoreInfo dsInfo, Integer limit, Long datasetId);
    
    public GenericTable queryByDataStoreTabInfo(DataStoreTabularInfo tabInfo, List<Long> datasetIds);
}
