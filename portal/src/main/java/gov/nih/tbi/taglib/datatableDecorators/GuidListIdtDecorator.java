package gov.nih.tbi.taglib.datatableDecorators;

import java.util.List;

import org.apache.struts2.ServletActionContext;

import gov.nih.tbi.guid.model.GuidSearchResult;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class GuidListIdtDecorator extends IdtDecorator {

	public static final String PSEUDO_GUID_OUTPUT = "PseudoGUID";
	public static final String PSEUDO_GUID_STORAGE = "PSEUDO_GUID";

	GuidSearchResult row;
	boolean isAdmin;

	public GuidListIdtDecorator() {
		// TODO Auto-generated constructor stub
	}

	public String initRow(Object rowObj, int rowIndex) {
		String feedback = super.initRow(rowObj, rowIndex);

		isAdmin = (boolean) ServletActionContext.getRequest().getAttribute("inAdmin");

		if (obj instanceof GuidSearchResult) {
			row = (GuidSearchResult) obj;
		}

		return feedback;
	}

	public String getGuidLink() {
		String guidString = row.getGuid();
		boolean inAdmin = (boolean) ServletActionContext.getRequest().getAttribute("inAdmin");
		String methodName = "view";
		String newContent = "";
		if (row.getType().equals("PseudoGUID")) {
			methodName = "viewPseudoGuid";
		}

		if (!inAdmin) {			
			if(row.getDetailsFlag() != null && row.getDetailsFlag()) {
				newContent = "<a href=\"javascript:redirectWithReferrer(\'/portal/guid/viewGuidAction!" + methodName
							+ ".action?guid="
							+ guidString
							+ "\');\">" + guidString + "<i class='fa fa-info-circle tdLink' style='padding-left: 1px; font-size: 16px; color: rgb(61, 138, 221) ! important;cursor: pointer;'></i></a>";
			}else {
				newContent = "<a href=\"javascript:redirectWithReferrer(\'/portal/guid/viewGuidAction!" + methodName
						+ ".action?guid="
						+ guidString
						+ "\');\">" + guidString + "</a>";				
			}

		} else {
			if(row.getDetailsFlag() != null && row.getDetailsFlag()) {
				newContent = "<a href=\"javascript:redirectWithReferrer(\'/portal/guidAdmin/viewGuidAction!" + methodName
						+ ".action?guid="
						+ guidString + "\');\">" + guidString + "<i class='fa fa-info-circle tdLink' style='padding-left: 1px; font-size: 16px; color: rgb(61, 138, 221) ! important;cursor: pointer;'></i></a>";				
			}else {
				newContent = "<a href=\"javascript:redirectWithReferrer(\'/portal/guidAdmin/viewGuidAction!" + methodName
						+ ".action?guid="
						+ guidString + "\');\">" + guidString + "</a>";				
			}

		}
		
		return newContent;
	}

	public String getFormattedType() {
		if (row.getType().equals(PSEUDO_GUID_STORAGE)) {
			return PSEUDO_GUID_OUTPUT;
		}
		return row.getType();
	}

	public String getDate() {
		return row.getDateCreated();
	}

	public String getUser() {
		return row.getFullName();
	}

	public String getLinked() {
		List<String> linked = row.getLinked();
		String output = String.join(", ", linked);
		return output;
	}
	
}
