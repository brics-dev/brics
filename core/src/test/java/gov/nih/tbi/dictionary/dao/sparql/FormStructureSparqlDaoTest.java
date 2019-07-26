package gov.nih.tbi.dictionary.dao.sparql;

import gov.nih.tbi.dictionary.dao.FormStructureSparqlDao;
import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class FormStructureSparqlDaoTest
{

    private FormStructureSparqlDao formStructureDao;
    private ApplicationContext ctx;

    @BeforeMethod
    protected void setUp()
    {

        ctx = new ClassPathXmlApplicationContext("test-context.xml");
        formStructureDao = ctx.getBean("formStructureSparqlDao", FormStructureSparqlDao.class);
    }
    
    @Test
    public void testGetAll()
    {
        List<SemanticFormStructure> formStructures = formStructureDao.getAll();
        Assert.assertTrue(formStructures != null); //better way to test this?
    }
    
    @Test
    public void testGet()
    {
        SemanticFormStructure formStructure = formStructureDao.get("http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/PDBP%20University%20of%20Pennsylvania%20Smell%20Identification%20Test_v1");
    }
    
    @Test
    public void testGetByShortNameVersion()
    {
        SemanticFormStructure formStructure = formStructureDao.get("UnivOfPennSmellIdenTest", "1.0");
        System.out.println(formStructure.getShortName());
        System.out.println(formStructure.getVersion());
    }
    
    @Test
    public void testGetByShortNameVersions()
    {
        List<NameAndVersion> nameAndVersions = new ArrayList<NameAndVersion> ();
        nameAndVersions.add(new NameAndVersion("CSFDataCollectionForm", "1.0"));
        nameAndVersions.add(new NameAndVersion("MedicalHistory", "2.0"));
        nameAndVersions.add(new NameAndVersion("BiosamplShipmntFormandInv", "1.0"));
        List<SemanticFormStructure> fs = formStructureDao.getByShortNameAndVersions(nameAndVersions);
        System.out.println("SUCCESS");
    }
}
