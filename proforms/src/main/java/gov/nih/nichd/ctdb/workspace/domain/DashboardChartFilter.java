package gov.nih.nichd.ctdb.workspace.domain;

public class DashboardChartFilter {
	private Integer currentStudyId;
	private Integer selectedSiteId;
	private Integer selectedGuidId;
	private Integer selectedStatusId;

	public DashboardChartFilter(){
		super();
	}
	public DashboardChartFilter(Integer currentStudyId, Integer selectedSiteId, Integer selectedGuidId, Integer selectedStatusId){
		this.currentStudyId = currentStudyId;
		this.selectedSiteId = selectedSiteId;
		this.selectedGuidId = selectedGuidId;
		this.selectedStatusId = selectedStatusId;
	}
	public Integer getCurrentStudyId() {
		return currentStudyId;
	}
	public void setCurrentStudyId(Integer currentStudyId) {
		this.currentStudyId = currentStudyId;
	}
	public Integer getSelectedSiteId() {
		return selectedSiteId;
	}
	public void setSelectedSiteId(Integer selectedSiteId) {
		this.selectedSiteId = selectedSiteId;
	}
	public Integer getSelectedGuidId() {
		return selectedGuidId;
	}
	public void setSelectedGuidId(Integer selectedGuidId) {
		this.selectedGuidId = selectedGuidId;
	}
	public Integer getSelectedStatusId() {
		return selectedStatusId;
	}
	public void setSelectedStatusId(Integer selectedStatusId) {
		this.selectedStatusId = selectedStatusId;
	}
}