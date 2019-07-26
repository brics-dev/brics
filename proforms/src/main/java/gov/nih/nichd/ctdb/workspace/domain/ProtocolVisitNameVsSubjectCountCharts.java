package gov.nih.nichd.ctdb.workspace.domain;

/**
 * Chart model to represent JSON data to UI
 * @author khanaly
 *
 */
public class ProtocolVisitNameVsSubjectCountCharts {
	private String visitType;
	private int subjectCount;

	public String getVisitType() {
		return visitType;
	}

	public void setVisitType(String visitType) {
		this.visitType = visitType;
	}

	public int getSubjectCount() {
		return subjectCount;
	}

	public void setSubjectCount(int subjectCount) {
		this.subjectCount = subjectCount;
	}
}
