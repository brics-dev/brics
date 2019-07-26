
package gov.nih.tbi.repository.dao;

import gov.nih.tbi.BasicDaoTest;
import gov.nih.tbi.ContextManager;
import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.model.DataType;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.dbunit.DatabaseUnitException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataStoreDaoTest extends BasicDaoTest
{

    private DataStoreDao dataStoreDao;

    @BeforeMethod(alwaysRun = true)
    protected void setUp() throws FileNotFoundException, DatabaseUnitException, SQLException
    {

        dataStoreDao = (DataStoreDao) ContextManager.getBean("dataStoreDao");
    }

    @Test(testName = "Database")
    public void testCreateTable()
    {

        final String tableName = "TestTable";
        final String dropTable = "drop table " + tableName + " cascade;";
        final int varCharSize = 55;

        Map<String, String> columns = new HashMap<String, String>();
        Formatter formatter = new Formatter();

        formatter.format(DataType.ALPHANUMERIC.getSqlFormatString(), varCharSize);
        columns.put("Alphanumeric", formatter.toString());
        columns.put("Numeric", DataType.NUMERIC.getSqlFormatString());
        columns.put("Date", DataType.DATE.getSqlFormatString());
        try
        {
            dataStoreDao.createTable(tableName, columns);
            dataStoreDao.getJdbcTemplate().execute(dropTable);
        }
        catch (SQLException e)
        {
            Assert.fail(e.getMessage());
        }
    }

    @Test(testName = "Database")
    public void testCreateSequence()
    {

        final String sequenceName = "TestTable" + CoreConstants.SEQUENCE_SUFFIX;
        final String dropSequence = "drop sequence \"" + sequenceName + "\";";

        try
        {
            dataStoreDao.createSequence(sequenceName);
            dataStoreDao.getJdbcTemplate().execute(dropSequence);
        }
        catch (SQLException e)
        {
            Assert.fail(e.getMessage());
        }
    }
}
