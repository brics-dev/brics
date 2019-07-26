package gov.nih.tbi.taglib.datatableDecorators;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.repository.model.hibernate.StudySupportingDocumentation;



public class RepoSupportingDocListDecorator extends Decorator {
	StudySupportingDocumentation supportDoc;

	public String initRow(Object obj, int viewIndex, int listIndex) {
		String feedback = super.initRow(obj, viewIndex, listIndex);
		supportDoc = (StudySupportingDocumentation) obj;
		return feedback;
	}
	
	public String getNameLink() {
		String link = "";
		if (supportDoc.getIsUrl()) {
			link = "<a href=\"" + supportDoc.getUrl() + "\" target=\"_blank\">" + supportDoc.getUrl() + "</a>";
		}
		else {
			link = "<a href=\"fileDownloadAction!download.action?fileId=" + String.valueOf(supportDoc.getUserFile().getId()) + "\">" + String.valueOf(supportDoc.getUserFile().getName()) + "</a>";
		}
		return link;
	}
	
	public String getTypeLink() {
		String link = "";
		String docType = supportDoc.getFileType().getName();
		String sdName;
		try {
			sdName = URLEncoder.encode(supportDoc.getName(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			sdName = supportDoc.getName();
		}
		
		if (docType.equals("Publication") || docType.equals("Software")) {
			link = "<a href=\"javascript:;\" onclick=\"showDocumentDetails(\'" + sdName + "\')\">" + supportDoc.getFileType().getName() + "</a>";
		}
		else {
			link = supportDoc.getFileType().getName();
		}

		return link;
	}
}
