
package gov.nih.tbi.semantic;

import java.sql.SQLException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import virtuoso.jena.driver.VirtGraph;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class RDFVirtuosoTest extends TestCase// extends BaseHibernateTestCase
{

    static Logger logger = Logger.getLogger(RDFVirtuosoTest.class);

    // Stage
    private static String db_url = "jdbc:virtuoso://REPLACED:1111";
    private static String db_user = "REPLACED";
    private static String db_pass = "REPLACED";
    private static String db_graph = "REPLACED";
    // private static String db_schema = "http://brics/schema/";

    // Dev
    // private static String db_url = "jdbc:virtuoso://REPLACED:1111";
    // private static String db_user = "REPLACED";
    // private static String db_pass = "REPLACED";
    // private static String db_graph = "http://REPLACED:8080/allTriples.ttl";
    // private static String db_schema = "http://brics/schema/";

    private VirtGraph graph = null;

    // private String ruleSet = "";

    // private String exportLocation = "D:\\RDFExport\\GeoffManley\\";
    private String exportLocation = "D:\\RDFExport\\Dev\\                                                                                                                                                                                                                                                                                       ";

    private String deFileName = "deTriples.ttl";
    private String formFileName = "formTriples.ttl";
    private String studyFileName = "studyTriples.ttl";
    private String datasetFileName = "datasetTriples.ttl";
    private String relationshipsFileName = "relationshipsTriples.ttl";
    private String securityFileName = "securityTriples.ttl";
    private String schemaFileName = "schemaTriples.ttl";
    private String repositoryFileName = "repositoryTriples.ttl";
    private String allFileName = "allTriples.ttl";

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
            graph.close();
            super.tearDown();
        }
        catch (Exception e)
        {

            e.printStackTrace();
        }
    }

    public void initializeRepository() throws SQLException
    {

        graph = new VirtGraph(db_graph, db_url, db_user, db_pass);

    }

    public void addAllToRepo()
    {

        Model temp = FileManager.get().loadModel(exportLocation + allFileName);
        addRDFToRepositoryFromFile(temp, true);
    }

    public void addAllFilesToRepo()
    {

        Model model = ModelFactory.createDefaultModel();

        Model deModel = FileManager.get().loadModel(exportLocation + deFileName);
        Model formModel = FileManager.get().loadModel(exportLocation + formFileName);
        Model datasetModel = FileManager.get().loadModel(exportLocation + datasetFileName);
        Model studyModel = FileManager.get().loadModel(exportLocation + studyFileName);
        Model relationModel = FileManager.get().loadModel(exportLocation + relationshipsFileName);
        Model schemaModel = FileManager.get().loadModel(exportLocation + schemaFileName);
        Model repositoryModel = FileManager.get().loadModel(exportLocation + repositoryFileName);

        model.add(deModel);
        model.add(formModel);
        model.add(datasetModel);
        model.add(studyModel);
        model.add(relationModel);
        model.add(schemaModel);
        model.add(repositoryModel);

        addRDFToRepositoryFromFile(model, true);
    }

    public void addRDFToRepositoryFromFile(Model model, Boolean clearContext)
    {

        if (clearContext == null)
        {
            clearContext = false;
        }

        try
        {

            if (clearContext)
            {
                graph.clear();
            }

            graph.getTransactionHandler().begin();
            graph.getBulkUpdateHandler().add(model.getGraph());
            graph.getTransactionHandler().commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            assertTrue(false);
        }

        logger.debug("Imported File: " + " TO " + db_url);
    }
}
