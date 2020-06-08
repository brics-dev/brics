package gov.nih.tbi.util;

import gov.nih.tbi.commons.util.SparqlConstructionUtil;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.pojo.BeanField;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.RepeatingCellColumn;
import gov.nih.tbi.semantic.model.GuidRDF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.aggregate.AggregatorFactory;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class InstancedDataQueryGenerator {

	/**
	 * Returns a list of all the Study IDs for the given form
	 * 
	 * @param form
	 * @return
	 */
	private static List<Long> aggregateStudyIds(FormResult form) {
		List<Long> studyIds = new ArrayList<Long>();

		List<StudyResult> studies = Collections.synchronizedList(form.getStudies());

		synchronized (studies) {
			Iterator<StudyResult> studiesIt = studies.iterator();
			while (studiesIt.hasNext()) {
				StudyResult study = studiesIt.next();
				studyIds.add(study.getId());
			}
		}

		return studyIds;
	}

	public static Query getInstancedDataQuery(FormResult form, Node accountNode, boolean forDownload) {
		Query query = QueryFactory.make();
		query.setQuerySelectType();
		ElementGroup body = new ElementGroup();
		ElementTriplesBlock block = new ElementTriplesBlock();
		body.addElement(block);
		query.setQueryPattern(body);
		query.addResultVar(QueryToolConstants.ROW_VAR);
		query.addResultVar(QueryToolConstants.DATASET_NAME_VAR);
		query.addResultVar(QueryToolConstants.SUBMISSION_VAR);
		query.addResultVar(QueryToolConstants.STUDY_ID_VAR);
		query.addResultVar(QueryToolConstants.STUDY_TITLE_VAR);
		query.addResultVar(QueryToolConstants.PREFIXED_ID_VAR);
		query.addResultVar(QueryToolConstants.DATASET_ID_VAR);
		query.addResultVar(QueryToolConstants.STUDY_PREFIXED_ID_VAR);
		query.addResultVar(QueryToolConstants.DO_HIGHLIGHT_VAR);
		query.addResultVar(QueryToolConstants.GUID_LABEL_VAR);
		query.addResultVar(QueryToolConstants.MDS_UPDRS_X_VAR);

		body.addElement(new ElementSubQuery(getRowSubquery(form, QueryToolConstants.ROW_VAR, accountNode)));

		Map<RepeatableGroup, String> groupVarMap = InstancedDataUtil.getGroupVariableMap(form);

		for (RepeatableGroup rg : form.getRepeatableGroups()) {
			List<DataElement> dataElementList = null;

			if (forDownload) {
				dataElementList = rg.getSelectedElements();
			} else {
				dataElementList = rg.getDataElements();
			}

			if (!rg.doesRepeat() && !dataElementList.isEmpty()) {
				body.addElement(new ElementOptional(createRepeatableGroupSubQuery(groupVarMap, form, rg)));

				for (DataElement de : dataElementList) {
					query.addResultVar(InstancedDataUtil.getDataElementVar(groupVarMap, form, rg, de));
				}
			}
		}

		return query;
	}

	private static Query getRowSubquery(FormResult form, Var rowVar, Node accountNode) {
		Query query = QueryFactory.make();
		ElementGroup body = new ElementGroup();
		ElementTriplesBlock block = new ElementTriplesBlock();
		query.setQuerySelectType();
		body.addElement(block);
		query.setQueryPattern(body);
		query.setQueryResultStar(true);
		block.addTriple(
				Triple.create(QueryToolConstants.ROW_VAR, RDF.type.asNode(), NodeFactory.createURI(form.getUri())));
		block = InstancedDataUtil.addPermissionTriples(block, accountNode, QueryToolConstants.ROW_VAR,
				QueryToolConstants.DATASET_VAR, QueryToolConstants.STUDY_ID_VAR);
		body.addElementFilter(new ElementFilter(
				InstancedDataUtil.isOneOfLong(QueryToolConstants.STUDY_ID_VAR, aggregateStudyIds(form))));
		block.addTriple(Triple.create(QueryToolConstants.ROW_VAR,
				QueryToolConstants.INSTANCED_ROW_PROPERTY_DATASET_NODE, QueryToolConstants.DATASET_ID_VAR));
		block.addTriple(Triple.create(QueryToolConstants.ROW_VAR, QueryToolConstants.ROW_DATASET_NAME,
				QueryToolConstants.DATASET_NAME_VAR));
		block.addTriple(Triple.create(QueryToolConstants.ROW_VAR, QueryToolConstants.ROW_SUBMISSION,
				QueryToolConstants.SUBMISSION_VAR));
		block.addTriple(Triple.create(QueryToolConstants.ROW_VAR, QueryToolConstants.ROW_STUDY,
				QueryToolConstants.STUDY_TITLE_VAR));
		block.addTriple(Triple.create(QueryToolConstants.ROW_VAR, QueryToolConstants.ROW_PREFIX,
				QueryToolConstants.PREFIXED_ID_VAR));
		block.addTriple(Triple.create(QueryToolConstants.ROW_VAR, QueryToolConstants.STUDY_PROPERTY_PREFIXED_ID,
				QueryToolConstants.STUDY_PREFIXED_ID_VAR));
		ElementTriplesBlock guidOptionalBlock = new ElementTriplesBlock();
		guidOptionalBlock.addTriple(
				Triple.create(QueryToolConstants.ROW_VAR, QueryToolConstants.ROW_GUID, QueryToolConstants.GUID_VAR));
		guidOptionalBlock.addTriple(
				Triple.create(QueryToolConstants.GUID_VAR, RDFS.label.asNode(), QueryToolConstants.GUID_LABEL_VAR));

		ElementOptional guidOptional = new ElementOptional(guidOptionalBlock);
		body.addElement(guidOptional);

		ElementTriplesBlock highlightOptionalBlock = new ElementTriplesBlock();
		highlightOptionalBlock.addTriple(Triple.create(QueryToolConstants.GUID_VAR, GuidRDF.DO_HIGHLIGHT_PROP.asNode(),
				QueryToolConstants.DO_HIGHLIGHT_VAR));
		ElementOptional highlightOptional = new ElementOptional(highlightOptionalBlock);
		body.addElement(highlightOptional);

		ElementTriplesBlock mdsUpdrsOptionalBlock = new ElementTriplesBlock();
		mdsUpdrsOptionalBlock.addTriple(Triple.create(QueryToolConstants.GUID_VAR, GuidRDF.MDS_UPDRS_X_PROP.asNode(),
				QueryToolConstants.MDS_UPDRS_X_VAR));
		ElementOptional mdsUpdrsOptional = new ElementOptional(mdsUpdrsOptionalBlock);
		body.addElement(mdsUpdrsOptional);


		return query;
	}

	private static ElementSubQuery createRepeatableGroupSubQuery(Map<RepeatableGroup, String> groupVariableMap,
			FormResult form, RepeatableGroup rg) {
		Query query = QueryFactory.make();
		query.setQuerySelectType();
		ElementGroup body = new ElementGroup();
		ElementTriplesBlock block =
				InstancedDataUtil.createRepeatableGroupPattern(QueryToolConstants.ROW_VAR, form, rg);
		body.addElement(block);
		query.setQueryPattern(body);
		query.addResultVar(QueryToolConstants.ROW_VAR);

		for (DataElement de : rg.getSelectedElements()) {
			ElementTriplesBlock deBlock = InstancedDataUtil.createDataElementPattern(form, rg, de);
			body.addElement(new ElementOptional(deBlock));
			query.addResultVar(InstancedDataUtil.getDataElementVar(groupVariableMap, form, rg, de));
		}

		return new ElementSubQuery(query);
	}

	/***********************************************************************************************************************/
	/*****************************
	 * Join Queries
	 ****************************************************************************/
	/***********************************************************************************************************************/
	/**
	 * Returns a query that will select the row uri, repeatable group, and the count of rows for that repeatable group +
	 * row This is so we can get a count of all the repeating group
	 * 
	 * @param form
	 * @param rowUris
	 * @return
	 */
	public static Query generateRepeatableGroupRowCountsQuery(FormResult form, Set<String> rowUris) {
		Query query = QueryFactory.make();
		query.setQuerySelectType();
		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();

		body.addElement(block);
		query.setQueryPattern(body);

		block.addTriple(Triple.create(QueryToolConstants.ROW_VARIABLE, RDF.type.asNode(),
				NodeFactory.createURI(form.getUri())));
		block.addTriple(
				Triple.create(QueryToolConstants.ROW_VARIABLE, QueryToolConstants.HAS_REPEATABLE_GROUP_INSTANCE_N,
						QueryToolConstants.INSTANCED_REPEATABLE_GROUP_VARIABLE));
		block.addTriple(Triple.create(QueryToolConstants.INSTANCED_REPEATABLE_GROUP_VARIABLE, RDF.type.asNode(),
				QueryToolConstants.REPEATABLE_GROUP_VARIABLE));

		block.addTriple(Triple.create(QueryToolConstants.DATA_ELEMENT_VARIABLE, RDFS.subClassOf.asNode(),
				QueryToolConstants.DATA_ELEMENT_CLASS));
		block.addTriple(Triple.create(QueryToolConstants.INSTANCED_REPEATABLE_GROUP_VARIABLE,
				QueryToolConstants.DATA_ELEMENT_VARIABLE, QueryToolConstants.VALUE_VARIABLE));

		body.addElementFilter(
				new ElementFilter(InstancedDataUtil.isOneOfUri(new ExprVar(QueryToolConstants.ROW_VARIABLE), rowUris)));

		query.addResultVar(QueryToolConstants.ROW_VARIABLE);
		query.addResultVar(QueryToolConstants.REPEATABLE_GROUP_VARIABLE);
		query.addResultVar(QueryToolConstants.COUNT_VARIABLE,
				new ExprAggregator(QueryToolConstants.COUNT_VARIABLE, AggregatorFactory.createCountExpr(true,
						new ExprVar(QueryToolConstants.INSTANCED_REPEATABLE_GROUP_VARIABLE))));
		query.addGroupBy(QueryToolConstants.ROW_VARIABLE);
		query.addGroupBy(QueryToolConstants.REPEATABLE_GROUP_VARIABLE);

		return query;
	}

	/**
	 * Builds the query to get the data for a single repeatable group in the form for only the given row URIs
	 * 
	 * @param form
	 * @param column
	 * @param uris
	 * @return
	 */
	public static List<Query> buildRepeatableGroupDataQueries(FormResult form, DataTableColumn column,
			List<String> uris, Node accountNode) {

		List<Query> queries = new ArrayList<Query>();
		int dataElementCount = 0;
		RepeatableGroup group = InstancedDataUtil.getRepeatableGroupUsingColumn(form, column);

		if (group != null) {
			dataElementCount = group.getSelectedElements().size();
		} else {
			// if no particular group is specified, set the de count as the sum of all the repeating groups' de size
			for (RepeatableGroup currentGroup : form.getRepeatableGroups()) {
				if (currentGroup.doesRepeat()) {
					dataElementCount += currentGroup.getSelectedElements().size();
				}
			}
		}

		int lowerIndex = 0;
		int upperIndex = lowerIndex + QueryToolConstants.INSTANCED_DATA_COLUMN_LIMIT - 1;
		if (upperIndex > dataElementCount - 1) {
			upperIndex = dataElementCount - 1;
		}
		while (lowerIndex <= dataElementCount - 1 && upperIndex >= 0) {
			queries.add(buildRepeatableGroupDataQuery(form, uris, group, lowerIndex, upperIndex, accountNode));

			if (upperIndex + QueryToolConstants.INSTANCED_DATA_COLUMN_LIMIT < dataElementCount) {
				lowerIndex = upperIndex + 1;
				upperIndex += QueryToolConstants.INSTANCED_DATA_COLUMN_LIMIT;
			} else {
				lowerIndex = upperIndex + 1;
				upperIndex += dataElementCount % QueryToolConstants.INSTANCED_DATA_COLUMN_LIMIT;
			}
		}

		return queries;
	}

	private static Query buildRepeatableGroupDataQuery(FormResult form, List<String> uris, RepeatableGroup group,
			int lowerIndex, int upperIndex, Node accountNode) {

		Query query = QueryFactory.make();
		query.setDistinct(true);
		query.setQuerySelectType();
		ElementTriplesBlock block = new ElementTriplesBlock();
		ElementGroup body = new ElementGroup();
		query.setQueryPattern(body);
		body.addElement(block);

		block.addTriple(Triple.create(QueryToolConstants.ROW_VARIABLE,
				QueryToolConstants.HAS_REPEATABLE_GROUP_INSTANCE_N, QueryToolConstants.REPEATABLE_GROUP_VARIABLE));
		block = InstancedDataUtil.addPermissionTriples(block, accountNode, QueryToolConstants.ROW_VARIABLE,
				QueryToolConstants.DATASET_VAR, QueryToolConstants.STUDY_ID_VAR);
		block.addTriple(Triple.create(QueryToolConstants.REPEATABLE_GROUP_VARIABLE, RDF.type.asNode(),
				NodeFactory.createURI(group.getUri())));

		// add the variables we want to select
		query.addResultVar(QueryToolConstants.ROW_VARIABLE);
		query.addResultVar(QueryToolConstants.REPEATABLE_GROUP_VARIABLE);

		// need to remove the question mark from the variable name
		String rgVar = InstancedDataUtil.getGroupVariable(form, group).substring(1);

		// for all the data elements that are in range of the indexes, add the patterns and the data element into the
		// selected variable list looks like...
		// optional { ?instancedRepeatableGroup <de_uri> ?deVariable . } then, adds the ?deVariable into the select
		for (int deIndex = lowerIndex; deIndex <= upperIndex; deIndex++) {
			DataElement dataElement = group.getSelectedElements().get(deIndex);
			String deVariable = rgVar + dataElement.getName();
			body.addElement(InstancedDataUtil.buildSingleOptionalPattern(QueryToolConstants.REPEATABLE_GROUP_VARIABLE,
					NodeFactory.createURI(dataElement.getUri()), Var.alloc(deVariable)));
			query.addResultVar(deVariable);
		}

		body.addElementFilter(new ElementFilter(
				InstancedDataUtil.isOneOfLong(QueryToolConstants.STUDY_ID_VAR, aggregateStudyIds(form))));

		// need to make sure we only select the rows we need.
		body.addElement(InstancedDataUtil.buildURIValuesSubQuery(QueryToolConstants.ROW_VARIABLE, uris));

		return query;
	}

	/**
	 * Returns the query used to get the instanced data of the selected query
	 * 
	 * @return
	 */
	public static List<String> getInstancedRepeatableGroupQuery(FormResult form, String submissionId,
			RepeatableGroup group, Node accountNode) {

		List<String> queries = new ArrayList<String>();
		int dataElementCount = 0;

		if (group != null) {
			dataElementCount = group.getSelectedElements().size();
		} else {
			// if no particular group is specified, set the de count as the sum of all the repeating groups' de size
			for (RepeatableGroup currentGroup : form.getRepeatableGroups()) {
				if (currentGroup.doesRepeat()) {
					dataElementCount += currentGroup.getSelectedElements().size();
				}
			}
		}

		int lowerIndex = 0;
		int upperIndex = lowerIndex + QueryToolConstants.INSTANCED_DATA_COLUMN_LIMIT - 1;
		if (upperIndex > dataElementCount - 1) {
			upperIndex = dataElementCount - 1;
		}
		while (lowerIndex <= dataElementCount - 1 && upperIndex >= 0) {
			queries.add(getInstancedRepeatableGroupQueryAux(form, submissionId, group, lowerIndex, upperIndex,
					accountNode));

			if (upperIndex + QueryToolConstants.INSTANCED_DATA_COLUMN_LIMIT < dataElementCount) {
				lowerIndex = upperIndex + 1;
				upperIndex += QueryToolConstants.INSTANCED_DATA_COLUMN_LIMIT;
			} else {
				lowerIndex = upperIndex + 1;
				upperIndex += dataElementCount % QueryToolConstants.INSTANCED_DATA_COLUMN_LIMIT;
			}
		}

		return queries;
	}

	/**
	 * Returns the query used to get the instanced data of the selected query
	 * 
	 * @return
	 */
	public static String getInstancedRepeatableGroupQueryAux(FormResult form, String submissionId,
			RepeatableGroup group, int lowerIndex, int upperIndex, Node accountNode) {

		Map<RepeatableGroup, String> groupVariableMap = InstancedDataUtil.getGroupVariableMap(form);
		Set<String> variableList = new HashSet<String>();

		variableList.add("?dsId");
		variableList.add("?submission");

		// gather the variables to be added to the select list
		for (int i = lowerIndex; i <= upperIndex; i++) {
			DataElement dataElement = group.getSelectedElements().get(i);
			String deVariable =
					"?" + InstancedDataUtil.getDataElementVar(groupVariableMap, form, group, dataElement).getName();
			variableList.add(deVariable);
		}

		StringBuffer queryBuffer = new StringBuffer("SELECT ?row");

		for (String variable : variableList) {
			queryBuffer.append(QueryToolConstants.WS).append(variable);
		}

		// combine the select part with the where part of the query
		queryBuffer.append(QueryToolConstants.NL).append(
				getInstancedRepeatableGroupQueryWhere(form, submissionId, group, lowerIndex, upperIndex, accountNode))
				.append("ORDER BY asc(?row) ASC(?submission)");

		return queryBuffer.toString();
	}

	/**
	 * Returns the "Where" part of the query we're going to use to get instanced data
	 * 
	 * @return
	 */
	private static String getInstancedRepeatableGroupQueryWhere(FormResult form, String submissionId,
			RepeatableGroup group, int lowerIndex, int upperIndex, Node accountNode) {

		StringBuffer query = new StringBuffer();
		Map<RepeatableGroup, String> groupVariableMap = InstancedDataUtil.getGroupVariableMap(form);

		query.append("WHERE {").append(QueryToolConstants.NL).append("?row a <").append(form.getUri()).append("> .")
				.append(QueryToolConstants.NL).append("?row dataset:datasetId ?dsId .").append(QueryToolConstants.NL);
		query = InstancedDataUtil.addPermissionTriples(query, accountNode.getURI(), "?row",
				QueryToolConstants.DATASET_VAR.getName(), QueryToolConstants.STUDY_ID_VAR.getName());
		query.append("?row dataset:submissionRecordJoinId ?submission .").append(QueryToolConstants.NL);

		if (submissionId != null) {
			query.append("?row dataset:submissionRecordJoinId " + submissionId + " .").append(QueryToolConstants.NL);
		}

		// TODO: make this a method
		if (group != null) {
			query.append("OPTIONAL {");

			// get the unique variable name
			String groupVariable = groupVariableMap.get(group);
			query.append("?row fs:hasRepeatableGroupInstance ").append(groupVariable).append(" .")
					.append(QueryToolConstants.NL).append(groupVariable).append(QueryToolConstants.WS).append("a <")
					.append(group.getUri()).append("> .").append(QueryToolConstants.NL);

			for (int deIndex = lowerIndex; deIndex <= upperIndex; deIndex++) {
				DataElement dataElement = group.getSelectedElements().get(deIndex);
				String deVariable =
						InstancedDataUtil.getDataElementVar(groupVariableMap, form, group, dataElement).getName();

				StringBuilder filterString = new StringBuilder();

				filterString.append(groupVariable).append(QueryToolConstants.WS).append("<")
						.append(dataElement.getUri()).append(">").append(QueryToolConstants.WS).append("?")
						.append(deVariable).append(" .");

				// if a filter exists on this data element, we wont want to have the optional clause around the pattern
				if (!form.hasFilterOnDataElement(group, dataElement)) {
					filterString.insert(0, QueryToolConstants.OPTIONAL_START);
					filterString.append(QueryToolConstants.END_BRACKET);
				}

				filterString.append(QueryToolConstants.NL);
				query.append(filterString);
			}
			query.append("}");
		}

		query.append(new ElementFilter(
				InstancedDataUtil.isOneOfLong(QueryToolConstants.STUDY_ID_VAR, aggregateStudyIds(form))).toString());

		query.append(QueryToolConstants.NL).append("}");

		return query.toString();
	}

	public static List<Query> buildRepeatableGroupDataQueries(DataTableColumn column, List<String> uris,
			List<FormResult> selectedForms, Node accountNode) {

		List<Query> queries = new ArrayList<Query>();
		int dataElementCount = 0;
		RepeatableGroup group = getRepeatableGroupUsingColumn(column, selectedForms);
		FormResult form = getFormByName(column.getForm(), selectedForms);

		if (group != null) {
			dataElementCount = group.getSelectedElements().size();
		} else {
			// if no particular group is specified, set the de count as the sum of all the repeating groups' de size
			for (RepeatableGroup currentGroup : form.getRepeatableGroups()) {
				if (currentGroup.doesRepeat()) {
					dataElementCount += currentGroup.getSelectedElements().size();
				}
			}
		}

		int lowerIndex = 0;
		int upperIndex = lowerIndex + QueryToolConstants.INSTANCED_DATA_COLUMN_LIMIT - 1;
		if (upperIndex > dataElementCount - 1) {
			upperIndex = dataElementCount - 1;
		}
		while (lowerIndex <= dataElementCount - 1 && upperIndex >= 0) {
			queries.add(buildRepeatableGroupDataQuery(form, uris, group, lowerIndex, upperIndex, accountNode));

			if (upperIndex + QueryToolConstants.INSTANCED_DATA_COLUMN_LIMIT < dataElementCount) {
				lowerIndex = upperIndex + 1;
				upperIndex += QueryToolConstants.INSTANCED_DATA_COLUMN_LIMIT;
			} else {
				lowerIndex = upperIndex + 1;
				upperIndex += dataElementCount % QueryToolConstants.INSTANCED_DATA_COLUMN_LIMIT;
			}
		}

		return queries;
	}

	public static Query getPermissibleValueQuery(List<String> permissibleValueUris) {
		Query query = QueryFactory.make();
		query.setQuerySelectType();
		ElementGroup body = new ElementGroup();
		query.setQueryPattern(body);
		ElementTriplesBlock block = new ElementTriplesBlock();
		body.addElement(block);
		
		query.addResultVar(QueryToolConstants.URI_VAR);
		for (int i = 1; i < QueryToolConstants.PV_FIELDS.size(); i++) {
			BeanField currentField = QueryToolConstants.PV_FIELDS.get(i);
			query.addResultVar(currentField.getName());
			block.addTriple(Triple.create(QueryToolConstants.URI_VAR,
					NodeFactory.createURI(currentField.getPropertyUri()), Var.alloc(currentField.getName())));
		}

		body.addElement(SparqlConstructionUtil.isOneOfUri(QueryToolConstants.URI_VAR.getName(), permissibleValueUris));
		return query;
	}

	/**
	 * Builds the query to get all the data elements and repeatable groups fields for a data element
	 * 
	 * @param formUri
	 * @return
	 */
	public static String getRepeatableGroupQuery(String formUri) {

		StringBuffer sb = new StringBuffer();
		// TODO:URICONSTANTS NEED TO MOVE THESE URI's to CONSTNATS FILE
		sb.append("SELECT *").append(QueryToolConstants.NL).append("WHERE {").append(QueryToolConstants.NL).append("<")
				.append(formUri).append(">")
				.append(" <http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/hasRepeatableGroup> ?rg .")
				.append(QueryToolConstants.NL)
				.append("?rg rdfs:subClassOf <http://ninds.nih.gov/repository/fitbir/1.0/RepeatableGroup>;")
				.append(QueryToolConstants.NL)
				.append("<http://ninds.nih.gov/repository/fitbir/1.0/RepeatableGroup/hasDataElement> ?uri .");

		for (int i = 1; i < QueryToolConstants.ELEMENT_FIELDS.size(); i++) {
			BeanField elementField = QueryToolConstants.ELEMENT_FIELDS.get(i);

			sb.append("OPTIONAL { ?uri ").append(elementField.getPropertyUri()).append(QueryToolConstants.WS)
					.append("?").append(elementField.getName()).append(" . }").append(QueryToolConstants.NL);
		}

		sb.append("}");

		return sb.toString();
	}

	/**
	 * A simpler method to build a queyr to just get de and rg uri's
	 * 
	 * @param formUri
	 * @return
	 */
	public static Query getDataElementInGroupSimple(String formUri) {
		Query query = QueryFactory.make();
		query.setQuerySelectType();
		ElementGroup body = new ElementGroup();
		query.setQueryPattern(body);
		ElementTriplesBlock block = new ElementTriplesBlock();
		body.addElement(block);
		query.addResultVar(QueryToolConstants.RG_VAR);
		query.addResultVar(QueryToolConstants.DATA_ELEMENT_VARIABLE);
		query.addResultVar(QueryToolConstants.RG_NAME_VARIABLE);
		query.addResultVar(QueryToolConstants.RG_POSITION_VARIABLE);
		query.addResultVar(QueryToolConstants.DE_POSITION_VARIABLE);
		query.addResultVar(QueryToolConstants.RG_TYPE_VARIABLE);
		query.addResultVar(QueryToolConstants.RG_THRESHOLD_VARIABLE);
		query.addResultVar(QueryToolConstants.REQUIRED_TYPE_VARIABLE);

		block.addTriple(Triple.create(NodeFactory.createURI(formUri), QueryToolConstants.HAS_REPEATABLE_GROUP,
				QueryToolConstants.RG_VAR));
		block.addTriple(
				Triple.create(QueryToolConstants.RG_VAR, RDFS.subClassOf.asNode(), QueryToolConstants.RG_CLASS_NODE));
		block.addTriple(Triple.create(QueryToolConstants.RG_VAR, QueryToolConstants.RG_NAME_PROPERTY,
				QueryToolConstants.RG_NAME_VARIABLE));
		block.addTriple(Triple.create(QueryToolConstants.RG_VAR, QueryToolConstants.RG_POSITION_PROPERTY,
				QueryToolConstants.RG_POSITION_VARIABLE));
		block.addTriple(Triple.create(QueryToolConstants.RG_VAR, QueryToolConstants.RG_TYPE_PROPERTY,
				QueryToolConstants.RG_TYPE_VARIABLE));
		block.addTriple(Triple.create(QueryToolConstants.RG_VAR, QueryToolConstants.RG_THRESHOLD_PROPERTY,
				QueryToolConstants.RG_THRESHOLD_VARIABLE));
		block.addTriple(Triple.create(QueryToolConstants.RG_VAR, QueryToolConstants.HAS_DATA_ELEMENT,
				QueryToolConstants.DATA_ELEMENT_VARIABLE));
		block.addTriple(Triple.create(QueryToolConstants.RG_VAR, QueryToolConstants.DATA_ELEMENT_VARIABLE,
				QueryToolConstants.DE_POSITION_VARIABLE));
		block.addTriple(Triple.create(QueryToolConstants.RG_VAR, QueryToolConstants.HAS_REQUIRED_TYPE,
				QueryToolConstants.REQUIRED_TYPE_N_VARIABLE));
		block.addTriple(Triple.create(QueryToolConstants.REQUIRED_TYPE_N_VARIABLE, RDFS.subClassOf.asNode(),
				QueryToolConstants.DATA_ELEMENT_VARIABLE));
		block.addTriple(Triple.create(QueryToolConstants.REQUIRED_TYPE_N_VARIABLE, RDFS.label.asNode(),
				QueryToolConstants.REQUIRED_TYPE_VARIABLE));

		return query;
	}

	/**
	 * Returns the repeatable group object by using the column object
	 * 
	 * @param column
	 * @return
	 */
	private static RepeatableGroup getRepeatableGroupUsingColumn(DataTableColumn column,
			List<FormResult> selectedForms) {

		RepeatableGroup group = null;
		FormResult currentForm = getFormFromColumn(column, selectedForms);

		if (currentForm != null) {
			for (RepeatableGroup rg : currentForm.getRepeatableGroups()) {
				if (rg.getName().equals(column.getRepeatableGroup())) {
					group = rg;
					break;
				}
			}
		}

		return group;
	}

	/**
	 * Retrieves a selected form by the given column object.
	 * 
	 * @param sortColumn - The data table column used to search over.
	 * @return The FormResult object that corresponds to the given column object. If the given column object could not
	 *         be matched with any of the selected forms, null will be returned.
	 */
	private static FormResult getFormFromColumn(DataTableColumn sortColumn, List<FormResult> selectedForms) {
		FormResult foundForm = null;

		// Search the list of selected forms for the form that is associated with the specified column.
		for (FormResult form : selectedForms) {
			if (form.getShortNameAndVersion().equals(sortColumn.getForm())) {
				foundForm = form;
				break;
			}
		}

		return foundForm;
	}

	private static FormResult getFormByName(String formName, List<FormResult> selectedForms) {
		for (FormResult form : selectedForms) {
			if (form.getShortNameAndVersion().equals(formName)) {
				return form;
			}
		}

		return null;
	}

	public static Query getDataColumnHasDataQuery(FormResult form, Node accountNode) {
		Query query = QueryFactory.make();
		query.setQuerySelectType();
		query.setDistinct(true);
		ElementGroup body = new ElementGroup();
		ElementTriplesBlock block = new ElementTriplesBlock();
		body.addElement(block);
		query.setQueryPattern(body);
		query.addResultVar(QueryToolConstants.REPEATABLE_GROUP_VARIABLE);
		query.addResultVar(QueryToolConstants.REPEATABLE_GROUP_NAME_VARIABLE);
		query.addResultVar(QueryToolConstants.DATA_ELEMENT_VARIABLE);
		query.addResultVar(QueryToolConstants.DATA_ELEMENT_NAME_VARIABLE);

		block.addTriple(
				Triple.create(QueryToolConstants.ROW_VAR, RDF.type.asNode(), NodeFactory.createURI(form.getUri())));
		block = InstancedDataUtil.addPermissionTriples(block, accountNode, QueryToolConstants.ROW_VAR,
				QueryToolConstants.DATASET_VAR, QueryToolConstants.STUDY_ID_VAR);
		body.addElementFilter(new ElementFilter(
				InstancedDataUtil.isOneOfLong(QueryToolConstants.STUDY_ID_VAR, aggregateStudyIds(form))));

		block.addTriple(Triple.create(QueryToolConstants.ROW_VAR, QueryToolConstants.HAS_REPEATABLE_GROUP_INSTANCE_N,
				QueryToolConstants.INSTANCED_REPEATABLE_GROUP_VARIABLE));
		block.addTriple(Triple.create(QueryToolConstants.INSTANCED_REPEATABLE_GROUP_VARIABLE, RDF.type.asNode(),
				QueryToolConstants.REPEATABLE_GROUP_VARIABLE));
		block.addTriple(Triple.create(QueryToolConstants.REPEATABLE_GROUP_VARIABLE,
				QueryToolConstants.REPEATABLE_GROUP_PROP_NAME_N, QueryToolConstants.REPEATABLE_GROUP_NAME_VARIABLE));
		block.addTriple(Triple.create(QueryToolConstants.INSTANCED_REPEATABLE_GROUP_VARIABLE,
				QueryToolConstants.DATA_ELEMENT_VARIABLE, QueryToolConstants.VALUE_VARIABLE));
		block.addTriple(Triple.create(QueryToolConstants.DATA_ELEMENT_VARIABLE,
				QueryToolConstants.DATA_ELEMENT_PROP_NAME_N, QueryToolConstants.DATA_ELEMENT_NAME_VARIABLE));

		body.addElement(new ElementFilter(InstancedDataUtil.buildNotEqualsExpression(QueryToolConstants.VALUE_VARIABLE,
				QueryToolConstants.EMPTY_STRING)));

		return query;
	}

	public static Query getHasHighlightedGuidQuery(FormResult form, Node accountNode) {
		Query query = QueryFactory.create();
		query.setQueryAskType();
		ElementGroup body = new ElementGroup();
		query.setQueryPattern(body);
		ElementTriplesBlock block = new ElementTriplesBlock();
		body.addElement(block);
		block.addTriple(
				Triple.create(QueryToolConstants.ROW_VAR, RDF.type.asNode(), NodeFactory.createURI(form.getUri())));
		block.addTriple(
				Triple.create(QueryToolConstants.ROW_VAR, QueryToolConstants.ROW_GUID, QueryToolConstants.GUID_VAR));
		block.addTriple(Triple.create(QueryToolConstants.GUID_VAR, GuidRDF.DO_HIGHLIGHT_PROP.asNode(),
				QueryToolConstants.DO_HIGHLIGHT_VAR));

		block = InstancedDataUtil.addPermissionTriples(block, accountNode, QueryToolConstants.ROW_VAR,
				QueryToolConstants.DATASET_VAR, QueryToolConstants.STUDY_ID_VAR);

		body.addElementFilter(new ElementFilter(
				InstancedDataUtil.isOneOfLong(QueryToolConstants.STUDY_ID_VAR, aggregateStudyIds(form))));


		return query;
	}

	public static Query loadByRepeatingColumnQuery(FormResult form, DataTableColumn tableColumn,
			List<RepeatingCellColumn> rgColumnsToLoad, Node accountNode) {
		RepeatableGroup rg = form.getRepeatableGroupByName(tableColumn.getRepeatableGroup());

		Query query = QueryFactory.create();
		query.setQuerySelectType();
		ElementGroup body = new ElementGroup();
		query.setQueryPattern(body);
		ElementTriplesBlock block = new ElementTriplesBlock();
		body.addElement(block);

		query.addResultVar(QueryToolConstants.ROW_VAR);
		block.addTriple(
				Triple.create(QueryToolConstants.ROW_VAR, RDF.type.asNode(), NodeFactory.createURI(form.getUri())));
		block = InstancedDataUtil.addPermissionTriples(block, accountNode, QueryToolConstants.ROW_VAR,
				QueryToolConstants.DATASET_VAR, QueryToolConstants.STUDY_ID_VAR);
		// block.addTriple(Triple.create(QueryToolConstants.ROW_VAR, QueryToolConstants.ROW_SUBMISSION,
		// QueryToolConstants.SUBMISSION_VAR));
		block.addTriple(Triple.create(QueryToolConstants.ROW_VAR, QueryToolConstants.HAS_INSTANCED_REPEATABLE_GROUP,
				QueryToolConstants.INSTANCED_REPEATABLE_GROUP_VARIABLE));
		block.addTriple(Triple.create(QueryToolConstants.INSTANCED_REPEATABLE_GROUP_VARIABLE, RDF.type.asNode(),
				NodeFactory.createURI(rg.getUri())));

		for (RepeatingCellColumn rgColumn : rgColumnsToLoad) {
			DataElement de = rg.getDataElement(rgColumn.getDataElement());
			Var deVariable = Var.alloc(de.getName());
			block.addTriple(Triple.create(QueryToolConstants.INSTANCED_REPEATABLE_GROUP_VARIABLE,
					NodeFactory.createURI(de.getUri()), deVariable));
			query.addResultVar(deVariable);
		}

		return query;
	}
}
