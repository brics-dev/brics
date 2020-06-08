package gov.nih.tbi.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import gov.nih.tbi.exceptions.FilterEvaluationException;
import gov.nih.tbi.exceptions.FilterEvaluatorException;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.InstancedRecord;
import gov.nih.tbi.repository.model.InstancedRepeatableGroupRow;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.RepeatingCellValue;

public class FilterEvaluator {
	private static final Logger log = Logger.getLogger(FilterEvaluator.class);

	// STRICT = true : to make sure error is returned if a variable is not defined
	// SILENT = false : to make sure we see all the errors
	protected JexlEngine engine;

	@NotNull
	protected JexlExpression expression;
	protected List<FormResult> forms;

	// filters from groups that do not repeat
	protected List<Filter> nonRgFilters;

	// map of the repeatable group column to its filter evaluator
	protected Map<DataTableColumn, RepeatableGroupFilterEvaluator> rgEvaluators;

	// all of the filter names after its been escaped
	protected Set<String> escapedFilterNames;

	public FilterEvaluator(String expression, List<FormResult> forms) throws FilterEvaluatorException {
		this.engine = new JexlBuilder().strict(true).silent(false).create();

		try {
			expression = FilterUtils.escapeFilterExpression(expression);
			this.expression = engine.createExpression(expression);
		} catch (NullPointerException e) {
			throw new NullArgumentException("Boolean expression cannot be null!");
		} catch (JexlException e) {
			throw new FilterEvaluatorException(
					"Error occurred while trying to evaluate the filter expression: " + expression, e);
		}

		this.nonRgFilters = new ArrayList<>();
		this.rgEvaluators = new HashMap<>();
		this.escapedFilterNames = new HashSet<>();
		this.forms = forms;

		// this is a multimap of repeatable group column to its list of filters
		// we will use this to create the evaluator map later
		ListMultimap<DataTableColumn, Filter> rgFilters = ArrayListMultimap.create();

		for (FormResult form : forms) {
			for (Filter filter : form.getFilters()) {
				escapedFilterNames.add(FilterUtils.escapeFilterExpression(filter.getName()));

				// separate out the filters by whether or not the group repeats
				if (DataElementFilter.class.isAssignableFrom(filter.getClass())) {
					DataElementFilter deFilter = (DataElementFilter) filter;
					if (deFilter.getGroup().doesRepeat()) {
						DataTableColumn column = new DataTableColumn(deFilter.getForm().getShortName(),
								deFilter.getGroup().getName(), null);
						rgFilters.put(column, filter);
					} else {
						nonRgFilters.add(filter);
					}
				} else {
					nonRgFilters.add(filter);
				}
			}
		}


		for (DataTableColumn currentRgColumn : rgFilters.keySet()) {
			String formName = currentRgColumn.getForm();
			String rgName = currentRgColumn.getRepeatableGroup();
			// may need a way to differentiate between forms having same repeatable group names
			String subExpression = getSubExpression(formName, rgName);
			List<Filter> filters = rgFilters.get(currentRgColumn);
			RepeatableGroupFilterEvaluator rgEvaluator = new RepeatableGroupFilterEvaluator(subExpression, filters);
			rgEvaluators.put(currentRgColumn, rgEvaluator);
		}
	}

	/**
	 * Returns only the part of the expression the pertains to the given repeatable group name.
	 * 
	 * @param groupName
	 * @return
	 */
	protected String getSubExpression(String formName, String groupName) {
		log.info("Original expression: " + expression.getSourceText());
		groupName = FilterUtils.escapeFilterExpression(groupName);
		groupName = groupName.replaceAll("[^A-Za-z0-9]", "\\$");
		groupName = groupName.replace("$", "\\$");
		String patternFormat = "([&|\\s!(]*%s_%s_[^\\)]+\\)+)";
		String patternString = String.format(patternFormat, formName, groupName);
		log.info("Sub-Expression regex: " + patternString);
		Pattern p = Pattern.compile(patternString);
		Matcher m = p.matcher(expression.getSourceText());

		StringBuffer subExpressionBuffer = new StringBuffer();

		while (m.find()) {
			String currentGroup = m.group();
			subExpressionBuffer.append(currentGroup);
		}

		String subFilterExpression = subExpressionBuffer.toString();
		p = Pattern.compile("^([\\s&|]*)(.+)");
		m = p.matcher(subFilterExpression);

		if (m.matches()) {
			subFilterExpression = m.group(2);
		}

		log.info("Sub-Expression: " + subFilterExpression);

		return subFilterExpression;
	}

	/**
	 * Builds the expression context using the evaluation result of each filter. For example, if filter 'f1' evaluates
	 * to true using the row data, it will set f1 => true
	 * 
	 * @param row
	 * @return
	 * @throws FilterEvaluatorException
	 */
	public boolean evaluate(InstancedRecord record) {

		JexlContext context = new MapContext();

		for (int i = 0; i < record.getSelectedRows().size(); i++) {
			InstancedRow currentInstancedRow = record.getSelectedRows().get(i);

			List<Filter> currentFilters = forms.get(i).getFilters();
			for (Filter filter : currentFilters) {
				String name = filter.getName();
				name = FilterUtils.escapeFilterExpression(name);
				boolean result = filter.evaluate(currentInstancedRow);
				context.set(name, result);
			}
		}
		boolean evalResult;

		try {
			evalResult = (Boolean) expression.evaluate(context);
		} catch (JexlException e) {
			throw new FilterEvaluationException("Error occurred while evaluating expression", e);
		}

		if (evalResult) {
			for (DataTableColumn currentRgColumn : rgEvaluators.keySet()) {
				RepeatingCellValue rcv = findRepeatingCell(record, currentRgColumn);

				if (rcv != null) {
					RepeatableGroupFilterEvaluator currentEvaluator = rgEvaluators.get(currentRgColumn);
					List<InstancedRepeatableGroupRow> rows = Collections.synchronizedList(rcv.getRows());

					synchronized (rows) {
						Iterator<InstancedRepeatableGroupRow> rowIt = rows.iterator();
						while (rowIt.hasNext()) {
							InstancedRepeatableGroupRow currentRow = rowIt.next();
							if (!currentEvaluator.evaluate(currentRow)) {
								rowIt.remove();
							}
						}

						// if data has been filtered out and repeatable group ends up being empty, return false so the
						// whole
						// record gets removed from the result.
						if (rows.isEmpty()) {
							rcv.setExpanded(false);
						}
					}
				}
			}
		}

		return evalResult;
	}

	private RepeatingCellValue findRepeatingCell(InstancedRecord record, DataTableColumn column) {
		String formName = column.getForm();
		int formIndex = -1;
		// find the form index based on form name
		for (int i = 0; i < forms.size(); i++) {
			FormResult currentForm = forms.get(i);
			if (currentForm != null && currentForm.getShortName().equals(formName)) {
				formIndex = i;
				break;
			}
		}

		InstancedRow instancedRow = record.getSelectedRows().get(formIndex);

		if (instancedRow == null) {
			return null;
		}

		return (RepeatingCellValue) instancedRow.getCellValue(column);
	}

	/**
	 * Returns true if the forms have any filters at all
	 * 
	 * @return
	 */
	public boolean hasFilters() {
		if (forms == null || forms.isEmpty()) {
			return false;
		}

		for (FormResult form : forms) {
			if (form.hasFilter()) {
				return true;
			}
		}

		return false;
	}
}
