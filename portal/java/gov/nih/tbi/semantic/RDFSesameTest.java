
package gov.nih.tbi.semantic;

import gov.nih.tbi.semantic.model.DataElementRDF;
import gov.nih.tbi.semantic.model.DatasetRDF;
import gov.nih.tbi.semantic.model.FormStructureRDF;
import gov.nih.tbi.semantic.model.StudyRDF;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;

public class RDFSesameTest extends TestCase// extends BaseHibernateTestCase
{

    static Logger logger = Logger.getLogger(RDFSesameTest.class);

    private static String sesameServer = "http://REPLACED:8080/";
    private static String sesameApp = "openrdf-sesame";
    private static String repositoryID = "BRICSDemo";
    // private String exportLocation = "D:\\RDFExport\\GeoffManley\\";
    private String exportLocation = "D:\\RDFExport\\";

    private String deFileName = "deTriples.ttl";
    private String formFileName = "formTriples.ttl";
    private String studyFileName = "studyTriples.ttl";
    private String datasetFileName = "datasetTriples.ttl";
    private String relationshipsFileName = "relationshipsTriples.ttl";
    private String securityFileName = "securityTriples.ttl";
    private String schemaFileName = "schemaTriples.ttl";
    private String repositoryFileName = "repositoryTriples.ttl";
    private String allFileName = "allTriples.ttl";

    Repository myRepo;
    RepositoryConnection con;

    @Override
    protected void setUp() throws Exception
    {

        initializeRepository();
        super.setUp();
    }

    @Override
    protected void tearDown()
    {

        try
        {
            con.close();
            super.tearDown();
        }
        catch (Exception e)
        {

            e.printStackTrace();
        }
    }

    private void initializeRepository()
    {

        try
        {
            myRepo = new HTTPRepository(sesameServer + sesameApp, repositoryID);
            con = myRepo.getConnection();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void addAllToRepo()
    {

        addRDFToRepositoryFromFile(allFileName, sesameServer, true);
    }

    public void addAllFilesToRepo()
    {

        addRDFToRepositoryFromFile(deFileName, DataElementRDF.URI_NS, true);
        addRDFToRepositoryFromFile(formFileName, FormStructureRDF.URI_NS, true);
        addRDFToRepositoryFromFile(datasetFileName, DatasetRDF.URI_NS, true);
        addRDFToRepositoryFromFile(studyFileName, StudyRDF.URI_NS, true);
        addRDFToRepositoryFromFile(relationshipsFileName, sesameServer, true);
        // addRDFToRepositoryFromFile(securityFileName, sesameServer, true);
        addRDFToRepositoryFromFile(schemaFileName, sesameServer, true);
        addRDFToRepositoryFromFile(repositoryFileName, sesameServer, true);
    }

    public void addRDFToRepositoryFromFile(String fileName, String baseURI, Boolean clearContext)
    {

        if (clearContext == null)
        {
            clearContext = false;
        }

        File file = new File(exportLocation + fileName);

        try
        {

            ValueFactory vf = myRepo.getValueFactory();
            URI context = vf.createURI(sesameServer + fileName);

            if (clearContext)
            {
                con.clear(context);
            }

            con.add(file, baseURI, RDFFormat.TURTLE, context);
            // con.add(file, baseURI, RDFFormat.TURTLE);

            myRepo.initialize();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            assertTrue(false);
        }

        logger.debug("Imported File: " + fileName + " TO " + sesameServer);
    }

}
