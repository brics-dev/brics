package gov.nih.tbi.dictionary.dao.sparql;

import java.util.List;

import gov.nih.tbi.dictionary.dao.KeywordSparqlDao;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class KeywordSparqlDaoTest
{
    private KeywordSparqlDao keywordDao;
    private ApplicationContext ctx;

    @BeforeMethod
    protected void setUp()
    {

        ctx = new ClassPathXmlApplicationContext("test-context.xml");
        keywordDao = ctx.getBean("keywordSparqlDao", KeywordSparqlDao.class);
    }
    
    @Test
    public void testGetAll()
    {
        List<Keyword> keywords = keywordDao.getAll();
        
        for(Keyword keyword:keywords)
        {
            System.out.println(keyword.getKeyword());
        }
    }
    
    @Test
    public void testSearch()
    {
        List<Keyword> keywords= keywordDao.search("quip");
        for(Keyword keyword:keywords)
        {
            System.out.println(keyword.getKeyword());
        }
    } 
    
    @Test
    public void testGetByName()
    {
        Keyword keyword= keywordDao.getByName("Equipment");
        System.out.println(keyword.getKeyword());
    }
}
