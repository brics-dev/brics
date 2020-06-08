package gov.nih.tbi.dao.impl;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;

import gov.nih.tbi.commons.model.RequiredType;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.dao.InstancedDataDao;
import gov.nih.tbi.exceptions.InstancedDataException;
import gov.nih.tbi.pojo.CellPosition;
import gov.nih.tbi.pojo.CodeMapping;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.DataTableColumnWithUri;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.pojo.PermissibleValue;
import gov.nih.tbi.pojo.QueryResult;
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
import gov.nih.tbi.util.ResultSetToPermissibleValue;

@Repository
@Transactional
public class InstancedDataDaoImpl implements InstancedDataDao, Serializable {

	private static final long serialVersionUID = 6999452529415924840L;
	private static final Logger log = LogManager.getLogger(InstancedDataDaoImpl.class.getName());

	@Autowired
	RDFStoreManager rdfStoreManager;

	@Autowired
	MetaDataCache metaDataCache;

	public InstancedDataDaoImpl() {}

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
		QueryResult instancedDataResult = rdfStoreManager.querySelect(query);
		Map<RepeatableGroup, String> groupVariableMap = InstancedDataUtil.getGroupVariableMap(form);

		// parse each result row into InstancedRow
		for (QuerySolution row : instancedDataResult.getQueryData()) {
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

			String guid = guidNode != null ? guidNode.toString() : QueryToolConstants.EMPTY_STRING;

			if (QueryToolConstants.EMPTY_STRING.equals(guid)) {
				form.setHasGuidData(false);
			}

			// get the boolean flag for if the current row should be highlighted or not. Not
			// having a value for this
			// flag should be equivalent to the flag being false.
			RDFNode doHightlightNode = row.get(QueryToolConstants.DO_HIGHLIGHT_VAR.getName());
			boolean doHighlight =
					doHightlightNode != null && doHightlightNode.toString().equalsIgnoreCase("true") ? true : false;

			RDFNode mdsUpdrsXNode = row.get(QueryToolConstants.MDS_UPDRS_X_VAR.getName());
			boolean inMdsUpdrsX =
					mdsUpdrsXNode != null && mdsUpdrsXNode.toString().equalsIgnoreCase("true") ? true : false;

			// see if the record already exists in the map
			InstancedRow instancedRow =
					new InstancedRow(rowUri, submissionId, datasetId, readableDatasetId, studyId, studyPrefixedId,
							studyName, datasetName, "", doHighlight, guid, form.getShortNameAndVersion(), inMdsUpdrsX);

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
						DataTableColumn column = form.getColumnFromString(form.getShortNameAndVersion(),
								group.getName(), dataElement.getName());

						if (deValue != null) {
							CellValueCode valueCode = createValueCode(codeMapping, dataElement, deValue);
							CellValue cellValue =
									formCache.internCell(new NonRepeatingCellValue(dataElement.getType(), valueCode));

							instancedRow.insertCell(column, cellValue);
						} else {
							CellValue cellValue = formCache.internCell(
									new NonRepeatingCellValue(dataElement.getType(), QueryToolConstants.EMPTY_STRING));
							instancedRow.insertCell(column, cellValue);
						}
					}
				} else {
					DataTableColumn column =
							form.getColumnFromString(form.getShortNameAndVersion(), group.getName(), null);
					CellValue cellValue = new RepeatingCellValue(group.getSelectedElements().size());
					instancedRow.insertCell(column, cellValue);
				}
			}
		}

		return formCache;
	}

	/**
	 * returns a multimap of the repeatable group column to a list of row uris which for the repeatable group, only has
	 * one row of data * @param form
	 * 
	 * @param rowUris
	 * @return
	 */
	public Map<CellPosition, Integer> getRepeatableGroupRowCounts(FormResult form, Set<String> rowUris) {

		// a map of the repeatable group cell position to the number of rows in that
		// repeatable group
		Map<CellPosition, Integer> repeatableGroupRowCountMap = new HashMap<CellPosition, Integer>();

		if (!form.getRepeatingRepeatableGroups().isEmpty()) {
			if (rowUris != null && !rowUris.isEmpty()) {
				Query query = InstancedDataQueryGenerator.generateRepeatableGroupRowCountsQuery(form, rowUris);
				QueryResult resultSet = rdfStoreManager.querySelect(query);

				for (QuerySolution row : resultSet.getQueryData()) {
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
			QueryResult instancedDataResult = rdfStoreManager.querySelect(query);
			Integer rowCounter = 0;

			// parse each result row into InstancedRow
			for (QuerySolution row : instancedDataResult.getQueryData()) {
				InstancedRepeatableGroupRow instancedRow = rowMap.get(rowCounter);

				if (instancedRow == null) {
					instancedRow = new InstancedRepeatableGroupRow();
				}

				// for each data element, insert the cell values
				for (DataElement de : group.getSelectedElements()) {
					String deVariable =
							InstancedDataUtil.getDataElementVar(groupVariableMap, form, group, de).getName();
					RepeatingCellColumn column =
							(RepeatingCellColumn) form.getColumnFromString(form.getShortNameAndVersion(),
									group.getName(), de.getName(), de.getType());

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

		// saving the row uris we are manipulating, so we can set them to
		// expanded at the end of this method.
		Set<String> touchedRows = new HashSet<String>();

		for (Query query : queries) {
			QueryResult resultSet = rdfStoreManager.querySelect(query);

			for (QuerySolution row : resultSet.getQueryData()) {
				String rowUri = row.get(QueryToolConstants.ROW_VARIABLE.getName()).toString();
				InstancedRepeatableGroupRow currentRgRow = null;

				RepeatingCellValue rcv = instancedDataTable.getRepeatingCellValueByUriAndColumn(form, column, rowUri);

				if (rcv != null && !rcv.isExpanded()) {
					touchedRows.add(rowUri);

					if (rowMap.get(rowUri) == null) {
						// if repeatable group is already expanded, thenw e will need to clear the row
						// to reload the
						// data
						rcv.getRows().clear();
						currentRgRow = new InstancedRepeatableGroupRow();
						rcv.addRow(currentRgRow);
						rowMap.put(rowUri, currentRgRow);
					} else {
						currentRgRow = rowMap.get(rowUri);
					}

					for (DataElement de : group.getDataElements()) {
						String deVariable = groupVariable + de.getName();
						RepeatingCellColumn currentColumn =
								(RepeatingCellColumn) form.getColumnFromString(form.getShortNameAndVersion(),
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

		for (String touchedRowUri : touchedRows) {
			RepeatingCellValue rcv =
					instancedDataTable.getRepeatingCellValueByUriAndColumn(form, column, touchedRowUri);
			rcv.setExpanded(true);
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

			QueryResult instancedDataResult = rdfStoreManager.querySelect(query);
			for (QuerySolution row : instancedDataResult.getQueryData()) {
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
					RepeatingCellColumn column =
							(RepeatingCellColumn) form.getColumnFromString(form.getShortNameAndVersion(), rg.getName(),
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

		// first, get a list of all data elements belonging to our set of repeatable
		// groups
		QueryResult resultSet = rdfStoreManager.querySelect(groupQuery);

		ResultSetToDataElement rsToDe = new ResultSetToDataElement();
		List<DataElement> dataElements = null;

		try {
			dataElements = rsToDe.getBeans(resultSet);
		} catch (Exception e1) {
			log.error("Could not get data elements from result set.", e1);
			dataElements = new ArrayList<DataElement>();
		}

		insertPermissibleValues(dataElements);

		return dataElements;
	}

	/**
	 * Populate data elements with the complete permissibleValue objects
	 * 
	 * @param dataElements
	 */
	protected void insertPermissibleValues(List<DataElement> dataElements) {
		if (dataElements == null || dataElements.isEmpty()) {
			return;
		}

		// URIs of the permissible values we are interested in completing
		List<String> permissibleValueUris = dataElements.stream().flatMap(de -> de.getPermissibleValues().stream())
				.collect(Collectors.toList()).stream().map(p -> p.getUri()).collect(Collectors.toList());

		Query pvQuery = InstancedDataQueryGenerator.getPermissibleValueQuery(permissibleValueUris);
		QueryResult resultSet = rdfStoreManager.querySelect(pvQuery);
		ResultSetToPermissibleValue rsToPv = new ResultSetToPermissibleValue();

		List<PermissibleValue> permissibleValues = null;

		try {
			permissibleValues = rsToPv.getBeans(resultSet);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException e) {
			log.error("Error occurred while parsing result set to PermissibleValues", e);
		}

		// this is a map of permissible value URIs to permissible value objects
		Map<String, PermissibleValue> permissibleValueUriMap =
				permissibleValues.stream().collect(Collectors.toMap(PermissibleValue::getUri, p -> p));

		// iterate through the permissible values and set the fields
		for (DataElement de : dataElements) {
			for (PermissibleValue pv : de.getPermissibleValues()) {
				PermissibleValue completePv = permissibleValueUriMap.get(pv.getUri());
				if (completePv != null) {
					pv.setValueLiteral(completePv.getValueLiteral());
					pv.setValueDescription(completePv.getValueDescription());
				}
			}
		}
	}

	public void addDataElementsToRepeatableGroups(FormResult form, List<DataElement> dataElements) {
		// this result set represents a mapping of data elements to repeatable groups
		// with only uri's
		QueryResult resultSet =
				rdfStoreManager.querySelect(InstancedDataQueryGenerator.getDataElementInGroupSimple(form.getUri()));

		List<RepeatableGroup> formRgList = form.getRepeatableGroups();

		for (QuerySolution row : resultSet.getQueryData()) {

			String groupUri = row.get(QueryToolConstants.RG_VAR.getName()).toString();
			String deUri = row.get(QueryToolConstants.DATA_ELEMENT_VARIABLE.getName()).toString();
			String groupName = row.get(QueryToolConstants.RG_NAME_VARIABLE.getName()).toString();
			String rgPosition = row.get(QueryToolConstants.RG_POSITION_VARIABLE.getName()).toString();
			String dePosition = row.get(QueryToolConstants.DE_POSITION_VARIABLE.getName()).toString();
			String rgType = row.get(QueryToolConstants.RG_TYPE_VARIABLE.getName()).toString();
			String rgThreshold = row.get(QueryToolConstants.RG_THRESHOLD_VARIABLE.getName()).toString();
			String requiredTypeString = row.get(QueryToolConstants.REQUIRED_TYPE_VARIABLE.getName()).toString();

			if (form.containsGroup(groupUri)) {
				search: for (RepeatableGroup group : formRgList) {
					if (groupUri.equals(group.getUri())) {
						for (DataElement dataElement : dataElements) {
							if (deUri.equals(dataElement.getUri())) {
								if (!group.containsDataElement(deUri)) {
									if (requiredTypeString != null) {
										RequiredType requiredType = RequiredType.valueOf(requiredTypeString);
										dataElement.setRequiredType(requiredType);
									}
									metaDataCache.setRgDePosition(group, dataElement, Integer.valueOf(dePosition));
									group.addDataElement(metaDataCache, dataElement);
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
						metaDataCache.setRgDePosition(newGroup, dataElement, Integer.valueOf(dePosition));

						if (requiredTypeString != null) {
							RequiredType requiredType = RequiredType.valueOf(requiredTypeString);
							dataElement.setRequiredType(requiredType);
						}

						newGroup.addDataElement(metaDataCache, dataElement);
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
		QueryResult result = rdfStoreManager.querySelect(query);

		String formUri = currentForm.getUri();
		String formName = currentForm.getShortName();

		for (QuerySolution row : result.getQueryData()) {
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
			DataTableColumn tableColumn = form.getColumnFromString(form.getShortNameAndVersion(), rgName, null);
			Query query =
					InstancedDataQueryGenerator.loadByRepeatingColumnQuery(form, tableColumn, rgColumns, accountNode);

			QueryResult rs = rdfStoreManager.querySelect(query);

			for (InstancedRow row : formCache.getRowUriMap().values()) {
				CellValue cv = row.getCellValue(tableColumn);
				if (cv.getIsRepeating()) {
					RepeatingCellValue rcv = (RepeatingCellValue) cv;
					if (!rcv.isExpanded()) {
						rcv.getRows().clear();
					}
				}
			}

			for (QuerySolution row : rs.getQueryData()) {
				String rowUri = InstancedDataUtil.trimRdfType(row.get(QueryToolConstants.ROW_VAR.getName()).toString());
				InstancedRow instancedRow = formCache.getByRowUri(rowUri);

				if (instancedRow != null) {

					CellValue cv = instancedRow.getCellValue(tableColumn);

					if (cv.getIsRepeating()) {
						RepeatingCellValue rcv = (RepeatingCellValue) cv;

						if (!rcv.isExpanded()) {
							InstancedRepeatableGroupRow rgRow = new InstancedRepeatableGroupRow();
							for (DataElement de : rg.getDataElements()) {
								RepeatingCellColumn currentColumn =
										(RepeatingCellColumn) form.getColumnFromString(form.getShortNameAndVersion(),
												rgName, de.getName(), de.getType());

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
}
