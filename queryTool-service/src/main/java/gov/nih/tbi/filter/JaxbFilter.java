package gov.nih.tbi.filter;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.RepeatableGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Model that represents a single filter. Contains a element and repeatable group to filter by.
 * 
 * @author Francis Chen
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement()
public class JaxbFilter implements Serializable {
	private static final long serialVersionUID = 8316978995099712048L;

	private DataElement element;
	private RepeatableGroup group;

	@XmlElementWrapper(name = "permissibleValues")
	@XmlElement(name = "permissibleValue", type = String.class)
	private List<String> permissibleValues;

	private Double maximum;
	private Double minimum;
	private String freeFormValue;
	private Date dateMin;
	private Date dateMax;
	private boolean blank;
	private FilterType filterType;
	
	public JaxbFilter() {
		this.element = new DataElement();
		this.group = new RepeatableGroup();
		this.permissibleValues = new ArrayList<String>();
		this.maximum = null;
		this.minimum = null;
		this.freeFormValue = "";
		this.dateMin = null;
		this.dateMax = null;
		this.blank = true;
	}

	public JaxbFilter(DataElement element) {
		initializeMinMax(element);
		this.element = (element != null ? element : new DataElement());
		this.group = new RepeatableGroup();
		this.permissibleValues = new ArrayList<String>();
		this.freeFormValue = "";
		this.dateMin = null;
		this.dateMax = null;
		this.blank = true;
	}

	public JaxbFilter(RepeatableGroup group, DataElement element) {
		initializeMinMax(element);
		this.group = (group != null ? group : new RepeatableGroup());
		this.element = (element != null ? element : new DataElement());
		this.permissibleValues = new ArrayList<String>();
		this.freeFormValue = "";
		this.dateMin = null;
		this.dateMax = null;
		this.blank = true;
	}

	/**
	 * Creates a new Filter instance that is a clone of the given base object. When cloning the list of permissible
	 * values, a new list will be created with the elements of the base list.
	 * 
	 * @param base - The base Filter object used for the cloning process.
	 * @throws NullPointerException When the argument is null.
	 */
	public JaxbFilter(JaxbFilter base) throws NullPointerException {
		if (base == null) {
			throw new NullPointerException("The base agrument cannot be null.");
		}

		this.blank = base.blank;
		this.dateMax = (base.dateMax != null ? (Date) base.dateMax.clone() : null);
		this.dateMin = (base.dateMin != null ? (Date) base.dateMin.clone() : null);
		this.element = new DataElement(base.element);
		this.freeFormValue = base.freeFormValue;
		this.group = new RepeatableGroup(base.group);
		this.maximum = base.maximum;
		this.minimum = base.minimum;
		this.permissibleValues = new ArrayList<String>(base.permissibleValues);
		this.filterType = base.filterType;
	}

	public FilterType getFilterType() {
		return filterType;
	}

	public void setFilterType(FilterType filterType) {
		this.filterType = filterType;
	}

	public JsonObject toJson() {
		JsonObject filterJson = new JsonObject();

		filterJson.addProperty("formUri", group.getUri());
		filterJson.addProperty("elementUri", element.getUri());
		filterJson.addProperty("freeFormValue", freeFormValue);
		filterJson.addProperty("maximum", maximum);
		filterJson.addProperty("minimum", minimum);
		filterJson.addProperty("blank", blank);

		String maxString = BRICSTimeDateUtil.dateToDateString(dateMax);
		String minString = BRICSTimeDateUtil.dateToDateString(dateMin);
		filterJson.addProperty("dateMax", maxString);
		filterJson.addProperty("dateMin", minString);
		filterJson.addProperty("filterType", filterType.name());

		JsonArray pvJson = new JsonArray();
		for (String pv : getPermissibleValues()) {
			pvJson.add(new JsonPrimitive(pv));
		}
		filterJson.add("permissibleValues", pvJson);

		return filterJson;
	}

	private void initializeMinMax(DataElement element) {
		if (element != null && element.getMinimumValue() != null && element.getMaximumValue() != null) {
			maximum = element.getMaximumValue();
			minimum = element.getMinimumValue();
		} else {
			maximum = null;
			minimum = null;
		}
	}

	public DataElement getElement() {
		return element;
	}

	public void setElement(DataElement element) {
		this.element = element != null ? element : new DataElement();
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

	public boolean isNumeric() {
		return (minimum != null) && (maximum != null);
	}

	public boolean isDate() {
		return (dateMin != null) && (dateMax != null);
	}

	public boolean isEmpty() {
		return minimum == null && maximum == null && permissibleValues.isEmpty() && freeFormValue.isEmpty()
				&& dateMin == null && dateMax == null && blank;
	}

	public List<String> getPermissibleValues() {
		return permissibleValues;
	}

	public void setPermissibleValues(List<String> permissibleValues) {
		if (permissibleValues != null) {
			this.permissibleValues = permissibleValues;
		} else {
			this.permissibleValues = new ArrayList<String>();
		}
	}

	public String getFreeFormValue() {
		return freeFormValue;
	}

	public void setFreeFormValue(String freeFormValue) {
		this.freeFormValue = freeFormValue != null ? freeFormValue : "";
	}

	public Date getDateMin() {
		return dateMin;
	}

	public boolean isBlank() {
		return blank;
	}

	public boolean getBlank() {
		return blank;
	}

	public void setBlank(boolean blank) {
		this.blank = blank;
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

	public boolean isMultiSelect() {
		return InputRestrictions.MULTIPLE.equals(element.getInputRestrictions());
	}

	public boolean isSingleSelect() {
		return InputRestrictions.SINGLE.equals(element.getInputRestrictions());
	}

	
	public boolean isFreeForm() {
		// note this isn't a simple InputRestrictions check because other, specify
		// filters are listed with an InputRestriction as free form and this would
		// capture those too
		if (this.permissibleValues == null) {
			return true;
		}
		return this.permissibleValues.isEmpty();
	}
	
	public boolean isMultiSelectOrCombo() {
		return InputRestrictions.MULTIPLE.equals(element.getInputRestrictions()) 
				|| ! isFreeForm();
	}

	public void clear() {
		this.dateMax = null;
		this.dateMin = null;
		this.element = new DataElement();
		this.group = new RepeatableGroup();
		this.maximum = null;
		this.minimum = null;
		this.permissibleValues.clear();
		this.freeFormValue = "";
		this.blank = false;
	}

	public RepeatableGroup getGroup() {
		return group;
	}

	public void setGroup(RepeatableGroup group) {
		this.group = group != null ? group : new RepeatableGroup();
	}

	/**
	 * Returns true if this filter has a value to filter on, false otherwise.
	 * 
	 * @return
	 */
	public boolean hasValue() {
		return !(this.dateMin == null && this.dateMax == null && this.permissibleValues.isEmpty()
				&& this.freeFormValue.isEmpty() && this.minimum == null && this.maximum == null);
	}

	/**
	 * Implementing support for determining equality of Filter instances. The equality tests will involve all non-static
	 * fields of this class.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		if (obj instanceof JaxbFilter) {
			JaxbFilter of = (JaxbFilter) obj;

			return this.element.equals(of.element) && this.group.equals(of.group)
					&& this.freeFormValue.equals(of.freeFormValue)
					&& this.permissibleValues.equals(of.permissibleValues) && (this.blank == of.blank)
					&& (this.minimum == of.minimum || (this.minimum != null && this.minimum.equals(of.minimum)))
					&& (this.maximum == of.maximum || (this.maximum != null && this.maximum.equals(of.maximum)))
					&& (this.dateMax == of.dateMax || (this.dateMax != null && this.dateMax.equals(of.dateMax)))
					&& (this.dateMin == of.dateMin || (this.dateMin != null && this.dateMin.equals(of.dateMin)));
		}

		return false;
	}

	/**
	 * Implementing the calculation of hash codes for Filter instances. The returned value will be based on the hashes
	 * of all non-static fields of this class.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + (blank ? 1231 : 1237);
		result = prime * result + ((dateMax == null) ? 0 : dateMax.hashCode());
		result = prime * result + ((dateMin == null) ? 0 : dateMin.hashCode());
		result = prime * result + ((element == null) ? 0 : element.hashCode());
		result = prime * result + ((freeFormValue == null) ? 0 : freeFormValue.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((maximum == null) ? 0 : maximum.hashCode());
		result = prime * result + ((minimum == null) ? 0 : minimum.hashCode());
		result = prime * result + ((permissibleValues == null) ? 0 : permissibleValues.hashCode());

		return result;
	}
}
