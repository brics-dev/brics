package gov.nih.tbi.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.expr.E_Bound;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalNot;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_NotEquals;
import com.hp.hpl.jena.sparql.expr.E_NotOneOf;
import com.hp.hpl.jena.sparql.expr.E_OneOf;
import com.hp.hpl.jena.sparql.expr.E_Regex;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.E_StrLowerCase;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDT;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDecimal;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDouble;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueInteger;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueNode;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueString;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.vocabulary.RDF;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.commons.util.SparqlConstructionUtil;
import gov.nih.tbi.commons.util.ValUtil;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.exceptions.InstancedDataException;
import gov.nih.tbi.filter.JaxbFilter;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.DataTableColumnWithUri;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.CellValue;
import gov.nih.tbi.repository.model.CellValueCode;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.FormHeader;
import gov.nih.tbi.repository.model.InstancedRecord;
import gov.nih.tbi.repository.model.InstancedRepeatableGroupRow;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;
import gov.nih.tbi.repository.model.RepeatableGroupHeader;
import gov.nih.tbi.repository.model.RepeatingCellColumn;
import gov.nih.tbi.repository.model.RepeatingCellValue;
import gov.nih.tbi.semantic.model.AccountRDF;
import gov.nih.tbi.semantic.model.FormStructureRDF;
import gov.nih.tbi.semantic.model.StudyRDF;

public class InstancedDataUtil extends SparqlConstructionUtil {

	public static final String AGE_YRS = "AgeYrs";
	public static final String NINETY_PLUS = "90+";
	public static final String AGE_RANGE_SEPARATOR = " - ";

	public static DataTableColumn getColumnFromRepeatableGroup(FormResult form, RepeatableGroup group) {

		String rgName = group.getName();
		for (DataTableColumn column : getOrderedColumns(form)) {
			if (form.getShortNameAndVersion().equals(column.getForm()) && rgName.equals(column.getRepeatableGroup())
					&& column.getDataElement() == null) {
				return column;
			}
		}

		return null;
	}

	/**
	 * Returns a set with all of the data element columns from the given selected forms
	 * 
	 * @param selectedForms
	 * @return
	 */
	public static Set<DataTableColumnWithUri> buildDataColumnSet(List<FormResult> selectedForms) {
		Set<DataTableColumnWithUri> dataColumnSet = new HashSet<DataTableColumnWithUri>();

		if (selectedForms != null) {
			for (FormResult selectedForm : selectedForms) {
				for (RepeatableGroup group : selectedForm.getRepeatableGroups()) {
					for (DataElement dataElement : group.getDataElements()) {
						dataColumnSet.add(new DataTableColumnWithUri(selectedForm, group, dataElement));
					}
				}
			}
		}

		return dataColumnSet;
	}

	/**
	 * Returns DataTableColumns as ordered in the forms. For repeatable groups that repeat, this is added as one column,
	 * the individual data elements inside that group don't get added as individual datatablecolumns
	 * 
	 * @param form
	 * @return
	 */
	public static List<DataTableColumn> getOrderedColumns(FormResult form) {

		List<DataTableColumn> orderedColumns = new ArrayList<DataTableColumn>();

		if (form != null) {
			for (RepeatableGroup group : form.getRepeatableGroups()) {
				if (group.doesRepeat()) {
					DataTableColumn column =
							form.getColumnFromString(form.getShortNameAndVersion(), group.getName(), null);
					orderedColumns.add(column);
				} else {
					for (DataElement element : group.getSelectedElements()) {
						DataTableColumn column = form.getColumnFromString(form.getShortNameAndVersion(),
								group.getName(), element.getName());
						orderedColumns.add(column);
					}
				}
			}
		}

		return orderedColumns;
	}

	public static List<DataTableColumn> getVisibleColumns(FormResult form) {
		List<DataTableColumn> columns = getOrderedColumns(form);


		if (form != null) {
			for (RepeatableGroup group : form.getRepeatableGroups()) {
				if (group.doesRepeat()) {

					for (DataElement element : group.getSelectedElements()) {
						RepeatingCellColumn rgColumn = new RepeatingCellColumn(form.getShortNameAndVersion(),
								group.getName(), element.getName());
						columns.add(rgColumn);
					}
				}
			}
		}
		
		return columns;
	}

	/**
	 * Returns the repeatable group object by using the column object
	 * 
	 * @param column
	 * @return
	 */
	public static RepeatableGroup getRepeatableGroupUsingColumn(FormResult form, DataTableColumn column) {

		if (form != null) {
			for (RepeatableGroup rg : form.getRepeatableGroups()) {
				if (rg.getName().equals(column.getRepeatableGroup())) {
					return rg;
				}
			}
		}

		return null;
	}

	/**
	 * Returns the string 'rowUri' with the form index of the given form appended to it e.g. calling this method with
	 * the primary form would yield rowUri0.
	 * 
	 * @param form
	 * @return
	 */
	public static String getRowUriVariableForJoin(List<FormResult> selectedForms, FormResult form) {
		int count = selectedForms.indexOf(form);

		return count >= 0 ? QueryToolConstants.ROW_URI + count : null;
	}

	public static DataElement getDataElementFromColumn(FormResult form, DataTableColumn column) {

		RepeatableGroup currentRg = null;

		if (form != null) {
			currentRg = form.getGroupByName(column.getRepeatableGroup());
		}

		if (currentRg != null) {
			for (DataElement de : currentRg.getSelectedElements()) {
				if (de.getName().equals(column.getDataElement())) {
					return de;
				}
			}
		}

		return null;
	}

	/**
	 * Retrieves a selected form by the given column object.
	 * 
	 * @param sortColumn - The data table column used to search over.
	 * @return The FormResult object that corresponds to the given column object. If the given column object could not
	 *         be matched with any of the selected forms, null will be returned.
	 */
	public static FormResult getFormFromColumn(List<FormResult> forms, DataTableColumn sortColumn) {
		FormResult foundForm = null;

		// Search the list of selected forms for the form that is associated with the specified column.
		for (FormResult form : forms) {
			if (form.getShortNameAndVersion().equals(sortColumn.getForm())) {
				return foundForm;
			}
		}

		return null;
	}

	/**
	 * Iterate through the list of selected forms, returns true if any of the non-primary forms have at least one
	 * filter.
	 * 
	 * @return True if any of the non-primary forms have a filter, false otherwise.
	 */
	public static boolean doesNonPrimaryFormHaveFilter(List<FormResult> formResults) {

		if (formResults.size() < 2) {
			throw new UnsupportedOperationException(
					"Calling this doesn't make sense if you don't have more than one forms selected");
		}

		for (int i = 1; i < formResults.size(); i++) {
			FormResult currentForm = formResults.get(i);
			if (currentForm.hasFilter()) {
				return true;
			}
		}

		return false;
	}

	public static String removeQuestionMark(String variable) {
		if (variable.startsWith("?")) {
			return variable.substring(1);
		}

		return variable;
	}

	/**
	 * Verifies whether or not the selected forms are able to be joined.
	 * 
	 * @return True if and only if there are any joined forms present and all selected forms have at least one GUID
	 *         element associated with it.
	 */
	public static boolean isJoined(List<FormResult> formResults) {

		return formResults.size() > 1 && formsHaveGuid(formResults);
	}

	/**
	 * Checks all selected forms in the "selectedForms" list to see if they all have GUID data elements. If the
	 * "selectedForms" list is empty, false will be returned.
	 * 
	 * @return True if and only if all selected forms have GUID data elements.
	 */
	public static boolean formsHaveGuid(List<FormResult> formResults) {

		// Check if there are any selected forms.
		if (!formResults.isEmpty()) {
			// Verify that all selected forms have GUIDs
			for (FormResult form : formResults) {
				if (!InstancedDataUtil.hasGuid(form)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Determines if all of the data elements in all selected forms are hidden or de-selected.
	 * 
	 * @return True if and only if all data elements in all of the selected forms are hidden.
	 */
	public static boolean areAllDataElementsHidden(List<FormResult> formResults) {
		for (FormResult form : formResults) {
			for (RepeatableGroup group : form.getRepeatableGroups()) {
				for (DataElement de : group.getDataElements()) {
					if (de.isSelected()) {
						return false;
					}
				}
			}
		}

		return true;
	}

	/**
	 * Appends the triples in input onto the output and return
	 * 
	 * @param output
	 * @param input
	 * @return
	 */
	public static ElementTriplesBlock appendTriples(ElementTriplesBlock output, ElementTriplesBlock input) {
		Iterator<Triple> inputIterator = input.patternElts();
		while (inputIterator.hasNext()) {
			output.addTriple(inputIterator.next());
		}

		return output;
	}

	/**
	 * Given the string version of sort order. Returns the integer representation used by Jena ARQ.
	 * 
	 * @param sortOrder - DESC for descending or ASC for ascending
	 * @return
	 */
	public static int getQuerySortOrder(String sortOrder) {
		if (QueryToolConstants.DESCENDING.equalsIgnoreCase(sortOrder)) {
			return Query.ORDER_DESCENDING;
		} else if (QueryToolConstants.ASCENDING.equalsIgnoreCase(sortOrder)) {
			return Query.ORDER_ASCENDING;
		} else {
			throw new InstancedDataException("Invalid query order string");
		}
	}

	/**
	 * Adds the triples used to filter the given row URI by the given account URIs permission properties
	 * 
	 * @param block
	 * @param accountUri
	 * @param rowVar
	 * @param datasetVar
	 * @return
	 */
	public static ElementTriplesBlock addPermissionTriples(ElementTriplesBlock block, Node accountUri, Var rowVar,
			Var datasetVar, Var studyIdVar) {
		block.addTriple(Triple.create(rowVar, FormStructureRDF.PROPERTY_DATASET.asNode(), datasetVar));
		block.addTriple(Triple.create(accountUri, AccountRDF.PROPERTY_DATASET.asNode(), datasetVar));
		block.addTriple(Triple.create(datasetVar, StudyRDF.PROPERTY_ID.asNode(), studyIdVar));
		return block;
	}

	public static StringBuffer addPermissionTriples(StringBuffer buffer, String accountUri, String rowVar,
			String datasetVar, String studyIdVar) {
		buffer.append("?row <").append(FormStructureRDF.PROPERTY_DATASET.getURI()).append("> ?dataset .")
				.append(QueryToolConstants.NL).append("<").append(accountUri).append("> <")
				.append(AccountRDF.PROPERTY_DATASET.getURI()).append("> ?").append(datasetVar).append(" .")
				.append(QueryToolConstants.NL).append("?").append(datasetVar).append(" <").append(StudyRDF.PROPERTY_ID)
				.append("> ?").append(studyIdVar).append(" .").append(QueryToolConstants.NL);

		return buffer;
	}

	public static final Var createVar(String variableString) {
		return Var.alloc(variableString.replaceAll("^\\?", QueryToolConstants.EMPTY_STRING));
	}

	/**
	 * Given a list of records, returns a multimap of the dataset IDs to all of the attached files from that dataset.
	 * This method is mainly for query tool download so portal can attach only the files that appear in the query tool
	 * result rather than the whole dataset
	 * 
	 * @param records
	 * @return
	 */
	public static ListMultimap<Long, String> getDatasetToAttachedFilesMap(List<InstancedRecord> records) {

		ListMultimap<Long, String> datasetToAttachedFilesMap = ArrayListMultimap.create();

		for (InstancedRecord record : records) {
			for (InstancedRow row : record.getSelectedRows()) {
				// in certain conditions, not all records will have all rows, so skip the rows that aren't available)
				if (row != null) {
					Long currentDatasetId = row.getDatasetId();
					for (CellValue currentCell : row.getCell().values()) {
						if (!currentCell.getIsRepeating()) {
							NonRepeatingCellValue nonRepeatingCell = (NonRepeatingCellValue) currentCell;

							DataType currentType = nonRepeatingCell.getDataElementType();

							switch (currentType) {
								case FILE:
								case THUMBNAIL:
								case TRIPLANAR:
									String cellValue = nonRepeatingCell.getValue();
									String fileName = BRICSStringUtils.pathToFileName(cellValue);

									if (fileName != null && !fileName.isEmpty()) {
										datasetToAttachedFilesMap.put(currentDatasetId, fileName);
									}
									break;
								default:
									break;
							}
						} else {
							RepeatingCellValue repeatingCell = (RepeatingCellValue) currentCell;

							for (InstancedRepeatableGroupRow groupRow : repeatingCell.getRows()) {
								for (Entry<RepeatingCellColumn, CellValueCode> groupCell : groupRow.getCell()
										.entrySet()) {

									RepeatingCellColumn rgColumn = groupCell.getKey();
									DataType currentType = rgColumn.getType();

									switch (currentType) {
										case FILE:
										case THUMBNAIL:
										case TRIPLANAR:
											String cellValue = groupCell.getValue().getValue();
											String fileName = BRICSStringUtils.pathToFileName(cellValue);

											if (fileName != null && !fileName.isEmpty()) {
												datasetToAttachedFilesMap.put(currentDatasetId, fileName);
											}
											break;
										default:
											break;
									}

								}
							}
						}
					}
				}
			}
		}

		return datasetToAttachedFilesMap;

	}

	/**
	 * Convenience methods to null check the rdf node before parsing it to a string
	 * 
	 * @param node
	 * @return
	 */
	public static String rdfNodeToString(RDFNode node) {

		if (node == null) {
			return null;
		}

		if (node.isLiteral()) {
			return node.asNode().getLiteralValue().toString();
		} else {
			return node.asResource().getURI();
		}
		// return InstancedDataUtil.trimRdfType(node.asNode().);
	}

	public static ElementTriplesBlock createRepeatableGroupPattern(Var rowVariable, FormResult form,
			RepeatableGroup group) {

		return addRepeatableGroupPattern(new ElementTriplesBlock(), rowVariable, form, group);
	}

	public static ElementTriplesBlock addRepeatableGroupPattern(ElementTriplesBlock block, Var rowVariable,
			FormResult form, RepeatableGroup group) {

		Var groupVariable = getGroupVar(form, group);

		return addRepeatableGroupPattern(block, rowVariable, groupVariable, group.getUri());
	}

	public static ElementTriplesBlock addRepeatableGroupPattern(ElementTriplesBlock block, Var rowVariable,
			Var groupVariable, String groupUri) {

		if (block != null) {
			block.addTriple(
					Triple.create(rowVariable, QueryToolConstants.HAS_REPEATABLE_GROUP_INSTANCE_N, groupVariable));

			block.addTriple(Triple.create(groupVariable, RDF.type.asNode(), NodeFactory.createURI(groupUri)));
		}

		return block;
	}

	public static Var getGroupVar(FormResult form, RepeatableGroup group) {

		Map<RepeatableGroup, String> groupVariableMap = InstancedDataUtil.getGroupVariableMap(form);
		String groupVariableString = groupVariableMap.get(group).substring(1);
		String variableString = form.getShortName() + groupVariableString;
		if (groupVariableString != null) {
			return Var.alloc(variableString);
		}

		return null;
	}

	public static Var getDataElementVar(FormResult form, RepeatableGroup group, DataElement element) {

		Map<RepeatableGroup, String> groupVariableMap = InstancedDataUtil.getGroupVariableMap(form);
		return getDataElementVar(groupVariableMap, form, group, element);
	}

	public static Var getDataElementVar(Map<RepeatableGroup, String> groupVariableMap, FormResult form,
			RepeatableGroup group, DataElement element) {

		if (groupVariableMap == null) {
			throw new InstancedDataException("Unable to generate data element variable name for query.");
		}

		String groupVariableString = groupVariableMap.get(group).substring(1);
		String variableString = form.getShortName() + groupVariableString + element.getName();

		if (groupVariableString != null) {
			return Var.alloc(variableString);
		} else {
			throw new InstancedDataException("Unable to generate data element variable name for query.");
		}
	}

	public static ElementTriplesBlock createDataElementPattern(FormResult form, RepeatableGroup group,
			DataElement element) {
		return addDataElementPattern(new ElementTriplesBlock(), form, group, element);
	}

	public static ElementTriplesBlock addDataElementPattern(ElementTriplesBlock block, FormResult form,
			RepeatableGroup group, DataElement element) {

		Var groupVariable = getGroupVar(form, group);
		Var deVariable = getDataElementVar(form, group, element);

		if (block != null) {
			block.addTriple(Triple.create(groupVariable, NodeFactory.createURI(element.getUri()), deVariable));
		}

		return block;
	}

	public static Element createOptionalDataElementPattern(FormResult form, RepeatableGroup group,
			DataElement element) {
		Var groupVariable = getGroupVar(form, group);
		Var deVariable = getDataElementVar(form, group, element);

		return buildSingleOptionalPattern(groupVariable, NodeFactory.createURI(element.getUri()), deVariable);
	}

	/**
	 * Creates an IN filter for a variable against a list of values Looks like, FILTER( ?variable IN "value1", "value2",
	 * ..."valueN" )
	 * 
	 * @param variable
	 * @param uris
	 * @return
	 */
	public static Expr isOneOfLong(Var variable, Collection<Long> values) {

		ExprList urisExpression = new ExprList();

		for (Long value : values) {
			urisExpression.add(new NodeValueInteger(value));
		}

		Expr filterExpression = new E_OneOf(new ExprVar(variable), urisExpression);

		return filterExpression;
	}

	/**
	 * Creates a list of string bindings for a single variable
	 * 
	 * @param variable
	 * @param values
	 * @return
	 */
	public static List<Binding> buildLiteralValuesBinding(Var variable, Collection<String> values) {

		List<Binding> bindings = new ArrayList<Binding>();

		for (String value : values) {
			BindingHashMap bindingHashMap = new BindingHashMap();
			bindingHashMap.add(variable, NodeFactory.createLiteral(value));
			bindings.add(bindingHashMap);
		}

		return bindings;
	}

	/**
	 * Creates a list of string bindings for a single variable
	 * 
	 * @param variable - variable to be used
	 * @param values - values to bind to the given variable
	 * @return a list of bindings
	 */
	public static List<Binding> buildTypedValuesBinding(Var variable, XSDDatatype type, Collection<String> values) {

		List<Binding> bindings = new ArrayList<Binding>();

		for (String value : values) {
			BindingHashMap bindingHashMap = new BindingHashMap();
			bindingHashMap.add(variable, NodeFactory.createLiteral(value, type));
			bindings.add(bindingHashMap);
		}

		return bindings;
	}

	/**
	 * Creates a list of URI bindings for a single variable
	 * 
	 * @param variable
	 * @param values
	 * @return
	 */
	public static List<Binding> buildURIValuesBinding(Var variable, Collection<String> values) {

		List<Binding> bindings = new ArrayList<Binding>();

		for (String value : values) {
			BindingHashMap bindingHashMap = new BindingHashMap();
			bindingHashMap.add(variable, NodeFactory.createURI(value));
			bindings.add(bindingHashMap);
		}

		return bindings;
	}

	/**
	 * Creates a list of Long bindings for a single variable
	 * 
	 * @param variable
	 * @param values
	 * @return
	 */
	public static List<Binding> buildLongValuesBinding(Var variable, Collection<Long> values) {

		List<Binding> bindings = new ArrayList<Binding>();

		for (Long value : values) {
			BindingHashMap bindingHashMap = new BindingHashMap();
			bindingHashMap.add(variable, NodeFactory.createLiteral(value.toString(), XSDDatatype.XSDlong));
			bindings.add(bindingHashMap);
		}

		return bindings;
	}

	/**
	 * Builds a sub-query with one VALUES clause with one variable binding to a list of values Looks something like, {
	 * VALUES (variable) { "value1", "value2", "value3", ..."valueN" } } This used as an alternative for FILTER IN when
	 * filtering against a large list of items.
	 * 
	 * @param variable
	 * @param values
	 * @return
	 */
	public static ElementSubQuery buildLongValuesSubQuery(Var variable, Collection<Long> values) {

		Query subQuery = QueryFactory.make();
		subQuery.setDistinct(true);

		List<Var> variables = new ArrayList<Var>();
		variables.add(variable);
		subQuery.setValuesDataBlock(variables, buildLongValuesBinding(variable, values));

		return new ElementSubQuery(subQuery);
	}

	/**
	 * Builds a sub-query with one VALUES clause with one variable binding to a list of values Looks something like, {
	 * VALUES (variable) { "value1", "value2", "value3", ..."valueN" } } This used as an alternative for FILTER IN when
	 * filtering against a large list of items.
	 * 
	 * @param variable
	 * @param values
	 * @return
	 */
	public static ElementSubQuery buildLiteralValuesSubQuery(Var variable, Collection<String> values) {

		Query subQuery = QueryFactory.make();
		subQuery.setDistinct(true);

		List<Var> variables = new ArrayList<Var>();
		variables.add(variable);
		subQuery.setValuesDataBlock(variables, buildLiteralValuesBinding(variable, values));

		return new ElementSubQuery(subQuery);
	}

	/**
	 * Builds a sub-query with one VALUES clause with one variable binding to a list of values Looks something like, {
	 * VALUES (variable) { "value1", "value2", "value3", ..."valueN" } } This used as an alternative for FILTER IN when
	 * filtering against a large list of items.
	 * 
	 * @param variable
	 * @param values
	 * @return
	 */
	public static ElementSubQuery buildTypedValuesSubQuery(Var variable, XSDDatatype type, Collection<String> values) {

		Query subQuery = QueryFactory.make();
		subQuery.setDistinct(true);

		List<Var> variables = new ArrayList<Var>();
		variables.add(variable);
		subQuery.setValuesDataBlock(variables, buildTypedValuesBinding(variable, type, values));

		return new ElementSubQuery(subQuery);
	}

	/**
	 * Builds a sub-query with one VALUES clause with one variable binding to a list of values Looks something like, {
	 * VALUES (variable) { "value1", "value2", "value3", ..."valueN" } } This used as an alternative for FILTER IN when
	 * filtering against a large list of items.
	 * 
	 * @param variable
	 * @param values
	 * @return
	 */
	public static ElementSubQuery buildURIValuesSubQuery(Var variable, Collection<String> values) {

		Query subQuery = QueryFactory.make();
		subQuery.setDistinct(true);

		List<Var> variables = new ArrayList<Var>();
		variables.add(variable);
		subQuery.setValuesDataBlock(variables, buildURIValuesBinding(variable, values));

		return new ElementSubQuery(subQuery);
	}

	public static String getGuidUri(FormResult form) {

		if (form != null) {
			for (RepeatableGroup group : form.getRepeatableGroups()) {
				if (!group.doesRepeat()) {
					for (DataElement de : group.getSelectedElements()) {
						if (DataType.GUID == de.getType()) {
							return de.getUri();
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Returns true if the given guid has a GUID data element
	 * 
	 * @param form
	 * @return
	 */
	public static boolean hasGuid(FormResult form) {

		if (form != null) {
			for (RepeatableGroup group : form.getRepeatableGroups()) {
				if (!group.doesRepeat()) {
					for (DataElement de : group.getDataElements()) {
						if (DataType.GUID == de.getType()) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public static Expr multiRegexFilter(Expr variable, List<String> searchKey) {

		LinkedList<String> searchKeyList = new LinkedList<String>();
		searchKeyList.addAll(searchKey);

		return multiRegexFilterHelper(variable, searchKeyList);
	}

	public static Expr multiRegexFilterHelper(Expr variable, LinkedList<String> searchKey) {

		if (searchKey.size() == 0) {
			return null;
		} else if (searchKey.size() == 1) // base case
		{
			return buildRegexFilterInsensitive(variable, searchKey.pop());
		} else
		// search key has more than one element
		{
			return new E_LogicalOr(buildRegexFilterInsensitive(variable, searchKey.pop()),
					multiRegexFilterHelper(variable, searchKey));
		}
	}

	public static ElementFilter generateElementFilter(String currentVariable, JaxbFilter filter) {
		Expr filterExpr = getFilterExpressionFromFilter(currentVariable, filter);

		if (filterExpr != null) {
			return new ElementFilter(filterExpr);
		} else {
			return null;
		}
	}

	public static ElementFilter generateElementFilterDelimited(String currentVariable, JaxbFilter filter) {
		String filterString = filter.getFreeFormValue();
		Set<String> stringSet = addFilterDelimited(filterString, ";");
		Expr filterExpr = multiRegexFilter(currentVariable, stringSet);

		return new ElementFilter(filterExpr);
	}

	public static Set<String> addFilterDelimited(String filterString, String delimiter) {
		String filterStringWithoutSpaces = filterString.replaceAll("\\s", "");
		List<String> filterStringList = Arrays.asList(filterStringWithoutSpaces.split(delimiter));

		return new HashSet<String>(filterStringList);
	}

	/**
	 * Given a variable name and a Filter object, create a element filter that corresponds the filter criteria specified
	 * in the filter object.
	 * 
	 * @param currentVariable
	 * @param filter
	 * @return
	 */
	public static Expr getFilterExpressionFromFilter(String currentVariable, JaxbFilter filter) {

		ExprVar variable = new ExprVar(currentVariable);
		Expr filterExpression = null;

		if (DataType.BIOSAMPLE == filter.getElement().getType() || DataType.GUID == filter.getElement().getType()) {
			String filterString = filter.getFreeFormValue();
			List<String> filterStrings = BRICSStringUtils.delimitedStringToList(filterString, ";");
			filterExpression = multiRegexFilter(variable, filterStrings);
		} else if (filter.isNumeric()) {
			Double min = filter.getMinimum();
			Double max = filter.getMaximum();
			Expr minExpr = new E_GreaterThanOrEqual(variable, NodeValueDecimal.makeDecimal(min.doubleValue()));
			Expr maxExpr = new E_LessThanOrEqual(variable, NodeValueDecimal.makeDecimal(max.doubleValue()));
			filterExpression = new E_LogicalAnd(minExpr, maxExpr);
		} else if (filter.isFreeForm() && !ValUtil.isBlank(filter.getFreeFormValue())) {
			filterExpression = buildRegexFilterInsensitive(variable, filter.getFreeFormValue());
		} else if (filter.isSingleSelect() && !ValUtil.isCollectionEmpty(filter.getPermissibleValues())) // string
		{
			if (DataType.NUMERIC == filter.getElement().getType()) {
				filterExpression = isOneOfNumeric(variable, filter.getPermissibleValues());
			} else {
				filterExpression = isOneOfString(variable, filter.getPermissibleValues());
			}
		} else if (filter.isMultiSelect()
				|| filter.isMultiSelectOrCombo() && !ValUtil.isCollectionEmpty(filter.getPermissibleValues())) {

			String freeForm = filter.getFreeFormValue();
			List<String> permissibleValues = filter.getPermissibleValues();
			if (freeForm != null && !freeForm.equals("")) {
				permissibleValues.add(freeForm);
			}
			filterExpression = multiRegexFilter(variable, permissibleValues);
			// free-form: filterExpression = buildRegexFilterInsensitive(variable, filter.getFreeFormValue());
			// multi-select: filterExpression = multiRegexFilter(variable, filter.getPermissibleValues());


		} else if (filter.isDate() && filter.getDateMax() != null && filter.getDateMin() != null) // data
		// filter
		{
			String minString = BRICSTimeDateUtil.dateToDateString(filter.getDateMin());
			String maxString = BRICSTimeDateUtil.dateToDateString(filter.getDateMax());

			Expr min = new E_GreaterThan(variable,
					new NodeValueDT(minString, NodeFactory.createLiteral(minString, XSDDatatype.XSDdateTime)));
			Expr max = new E_LessThanOrEqual(variable,
					new NodeValueDT(maxString, NodeFactory.createLiteral(maxString, XSDDatatype.XSDdateTime)));
			filterExpression = new E_LogicalAnd(min, max);
		} else if (filter.isBlank()) {
			return null;
		} else { // !filter.isBlank()
			return new E_NotEquals(variable, new NodeValueString(QueryToolConstants.EMPTY_STRING));
		}

		if (filter.isBlank()) {
			filterExpression = new E_LogicalOr(
					new E_Equals(new E_Str(variable), new NodeValueString(QueryToolConstants.EMPTY_STRING)),
					filterExpression);
			filterExpression = new E_LogicalOr(new E_LogicalNot(new E_Bound(variable)), filterExpression);
			return filterExpression;
		} else {
			return filterExpression;
		}
	}

	/**
	 * Returns an expression for regex given a variable name and a value. Assumes case insensitive. e.g.
	 * Filter(regex(?variable, "keyword", i))
	 * 
	 * @return
	 */
	public static Expr buildRegexFilterInsensitive(Expr variable, String keyword) {

		return new E_Regex(variable, regexEscape(keyword), "i");
	}

	public static Expr isOneOfNumeric(Expr variable, Collection<String> values) {

		ExprList urisExpression = new ExprList();

		for (String value : values) {
			urisExpression.add(new NodeValueDouble(Double.valueOf(value)));
		}

		Expr filterExpression = new E_OneOf(variable, urisExpression);

		return filterExpression;
	}

	/**
	 * Creates an IN filter for a variable against a list of values Looks like, FILTER( ?variable IN "value1", "value2",
	 * ..."valueN" )
	 * 
	 * @param variable
	 * @param uris
	 * @return
	 */
	public static Expr isOneOfString(Expr variable, Collection<String> values) {

		ExprList exprList = new ExprList();

		for (String value : values) {
			exprList.add(new NodeValueString(value.toLowerCase()));
		}

		Expr filterExpression = new E_OneOf(new E_StrLowerCase(variable), exprList);

		return filterExpression;
	}

	/**
	 * Creates an IN filter for a variable against a list of values Looks like, FILTER( ?variable IN <value1>, <value2>,
	 * ...<valueN> )
	 * 
	 * @param variable
	 * @param uris
	 * @return
	 */
	public static Expr isOneOfUri(Expr variable, Collection<String> values) {
		ExprList urisExpression = new ExprList();

		for (String value : values) {
			urisExpression.add(new NodeValueNode(NodeFactory.createURI(value)));
		}

		Expr filterExpression = new E_OneOf(variable, urisExpression);

		return filterExpression;
	}

	/**
	 * Creates an NOT IN filter for a variable against a list of values Looks like, FILTER( ?variable NOT IN <value1>,
	 * <value2>, ...<valueN> )
	 * 
	 * @param variable
	 * @param uris
	 * @return
	 */
	public static Expr isNotOneOfUri(Expr variable, Collection<String> values) {
		ExprList urisExpression = new ExprList();

		for (String value : values) {
			urisExpression.add(new NodeValueNode(NodeFactory.createURI(value)));
		}

		Expr filterExpression = new E_NotOneOf(variable, urisExpression);

		return filterExpression;
	}


	/**
	 * Returns what should be inside the filter part of the query for a particular data element filter. Depends on what
	 * type of filter it is, but the general format is going to be like, ?variable = blablabla
	 * 
	 * @param currentVariable
	 * @param filter
	 * @return
	 */
	public static StringBuffer getQueryForFilter(String currentVariable, JaxbFilter filter) {
		StringBuffer query = new StringBuffer();

		if (!filter.isBlank()) {
			if (filter.isNumeric()) {
				query.append("(bound(" + currentVariable + "))&&");
			} else if (InputRestrictions.FREE_FORM.equals(filter.getElement().getInputRestrictions())
					&& !ValUtil.isBlank(filter.getFreeFormValue())) {
				query.append("(bound(" + currentVariable + "))&&");
			} else if (InputRestrictions.SINGLE.equals(filter.getElement().getInputRestrictions())
					&& !ValUtil.isCollectionEmpty(filter.getPermissibleValues())) {
				// Add string filter
				query.append("(bound(" + currentVariable + "))&&");
			} else if (InputRestrictions.MULTIPLE.equals(filter.getElement().getInputRestrictions())
					&& !ValUtil.isCollectionEmpty(filter.getPermissibleValues())) {
				query.append("(bound(" + currentVariable + "))&&");
			} else if (filter.isDate() && filter.getDateMax() != null && filter.getDateMin() != null) {
				query.append("(bound(" + currentVariable + "))&&");
			} else {
				query.append("(bound(" + currentVariable + "))||");
			}
		} else {
			// Add blank filter if the include blanks check box is selected.
			query.append("(!bound(" + currentVariable + "))||");
		}

		if (DataType.BIOSAMPLE.getValue().equals(filter.getElement().getType())
				|| DataType.GUID.getValue().equals(filter.getElement().getType())) {
			String filterString = filter.getFreeFormValue();
			List<String> filterStrings = BRICSStringUtils.delimitedStringToList(filterString, ";");

			for (String value : filterStrings) {
				query.append("regex( str(").append(currentVariable).append("), \"")
						.append(value.replaceAll("\"", "\\\"")).append("\"").append(", \"i\") || ");
			}
			query.replace(query.length() - 4, query.length(), QueryToolConstants.EMPTY_STRING);
			query.append(") .").append(QueryToolConstants.NL);
		} else if (filter.isNumeric()) {
			// Add numeric filter.
			query.append(currentVariable).append(">=\"").append(filter.getMinimum()).append("\"^^xsd:decimal && ")
					.append(currentVariable).append("<=\"").append(filter.getMaximum()).append("\"^^xsd:decimal) .")
					.append(QueryToolConstants.NL);
		} else if (InputRestrictions.FREE_FORM.equals(filter.getElement().getInputRestrictions())
				&& !ValUtil.isBlank(filter.getFreeFormValue())) {
			query.append("(regex( str(").append(currentVariable).append("), \"")
					.append(filter.getFreeFormValue().replaceAll("\"", "\\\"")).append("\"").append(", \"i\"))");
			query.append(") .").append(QueryToolConstants.NL);
		} else if (InputRestrictions.SINGLE.equals(filter.getElement().getInputRestrictions())
				&& !ValUtil.isCollectionEmpty(filter.getPermissibleValues())) {
			// String filter
			for (String value : filter.getPermissibleValues()) {
				query.append("lcase(str(").append(currentVariable).append(")) = \"")
						.append(value.toLowerCase().replaceAll("\"", "\\\\\"")).append("\" || ");
			}

			query.replace(query.length() - 4, query.length(), QueryToolConstants.EMPTY_STRING);
			query.append(") .").append(QueryToolConstants.NL);
		} else if (InputRestrictions.MULTIPLE.equals(filter.getElement().getInputRestrictions())
				&& !ValUtil.isCollectionEmpty(filter.getPermissibleValues())) {
			for (String value : filter.getPermissibleValues()) {
				query.append("regex( str(").append(currentVariable).append("), \"")
						.append(value.replaceAll("\"", "\\\"")).append("\"").append(", \"i\") || ");
			}
			query.replace(query.length() - 4, query.length(), QueryToolConstants.EMPTY_STRING);
			query.append(") .").append(QueryToolConstants.NL);
		} else if (filter.isDate() && filter.getDateMax() != null && filter.getDateMin() != null) {
			// date filter
			query.append(currentVariable).append(">=\"")
					.append(QueryToolConstants.FILTER_DATE_FORMATTER.format(filter.getDateMin()))
					.append("\"^^xsd:dateTime && ").append(currentVariable).append("<=\"")
					.append(QueryToolConstants.FILTER_DATE_FORMATTER.format(filter.getDateMax()))
					.append("\"^^xsd:dateTime) .").append(QueryToolConstants.NL);
		} else {
			query.append("bound(").append(currentVariable).append(") ) .").append(QueryToolConstants.NL);
		}

		return query;
	}

	/**
	 * Returns DataTableColumns as ordered in the forms.
	 * 
	 * @param form
	 * @return
	 */
	public static List<DataTableColumn> getAllOrderedColumns(FormResult form) {

		List<DataTableColumn> orderedColumns = new ArrayList<DataTableColumn>();

		if (form != null) {
			for (RepeatableGroup group : form.getRepeatableGroups()) {
				for (DataElement element : group.getSelectedElements()) {
					DataTableColumn column =
							form.getColumnFromString(form.getShortNameAndVersion(), group.getName(), element.getName());
					orderedColumns.add(column);
				}
			}
		}

		return orderedColumns;
	}

	/**
	 * Convenience method to build a optional block using one triple
	 * 
	 * @param subject
	 * @param property
	 * @param object
	 * @return
	 */
	public static ElementOptional buildSingleOptionalPattern(Node subject, Node property, Node object) {

		ElementTriplesBlock optionalBlock = new ElementTriplesBlock();
		optionalBlock.addTriple(Triple.create(subject, property, object));
		ElementOptional elementOptional = new ElementOptional(optionalBlock);
		return elementOptional;
	}

	public static String trimRdfType(String s) {

		String[] parts = s.split("\\^\\^");
		return parts[0].trim();
	}

	public static FormHeader getInitialFormHeader(FormResult form, boolean forDownload) {

		FormHeader formHeader = new FormHeader();
		formHeader.setName(form.getShortName());
		// In order to support same version joins and flattened CSV, we must track the version number
		formHeader.setVersion(form.getVersion());

		for (RepeatableGroup rg : form.getRepeatableGroups()) {

			List<DataElement> deList = null;

			if (forDownload) {
				deList = rg.getSelectedElements();
			} else {
				deList = rg.getDataElements();
			}

			if (!deList.isEmpty()) {
				RepeatableGroupHeader rgHeader = new RepeatableGroupHeader();
				rgHeader.setName(rg.getName());
				rgHeader.setDoesRepeat(rg.doesRepeat());

				for (DataElement de : deList) {
					rgHeader.addDataElementHeader(de.getName());
				}

				formHeader.addRepeatableGroupHeader(rgHeader);
			}
		}

		return formHeader;
	}

	public static FormHeader getJoinedInitialFormHeader(FormResult form, boolean forDownload) {

		FormHeader formHeader = new FormHeader();
		formHeader.setName(form.getShortName());
		// In order to support same version joins and flattened CSV, we must track the version number
		formHeader.setVersion(form.getVersion());

		RepeatableGroupHeader hardCodedRg = new RepeatableGroupHeader();
		formHeader.addRepeatableGroupHeader(hardCodedRg);
		hardCodedRg.setName(QueryToolConstants.EMPTY_STRING);
		hardCodedRg.addDataElementHeader("Study ID");
		hardCodedRg.addDataElementHeader("Dataset");

		for (RepeatableGroup rg : form.getRepeatableGroups()) {

			List<DataElement> deList = null;

			if (forDownload) {
				deList = rg.getSelectedElements();
			} else {
				deList = rg.getDataElements();
			}

			if (!deList.isEmpty()) {
				RepeatableGroupHeader rgHeader = new RepeatableGroupHeader();
				rgHeader.setName(rg.getName());
				rgHeader.setDoesRepeat(rg.doesRepeat());

				for (DataElement de : deList) {
					rgHeader.addDataElementHeader(de.getName());
				}

				formHeader.addRepeatableGroupHeader(rgHeader);
			}
		}

		return formHeader;
	}

	/**
	 * Returns the IN filter used to remove rows the user does not have access to
	 * 
	 * @return
	 */
	public static String buildDatasetUnions(List<Long> dsIds) {

		if (dsIds == null || dsIds.isEmpty()) {
			return "?row dataset:datasetId 9999999 .";
		}

		StringBuffer sb = new StringBuffer();

		if (dsIds.size() > 2) {
			sb.append("{ ");
		}

		for (Long dsId : dsIds) {
			sb.append(" { ?row dataset:datasetId ").append(dsId).append(" } UNION ");
		}

		sb.replace(sb.length() - 6, sb.length(), QueryToolConstants.EMPTY_STRING);

		if (dsIds.size() > 2) {
			sb.append("} . ");
		}

		sb.append(QueryToolConstants.NL);

		return sb.toString();
	}

	public static String getValidRepeatableGroupName(String rgName) {
		return rgName.replaceAll("[^A-Za-z0-9]", "");
	}

	/***********************************************/
	/**
	 * Returns a hash map of the repeatable group to its unique variable name to be used in building the queries
	 * 
	 * @return
	 */
	public static Map<RepeatableGroup, String> getGroupVariableMap(FormResult form) {

		List<String> alreadyAdded = new ArrayList<String>();
		Map<RepeatableGroup, String> groupVariableMap = new HashMap<RepeatableGroup, String>();
		for (RepeatableGroup repeatableGroup : form.getRepeatableGroups()) {
			String groupVariable = "?" + repeatableGroup.getName();
			groupVariable = groupVariable.replaceAll("[^A-Za-z0-9]", ""); // for some reason whitespaces are allowed in
			// repeatable group names. Need to get rid of them.
			groupVariable = "?" + groupVariable;

			Integer number = 0;

			// append numbers by increments until the variable name is unique
			while (alreadyAdded.contains(groupVariable)) {
				// if a number has been already added to the end, remove the numbers so we can append a new number
				// if no number has been appended yet then the current number should be 0
				if (number > 0) {
					int groupVarLength = groupVariable.length();
					groupVariable =
							groupVariable.substring(groupVarLength - number.toString().length(), groupVarLength);
				}

				groupVariable = groupVariable + number;
				number++;
			}

			alreadyAdded.add(groupVariable);

			groupVariableMap.put(repeatableGroup, groupVariable);
		}

		return groupVariableMap;
	}

	/**
	 * Returns the variable to be used for the guid data element in SPARQL query
	 * 
	 * @param form
	 * @return
	 */
	public static String getGuidVar(FormResult form) {

		for (RepeatableGroup group : form.getRepeatableGroups()) {
			for (DataElement element : group.getSelectedElements()) {
				if (DataType.GUID == element.getType()) {
					return getGroupVariableMap(form).get(group) + element.getName();
				}
			}
		}

		return null;
	}

	public static String getGroupVariable(FormResult form, RepeatableGroup group) {
		return InstancedDataUtil.getGroupVariableMap(form).get(group);
	}

	public static RepeatableGroup getGuidRg(FormResult form) {

		for (RepeatableGroup group : form.getRepeatableGroups()) {
			for (DataElement element : group.getSelectedElements()) {
				if (DataType.GUID == element.getType()) {
					return group;
				}
			}
		}

		return null;
	}

	public static String buildValuesPattern(String variable, List<String> values) {

		StringBuffer sb = new StringBuffer();

		sb.append("values ?" + variable + " { ");

		if (values == null || values.isEmpty()) {
			sb.append("99999 ");
		} else {
			for (String value : values) {
				sb.append(value).append(QueryToolConstants.WS);
			}
		}

		sb.replace(sb.length() - 1, sb.length(), QueryToolConstants.EMPTY_STRING);
		sb.append("} ");

		return sb.toString();
	}

	public static String buildDatasetValuesPattern(List<Long> dsIds, String variable) {

		StringBuffer sb = new StringBuffer();

		if (variable == null || variable.isEmpty()) {
			variable = "?dsId";
		}

		sb.append("values " + variable + " { ");

		if (dsIds == null || dsIds.isEmpty()) {
			sb.append("99999 ");
		} else {
			for (Long dsId : dsIds) {
				sb.append(dsId).append(QueryToolConstants.WS);
			}
		}

		sb.replace(sb.length() - 1, sb.length(), QueryToolConstants.EMPTY_STRING);
		sb.append("} ");

		return sb.toString();
	}

	public static String buildDatasetFilters(List<Long> dsIds) {

		StringBuffer sb = new StringBuffer();

		sb.append("FILTER ( ?dsId IN ( ");

		if (dsIds == null || dsIds.isEmpty()) {
			sb.append("99999, ");
		} else {
			for (Long dsId : dsIds) {
				sb.append(dsId).append(", ");
			}
		}

		sb.replace(sb.length() - 2, sb.length(), QueryToolConstants.EMPTY_STRING);
		sb.append(" )) . ");

		return sb.toString();
	}

	public static String buildGuidFilters(String variable, Set<String> guids) {

		StringBuffer sb = new StringBuffer();

		sb.append("values " + variable + " { ");

		if (guids == null || guids.isEmpty()) {
			sb.append("\"NOTHINGNULL\", ");
		} else {
			for (String guid : guids) {
				sb.append(QueryToolConstants.DOUBLE_QUOTE).append(guid).append(QueryToolConstants.DOUBLE_QUOTE)
						.append("^^").append(QueryToolConstants.STRING_TYPE).append(" ");
			}
		}

		sb.append(" } ");

		return sb.toString();
	}

	public static boolean hasRepeatingGroup(FormResult form) {

		for (RepeatableGroup group : form.getRepeatableGroups()) {
			if (group.doesRepeat()) {
				return true;
			}
		}

		return false;
	}

	public static void selectGuidElements(FormResult form) {

		if (form != null) {
			if (form.getRepeatableGroups() != null) {
				for (RepeatableGroup rg : form.getRepeatableGroups()) {
					if (rg.getDataElements() != null) {
						for (DataElement de : rg.getDataElements()) {
							if (DataType.GUID == de.getType() && !de.isSelected()) {
								de.setSelected(true);
								return;
							}
						}
					}
				}
			}
		}
	}


	public static boolean isDisplaySchema(String displayOption) {
		return (displayOption != null && !CellValueCode.PERMISSIBLE_VALUE.equals(displayOption)
				&& !CellValueCode.OUTPUT_CODE.equals(displayOption)
				&& !CellValueCode.OUTPUT_CODE_PV.equals(displayOption));
	}


	/**
	 * 
	 * @param sortColumn - in the format of formNameAndVersion,repeatbableGroupName,dataElementName
	 * @return
	 */
	public static DataTableColumn getSortColumn(String sortColName) {
		if (ValUtil.isBlank(sortColName)) {
			return null;
		}

		String[] sortColArr = sortColName.split(",");
		if (sortColArr == null) {
			return null;
		}

		DataTableColumn sortColumn = null;
		if (sortColArr.length == 1) {
			// Hardcoded column for single form or GUID column for joined forms
			sortColumn = new DataTableColumn(sortColArr[0]);

		} else if (sortColArr.length == 2) {
			// Study or Data Set column for joined forms, format: formNameVersion,?study or ?prefixId
			sortColumn = new DataTableColumn(sortColArr[0], sortColArr[1]);

		} else if (sortColArr.length == 3) {
			// regular column are passed in as formNameVersion,rgName,deName
			sortColumn = new DataTableColumn(sortColArr[0], sortColArr[1], sortColArr[2]);
		}

		return sortColumn;

	}


	public static Node getAccountNode(String userName) {
		return NodeFactory.createURI(AccountRDF.createResource(userName).getURI());
	}
	
	/**
	 * Returns the age range for the given point value of AgeYrs. 
	 * For example, 32 will be converted to 30 - 39.
	 */
	public static String getAgeYrsRange(String ageYrsVal) {
		double ageYrsDbl;
		
		try {
			ageYrsDbl = Double.valueOf(ageYrsVal);
		} catch (NumberFormatException | NullPointerException e) {
			// Return empty string if any exception occurred.
			return "";
		}
		
		if (ageYrsDbl < 0) {
			return "";
		}
		
		int lowerRange = (int) ageYrsDbl / 10 * 10;
		int upperRange = lowerRange + 9;
		
		if (lowerRange < 90) {
			return lowerRange + AGE_RANGE_SEPARATOR + upperRange;
		} else {
			return NINETY_PLUS;
		}
	}
	
}
