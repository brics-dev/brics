
package gov.nih.tbi.semantic.model;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * BRICS vocabulary items
 * 
 * @author Nimesh Patel
 * 
 */
public class RepositoryRDF
{

    public static final String URI_NS = "http://ninds.nih.gov/repository/fitbir/1.0/Repository/";

    // Resources
    public static final Resource RepositoryTable = resource("RepositoryTable");
    public static final Resource RepositoryRow = resource("RepositoryRow");

    // Properties
    // public static final Property PROPERTY_DATASET_ID = property("dataset_id");

    // Relationship Properties
    public static final Property hasRow = property("hasRow");
    public static final Property hasTable = property("hasTable");

    public static final Resource resource(String local)
    {

        return ResourceFactory.createResource(URI_NS + local);
    }

    public static final Property property(String local)
    {

        return ResourceFactory.createProperty(URI_NS, local);
    }

    /**
     * returns the URI for this schema
     * 
     * @return the URI for this schema
     */
    public static String getURI()
    {

        return URI_NS;
    }
}
