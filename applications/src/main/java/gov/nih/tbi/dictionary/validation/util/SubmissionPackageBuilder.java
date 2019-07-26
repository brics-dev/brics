package gov.nih.tbi.dictionary.validation.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.dictionary.validation.model.DataSubmission;
import gov.nih.tbi.dictionary.validation.model.FileNode;
import gov.nih.tbi.repository.model.SubmissionDataFile;
import gov.nih.tbi.repository.model.SubmissionPackage;
import gov.nih.tbi.repository.model.SubmissionType;

/**
 * A package for building a submissionPackage from a submission
 * 
 * @author mvalei
 * 
 */
public class SubmissionPackageBuilder {

	private DataSubmission submission;
	private SubmissionPackage submissionPackage;
	private String dataFilePath;
	private String errorMessage;
	private Date date;

	public SubmissionPackageBuilder() {

		setSubmission(null);
		setSubmissionPackage(null);
		setDataFilePath(null);
		date = new Date();
		errorMessage = "";
	}

	public SubmissionPackageBuilder(DataSubmission submission, String dataFilePath, Date date) {

		this.setSubmission(submission);
		this.setDataFilePath(dataFilePath);
		setSubmissionPackage(null);
		this.setDate(date);
		errorMessage = "";
	}

	public String getDataFilePath() {

		return dataFilePath;
	}

	public void setDataFilePath(String dataFilePath) {

		this.dataFilePath = dataFilePath;
	}

	public DataSubmission getSubmission() {

		return submission;
	}

	public void setSubmission(DataSubmission submission) {

		this.submission = submission;
	}

	public SubmissionPackage getSubmissionPackage() {

		return submissionPackage;
	}

	public void setSubmissionPackage(SubmissionPackage submissionPackage) {

		this.submissionPackage = submissionPackage;
	}

	public String getErrorMessage() {

		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {

		this.errorMessage = errorMessage;
	}

	public Date getDate() {

		return date;
	}

	public void setDate(Date date) {

		this.date = date;
	}

	/**
	 * Creates a SubmissionPackage and stores it in submissionPackage. Data for submissionPackage from from submission
	 * On failure, the reason is printed in errorMessageField
	 * 
	 * @return boolean: true on success, false on failure
	 */
	public boolean build() {

		// Make sure that submission is not null
		if (submission == null) {
			errorMessage = "SubmissionPackage Builder could not read from the submission. [submission is null]";
			return false;
		}
		if (dataFilePath == null) {
			errorMessage = "SubmissionPackageBuilder could not read the data file path. [dataFilePath is null]";
			return false;
		}

		// Create the submission package
		SubmissionPackage submissionPackage = new SubmissionPackage();

		// Set the name of the package with a timestamp
		String packageName = "dataFile-" + date.getTime();
		submissionPackage.setName(packageName);

		// Create the dataset list and the associatedFiles list to add to the submission package
		List<SubmissionDataFile> associatedFiles = new ArrayList<SubmissionDataFile>();
		List<SubmissionDataFile> datasets = new ArrayList<SubmissionDataFile>();
		buildFileLists(submission.getRoot(), datasets, associatedFiles);
		submissionPackage.setDatasets(datasets);
		submissionPackage.setAssociatedFiles(associatedFiles);

		// Create set of submission types based on the dataset types
		Set<SubmissionType> submissionTypes = new LinkedHashSet<SubmissionType>();
		for (SubmissionDataFile d : datasets) {
			submissionTypes.add(d.getType());
		}
		submissionPackage.setTypes(submissionTypes);

		// Bytes for the submissionPackage is a sum of all the files in the process
		Long bytes = 0L;

		for (SubmissionDataFile d : datasets) {
			bytes += d.getBytes();
		}
		for (SubmissionDataFile a : associatedFiles) {
			bytes += a.getBytes();
		}
		submissionPackage.setBytes(bytes);

		bytes = new File(dataFilePath).length();
		submissionPackage.setDataFileBytes(bytes);

		// The hash is an md5 of the data file
		FileReader fr;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");

			fr = new FileReader(dataFilePath);
			int bytesRead = 0;
			while (fr.ready() && bytesRead < ModelConstants.MD5_HASH_SIZE) {
				int input = fr.read();
				md5.update((byte) input);
				bytesRead++;
			}

			submissionPackage.setCrcHash("" + new BigInteger(1, md5.digest()).toString(16));
		} catch (FileNotFoundException e) {
			errorMessage = "Could not find datafile at specified path.";
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			errorMessage = "Could not read from datafile.";
			e.printStackTrace();
			return false;
		} catch (NoSuchAlgorithmException e) {
			errorMessage = "Could not digest on the given algorithm.";
			e.printStackTrace();
			return false;
		}

		this.submissionPackage = submissionPackage;
		return true;
	}

	/**
	 * Iterates through the fileNode and its children, appending any valid data files to the list of
	 * SubmissionDataFiles.
	 * 
	 * This function does NOT check for duplicate SubmissionDataFile entries
	 * 
	 * @param node : the current node of the file tree
	 * @param datasets : nodes that are found are appended to this list
	 */
	private void buildFileLists(FileNode node, List<SubmissionDataFile> datasets,
			List<SubmissionDataFile> associatedFiles) {

		// Make sure node is valid
		if (node.isValid()) {
			if (!node.getType().equals(FileNode.FileType.DIR)) {
				SubmissionDataFile dataset = new SubmissionDataFile();
				dataset.setName(node.getName());
				dataset.setPath(node.getConicalPath());
				dataset.setBytes(node.getFileSize());
				dataset.setCrcHash(node.getCrcHash());

				SubmissionType type = submission.getSubmissionTypeByName(node.getStructureName());
				if (type != null) {
					dataset.setType(type);
				}

				if (node.getType().equals(FileNode.FileType.CSV) || node.getType().equals(FileNode.FileType.TAB)
						|| node.getType().equals(FileNode.FileType.XML)) {


					datasets.add(dataset);
				} else if (node.getType().equals(FileNode.FileType.ASSOCIATED)
						|| node.getType().equals(FileNode.FileType.THUMBNAIL)
						|| node.getType().equals(FileNode.FileType.TRANSLATION_RULE)
						|| node.getType().equals(FileNode.FileType.TRIPLANAR)) {
					associatedFiles.add(dataset);

				}
			}
			// If a fileNode is not valid, then none of its children should be valid
			for (FileNode n : node.getChildren()) {
				buildFileLists(n, datasets, associatedFiles);
			}
		}

	}

}
