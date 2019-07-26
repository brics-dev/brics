package gov.nih.tbi.util;

import java.util.Comparator;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.repository.model.CellValue;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.InstancedRecord;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;

public class InstancedRecordComparator implements Comparator<InstancedRecord> {
	private DataTableColumn sortColumn;
	private String sortOrder;

	public InstancedRecordComparator(DataTableColumn sortColumn, String sortOrder) {
		super();
		this.sortColumn = sortColumn;
		this.sortOrder = sortOrder;
	}

	public DataTableColumn getSortColumn() {
		return sortColumn;
	}


	public void setSortColumn(DataTableColumn sortColumn) {
		this.sortColumn = sortColumn;
	}


	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	private String getValueFromRecord(InstancedRecord record, DataTableColumn column) {
		InstancedRow row = null;

		// find the instanced row we are sorting
		for (InstancedRow currentRow : record.getSelectedRows()) {
			if (currentRow != null) { // it's possible for row to be null
				if (currentRow.getFormShortName().equals(column.getForm())) {
					row = currentRow;
					break;
				}
			}
		}

		// find the relevant value we are sorting on
		if (row != null) {
			if (!column.isHardCoded()) {
				CellValue cv = row.getCellValue(column);

				if (cv instanceof NonRepeatingCellValue) {
					NonRepeatingCellValue ncv = (NonRepeatingCellValue) cv;

					return ncv.getValue();
				} else {
					throw new UnsupportedOperationException("Cannot sort by a column from a repeatable group!");
				}
			} else {
				String hardCodedColumn = column.getHardCoded();

				if (QueryToolConstants.STUDY_COLUMN_VAR.equals(hardCodedColumn)) {
					return row.getStudyId();
				} else if (QueryToolConstants.DATASET_COLUMN_VAR.equals(hardCodedColumn)) {
					return row.getDatasetId().toString();
				} else if (QueryToolConstants.GUID_VAR.getName().equals(hardCodedColumn)) {
					return row.getGuid();
				} else {
					throw new UnsupportedOperationException("Cannot sort by column " + hardCodedColumn + "!");
				}
			}
		}

		if (row == null && column.isHardCoded()) {
			String hardCodedColumn = column.getHardCoded();

			if (QueryToolConstants.GUID_COLUMN_VAR.equals(hardCodedColumn)) {
				return record.getPrimaryKey();
			}
		}

		return "";
	}

	@Override
	public int compare(InstancedRecord record1, InstancedRecord record2) {
		// TODO: Implement for types other than strings
		String value1 = getValueFromRecord(record1, sortColumn);
		String value2 = getValueFromRecord(record2, sortColumn);

		if (QueryToolConstants.ASCENDING.equalsIgnoreCase(sortOrder)) {
			return value1.compareTo(value2);
		} else {
			return value2.compareTo(value1);
		}
	}
}
