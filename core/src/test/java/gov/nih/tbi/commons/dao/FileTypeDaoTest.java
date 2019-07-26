
package gov.nih.tbi.commons.dao;

import gov.nih.tbi.BasicDaoTest;
import gov.nih.tbi.ContextManager;
import gov.nih.tbi.DatabaseSetup;
import gov.nih.tbi.commons.model.FileClassification;
import gov.nih.tbi.commons.model.hibernate.FileType;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FileTypeDaoTest extends BasicDaoTest
{

    static Logger logger = Logger.getLogger(FileType.class);

    private static String dataSource = "metaConnection";
    private static String sampleData = "src/test/resources/commonsTestData.xml";

    private FileTypeDao fileTypeDao;

    @BeforeMethod(alwaysRun = true)
    protected void setUp() throws DatabaseUnitException, SQLException, FileNotFoundException
    {

        fileTypeDao = (FileTypeDao) ContextManager.getBean("fileTypeDao");

        DatabaseSetup.setup(dataSource, sampleData);
    }

    @Test
    public void testGetFileTypeByClassification() throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testGetFileTypeByClassification) ====================");

        List<FileType> admin = fileTypeDao.getFileTypeByClassification(FileClassification.ADMIN);
        List<FileType> supporting = fileTypeDao.getFileTypeByClassification(FileClassification.SUPPORTING_DOCUMENT);

        Assert.assertEquals(admin.size(), 4);
        Assert.assertEquals(supporting.size(), 2);
    }
}
