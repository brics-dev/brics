package gov.nih.tbi.account.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.AccountReportType;
import gov.nih.tbi.account.model.hibernate.AccountReportingLog;
import gov.nih.tbi.account.service.hibernate.AccountReportingManager;

@Service
public class AccountReportingReportGenerator {

	private static Logger logger = Logger.getLogger(AccountReportingReportGenerator.class);
	
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	
	private static final String TOKEN_ORG_NAME = "${ORG_NAME}";
	private static final String TOKEN_SYSTEM_LOGO = "${SYSTEM_LOGO}";
	private static final String REPORT_DATE_RANGE ="${DATE_RANGE}";
	private static final String TEMP_FILE_PREFIX = "report_temp";
	private static final String TEMP_FILE_SUFFIX = ".html";
	private static final String REPORT_TEMPLATE_FILE_ALL = "/account/account_reporting/account_reporting_template_all.html";
	private static final String REPORT_TEMPLATE_FILE_MONTHLY = "/account/account_reporting/account_reporting_template_monthly.html";
	private static final String REPORT_TEMPALTE_FILE_SINGLE = "/account/account_reporting/account_reporting_template_single.html";
	private static final String REPORT_TEMP_FILE = "temp_template.html";
	private static final String TEMP_IMAGE_PATH = "logo.png";
	
	
	
	private static final String REPORT_LOGO_IMGSRC = "<img src=\"logo.png\" />";
	
	//private static final String REPORT_LOGO_IMGSRC = "<img src=\"./logos/" + TOKEN_ORG_NAME + "/logo.png\" />";

	private static final String REPORT_LOGO_PATH = "/account/account_reporting/logos/" + TOKEN_ORG_NAME + "/logo.png";
	
	//private static final String REPORT_LOGO_IMGSRC = "<img src=\"/account/account_reporting/logos/fitbir/logo.png\" />";
	
	private static final String DSR_NEW = "${DSR_NEW}";
	private static final String DSR_APPROVED = "${DSR_APPROVED}";
	private static final String DSR_REJECTED = "${DSR_REJECTED}";
	private static final String DSR_AWAITING = "${DSR_AWAITING}";
	private static final String DSR_PENDING = "${DSR_PENDING}";
	private static final String DSR_SINGLE = "${DSR_SINGLE}";
	
	private static final String DAR_NEW = "${DAR_NEW}";
	private static final String DAR_APPROVED = "${DAR_APPROVED}";
	private static final String DAR_REJECTED = "${DAR_REJECTED}";
	private static final String DAR_AWAITING = "${DAR_AWAITING}";
	private static final String DAR_PENDING = "${DAR_PENDING}";
	private static final String DAR_SINGLE = "${DAR_SINGLE}";
	
	private static final String DSR_DAR_NEW = "${DSR_DAR_NEW}";
	private static final String DSR_DAR_APPROVED = "${DSR_DAR_APPROVED}";
	private static final String DSR_DAR_REJECTED = "${DSR_DAR_REJECTED}";
	private static final String DSR_DAR_AWAITING = "${DSR_DAR_AWAITING}";
	private static final String DSR_DAR_PENDING = "${DSR_DAR_PENDING}";
	private static final String DSR_DAR_SINGLE = "${DSR_DAR_SINGLE}";
	
	private static final String OTHER_NEW = "${OTHER_NEW}";
	private static final String OTHER_APPROVED = "${OTHER_APPROVED}";
	private static final String OTHER_REJECTED = "${OTHER_REJECTED}";
	private static final String OTHER_AWAITING = "${OTHER_AWAITING}";
	private static final String OTHER_PENDING = "${OTHER_PENDING}";
	private static final String OTHER_SINGLE = "${OTHER_SINGLE}";
	
	
	private static final String ACCOUNT_REQUEST_TYPE = "${ACCOUNT_REQUEST_TYPE}";
	
	private static final String RUNNING_TALLY_APPROVED = "${RUNNING_TALLY_APPROVED}";
	private static final String RUNNING_TALLY_REJECTED = "${RUNNING_TALLY_REJECTED}";
	private static final String RUNNING_TALLY_SUM = "${RUNNING_TALLY_SUM}";
	private static final String RUNNING_TALLY_YEAR = "${RUNNING_TALLY_YEAR}";
	
	
	private static final String DSR = "DSR";
	private static final String DAR = "DAR";
	private static final String DSR_DAR = "DSR + DAR";
	private static final String OTHER = "OTHER";
	
	@Autowired
	private AccountReportingManager accountReportingManager;
	
	@Autowired
	protected ModulesConstants modulesConstants;

	
	
	public byte[] generateReport(String reportType, String startDate, String endDate,  String orgName, List<AccountReportingLog> list, Boolean isMonthlyReport) throws IOException {

	
		logger.debug("Generating report for " + orgName + " from " + startDate +" to " + endDate);
		
		// Create a temporary html file dynamically from the template 
		String templateType;
		if(reportType.equals(AccountReportType.ALL.getName())) {
			
			if(isMonthlyReport) {
				templateType = REPORT_TEMPLATE_FILE_MONTHLY;
			}
			else {
				templateType = REPORT_TEMPLATE_FILE_ALL;
			}
		}
		else {
			templateType = REPORT_TEMPALTE_FILE_SINGLE;
		}
		
		
		InputStream inputStream = getClass().getResourceAsStream(templateType);
		String htmlText = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
		File templateFile = new File(modulesConstants.getAccountReportingRootFilePath() + REPORT_TEMP_FILE);
		 
	    FileUtils.copyInputStreamToFile(inputStream, templateFile);
	    
	    InputStream inputStreamImage = getClass().getResourceAsStream(REPORT_LOGO_PATH.replace(TOKEN_ORG_NAME, orgName.toLowerCase()));
	    File tempImage = new File(modulesConstants.getAccountReportingRootFilePath() + TEMP_IMAGE_PATH);
	    FileUtils.copyInputStreamToFile(inputStreamImage, tempImage);

		htmlText = htmlText.replace(TOKEN_SYSTEM_LOGO, REPORT_LOGO_IMGSRC);
		htmlText = htmlText.replace(TOKEN_ORG_NAME, orgName.replaceAll("_", " "));
		
		String dateRangeText = "";
		
		if(!startDate.isEmpty() && !endDate.isEmpty()) {
			dateRangeText = " from " + startDate + " - " + endDate;
		}
		
		htmlText = htmlText.replace(REPORT_DATE_RANGE, dateRangeText);
			
		switch(AccountReportType.getByName(reportType)) {
		
			case ALL:
				htmlText = populateAll(htmlText, list);
				break;
				
			case NEW:
				htmlText = populateSingle(htmlText, list, AccountReportType.NEW);
				break;
				
			case APPROVED:
				htmlText = populateSingle(htmlText, list, AccountReportType.APPROVED);
				break;
				
			case REJECTED:
				htmlText = populateSingle(htmlText, list, AccountReportType.REJECTED);
				break;
				
			case AWAITING:
				htmlText = populateSingle(htmlText, list, AccountReportType.AWAITING);
				break;
			
			case PENDING:
				htmlText = populateSingle(htmlText, list, AccountReportType.PENDING);
				break;
				
			default:
				logger.error("Could not find report type");
				break;
		
		}
		
		int tallyApproved = accountReportingManager.getRunningTally(AccountReportType.APPROVED);
		int tallyRejected = accountReportingManager.getRunningTally(AccountReportType.REJECTED);
		
		htmlText = htmlText.replace(RUNNING_TALLY_APPROVED, String.valueOf(tallyApproved));
		htmlText = htmlText.replace(RUNNING_TALLY_REJECTED, String.valueOf(tallyRejected));
		htmlText = htmlText.replace(RUNNING_TALLY_SUM, String.valueOf(tallyApproved + tallyRejected));
		
		Calendar cal = Calendar.getInstance();
		
		htmlText = htmlText.replace(RUNNING_TALLY_YEAR, String.valueOf(cal.get(Calendar.YEAR)));
		
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
			
			logger.info("Report file generated for " +  orgName + " from " + startDate +" to " + endDate);

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
	
	private Map<String, Integer> getAccountTypeValues(List<AccountReportingLog> list, String reportType){
		Map<String, Integer> map = new HashMap<>();
		map.put("DSR", 0);
		map.put("DAR", 0);
		map.put("DSR + DAR", 0);
		map.put("OTHER", 0);
		for(AccountReportingLog log: list) {
			if(reportType.equals(log.getAccountReportType().getName())) {
				map.replace(log.getRequestedAccountType().getName(), map.get(log.getRequestedAccountType().getName()), map.get(log.getRequestedAccountType().getName()) +1 );
			}
		}		
		return map;
	}
	private String populateAll(String htmlText, List<AccountReportingLog> list) {
		htmlText = populateNew(htmlText, list);
		htmlText = populateApproved(htmlText, list);
		htmlText = populateRejected(htmlText, list);
		htmlText = populateAwaiting(htmlText, list);
		htmlText = populatePending(htmlText, list);
		
		return htmlText;
		
	}
	
	private String populateNew(String htmlText, List<AccountReportingLog> list) {
		Map<String, Integer> map = getAccountTypeValues(list, AccountReportType.NEW.getName());
		
		htmlText = htmlText.replace(DSR_NEW, map.get(DSR).toString());
		htmlText = htmlText.replace(DAR_NEW, map.get(DAR).toString());
		htmlText = htmlText.replace(DSR_DAR_NEW, map.get(DSR_DAR).toString());
		htmlText = htmlText.replace(OTHER_NEW, map.get(OTHER).toString());
		htmlText = htmlText.replace(ACCOUNT_REQUEST_TYPE, AccountReportType.NEW.getDescription());
			
		return htmlText;
		
	}
	
	private String populateApproved(String htmlText, List<AccountReportingLog> list) {
		Map<String, Integer> map = getAccountTypeValues(list, AccountReportType.APPROVED.getName());
		
		htmlText = htmlText.replace(DSR_APPROVED, map.get(DSR).toString());
		htmlText = htmlText.replace(DAR_APPROVED, map.get(DAR).toString());
		htmlText = htmlText.replace(DSR_DAR_APPROVED, map.get(DSR_DAR).toString());
		htmlText = htmlText.replace(OTHER_APPROVED, map.get(OTHER).toString());
		htmlText = htmlText.replace(ACCOUNT_REQUEST_TYPE, AccountReportType.APPROVED.getDescription());
		
		return htmlText;
				
	}
	private String populateRejected(String htmlText, List<AccountReportingLog> list) {
		Map<String, Integer> map = getAccountTypeValues(list, AccountReportType.REJECTED.getName());
		
		htmlText = htmlText.replace(DSR_REJECTED, map.get(DSR).toString());
		htmlText = htmlText.replace(DAR_REJECTED, map.get(DAR).toString());
		htmlText = htmlText.replace(DSR_DAR_REJECTED, map.get(DSR_DAR).toString());
		htmlText = htmlText.replace(OTHER_REJECTED, map.get(OTHER).toString());
		htmlText = htmlText.replace(ACCOUNT_REQUEST_TYPE, AccountReportType.REJECTED.getDescription());
		
		return htmlText;	
	}
	
	private String populatePending(String htmlText, List<AccountReportingLog> list) {
		Map<String, Integer> map = getAccountTypeValues(list, AccountReportType.PENDING.getName());
		
		htmlText = htmlText.replace(DSR_PENDING, map.get(DSR).toString());
		htmlText = htmlText.replace(DAR_PENDING, map.get(DAR).toString());
		htmlText = htmlText.replace(DSR_DAR_PENDING, map.get(DSR_DAR).toString());
		htmlText = htmlText.replace(OTHER_PENDING, map.get(OTHER).toString());
		htmlText = htmlText.replace(ACCOUNT_REQUEST_TYPE, AccountReportType.PENDING.getDescription());

		
		return htmlText;		
	}
	
	private String populateAwaiting(String htmlText, List<AccountReportingLog> list) {
		Map<String, Integer> map = getAccountTypeValues(list, AccountReportType.AWAITING.getName());
		
		htmlText = htmlText.replace(DSR_AWAITING, map.get(DSR).toString());
		htmlText = htmlText.replace(DAR_AWAITING, map.get(DAR).toString());
		htmlText = htmlText.replace(DSR_DAR_AWAITING, map.get(DSR_DAR).toString());
		htmlText = htmlText.replace(OTHER_AWAITING, map.get(OTHER).toString());
		htmlText = htmlText.replace(ACCOUNT_REQUEST_TYPE, AccountReportType.AWAITING.getDescription());

		
		return htmlText;		
	}
	
	private String populateSingle(String htmlText, List<AccountReportingLog> list, AccountReportType accountReportType) {
		
		Map<String, Integer> map = getAccountTypeValues(list,accountReportType.getName());
		
		htmlText = htmlText.replace(DSR_SINGLE, map.get(DSR).toString());
		htmlText = htmlText.replace(DAR_SINGLE, map.get(DAR).toString());
		htmlText = htmlText.replace(DSR_DAR_SINGLE, map.get(DSR_DAR).toString());
		htmlText = htmlText.replace(OTHER_SINGLE, map.get(OTHER).toString());
		htmlText = htmlText.replace(ACCOUNT_REQUEST_TYPE, AccountReportType.AWAITING.getDescription());
		
		return htmlText;
	}


}
