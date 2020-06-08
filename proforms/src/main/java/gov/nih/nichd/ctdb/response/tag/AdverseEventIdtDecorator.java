package gov.nih.nichd.ctdb.response.tag;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.tag.ActionIdtDecorator;
import gov.nih.nichd.ctdb.response.domain.AdverseEvent;


/**
 * AdverseEventIdtDecorator enables a table to have a column with Action links This class works with the
 * <code>display</code> tag library.
 *
 * @author wangla
 * 
 */
public class AdverseEventIdtDecorator extends ActionIdtDecorator {
	public AdverseEventIdtDecorator() {
		super();
	}

	public String getFmCompleteddate() throws JspException {
		AdverseEvent ae = (AdverseEvent) this.getObject();
		String fcdate = ae.getFormCompleteddate();
		if (fcdate != null) {
			fcdate = fcdate.substring(0, fcdate.indexOf(" "));
			return fcdate;
		} else {
			return "N/A";
		}
	}

	public String getVisitdate() throws JspException {
		AdverseEvent ae = (AdverseEvent) this.getObject();
		String vdate = ae.getVisitdate();
		String newVdate = "N/A";
		if (vdate != null) {
			Date date = tryParse(vdate);
			if (date != null) {
				SimpleDateFormat df2 = new SimpleDateFormat("dd-MMM-yyyy HH:mm"); // "MM/dd/yyyy HH:mm:ss"
				newVdate = df2.format(date);
			}
		}

		return newVdate;
	}

	public String getAeStartdate() throws JspException {
		AdverseEvent ae = (AdverseEvent) this.getObject();
		String sdate = ae.getAeStartDate();
		String newVdate = "N/A";
		if (sdate != null) {
			if (sdate.equalsIgnoreCase(CtdbConstants.QUESTION_NO_ANSWER)) {
				newVdate = CtdbConstants.QUESTION_NO_ANSWER;
			} else {
				Date date = tryParse(sdate);
				if (date != null) {
					SimpleDateFormat df2 = new SimpleDateFormat("dd-MMM-yyyy HH:mm"); // "MM/dd/yyyy HH:mm:ss"
					newVdate = df2.format(date);
				}
			}
		}

		return newVdate;
	}

	public String getAeEnddate() throws JspException {
		AdverseEvent ae = (AdverseEvent) this.getObject();
		String edate = ae.getAeEndDate();
		String newVdate = "N/A";
		if (edate != null) {
			if (edate.equalsIgnoreCase(CtdbConstants.QUESTION_NO_ANSWER)) {
				newVdate = CtdbConstants.QUESTION_NO_ANSWER;
			} else {
				Date date = tryParse(edate);
				if (date != null) {
					SimpleDateFormat df2 = new SimpleDateFormat("dd-MMM-yyyy HH:mm"); // "MM/dd/yyyy HH:mm:ss"
					newVdate = df2.format(date);
				}
			}
		}

		return newVdate;
	}

	public String getAnswerUpddate() throws JspException {
		AdverseEvent ae = (AdverseEvent) this.getObject();
		String ansUpddate = ae.getAnsUpddate();
		String newVdate = "N/A";
		if (ansUpddate != null) {
			Date date = tryParse(ansUpddate);
			if (date != null) {
				SimpleDateFormat df2 = new SimpleDateFormat("dd-MMM-yyyy HH:mm"); // "MM/dd/yyyy HH:mm:ss"
				newVdate = df2.format(date);
			}
		}

		return newVdate;
	}

	public String getSubjectLk() throws JspException {
		AdverseEvent ae = (AdverseEvent) this.getObject();
		String subject = ae.getSubject();
		if (subject != null && !subject.trim().isEmpty()) {
			String subjectTm = subject.trim();
			int selected_Form_Ids = ae.getAdministeredformId();
			subject = "<a href=\"" + this.getWebRoot()
					+ "/response/dataCollection.action?action=editForm&mode=formPatient&aformId=" + selected_Form_Ids
					+ "&editUser=" + "1" + "\">" + subjectTm + "</a>";
		}
		return subject;
	}

	String[] formatDates = {"yyyy-MM-dd HH:mm", "yyyy-MM-dd", "MM/DD/yyyy", "dd/MM/yyyy", "MM-dd-yyyy", "dd-MM-yyyy"};

	private Date tryParse(String dateString) {
		for (String formatDate : formatDates) {
			try {
				return new SimpleDateFormat(formatDate).parse(dateString);
			} catch (ParseException e) {
			}
		}

		return null;
	}
}
