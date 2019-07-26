package gov.nih.tbi.repository.model;

import gov.nih.tbi.ModelConstants;

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

	public NonRepeatingCellValue(String value) {

		super(ModelConstants.EMPTY_STRING, false);
		this.valueCode = new CellValueCode(value);
	}

	public NonRepeatingCellValue(CellValueCode valueCode) {

		super(ModelConstants.EMPTY_STRING, false);
		this.valueCode = valueCode;
	}

	public NonRepeatingCellValue(String dataElementType, String value) {

		super(dataElementType, false);
		this.valueCode = new CellValueCode(value);
	}

	public NonRepeatingCellValue(String dataElementType, CellValueCode valueCode) {

		super(dataElementType, false);
		this.valueCode = valueCode;
	}

	@Override
	public String getDataElementType() {

		return dataElementType;
	}

	@Override
	public void setDataElementType(String dataElementType) {

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
}
