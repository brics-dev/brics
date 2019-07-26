
package gov.nih.tbi.commons.dao;

import gov.nih.tbi.BasicDaoTest;
import gov.nih.tbi.ContextManager;
import gov.nih.tbi.DatabaseSetup;
import gov.nih.tbi.commons.model.hibernate.User;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UserDaoTest extends BasicDaoTest
{

    static Logger logger = Logger.getLogger(User.class);

    private static String dataSource = "metaConnection";
    private static String sampleData = "src/test/resources/accountTestData.xml";

    private UserDao userDao;

    @BeforeMethod(alwaysRun = true)
    protected void setUp() throws DatabaseUnitException, SQLException, FileNotFoundException
    {

        userDao = (UserDao) ContextManager.getBean("userDao");

        DatabaseSetup.setup(dataSource, sampleData);
    }

    @Test
    public void testGetByEmail() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testGetByEmail) ====================");

        User user = userDao.getByEmail("harry@hogwarts.edu");

        Assert.assertFalse(user == null);
        Assert.assertFalse(user.getId() == null);
        Assert.assertTrue(user.getId().equals(1L));

        User user2 = userDao.getByEmail("NotAValid@User.Name");

        Assert.assertEquals(user2, null);
    }

    @Test
    public void testGetAllEmails() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testGetAllEmails) ====================");

        List<String> emails = userDao.getAllEmails();
        logger.info(emails);
        Assert.assertFalse(emails == null);
        Assert.assertTrue(emails.size() == 6);
        Assert.assertTrue(emails.contains("harry@hogwarts.edu"));
    }
}
