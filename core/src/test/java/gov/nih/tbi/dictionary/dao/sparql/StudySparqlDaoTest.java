//package gov.nih.tbi.dictionary.dao.sparql;
//
//import gov.nih.tbi.repository.dao.StudySparqlDao;
//import gov.nih.tbi.repository.model.hibernate.Study;
//
//import java.util.List;
//
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
//import org.testng.Assert;
//import org.testng.annotations.BeforeMethod;
//import org.testng.annotations.Test;
//
//
//public class StudySparqlDaoTest
//{
//    private StudySparqlDao studyDao;
//    private ApplicationContext ctx;
//
//    @BeforeMethod
//    protected void setUp()
//    {
//
//        ctx = new ClassPathXmlApplicationContext("test-context.xml");
//        studyDao = ctx.getBean("studySparqlDao", StudySparqlDao.class);
//    }
//
//    @Test
//    public void testGetLazy()
//    {
//
//        List<Study> study = studyDao.getAllBasic();
//        Assert.assertTrue(study != null); //better way to test this?
//    }
//    
//    @Test
//    public void testGetEager()
//    {
//        List<Study> study = studyDao.getAll();
//        Assert.assertTrue(study != null); //better way to test this?
//    }
//}
