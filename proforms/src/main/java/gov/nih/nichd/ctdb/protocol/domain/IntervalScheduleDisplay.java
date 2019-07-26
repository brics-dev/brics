package gov.nih.nichd.ctdb.protocol.domain;
import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;


public class IntervalScheduleDisplay extends CtdbDomainObject 
{
	
	private static final long serialVersionUID = 1L;
	
	public static final String SCHEDULER_STATUS_SCHEDULED = "Scheduled";
	public static final String SCHEDULER_STATUS_NOT_SCHEDULED = "Not Scheduled";
	
	private String intervalName = "";
	private String schedulerStatus = "";
    
	public String getIntervalName() {
		return intervalName;
	}

	public void setIntervalName(String intervalName) {
		this.intervalName = intervalName;
	}

	public String getSchedulerStatus() {
		return schedulerStatus;
	}

	public void setSchedulerStatus(String schedulerStatus) {
		this.schedulerStatus = schedulerStatus;
	}

    public Document toXML() throws TransformationException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("No Implementation at this time for the method toXML() in Event.");
    }
}
