package gov.nih.tbi.account.portal;

import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import gov.nih.tbi.account.model.AccountType;
import gov.nih.tbi.repository.model.hibernate.UserFile;

public class RenewalRequestActionTest {

	@BeforeMethod
	public void init() {
		
	}

	@Test
	public void checkDsrMissingFileTest() {

		List<UserFile> userFiles = new ArrayList<UserFile>();

		Set<String> missingFiles1 = RenewalRequestAction.checkMissingFiles(AccountType.DSR, userFiles);
		assertTrue(missingFiles1.size() == 1 && missingFiles1.contains(RenewalRequestAction.DSR_FILE_TYPE));

		UserFile dsrUserFile = new UserFile();
		dsrUserFile.setDescription(RenewalRequestAction.DSR_FILE_TYPE);

		// Test the upload date limit
		// Set the file uploaded date to one day before the date limit, so it is not the latest and not counted
		Calendar cal1 = Calendar.getInstance();
		cal1.add(Calendar.DATE, -1 - RenewalRequestAction.FILE_UPDATED_DAY_LIMIT);
		Date uploadDate = cal1.getTime();
		dsrUserFile.setUploadedDate(uploadDate);
		userFiles.add(dsrUserFile);

		Set<String> missingFiles2 = RenewalRequestAction.checkMissingFiles(AccountType.DSR, userFiles);
		assertTrue(missingFiles1.size() == 1 && missingFiles2.contains(RenewalRequestAction.DSR_FILE_TYPE));

		// Set uploaded date to one day after the file updated date limit, so it counts
		Calendar cal2 = Calendar.getInstance();
		cal2.add(Calendar.DATE, 1 - RenewalRequestAction.FILE_UPDATED_DAY_LIMIT);
		Date uploadDate2 = cal2.getTime();
		dsrUserFile.setUploadedDate(uploadDate2);

		Set<String> missingFiles3 = RenewalRequestAction.checkMissingFiles(AccountType.DSR, userFiles);
		assertTrue(missingFiles3 == null || missingFiles3.isEmpty());
	}

	
	@Test
	public void checkDarMissingFileTest() {

		List<UserFile> userFiles = new ArrayList<UserFile>();

		Set<String> missingFiles1 = RenewalRequestAction.checkMissingFiles(AccountType.DAR, userFiles);
		assertTrue(missingFiles1.size() == 2 && missingFiles1.contains(RenewalRequestAction.DAR_FILE_TYPE)
				&& missingFiles1.contains(RenewalRequestAction.BIO_FILE_TYPE));

		// Set uploaded date to one day after the file updated date limit, so it counts
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1 - RenewalRequestAction.FILE_UPDATED_DAY_LIMIT);
		Date uploadDate = cal.getTime();

		UserFile darUserFile = new UserFile();
		darUserFile.setDescription(RenewalRequestAction.DAR_FILE_TYPE);
		darUserFile.setUploadedDate(uploadDate);
		
		UserFile bioUserFile = new UserFile();
		bioUserFile.setDescription(RenewalRequestAction.BIO_FILE_TYPE);
		bioUserFile.setUploadedDate(uploadDate);
		
		userFiles.add(darUserFile);
		Set<String> missingFiles2 = RenewalRequestAction.checkMissingFiles(AccountType.DAR, userFiles);
		assertTrue(missingFiles2.size() == 1 && missingFiles2.contains(RenewalRequestAction.BIO_FILE_TYPE));

		userFiles.clear();
		
		userFiles.add(bioUserFile);
		Set<String> missingFiles3 = RenewalRequestAction.checkMissingFiles(AccountType.DAR, userFiles);
		assertTrue(missingFiles3.size() == 1 && missingFiles3.contains(RenewalRequestAction.DAR_FILE_TYPE));

		userFiles.clear();
		
		userFiles.add(darUserFile);
		userFiles.add(bioUserFile);
		Set<String> missingFiles4 = RenewalRequestAction.checkMissingFiles(AccountType.DAR, userFiles);
		assertTrue(missingFiles4 == null || missingFiles4.isEmpty());
	}

	
	@Test
	public void checkDsrdarMissingFileTest() {

		List<UserFile> userFiles = new ArrayList<UserFile>();

		Set<String> missingFiles1 = RenewalRequestAction.checkMissingFiles(AccountType.DSRDAR, userFiles);
		assertTrue(missingFiles1.size() == 3 && missingFiles1.contains(RenewalRequestAction.DAR_FILE_TYPE)
				&& missingFiles1.contains(RenewalRequestAction.DSR_FILE_TYPE)
				&& missingFiles1.contains(RenewalRequestAction.BIO_FILE_TYPE));

		// Set uploaded date to one day after the file updated date limit, so it counts
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1 - RenewalRequestAction.FILE_UPDATED_DAY_LIMIT);
		Date uploadDate = cal.getTime();

		UserFile darUserFile = new UserFile();
		darUserFile.setDescription(RenewalRequestAction.DAR_FILE_TYPE);
		darUserFile.setUploadedDate(uploadDate);
		
		UserFile dsrUserFile = new UserFile();
		dsrUserFile.setDescription(RenewalRequestAction.DSR_FILE_TYPE);
		dsrUserFile.setUploadedDate(uploadDate);
		
		UserFile bioUserFile = new UserFile();
		bioUserFile.setDescription(RenewalRequestAction.BIO_FILE_TYPE);
		bioUserFile.setUploadedDate(uploadDate);
		
		userFiles.add(darUserFile);
		Set<String> missingFiles2 = RenewalRequestAction.checkMissingFiles(AccountType.DSRDAR, userFiles);
		assertTrue(missingFiles2.size() == 2 && missingFiles1.contains(RenewalRequestAction.DSR_FILE_TYPE)
				&& missingFiles2.contains(RenewalRequestAction.BIO_FILE_TYPE));

		userFiles.clear();

		userFiles.add(dsrUserFile);
		Set<String> missingFiles3 = RenewalRequestAction.checkMissingFiles(AccountType.DSRDAR, userFiles);
		assertTrue(missingFiles3.size() == 2 && missingFiles3.contains(RenewalRequestAction.DAR_FILE_TYPE)
				&& missingFiles3.contains(RenewalRequestAction.BIO_FILE_TYPE));

		userFiles.clear();
		
		userFiles.add(bioUserFile);
		Set<String> missingFiles4 = RenewalRequestAction.checkMissingFiles(AccountType.DSRDAR, userFiles);
		assertTrue(missingFiles4.size() == 2 && missingFiles4.contains(RenewalRequestAction.DAR_FILE_TYPE)
				&& missingFiles4.contains(RenewalRequestAction.DSR_FILE_TYPE));

		userFiles.clear();
		
		userFiles.add(darUserFile);
		userFiles.add(dsrUserFile);
		Set<String> missingFiles5 = RenewalRequestAction.checkMissingFiles(AccountType.DSRDAR, userFiles);
		assertTrue(missingFiles5.size() == 1 && missingFiles5.contains(RenewalRequestAction.BIO_FILE_TYPE));
		
		userFiles.clear();
		
		userFiles.add(darUserFile);
		userFiles.add(bioUserFile);
		Set<String> missingFiles6 = RenewalRequestAction.checkMissingFiles(AccountType.DSRDAR, userFiles);
		assertTrue(missingFiles6.size() == 1 && missingFiles6.contains(RenewalRequestAction.DSR_FILE_TYPE));
		
		userFiles.clear();
		
		userFiles.add(dsrUserFile);
		userFiles.add(bioUserFile);
		Set<String> missingFiles7 = RenewalRequestAction.checkMissingFiles(AccountType.DSRDAR, userFiles);
		assertTrue(missingFiles7.size() == 1 && missingFiles7.contains(RenewalRequestAction.DAR_FILE_TYPE));
		
		userFiles.clear();
		userFiles.add(darUserFile);
		userFiles.add(dsrUserFile);
		userFiles.add(bioUserFile);
		Set<String> missingFiles8 = RenewalRequestAction.checkMissingFiles(AccountType.DSRDAR, userFiles);
		assertTrue(missingFiles8 == null || missingFiles8.isEmpty());
	}

}
