package gov.nih.nichd.ctdb.question.tag;

import gov.nih.nichd.ctdb.common.util.DateFormatter;
import gov.nih.nichd.ctdb.response.domain.SubmissionSummaryReport;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class SubmissionReportDecorator extends IdtDecorator {
	
	/**
	 * Gets the system standard string representation of the visit date.
	 * 
	 * @return	The visit date as a string
	 */
	public String getVisitDateStr()	{
		SubmissionSummaryReport report = (SubmissionSummaryReport) this.getObject();;
		
		return DateFormatter.getFormattedDateWithTime(report.getVisitDate());
	}
}
