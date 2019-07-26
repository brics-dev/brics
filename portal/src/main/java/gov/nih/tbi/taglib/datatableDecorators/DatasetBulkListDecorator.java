package gov.nih.tbi.taglib.datatableDecorators;


import java.util.HashMap;

import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;
import gov.nih.tbi.repository.model.hibernate.Dataset;

public class DatasetBulkListDecorator extends Decorator {
	
	BasicDataset dataset;
	
	public String initRow(Object obj, int viewIndex, int listIndex) {
		String feedback = super.initRow(obj, viewIndex, listIndex);
		if (obj instanceof BasicDataset) {
			dataset = (BasicDataset) obj;
		} else if (obj instanceof Dataset) {
			dataset = new BasicDataset((Dataset) obj);
		}
		
		return feedback;
	}
	
	public String getNameLink() {
			return "<a href=\"/portal/studyAdmin/datasetAction!view.action?prefixedId=" + dataset.getPrefixedId() + "\">"  + dataset.getName() + "</a>";	
	}
	
	public String getSubmitter() {
		return dataset.getSubmitter().getFullName();
	}
	
	public String getStudyName(){
		return dataset.getStudy().getTitle();
	}
	
	public String getBulkDatasetStatus(){
		String result = "";
		if (dataset.getDatasetRequestStatus() != null) 
			result = dataset.getDatasetStatus().getName()+"--Requested "+ dataset.getDatasetRequestStatus().getVerb();
		else
			result = dataset.getDatasetStatus().getName();
		
		return result;
	}
	
	public String getAccessRecord(){
		
		@SuppressWarnings("unchecked")
		HashMap<Long, String> accessRecord=  (HashMap<Long, String>) this.getPageContext().findAttribute("accessRecord");
		
		String result =accessRecord.get(dataset.getId());
		
		if(result.equals(PortalConstants.ACCESSRECORD_YES))
			result ="<span class=\"red-text\">"+result+"</span>";
			
		return result;
	}

}
