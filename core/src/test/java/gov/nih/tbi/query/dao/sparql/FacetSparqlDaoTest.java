//
//package gov.nih.tbi.query.dao.sparql;
//
//import gov.nih.tbi.commons.util.RDFConstants;
//import gov.nih.tbi.query.dao.FacetSparqlDao;
//import gov.nih.tbi.query.model.Facet;
//import gov.nih.tbi.query.model.FacetItem;
//
//import java.util.List;
//
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
//import org.testng.Assert;
//import org.testng.annotations.BeforeMethod;
//import org.testng.annotations.Test;
//
//public class FacetSparqlDaoTest
//{
//
//    private FacetSparqlDao facetDao;
//    private ApplicationContext ctx;
//
//    @BeforeMethod
//    protected void setUp()
//    {
//
//        ctx = new ClassPathXmlApplicationContext("test-context.xml");
//        facetDao = ctx.getBean("facetSparqlDao", FacetSparqlDao.class);
//    }
//    
//    @Test
//    public void testGetLazy()
//    {
//
//        List<Facet> facets = facetDao.getAllBasic();
//
//        Assert.assertTrue(RDFConstants.QT_FACETS.size() == facets.size());
//
//        for (int i = 0; i < facets.size(); i++)
//        {
//            Assert.assertTrue(RDFConstants.QT_FACETS.get(i).equals(facets.get(i)));
//        }
//    }
//
//    @Test
//    public void testGetEager()
//    {
//
//        List<Facet> facets = facetDao.getAll();
//        for(Facet facet:facets)
//        {
//            if(facet.getItems() != null)
//            {
//                for(FacetItem item:facet.getItems())
//                {
//                    System.out.println(item.getLabel());
//                    System.out.println(item.getRdfURI());
//                    System.out.println(item.getCount());
//                }
//            }
//        }
//        Assert.assertTrue(facets != null); // better way to test this?
//    }
//}
