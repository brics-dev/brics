package gov.nih.tbi.service.impl;

import gov.nih.tbi.commons.util.ValUtil;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.dao.InstancedDataDao;
import gov.nih.tbi.exceptions.FilterEvaluatorException;
import gov.nih.tbi.exceptions.InstancedDataException;
import gov.nih.tbi.filter.DataElementFilter;
import gov.nih.tbi.filter.Filter;
import gov.nih.tbi.pojo.CellPosition;
import gov.nih.tbi.pojo.CodeMapping;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.DataTableColumnWithUri;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.pojo.RepeatableGroupExpansionTracker;
import gov.nih.tbi.repository.model.CellValue;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.FormHeader;
import gov.nih.tbi.repository.model.InstancedRecord;
import gov.nih.tbi.repository.model.InstancedRepeatableGroupRow;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.RepeatingCellColumn;
import gov.nih.tbi.repository.model.RepeatingCellValue;
import gov.nih.tbi.service.InstancedDataManager;
import gov.nih.tbi.service.cache.InstancedDataCache;
import gov.nih.tbi.service.cache.InstancedDataCacheUtils;
import gov.nih.tbi.service.cache.InstancedDataFormCache;
import gov.nih.tbi.service.join.Joiner;
import gov.nih.tbi.util.InstancedDataUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.hp.hpl.jena.graph.Node;

/**
 * ManagedBean that handles the business logic of InstancedDataTable Warning: please do not pass DataCart object to this
 * class directly!
 * 
 * @author jim3
 *
 */
@Component
@Scope("application")
public class InstancedDataManagerImpl implements InstancedDataManager, Serializable {

	private static final long serialVersionUID = 8051662464348857141L;
	private static final Logger log = Logger.getLogger(InstancedDataManagerImpl.class);

	@Autowired
	InstancedDataDao instancedDataDao;

	public void seedFormDataElements(FormResult form) {

		if (ValUtil.isCollectionEmpty(form.getRepeatableGroups())) {
			log.info("Loading seeding data element data for " + form.getShortName());
			List<DataElement> dataElements = instancedDataDao.getDataElementsForRepeatableGroup(form);
			instancedDataDao.addDataElementsToRepeatableGroups(form, dataElements);

			// set all data elements to selected ???? maybe unnecessary
			for (RepeatableGroup group : form.getRepeatableGroups()) {
				for (DataElement de : group.getDataElements()) {
					de.setSelected(true);
				}
			}

			Collections.sort(form.getRepeatableGroups());
		}
	}

	public InstancedDataTable buildInstancedDataTable(List<FormResult> selectedForms, int offset, int limit,
			DataTableColumn sortColumn, String sortOrder, InstancedDataCache instancedDataCache,
			CodeMapping codeMapping, String userName, boolean forDownload, boolean doApplyFilter,
			String filterExpression) throws FilterEvaluatorException {

		InstancedDataTable instancedDataTable = new InstancedDataTable();
		long startTime = System.nanoTime();

		Node accountNode = InstancedDataUtil.getAccountNode(userName);
		boolean isJoined = InstancedDataUtil.isJoined(selectedForms);

		instancedDataTable.setOffset(offset);
		instancedDataTable.setLimit(limit);
		instancedDataTable.setSortColumn(sortColumn);
		instancedDataTable.setSortOrder(sortOrder);
		instancedDataTable.setJoined(isJoined);
		instancedDataTable.setFilterExpression(filterExpression);
		instancedDataTable.setForms(selectedForms);

		// if we're querying down to less data than the current selected page can show,
		// reset to first page
		if (instancedDataTable.getRowCount() < offset) {
			instancedDataTable.setOffset(0);
		}

		buildDataTableHeaders(selectedForms, instancedDataTable, forDownload);

		if (limit > 0) {
			buildDataTableData(selectedForms, instancedDataTable, instancedDataCache, codeMapping, accountNode,
					forDownload, doApplyFilter, filterExpression);
		}

		instancedDataTable.setRowCount(instancedDataCache.getCachedRowCount());

		long endTime = System.nanoTime();
		log.info("Time to load data table: " + ((endTime - startTime) / 1000000) + "ms");

		return instancedDataTable;
	}

	// Creates and set the header for InstancedDataTable
	private void buildDataTableHeaders(List<FormResult> selectedForms, InstancedDataTable instancedDataTable,
			boolean forDownload) {

		List<FormHeader> formHeaders = new ArrayList<FormHeader>();

		if (!instancedDataTable.isJoined()) {
			FormResult form = selectedForms.get(0);
			if (form != null) {
				formHeaders.add(InstancedDataUtil.getInitialFormHeader(form, forDownload));
			}
		} else {
			for (FormResult form : selectedForms) {
				InstancedDataUtil.selectGuidElements(form);
				formHeaders.add(InstancedDataUtil.getJoinedInitialFormHeader(form, forDownload));
			}
		}

		instancedDataTable.setHeaders(formHeaders);
	}

	// Run the query and load each record to populate InstancedDataTable cells.
	public void buildDataTableData(List<FormResult> selectedForms, InstancedDataTable instancedDataTable,
			InstancedDataCache instancedDataCache, CodeMapping codeMapping, Node accountNode, boolean forDownload,
			boolean applyFilter, String filterExpression) throws FilterEvaluatorException {

		int deTotalCount = 0;
		long startTime = System.nanoTime();

		for (FormResult selectedForm : selectedForms) {
			log.info(" Selected form :: " + selectedForm.getShortName() + " DE count :: "
					+ selectedForm.getDataElementCount());
			int formDECount = selectedForm.getDataElementCount();
			deTotalCount = deTotalCount + formDECount;
			log.info(" Total DE  " + deTotalCount);
		}

		if (applyFilter) {
			log.info("Filter Expression: " + filterExpression);
		}

		log.info(" Final Total Data Elements :: " + deTotalCount);

		if (instancedDataCache == null || instancedDataCache.isEmpty()) {
			instancedDataCache = buildInstancedRowCache(selectedForms, codeMapping, accountNode, forDownload);
		}

		loadInstancedRecord(selectedForms, instancedDataTable, instancedDataCache, codeMapping, accountNode,
				applyFilter, filterExpression);

		if (!instancedDataTable.isJoined()) {
			Set<RepeatableGroupExpansionTracker> rgExpTrackers = instancedDataTable.getRgExpandTrackers();
			if (rgExpTrackers != null && !rgExpTrackers.isEmpty()) {

				for (RepeatableGroupExpansionTracker rgExpandTracker : rgExpTrackers) {
					// need to check if the submission still exists in the records list
					for (InstancedRecord record : instancedDataTable.getInstancedRecords()) {

						if (record.hasSubmissionId(rgExpandTracker.getSubmissionId())) {
							loadRepeatableGroupRowsBySubmissionId(selectedForms, instancedDataTable,
									rgExpandTracker.getSubmissionId(), rgExpandTracker.getColumn(), codeMapping,
									accountNode);
							break;
						}
					}
				}
			}
		}

		long endTime = System.nanoTime();
		log.info("Time to build table: " + ((endTime - startTime) / 1000000) + "ms");
	}

	public InstancedDataCache buildInstancedRowCache(List<FormResult> selectedForms, CodeMapping codeMapping,
			Node accountNode, boolean forDownload) {

		long startTime = System.nanoTime();
		InstancedDataCache instancedDataCache = new InstancedDataCache();

		log.info("InstancedRowCache is empty, loading all the InstancedRows ...");
		if (selectedForms.size() == 1) {
			FormResult form = selectedForms.get(0);

			// cache all of the InstancedRows for the form into a Row URI -> InstancedRow
			// hashmap
			instancedDataCache.putFormCache(form.getShortNameAndVersion(),
					instancedDataDao.loadAll(form, codeMapping, accountNode, forDownload));

		} else {
			for (FormResult form : selectedForms) {
				instancedDataCache.putFormCache(form.getShortNameAndVersion(),
						instancedDataDao.loadAll(form, codeMapping, accountNode, forDownload));
			}
		}

		long endTime = System.nanoTime();
		log.info("Time to load all the InstancedRows: " + ((endTime - startTime) / 1000000) + "ms");

		return instancedDataCache;
	}

	/**
	 * Since filtering is done in memory instead of Virtuoso now, we will need to load repeatable group data piecewise
	 * when filters are applied. This method will load data for data elements that have filters applied to them, but
	 * only for the groups that have not yet been expanded.
	 * 
	 * @param selectedForms
	 * @param instancedDataCache
	 * @param accountNode
	 */
	private void loadDataForFilter(List<FormResult> selectedForms, InstancedDataCache instancedDataCache,
			Node accountNode) {

		for (FormResult form : selectedForms) {
			InstancedDataFormCache formCache = instancedDataCache.getByFormName(form.getShortNameAndVersion());
			List<RepeatingCellColumn> columnsToLoad = new ArrayList<>();
			for (Filter filter : form.getFilters()) {
				if (filter instanceof DataElementFilter) {
					DataElementFilter deFilter = (DataElementFilter) filter;

					if (deFilter.getGroup().doesRepeat()) {
						RepeatingCellColumn column = (RepeatingCellColumn) deFilter.getForm().getColumnFromString(
								deFilter.getForm().getShortNameAndVersion(), deFilter.getGroup().getName(),
								deFilter.getElement().getName(), deFilter.getElement().getType());

						columnsToLoad.add(column);
					}
				}
			}

			instancedDataDao.loadRgDataByColumns(form, columnsToLoad, formCache, accountNode);
		}
	}

	private void loadInstancedRecord(List<FormResult> selectedForms, InstancedDataTable instancedDataTable,
			InstancedDataCache instancedDataCache, CodeMapping codeMapping, Node accountNode, boolean doApplyFilter,
			String filterExpression) throws FilterEvaluatorException {
		long startTime = System.nanoTime();

		boolean isJoined = InstancedDataUtil.isJoined(selectedForms);

		// we want to refresh the result cache if it is empty or if a filter has just
		// been applied
		if (doApplyFilter || !instancedDataCache.isResultCached()) {
			if (!isJoined) {
				InstancedDataFormCache formCache =
						instancedDataCache.getByFormName(selectedForms.get(0).getShortNameAndVersion());

				List<InstancedRecord> records = new ArrayList<InstancedRecord>();

				if (formCache != null) {
					if (formCache.getRowUriMap() != null) {
						for (InstancedRow currentRow : formCache.getRowUriMap().values()) {
							InstancedRecord record = new InstancedRecord(currentRow.getSubmissionId());
							record.addSelectedRow(currentRow);
							if (record != null) {
								records.add(record);
							}
						}
					}
				}

				instancedDataCache.setResultCache(records);
			} else {
				Joiner joiner = new Joiner(selectedForms, instancedDataCache);
				instancedDataCache.setResultCache(joiner.doJoin());
			}
		}

		log.info("doApplyFilter: " + doApplyFilter);

		if (doApplyFilter) {
			loadDataForFilter(selectedForms, instancedDataCache, accountNode);
			InstancedDataCacheUtils.applyFilter(filterExpression, selectedForms, instancedDataCache);
		}

		// TODO: need to pass filter into this call at some point
		List<InstancedRecord> instancedRecords = InstancedDataCacheUtils.getPageData(instancedDataCache,
				instancedDataTable.getOffset(), instancedDataTable.getLimit(), instancedDataTable.getSortColumn(),
				instancedDataTable.getSortOrder());

		log.info("final join row count: " + instancedDataCache.getCachedRowCount());

		long endTime = System.nanoTime();
		log.info("Time to do join: " + ((endTime - startTime) / 1000000) + "ms");

		instancedDataTable.setInstancedRecords(instancedRecords);

		if (instancedDataTable.getLimit() != Integer.MAX_VALUE) {
			long rgStart = System.nanoTime();
			for (int i = 0; i < selectedForms.size(); i++) {
				FormResult form = selectedForms.get(i);
				Set<String> rowUris = new HashSet<String>();

				for (InstancedRecord instancedRecord : instancedRecords) {
					InstancedRow row = instancedRecord.getSelectedRows().get(i);
					if (row != null) {
						rowUris.add(row.getRowUri());
					}
				}

				if (rowUris != null && !rowUris.isEmpty()) {
					Map<CellPosition, Integer> repeatableGroupRowCountMap =
							instancedDataDao.getRepeatableGroupRowCounts(form, rowUris);

					insertRepeatableGroupRowCounts(instancedDataTable, form, repeatableGroupRowCountMap);

					List<CellPosition> singleRowCells = findSingleRowCell(repeatableGroupRowCountMap);

					if (!singleRowCells.isEmpty()) {
						loadRepeatableGroups(selectedForms, instancedDataTable, form, singleRowCells, codeMapping,
								accountNode);
					}
				}
			}

			long rgEnd = System.nanoTime();
			log.info("Time to load repeatable groups: " + ((rgEnd - rgStart) / 1000000) + "ms");
		}
	}

	public void loadRepeatableGroupRows(InstancedDataTable instancedDataTable, FormResult form, String rowUri,
			String rgName, CodeMapping codeMapping, String userName) {

		InstancedRow row = null;

		// search the records to get the row we are interested in loading the repeatable
		// group
		search: for (InstancedRecord record : instancedDataTable.getInstancedRecords()) {
			for (InstancedRow tmpRow : record.getSelectedRows()) {
				if (tmpRow != null && tmpRow.getRowUri().equals(rowUri)) {
					row = tmpRow;
					break search;
				}
			}
		}

		// this means the given uri is not on the current page
		if (row == null) {
			throw new InstancedDataException("The specified rowUri is not in the current page!");
		}

		DataTableColumn column = form.getColumnFromString(form.getShortNameAndVersion(), rgName, null);
		CellValue cellValue = row.getCellValue(column);

		// find the repeatable group object
		RepeatableGroup group = getRepeatableGroupUsingColumn(form, column);
		if (group != null && cellValue != null && cellValue instanceof RepeatingCellValue) {
			RepeatingCellValue rcv = (RepeatingCellValue) cellValue;
			rcv.setExpanded(true);
			Node accountNode = InstancedDataUtil.getAccountNode(userName);

			List<InstancedRepeatableGroupRow> rgRows = instancedDataDao.getSelectedRepeatableGroupInstancedData(form,
					row.getSubmissionId(), group, codeMapping, accountNode);
			rcv.setRows(rgRows);

			// load group data
			instancedDataTable.addRgExpandTracker(new RepeatableGroupExpansionTracker(row.getSubmissionId(), column));
		} else {
			log.error("Repeatable group described by column, " + column.toString() + ", does not exist!");
			throw new InstancedDataException(
					"Repeatable group described by column, " + column.toString() + ", does not exist!");
		}
	}

	public void collapseRepeatableGroupRows(InstancedDataTable instancedDataTable, FormResult form, String rowUri,
			String rgName) {

		InstancedRow row = null;

		// search the records to get the row we are interested in loading the repeatable
		// group
		search: for (InstancedRecord record : instancedDataTable.getInstancedRecords()) {
			for (InstancedRow tmpRow : record.getSelectedRows()) {
				if (tmpRow != null && tmpRow.getRowUri().equals(rowUri)) {
					row = tmpRow;
					break search;
				}
			}
		}

		// we really only needed to check one for null, but oh wells.
		if (row == null) {
			throw new InstancedDataException("The specified rowUri is not in the current page!");
		}

		DataTableColumn column = form.getColumnFromString(form.getShortNameAndVersion(), rgName, null);
		CellValue cv = row.getCellValue(column);

		if (cv instanceof RepeatingCellValue) {
			RepeatingCellValue rcv = (RepeatingCellValue) cv;
			rcv.setExpanded(false);
			rcv.getRows().clear();
			instancedDataTable
					.removeRgExpandTracker(new RepeatableGroupExpansionTracker(row.getSubmissionId(), column));
		}
	}

	public void refreshRepeatableGroupRows(InstancedDataTable instancedDataTable, InstancedDataCache cache,
			FormResult form, String rowUri, String rgName, CodeMapping codeMapping, String userName) {

		InstancedRow row = null;

		// search the records to get the row we are interested in loading the repeatable
		// group
		row = cache.getByFormName(form.getShortNameAndVersion()).getByRowUri(rowUri);

		// this means the given uri is not on the current page
		if (row == null) {
			throw new InstancedDataException("The specified rowUri is not in the current page!");
		}

		DataTableColumn column = form.getColumnFromString(form.getShortNameAndVersion(), rgName, null);
		CellValue cellValue = row.getCellValue(column);

		if (cellValue instanceof RepeatingCellValue) {
			RepeatingCellValue rcv = (RepeatingCellValue) cellValue;
			rcv.setExpanded(false);
			instancedDataTable
					.removeRgExpandTracker(new RepeatableGroupExpansionTracker(row.getSubmissionId(), column));
		}

		// find the repeatable group object
		RepeatableGroup group = getRepeatableGroupUsingColumn(form, column);

		if (group != null && cellValue != null && cellValue instanceof RepeatingCellValue) {
			RepeatingCellValue rcv = (RepeatingCellValue) cellValue;
			rcv.setExpanded(true);
			Node accountNode = InstancedDataUtil.getAccountNode(userName);

			List<InstancedRepeatableGroupRow> rgRows = instancedDataDao.getSelectedRepeatableGroupInstancedData(form,
					row.getSubmissionId(), group, codeMapping, accountNode);
			rcv.setRows(rgRows);

			// load group data
			instancedDataTable.addRgExpandTracker(new RepeatableGroupExpansionTracker(row.getSubmissionId(), column));
		} else {
			log.error("Repeatable group described by column, " + column.toString() + ", does not exist!");
			throw new InstancedDataException(
					"Repeatable group described by column, " + column.toString() + ", does not exist!");
		}
	}

	/**
	 * TODO: change rg expansion to use this method once ryan comes back
	 * 
	 * @param submissionId
	 * @param column
	 * @throws IllegalServletArgumentException
	 */
	private void loadRepeatableGroupRowsBySubmissionId(List<FormResult> selectedForms,
			InstancedDataTable instancedDataTable, String submissionId, DataTableColumn column, CodeMapping codeMapping,
			Node accountNode) {

		InstancedRow currentRow = null;
		FormResult currentForm = getFormFromColumn(selectedForms, column);

		// Check if the given column is in one of the listed selected forms.
		if (currentForm == null) {
			String msg = "The given column name, " + column.toString() + ", is invalid!";
			log.error(msg);
			// throw new IllegalServletArgumentException(msg);
		}

		// search the records to get the row we are interested in loading the repeatable
		// group.
		search: for (InstancedRecord record : instancedDataTable.getInstancedRecords()) {
			for (InstancedRow row : record.getSelectedRows()) {
				if (row.getSubmissionId().equals(submissionId)) {
					currentRow = row;
					break search;
				}
			}
		}

		// this means the given URI is not on the current page
		if (currentRow == null) {
			return;
		}

		CellValue cellValue = currentRow.getCellValue(column);

		// find the repeatable group object
		RepeatableGroup group = getRepeatableGroupUsingColumn(currentForm, column);

		if (group != null && cellValue != null && (cellValue instanceof RepeatingCellValue)) {
			RepeatingCellValue rcv = (RepeatingCellValue) cellValue;
			rcv.setExpanded(true);

			// load group data
			List<InstancedRepeatableGroupRow> rgRows = instancedDataDao.getSelectedRepeatableGroupInstancedData(
					currentForm, currentRow.getSubmissionId(), group, codeMapping, accountNode);
			rcv.setRows(rgRows);

			instancedDataTable
					.addRgExpandTracker(new RepeatableGroupExpansionTracker(currentRow.getSubmissionId(), column));
		} else {
			String msg = "Repeatable group described by column, " + column.toString() + ", does not exist!";
			log.error(msg);
			// throw new IllegalServletArgumentException(msg);
		}
	}

	private void insertRepeatableGroupRowCounts(InstancedDataTable instancedDataTable, FormResult form,
			Map<CellPosition, Integer> rgRowCountMap) {

		for (CellPosition currentPosition : rgRowCountMap.keySet()) {

			// get the relevant column and rowUri
			DataTableColumn currentColumn = currentPosition.getColumn();
			String currentRowUri = currentPosition.getRowUri();

			Integer rowCount = rgRowCountMap.get(currentPosition);

			RepeatingCellValue rcv =
					instancedDataTable.getRepeatingCellValueByUriAndColumn(form, currentColumn, currentRowUri);

			if (rcv != null) {
				rcv.setRowCount(rowCount);
			}
		}
	}

	private List<CellPosition> findSingleRowCell(Map<CellPosition, Integer> rgRowCountMap) {
		List<CellPosition> singleRowRgPositions = new ArrayList<CellPosition>();

		for (CellPosition position : rgRowCountMap.keySet()) {
			Integer rowCount = rgRowCountMap.get(position);

			if (rowCount == 1) {
				singleRowRgPositions.add(position);
			}
		}

		return singleRowRgPositions;
	}

	/**
	 * Given a multimap of datatable columns to list of row uris where the group only has one row of data, loads all the
	 * repeatable group data and insert it into the repeatingCellValue
	 * 
	 * @param rgUriMap
	 * @return
	 */
	private void loadRepeatableGroups(List<FormResult> selectedForms, InstancedDataTable instancedDataTable,
			FormResult form, List<CellPosition> cellsToLoad, CodeMapping codeMapping, Node accountNode) {

		ListMultimap<DataTableColumn, String> columnsToRowUris = ArrayListMultimap.create();

		for (CellPosition cellPosition : cellsToLoad) {
			DataTableColumn column = cellPosition.getColumn();
			String rowUri = cellPosition.getRowUri();
			columnsToRowUris.put(column, rowUri);
		}

		for (DataTableColumn column : columnsToRowUris.keySet()) {
			List<String> rowUris = columnsToRowUris.get(column);
			if (rowUris != null) {
				instancedDataDao.loadRepeatableGroupRowsByUris(form, column, rowUris, selectedForms, codeMapping,
						accountNode, instancedDataTable);
			}
		}
	}

	/**
	 * Retrieves a selected form by the given column object.
	 * 
	 * @param sortColumn - The data table column used to search over.
	 * @return The FormResult object that corresponds to the given column object. If the given column object could not
	 *         be matched with any of the selected forms, null will be returned.
	 */
	private FormResult getFormFromColumn(List<FormResult> selectedForms, DataTableColumn sortColumn) {
		FormResult foundForm = null;

		// Search the list of selected forms for the form that is associated with the
		// specified column.
		for (FormResult form : selectedForms) {
			if (form.getShortNameAndVersion().equals(sortColumn.getForm())) {
				foundForm = form;
				break;
			}
		}

		return foundForm;
	}

	/**
	 * Returns the repeatable group object by using the column object
	 * 
	 * @param column
	 * @return
	 */
	private RepeatableGroup getRepeatableGroupUsingColumn(FormResult currentForm, DataTableColumn column) {

		if (currentForm != null) {
			for (RepeatableGroup rg : currentForm.getRepeatableGroups()) {
				if (rg.getName().equals(column.getRepeatableGroup())) {
					return rg;
				}
			}
		}

		return null;
	}

	/************************ Download Query *******************/

	/**
	 * Returns the instanced records for the form with all repeatable groups expanded.
	 * 
	 * @return
	 * @throws FilterEvaluatorException
	 * @throws IllegalServletArgumentException
	 */
	public InstancedDataTable buildInstancedDataTableForDownload(List<FormResult> formList, DataTableColumn sortColumn,
			String sortOrder, InstancedDataCache instancedDataCache, CodeMapping codeMapping, String userName,
			boolean isCartDownload, boolean isNormalCsv, boolean doApplyFilter, String booleanExpression)
			throws FilterEvaluatorException {

		if (isCartDownload) {
			for (FormResult form : formList) {
				seedFormDataElements(form);
			}
		}

		InstancedDataTable instancedDataTable = buildInstancedDataTable(formList, 0, Integer.MAX_VALUE, sortColumn,
				sortOrder, instancedDataCache, codeMapping, userName, true, doApplyFilter, booleanExpression);

		log.info("Loading repeatable group data...");
		// load data from the repeating repeatable groups
		loadAllRepeatableGroupRows(instancedDataTable, formList, isNormalCsv, codeMapping, userName);

		log.info("...Done");

		instancedDataTable.setAttachedFilesMap(
				InstancedDataUtil.getDatasetToAttachedFilesMap(instancedDataTable.getInstancedRecords()));

		return instancedDataTable;
	}

	/**
	 * Loads all repeatable groups belonging to data already loaded in the data table
	 */
	private void loadAllRepeatableGroupRows(InstancedDataTable instancedDataTable, List<FormResult> forms,
			boolean isNormalCsv, CodeMapping codeMapping, String userName) {

		for (int formIndex = 0; formIndex < forms.size(); formIndex++) {
			FormResult form = forms.get(formIndex);
			if (InstancedDataUtil.hasRepeatingGroup(form)) {

				Node accountNode = InstancedDataUtil.getAccountNode(userName);

				for (RepeatableGroup rg : form.getRepeatingRepeatableGroups()) {
					// this is a map of submission ID to repeatable group
					ListMultimap<String, InstancedRepeatableGroupRow> rgDataMap =
							instancedDataDao.getRepeatableGroupData(form, rg, accountNode, codeMapping);
					insertRepeatableRows(form, rg, formIndex, rgDataMap, instancedDataTable);
				}

				padRepeatableGroups(form, formIndex, instancedDataTable);
			}
		}
	}

	/**
	 * Here's the deal, since we are no longer doing Cartesian joins in the backend, the data from different repeatable
	 * groups will have mismatched row count. Meaning that if RG1 only has 2 rows but RG2 has 4 rows, there used to be
	 * two empty rows in RG1 due to the nature of the join. Without doing the join, those two empty rows don't exist. In
	 * order to have our original CSV serialization code in QueryToolRestService work nicely with the missing rows, we
	 * will have to add them here.
	 * 
	 * @param form
	 * @param instancedDataTable
	 */
	protected void padRepeatableGroups(FormResult form, int formIndex, InstancedDataTable instancedDataTable) {
		long startTime = System.nanoTime();
		for (InstancedRecord record : instancedDataTable.getInstancedRecords()) {
			InstancedRow row = record.getSelectedRows().get(formIndex);

			if (row != null) {
				// first, find the size of the longest repeatable group
				int maxRgSize = Integer.MIN_VALUE;
				for (Entry<DataTableColumn, CellValue> cellEntry : row.getCell().entrySet()) {
					DataTableColumn column = cellEntry.getKey();

					if (column.getDataElement() == null && !column.isHardCoded()) {
						RepeatingCellValue cv = (RepeatingCellValue) cellEntry.getValue();
						int rgSize = cv.getRows().size();
						if (rgSize > maxRgSize) {
							maxRgSize = rgSize;
						}
					}
				}

				for (Entry<DataTableColumn, CellValue> cellEntry : row.getCell().entrySet()) {
					DataTableColumn column = cellEntry.getKey();

					if (column.getDataElement() == null && !column.isHardCoded()) {
						RepeatingCellValue rcv = (RepeatingCellValue) cellEntry.getValue();

						int rgSize = rcv.getRows().size();
						// need to pad out the rg size so it is the same as the max
						if (rgSize < maxRgSize) {
							InstancedRepeatableGroupRow blankRow = new InstancedRepeatableGroupRow();

							RepeatableGroup rg = form.getGroupByName(column.getRepeatableGroup());

							for (DataElement de : rg.getSelectedElements()) {
								RepeatingCellColumn rgColumn =
										(RepeatingCellColumn) form.getColumnFromString(column.getForm(),
												column.getRepeatableGroup(), de.getName(), de.getType());

								blankRow.insertCell(rgColumn, QueryToolConstants.EMPTY_STRING);
							}

							for (int i = 0; i < maxRgSize - rgSize; i++) {
								rcv.addRow(blankRow);
							}
						}
					}
				}
			}
		}
		long endTime = System.nanoTime();
		log.info("Time to pad " + form.getShortName() + ": " + ((endTime - startTime) / 1000000) + "ms");
	}

	private void insertRepeatableRows(FormResult form, RepeatableGroup rg, int formIndex,
			ListMultimap<String, InstancedRepeatableGroupRow> rgDataMap, InstancedDataTable instancedDataTable) {

		Map<String, InstancedRow> submissionIdToInstancedRowMap = new HashMap<String, InstancedRow>();
		DataTableColumn rgColumn = form.getColumnFromString(form.getShortNameAndVersion(), rg.getName(), null);

		for (InstancedRecord record : instancedDataTable.getInstancedRecords()) {
			InstancedRow row = record.getSelectedRows().get(formIndex);
			if (row != null) {
				submissionIdToInstancedRowMap.put(row.getSubmissionId(), row);

				// make sure we start with a clean slate for all the repeating cells. this is
				// because partial data could
				// have gotten loaded when filters were applied to the repeating group.
				CellValue cv = row.getCellValue(rgColumn);
				if (cv.getIsRepeating()) {
					RepeatingCellValue rcv = (RepeatingCellValue) cv;
					rcv.getRows().clear();
				}
			}
		}

		for (Entry<String, InstancedRepeatableGroupRow> rgDataEntry : rgDataMap.entries()) {
			String submissionId = rgDataEntry.getKey();
			InstancedRow instancedRow = submissionIdToInstancedRowMap.get(submissionId);
			InstancedRepeatableGroupRow instancedRgRow = rgDataEntry.getValue();

			if (instancedRow != null) {
				CellValue cellValue = instancedRow.getCellValue(rgColumn);

				// insert all the rgRows
				if (cellValue.getIsRepeating()) {
					RepeatingCellValue rcv = (RepeatingCellValue) cellValue;
					rcv.addRow(instancedRgRow);
				}
			}
		}
	}

	public void reloadRepeatableGroup(List<FormResult> selectedForms, FormResult form, RepeatableGroup group,
			InstancedDataTable instancedDataTable, CodeMapping codeMapping, Node accountNode) {
		if (!group.doesRepeat()) {
			return;
		}

		int formIndex = selectedForms.indexOf(form);
		if (formIndex < 0) {
			throw new InstancedDataException("Selected form not found!");
		}

		List<InstancedRecord> records = instancedDataTable.getInstancedRecords();
		List<CellPosition> positionsToReload = new ArrayList<CellPosition>();

		for (InstancedRecord record : records) {
			InstancedRow currentRow = record.getSelectedRows().get(formIndex);
			if (currentRow != null) {
				DataTableColumn currentRgColumn = InstancedDataUtil.getColumnFromRepeatableGroup(form, group);
				CellValue cv = currentRow.getCellValue(currentRgColumn);
				if (cv != null && cv instanceof RepeatingCellValue) {
					RepeatingCellValue rcv = (RepeatingCellValue) cv;
					if (rcv.getRowCount() == 1) {
						positionsToReload.add(new CellPosition(currentRgColumn, currentRow.getRowUri()));
					}
				}
			}
		}

		if (!positionsToReload.isEmpty()) {
			loadRepeatableGroups(selectedForms, instancedDataTable, form, positionsToReload, codeMapping, accountNode);
		}
	}

	public boolean hasHighlightedGuid(FormResult form, Node accountNode) {
		return instancedDataDao.hasHighlightedGuid(form, accountNode);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<DataTableColumnWithUri> getColumnsWithNoData(List<FormResult> selectedForms, final Node accountNode) {
		// We are going to get a list of columns that have data from the database,
		// because that is easier than getting a
		// list of columns that don't have data.

		// To do this we are going to start columnsWithNoData with all of the columns
		// throughout the selected forms,
		// then we are going to remove all the columns in order to get a set of all
		// columns that have no data.
		Set<DataTableColumnWithUri> columnsWithNoData = InstancedDataUtil.buildDataColumnSet(selectedForms);
		Set<DataTableColumnWithUri> columnsWithData = new HashSet<DataTableColumnWithUri>();

		// For each selected form, we are going to start a thread to get all of the
		// columns that have data
		ExecutorService threadPool = Executors.newFixedThreadPool(selectedForms.size());
		Set<Future<Set<DataTableColumnWithUri>>> futures = new HashSet<Future<Set<DataTableColumnWithUri>>>();

		for (final FormResult currentForm : selectedForms) {
			Callable<Set<DataTableColumnWithUri>> callable = new Callable<Set<DataTableColumnWithUri>>() {

				public Set<DataTableColumnWithUri> call() throws Exception {
					return instancedDataDao.getColumnsWithData(currentForm, accountNode);
				}
			};

			Future<Set<DataTableColumnWithUri>> future = threadPool.submit(callable);
			futures.add(future);
		}

		// here, we will combine all of the columns that have data
		try {
			for (Future<Set<DataTableColumnWithUri>> future : futures) {
				columnsWithData.addAll(future.get());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} finally {
			threadPool.shutdown();
		}

		// now only columns with no data should be in columnsWithNoData.
		columnsWithNoData.removeAll(columnsWithData);

		return columnsWithNoData;
	}
}
