package gov.nih.tbi.taglib.datatableDecorators;

import org.apache.struts2.ServletActionContext;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.repository.model.hibernate.DatasetSubject;

public class GuidDatasetListIdtDecorator extends IdtDecorator {

	private boolean isAdmin;

	public GuidDatasetListIdtDecorator() {
		super();
		isAdmin = getInAdmin();
	}

	public String getDatasetNameLink() {
		DatasetSubject ds = (DatasetSubject) this.getObject();
		StringBuilder output = new StringBuilder();
		output.append("<a href=\"javascript:viewDataset('").append(ds.getData().getDataId()).append("', ");
		output.append(isAdmin).append(")\">");
		output.append(ds.getData().getDataName());
		output.append("</a>");
		return output.toString();
	}

	public String getStudyNameLink() {
		DatasetSubject ds = (DatasetSubject) this.getObject();
		StringBuilder output = new StringBuilder();
		output.append("<a href=\"javascript:viewStudy('").append(ds.getDataset().getStudy().getId()).append("', ");
		output.append(isAdmin).append(")\">");
		output.append(ds.getData().getContainerName());
		output.append("</a>");
		return output.toString();
	}

	private boolean getInAdmin() {
		boolean output = false;
		String namespace = ServletActionContext.getActionMapping().getNamespace().substring(1);
		if (namespace != null) {
			output = PortalConstants.NAMESPACE_GUID_ADMIN.equals(namespace);
		}
		return output;
	}
}
