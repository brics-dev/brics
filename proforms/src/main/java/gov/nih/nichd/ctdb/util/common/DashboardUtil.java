package gov.nih.nichd.ctdb.util.common;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DashboardUtil {
	/**
	 * The DashboardUtil constructor.
	 */
	public DashboardUtil() {

	}

	public static String diffDate(String startDate) {
		String yearStr = startDate.substring(0, 4);
		String monthStr = startDate.substring(5, 7);
		String dayStr = startDate.substring(8, 10);

		LocalDate sDate = LocalDate.of(Integer.parseInt(yearStr), Integer.parseInt(monthStr), Integer.parseInt(dayStr));

		LocalDate today = LocalDate.now();
		Period age = Period.between(sDate, today);
		int years = age.getYears();
		int months = age.getMonths();
		return years + "." + months;
	}
}
