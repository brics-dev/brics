package gov.nih.tbi.repository.table;

import java.util.HashMap;
import java.util.Map;

import gov.nih.tbi.ModelConstants;

public enum DatasetTableColumn {
	CHECK_BOX(0, ModelConstants.EMPTY_STRING, Boolean.class), NAME(1, "Name", String.class), STUDY_TITLE(2, "Study", String.class), PROGRESS(3, "Progress", String.class);

	private int columnIndex;
	private String columnName;
	private Class<?> thisClass;
	private static Map<Integer, DatasetTableColumn> columnIndexMap = new HashMap<Integer, DatasetTableColumn>();

	static {
		for (DatasetTableColumn column : DatasetTableColumn.values()) {
			columnIndexMap.put(column.getColumnIndex(), column);
		}
	}

	private DatasetTableColumn(int columnIndex, String columnName, Class<?> thisClass) {
		this.columnIndex = columnIndex;
		this.columnName = columnName;
		this.thisClass = thisClass;
	}

	public int getColumnIndex() {
		return columnIndex;
	}

	public String getColumnName() {
		return columnName;
	}
	
	

	public Class<?> getThisClass() {
		return thisClass;
	}

	public static DatasetTableColumn getByColumnIndex(int columnIndex) {
		DatasetTableColumn column = columnIndexMap.get(columnIndex);
		if (column == null) {
			throw new IndexOutOfBoundsException("Column index does not exist in the enum: " + columnIndex);
		}

		return column;
	}
}
