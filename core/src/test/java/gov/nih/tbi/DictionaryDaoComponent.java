
package gov.nih.tbi;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.dictionary.dao.AliasDao;
import gov.nih.tbi.dictionary.dao.DataElementDao;
import gov.nih.tbi.dictionary.dao.DataStructureDao;
import gov.nih.tbi.dictionary.dao.KeywordSparqlDao;
import gov.nih.tbi.dictionary.dao.MapElementDao;
import gov.nih.tbi.dictionary.dao.RepeatableGroupDao;

public class DictionaryDaoComponent extends DaoComponent
{

    static Logger logger = Logger.getLogger(DictionaryDaoComponent.class);

    /**********************************************************************************/

    @Autowired
    DataStructureDao dataStructureDao;

    @Autowired
    MapElementDao mapElementDao;

    @Autowired
    DataElementDao dataElementDao;

    @Autowired
    KeywordSparqlDao keywordDao;

    @Autowired
    RepeatableGroupDao repeatableGroupDao;

    @Autowired
    AliasDao aliasDao;

    /**********************************************************************************/

}
