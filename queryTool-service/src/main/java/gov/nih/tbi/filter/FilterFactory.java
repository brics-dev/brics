package gov.nih.tbi.filter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.exceptions.FilterException;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;

public class FilterFactory {
	private static final Logger log = LogManager.getLogger(FilterFactory.class.getName());

	public static DelimitedMultiSelectFilter createDelimitedMultiSelectFilter(FormResult form, RepeatableGroup rg,
			DataElement de, boolean blank, String delimitedValues) {
		return new DelimitedMultiSelectFilter(form, rg, de, blank, delimitedValues);
	}

	public static FreeFormFilter createFreeFormFilter(FormResult form, RepeatableGroup rg, DataElement de,
			boolean blank, String value) {
		return new FreeFormFilter(form, rg, de, blank, value);
	}

	public static RangedNumericFilter createRangedNumericFilter(FormResult form, RepeatableGroup rg, DataElement de,
			boolean blank, Double max, Double min) {
		return new RangedNumericFilter(form, rg, de, blank, max, min);
	}

	public static ChangeInDiagnosisFilter createChangeInDiagnosisFilter(String value) {
		return new ChangeInDiagnosisFilter(value);
	}

	public static DateFilter createDateFilter(FormResult form, RepeatableGroup rg, DataElement de, boolean blank,
			Date dateMax, Date dateMin) {
		return new DateFilter(form, rg, de, blank, dateMax, dateMin);
	}

	public static PermissibleValueFilter createPermissibleValueFilter(FormResult form, RepeatableGroup rg,
			DataElement de, boolean blank, List<String> values, String freeFormValue) {
		return new PermissibleValueFilter(form, rg, de, blank, values, freeFormValue);
	}

	/**
	 * Given a repeatable group and a data element, infer which DataElementFilter type this should be created for the
	 * data element and return the empty filter object.
	 * 
	 * @param rg
	 * @param de
	 * @return
	 */
	public static DataElementFilter createFilterByInference(FormResult form, RepeatableGroup rg, DataElement de) {
		if (de == null) {
			return null;
		}

		if (DataType.BIOSAMPLE.getValue().equals(de.getType()) || DataType.GUID.getValue().equals(de.getType())) {
			// delimited string filter
			return createDelimitedMultiSelectFilter(form, rg, de, false, null);
		} else if (DataType.DATE.getValue().equals(de.getType())) {
			return createDateFilter(form, rg, de, false, null, null);
		} else if (de.getMaximumValue() != null && de.getMinimumValue() != null) {
			// ranged numeric
			return createRangedNumericFilter(form, rg, de, false, null, null);
		} else if (de.hasPermissibleValues()) {
			// permissible value filter
			return createPermissibleValueFilter(form, rg, de, false, null, null);
		} else if (InputRestrictions.FREE_FORM == de.getInputRestrictions()) {
			// free form
			return createFreeFormFilter(form, rg, de, false, null);
		} else {
			return null;
		}
	}


	private static FilterType determineType(DataElement de, JsonObject filterJson) {
		if (filterJson.has("filterType")
				&& filterJson.get("filterType").getAsString().equalsIgnoreCase(FilterType.CHANGE_IN_DIAGNOSIS.name())) {
			return FilterType.CHANGE_IN_DIAGNOSIS;
		} else if (DataType.BIOSAMPLE.getValue().equals(de.getType())
				|| DataType.GUID.getValue().equals(de.getType())) {
			return FilterType.DELIMITED_MULTI_SELECT;
		} else if (filterJson.has("permissibleValues")) {
			return FilterType.PERMISSIBLE_VALUE;
		} else if (filterJson.has("maximum") && filterJson.has("minimum")) {
			return FilterType.RANGED_NUMERIC;
		} else if (filterJson.has("dateMin") && filterJson.has("dateMax")) {
			return FilterType.DATE;
		} else {
			return FilterType.FREE_FORM;
		}
	}

	public static Filter parseJson(FormResult form, RepeatableGroup rg, DataElement de, JsonObject filterJson) {
		if (log.isDebugEnabled()) {
			log.debug("Received JSON filterJson: \n" + filterJson.toString());
		}

		boolean blank = false;

		if (filterJson.has("blank")) {
			blank = filterJson.get("blank").getAsBoolean();
		}

		FilterType filterType = determineType(de, filterJson);

		List<String> permissibleValues = null;

		if (filterJson.has("permissibleValues")) {
			JsonArray pvArrayJson = filterJson.getAsJsonArray("permissibleValues");
			permissibleValues = new ArrayList<String>();
			for (int j = 0; j < pvArrayJson.size(); j++) {
				permissibleValues.add(pvArrayJson.get(j).getAsString());
			}
		}

		switch (filterType) {
			case CHANGE_IN_DIAGNOSIS:
				String value = permissibleValues.get(0);
				return FilterFactory.createChangeInDiagnosisFilter(value);
			case PERMISSIBLE_VALUE:
				// if the current data element is single-select w/ other, specify, then we will need to add the free
				// form value
				String otherSpecifyValue = null;
				if (filterJson.has("freeFormValue")) {
					otherSpecifyValue = filterJson.getAsJsonPrimitive("freeFormValue").getAsString();
				}

				return FilterFactory.createPermissibleValueFilter(form, rg, de, blank, permissibleValues,
						otherSpecifyValue);
			case RANGED_NUMERIC:
				Double maximum = filterJson.get("maximum").getAsDouble();
				Double minimum = filterJson.get("minimum").getAsDouble();
				return FilterFactory.createRangedNumericFilter(form, rg, de, blank, maximum, minimum);
			case FREE_FORM:
				JsonPrimitive jsonValue = filterJson.getAsJsonPrimitive("freeFormValue");
				String freeFormValue = null;
				
				//it is possible for the user to leave the filter value null and hit apply filter.
				if(jsonValue != null) {
					freeFormValue = filterJson.getAsJsonPrimitive("freeFormValue").getAsString();
				}
				
				return FilterFactory.createFreeFormFilter(form, rg, de, blank, freeFormValue);
			case DATE:
				String dateMaxString = filterJson.getAsJsonPrimitive("dateMax").getAsString();
				String dateMinString = filterJson.getAsJsonPrimitive("dateMin").getAsString();
				Date dateMax = BRICSTimeDateUtil.parseTwoDigitSlashDate(dateMaxString);
				Date dateMin = BRICSTimeDateUtil.parseTwoDigitSlashDate(dateMinString);
				return FilterFactory.createDateFilter(form, rg, de, blank, dateMax, dateMin);
			case DELIMITED_MULTI_SELECT:
				String delimitedValues = filterJson.getAsJsonPrimitive("freeFormValue").getAsString();
				return FilterFactory.createDelimitedMultiSelectFilter(form, rg, de, blank, delimitedValues);
			default:
				throw new FilterException("Could not determine filter type from the given JSON!");
		}
	}


	public static Filter createFilter(JaxbFilter jaxbFilter) {
		// TODO: Implement me.
		return null;
	}
}
