package gov.nih.brics.job;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.AccountReportType;
import gov.nih.tbi.account.model.hibernate.AccountReportingLog;
import gov.nih.tbi.account.service.AccountReportingReportGenerator;
import gov.nih.tbi.account.service.hibernate.AccountReportingManager;

@Component
@Scope("singleton")
public class AccountReportingEmailJob extends EmailJobs
{

	private static Logger log = Logger.getLogger(AccountReportingEmailJob.class);
			
			
	@Autowired
	AccountReportingReportGenerator generator;
	
	@Autowired
	AccountReportingManager accountReportingManger;
	
	@Autowired
	ModulesConstants modulesConstants;
	
	private static final String REPORT_SUBJECT = "Monthly Account Email Report";
	private static final String EMAIL_BODY = "This is the monthly account reports email for ";
	private static final String FILE_NAME = " Monthly Account Report.pdf";
	private static final String FILE_PATH = "/account/";
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public void doJob() {
		log.log(Level.INFO, "---------Beginning AccountReportingEmailJob job");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Date endDate = cal.getTime();
		cal.set(Calendar.DATE, 1);
		Date startDate = cal.getTime();
		
		String start = dateFormat.format(startDate);
		String end = dateFormat.format(endDate);
		
		List<AccountReportingLog> list = accountReportingManger.generateReport(AccountReportType.ALL, startDate, endDate);
		String emailAddress = modulesConstants.getModulesOrgEmail();
		String monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
		String emailBody = EMAIL_BODY + monthName;	
		String fileName = monthName + " " + cal.get(Calendar.YEAR) + FILE_NAME;

		File attachment = new File(modulesConstants.getAccountReportingRootFilePath() + FILE_PATH + fileName);
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] file = generator.generateReport(AccountReportType.ALL.getName(), start, end,
					modulesConstants.getModulesOrgName(), list, true);
			baos.write(file);
			
			InputStream stream = new ByteArrayInputStream(file);
			
			FileUtils.copyInputStreamToFile(stream, attachment);
			
			stream.close();

		} catch (Exception e) {
			log.error("Error creating account report!");
			e.printStackTrace();
		}
		
		DataSource source  = new FileDataSource(attachment);	
		try {
			sendEmailWithAttachment(REPORT_SUBJECT, emailBody, emailAddress, source, fileName);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
		log.log(Level.INFO, "---------End AccountReportingEmailJob job");
		
		
	}
	
}
