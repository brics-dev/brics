package gov.nih.tbi.repository.table;

import gov.nih.tbi.commons.model.DatasetFileStatus;
import gov.nih.tbi.repository.model.DatasetTableItem;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.table.AbstractTableModel;

public class DatasetTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -5693900633174854577L;

	private List<DatasetTableItem> rows = new ArrayList<DatasetTableItem>();
	private Map<Long, Dataset> datasetMap = new HashMap<Long, Dataset>(); // this map is used to quickly access the
																			// datasets by ID
	private JButton uploadButton;

	public DatasetTableModel(List<Dataset> datasets, JButton uploadButton) {
		this.uploadButton = uploadButton;

		for (Dataset dataset : datasets) {
			if (hasPendingFiles(dataset)) { //double check if there is at least one dataset files in the dataset that is in pending status
				datasetMap.put(dataset.getId(), dataset);
				rows.add(new DatasetTableItem(dataset));
			}
		}
	}

	private boolean hasPendingFiles(Dataset dataset) {
		for (DatasetFile file : dataset.getDatasetFileSet()) {
			if (DatasetFileStatus.PENDING.equals(file.getDatasetFileStatus())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return DatasetTableColumn.getByColumnIndex(columnIndex).getColumnName();
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return DatasetTableColumn.getByColumnIndex(columnIndex).getThisClass();
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return column == DatasetTableColumn.CHECK_BOX.getColumnIndex();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		DatasetTableItem row = rows.get(rowIndex);
		DatasetTableColumn column = DatasetTableColumn.getByColumnIndex(columnIndex);

		switch (column) {
			case CHECK_BOX:
				return row.isChecked();
			case NAME:
				return row.getName();
			case STUDY_TITLE:
				return row.getStudyTitle();
			case PROGRESS:
				return row.getUploadProgress();
			default: // this should never happen
				throw new IllegalArgumentException("This column is not supported: " + column.getColumnName());
		}
	}

	public List<Dataset> getSelectedDatasetIds() {
		List<Dataset> selectedDatasets = new ArrayList<Dataset>();

		for (DatasetTableItem item : rows) {
			if (item.isChecked()) {
				selectedDatasets.add(datasetMap.get(item.getId()));
			}
		}

		return selectedDatasets;
	}

	private boolean atLeastOneChecked() {
		for (DatasetTableItem row : rows) {
			if (row.isChecked()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		if (isCellEditable(row, column)) {
			rows.get(row).setChecked((Boolean) value);
			uploadButton.setEnabled(atLeastOneChecked());
		}

		fireTableCellUpdated(row, column);
	}

	@Override
	public int getColumnCount() {
		return DatasetTableColumn.values().length;
	}
}
