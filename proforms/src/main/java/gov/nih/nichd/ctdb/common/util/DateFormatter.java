package gov.nih.nichd.ctdb.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import gov.nih.nichd.ctdb.util.common.SysPropUtil;

public class DateFormatter
{
	public static final String PROFORMS_DATE_FORMAT_WITH_TIME = SysPropUtil.getProperty("default.system.datetimeformat");
	public static final String PROFORMS_DATA_FORMAT = SysPropUtil.getProperty("default.system.dateformat");
	
	/**
	 * Formats the specified java.util.Date object to the standard data representation for ProFoRMS.
	 * The formatted string will show both the date and time in the following format:  "yyyy-MM-dd kk:mm"
	 * 
	 * @param d - The Date object to format
	 * @return	the formatted time string in the specified ProFoRMS format
	 */
	public static String getFormattedDateWithTime(Date d)
	{
		SimpleDateFormat dFormat = new SimpleDateFormat(PROFORMS_DATE_FORMAT_WITH_TIME);
		
		return d != null ? dFormat.format(d) : "";
	}
	
	/**
	 * Formats the specified java.util.Date object to the standard data representation for ProFoRMS.
	 * The formatted string will show the date in the following format:  "yyyy-MM-dd"
	 * 
	 * @param d - The Date object to format
	 * @return	the formatted time string in the specified ProFoRMS format
	 */
	public static String getFormattedDate(Date d)
	{
		SimpleDateFormat dFormat = new SimpleDateFormat(PROFORMS_DATA_FORMAT);
		
		return d != null ? dFormat.format(d) : "";
	}
}
