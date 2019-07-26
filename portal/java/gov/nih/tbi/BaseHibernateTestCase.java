
package gov.nih.tbi;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;

public abstract class BaseHibernateTestCase extends TestCase
{

    Log log = LogFactory.getLog(getClass());

    protected void setUpMeta() throws Exception
    {

        super.setUp();
        // setup if required
        if (!TestHibernateFactory.isSessionFactoryMetaInitialized())
        {
            TestHibernateFactory.buildSessionFactoryMeta();
        }
    }

    protected void setUpDictionary() throws Exception
    {

        super.setUp();
        // setup if required
        if (!TestHibernateFactory.isSessionFactoryDictionaryInitialized())
        {
            TestHibernateFactory.buildSessionFactoryDictionary();
        }
    }

    protected void setUpRepository() throws Exception
    {

        super.setUp();
        // setup if required
        if (!TestHibernateFactory.isSessionFactoryRepoInitialized())
        {
            TestHibernateFactory.buildSessionFactoryRepository();
        }
    }

    protected SessionFactory getSessionFactory()
    {

        return TestHibernateFactory.getSessionFactoryMeta();
    }

}