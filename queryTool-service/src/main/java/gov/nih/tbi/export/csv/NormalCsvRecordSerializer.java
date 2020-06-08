package gov.nih.tbi.export.csv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Set;

import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.repository.model.CellValue;
import gov.nih.tbi.repository.model.CellValueCode;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.InstancedRecord;
import gov.nih.tbi.repository.model.InstancedRepeatableGroupRow;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;
import gov.nih.tbi.repository.model.RepeatingCellColumn;
import gov.nih.tbi.repository.model.RepeatingCellValue;
import gov.nih.tbi.util.InstancedDataUtil;

public class NormalCsvRecordSerializer extends AbstractCsvRecordSerializer implements CsvRecordSerializer {

	public NormalCsvRecordSerializer(InstancedDataTable instancedDataTable, boolean showAgeRange) {
		super(instancedDataTable, showAgeRange);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String serializeHeader() {
		List<String> headerList = instancedDataTable.getCsvHeaders();
		return BRICSStringUtils.concatWithDelimiter(headerList, ",") + "\n";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String serializeRecord(InstancedRecord record, Set<DataTableColumn> visibleColumns) {
		List<List<String>> recordList = new ArrayList<List<String>>();
		List<InstancedRow> rowList = record.getSelectedRows();

		List<String> row = new ArrayList<String>();

		int columnIndex = 0;
		// row index -> column index
		Map<Integer, Integer> columnSpacingTracker = new TreeMap<Integer, Integer>();
		// row index -> row data array
		Map<Integer, List<String>> repeatingRows = new TreeMap<Integer, List<String>>();

		if (instancedDataTable.isJoined()) {
			row.add(sanitizeCsvValue(record.getPrimaryKey()));
			columnIndex++;
		}

		// Add joined rows if there is any
		for (int formIndex = 0; formIndex < headers.size(); formIndex++) {

			if (rowList.get(formIndex) == null) {
				for (int i = 0; i < instancedDataTable.getColumnLengthByFormIndex(formIndex); i++) {
					row.add(QueryToolConstants.EMPTY_STRING);
					columnIndex++;
				}
			} else {
				InstancedRow currentRow = rowList.get(formIndex);
				columnIndex = addInstancedRow(row, currentRow, columnIndex, columnSpacingTracker, repeatingRows,
						displayOption, visibleColumns);
			}
		}

		int emptyColumnsToAddToRow = instancedDataTable.getColumnLengthByFormIndex(0) - row.size();
		for (int i = emptyColumnsToAddToRow; i > 0; i--) {
			row.add(QueryToolConstants.EMPTY_STRING);
		}

		// add the first row (that include non-repeating data)
		recordList.add(row);

		// appended empty spaces in case the expanded rg was not at the end of the table
		for (Entry<Integer, List<String>> repeatingRowEntry : repeatingRows.entrySet()) {

			int emptyColumnsToAdd = row.size() - repeatingRowEntry.getValue().size();
			for (int i = emptyColumnsToAdd; i > 0; i--) {
				repeatingRowEntry.getValue().add(QueryToolConstants.EMPTY_STRING);
			}
			recordList.add(repeatingRowEntry.getValue());
		}

		return recordListToString(recordList);
	}

	private int addInstancedRow(List<String> row, InstancedRow instancedRow, int columnIndex,
			Map<Integer, Integer> columnSpacingTracker, Map<Integer, List<String>> repeatingRows, String displayOption,
			Set<DataTableColumn> visibleColumns) {

		row.add(sanitizeCsvValue(instancedRow.getStudyId()));
		columnIndex++;
		row.add(sanitizeCsvValue(instancedRow.getReadableDatasetId()));
		columnIndex++;

		if (instancedRow != null && instancedRow.getCell() != null) {
			for (Entry<DataTableColumn, CellValue> cellEntry : instancedRow.getCell().entrySet()) {
				DataTableColumn currentColumn = cellEntry.getKey();
				CellValue cell = cellEntry.getValue();

				if (visibleColumns.contains(currentColumn)) {
					boolean isAgeYrsDe = InstancedDataUtil.AGE_YRS.equals(currentColumn.getDataElement()); 
					
					if (!cell.getIsRepeating()) {	// if cell does not repeat
						NonRepeatingCellValue nrc = (NonRepeatingCellValue) cell;
						String cellValue = nrc.getValue(displayOption);
						if (isAgeYrsDe && showAgeRange) {
							cellValue = InstancedDataUtil.getAgeYrsRange(cellValue);
						}
						row.add(sanitizeCsvValue(cellValue));
						columnIndex++;
					} else {	// if cell does repeat
						RepeatingCellValue rcv = (RepeatingCellValue) cell;
						int rgRowIndex = 0;

						if (rcv != null) {
							for (InstancedRepeatableGroupRow rgRow : rcv.getRows()) {
								// if we're looking at the first row of the rg, just
								// insert it straight into the same row as the non-repeating data
								if (rgRowIndex == 0) {
									for (Entry<RepeatingCellColumn, CellValueCode> rgData : rgRow.getCell()
											.entrySet()) {
										RepeatingCellColumn currentRgColumn = rgData.getKey();

										if (visibleColumns.contains(currentRgColumn)) {
											String rgValue = rgData.getValue().getValue(displayOption);
											if (isAgeYrsDe && showAgeRange) {
												rgValue = InstancedDataUtil.getAgeYrsRange(rgValue);
											}
											row.add(sanitizeCsvValue(rgValue));
										}
									}

								} else {	// if not the first row of the rg
									List<String> rgRowData = repeatingRows.get(rgRowIndex);

									// # of empty strings we need to prepend to get the rg data in the right place
									int emptyStringsToAdd = 0;

									if (rgRowData == null) {
										rgRowData = new ArrayList<String>();
										repeatingRows.put(rgRowIndex, rgRowData);
									}

									int lastColumnIndex = 0;

									if (columnSpacingTracker.get(rgRowIndex) != null) {
										lastColumnIndex = columnSpacingTracker.get(rgRowIndex);
									}

									emptyStringsToAdd += (columnIndex - lastColumnIndex);

									for (int i = emptyStringsToAdd; i > 0; i--) { // add the empty cells from before
										rgRowData.add(QueryToolConstants.EMPTY_STRING);
									}

									// start add rg data
									if (rgRow.getCell() != null) {
										for (Entry<RepeatingCellColumn, CellValueCode> rgData : rgRow.getCell()
												.entrySet()) {
											RepeatingCellColumn currentRgColumn = rgData.getKey();

											if (visibleColumns.contains(currentRgColumn)) {
												String rgValue = rgData.getValue().getValue(displayOption);
												if (isAgeYrsDe && showAgeRange) {
													rgValue = InstancedDataUtil.getAgeYrsRange(rgValue);
												}
												rgRowData.add(sanitizeCsvValue(rgValue));
											}
										}
									} else {
										// rg has no data, so we need to replace with empty strings
										for (int i = 0; i < rcv.getDataElementCount(); i++) {
											rgRowData.add(QueryToolConstants.EMPTY_STRING);
										}
									}
								}

								columnSpacingTracker.put(rgRowIndex, columnIndex + rcv.getDataElementCount());
								rgRowIndex++;
							}

							columnIndex += rcv.getDataElementCount();
						}
					}
				}
			}
		}

		return columnIndex;
	}
}
