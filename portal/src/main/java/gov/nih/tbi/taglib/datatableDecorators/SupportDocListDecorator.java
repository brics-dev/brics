package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.repository.model.SupportingDocumentationInterface;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;
import org.apache.taglibs.display.Decorator;

public class SupportDocListDecorator extends Decorator {

	public String getSelectCheckbox() throws UnsupportedEncodingException {

		SupportingDocumentationInterface sd = (SupportingDocumentationInterface) this.getObject();

		String sdName = URLEncoder.encode(sd.getName(), "UTF-8");
		String output = "<input  type=\"checkbox\" name=\"docCheckbox\" value=\"" + sdName + "\" />\n";
		return output;
	}

	private String getMetaStudyDownloadLink(SupportingDocumentationInterface sd, MetaStudy metaStudy) {
		// If user file has been saved, creates a file downloading link
		if (sd.getId() != null && sd.getUserFile().getId() != null) {
			String link = "metaStudyExportAction!download.action?supportingDocId=" + sd.getId();

			// metastudy is only null when editing, no need for file download here because
			// you can't download a file if it hasn't been saved
			if (metaStudy != null) {
				link += "&metaStudyId=" + metaStudy.getId();
			} else {

			}
			return "<a href=\"" + link + "\">" + sd.getName() + "</a>";

		} else {
			// Otherwise just show the name without link
			return sd.getName();
		}
	}

	private String getGenericDownloadLink(SupportingDocumentationInterface sd) {
		// If user file has been saved, creates a file downloading link
		if (sd.getId() != null && sd.getUserFile().getId() != null) {

			String link = "fileDownloadAction!download.action?fileId=" + sd.getUserFile().getId();
			return "<a href=\"" + link + "\">" + sd.getName() + "</a>";
		} else {
			// Otherwise just show the name without link
			return sd.getName();
		}
	}

	public String getDocNameLink() {
		MetaStudy metaStudy = (MetaStudy) this.getPageContext().findAttribute("currentMetaStudy");
		SupportingDocumentationInterface sd = (SupportingDocumentationInterface) this.getObject();
		String url = sd.getUrl();


		if (!StringUtils.isBlank(url)) { // if supporting documentation is an URL
			if (!url.startsWith("http")) {
				url = "http://" + url;
			}
			return "<a href=\"" + url + "\" target=\"_blank\">" + sd.getName() + "</a>";
		} else { // if supporting documentation is an uploaded file
			if (metaStudy != null) { // meta study has it's own export link generation
				return getMetaStudyDownloadLink(sd, metaStudy);
			} else { // all other types of files will use the generic link generator
				return getGenericDownloadLink(sd);
			}
		}
	}
	
	public String getTypeLink() {
		SupportingDocumentationInterface sd = (SupportingDocumentationInterface) this.getObject();
		String link = "";
		String docType = sd.getFileType().getName();
		String sdName;
		try {
			sdName = URLEncoder.encode(sd.getName(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			sdName = sd.getName();
		}
		
		if (docType.equals("Publication") || docType.equals("Software")) {
			link = "<a href=\"javascript:;\" onclick=\"showDocumentDetails(\'" + sdName + "\')\">" + sd.getFileType().getName() + "</a>";
		}
		else {
			link = sd.getFileType().getName();
		}

		return link;
	}

	public String getDescriptionEllipsis() {
		SupportingDocumentationInterface sd = (SupportingDocumentationInterface) this.getObject();

		String desc = sd.getDescription();
		
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
