package gov.nih.nichd.ctdb.util.pdfGen;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.xhtmlrenderer.pdf.ITextRenderer;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.emailtrigger.domain.SentEmail;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.DataEntryDraft;
import gov.nih.nichd.ctdb.response.domain.DataEntryHeader;
import gov.nih.nichd.ctdb.response.domain.EditAnswerDisplay;
import gov.nih.nichd.ctdb.util.PdfConstants;
import gov.nih.nichd.ctdb.util.PdfDocGenerator;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
//import gov.nih.nichd.ctdb.protocol.domain.Protocol;

public class DataCollectionAuditPdfGenerator {
	static Logger logger = Logger.getLogger(DataCollectionAuditPdfGenerator.class);

	private Protocol protocol;
	private DataEntryHeader dataEntryHeader;
	private List<EditAnswerDisplay> editArchives;
	private List<EditAnswerDisplay> auditComments;
	private List<EditAnswerDisplay> originalEntries;
	private List<SentEmail> sentEmails;
	private List<AdministeredForm> finalLockList;
	private List<DataEntryDraft> dataEntryList;

	private static final String TOKEN_ORG_NAME = "${ORG_NAME}";
	private static final String TOKEN_SYSTEM_LOGO = "${SYSTEM_LOGO}";
	private static final String TOKEN_TITLE = "${TITLE}";
	private static final String TEMPLATE_FILE = "/pdf/collection_audit_export_template.html";
	private static final String LOGO_SRC = "<img src=\"./logos/" + TOKEN_ORG_NAME + "/logo.png\" />";
	private static final String TEMP_FILE_PREFIX = "collection_audit_temp";
	private static final String TEMP_FILE_SUFFIX = ".html";

	private static final String E_FORM_NAME = "${eFormName}";
	private static final String PROTOCOL_NAME = "${ProtocolName}";
	private static final String SUBJECT_GUID = "${SubjectGUID}";
	private static final String COLLECTION_VISIT_DT = "${CollectionVisitDate}";
	private static final String SCHEDULED_VISIT_DT = "${ScheduledVisitDate}"; 	
	private static final String VISIT_TYPE = "${VisitType}";

	private static final String FORM_SUMMARY_STATUS_TOKEN_TABLE_DATA = "${Form_Summary_Status_TABLE_DATA}";
	private static final String ORIGINAL_ENTRIES_TOKEN_TABLE_DATA = "${Original_Entries_TABLE_DATA}";
	private static final String ANSWERS_EDITED_TOKEN_TABLE_DATA = "${Answers_Edited_TABLE_DATA}";
	private static final String AUDIT_COMMENT_TABLE_DATA = "${Audit_Comment_TABLE_DATA}";
	private static final String SENT_EMAILS_TOKEN_TABLE_DATA = "${Sent_Emails_TABLE_DATA}";

	SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");

	//	Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
	//	boolean enableEsignature = protocol.isEnableEsignature();

	public DataCollectionAuditPdfGenerator(Protocol protocol, DataEntryHeader dataEntryHeader, List<EditAnswerDisplay> editArchives, List<EditAnswerDisplay> auditComments, List<SentEmail> sentEmails, List<EditAnswerDisplay> originalEntries, List<DataEntryDraft> dataEntryList) {
		super();
		this.protocol = protocol;
		this.dataEntryHeader = dataEntryHeader;
		this.editArchives = editArchives;
		this.auditComments = auditComments;
		this.sentEmails = sentEmails;
		this.originalEntries = originalEntries;
		this.dataEntryList = dataEntryList;
	}

	/**
	 * Returns empty string if given and null string and also escapes all HTML
	 * 
	 * @param text
	 * @return
	 */
	private String escapeHtmlText(String text) {
		if (text == null || PdfConstants.NULL.equals(text)) {
			return PdfConstants.EMPTY_STRING;
		}

		text = StringEscapeUtils.escapeHtml(text);
		return text;
	}

	/**
	 * This method generates the HTML for the table data for all the Answers Edited results.
	 * 
	 * @return
	 */
	private String generateFormSummaryStatusTableData() {
		StringBuffer sb = new StringBuffer();
		for (DataEntryDraft dataEntryList : dataEntryList) {
			sb.append(generateFormSummaryStatusTableRow(dataEntryList));
		}
		return sb.toString();
	}

	/**
	 * This method generates the html for a single row (a single Answers Edited result).
	 * 
	 * @param guidResult
	 * @return
	 */
	private String generateFormSummaryStatusTableRow(DataEntryDraft dataEntry) {
		StringBuffer sb = new StringBuffer();
		sb.append("<tr><td>");
		sb.append(escapeHtmlText(dataEntry.getDataEnteredByName())).append("</td><td>");
		String date_time = "N/A";
		if (dataEntry.getCreatedDate() != null) {
			date_time = df.format(dataEntry.getCreatedDate());
		}
		sb.append(escapeHtmlText(date_time)).append("</td><td>");
		sb.append(escapeHtmlText(dataEntry.getStatus())).append("</td><td>");
		String eSignature = "";
		if (dataEntry.getStatus().equals(CtdbConstants.DATACOLLECTION_STATUS_COMPLETED)
				|| dataEntry.getStatus().equals(CtdbConstants.DATACOLLECTION_STATUS_LOCKED)) {
			eSignature = "Signed";
		}
		sb.append(escapeHtmlText(eSignature)).append("</td></tr>");
		return sb.toString();
	}


	/**
	 * This method generates the HTML for the table data for all the Answers Edited results.
	 * 
	 * @return
	 */
	private String generateOriginalEntriesTableData() {
		StringBuffer sb = new StringBuffer();

		for (EditAnswerDisplay originalEntries : originalEntries) {
			sb.append(generateOriginalEntriesTableRow(originalEntries));
		}
		return sb.toString();
	}

	/**
	 * This method generates the html for a single row (a single Answers Edited result).
	 * 
	 * @param guidResult
	 * @return
	 */
	private String generateOriginalEntriesTableRow(EditAnswerDisplay originalEntries) {
		StringBuffer sb = new StringBuffer();
		sb.append("<tr><td>");
		sb.append(escapeHtmlText(originalEntries.getUsername())).append("</td><td>");
		String date_time = "N/A";
		if (originalEntries.getEditDate() != null) {
			date_time = df.format(originalEntries.getEditDate());
		}
		sb.append(escapeHtmlText(date_time)).append("</td><td>");
		sb.append(escapeHtmlText(originalEntries.getSectionName())).append("</td><td>");
		sb.append(escapeHtmlText(originalEntries.getDataElementName())).append("</td><td>");
		sb.append(escapeHtmlText(originalEntries.getQuestionText())).append("</td><td>");
		sb.append(escapeHtmlText(originalEntries.getPrevAnswer())).append("</td><td>");
		sb.append(escapeHtmlText(originalEntries.getEditAnswer())).append("</td></tr>");
		return sb.toString();
	}

	/**
	 * This method generates the HTML for the table data for all the Answers Edited results.
	 * 
	 * @return
	 */
	private String generateAnswersEditedTableData() {
		StringBuffer sb = new StringBuffer();
		for (EditAnswerDisplay editArchives : editArchives) {
			sb.append(generateAnswersEditedTableRow(editArchives));
		}
		return sb.toString();
	}

	/**
	 * This method generates the html for a single row (a single Answers Edited result).
	 * 
	 * @param guidResult
	 * @return
	 */
	private String generateAnswersEditedTableRow(EditAnswerDisplay editArchives) {
		boolean enableEsignature = protocol.isEnableEsignature();
		StringBuffer sb = new StringBuffer();
		sb.append("<tr><td>");
		sb.append(escapeHtmlText(editArchives.getUsername())).append("</td><td>");
		String date_time = "N/A";
		if (editArchives.getEditDate() != null) {
			date_time = df.format(editArchives.getEditDate());
		}
		sb.append(escapeHtmlText(date_time)).append("</td><td>");
		sb.append(escapeHtmlText(editArchives.getSectionName())).append("</td><td>");
		sb.append(escapeHtmlText(editArchives.getDataElementName())).append("</td><td>");
		sb.append(escapeHtmlText(editArchives.getQuestionText())).append("</td><td>");
		sb.append(escapeHtmlText(editArchives.getPrevAnswer())).append("</td><td>");
		sb.append(escapeHtmlText(editArchives.getEditAnswer())).append("</td><td>");
		sb.append(escapeHtmlText(editArchives.getReasonForEdit())).append("</td><td>");
		String eSignature = "";
		if (enableEsignature) {
			eSignature = "Signed";
		}
		sb.append(escapeHtmlText(eSignature)).append("</td></tr>");
		return sb.toString();
	}

	/**
	 * This method generates the HTML for the table data for all the Answers Edited results.
	 * 
	 * @return
	 */
	private String generateAuditCommentTableData() {
		StringBuffer sb = new StringBuffer();
		for (EditAnswerDisplay auditComments : auditComments) {
			sb.append(generateAuditCommentTableRow(auditComments));
		}
		return sb.toString();
	}

	/**
	 * This method generates the html for a single row (a single Answers Edited result).
	 * 
	 * @param guidResult
	 * @return
	 */
	private String generateAuditCommentTableRow(EditAnswerDisplay auditComments) {
		boolean enableEsignature = protocol.isEnableEsignature();
		StringBuffer sb = new StringBuffer();
		sb.append("<tr><td>");
		sb.append(escapeHtmlText(auditComments.getUsername())).append("</td><td>");
		String date_time = "N/A";
		if (auditComments.getEditDate() != null) {
			date_time = df.format(auditComments.getEditDate());
		}
		sb.append(escapeHtmlText(date_time)).append("</td><td>");
		sb.append(escapeHtmlText(auditComments.getSectionName())).append("</td><td>");
		sb.append(escapeHtmlText(auditComments.getDataElementName())).append("</td><td>");
		sb.append(escapeHtmlText(auditComments.getQuestionText())).append("</td><td>");
		sb.append(escapeHtmlText(String.join("; ", auditComments.getPreviousAnswer()))).append("</td><td>");
		sb.append(escapeHtmlText(String.join("; ", auditComments.getEditedAnswer()))).append("</td><td>");
		sb.append(escapeHtmlText(auditComments.getAuditcommentForEdit())).append("</td><td>");
		String eSignature = "";
		if (enableEsignature) {
			eSignature = "Signed";
		}
		sb.append(escapeHtmlText(eSignature)).append("</td></tr>");
		return sb.toString();
	}

	/**
	 * This method generates the HTML for the table data for all the sent Emails results.
	 * 
	 * @return
	 */
	private String generateSentEmailsTableData() {
		StringBuffer sb = new StringBuffer();
		for (SentEmail sentEmails : sentEmails) {
			sb.append(generateSentEmailsTableRow(sentEmails));
		}
		return sb.toString();
	}

	/**
	 * This method generates the html for a single row (a single sent Email result).
	 * 
	 * @param guidResult
	 * @return
	 */
	private String generateSentEmailsTableRow(SentEmail sentEmails) {
		StringBuffer sb = new StringBuffer();
		sb.append("<tr><td>");
		String date_sent = "N/A";
		if (sentEmails.getDateSent() != null) {
			date_sent = df.format(sentEmails.getDateSent());
		}
		sb.append(escapeHtmlText(date_sent)).append("</td><td>");
		sb.append(escapeHtmlText(sentEmails.getToEmailAddress())).append("</td><td>");
		sb.append(escapeHtmlText(sentEmails.getCcEmailAddress())).append("</td><td>");
		sb.append(escapeHtmlText(sentEmails.getSubject())).append("</td><td>");
		sb.append(escapeHtmlText(sentEmails.getTriggeredAnswer())).append("</td></tr>");
		return sb.toString();
	}
	public byte[] generate(String orgName) throws IOException {

		// This is for eyegene with orgName as "NEI BRICS", we don't want folder name to contain white spaces.
		if (orgName.contains(" ")) {
			int i = orgName.indexOf(" ");
			orgName = orgName.substring(0, i);
		}

		// Create a temporary html file dynamically from the template
		URL url = PdfDocGenerator.class.getClassLoader().getResource(TEMPLATE_FILE);
		File templateFile = new File(url.getPath());
		String htmlText = FileUtils.readFileToString(templateFile, StandardCharsets.UTF_8);

		String dateStamp = BRICSTimeDateUtil.dateToDateString(new Date());

		String logoPath = LOGO_SRC.replace(TOKEN_ORG_NAME, orgName.toLowerCase());
		logger.info("Logo Image Path: " + logoPath);

		htmlText = htmlText.replace(TOKEN_SYSTEM_LOGO, logoPath);
		htmlText = htmlText.replace(TOKEN_TITLE, "Data Collection Audit Log - " + dateStamp);

		htmlText = htmlText.replace(E_FORM_NAME, escapeHtmlText(dataEntryHeader.getFormDisplay()));
		htmlText = htmlText.replace(PROTOCOL_NAME, escapeHtmlText(dataEntryHeader.getStudyName()));
		htmlText = htmlText.replace(SUBJECT_GUID, dataEntryHeader.getPatientDisplay());
		htmlText = htmlText.replace(COLLECTION_VISIT_DT, dataEntryHeader.getDateDisplay());
		htmlText = htmlText.replace(SCHEDULED_VISIT_DT, dataEntryHeader.getScheduledVisitDateDisplay()); 	
		htmlText = htmlText.replace(VISIT_TYPE, escapeHtmlText(dataEntryHeader.getIntervalDisplay()));

		htmlText = htmlText.replace(FORM_SUMMARY_STATUS_TOKEN_TABLE_DATA, generateFormSummaryStatusTableData());
		htmlText = htmlText.replace(ORIGINAL_ENTRIES_TOKEN_TABLE_DATA, generateOriginalEntriesTableData());
		htmlText = htmlText.replace(ANSWERS_EDITED_TOKEN_TABLE_DATA, generateAnswersEditedTableData());
		htmlText = htmlText.replace(AUDIT_COMMENT_TABLE_DATA, generateAuditCommentTableData());
		htmlText = htmlText.replace(SENT_EMAILS_TOKEN_TABLE_DATA, generateSentEmailsTableData());


		logger.info("Generated HTML text...");
		File tempHtmlFile = null;
		byte[] fileBytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			// Create a temporary html file in the same directory as the template.
			tempHtmlFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX, templateFile.getParentFile());
			FileUtils.writeStringToFile(tempHtmlFile, htmlText, StandardCharsets.UTF_8);

			logger.info("Rendering PDF...");

			final ITextRenderer renderer = new ITextRenderer();
			renderer.setDocument(tempHtmlFile);
			renderer.layout();
			renderer.createPDF(bos);
			fileBytes = bos.toByteArray();
			logger.info("Finished rendering PDF!");
		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			try {
				if (bos != null)
					bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if (tempHtmlFile != null)
					tempHtmlFile.delete();
			} catch (SecurityException se) {
				se.printStackTrace();
			}
		}

		return fileBytes;
	}
}
