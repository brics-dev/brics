
package gov.nih.tbi.repository.dao;

import gov.nih.tbi.ContextManager;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.repository.model.hibernate.DataStoreInfo;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

import junit.framework.Assert;

import org.dbunit.DatabaseUnitException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataStoreInfoDaoTest
{

    public static final String dataSource = "metaConnection";
    public static final String sampleData = "src/test/resources/repositoryTestData.xml";

    private DataStoreInfoDao dataStoreInfoDao;

    @BeforeMethod(alwaysRun = true)
    protected void setUp() throws DatabaseUnitException, SQLException, FileNotFoundException
    {

        dataStoreInfoDao = (DataStoreInfoDao) ContextManager.getBean("dataStoreInfoDao");

        // DatabaseSetup.setup(dataSource, sampleData);
    }

    @Test
    public void testGetSortedList()
    {

        List<DataStoreInfo> infos = dataStoreInfoDao.getAllSorted("id", false);
        long prev = infos.get(0).getId();
        for (DataStoreInfo info : infos)
        {
            Assert.assertTrue(prev >= info.getId());
            prev = info.getId();
        }

        infos.clear();
        infos = dataStoreInfoDao.getAllSorted("id", true);
        prev = infos.get(0).getId();
        for (DataStoreInfo info : infos)
        {
            Assert.assertTrue(prev <= info.getId());
            prev = info.getId();
        }
    }

    @Test
    public void testGetByDataStructureId()
    {

        Long id = 201L;

        FormStructure ds = new FormStructure();
        ds.setId(id);
        DataStoreInfo info = dataStoreInfoDao.getByDataStructureId(ds.getId());
        Assert.assertTrue(id.equals(info.getId()));
    }
}
