
package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.BasicDaoTest;
import gov.nih.tbi.ContextManager;
import gov.nih.tbi.DatabaseSetup;
import gov.nih.tbi.dictionary.model.hibernate.Classification;

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

public class ClassificationDaoTest extends BasicDaoTest
{

    static Logger logger = Logger.getLogger(DiseaseDaoTest.class);

    private ClassificationDao classificationDao;
    private static String dataSource = "metaConnection";
    private static String sampleData = "src/test/resources/dictionaryTestData.xml";
    private Set<Long> ids;

    @BeforeMethod(alwaysRun = true)
    protected void setUp() throws DatabaseUnitException, SQLException, FileNotFoundException
    {

        classificationDao = (ClassificationDao) ContextManager.getBean("classificationDao");

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

        logger.info("================ BEGIN TEST (Classification getAll() ====================");

        List<Classification> classificationList = classificationDao.getAll();

        logger.info("Number of Classifications returned: " + classificationList.size());
        String out = "";
        for (Classification classification : classificationList)
        {
            out = out + classification.toString() + ", ";
        }
        logger.info("Output: " + out);
        Assert.assertTrue(classificationList.size() == 2);
        Assert.assertTrue(classificationList.get(0).getId() == 2);
        Assert.assertTrue(classificationList.get(1).getId() == 1);
    }

    @Test(testName = "Database")
    public void testGetUserList() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST ( Classification getAll() ) ====================");

        List<Classification> classificationList = classificationDao.getUserList();
        List<Classification> fullList = classificationDao.getAll();

        logger.info("Number of Classifications returned: " + classificationList.size());
        String out = "";
        for (Classification classification : classificationList)
        {
            out = out + classification.toString() + ", ";
        }
        logger.info("Output: " + out);

        // Make sure that every and only the classifications with canCreate are on the list.
        outer: for (Classification c : classificationList)
        {
            for (Classification f : fullList)
            {
                if (c.getId().equals(f.getId()))
                {
                    if (!c.getCanCreate())
                    {
                        Assert.fail(c.getName() + " is on the list without being canCreate.");
                    }
                    continue outer;
                }
            }
            if (c.getCanCreate())
            {
                Assert.fail(c.getName() + " is not on the list but is is canCreate.");
            }
        }
    }
}
