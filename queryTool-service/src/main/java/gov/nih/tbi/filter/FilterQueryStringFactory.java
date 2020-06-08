package gov.nih.tbi.filter;

import java.util.List;
import java.util.regex.Matcher;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.exceptions.FilterQueryStringException;
import gov.nih.tbi.pojo.FormResult;

public class FilterQueryStringFactory {
	private String filterExpression;
	private List<FormResult> forms;

	public FilterQueryStringFactory(String filterExpression, List<FormResult> forms) {
		this.filterExpression = filterExpression;
		this.forms = forms;
	}

	/**
	 * Generates the filter query string by calling toString on each filter and replacing the filter name in the
	 * filterExpression with its output.
	 * 
	 * @return
	 * @throws FilterQueryStringException
	 */
	public String generateString() throws FilterQueryStringException {
		String output = filterExpression;

		output = output.replaceAll("\\|\\|", "OR");
		output = output.replaceAll("\\&\\&", "AND");
		output = output.replaceAll("\\!", "NOT");

		if (output == null || output.isEmpty()) {
			return QueryToolConstants.EMPTY_STRING;
		}

		for (FormResult form : forms) {
			for (Filter filter : form.getFilters()) {
				String filterName = filter.getName();

				if (!output.contains(filterName)) {
					throw new FilterQueryStringException("Filter expression is missing the token: " + filterName);
				}

				String queryString = filter.toString();
				output = output.replaceFirst(Matcher.quoteReplacement(filterName),
						Matcher.quoteReplacement(queryString));
			}
		}

		return output;
	}
}
