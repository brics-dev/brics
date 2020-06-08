package gov.nih.tbi.repository.model;

import gov.nih.tbi.commons.model.DataType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "nonRepeatingCellValue")
@XmlAccessorType(XmlAccessType.FIELD)
public class NonRepeatingCellValue extends CellValue {

	private static final long serialVersionUID = 5006317993488959656L;

	@XmlElement(name = "cellValueCode", type = CellValueCode.class)
	private CellValueCode valueCode; // value of the cell

	public NonRepeatingCellValue(DataType dataElementType, String value) {

		super(dataElementType, false);
		this.valueCode = new CellValueCode(value);
	}

	public NonRepeatingCellValue(DataType dataElementType, CellValueCode valueCode) {

		super(dataElementType, false);
		this.valueCode = valueCode;
	}

	@Override
	public DataType getDataElementType() {

		return dataElementType;
	}

	@Override
	public void setDataElementType(DataType dataElementType) {

		this.dataElementType = dataElementType;
	}

	public String getValue() {
		return valueCode.getValue(CellValueCode.PERMISSIBLE_VALUE);
	}

	public String getValue(String displayOption) {
		return valueCode.getValue(displayOption);
	}

	public void setValue(String value) {

		this.valueCode = new CellValueCode(value);
	}
	
	public CellValueCode getValueCode() {
		return valueCode;
	}

	@Override
	public int getRowSize() {
		return 1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((valueCode == null) ? 0 : valueCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NonRepeatingCellValue other = (NonRepeatingCellValue) obj;
		if (valueCode == null) {
			if (other.valueCode != null)
				return false;
		} else if (!valueCode.equals(other.valueCode))
			return false;
		return true;
	}
}
