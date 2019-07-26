package gov.nih.tbi.semantic.model;

import gov.nih.tbi.account.model.hibernate.Account;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class AccountRDF {
	public static final String STRING = "Account";

	// TODO: Dynamic add Disease?
	public static final String BASE_URI_NS = "http://ninds.nih.gov/repository/fitbir/1.0/";
	public static final String URI_NS = BASE_URI_NS + STRING + "/";

	// Resources
	public static final Resource RESOURCE_ACCOUNT = resource(STRING);

	// Properties
	public static final Property PROPERTY_ID = property("id");
	public static final Property PROPERTY_USERNAME = property("username");
	public static final Property PROPERTY_DATASET = property("dataset");

	public static final Resource resource(String local) {

		return ResourceFactory.createResource(BASE_URI_NS + local);
	}

	public static final Property property(String local) {

		return ResourceFactory.createProperty(URI_NS, local);
	}

	public static String generateName(Account account) {

		return account.getUserName();
	}

	public static Resource createResource(Account account) {
		return createResource(generateName(account));
	}

	public static Resource createResource(String username) {

		Resource resource = null;

		try {
			resource = ResourceFactory.createResource(URIUtil.encodeQuery(URI_NS + username));
		} catch (URIException e) {
			e.printStackTrace();
		}
		return resource;

	}
}
