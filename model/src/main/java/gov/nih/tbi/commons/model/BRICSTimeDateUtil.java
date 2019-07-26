package gov.nih.tbi.commons.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.time.DateUtils;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import gov.nih.tbi.ModelConstants;

public class BRICSTimeDateUtil {
	static Logger logger = Logger.getLogger(BRICSTimeDateUtil.class);
	public static final long ONE_SECOND = 1000L; // milli seconds
	public static final long ONE_MINUTE = 60L * ONE_SECOND;
	public static final long ONE_HOUR = 60L * ONE_MINUTE;
	public static final long ONE_DAY = 24L * ONE_HOUR;

	private static DateTimeZone currentTimeZone = DateTimeZone.forTimeZone(Calendar.getInstance().getTimeZone());

	private static DateTimeFormatter standardDateFormatter = ISODateTimeFormat.date().withZone(currentTimeZone);

	private static DateTimeFormatter standardDateFormatterUtc = ISODateTimeFormat.date().withZoneUTC();

	private static DateTimeFormatter standardDateTimeFormatter =
			ISODateTimeFormat.dateTimeNoMillis().withZone(currentTimeZone);

	private static DateTimeFormatter standardDateTimeParser =
			ISODateTimeFormat.dateTimeParser().withZone(currentTimeZone);

	private static DateTimeFormatter repositoryDateTimeParser =
			DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.S").withZoneUTC();

	private static DateTimeFormatter zuluDateTimeFormatter =
			ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC);

	private static DateTimeFormatter utcTimeStamp = DateTimeFormat.forPattern("HH:mm:ss").withZone(DateTimeZone.UTC);

	private static DateTimeFormatter twoDigitSlashDateFormatter = DateTimeFormat.forPattern("MM/dd/yy").withZoneUTC();

	private static DateTimeFormatter timestampDateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH-mm-ss");

	// format for a readable date and time, e.g. 2015 February 27, 01:50PM
	private static DateTimeFormatter readableDateTimeFormatter = DateTimeFormat.forPattern("yyyy MMMM dd, hh:mma");

	/**
	 * Returns the current time in a user readable format. (e.g. 2015 February 27, 01:50PM)
	 * 
	 * @return
	 */
	public static String getCurrentReadableTimeString() {
		return dateToReadableDateTimeString(getCurrentTime());
	}

	/**
	 * Given milliseconds from epoch, return the string representation of date in ISO format. yyyy-MM-dd
	 * 
	 * @param date
	 * @return
	 */
	public static String formatDate(Date date) {

		return date != null ? standardDateFormatter.print(date.getTime()) : ModelConstants.EMPTY_STRING;
	}

	public static String formatTimeStamp(Date date) {

		return timestampDateTimeFormatter.print(date.getTime());
	}

	public static String formatDateTime(Date date) throws DateParseException {

		if (date == null) {
			throw new DateParseException("Date object is null!");
		}

		return standardDateTimeFormatter.print(date.getTime());
	}

	public static String dateToStringUtc(Date date) {
		return date != null ? standardDateFormatterUtc.print(date.getTime()) : ModelConstants.EMPTY_STRING;
	}

	/**
	 * Given date object, return the string representation of date and time in ISO format. yyyy-MM-dd
	 * 
	 * @return
	 */
	public static String dateToDateString(Date date) {

		return date != null ? standardDateFormatter.print(date.getTime()) : ModelConstants.EMPTY_STRING;
	}

	/**
	 * Given date object, return the string representation of date and time in ISO format. yyyy-MM-dd'T'HH:mm:ssZZ
	 * 
	 * @return
	 */
	public static String dateToDateTimeString(Date date) {

		return date != null ? standardDateTimeFormatter.print(date.getTime()) : ModelConstants.EMPTY_STRING;
	}

	/**
	 * Given a date object, return the string representation of date and time in a user readable format. (e.g. 2015
	 * February 27, 01:50PM)
	 * 
	 * @param date
	 * @return
	 */
	public static String dateToReadableDateTimeString(Date date) {
		return date != null ? readableDateTimeFormatter.print(date.getTime()) : ModelConstants.EMPTY_STRING;
	}

	/**
	 * Given a string, return the date object accordingly
	 * 
	 * @param dateStr
	 * @return
	 */
	public static Date stringToDate(String dateStr) {

		if (dateStr == null || dateStr.isEmpty()) {
			return null;
		}

		return new Date(standardDateTimeParser.parseMillis(dateStr));
	}

	/**
	 * Parses date string MM/dd/yy into a Date object.
	 * 
	 * @param dateStr
	 * @return
	 */
	public static Date parseTwoDigitSlashDate(String dateStr) {
		if (dateStr == null || dateStr.isEmpty()) {
			return null;
		}

		return new Date(twoDigitSlashDateFormatter.parseMillis(dateStr));
	}

	/**
	 * Return the milliseconds from epoch of the start of the current day. Like... yyyy-MM-dd'T'00:00:00
	 * 
	 * @return
	 */
	public static long getStartOfCurrentDay() {

		return new DateTime(currentTimeZone).withMillisOfDay(0).getMillis();
	}

	/**
	 * Returns the milliseconds from epoch
	 * 
	 * @return
	 */
	public static long getCurrentTimeMillis() {

		return new DateTime(currentTimeZone).getMillis();
	}

	/**
	 * Returns the date object with the current time
	 * 
	 * @return
	 */
	public static Date getCurrentTime() {

		return new DateTime(currentTimeZone).toDate();
	}

	public static String dateToZuluTime(Date date) {
		return zuluDateTimeFormatter.print(date.getTime());
	}

	public static Date zuluStringToDate(String dateString) {
		if (dateString == null || dateString.isEmpty()) {
			return null;
		}

		return new Date(zuluDateTimeFormatter.parseMillis(dateString));
	}

	/**
	 * Returns the date formatted as a UTC timestamp HH:mm:ss
	 * 
	 * @param date
	 * @return
	 */
	public static String dateToUtcTimeStamp(Date date) {
		return utcTimeStamp.print(date.getTime());
	}

	/**
	 * Method to return whether an approved account is pending login for more than tow business days
	 * 
	 * @param fromDate this is the lastSuccessfulLogin coming from Account table
	 * @param numberOfDays is the business days we want to calculate
	 * @return true if the days are more than two or false
	 */
	public static boolean calculateBusinessDays(Date lastSuccessfulLogin, int businessDays) {
		boolean accountRequestPendingMorethanTwoBusinessDays = false;
		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();
		from.setTime(lastSuccessfulLogin);
		to.setTime(getCurrentTime());
		int numberOfDays = 0;
		int usFederalHolidays = 0;
		SimpleDateFormat dateOnly = new SimpleDateFormat("MM/dd/yyyy");
		Calendar newYearsDayObserved = Calendar.getInstance();
		Calendar martinLutherKingObserved = Calendar.getInstance();
		Calendar presidentsDayObserved = Calendar.getInstance();
		Calendar memorialDayObserved = Calendar.getInstance();
		Calendar independenceDayObserved = Calendar.getInstance();
		Calendar laborDayObserved = Calendar.getInstance();
		Calendar colombusDayObserved = Calendar.getInstance();
		Calendar veteransDayObserved = Calendar.getInstance();
		Calendar thanksgivingObserved = Calendar.getInstance();
		Calendar christmasDayObserved = Calendar.getInstance();

		while (from.before(to)) {
			if ((Calendar.SATURDAY != from.get(Calendar.DAY_OF_WEEK))
					&& (Calendar.SUNDAY != from.get(Calendar.DAY_OF_WEEK))
					&& (colombusDayObserved.DAY_OF_WEEK != from.get(Calendar.DAY_OF_WEEK))
					&& (colombusDayObserved.DAY_OF_WEEK != from.get(Calendar.DAY_OF_WEEK))
					&& (colombusDayObserved.DAY_OF_WEEK != from.get(Calendar.DAY_OF_WEEK))
					&& (colombusDayObserved.DAY_OF_WEEK != from.get(Calendar.DAY_OF_WEEK))
					&& (colombusDayObserved.DAY_OF_WEEK != from.get(Calendar.DAY_OF_WEEK))
					&& (colombusDayObserved.DAY_OF_WEEK != from.get(Calendar.DAY_OF_WEEK))
					&& (colombusDayObserved.DAY_OF_WEEK != from.get(Calendar.DAY_OF_WEEK))
					&& (colombusDayObserved.DAY_OF_WEEK != from.get(Calendar.DAY_OF_WEEK))
					&& (colombusDayObserved.DAY_OF_WEEK != from.get(Calendar.DAY_OF_WEEK))
					&& (colombusDayObserved.DAY_OF_WEEK != from.get(Calendar.DAY_OF_WEEK))

			) {
				newYearsDayObserved = NewYearsDayObserved(from.get(Calendar.YEAR));
				martinLutherKingObserved = MartinLutherKingObserved(from.get(Calendar.YEAR));

				presidentsDayObserved = PresidentsDayObserved(from.get(Calendar.YEAR));

				memorialDayObserved = MemorialDayObserved(from.get(Calendar.YEAR));

				independenceDayObserved = IndependenceDayObserved(from.get(Calendar.YEAR));

				laborDayObserved = LaborDayObserved(from.get(Calendar.YEAR));

				colombusDayObserved = ColumbusDayObserved(from.get(Calendar.YEAR));

				veteransDayObserved = VeteransDayObserved(from.get(Calendar.YEAR));

				thanksgivingObserved = ThanksgivingObserved(from.get(Calendar.YEAR));

				christmasDayObserved = ChristmasDayObserved(from.get(Calendar.YEAR));
				if (DateUtils.isSameDay(newYearsDayObserved, from)) {
					logger.info("<---newYearsDayObserved in Date range--->"
							+ dateOnly.format(newYearsDayObserved.getTime()));
					usFederalHolidays++;
				}
				if (DateUtils.isSameDay(martinLutherKingObserved, from)) {
					logger.info("<---martinLutherKingObserved in Date range--->"
							+ dateOnly.format(martinLutherKingObserved.getTime()));
					usFederalHolidays++;
				}
				if (DateUtils.isSameDay(presidentsDayObserved, from)) {
					logger.info("<---presidentsDayObserved in Date range--->"
							+ dateOnly.format(presidentsDayObserved.getTime()));
					usFederalHolidays++;
				}
				if (DateUtils.isSameDay(memorialDayObserved, from)) {
					logger.info("<---memorialDayObserved in Date range--->"
							+ dateOnly.format(memorialDayObserved.getTime()));
					usFederalHolidays++;
				}
				if (DateUtils.isSameDay(independenceDayObserved, from)) {
					logger.info("<---independenceDayObserved in Date range--->"
							+ dateOnly.format(independenceDayObserved.getTime()));
					usFederalHolidays++;
				}
				if (DateUtils.isSameDay(laborDayObserved, from)) {
					logger.info("<---laborDayObserved in Date range--->" + dateOnly.format(laborDayObserved.getTime()));
					usFederalHolidays++;
				}
				if (DateUtils.isSameDay(colombusDayObserved, from)) {
					logger.info("<---colombusDayObserved in Date range--->"
							+ dateOnly.format(colombusDayObserved.getTime()));
					usFederalHolidays++;
				}
				if (DateUtils.isSameDay(veteransDayObserved, from)) {
					logger.info("<---veteransDayObserved in Date range--->"
							+ dateOnly.format(veteransDayObserved.getTime()));
					usFederalHolidays++;
				}
				if (DateUtils.isSameDay(thanksgivingObserved, from)) {
					logger.info("<---thanksgivingObserved in Date range--->"
							+ dateOnly.format(thanksgivingObserved.getTime()));
					usFederalHolidays++;
				}
				if (DateUtils.isSameDay(christmasDayObserved, from)) {
					logger.info("<---christmasDayObserved in Date range--->"
							+ dateOnly.format(christmasDayObserved.getTime()));
					usFederalHolidays++;
				}

				numberOfDays++;
				from.add(Calendar.DATE, 1);


			} else {
				from.add(Calendar.DATE, 1);
			}

		}

		// total no of business days excluding federal holidays and same date
		numberOfDays = numberOfDays - usFederalHolidays - 1;
		logger.info("numberOfDays:\t" + numberOfDays);
		if (numberOfDays > businessDays) {
			accountRequestPendingMorethanTwoBusinessDays = true;
		}

		return accountRequestPendingMorethanTwoBusinessDays;

	}

	public static java.util.Calendar NewYearsDayObserved(int nYear) {
		Calendar cal = new GregorianCalendar(nYear, Calendar.JANUARY, 1);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SATURDAY:
				return (new GregorianCalendar(--nYear, Calendar.DECEMBER, 31));
			case Calendar.SUNDAY:
				return (new GregorianCalendar(nYear, Calendar.JANUARY, 2));
			case Calendar.MONDAY:
			case Calendar.TUESDAY:
			case Calendar.WEDNESDAY:
			case Calendar.THURSDAY:
			case Calendar.FRIDAY:
			default:
				return cal;
		}
	}

	public static java.util.Calendar MartinLutherKingObserved(int nYear) {
		// Third Monday in January
		Calendar cal = new GregorianCalendar(nYear, Calendar.JANUARY, 1);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SUNDAY:
				return (new GregorianCalendar(nYear, Calendar.JANUARY, 16));
			case Calendar.MONDAY:
				return (new GregorianCalendar(nYear, Calendar.JANUARY, 15));
			case Calendar.TUESDAY:
				return (new GregorianCalendar(nYear, Calendar.JANUARY, 21));
			case Calendar.WEDNESDAY:
				return (new GregorianCalendar(nYear, Calendar.JANUARY, 20));
			case Calendar.THURSDAY:
				return (new GregorianCalendar(nYear, Calendar.JANUARY, 19));
			case Calendar.FRIDAY:
				return (new GregorianCalendar(nYear, Calendar.JANUARY, 18));
			default: // Saturday
				return (new GregorianCalendar(nYear, Calendar.JANUARY, 17));
		}
	}

	public static java.util.Calendar PresidentsDayObserved(int nYear) {
		// Third Monday in February
		Calendar cal = new GregorianCalendar(nYear, Calendar.FEBRUARY, 1);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SUNDAY:
				return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 16));
			case Calendar.MONDAY:
				return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 15));
			case Calendar.TUESDAY:
				return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 21));
			case Calendar.WEDNESDAY:
				return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 20));
			case Calendar.THURSDAY:
				return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 19));
			case Calendar.FRIDAY:
				return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 18));
			default: // Saturday
				return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 17));
		}
	}

	public static java.util.Calendar MemorialDayObserved(int nYear) {
		// Last Monday in May
		Calendar cal = new GregorianCalendar(nYear, Calendar.MAY, 1);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SUNDAY:
				return (new GregorianCalendar(nYear, Calendar.MAY, 30));
			case Calendar.MONDAY:
				return (new GregorianCalendar(nYear, Calendar.MAY, 29));
			case Calendar.TUESDAY:
				return (new GregorianCalendar(nYear, Calendar.MAY, 28));
			case Calendar.WEDNESDAY:
				return (new GregorianCalendar(nYear, Calendar.MAY, 27));
			case Calendar.THURSDAY:
				return (new GregorianCalendar(nYear, Calendar.MAY, 26));
			case Calendar.FRIDAY:
				return (new GregorianCalendar(nYear, Calendar.MAY, 25));
			default: // Saturday
				return (new GregorianCalendar(nYear, Calendar.MAY, 31));
		}
	}

	public static java.util.Calendar IndependenceDayObserved(int nYear) {
		Calendar cal = new GregorianCalendar(nYear, Calendar.JULY, 4);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SATURDAY:
				return (new GregorianCalendar(nYear, Calendar.JULY, 3));
			case Calendar.SUNDAY:
				return (new GregorianCalendar(nYear, Calendar.JULY, 5));
			case Calendar.MONDAY:
			case Calendar.TUESDAY:
			case Calendar.WEDNESDAY:
			case Calendar.THURSDAY:
			case Calendar.FRIDAY:
			default:
				return cal;
		}
	}

	public static java.util.Calendar LaborDayObserved(int nYear) {
		// The first Monday in September
		Calendar cal = new GregorianCalendar(nYear, Calendar.SEPTEMBER, 1);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.TUESDAY:
				return (new GregorianCalendar(nYear, Calendar.SEPTEMBER, 7));
			case Calendar.WEDNESDAY:
				return (new GregorianCalendar(nYear, Calendar.SEPTEMBER, 6));
			case Calendar.THURSDAY:
				return (new GregorianCalendar(nYear, Calendar.SEPTEMBER, 5));
			case Calendar.FRIDAY:
				return (new GregorianCalendar(nYear, Calendar.SEPTEMBER, 4));
			case Calendar.SATURDAY:
				return (new GregorianCalendar(nYear, Calendar.SEPTEMBER, 3));
			case Calendar.SUNDAY:
				return (new GregorianCalendar(nYear, Calendar.SEPTEMBER, 2));
			case Calendar.MONDAY:
			default:
				return cal;
		}
	}

	public static java.util.Calendar ColumbusDayObserved(int nYear) {
		// Second Monday in October
		Calendar cal = new GregorianCalendar(nYear, Calendar.OCTOBER, 1);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SUNDAY:
				return (new GregorianCalendar(nYear, Calendar.OCTOBER, 9));
			case Calendar.MONDAY:
				return (new GregorianCalendar(nYear, Calendar.OCTOBER, 8));
			case Calendar.TUESDAY:
				return (new GregorianCalendar(nYear, Calendar.OCTOBER, 14));
			case Calendar.WEDNESDAY:
				return (new GregorianCalendar(nYear, Calendar.OCTOBER, 13));
			case Calendar.THURSDAY:
				return (new GregorianCalendar(nYear, Calendar.OCTOBER, 12));
			case Calendar.FRIDAY:
				return (new GregorianCalendar(nYear, Calendar.OCTOBER, 11));
			default:
				return (new GregorianCalendar(nYear, Calendar.OCTOBER, 10));
		}

	}

	public static java.util.Calendar VeteransDayObserved(int nYear) {
		// November 11th
		Calendar cal = new GregorianCalendar(nYear, Calendar.NOVEMBER, 11);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SATURDAY:
				return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 10));
			case Calendar.SUNDAY:
				return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 12));
			case Calendar.MONDAY:
			case Calendar.TUESDAY:
			case Calendar.WEDNESDAY:
			case Calendar.THURSDAY:
			case Calendar.FRIDAY:
			default:
				return cal;
		}
	}

	public static java.util.Calendar ThanksgivingObserved(int nYear) {
		Calendar cal = new GregorianCalendar(nYear, Calendar.NOVEMBER, 1);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SUNDAY:
				return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 26));
			case Calendar.MONDAY:
				return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 25));
			case Calendar.TUESDAY:
				return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 24));
			case Calendar.WEDNESDAY:
				return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 23));
			case Calendar.THURSDAY:
				return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 22));
			case Calendar.FRIDAY:
				return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 28));
			default: // Saturday
				return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 27));
		}
	}

	public static java.util.Calendar ChristmasDayObserved(int nYear) {
		Calendar cal = new GregorianCalendar(nYear, Calendar.DECEMBER, 25);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SATURDAY:
				return (new GregorianCalendar(nYear, Calendar.DECEMBER, 24));
			case Calendar.SUNDAY:
				return (new GregorianCalendar(nYear, Calendar.DECEMBER, 26));
			case Calendar.MONDAY:
			case Calendar.TUESDAY:
			case Calendar.WEDNESDAY:
			case Calendar.THURSDAY:
			case Calendar.FRIDAY:
			default:
				return cal;
		}
	}

	/**
	 * Parses string in the format of YYYY-MM-DD HH:mm:ss.S to date object. Will truncate anything past the one digit of
	 * precision we allow for milliseconds.
	 * 
	 * @param dateStr
	 * @return
	 */
	public static Date parseRepositoryDate(String dateStr) {
		if (dateStr == null || dateStr.isEmpty()) {
			return null;
		}

		// we have cases where there may be more than one digit of precision for milliseconds. need to truncate it so
		// that it fits our formatter.
		final int PARSER_LENGTH = 21;

		if (dateStr.length() > PARSER_LENGTH) {
			dateStr = dateStr.substring(0, 21);
		}

		return new Date(repositoryDateTimeParser.parseMillis(dateStr));
	}

	public static String repositoryDateToString(Date date) {
		return date != null ? repositoryDateTimeParser.print(date.getTime()) : ModelConstants.EMPTY_STRING;
	}

	/**
	 * Returns true is the current time is between the specified hour ranges. start hour is inclusive and end hour is
	 * exclusive
	 * 
	 * @param startHour
	 * @param endHour
	 * @return
	 */
	public static boolean nowBetweenHourRange(int startHour, int endHour) {
		logger.debug("Checking time interval...");
		DateTime currentTime = DateTime.now();
		logger.debug("Current Time: " + currentTime);
		DateTime startingTime = new DateTime(currentTime.getYear(), currentTime.getMonthOfYear(),
				currentTime.getDayOfMonth(), startHour, 0);
		logger.debug("Start Interval: " + startingTime);
		DateTime endTime = new DateTime(currentTime.getYear(), currentTime.getMonthOfYear(),
				currentTime.getDayOfMonth(), endHour, 0);
		logger.debug("End Interval: " + endTime);
		Interval timeInterval = new Interval(startingTime, endTime);
		boolean inInterval = timeInterval.containsNow();
		logger.debug("In interval? " + inInterval);
		return inInterval;
	}

	/**
	 * Method that takes year and month as input (1975 Oct ) and convert in ISO(yyyy-MM-dd format)
	 * 
	 * @param dateYearMonth
	 * @return ISO date (yyyy-MM-dd) format
	 */
	public static Date formatMonthYearInISO(String dateYearMonth) {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy MMM");
		DateFormat isoWithoutTime = new SimpleDateFormat("yyyy-MM-dd");
		Date isoDate = null;
		try {
			isoDate = inputFormat.parse(dateYearMonth);

			System.out.println(isoWithoutTime.format(isoDate));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isoDate;


	}

	public static int getDayOfWeek(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_WEEK);
	}

	public static int getDayOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}
}
