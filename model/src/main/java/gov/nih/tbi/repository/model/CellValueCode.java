package gov.nih.tbi.repository.model;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.util.ValUtil;

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "cellValueCode")
@XmlAccessorType(XmlAccessType.FIELD)
public class CellValueCode implements Serializable {

	private static final long serialVersionUID = 2874677234985024723L;

	public final static String PERMISSIBLE_VALUE = "pv";
	public final static String OUTPUT_CODE = "outputCode";
	public final static String OUTPUT_CODE_PV = "outputCode/pv";

	@XmlAttribute
	private String value;

	@XmlAttribute
	private String outputCode;

	private Map<String, String> schemaValues;

	@XmlAttribute
	private boolean hasValueRange;

	public CellValueCode() {}

	public CellValueCode(String value) {
		this.value = value;
		this.outputCode = null;
		this.hasValueRange = false;
	}

	public CellValueCode(String value, String outputCode, boolean hasValueRange, Map<String, String> schemaValues) {
		this.value = value;
		this.outputCode = outputCode;
		this.hasValueRange = hasValueRange;
		this.schemaValues = schemaValues;
	}

	public String getValue() {
		return value;
	}

	public String getValue(String displayOption) {
		
		if (!hasValueRange || displayOption == null) {
			return value;
		}

		if (OUTPUT_CODE.equals(displayOption)) {
			return outputCode;

		} else if (OUTPUT_CODE_PV.equals(displayOption)) {
			return (ValUtil.isBlank(outputCode) ? value : outputCode);

		} else if (PERMISSIBLE_VALUE.equals(displayOption)) {
			return value;

		} else {  // Schema Translation
			String schemaValue = schemaValues.get(displayOption);
			return (schemaValue == null || (schemaValue!=null && schemaValue.equals(ModelConstants.NULL)) 
					? ModelConstants.EMPTY_STRING : schemaValue);
		}

	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getOutputCode() {
		return outputCode;
	}

	public void setOutputCode(String outputCode) {
		this.outputCode = outputCode;
	}

	public boolean isHasValueRange() {
		return hasValueRange;
	}

	public void setHasValueRange(boolean hasValueRange) {
		this.hasValueRange = hasValueRange;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (hasValueRange ? 1231 : 1237);
		result = prime * result + ((outputCode == null) ? 0 : outputCode.hashCode());
		result = prime * result + ((schemaValues == null) ? 0 : schemaValues.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		CellValueCode other = (CellValueCode) obj;
		if (hasValueRange != other.hasValueRange)
			return false;
		if (outputCode == null) {
			if (other.outputCode != null)
				return false;
		} else if (!outputCode.equals(other.outputCode))
			return false;
		if (schemaValues == null) {
			if (other.schemaValues != null)
				return false;
		} else if (!schemaValues.equals(other.schemaValues))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CellValueCode [value=" + value + ", outputCode=" + outputCode + ", schemaValues=" + schemaValues
				+ ", hasValueRange=" + hasValueRange + "]";
	}
}
