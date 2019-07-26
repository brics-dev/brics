
package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.BasicDaoTest;
import gov.nih.tbi.ContextManager;
import gov.nih.tbi.DatabaseSetup;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test file for RepeatableGroupDao
 * 
 * @author mvalei
 * 
 */
public class RepeatableGroupDaoTest extends BasicDaoTest
{

    static Logger logger = Logger.getLogger(RepeatableGroupDaoTest.class);

    private RepeatableGroupDao repeatableGroupDao;
    private static String dataSource = "metaConnection";
    private static String sampleData = "src/test/resources/dictionaryTestData.xml";

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws DatabaseUnitException, SQLException, FileNotFoundException
    {

        repeatableGroupDao = (RepeatableGroupDao) ContextManager.getBean("repeatableGroupDao");

        DatabaseSetup.setup(dataSource, sampleData);

    }

    @DataProvider(name = "testGetIdsByNameAndDSDataProvider")
    public Object[][] testGetIdsByNameAndDSDataProvider() throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, DatabaseUnitException, SQLException, FileNotFoundException
    {

        // These inputs correspond to keyword_element entries in the test database
        return new Object[][] { { 1L, "ForDS1", 1L }, { 5L, "ForDS5", 5L } };
    }

    @Test(testName = "database", dataProvider = "testGetIdsByNameAndDSDataProvider")
    public void testGetIdsByNameAndDS(Long dsId, String name, Long id) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException, DatabaseUnitException, SQLException,
            FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testGetIdsByNameAndDS) ====================");
        if (name != null && dsId != null)
        {
            List<Long> rgIdList = repeatableGroupDao.getIdsByNameAndDS(name, dsId);
            Assert.assertEquals(rgIdList.size(), 1);
            Assert.assertEquals(rgIdList.get(0), id);
        }
        else
        {
            Assert.assertTrue(false, "Bad inputs in testGetIdsByNameAndDSDataProvider. Inputs should be non-null");
        }
    }

    @DataProvider(name = "testGetIdsByNameAndDSExpectedFailureDataProvider")
    public Object[][] testGetIdsByNameAndDSExpectedFailureDataProvider() throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException, DatabaseUnitException, SQLException,
            FileNotFoundException
    {

        // These inputs should result in the getIdsByNameAndDS funtion returning an empty list
        return new Object[][] { { null, "ForDS1" }, { 5L, null }, { 56L, "ForDS1" }, { 2L, "ForDS" },
                { 89L, "NotAName" } };
    }

    @Test(testName = "database", dataProvider = "testGetIdsByNameAndDSExpectedFailureDataProvider")
    public void testGetIdsByNameAndDSExpectedFailure(Long dsId, String name) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException, DatabaseUnitException, SQLException,
            FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testGetIdsByNameAndDSExpectedFailure) ====================");
        Assert.assertTrue(repeatableGroupDao.getIdsByNameAndDS(name, dsId).isEmpty());
    }
}
