package gov.nih.tbi.export.csv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

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

public class NormalCsvRecordSerializer extends AbstractCsvRecordSerializer implements CsvRecordSerializer {
	
	public NormalCsvRecordSerializer(InstancedDataTable instancedDataTable) {
		super(instancedDataTable);
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
	public String serializeRecord(InstancedRecord record) {
		List<List<String>> recordList = new ArrayList<List<String>>();
		List<InstancedRow> rowList = record.getSelectedRows();
		InstancedRow selectedRow = rowList.get(0);

		List<InstancedRow> joinedRowList = new ArrayList<InstancedRow>(headers.size() - 1);
		for (int i = 1; i < headers.size(); i++) {
			if (rowList.size() > i) { // && rowList.get(i) != null) {
				joinedRowList.add(rowList.get(i));
			}
		}

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

		// Add selected or primary row
		columnIndex =
				addInstancedRow(row, selectedRow, columnIndex, columnSpacingTracker, repeatingRows, displayOption);

		// Add joined rows if there is any
		for (int formIndex = 1; formIndex < headers.size(); formIndex++) {

			if (formIndex > joinedRowList.size() || joinedRowList.get(formIndex - 1) == null) {
				for (int i = 0; i < instancedDataTable.getColumnLengthByFormIndex(formIndex); i++) {
					row.add(QueryToolConstants.EMPTY_STRING);
					columnIndex++;
				}
			} else {
				InstancedRow joinedRow = joinedRowList.get(formIndex - 1);

				// For some reason, all rows are off by one for joined forms. This is a manual override
				// and we reset the override when complete (rather than compounding the offset).
				// It is possible that the offset is identical to the NUMBER_OF_HARDCODED_COLUMNS, but that is
				// difficult to check
				// without more joinable forms.
				// Incidentally, this issue only occurs on subsequent rows... each "initial row" of a record is
				// perfectly fine, but
				// subsequent rows are being affected.
				columnIndex = addInstancedRow(row, joinedRow, (columnIndex + 2), columnSpacingTracker, repeatingRows,
						displayOption);
				columnIndex = columnIndex - 2;
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
			Map<Integer, Integer> columnSpacingTracker, Map<Integer, List<String>> repeatingRows,
			String displayOption) {

		row.add(sanitizeCsvValue(instancedRow.getStudyId()));
		row.add(sanitizeCsvValue(instancedRow.getReadableDatasetId()));

		if (instancedRow != null && instancedRow.getCell() != null) {
			for (Entry<DataTableColumn, CellValue> cellEntry : instancedRow.getCell().entrySet()) {
				CellValue cell = cellEntry.getValue();

				if (!cell.getIsRepeating()) {	// if cell does not repeat
					NonRepeatingCellValue nrc = (NonRepeatingCellValue) cell;
					row.add(sanitizeCsvValue(nrc.getValue(displayOption)));
					columnIndex++;

				} else {	// if cell does repeat
					RepeatingCellValue rcv = (RepeatingCellValue) cell;
					int rgRowIndex = 0;

					if (rcv != null) {
						for (InstancedRepeatableGroupRow rgRow : rcv.getRows()) {
							// if we're looking at the first row of the rg, just
							// insert it straight into the same row as the non-repeating data
							if (rgRowIndex == 0) {
								for (Entry<RepeatingCellColumn, CellValueCode> rgData : rgRow.getCell().entrySet()) {
									String rgValue = rgData.getValue().getValue(displayOption);
									row.add(sanitizeCsvValue(rgValue));
								}

							} else {	// if not the first row of the rg
								List<String> rgRowData = repeatingRows.get(rgRowIndex);

								// # of empty strings we need to prepend to get the rg data in the right place
								int emptyStringsToAdd = 0;

								if (rgRowData == null) {
									rgRowData = new ArrayList<String>();
									repeatingRows.put(rgRowIndex, rgRowData);

									// offset the empty rows by the amount of rows that have been hardcoded
									// e.g. Study, Dataset
									emptyStringsToAdd += QueryToolConstants.NUM_OF_HARDCODED_COLUMNS;
								}

								int lastColumnIndex =
										columnSpacingTracker.get(rgRowIndex) == null ? 0 : columnSpacingTracker
												.get(rgRowIndex);

								emptyStringsToAdd += (columnIndex - lastColumnIndex);

								for (int i = emptyStringsToAdd; i > 0; i--) { // add the empty cells from before
									rgRowData.add(QueryToolConstants.EMPTY_STRING);
								}

								// start add rg data
								if (rgRow.getCell() != null) {
									for (Entry<RepeatingCellColumn, CellValueCode> rgData : rgRow.getCell()
											.entrySet()) {
										String rgValue = rgData.getValue().getValue(displayOption);
										rgRowData.add((sanitizeCsvValue(rgValue)));
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

		return columnIndex;
	}
}
