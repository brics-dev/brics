package gov.nih.nichd.ctdb.common.tag;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.taglibs.display.ColumnDecorator;

import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * TimeColumnDecorator formats a column value into the system default time format.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class TimeColumnDecorator extends ColumnDecorator {
    private DateFormat sdf = new SimpleDateFormat(SysPropUtil.getProperty("default.system.timeformat"));

    /**
     * Formats the column value to the default time format
     *
     * @param columnValue The column value to format
     * @return The formated columnValue
     */
    public String decorate(Object columnValue) {
        Date t = (Date) columnValue;
        return sdf.format(t);
    }
}