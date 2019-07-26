
package gov.nih.tbi;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.Reader;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.BitSet;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSet;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.database.statement.IPreparedBatchStatement;
import org.dbunit.database.statement.IStatementFactory;
import org.dbunit.database.statement.StatementFactory;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.RowOutOfBoundsException;
import org.dbunit.dataset.datatype.TypeCastException;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.dbunit.operation.InsertOperation;
import org.dbunit.operation.OperationData;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.InputSource;

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
            logger.info("[[[[[[[[[[[[[[[[[[[[[ CLEAN INSERT ]]]]]]]]]]]]]]]]]]]]]");
            DatabaseOperation.CLEAN_INSERT.execute(connection, initializeDataSet(sampleDataFileName));
            connection.close();
        }
        catch (Exception e)
        {
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
            logger.info("[[[[[[[[[[[[[[[[[[[[[ CLEAN INSERT ]]]]]]]]]]]]]]]]]]]]]");
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
            connection.close();
        }
        catch (Exception e)
        {
            Assert.fail("Failure in setupWithDataSet() " + e.getMessage());
        }
    }

    public static void backupDatabase(String dataSourceName, String schema)
    {

        logger.info("[[[[[[[[[[[[[[[[[[[[[ BACKUP ]]]]]]]]]]]]]]]]]]]]]");
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
            Assert.fail("Failure in backupDatabase() " + e.getMessage());
        }
    }

    public static void restoreDatabase(String dataSourceName) throws SQLException
    {

        logger.info("[[[[[[[[[[[[[[[[[[[[[ RESTORE ]]]]]]]]]]]]]]]]]]]]]");
        DataSource ds = (DataSource) ContextManager.getBean(dataSourceName);
        IDatabaseConnection connection = null;
        Statement statement = null;
        try
        {
            connection = new DatabaseConnection(ds.getConnection());
            DatabaseOperation.CLEAN_INSERT.execute(connection,
                    initializeDataSet("src/test/resources/accountTestData.xml"));
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
