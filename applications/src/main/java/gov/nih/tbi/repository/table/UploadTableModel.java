package gov.nih.tbi.repository.table;

import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.repository.UploadItem;
import gov.nih.tbi.repository.UploadManagerController;
import gov.nih.tbi.repository.UploadStatus;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Table model to properly manage the behavior of our upload table
 */
public class UploadTableModel extends AbstractTableModel {


	// Column indexes in the table
	public static final int QUEUE_COLUMN = 0;
	public static final int FILE_COLUMN = 1;
	public static final int STUDY_COLUMN = 2;
	public static final int DATASET_COLUMN = 3;
	public static final int UPLOAD_SPEED_COLUMN = 4;
	public static final int UPLOAD_PROGRESS_COLUMN = 5;
	public static final int TIME_REMAINING_COLUMN = 6;
	public static final int STATUS_COLUMN = 7;

	public static final String UPLOAD_SPEED_UNIT = " kB/s";
	private static final long serialVersionUID = 970794080710962702L;

	// name of the columns
	String[] columns = {ServiceConstants.EMPTY_STRING, "File Name", "Study", "Dataset", "Upload Speed",
			"Upload Progress", "Time Remaining", "Status"};
	// data in the table
	List<List<Object>> data;

	public UploadTableModel() {

		data = new ArrayList<List<Object>>();
	}

	@Override
	public String getColumnName(int columnIndex) {

		return columns[columnIndex];
	}

	@Override
	public int getRowCount() {

		return data.size();
	}

	@Override
	public int getColumnCount() {

		return columns.length;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {

		return false;
	}

	/**
	 * Remove an item from the table. Will shift the rows up to fill in the item that got removed.
	 * 
	 * @param row - Row of the item to be removed.
	 */
	public void removeRow(int row) {

		if (data.size() == 1 || row == data.size() - 1) {
			data.remove(row);
		} else {
			for (int i = row; i < data.size() - 1; i++) {
				data.set(i, data.get(i + 1));
				if (UploadManagerController.getUploadItem(i + 1) != null) {
					UploadManagerController.getUploadItem(i + 1).setTableIndex(i);
				}
			}
			data.remove(data.size() - 1);
		}

		this.fireTableRowsDeleted(row, row);

	}

	/**
	 * Adds a new item into the table.
	 * 
	 * @param item - UploadItem to be added into the table.
	 */
	public void addNewRow(UploadItem item) {

		List<Object> newRow = new ArrayList<Object>();
		newRow.add(item.getTableIndex());
		newRow.add(item.getDatasetFile().getUserFile().getName());
		newRow.add(item.getStudyName());
		newRow.add(item.getDatasetName());
		// upload speed and progress are empty since their data are updated from the upload threads
		newRow.add(ServiceConstants.EMPTY_STRING);
		newRow.add(ServiceConstants.EMPTY_STRING);
		newRow.add(ServiceConstants.EMPTY_STRING);
		newRow.add(ServiceConstants.EMPTY_STRING);
		if (data == null) {
			data = new ArrayList<List<Object>>();
		}
		data.add(newRow);
		this.fireTableRowsInserted(item.getTableIndex(), item.getTableIndex());
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		if (data.get(rowIndex) == null) {
			return null;
		}

		UploadItem currentUpload = UploadManagerController.getUploadItem(rowIndex);

		if (currentUpload != null) { // current upload should never be null
			if (columnIndex == QUEUE_COLUMN) {
				return (Integer) currentUpload.getTableIndex() + 1;
			} else if (columnIndex == UPLOAD_SPEED_COLUMN) { // upload speed returns the data transfer rate in kB/s
				return currentUpload.getUploadChannel() != null ? currentUpload.getUploadChannel().getUploadSpeed() : 0 + UPLOAD_SPEED_UNIT;
			} else if (columnIndex == UPLOAD_PROGRESS_COLUMN) { // upload progress column returns progress of the
																// upload
				return currentUpload.getUploadChannel() != null ? currentUpload.getUploadChannel().getUploadProgress() : 0;
			} else if (columnIndex == TIME_REMAINING_COLUMN) {
				return currentUpload.getUploadChannel() != null ? currentUpload.getUploadChannel()
						.getTimeToCompletion() : "0h 0m 0s";
			} else if (columnIndex == STATUS_COLUMN) {
				if(currentUpload.getUploadChannel() != null &&	currentUpload.getUploadChannel().getUploadProgressManager() != null ){
					return currentUpload.getUploadChannel().getStatus();
				}
				return currentUpload.getUploadStatus().getName();
			} else { // otherwise just return the data in the cell
				return data.get(rowIndex).get(columnIndex);
			}
		} else {
			return null;
		}
	}

	public void setStatus(UploadStatus status, int tableIndex) {
		for (int i = 0; i < data.size(); i++) {
			List<Object> row = data.get(i);
			if (row.get((Integer) QUEUE_COLUMN).equals(new Integer(tableIndex))) {
				row.set(STATUS_COLUMN, status.getName());
				this.fireTableRowsUpdated(tableIndex, tableIndex);
				break;
			}
		}
	}

	/**
	 * Cancels the upload of a specific row
	 * 
	 * @param rowIndex
	 */
	public void cancelSingleItemUpload(int rowIndex) {
		UploadManagerController.cancelSingleItemUpload(UploadManagerController.getUploadItem(rowIndex));
		this.fireTableCellUpdated(rowIndex, STATUS_COLUMN);
	}

	/**
	 */
	public void cancelAllUpload() {
		for (int rowIdx = 0; rowIdx < getRowCount(); rowIdx++) {
			UploadItem uploadItem  = UploadManagerController.getUploadItem(rowIdx);
			uploadItem.setUploadStatus(UploadStatus.CANCELLED);
			uploadItem.getUploadChannel().setupCancelStatus();
			this.fireTableRowsUpdated(rowIdx, rowIdx);	
		}
	}

	/**
	 * Returns a row in the table as a list, where each item is listed in column order
	 * 
	 * @param rowIndex - row of the table to grab the dataset name from
	 * @return
	 */
	public List<Object> getRowInfo(int rowIndex) {

		if (getRowCount() > rowIndex && data.get(rowIndex) != null) {
			return data.get(rowIndex);
		}

		return null;
	}
}
