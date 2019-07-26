package gov.nih.nichd.ctdb.response.tag;

import gov.nih.nichd.ctdb.common.tag.ActionIdtDecorator;
import gov.nih.nichd.ctdb.response.form.ReportingForm;

public class CompletedVisitsReportIdtDecorator extends ActionIdtDecorator {
	public String getBaselineVisitTotal() {
		ReportingForm form = (ReportingForm) getObject();
		int formCount = form.getBaseline();

		return formCount != -1 ? Integer.toString(formCount) : "N/A";
	}

	public String getSixMonthsVisitTotal() {
		ReportingForm form = (ReportingForm) getObject();
		int formCount = form.getSixMonths();

		return formCount != -1 ? Integer.toString(formCount) : "N/A";
	}

	public String getTwelveMonthsVisitTotal() {
		ReportingForm form = (ReportingForm) getObject();
		int formCount = form.getTwelveMonths();

		return formCount != -1 ? Integer.toString(formCount) : "N/A";
	}

	public String getEighteenMonthsVisitTotal() {
		ReportingForm form = (ReportingForm) getObject();
		int formCount = form.getEighteenMonths();

		return formCount != -1 ? Integer.toString(formCount) : "N/A";
	}

	public String getTwentyFourMonthsVisitTotal() {
		ReportingForm form = (ReportingForm) getObject();
		int formCount = form.getTwentyFourMonths();

		return formCount != -1 ? Integer.toString(formCount) : "N/A";
	}

	public String getThirtyMonthsVisitTotal() {
		ReportingForm form = (ReportingForm) getObject();
		int formCount = form.getThirtyMonths();

		return formCount != -1 ? Integer.toString(formCount) : "N/A";
	}

	public String getThirtySixMonthsVisitTotal() {
		ReportingForm form = (ReportingForm) getObject();
		int formCount = form.getThirtySixMonths();

		return formCount != -1 ? Integer.toString(formCount) : "N/A";
	}
	
	public String getFortyTwoMonthsVisitTotal() {
		ReportingForm form = (ReportingForm) getObject();
		int formCount = form.getFortyTwoMonths();
		
		return formCount != -1 ? Integer.toString(formCount) : "N/A";
	}
	
	public String getFortyEightMonthsVisitTotal() {
		ReportingForm form = (ReportingForm) getObject();
		int formCount = form.getFortyEightMonths();
		
		return formCount != -1 ? Integer.toString(formCount) : "N/A";
	}
	
	public String getFiftyFourMonthsVisitTotal() {
		ReportingForm form = (ReportingForm) getObject();
		int formCount = form.getFiftyFourMonths();
		
		return formCount != -1 ? Integer.toString(formCount) : "N/A";
	}
	
	public String getSixtyMonthsVisitTotal() {
		ReportingForm form = (ReportingForm) getObject();
		int formCount = form.getSixtyMonths();
		
		return formCount != -1 ? Integer.toString(formCount) : "N/A";
	}
}
