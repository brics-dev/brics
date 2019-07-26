package gov.nih.tbi.service;

import gov.nih.tbi.repository.model.hibernate.UserFile;

import java.io.UnsupportedEncodingException;

import org.jasig.cas.client.validation.Assertion;

public interface MetaStudyManager {

	public String validateMetaStudyDataDescription(String description);

	public String validateMetaStudyDataFRequiredFields(String fileName, String description);
	
	/*
	 * this method validates if the file name the user enters is a valid file name. the logic here basically assumes if
	 * you are able to create a file with the name passed in. the file name is valid. if you are not able (an exception
	 * is thrown), than there are illegal characters in the file. file is deleted directly after the test.
	 */
	public boolean validateMetaDataStudyFileName(String fileName);

	/*
	 * this will make a web service call to the meta study module. Check if the file name is unique
	 */
	public boolean isMetaStudyFileNameUnique(String fileName, long metaStudyId) throws UnsupportedEncodingException;

	/*
	 * this will make a web service call to the meta study module. adding a user file as a meta study data object to a
	 * meta study
	 */
	public void sendDownloadPackageToMetaStudy(UserFile instanceDataFile, Long metaStudyId, Assertion casAssertion)
			throws UnsupportedEncodingException;

}
