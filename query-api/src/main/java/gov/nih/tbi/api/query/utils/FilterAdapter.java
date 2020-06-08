package gov.nih.tbi.api.query.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import gov.nih.tbi.api.query.exception.ApiEntityNotFoundException;
import gov.nih.tbi.api.query.model.Operator;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.filter.DateFilter;
import gov.nih.tbi.filter.DelimitedMultiSelectFilter;
import gov.nih.tbi.filter.Filter;
import gov.nih.tbi.filter.FilterFactory;
import gov.nih.tbi.filter.FreeFormFilter;
import gov.nih.tbi.filter.MultiSelectFilter;
import gov.nih.tbi.filter.RangedNumericFilter;
import gov.nih.tbi.filter.SingleSelectFilter;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;

public class FilterAdapter {
	private List<FormResult> formResults;
	private List<gov.nih.tbi.api.query.model.Filter> apiFilters;

	public FilterAdapter(List<FormResult> formResults, List<gov.nih.tbi.api.query.model.Filter> apiFilters) {
		this.formResults = formResults;
		this.apiFilters = apiFilters;
		initializeFilterNames();
	}

	public List<FormResult> getFormResults() {
		return formResults;
	}

	public void setFormResults(List<FormResult> formResults) {
		this.formResults = formResults;
	}

	public List<gov.nih.tbi.api.query.model.Filter> getApiFilters() {
		return apiFilters;
	}

	public void setApiFilters(List<gov.nih.tbi.api.query.model.Filter> apiFilters) {
		this.apiFilters = apiFilters;
	}

	/**
	 * This method will initialize the filters with unique names. Uniqueness is enforced by appending $<i>number</i> to
	 * the end, where the number gets incremented (starting from 1) every time it finds an existing name.
	 */
	protected void initializeFilterNames() {
		List<Integer> endingNums = new ArrayList<Integer>();

		for (gov.nih.tbi.api.query.model.Filter apiFilter : apiFilters) {
			String currentName = generateFilterName(apiFilter);
			String baseName = currentName;
			Integer currentNum = 0;

			while (endingNums.contains(currentNum)) {
				currentNum++;
				currentName = baseName + "_" + currentNum;
			}

			endingNums.add(currentNum);
			apiFilter.setName(currentName);
		}
	}

	protected static String generateFilterName(gov.nih.tbi.api.query.model.Filter apiFilter) {
		final String FILTER_FORMAT = "%s_%s_%s";
		String currentName = String.format(FILTER_FORMAT, apiFilter.getForm(), apiFilter.getRepeatableGroup(),
				apiFilter.getDataElement());
		currentName = currentName.replaceAll("[^A-Za-z0-9_]", "\\$");
		return currentName;
	}

	/**
	 * Convert the API filters into query tool filters. Add the filters to the appropriate form result.
	 */
	public void adaptFilters() {
		for (gov.nih.tbi.api.query.model.Filter apiFilter : apiFilters) {
			FormResult currentForm = null;
			for (FormResult formResult : formResults) {
				if (formResult.getShortName().equals(apiFilter.getForm())) {
					currentForm = formResult;
					break;
				}
			}

			if (currentForm == null) {
				throw new ApiEntityNotFoundException("Form structure not found: " + apiFilter.getForm());
			}

			RepeatableGroup rg = currentForm.getGroupByName(apiFilter.getRepeatableGroup());

			if (rg == null) {
				throw new ApiEntityNotFoundException("Repeatable group not found: " + apiFilter.getRepeatableGroup());
			}

			DataElement de = currentForm.getElement(apiFilter.getDataElement());

			if (de == null) {
				throw new ApiEntityNotFoundException("Data element not found: " + apiFilter.getDataElement());
			}

			Filter filter = FilterFactory.createFilterByInference(currentForm, rg, de, apiFilter.getName());

			switch (filter.getFilterType()) {
				case DATE:
					DateFilter dateFilter = (DateFilter) filter;
					if (apiFilter.getRangeStart() != null && apiFilter.getRangeEnd() != null) {
						Date startDate = BRICSTimeDateUtil.formatMonthYearInISO(apiFilter.getRangeStart());
						Date endDate = BRICSTimeDateUtil.formatMonthYearInISO(apiFilter.getRangeEnd());
						dateFilter.setDateMin(startDate);
						dateFilter.setDateMax(endDate);
					}
					currentForm.addFilter(dateFilter);
					break;
				case DELIMITED_MULTI_SELECT:
					DelimitedMultiSelectFilter delimitedMultiselectFilter = (DelimitedMultiSelectFilter) filter;
					if (!apiFilter.getValue().isEmpty()) {
						delimitedMultiselectFilter.setValues(apiFilter.getValue());
					}
					currentForm.addFilter(delimitedMultiselectFilter);
					break;
				case FREE_FORM:
					FreeFormFilter freeFormFilter = (FreeFormFilter) filter;
					if (!apiFilter.getValue().isEmpty()) {
						freeFormFilter.setValue(apiFilter.getValue().get(0));
					}
					currentForm.addFilter(freeFormFilter);
					break;
				case MULTI_SELECT:
					// TODO: Add in mode select
					MultiSelectFilter multiselectFilter = (MultiSelectFilter) filter;
					if (!apiFilter.getValue().isEmpty()) {
						multiselectFilter.setValues(apiFilter.getValue());
					}
					currentForm.addFilter(multiselectFilter);
					break;
				case RANGED_NUMERIC:
					RangedNumericFilter rangedNumericFilter = (RangedNumericFilter) filter;
					if (apiFilter.getRangeStart() != null && apiFilter.getRangeEnd() != null) {
						rangedNumericFilter.setMinimum(Double.valueOf(apiFilter.getRangeStart()));
						rangedNumericFilter.setMaximum(Double.valueOf(apiFilter.getRangeEnd()));
					}
					currentForm.addFilter(rangedNumericFilter);
					break;
				case SINGLE_SELECT:
					SingleSelectFilter singleSelectFilter = (SingleSelectFilter) filter;
					if (!apiFilter.getValue().isEmpty()) {
						singleSelectFilter.setValues(apiFilter.getValue());
					}
					currentForm.addFilter(singleSelectFilter);
					break;
				default:
					throw new UnsupportedOperationException();
			}
		}
	}

	/**
	 * Builds the filter boolean expression that query tool will use to filter the data.
	 * 
	 * @return
	 */
	public String buildExpression() {

		// Here, we are going to group the filters by form structure + repeatable group + data element. The purpose of
		// grouping them is so we can add them to the same precedence group so that they are all evaluated at the same
		// time. This is similar to how it is done in the real query tool.
		ListMultimap<String, gov.nih.tbi.api.query.model.Filter> filterGroups = ArrayListMultimap.create();

		for (gov.nih.tbi.api.query.model.Filter filter : apiFilters) {
			String groupName = filter.getForm() + filter.getRepeatableGroup() + filter.getDataElement();
			filterGroups.put(groupName, filter);
		}

		StringBuilder sb = new StringBuilder();
		Collection<Entry<String, Collection<gov.nih.tbi.api.query.model.Filter>>> filterEntries =
				filterGroups.asMap().entrySet();

		Iterator<Entry<String, Collection<gov.nih.tbi.api.query.model.Filter>>> filterEntriesIt =
				filterEntries.iterator();

		while (filterEntriesIt.hasNext()) {

			List<gov.nih.tbi.api.query.model.Filter> currentFilters =
					new ArrayList<>(filterEntriesIt.next().getValue());

			String subExpression = buildSubExpression(currentFilters);

			Operator operator = currentFilters.get(currentFilters.size() - 1).getOperator();
			sb.append("(").append(subExpression).append(")");

			// only append the operator if we are not at the last filter group
			if (filterEntriesIt.hasNext()) {
				sb.append(" ").append(operator.getBooleanValue()).append(" ");
			}
		}

		return sb.toString();
	}

	/**
	 * This method is intended to build the filter expression for one filter group. A filter group is a collection of
	 * filters for the same repeatable group and data element.
	 * 
	 * @param filters
	 * @return
	 */
	protected String buildSubExpression(List<gov.nih.tbi.api.query.model.Filter> filters) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < filters.size(); i++) {
			gov.nih.tbi.api.query.model.Filter apiFilter = filters.get(i);
			String start = "";
			String end = "";

			if (apiFilter.isNegation()) {
				start = "!(";
				end = ")";
			}

			if (apiFilter.getPrecedenceStart() != null) {
				start = apiFilter.getPrecedenceStart().toString() + start;
			}

			if (apiFilter.getPrecedenceEnd() != null) {
				end = end + apiFilter.getPrecedenceEnd().toString();
			}

			sb.append(start);
			sb.append(apiFilter.getName());
			sb.append(end);

			// only append the operator if we are not at the last filter
			if (i != (filters.size() - 1)) {
				sb.append(" ").append(apiFilter.getOperator().getBooleanValue()).append(" ");
			}
		}

		return sb.toString();
	}
}
