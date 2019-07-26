package gov.nih.tbi.dao.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.dao.InstancedDataDao;
import gov.nih.tbi.exceptions.InstancedDataException;
import gov.nih.tbi.pojo.CellPosition;
import gov.nih.tbi.pojo.CodeMapping;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.DataTableColumnWithUri;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.CellValue;
import gov.nih.tbi.repository.model.CellValueCode;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.InstancedRepeatableGroupRow;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;
import gov.nih.tbi.repository.model.RepeatingCellColumn;
import gov.nih.tbi.repository.model.RepeatingCellValue;
import gov.nih.tbi.service.RDFStoreManager;
import gov.nih.tbi.service.cache.InstancedDataFormCache;
import gov.nih.tbi.service.model.MetaDataCache;
import gov.nih.tbi.util.InstancedDataQueryGenerator;
import gov.nih.tbi.util.InstancedDataUtil;
import gov.nih.tbi.util.ResultSetToDataElement;

@Repository
@Transactional
public class InstancedDataDaoImpl implements InstancedDataDao, Serializable {

	private static final long serialVersionUID = 6999452529415924840L;
	private static final Logger log = LogManager.getLogger(InstancedDataDaoImpl.class.getName());


	@Autowired
	RDFStoreManager rdfStoreManager;

	public InstancedDataDaoImpl() {}

	public Set<String> getRowUrisToLoad(FormResult form, int offset, int limit, DataTableColumn sortColumn,
			String sortOrder, Node accountNode) {
		Query rowUriQuery = InstancedDataQueryGenerator.getInstancedDataRowUriQuery(form, offset, limit, sortColumn,
				sortOrder, accountNode);

		ResultSet rowUriResultSet = rdfStoreManager.querySelect(rowUriQuery);

		// need link hashset here to preserve order.
		Set<String> rowUris = new LinkedHashSet<String>();


		while (rowUriResultSet.hasNext()) {
			QuerySolution row = rowUriResultSet.next();
			String rowUri = row.get(QueryToolConstants.ROW_VAR.getName()).toString();
			rowUris.add(rowUri);
		}

		return rowUris;
	}

	public int getRowCount(FormResult form, Node accountNode) {
		Query query = InstancedDataQueryGenerator.generateSingleFormRowCountQuery(form, accountNode);

		ResultSet result = rdfStoreManager.querySelect(query);
		QuerySolution row = result.next();
		String rowCountString = InstancedDataUtil.rdfNodeToString(row.get("count"));

		return Integer.valueOf(rowCountString);
	}

	public int getRowCount(List<FormResult> selectedForms, Node accountNode) {
		long startTime = System.nanoTime();

		if (selectedForms.isEmpty()) {
			return 0;
		}

		ResultSet rowCountResult = rdfStoreManager
				.querySelect(InstancedDataQueryGenerator.generateJoinedFormRowCountQuery(selectedForms, accountNode));


		if (rowCountResult.hasNext()) {
			QuerySolution row = rowCountResult.next();
			String rowCountString = InstancedDataUtil.rdfNodeToString(row.get("count"));

			long endTime = System.nanoTime();
			log.info("Time to load row count: " + ((endTime - startTime) / 1000000) + "ms");

			return Integer.valueOf(rowCountString);
		} else {
			throw new InstancedDataException("No result returned for row count");
		}
	}

	/**
	 * Loads all of the InstancedRows into the cache from the given form
	 * 
	 * @param form
	 * @return
	 */
	public InstancedDataFormCache loadAll(FormResult form, CodeMapping codeMapping, Node accountNode,
			boolean forDownload) {

		InstancedDataFormCache formCache = new InstancedDataFormCache();

		Query query = InstancedDataQueryGenerator.getInstancedDataQuery(form, accountNode, forDownload);

		// query database
		ResultSet instancedDataResult = rdfStoreManager.querySelect(query);
		Map<RepeatableGroup, String> groupVariableMap = InstancedDataUtil.getGroupVariableMap(form);

		// parse each result row into InstancedRow
		while (instancedDataResult.hasNext()) {
			QuerySolution row = instancedDataResult.next();
			String rowUri = row.get("row").toString();
			String submissionId = InstancedDataUtil.trimRdfType(row.get("submission").toString());

			// get the dataset id
			String dataSetIdString = InstancedDataUtil.trimRdfType(row.get("dsId").toString());
			Long datasetId = Long.valueOf(dataSetIdString);
			String studyId = InstancedDataUtil.trimRdfType(row.get("studyId").toString());
			String studyName = InstancedDataUtil.trimRdfType(row.get("study").toString());
			String readableDatasetId = InstancedDataUtil.trimRdfType(row.get("prefixedId").toString());
			String datasetName = InstancedDataUtil.trimRdfType(row.get("datasetName").toString());
			String studyPrefixedId = InstancedDataUtil
					.trimRdfType(row.get(QueryToolConstants.STUDY_PREFIXED_ID_VAR.getName()).toString());
			RDFNode guidNode = row.get(QueryToolConstants.GUID_LABEL_VAR.getName());

			String guid = guidNode != null ? guidNode.toString() : "";

			// get the boolean flag for if the current row should be highlighted or not. Not having a value for this
			// flag should be equivalent to the flag being false.
			RDFNode doHightlightNode = row.get(QueryToolConstants.DO_HIGHLIGHT_VAR.getName());
			boolean doHighlight =
					doHightlightNode != null && doHightlightNode.toString().equalsIgnoreCase("true") ? true : false;

			// see if the record already exists in the map
			InstancedRow instancedRow = new InstancedRow(rowUri, submissionId, datasetId, readableDatasetId, studyId,
					studyPrefixedId, studyName, datasetName, "", doHighlight, guid, form.getShortNameAndVersion());

			formCache.putRow(instancedRow);

			List<RepeatableGroup> rgList = null;

			if (forDownload) {
				rgList = form.getSelectedRepeatableGroups();
			} else {
				rgList = form.getRepeatableGroups();
			}

			for (RepeatableGroup group : rgList) {
				if (!group.doesRepeat()) {

					List<DataElement> deList = null;

					if (forDownload) {
						deList = group.getSelectedElements();
					} else {
						deList = group.getDataElements();
					}

					for (DataElement dataElement : deList) {
						Var deVariable =
								InstancedDataUtil.getDataElementVar(groupVariableMap, form, group, dataElement);
						String deValue = InstancedDataUtil.rdfNodeToString(row.get(deVariable.getName()));
						DataTableColumn column = new DataTableColumn(form.getShortNameAndVersion(), group.getName(),
								dataElement.getName());

						if (deValue != null) {
							CellValueCode valueCode = createValueCode(codeMapping, dataElement, deValue);
							CellValue cellValue = new NonRepeatingCellValue(dataElement.getType(), valueCode);

							instancedRow.insertCell(column, cellValue);
						} else {
							CellValue cellValue =
									new NonRepeatingCellValue(dataElement.getType(), QueryToolConstants.EMPTY_STRING);
							instancedRow.insertCell(column, cellValue);
						}
					}
				} else {
					DataTableColumn column = new DataTableColumn(form.getShortNameAndVersion(), group.getName(), null);
					CellValue cellValue = new RepeatingCellValue(group.getSelectedElements().size());
					instancedRow.insertCell(column, cellValue);
				}
			}
		}


		return formCache;
	}

	/**
	 * Returns the result of the join with only the row URIs and the GUIDs they're joined on The variables are going to
	 * rowUri0, rowUri1, ..., rowUriN, guid
	 * 
	 * @param offset
	 * @param limit
	 * @param sortColumn
	 * @param sortOrder
	 * @return
	 */
	public ResultSet getJoinedRowUrisToLoad(List<FormResult> selectedForms, int offset, int limit,
			DataTableColumn sortColumn, String sortOrder, Node accountNode) {
		Query query = InstancedDataQueryGenerator.generateJoinUriQuery(selectedForms, offset, limit, sortColumn,
				sortOrder, accountNode);
		return rdfStoreManager.querySelect(query);
	}

	/**
	 * returns a multimap of the repeatable group column to a list of row uris which for the repeatable group, only has
	 * one row of data * @param form
	 * 
	 * @param rowUris
	 * @return
	 */
	public Map<CellPosition, Integer> getRepeatableGroupRowCounts(FormResult form, Set<String> rowUris) {


		// a map of the repeatable group cell position to the number of rows in that repeatable group
		Map<CellPosition, Integer> repeatableGroupRowCountMap = new HashMap<CellPosition, Integer>();

		if (!form.getRepeatingRepeatableGroups().isEmpty()) {
			if (rowUris != null && !rowUris.isEmpty()) {
				Query query = InstancedDataQueryGenerator.generateRepeatableGroupRowCountsQuery(form, rowUris);
				ResultSet resultSet = rdfStoreManager.querySelect(query);

				while (resultSet.hasNext()) {
					QuerySolution row = resultSet.next();
					String rowUri = row.get(QueryToolConstants.ROW_VARIABLE.getName()).toString();
					String rgUri = row.get(QueryToolConstants.REPEATABLE_GROUP_VARIABLE.getName()).toString();
					String countString = row.get(QueryToolConstants.COUNT_VARIABLE.getName()).toString();
					countString = InstancedDataUtil.trimRdfType(countString);

					RepeatableGroup currentGroup = form.getRepeatableGroupByUri(rgUri);

					if (currentGroup == null) {
						throw new InstancedDataException("Repeatable group URI, " + rgUri
								+ " does not match any repeatable groups in the current form.");
					}

					if (currentGroup.doesRepeat()) {
						DataTableColumn column = InstancedDataUtil.getColumnFromRepeatableGroup(form, currentGroup);

						CellPosition currentPosition = new CellPosition(column, rowUri);
						repeatableGroupRowCountMap.put(currentPosition, Integer.valueOf(countString));
					}
				}
			}
		}

		return repeatableGroupRowCountMap;
	}

	@Deprecated
	private Map<CellPosition, InstancedRepeatableGroupRow> getRepeatableGroupData(FormResult form,
			DataTableColumn column, List<String> uris, CodeMapping codeMapping, Node accountNode) {

		List<Query> queries =
				InstancedDataQueryGenerator.buildRepeatableGroupDataQueries(form, column, uris, accountNode);
		RepeatableGroup group = form.getGroupByName(column.getRepeatableGroup());
		String groupVariable = InstancedDataUtil.getGroupVariableMap(form).get(group);
		Map<CellPosition, InstancedRepeatableGroupRow> rowMap =
				new HashMap<CellPosition, InstancedRepeatableGroupRow>();

		for (Query query : queries) {
			ResultSet resultSet = rdfStoreManager.querySelect(query);

			while (resultSet.hasNext()) {
				QuerySolution row = resultSet.next();
				String rowUri = row.get(QueryToolConstants.ROW_VARIABLE.getName()).toString();
				InstancedRepeatableGroupRow currentRgRow = null;

				if (rowMap.get(rowUri) == null) {
					currentRgRow = new InstancedRepeatableGroupRow();
					rowMap.put(new CellPosition(column, rowUri), currentRgRow);
				} else {
					currentRgRow = rowMap.get(rowUri);
				}

				for (DataElement de : group.getSelectedElements()) {
					String deVariable = groupVariable + de.getName();
					RepeatingCellColumn currentColumn = new RepeatingCellColumn(form.getShortNameAndVersion(),
							group.getName(), de.getName(), de.getType());

					if (row.get(deVariable) != null) {
						// value of the data element
						String deValue = InstancedDataUtil.rdfNodeToString(row.get(deVariable));
						CellValueCode valueCode = codeMapping.getCellValueCode(de, deValue);
						currentRgRow.insertCell(currentColumn, valueCode);

					} else if (currentRgRow.getCellValue(currentColumn) == null) {
						currentRgRow.insertCell(currentColumn, QueryToolConstants.EMPTY_STRING);
					}
				}
			}
		}

		return rowMap;
	}

	public ListMultimap<CellPosition, InstancedRepeatableGroupRow> getRepeatableGroupData(FormResult form,
			List<CellPosition> repeatableGroupsToLoad, CodeMapping codeMapping, Node accountNode) {
		// we need to group the row URIs by the columns first in order to generate the queries more efficiently
		ListMultimap<DataTableColumn, String> columnToRowUriMap = ArrayListMultimap.create();

		ListMultimap<CellPosition, InstancedRepeatableGroupRow> loadedRepeatableGroupRows = ArrayListMultimap.create();

		for (CellPosition position : repeatableGroupsToLoad) {
			columnToRowUriMap.put(position.getColumn(), position.getRowUri());
		}

		for (DataTableColumn column : columnToRowUriMap.keySet()) {
			List<String> rowUris = columnToRowUriMap.get(column);

			if (rowUris != null) { // this should really never be null
				Map<CellPosition, InstancedRepeatableGroupRow> repeatableGroupRows =
						getRepeatableGroupData(form, column, rowUris, codeMapping, accountNode);

				for (Entry<CellPosition, InstancedRepeatableGroupRow> repeatableGroupEntry : repeatableGroupRows
						.entrySet()) {

					CellPosition position = repeatableGroupEntry.getKey();
					InstancedRepeatableGroupRow rgRow = repeatableGroupEntry.getValue();
					loadedRepeatableGroupRows.put(position, rgRow);
				}
			}
		}

		return loadedRepeatableGroupRows;
	}

	public List<InstancedRepeatableGroupRow> getSelectedRepeatableGroupInstancedData(FormResult form,
			String submissionId, RepeatableGroup group, CodeMapping codeMapping, Node accountNode) {

		TreeMap<Integer, InstancedRepeatableGroupRow> rowMap = new TreeMap<Integer, InstancedRepeatableGroupRow>();
		Map<RepeatableGroup, String> groupVariableMap = InstancedDataUtil.getGroupVariableMap(form);
		List<String> submissionIds = new ArrayList<String>();
		submissionIds.add(submissionId);

		List<String> queries =
				InstancedDataQueryGenerator.getInstancedRepeatableGroupQuery(form, submissionId, group, accountNode);

		for (String query : queries) {
			// query database
			ResultSet instancedDataResult = rdfStoreManager.querySelect(query);
			Integer rowCounter = 0;

			// parse each result row into InstancedRow
			while (instancedDataResult.hasNext()) {
				QuerySolution row = instancedDataResult.next();
				InstancedRepeatableGroupRow instancedRow = rowMap.get(rowCounter);

				if (instancedRow == null) {
					instancedRow = new InstancedRepeatableGroupRow();
				}

				// for each data element, insert the cell values
				for (DataElement de : group.getSelectedElements()) {
					String deVariable =
							InstancedDataUtil.getDataElementVar(groupVariableMap, form, group, de).getName();
					RepeatingCellColumn column = new RepeatingCellColumn(form.getShortNameAndVersion(), group.getName(),
							de.getName(), de.getType());

					if (row.get(deVariable) != null) {
						String deValue = InstancedDataUtil.rdfNodeToString(row.get(deVariable));

						CellValueCode valueCode = codeMapping.getCellValueCode(de, deValue);
						instancedRow.insertCell(column, valueCode);

					} else if (instancedRow.getCellValue(column) == null) {
						instancedRow.insertCell(column, QueryToolConstants.EMPTY_STRING);
					}
				}

				rowMap.put(rowCounter, instancedRow);
				rowCounter++;
			}
		}

		// return an arraylist
		return new ArrayList<InstancedRepeatableGroupRow>(rowMap.values());
	}


	public boolean hasHighlightedGuid(FormResult form, Node accountNode) {
		Query query = InstancedDataQueryGenerator.getHasHighlightedGuidQuery(form, accountNode);

		boolean hasHighlightedGuid = rdfStoreManager.queryAsk(query);

		return hasHighlightedGuid;
	}

	/**
	 * Given a list of repeatable uris, load the instanced repeatable group data
	 * 
	 * @param column
	 * @param uris
	 */
	public void loadRepeatableGroupRowsByUris(FormResult form, DataTableColumn column, List<String> uris,
			List<FormResult> selectedForms, CodeMapping codeMapping, Node accountNode,
			InstancedDataTable instancedDataTable) {
		List<Query> queries =
				InstancedDataQueryGenerator.buildRepeatableGroupDataQueries(column, uris, selectedForms, accountNode);
		RepeatableGroup group = form.getGroupByName(column.getRepeatableGroup());
		String groupVariable = InstancedDataUtil.getGroupVariableMap(form).get(group);
		Map<String, InstancedRepeatableGroupRow> rowMap = new HashMap<String, InstancedRepeatableGroupRow>();

		for (Query query : queries) {
			ResultSet resultSet = rdfStoreManager.querySelect(query);

			while (resultSet.hasNext()) {
				QuerySolution row = resultSet.next();
				String rowUri = row.get(QueryToolConstants.ROW_VARIABLE.getName()).toString();
				InstancedRepeatableGroupRow currentRgRow = null;

				RepeatingCellValue rcv = instancedDataTable.getRepeatingCellValueByUriAndColumn(form, column, rowUri);

				if (rcv != null && !rcv.isExpanded()) {
					if (rowMap.get(rowUri) == null) {
						// if repeatable group is already expanded, thenw e will need to clear the row to reload the
						// data
						rcv.getRows().clear();
						rcv.setExpanded(true);

						currentRgRow = new InstancedRepeatableGroupRow();
						rcv.addRow(currentRgRow);
						rowMap.put(rowUri, currentRgRow);
					} else {
						currentRgRow = rowMap.get(rowUri);
					}

					for (DataElement de : group.getDataElements()) {
						String deVariable = groupVariable + de.getName();
						RepeatingCellColumn currentColumn = new RepeatingCellColumn(form.getShortNameAndVersion(),
								group.getName(), de.getName(), de.getType());

						if (row.get(deVariable) != null) {
							// value of the data element
							String deValue = InstancedDataUtil.rdfNodeToString(row.get(deVariable));
							CellValueCode valueCode = createValueCode(codeMapping, de, deValue);
							currentRgRow.insertCell(currentColumn, valueCode);

						} else if (currentRgRow.getCellValue(currentColumn) == null) {
							currentRgRow.insertCell(currentColumn, QueryToolConstants.EMPTY_STRING);
						}
					}
				}
			}
		}
	}

	public ListMultimap<String, InstancedRepeatableGroupRow> getRepeatableGroupData(FormResult form, RepeatableGroup rg,
			Node accountNode, CodeMapping codeMapping) {
		ListMultimap<String, InstancedRepeatableGroupRow> rgDataMap = ArrayListMultimap.create();

		List<String> queries =
				InstancedDataQueryGenerator.getInstancedRepeatableGroupQuery(form, null, rg, accountNode);
		Map<RepeatableGroup, String> groupVariableMap = InstancedDataUtil.getGroupVariableMap(form);

		long startTime = System.nanoTime();
		// we have the current rg object from the map's keys
		log.info("Current group: " + rg.getName());
		for (String query : queries) {
			Map<String, Integer> submissionRowCounter = new HashMap<String, Integer>();

			ResultSet instancedDataResult = rdfStoreManager.querySelect(query);
			while (instancedDataResult.hasNext()) {
				QuerySolution row = instancedDataResult.next();

				String submissionId = InstancedDataUtil.trimRdfType(row.get("submission").toString());
				Integer rowCounter = submissionRowCounter.get(submissionId);

				if (rowCounter == null) {
					rowCounter = 0;
					submissionRowCounter.put(submissionId, rowCounter);
				}

				List<InstancedRepeatableGroupRow> instancedRows = rgDataMap.get(submissionId);
				InstancedRepeatableGroupRow currentRow = null;

				if (rowCounter < instancedRows.size()) {
					currentRow = instancedRows.get(rowCounter);
				} else {
					currentRow = new InstancedRepeatableGroupRow();
					rgDataMap.put(submissionId, currentRow);
				}

				for (DataElement dataElement : rg.getSelectedElements()) {
					String deVariable =
							InstancedDataUtil.getDataElementVar(groupVariableMap, form, rg, dataElement).getName();
					RepeatingCellColumn column = new RepeatingCellColumn(form.getShortNameAndVersion(), rg.getName(),
							dataElement.getName(), dataElement.getType());

					if (row.get(deVariable) != null) {
						// value of the data element
						String deValue = InstancedDataUtil.rdfNodeToString(row.get(deVariable));

						// insert value into the instancedRgRow
						CellValueCode valueCode = createValueCode(codeMapping, dataElement, deValue);
						currentRow.insertCell(column, valueCode);
					}
					// if the column hasn't had a value inserted then we insert empty string
					else if (currentRow.getCellValue(column) == null) {
						currentRow.insertCell(column, QueryToolConstants.EMPTY_STRING);
					}
				}

				submissionRowCounter.put(submissionId, ++rowCounter);
			}

		}

		long endTime = System.nanoTime();
		log.info("Time to load repeatable group: " + ((endTime - startTime) / 1000000) + "ms");

		return rgDataMap;
	}

	public List<DataElement> getDataElementsForRepeatableGroup(FormResult form) {

		String groupQuery = InstancedDataQueryGenerator.getRepeatableGroupQuery(form.getUri());

		// first, get a list of all data elements belonging to our set of repeatable groups
		ResultSet resultSet = rdfStoreManager.querySelect(groupQuery);

		ResultSetToDataElement rsToDe = new ResultSetToDataElement();
		List<DataElement> dataElements = null;

		try {
			dataElements = rsToDe.getBeans(resultSet);
		} catch (Exception e1) {
			log.error("Could not get data elements from result set.", e1);
			dataElements = new ArrayList<DataElement>();
		}

		return dataElements;
	}

	public void addDataElementsToRepeatableGroups(FormResult form, List<DataElement> dataElements) {
		// this result set represents a mapping of data elements to repeatable groups with only uri's
		ResultSet resultSet =
				rdfStoreManager.querySelect(InstancedDataQueryGenerator.getDataElementInGroupSimple(form.getUri()));

		List<RepeatableGroup> formRgList = form.getRepeatableGroups();

		while (resultSet.hasNext()) {
			QuerySolution row = resultSet.next();
			String groupUri = row.get("rg").toString();
			String deUri = row.get("uri").toString();
			String groupName = row.get("rg_name").toString();
			String rgPosition = row.get("rg_position").toString();
			String dePosition = row.get("de_position").toString();
			String rgType = row.get("rg_type").toString();
			String rgThreshold = row.get("rg_threshold").toString();

			if (form.containsGroup(groupUri)) {
				search: for (RepeatableGroup group : formRgList) {
					if (groupUri.equals(group.getUri())) {
						for (DataElement dataElement : dataElements) {
							if (deUri.equals(dataElement.getUri())) {
								if (!group.containsDataElement(deUri)) {
									MetaDataCache.setRgDePosition(group, dataElement, Integer.valueOf(dePosition));
									group.addDataElement(dataElement);
								}

								break search;
							}
						}
					}
				}
			} else {
				RepeatableGroup newGroup = new RepeatableGroup();
				newGroup.setName(groupName);
				newGroup.setPosition(rgPosition);
				newGroup.setType(rgType);
				newGroup.setThreshold(Integer.valueOf(rgThreshold));

				for (DataElement dataElement : dataElements) {
					if (deUri.equals(dataElement.getUri())) {
						newGroup.setUri(groupUri);
						MetaDataCache.setRgDePosition(newGroup, dataElement, Integer.valueOf(dePosition));
						newGroup.addDataElement(dataElement);
						break;
					}
				}

				formRgList.add(newGroup);
			}
		}
	}


	public CellValueCode createValueCode(CodeMapping codeMapping, DataElement de, String deValue) {
		CellValueCode valueCode = null;

		if (codeMapping != null) {
			valueCode = codeMapping.getCellValueCode(de, deValue);
		} else {
			valueCode = new CellValueCode(deValue);
		}

		return valueCode;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<DataTableColumnWithUri> getColumnsWithData(FormResult currentForm, Node accountNode) {
		Set<DataTableColumnWithUri> columnsWithData = new HashSet<DataTableColumnWithUri>();

		Query query = InstancedDataQueryGenerator.getDataColumnHasDataQuery(currentForm, accountNode);
		ResultSet result = rdfStoreManager.querySelect(query);

		String formUri = currentForm.getUri();
		String formName = currentForm.getShortName();

		while (result.hasNext()) {
			QuerySolution row = result.next();
			String rgUri = InstancedDataUtil
					.trimRdfType(row.get(QueryToolConstants.REPEATABLE_GROUP_VARIABLE.getName()).toString());
			String rgName = InstancedDataUtil
					.trimRdfType(row.get(QueryToolConstants.REPEATABLE_GROUP_NAME_VARIABLE.getName()).toString());
			String deUri = InstancedDataUtil
					.trimRdfType(row.get(QueryToolConstants.DATA_ELEMENT_VARIABLE.getName()).toString());
			String deName = InstancedDataUtil
					.trimRdfType(row.get(QueryToolConstants.DATA_ELEMENT_NAME_VARIABLE.getName()).toString());

			DataTableColumnWithUri dataTableColumn = new DataTableColumnWithUri();
			dataTableColumn.setForm(formName);
			dataTableColumn.setFormUri(formUri);
			dataTableColumn.setRepeatableGroup(rgName);
			dataTableColumn.setRepeatableGroupUri(rgUri);
			dataTableColumn.setDataElement(deName);
			dataTableColumn.setDataElementUri(deUri);
			columnsWithData.add(dataTableColumn);
		}

		return columnsWithData;
	}

	/**
	 * {@inheritDoc}
	 */
	public void loadRgDataByColumns(FormResult form, List<RepeatingCellColumn> columnsToLoad,
			InstancedDataFormCache formCache, Node accountNode) {

		if (columnsToLoad.isEmpty()) {
			return;
		}

		ListMultimap<String, RepeatingCellColumn> rgNameToColumnMap = ArrayListMultimap.create();

		for (RepeatingCellColumn rgColumn : columnsToLoad) {
			String rgName = rgColumn.getRepeatableGroup();
			rgNameToColumnMap.put(rgName, rgColumn);
		}

		for (Entry<String, Collection<RepeatingCellColumn>> rgEntry : rgNameToColumnMap.asMap().entrySet()) {
			String rgName = rgEntry.getKey();
			RepeatableGroup rg = form.getRepeatableGroupByName(rgName);
			List<RepeatingCellColumn> rgColumns = new ArrayList<>(rgEntry.getValue());
			DataTableColumn tableColumn = new DataTableColumn(form.getShortNameAndVersion(), rgName, null);
			Query query =
					InstancedDataQueryGenerator.loadByRepeatingColumnQuery(form, tableColumn, rgColumns, accountNode);

			ResultSet rs = rdfStoreManager.querySelect(query);

			while (rs.hasNext()) {
				QuerySolution row = rs.next();

				String rowUri = InstancedDataUtil.trimRdfType(row.get(QueryToolConstants.ROW_VAR.getName()).toString());
				InstancedRow instancedRow = formCache.getByRowUri(rowUri);

				if (instancedRow != null) {

					CellValue cv = instancedRow.getCellValue(tableColumn);

					if (cv.getIsRepeating()) {
						RepeatingCellValue rcv = (RepeatingCellValue) cv;

						if (!rcv.isExpanded()) {
							InstancedRepeatableGroupRow rgRow = new InstancedRepeatableGroupRow();
							for (DataElement de : rg.getDataElements()) {
								RepeatingCellColumn currentColumn = new RepeatingCellColumn(
										form.getShortNameAndVersion(), rgName, de.getName(), de.getType());

								if (rgColumns.contains(currentColumn)) {
									String rgValue = InstancedDataUtil
											.trimRdfType(row.get(currentColumn.getDataElement()).toString());
									rgRow.insertCell(currentColumn, rgValue);
								} else {
									rgRow.insertCell(currentColumn, QueryToolConstants.EMPTY_STRING);
								}
							}

							rcv.addRow(rgRow);
						}
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getAllBiosampleIdsInverse(FormResult biosampleForm, Set<String> unselectedRowUri,
			Node accountNode) {
		Set<String> biosampleIds = new HashSet<>();

		Query query =
				InstancedDataQueryGenerator.getBiosampleQueryInverse(biosampleForm, unselectedRowUri, accountNode);

		ResultSet rs = rdfStoreManager.querySelect(query);

		while (rs.hasNext()) {
			QuerySolution row = rs.next();
			String currentBiosampleId = row.get(QueryToolConstants.BIOSAMPLE_VAR_NAME).toString();
			biosampleIds.add(currentBiosampleId);
		}

		return biosampleIds;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getRowUrisByBiosampleIdInverse(FormResult biosampleForm, Set<String> unselectedRowUri,
			Node accountNode) {
		Set<String> biosampleIds = new HashSet<>();

		Query query =
				InstancedDataQueryGenerator.getBiosampleQueryInverse(biosampleForm, unselectedRowUri, accountNode);

		ResultSet rs = rdfStoreManager.querySelect(query);

		while (rs.hasNext()) {
			QuerySolution row = rs.next();
			String currentBiosampleId = row.get(QueryToolConstants.ROW_VAR.getName()).toString();
			biosampleIds.add(currentBiosampleId);
		}

		return biosampleIds;
	}
}
