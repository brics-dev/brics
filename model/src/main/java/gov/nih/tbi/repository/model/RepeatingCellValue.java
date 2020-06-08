
package gov.nih.tbi.repository.model;

import gov.nih.tbi.commons.model.DataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "repeatingCellValue")
@XmlAccessorType(XmlAccessType.FIELD)
public class RepeatingCellValue extends CellValue {

	private static final long serialVersionUID = 700705330984633475L;

	/**
	 * List of repeatable group rows
	 */
	@XmlElement(name = "instancedRepeatableGroupRow")
	private List<InstancedRepeatableGroupRow> rows;

	// denotes whether or not the repeatable group has been expanded in the frontend
	@XmlTransient
	private boolean expanded;

	@XmlTransient
	private int rowCount;

	@XmlAttribute
	private int dataElementCount; // number of data elements in this repeatable group

	protected RepeatingCellValue() {
		super();
		this.rows = new ArrayList<InstancedRepeatableGroupRow>();
	}

	public RepeatingCellValue(DataType dataElementType, int dataElementCount, int rowCount) {
		super(dataElementType, true);
		this.rows = new ArrayList<InstancedRepeatableGroupRow>();
		this.dataElementCount = dataElementCount;
		this.expanded = false;
		this.rowCount = rowCount;
	}

	public RepeatingCellValue(int dataElementCount) {
		super(null, true);
		this.rows = new ArrayList<InstancedRepeatableGroupRow>();
		this.dataElementCount = dataElementCount;
		this.expanded = false;
	}

	public int getDataElementCount() {

		return dataElementCount;
	}

	public void setDataElementCount(int dataElementCount) {

		this.dataElementCount = dataElementCount;
	}

	public void addRow(InstancedRepeatableGroupRow row) {
		rows.add(row);
	}

	public void addAll(Collection<InstancedRepeatableGroupRow> rowList) {
		rows.addAll(rowList);
	}

	@Override
	public DataType getDataElementType() {

		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataElementType(DataType dataElementType) {

		throw new UnsupportedOperationException();
	}

	public void setRows(List<InstancedRepeatableGroupRow> rows) {
		this.rows.clear();

		if (rows != null) {
			this.rows.addAll(rows);
		}
	}

	public List<InstancedRepeatableGroupRow> getRows() {

		return rows;
	}

	public boolean isExpanded() {

		return expanded;
	}

	public void setExpanded(boolean expanded) {

		this.expanded = expanded;
	}

	public int getRowCount() {

		return rowCount;
	}

	public boolean hasData() {

		return rowCount > 0;
	}

	public void setRowCount(int rowCount) {

		this.rowCount = rowCount;
	}

	@Override
	public int getRowSize() {

		if (!expanded || rows.isEmpty()) {
			return 1;
		} else {
			return rows.size();
		}
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + dataElementCount;
		result = prime * result + (expanded ? 1231 : 1237);
		result = prime * result + rowCount;
		result = prime * result + ((rows == null) ? 0 : rows.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		if (obj instanceof RepeatingCellValue) {
			RepeatingCellValue rcv = (RepeatingCellValue) obj;

			return (this.dataElementCount == rcv.dataElementCount) && (this.expanded == rcv.expanded)
					&& (this.rowCount == rcv.rowCount) && this.rows.equals(rcv.rows);
		}

		return false;
	}
}
