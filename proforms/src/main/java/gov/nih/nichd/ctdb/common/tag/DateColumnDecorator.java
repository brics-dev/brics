package gov.nih.nichd.ctdb.common.tag;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.taglibs.display.ColumnDecorator;

/**
 * DateColumnDecorator formats a column value into the system default date format.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class DateColumnDecorator extends ColumnDecorator {
    //private DateFormat sdf = new SimpleDateFormat(SysPropUtil.getProperty("default.system.dateformat"));
    private DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    

    /**
     * Formats the column value to the default date format
     *
     * @param columnValue The column value to format
     * @return The formated columnValue
     */
	public String decorate(Object columnValue) {
    	String dateStr;
    	Date t = new Date();
    	if(columnValue instanceof String){
    		dateStr = (String)columnValue;
    		try{
    			t = sdf.parse(dateStr);
    		}catch (Exception pe){
    			pe.printStackTrace();
    		}
    		
    	} else if (columnValue instanceof Date){
    		t = (Date) columnValue;
    	}
        return sdf.format(t);
    }
}