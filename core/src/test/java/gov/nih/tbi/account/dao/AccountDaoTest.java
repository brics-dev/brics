
package gov.nih.tbi.account.dao;

import gov.nih.tbi.BasicDaoTest;
import gov.nih.tbi.ContextManager;
import gov.nih.tbi.DatabaseSetup;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.BasicAccount;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.util.PaginationData;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AccountDaoTest extends BasicDaoTest
{

    static Logger logger = Logger.getLogger(Account.class);

    private static String dataSource = "metaConnection";
    private static String sampleData = "src/test/resources/accountTestData.xml";
    private static IDataSet dataSet = DatabaseSetup.initializeDataSet(sampleData);

    private AccountDao accountDao;

    @BeforeMethod(alwaysRun = true)
    protected void setUp() throws DatabaseUnitException, SQLException, FileNotFoundException
    {

        accountDao = (AccountDao) ContextManager.getBean("accountDao");

        DatabaseSetup.setupWithDataSet(dataSource, dataSet);
    }

    @Test
    public void testGetByUserName() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testGetByUserName) ====================");

        ITable accountTable = dataSet.getTable("account");
        List<String> userNames = new ArrayList<String>();
        for (int i = 0; i < accountTable.getRowCount(); i++)
        {
            String userName = (String) accountTable.getValue(i, "user_name");
            userNames.add(userName);
            Account account = accountDao.getByUserName(userName);

            Assert.assertFalse(account == null);
            Assert.assertEquals(userName, account.getUserName());
        }

        final String invalidUserName = "NotAValidUserName";
        Assert.assertFalse(userNames.contains(invalidUserName));
        Account account = accountDao.getByUserName(invalidUserName);
        Assert.assertNull(account);
    }

    @Test
    public void testGetAllUserNames() throws DataSetException
    {

        logger.info("================ BEGIN TEST (testGetAllUserNames) ====================");

        ITable accountTable = dataSet.getTable("account");
        List<String> userNames = new ArrayList<String>();
        for (int i = 0; i < accountTable.getRowCount(); i++)
        {
            String userName = (String) accountTable.getValue(i, "user_name");
            userNames.add(userName);
        }

        List<String> dbUserNames = accountDao.getAllUserNames();
        for (String userName : dbUserNames)
        {
            Assert.assertTrue(userNames.contains(userName));
        }

        Assert.assertEquals(userNames.size(), dbUserNames.size());
    }

    @Test
    public void testGetByEmail() throws DataSetException
    {

        ITable userTable = dataSet.getTable("tbi_user");
        List<String> emails = new ArrayList<String>();
        for (int i = 0; i < userTable.getRowCount(); i++)
        {
            String email = (String) userTable.getValue(i, "email");
            emails.add(email);
            Account account = accountDao.getByEmail(email);
            Assert.assertEquals(email, account.getUser().getEmail());
        }
    }

    @Test
    public void testGetByUser() throws DataSetException
    {

        ITable userTable = dataSet.getTable("tbi_user");
        for (int i = 0; i < userTable.getRowCount(); i++)
        {
            String first_name = (String) userTable.getValue(i, "first_name");
            String last_name = (String) userTable.getValue(i, "last_name");
            Long id = Long.parseLong((String) userTable.getValue(i, "id"));
            String email = (String) userTable.getValue(i, "email");

            User newUser = new User();
            newUser.setFirstName(first_name);
            newUser.setLastName(last_name);
            newUser.setId(id);
            newUser.setEmail(email);

            Account account = accountDao.getByUser(newUser);

            Assert.assertNotNull(account);
            Assert.assertEquals(account.getUser().getId(), newUser.getId());
        }
    }

    @DataProvider(name = "testSearchPaginationDataProvider")
    public Object[][] testSearchDataProvider() throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, DatabaseUnitException, SQLException, FileNotFoundException
    {

        // Sets of pagination data, the expected size, and the username of the expected first result.
        return new Object[][] { { 1, 20, "userName", 6, "aaaaaa" }, { 1, 2, "u.lastName", 2, "bbbbbb" },
                { 2, 1, "u.lastName", 1, "mvalei" }, { 2, 3, "u.email", 3, "cccccc" } };
    }

    @Test(dataProvider = "testSearchPaginationDataProvider")
    public void testSearchPagination(int page, int pageSize, String sort, int expectedSize, String expectedUserName)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, DatabaseUnitException,
            SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testSearchPagination) ====================");
        List<BasicAccount> list = accountDao.search(null, null, new PaginationData(page, pageSize, false, sort));

        Assert.assertEquals(list.size(), expectedSize);
        Assert.assertEquals(list.get(0).getUserName(), expectedUserName);

    }
}
