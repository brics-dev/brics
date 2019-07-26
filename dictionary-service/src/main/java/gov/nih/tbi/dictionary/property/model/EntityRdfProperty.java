package gov.nih.tbi.dictionary.property.model;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;

public class EntityRdfProperty extends EntityProperty {

	List<Node> resultNodes;

	// Length is always 3. Correspond to arguments of
	// body.addElement(buildSingleOptionalPattern(...) in QueryConstructionUtil
	List<Node[]> triples;

	public EntityRdfProperty(String fieldsValue, Node resultNode, Node subjectNode, Node propertyNode,
			Node objectNode) {
		this.setFieldsValue(fieldsValue);
		resultNodes = new ArrayList<Node>();
		resultNodes.add(resultNode);
		this.setDatastore(RDF_PROPERTY);
		triples = new ArrayList<Node[]>();
		triples.add(new Node[] {subjectNode, propertyNode, objectNode});

	}

	public EntityRdfProperty(String fieldsValue, ArrayList<Node> resultNodes, ArrayList<Node[]> triples) {
		this.setFieldsValue(fieldsValue);
		this.resultNodes = resultNodes;
		this.setDatastore(RDF_PROPERTY);
		this.triples = triples;
	}

	public List<Node> getResultNodes() {
		return resultNodes;
	}

	public List<Node[]> getTriples() {
		return triples;
	}

	// public static class Triple {
	// private Node subjectNode;
	// private Node propertyNode;
	// private Node objectNode;
	//
	// public Triple(Node subjectNode, Node propertyNode, Node objectNode) {
	// this.subjectNode = subjectNode;
	// this.propertyNode = propertyNode;
	// this.objectNode = objectNode;
	// }
	//
	// public Node getSubjectNode() {
	// return subjectNode;
	// }
	//
	// public Node getPropertyNode() {
	// return propertyNode;
	// }
	//
	// public Node getObjectNode() {
	// return objectNode;
	// }
	// }

}
