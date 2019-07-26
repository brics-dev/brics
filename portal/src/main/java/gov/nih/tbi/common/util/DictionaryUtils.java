package gov.nih.tbi.common.util;

import java.util.Date;
import java.util.List;

public class DictionaryUtils {
	
	public static Date getMostCurrentDate(List<Date> dates){
		
		Date latestDate = dates.get(0);
		for(Date date: dates){
			if(date == null){
				continue;
			}
			if (date.after(latestDate)){
				latestDate = date;
			}
		}
		
		return latestDate;
	}

}
