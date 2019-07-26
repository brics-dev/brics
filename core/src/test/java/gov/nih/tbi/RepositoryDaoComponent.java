
package gov.nih.tbi;

import gov.nih.tbi.repository.dao.DataStoreBinaryInfoDao;
import gov.nih.tbi.repository.dao.DataStoreInfoDao;
import gov.nih.tbi.repository.dao.DataStoreTabularColumnInfoDao;
import gov.nih.tbi.repository.dao.DataStoreTabularInfoDao;
import gov.nih.tbi.repository.dao.DatafileEndpointInfoDao;
import gov.nih.tbi.repository.dao.UserFileDao;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class RepositoryDaoComponent extends DaoComponent
{

    static Logger logger = Logger.getLogger(RepositoryDaoComponent.class);

    /**********************************************************************************/

    @Autowired
    DatafileEndpointInfoDao dataFileEndpointInfoDao;

    @Autowired
    DataStoreBinaryInfoDao dataStoreBinaryInfoDao;

    @Autowired
    DataStoreInfoDao dataStoreInfoDao;

    @Autowired
    DataStoreTabularColumnInfoDao dataStoreTabularColumnInfoDao;

    @Autowired
    DataStoreTabularInfoDao dataStoreTabularInfoDao;

    @Autowired
    UserFileDao userFileDao;

    /**********************************************************************************/

}
