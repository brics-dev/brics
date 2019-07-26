package gov.nih.tbi.export.csv;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.repository.model.CellValue;
import gov.nih.tbi.repository.model.CellValueCode;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.FormHeader;
import gov.nih.tbi.repository.model.InstancedRecord;
import gov.nih.tbi.repository.model.InstancedRepeatableGroupRow;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;
import gov.nih.tbi.repository.model.RepeatingCellColumn;
import gov.nih.tbi.repository.model.RepeatingCellValue;

public class FlattenCsvRecordSerializer extends AbstractCsvRecordSerializer implements CsvRecordSerializer {
	private static final Logger log = Logger.getLogger(FlattenCsvRecordSerializer.class);


	Map<String, Integer> paddingNumber;

	public FlattenCsvRecordSerializer(InstancedDataTable instancedDataTable) {
		super(instancedDataTable);

		paddingNumber = instancedDataTable.getGreatestNumber();
		List<FormHeader> headers = instancedDataTable.getHeaders();

		for (FormHeader f : headers) {
			String key = f.getName() + "V" + f.getVersion();
			int i = 0;
			for (String eachElement : paddingNumber.keySet()) {
				if (eachElement.startsWith(key)) {
					i = i + paddingNumber.get(eachElement);
				}
			}
			paddingNumber.put(key, i);
		}
	}

	@Override
	public String serializeHeader() {
		List<String> headerList = instancedDataTable.getFlattenedCSVHeaders();
		return BRICSStringUtils.concatWithDelimiter(headerList, ",") + "\n";
	}

	@Override
	public String serializeRecord(InstancedRecord record) {
		List<List<String>> recordList = new ArrayList<List<String>>();
		List<InstancedRow> rowList = record.getSelectedRows();
		InstancedRow selectedRow = rowList.get(0);

		List<InstancedRow> joinedRowList = new ArrayList<InstancedRow>(headers.size() - 1);

		for (int i = 1; i < headers.size(); i++) {
			if (rowList.size() > 1) { // && rowList.get(i) != null) {
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
		columnIndex = addFlattenedInstancedRow(row, selectedRow, columnIndex, columnSpacingTracker, repeatingRows,
				displayOption, paddingNumber);


		// Add joined rows if there is any
		for (int formIndex = 1; formIndex < headers.size(); formIndex++) {

			if (formIndex > joinedRowList.size() || joinedRowList.get(formIndex - 1) == null) {
				FormHeader fh = headers.get(formIndex);

				// the +2 here accounts for the study and dataset columns that are not a part of the form, but
				// always a part of the export
				int maxValue = paddingNumber.get(fh.getName() + "V" + fh.getVersion());

				// The study and dataset columns are not included in the paddingNumber values.
				maxValue = maxValue + QueryToolConstants.NUM_OF_HARDCODED_COLUMNS;

				for (int i = 0; i < maxValue; i++) {
					row.add(QueryToolConstants.EMPTY_STRING);
					columnIndex++;
				}
			} else {
				InstancedRow joinedRow = joinedRowList.get(formIndex - 1);
				columnIndex = addFlattenedInstancedRow(row, joinedRow, columnIndex, columnSpacingTracker, repeatingRows,
						displayOption, paddingNumber);
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

	private int addFlattenedInstancedRow(List<String> row, InstancedRow instancedRow, int columnIndex,
			Map<Integer, Integer> columnSpacingTracker, Map<Integer, List<String>> repeatingRows, String displayOption,
			Map<String, Integer> paddingNumbers) {

		LinkedHashMap<String, List<String>> mapForRow = new LinkedHashMap<String, List<String>>();

		List<String> studyList = new ArrayList<String>();
		studyList.add(instancedRow.getStudyId());
		List<String> datasetIdList = new ArrayList<String>();
		datasetIdList.add(instancedRow.getReadableDatasetId());

		mapForRow.put("studyId", studyList);
		mapForRow.put("datasetId", datasetIdList);

		if (instancedRow != null && instancedRow.getCell() != null) {
			for (Entry<DataTableColumn, CellValue> cellEntry : instancedRow.getCell().entrySet()) {
				CellValue cell = cellEntry.getValue();

				if (!cell.getIsRepeating()) {	// if cell does not repeat
					NonRepeatingCellValue nrc = (NonRepeatingCellValue) cell;
					// row.add(nrc.getValue(displayOption));

					List<String> nrcList = new ArrayList<String>();
					nrcList.add(nrc.getValue(displayOption));
					// mapForRow.put(nrc.getDataElementType(), nrcList);
					mapForRow.put(cellEntry.getKey().toString(), nrcList);

					columnIndex++;

				} else {	// if cell does repeat
					RepeatingCellValue rcv = (RepeatingCellValue) cell;

					if (rcv != null) {
						for (InstancedRepeatableGroupRow rgRow : rcv.getRows()) {

							for (Entry<RepeatingCellColumn, CellValueCode> repeatingCellEntry : rgRow.getCell()
									.entrySet()) {
								RepeatingCellColumn repeatingCellColumnKey = repeatingCellEntry.getKey();
								CellValueCode cvv = repeatingCellEntry.getValue();
								log.debug("Repeating Cell Column: " + repeatingCellColumnKey + ", Value: \""
										+ cvv.getValue() + "\"");
								String rgKeyColumnKey = "";
								if (!QueryToolConstants.EMPTY_STRING
										.equals(repeatingCellColumnKey.getRepeatableGroup())) {
									rgKeyColumnKey = repeatingCellColumnKey.getRepeatableGroup() + ".";
								}
								String elementKey = repeatingCellColumnKey.getForm() + "." + rgKeyColumnKey
										+ repeatingCellColumnKey.getDataElement();

								// now that we aren't counting "empty" in the columns, we have to exclude them here....
								// is this the correct behavior?
								if (cvv.getValue(displayOption) != null) { // && !cvv.getValue(displayOption).isEmpty())
																			 // {
									// if (!cvv.getValue(displayOption).isEmpty() && mapForRow.containsKey(elementKey))
									// {
									if (mapForRow.containsKey(elementKey)) {
										List<String> elementValueList = mapForRow.get(elementKey);
										elementValueList.add(cvv.getValue(displayOption));

									} else {
										List<String> elementValueList = new LinkedList<String>();
										elementValueList.add(cvv.getValue(displayOption));
										mapForRow.put(elementKey, elementValueList);
									}
								}
								columnIndex++;
							}
						}
					}
				}
			}

			for (Entry<String, List<String>> rowEntry : mapForRow.entrySet()) {
				if (log.isDebugEnabled()) {
					String currentRow = "";

					for (String cellValue : row) {
						currentRow += cellValue + ", ";
					}

					currentRow = "[ " + currentRow.substring(0, currentRow.length() == 0 ? 0 : currentRow.length() - 1)
							+ " ]";

					log.debug("Current Row: " + currentRow);
				}

				String mapKey = rowEntry.getKey();
				List<String> valueList = rowEntry.getValue();

				log.debug("Current Map Key: " + mapKey);
				if (paddingNumbers.containsKey(mapKey)) {

					for (String value : valueList) {
						log.debug("Adding \"" + value + "\" to current row");
						row.add(sanitizeCsvValue(value));
					}

					int padUntil = paddingNumbers.get(mapKey);
					int padFrom = valueList.size();

					log.debug("Pad Until: " + padUntil);
					log.debug("Pad From: " + padFrom);

					while (padFrom < padUntil) {
						row.add(QueryToolConstants.EMPTY_STRING);
						padFrom++;
					}
				} else {
					row.add(sanitizeCsvValue(mapForRow.get(mapKey).get(0)));
				}
			}
		}
		return columnIndex;
	}
}
