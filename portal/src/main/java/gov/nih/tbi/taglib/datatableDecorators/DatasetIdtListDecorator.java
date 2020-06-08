package gov.nih.tbi.taglib.datatableDecorators;

import java.util.HashMap;

import org.apache.struts2.ServletActionContext;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;
import gov.nih.tbi.repository.model.hibernate.Dataset;

public class DatasetIdtListDecorator extends IdtDecorator {
	boolean inAdmin;
	BasicDataset dataset;
	EntityMap permission;
	public static final int DATASET_NAME_LIMIT = 50;

	public String initRow(Object obj, int viewIndex) {
		String feedback = super.initRow(obj, viewIndex);
		if (obj instanceof BasicDataset) {
			dataset = (BasicDataset) obj;
		} else if (obj instanceof Dataset) {
			dataset = new BasicDataset((Dataset) obj);
		}
		inAdmin = (boolean) ServletActionContext.getRequest().getAttribute("inAdmin");

		@SuppressWarnings("unchecked")
		HashMap<Long, EntityMap> permissionList =
				(HashMap<Long, EntityMap>) ServletActionContext.getRequest().getAttribute("permissionList");
		if (permissionList != null && permissionList.containsKey(dataset.getId())) {
			permission = permissionList.get(dataset.getId());
		} else {
			permission = new EntityMap();
			permission.setPermission(PermissionType.ADMIN);
		}

		return feedback;
	}

	public String getNameLink() {
		if(inAdmin) {
			if (dataset.getName().length() > DATASET_NAME_LIMIT) {
				return "<span title=\"" + dataset.getName() + "\"><a href=\"/portal/studyAdmin/datasetAction!view.action?prefixedId=" + dataset.getPrefixedId() 
						+ "\">"  + dataset.getName().substring(0,DATASET_NAME_LIMIT-1).concat("...") + "</a></span>";
			} else {
				return "<a href=\"/portal/studyAdmin/datasetAction!view.action?prefixedId=" + dataset.getPrefixedId()
				+ "\">" + dataset.getName() + "</a>";
				
			}
		}else {
			if (dataset.getName().length() > DATASET_NAME_LIMIT) {
				return "<span title=\"" + dataset.getName() + "\"><a href=\"javascript:viewDataset('" + dataset.getPrefixedId() + "', 'false')\">" + 
						dataset.getName().substring(0,DATASET_NAME_LIMIT-1).concat("...")
						+ "</a></span>";
			} else {
				return "<a href=\"javascript:viewDataset('" + dataset.getPrefixedId() + "', 'false')\">" + dataset.getName()
						+ "</a>";
			}
		}	
	}

	public String getStudyLink() {
		if( dataset.getStudy() == null) {
			return "";
		}
		if (inAdmin) {
			return "<a href=\"/portal/studyAdmin/viewStudyAction!view.action?source=datasetList&studyId="
					+ dataset.getStudy().getPrefixedId() + "\">" + dataset.getStudy().getTitle() + "</a>";
		} else {
			return "<a href=\"/portal/study/viewStudyAction!view.action?studyId=" + dataset.getStudy().getPrefixedId()
					+ "\">" + dataset.getStudy().getTitle() + "</a>";
		}
	}

	public String getRequestedStatus() {
		return (dataset.getDatasetRequestStatus() != null) ? "true" : "false";
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
			// ownerName = Long.toString(permission.getAccount().getUser().getId());
			ownerName = Long.toString(dataset.getSubmitter().getId());
		} catch (Exception e) {
			// do nothing since ownerName is empty string already
		}
		return ownerName;
	}

	public String getDataSetSelectInput() {

		return "<input type=\"checkbox\" class=\"datasetBulkCheckbox\"" + " id=\"" + dataset.getId() + "\" value=\""
				+ dataset.getId() + "\" />";
	}

	public String getDatasetStatus() {
		String result = "";
		if (dataset.getDatasetRequestStatus() != null) {
			result = dataset.getDatasetStatus().getName() + "--Requested "
					+ dataset.getDatasetRequestStatus().getVerb();
		} else {
			result = dataset.getDatasetStatus().getName();
		}
		return result;
	}

	public String getNumberOfRecords() {
		String result = "";

		if (dataset.getRecordCount() != null) {
			result = String.valueOf(dataset.getRecordCount());
		}

		return result;
	}
}
