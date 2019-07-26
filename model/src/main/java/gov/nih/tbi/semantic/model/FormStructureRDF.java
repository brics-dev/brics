
package gov.nih.tbi.semantic.model;

import gov.nih.tbi.dictionary.model.hibernate.FormStructure;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.core.Var;

/**
 * BRICS vocabulary items
 * 
 * @author Nimesh Patel
 * 
 */
public class FormStructureRDF {
	public static final Var FORM_INSTANCE_VAR = Var.alloc("formInstance");
	public static final String STRING_FORM_STRUCTURE = "FormStructure";
	public static final String STRING_REPEATABLE_GROUP = "RepeatableGroup";

	public static final String BASE_URI_NS = "http://ninds.nih.gov/dictionary/ibis/1.0/";
	public static final String URI_NS = BASE_URI_NS + STRING_FORM_STRUCTURE + "/";

	// Resources
	public static final Resource RESOURCE_FORM_STRUCTURE = resource(STRING_FORM_STRUCTURE);
	public static final Resource RESOURCE_REPEATABLE_GROUP = resource(STRING_REPEATABLE_GROUP);

	// Properties
	public static final Property PROPERTY_ID = property("formStructureId");
	public static final Property PROPERTY_DESCRIPTION = property("description");
	public static final Property PROPERTY_TITLE = property("title");
	public static final Property PROPERTY_SHORT_NAME = property("shortName");
	public static final Property PROPERTY_VERSION = property("version");
	public static final Property PROPERTY_ORGANIZATION = property("organization");
	public static final Property PROPERTY_PUBLICATION_DATE = property("publicationDate");
	public static final Property PROPERTY_MODIFIED_DATE = property("modifiedDate");
	public static final Property PROPERTY_DISEASE = property("disease");
	public static final Property PROPERTY_MODIFIED_ACCOUNT = property("modifiedAccountId");
	public static final Property PROPERTY_GUID = property("guid");
	public static final Property PROPERTY_DATASET = property("dataset");

	// Relationship Properties
	public static final Property RELATION_PROPERTY_HAS_REPEATABLE_GROUP = property("hasRepeatableGroup");

	// FormInstance Properties
	public static final Property RELATION_PROPERTY_HAS_REPEATABLE_GROUP_INSTANCE =
			property("hasRepeatableGroupInstance");
	public static final Property RELATION_PROPERTY_IS_OF_FORM_INSTANCE = property("isOfFormInstance");

	public static final Resource resource(String local) {

		return ResourceFactory.createResource(BASE_URI_NS + local);
	}

	public static final Property property(String local) {

		return ResourceFactory.createProperty(URI_NS, local);
	}

	public static Resource createFormResource(FormStructure form) {
		return createFormResource(form.getShortName());
	}

	/**
	 * Uses the title and version number to generate the Form Resource rather than the AbstractDataStructure. This
	 * allows wider functionality.
	 * 
	 * @param name
	 * @return
	 */
	public static Resource createFormResource(String name) {

		Resource formResource = null;

		try {
			formResource = ResourceFactory.createResource(URIUtil.encodeQuery(URI_NS + generateName(name)));
		} catch (URIException e) {
			e.printStackTrace();
		}
		return formResource;

	}

	@Deprecated
	public static Resource createFormResourceInstance(FormStructure form, int rowNumber) {
		return createFormResourceInstance(form.getShortName(), rowNumber);
	}

	public static Resource createFormResourceInstance(FormStructure form, String submissionId) {
		return createFormResourceInstance(form.getShortName(), submissionId);
	}


	/**
	 * Extends from the createFormResourceInstance(AbstractDataStructure, int) to allow creation based on the parts. To
	 * be used when only the contents are available and the original object is not. The original now calls this method.
	 * 
	 * The String "modifier" is used to make the data instance unique for the title and version. The original value was
	 * the row prepended with "_row". Now, it allows greater flexibility - specifically allowing us to pass in a
	 * datasetid "_ds" and a row.
	 * 
	 * @param name
	 * @param modifier
	 * @return
	 */
	public static Resource createFormResourceInstance(String name, String modifier) {

		Resource formResource = null;

		try {
			formResource = ResourceFactory.createResource(URIUtil.encodeQuery(URI_NS + generateName(name) + modifier));
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return formResource;

	}

	public static Resource createFormResourceInstance(String name, int rowNumber) {
		String rowNumberPadded = String.format("%07d", rowNumber);
		return createFormResourceInstance(name, "_row" + rowNumberPadded);
	}

	public static String generateName(FormStructure form) {

		return generateName(form.getShortName());
	}

	/**
	 * Extend from the base generateName(AbstractDataStructure) to generate a name for the node based on its parts
	 * rather than on the object.
	 * 
	 * @param title
	 * @return
	 */
	public static String generateName(String name) {
		return name;
	}
}
