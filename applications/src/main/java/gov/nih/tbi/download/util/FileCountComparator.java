package gov.nih.tbi.download.util;

import java.util.Comparator;

/**
 * Sort table column by file count where the content format is N*|FILE_COUNTS
 * 
 * @author liux8
 *
 */
public class FileCountComparator implements Comparator<String> {

	@Override
	public int compare(String string1, String string2) {
		int index = string1.indexOf("/");
		int count1 =
				(index < 0) ? Integer.parseInt(string1.split(" ")[0]) : Integer.parseInt(string1.substring(index + 1)
						.split(" ")[0]);
		index = string2.indexOf("/");
		int count2 =
				(index < 0) ? Integer.parseInt(string2.split(" ")[0]) : Integer.parseInt(string2.substring(index + 1)
						.split(" ")[0]);
		return count1 - count2;
	}
}