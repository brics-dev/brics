package gov.nih.tbi.semantic.model;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class GuidRDF {
	public static final String STRING_GUID = "Guid";

	public static final String BASE_URI_NS = "http://ninds.nih.gov/repository/fitbir/1.0/";
	public static final String URI_NS = BASE_URI_NS + STRING_GUID + "/";
	
	public static final Property DO_HIGHLIGHT_PROP = property("doHighlight");
	// Resources
	public static final Resource RESOURCE_GUID = resource(STRING_GUID);

	public static final Resource resource(String local) {

		return ResourceFactory.createResource(BASE_URI_NS + local);
	}

	public static final Property property(String local) {

		return ResourceFactory.createProperty(URI_NS, local);
	}

	public static Resource createGuidResource(String guid) {
		Resource guidResource = null;

		try {
			guidResource = ResourceFactory.createResource(URIUtil.encodeQuery(URI_NS + guid));
		} catch (URIException e) {
			e.printStackTrace();
		}
		return guidResource;
	}
}
