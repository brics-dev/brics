package gov.nih.tbi.filter;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;

import gov.nih.tbi.exceptions.FilterEvaluatorException;
import gov.nih.tbi.repository.model.InstancedRepeatableGroupRow;

public class RepeatableGroupFilterEvaluator {
	private static final Logger log = Logger.getLogger(RepeatableGroupFilterEvaluator.class);

	// STRICT = true : to make sure error is returned if a variable is not defined
	// SILENT = false : to make sure we see all the errors
	protected JexlEngine engine;

	@NotNull
	protected JexlExpression expression;
	protected List<Filter> filters;

	public RepeatableGroupFilterEvaluator(String expression, List<Filter> filters) throws FilterEvaluatorException {
		this.engine = new JexlBuilder().strict(true).silent(false).create();

		expression = FilterUtils.escapeFilterExpression(expression);
		try {
			this.expression = engine.createExpression(expression);
		} catch (NullPointerException e) {
			throw new NullArgumentException("Boolean expression cannot be null!");
		} catch (JexlException e) {
			throw new FilterEvaluatorException(
					"Error occurred while trying to evaluate the filter expression: " + expression, e);
		}

		this.filters = filters;
	}

	/**
	 * Builds the expression context using the evaluation result of each filter. For example, if filter 'f1' evaluates
	 * to true using the row data, it will set f1 => true
	 * 
	 * @param row
	 * @return
	 */
	public boolean evaluate(InstancedRepeatableGroupRow row) {
		JexlContext context = new MapContext();

		for (Filter filter : filters) {
			String name = filter.getName();
			name = FilterUtils.escapeFilterExpression(name);
			boolean result = filter.evaluate(row);
			context.set(name, result);
		}

		return (Boolean) expression.evaluate(context);
	}
}
