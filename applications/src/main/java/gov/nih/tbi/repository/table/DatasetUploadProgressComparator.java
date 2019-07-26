package gov.nih.tbi.repository.table;

import java.util.Comparator;

/**
 * Compares the progress string for the dataset re-submission table. The string to be compare is composed of 'number of
 * completed files/total number of files'. This sort will only compare the number of completed files, ignoring total
 * number of files completely.
 * 
 * @author Francis Chen
 *
 */
public class DatasetUploadProgressComparator implements Comparator<String> {

	/**
	 * Compares the progress string for the dataset re-submission table. The string to be compare is composed of 'number
	 * of completed files/total number of files'. This sort will only compare the number of completed files, ignoring
	 * total number of files completely.
	 */
	@Override
	public int compare(String o1, String o2) {
		Integer firstNumber1 = Integer.valueOf(o1.split("/")[0]);
		Integer firstNumber2 = Integer.valueOf(o2.split("/")[0]);
		return Integer.compare(firstNumber1, firstNumber2);
	}

}
