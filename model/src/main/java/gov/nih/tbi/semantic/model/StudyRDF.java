package gov.nih.tbi.semantic.model;

import gov.nih.tbi.repository.model.AbstractStudy;
import gov.nih.tbi.repository.model.hibernate.Study;

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
public class StudyRDF {

	public static final String STRING_STUDY = "Study";

	// TODO: Dynamic ADD Disease?
	public static final String BASE_URI_NS = "http://ninds.nih.gov/repository/fitbir/1.0/";
	public static final String URI_NS = BASE_URI_NS + STRING_STUDY + "/";

	// Resources
	public static final Resource RESOURCE_STUDY = resource(STRING_STUDY);

	// Properties
	public static final Property PROPERTY_ID = property("studyId");
	public static final Property PROPERTY_ABSTRACT = property("abstract");

	public static final Property PROPERTY_DATA_MANAGER = property("dataManager");
	public static final Property PROPERTY_DATA_MANAGER_EMAIL = property("dataManagerEmail");

	public static final Property PROPERTY_DATE_CREATED = property("dateCreated");
	public static final Property PROPERTY_PI = property("principalInvestigator");
	public static final Property PROPERTY_PI_EMAIL = property("principalInvestigatorEmail");
	public static final Property PROPERTY_TITLE = property("title");
	public static final Property PROPERTY_GRANT = property("grant");
	public static final Property PROPERTY_CLINICAL_TRIAL = property("clinicalTrial");
	public static final Property PROPERTY_STATUS = property("status");
	public static final Property PROPERTY_PREFIXED_ID = property("prefixedId");

	// Relationship Properties
	public static final Property RELATION_PROPERTY_HAS_DATA_FORM = property("hasDataForm");

	// Exhibit Relationship
	public static final Property RELATION_PROPERTY_FACETED_STUDY = property("facetedStudy");
	public static final Property RELATION_PROPERTY_FACETED_FORM = property("facetedForm");
	public static final Property RELATION_PROPERTY_FACETED_DE = property("facetedDE");
	public static final Property RELATION_PROPERTY_FACETED_DATASET = property("facetedDataset");

	public static final Resource resource(String local) {

		return ResourceFactory.createResource(BASE_URI_NS + local);
	}

	public static final Property property(String local) {

		return ResourceFactory.createProperty(URI_NS, local);
	}

	// Should be moved to Study Domain
	public static Resource createStudyResource(Study study) {

		Resource studyResource = null;

		try {
			studyResource = ResourceFactory.createResource(URIUtil.encodeQuery(URI_NS + study.getTitle()));
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return studyResource;

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
