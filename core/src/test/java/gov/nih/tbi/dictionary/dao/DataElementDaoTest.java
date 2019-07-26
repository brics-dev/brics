
package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.BasicDaoTest;
import gov.nih.tbi.ContextManager;
import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.DatabaseSetup;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.model.hibernate.Alias;
import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test for Data Element Dao
 * 
 * @author Andrew Johnson
 * @author Francis Chen
 * 
 */
public class DataElementDaoTest extends BasicDaoTest
{

    static Logger logger = Logger.getLogger(DataElementDaoTest.class);

    private DataElementDao dataElementDao;
    private DiseaseDao diseaseDao;
    private DomainDao domainDao;
    private SubDomainDao subDomainDao;
    private CategoryDao categoryDao;

    private Set<Long> ids = new HashSet<Long>();

    private static final String dataSource = "metaConnection";
    private static final String sampleData = "src/test/resources/dictionaryTestData.xml";
    private static final IDataSet dataSet = DatabaseSetup.initializeDataSet(sampleData);

    @BeforeMethod(alwaysRun = true)
    protected void setUp() throws DatabaseUnitException
    {

        dataElementDao = (DataElementDao) ContextManager.getBean("dataElementDao");
        diseaseDao = (DiseaseDao) ContextManager.getBean("diseaseDao");
        domainDao = (DomainDao) ContextManager.getBean("domainDao");
        subDomainDao = (SubDomainDao) ContextManager.getBean("subDomainDao");
        categoryDao = (CategoryDao) ContextManager.getBean("categoryDao");

        DatabaseSetup.setupWithDataSet(dataSource, dataSet);

        for (int i = 0; i < dataSet.getTable("data_element").getRowCount(); i++)
        {
            ids.add(Long.parseLong((String) dataSet.getTable("data_element").getValue(i, "id")));
        }
    }

    @Test(testName = "Database")
    public void testSearch() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testSearch) ====================");

        PaginationData pageData = new PaginationData();
        List<DataElement> deList = dataElementDao.search(ids, null, null, CoreConstants.EMPTY_STRING, pageData, null);

        logger.info("Number of DataElements returned: " + deList.size());

        Assert.assertEquals(deList.size(), dataSet.getTable("data_element").getRowCount());
    }

    @Test(testName = "Database")
    public void testGetByName() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testGetByName) ====================");

        List<String> names = new ArrayList<String>();
        for (int i = 0; i < dataSet.getTable("data_element").getRowCount(); i++)
        {
            names.add((String) dataSet.getTable("data_element").getValue(i, "ELEMENT_NAME"));
        }

        for (String name : names)
        {
            DataElement element = dataElementDao.getLatestByName(name);
            Assert.assertNotNull(element);
            Assert.assertEquals(name, element.getName());
        }
    }

    @Test(testName = "Database")
    public void testSearchWithKeyword() throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, DatabaseUnitException, SQLException, FileNotFoundException
    {

        final String searchKey = "subject";

        logger.info("================ BEGIN TEST (testSearchWithKeyword) ====================");
        PaginationData pageData = new PaginationData();
        List<DataElement> deList = dataElementDao.search(ids, null, null, searchKey, pageData, null);

        logger.info("Number of DataElements returned: " + deList.size());

        for (DataElement dataElement : deList)
        {
            if (!dataElement.getDescription().contains(searchKey))
            {
                if (!dataElement.getTitle().contains(searchKey))
                {
                    boolean hasKeyword = false;
                    for (Keyword k : dataElement.getKeywords())
                    {
                        if (k.getKeyword().contains(searchKey))
                        {
                            hasKeyword = true;
                        }
                    }
                    if (!hasKeyword)
                    {
                        boolean hasValueRange = false;
                        for (ValueRange valueRange : dataElement.getValueRangeList())
                        {
                            if (valueRange.getDescription().contains(searchKey))
                            {
                                hasValueRange = true;
                            }
                        }
                        if (!hasValueRange)
                        {
                            boolean hasAlias = false;
                            for (Alias alias : dataElement.getAliasList())
                            {
                                if (alias.getName().contains(searchKey))
                                {
                                    hasAlias = true;
                                }
                            }
                            if (!hasAlias)
                            {
                                // the searchkey wasn't in any of the search locations
                                Assert.fail("Search key: " + searchKey + " could not be found in element "
                                        + dataElement.getId());
                            }
                        }
                    }
                }
            }

        }
    }

    @Test(testName = "Database")
    public void testSearchWithKeywordExpectedFail() throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, DatabaseUnitException, SQLException, FileNotFoundException
    {

        final String searchKey = "xxxxxxxxxxxxxxxxxxxxxx1xxx";
        logger.info("================ BEGIN TEST (testSearchWithKeywordExpectedFail) ====================");
        PaginationData pageData = new PaginationData();
        List<DataElement> deList = dataElementDao.search(ids, null, null, searchKey, pageData, null);

        logger.info("Number of DataElements returned: " + deList.size());

        Assert.assertTrue(deList.size() == 0);
    }

    @Test(testName = "Database")
    public void testSearchWithFourMaxResults() throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testSearchWithMaxResults) ====================");

        for (int x = 0; x <= 4; x++)
        {
            List<DataElement> deList = dataElementDao.search(ids, null, null, "", new PaginationData(1, x, true, null),
                    null);

            logger.info("Number of DataElements returned: " + deList.size() + " Expected: " + x);

            Assert.assertTrue(deList.size() == x);
        }
    }

    @Test(testName = "Database")
    public void testSearchWithOneLocation() throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testSearchWithOneLocation) ====================");

        List<String> list = new ArrayList<String>();
        list.add("title");

        List<DataElement> deList = dataElementDao.search(ids, null, null, "unattached", new PaginationData(null, null,
                true, null, list), null);

        logger.info("Number of DataElements returned: " + deList.size());

        Assert.assertEquals(deList.size(), 2);
    }

    @Test(testName = "Database")
    public void testGetByIdList() throws DataSetException
    {

        final int idCount = 4;

        List<Long> ids = new ArrayList<Long>();
        for (int i = 0; i < idCount; i++)
        {
            ids.add(Long.parseLong((String) dataSet.getTable("data_element").getValue(i, "id")));
        }

        List<DataElement> dataElements = dataElementDao.getByIdList(ids);

        Assert.assertTrue(ids.size() == dataElements.size());
        for (DataElement dataElement : dataElements)
        {
            Assert.assertTrue(ids.contains(dataElement.getId()));
        }
    }

    @DataProvider(name = "testSearchFilteredDataElementsDataProvider")
    public Object[][] testSearchFilteredDataElementsDataProvider() throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException, DatabaseUnitException, SQLException,
            FileNotFoundException
    {

        return new Object[][] { { null, domainDao.get(1L), null, 2 },
                { null, domainDao.get(1L), subDomainDao.get(2L), 1 }, { null, null, null, 7 } };
    }

    @Test(testName = "Database")
    public void testCommonFlag() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testSearchFilteredDataElements) ====================");

        List<DataElement> deList = dataElementDao.search(ids, null, null, CoreConstants.EMPTY_STRING, null, null);
        Category category = categoryDao.get(0L);
        List<DataElement> commonDeList = dataElementDao.search(ids, category, null, CoreConstants.EMPTY_STRING, null,
                null);
        category = categoryDao.get(1L);
        List<DataElement> dataDeList = dataElementDao.search(ids, category, null, CoreConstants.EMPTY_STRING, null,
                null);
        logger.info("Total element count: " + deList.size());
        logger.info("Number of common elements: " + commonDeList.size());
        logger.info("Number of data elements: " + dataDeList.size());

        for (DataElement de : commonDeList)
        {
            Assert.assertTrue(de.getCategory().equals(categoryDao.get(0L)));
        }
        for (DataElement de : dataDeList)
        {
            Assert.assertTrue(de.getCategory().equals(categoryDao.get(1L)));
        }

        Assert.assertEquals(deList.size(), commonDeList.size() + dataDeList.size());
    }

    @DataProvider(name = "testSortedSearchResultsDataProvider")
    public Object[][] testSortedSearchResultsDataProvider() throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, DatabaseUnitException, SQLException, FileNotFoundException
    {

        // List all the ways we want to be able to sort the search results
        return new Object[][] { { "name", true }, { "shortDescription", true }, { "title", true }, { "name", false },
                { "shortDescription", false }, { "title", false } };
    }

    @Test(testName = "Database", dataProvider = "testSortedSearchResultsDataProvider")
    public void testSortedSearchResults(String sort, Boolean asc) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, DatabaseUnitException,
            SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testSortedSearchResults) ====================");

        String getMethodName = "get" + sort.substring(0, 1).toUpperCase() + sort.substring(1);
        PaginationData pageData = new PaginationData(null, null, asc, sort);
        List<DataElement> deList = dataElementDao.search(ids, null, null, CoreConstants.EMPTY_STRING, pageData, null);

        // If there are no elements to test then return and do not create a null pointer error when creating the method
        if (deList.isEmpty())
            return;
        Method method = deList.get(0).getClass().getMethod(getMethodName);

        logger.info((String) method.invoke(deList.get(0)));
        for (int i = 1; i < deList.size(); i++)
        {
            logger.info((String) method.invoke(deList.get(i)));
            if (asc)
            {
                Assert.assertTrue(((String) method.invoke(deList.get(i))).compareToIgnoreCase((String) method
                        .invoke(deList.get(i - 1))) <= 0);
            }
            else
            {
                Assert.assertTrue(((String) method.invoke(deList.get(i))).compareToIgnoreCase((String) method
                        .invoke(deList.get(i - 1))) >= 0);
            }
        }
    }
}
