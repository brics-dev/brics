package gov.nih.tbi.taglib.datatableDecorators;

import java.util.HashMap;

import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;
import gov.nih.tbi.repository.model.hibernate.Dataset;

public class DatasetListDecorator extends Decorator {
	boolean inAdmin;
	BasicDataset dataset;
	EntityMap permission;

	public String initRow(Object obj, int viewIndex, int listIndex) {
		String feedback = super.initRow(obj, viewIndex, listIndex);
		if (obj instanceof BasicDataset) {
			dataset = (BasicDataset) obj;
		} else if (obj instanceof Dataset) {
			dataset = new BasicDataset((Dataset) obj);
		}
		inAdmin = (boolean) this.getPageContext().findAttribute("inAdmin");

		HashMap<Long, EntityMap> permissionList =
				(HashMap<Long, EntityMap>) this.getPageContext().findAttribute("permissionList");
		if (permissionList != null && permissionList.containsKey(dataset.getId())) {
			permission = permissionList.get(dataset.getId());
		} else {
			permission = new EntityMap();
			permission.setPermission(PermissionType.ADMIN);
		}

		return feedback;
	}

	public String getNameLink() {
		if (inAdmin) {
			return "<a href=\"/portal/studyAdmin/datasetAction!view.action?prefixedId=" + dataset.getPrefixedId() + "\">"  + dataset.getName() + "</a>";
		} else {
			return "<a href=\"javascript:viewDataset('" + dataset.getPrefixedId() + "', 'false')\">" + dataset.getName() + "</a>";
		}
	}

	public String getStudyLink() {
		if (inAdmin) {
			return "<a href=\"/portal/studyAdmin/viewStudyAction!view.action?source=datasetList&studyId="
					+ dataset.getStudy().getPrefixedId() + "\">" + dataset.getStudy().getTitle() + "</a>";
		} else {
			return "<a href=\"/portal/study/viewStudyAction!view.action?studyId=" + dataset.getStudy().getPrefixedId() + "\">" 
					+ dataset.getStudy().getTitle() + "</a>";
		}
	}

	public String getSubmitter() {
		return dataset.getSubmitter().getFullName();
	}

	public String getStatus() {
		if (dataset.getDatasetRequestStatus() != null) {
			return "<span class=\"red-text\">" + dataset.getDatasetRequestStatus().getName() + "</span>";
		} else {
			return dataset.getDatasetStatus().getName();
		}
	}

	public String getStatusName() {
		if (dataset.getDatasetRequestStatus() != null) {
			return dataset.getDatasetRequestStatus().getName() + "&nbsp;<i>(Pending)</i>";
		} else {
			return dataset.getDatasetStatus().getName();
		}
	}

	public String getRequestStatus() {
		String result = "";
		if (dataset.getDatasetRequestStatus() != null) {
			result = Long.toString(dataset.getDatasetRequestStatus().getId());
		}
		return result;
	}

	public String getOwner() {
		String ownerName = "";
		try {
			//ownerName = Long.toString(permission.getAccount().getUser().getId());
			ownerName = Long.toString(dataset.getSubmitter().getId());
		} catch (Exception e) {
			// do nothing since ownerName is empty string already
		}
		return ownerName;
	}
	
	public String getDataSetSelectInput() {
		
		return "<input type=\"checkbox\" class=\"datasetBulkCheckbox\"" + " id=\""+ dataset.getId() +"\" value=\""+ dataset.getId() +"\" />";
	}
	
	public String getDatasetStatus(){
		String result = "";
		if (dataset.getDatasetRequestStatus() != null) {
			result = dataset.getDatasetStatus().getName()+"--Requested "+ dataset.getDatasetRequestStatus().getVerb();
		}else{
			result = dataset.getDatasetStatus().getName();
		}
		return result;
	}
	
	public String getNumberOfRecords(){
		String result ="";
		
		if(dataset.getRecordCount() !=null){
			result = String.valueOf(dataset.getRecordCount());
		}
		
		return result;
	}
}
