package gov.nih.nichd.ctdb.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Utils {

	/**
	 * Helper method to convert a comma delimited string list to a list of Integer objects.
	 * 
	 * @param strList - Comma delimited list of numbers
	 * @return	A list of Integer objects made from the given comma delimited list
	 * @throws NumberFormatException	When a sub-string cannot be converted to an int.
	 */
	public static synchronized List<Integer> convertStrToIntArray(String strList) throws NumberFormatException {
		String[] strArray = strList.split(",");
		List<Integer> intArray = new ArrayList<Integer>(strArray.length);

		for (int i = 0; i < strArray.length; i++) {
			if (strArray[i].length() > 0) {
				intArray.add(Integer.parseInt(strArray[i].trim()));
			}
		}    		

		return intArray;
	}
	
	
	/**
	 * Helper method to convert a comma delimited string list to a list of Long objects.
	 * 
	 * @param strList - Comma delimited list of numbers
	 * @return	A list of Integer objects made from the given comma delimited list
	 * @throws NumberFormatException	When a sub-string cannot be converted to an int.
	 */
	public static synchronized List<Long> convertStrToLongArray(String strList) throws NumberFormatException {
		String[] strArray = strList.split(",");
		List<Long> longArray = new ArrayList<Long>(strArray.length);
		
		for (int i = 0; i < strArray.length; i++) {
			if (strArray[i].length() > 0) {
				longArray.add(new Long(Long.parseLong(strArray[i].trim())));
			}
		}
		
		return longArray;
	}
	
	/**
	 * Checks if the given string is null or if it the string is blank after calling trim().
	 * 
	 * @param s - The string to be tested
	 * @return	True if and only if the given string is null or it is blank after removing any trailing
	 * or leading white space characters.
	 */
	public static synchronized boolean isBlank(String s) {
		return (s == null || s.trim().isEmpty());
	}
	

	/**
	 * Converts a list of strings (i.e. visit type or form name) into a grammatically correct comma separated string.
	 * 
	 * @param nameList - List of object names as a String
	 * @return	A listing of names for use in a success or error message.
	 */
	public static synchronized String convertListToString(List<String> nameList) {
	
		if (nameList.isEmpty()) {
			return "";
		}
		
		String strList = "";
		int size = nameList.size();
		
		// Check if 3 or more items were deleted
		if (size > 2) {
			StringBuffer nb = new StringBuffer();
			int counter = 1;
			
			for (String s : nameList) {
				nb.append("\"").append(s);
				
				// Check if just a comma needs to be appended 
				if (counter < (size - 1)) {
					nb.append(",\" ");
				} else if (counter == (size - 1)) { // check if this is the next to last element
					nb.append(",\" and ");
				} else {
					nb.append("\"");
				}
				counter++;
			}
			
			strList = nb.toString();
		}
		// Check if two items were deleted
		else if (size == 2) {
			strList = "\"" + nameList.get(0) + "\" and \"" + nameList.get(1) + "\"";
		}
		// Only one item was deleted
		else {
			strList = "\"" + nameList.get(0) + "\"";
		}
		
		return strList;
	}

	/**
	 * 
	 * 
	 * @param paramString
	 * @return
	 */
	public static ArrayList<String> tokenizeToList(String paramString) {
		return tokenizeToList(paramString, ",");
	}

	/**
	 * 
	 * 
	 * @param paramString1
	 * @param paramString2
	 * @return
	 */
	public static synchronized ArrayList<String> tokenizeToList(String paramString1, String paramString2) {
		ArrayList<String> localArrayList = new ArrayList<String>();
		StringTokenizer localStringTokenizer = new StringTokenizer(paramString1, paramString2);

		while (localStringTokenizer.hasMoreTokens()) {
			localArrayList.add(localStringTokenizer.nextToken());
		}

		return localArrayList;
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
			}
			catch (NumberFormatException nfe) {
				return false;
			}
		}

		return isNumber;
	}
}
