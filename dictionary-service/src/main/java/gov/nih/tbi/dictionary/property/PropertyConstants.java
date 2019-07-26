package gov.nih.tbi.dictionary.property;

import gov.nih.tbi.commons.util.RDFConstants;
import gov.nih.tbi.dictionary.property.model.EntityProperty;
import gov.nih.tbi.dictionary.property.model.EntityRdfProperty;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;

public class PropertyConstants {

	public final static List<EntityProperty> DE_PROPERTIES = new ArrayList<EntityProperty>();

	static {
		// @formatter:off
		// simple data element values (single triples that return strings that can be displayed)
		// title
		DE_PROPERTIES.add(new EntityRdfProperty("title", RDFConstants.TITLE_VARIABLE, RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_TITLE_NODE_N, RDFConstants.TITLE_VARIABLE));

		// status
		DE_PROPERTIES.add(new EntityRdfProperty("status", RDFConstants.STATUS_VARIABLE, RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_STATUS_NODE_N, RDFConstants.STATUS_VARIABLE));

		// description
		DE_PROPERTIES.add(new EntityRdfProperty("description", RDFConstants.DESCRIPTION_VARIABLE, RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_DESCRIPTION_NODE_N, RDFConstants.DESCRIPTION_VARIABLE));

		// shortDescription
		DE_PROPERTIES.add(new EntityRdfProperty("shortDescription", RDFConstants.SHORT_DESCRIPTION_VARIABLE, RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_SHORT_DESCRIPTION_NODE_N, RDFConstants.SHORT_DESCRIPTION_VARIABLE));

		// format
		DE_PROPERTIES.add(new EntityRdfProperty("format", RDFConstants.FORMAT_VARIABLE, RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_FORMAT, RDFConstants.FORMAT_VARIABLE));

		// notes
		DE_PROPERTIES.add(new EntityRdfProperty("notes", RDFConstants.NOTES_VARIABLE, RDFConstants.URI_NODE, RDFConstants.PROPERTY_DE_NOTES_NODE_N, RDFConstants.NOTES_VARIABLE));

		// historical notes
		DE_PROPERTIES.add(new EntityRdfProperty("historicalNotes", RDFConstants.HISTORICAL_NOTES_VARIABLE, RDFConstants.URI_NODE, RDFConstants.PROPERTY_DE_HISTORICAL_NOTES_NODE_N, RDFConstants.HISTORICAL_NOTES_VARIABLE));

		// references
		DE_PROPERTIES.add(new EntityRdfProperty("references", RDFConstants.REFERENCES_VARIABLE, RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_REFERENCES_N, RDFConstants.REFERENCES_VARIABLE));

		// population
		DE_PROPERTIES.add(new EntityRdfProperty("population", RDFConstants.POPULATION_VARIABLE, RDFConstants.URI_NODE, RDFConstants.PROPERTY_DE_POPULATION_NODE_N, RDFConstants.POPULATION_VARIABLE));

		// guidelines
		DE_PROPERTIES.add(new EntityRdfProperty("guidlines", RDFConstants.GUIDELINES_VARIABLE, RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_GUIDELINES_NODE_N, RDFConstants.GUIDELINES_VARIABLE));

		// seeAlso
		DE_PROPERTIES.add(new EntityRdfProperty("seeAlso", RDFConstants.SEE_ALSO_VARIABLE, RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_SEE_ALSO_NODE_N, RDFConstants.SEE_ALSO_VARIABLE));

		// complex data element values
		// submitter
		ArrayList<Node> submitterResultNodes = new ArrayList<Node>();
		submitterResultNodes.add(RDFConstants.SUBMITTING_CONTACT_INFO_VARIABLE);
		submitterResultNodes.add(RDFConstants.SUBMITTING_CONTACT_NAME_VARIABLE);
		submitterResultNodes.add(RDFConstants.SUBMITTING_ORG_NAME_VARIABLE);
		ArrayList<Node[]> submitterTriples = new ArrayList<Node[]>();
		submitterTriples.add(new Node[] {RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_SUBMITTING_ORG_NAME_NODE_N, RDFConstants.SUBMITTING_ORG_NAME_VARIABLE});
		submitterTriples.add(new Node[] {RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_SUBMITTING_CONTACT_NAME_NODE_N, RDFConstants.SUBMITTING_CONTACT_NAME_VARIABLE});
		submitterTriples.add(new Node[] {RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_SUBMITTING_CONTACT_INFO_NODE_N, RDFConstants.SUBMITTING_CONTACT_INFO_VARIABLE});
		DE_PROPERTIES.add(new EntityRdfProperty("submitter", submitterResultNodes, submitterTriples));
		
		// steward
		ArrayList<Node> stewardResultNodes = new ArrayList<Node>();
		stewardResultNodes.add(RDFConstants.SUBMITTING_CONTACT_INFO_VARIABLE);
		stewardResultNodes.add(RDFConstants.SUBMITTING_CONTACT_NAME_VARIABLE);
		stewardResultNodes.add(RDFConstants.SUBMITTING_ORG_NAME_VARIABLE);
		ArrayList<Node[]> stewardTriples = new ArrayList<Node[]>();
		stewardTriples.add(new Node[] {RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_STEWARD_CONTACT_INFO_NODE_N, RDFConstants.STEWARD_CONTACT_INFO_VARIABLE});
		stewardTriples.add(new Node[] {RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_STEWARD_CONTACT_NAME_NODE_N, RDFConstants.STEWARD_CONTACT_NAME_VARIABLE});
		stewardTriples.add(new Node[] {RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_STEWARD_ORG_NAME_NODE_N, RDFConstants.STEWARD_ORG_NAME_VARIABLE});
		DE_PROPERTIES.add(new EntityRdfProperty("steward", stewardResultNodes, stewardTriples));
		
		// steward
		ArrayList<Node> categoryResultNodes = new ArrayList<Node>();
		categoryResultNodes.add(RDFConstants.CATEGORY_TITLE_VARIABLE);
		ArrayList<Node[]> categoryTriples = new ArrayList<Node[]>();
		categoryTriples.add(new Node[] {RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_CATEGORY_NODE_N, RDFConstants.CATEGORY_URI_VARIABLE});
		categoryTriples.add(new Node[] {RDFConstants.CATEGORY_URI_VARIABLE, RDFConstants.PROPERTY_BRICS_TITLE_NODE_N,RDFConstants.CATEGORY_TITLE_VARIABLE});
		DE_PROPERTIES.add(new EntityRdfProperty("category", categoryResultNodes, categoryTriples));
		// @formatter:on

	}
}
