
package gov.nih.tbi.account.dao;

import gov.nih.tbi.BasicDaoTest;
import gov.nih.tbi.ContextManager;
import gov.nih.tbi.DatabaseSetup;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.commons.model.EntityType;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EntityMapDaoTest extends BasicDaoTest
{

    static Logger logger = Logger.getLogger(EntityMapDaoTest.class);

    private static String dataSource = "metaConnection";
    private static String sampleData = "src/test/resources/accountTestData.xml";
    private Account account;

    private EntityMapDao entityMapDao;
    private AccountDao accountDao;

    @BeforeMethod(alwaysRun = true)
    protected void setUp() throws DatabaseUnitException, SQLException, FileNotFoundException
    {

        entityMapDao = (EntityMapDao) ContextManager.getBean("entityMapDao");
        accountDao = (AccountDao) ContextManager.getBean("accountDao");

        account = accountDao.get(1L);

        DatabaseSetup.setup(dataSource, sampleData);
    }

    @Test
    public void listUserAccess() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (listUserAccess) ====================");

        List<EntityMap> entityMapList = entityMapDao.listUserAccess(account, new HashSet<PermissionGroup>(),
                EntityType.DATA_STRUCTURE, false);

        Assert.assertTrue(entityMapList.size() > 0);

        for (EntityMap entityMap : entityMapList)
        {
            logger.info(entityMap);
        }
    }

    @Test
    public void listEntityAccess() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (listEntityAccess) ====================");

        List<EntityMap> entityMapList = entityMapDao.listEntityAccess(1L, EntityType.DATA_STRUCTURE);

        Assert.assertTrue(entityMapList.size() > 0);

        for (EntityMap entityMap : entityMapList)
        {
            logger.info(entityMap);
        }
    }

    @Test
    public void testGet() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            DatabaseUnitException, SQLException, FileNotFoundException
    {

        logger.info("================ BEGIN TEST (testGet) ====================");

        EntityMap entityMap = entityMapDao.get(account, new HashSet<PermissionGroup>(), EntityType.DATA_STRUCTURE, 1L,
                false);

        Assert.assertNotNull(entityMap);
    }
}
