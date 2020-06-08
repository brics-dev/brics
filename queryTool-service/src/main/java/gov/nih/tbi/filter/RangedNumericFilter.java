package gov.nih.tbi.filter;

import java.io.Serializable;

import com.google.gson.JsonObject;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;

public class RangedNumericFilter extends DataElementFilter implements Filter, Serializable {
	private static final long serialVersionUID = 2509973278753739091L;

	private Double maximum;
	private Double minimum;

	public RangedNumericFilter(FormResult form, RepeatableGroup group, DataElement element, Double maximum,
			Double minimum, String name, String logicBefore, Integer groupingBefore, Integer groupingAfter) {
		super(form, group, element, name, logicBefore, groupingBefore, groupingAfter);
		this.maximum = maximum;
		this.minimum = minimum;
	}

	public Double getMaximum() {
		return maximum;
	}

	public void setMaximum(Double maximum) {
		this.maximum = maximum;
	}

	public Double getMinimum() {
		return minimum;
	}

	public void setMinimum(Double minimum) {
		this.minimum = minimum;
	}

	@Override
	public JsonObject toJson() {
		JsonObject filterJson = super.toJson();

		filterJson.addProperty("maximum", maximum);
		filterJson.addProperty("minimum", minimum);

		return filterJson;
	}

	@Override
	public FilterType getFilterType() {
		return FilterType.RANGED_NUMERIC;
	}

	@Override
	public boolean isEmpty() {
		return maximum == null || minimum == null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((maximum == null) ? 0 : maximum.hashCode());
		result = prime * result + ((minimum == null) ? 0 : minimum.hashCode());
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
		RangedNumericFilter other = (RangedNumericFilter) obj;
		if (maximum == null) {
			if (other.maximum != null)
				return false;
		} else if (!maximum.equals(other.maximum))
			return false;
		if (minimum == null) {
			if (other.minimum != null)
				return false;
		} else if (!minimum.equals(other.minimum))
			return false;
		return true;
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean evaluate(String cellValue) {
		if (cellValue == null || cellValue.isEmpty()) {
			return false;
		}

		Double doubleCellValue = Double.valueOf(cellValue);

		boolean output = true;

		if (maximum != null) {
			output = output && doubleCellValue <= maximum;
		}

		if (minimum != null) {
			output = output && doubleCellValue >= minimum;
		}

		return output;
	}


	@Override
	public String toString() {
		String output = QueryToolConstants.EMPTY_STRING;

		if (!isEmpty()) {
			// filter name, minimum, filter name, maximum
			final String queryFormat = "(%s >= %.2f AND %s <= %.2f)";
			output += String.format(queryFormat, getReadableFilterName(), getMinimum(), getReadableFilterName(),
					getMaximum());
		}

		return output;
	}
}
