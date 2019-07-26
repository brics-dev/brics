package gov.nih.tbi.commons.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nih.tbi.ModelConstants;

/**
 * This class is used to store convenient string manipulation utility methods
 * 
 * @author Francis Chen
 *
 */
public class BRICSStringUtils {

	private static final Pattern FILE_PATH_REGEX_PATTERN = Pattern.compile(".*[\\\\\\/]([^\\\\\\/]+)");

	/**
	 * Concatenates the list of provided strings with the delimiter placed between each string element. (e.g. given
	 * array {1, 2, 3, 4, 5} and delimiter ';' will return string "1;2;3;4;5"
	 * 
	 * @param stringList
	 * @param delimitor
	 * @return
	 */
	public static String concatWithDelimiter(Collection<String> stringList, String delimiter) {

		if (stringList.isEmpty()) {
			return ModelConstants.EMPTY_STRING;
		}

		StringBuffer buffer = new StringBuffer();

		for (String string : stringList) {
			buffer.append(string);
			buffer.append(delimiter);
		}

		buffer.replace(buffer.length() - delimiter.length(), buffer.length(), ModelConstants.EMPTY_STRING);

		return buffer.toString();
	}


	/**
	 * Concatenates the list of provided strings with the delimiter placed between each string element. (e.g. given
	 * array {1, 2, 3, 4, 5} and delimiter ';' will return string "1;2;3;4;5"
	 * 
	 * @param stringList
	 * @param delimitor
	 * @return
	 */
	public static List<String> delimitedStringToList(String stringListString, String delimiter) {
		return delimitedStringToList(stringListString, delimiter, false);
	}

	/**
	 * Concatenates the list of provided strings with the delimiter placed between each string element. (e.g. given
	 * array {1, 2, 3, 4, 5} and delimiter ';' will return string "1;2;3;4;5"
	 * 
	 * @param stringList
	 * @param delimitor
	 * @return
	 */
	public static List<String> delimitedStringToList(String stringListString, String delimiter, boolean doTrim) {
		List<String> list = new ArrayList<String>();

		if (stringListString.isEmpty()) {
			return list;
		}

		String[] strings = stringListString.split(delimiter);

		if (doTrim) {
			for (int i = 0; i < strings.length; i++) {
				strings[i] = strings[i].trim();
			}
		}

		return Arrays.asList(strings);
	}

	/**
	 * Given a file path, return only the file name.
	 * 
	 * @param filePath
	 * @return File name, or null if the file path is invalid.
	 */
	public static String pathToFileName(String filePath) {
		Matcher matcher = FILE_PATH_REGEX_PATTERN.matcher(filePath);

		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}

	/**
	 * Returns the part of the string after the last forward slash
	 * 
	 * @param uri
	 * @return
	 */
	public static String uriAfterLastSlash(String uri) {
		int lastIndex = uri.lastIndexOf('/');

		if (lastIndex > 0) {
			return uri.substring(uri.lastIndexOf('/') + 1, uri.length());
		} else {
			return uri;
		}
	}

	/**
	 * Capitalizes the first character of the string
	 * 
	 * @param string
	 * @return
	 */
	public static String capitalizeFirstCharacter(String string) {
		if (string.length() < 1) {
			return string;
		}

		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}


	/**
	 * Convert name to last, first format if it's not in this format
	 * 
	 * @param origName
	 * @return
	 */
	public static String convertNameFormat(String origName) {
		
		if(origName == null) {
			return null;
		}
		
		String returnName = origName;
		if (origName.indexOf(",") < 0 && origName.indexOf(" ") > 0) {
			List<String> nameArrList = Arrays.asList(origName.split(" "));
			String lastName = nameArrList.get(nameArrList.size() - 1);
			List<String> fNamesList = new ArrayList<String>(nameArrList.subList(0, nameArrList.size() - 1));
			if (fNamesList != null) {
				StringBuffer firstName = new StringBuffer();

				for (Iterator<String> it = fNamesList.iterator(); it.hasNext();) {
					firstName.append(it.next());

					if (it.hasNext()) {
						firstName.append(" ");
					}
				}

				returnName = lastName + ", " + firstName.toString();
			}
		}
		
		return returnName;
	}
	
	public static String formatStringForJson(String s){
		s = s.replaceAll("\t", " ");
		s = s.replaceAll("\n", " ");
		s = s.replaceAll("\"", "'");
		s = s.replaceAll("â€”", "-");
		s = s.replaceAll("â€“", "-");
		
		return s;
	}
}
