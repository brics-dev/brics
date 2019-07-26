
package gov.nih.tbi.semantic.model;

import gov.nih.tbi.commons.model.PermissionType;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * BRICS vocabulary items
 * 
 * @author Nimesh Patel
 * 
 */
public class SecurityRDF
{

    public static final String URI_NS = "http://ninds.nih.gov/repository/1.0/Security/";

    public static final Property PROPERTY_HASATLEASTREADACCESS = property("hasAtLeastReadAccess");
    public static final Property PROPERTY_READ = property("read");
    public static final Property PROPERTY_WRITE = property("write");
    public static final Property PROPERTY_ADMIN = property("admin");
    public static final Property PROPERTY_OWNER = property("owner");

    public static final Resource resource(String local)
    {

        return ResourceFactory.createResource(URI_NS + local);
    }

    public static final Property property(String local)
    {

        return ResourceFactory.createProperty(URI_NS, local);
    }

    public static Property getPermissionProperty(PermissionType type)
    {

        switch (type)
        {
        case READ:
            return PROPERTY_READ;
        case WRITE:
            return PROPERTY_WRITE;
        case ADMIN:
            return PROPERTY_ADMIN;
        case OWNER:
            return PROPERTY_OWNER;
        default:
            return null;
        }
    }
}
