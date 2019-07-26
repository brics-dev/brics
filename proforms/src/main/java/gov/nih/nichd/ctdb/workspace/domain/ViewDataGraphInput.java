package gov.nih.nichd.ctdb.workspace.domain;

import java.util.List;

public class ViewDataGraphInput {
	
	private List<Integer> siteIds;
	private Integer guidId;
	private List<Integer> intervalIds;
	private List<EformsSelected> eforms;
	
	public List<Integer> getSiteIds() {
		return siteIds;
	}
	public void setSiteIds(List<Integer> siteIds) {
		this.siteIds = siteIds;
	}
	public Integer getGuidId() {
		return guidId;
	}
	public void setGuidId(Integer guidId){
		this.guidId = guidId;
	}
	public List<Integer> getIntervalIds() {
		return intervalIds;
	}
	public void setIntervalIds(List<Integer> intervalIds) {
		this.intervalIds = intervalIds;
	}
	public List<EformsSelected> getEforms() {
		return eforms;
	}
	public void setEforms(List<EformsSelected> eforms) {
		this.eforms = eforms;
	}
	
	
}
