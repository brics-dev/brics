
package gov.nih.tbi.guid.ws;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;

public class DatabaseSetup
{

    static Logger logger = Logger.getLogger(DatabaseSetup.class);

    public static void setup(String dataSourceName, String sampleDataFileName)
    {

        DataSource ds = (DataSource) ContextManager.getBean(dataSourceName);
        IDatabaseConnection connection = null;
        try
        {
            connection = new DatabaseConnection(ds.getConnection());
            logger.debug("[[[[[[[[[[[[[[[[[[[[[ CLEAN INSERT ]]]]]]]]]]]]]]]]]]]]]");
            DatabaseOperation.CLEAN_INSERT.execute(connection, initializeDataSet(sampleDataFileName));
            connection.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail("Failure in setup() " + e.getMessage());
        }
    }

    public static void setupWithDataSet(String dataSourceName, IDataSet dataSet)
    {

        DataSource ds = (DataSource) ContextManager.getBean(dataSourceName);
        IDatabaseConnection connection = null;
        try
        {
            connection = new DatabaseConnection(ds.getConnection());
            logger.debug("[[[[[[[[[[[[[[[[[[[[[ CLEAN INSERT ]]]]]]]]]]]]]]]]]]]]]");
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
            connection.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail("Failure in setupWithDataSet() " + e.getMessage());
        }
    }

    public static void backupDatabase(String dataSourceName, String schema)
    {

        logger.debug("[[[[[[[[[[[[[[[[[[[[[ BACKUP ]]]]]]]]]]]]]]]]]]]]]");
        DataSource ds = (DataSource) ContextManager.getBean(dataSourceName);
        IDatabaseConnection connection = null;
        try
        {
            connection = new DatabaseConnection(ds.getConnection(), schema);
            IDataSet fullDataSet = connection.createDataSet();
            FlatXmlDataSet.write(fullDataSet, new FileOutputStream("full.xml"));
            connection.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail("Failure in backupDatabase() " + e.getMessage());
        }
    }

    public static void restoreDatabase(String dataSourceName) throws SQLException
    {

        logger.debug("[[[[[[[[[[[[[[[[[[[[[ RESTORE ]]]]]]]]]]]]]]]]]]]]]");
        DataSource ds = (DataSource) ContextManager.getBean(dataSourceName);
        IDatabaseConnection connection = null;
        Statement statement = null;
        try
        {
            connection = new DatabaseConnection(ds.getConnection());
            DatabaseOperation.CLEAN_INSERT
                    .execute(connection, initializeDataSet("src/test/resources/guidTestData.xml"));
            connection.getConnection().setAutoCommit(false);
            statement = connection.getConnection().createStatement();
            statement.execute("SET CONSTRAINTS ALL DEFERRED;");
            DatabaseOperation.CLEAN_INSERT.execute(connection, initializeDataSet("full.xml"));
            connection.getConnection().commit();
            connection.getConnection().setAutoCommit(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail("Failure in restoreDatabase() " + e.getMessage());
        }
        finally
        {
            statement.close();
            connection.close();
            statement = null;
        }
    }

    public static IDataSet initializeDataSet(String fileName)
    {

        IDataSet dataSet = null;
        try
        {
            dataSet = (new FlatXmlDataSetBuilder()).build(new FileInputStream(fileName));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail("Failure in initializeDataSet() " + e.getMessage());
        }
        return dataSet;
    }
}
