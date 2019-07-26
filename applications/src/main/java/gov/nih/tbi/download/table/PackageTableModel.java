package gov.nih.tbi.download.table;

import gov.nih.tbi.repository.model.DownloadPackageOrigin;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * The table model class used to display information at the macro-level. Contains the download packages.
 * 
 * @author wangvg
 * 
 */
public class PackageTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -4409379703938445414L;

	public final static int CHECKBOX_COLUMN = 0;
	public final static int NAME_COLUMN = 1;
	public final static int DATE_COLUMN = 2;
	public final static int FILES_COLUMN = 3;
	public final static int SIZE_COLUMN = 4;
	public final static int PROGRESS_COLUMN = 5;
	public final static int STATUS_COLUMN = 6;
	public final static int ORIGIN_COLUMN = 7;

	private static boolean checkboxEditable = true;

	private final static String[] columnNames = new String[] {"", "Name", "Date Added", "Files", "Size", "Progress",
			"Status", "Origin"};

	private final static Class<?>[] columnClasses = new Class[] {Boolean.class, String.class, String.class,
			String.class, String.class, Float.class, String.class, String.class};

	private final String[] FILE_SIZE_UNITS = new String[] {"B", "KB", "MB", "GB", "TB"};

	private ArrayList<PackageRow> rows;

	public PackageTableModel() {
		rows = new ArrayList<PackageRow>();
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

		PackageRow rowItem = rows.get(rowIndex);
		DownloadPackage item = rowItem.getPackage();

		if (columnIndex == CHECKBOX_COLUMN) {
			return rowItem.isSelected();
		} else if (columnIndex == NAME_COLUMN) {
			return item.getName();
		} else if (columnIndex == DATE_COLUMN) {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
			String date = dateFormatter.format(item.getDateAdded());
			return date;
		} else if (columnIndex == ORIGIN_COLUMN) {
			// TODO: temporary solution for the consistent display origin type. best solution is to fix the database
			// and use numeric origin type.
			String origin = item.getOrigin().name();
			if (origin.equalsIgnoreCase(DownloadPackageOrigin.DATASET.toString())) {
				return "Data Repository";
			} else if (origin.equalsIgnoreCase(DownloadPackageOrigin.QUERY_TOOL.toString())) {
				return "Query Tool";
			}
			return item.getOrigin().name();
		} else if (columnIndex == FILES_COLUMN) {
			String totalFiles = String.valueOf(rowItem.getFiles().size());
			String selectedFiles = String.valueOf(rowItem.getSelectedNumber());
			String outputString = selectedFiles + "/" + totalFiles;
			return outputString;
		} else if (columnIndex == SIZE_COLUMN) {
			Long fileSize = rowItem.getTotalFileSize();
			Long selectedSize = 0L;
			for (DownloadableRow row : rowItem.getFiles()) {
				if (row.isSelected()) {
					selectedSize += row.getSize();
				}
			}

			String selectedSizeString;
			if (selectedSize == 0L) {
				selectedSizeString = "0";
			} else {
				int digitGroups = (int) (Math.log10(fileSize) / Math.log10(1024));
				double adjustedSize = selectedSize / Math.pow(1024, digitGroups);
				selectedSizeString = String.format("%4.1f", adjustedSize);
			}
			String fileSizeString = sizeToStr(fileSize);
			String outputString = selectedSizeString + "/" + fileSizeString;
			return outputString;
		} else if (columnIndex == PROGRESS_COLUMN) {
			return rowItem.getProgress();
		} else if (columnIndex == STATUS_COLUMN) {
			return rowItem.getStatus().getName();
		}
		return null;
	}

	public PackageRow get(int index) {
		return rows.get(index);
	}

	public void add(PackageRow pkg) {
		int index = rows.size();
		rows.add(pkg);
		fireTableRowsInserted(index, index);
	}

	public ArrayList<PackageRow> getRows() {
		return rows;
	}

	public boolean isRowSelected(int row) {
		return rows.get(row).isSelected();
	}

	public void clearAll() {
		int size = rows.size();
		if (size > 0) {
			rows.clear();
			fireTableRowsDeleted(0, size - 1);
		}
	}


	public int getSelectedNumber() {
		int cnt = 0;
		for (PackageRow item : rows) {
			if (item.isSelected())
				cnt++;
		}
		return cnt;
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

	public void removeByDownloadableIds(List<Long> ids) {
		Iterator<PackageRow> iter = rows.iterator();
		while (iter.hasNext()) {
			PackageRow row = iter.next();
			Long id = row.getPackage().getId();
			if (ids.contains(id)) {
				iter.remove();
			}
		}

		fireTableDataChanged();
	}

	public int indexOf(PackageRow item) {
		return rows.indexOf(item);
	}

	public String getSelectedSize() {
		long size = 0L;
		for (PackageRow pkg : rows) {
			if (pkg.isSelected()) {
				for (DownloadableRow row : pkg.getFiles()) {
					if (row.isSelected()) {
						size += row.getSize();
					}
				}
			}
		}
		return sizeToStr(size);
	}

	public void setCheckboxEditable(Boolean flag) {
		checkboxEditable = flag;
	}

	public boolean isCheckboxEditable() {
		return checkboxEditable;
	}
}
