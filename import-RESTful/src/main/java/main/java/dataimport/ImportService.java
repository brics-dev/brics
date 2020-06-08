package main.java.dataimport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.commons.ws.HashMethods;
import gov.nih.tbi.repository.ws.RepositoryProvider;
import main.java.dataimport.model.SubmissionMetaData;

@Path("/DataSubmission")
public class ImportService {
	private static final Logger logger = Logger.getLogger(ImportService.class);

	public static final long epochSeed = 1370656103000L;
	protected String WORK_DIRECTORY_PATH = "";

	private String DOWNLOADED_PACKAGE_PATH = "downloadPackages" + File.separator;
	protected String DOWNLOADED_DATA_DIRECTORY =
			System.getProperty("user.home") + File.separator + DOWNLOADED_PACKAGE_PATH;

	private String rootDirectoryPath;

	// private Properties modulesProperties = new Properties();
	// private Properties commonsProperties = new Properties();

	// @Autowired
	// protected RepositoryManager repositoryManager;

	@Autowired
	protected ModulesConstants modulesConstants;

	@Autowired
	private ImportDelegate processSubmissionDelegate;

	@Autowired
	private ImportDelegate processSubmission;

	public ImportService() {
		System.out.println("*** Initializing ImportService ***");
	}

	/*
	 * URL end point http://localhost:8080/import-RESTful/rest/DataSubmission/csvInputStream
	 * http://localhost:8080/import-RESTful/rest/DataSubmission/csvInputStream?isBiosample=true&repository="BIOFIND"
	 */
	@POST
	@Path("csvInputStream")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String postDataSubmission(String incomingCSV, @QueryParam("isBiosample") boolean isBiosample,
			@DefaultValue("null") @QueryParam("repository") String repositoryName) {

		logger.info("Creating a submission for the following CSV:\n" + incomingCSV + "\n");
		
		String result = "";

		try {
			loadProperties();

			List<String> metaDataLine = createSubmission(incomingCSV);

			SubmissionMetaData submissionMetaData = new SubmissionMetaData(metaDataLine);

			// returns from the processing array
			result = processSubmissionDelegate.processJob(submissionMetaData, isBiosample, repositoryName);
		} catch (Exception e) {
			logger.error("Unable to process job due to the following errors:", e);
			return "ERROR: unable to process job due to the following errors: " + e.getLocalizedMessage();
		}

		// 6. return success

		return result;
	}
	
	public void loadProperties() throws FileNotFoundException, IOException {

		// modulesProperties.load((new FileInputStream(MODULES_PROPERTIES_LOCATION)));
		// commonsProperties.load(new FileInputStream(COMMON_PROPERTIES_LOCATION));

		// WORK_DIRECTORY_PATH = commonsProperties.getProperty("ImportService.datadrop.location");
		WORK_DIRECTORY_PATH = modulesConstants.getImportServiceDatadropLocation();
	}

	private List<String> createSubmission(String incomingCSV) {
		List<String> results = new ArrayList<String>();

		try {
			// Create the submission ID from the current date.
			Date date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			String submissionID = dateFormat.format(date);

			// Log the new submission ID.
			logger.info("Creating submission...");
			logger.info("Submission ID: " + submissionID);

			// remove the first line from the csv which contains the email,
			// dataSetName, and Study

			// Parse the delimited incoming message with information about
			// the submission

			Scanner submissionPackageData = new Scanner(incomingCSV);
			String firstLine = submissionPackageData.nextLine();

			submissionPackageData.close();

			String[] dataArray = firstLine.split(",");

			for (int i = 0; i < 3; i++) {
				if (dataArray[i].contains("\"")) {
					results.add(dataArray[i].replace("\"", ""));
				} else {
					results.add(dataArray[i]);
				}
			}

			// Create a new directory and assign the package a UID
			rootDirectoryPath = WORK_DIRECTORY_PATH + submissionID;
			File directory = new File(rootDirectoryPath);

			FileUtils.forceMkdir(directory);

			logger.info("Adding directory: " + directory.getAbsolutePath());

			results.add(directory.getAbsolutePath() + File.separator);

			// Create a submission CSV in its specified working directory
			// work DATETIME
			// write the file

			// nix the header information and write the file
			String incomingWriteCSV = incomingCSV.substring(incomingCSV.indexOf("\n") + 1);
			String fileName = directory.getAbsolutePath() + File.separator + submissionID + ".csv";

			resolveAndReplaceUrlsWithFiles(incomingWriteCSV, new File(fileName));
			results.add(fileName);

			// Add the rest of the results
			for (int i = 3; i < dataArray.length; i++) {
				results.add(dataArray[i]);
			}
		} catch (Exception e) {
			logger.error("Error Creating Submission Package Directory:", e);
			// Send email to user email >> results.get(0)
			processSubmission.emailToUser(incomingCSV, false, results.get(0));
		}

		return results;

	}

	private void resolveAndReplaceUrlsWithFiles(String bigString, File csvOutputFile) throws IOException {
		CSVReader reader = null;
		FileWriter w = null;
		// Write the header block without escaping.
		CSVWriter headerWriter = null;
		CSVWriter writer = null;

		try {

			reader = new CSVReader(new StringReader(bigString));
			w = new FileWriter(csvOutputFile);
			// Write the header block without escaping.
			headerWriter = new CSVWriter(w, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
			writer = new CSVWriter(w);

			headerWriter.writeNext(reader.readNext());

			// retain the header information
			String[] headerValues = null;
			headerValues = reader.readNext();

			headerWriter.writeNext(headerValues);
			headerWriter.flush();

			String[] lineValues;

			while ((lineValues = reader.readNext()) != null) {

				if (headerValues != null && lineValues[0] != null
						&& ((lineValues[0].equals("x") || lineValues[0].equals("")))) {
					lineValues = resolveAndReplaceProformsUrlWithFile(lineValues, headerValues);
				}

				writer.writeNext(lineValues);
				writer.flush();
			}
		} finally {
			if (reader != null) {
				reader.close();
			}

			if (writer != null) {
				writer.close();
			}

			if (headerWriter != null) {
				headerWriter.close();
			}
		}
	}

	// only applicable to proforms
	private String[] resolveAndReplaceProformsUrlWithFile(String[] lineValues, String[] columnValues) {
		for (int i = 0; i < lineValues.length; i++) {
			logger.debug("These are the values being written to the line: " + lineValues[i]);
			if (lineValues[i].startsWith("http") && lineValues[i].contains("proforms")) {
				try {
					RestFileRetriever retriever = new RestFileRetriever(lineValues[i], rootDirectoryPath);
					String filename = retriever.getDestinationFileName();

					filename = resolveFileRenamingRules(filename, columnValues[i]);
					retriever.setDestinationFileName(filename);
					retriever.copyFile();

					// store modified filename
					lineValues[i] = filename;
				} catch (IOException e) {
					logger.warn(
							"An entry in the import appeared to be a URL but would not resolve or translate to URL: "
									+ lineValues[i]);
				}
			}
		}

		return lineValues;
	}

	// Rules: given a filename, apply the prefix, a unique ID, and original extension
	// URL must be of the form "https://pdbp-stage.cit.nih.gov/proforms/ws/submission/download/250?fileName=1900B.PNG"
	private String resolveFileRenamingRules(String filename, String filenamePrefix) {
		if (filenamePrefix != null && filenamePrefix != "" && filenamePrefix.contains(".")) {
			filenamePrefix = filenamePrefix.substring(filenamePrefix.lastIndexOf(".") + 1, filenamePrefix.length());
		}

		String extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
		return filenamePrefix + "_" + getPseudoRandomID() + "." + extension;
	}

	// retrieve a unique ~6 digit base 36 alphanumeric
	private String getPseudoRandomID() {
		String smallLong = String.valueOf(System.currentTimeMillis() - epochSeed);
		smallLong = smallLong.substring(0, smallLong.length() - 1);
		String slicedEpoch = Long.toString(Long.valueOf(smallLong), 36);

		try {
			Thread.sleep(11);
		} catch (InterruptedException e) {
			logger.error("Could not create a random ID.", e);
		}

		return slicedEpoch;
	}

//TODO: delete me
//	@POST
//	@Path("postDownloadedData/{formName}")
//	@Produces(MediaType.TEXT_PLAIN)
//	@Deprecated
//	public Response postDownloadedData(@PathParam("formName") String formName) {
//
//		Response response = null;
//
//		List<String> failedFileList = null;
//		List<String> targetFormName = new ArrayList<String>();
//		try {
//			loadProperties();
//
//			File target = new File(DOWNLOADED_DATA_DIRECTORY);
//
//			if (target.isDirectory()) {
//				String[] subFolders = target.list();
//				if (subFolders != null) {
//					for (String folderName : subFolders) {
//						File subTarget = new File(DOWNLOADED_DATA_DIRECTORY + folderName);
//						logger.debug("subTarget folder: " + DOWNLOADED_DATA_DIRECTORY + folderName);
//						if (subTarget.isDirectory()) {
//							String[] targetFiles = subTarget.list();
//							String[] associatedFiles = new String[targetFiles.length];
//
//							for (String fileName : targetFiles) {
//								/* Read the form name from the csv file */
//								String fsInCSV = null;
//								if (fileName.endsWith(".csv")) {
//									String secondLine = (String) FileUtils
//											.readLines(new File(subTarget + File.separator + fileName)).get(1);
//									fsInCSV = secondLine.split(",")[0].replaceAll("\"", "");
//									targetFormName.add(fsInCSV);
//									logger.debug("fsInCSV: " + fsInCSV);
//								}
//
//								/* find target data file and do submission */
//								if (fsInCSV != null
//										&& (fsInCSV.equalsIgnoreCase(formName)
//												|| fsInCSV.indexOf(formName + "_clone_") >= 0)
//										&& !Arrays.asList(associatedFiles).contains(fileName)) {
//
//									associatedFiles = ArrayUtils.removeElement(targetFiles, fileName);
//
//									try {
//										File incomingCSV = new File(
//												DOWNLOADED_DATA_DIRECTORY + folderName + File.separator + fileName);
//										List<String> results =
//												createSubmission(FileUtils.readFileToString(incomingCSV));
//
//										/* Copy associated files from current directory to submission directory */
//										for (String associatedFile : associatedFiles) {
//											File sourceFile = new File(DOWNLOADED_DATA_DIRECTORY + folderName
//													+ File.separator + associatedFile);
//
//											File destDir = new File(results.get(3));
//											try {
//												// Copy additional data in other csv file for the same form within same
//												// directory if any
//												if (sourceFile.getName().endsWith(".csv")
//														&& sourceFile.getName().startsWith(fsInCSV)) {
//													String sourceFileReadCSV = FileUtils.readFileToString(sourceFile);
//													String sourceFileWriteCSV = sourceFileReadCSV
//															.substring(sourceFileReadCSV.indexOf("\n") + 1);
//													resolveAndReplaceUrlsWithFiles(sourceFileWriteCSV,
//															new File(results.get(3) + File.separator + associatedFile));
//												} else if (!sourceFile.getName().endsWith(".csv")) { // Copy other
//																									 // attached
//																									 // non-csv filed
//													FileUtils.copyFileToDirectory(sourceFile, destDir);
//												}
//											} catch (IOException e) {
//												logger.error("Unable to copy the associated file " + associatedFile
//														+ " to submission location due to the following errors:", e);
//											}
//											targetFiles = ArrayUtils.removeElement(targetFiles, associatedFile);
//										}
//
//										/* get the submissionId */
//										String submissionId =
//												rootDirectoryPath.lastIndexOf(File.separator) > 0 ? rootDirectoryPath
//														.substring(rootDirectoryPath.lastIndexOf(File.separator)
//																+ 1) : rootDirectoryPath.substring(
//																		rootDirectoryPath.lastIndexOf("/") + 1);
//
//										List<String> administeredFormIds = new ArrayList<String>();
//										for (int i = 5; i < results.size(); i++) {
//											administeredFormIds.add(results.get(i));
//										}
//
//										String vtUrl = modulesConstants.getModulesVTURL();
//										String adminUsername = modulesConstants.getAdministratorUsername();
//										String adminSaltedPasswd = modulesConstants.getSaltedAdministratorPassword();
//										String serverHash = HashMethods.getServerHash(adminUsername, adminSaltedPasswd);
//
//										/* set userLogin() to be administrator */
//										RepositoryProvider repositoryProvider =
//												new RepositoryProvider(vtUrl, adminUsername, serverHash);
//
//										processSubmissionDelegate.setRepositoryProvider(repositoryProvider);
//
//										String datasetName =
//												results.get(1).replaceAll("((_|-)\\d+$)", "_" + submissionId);
//										logger.info("user email: " + results.get(0) + " dataset name: " + datasetName
//												+ " study name: " + results.get(2) + " submission location: "
//												+ results.get(3) + " submission file location: " + results.get(4));
//										// returns from the processing array
//										processSubmissionDelegate.processJob(results.get(0), datasetName,
//												results.get(2), results.get(3), results.get(4), administeredFormIds,
//												false, null);
//									} catch (Exception e) {
//										logger.error("Unable to submit " + fileName + " due to the following errors:",
//												e);
//										failedFileList = new ArrayList<String>();
//										failedFileList.add(fileName);
//									}
//								}
//							}
//						}
//
//					} // end for
//				}
//			} // end if
//
//			if (failedFileList == null) {
//				response = Response.status(200).entity("Success").build();
//			} else if (!Arrays.asList(targetFormName).contains(formName)) {
//				response = Response.status(204).entity("No data to submit for form " + formName).build();
//			} else {
//				response = Response.status(500).entity("ERROR: failed to submit following files: "
//						+ BRICSStringUtils.concatWithDelimiter(failedFileList, ", ")).build();
//				throw new InternalServerErrorException(response);
//			}
//
//		} catch (Exception e) {
//			logger.error("Unable to process job due to the following errors:", e);
//			response = Response.status(500)
//					.entity("ERROR: Unable to process job due to the following errors: " + e.getMessage()).build();
//			throw new InternalServerErrorException(response);
//		}
//
//		return response;
//	}

}
