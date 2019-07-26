package gov.nih.tbi.download.util;

import java.util.Comparator;

/**
 * This class has been moved to its own class so that it is usable
 * from both the macro view and the micro view. -Victor Wang
 * 
 * @author wangvg
 *
 */
@SuppressWarnings("rawtypes")
public class ProgressComparator implements Comparator {
	
	@Override
	public int compare(Object o1, Object o2) {

		Integer int1 = (Integer) o1;
		Integer int2 = (Integer) o2;

		return Integer.compare(int1, int2);
	}
}
