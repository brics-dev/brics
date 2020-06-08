package gov.nih.tbi.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.exceptions.FilterQueryStringException;
import gov.nih.tbi.filter.FilterQueryStringFactory;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
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

public class InstancedDataTableSerializer implements JsonSerializer<InstancedDataTable> {

	private static final Logger log = LogManager.getLogger(InstancedDataTableSerializer.class.getName());

	private String displayOption;
	private boolean showAgeRange;
	
	public InstancedDataTableSerializer(boolean showAgeRange) {
		this.showAgeRange = showAgeRange;
	}
	
	/**
	 * Build the filter query string using the given InstancedDataTable
	 * 
	 * @param table
	 * @return
	 * @throws FilterQueryStringException
	 */
	private String buildQueryString(InstancedDataTable table) throws FilterQueryStringException {
		String filterExpression = table.getFilterExpression();
		List<FormResult> forms = table.getForms();
		FilterQueryStringFactory queryStringFactory = new FilterQueryStringFactory(filterExpression, forms);
		String queryString = queryStringFactory.generateString();
		log.info("Filter query string: " + queryString);
		return queryString;
	}

	@Override
	public JsonElement serialize(InstancedDataTable table, Type typeOfSrc, JsonSerializationContext context) {

		this.displayOption = table.getDisplayOption();

		JsonObject json = new JsonObject();
		json.addProperty("iTotalDisplayRecords", table.getRowCount());
		json.addProperty("iTotalRecords", table.getRowCount());

		if (table.getFilterExpression() != null && !table.getFilterExpression().isEmpty()) {
			try {
				json.addProperty("queryString", buildQueryString(table));
			} catch (FilterQueryStringException e) {
				log.error("Error occured while trying to generate the query string for the instanced data table JSON",
						e);
			}
		}

		JsonArray tableRows = new JsonArray();
		Set<String> addedSelectedRowUris = new HashSet<String>();

		List<FormHeader> headers = table.getHeaders();
		boolean isJoined = headers.size() > 1;

		if (table.getInstancedRecords() != null) {

			List<InstancedRecord> records = Collections.synchronizedList(table.getInstancedRecords());

			synchronized (records) {
				Iterator<InstancedRecord> recordsIt = records.iterator();

				while (recordsIt.hasNext()) {
					InstancedRecord record = recordsIt.next();
					JsonArray row = new JsonArray();

					List<InstancedRow> rowList = record.getSelectedRows();
					if (rowList == null || rowList.isEmpty()) {
						log.info("InstancedRecord " + record.getPrimaryKey() + " has no rows.");
						continue;
					}

					InstancedRow selectedRow = rowList.get(0);
					List<InstancedRow> joinedRowList = new ArrayList<InstancedRow>(headers.size() - 1);
					for (int i = 1; i < headers.size(); i++) {
						if (rowList.size() > i) {
							joinedRowList.add(rowList.get(i));
						}
					}

					String renderFlag = QueryToolConstants.EMPTY_STRING;
					if (isJoined && selectedRow != null) {
						if (addedSelectedRowUris.contains(selectedRow.getRowUri())) {
							renderFlag = QueryToolConstants.GREYED_FLAG;
						} else {
							addedSelectedRowUris.add(selectedRow.getRowUri());
						}
					}

					if (renderFlag.isEmpty() && selectedRow != null) {
						if (selectedRow.isDoHighlight()) {
							renderFlag += QueryToolConstants.HIGHLIGHTED_FLAG;
						}

						if (selectedRow.isInMdsUpdrsX()) {
							renderFlag += QueryToolConstants.MDS_UPDRS_X_FLAG;
						}
					}

					int columnIndex = 0;
					Map<Integer, Integer> columnSpacingTracker = new TreeMap<Integer, Integer>();
					Map<Integer, JsonArray> repeatingRows = new TreeMap<Integer, JsonArray>();

					if (isJoined) {
						row.add(new JsonPrimitive(renderFlag + record.getPrimaryKey()));
						columnIndex++;
					}

					// Add selected row or primary row
					if (selectedRow == null) {
						columnIndex += addEmptyInstancedRow(headers.get(0), row); // Will this ever be possible?

					} else {
						columnIndex = addInstancedRow(row, renderFlag, selectedRow, columnIndex, headers,
								columnSpacingTracker, repeatingRows);
					}

					// Add joined instance rows if there is any
					for (int formIndex = 1; formIndex < headers.size(); formIndex++) {

						if (formIndex > joinedRowList.size() || joinedRowList.get(formIndex - 1) == null) {
							columnIndex += addEmptyInstancedRow(headers.get(formIndex), row);

						} else {
							InstancedRow joinedRow = joinedRowList.get(formIndex - 1);

							String joinRenderFlag = QueryToolConstants.EMPTY_STRING;
							if (joinedRow != null) {
								if (addedSelectedRowUris.contains(joinedRow.getRowUri())) {
									joinRenderFlag = QueryToolConstants.GREYED_FLAG;
								} else {
									addedSelectedRowUris.add(joinedRow.getRowUri());
								}
							}

							if (joinRenderFlag.isEmpty() && joinedRow != null) {
								if (joinedRow.isDoHighlight()) {
									joinRenderFlag = QueryToolConstants.HIGHLIGHTED_FLAG;
								}
								if (joinedRow.isInMdsUpdrsX()) {
									joinRenderFlag += QueryToolConstants.MDS_UPDRS_X_FLAG;
								}
							}

							columnIndex = addInstancedRow(row, joinRenderFlag, joinedRow, columnIndex, headers,
									columnSpacingTracker, repeatingRows);
						}
					}

					tableRows.add(row);

					// append empty spaces to the end of each row in case the expanded rg was not at
					// the end of the table
					for (JsonArray repeatingRow : repeatingRows.values()) {
						int emptyColumnsToAdd = row.size() - repeatingRow.size();
						for (int i = emptyColumnsToAdd; i > 0; i--) {
							addEmptyCell(repeatingRow);
						}

						tableRows.add(repeatingRow);
					}
				}
			}
		}

		json.add("aaData", tableRows);
		return json;
	}

	/**
	 * Fix short name and short name+version mismatch in InstancedDataManager
	 * instead
	 */
	private String removeVersion(String string) {
		return string.substring(0, string.lastIndexOf("V"));
	}

	/**
	 * Returns true if the column exists in the headers, otherwise return false.
	 * 
	 * @param headers
	 * @param column
	 * @return
	 */
	private boolean isColumnAdded(List<FormHeader> headers, DataTableColumn column) {

		if (column.isHardCoded()) {
			return true;
		}

		for (FormHeader formHeader : headers) {
			if (formHeader.getName().equals(removeVersion(column.getForm()))) {
				for (RepeatableGroupHeader rgHeader : formHeader.getRepeatableGroupHeaders()) {
					if (rgHeader.getName().equals(column.getRepeatableGroup())) {
						if (column.getDataElement() == null) {
							return true;
						} else {
							for (String deHeader : rgHeader.getDataElementHeaders()) {
								if (deHeader.equals(column.getDataElement())) {
									return true;
								}
							}
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * When the InstancedRow is null, we need to add empty cells for each header
	 * column.
	 * 
	 * @param header - FormHeader
	 * @param row    - JsonArray
	 * @return the number of empty cells that we added.
	 */
	private int addEmptyInstancedRow(FormHeader header, JsonArray row) {
		int cellCount = 0;

		for (RepeatableGroupHeader rgHeader : header.getRepeatableGroupHeaders()) {
			int deHeaderCount = rgHeader.getDataElementHeaders().size();
			cellCount += deHeaderCount;

			for (int i = 0; i < deHeaderCount; i++) {
				addEmptyCell(row);
			}
		}
		return cellCount;
	}

	/**
	 * This method adds an InstancedRow to the output JsonArray and return the
	 * ending column index after it is added.
	 * 
	 * @param row                  - JsonArray
	 * @param greyedFlag           - Either empty string or
	 *                             QueryToolConstants.GREYED_FLAG.
	 * @param instancedRow         - InstancedRow that the cell in in.
	 * @param columnIndex          - The beginning column index before the row is
	 *                             added.
	 * @param headers              - List of form headers.
	 * @param columnSpacingTracker - A map that tracks the column index of the
	 *                             repeating group cells
	 * @param repeatingRows        - A map that stores the row index and the
	 *                             repeatable group row JsonArrays
	 * @return the ending column index after the row is added.
	 */
	private int addInstancedRow(JsonArray row, String renderFlag, InstancedRow instancedRow, int columnIndex,
			List<FormHeader> headers, Map<Integer, Integer> columnSpacingTracker,
			Map<Integer, JsonArray> repeatingRows) {

		row.add(new JsonPrimitive(renderFlag + instancedRow.getStudyId()));
		columnIndex++;
		row.add(new JsonPrimitive(renderFlag + instancedRow.getReadableDatasetId()));
		columnIndex++;

		for (Entry<DataTableColumn, CellValue> cellEntry : instancedRow.getCell().entrySet()) {
			DataTableColumn column = cellEntry.getKey();
			CellValue cell = cellEntry.getValue();
			boolean isAgeYrsDe = InstancedDataUtil.AGE_YRS.equals(column.getDataElement()); 

			if (isColumnAdded(headers, column)) {
				if (cell instanceof NonRepeatingCellValue) {
					addNonRepeatingCell(row, renderFlag, instancedRow, cellEntry, isAgeYrsDe);
					columnIndex++;

				} else if (cell instanceof RepeatingCellValue) {
					addRepeatingCell(row, renderFlag, instancedRow, columnIndex, cellEntry, columnSpacingTracker,
							repeatingRows, isAgeYrsDe);
					columnIndex += ((RepeatingCellValue) cell).getDataElementCount();
				}
			}
		}

		return columnIndex;
	}

	/**
	 * This method adds a non-repeating cell to the output JsonArray row.
	 * 
	 * @param row          - JsonArray
	 * @param renderFlag   - Either empty string or QueryToolConstants.GREYED_FLAG.
	 * @param instancedRow - InstancedRow that the cell in in.
	 * @param cellEntry    - Map entry of the cell that contains the column
	 *                     information and the cell value.
	 */
	private void addNonRepeatingCell(JsonArray row, String renderFlag, InstancedRow instancedRow,
			Entry<DataTableColumn, CellValue> cellEntry, boolean isAgeYrsDe) {

		NonRepeatingCellValue nrc = (NonRepeatingCellValue) cellEntry.getValue();
		DataType deType = nrc.getDataElementType();

		switch (deType) {
		case BIOSAMPLE:
			String formName = cellEntry.getKey().getForm();
			String rowUri = instancedRow.getRowUri();
			row.add(new JsonPrimitive(renderFlag + QueryToolConstants.BIOSAMPLE_CELL_PREFIX + ":" + formName + ","
					+ rowUri + "," + nrc.getValue()));
			break;
		case THUMBNAIL:
			addThumbnailCell(row, renderFlag, instancedRow, nrc.getValue());
			break;
		case TRIPLANAR:
			addTriPlanarCell(row, renderFlag, instancedRow, nrc.getValue());
			break;
		case FILE:
			addFileCell(row, renderFlag, instancedRow, nrc.getValue());
			break;
		default:
			String cellValue = nrc.getValue(displayOption);
			if (isAgeYrsDe && showAgeRange) {
				cellValue = InstancedDataUtil.getAgeYrsRange(cellValue);
			}
			
			row.add(new JsonPrimitive(renderFlag + cellValue));
			break;
		}
	}

	/**
	 * This method adds a repeating group cell to the output JsonArray row.
	 * 
	 * @param row                  - JsonArray
	 * @param renderFlag           - Either empty string or
	 *                             QueryToolConstants.GREYED_FLAG.
	 * @param instancedRow         - InstancedRow that the cell in in.
	 * @param columnIndex          - Column index of this cell in the data table.
	 * @param cellEntry            - Map entry of the cell that contains the column
	 *                             information and the cell value.
	 * @param columnSpacingTracker - A map that tracks the column index of the
	 *                             repeating group cells
	 * @param repeatingRows        - A map that stores the row index and the
	 *                             repeatable group row JsonArrays
	 */
	private void addRepeatingCell(JsonArray row, String renderFlag, InstancedRow instancedRow, int columnIndex,
			Entry<DataTableColumn, CellValue> cellEntry, Map<Integer, Integer> columnSpacingTracker,
			Map<Integer, JsonArray> repeatingRows, boolean isAgeYrsDe) {

		RepeatingCellValue rcv = (RepeatingCellValue) cellEntry.getValue();
		int rgRowIndex = 0;
		int rcvDECount = rcv.getDataElementCount();

		// If the cell is expanded and not greyed or has only one row, add the expanded
		// repeatable group rows
		if (rcv.isExpanded() && (!(renderFlag.equals(QueryToolConstants.GREYED_FLAG)) || rcv.getRowCount() == 1)) {
			if (rcv.getRowCount() != 1) {
				addExpandedRGDataCell(row, renderFlag, instancedRow.getRowUri(), cellEntry);
			}

			columnSpacingTracker.put(rgRowIndex, columnIndex);
			rgRowIndex++;

			List<InstancedRepeatableGroupRow> rgRows = Collections.synchronizedList(rcv.getRows());

			synchronized (rgRows) {
				Iterator<InstancedRepeatableGroupRow> rgRowsIt = rgRows.iterator();

				while (rgRowsIt.hasNext()) {
					InstancedRepeatableGroupRow rgRow = rgRowsIt.next();

					if (rcv.getRowCount() == 1) {
						addRepeatableGroupRow(row, renderFlag, rgRow, instancedRow, rcvDECount, isAgeYrsDe);

					} else {
						JsonArray jsonRgRow = repeatingRows.get(rgRowIndex);
						if (jsonRgRow == null) {
							jsonRgRow = new JsonArray();
							repeatingRows.put(rgRowIndex, jsonRgRow);
						}

						int lastColumnIndex = columnSpacingTracker.get(rgRowIndex) == null ? 0
								: columnSpacingTracker.get(rgRowIndex);

						// add the empty cells from the beginning of the row
						for (int i = 0; i < (columnIndex - lastColumnIndex); i++) {
							addEmptyCell(jsonRgRow);
						}

						// add each repeatable group row
						addRepeatableGroupRow(jsonRgRow, renderFlag, rgRow, instancedRow, rcvDECount, isAgeYrsDe);

						columnSpacingTracker.put(rgRowIndex, columnIndex + rcvDECount);
						rgRowIndex++;
					}
				}
			}
		} else {
			// Otherwise add the collapsed repeating group row with Expand button
			addCollapsedRGDataCell(row, renderFlag, instancedRow.getRowUri(), cellEntry);
		}
	}

	/**
	 * This method adds a thumbnail image cell to the output JsonArray row.
	 * 
	 * @param row          - JsonArray
	 * @param renderFlag   - Either empty string or QueryToolConstants.GREYED_FLAG.
	 * @param instancedRow - InstancedRow that the cell in in.
	 * @param value        - display value of the cell, i.e. image file name in this
	 *                     case.
	 */
	private void addThumbnailCell(JsonArray row, String renderFlag, InstancedRow instancedRow, String value) {
		row.add(new JsonPrimitive(renderFlag + DataType.THUMBNAIL.getValue() + ":" + instancedRow.getStudyPrefixedId()
				+ "," + instancedRow.getDatasetName() + "," + value));
	}

	/**
	 * This method adds a Tri-Planar cell to the output JsonArray row.
	 * 
	 * @param row          - JsonArray
	 * @param renderFlag   - Either empty string or QueryToolConstants.GREYED_FLAG.
	 * @param instancedRow - InstancedRow that the cell in in.
	 * @param value        - display value of the cell, i.e. image file name in this
	 *                     case.
	 */
	private void addTriPlanarCell(JsonArray row, String renderFlag, InstancedRow instancedRow, String value) {
		row.add(new JsonPrimitive(renderFlag + DataType.TRIPLANAR.getValue() + ":" + instancedRow.getStudyPrefixedId()
				+ "," + instancedRow.getDatasetName() + "," + value));
	}

	private void addFileCell(JsonArray row, String renderFlag, InstancedRow instancedRow, String value) {
		row.add(new JsonPrimitive(renderFlag + DataType.FILE.getValue() + ":" + instancedRow.getStudyPrefixedId() + ","
				+ instancedRow.getDatasetName() + "," + value));
	}

	/**
	 * Adds an empty cell to the output JsonArray row.
	 * 
	 * @param row - JsonArray
	 */
	private void addEmptyCell(JsonArray row) {
		row.add(new JsonPrimitive(QueryToolConstants.EMPTY_STRING));
	}

	/**
	 * This method adds the expanded repeatable group data cell to the output
	 * JsonArray row.
	 * 
	 * @param row        - JsonArray
	 * @param renderFlag - Either empty string or QueryToolConstants.GREYED_FLAG.
	 * @param rowUri     - rowUri of the InstancedRow that the cell in in.
	 * @param cellEntry  - Map entry of the cell that contains the column
	 *                   information and the cell value.
	 */
	private void addExpandedRGDataCell(JsonArray row, String renderFlag, String rowUri,
			Entry<DataTableColumn, CellValue> cellEntry) {
		DataTableColumn column = cellEntry.getKey();
		RepeatingCellValue rcv = (RepeatingCellValue) cellEntry.getValue();

		row.add(new JsonPrimitive(renderFlag + "-collapseButton:" + rcv.getDataElementCount() + "," + rowUri + ","
				+ column.getForm() + "," + column.getRepeatableGroup()));

		for (int i = rcv.getDataElementCount() - 1; i > 0; i--) {
			addEmptyCell(row);
		}
	}

	/**
	 * This method adds the collapsed repeatable group data cell to the output
	 * JsonArray row.
	 * 
	 * @param row        - JsonArray
	 * @param renderFlag - Either empty string or QueryToolConstants.GREYED_FLAG.
	 * @param rowUri     - rowUri of the InstancedRow that the cell in in.
	 * @param cellEntry  - Map entry of the cell that contains the column
	 *                   information and the cell value.
	 */
	private void addCollapsedRGDataCell(JsonArray row, String renderFlag, String rowUri,
			Entry<DataTableColumn, CellValue> cellEntry) {
		DataTableColumn column = cellEntry.getKey();
		RepeatingCellValue rcv = (RepeatingCellValue) cellEntry.getValue();

		if (!rcv.hasData()) {
			row.add(new JsonPrimitive("-disabledButton"));
		} else {
			row.add(new JsonPrimitive(renderFlag + "-rgButton:" + rcv.getDataElementCount() + "," + rowUri + ","
					+ column.getForm() + "," + column.getRepeatableGroup()));
		}

		for (int i = rcv.getDataElementCount() - 1; i > 0; i--) {
			addEmptyCell(row);
		}
	}

	/**
	 * This method adds one expanded repeatable group row to the output JsonArray
	 * row.
	 * 
	 * @param row          - JsonArray
	 * @param renderFlag   - Either empty string or QueryToolConstants.GREYED_FLAG.
	 * @param rgRow        - InstancedRepeatableGroupRow to be added.
	 * @param cellEntry    - Map entry of the cell that contains the column
	 * @param instancedRow - InstancedRow that rgRow belongs to.
	 *                     information and the cell value.
	 */
	private void addRepeatableGroupRow(JsonArray row, String renderFlag, InstancedRepeatableGroupRow rgRow,
			InstancedRow instancedRow, int rcvDECount, boolean isAgeYrsDe) {

		if (rgRow != null && rgRow.getCell() != null) {
			Map<RepeatingCellColumn, CellValueCode> rgDataMap = Collections.synchronizedMap(rgRow.getCell());
			Set<Entry<RepeatingCellColumn, CellValueCode>> rgDataEntrySet = rgDataMap.entrySet();
			
			synchronized (rgDataMap) {
				Iterator<Entry<RepeatingCellColumn, CellValueCode>> rgDataEntryIt = rgDataEntrySet.iterator();
				
				while(rgDataEntryIt.hasNext()) {
					Entry<RepeatingCellColumn, CellValueCode> rgData = rgDataEntryIt.next();
					String rgValue = rgData.getValue().getValue(displayOption);
					DataType dataType = rgData.getKey().getType();

					switch (dataType) {
					case THUMBNAIL:
						addThumbnailCell(row, renderFlag, instancedRow, rgValue);
						break;
					case TRIPLANAR:
						addTriPlanarCell(row, renderFlag, instancedRow, rgValue);
						break;
					case FILE:
						addFileCell(row, renderFlag, instancedRow, rgValue);
						break;
					default:
						if (isAgeYrsDe && showAgeRange) {
							rgValue = InstancedDataUtil.getAgeYrsRange(rgValue);
						}
						
						row.add(new JsonPrimitive(renderFlag + rgValue));
						break;
					}
				}
			}
		} else {
			// rg has no data, so we need to replace with empty strings
			for (int i = 0; i < rcvDECount; i++) {
				addEmptyCell(row);
			}
		}
	}

}
