package gov.nih.tbi.pojo;

import gov.nih.tbi.repository.model.DataTableColumn;

import java.io.Serializable;

public class CellPosition implements Serializable {
	private static final long serialVersionUID = 5753816551021059790L;

	private DataTableColumn column;
	private String rowUri;

	public CellPosition(DataTableColumn column, String rowUri) {
		super();
		this.column = column;
		this.rowUri = rowUri;
	}

	public DataTableColumn getColumn() {
		return column;
	}

	public void setColumn(DataTableColumn column) {
		this.column = column;
	}

	public String getRowUri() {
		return rowUri;
	}

	public void setRowUri(String rowUri) {
		this.rowUri = rowUri;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((column == null) ? 0 : column.hashCode());
		result = prime * result + ((rowUri == null) ? 0 : rowUri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CellPosition other = (CellPosition) obj;
		if (column == null) {
			if (other.column != null)
				return false;
		} else if (!column.equals(other.column))
			return false;
		if (rowUri == null) {
			if (other.rowUri != null)
				return false;
		} else if (!rowUri.equals(other.rowUri))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CellPosition [column=" + column + ", rowUri=" + rowUri + "]";
	}
}
