
package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.BasicDaoTest;
import gov.nih.tbi.ContextManager;
import gov.nih.tbi.DatabaseSetup;
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
 * Test file for KeywordDao
 * 
 * @author mvalei
 * 
 */
public class KeywordDaoTest extends BasicDaoTest
{

    static Logger logger = Logger.getLogger(KeywordDaoTest.class);

    private KeywordSparqlDao keywordDao;
    private static String dataSource = "metaConnection";
    private static String sampleData = "src/test/resources/dictionaryTestData.xml";

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws DatabaseUnitException, SQLException, FileNotFoundException
    {

        keywordDao = (KeywordSparqlDao) ContextManager.getBean("keywordDao");

        DatabaseSetup.setup(dataSource, sampleData);

    }

    @DataProvider(name = "testGetByKeywordDataProvider")
    public Object[][] testGetByKeywordDataProvider() throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, DatabaseUnitException, SQLException, FileNotFoundException
    {

        return new Object[][] { { "car", true }, { "boat", true }, { "train", true }, { "NotAKeyword", false } };
    }

    @Test(testName = "database", dataProvider = "testGetByKeywordDataProvider")
    public void testGetByKeyword(String key, Boolean valid) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testGetByKeyword) ====================");
        if (valid)
        {
            Assert.assertTrue(keywordDao.getByName(key).getKeyword().equals(key));
        }
        else
        {
            Assert.assertTrue(keywordDao.getByName(key) == null);
        }
    }

    @DataProvider(name = "testSearchDataProvider")
    public Object[][] testSearchDataProvider() throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, DatabaseUnitException, SQLException, FileNotFoundException
    {

        return new Object[][] { { "a" }, { "boat" }, { null } };
    }

    @Test(testName = "database", dataProvider = "testSearchDataProvider")
    public void testSearch(String string) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, DatabaseUnitException, SQLException, FileNotFoundException
    {

        List<Keyword> fullList = keywordDao.search("");
        List<Keyword> kList = keywordDao.search(string);
        for (Keyword key : fullList)
        {
            // This try block prevents the if statement from failing when string is null
            try
            {
                if (key.getKeyword().contains(string))
                {
                    Assert.assertTrue(kList.contains(key));
                }
                else
                {
                    Assert.assertFalse(kList.contains(key));
                }
            }
            catch (NullPointerException e)
            {
                Assert.assertTrue(kList.contains(key));
            }
        }
    }
}
