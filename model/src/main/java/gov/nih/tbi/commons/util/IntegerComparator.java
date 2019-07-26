package gov.nih.tbi.commons.util;

import java.util.Comparator;

/**
 * Needed a way to get a Integer comparator.
 * @author Francis Chen
 *
 */
public class IntegerComparator implements Comparator<Integer> {

	@Override
	public int compare(Integer o1, Integer o2) {
		return o1.compareTo(o2);
	}
}
