
package gov.nih.tbi.account.dao;

import gov.nih.tbi.BasicDaoTest;
import gov.nih.tbi.ContextManager;
import gov.nih.tbi.DatabaseSetup;
import gov.nih.tbi.commons.model.hibernate.State;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StateDaoTest extends BasicDaoTest
{

    static Logger logger = Logger.getLogger(State.class);

    private static String dataSource = "metaConnection";
    private static String sampleData = "src/test/resources/accountTestData.xml";

    private StateDao stateDao;

    @BeforeMethod(alwaysRun = true)
    protected void setUp() throws DatabaseUnitException, SQLException, FileNotFoundException
    {

        stateDao = (StateDao) ContextManager.getBean("stateDao");

        DatabaseSetup.setup(dataSource, sampleData);
    }

    @Test
    public void testGetAll() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testGetByUserName) ====================");

        List<State> states = stateDao.getAll();

        Assert.assertTrue(states.size() == 3);
        Assert.assertEquals(states.get(0).getName(), "Hawaii");
        Assert.assertEquals(states.get(1).getName(), "Maryland");
    }
}
