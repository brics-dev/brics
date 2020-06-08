package gov.nih.tbi.filter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.exceptions.FilterParseException;
import gov.nih.tbi.filter.DelimitedMultiSelectFilter.DelimitedMultiSelectMode;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;

public class FilterFactory {
	private static final Logger log = LogManager.getLogger(FilterFactory.class.getName());

	/**
	 * Given a repeatable group and a data element, infer which DataElementFilter type this should be created for the
	 * data element and return the empty filter object.
	 * 
	 * @param rg
	 * @param de
	 * @return
	 */
	public static DataElementFilter createFilterByInference(FormResult form, RepeatableGroup rg, DataElement de,
			String name) {
		if (de == null) {
			return null;
		}

		if (DataType.BIOSAMPLE == de.getType() || DataType.GUID == de.getType()) {
			// delimited string filter
			return new DelimitedMultiSelectFilter(form, rg, de, null, name, null, null, null,
					DelimitedMultiSelectMode.INCLUSIVE);
		} else if (DataType.DATE == de.getType()) {
			return new DateFilter(form, rg, de, null, null, name, null, null, null);
		} else if (de.getMaximumValue() != null && de.getMinimumValue() != null) {
			// ranged numeric
			return new RangedNumericFilter(form, rg, de, null, null, name, null, null, null);
		} else if (de.hasPermissibleValues()) {
			switch (de.getInputRestrictions()) {
				case SINGLE:
				case FREE_FORM:
					return new SingleSelectFilter(form, rg, de, null, null, name, null, null, null);
				case MULTIPLE:
					return new MultiSelectFilter(form, rg, de, null, null, name, FilterMode.INCLUSIVE, null, null,
							null);
				default:
					throw new UnsupportedOperationException(
							de.getInputRestrictions().getValue() + " is an unsupported input restriction.");
			}

		} else if (InputRestrictions.FREE_FORM == de.getInputRestrictions()) {
			// free form
			return new FreeFormFilter(form, rg, de, null, name, null, null, null, FilterMode.INCLUSIVE);
		} else {
			return null;
		}
	}

	public static Filter parseJson(FormResult form, RepeatableGroup rg, DataElement de, JsonObject filterJson)
			throws FilterParseException {
		if (log.isDebugEnabled()) {
			log.debug("Received JSON filterJson: \n" + filterJson.toString());
		}

		String filterTypeString = filterJson.getAsJsonPrimitive("filterJavaType").getAsString();
		FilterType filterType = FilterType.valueOf(filterTypeString);

		List<String> permissibleValues = null;

		if (filterJson.has("permissibleValues")) {
			JsonArray pvArrayJson = filterJson.getAsJsonArray("permissibleValues");
			permissibleValues = new ArrayList<String>();
			for (int j = 0; j < pvArrayJson.size(); j++) {
				permissibleValues.add(pvArrayJson.get(j).getAsString());
			}
		}

		String freeFormValue = null;
		if (filterJson.has("freeFormValue")) {
			freeFormValue = filterJson.getAsJsonPrimitive("freeFormValue").getAsString();
		}

		// TODO: this will probably be changed, not sure what ryan will call the filter
		// name field
		String name = filterJson.get("name").getAsString();

		String logicBefore = null;
		if (filterJson.has("logicBefore")) {
			logicBefore = filterJson.getAsJsonPrimitive("logicBefore").getAsString();
		}

		Integer groupingBefore = null;
		if (filterJson.has("groupingBefore")) {
			groupingBefore = filterJson.getAsJsonPrimitive("groupingBefore").getAsInt();
		}

		Integer groupingAfter = null;
		if (filterJson.has("groupingAfter")) {
			groupingAfter = filterJson.getAsJsonPrimitive("groupingAfter").getAsInt();
		}

		switch (filterType) {
			case DATASET:
				return new DatasetFilter(name, freeFormValue, logicBefore, groupingBefore, groupingAfter);
			case CHANGE_IN_DIAGNOSIS:
				String value = permissibleValues.get(0);
				return new ChangeInDiagnosisFilter(value, name, logicBefore, groupingBefore, groupingAfter);
			case SINGLE_SELECT:
				return new SingleSelectFilter(form, rg, de, permissibleValues, freeFormValue, name, logicBefore,
						groupingBefore, groupingAfter);
			case MULTI_SELECT:
				if (!filterJson.has("mode")) {
					throw new FilterParseException("Mode property is required for multi-select filters.");
				}

				FilterMode multiSelectMode = FilterMode.valueOf(filterJson.get("mode").getAsString().toUpperCase());

				boolean multiData = false;

				if (filterJson.has("multiData")) {
					multiData = filterJson.get("multiData").getAsBoolean();
				}

				return new MultiSelectFilter(form, rg, de, permissibleValues, freeFormValue, name, multiSelectMode,
						multiData, logicBefore, groupingBefore, groupingAfter);
			case RANGED_NUMERIC:
				Double maximum = null;
				Double minimum = null;

				if (filterJson.has("maximum")) {
					maximum = filterJson.get("maximum").getAsDouble();
				}

				if (filterJson.has("minimum")) {
					minimum = filterJson.get("minimum").getAsDouble();
				}
				return new RangedNumericFilter(form, rg, de, maximum, minimum, name, logicBefore, groupingBefore,
						groupingAfter);
			case MULTI_RANGE:
				return new MultiRangeFilter(form, rg, de, permissibleValues, name, logicBefore, groupingBefore,
						groupingAfter);
			case FREE_FORM:
				if (!filterJson.has("mode")) {
					throw new FilterParseException("Mode property is required for free-form filters.");
				}

				FilterMode freeFormMode = FilterMode.valueOf(filterJson.get("mode").getAsString().toUpperCase());

				return new FreeFormFilter(form, rg, de, freeFormValue, name, logicBefore, groupingBefore, groupingAfter,
						freeFormMode);
			case DATE:
				String dateMaxString = filterJson.getAsJsonPrimitive("dateMax").getAsString();
				String dateMinString = filterJson.getAsJsonPrimitive("dateMin").getAsString();
				Date dateMax = BRICSTimeDateUtil.parseTwoDigitSlashDate(dateMaxString);
				Date dateMin = BRICSTimeDateUtil.parseTwoDigitSlashDate(dateMinString);
				return new DateFilter(form, rg, de, dateMax, dateMin, name, logicBefore, groupingBefore, groupingAfter);
			case DELIMITED_MULTI_SELECT:
				if (!filterJson.has("mode")) {
					throw new FilterParseException("Mode property is required for delimited multi-select filters.");
				}

				DelimitedMultiSelectMode delimitedMultiSelectMode =
						DelimitedMultiSelectMode.valueOf(filterJson.get("mode").getAsString().toUpperCase());

				return new DelimitedMultiSelectFilter(form, rg, de, freeFormValue, name, logicBefore, groupingBefore,
						groupingAfter, delimitedMultiSelectMode);
			case SHOW_BLANKS:
				return new ShowBlanksFilter(form, rg, de, name, logicBefore, groupingBefore, groupingAfter);
			default:
				throw new UnsupportedOperationException(
						filterType.name() + " is unsupported in FilterFactory.parseJson");
		}
	}

	public static Filter createFilter(JaxbFilter jaxbFilter) {
		// TODO: Implement me.
		return null;
	}
}
