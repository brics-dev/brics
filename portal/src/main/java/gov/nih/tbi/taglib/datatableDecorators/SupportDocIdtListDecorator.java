package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.repository.model.SupportingDocumentationInterface;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;

public class SupportDocIdtListDecorator extends IdtDecorator {
	
	private MetaStudy currentMetaStudy;

	public SupportDocIdtListDecorator() {};
	
	public SupportDocIdtListDecorator(MetaStudy currentMetaStudy) {
		this.setCurrentMetaStudy(currentMetaStudy);
	};
	
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
			return "<a class=tdLink href=\"" + link + "\">" + sd.getName() + "</a>";

		} else {
			// Otherwise just show the name without link
			return sd.getName();
		}
	}

	private String getGenericDownloadLink(SupportingDocumentationInterface sd) {
		// If user file has been saved, creates a file downloading link
		if (sd.getId() != null && sd.getUserFile().getId() != null) {

			String link = "fileDownloadAction!download.action?fileId=" + sd.getUserFile().getId();
			return "<a class=tdLink href=\"" + link + "\">" + sd.getName() + "</a>";
		} else {
			// Otherwise just show the name without link
			return sd.getName();
		}
	}

	public String getDocNameLink() {
		MetaStudy metaStudy = currentMetaStudy;
		SupportingDocumentationInterface sd = (SupportingDocumentationInterface) this.getObject();
		String url = sd.getUrl();


		if (!StringUtils.isBlank(url)) { // if supporting documentation is an URL
			if (!url.startsWith("http")) {
				url = "http://" + url;
			}
			return "<a class=tdLink href=\"" + url + "\" target=\"_blank\">" + sd.getName() + "</a>";
		} else { // if supporting documentation is an uploaded file
			if (metaStudy != null) { // meta study has it's own export link generation
				return getMetaStudyDownloadLink(sd, metaStudy);
			} else { // all other types of files will use the generic link generator
				return getGenericDownloadLink(sd);
			}
		}
	}
	
	public String getTitle() {
		SupportingDocumentationInterface sd = (SupportingDocumentationInterface) this.getObject();
		return sd.getPublication() != null ? sd.getPublication().getTitle() : sd.getTitle();
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
			link = "<a class=tdLink href=\"javascript:;\" onclick=\"showDocumentDetails(\'" + sdName + "\')\">"
					+ sd.getFileType().getName() + "</a>";
		} else {
			link = sd.getFileType().getName();
		}

		return link;
	}

	public MetaStudy getCurrentMetaStudy() {
		return currentMetaStudy;
	}

	public void setCurrentMetaStudy(MetaStudy currentMetaStudy) {
		this.currentMetaStudy = currentMetaStudy;
	}
}
