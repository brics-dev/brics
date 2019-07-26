package gov.nih.tbi.service.impl;

import gov.nih.tbi.constants.ApplicationConstants;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.service.MetaStudyManager;
import gov.nih.tbi.util.QueryRestProviderUtils;
import gov.nih.tbi.ws.provider.RestMetaStudyProvider;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.jasig.cas.client.validation.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("application")
public class MetaStudyManagerImpl implements MetaStudyManager {

	@Autowired
	ApplicationConstants constants;

	/*
	 * this method validates the meta study description field current requirements are length <= 1000 required field.
	 */
	public String validateMetaStudyDataDescription(String description) {
		if (description.length() > 1000) {
			return "Data file description must be less than 1000 characters.";
		}
		return null;
	}

	public String validateMetaStudyDataFRequiredFields(String fileName, String description) {
		String errors = "";
		if (fileName == null || fileName.trim().equals("")) {
			errors = "Data file name is a required field. ";
		}

		if (description == null || description.trim().equals("")) {
			errors += "Data file description is a required field.";
		}
		return errors;
	}

	/*
	 * this method validates if the file name the user enters is a valid file name. the logic here basically assumes if
	 * you are able to create a file with the name passed in. the file name is valid. if you are not able (an exception
	 * is thrown), than there are illegal characters in the file. file is deleted directly after the test.
	 */
	public boolean validateMetaDataStudyFileName(String fileName) {
		File tmptFIle = new File(fileName);
		try {
			if (tmptFIle.createNewFile()) {
				tmptFIle.delete();
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/*
	 * this will make a web service call to the meta study module. Check if the file name is unique
	 */
	public boolean isMetaStudyFileNameUnique(String fileName, long metaStudyId) throws UnsupportedEncodingException {
		
		RestMetaStudyProvider restMetaStudyProvider = new RestMetaStudyProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));
		
		return restMetaStudyProvider.isMetaStudyDataFileNameUnique(fileName, metaStudyId,
				constants.getMetaStudyServiceURL());
	}

	/*
	 * this will make a web service call to the meta study module. adding a user file as a meta study data object to a
	 * meta study
	 */
	public void sendDownloadPackageToMetaStudy(UserFile instanceDataFile, Long metaStudyId, Assertion casAssertion)
			throws UnsupportedEncodingException {
		RestMetaStudyProvider restMetaStudyProvider = null;
		if(ApplicationConstants.isWebservicesSecured()){
			restMetaStudyProvider = new RestMetaStudyProvider(constants.getModulesAccountURL(),
							QueryRestProviderUtils.getOnlyProxyTicketByAssertion(constants.getModulesAccountURL(), casAssertion));
		} else {
				restMetaStudyProvider = new RestMetaStudyProvider(constants.getModulesAccountURL(),
								QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));
			}
		
		restMetaStudyProvider.sendDownloadPackageToMetaStudy(instanceDataFile, metaStudyId, constants.getMetaStudyServiceURL());
	}

}
