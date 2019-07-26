package gov.nih.tbi.dictionary.ws.cxf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HEAD;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import gov.nih.tbi.account.ws.AbstractRestService;
import gov.nih.tbi.commons.service.EformManager;
import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;
import gov.nih.tbi.dictionary.model.hibernate.eform.Question;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAttribute;
import gov.nih.tbi.dictionary.model.hibernate.eform.Section;
import gov.nih.tbi.dictionary.model.hibernate.eform.SectionQuestion;

/**
 * This class is only intended to be used for the submission issues related to migrated eForms from the Batman release.
 * Please do not use or rely on any methods or web service calls in this class. This class will be removed once that
 * cleanup effort is complete.
 * 
 * TODO Remove this class and any references to it once the submission issues related to erroneous migrated eForms are
 * completed.
 * 
 * @author jeng
 */
@Path("/dictionary/tools/eforms/validation")
public class EformValidationService extends AbstractRestService {
	private static final Logger logger = Logger.getLogger(EformValidationService.class);

	@Autowired
	private EformManager eformManager;

	private class StreamingCSVFileOutput implements StreamingOutput {
		private File csvFile;

		public StreamingCSVFileOutput(File inFile) throws WebApplicationException {
			if ((inFile != null) && inFile.exists()) {
				csvFile = inFile;
			} else {
				throw new InternalServerErrorException("The given CSV file does not exist.");
			}
		}

		@Override
		public void write(OutputStream out) throws IOException, WebApplicationException {
			FileInputStream in = new FileInputStream(csvFile);
			byte[] buffer = new byte[1024];
			int bytesRead = 0;

			try {
				// Stream the file out to the user's browser.
				bytesRead = in.read(buffer);

				while (bytesRead > 0) {
					out.write(buffer, 0, bytesRead);
					out.flush();
					bytesRead = in.read(buffer);
				}
			} finally {
				in.close();
				out.close();
			}

			// Deleted the temp file.
			csvFile.delete();
		}

	}

	@HEAD
	public Response handleHeadRequest() {
		return Response.noContent().header("Pragma", "no-cache")
				.header("Cache-Control", "no-store, no-cache, must-revalidate")
				.header("X-Content-Type-Options", "nosniff").header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Credentials", "false")
				.header("Access-Control-Allow-Methods", "HEAD, GET, POST")
				.header("Access-Control-Allow-Headers", "Content-Type, Content-Range, Content-Disposition")
				.header("Vary", "Accept").build();
	}

	@POST
	@Path("start")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response doValidation(
			@Multipart("csvFile") Attachment csvAttach,
			@Multipart(value = "noDups", required = false) Boolean filterDups)
			throws WebApplicationException {
		// Check if the filter duplicates flag is null.
		if (filterDups == null) {
			filterDups = Boolean.FALSE;
		}

		JsonObject responseJson = new JsonObject();
		InputStream csvIn = csvAttach.getObject(InputStream.class);
		CSVReader csvReader = new CSVReader(new InputStreamReader(csvIn));

		logger.info("Reading uploaded CSV file. " + csvAttach.getContentDisposition().toString());

		try {
			String[] header = csvReader.readNext();

			if (header != null) {
				int numEForms = 0;
				JsonArray issues = new JsonArray();
				String currEFormName = "";
				String currSectionName = "";
				Eform currEform = null;
				Section currSection = null;
				Integer currSectionFormRow = null;
				String[] line = csvReader.readNext();

				// Loop throw the rest of the CSV file.
				while (line != null) {
					// Check if this line is for a different eForm
					if (!line[0].equals(currEFormName)) {
						numEForms++;
						currEFormName = line[0];
						currSectionName = "";
						logger.debug("Fetching the " + currEFormName + " eForm form the database.");
						currEform = eformManager.getEformNoLazyLoad(currEFormName);

						// Check if an eForm was retrieved.
						if (currEform == null) {
							JsonObject issueObj = new JsonObject();

							issueObj.addProperty("eFormName", currEFormName);
							issueObj.addProperty("sectionName", "N/A");
							issueObj.addProperty("questionName", "N/A");
							issueObj.addProperty("issueDesc", "No eForm found for short name " + currEFormName + ".");
							addIssueToArray(issueObj, issues, filterDups);

							// Skip to the next line.
							currEFormName = "";
							currSectionName = "";
							line = csvReader.readNext();
							continue;
						}
					}

					// Check if this line refers to a different section name.
					if (!line[1].equals(currSectionName)) {
						currSectionName = line[1];
						currSectionFormRow = Integer.valueOf(line[6]);
						currSection = getSectionFromEform(currEform, currSectionName, currSectionFormRow);

						// Check if the section was found.
						if (currSection == null) {
							JsonObject issueObj = new JsonObject();

							issueObj.addProperty("eFormName", currEFormName);
							issueObj.addProperty("sectionName", currSectionName);
							issueObj.addProperty("questionName", "N/A");
							issueObj.addProperty("issueDesc", "No section found for section name " + currSectionName
									+ " and form row " + currSectionFormRow.toString() + ".");
							addIssueToArray(issueObj, issues, filterDups);

							// Skip to the next line.
							currSectionName = "";
							line = csvReader.readNext();
							continue;
						}
						// Validate the section data.
						else {
							logger.debug("Validating section " + currSectionName + "...");
							validateSectionInfo(line, currEform, currSection, issues, filterDups);
						}
					}

					// Validate the question data.
					validateQuestionInfo(line, currEform, currSection, issues, filterDups);

					// Read in the next line.
					line = csvReader.readNext();
				}

				logger.info("Completed all validation taskes. Generating final response JSON.");

				// Add final result data to the response JSON.
				String msg =
						"Proccessing completed. Found " + issues.size() + " issues out of " + numEForms + " eForms.";
				responseJson.addProperty("message", msg);
				responseJson.add("issueArray", issues);
			} else {
				responseJson.addProperty("message", "The uploaded CSV file is empty.");
				responseJson.add("issueArray", new JsonArray());
			}
		} catch (IOException ie) {
			String msg = "Couldn't read the uploaded CSV file.";
			logger.error(msg, ie);
			throw new InternalServerErrorException(msg, ie);
		} catch (NumberFormatException nfe) {
			String msg = "Couldn't convert one of the cells into a number.";
			logger.error(msg, nfe);
			throw new BadRequestException(msg, nfe);
		} finally {
			try {
				csvReader.close();
			} catch (IOException ie) {
				logger.warn("Couldn't close the CSV reader: " + ie.getMessage());
			}
		}

		return Response.ok(responseJson.toString()).build();
	}

	@POST
	@Path("genCsvFile")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response generateCsvResultFile(@DefaultValue("[]") @FormParam("issueArray") String jsonArrayStr) {
		File csvFile = null;

		try {
			JsonParser parser = new JsonParser();
			JsonArray issueArray = parser.parse(jsonArrayStr).getAsJsonArray();

			// Create a temp file and set it to delete itself when the JVM shuts down.
			csvFile = File.createTempFile("eFormValResults-" + (new Date()).getTime(), ".csv");
			csvFile.deleteOnExit();

			// Generate a CSV file from the given JSON Array.
			CSVWriter writer = new CSVWriter(new FileWriter(csvFile));

			try {
				// Write the header in the file.
				writer.writeNext(
						new String[] {"eForm Short Name", "Section Name", "Question Name", "Issue Description"});

				// Write the JSON array data to the file.
				for (JsonElement elm : issueArray) {
					JsonObject issue = elm.getAsJsonObject();
					String eFormName = issue.get("eFormName").getAsString();
					String sectionName = issue.get("sectionName").getAsString();
					String questionName = issue.get("questionName").getAsString();
					String descr = issue.get("issueDesc").getAsString();

					writer.writeNext(new String[] {eFormName, sectionName, questionName, descr});
					writer.flush();
				}
			} finally {
				writer.close();
			}
		} catch (JsonSyntaxException | IllegalStateException e) {
			String msg = "Couldn't parse the JSON object.";
			logger.error(msg, e);
			throw new BadRequestException(msg);
		} catch (IOException e) {
			String msg = "Error occured while generating the CSV file.";
			logger.error(msg, e);
			throw new InternalServerErrorException(msg);
		}

		return Response.ok(new StreamingCSVFileOutput(csvFile))
				.header("Content-Disposition", "attachment; filename=\"" + csvFile.getName() + "\"")
				.header("Content-Length", Long.toString(csvFile.length())).build();
	}

	/**
	 * Gets a section from the given list by the specified section name and form row number.
	 * 
	 * @param form - The eForm containing the set of sections to search over.
	 * @param sectionName - The name of the section to search for.
	 * @param formRow - The form row number of the section to search for.
	 * @return A section form the list that matches the given section name and form row number.
	 */
	private Section getSectionFromEform(Eform form, String sectionName, Integer formRow) {
		Section found = null;

		for (Section s : form.getSectionList()) {
			if (s.getName().equals(sectionName) && s.getFormRow().equals(formRow)) {
				found = s;
				break;
			}
		}

		return found;
	}

	/**
	 * Run validation checks on the current section. All issues will be recorded in the given JSON array.
	 * 
	 * @param csvLine - The current line of the CSV file containing ProFoRMS form data.
	 * @param eForm - The current eForm dictionary object that is being verified.
	 * @param section - The current dictionary section object, which will be the subject to the validation tests.
	 * @param issueArray - Used to record any validation issues that may occur.
	 * @param filterDups - Flag used to indicate whether or not to filter out duplicate issues from the issueArray.
	 */
	private void validateSectionInfo(String[] csvLine, Eform eForm, Section section, JsonArray issueArray,
			Boolean filterDups) {
		String currEFormName = eForm.getShortName();
		String currSectionName = section.getName();

		// Check repeatable flag.
		if (!Boolean.valueOf(csvLine[2]).equals(section.getIsRepeatable())) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", currEFormName);
			issueObj.addProperty("sectionName", currSectionName);
			issueObj.addProperty("questionName", "N/A");
			issueObj.addProperty("issueDesc", "Section repeatable flag doesn't match. " + "ProFoRMS value => " + csvLine[2]
					+ ", eForm value => " + section.getIsRepeatable());
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the repeated parent section ID.
		if ((section.getRepeatedSectionParent() == null) && (Long.valueOf(csvLine[3]) > 0)) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", currEFormName);
			issueObj.addProperty("sectionName", currSectionName);
			issueObj.addProperty("questionName", "N/A");
			issueObj.addProperty("issueDesc", "Section's repeated parient ID is invalid. " + "ProFoRMS value => "
					+ csvLine[3] + ", eForm value => " + section.getRepeatedSectionParent());
			addIssueToArray(issueObj, issueArray, filterDups);
		} else if (section.getRepeatedSectionParent() != null) {
			// Verify if the repeated section parent refers to a valid section.
			checkRepeatedSectionParentExists(eForm, section, issueArray, filterDups);
		}

		// Check initial repeated sections number.
		if (!Integer.valueOf(csvLine[4]).equals(section.getInitialRepeatedSections())) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", currEFormName);
			issueObj.addProperty("sectionName", currSectionName);
			issueObj.addProperty("questionName", "N/A");
			issueObj.addProperty("issueDesc",
					"Section's initial repeated sections numbers don't match. ProFoRMS value => " + csvLine[4]
							+ ", eForm value => " + section.getInitialRepeatedSections());
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check max repeatable sections number.
		if (!Integer.valueOf(csvLine[5]).equals(section.getMaxRepeatedSections())) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", currEFormName);
			issueObj.addProperty("sectionName", currSectionName);
			issueObj.addProperty("questionName", "N/A");
			issueObj.addProperty("issueDesc",
					"Section's max repeatable sections numbers don't match. ProFoRMS value => " + csvLine[5]
							+ ", eForm value => " + section.getMaxRepeatedSections());
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check group name.
		if (!StringUtils.isEmpty(csvLine[8]) && !csvLine[8].equals(section.getGroupName())) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", currEFormName);
			issueObj.addProperty("sectionName", currSectionName);
			issueObj.addProperty("questionName", "N/A");
			issueObj.addProperty("issueDesc", "Section's group names don't match. ProFoRMS value => " + csvLine[8]
					+ ", eForm value =>" + section.getGroupName());
			addIssueToArray(issueObj, issueArray, filterDups);
		}
	}

	/**
	 * Verifies that the repeated section parent is amongst the listing of sections in the current eForm.
	 * 
	 * @param eForm - The eForm that will be the subject of the search.
	 * @param currSection - The current section to verify.
	 * @param issueArray - A JSON array of validation issues, which will be used to record any irregularities.
	 * @param filterDups - Flag used to indicate whether or not to filter out duplicate issues from the issueArray.
	 */
	private void checkRepeatedSectionParentExists(Eform eForm, Section currSection, JsonArray issueArray,
			Boolean filterDups) {
		boolean found = false;

		for (Section s : eForm.getSectionList()) {
			if (s.getId().equals(currSection.getRepeatedSectionParent())) {
				found = true;
				break;
			}
		}

		// Check if the repeated parent was found.
		if (!found) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eForm.getShortName());
			issueObj.addProperty("sectionName", currSection.getName());
			issueObj.addProperty("questionName", "N/A");
			issueObj.addProperty("issueDesc", "Couldn't find the repeated section parent ("
					+ currSection.getRepeatedSectionParent() + ") in the sections of this eForm.");
			addIssueToArray(issueObj, issueArray, filterDups);
		}
	}

	/**
	 * Runs validation checks on the question data of the current line of the CSV file. All issues will be recorded in
	 * the given JSON array.
	 * 
	 * @param csvLine - The current line of the CSV file being processed.
	 * @param eForm - The current eForm being processed.
	 * @param section - The current section of the eForm being processed.
	 * @param issueArray - A JSON array of validation issues, which will be used to record any irregularities.
	 * @param filterDups - Flag used to indicate whether or not to filter out duplicate issues from the issueArray.
	 */
	private void validateQuestionInfo(String[] csvLine, Eform eForm, Section section, JsonArray issueArray,
			Boolean filterDups) {
		String eFormShortName = eForm.getShortName();
		String sectionName = section.getName();
		SectionQuestion sq = getSectionQuestionByName(section, csvLine[9]);

		// Check if the section question was found.
		if (sq == null) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", csvLine[9]);
			issueObj.addProperty("issueDesc", "Couldn't find the " + csvLine[9] + " question in this section.");
			addIssueToArray(issueObj, issueArray, filterDups);

			return;
		}

		Question question = sq.getQuestion();
		QuestionAttribute qa = question.getQuestionAttribute();
		String questionName = question.getName();

		// Check the if the question group names match.
		String groupName = !qa.getGroupName().equalsIgnoreCase("none") ? qa.getGroupName() : "";

		if (!groupName.equals(csvLine[10])) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc", "The question's group names don't match. ProFoRMS value => "
					+ csvLine[10] + ", eForm value => " + qa.getGroupName());
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the data element names match.
		String dataElemName = !qa.getDataElementName().equalsIgnoreCase("none") ? qa.getDataElementName() : "";

		if (!dataElemName.equals(csvLine[11])) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc", "The question's data element names don't match. ProFoRMS value => "
					+ csvLine[11] + ", eForm value => " + qa.getDataElementName());
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the required flag.
		if (!qa.getRequiredFlag().equals(Boolean.valueOf(csvLine[12]))) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc", "The question's required flags don't match. ProFoRMS value => "
					+ csvLine[12] + ", eForm value => " + qa.getRequiredFlag().toString());
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the calculated flag.
		if (!qa.getCalculatedFlag().equals(Boolean.valueOf(csvLine[13]))) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc", "The question's calculated flags don't match. ProFoRMS value => "
					+ csvLine[13] + ", eForm value => " + qa.getCalculatedFlag().toString());
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the calculation string.
		String calcString = sq.getCalculation() != null ? sq.getCalculation() : "";

		if (!csvLine[14].equals(calcString)) {
			if (StringUtils.isBlank(csvLine[14]) || StringUtils.isBlank(calcString)) {
				JsonObject issueObj = new JsonObject();

				issueObj.addProperty("eFormName", eFormShortName);
				issueObj.addProperty("sectionName", sectionName);
				issueObj.addProperty("questionName", questionName);
				issueObj.addProperty("issueDesc", "The question's calculation strings don't match. ProFoRMS value => "
						+ csvLine[14] + ", eForm value => " + calcString);
				addIssueToArray(issueObj, issueArray, filterDups);
			}
			// Do more validation checks on the calculation strings.
			else {
				validateCalcString(csvLine, calcString, eForm, issueArray, filterDups);
			}
		}

		// Check the skip rule flag.
		if (!qa.getSkipRuleFlag().equals(Boolean.valueOf(csvLine[15]))) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc", "The question's skip rule flags don't match. ProFoRMS value => "
					+ csvLine[15] + ", eForm value => " + qa.getSkipRuleFlag().toString());
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the skip rule types.
		String skipRuleType = qa.getSkipRuleType() != null ? String.valueOf(qa.getSkipRuleType().getValue()) : "";

		if (!skipRuleType.equals(csvLine[16])) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc", "The question's skip rule types don't match. ProFoRMS value => "
					+ csvLine[16] + ", eForm value => " + skipRuleType);
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the skip rule operator type.
		String skipRuleOptType =
				qa.getSkipRuleOperatorType() != null ? String.valueOf(qa.getSkipRuleOperatorType().getValue()) : "";

		if (!skipRuleOptType.equals(csvLine[17])) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc", "The question's skip rule operator types don't match. ProFoRMS value => "
					+ csvLine[17] + ", eForm value => " + skipRuleOptType);
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the skip rule equals value.
		String skipRuleEquals = qa.getSkipRuleEquals() != null ? qa.getSkipRuleEquals() : "";

		if (!skipRuleEquals.equals(csvLine[18])) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc", "The question's skip rule equals values don't match. ProFoRMS value => "
					+ csvLine[18] + ", eForm value => " + skipRuleEquals);
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the range operator.
		if (!qa.getRangeOperator().equals(csvLine[19])) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc", "The question's range operators don't match. ProFoRMS value => "
					+ csvLine[19] + ", eForm value => " + qa.getRangeOperator());
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the range value 1 values.
		String rangeVal1 = qa.getRangeValue1() != null ? qa.getRangeValue1() : "";

		if (!rangeVal1.equals(csvLine[20])) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc", "The question's \"range value 1\" values don't match. ProFoRMS value => "
					+ csvLine[20] + ", eForm value => " + rangeVal1);
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the range value 2 values.
		String rangeVal2 = qa.getRangeValue2() != null ? qa.getRangeValue2() : "";

		if (!rangeVal2.equals(csvLine[21])) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc", "The question's \"range value 2\" values don't match. ProFoRMS value => "
					+ csvLine[21] + ", eForm value => " + rangeVal2);
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the date conversion factors.
		String dateConverFactors = qa.getDtConversionFactor() > 0 ? String.valueOf(qa.getDtConversionFactor()) : "";

		if (!dateConverFactors.equals(csvLine[22])) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc", "The question's date conversion factors don't match. ProFoRMS value => "
					+ csvLine[22] + ", eForm value => " + dateConverFactors);
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the answer types.
		try {
			if (qa.getAnswerType().getValue() != Integer.parseInt(csvLine[23])) {
				JsonObject issueObj = new JsonObject();

				issueObj.addProperty("eFormName", eFormShortName);
				issueObj.addProperty("sectionName", sectionName);
				issueObj.addProperty("questionName", questionName);
				issueObj.addProperty("issueDesc", "The question's answer types don't match. ProFoRMS value => "
						+ csvLine[23] + ", eForm value => " + qa.getAnswerType().getValue());
				addIssueToArray(issueObj, issueArray, filterDups);
			}
		} catch (NumberFormatException e) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc",
					"The ProFoRMS question answer type (" + csvLine[23] + ") is not a number.");
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the minimum character limits.
		try {
			if (!qa.getMinCharacters().equals(Integer.valueOf(csvLine[24]))) {
				JsonObject issueObj = new JsonObject();

				issueObj.addProperty("eFormName", eFormShortName);
				issueObj.addProperty("sectionName", sectionName);
				issueObj.addProperty("questionName", questionName);
				issueObj.addProperty("issueDesc",
						"The question's minimum character limits don't match. ProFoRMS value => " + csvLine[24]
								+ ", eForm value => " + qa.getMinCharacters());
				addIssueToArray(issueObj, issueArray, filterDups);
			}
		} catch (NumberFormatException e) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc",
					"The ProFoRMS question's minimum charactor limit (" + csvLine[24] + ") is not a number.");
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the maximum character limits.
		try {
			if (!qa.getMaxCharacters().equals(Integer.valueOf(csvLine[25]))) {
				JsonObject issueObj = new JsonObject();

				issueObj.addProperty("eFormName", eFormShortName);
				issueObj.addProperty("sectionName", sectionName);
				issueObj.addProperty("questionName", questionName);
				issueObj.addProperty("issueDesc",
						"The question's maximum character limits don't match. ProFoRMS value => " + csvLine[25]
								+ ", eForm value => " + qa.getMaxCharacters());
				addIssueToArray(issueObj, issueArray, filterDups);
			}
		} catch (NumberFormatException e) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc",
					"The ProFoRMS question's maximum charactor limit (" + csvLine[25] + ") is not a number.");
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the pre-population flags.
		if (!qa.getPrepopulation().equals(Boolean.valueOf(csvLine[26]))) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc", "The question's pre-population flags don't match. ProFoRMS value => "
					+ csvLine[26] + ", eForm value => " + qa.getPrepopulation());
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the pre-population values.
		String prePopVal = qa.getPrepopulationValue() != null ? qa.getPrepopulationValue() : "";

		if (!prePopVal.equals(csvLine[27])) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc", "The question's pre-population values don't match. ProFoRMS value => "
					+ csvLine[27] + ", eForm value => " + prePopVal);
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the decimal precision values.
		try {
			if (!qa.getDecimalPrecision().equals(Integer.valueOf(csvLine[28]))) {
				JsonObject issueObj = new JsonObject();

				issueObj.addProperty("eFormName", eFormShortName);
				issueObj.addProperty("sectionName", sectionName);
				issueObj.addProperty("questionName", questionName);
				issueObj.addProperty("issueDesc",
						"The question's decimal precision values don't match. ProFoRMS value => " + csvLine[28]
								+ ", eForm value => " + qa.getDecimalPrecision());
				addIssueToArray(issueObj, issueArray, filterDups);
			}
		} catch (NumberFormatException e) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc",
					"The ProFoRMS question's decimal precision value (" + csvLine[28] + ") is not a number.");
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the conversion factor flags.
		if (!qa.getHasConversionFactor().equals(Boolean.valueOf(csvLine[29]))) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc", "The question's conversion factor flags don't match. ProFoRMS value => "
					+ csvLine[29] + ", eForm value => " + qa.getHasConversionFactor());
			addIssueToArray(issueObj, issueArray, filterDups);
		}

		// Check the conversion factor values.
		String converFactor = qa.getConversionFactor() != null ? qa.getConversionFactor() : "";

		if (!converFactor.equals(csvLine[30])) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", eFormShortName);
			issueObj.addProperty("sectionName", sectionName);
			issueObj.addProperty("questionName", questionName);
			issueObj.addProperty("issueDesc", "The question's conversion factor values don't match. ProFoRMS value => "
					+ csvLine[30] + ", eForm value => " + converFactor);
			addIssueToArray(issueObj, issueArray, filterDups);
		}
	}

	/**
	 * Runs further validation tests on the calculation string of both forms.
	 * 
	 * @param csvLine - The current CSV line being processed.
	 * @param bricsCalc - The calculation string of the BRICS eForm to run tests against.
	 * @param eForm - The current eForm being processed.
	 * @param issueArray - A JSON array of validation issues, which will be used to record any irregularities.
	 * @param filterDups - Flag used to indicate whether or not to filter out duplicate issues from the issueArray.
	 */
	private void validateCalcString(String[] csvLine, String bricsCalc, Eform eForm, JsonArray issueArray,
			Boolean filterDups) {
		String proformsMathPart = csvLine[14].replaceAll("\\[S_\\d+_Q_\\d+\\]", "").trim();
		String bricsMathPart = bricsCalc.replaceAll("\\[S_\\d+_Q_\\d+\\]", "").trim();

		// Check if the arithmetic portion of the calculation string matches.
		if (!proformsMathPart.equals(bricsMathPart)) {
			JsonObject issueObj = new JsonObject();

			issueObj.addProperty("eFormName", csvLine[0]);
			issueObj.addProperty("sectionName", csvLine[1]);
			issueObj.addProperty("questionName", csvLine[9]);
			issueObj.addProperty("issueDesc",
					"The arithmetic portions of the question's calculation string do not match. ProFoRMS value => "
					+ csvLine[14] + ", eForm value => " + bricsCalc);
			addIssueToArray(issueObj, issueArray, filterDups);

			return;
		}

		// Get all of the section question references from the BRICS calculation string.
		List<String> sectQuestRefList = new ArrayList<String>(3);
		Pattern p = Pattern.compile("\\[S_\\d+_Q_\\d+\\]");
		Matcher m = p.matcher(bricsCalc);

		while (m.find()) {
			sectQuestRefList.add(m.group().trim());
		}

		// Validate the section question references.
		p = Pattern.compile("\\d+");
		
		for (String sqRef : sectQuestRefList) {
			// Parse out the section and question IDs.
			List<String> ids = new ArrayList<String>(2);
			m = p.matcher(sqRef);

			while (m.find()) {
				ids.add(m.group().trim());
			}
			
			Long sectId = Long.valueOf(ids.get(0));
			Long quesId = Long.valueOf(ids.get(1));

			// Search for the referenced section and question.
			boolean found = false;

			sectLoop: for (Section sect : eForm.getSectionList()) {
				if (sect.getId().equals(sectId)) {
					for (SectionQuestion sq : sect.getSectionQuestion()) {
						if (sq.getQuestion().getId().equals(quesId)) {
							found = true;
							break sectLoop;
						}
					}
				}
			}

			// Check if the reference was found.
			if (!found) {
				JsonObject issueObj = new JsonObject();

				issueObj.addProperty("eFormName", csvLine[0]);
				issueObj.addProperty("sectionName", csvLine[1]);
				issueObj.addProperty("questionName", csvLine[9]);
				issueObj.addProperty("issueDesc", "Can't find section or question referenced in " + sqRef
						+ " of the calculation string " + bricsCalc + ".");
				addIssueToArray(issueObj, issueArray, filterDups);
			}
		}
	}

	/**
	 * Gets the SectionQuestion object whose associated question matches the given question name.
	 * 
	 * @param section - The section to search through.
	 * @param questionName - The name of the target question.
	 * @return The found SectionQuestion object, or null if none is found.
	 */
	private SectionQuestion getSectionQuestionByName(Section section, String questionName) {
		SectionQuestion sectionQuestion = null;

		for (SectionQuestion sq : section.getSectionQuestion()) {
			if (sq.getQuestion().getName().equals(questionName)) {
				sectionQuestion = sq;
				break;
			}
		}

		return sectionQuestion;
	}

	/**
	 * 
	 * @param newIssue
	 * @param issueArray
	 * @param filterDups
	 * @throws IllegalStateException
	 * @throws ClassCastException
	 */
	private void addIssueToArray(JsonObject newIssue, JsonArray issueArray, Boolean filterDups)
			throws IllegalStateException, ClassCastException {
		// Check the filter dups flag.
		if (!filterDups) {
			issueArray.add(newIssue);
			return;
		}

		boolean found = false;
		String newEformName = newIssue.get("eFormName").getAsString();
		String newSectionName = newIssue.get("sectionName").getAsString();
		String newQuestionName = newIssue.get("questionName").getAsString();
		String newDescr = newIssue.get("issueDesc").getAsString();

		for (JsonElement elm : issueArray) {
			JsonObject issue = elm.getAsJsonObject();
			String eFormName = issue.get("eFormName").getAsString();
			String sectionName = issue.get("sectionName").getAsString();
			String questionName = issue.get("questionName").getAsString();
			String descr = issue.get("issueDesc").getAsString();

			// Check values for the current issue and see if they match the new issue.
			if (newEformName.equals(eFormName) && newSectionName.equals(sectionName)
					&& newQuestionName.equals(questionName) && newDescr.equals(descr)) {
				found = true;
				break;
			}
		}

		// Check if the new issue can be added to the issue array.
		if (!found) {
			issueArray.add(newIssue);
		}
	}

	public EformManager getEformManager() {
		return eformManager;
	}

	public void setEformManager(EformManager eformManager) {
		this.eformManager = eformManager;
	}

}
