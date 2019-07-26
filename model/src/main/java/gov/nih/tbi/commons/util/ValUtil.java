package gov.nih.tbi.commons.util;

import java.util.Collection;
import java.util.Map;

/**
 * A collection of helpful static methods used for common validation tasks.
 * 
 * @author jeng
 *
 */
public class ValUtil {
	/**
	 * Checks if the given string is null or if it the string is blank after calling trim().
	 * 
	 * @param s - The string to be tested
	 * @return True if and only if the given string is null or it is blank after removing any trailing or leading white
	 *         space characters.
	 */
	public static synchronized boolean isBlank(String s) {
		return (s == null || s.trim().isEmpty());
	}

	/**
	 * Checks if the given list or collection is null or empty.
	 * 
	 * @param coll - The list or collection to be tested.
	 * @return True if and only if the given collection is null or empty.
	 */
	public static synchronized <T> boolean isCollectionEmpty(Collection<T> coll) {
		return (coll == null || coll.isEmpty());
	}

	/**
	 * Checks if the given map is null or empty.
	 * 
	 * @param map - The map to be tested.
	 * @return True if and only if the given map is null or empty.
	 */
	public static synchronized <K, V> boolean isMapEmpty(Map<K, V> map) {
		return (map == null || map.isEmpty());
	}

	/**
	 * Checks if the argument string represents a valid number. A number is considered valid if it successfully returns
	 * a value from the {@link Double#valueOf(String)} method, and the returned value is not a "Not-a-Number" (NaN)
	 * value.
	 * 
	 * @param testStr - The string to test.
	 * @return True if and only if the argument is a numeric string, and does not represent the NaN value.
	 */
	public static synchronized boolean isNumeric(String testStr) {
		boolean isNumber = false;

		if (testStr != null) {
			try {
				Double num = Double.valueOf(testStr);

				if (num != null && !num.isNaN()) {
					isNumber = true;
				}
			} catch (NumberFormatException nfe) {
				return false;
			}
		}

		return isNumber;
	}
}
