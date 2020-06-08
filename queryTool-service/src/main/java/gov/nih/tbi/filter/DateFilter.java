package gov.nih.tbi.filter;

import java.io.Serializable;
import java.util.Date;

import com.google.gson.JsonObject;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;

public class DateFilter extends DataElementFilter implements Filter, Serializable {
	private static final long serialVersionUID = 8610505275286107469L;

	private Date dateMin;
	private Date dateMax;

	public DateFilter(FormResult form, RepeatableGroup group, DataElement element, Date dateMax, Date dateMin,
			String name, String logicBefore, Integer groupingBefore, Integer groupingAfter) {
		super(form, group, element, name, logicBefore, groupingBefore, groupingAfter);
		this.dateMax = dateMax;
		this.dateMin = dateMin;
	}

	public Date getDateMin() {
		return dateMin;
	}

	public void setDateMin(Date dateMin) {
		this.dateMin = dateMin;
	}

	public Date getDateMax() {
		return dateMax;
	}

	public void setDateMax(Date dateMax) {
		this.dateMax = dateMax;
	}

	@Override
	public JsonObject toJson() {
		JsonObject filterJson = super.toJson();

		String maxString = BRICSTimeDateUtil.dateToStringUtc(dateMax);
		String minString = BRICSTimeDateUtil.dateToStringUtc(dateMin);
		filterJson.addProperty("dateMax", maxString);
		filterJson.addProperty("dateMin", minString);

		return filterJson;
	}

	@Override
	public FilterType getFilterType() {
		return FilterType.DATE;
	}

	@Override
	public boolean isEmpty() {
		return dateMin == null || dateMax == null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((dateMax == null) ? 0 : dateMax.hashCode());
		result = prime * result + ((dateMin == null) ? 0 : dateMin.hashCode());
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
		DateFilter other = (DateFilter) obj;
		if (dateMax == null) {
			if (other.dateMax != null)
				return false;
		} else if (!dateMax.equals(other.dateMax))
			return false;
		if (dateMin == null) {
			if (other.dateMin != null)
				return false;
		} else if (!dateMin.equals(other.dateMin))
			return false;
		return true;
	}

	@Override
	public boolean evaluate(String cellValue) {
		if (cellValue == null || cellValue.isEmpty()) {
			return false;
		}

		Date cellDateValue = BRICSTimeDateUtil.zuluStringToDate(cellValue);

		if (dateMax == null && dateMin == null) {
			return true;
		}

		return cellDateValue.compareTo(dateMin) >= 0 && cellDateValue.compareTo(dateMax) <= 0;
	}

	public String toString() {
		final String queryFormat = "(%s >= %s AND %s <= %s)";

		String output = QueryToolConstants.EMPTY_STRING;

		if (!isEmpty()) {
			String maxString = BRICSTimeDateUtil.dateToStringUtc(dateMax);
			String minString = BRICSTimeDateUtil.dateToStringUtc(dateMin);
			output +=
					String.format(queryFormat, getReadableFilterName(), minString, getReadableFilterName(), maxString);
		}

		return output;
	}
}
