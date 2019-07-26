
package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.BasicDaoTest;
import gov.nih.tbi.ContextManager;
import gov.nih.tbi.DatabaseSetup;
import gov.nih.tbi.dictionary.model.hibernate.SubDomain;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test for SubDomainDao
 * 
 * @author Francis Chen
 * 
 */
public class SubDomainDaoTest extends BasicDaoTest
{

    static Logger logger = Logger.getLogger(SubDomainDaoTest.class);

    private SubDomainDao subDomainDao;
    private static String dataSource = "metaConnection";
    private static String sampleData = "src/test/resources/dictionaryTestData.xml";
    private Set<Long> ids;

    @BeforeMethod(alwaysRun = true)
    protected void setUp() throws DatabaseUnitException, SQLException, FileNotFoundException
    {

        subDomainDao = (SubDomainDao) ContextManager.getBean("subDomainDao");

        DatabaseSetup.setup(dataSource, sampleData);

        if (ids == null)
        {
            ids = new HashSet<Long>();
        }

        ids.clear();
        for (long i = 0; i < 20; i++)
        {
            ids.add(i);
        }
    }

    @Test(testName = "Database")
    public void testGetAll() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testGetAll) ====================");

        List<SubDomain> subDomainList = subDomainDao.getAll();

        logger.info("Number of Domains returned: " + subDomainList.size());
        String out = "";
        for (SubDomain subDomain : subDomainList)
        {
            out = out + subDomain.toString() + ", ";
        }
        logger.info("Output: " + out);
        Assert.assertTrue(subDomainList.size() == 2);
        Assert.assertTrue(subDomainList.get(0).getId() == 2);
        Assert.assertTrue(subDomainList.get(1).getId() == 1);
    }
}
