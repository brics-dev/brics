package dataimport;

import gov.nih.tbi.ApplicationsConstants;
import gov.nih.tbi.commons.model.DatasetFileStatus;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.dictionary.validation.ValidationController;
import gov.nih.tbi.dictionary.validation.model.DataStructureTable;
import gov.nih.tbi.dictionary.validation.model.DataSubmission;
import gov.nih.tbi.dictionary.validation.model.FileNode;
import gov.nih.tbi.dictionary.validation.model.ValidationOutput;
import gov.nih.tbi.repository.UploadItem;
import gov.nih.tbi.repository.UploadManagerController;
import gov.nih.tbi.repository.UploadStatus;
import gov.nih.tbi.repository.model.SubmissionTicket;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.ws.RepositoryProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.TreeSet;

// email sender
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

public class ImportDelegate {

	static Logger logger = Logger.getLogger(ImportDelegate.class);

	// These WILL be moved to external .properties files

	String PROPERTIES_LOCATION = "/opt/apache-tomcat/brics/modules.properties";
	String WORK_DIRECTORY_PATH = "/home/tbi_data_drop/work/"; // TODO: Adjust for disease specific PD or FITBIR should
																// this be located in a more central location ?

	// String PROPERTIES_LOCATION = "c:\\brics\\modules.properties";

	String EMAIL_SENDER = "BRICS_Data_Processor@nih.gov";

	RepositoryProvider repositoryProvider = null;

	private int uploadToken = ApplicationsConstants.MAXIMUM_CONCURRENT_UPLOAD;

	// Controller and connection
	private String rootDirectoryPath;
	private String bricsUrl;
	private String ddtUrl;
	private String userName;
	private String password;

	private ValidationController controller;

	// Mail sender
	private String host;

	// stores the list of upload items loaded
	private static List<UploadItem> uploadList = new ArrayList<UploadItem>();
	// list of upload items still in queue
	private static Queue<UploadItem> uploadQueue = new LinkedList<UploadItem>();
	private static int tableIndex = 0;

	public void processJob(String userEmail, String dataSetName, String studyName, String submissionLocation)
			throws Exception {

		WORK_DIRECTORY_PATH = submissionLocation;

		loadProperties();

		// lets also move all status messages to constants ??
		logger.info("Validating Package");

		// 1. Validate package by pointing to the working directory
		List<String> messages = validate(); // return type should be the results

		// there are only text based returns in the controller
		if (messages.toString().contains(
				"All files are valid., A new submission file and zip have been created in the working directory.")) {
			// 2. Submit the Package to the BRICS repository
			messages.addAll(submit(dataSetName, studyName));
			messages.add(submissionLocation);
			// Email success to user
			// emailToUser(messages, true, userEmail);
			sendMessageToHTTP(messages, true, userEmail);
		} else {
			// Email failure to user along with the results
			// emailToUser(messages, false, userEmail);
			messages.add(dataSetName);
			messages.add(studyName);
			messages.add(submissionLocation);
			sendMessageToHTTP(messages, false, userEmail);
		}

	}

	public void loadProperties() {

		Properties modulesProperties = new Properties();
		try {
			modulesProperties.load((new FileInputStream(PROPERTIES_LOCATION)));
		} catch (Exception e) {
			logger.fatal("Unable to load modules properties file. " + e);
		}

		bricsUrl = modulesProperties.getProperty("modules.vt.url.server");
		logger.info(bricsUrl);

		ddtUrl = modulesProperties.getProperty("modules.ddt.url.server");
		logger.info(ddtUrl);

		userName = "anonymous";
		password = "";

		rootDirectoryPath = WORK_DIRECTORY_PATH; // this will eventually be part of the commons.prop

		// move to mail.properties?
		host = "mailfwd.nih.gov";

	}

	public List<String> validate() {

		try {
			controller = new ValidationController(null, bricsUrl, ddtUrl, userName, password);
		} catch (Exception e) {
			logger.fatal("Unable to connect to FITBIR. ERROR: \n", e);
		}

		// create a list of messages that will later serve as the response header and content
		List<String> messages = new ArrayList<String>();

		messages.add(controller.load(rootDirectoryPath)); // may want to replace the null with something else

		messages.add(controller.validate());

		messages.addAll(getErrors(controller.getSubmission(), controller.getSubmission().getRoot(), true));

		messages.add(controller.buildSubmission(rootDirectoryPath));

		System.out.println(messages.toString());
		// return will be the results

		return messages;
	}

	public List<String> submit(String dataSetName, String studyName) {

		// submits or uploads the successfully created package to brics
		logger.info("submitting valid job to upload manager");

		// unmarshal the submission package into SubmissionPackage Object
		SubmissionTicket submissionTicket =
				UploadManagerController.unmarshalSubmissionTicket(rootDirectoryPath + getSubmissionFiles().get(0));

		Dataset dataset = processSubmissionTicket(submissionTicket, dataSetName, studyName);

		loadUploadQueue(dataset);

		List<String> submissionInformation = new ArrayList<String>();

		submissionInformation.add(dataSetName);
		submissionInformation.add(studyName);
		submissionInformation.add(submissionTicket.getEnvironment());
		submissionInformation.add(submissionTicket.getVersion());
		submissionInformation.add(submissionTicket.getSubmissionPackage().getName());

		System.out.println(dataset.toString());
		return submissionInformation;
	}

	// process the submission tickets locally

	public Dataset processSubmissionTicket(SubmissionTicket submissionTicket, String dataSetName, String studyName) {

		String serverPath =
				UploadManagerController.getConnectionConfig().getProperty("TBI_SFTP_BASEDIR") + studyName
						+ ServiceConstants.FILE_SEPARATER + dataSetName + ServiceConstants.FILE_SEPARATER;

		logger.info(serverPath);
		try {
			repositoryProvider = new RepositoryProvider(bricsUrl, userName, password);
		} catch (Exception e) {
			logger.error("Error: ", e);
		}

		return repositoryProvider.processSubmissionTicket(submissionTicket, rootDirectoryPath, serverPath, studyName,
				dataSetName, false);

	}

	private List<String> getErrors(DataSubmission submission, FileNode node, boolean warnings) {

		// this list can be used for both an email and a potential data transfer to ProFoRMS

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

	boolean sendMessageToHTTP(List<String> messages, boolean success, String emailAddress) {

		// This MUST be a constant
		String url = "http://proforms-mirth-dev.cit.nih.gov:80/";
		String charset = "UTF-8";

		// build text of email

		StringBuilder validationResultsText = new StringBuilder();

		// Now set the actual message lets take these out as constants later

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
		try {
			String query =
					String.format("email=%s&validation=%s", URLEncoder.encode(emailAddress, charset),
							URLEncoder.encode(validationResults, charset));

			URLConnection connection = new URL(url).openConnection();
			connection.setDoOutput(true); // Triggers POST.
			connection.setRequestProperty("Accept-Charset", charset);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
			OutputStream output = null;

			try {
				output = connection.getOutputStream();
				output.write(query.getBytes(charset));
			} finally {
				if (output != null)
					try {
						output.close();
					} catch (IOException logOrIgnore) {
					}
			}

			InputStream response = connection.getInputStream();

			return true;

		} catch (Exception e) {
			logger.fatal("Fatal Error:" + e);
			return false;
		}

	}

	boolean emailToUser(List<String> messages, boolean success, String emailAddress) {

		// sends out an email with the responses for now lets print a response

		logger.info("The process was successful: " + success);
		logger.info("Messages: " + messages);
		logger.info("Sent to: " + emailAddress);

		// Recipient's email ID needs to be mentioned.
		String to = emailAddress;

		// Sender's email ID needs to be mentioned
		String from = EMAIL_SENDER;

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
			message.setFrom(new InternetAddress(from));

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
			logger.error("Message failed with exception: " + mex);
			return false;
		}

		return true;

	}

	private List<String> getSubmissionFiles() {

		// This is a list return in case of future iterations where we will need to process multiple tickets etc.
		List<String> submissionFileList = new ArrayList<String>();

		File dir = new File(rootDirectoryPath);

		File[] files = dir.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {

				return name.contains("submissionTicket");
			}
		});

		for (File submissionFile : files) {
			submissionFileList.add(submissionFile.getName());
		}

		return submissionFileList;

	}

	// Upload queue methods - we may want to abstract these later as I wasn't expecting the controller to
	// be incompatible.

	/**
	 * Loads the upload queue
	 * 
	 * @param dataset
	 */
	private void loadUploadQueue(Dataset dataset) {

		try {
			addToQueue(dataset);
		} catch (Exception e) {
			// if fail then return and fail on submission
			logger.fatal(e);
			return;
		}
		logger.info(uploadToken);

		while (uploadToken > 0) {
			logger.info(uploadToken);
			nextUpload();
		}
	}

	/**
	 * Add the DatasetFiles from the Dataset into the upload queue
	 * 
	 * @param dataset - Dataset to be added into the upload queue.
	 * @throws Exception
	 */
	public void addToQueue(Dataset dataset) throws Exception {

		for (DatasetFile datasetFile : repositoryProvider.getDatasetFiles(dataset.getId())) {
			if (DatasetFileStatus.PENDING.equals(datasetFile.getDatasetFileStatus())) {
				UploadItem item =
						new UploadItem(tableIndex, dataset, datasetFile, dataset.getStudy().getTitle(),
								UploadStatus.QUEUED, dataset.getStudy().getPrefixedId());
				uploadQueue.add(item);
				logger.info("" + uploadQueue.toString());
				uploadList.add(item);
				// increment table index for the next item
				tableIndex++;
			}
		}
	}

	/**
	 * Polls the next upload from the upload queue and initiates a new thread to upload it
	 */

	public void nextUpload() {

		UploadItem nextFile = uploadQueue.poll();
		uploadFile(nextFile);
		uploadToken--;
	}

	public void uploadFile(UploadItem uploadItem) {

		if (uploadItem != null) {

			// start new thread to upload the file
			UploadChannel uploadChannel = new UploadChannel(uploadItem, UploadManagerController.getConnectionConfig());

			Thread thread = new Thread(uploadChannel);

			thread.start();

			// add the thread into upload map that uses the unique dataset name as a key

			uploadItem.setUploadStatus(UploadStatus.UPLOADING);

			try {
				logger.info("Awaiting thread to complete uploading");
				thread.join();
				uploadItem.setUploadStatus(UploadStatus.COMPLETED);
				thread.interrupt();
			} catch (InterruptedException e1) {
				logger.fatal(e1);
			}

			try {
				repositoryProvider.setDatasetFileToComplete(uploadItem.getDatasetFile().getId());
			} catch (MalformedURLException e) {
				logger.error(e);
			}

		}
	}

}
