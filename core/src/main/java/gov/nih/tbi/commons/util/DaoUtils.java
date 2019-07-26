package gov.nih.tbi.commons.util;

import gov.nih.tbi.CoreConstants;

import java.util.Iterator;
import java.util.Set;

/**
 * Dao Helper methods
 * 
 * @author Andrew Johnson
 * 
 */
public class DaoUtils {

	private static final String PAREN_LEFT = " ( ";
	private static final String PAREN_RIGHT = " ) ";
	private static final String COMMA = " , ";
	private static final String IN = " IN ";
	private static final String OR = " OR ";

	private static final int MAX_VALUES = 1000;

	/**
	 * This method turns a Set and a Field and expands it into database IN statement. ex: field = de.id, ids = {1, 2, 3,
	 * 4, ...} returns ((de.id in (1, 2, 3, 4)) or ...)
	 * 
	 * If more than 1000 ids are given, then the ids will be broken into multiple 'or'd statements inside the out most
	 * parenthesis.
	 * 
	 * Note: This method will only work for numeric ids, OR ids that are already surrounded with quotes.
	 * 
	 * @param field
	 * @param ids
	 * @return
	 */
	public static String expandIdClause(String field, Set<?> ids) {

		int iCount = 0; // This is used to keep track of how many ids have been printed

		// If the list of ids is empty, return an always FALSE case
		if (ids == null || ids.size() == 0) {
			return "1=2";
		}

		StringBuilder clauseBuilder = new StringBuilder();

		// (( {field} IN (
		clauseBuilder.append(PAREN_LEFT + PAREN_LEFT + field + IN + PAREN_LEFT);

		Iterator<?> iterator = ids.iterator();
		while (iterator.hasNext()) {
			// {id},
			clauseBuilder.append(iterator.next() + COMMA);

			iCount++; // increase counter
			if (iCount == MAX_VALUES) // if there are more than 1000 ids, we must break for an OR statement
			{
				clauseBuilder = clauseBuilder.delete(clauseBuilder.lastIndexOf(COMMA), clauseBuilder.length());
				clauseBuilder.append(PAREN_RIGHT + PAREN_RIGHT + OR + PAREN_LEFT + field + IN + PAREN_LEFT);

				iCount = 0; // reset counter
			}
		}

		if (clauseBuilder.indexOf(OR) != -1) {
			// Remove any extra ORs
			clauseBuilder = clauseBuilder.delete(clauseBuilder.lastIndexOf(OR), clauseBuilder.length());
		} else {
			if (clauseBuilder.indexOf(COMMA) != -1) {
				// Remove any extra commas
				clauseBuilder = clauseBuilder.delete(clauseBuilder.lastIndexOf(COMMA), clauseBuilder.length());
			}

			// close brackets
			clauseBuilder.append(PAREN_RIGHT + PAREN_RIGHT);
		}

		// close clause
		clauseBuilder.append(PAREN_RIGHT);

		return clauseBuilder.toString();
	}

	/**
	 * Adds a suffix to the provided string. Should be used if a given string collides with reserved or existing
	 * columns.
	 * 
	 * @see gov.nih.tbi.CoreConstants#SYSTEM_COLUMN_SUB_SUFFIX
	 * 
	 * @param name - the name to modify
	 * @return the modified name
	 */
	public static String getNameSubstitution(String name) {

		return name + CoreConstants.SYSTEM_COLUMN_SUB_SUFFIX;
	}
}
