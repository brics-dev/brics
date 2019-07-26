	package gov.nih.nichd.ctdb.common.tag;

	import org.apache.taglibs.display.ColumnDecorator;

	import java.text.DateFormat;
	import java.text.SimpleDateFormat;
	import java.util.Date;

public class YyyyMMddHHmmColumnDecorator extends ColumnDecorator {
	    private DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	    public String decorate(Object columnValue) {
	        if (columnValue == null) {
	            return "N/A";
	        }
	        Date t = (Date) columnValue;
	        return sdf.format(t);
	    }
}
