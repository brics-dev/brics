package gov.nih.tbi.filter;

public class FilterUtils {
	public static String escapeFilterExpression(String filterExpression) {
		String escapedExpression = filterExpression.replaceAll("[^A-Za-z0-9_$()&|!\\s]", "\\$");
		return escapedExpression;
	}
}
