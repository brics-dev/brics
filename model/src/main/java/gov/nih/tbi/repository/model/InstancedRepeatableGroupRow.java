
package gov.nih.tbi.repository.model;

import java.io.Serializable;
import java.util.LinkedHashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Represent a single instance data row. Has a hashmap called cell that maps a value to the appropriate column
 * 
 * @author Francis Chen
 * 
 */
@XmlRootElement(name = "instancedRepeatableGroupRow")
@XmlAccessorType(XmlAccessType.FIELD)
public class InstancedRepeatableGroupRow implements Serializable {

	private static final long serialVersionUID = -2999852105163465318L;

	@XmlJavaTypeAdapter(InstancedRepeatableGroupRowAdapter.class)
	private LinkedHashMap<RepeatingCellColumn, CellValueCode> cell;

	public InstancedRepeatableGroupRow() {
		this.cell = new LinkedHashMap<RepeatingCellColumn, CellValueCode>();
	}

	public String getCellValue(RepeatingCellColumn column) {
		return getCellValue(column, CellValueCode.PERMISSIBLE_VALUE);
	}

	public String getCellValue(RepeatingCellColumn column, String displayOption) {
		if (cell.get(column) != null) {
			return cell.get(column).getValue(displayOption);
		} else {
			return null;
		}
	}

	public void insertCell(RepeatingCellColumn column, String cellValue) {
		insertCell(column, new CellValueCode(cellValue));
	}

	public void insertCell(RepeatingCellColumn column, CellValueCode cellValueCode) {
		this.cell.put(column, cellValueCode);
	}

	public LinkedHashMap<RepeatingCellColumn, CellValueCode> getCell() {
		return cell;
	}

	public void setCell(LinkedHashMap<RepeatingCellColumn, CellValueCode> cell) {
		if (cell != null) {
			this.cell = cell;
		} else {
			this.cell = new LinkedHashMap<RepeatingCellColumn, CellValueCode>();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cell == null) ? 0 : cell.hashCode());
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
		InstancedRepeatableGroupRow other = (InstancedRepeatableGroupRow) obj;
		if (cell == null) {
			if (other.cell != null)
				return false;
		} else if (!cell.equals(other.cell))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "InstancedRepeatableGroupRow [cell=" + cell + "]";
	}

}
