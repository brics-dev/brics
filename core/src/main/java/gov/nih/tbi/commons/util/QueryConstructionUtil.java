package gov.nih.tbi.commons.util;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.dictionary.model.hibernate.Classification;
import gov.nih.tbi.dictionary.model.hibernate.ClassificationElement;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseElement;
import gov.nih.tbi.dictionary.model.hibernate.DomainPair;
import gov.nih.tbi.dictionary.model.hibernate.ExternalId;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;
import gov.nih.tbi.dictionary.model.hibernate.SubDomainElement;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;
import gov.nih.tbi.semantic.model.E_Distinct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.http.impl.cookie.DateParseException;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Bound;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_NotExists;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.aggregate.AggCountVarDistinct;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
import com.hp.hpl.jena.sparql.expr.aggregate.AggregatorFactory;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueString;
import com.hp.hpl.jena.sparql.modify.request.QuadAcc;
import com.hp.hpl.jena.sparql.modify.request.QuadDataAcc;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class QueryConstructionUtil extends SparqlConstructionUtil {

	/**
	 * Given a set of search words, if the search word is enclosed in quotes, we need to remove the quotes and enter the
	 * searchword as is. If the search word is not enclosed in quotes, we need to replace whitespaces with wildcard .*
	 * See <b>PS-2367</b> for more info.
	 * 
	 * @param Set<String> searchWords
	 * @return Set<String>
	 */
	public static Set<String> processSearchTerms(Set<String> searchTerms) {
		Set<String> finalSearchTerms = new HashSet<String>();

		for (String searchTerm : searchTerms) {

			if (searchTerm.startsWith(CoreConstants.QUOTE) && searchTerm.endsWith(CoreConstants.QUOTE)) {
				// if we have starting and ending quotes, we need to remove them as we don't actually want to search for
				// the quotes.
				searchTerm = searchTerm.substring(1, searchTerm.length() - 1);
			} 
			else if (searchTerm.endsWith(CoreConstants.ASTRIXWILDCARD)){
				searchTerm = CoreConstants.CIRCUMFLEX + searchTerm.replace(CoreConstants.ASTRIXWILDCARD, "");
			}
			else if (searchTerm.startsWith(CoreConstants.ASTRIXWILDCARD)){
				searchTerm =CoreConstants.SEARCH_CHARACTER+ searchTerm.replaceAll("\\s+", CoreConstants.REGEX_MATCH_ANY);
			}
			else if (searchTerm.endsWith(CoreConstants.QUESTIONWILDCARD)){
				searchTerm = CoreConstants.CIRCUMFLEX + searchTerm.replace(CoreConstants.QUESTIONWILDCARD, CoreConstants.REGEXMATCH);
			}
			else {
				// if the search term is not enclosed with double quotes "...we will replace all whitespaces
				// with multi-character wildcard characters.
				searchTerm = searchTerm.replaceAll("\\s+", CoreConstants.REGEX_MATCH_ANY);
			}
			finalSearchTerms.add(searchTerm);
		}

		return finalSearchTerms;
	}

	public static QuadAcc generateDataElementDeleteTriples(SemanticDataElement de) {

		Node deURI = null;

		// use existing URI
		if (de.getUri() != null) {
			deURI = NodeFactory.createURI(de.getUri());
		} else
		// generate new URI
		{
			deURI = createDataElementUri(de);
			de.setUri(deURI.getURI());
		}

		QuadAcc quads = new QuadAcc();
		quads.addTriple(Triple.create(deURI, Var.alloc("p"), Var.alloc("o")));

		return quads;
	}

	/**
	 * Generate the triples for the provided classification
	 * 
	 * @param classification
	 * @return
	 */
	public final static QuadDataAcc generateClassificationTriples(Classification classification) {

		Node classificationURI = null;

		if (classification.getName() == null) {
			throw new NullPointerException("Classification name cannot be null!");
		}

		// if the uri already exists, just use the existing URI
		if (classification.getUri() != null) {
			classificationURI = NodeFactory.createURI(classification.getUri());
		} else
		// generate new URI
		{
			classificationURI = createClassificationUri(classification);
			classification.setUri(classificationURI.getURI());
		}

		QuadDataAcc quads = new QuadDataAcc();
		quads.addTriple(Triple.create(classificationURI, RDFS.Nodes.subClassOf,
				NodeFactory.createURI(RDFConstants.CLASSIFICATION)));
		quads.addTriple(Triple.create(classificationURI, RDF.type.asNode(), RDFS.Class.asNode()));
		quads.addTriple(Triple.create(classificationURI, RDFS.isDefinedBy.asNode(), classificationURI));
		quads.addTriple(Triple.create(classificationURI, RDFS.label.asNode(),
				NodeFactory.createLiteral(classification.getName())));
		quads.addTriple(Triple.create(classificationURI, RDFConstants.PROPERTY_CLASSIFICATION_CAN_CREATE,
				NodeFactory.createLiteral(classification.getCanCreate().toString(), XSDDatatype.XSDboolean)));

		return quads;
	}

	public final static QuadDataAcc generateKeywordTriples(Keyword keyword) {

		Node keywordUri = null;

		if (keyword.getUri() != null) {
			keywordUri = NodeFactory.createURI(keyword.getUri());
		} else {
			keywordUri = createKeywordUri(keyword);
			keyword.setUri(keywordUri.getURI());
		}

		QuadDataAcc quads = new QuadDataAcc();

		quads.addTriple(Triple.create(keywordUri, RDF.type.asNode(), NodeFactory.createURI(RDFConstants.KEYWORD)));
		quads.addTriple(Triple.create(keywordUri, NodeFactory.createURI(RDFConstants.BRICS.concat("#shortName")),
				NodeFactory.createLiteral(keyword.getKeyword())));
		quads.addTriple(Triple.create(keywordUri, NodeFactory.createURI(RDFConstants.BRICS.concat("#title")),
				NodeFactory.createLiteral(keyword.getKeyword())));

		/*
		 * quads.addTriple(Triple.create(keywordUri, RDF.type.asNode(), RDFS.Class.asNode()));
		 * quads.addTriple(Triple.create(keywordUri, RDFS.isDefinedBy.asNode(), keywordUri));
		 * quads.addTriple(Triple.create(keywordUri, RDFS.label.asNode(),
		 * NodeFactory.createLiteral(keyword.getKeyword()))); quads.addTriple(Triple.create(keywordUri,
		 * RDFConstants.PROPERTY_KEWORD_COUNT, NodeFactory.createLiteral(keyword.getCount().toString(),
		 * XSDDatatype.XSDlong)));
		 */

		return quads;
	}

	public final static QuadDataAcc generateLabelTriples(Keyword label) {

		Node labelUri = null;

		if (label.getUri() != null) {
			labelUri = NodeFactory.createURI(label.getUri());
		} else {
			labelUri = createLabelUri(label);
			label.setUri(labelUri.getURI());
		}

		QuadDataAcc quads = new QuadDataAcc();

		quads.addTriple(Triple.create(labelUri, RDF.type.asNode(), NodeFactory.createURI(RDFConstants.LABEL)));
		quads.addTriple(Triple.create(labelUri, NodeFactory.createURI(RDFConstants.BRICS.concat("#shortName")),
				NodeFactory.createLiteral(label.getKeyword())));
		quads.addTriple(Triple.create(labelUri, NodeFactory.createURI(RDFConstants.BRICS.concat("#title")),
				NodeFactory.createLiteral(label.getKeyword())));

		return quads;
	}

	/**
	 * Append the triples that limites the results to the latest data elements ?uri
	 * <http://ninds.nih.gov/dictionary/1.0/brics#type> ?baseUri . ?baseUri
	 * <http://ninds.nih.gov/dictionary/1.0/brics#latest> ?uri .
	 *
	 * @param query
	 * @return
	 */
	public final static Query addLatestTriples(Query query) {
		ElementGroup group = (ElementGroup) query.getQueryPattern();
		ElementTriplesBlock triples = (ElementTriplesBlock) group.getElements().get(0);
		triples.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_TYPE_NODE_N,
				RDFConstants.BASE_URI_VARIABLE));
		triples.addTriple(Triple.create(RDFConstants.BASE_URI_VARIABLE, RDFConstants.PROPERTY_BRICS_LATEST_NODE_N,
				RDFConstants.URI_NODE));
		return query;
	}

	public final static QuadDataAcc generateDataElementTriples(SemanticDataElement de)
			throws DatatypeFormatException, DateParseException {

		Node deURI = null;
		if (de.getVersion() == null) {
			de.setVersion("1.0");
		}

		// use existing URI
		if (de.getDateCreated() == null) {
			de.setDateCreated(new Date());
		}

		deURI = createDataElementUri(de);
		de.setUri(deURI.getURI());

		QuadDataAcc quads = new QuadDataAcc();

		/** Add RDF elements **/
		// quads.addTriple(Triple.create(deURI, RDFS.Nodes.subClassOf, RDFConstants.DATA_ELEMENT_NODE));
		quads.addTriple(Triple.create(deURI, RDF.type.asNode(), RDFConstants.DATA_ELEMENT_NODE));
		// quads.ad dTriple(Triple.create(deURI, RDF.type.asNode(), RDFS.Class.asNode()));
		quads.addTriple(Triple.create(deURI, NodeFactory.createURI(RDFConstants.BRICS.concat("#type")),
				createDataElementBaseUri(de)));
		quads.addTriple(Triple.create(createDataElementBaseUri(de),
				NodeFactory.createURI(RDFConstants.BRICS.concat("#latest")), deURI));
		// quads.addTriple(Triple.create(deURI, RDFS.isDefinedBy.asNode(), deURI));

		/** Add BRICS related elements **/
		if (de.getTitle() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_BRICS_TITLE_NODE_N,
					NodeFactory.createLiteral(de.getTitle())));
		if (de.getDescription() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_BRICS_DESCRIPTION_NODE_N,
					NodeFactory.createLiteral(de.getDescription())));
		if (de.getName() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_BRICS_SHORT_NAME_NODE_N,
					NodeFactory.createLiteral(de.getName())));
		if (de.getVersion() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_BRICS_VERSION_NODE_N,
					NodeFactory.createLiteral(de.getVersion().toString())));

		/*
		 * Check status. Set it to "DRAFT" if it is not previously set.
		 */
		DataElementStatus status = de.getStatus();
		if (status == null) {
			status = DataElementStatus.DRAFT;
		}
		quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_BRICS_STATUS_NODE_N,
				NodeFactory.createLiteral(status.getName())));

		for (SubDomainElement sde : de.getSubDomainElementList()) {
			Node subdomainNode = createSubDomainNode(sde);
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_DE_SUBDOMAIN_NODE_N, subdomainNode));
			quads.addTriple(Triple.create(subdomainNode, RDFConstants.PROPERTY_SUBDOMAIN_DISEASE_N,
					NodeFactory.createLiteral(sde.getDisease().getName())));
			quads.addTriple(Triple.create(subdomainNode, RDFConstants.PROPERTY_SUBDOMAIN_DOMAIN_N,
					NodeFactory.createLiteral(sde.getDomain().getName())));
			quads.addTriple(Triple.create(subdomainNode, RDFConstants.PROPERTY_SUBDOMAIN_SUBDOMAIN_N,
					NodeFactory.createLiteral(sde.getSubDomain().getName())));
		}

		/** Add DataElement specific elements **/
		if (de.getModifiedDate() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_DE_MODIFIED_DATE_N, NodeFactory
					.createLiteral(BRICSTimeDateUtil.formatDateTime(de.getModifiedDate()), XSDDatatype.XSDdateTime)));
		if (de.getDateCreated() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_DE_DATE_CREATED_N, NodeFactory
					.createLiteral(BRICSTimeDateUtil.formatDateTime(de.getDateCreated()), XSDDatatype.XSDdateTime)));
		if (de.getCreatedBy() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_DE_CREATED_BY_N,
					NodeFactory.createLiteral(de.getCreatedBy())));
		if (de.getNotes() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_DE_NOTES_NODE_N,
					NodeFactory.createLiteral(de.getNotes())));
		if (de.getPopulation() != null) {
			// added an extra if statements for 1199
			if (de.getPopulation().getName() != null && !de.getPopulation().getName().isEmpty()) {
				quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_DE_POPULATION_NODE_N,
						NodeFactory.createLiteral(de.getPopulation().getName())));
			} else {
				throw new NullPointerException(
						"The population name is null or empty. This is a required field. The data element variable name = "
								+ de.getName() + " and the version is = " + de.getVersion()
								+ ". The semantic part was not saved to the database!");
			}
		}
		if (de.getHistoricalNotes() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_DE_HISTORICAL_NOTES_NODE_N,
					NodeFactory.createLiteral(de.getHistoricalNotes())));

		if (de.getReferences() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_ELEMENT_REFERENCES_N,
					NodeFactory.createLiteral(de.getReferences())));

		if (de.getFormat() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_ELEMENT_FORMAT,
					NodeFactory.createLiteral(de.getFormat())));
		if (de.getGuidelines() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_ELEMENT_GUIDELINES_NODE_N,
					NodeFactory.createLiteral(de.getGuidelines())));
		if (de.getShortDescription() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_ELEMENT_SHORT_DESCRIPTION_NODE_N,
					NodeFactory.createLiteral(de.getShortDescription())));
		if (de.getCategory() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_ELEMENT_CATEGORY_NODE_N, NodeFactory.createURI(
					RDFConstants.PROPERTY_ELEMENT_CATEGORY_NODE_N.getURI() + "/" + de.getCategory().getShortName())));
		if (de.getSubmittingContactInfo() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_ELEMENT_SUBMITTING_CONTACT_INFO_NODE_N,
					NodeFactory.createLiteral(de.getSubmittingContactInfo())));
		if (de.getSubmittingContactName() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_ELEMENT_SUBMITTING_CONTACT_NAME_NODE_N,
					NodeFactory.createLiteral(de.getSubmittingContactName())));
		if (de.getSubmittingOrgName() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_ELEMENT_SUBMITTING_ORG_NAME_NODE_N,
					NodeFactory.createLiteral(de.getSubmittingOrgName())));
		if (de.getStewardContactInfo() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_ELEMENT_STEWARD_CONTACT_INFO_NODE_N,
					NodeFactory.createLiteral(de.getStewardContactInfo())));
		if (de.getStewardContactName() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_ELEMENT_STEWARD_CONTACT_NAME_NODE_N,
					NodeFactory.createLiteral(de.getStewardContactName())));
		if (de.getStewardOrgName() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_ELEMENT_STEWARD_ORG_NAME_NODE_N,
					NodeFactory.createLiteral(de.getStewardOrgName())));
		if (de.getEffectiveDate() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_ELEMENT_EFFECTIVE_DATE_NODE_N, NodeFactory
					.createLiteral(BRICSTimeDateUtil.formatDateTime(de.getEffectiveDate()), XSDDatatype.XSDdateTime)));
		if (de.getUntilDate() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_ELEMENT_UNTIL_DATE_NODE_N, NodeFactory
					.createLiteral(BRICSTimeDateUtil.formatDateTime(de.getUntilDate()), XSDDatatype.XSDdateTime)));
		if (de.getSeeAlso() != null)
			quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_ELEMENT_SEE_ALSO_NODE_N,
					NodeFactory.createLiteral(de.getSeeAlso())));

		if (de.getType() != null) {
			try {
				quads.addTriple(Triple.create(deURI, RDFConstants.PROPERTY_ELEMENT_TYPE_NODE_N,
						NodeFactory.createLiteral(de.getType().name())));
			} catch (Exception e) {
				// ignore.... this requires testing on the server.
			}
		}

		// if (de.getDiseaseList() != null)
		// {
		// for (DiseaseElement diseaseElement : de.getDiseaseList())
		// {
		// quads = addTriples(quads, generateDiseaseElementTriples(de, diseaseElement));
		// }
		// }

		if (de.getClassificationElementList() != null) {
			for (ClassificationElement ce : de.getClassificationElementList()) {
				quads = SparqlConstructionUtil.addTriples(quads, generateClassificationElementTriples(de, ce));
			}
		}

		if (de.getKeywords() != null) {
			for (Keyword k : de.getKeywords()) {
				quads = SparqlConstructionUtil.addTriples(quads, generateKeywordElementTriples(de, k));
			}
		}

		if (de.getLabels() != null) {
			for (Keyword label : de.getLabels()) {
				quads = SparqlConstructionUtil.addTriples(quads, generateLabelElementTriples(de, label));
			}
		}

		if (de.getExternalIdSet() != null) {
			for (ExternalId externalId : de.getExternalIdSet()) {
				if (externalId != null && externalId.getValue() != null && !externalId.getValue().isEmpty()) {
					quads = SparqlConstructionUtil.addTriples(quads, generateExternalIdTriples(de, externalId));
				}
			}
		}

		if (de.getValueRangeList() != null) {
			for (ValueRange pv : de.getValueRangeList()) {
				quads = SparqlConstructionUtil.addTriples(quads, generatePermissibleValueTriples(de, pv));
			}
		}

		return quads;
	}

	private static Node createSubDomainNode(SubDomainElement sde) {

		Node uri = null;

		try {
			return NodeFactory.createURI(
					URIUtil.encodeQuery(RDFConstants.SUBDOMAIN_NS + uri_encode(sde.getDisease().getName()) + "/"
							+ uri_encode(sde.getDomain().getName()) + "/" + uri_encode(sde.getSubDomain().getName())));

		} catch (URIException e) {
			e.printStackTrace();
		}

		return uri;
	}

	/**
	 * Generate triples for permissible values
	 * 
	 * @param de
	 * @param pv
	 * @return
	 */
	private static List<Triple> generatePermissibleValueTriples(SemanticDataElement de, ValueRange pv) {

		List<Triple> triples = new ArrayList<Triple>();
		Node deUri = NodeFactory.createURI(de.getUri());
		Node pvUri = null;

		if (pv.getUri() != null) {
			pvUri = NodeFactory.createURI(pv.getUri());
		} else {
			pvUri = createPermissibleValueUri(de, pv);
			pv.setUri(pvUri.getURI());
		}

		if (pv.getValueRange() != null) {
			triples.add(Triple.create(deUri, RDFConstants.PROPERTY_ELEMENT_PERMISSIBLE_VALUE_N, pvUri));
			triples.add(Triple.create(pvUri, RDF.type.asNode(), NodeFactory.createURI(RDFConstants.PERMISSIBLE_VALUE)));
			triples.add(Triple.create(pvUri, RDFS.label.asNode(), NodeFactory.createLiteral(pv.getValueRange())));
		}

		if (pv.getDescription() != null) {
			triples.add(Triple.create(pvUri, NodeFactory.createURI(RDFConstants.PROPERTY_BRICS_DESCRIPTION_N),
					NodeFactory.createLiteral(pv.getDescription())));
		}

		return triples;
	}

	/**
	 * Generate triples for externalIds
	 * 
	 * @param de
	 * @param externalId
	 * @return
	 */
	private static List<Triple> generateExternalIdTriples(SemanticDataElement de, ExternalId externalId) {

		List<Triple> triples = new ArrayList<Triple>();
		Node deUri = NodeFactory.createURI(de.getUri());
		Node externalIdURI = null;

		if (externalId.getUri() != null) {
			externalIdURI = NodeFactory.createURI(externalId.getUri());
		} else {
			externalIdURI = createExternalIdURI(externalId);
			externalId.setUri(externalIdURI.getURI());
		}

		externalId.setUri(externalIdURI.getURI());

		triples.add(Triple.create(deUri, RDFConstants.PROPERTY_ELEMENT_EXTERNAL_ID, externalIdURI));
		triples.add(Triple.create(externalIdURI, RDFS.subClassOf.asNode(),
				NodeFactory.createURI(RDFConstants.EXTERNAL_ID)));
		triples.add(Triple.create(externalIdURI, RDF.type.asNode(), RDFS.Class.asNode()));
		triples.add(Triple.create(externalIdURI, RDFS.isDefinedBy.asNode(), externalIdURI));
		triples.add(Triple.create(externalIdURI, RDFConstants.PROPERTY_EXTERNAL_ID_TYPE,
				NodeFactory.createLiteral(externalId.getSchema().getName())));
		triples.add(Triple.create(externalIdURI, RDFConstants.PROPERTY_EXTERNAL_ID_VALUE,
				NodeFactory.createLiteral(externalId.getValue())));

		return triples;
	}

	/**
	 * Generate triples for keywordElements
	 * 
	 * @param de
	 * @param kw
	 * @return
	 */
	private static List<Triple> generateKeywordElementTriples(SemanticDataElement de, Keyword keyword) {

		List<Triple> triples = new ArrayList<Triple>();
		Node deUri = NodeFactory.createURI(de.getUri());
		Node keywordUri = null;

		if (keyword.getUri() != null) {
			keywordUri = NodeFactory.createURI(keyword.getUri());
		} else {
			keywordUri = createKeywordUri(keyword);
			keyword.setUri(keywordUri.getURI());
		}

		triples.add(Triple.create(keywordUri, RDF.type.asNode(), NodeFactory.createURI(RDFConstants.KEYWORD)));
		triples.add(Triple.create(keywordUri, NodeFactory.createURI(RDFConstants.BRICS.concat("#shortName")),
				NodeFactory.createLiteral(keyword.getKeyword())));
		triples.add(Triple.create(keywordUri, NodeFactory.createURI(RDFConstants.BRICS.concat("#title")),
				NodeFactory.createLiteral(keyword.getKeyword())));

		triples.add(Triple.create(deUri, RDFConstants.PROPERTY_ELEMENT_KEYWORDS_N, keywordUri));
		// triples.add(Triple.create(kwUri, RDFS.subClassOf.asNode(), NodeFactory.createURI(RDFConstants.KEYWORD)));
		// triples.add(Triple.create(kwUri, RDF.type.asNode(), RDFS.Class.asNode()));
		// triples.add(Triple.create(kwUri, RDFS.isDefinedBy.asNode(), kwUri));
		// triples.add(Triple.create(kwUri, RDFS.label.asNode(), NodeFactory.createLiteral(kw.getKeyword())));
		// triples.add(Triple.create(kwUri, RDFConstants.PROPERTY_KEWORD_COUNT,
		// NodeFactory.createLiteral(kw.getCount().toString(), XSDDatatype.XSDlong)));

		return triples;
	}

	public static Query getLabelDataElementQuery() {

		Query labelDataElementQuery = QueryFactory.make();
		labelDataElementQuery.setQuerySelectType();
		labelDataElementQuery.setDistinct(true);

		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		labelDataElementQuery.setQueryPattern(body);

		block.addTriple(Triple.create(RDFConstants.DATA_ELEMENT_VARIABLE, RDFConstants.PROPERTY_ELEMENT_LABELS,
				RDFConstants.LABEL_VARIABLE));

		labelDataElementQuery.addResultVar(RDFConstants.DATA_ELEMENT_VARIABLE);
		labelDataElementQuery.addResultVar(RDFConstants.LABEL_VARIABLE);

		return labelDataElementQuery;
	}

	/**
	 * Generate triples for keywordElements
	 * 
	 * @param de
	 * @param kw
	 * @return
	 */
	private static List<Triple> generateLabelElementTriples(SemanticDataElement de, Keyword label) {

		List<Triple> triples = new ArrayList<Triple>();
		Node deUri = NodeFactory.createURI(de.getUri());
		Node labelUri = null;

		if (label.getUri() != null) {
			labelUri = NodeFactory.createURI(label.getUri());
		} else {
			labelUri = createLabelUri(label);
			label.setUri(labelUri.getURI());
		}

		triples.add(Triple.create(labelUri, RDF.type.asNode(), NodeFactory.createURI(RDFConstants.LABEL)));
		triples.add(Triple.create(labelUri, NodeFactory.createURI(RDFConstants.BRICS.concat("#shortName")),
				NodeFactory.createLiteral(label.getKeyword())));
		triples.add(Triple.create(labelUri, NodeFactory.createURI(RDFConstants.BRICS.concat("#title")),
				NodeFactory.createLiteral(label.getKeyword())));

		triples.add(Triple.create(deUri, RDFConstants.PROPERTY_ELEMENT_LABELS_N, labelUri));
		// triples.add(Triple.create(kwUri, RDFS.subClassOf.asNode(), NodeFactory.createURI(RDFConstants.KEYWORD)));
		// triples.add(Triple.create(kwUri, RDF.type.asNode(), RDFS.Class.asNode()));
		// triples.add(Triple.create(kwUri, RDFS.isDefinedBy.asNode(), kwUri));
		// triples.add(Triple.create(kwUri, RDFS.label.asNode(), NodeFactory.createLiteral(kw.getKeyword())));
		// triples.add(Triple.create(kwUri, RDFConstants.PROPERTY_KEWORD_COUNT,
		// NodeFactory.createLiteral(kw.getCount().toString(), XSDDatatype.XSDlong)));

		return triples;
	}

	/**
	 * Generate triples for classification elements
	 * 
	 * @param de
	 * @param ce
	 * @return
	 */
	private static List<Triple> generateClassificationElementTriples(SemanticDataElement de, ClassificationElement ce) {

		List<Triple> triples = new ArrayList<Triple>();
		Node deUri = NodeFactory.createURI(de.getUri());
		Node ceUri = null;

		if (ce.getUri() != null) {
			ceUri = NodeFactory.createURI(ce.getUri());
		} else {
			ceUri = createClassificationElementURI(ce);
			ce.setUri(ceUri.getURI());
		}

		triples.add(Triple.create(ceUri, RDFConstants.PROPERTY_CLASSIFICATION_DISEASE_N,
				NodeFactory.createLiteral(ce.getDisease().getName())));
		triples.add(Triple.create(ceUri, RDFConstants.PROPERTY_CLASSIFICATION_SUBGROUP_N,
				NodeFactory.createLiteral(ce.getSubgroup().getSubgroupName())));
		triples.add(Triple.create(ceUri, RDFConstants.PROPERTY_CLASSIFICATION_VALUE_N,
				NodeFactory.createLiteral(ce.getClassification().getName())));

		triples.add(Triple.create(deUri, RDFConstants.PROPERTY_ELEMENT_CLASSIFICATION_ELEMENT_N, ceUri));

		return triples;
	}

	/**
	 * Returns a list of triples for a single disease element
	 * 
	 * @param dataElement
	 * @param diseaseElement
	 * @return
	 */
	private static List<Triple> generateDiseaseElementTriples(SemanticDataElement dataElement,
			DiseaseElement diseaseElement) {

		List<Triple> triples = new ArrayList<Triple>();
		Node deUri = NodeFactory.createURI(dataElement.getUri());
		Node diseaseElementUri = null;

		if (diseaseElement.getUri() != null) {
			diseaseElementUri = NodeFactory.createURI(diseaseElement.getUri());
		} else {
			diseaseElementUri = createDiseaseElementURI(dataElement, diseaseElement);
			diseaseElement.setUri(diseaseElementUri.getURI());
		}

		triples.add(Triple.create(deUri, RDFConstants.PROPERTY_ELEMENT_DISEASE_ELEMENT_NODE_N, diseaseElementUri));
		triples.add(Triple.create(diseaseElementUri, RDFConstants.PROPERTY_DISEASE_ELEMENT_DISEASE_NODE,
				NodeFactory.createLiteral(diseaseElement.getDisease().getName())));
		triples.add(Triple.create(diseaseElementUri, RDFS.subClassOf.asNode(),
				NodeFactory.createURI(RDFConstants.DISEASE_ELEMENT)));
		triples.add(Triple.create(diseaseElementUri, RDF.type.asNode(), RDFS.Class.asNode()));
		triples.add(Triple.create(diseaseElementUri, RDFS.isDefinedBy.asNode(), diseaseElementUri));

		for (DomainPair domainPair : diseaseElement.getDomainList()) {
			Node domainPairURI = createDomainPairURI(dataElement, diseaseElement, domainPair);

			triples.add(Triple.create(diseaseElementUri, RDFConstants.PROPERTY_DISEASE_ELEMENT_DOMAIN_PAIR_NODE,
					domainPairURI));
			triples.add(Triple.create(domainPairURI, RDFS.subClassOf.asNode(),
					NodeFactory.createURI(RDFConstants.DOMAIN_PAIR)));
			triples.add(Triple.create(domainPairURI, RDF.type.asNode(), RDFS.Class.asNode()));
			triples.add(Triple.create(domainPairURI, RDFS.isDefinedBy.asNode(), domainPairURI));
			triples.add(Triple.create(domainPairURI, RDFConstants.PROPERTY_DOMAIN_PAIR_DOMAIN_NODE,
					NodeFactory.createLiteral(domainPair.getDomain().getName())));
			triples.add(Triple.create(domainPairURI, RDFConstants.PROPERTY_DOMAIN_PAIR_SUB_DOMAIN_NODE,
					NodeFactory.createLiteral(domainPair.getSubdomain().getName())));
		}

		return triples;
	}

	public static Query getDomainPairQuery() {

		Query domainPairQuery = QueryFactory.make();
		domainPairQuery.setQuerySelectType();
		domainPairQuery.setDistinct(true);
		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		domainPairQuery.setQueryPattern(body);

		block.addTriple(Triple.create(RDFConstants.DISEASE_ELEMENT_VARIABLE,
				RDFConstants.PROPERTY_DISEASE_ELEMENT_DOMAIN_PAIR_NODE, RDFConstants.DOMAIN_PAIR_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.DOMAIN_PAIR_VARIABLE, RDFConstants.PROPERTY_DOMAIN_PAIR_DOMAIN_NODE,
				RDFConstants.DOMAIN_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.DOMAIN_PAIR_VARIABLE,
				RDFConstants.PROPERTY_DOMAIN_PAIR_SUB_DOMAIN_NODE, RDFConstants.SUB_DOMAIN_VARIABLE));

		domainPairQuery.addResultVar(RDFConstants.DISEASE_ELEMENT_VARIABLE);
		domainPairQuery.addResultVar(RDFConstants.DOMAIN_PAIR_VARIABLE);
		domainPairQuery.addResultVar(RDFConstants.DOMAIN_VARIABLE);
		domainPairQuery.addResultVar(RDFConstants.SUB_DOMAIN_VARIABLE);

		return domainPairQuery;
	}

	public static Query getFormDiseaseQuery() {

		Query formDiseaseQuery = QueryFactory.make();
		formDiseaseQuery.setQuerySelectType();
		formDiseaseQuery.setDistinct(true);
		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		formDiseaseQuery.setQueryPattern(body);

		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFS.Nodes.subClassOf, RDFConstants.FORM_STRUCTURE_NODE));

		// adding variables to be selected
		formDiseaseQuery.addResultVar(RDFConstants.URI_VARIABLE_NAME);
		formDiseaseQuery.addResultVar(RDFConstants.DISEASE_VARIABLE);

		body.addElement(QueryConstructionUtil.buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_FS_DISEASE_NODE_N, RDFConstants.DISEASE_VARIABLE));

		return formDiseaseQuery;
	}

	/**
	 * Generates the basic Form Structure query. Adapted to the new RDF model.
	 * 
	 * @return
	 */
	public final static Query getBasicFormStructureQuery() {

		Query basicFormStructureQuery = QueryFactory.make();
		basicFormStructureQuery.setQuerySelectType();
		basicFormStructureQuery.setDistinct(true);

		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		basicFormStructureQuery.setQueryPattern(body);
		// block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFS.Nodes.subClassOf,
		// RDFConstants.FORM_STRUCTURE_NODE));
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDF.type.asNode(), RDFConstants.FORM_STRUCTURE_NODE));
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_SHORT_NAME_NODE_N,
				RDFConstants.SHORT_NAME_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_VERSION_NODE_N,
				RDFConstants.VERSION_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_FS_DISEASE_NODE_N,
				RDFConstants.DISEASE_VARIABLE));

		basicFormStructureQuery.addResultVar(RDFConstants.URI_VARIABLE_NAME);
		basicFormStructureQuery.addResultVar(RDFConstants.SHORT_NAME_VARIABLE);
		basicFormStructureQuery.addResultVar(RDFConstants.DESCRIPTION_VARIABLE);
		basicFormStructureQuery.addResultVar(RDFConstants.VERSION_VARIABLE);
		basicFormStructureQuery.addResultVar(RDFConstants.TITLE_VARIABLE);
		basicFormStructureQuery.addResultVar(RDFConstants.DISEASE_VARIABLE,
				new ExprAggregator(RDFConstants.DISEASE_VARIABLE, AggregatorFactory.createGroupConcat(false,
						new E_Distinct(new ExprVar(RDFConstants.DISEASE_VARIABLE)), ",", null)));
		basicFormStructureQuery.addResultVar(RDFConstants.LABEL_VARIABLE,
				new ExprAggregator(RDFConstants.LABEL_VARIABLE, AggregatorFactory.createGroupConcat(false,
						new E_Distinct(new ExprVar(RDFConstants.LABEL_VARIABLE)), ",", null)));
		basicFormStructureQuery.addResultVar(RDFConstants.LABEL_ID_VARIABLE,
				new ExprAggregator(RDFConstants.LABEL_ID_VARIABLE, AggregatorFactory.createGroupConcat(false,
						new E_Distinct(new ExprVar(RDFConstants.LABEL_ID_VARIABLE)), ",", null)));
		
		basicFormStructureQuery.addResultVar(RDFConstants.ORGANIZATION_VARIABLE);
		basicFormStructureQuery.addResultVar(RDFConstants.MODIFIED_DATE_VARIABLE);
		basicFormStructureQuery.addResultVar(RDFConstants.STATUS_VARIABLE);
		basicFormStructureQuery.addResultVar(RDFConstants.SUBMISSION_TYPE_VARIABLE);
		basicFormStructureQuery.addResultVar(RDFConstants.STANDARDIZATION_VARIABLE);  // -----------------------------------------------------------------------------------------------------------------------------------------------
		basicFormStructureQuery.addResultVar(RDFConstants.DATE_CREATED_VARIABLE);
		basicFormStructureQuery.addResultVar(RDFConstants.CREATED_BY_VARIABLE);
		basicFormStructureQuery.addResultVar(RDFConstants.REQUIRED_VARIABLE,
				new ExprAggregator(RDFConstants.REQUIRED_VARIABLE, AggregatorFactory.createGroupConcat(false,
						new E_Distinct(new ExprVar(RDFConstants.REQUIRED_VARIABLE)), ",", null)));
		basicFormStructureQuery.addResultVar(RDFConstants.IS_COPYRIGHTED_VARIABLE);

		// adding variables to be selected
		List<Triple> standardizationList = new ArrayList<Triple>();
		standardizationList.add(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_FS_STANDARDIZATION_NODE_N,
				RDFConstants.STANDARDIZATION_NODE_VARIABLE));
		standardizationList.add(Triple.create(RDFConstants.STANDARDIZATION_NODE_VARIABLE, RDFS.label.asNode(),
				RDFConstants.STANDARDIZATION_VARIABLE));
		body.addElement(buildGroupOptionalPattern(standardizationList));

		List<Triple> requiredList = new ArrayList<Triple>();
		requiredList.add(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_FS_REQUIRED_NODE_N,
				RDFConstants.REQUIRED_NODE_VARIABLE));
		requiredList.add(Triple.create(RDFConstants.REQUIRED_NODE_VARIABLE, RDFS.label.asNode(),
				RDFConstants.REQUIRED_VARIABLE));
		body.addElement(buildGroupOptionalPattern(requiredList));

		List<Triple> labelList = new ArrayList<Triple>();
		labelList.add(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_FS_LABEL_NODE_N,
				RDFConstants.LABEL_NODE_VARIABLE));
		labelList.add(Triple.create(RDFConstants.LABEL_NODE_VARIABLE, RDFS.label.asNode(),
				RDFConstants.LABEL_VARIABLE));
		labelList.add(Triple.create(RDFConstants.LABEL_NODE_VARIABLE, RDFConstants.PROPERTY_FS_LABEL_ID_NODE_N,
				RDFConstants.LABEL_ID_VARIABLE));
		body.addElement(buildGroupOptionalPattern(labelList));
		
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_BRICS_DESCRIPTION_NODE_N, RDFConstants.DESCRIPTION_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_TITLE_NODE_N,
				RDFConstants.TITLE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_FS_ORGANIZATION_NODE_N,
				RDFConstants.ORGANIZATION_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_FS_MODIFIED_DATE_NODE_N,
				RDFConstants.MODIFIED_DATE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_STATUS_NODE_N,
				RDFConstants.STATUS_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_FS_SUBMISSION_TYPE_NODE_N, RDFConstants.SUBMISSION_TYPE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_FS_DATE_CREATED_NODE_N,
				RDFConstants.DATE_CREATED_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_FS_CREATED_BY_NODE_N,
				RDFConstants.CREATED_BY_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_FS_IS_COPYRIGHTED_NODE_N, RDFConstants.IS_COPYRIGHTED_VARIABLE));

		return basicFormStructureQuery;
	}

	public static final Expr filterListToOrs(LinkedList<Expr> filters) {

		if (filters.size() == 0) {
			return null;
		} else if (filters.size() == 1) {
			return filters.pop();
		} else {
			return new E_LogicalOr(filters.pop(), filterListToOrs(filters));
		}
	}

	public static final Query getDiseaseElementQuery() {

		Query diseaseElementQuery = QueryFactory.make();
		diseaseElementQuery.setQuerySelectType();
		diseaseElementQuery.setDistinct(true);
		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		diseaseElementQuery.setQueryPattern(body);

		block.addTriple(Triple.create(RDFConstants.DATA_ELEMENT_VARIABLE,
				RDFConstants.PROPERTY_ELEMENT_DISEASE_ELEMENT_NODE_N, RDFConstants.DISEASE_ELEMENT_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.DISEASE_ELEMENT_VARIABLE,
				RDFConstants.PROPERTY_DISEASE_ELEMENT_DISEASE_NODE, RDFConstants.DISEASE_VARIABLE));

		diseaseElementQuery.addResultVar(RDFConstants.DATA_ELEMENT_VARIABLE);
		diseaseElementQuery.addResultVar(RDFConstants.DISEASE_ELEMENT_VARIABLE);
		diseaseElementQuery.addResultVar(RDFConstants.DISEASE_VARIABLE);

		return diseaseElementQuery;
	}

	public static final Query getBasicDataElementQuery() {

		Query basicDataElementQuery = QueryFactory.make();
		basicDataElementQuery.setQuerySelectType();
		basicDataElementQuery.setDistinct(false);

		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		basicDataElementQuery.setQueryPattern(body);
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDF.type.asNode(), RDFConstants.DATA_ELEMENT_NODE));
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_SHORT_NAME_NODE_N,
				RDFConstants.SHORT_NAME_VARIABLE));

		// adding variables to be selected
		basicDataElementQuery.addResultVar(RDFConstants.URI_VARIABLE_NAME);
		basicDataElementQuery.addResultVar(RDFConstants.SHORT_NAME_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.DESCRIPTION_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.SHORT_DESCRIPTION_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.FORMAT_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.NOTES_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.HISTORICAL_NOTES_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.REFERENCES_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.POPULATION_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.GUIDELINES_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.TITLE_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.CATEGORY_TITLE_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.CATEGORY_SHORT_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.STATUS_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.VERSION_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.SUBMITTING_CONTACT_INFO_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.SUBMITTING_CONTACT_NAME_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.SUBMITTING_ORG_NAME_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.STEWARD_CONTACT_INFO_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.STEWARD_CONTACT_NAME_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.STEWARD_ORG_NAME_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.EFFECTIVE_DATE_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.UNTIL_DATE_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.SEE_ALSO_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.ELEMENT_TYPE_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.MODIFIED_DATE_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.DATE_CREATED_VARIABLE);
		basicDataElementQuery.addResultVar(RDFConstants.CREATED_BY_VARIABLE);

		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_TYPE_NODE_N,
				RDFConstants.ELEMENT_TYPE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_DE_DATE_CREATED_N,
				RDFConstants.DATE_CREATED_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_DE_CREATED_BY_N,
				RDFConstants.CREATED_BY_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_DE_MODIFIED_DATE_N,
				RDFConstants.MODIFIED_DATE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_BRICS_DESCRIPTION_NODE_N, RDFConstants.DESCRIPTION_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_SHORT_DESCRIPTION_NODE_N, RDFConstants.SHORT_DESCRIPTION_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_TITLE_NODE_N,
				RDFConstants.TITLE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_FORMAT,
				RDFConstants.FORMAT_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_DE_NOTES_NODE_N,
				RDFConstants.NOTES_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_DE_HISTORICAL_NOTES_NODE_N, RDFConstants.HISTORICAL_NOTES_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_REFERENCES_N,
				RDFConstants.REFERENCES_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_DE_POPULATION_NODE_N,
				RDFConstants.POPULATION_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_DE_HISTORICAL_NOTES_NODE_N, RDFConstants.HISTORICAL_NOTES_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_GUIDELINES_NODE_N, RDFConstants.GUIDELINES_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_STATUS_NODE_N,
				RDFConstants.STATUS_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_VERSION_NODE_N,
				RDFConstants.VERSION_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_SUBMITTING_CONTACT_INFO_NODE_N,
				RDFConstants.SUBMITTING_CONTACT_INFO_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_SUBMITTING_CONTACT_NAME_NODE_N,
				RDFConstants.SUBMITTING_CONTACT_NAME_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_SUBMITTING_ORG_NAME_NODE_N, RDFConstants.SUBMITTING_ORG_NAME_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_STEWARD_CONTACT_INFO_NODE_N, RDFConstants.STEWARD_CONTACT_INFO_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_STEWARD_CONTACT_NAME_NODE_N, RDFConstants.STEWARD_CONTACT_NAME_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_STEWARD_ORG_NAME_NODE_N, RDFConstants.STEWARD_ORG_NAME_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_EFFECTIVE_DATE_NODE_N, RDFConstants.EFFECTIVE_DATE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE,
				RDFConstants.PROPERTY_ELEMENT_UNTIL_DATE_NODE_N, RDFConstants.UNTIL_DATE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_SEE_ALSO_NODE_N,
				RDFConstants.SEE_ALSO_VARIABLE));
		/**
		 * The Category field now contains references to a single "main" element. This requires the optional field to
		 * contain several fields.
		 */
		Collection<Triple> categorySet = new HashSet<Triple>();
		categorySet.add(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_CATEGORY_NODE_N,
				RDFConstants.CATEGORY_URI_VARIABLE));
		categorySet.add(Triple.create(RDFConstants.CATEGORY_URI_VARIABLE, RDFConstants.PROPERTY_BRICS_TITLE_NODE_N,
				RDFConstants.CATEGORY_TITLE_VARIABLE));
		categorySet.add(Triple.create(RDFConstants.CATEGORY_URI_VARIABLE, RDFConstants.PROPERTY_BRICS_SHORT_NAME_NODE_N,
				RDFConstants.CATEGORY_SHORT_VARIABLE));

		body.addElement(buildGroupOptionalPattern(categorySet));

		return basicDataElementQuery;
	}

	/**
	 * Query statement for getting all data elements with until_date.
	 * 
	 * @return
	 */
	public static Query getWithUntilDateQuery() {
		Query query = getBasicDataElementQuery();
		Expr untilDateBound = new E_Bound(new ExprVar(RDFConstants.UNTIL_DATE_VARIABLE));
		((ElementGroup) query.getQueryPattern()).addElementFilter(new ElementFilter(untilDateBound));
		return query;
	}

	/**
	 * Query statement for getting all Retired or Deprecated data elements that do not have until_date.
	 * 
	 * @return
	 */
	public static Query getWithoutUntilDateQuery() {
		Query query = getBasicDataElementQuery();
		ElementGroup notExistsGroup = new ElementGroup();
		ElementTriplesBlock notExistsBlock = new ElementTriplesBlock();
		notExistsGroup.addElement(notExistsBlock);
		notExistsBlock.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_ELEMENT_UNTIL_DATE_NODE_N,
				RDFConstants.UNTIL_DATE_VARIABLE));
		Expr notExists = new E_NotExists(notExistsGroup);
		Expr statusExpression = new E_LogicalOr(
				new E_Equals(new E_Str(new ExprVar(RDFConstants.STATUS_VARIABLE)), new NodeValueString("Retired")),
				new E_Equals(new E_Str(new ExprVar(RDFConstants.STATUS_VARIABLE)), new NodeValueString("Deprecated")));
		((ElementGroup) query.getQueryPattern()).addElementFilter(new ElementFilter(notExists));
		((ElementGroup) query.getQueryPattern()).addElementFilter(new ElementFilter(statusExpression));
		return query;
	}

	public static Query getClassificationQuery() {

		Query classificationQuery = QueryFactory.make();
		classificationQuery.setQuerySelectType();
		classificationQuery.setDistinct(true);

		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		classificationQuery.setQueryPattern(body);

		block.addTriple(Triple.create(RDFConstants.CLASSIFICATION_VARIABLE, RDFS.subClassOf.asNode(),
				NodeFactory.createURI(RDFConstants.CLASSIFICATION)));
		block.addTriple(Triple.create(RDFConstants.CLASSIFICATION_VARIABLE,
				RDFConstants.PROPERTY_CLASSIFICATION_CAN_CREATE, RDFConstants.CAN_CREATE_VARIABLE));
		block.addTriple(
				Triple.create(RDFConstants.CLASSIFICATION_VARIABLE, RDFS.label.asNode(), RDFConstants.NAME_VARIABLE));

		classificationQuery.addResultVar(RDFConstants.CLASSIFICATION_VARIABLE);
		classificationQuery.addResultVar(RDFConstants.NAME_VARIABLE);
		classificationQuery.addResultVar(RDFConstants.CAN_CREATE_VARIABLE);

		return classificationQuery;
	}

	public static Query getClassificationElementQuery() {

		Query classificationElementQuery = QueryFactory.make();
		classificationElementQuery.setQuerySelectType();
		classificationElementQuery.setDistinct(true);

		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		classificationElementQuery.setQueryPattern(body);

		block.addTriple(Triple.create(RDFConstants.DATA_ELEMENT_VARIABLE,
				RDFConstants.PROPERTY_ELEMENT_CLASSIFICATION_ELEMENT_N, RDFConstants.CLASSIFICATION_URI_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.CLASSIFICATION_URI_VARIABLE,
				RDFConstants.PROPERTY_CLASSIFICATION_DISEASE_N, RDFConstants.DISEASE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.CLASSIFICATION_URI_VARIABLE,
				RDFConstants.PROPERTY_CLASSIFICATION_VALUE_N, RDFConstants.CLASSIFICATION_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.CLASSIFICATION_URI_VARIABLE,
				RDFConstants.PROPERTY_CLASSIFICATION_SUBGROUP_N, RDFConstants.SUBGROUP_VARIABLE));

		classificationElementQuery.addResultVar(RDFConstants.DATA_ELEMENT_VARIABLE);
		classificationElementQuery.addResultVar(RDFConstants.DISEASE_VARIABLE);
		classificationElementQuery.addResultVar(RDFConstants.CLASSIFICATION_VARIABLE);
		classificationElementQuery.addResultVar(RDFConstants.SUBGROUP_VARIABLE);

		return classificationElementQuery;
	}

	public static Query getSubdomainQuery() {

		Query subdomainElementQuery = QueryFactory.make();
		subdomainElementQuery.setQuerySelectType();
		subdomainElementQuery.setDistinct(true);

		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		subdomainElementQuery.setQueryPattern(body);

		block.addTriple(Triple.create(RDFConstants.DATA_ELEMENT_VARIABLE, RDFConstants.PROPERTY_DE_SUBDOMAIN_NODE_N,
				RDFConstants.SUB_DOMAIN_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.SUB_DOMAIN_VARIABLE, RDFConstants.PROPERTY_SUBDOMAIN_DISEASE_N,
				RDFConstants.DISEASE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.SUB_DOMAIN_VARIABLE,
				RDFConstants.PROPERTY_SUBDOMAIN_DOMAIN_N, RDFConstants.DOMAIN_VARIABLE));
		body.addElement(buildSingleOptionalPattern(RDFConstants.SUB_DOMAIN_VARIABLE,
				RDFConstants.PROPERTY_SUBDOMAIN_SUBDOMAIN_N, RDFConstants.SUBGROUP_VARIABLE));

		subdomainElementQuery.addResultVar(RDFConstants.DATA_ELEMENT_VARIABLE);
		subdomainElementQuery.addResultVar(RDFConstants.DISEASE_VARIABLE);
		subdomainElementQuery.addResultVar(RDFConstants.DOMAIN_VARIABLE);
		subdomainElementQuery.addResultVar(RDFConstants.SUBGROUP_VARIABLE);

		return subdomainElementQuery;
	}

	/*
	 * public static Query getSubgroupQuery() {
	 * 
	 * Query subgroupQuery = QueryFactory.make(); subgroupQuery.setQuerySelectType(); subgroupQuery.setDistinct(true);
	 * 
	 * ElementTriplesBlock block = new ElementTriplesBlock(); ElementGroup body = new ElementGroup();
	 * body.addElement(block); subgroupQuery.setQueryPattern(body);
	 * 
	 * block.addTriple(Triple.create(RDFConstants.CLASSIFICATION_ELEMENT_VARIABLE,
	 * RDFConstants.PROPERTY_CLASSIFICATION_ELEMENT_SUBGROUP, RDFConstants.SUBGROUP_VARIABLE));
	 * subgroupQuery.addResultVar(RDFConstants.CLASSIFICATION_ELEMENT_VARIABLE);
	 * subgroupQuery.addResultVar(RDFConstants.SUBGROUP_VARIABLE);
	 * 
	 * return subgroupQuery; }
	 */

	public static Query getPermissibleValueQuery() {

		Query permissibleValueQuery = QueryFactory.make();
		permissibleValueQuery.setQuerySelectType();
		permissibleValueQuery.setDistinct(true);

		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		permissibleValueQuery.setQueryPattern(body);

		block.addTriple(Triple.create(RDFConstants.DATA_ELEMENT_VARIABLE,
				RDFConstants.PROPERTY_ELEMENT_PERMISSIBLE_VALUE_N, RDFConstants.PERMISSIBLE_VALUE_NODE_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.PERMISSIBLE_VALUE_NODE_VARIABLE, RDFS.label.asNode(),
				RDFConstants.PERMISSIBLE_VALUE_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.PERMISSIBLE_VALUE_NODE_VARIABLE,
				NodeFactory.createURI(RDFConstants.PROPERTY_BRICS_DESCRIPTION_N),
				RDFConstants.PERMISSIBLE_VALUE_DESCRIPTION_VARIABLE));

		permissibleValueQuery.addResultVar(RDFConstants.DATA_ELEMENT_VARIABLE);
		permissibleValueQuery.addResultVar(RDFConstants.PERMISSIBLE_VALUE_VARIABLE);
		permissibleValueQuery.addResultVar(RDFConstants.PERMISSIBLE_VALUE_NODE_VARIABLE);
		permissibleValueQuery.addResultVar(RDFConstants.PERMISSIBLE_VALUE_DESCRIPTION_VARIABLE);

		return permissibleValueQuery;
	}

	public static Query getKeywordQuery() {

		Query keywordQuery = QueryFactory.make();
		keywordQuery.setQuerySelectType();
		keywordQuery.setDistinct(true);

		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		keywordQuery.setQueryPattern(body);

		Var pseudoCount = Var.alloc("pseudoCount");

		block.addTriple(Triple.create(RDFConstants.KEYWORD_VARIABLE, RDF.type.asNode(),
				NodeFactory.createURI(RDFConstants.KEYWORD)));
		block.addTriple(Triple.create(RDFConstants.KEYWORD_VARIABLE, RDFConstants.PROPERTY_BRICS_SHORT_NAME_NODE_N,
				RDFConstants.VALUE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(pseudoCount, RDFConstants.PROPERTY_ELEMENT_KEYWORDS_N,
				RDFConstants.KEYWORD_VARIABLE));

		keywordQuery.addResultVar(RDFConstants.KEYWORD_VARIABLE);
		keywordQuery.addResultVar(RDFConstants.VALUE_VARIABLE);

		keywordQuery.addGroupBy(RDFConstants.KEYWORD_VARIABLE);
		keywordQuery.addGroupBy(RDFConstants.VALUE_VARIABLE);

		Aggregator keyCountAgg = new AggCountVarDistinct(new ExprVar(pseudoCount));
		ExprAggregator keyCountExprAgg = new ExprAggregator(RDFConstants.COUNT_VARIABLE, keyCountAgg);

		keywordQuery.addResultVar(RDFConstants.COUNT_VARIABLE, keyCountExprAgg);

		keywordQuery.allocAggregate(keyCountAgg);

		return keywordQuery;
	}

	public static Query getLabelElementQuery() {

		Query labelQuery = QueryFactory.make();
		labelQuery.setQuerySelectType();
		labelQuery.setDistinct(true);

		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		labelQuery.setQueryPattern(body);

		Var pseudoCount = Var.alloc("pseudoCount");

		block.addTriple(Triple.create(RDFConstants.DATA_ELEMENT_VARIABLE, RDFConstants.PROPERTY_ELEMENT_LABELS_N,
				RDFConstants.LABEL_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.LABEL_VARIABLE, RDFConstants.PROPERTY_BRICS_SHORT_NAME_NODE_N,
				RDFConstants.VALUE_VARIABLE));

		body.addElement(buildSingleOptionalPattern(pseudoCount, RDFConstants.PROPERTY_ELEMENT_LABELS_N,
				RDFConstants.LABEL_VARIABLE));

		labelQuery.addResultVar(RDFConstants.DATA_ELEMENT_VARIABLE);
		labelQuery.addResultVar(RDFConstants.LABEL_VARIABLE);
		labelQuery.addResultVar(RDFConstants.VALUE_VARIABLE);

		labelQuery.addGroupBy(RDFConstants.DATA_ELEMENT_VARIABLE);
		labelQuery.addGroupBy(RDFConstants.LABEL_VARIABLE);
		labelQuery.addGroupBy(RDFConstants.VALUE_VARIABLE);

		Aggregator labelCountAgg = new AggCountVarDistinct(new ExprVar(pseudoCount));
		ExprAggregator labelCountExprAgg = new ExprAggregator(RDFConstants.COUNT_VARIABLE, labelCountAgg);

		labelQuery.addResultVar(RDFConstants.COUNT_VARIABLE, labelCountExprAgg);

		labelQuery.allocAggregate(labelCountAgg);

		return labelQuery;
	}

	public static Query getLabelQuery() {

		Query labelQuery = QueryFactory.make();
		labelQuery.setQuerySelectType();
		labelQuery.setDistinct(true);

		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		labelQuery.setQueryPattern(body);

		Var pseudoCount = Var.alloc("pseudoCount");

		block.addTriple(Triple.create(RDFConstants.LABEL_VARIABLE, RDF.type.asNode(),
				NodeFactory.createURI(RDFConstants.LABEL)));
		block.addTriple(Triple.create(RDFConstants.LABEL_VARIABLE, RDFConstants.PROPERTY_BRICS_SHORT_NAME_NODE_N,
				RDFConstants.VALUE_VARIABLE));
		body.addElement(buildSingleOptionalPattern(pseudoCount, RDFConstants.PROPERTY_ELEMENT_LABELS_N,
				RDFConstants.LABEL_VARIABLE));

		labelQuery.addResultVar(RDFConstants.LABEL_VARIABLE);
		labelQuery.addResultVar(RDFConstants.VALUE_VARIABLE);

		labelQuery.addGroupBy(RDFConstants.LABEL_VARIABLE);
		labelQuery.addGroupBy(RDFConstants.VALUE_VARIABLE);

		Aggregator labelCountAgg = new AggCountVarDistinct(new ExprVar(pseudoCount));
		ExprAggregator labelCountExprAgg = new ExprAggregator(RDFConstants.COUNT_VARIABLE, labelCountAgg);

		labelQuery.addResultVar(RDFConstants.COUNT_VARIABLE, labelCountExprAgg);

		labelQuery.allocAggregate(labelCountAgg);

		return labelQuery;
	}

	public static Query getKeywordElementQueryWithoutCount() {

		Query keywordQuery = QueryFactory.make();
		keywordQuery.setQuerySelectType();
		keywordQuery.setDistinct(true);

		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		keywordQuery.setQueryPattern(body);

		block.addTriple(Triple.create(RDFConstants.DATA_ELEMENT_VARIABLE, RDFConstants.PROPERTY_ELEMENT_KEYWORDS_N,
				RDFConstants.KEYWORD_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.KEYWORD_VARIABLE, RDFConstants.PROPERTY_BRICS_SHORT_NAME_NODE_N,
				RDFConstants.VALUE_VARIABLE));

		keywordQuery.addResultVar(RDFConstants.DATA_ELEMENT_VARIABLE);
		keywordQuery.addResultVar(RDFConstants.KEYWORD_VARIABLE);
		keywordQuery.addResultVar(RDFConstants.VALUE_VARIABLE);

		return keywordQuery;
	}

	public static Query getKeywordElementQuery() {

		Query keywordQuery = getKeywordElementQueryWithoutCount();
		ElementGroup body = (ElementGroup) keywordQuery.getQueryPattern();

		Var pseudoCount = Var.alloc("pseudoCount");

		body.addElement(buildSingleOptionalPattern(pseudoCount, RDFConstants.PROPERTY_ELEMENT_KEYWORDS_N,
				RDFConstants.KEYWORD_VARIABLE));

		Aggregator keyCountAgg = new AggCountVarDistinct(new ExprVar(pseudoCount));
		ExprAggregator keyCountExprAgg = new ExprAggregator(RDFConstants.COUNT_VARIABLE, keyCountAgg);

		keywordQuery.addResultVar(RDFConstants.COUNT_VARIABLE, keyCountExprAgg);
		keywordQuery.addGroupBy(RDFConstants.DATA_ELEMENT_VARIABLE);
		keywordQuery.addGroupBy(RDFConstants.KEYWORD_VARIABLE);
		keywordQuery.addGroupBy(RDFConstants.VALUE_VARIABLE);
		keywordQuery.allocAggregate(keyCountAgg);

		return keywordQuery;
	}

	public static Query getExternalIdQuery() {

		Query externalIdQuery = QueryFactory.make();
		externalIdQuery.setQuerySelectType();
		externalIdQuery.setDistinct(true);

		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		externalIdQuery.setQueryPattern(body);

		block.addTriple(Triple.create(RDFConstants.DATA_ELEMENT_VARIABLE, RDFConstants.PROPERTY_ELEMENT_EXTERNAL_ID,
				RDFConstants.EXTERNAL_ID_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.EXTERNAL_ID_VARIABLE, RDFConstants.PROPERTY_EXTERNAL_ID_VALUE,
				RDFConstants.VALUE_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.EXTERNAL_ID_VARIABLE, RDFConstants.PROPERTY_EXTERNAL_ID_TYPE,
				RDFConstants.TYPE_VARIABLE));

		externalIdQuery.addResultVar(RDFConstants.DATA_ELEMENT_VARIABLE);
		externalIdQuery.addResultVar(RDFConstants.EXTERNAL_ID_VARIABLE);
		externalIdQuery.addResultVar(RDFConstants.VALUE_VARIABLE);
		externalIdQuery.addResultVar(RDFConstants.TYPE_VARIABLE);

		return externalIdQuery;
	}

	public static Query getKeywordDataElementQuery() {

		Query keywordDataElementQuery = QueryFactory.make();
		keywordDataElementQuery.setQuerySelectType();
		keywordDataElementQuery.setDistinct(true);

		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		keywordDataElementQuery.setQueryPattern(body);

		block.addTriple(Triple.create(RDFConstants.DATA_ELEMENT_VARIABLE, RDFConstants.PROPERTY_ELEMENT_KEYWORDS,
				RDFConstants.KEYWORD_VARIABLE));

		keywordDataElementQuery.addResultVar(RDFConstants.DATA_ELEMENT_VARIABLE);
		keywordDataElementQuery.addResultVar(RDFConstants.KEYWORD_VARIABLE);

		return keywordDataElementQuery;
	}
	
	public static Query getDataElementShortNameQuery() {
		Query dataElementShortNameQuery = QueryFactory.make();
		dataElementShortNameQuery.setQuerySelectType();
		dataElementShortNameQuery.setDistinct(true);
		dataElementShortNameQuery.addResultVar(RDFConstants.SHORT_NAME_VARIABLE);
		
		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		dataElementShortNameQuery.setQueryPattern(body);
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDF.type.asNode(), RDFConstants.DATA_ELEMENT_NODE));
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_SHORT_NAME_NODE_N,
				RDFConstants.SHORT_NAME_VARIABLE));
			
		return dataElementShortNameQuery;
	}



	/**
	 * Builds name and versions filter for the query
	 * 
	 * @param nameVar
	 * @param versionVar
	 * @param nameAndVersions
	 * @return
	 */
	public static ElementFilter getNameVersionFilter(Var nameVar, Var versionVar,
			List<NameAndVersion> nameAndVersions) {

		ElementFilter filter = new ElementFilter(
				buildNameVersionOrs(nameVar, versionVar, new LinkedList<NameAndVersion>(nameAndVersions)));
		return filter;
	}

	/**
	 * Builds a series of and and or expressions for the short name and version filters Should come out with queries
	 * like (name = "bla" && version = 1) || (name = "bla2" && version = 1) || ...etc...
	 * 
	 * @param nameVar
	 * @param versionVar
	 * @param nameAndVersions
	 * @return
	 */
	private static Expr buildNameVersionOrs(Var nameVar, Var versionVar, LinkedList<NameAndVersion> nameAndVersions) {

		// quit and return null if the nameAndVersion
		if (nameAndVersions == null || nameAndVersions.isEmpty()) {
			return null;
		} else if (nameAndVersions.size() == 1) // base case, return the last single expression
		{
			NameAndVersion currentNameVersion = nameAndVersions.pop();
			return buildNameVersionExpression(nameVar, versionVar, currentNameVersion);
		} else
		// still have more than one expression to Or, so we recursively call this method wrapped by a logical Or
		{
			NameAndVersion currentNameVersion = nameAndVersions.pop(); // get the next combination of name and
																		 // version
			return new E_LogicalOr(buildNameVersionExpression(nameVar, versionVar, currentNameVersion),
					buildNameVersionOrs(nameVar, versionVar, nameAndVersions));
		}
	}

	/**
	 * Returns name and version equals expressions wrapped by a logical And. Something like... name = "blah" && version
	 * = 1
	 * 
	 * @param nameVar
	 * @param versionVar
	 * @param nameAndVersion
	 * @return
	 */
	private static E_LogicalAnd buildNameVersionExpression(Var nameVar, Var versionVar, NameAndVersion nameAndVersion) {

		Expr nameExpr = new E_Equals(new ExprVar(nameVar), new NodeValueString(nameAndVersion.getName()));
		Expr versionExpr = new E_Equals(new ExprVar(versionVar), new NodeValueString(nameAndVersion.getVersion()));
		return new E_LogicalAnd(nameExpr, versionExpr);
	}

	/************** URI CREATORS ****************/

	public static Node createClassificationUri(Classification classification) {

		return createClassificationUri(classification.getName());
	}

	public static Node createClassificationUri(String name) {

		Node uri = null;

		try {
			uri = NodeFactory.createURI(URIUtil.encodeQuery(RDFConstants.CLASSIFICATION_NS + name));
		} catch (URIException e) {
			e.printStackTrace();
		}
		return uri;
	}

	public static Node createDataElementBaseUri(SemanticDataElement de) {

		Node uri = null;

		try {
			uri = NodeFactory.createURI(URIUtil.encodeQuery(RDFConstants.ELEMENT_NS + de.getName()));
		} catch (URIException e) {
			e.printStackTrace();
		}
		return uri;

	}

	public static Node createDataElementUri(SemanticDataElement de) {

		return createDataElementUri(de.getName(), de.getVersion());
	}

	public static Node createDataElementUri(String name, String version) {

		Node uri = null;

		uri = NodeFactory.createURI(createDataElementUriString(name, version));

		return uri;
	}

	public static String createDataElementUriString(String name, String version) {

		String uri = null;

		try {
			uri = URIUtil.encodeQuery(RDFConstants.ELEMENT + "/" + name + "/" + version);
		} catch (URIException e) {
			e.printStackTrace();
		}

		return uri;
	}

	public static Node createDomainPairURI(SemanticDataElement dataElement, DiseaseElement diseaseElement,
			DomainPair domainPair) {

		return createDomainPairURI(dataElement.getName(), diseaseElement.getDisease().getName(),
				domainPair.getDomain().getName(), domainPair.getSubdomain().getName());
	}

	public static Node createDomainPairURI(String dataElement, String disease, String domain, String subdomain) {

		Node uri = null;

		try {
			uri = NodeFactory.createURI(URIUtil.encodeQuery(
					RDFConstants.DOMAIN_PAIR_NS + dataElement + "_" + disease + "_" + domain + "_" + subdomain));
		} catch (URIException e) {
			e.printStackTrace();
		}
		return uri;
	}

	public static Node createClassificationElementURI(ClassificationElement classificationElement) {

		Disease disease = classificationElement.getDisease();
		Subgroup subgroup = classificationElement.getSubgroup();
		Classification classification = classificationElement.getClassification();

		String diseaseString = "none";
		String subgroupString = "none";
		String classificationString = "none";

		if (disease != null) {
			diseaseString = uri_encode(disease.getName());
		}

		if (subgroup != null) {
			subgroupString = uri_encode(subgroup.getSubgroupName());
		}

		if (classification != null) {
			classificationString = uri_encode(classification.getName());
		}

		Node node = null;

		try {
			node = NodeFactory.createURI(URIUtil.encodeQuery(RDFConstants.CLASSIFICATION_NS + diseaseString + "/"
					+ subgroupString + "/" + classificationString));
		} catch (URIException e) {
			e.printStackTrace();
		}

		return node;
	}

	private static String uri_encode(String string) {

		string = string.toLowerCase();
		string = string.replace(" ", "_");
		string = string.replace("/", "_");
		string = string.replace(":", "");
		string = string.replace("'", "");
		string = string.replace("(", "");
		string = string.replace(")", "");

		return string;
	}

	/**
	 * @deprecated use {@link createClassificationElementURI(ClassificationElement classificationElement)} instead
	 * @param dataElement
	 * @param classificationElement
	 * @return
	 */
	public static Node createClassificationElementURI(SemanticDataElement dataElement,
			ClassificationElement classificationElement) {

		return createClassificationElementURI(dataElement.getName(),
				classificationElement.getClassification().getName());
	}

	/**
	 * @deprecated original method. use new model instead {@link createClassificationElementURI(ClassificationElement
	 *             classificationElement)}
	 * @param dataElementName
	 * @param classification
	 * @return
	 */
	public static Node createClassificationElementURI(String dataElementName, String classification) {

		Node uri = null;

		try {
			return NodeFactory.createURI(URIUtil
					.encodeQuery(RDFConstants.CLASSIFICATION_ELEMENT_NS + dataElementName + "_" + classification));
		} catch (URIException e) {
			e.printStackTrace();
		}

		return uri;
	}

	public static Node createDiseaseElementURI(SemanticDataElement dataElement, DiseaseElement diseaseElement) {

		String diseaseName = diseaseElement.getDisease().getName();
		String dataElementName = dataElement.getName();
		return createDiseaseElementURI(dataElementName, diseaseName);
	}

	public static Node createDiseaseElementURI(String dataElementName, String diseaseName) {

		Node uri = null;
		try {
			return NodeFactory.createURI(
					URIUtil.encodeQuery(RDFConstants.DISEASE_ELEMENT_NS + dataElementName + "_" + diseaseName));
		} catch (URIException e) {
			e.printStackTrace();
		}

		return uri;
	}

	public static Node createClassificationURI(Classification classification) {

		Node uri = null;

		try {
			return NodeFactory
					.createURI(URIUtil.encodeQuery(RDFConstants.CLASSIFICATION_NS + classification.getName()));
		} catch (URIException e) {
			e.printStackTrace();
		}

		return uri;
	}

	public static Node createKeywordUri(Keyword kw) {

		Node uri = null;

		try {
			return NodeFactory.createURI(URIUtil.encodeQuery(RDFConstants.KEYWORD_NS + uri_encode(kw.getKeyword())));
		} catch (URIException e) {
			e.printStackTrace();
		}

		return uri;
	}

	public static Node createLabelUri(Keyword label) {

		Node uri = null;

		try {
			return NodeFactory.createURI(URIUtil.encodeQuery(RDFConstants.LABEL_NS + uri_encode(label.getKeyword())));
		} catch (URIException e) {
			e.printStackTrace();
		}

		return uri;
	}

	public static Node createExternalIdURI(ExternalId externalId) {

		return createExternalIdURI(externalId.getSchema().getName(), externalId.getValue());
	}

	private static Node createExternalIdURI(String type, String value) {

		Node uri = null;

		try {
			return NodeFactory.createURI(URIUtil.encodeQuery(RDFConstants.EXTERNAL_ID_NS + type + "_" + value));
		} catch (URIException e) {
			e.printStackTrace();
		}

		return uri;
	}

	public static Node createPermissibleValueUri(SemanticDataElement de, ValueRange pv) {

		return createPermissibleValueUri(de.getName(), pv.getValueRange());
	}

	public static Node createFormStructureUriNode(String shortName, String version) {

		return NodeFactory.createURI(createFormStructureUri(shortName, version));
	}

	public static String createFormStructureUri(String shortName, String version) {

		if (shortName == null || version == null) {
			throw new UnsupportedOperationException(
					"Cannot call createFormStructureUri without a short name or a version");
		}

		try {
			return URIUtil.encodeQuery(RDFConstants.FORM_STRUCTURE + "/" + shortName + "/" + version);
		} catch (URIException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static Node createPermissibleValueUri(String name, String valueRange) {

		Node uri = null;

		try {
			return NodeFactory.createURI(URIUtil
					.encodeQuery(RDFConstants.PERMISSIBLE_VALUE_NS + uri_encode(name) + "/" + uri_encode(valueRange)));
		} catch (URIException e) {
			e.printStackTrace();
		}

		return uri;
	}
}
