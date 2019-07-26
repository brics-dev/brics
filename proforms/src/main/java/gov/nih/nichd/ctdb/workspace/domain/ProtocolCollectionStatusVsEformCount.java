package gov.nih.nichd.ctdb.workspace.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * DrillDown Chart model
 * @author lfan
 *
 */
public class ProtocolCollectionStatusVsEformCount {
	private String collectionStatus;
	private int eFormCount;
	private List<String> eFormNameList = new ArrayList<String>();
	
	public String getCollectionStatus() {
		return collectionStatus;
	}
	public void setCollectionStatus(String collectionStatus) {
		this.collectionStatus = collectionStatus;
	}
	public int getEFormCount() {
		return eFormCount;
	}
	public void setEFormCount(int eFormCount) {
		this.eFormCount = eFormCount;
	}
	public List<String> getEFormNameList() {
		return this.eFormNameList;
	}
	public void setEFormNameList(List<String> eFormNameList){
		this.eFormNameList = eFormNameList;
	}

}
