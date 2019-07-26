
package gov.nih.tbi.account.dao;

import gov.nih.tbi.BasicDaoTest;
import gov.nih.tbi.ContextManager;
import gov.nih.tbi.DatabaseSetup;
import gov.nih.tbi.commons.model.hibernate.Country;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CountryDaoTest extends BasicDaoTest
{

    static Logger logger = Logger.getLogger(Country.class);

    private static String dataSource = "metaConnection";
    private static String sampleData = "src/test/resources/accountTestData.xml";

    private CountryDao countryDao;

    @BeforeMethod(alwaysRun = true)
    protected void setUp() throws DatabaseUnitException, SQLException, FileNotFoundException
    {

        countryDao = (CountryDao) ContextManager.getBean("countryDao");

        DatabaseSetup.setup(dataSource, sampleData);
    }

    @Test
    public void testGetAll() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testGetByUserName) ====================");

        List<Country> countries = countryDao.getAll();

        Assert.assertTrue(countries.size() == 3);
        Assert.assertEquals(countries.get(0).getName(), "United States of America");
        Assert.assertEquals(countries.get(1).getName(), "Canadia");
    }
}
