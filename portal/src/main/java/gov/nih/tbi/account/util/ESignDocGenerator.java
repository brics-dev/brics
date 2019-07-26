package gov.nih.tbi.account.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.xhtmlrenderer.pdf.ITextRenderer;

import gov.nih.tbi.account.model.hibernate.ElectronicSignature;
import gov.nih.tbi.commons.util.BRICSStringUtils;

public class ESignDocGenerator {

	private static Logger logger = Logger.getLogger(ESignDocGenerator.class);
	
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	private static final String TOKEN_ORG_NAME = "${ORG_NAME}";
	private static final String TOKEN_SYSTEM_LOGO = "${SYSTEM_LOGO}";
	private static final String TOKEN_FULL_NAME = "${FULL_NAME}";
	private static final String TOKEN_DATE = "${DATE}";
	private static final String TEMP_FILE_PREFIX = "esign_temp";
	private static final String TEMP_FILE_SUFFIX = ".html";
	private static final String ESIGN_TEMPLATE_FILE = "/pdf/esign_template.html";
	private static final String ESIGN_LOGO_IMGSRC = "<img src=\"./logos/" + TOKEN_ORG_NAME + "/logo.png\" />";
	
	private String firstName;
	private String middleName;
	private String lastName;
	private Date signDate;

	public ESignDocGenerator() {
	}

	public ESignDocGenerator(String firstName, String middleName, String lastName, Date signDate) {
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.signDate = signDate;
	}

	public ESignDocGenerator(ElectronicSignature es) {
		this.firstName = es.getFirstName();
		this.middleName = es.getMiddleName();
		this.lastName = es.getLastName();
		this.signDate = es.getSignatureDate();
	}


	public byte[] generateESignature(String orgName) throws IOException {

		String fullName = this.getFullName();
		logger.debug("Generate esignature for " + fullName);
		
		// This is for eyegene with orgName as "NEI BRICS", we don't want folder name to contain white spaces. 
		if (orgName.contains(" ")) {
			int i = orgName.indexOf(" ");
			orgName = orgName.substring(0, i);
		}
		
		// Create a temporary html file dynamically from the template 
		URL url = ESignDocGenerator.class.getClassLoader().getResource(ESIGN_TEMPLATE_FILE);
		File templateFile = new File(url.getPath());
		String htmlText = FileUtils.readFileToString(templateFile, StandardCharsets.UTF_8);

		htmlText = htmlText.replace(TOKEN_SYSTEM_LOGO, ESIGN_LOGO_IMGSRC.replace(TOKEN_ORG_NAME, orgName));
		htmlText = htmlText.replace(TOKEN_FULL_NAME, fullName);
		htmlText = htmlText.replace(TOKEN_DATE, dateFormatter.format(this.getSignDate()));

		File tempHtmlFile = null;
		byte[] fileBytes = null;
		ByteArrayOutputStream fos = new ByteArrayOutputStream();

		try {
			// Create a temporary html file in the same directory as the template.
			tempHtmlFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX, templateFile.getParentFile());
			FileUtils.writeStringToFile(tempHtmlFile, htmlText, StandardCharsets.UTF_8);

			final ITextRenderer renderer = new ITextRenderer();
			renderer.setDocument(tempHtmlFile);
			renderer.layout();
			renderer.createPDF(fos);
			fileBytes = fos.toByteArray();
			
			logger.info("Esignature file generated for " + fullName);

		} catch (IOException e) {
			e.printStackTrace();
			
		} finally {
			try {
				if (fos != null) 
					fos.close();
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

	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Date getSignDate() {
		return signDate;
	}

	public void setSignDate(Date signDate) {
		this.signDate = signDate;
	}

	public String getFullName() {
		StringBuilder sb = new StringBuilder();
		sb.append(BRICSStringUtils.capitalizeFirstCharacter(firstName.trim()));

		if (!StringUtils.isEmpty(middleName)) {
			sb.append(" ").append(BRICSStringUtils.capitalizeFirstCharacter(middleName.trim()));
		}

		sb.append(" ").append(BRICSStringUtils.capitalizeFirstCharacter(lastName.trim()));
		return sb.toString();
	}

}
