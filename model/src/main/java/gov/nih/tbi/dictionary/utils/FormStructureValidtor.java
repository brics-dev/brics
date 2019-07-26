package gov.nih.tbi.dictionary.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import gov.nih.tbi.commons.model.RepeatableType;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseStructure;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;

public class FormStructureValidtor {
	
	private FormStructure dataStructureToValidate;
	
	public FormStructureValidtor(FormStructure dataStructureToTest){
		this.setDataStructureToValidate(dataStructureToTest);
	}
	/*
	 * This method will validate the data structure that is uploaded through the admin XML upload and catch any null
	 * pointers before the DS gets submitted It will return a list of any errors.
	 */
	public List<String> validateDataStructure() {

		List<String> dataStructureErrors = new ArrayList<String>();

		// Need to test each repeatable group is 0-x and 0 has a name of main
		Pattern specialChar = Pattern.compile("[^a-z0-9_ ]", Pattern.CASE_INSENSITIVE);
		Pattern singleChar = Pattern.compile("[^a-z ]", Pattern.CASE_INSENSITIVE);

		// validate length of title
		if (dataStructureToValidate.getTitle().length() > 100) {
			dataStructureErrors.add("Title cannot be greater than 100 characters.");
		}

		// validate there is a title
		if (dataStructureToValidate.getTitle().trim().isEmpty()) {
			dataStructureErrors.add("Title is a required field.");
		}
		// This check to make sure the form type is not null
		if (dataStructureToValidate.getFileType() == null) {
			dataStructureErrors.add("The file type in the form structure is blank or not recognized by the system. File type is required and "
							+ "case sensitive. Please review the file type and check for spelling errors or lower case letters.");
		}

		// validate there is a short name
		if (dataStructureToValidate.getShortName().trim().isEmpty()) {
			dataStructureErrors.add("Short Name is a required field.");
		} else {

			// validate short name length
			if (dataStructureToValidate.getShortName().length() > 26) {
				dataStructureErrors.add("Short name cannot be greater than 26 characters.");
			}

			// validate short name chars
			if (specialChar.matcher(dataStructureToValidate.getShortName()).find()) {
				dataStructureErrors.add("Short name uses invalid special characters.");
			}

			// validate short name chars
			if (singleChar.matcher(dataStructureToValidate.getShortName().subSequence(0, 1)).find()) {
				dataStructureErrors.add("Short name must start with a letter.");
			}

			// validate short name has no spaces
			if (dataStructureToValidate.getShortName().indexOf(' ') != -1) {
				dataStructureErrors.add("Short name cannot contain spaces.");
			}
		}

		if (dataStructureToValidate.getDescription().trim().isEmpty()) {
			dataStructureErrors.add("Description is a required field.");
		}

		// validate length of Description
		if (dataStructureToValidate.getDescription().length() > 1000) {
			dataStructureErrors.add("Description cannot be greater than 1000 characters.");
		}

		// validate length of Organization
		if (dataStructureToValidate.getOrganization().length() > 55) {
			dataStructureErrors.add("Organization cannot be greater than 55 characters.");
		}

		// validate length of Organization
		if (dataStructureToValidate.getStandardization() == null
				|| dataStructureToValidate.getStandardization().getName() == null) {
			dataStructureErrors.add("Standardization type was not found in the system.");
		}

		// this method will check to makse sure there are no duplicate
		// repeatable groups
		if (duplicateRGNames(dataStructureToValidate)) {
			dataStructureErrors.add("There are duplicate repeatable group names in the form structure.");
		}

		// This boolean will keep track to make sure there is a repeatable group
		// called main
		boolean hasMainGroup = false;
		for (RepeatableGroup testRepeatableGroup : dataStructureToValidate.getRepeatableGroups()) {
			// set boolean to true if there is a repeatable group called main
			if (testRepeatableGroup.getName().equals("Main")) {
				hasMainGroup = true;

				// This method tests to make sure the elements of the RG main
				// are set to the proper type and threshold.
				List<String> mainElementErrors = testMainRGElements(testRepeatableGroup);
				if (!mainElementErrors.isEmpty()) {
					for (String mainErrors : mainElementErrors) {
						dataStructureErrors.add(mainErrors);
					}
				}
			}

			if (testRepeatableGroup.getType() == null) {
				dataStructureErrors.add(
						"A valid Type was not found in the repeatable group " + testRepeatableGroup.getName() + ".");
			}
			if (testRepeatableGroup.getName().length() > 55) {
				dataStructureErrors.add("Repeatable group " + testRepeatableGroup.getName()
						+ " has exceeded the maximum number of 55 characters.");
			}

			List<String> invalidDE = invalidDataElements(testRepeatableGroup);
			if (!invalidDE.isEmpty()) {
				for (String deName : invalidDE) {
					dataStructureErrors.add("There is an invalid data element '" + deName
							+ " 'in the repeatable group: " + testRepeatableGroup.getName() + ".");
				}
			}

			List<String> duplicateDE = duplicateDataElements(testRepeatableGroup);
			// if there are errors from the method loop through and add all DE
			// names to the error messages
			if (!duplicateDE.isEmpty()) {
				for (String deName : duplicateDE) {
					dataStructureErrors.add("There is a duplicate data element '" + deName
							+ " 'in the repeatable group: " + testRepeatableGroup.getName() + ".");
				}
			}

			// This will see if there are any required types that are null and
			// display and error message
			// the method return the number of required type errors to customize
			// the error message
			int requiredTypeErrors = nullRequredTypes(testRepeatableGroup);

			if (requiredTypeErrors == 1) {
				dataStructureErrors.add("There is a required type in the repeatable group '"
						+ testRepeatableGroup.getName() + "' that is not recognized by the system. Required type is "
						+ "case sensitive. Please review the map elements and capitalize all required types.");
			} else if (requiredTypeErrors > 1) {
				dataStructureErrors.add("There are multiple required types in the repeatable group '"
						+ testRepeatableGroup.getName() + "' that are not recognized by the system. Required type is "
						+ "case sensitive. Please review the map elements and capitalize all required types.");
			}

		}
		// test to make sure there is a repeatable group called main
		if (!hasMainGroup) {
			dataStructureErrors.add("The form structure does not have a repeatable group named Main.");
		}

		// This loops through the diseases to make sure there are no null
		// diseases
		for (DiseaseStructure testDiseaseList : dataStructureToValidate.getDiseaseList()) {
			// Obtain the disease from the xml file and validate it against the
			// static manager
			Disease disease = testDiseaseList.getDisease();
			if (disease.getId() == null) {
				dataStructureErrors.add("The disease '" + disease.getName()
						+ "' could not be found. Please check the disease name in the form structure for spelling errors.");
			}
		}

		// This method tests to make sure there are no duplicate diseases
		if (duplicateDiseaseNames(dataStructureToValidate)) {
			dataStructureErrors.add("There are duplicate disease names in the form structure.");
		}

		return dataStructureErrors;
	}
	
	/**
	 * This method will test to make sure that a repeatable group called main will have a threshold of 1 and type of
	 * exactly
	 * 
	 * @param testRG
	 * @return
	 */
	private final List<String> testMainRGElements(RepeatableGroup testRG) {

		List<String> mainErrors = new ArrayList<String>();

		if (testRG.getPosition() != 0) {
			mainErrors.add("Repeatable group " + testRG.getName() + " must have a position set to 0.");
		}

		if (testRG.getThreshold() != 1) {
			mainErrors.add("Repeatable group " + testRG.getName() + " must have a threshold set to 1.");
		}

		if (testRG.getType() != null) {
			if (!testRG.getType().equals(RepeatableType.EXACTLY)) {
				mainErrors.add("Repeatable group " + testRG.getName() + " must have a type set to EXACTLY.");
			}
		}

		return mainErrors;
	}
	
	/**
	 * This method will take a RG and look for all duplicate DEs The method will return a list of the duplicate DEs
	 * 
	 * @param testGroup
	 * @return
	 */
	private final List<String> invalidDataElements(RepeatableGroup testGroup) {

		List<String> nameOfElements = new ArrayList<String>();

		for (MapElement testMapElement : testGroup.getMapElements()) {

			StructuralDataElement currentDataElement = testMapElement.getStructuralDataElement();

			// if the data element name is null there is no point in checking
			// for a duplicate name
			// there is a check to handle null DE names.

			// The only way the currentDataElement could be null is if the Data
			// Element Specified in the Repeatable
			// Group was
			if (currentDataElement.getId() == null) {
				nameOfElements.add(currentDataElement.getName());
			}

		}

		return nameOfElements;
	}
	
	/**
	 * This method counts the number of null required types in a RG
	 */
	private final int nullRequredTypes(RepeatableGroup testGroup) {

		int errors = 0;

		for (MapElement testElement : testGroup.getMapElements()) {
			if (testElement.getRequiredType() == null) {
				errors++;
			}
		}

		return errors;
	}
	
	private final boolean duplicateDiseaseNames(FormStructure rGToTest) {

		// This list
		List<String> diseaseNames = new ArrayList<String>();
		boolean hasDuplicateNames = false;

		// Add all names to a list
		if (rGToTest.getDiseaseList().size() > 1) {
			for (DiseaseStructure testDiseaseList : rGToTest.getDiseaseList()) {
				if (testDiseaseList.getDisease().getName().trim() != null) {
					diseaseNames.add(testDiseaseList.getDisease().getName().trim());
				}
			}
			// sort list to find duplicates
			Collections.sort(diseaseNames);

			// compare the first list to the next one for duplicates
			for (int i = 0; i < diseaseNames.size(); i++) {
				String outterName = diseaseNames.get(i);
				int t = i + 1;
				if (t < diseaseNames.size()) {
					if (outterName.equals(diseaseNames.get(t))) {
						hasDuplicateNames = true;
					}
				}

			}
		}
		return hasDuplicateNames;
	}
	
	/**
	 * This method will take a RG and look for all duplicate DEs The method will return a list of the duplicate DEs
	 * 
	 * @param testGroup
	 * @return
	 */
	private final List<String> duplicateDataElements(RepeatableGroup testGroup) {

		List<String> nameOfElements = new ArrayList<String>();

		for (MapElement testMapElement : testGroup.getMapElements()) {

			StructuralDataElement currentDataElement = testMapElement.getStructuralDataElement();

			// if the data element name is null there is no point in checking
			// for a duplicate name
			// there is a check to handle null DE names.

			for (MapElement dupMapElement : testGroup.getMapElements()) {
				StructuralDataElement dupDataElement = dupMapElement.getStructuralDataElement();

				// if the data element has the same name and position than
				// it is the same DE not a duplicate
				if (currentDataElement.getName().equalsIgnoreCase(dupDataElement.getName())
						&& !testMapElement.getPosition().equals(dupMapElement.getPosition())) {
					boolean inList = false;
					// if the data element name is in the list the DE has
					// already been accounted for
					for (String currentList : nameOfElements) {
						if (currentDataElement.getName().equalsIgnoreCase(currentList)) {
							inList = true;
						}
						if (inList) {
							break;
						}
					}
					if (!inList) {
						nameOfElements.add(currentDataElement.getName());
					}
				}
			}

		}

		return nameOfElements;
	}
	
	/**
	 * This method looks for duplicate RG names in a data structure and returns true if duplicate names are found.
	 * 
	 * @param rGToTest
	 * @return
	 */
	private final boolean duplicateRGNames(FormStructure rGToTest) {

		List<String> groupNames = new ArrayList<String>();
		boolean hasDuplicateNames = false;

		if (rGToTest.getRepeatableGroups().size() > 1) {
			// Add all names to a list
			for (RepeatableGroup testRepeatableGroup : rGToTest.getRepeatableGroups()) {
				if (testRepeatableGroup.getName().trim() != null) {
					groupNames.add(testRepeatableGroup.getName());
				}
			}
			// sort list to find duplicates
			Collections.sort(groupNames);

			// compare the string to the next one in the list
			for (int i = 0; i < groupNames.size(); i++) {
				String outterName = groupNames.get(i);
				int t = i + 1;
				if (t < groupNames.size()) {
					if (outterName.equals(groupNames.get(t))) {
						hasDuplicateNames = true;
					}
				}

			}
		}
		return hasDuplicateNames;
	}
	
	private void setDataStructureToValidate(FormStructure dataStructureToValidate){
		this.dataStructureToValidate = dataStructureToValidate;
	}
	
	public List<String> validateCatQuestionOid(ArrayList<String> catQuestions) {
		List<String> errorMsg = new ArrayList<String>();
		for (RepeatableGroup testRepeatableGroup : dataStructureToValidate.getRepeatableGroups()) {
			if (!testRepeatableGroup.getName().equals("Main") && !testRepeatableGroup.getName().equals("Final Results")) {
				for (MapElement testMapElement : testRepeatableGroup.getMapElements()) {
					StructuralDataElement currentDataElement = testMapElement.getStructuralDataElement();
					String qItemOID = currentDataElement.getFormItemId();
					String deName = currentDataElement.getName();
					if(!(deName.equals("catTSCORE") || deName.equals("catStandardError") || deName.equals("catQuestionPosition"))) {
						if(catQuestions.indexOf(qItemOID) == -1) {
							errorMsg.add("The Question Form Item OID: "+ currentDataElement.getFormItemId() + "["+
									currentDataElement.getName()+"] is not belong to current Data Structure");
						}
					}
				}
			}
		}
		return errorMsg;
	}
}