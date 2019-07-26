
package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.BasicDaoTest;
import gov.nih.tbi.ContextManager;
import gov.nih.tbi.DatabaseSetup;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Disease;

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
 * Test for DiseaseDao
 * 
 * @author Francis Chen
 */
public class DiseaseDaoTest extends BasicDaoTest
{

    static Logger logger = Logger.getLogger(DiseaseDaoTest.class);

    private DiseaseDao diseaseDao;
    private static String dataSource = "metaConnection";
    private static String sampleData = "src/test/resources/dictionaryTestData.xml";
    private Set<Long> ids;

    @BeforeMethod(alwaysRun = true)
    protected void setUp() throws DatabaseUnitException, SQLException, FileNotFoundException
    {

        diseaseDao = (DiseaseDao) ContextManager.getBean("diseaseDao");

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

        logger.info("================ BEGIN TEST (testSearch) ====================");

        List<Disease> diseaseList = diseaseDao.getAll();

        logger.info("Number of Diseases returned: " + diseaseList.size());
        String out = "";
        for (Disease disease : diseaseList)
        {
            out = out + disease.toString() + ", ";
        }
        logger.info("Output: " + out);
        Assert.assertTrue(diseaseList.size() == 2);
        Assert.assertTrue(diseaseList.get(0).getId() == 2);
        Assert.assertTrue(diseaseList.get(1).getId() == 1);
    }
}
