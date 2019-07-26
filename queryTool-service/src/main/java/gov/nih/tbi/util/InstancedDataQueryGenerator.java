package gov.nih.tbi.util;

import gov.nih.tbi.commons.util.SparqlConstructionUtil;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.exceptions.InstancedDataException;
import gov.nih.tbi.filter.DataElementFilter;
import gov.nih.tbi.filter.Filter;
import gov.nih.tbi.pojo.BeanField;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.RepeatingCellColumn;
import gov.nih.tbi.semantic.model.GuidRDF;

import java.util.ArrayList;
import java.util.HashSet;
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


	private static final boolean INNER_JOIN = true;
	private static final boolean OUTER_JOIN = false;

	/**
	 * Returns a list of all the Study IDs for the given form
	 * 
	 * @param form
	 * @return
	 */
	private static List<Long> aggregateStudyIds(FormResult form) {
		List<Long> studyIds = new ArrayList<Long>();

		for (StudyResult study : form.getStudies()) {
			studyIds.add(study.getId());
		}

		return studyIds;
	}

	/**
	 * Returns the query used to get the row count for a single form
	 * 
	 * @return
	 */
	public static Query generateSingleFormRowCountQuery(FormResult form, Node accountNode) {
		Query query = QueryFactory.make();
		query.setQuerySelectType();

		ElementGroup body = new ElementGroup();
		ElementTriplesBlock block = new ElementTriplesBlock();
		body.addElement(block);
		query.setQueryPattern(body);
		query.addResultVar(QueryToolConstants.COUNT_VARIABLE, new ExprAggregator(QueryToolConstants.COUNT_VARIABLE,
				AggregatorFactory.createCountExpr(true, new ExprVar(QueryToolConstants.ROW_VAR))));
		block.addTriple(
				Triple.create(QueryToolConstants.ROW_VAR, RDF.type.asNode(), NodeFactory.createURI(form.getUri())));
		block = InstancedDataUtil.addPermissionTriples(block, accountNode, QueryToolConstants.ROW_VAR,
				QueryToolConstants.DATASET_VAR, QueryToolConstants.STUDY_ID_VAR);
		body.addElementFilter(new ElementFilter(
				InstancedDataUtil.isOneOfLong(QueryToolConstants.STUDY_ID_VAR, aggregateStudyIds(form))));
		body.addElement(SparqlConstructionUtil.buildSingleOptionalPattern(QueryToolConstants.ROW_VAR,
				QueryToolConstants.ROW_GUID, QueryToolConstants.GUID_VAR));
		// use these lists to track which repeatable group and data element triples have already been added so we don't
		// end up with duplicate triples.
		List<RepeatableGroup> rgAdded = new ArrayList<RepeatableGroup>();
		List<DataTableColumn> dataElementsAdded = new ArrayList<DataTableColumn>();


		// if need to reference a particular repeatable group or data element for the filter, we will need to add
		// the triple for those.
		if (form.getFilters() != null && !form.getFilters().isEmpty()) {
			for (Filter filter : form.getFilters()) {
				if (filter.getFilterType() == FilterType.CHANGE_IN_DIAGNOSIS) {
					filterChangeInDiagnosis(body, filter);
				} else {
					if (!filter.isEmpty()) {
						DataElementFilter deFilter = (DataElementFilter) filter;

						RepeatableGroup group = deFilter.getGroup();
						DataElement element = deFilter.getElement();

						if (!rgAdded.contains(group)) {
							block = InstancedDataUtil.addRepeatableGroupPattern(block, QueryToolConstants.ROW_VAR, form,
									group);
							rgAdded.add(group);
						}

						DataTableColumn filterColumn =
								new DataTableColumn(form.getShortNameAndVersion(), group.getName(), element.getName());
						if (!dataElementsAdded.contains(filterColumn)) {

							body.addElement(InstancedDataUtil.createOptionalDataElementPattern(form, group, element));
							dataElementsAdded.add(filterColumn);
						}

						String filterVariable = InstancedDataUtil.getDataElementVar(form, group, element).getName();
						ElementFilter elementFilter = deFilter.toElementFilter(filterVariable);

						if (elementFilter != null) {
							body.addElementFilter(elementFilter);
						}
					}
				}
			}
		}

		return query;
	}

	/**
	 * Adds the elements necessary to filter by change in diagnosis
	 * 
	 * @param body
	 * @param filter
	 */
	private static void filterChangeInDiagnosis(ElementGroup body, Filter filter) {

		ElementTriplesBlock doHighlightBlock = new ElementTriplesBlock();
		doHighlightBlock.addTriple(Triple.create(QueryToolConstants.GUID_VAR, GuidRDF.DO_HIGHLIGHT_PROP.asNode(),
				QueryToolConstants.DO_HIGHLIGHT_VAR));
		ElementOptional doHighlightGroup = new ElementOptional(doHighlightBlock);
		body.addElement(doHighlightGroup);

		ElementFilter elementFilter = filter.toElementFilter(QueryToolConstants.DO_HIGHLIGHT_VAR.getName());

		if (elementFilter != null) {
			body.addElement(elementFilter);
		}
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
	public static Query generateInnerJoinQuery(List<FormResult> selectedForms, DataTableColumn sortColumn,
			String sortOrder, Node accountNode) {
		return getJoinQuery(selectedForms, INNER_JOIN, sortColumn, sortOrder, accountNode);
	}

	public static Query generateOuterJoinQuery(List<FormResult> selectedForms, DataTableColumn sortColumn,
			String sortOrder, Node accountNode) {
		return getJoinQuery(selectedForms, OUTER_JOIN, sortColumn, sortOrder, accountNode);
	}

	/**
	 * Returns the query to get Row URIs and GUID fields of each row after a join of all the forms in selectedForms list
	 * 
	 * @param joinType - INNER_JOIN = results shall be inner join. OUTER_JOIN = results shall be left outer join
	 * @param sortColumn
	 * @param sortOrder
	 * @return
	 */
	private static Query getJoinQuery(List<FormResult> selectedForms, boolean joinType, DataTableColumn sortColumn,
			String sortOrder, Node accountNode) {
		Query query = QueryFactory.create();
		query.setQuerySelectType();

		ElementGroup body = new ElementGroup();
		query.setQueryPattern(body);

		for (int i = 0; i < selectedForms.size(); i++) {
			FormResult currentForm = selectedForms.get(i);

			if (joinType == INNER_JOIN || i == 0) {
				body.addElement(generateJoinSubQuery(currentForm, i, sortColumn, sortOrder, joinType, accountNode));
			} else {
				body.addElement(new ElementOptional(
						generateJoinSubQuery(currentForm, i, sortColumn, sortOrder, joinType, accountNode)));
			}

			if (sortColumn != null && !sortColumn.isHardCoded()
					&& sortColumn.getForm().equals(currentForm.getShortNameAndVersion())) {
				// put together the variable to sort by
				RepeatableGroup sortGroup = InstancedDataUtil.getRepeatableGroupUsingColumn(currentForm, sortColumn);
				DataElement sortElement = InstancedDataUtil.getDataElementFromColumn(currentForm, sortColumn);
				String sortVariable =
						InstancedDataUtil.getDataElementVar(currentForm, sortGroup, sortElement).toString();
				sortVariable = InstancedDataUtil.removeQuestionMark(sortVariable);
				int direction = InstancedDataUtil.getQuerySortOrder(sortOrder);
				query.addOrderBy(new ExprVar(sortVariable), direction);
			}
		}

		// here, we are adding the part of the query that sorts by the given sortColumn and sort order
		// if the sort is by a data element, then we will need to add the triples for said data element in order to
		// sort
		if (sortColumn != null) {

			// here, determine if the sort column is from the current form
			if (sortColumn.isHardCoded()) {
				String sortVariable = sortColumn.getHardCoded();
				int direction = InstancedDataUtil.getQuerySortOrder(sortOrder);
				query.addOrderBy(InstancedDataUtil.createVar(sortVariable), direction);
			}
		}

		return query;
	}

	/**
	 * Generate the subquery for each form in the join
	 * 
	 * @param form
	 * @param index
	 * @param sortColumn
	 * @param sortOrder
	 * @param joinType
	 * @return
	 */
	// TODO: change this to protected
	public static ElementSubQuery generateJoinSubQuery(FormResult form, int index, DataTableColumn sortColumn,
			String sortOrder, boolean joinType, Node accountNode) {

		List<RepeatableGroup> rgAdded = new ArrayList<RepeatableGroup>();
		List<DataTableColumn> dataElementsAdded = new ArrayList<DataTableColumn>();

		Query query = QueryFactory.create();
		query.setQuerySelectType();
		ElementGroup body = new ElementGroup();
		ElementTriplesBlock block = new ElementTriplesBlock();
		body.addElement(block);
		query.setQueryPattern(body);

		Var rowUriVar = Var.alloc(QueryToolConstants.ROW_URI + index);
		query.addResultVar(rowUriVar);
		query.addResultVar(QueryToolConstants.GUID_VAR);
		Var datasetVar = Var.alloc("dataset" + index);
		Var studyIdVar = Var.alloc("studyId" + index);

		block.addTriple(Triple.create(rowUriVar, RDF.type.asNode(), NodeFactory.createURI(form.getUri())));
		block.addTriple(Triple.create(rowUriVar, QueryToolConstants.ROW_GUID, QueryToolConstants.GUID_VAR));

		if (sortColumn != null && sortColumn.getHardCoded() != null
				&& (QueryToolConstants.GUID_COLUMN_VAR.equals(sortColumn.getHardCoded())
						|| sortColumn.getForm().equals(form.getShortNameAndVersion()))) {
			if (QueryToolConstants.STUDY_COLUMN_VAR.equals(sortColumn.getHardCoded())) {
				block.addTriple(
						Triple.create(rowUriVar, QueryToolConstants.ROW_STUDY, QueryToolConstants.STUDY_TITLE_VAR));
				query.addResultVar(QueryToolConstants.STUDY_TITLE_VAR.getName());
			} else if (QueryToolConstants.DATASET_COLUMN_VAR.equals(sortColumn.getHardCoded())) {
				block.addTriple(
						Triple.create(rowUriVar, QueryToolConstants.ROW_PREFIX, QueryToolConstants.PREFIXED_ID_VAR));
				query.addResultVar(QueryToolConstants.PREFIXED_ID_VAR.getName());
			}
		}

		block = InstancedDataUtil.addPermissionTriples(block, accountNode, rowUriVar, datasetVar, studyIdVar);

		body.addElementFilter(new ElementFilter(InstancedDataUtil.isOneOfLong(studyIdVar, aggregateStudyIds(form))));

		Map<RepeatableGroup, String> groupVariableMap = null;

		if (form.getFilters() != null && !form.getFilters().isEmpty()) {
			groupVariableMap = InstancedDataUtil.getGroupVariableMap(form);

			for (Filter filter : form.getFilters()) {
				if (filter.getFilterType() == FilterType.CHANGE_IN_DIAGNOSIS) {
					filterChangeInDiagnosis(body, filter);
				} else {
					DataElementFilter deFilter = (DataElementFilter) filter;

					if (!filter.isEmpty()) {
						RepeatableGroup group = deFilter.getGroup();
						DataElement element = deFilter.getElement();

						if (!rgAdded.contains(group)) {

							ElementTriplesBlock rgBlock =
									InstancedDataUtil.createRepeatableGroupPattern(rowUriVar, form, group);
							body.addElement(rgBlock);
							rgAdded.add(group);
						}

						DataTableColumn filterColumn =
								new DataTableColumn(form.getShortNameAndVersion(), group.getName(), element.getName());
						if (!dataElementsAdded.contains(filterColumn)) {
							// ElementTriplesBlock deBlock = InstancedDataUtil.createDataElementPattern(form, group,
							// element);
							ElementOptional deBlock = new ElementOptional(
									InstancedDataUtil.createDataElementPattern(form, group, element));
							// ElementGroup deGroup = new ElementGroup();
							// deGroup.addElement(deBlock);
							body.addElement(deBlock);
							dataElementsAdded.add(filterColumn);
						}

						// add the actual filters into the query
						ElementFilter elementFilter = deFilter.toElementFilter(
								InstancedDataUtil.getDataElementVar(groupVariableMap, form, group, element).getName());

						if (elementFilter != null) {
							body.addElementFilter(elementFilter);
						}
					}
				}
			}
		}


		// here, we are adding the part of the query that sorts by the given sortColumn and sort order the sort
		// is by a data element, then we will need to add the triples for said data element in order to sort
		if (sortColumn != null) {
			// here, determine if the sort column is from the current form
			if (!sortColumn.isHardCoded() && sortColumn.getForm().equals(form.getShortNameAndVersion())) {
				// put together the variable to sort by
				RepeatableGroup sortGroup = InstancedDataUtil.getRepeatableGroupUsingColumn(form, sortColumn);
				DataElement sortElement = InstancedDataUtil.getDataElementFromColumn(form, sortColumn);

				if (!rgAdded.contains(sortGroup)) {
					ElementOptional rgBlock = new ElementOptional(
							InstancedDataUtil.createRepeatableGroupPattern(rowUriVar, form, sortGroup));
					body.addElement(rgBlock);
					rgAdded.add(sortGroup);
				}

				// add triple for the data element
				if (sortElement != null && !dataElementsAdded.contains(sortColumn)) {
					ElementTriplesBlock deBlock =
							InstancedDataUtil.createDataElementPattern(form, sortGroup, sortElement);
					ElementGroup deGroup = new ElementGroup();
					deGroup.addElement(deBlock);
					body.addElement(deGroup);

					dataElementsAdded.add(sortColumn);
				}

				if (groupVariableMap == null) {
					groupVariableMap = InstancedDataUtil.getGroupVariableMap(form);
				}

				String sortVariable =
						InstancedDataUtil.getDataElementVar(groupVariableMap, form, sortGroup, sortElement).toString();
				query.addResultVar(sortVariable);
			}
		}

		return new ElementSubQuery(query);
	}

	/**
	 * Given a list of biosample IDs not selected by the user, this will return a SPARQL query that will get all rowUri
	 * and biosample that the user has selected.
	 * 
	 * @param form
	 * @param unselectedRowUri
	 * @param accountNode
	 * @return
	 */
	public static Query getBiosampleQueryInverse(FormResult form, Set<String> unselectedRowUri, Node accountNode) {
		Query query = QueryFactory.make();
		query.setQuerySelectType();
		ElementGroup body = new ElementGroup();
		ElementTriplesBlock block = new ElementTriplesBlock();
		body.addElement(block);
		query.setQueryPattern(body);
		query.addResultVar(QueryToolConstants.ROW_VAR);
		block.addTriple(
				Triple.create(QueryToolConstants.ROW_VAR, RDF.type.asNode(), NodeFactory.createURI(form.getUri())));
		block = InstancedDataUtil.addPermissionTriples(block, accountNode, QueryToolConstants.ROW_VAR,
				QueryToolConstants.DATASET_VAR, QueryToolConstants.STUDY_ID_VAR);

		RepeatableGroup biosampleRg = null;
		DataElement biosampleDe = null;

		for (RepeatableGroup rg : form.getRepeatableGroups()) {
			for (DataElement de : rg.getDataElements()) {
				if (QueryToolConstants.BIOSAMPLE_TYPE.equals(de.getType())) {
					biosampleRg = rg;
					biosampleDe = de;
				}
			}
		}

		if (biosampleRg == null || biosampleDe == null) {
			throw new InstancedDataException("There is no biosample data element in the given form!");
		}

		block = InstancedDataUtil.addRepeatableGroupPattern(block, QueryToolConstants.ROW_VAR, form, biosampleRg);

		Map<RepeatableGroup, String> groupVariableMap = InstancedDataUtil.getGroupVariableMap(form);

		Var biosampleGroupVar = InstancedDataUtil.getGroupVar(form, biosampleRg);
		Node biosampleDeUri = NodeFactory.createURI(biosampleDe.getUri());
		Var biosampleVar = InstancedDataUtil.getDataElementVar(groupVariableMap, form, biosampleRg, biosampleDe);
		query.addResultVar(QueryToolConstants.BIOSAMPLE_VAR, new ExprVar(biosampleVar));
		block.addTriple(Triple.create(biosampleGroupVar, biosampleDeUri, biosampleVar));

		DataTableColumn biosampleColumn =
				new DataTableColumn(form.getShortNameAndVersion(), biosampleRg.getName(), biosampleDe.getName());

		// if need to reference a particular repeatable group or data element for the filter, we will need to add
		// the triple for those.
		if (form.getFilters() != null && !form.getFilters().isEmpty()) {

			for (Filter filter : form.getFilters()) {
				if (!filter.isEmpty()) {
					if (filter.getFilterType() == FilterType.CHANGE_IN_DIAGNOSIS) {
						filterChangeInDiagnosis(body, filter);
					} else {
						DataElementFilter deFilter = (DataElementFilter) filter;
						RepeatableGroup group = deFilter.getGroup();
						DataElement element = deFilter.getElement();

						if (!group.equals(biosampleRg)) {
							block = InstancedDataUtil.addRepeatableGroupPattern(block, QueryToolConstants.ROW_VAR, form,
									group);
						}

						DataTableColumn filterColumn =
								new DataTableColumn(form.getShortNameAndVersion(), group.getName(), element.getName());
						if (!filterColumn.equals(biosampleColumn)) {

							body.addElement(InstancedDataUtil.createOptionalDataElementPattern(form, group, element));
						}

						Var deVar = InstancedDataUtil.getDataElementVar(groupVariableMap, form, group, element);
						ElementFilter elementFilter = deFilter.toElementFilter(deVar.getName());

						if (elementFilter != null) {
							body.addElementFilter(elementFilter);
						}
					}
				}
			}
		}

		// filter out all the unselected rows.
		if (unselectedRowUri != null && !unselectedRowUri.isEmpty()) {
			body.addElementFilter(new ElementFilter(InstancedDataUtil
					.isNotOneOfUri(new ExprVar(QueryToolConstants.ROW_VAR.getName()), unselectedRowUri)));
		}

		return query;
	}

	public static Query getInstancedDataRowUriQuery(FormResult form, int offset, int limit, DataTableColumn sortColumn,
			String sortOrder, Node accountNode) {
		Query outerQuery = QueryFactory.make();
		outerQuery.setDistinct(true);
		Query query = QueryFactory.make();
		outerQuery.setQuerySelectType();
		query.setQuerySelectType();

		if (limit < Integer.MAX_VALUE) {
			outerQuery.setLimit(limit);
			outerQuery.setOffset(offset);
		}

		ElementGroup body = new ElementGroup();
		ElementTriplesBlock block = new ElementTriplesBlock();
		body.addElement(block);
		query.setQueryPattern(body);
		query.addResultVar(QueryToolConstants.ROW_VAR);
		outerQuery.addResultVar(QueryToolConstants.ROW_VAR);
		block.addTriple(
				Triple.create(QueryToolConstants.ROW_VAR, RDF.type.asNode(), NodeFactory.createURI(form.getUri())));
		block = InstancedDataUtil.addPermissionTriples(block, accountNode, QueryToolConstants.ROW_VAR,
				QueryToolConstants.DATASET_VAR, QueryToolConstants.STUDY_ID_VAR);
		body.addElement(SparqlConstructionUtil.buildSingleOptionalPattern(QueryToolConstants.ROW_VAR,
				QueryToolConstants.ROW_GUID, QueryToolConstants.GUID_VAR));
		body.addElementFilter(new ElementFilter(
				InstancedDataUtil.isOneOfLong(QueryToolConstants.STUDY_ID_VAR, aggregateStudyIds(form))));

		// use these lists to track which repeatable group and data element triples have already been added so we don't
		// end up with duplicate triples.
		List<RepeatableGroup> rgAdded = new ArrayList<RepeatableGroup>();
		List<DataTableColumn> dataElementsAdded = new ArrayList<DataTableColumn>();

		Map<RepeatableGroup, String> groupVariableMap = null;

		// if need to reference a particular repeatable group or data element for the filter, we will need to add
		// the triple for those.
		if (form.getFilters() != null && !form.getFilters().isEmpty()) {
			groupVariableMap = InstancedDataUtil.getGroupVariableMap(form);

			for (Filter filter : form.getFilters()) {
				if (!filter.isEmpty()) {
					if (filter.getFilterType() == FilterType.CHANGE_IN_DIAGNOSIS) {
						filterChangeInDiagnosis(body, filter);
					} else {
						DataElementFilter deFilter = (DataElementFilter) filter;
						RepeatableGroup group = deFilter.getGroup();
						DataElement element = deFilter.getElement();

						if (!rgAdded.contains(group)) {
							block = InstancedDataUtil.addRepeatableGroupPattern(block, QueryToolConstants.ROW_VAR, form,
									group);
							rgAdded.add(group);
						}

						DataTableColumn filterColumn =
								new DataTableColumn(form.getShortNameAndVersion(), group.getName(), element.getName());
						if (!dataElementsAdded.contains(filterColumn)) {

							body.addElement(InstancedDataUtil.createOptionalDataElementPattern(form, group, element));
							dataElementsAdded.add(filterColumn);
						}

						Var deVar = InstancedDataUtil.getDataElementVar(groupVariableMap, form, group, element);
						ElementFilter elementFilter = deFilter.toElementFilter(deVar.getName());

						if (elementFilter != null) {
							body.addElementFilter(elementFilter);
						}
					}
				}
			}
		}

		// here, we are adding the part of the query that sorts by the given sortColumn and sort order if the sort
		// is by a data element, then we will need to add the triples for said data element in order to sort
		if (sortColumn != null) {

			String sortVariable = null;
			int direction = InstancedDataUtil.getQuerySortOrder(sortOrder);

			if (!sortColumn.isHardCoded()) {
				// put together the variable to sort by
				RepeatableGroup sortGroup = InstancedDataUtil.getRepeatableGroupUsingColumn(form, sortColumn);
				DataElement sortElement = InstancedDataUtil.getDataElementFromColumn(form, sortColumn);

				// add the triple for the repeatable group
				if (sortGroup != null && !rgAdded.contains(sortGroup)) {
					block = InstancedDataUtil.addRepeatableGroupPattern(block, QueryToolConstants.ROW_VAR, form,
							sortGroup);
					rgAdded.add(sortGroup);
				}

				// add triple for the data element
				if (sortElement != null && !dataElementsAdded.contains(sortColumn)) {
					body.addElement(InstancedDataUtil.createOptionalDataElementPattern(form, sortGroup, sortElement));
					dataElementsAdded.add(sortColumn);
				}

				if (groupVariableMap == null) {
					groupVariableMap = InstancedDataUtil.getGroupVariableMap(form);
				}

				sortVariable =
						InstancedDataUtil.getDataElementVar(groupVariableMap, form, sortGroup, sortElement).toString();
				sortVariable = InstancedDataUtil.removeQuestionMark(sortVariable);

				query.addOrderBy(new ExprVar(sortVariable), direction);
			} else {
				sortVariable = sortColumn.getHardCoded();
				sortVariable = InstancedDataUtil.removeQuestionMark(sortVariable);

				if (QueryToolConstants.PREFIXED_ID_VAR.getName().equals(sortVariable)) {
					block.addTriple(Triple.create(QueryToolConstants.ROW_VAR, QueryToolConstants.ROW_PREFIX,
							QueryToolConstants.PREFIXED_ID_VAR));
					query.addResultVar(QueryToolConstants.PREFIXED_ID_VAR);
				}

				outerQuery.addOrderBy(new ExprVar(sortVariable), direction);
			}
		}

		outerQuery.setQueryPattern(new ElementSubQuery(query));

		return outerQuery;
	}

	public static Query generateJoinUriQuery(List<FormResult> selectedForms, int offset, int limit,
			DataTableColumn sortColumn, String sortOrder, Node accountNode) {
		Query query = QueryFactory.create();

		Query subQuery = null;
		if (InstancedDataUtil.doesNonPrimaryFormHaveFilter(selectedForms)) {
			subQuery = generateInnerJoinQuery(selectedForms, sortColumn, sortOrder, accountNode);
		} else {
			subQuery = generateOuterJoinQuery(selectedForms, sortColumn, sortOrder, accountNode);
		}

		ElementSubQuery subQueryElement = new ElementSubQuery(subQuery);
		query.setQueryPattern(subQueryElement);
		query.setQuerySelectType();

		if (limit < Integer.MAX_VALUE) {
			query.setLimit(limit);
			query.setOffset(offset);
		}

		for (int i = 0; i < selectedForms.size(); i++) {
			Var rowUriVar = Var.alloc(QueryToolConstants.ROW_URI + i);
			query.addResultVar(rowUriVar);
			query.addResultVar(QueryToolConstants.GUID_VAR);
			subQuery.addResultVar(rowUriVar);
			subQuery.addResultVar(QueryToolConstants.GUID_VAR);
		}

		return query;
	}

	/**
	 * Returns a query that will select the row uri, repeatable group, and the count of rows for that repeatable group +
	 * row This is so we can get a count of all the repeating group
	 * 
	 * @param form
	 * @param rowUris
	 * @return
	 */
	public static Query generateRepeatableGroupRowCountsQuery(FormResult form, Set<String> rowUris) {

		List<RepeatableGroup> repeatingRgs = form.getRepeatingRepeatableGroups();
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

		for (Filter filter : form.getFilters()) {
			DataElementFilter deFilter = (DataElementFilter) filter;

			if (repeatingRgs.contains(deFilter.getGroup())) {
				Var groupVariable = Var.alloc(InstancedDataUtil.getGroupVariable(form, deFilter.getGroup()));
				Var deVariable = Var.alloc(InstancedDataUtil.getGroupVariable(form, deFilter.getGroup()).substring(1)
						+ deFilter.getElement().getName());

				block.addTriple(Triple.create(QueryToolConstants.ROW_VARIABLE,
						QueryToolConstants.HAS_REPEATABLE_GROUP_INSTANCE_N, groupVariable));
				block.addTriple(Triple.create(groupVariable, RDF.type.asNode(),
						NodeFactory.createURI(deFilter.getGroup().getUri())));
				block.addTriple(Triple.create(groupVariable, NodeFactory.createURI(deFilter.getElement().getUri()),
						deVariable));

				ElementFilter elementFilter = filter.toElementFilter(deVariable.getName());

				if (elementFilter != null) {
					body.addElementFilter(elementFilter);
				}
			}
		}
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

	public static Query generateJoinedFormRowCountQuery(List<FormResult> selectedForms, Node accountNode) {

		// if we're not doing joins, or if the primary form has filters, or if any of the a joined forms do not have any
		// filters applied
		Query query = null;

		if (selectedForms.size() == 1) { // non-joined row count query
			query = generateSingleFormRowCountQuery(selectedForms.get(0), accountNode);
		} else { // joined row count query
			if (InstancedDataUtil.doesNonPrimaryFormHaveFilter(selectedForms)) {
				query = generateInnerJoinQuery(selectedForms, null, null, accountNode);
			} else {
				query = generateOuterJoinQuery(selectedForms, null, null, accountNode);
			}

			query.addResultVar(QueryToolConstants.COUNT_VARIABLE, new ExprAggregator(QueryToolConstants.COUNT_VARIABLE,
					AggregatorFactory.createCountExpr(false, new ExprVar(QueryToolConstants.GUID_VAR))));

		}
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

		// append filters
		if (form.getFilters() != null && !form.getFilters().isEmpty()) {
			for (Filter filter : form.getFilters()) {
				DataElementFilter deFilter = (DataElementFilter) filter;

				RepeatableGroup currentGroup = deFilter.getGroup();

				String groupVariable = groupVariableMap.get(currentGroup);
				DataElement element = deFilter.getElement();
				int deIndex = currentGroup.getIndexOfDataElement(element);

				String deVariable =
						InstancedDataUtil.getDataElementVar(groupVariableMap, form, currentGroup, element).getName();

				// if the pattern for the data element hasn't been added in this query yet, then we will need to add
				// it. use the index of
				// the data element to check whether or not it should've been added already or not if deindex is not
				// in the range of low and
				// upper, then add the pattern we will also need to add it if the group repeats
				if (deIndex < lowerIndex || deIndex > upperIndex || !group.equals(currentGroup)) {
					query.append("OPTIONAL { ?row fs:hasRepeatableGroupInstance ").append(groupVariable).append(" .")
							.append(QueryToolConstants.NL).append(groupVariable).append(QueryToolConstants.WS)
							.append("a <").append(currentGroup.getUri()).append("> .").append(QueryToolConstants.NL);

					query.append(QueryToolConstants.OPTIONAL_START).append(groupVariable).append(QueryToolConstants.WS)
							.append("<").append(element.getUri()).append(">").append(QueryToolConstants.NL).append("?")
							.append(deVariable).append(" .").append(QueryToolConstants.END_BRACKET)
							.append(QueryToolConstants.NL);
					query.append("}");
				}

				ElementFilter elementFilter = deFilter.toElementFilter(deVariable);
				if (elementFilter != null) {
					query.append(elementFilter);
				}
			}
		}

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
	public static String getDataElementInGroupSimple(String formUri) {

		StringBuffer sb = new StringBuffer();
		// TODO:URICONSTANTS NEED TO MOVE THESE URI's to CONSTNATS FILE
		sb.append("SELECT *").append(QueryToolConstants.NL).append("WHERE {").append(QueryToolConstants.NL).append("<")
				.append(formUri).append(">")
				.append(" <http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/hasRepeatableGroup> ?rg .")
				.append(QueryToolConstants.NL)
				.append("?rg <http://ninds.nih.gov/repository/fitbir/1.0/RepeatableGroup/name> ?rg_name;")
				.append(QueryToolConstants.NL).append("rg:position ?rg_position;")
				.append("rdfs:subClassOf <http://ninds.nih.gov/repository/fitbir/1.0/RepeatableGroup>;")
				.append("<http://ninds.nih.gov/repository/fitbir/1.0/RepeatableGroup/type> ?rg_type;")
				.append("<http://ninds.nih.gov/repository/fitbir/1.0/RepeatableGroup/threshold> ?rg_threshold;")
				.append(QueryToolConstants.NL).append("rg:hasDataElement ?uri;").append(QueryToolConstants.NL)
				.append("?uri ?de_position .").append(QueryToolConstants.NL).append("}");

		return sb.toString();
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
