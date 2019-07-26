
package gov.nih.tbi;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

/**
 * Smoke test for Account Daos
 * 
 * @author Andrew Johnson
 * 
 */
public class RepositoryDaoTest extends BasicDaoTest
{

    static Logger logger = Logger.getLogger(RepositoryDaoTest.class);

    private RepositoryDaoComponent repositoryDaoComponent;

    @BeforeMethod(alwaysRun = true)
    protected void setUp() throws DatabaseUnitException, SQLException, FileNotFoundException
    {

        repositoryDaoComponent = (RepositoryDaoComponent) ContextManager.getBean("repositoryDaoComponent");

    }

    @Parameters({ "repositoryMetaDataSource", "repositoryTestData" })
    @Test(testName = "Database")
    public void testDatabaseSetup(String dataSource, String sampleData) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException, DatabaseUnitException, SQLException,
            FileNotFoundException
    {

        DatabaseSetup.setup(dataSource, sampleData);

        logger.info("================ BEGIN TESTS ====================");
        repositoryDaoComponent.getAll();
    }

}
