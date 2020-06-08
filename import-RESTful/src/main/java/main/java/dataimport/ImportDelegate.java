package main.java.dataimport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.TreeSet;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.ApplicationsConstants;
import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.AccountProvider;
import gov.nih.tbi.commons.AppConfig;
import gov.nih.tbi.commons.WebstartException;
import gov.nih.tbi.commons.WebstartRestProvider;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.DatasetFileStatus;
import gov.nih.tbi.commons.model.exceptions.DataIsolationException;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.ws.HashMethods;
import gov.nih.tbi.dictionary.validation.ValidationController;
import gov.nih.tbi.dictionary.validation.model.DataStructureTable;
import gov.nih.tbi.dictionary.validation.model.DataSubmission;
import gov.nih.tbi.dictionary.validation.model.FileNode;
import gov.nih.tbi.dictionary.validation.model.ValidationOutput;
import gov.nih.tbi.ordermanager.model.DerivedBiosampleConfigurations;
import gov.nih.tbi.repository.UploadItem;
import gov.nih.tbi.repository.UploadManagerController;
import gov.nih.tbi.repository.UploadStatus;
import gov.nih.tbi.repository.model.SubmissionTicket;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.ws.RepositoryProvider;
import main.java.dataimport.exception.BiosampleRepositoryNotFound;
import main.java.dataimport.exception.DerivedDataConfigurationException;
import main.java.dataimport.model.SubmissionMetaData;

public class ImportDelegate {
	private static final Logger logger = Logger.getLogger(ImportDelegate.class);

	@Autowired
	private ModulesConstants modulesConstants;

	String WORK_DIRECTORY_PATH;

	String MIRTH_CONNECT_CHANNEL;

	String EMAIL_SENDER = "";

	// XXX: BL: need to fully bake or get rid of this threading model
	private int uploadToken = ApplicationsConstants.MAXIMUM_CONCURRENT_UPLOAD;

	// Controller and connection
	private String rootDirectoryPath;
	private String bricsUrl;
	private String ddtUrl;
	private String userName;
	private String password;
	private String queryToolUrl;
	private String operationsEmail;

	private ValidationController controller;

	// Mail sender
	private String host;

	// stores the list of upload items loaded
	private static List<UploadItem> uploadList = new ArrayList<UploadItem>();
	// list of upload items still in queue
	private static Queue<UploadItem> uploadQueue = new LinkedList<UploadItem>();
	private static int tableIndex = 0;

	private final static int VALIDATION_MESSAGE_LIMIT_BYTES = 1024;
	
	private static final boolean IS_COMING_FROM_PROFORMS = true;

	/**
	 * Retrieve configuration for deriving data. Configuration is derived from derivedDataConfiguration.xml resource.
	 * 
	 * @return
	 */
	private DerivedBiosampleConfigurations getDerivedDataConfiguration() {
		FileReader fileReader = null;
		DerivedBiosampleConfigurations derivedDataConfiguration = null;

		try {
			JAXBContext jc = JAXBContext.newInstance(DerivedBiosampleConfigurations.class);
			InputStream inputStream = getClass().getResourceAsStream("/derivedDataConfiguration.xml");
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			derivedDataConfiguration = (DerivedBiosampleConfigurations) unmarshaller.unmarshal(inputStream);
		} catch (JAXBException e) {
			e.printStackTrace();
		} finally {
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return derivedDataConfiguration;
	}

	/**
	 * Appends column headers for data what we are going to derive and run it through the validation service. Returns
	 * true if the resulting csv is valid, false otherwise.
	 * 
	 * @param derivedDataConfiguration
	 * @param submissionFileLocation
	 * @param repositoryName
	 * @return
	 */
	private boolean validateCatalog(DerivedBiosampleConfigurations derivedDataConfiguration,
			SubmissionMetaData metaData, String repositoryName) {

		// write a copy of the original catalog with columns for our derived data appended to column headers
		CatalogDummyDataService dummyDataService = new CatalogDummyDataService(derivedDataConfiguration,
				metaData.getSubmissionFileLocation(), repositoryName);
		File dummyFile = null;

		try {
			dummyFile = dummyDataService.writeDummyCatalog();
		} catch (BiosampleRepositoryNotFound e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			emailToUser(e.getMessage(), false, metaData.getUserEmail());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			emailToUser(e.getMessage(), false, metaData.getUserEmail());
		}

		logger.info("Validating dummy CSV: " + dummyFile);
		List<String> errorMessages = validate(dummyFile.getParent() + File.separator ,metaData);

		// if the csv is valid
		if (errorMessages.toString()
				.contains("All files are valid. Click on a validated file from the list to view its warnings and/or errors., A new submission file has been created in the working directory.")) {
			logger.info("Dummy file is valid, going ahead to map derived data.");
			return true;
		} else { // if the csv is invalid
			errorMessages.add("Catalog failed validation before data derivation");
			emailBioSampleMessageToUser(errorMessages, false, metaData.getUserEmail()); // notify OPs that validation
																						 // failed
			logger.error("Dummy file validation failed with the following errors: " + errorMessages);
			return false;
		}
	}

	boolean emailToUser(String message, boolean success, String email) {
		List<String> messages = new ArrayList<String>();
		messages.add(message);
		return emailToUser(messages, success, email);
	}

	public RepositoryProvider getRepositoryProvider(Account account) {
		RepositoryProvider repositoryProvider = null;
		try {
			repositoryProvider = new RepositoryProvider(bricsUrl, account.getUserName(), HashMethods
					.getServerHash(account.getUserName(), HashMethods.convertFromByte(account.getPassword())));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return repositoryProvider;
	}

	public RepositoryProvider getRepositoryProvider() {
		try {
			return new RepositoryProvider(bricsUrl, userName, password);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public String processJob(SubmissionMetaData metaData, boolean isBiosample, String repositoryName) {
		WORK_DIRECTORY_PATH = metaData.getSubmissionLocation();

		loadProperties();

		Account submitterAccount = null;

		if (!isBiosample && !metaData.isSubjectSubmitted()) {
			submitterAccount = getAccountById(metaData.getLockedByAccountId());
		}

		RepositoryProvider repositoryProvider = null;
		
		String result = "";

		// there are some submission that are not properly linked to a BRICS user ID. Fallback to use anonymous user in
		// that case.
		if (submitterAccount == null) {
			if (!metaData.isSubjectSubmitted()) {
				logger.warn(
						"User with ID " + metaData.getLockedByAccountId() + " not found.  Setting user to anonymous.");
			}
			repositoryProvider = getRepositoryProvider();
		} else {
			repositoryProvider = getRepositoryProvider(submitterAccount);
		}

		Study study = repositoryProvider.getStudyByPrefixedId(metaData.getStudyPrefixedId());

		if (study == null) {
			throw new NullPointerException("Was not able to find study with ID: " + metaData.getStudyPrefixedId());
		}

		String studyName = study.getTitle();
		logger.info("Current submission study: " + studyName);

		if (isBiosample) {
			result = processBiosample(repositoryProvider, metaData, study, repositoryName);
		} else {
			result = processSubmission(repositoryProvider, metaData, study, repositoryName);
		}
		
		return result;
	}

	public String processBiosample(RepositoryProvider repositoryProvider, SubmissionMetaData metaData, Study study,
			String repositoryName) {

		// load configurations for deriving data
		DerivedBiosampleConfigurations derivedDataConfiguration = getDerivedDataConfiguration();

		if (derivedDataConfiguration == null) {
			String errorMessage = "Derived data configuration XML not found! (derivedDataConfiguration.xml)";
			emailToUser(errorMessage, false, metaData.getUserEmail());
			throw new DerivedDataConfigurationException(errorMessage);
		}

		// validate the catalog with dummy data before attempting to derive data
		if (validateCatalog(derivedDataConfiguration, metaData, repositoryName)) {
			WebstartRestProvider webstartRestProvider = new WebstartRestProvider(ddtUrl,
					modulesConstants.getAdministratorUsername(), modulesConstants.getSaltedAdministratorPassword());
			DerivedDataMapper derivedDataMapper = new DerivedDataMapper(derivedDataConfiguration, queryToolUrl,
					webstartRestProvider, metaData.getSubmissionFileLocation(), repositoryName, bricsUrl);

			try {
				// map derived data to a new csv
				List<String> messages = derivedDataMapper.mapDerivedData();
				String derivedDataSubmissionLocation = metaData.getSubmissionLocation()
						+ DerivedDataMapper.DERIVED_DATA_DIRECTORY_NAME + File.separator;
				logger.info("Submitting to repository using: " + derivedDataSubmissionLocation);
				submitToRepository(repositoryProvider, metaData, study, derivedDataSubmissionLocation, messages, true);
			} catch (Exception e) {
				e.printStackTrace();
				emailToUser(e.toString(), false, metaData.getUserEmail());
				return "Failed";
			}
			
			return "Success";
		}
		
		
		return "Failed";
	}

	public String processSubmission(RepositoryProvider repositoryProvider, SubmissionMetaData metaData, Study study,
			String repositoryName) {
		String result = submitToRepository(repositoryProvider, metaData, study, rootDirectoryPath, null, false);
		return result;
	}

	private Account getAccountById(Long id) {
		try {
			AccountProvider accountProvider = new AccountProvider(modulesConstants.getModulesAccountURL(),
					modulesConstants.getAdministratorUsername(),
					HashMethods.getServerHash(modulesConstants.getAdministratorUsername(),
							modulesConstants.getSaltedAdministratorPassword()));

			return accountProvider.getAccountById(id);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String submitToRepository(RepositoryProvider repositoryProvider, SubmissionMetaData metaData, Study study,
			String submissionDirectory, List<String> messages, boolean isBiosample) {


		// lets also move all status messages to constants
		logger.info("Validating Package");
		logger.info("Submission location: " + submissionDirectory);

		if (messages == null) {
			messages = new ArrayList<String>();
		}

		// 1. Validate package by pointing to the working directory
		List<String> validationErrors = validate(submissionDirectory, metaData);
		logger.info("Validation messages: " + validationErrors);
		messages.addAll(validationErrors); // add validation errors into the rest of our messages to be sent to user.

		// there are only text based returns in the controller
		if (messages.toString()
				.contains("All files are valid. Click on a validated file from the list to view its warnings and/or errors., A new submission file has been created in the working directory.")) {

			if (isBiosample) {
				// archive all
				logger.info("Archiving...");
				archiveAll(study.getTitle());
			}

			// 2. Submit the Package to the BRICS repository
			logger.info("Submitting package to the BRICS repository...");

			Date submitDate = BRICSTimeDateUtil.parseRepositoryDate(metaData.getLockedDate());
			Long administeredFormId = metaData.getAdministeredFormId();

			messages.addAll(submit(repositoryProvider, submissionDirectory, metaData.getDatasetName(), study,
					isBiosample, submitDate, administeredFormId, metaData.isSubjectSubmitted()));
			messages.add(submissionDirectory);
			logger.info("Messages after submitting the package: " + messages.toString());



			if (!isBiosample) {
				sendMessageToHTTP(messages, true, metaData.getUserEmail(), metaData.getAdministeredFormId(),
						submissionDirectory);
			} else {
				// Email success to user
				emailBioSampleMessageToUser(messages, true, metaData.getUserEmail());
			}
			return "Success";
		} else {
			logger.error("Submission validation failed!");
			// Email failure to user along with the results
			messages.add(metaData.getDatasetName());
			messages.add(study.getPrefixedId());
			messages.add(submissionDirectory);

			if (!isBiosample) {
				sendMessageToHTTP(messages, false, metaData.getUserEmail(), metaData.getAdministeredFormId(),
						submissionDirectory);
			} else {
				// Email failure to user
				emailBioSampleMessageToUser(messages, false, metaData.getUserEmail());
			}
		}
		return "Failure";
	}

	private void archiveAll(String studyName) {
		logger.info("Archiving " + studyName);

		try {
			String url = bricsUrl + "portal/ws/repository/repository/Study/archiveAll/"
					+ URLEncoder.encode(studyName, "UTF-8");

			if (logger.isDebugEnabled()) {
				logger.debug("Archive Web Service URL: " + url);
			}

			WebClient client = WebClient.create(url);

			Response response = client.accept("text/xml").get(Response.class);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void loadProperties() {


		// bricsUrl = modulesProperties.getProperty("modules.vt.url.server");
		bricsUrl = modulesConstants.getModulesVTURL();
		// logger.debug("BRICS Base URL: " + bricsUrl);

		// ddtUrl = modulesProperties.getProperty("modules.ddt.url.server");
		ddtUrl = modulesConstants.getModulesDDTURL();
		// logger.debug("Dictionary Base URL: " + ddtUrl);

		// MIRTH_CONNECT_CHANNEL = commonsProperties.getProperty("MIRTH.connect.channel.url");
		MIRTH_CONNECT_CHANNEL = modulesConstants.getMirthConnectChannelUrl();
		// logger.debug("Mirth Channel URL: " + MIRTH_CONNECT_CHANNEL);

		// queryToolUrl = modulesProperties.getProperty("modules.qt.url.server");
		queryToolUrl = modulesConstants.getModulesQTURL();
		// logger.debug("Query Tool URL: " + queryToolUrl);

		// operationsEmail = modulesProperties.getProperty("modules.org.email");
		operationsEmail = modulesConstants.getModulesOrgEmail();
		logger.debug("Operations Email Address: " + operationsEmail);

		this.userName = "anonymous";
		this.password = "";

		rootDirectoryPath = WORK_DIRECTORY_PATH;  // this will eventually be part of the commons.prop

		// change this to localhost if you are going to run this locally
		host = "mailfwd.nih.gov";
		//host = "localhost";

		String portalRoot = this.modulesConstants.getModulesPortalRoot();

		boolean appConfigIsSet = AppConfig.getIsSet();

		if (!appConfigIsSet) {

			String sftpName = this.modulesConstants.getWebstartSftpName();
			String sftpBaseDir = this.modulesConstants.getWebstartSftpBasedir();
			String sftpPort = this.modulesConstants.getWebstartSftpPort();
			String sftpUrl = this.modulesConstants.getWebstartSftpUrl();
			String sftpUser = this.modulesConstants.getWebstartSftpUser();
			String sftpPasswd = this.modulesConstants.getWebstartSftpPassword();

			this.setSubmissionToolAppConfig(this.bricsUrl, this.ddtUrl, this.userName, this.password, portalRoot,
					sftpName, sftpBaseDir, sftpPort, sftpUrl, sftpUser, sftpPasswd);
		}

	}

	public List<String> validate(String directoryPath, SubmissionMetaData metaData) {
		// return will be the results
		try {
			controller = new ValidationController(null, bricsUrl, ddtUrl, userName, password, IS_COMING_FROM_PROFORMS);

			if (controller == null) {
				logger.error("Validation controller was not created correctly.");
			} else {
				logger.info("Validation controller created.");
			}
		} catch (Exception e) {
			logger.fatal("Unable to connect to FITBIR. ERROR: \n", e);
		}

		// create a list of messages that will later serve as the response header and content
		List<String> messages = new ArrayList<String>();

		// Load files for processing.
		logger.info("Loading files from: " + directoryPath);
		// TODO may want to replace the null with something else
		String wsLoadMsg = controller.load(directoryPath);
		messages.add(wsLoadMsg);
		logger.debug("Message after loading files: " + messages.toString());

		// Run the validation rules against the loaded files.
		logger.info("Validating the submission...");
		messages.add(controller.validate());
		logger.debug("Messages after validation: " + messages.toString());

		// Get any errors from the submission object.
		messages.addAll(getErrors(controller.getSubmission(), controller.getSubmission().getRoot(), true));
		logger.debug("Messages after processing any errors: " + messages.toString());

		// Build the submission package.
		logger.info("Building the submission package...");
		messages.add(controller.buildSubmission(directoryPath,true,metaData.getDatasetName()));

		logger.info("Validation messages: " + messages.toString());

		return messages;
	}

	public List<String> submit(RepositoryProvider repositoryProvider, String submissionDirectory, String dataSetName,
			Study study, boolean isDerived, Date submitDate, Long administeredFormId, boolean isSubjectSubmitted) {

		// submits or uploads the successfully created package to brics
		logger.info("Submitting valid job to upload manager...");

		List<String> submissionInformation = new ArrayList<String>();

		try {
			// unmarshal the submission package into SubmissionPackage Object
			SubmissionTicket submissionTicket = UploadManagerController
					.unmarshalSubmissionTicket(submissionDirectory + getSubmissionFiles(submissionDirectory).get(0));

			Dataset dataset = processSubmissionTicket(repositoryProvider, submissionDirectory, submissionTicket,
					dataSetName, study, isDerived, submitDate, administeredFormId, isSubjectSubmitted);

			loadUploadQueue(repositoryProvider, dataset, isDerived);

			submissionInformation.add(dataSetName);
			submissionInformation.add(study.getPrefixedId());
			submissionInformation.add(submissionTicket.getEnvironment());
			submissionInformation.add(submissionTicket.getVersion());
			submissionInformation.add(submissionTicket.getSubmissionPackages().get(0).getName());

			logger.info("Submitted job for data set: " + dataset.getName());


		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return submissionInformation;
	}

	public Dataset processSubmissionTicket(RepositoryProvider repositoryProvider, String submissionDirectory,
			SubmissionTicket submissionTicket, String dataSetName, Study study, boolean isDerived, Date submitDate,
			Long administeredFormId, boolean isSubjectSubmitted) throws MalformedURLException {
		String sftpBaseDir = modulesConstants.getWebstartSftpBasedir();

		String serverPath = sftpBaseDir + study.getPrefixedId() + ServiceConstants.FILE_SEPARATER + dataSetName
				+ ServiceConstants.FILE_SEPARATER;
		logger.info("Processing submission ticket at " + serverPath);

		boolean isProformsSubmission;

		// biosample automatically not a proforms submission, all other submissions will be from proforms.
		if (isDerived) {
			isProformsSubmission = false;
		} else {
			isProformsSubmission = true;
		}

		Dataset result = repositoryProvider.processSubmissionTicket(submissionTicket, submissionDirectory, serverPath,
				study.getTitle(), dataSetName, isDerived, isProformsSubmission, submitDate, administeredFormId,
				isSubjectSubmitted);

		return result;
	}

	private List<String> getErrors(DataSubmission submission, FileNode node, boolean warnings) {
		// this list can be used for both an email and a potential data transfer
		// to ProFoRMS

		List<String> errorList = new ArrayList<String>();

		DataStructureTable table = submission.getFileData(node);

		// If there is a data table for this file
		if (table != null) {
			TreeSet<ValidationOutput> output;
			if (warnings) {
				output = table.getWarnings();
			}
			output = table.getErrors();
			for (ValidationOutput o : output) {
				errorList.add(o.getTypeString() + ": " + o.toString());
			}
		}

		// Recursively add errors to a running list children's errors
		for (int i = 0; i < node.getChildCount(); i++) {
			errorList = getErrors(submission, (FileNode) node.getChildAt(i), warnings);
		}

		return errorList;
	}

	/**
	 * Write the entire validation message to the submission direct/failed validation message.txt
	 * 
	 * @param submissionDirectory
	 * @param validationMessage
	 */
	private void writeValidationMessage(String submissionDirectory, String validationMessage) {
		File validationFailedFile = new File(submissionDirectory + File.separator + "failed validation message.txt");
		PrintStream stream = null;

		try {
			if (!validationFailedFile.exists()) {
				if (validationFailedFile.getParentFile() != null) {
					validationFailedFile.getParentFile().mkdirs();
				}
				validationFailedFile.createNewFile();
			}

			stream = new PrintStream(validationFailedFile);
			stream.println(validationMessage);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}

	/**
	 * This method sends a message back to the step 2 mirth channel to let it know whether or not the submission passed
	 * validation.
	 * 
	 * @param messages
	 * @param success
	 * @param emailAddress
	 * @param administeredFormId
	 * @param submissionDirectory
	 * @return
	 */
	boolean sendMessageToHTTP(List<String> messages, boolean success, String emailAddress, Long administeredFormId,
			String submissionDirectory) {
		logger.info("Constructing Mirth message...");

		// This MUST be a constant
		String url = MIRTH_CONNECT_CHANNEL;

		if (url == null) {
			logger.error("Mirth connect channel is null.");
			return false;
		}

		// build text of email
		StringBuilder validationResultsText = new StringBuilder();

		if (success) {
			validationResultsText.append("Your import was successful with the following messages: \n");
		} else {
			validationResultsText.append("Your import failed with the following messages: \n");
		}

		for (String line : messages) {
			if (line != null) {
				validationResultsText.append("*" + line + "\n");
			}
		}

		String validationResults = validationResultsText.toString();
		
		if(!success) {
			emailToUser(messages, false, emailAddress);
		}

		// we used to send back the entire validation message. This turned out to be a bad idea when validation messages
		// are over the post size limit.
		// However during validation failure we will need to start the message with "Your import failed" or Mirth
		// won't
		// know that the submission failed validation.
		if (validationResults.getBytes().length > VALIDATION_MESSAGE_LIMIT_BYTES) {
			if (validationResults.contains("Your import failed")) {
				writeValidationMessage(submissionDirectory, validationResults);
				validationResults =
						"Your import failed.   We are unable to show you the validation message due to it being too long.  You will be able to find the validation logs inside "
								+ submissionDirectory;
			} else {
				validationResults = "Success";
			}
		}


		logger.info("Sending message back to Mirth...");
		logger.info("Email: " + emailAddress);
		logger.debug("Validation Message:\n" + validationResults);  // this can get very long. Set it to debug instead
		logger.info("Administered Form IDs: " + administeredFormId.toString());


		return true;
	}

	boolean emailToUser(List<String> messages, boolean success, String emailAddress) {
		// sends out an email with the responses for now lets print a response

		logger.info("The process was successful: " + success);
		logger.info("Messages: " + messages);
		logger.info("Sent to: " + emailAddress);

		// Recipient's email ID needs to be mentioned.
		String to = emailAddress;

		// Sender's email ID needs to be mentioned
		EMAIL_SENDER = modulesConstants.getDataProcessorEmailSender();
		logger.info("Email Sender : " + EMAIL_SENDER);

		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.setProperty("mail.smtp.host", host);

		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties);

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(EMAIL_SENDER));

			// Set To: header field of the header.
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

			// Set Subject: header field
			message.setSubject("Data Import Results");

			// build text of email
			StringBuilder emailText = new StringBuilder();

			for (String line : messages) {
				if (line != null) {
					emailText.append("*" + line + "\n");
				}
			}

			// Now set the actual message
			if (success) {
				message.setText("Your import was successful with the following messages: \n" + emailText.toString());
			} else {
				message.setText("Your import failed with the following messages: \n" + emailText.toString());
			}

			// Send message
			Transport.send(message);

			logger.info("Message Sent to User");
		} catch (MessagingException mex) {
			logger.error("Message failed with exception: ", mex);
			return false;
		}

		return true;
	}

	boolean emailBioSampleMessageToUser(List<String> messages, boolean success, String emailAddress) {
		// sends out an email with the responses for now lets print a response

		logger.info("The process was successful: " + success);
		logger.info("Messages: " + messages);
		logger.info("Sent to: " + emailAddress);

		// Recipient's email ID needs to be mentioned.
		String to = emailAddress;

		// Sender's email ID needs to be mentioned
		EMAIL_SENDER = modulesConstants.getDataProcessorEmailSender();
		logger.info("Email Sender : " + EMAIL_SENDER);

		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.setProperty("mail.smtp.host", host);

		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties);

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(EMAIL_SENDER));

			// Set To: header field of the header.
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

			// Set Subject: header field
			message.setSubject("Data Import Results");

			// Set content to html


			// build text of email
			StringBuilder emailText = new StringBuilder();

			emailText.append(createBiosampleTableAndHeader());

			for (String line : messages) {
				if (line != null && appendLine(line) != "") {
					emailText.append(appendLine(line));
				}
			}

			emailText.append(closeTableTag());

			// Now set the actual message
			if (success) {
				message.setContent("Your import was successful with the following messages: \n" + emailText.toString(),
						"text/html; charset=utf-8");
			} else {
				message.setContent("Your import failed with the following messages: \n" + emailText.toString(),
						"text/html; charset=utf-8");
			}

			// Send message

			Transport.send(message);

			logger.info("Message Sent to User");
		} catch (MessagingException mex) {
			logger.error("Message failed with exception: ", mex);
			return false;
		}

		return true;
	}

	private static String createBiosampleTableAndHeader() {
		return "<table border = \"1\">\n<tr><th>GUID</th><th>Visit Type</th><th>Form Title</th><th>Error/Issue</th><th>Site Name</th><th>Sample Type</th></tr>\n";
	}

	private static String appendLine(String s) {
		HashMap<String, String> map = parseBiosampleLine(s);

		if (map.isEmpty()) {
			return "";
		}

		return "<tr>" + "<th>" + map.get("GUID") + "</th>" + "<th>" + map.get("Visit Type") + "</th>" + "<th>"
				+ map.get("Form Title") + "</th>" + "<th>" + map.get("Error") + "</th>" + "<th>" + map.get("SiteName")
				+ "</th>" + "<th>" + map.get("SampleType") + "</th>" + "</tr>\n";
	}

	private static String closeTableTag() {
		return "</table>";
	}

	private static HashMap<String, String> parseBiosampleLine(String s) {

		HashMap<String, String> map = new HashMap<String, String>();
		String[] stringArray = s.split(";");

		if (stringArray.length < 4) {
			return new HashMap<String, String>();
		}
		map.put("GUID", stringArray[0]);
		map.put("Visit Type", stringArray[1]);
		map.put("Form Title", stringArray[2]);
		map.put("Error", stringArray[3]);
		map.put("SiteName", stringArray[4]);
		map.put("SampleType", stringArray[5]);
		return map;

	}


	/**
	 * Returns a list of files in the submission directory
	 * 
	 * @param submissionDirectory
	 * @return
	 */
	private List<String> getSubmissionFiles(String submissionDirectory) {
		// This is a list return in case of future iterations where we will need to process multiple tickets etc.
		List<String> submissionFileList = new ArrayList<String>();

		File dir = new File(submissionDirectory);

		File[] files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.contains("submissionTicket");
			}
		});

		if (files != null) {
			for (File submissionFile : files) {
				submissionFileList.add(submissionFile.getName());
			}
		}

		return submissionFileList;
	}

	// TODO Upload queue methods - we may want to abstract these later as I wasn't expecting the controller to
	// be incompatible.

	/**
	 * Loads the upload queue
	 * 
	 * @param dataset
	 */
	private void loadUploadQueue(RepositoryProvider repositoryProvider, Dataset dataset, boolean isDerived) {
		try {
			logger.info("Adding the following data set to the upload queue: " + dataset.toString());
			addToQueue(repositoryProvider, dataset);
		} catch (Exception e) {
			// if fail then return and fail on submission
			logger.error("Could not add data set to queue.", e);
			return;
		}

		logger.info("Upload Token: " + uploadToken);


		// XXX: BL: could be introducing a threading problem here, orig. limited to 3 uploads (then exit)
		// looks like the original idea was to have 3 concurrent threads, but managed elsewhere
		// uploadToken represented maxThreads, I've changed it to represent the number of files for this service import
		// operation

		int numberOfFilesToUpload = dataset.getDatasetFileSet().size();
		for (int i = 0; i < numberOfFilesToUpload; i++) {
			logger.info("Loading file: " + i);
			nextUpload(repositoryProvider, isDerived);
		}
	}

	/**
	 * Add the DatasetFiles from the Dataset into the upload queue
	 * 
	 * @param dataset - Dataset to be added into the upload queue.
	 * @throws Exception
	 */
	public void addToQueue(RepositoryProvider repositoryProvider, Dataset dataset) throws Exception {

		for (DatasetFile datasetFile : repositoryProvider.getDatasetFiles(dataset.getId())) {
			if (DatasetFileStatus.PENDING.equals(datasetFile.getDatasetFileStatus())) {
				String studyTitle = dataset.getStudy().getTitle();
				String prefixedId = dataset.getStudy().getPrefixedId();

				logger.info("Adding dataset upload item to queue");
				logger.info("Dataset upload item title: <" + studyTitle + ">");
				logger.info("Dataset upload item prefixedId: <" + prefixedId + ">");

				UploadItem item =
						new UploadItem(tableIndex, dataset, datasetFile, studyTitle, UploadStatus.QUEUED, prefixedId);

				uploadQueue.add(item);
				logger.info("Upload Queue: " + uploadQueue.toString());
				uploadList.add(item);
				// increment table index for the next item
				tableIndex++;
			}
		}
	}

	/**
	 * Polls the next upload from the upload queue and initiates a new thread to upload it
	 */
	public void nextUpload(RepositoryProvider repositoryProvider, boolean isDerived) {
		UploadItem nextFile = uploadQueue.poll();
		uploadFile(repositoryProvider, nextFile, isDerived);
		uploadToken--;
	}

	public void uploadFile(RepositoryProvider repositoryProvider, UploadItem uploadItem, boolean isDerived) {
		if (uploadItem != null) {

			// start new thread to upload the file
			UploadChannel uploadChannel = new UploadChannel(uploadItem);

			Thread thread = new Thread(uploadChannel);

			thread.start();

			// add the thread into upload map that uses the unique data set name as a key
			uploadItem.setUploadStatus(UploadStatus.UPLOADING);

			try {
				logger.info("Awaiting thread to complete uploading");
				thread.join();
				uploadItem.setUploadStatus(UploadStatus.COMPLETED);
				thread.interrupt();
			} catch (InterruptedException e1) {
				logger.error(e1);
			}

			try {
				if (isDerived) {
					// TODO: this is a hack to get the biosample email to send out. Please fix me, future-me.
					logger.info("Is biosample, starting new thread to set dataset file to complete.");
					// asyncSetDatasetFileToComplete(uploadItem.getDatasetFile().getId());

					Runnable setToCompleteRunnable = () -> {
						try {
							repositoryProvider.setDatasetFileToComplete(uploadItem.getDatasetFile().getId());
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
					};

					new Thread(setToCompleteRunnable).start();
				} else {
					repositoryProvider.setDatasetFileToComplete(uploadItem.getDatasetFile().getId());
				}
			} catch (MalformedURLException e) {
				logger.error(e);
			}
		}
	}

	public void setSubmissionToolAppConfig(String _bricsUrl, String _ddtUrl, String _user, String _passwd,
			String _portalRoot, String _sftpName, String _sftpBasedir, String _sftpPort, String _sftpUrl,
			String _sftpUser, String _sftpPasswd) {

		try {

			AppConfig config = AppConfig.getInstance();
			String confSecurKey = config.init();

			logger.info("Setting submission tool app config: BRICS_URL = <" + _bricsUrl + ">");
			logger.info("Setting submission tool app config: DDT_URL = <" + _ddtUrl + ">");
			logger.info("Setting submission tool app config: USERNAME = <" + _user + ">");
			logger.info("Setting submission tool app config: PASSWD = <" + _passwd + ">");
			logger.info("Setting submission tool app config: PORTAL_ROOT = <" + _portalRoot + ">");
			logger.info("Setting submission tool app config: SFTP_NAME = <" + _sftpName + ">");
			logger.info("Setting submission tool app config: SFTP_BASEDIR = <" + _sftpBasedir + ">");
			logger.info("Setting submission tool app config: SFTP_PORT = <" + _sftpPort + ">");
			logger.info("Setting submission tool app config: SFTP_URL = <" + _sftpUrl + ">");
			logger.info("Setting submission tool app config: SFTP_USER = <" + _sftpUser + ">");
			logger.info("Setting submission tool app config: SFTP_PASSWORD = <" + _sftpPasswd + ">");

			config.setProperty("BRICS_URL", _bricsUrl, confSecurKey);
			config.setProperty("DDT_URL", _ddtUrl, confSecurKey);
			config.setProperty("USERNAME", _user, confSecurKey);
			config.setProperty("PASSWD", _passwd, confSecurKey);
			config.setProperty("PORTAL_ROOT", _portalRoot, confSecurKey);
			config.setProperty("SFTP_NAME", _sftpName, confSecurKey);
			config.setProperty("SFTP_BASEDIR", _sftpBasedir, confSecurKey);
			config.setProperty("SFTP_PORT", _sftpPort, confSecurKey);
			config.setProperty("SFTP_URL", _sftpUrl, confSecurKey);
			config.setProperty("SFTP_USER", _sftpUser, confSecurKey);
			config.setProperty("SFTP_PASSWORD", _sftpPasswd, confSecurKey);

			config.commit(confSecurKey);

		} catch (DataIsolationException isolExc) {

			throw new WebstartException("Unable to initiate submission tool app config");
		}
	}
}
