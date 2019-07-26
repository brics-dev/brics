
package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.BasicDaoTest;
import gov.nih.tbi.ContextManager;
import gov.nih.tbi.DatabaseSetup;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test file for MapElementDao
 * 
 * @author mvalei
 * 
 */
public class MapElementDaoTest extends BasicDaoTest
{

    static Logger logger = Logger.getLogger(MapElementDaoTest.class);

    private MapElementDao mapElementDao;
    private static String dataSource = "metaConnection";
    private static String sampleData = "src/test/resources/dictionaryTestData.xml";

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws DatabaseUnitException, SQLException, FileNotFoundException
    {

        mapElementDao = (MapElementDao) ContextManager.getBean("mapElementDao");

        DatabaseSetup.setup(dataSource, sampleData);

    }

    /**
     * This test is no longer necessary because DeleteAll has been deprecated
     * 
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws DatabaseUnitException
     * @throws SQLException
     * @throws FileNotFoundException
     */
    @SuppressWarnings("deprecation")
    @Test(testName = "database")
    public void testDeleteAll() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            DatabaseUnitException, SQLException, FileNotFoundException
    {

        Collection<MapElement> list = new LinkedHashSet<MapElement>();
        for (Long i = 1L; i < 5L; i++)
        {
            list.add(mapElementDao.get(i));
        }

        mapElementDao.deleteAll(list);
        list = mapElementDao.getAll();

        for (MapElement me : list)
        {
            Assert.assertTrue(me.getId() >= 5);
        }

    }

    @DataProvider(name = "testGetByNameDataProvider")
    public Object[][] testGetByNameDataProvider() throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, DatabaseUnitException, SQLException, FileNotFoundException
    {

        return new Object[][] { { "comments" } };
    }

    @Test(testName = "database", dataProvider = "testGetByNameDataProvider")
    public void testGetByName(String name) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testGetByName) ====================");

        // Assert.assertFalse(mapElementDao.getByName(name).isEmpty());
    }

}
