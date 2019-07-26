package gov.nih.nichd.ctdb.common.tag;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.taglibs.display.ColumnDecorator;

import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * DateTimeColumnDecorator formats a column value into the system default date and time format.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class DateTimeColumnDecorator extends ColumnDecorator {
    private DateFormat sdf = new SimpleDateFormat(SysPropUtil.getProperty("default.system.dateformat") + " " +
            SysPropUtil.getProperty("default.system.timeformat"));

    /**
     * Formats the column value to the default date and time format
     *
     * @param columnValue The column value to format
     * @return The formated columnValue
     */
    public String decorate(Object columnValue) {
        if (columnValue == null) {
            return "N/A";
        }
        Date t = (Date) columnValue;
        SimpleDateFormat dateFormat = new SimpleDateFormat(SysPropUtil.getProperty("default.system.datetimeformat"));
        
        return dateFormat.format(t);
    }
}