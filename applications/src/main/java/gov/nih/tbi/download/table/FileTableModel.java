package gov.nih.tbi.download.table;

import gov.nih.tbi.repository.model.hibernate.DatasetDownloadFile;
import gov.nih.tbi.repository.model.hibernate.Downloadable;
import gov.nih.tbi.repository.model.hibernate.QueryToolDownloadFile;

import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

/**
 * Table model used to display information from the micro-level. The focus is on the package's files.
 * 
 * @author wangvg
 * 
 */
public class FileTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -2270977106957558484L;
	public final static int CHECKBOX_COLUMN = 0;
	public final static int NAME_COLUMN = 1;
	public final static int STUDY_COLUMN = 2;
	public final static int TYPE_COLUMN = 3;
	public final static int SIZE_COLUMN = 4;
	public final static int PROGRESS_COLUMN = 5;
	public final static int STATUS_COLUMN = 6;

	private static boolean checkboxEditable = true;
	private final static String[] columnNames = new String[] {"", "Name", "Study", "File Type", "Size",
			"Progress", "Status"};

	private final static Class<?>[] columnClasses = new Class[] {Boolean.class, String.class, String.class,
			String.class, String.class, Float.class, String.class};

	private final String[] FILE_SIZE_UNITS = new String[] {"B", "KB", "MB", "GB", "TB"};

	private ArrayList<DownloadableRow> rows;

	public FileTableModel() {
		rows = new ArrayList<DownloadableRow>();
	}

	public void setRows(ArrayList<DownloadableRow> newRows) {
		rows = newRows;
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columnClasses[columnIndex];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Downloadable item = rows.get(rowIndex).getDownloadable();

		if (columnIndex == CHECKBOX_COLUMN) {
			return rows.get(rowIndex).isSelected();
		} else if (columnIndex == NAME_COLUMN) {
			return item.getUserFile().getName();
		} else if (columnIndex == STUDY_COLUMN) {
			if (item instanceof QueryToolDownloadFile)
				return "Query";
			else {
				DatasetDownloadFile file = (DatasetDownloadFile) item;
				return file.getDataset().getStudy().getTitle();
			}
		} else if (columnIndex == TYPE_COLUMN) {
			return rows.get(rowIndex).getFileType();
		} else if (columnIndex == SIZE_COLUMN) {
			Long fileSize = item.getUserFile().getSize();
			return sizeToStr(fileSize);
		} else if (columnIndex == PROGRESS_COLUMN) {
			return rows.get(rowIndex).getProgressPercent();
		} else if (columnIndex == STATUS_COLUMN) {
			return rows.get(rowIndex).getDownloadStatus();
		}
		return null;
	}

	public DownloadableRow get(int index) {
		return rows.get(index);
	}

	public ArrayList<DownloadableRow> getRows() {
		return rows;
	}

	public void clearAll() {
		rows = new ArrayList<DownloadableRow>();
		fireTableDataChanged();
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		if (isCellEditable(row, column)) {
			rows.get(row).setSelected((Boolean) value);
		}

		fireTableCellUpdated(row, column);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return column == CHECKBOX_COLUMN && checkboxEditable;
	}

	/**
	 * Used to make it easier to create a unified check header class
	 */
	/*
	 * public boolean allSelected_old() { if (rows.size() == 0) return false;
	 * 
	 * for (DownloadableRow row : rows) { if (!row.isSelected()) { return false; } } return true; }
	 */
	public int indexOf(DownloadableRow row) {
		return rows.indexOf(row);
	}

	/**
	 * Returns a string formated to be the size in bytes.
	 * 
	 * @param size - Long representation of the file's size in bytes
	 * @return
	 */
	private String sizeToStr(long size) {
		DecimalFormat byteFormatter = new DecimalFormat("#,##0.#");

		if (size < 1)
			return "0 B";

		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

		return String.format("%s %s", byteFormatter.format(size / Math.pow(1024, digitGroups)),
				FILE_SIZE_UNITS[digitGroups]);
	}

	public void setCheckboxEditable(Boolean flag) {
		checkboxEditable = flag;
	}

	public boolean isCheckboxEditable() {
		return checkboxEditable;
	}

}
