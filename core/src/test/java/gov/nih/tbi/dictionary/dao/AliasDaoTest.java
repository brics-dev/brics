
package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.BasicDaoTest;
import gov.nih.tbi.ContextManager;
import gov.nih.tbi.DatabaseSetup;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test file for AliasDao
 * 
 * @author mvalei
 * 
 */
public class AliasDaoTest extends BasicDaoTest
{

    static Logger logger = Logger.getLogger(AliasDaoTest.class);

    private AliasDao aliasDao;
    private static String dataSource = "metaConnection";
    private static String sampleData = "src/test/resources/dictionaryTestData.xml";

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws DatabaseUnitException, SQLException, FileNotFoundException
    {

        aliasDao = (AliasDao) ContextManager.getBean("aliasDao");

        DatabaseSetup.setup(dataSource, sampleData);

    }

    @DataProvider(name = "testGetAliasByNameDataProvider")
    public Object[][] testGetAliasByNameDataProvider() throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, DatabaseUnitException, SQLException, FileNotFoundException
    {

        // These should be correct names of aliases in the test data
        return new Object[][] { { "Alias1" }, { "Alias2" }, { "Blood Type" }, { "alias1" } };
    }

    @Test(testName = "database", dataProvider = "testGetAliasByNameDataProvider")
    public void testGetAliasByName(String name) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testGetAliasByName) ====================");
        Assert.assertEquals(aliasDao.getAliasByName(name).getName().toUpperCase(), name.toUpperCase());
    }

    @DataProvider(name = "testGetAliasByNameExpectedFailureDataProvider")
    public Object[][] testGetAliasByNameExpectedFailureDataProvider() throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException, DatabaseUnitException, SQLException,
            FileNotFoundException
    {

        // These should be invalid names of aliases in the test data so the getAliasByName function will return null
        return new Object[][] { { null }, { "Alias" }, { "BloodType" }, { "" } };
    }

    @Test(testName = "database", dataProvider = "testGetAliasByNameExpectedFailureDataProvider")
    public void testGetAliasByNameExpectedFailure(String name) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testGetAliasByNameExpectedFailure) ====================");
        Assert.assertEquals(aliasDao.getAliasByName(name), null);
    }
}
