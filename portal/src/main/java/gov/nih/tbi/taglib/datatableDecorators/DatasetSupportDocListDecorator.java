package gov.nih.tbi.taglib.datatableDecorators;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.repository.model.hibernate.SupportingDocumentation;

public class DatasetSupportDocListDecorator extends Decorator {
	
	SupportingDocumentation supportingDocumentation;
	
	public String initRow(Object obj, int viewIndex, int listIndex) {
		String feedback = super.initRow(obj, viewIndex, listIndex);
		
		supportingDocumentation = (SupportingDocumentation) this.getObject();
		return feedback;
	}
	
	public String getSelectCheckbox() throws UnsupportedEncodingException {

		String sdName = URLEncoder.encode(supportingDocumentation.getName(), "UTF-8");
		String output = "<input  type=\"checkbox\" name=\"docCheckbox\" value=\"" + sdName + "\" />\n";
		return output;
	}
	
	public String getDocNameLink() {
		
		// If user file has been saved, creates a file downloading link
				if (supportingDocumentation.getId() != null && supportingDocumentation.getUserFile().getId() != null) {

					String link = "fileDownloadAction!download.action?fileId=" + supportingDocumentation.getUserFile().getId();
					return "<a href=\"" + link + "\">" + supportingDocumentation.getName() + "</a>";
				} else {
					// Otherwise just show the name without link
					return supportingDocumentation.getName();
				}
	}
	public String getDescriptionEllipsis() {

		String desc = supportingDocumentation.getDescription();
		String descLine = "";
		if (desc.length() > PortalConstants.ELLIPSIS_CHARACTER_COUNT) {
			descLine = "<span class=\"descBeginning\">";
			descLine += desc.substring(0, PortalConstants.ELLIPSIS_CHARACTER_COUNT + 1);
			descLine +=
					" <a href=\"javascript:;\" class=\"ellipsisExpandCollapse\" onclick=\"ellipsisExpandCollapse(this)\">...</a></span>";
			descLine += "<span class=\"descAll\">";
			descLine += desc;
			descLine +=
					" <a href=\"javascript:;\" class=\"ellipsisExpandCollapse\" onclick=\"ellipsisExpandCollapse(this)\">collapse</a></span>";
		} else {
			descLine = desc;
		}
		return descLine;
	}

}
