package gov.nih.tbi.semantic.model;

import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseElement;
import gov.nih.tbi.dictionary.model.hibernate.DomainPair;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * BRICS vocabulary items
 * 
 * @author Nimesh Patel
 * 
 */
public class DataElementRDF {
	static Logger log = Logger.getLogger(DataElementRDF.class);
	public static final String STRING_ELEMENT = "Element";
	public static final String STRING_COMMON_DATA_ELEMENT = "CommonDataElement";
	public static final String STRING_DATA_ELEMENT = "DataElement";
	public static final String STRING_DISEASE_ELEMENT = "DiseaseElement";
	public static final String STRING_DOMAIN_PAIR = "DomainPair";
	public static final String STRING_REQUIRED_TYPE = "RequiredType";

	public static final String BASE_URI_NS = "http://ninds.nih.gov/dictionary/ibis/1.0/";
	public static final String URI_NS = BASE_URI_NS + STRING_ELEMENT + "/";

	public static final String BASE_REPO_NS = "http://ninds.nih.gov/repository/fitbir/1.0/";
	public static final String REPO_NS = BASE_URI_NS + STRING_ELEMENT + "/";

	public static final String PERMISSIBLE_VALUE_CLASS = URI_NS + STRING_DATA_ELEMENT + "/permissibleValue";
	public static final String PERMISSIBLE_VALUE_NODE_FORMAT = URI_NS + STRING_DATA_ELEMENT + "/%s/permissibleValue/%s";

	// Resources
	public static final Resource RESOURCE_ELEMENT = ResourceFactory.createResource(BASE_URI_NS + "Element");
	public static final Resource RESOURCE_CDE = resource(STRING_COMMON_DATA_ELEMENT);
	public static final Resource RESOURCE_DE = resource(STRING_DATA_ELEMENT);
	public static final Resource RESOURCE_REQUIRED_TYPE = resource(STRING_REQUIRED_TYPE);

	// Properties
	public static final Property PROPERTY_ID = property("dataElementId");
	public static final Property PROPERTY_ELEMENT_NAME = property("elementName");
	public static final Property PROPERTY_DISEASE_ELEMENT = property("diseaseElement");
	public static final Property PROPERTY_ELEMENT_DISEASE = property("elementDisease");
	public static final Property PROPERTY_ELEMENT_CATEGORY = property("category");

	public static final Property PROPERTY_ELEMENT_DOMAIN_PAIR = property("domainPair");
	public static final Property PROPERTY_DECRIPTION = property("description");
	public static final Property PROPERTY_TITLE = property("title");

	public static final Property PROPERTY_ELEMENT_TYPE = property("elementType");
	public static final Property PROPERTY_INPUT_RESTRICTION = property("inputRestriction");
	public static final Property PROPERTY_ELEMENT_SIZE = property("elementSize");

	public static final Property PROPERTY_FORMAT = property("format");
	public static final Property PROPERTY_SHORT_DESCRIPTION = property("shortDescription");
	public static final Property PROPERTY_NOTES = property("notes");
	public static final Property PROPERTY_GUIDELINES = property("guidelines");
	public static final Property PROPERTY_HISTORICAL_NOTES = property("historicalNotes");
	public static final Property PROPERTY_REFERENCE = property("reference");
	public static final Property PROPERTY_CLASSIFICATION = property("classification");

	public static final Property PROPERTY_MAXIMUM_VALUE = property("maximumValue");
	public static final Property PROPERTY_MINIMUM_VALUE = property("minimumValue");
	public static final Property PROPERTY_PERMISSIBLE_VALUE = property("permissibleValue");

	public static final Property PROPERTY_PERMISSIBLE_VALUES = property("permissibleValues");
	public static final Property PROPERTY_PERMISSIBLE_VALUE_DESCRIPTION = property("permissibleValues#description");
	public static final Property PROPERTY_PERMISSIBLE_VALUE_VALUE = property("permissibleValues#value");
	public static final Property PROPERTY_DOMAIN = property("domain");
	public static final Property PROPERTY_SUBDOMAIN = property("subdomain");
	public static final Property PROPERTY_KEYWORD = property("keyword");
	public static final Property PROPERTY_LABEL = property("label");
	public static final Property PROPERTY_POPULATION = property("population");
	public static final Property POSITION = property("position");
	public static final Property PROPERTY_EXTERNAL_ID = property("externalId");
	public static final Property PROPERTY_CLASSIFICATIONS = property("classifications");
	public static final Property PROPERTY_SEE_ALSO = property("seeAlso");

	// Relationship Properties
	public static final Property RELATION_PROPERTY_HAS_REPEATABLE_GROUP = property("hasRepeatableGroup");

	public static final Resource resource(String local) {

		return ResourceFactory.createResource(URI_NS + local);
	}

	public static final Property property(String local) {

		return ResourceFactory.createProperty(URI_NS, local);
	}

	public static String getBaseURI() {

		return BASE_URI_NS;
	}

	// determines XSD Datatype of Data Element
	public static XSDDatatype determineDataType(StructuralDataElement de) {

		switch (de.getType()) {
			case ALPHANUMERIC:
				return XSDDatatype.XSDstring;
			case NUMERIC:
				// numeric multi-selects must be defined as a string, since there is a
				// possibility of a semi-colon delimiter being inserted
				if (InputRestrictions.MULTIPLE == de.getRestrictions()) {
					return XSDDatatype.XSDstring;
				}

				return XSDDatatype.XSDdecimal;
			case DATE:
				return XSDDatatype.XSDdateTime;
			case GUID:
				return XSDDatatype.XSDstring;
			case FILE:
				return XSDDatatype.XSDstring;
			case THUMBNAIL:
				return XSDDatatype.XSDstring;
			case BIOSAMPLE:
				return XSDDatatype.XSDstring;
			case TRIPLANAR:
				return XSDDatatype.XSDstring;
			default:
				return XSDDatatype.XSDstring;
		}
	}

	// Should be moved to Data Element Domain
	public static Resource createDEResource(StructuralDataElement de) {

		Resource cdeResource = null;

		try {
			if (de.isCommonDataElement()) {
				cdeResource = ResourceFactory
						.createResource(URIUtil.encodeQuery(URI_NS + STRING_COMMON_DATA_ELEMENT + "/" + de.getName()));
			} else {
				cdeResource = ResourceFactory
						.createResource(URIUtil.encodeQuery(URI_NS + STRING_DATA_ELEMENT + "/" + de.getName()));
			}
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cdeResource;

	}


	public static Resource createRequiredTypeResource(MapElement me) {

		Resource resource = null;

		try {
			resource = ResourceFactory.createResource(URIUtil.encodeQuery(
					URI_NS + STRING_DATA_ELEMENT + "/" + me.getRepeatableGroup().getDataStructure().getShortName() + "/"
							+ me.getRepeatableGroup().getName() + "/" + me.getStructuralDataElement().getName()
							+ "/requiredType"));
		} catch (URIException e) {
			e.printStackTrace();
		}
		return resource;
	}

	public static Resource createPermissibleValueResource(String dataElementName, ValueRange valueRange) {
		Resource resource = null;
		try {
			String nodeName = String.format(PERMISSIBLE_VALUE_NODE_FORMAT, dataElementName, valueRange.getValueRange());
			resource = ResourceFactory.createResource(URIUtil.encodeQuery(nodeName));
		} catch (URIException e) {
			log.error("Unable to create permissible value URI", e);
		}

		return resource;
	}

	// Should be moved to Data Element Domain
	public static Resource createDiseaseElementResource(DiseaseElement diseaseElement) {
		Resource diseaseElementResource = null;

		try {
			diseaseElementResource = ResourceFactory.createResource(
					URIUtil.encodeQuery(URI_NS + STRING_DISEASE_ELEMENT + "/" + diseaseElement.getId()));

		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return diseaseElementResource;
	}

	public static Resource createDomainPairResource(DomainPair domainPair) {
		Resource domainPairResource = null;

		try {
			domainPairResource = ResourceFactory.createResource(
					URIUtil.encodeQuery(URI_NS + STRING_DOMAIN_PAIR + "/" + generateDomainPairName(domainPair)));
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return domainPairResource;
	}

	private static String generateDomainPairName(DomainPair domainPair) {
		return domainPair.getDomain().getName() + "_" + domainPair.getSubdomain().getName();
	}

	public static Property createDEProperty(StructuralDataElement de) {

		Property cdeProperty = null;

		try {
			if (de.isCommonDataElement()) {
				cdeProperty = ResourceFactory
						.createProperty(URIUtil.encodeQuery(URI_NS + STRING_COMMON_DATA_ELEMENT + "/" + de.getName()));
			} else {
				cdeProperty = ResourceFactory
						.createProperty(URIUtil.encodeQuery(URI_NS + STRING_DATA_ELEMENT + "/" + de.getName()));
			}

		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cdeProperty;
	}

	public static Resource createDataElementValueResource(StructuralDataElement dataElement, String columnValue) {
		Resource cdeResource = null;

		try {
			if (dataElement.isCommonDataElement()) {
				cdeResource = ResourceFactory.createResource(URIUtil.encodeQuery(
						REPO_NS + STRING_COMMON_DATA_ELEMENT + "/" + dataElement.getName() + "/" + columnValue));
			} else {
				cdeResource = ResourceFactory
						.createResource(URIUtil.encodeQuery(REPO_NS + STRING_DATA_ELEMENT + "/" + dataElement.getName())
								+ "/" + columnValue);
			}
		} catch (URIException e) {
			e.printStackTrace();
		}

		return cdeResource;
	}
}
