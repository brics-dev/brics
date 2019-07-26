package gov.nih.tbi.dictionary.portal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.dictionary.dao.DataElementDao;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;

public class DeleteDataElementAction extends BaseDictionaryAction {

	private static final long serialVersionUID = 7022913443480977098L;

	private static final Logger logger = Logger.getLogger(DeleteDataElementAction.class);

	private String dataElements;

	private List<String> errorsList = new ArrayList<String>();

	public String getDataElements() {
		return dataElements;
	}

	public void setDataElements(String dataElementList) {
		this.dataElements = dataElementList;
	}

	public List<String> getErrorsList() {
		return errorsList;
	}

	public void setErrorsList(List<String> errorsList) {
		this.errorsList = errorsList;
	}

	public String delete() {
		return PortalConstants.ACTION_DELETE_DATA_ELEMENT;
	}

	public String adminDelete() {
		List<String> deDeleteList = new ArrayList<>();

		deDeleteList = getDataElements().contains(";") ? Arrays.asList(getDataElements().split(";"))
				: Arrays.asList(getDataElements());

		for (String deName : deDeleteList) {
			DataElement element = null;
			String deNameFromVirtuoso = dictionaryManager.getDEShortNameByNameIgnoreCases(deName);
			if (deNameFromVirtuoso != null && !deNameFromVirtuoso.isEmpty()) {
				element = dictionaryManager.getDataElement(deNameFromVirtuoso, "1.0");
				List<FormStructure> fsList = dictionaryManager.getAttachedDataStructure(deNameFromVirtuoso, "1.0");
				String fsListAsString = "";
				for(FormStructure fs:fsList){
					fsListAsString = fsListAsString.concat(fs.getShortName() + " ");
				}
				if (element.getStatus().equals(DataElementStatus.DRAFT) && fsList.isEmpty()) {
					logger.info("Deleting " + deName);
					dictionaryManager.deleteDataElement(deName);
				} else {
					if (!element.getStatus().equals(DataElementStatus.DRAFT)) {
						logger.info("Element " + deName + " is not in Draft Status and was not deleted");
						errorsList.add("Element " + deName + " is not in Draft Status and was not deleted");
					} else if (!fsList.isEmpty()) {
						logger.info("Element " + deName + " exists in Form Structure(s): " + fsListAsString + "and was not deleted");
						errorsList.add("Element " + deName + " exists in Form Structure(s): " + fsListAsString + "and was not deleted");
					}
				}
			} else {
				if (element == null) {
					logger.info("Element " + deName + " does not exist");
					errorsList.add("Element " + deName + " does not exist");
				}
			}
		}

		if (errorsList.isEmpty()) {
			return PortalConstants.ACTION_VIEW;
		}
		return PortalConstants.ACTION_DELETE_DATA_ELEMENT;
	}
	
	public String cancel(){
		
		return PortalConstants.ACTION_VIEW;
	}
}
