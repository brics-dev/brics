
package gov.nih.tbi.repository.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import gov.nih.tbi.commons.model.DataType;

/**
 * Represents a single cell the record. Can either be non-repeating cell or a repeating cell (button)
 * 
 * @author Francis Chen
 * 
 */
@XmlRootElement(name = "cellValue")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class CellValue implements Serializable {

	private static final long serialVersionUID = -2105972822830366761L;

	/**
	 * Type of the data element this cell represents. Basically used to generate the link to view biosample
	 */
	@XmlAttribute
	protected DataType dataElementType;

	@XmlAttribute
	protected boolean isRepeating;

	protected CellValue() {}

	public CellValue(DataType dataElementType, boolean isRepeating) {

		this.dataElementType = dataElementType;
		this.isRepeating = isRepeating;
	}

	/**
	 * Denotes whether or not this cell is a repeating cell or a non-repeating cell
	 * 
	 * @return
	 */
	public boolean getIsRepeating() {

		return isRepeating;
	}

	public DataType getDataElementType() {
		return dataElementType;
	}

	public void setDataElementType(DataType dataElementType) {
		this.dataElementType = dataElementType;
	}

	public abstract int getRowSize();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataElementType == null) ? 0 : dataElementType.hashCode());
		result = prime * result + (isRepeating ? 1231 : 1237);
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
		CellValue other = (CellValue) obj;
		if (dataElementType != other.dataElementType)
			return false;
		if (isRepeating != other.isRepeating)
			return false;
		return true;
	}
}
