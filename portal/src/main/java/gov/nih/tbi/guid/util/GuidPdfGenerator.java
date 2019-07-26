package gov.nih.tbi.guid.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.xhtmlrenderer.pdf.ITextRenderer;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.util.ESignDocGenerator;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.guid.model.GuidSearchResult;

public class GuidPdfGenerator {
	static Logger logger = Logger.getLogger(GuidPdfGenerator.class);

	private List<GuidSearchResult> guidResults;

	private static final String TOKEN_ORG_NAME = "${ORG_NAME}";
	private static final String TOKEN_SYSTEM_LOGO = "${SYSTEM_LOGO}";
	private static final String TOKEN_TABLE_DATA = "${TABLE_DATA}";
	private static final String TOKEN_TITLE = "${TITLE}";
	private static final String TEMPLATE_FILE = "/pdf/guid_export_template.html";
	private static final String LOGO_SRC = "<img src=\"./logos/" + TOKEN_ORG_NAME + "/logo.png\" />";
	private static final String TEMP_FILE_PREFIX = "guid_temp";
	private static final String TEMP_FILE_SUFFIX = ".html";

	public GuidPdfGenerator(List<GuidSearchResult> guidResults) {
		super();
		this.guidResults = guidResults;
	}

	/**
	 * Returns empty string if given and null string and also escapes all HTML
	 * 
	 * @param text
	 * @return
	 */
	private String escapeHtmlText(String text) {
		if (text == null || PortalConstants.NULL.equals(text)) {
			return PortalConstants.EMPTY_STRING;
		}

		text = StringEscapeUtils.escapeHtml(text);
		return text;
	}

	/**
	 * This method generates the HTML for the table data for all the guid results.
	 * 
	 * @return
	 */
	private String generateTableData() {
		StringBuffer sb = new StringBuffer();
		for (GuidSearchResult guidResult : guidResults) {
			sb.append(generateTableRow(guidResult));
		}

		return sb.toString();
	}

	/**
	 * This method generates the html for a single row (a single guid result).
	 * 
	 * @param guidResult
	 * @return
	 */
	private String generateTableRow(GuidSearchResult guidResult) {
		StringBuffer sb = new StringBuffer();
		sb.append("<tr><td>");
		sb.append(escapeHtmlText(guidResult.getGuid())).append("</td><td>");
		sb.append(escapeHtmlText(guidResult.getType())).append("</td><td>");
		sb.append(escapeHtmlText(guidResult.getServerShortName())).append("</td><td>");
		sb.append(escapeHtmlText(guidResult.getOrganization())).append("</td><td>");
		sb.append(escapeHtmlText(guidResult.getFullName())).append("</td><td>");
		sb.append(escapeHtmlText(guidResult.getDateCreated())).append("</td><td>");
		sb.append(escapeHtmlText(BRICSStringUtils.concatWithDelimiter(guidResult.getLinked(), ", ")))
				.append("</td></tr>");
		return sb.toString();
	}

	public byte[] generate(String orgName) throws IOException {

		// This is for eyegene with orgName as "NEI BRICS", we don't want folder name to contain white spaces.
		if (orgName.contains(" ")) {
			int i = orgName.indexOf(" ");
			orgName = orgName.substring(0, i);
		}

		// Create a temporary html file dynamically from the template
		URL url = ESignDocGenerator.class.getClassLoader().getResource(TEMPLATE_FILE);
		File templateFile = new File(url.getPath());
		String htmlText = FileUtils.readFileToString(templateFile, StandardCharsets.UTF_8);

		String dateStamp = BRICSTimeDateUtil.dateToDateString(new Date());

		String logoPath = LOGO_SRC.replace(TOKEN_ORG_NAME, orgName.toLowerCase());
		logger.info("Logo Image Path: " + logoPath);

		htmlText = htmlText.replace(TOKEN_SYSTEM_LOGO, logoPath);
		htmlText = htmlText.replace(TOKEN_TITLE, "GUID LIST - " + dateStamp);
		htmlText = htmlText.replace(TOKEN_TABLE_DATA, generateTableData());

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
