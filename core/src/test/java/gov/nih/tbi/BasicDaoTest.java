
package gov.nih.tbi;

import gov.nih.tbi.DatabaseSetup;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class BasicDaoTest
{

    static Logger logger = Logger.getLogger(BasicDaoTest.class);

    private static String source = "metaConnection";
    private static String schema = "tbi_meta";

    @BeforeSuite(alwaysRun = true)
    protected void backupLocalDB() throws DatabaseUnitException, SQLException, FileNotFoundException
    {

        DatabaseSetup.backupDatabase(source, schema);
    }

    @AfterSuite(alwaysRun = true)
    protected void restoreLocalDB() throws DatabaseUnitException, SQLException, FileNotFoundException
    {

        DatabaseSetup.restoreDatabase(source);
    }
}
