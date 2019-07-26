
package gov.nih.tbi.semantic.model;

import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * BRICS vocabulary items
 * 
 * @author Nimesh Patel
 * 
 */
public class RepeatableGroupRDF {

	public static final String STRING_RG = "RepeatableGroup";

	// TODO: Dynamic ADD Disease?
	public static final String BASE_URI_NS = "http://ninds.nih.gov/repository/fitbir/1.0/";
	public static final String URI_NS = BASE_URI_NS + STRING_RG + "/";

	// Resources
	public static final Resource RESOURCE_RG = resource(STRING_RG);

	// Properties
	public static final Property PROPERTY_NAME = property("name");

	// Relationship Properties
	public static final Property RELATION_PROPERTY_HAS_FORM = property("hasForm");
	public static final Property RELATION_PROPERTY_HAS_DATA_ELEMENT = property("hasDataElement");

	public static final Property PROPERTY_POSITION = property("position");
	public static final Property PROPERTY_REPEAT_TYPE = property("type");
	public static final Property PROPERTY_THRESHOLD = property("threshold");

	public static final Resource resource(String local) {

		return ResourceFactory.createResource(BASE_URI_NS + local);
	}

	public static final Property property(String local) {

		return ResourceFactory.createProperty(URI_NS, local);
	}

	public static Resource createRGResource(RepeatableGroup rg) {
		return createRGResource(rg.getDataStructure().getShortName(), rg.getName());
	}

	/**
	 * Generates the repeatable group resource based on the title, version and name rather than on the original
	 * RepeatableGroup object.
	 * 
	 * @param title of the form
	 * @param version of the form
	 * @param name of the repeatable group
	 * @return
	 */
	public static Resource createRGResource(String title, String name) {

		Resource rgResource = null;

		try {
			rgResource = ResourceFactory.createResource(URIUtil.encodeQuery(URI_NS + generateName(title, name)));
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rgResource;

	}
	
	public static Resource createRGResourceInstance(RepeatableGroup rg, int rowNumber) {
		return createRGResourceInstance(rg.getDataStructure().getShortName(), rg.getName(), rowNumber);
	}

	/**
	 * This method generates the RDF Resource based on the title, version and name of the repeatable group rather than
	 * on the direct repeatable group itself. The original method calls this one.
	 * 
	 * The String "modifier" is used to make the data instance unique for the title and version. The original value was
	 * the row prepended with "_row". Now, it allows greater flexibility - specifically allowing us to pass in a
	 * datasetid "_ds" and a row.
	 * 
	 * @param title of the form
	 * @param version of the form
	 * @param name of the repeatable group
	 * @param modifier
	 * @return
	 */
	public static Resource createRGResourceInstance(String title, String name, String modifier) {

		Resource rgResource = null;

		try {
			rgResource =
					ResourceFactory.createResource(URIUtil.encodeQuery(URI_NS + generateName(title, name) + modifier));
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rgResource;

	}

	public static Resource createRGResourceInstance(String title, String name, int rowNumber) {
		String rowNumberPadded = String.format("%07d", rowNumber);
		return createRGResourceInstance(title, name, "_row" + rowNumberPadded);
	}

	public static String generateName(RepeatableGroup rg) {

		return generateName(rg.getDataStructure().getShortName(), rg.getName());
	}

	/**
	 * Use separate objects to determine the name of the element rather than the RepeatableGroup object. The previous
	 * method (that accepts a RepeatableGroup) now calls this method.
	 * 
	 * @param title
	 * @param version
	 * @param name
	 * @return
	 */
	public static String generateName(String title, String name) {

		return title + " " + name;
	}

	// Delete Me
	// public static Property createStudyProperty(AbstractStudy study)
	// {
	//
	// Property studyProperty = null;
	//
	// try
	// {
	// studyProperty = ResourceFactory.createProperty(URIUtil.encodeQuery(URI_NS + study.getTitle()));
	// }
	// catch (URIException e)
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return studyProperty;
	// }
}
