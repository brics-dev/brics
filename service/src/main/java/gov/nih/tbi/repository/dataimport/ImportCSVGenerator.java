package gov.nih.tbi.repository.dataimport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sun.mail.iap.ProtocolException;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.repository.dao.ProformsDataImportDao;

//This class is used to replace Step 1 of the MIRTH process.
@Service
@Scope("singleton")
public class ImportCSVGenerator {
	private static final Logger logger = Logger.getLogger(ImportCSVGenerator.class);

	@Autowired
	ModulesConstants modulesConstants;

	@Autowired
	ProformsValidation proformsValidation;

	@Autowired
	ProformsDataImportDao proformsDataImportDao;

	private static Integer SUBMISSION_STATUS = 4;

	/**
	 * Main method for handling submissions. Called by brics scheduler
	 * 
	 * @throws MalformedURLException
	 * @throws ProtocolException
	 * @throws IOException
	 * @throws SQLException
	 */
	public synchronized void processSubmissions() throws ProtocolException, IOException, SQLException {
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmssS");
		List<AdministeredFormProcessingInfo> adminFormInfoList = proformsDataImportDao.getAllAdminForms();
		for (AdministeredFormProcessingInfo adminFormInfo : adminFormInfoList) {
			List<String> dataProcessingInfo = new ArrayList<>();
			List<String> messages = new ArrayList<>();
			String dataProcessingHeader = "";
			String header = "";
			String currentDateTime = f.format(new Date());
			dataProcessingInfo.add(modulesConstants.getModulesOrgEmail());
			dataProcessingInfo.add(adminFormInfo.getDataStructure() + "_" + currentDateTime);
			dataProcessingInfo.add(adminFormInfo.getStudyId());
			dataProcessingInfo.add(adminFormInfo.getAdminFormId());
			dataProcessingInfo.add(fixUTCDate(proformsDataImportDao.getFinalLockDate(adminFormInfo.getAdminFormId())));
			dataProcessingInfo.add(proformsDataImportDao.getFinalLockByUser(adminFormInfo.getAdminFormId()));

			for (String s : dataProcessingInfo) {
				dataProcessingHeader = dataProcessingHeader.concat(s + ",");
			}
			dataProcessingHeader = dataProcessingHeader.substring(0, dataProcessingHeader.length() - 1) + "\n";
			header = dataProcessingHeader.concat(adminFormInfo.getDataStructure());
			String csv = generateCSV(adminFormInfo.getAdminFormId());
			logger.info("The CSV is: " + csv);

			messages.add(header + "\n" + csv);
			header = csv = "";

			String validationMessage = submitDataToImportRestful(messages); // submit then update status
			logger.info("Response is: " + validationMessage);

			proformsDataImportDao.updateAdminFormSubmissionStatus(SUBMISSION_STATUS, adminFormInfo.getAdminFormId());
			logger.info("Update status of admin form: " + adminFormInfo.getAdminFormId());
			logger.info("Sending response to Step 2");

			proformsValidation.updateProformsSubmissionStatus(validationMessage, adminFormInfo.getAdminFormId());

		}
	}

	/**
	 * Generates the CSV string to be submitted to import-RESTful. It gathers the
	 * ResultSet and ResultSetMetaData from performCrosstabStatement() and creates
	 * the submission CSV.
	 * 
	 * @param rs
	 * @param md
	 * @param multiSelects
	 * @return The data from the ResultSet in a CSV string
	 * @throws SQLException
	 */
	private String generateCSV(String adminFormId) throws SQLException {
		List<DataImportDataElementData> dataElementDataList = proformsDataImportDao
				.getDataFromDataSubmissionView(adminFormId);
		List<DataImportDataElementData> dataElementData = new ArrayList<>();
		List<String> multiSelects = proformsDataImportDao.getMultiSelectGroupDataElements(adminFormId);
		List<DataImportRepeatableGroupData> repeatableGroupData = new ArrayList<>();
		String csv = "";

		for (DataImportDataElementData dataElementDataObj : dataElementDataList) {
			String repeatableGroupName = dataElementDataObj.getRepeatableGroupName();
			String groupDataElement = dataElementDataObj.getGroupDataElement();
			if (isInArray(dataElementData, groupDataElement) == -1) {
				DataImportDataElementData de = new DataImportDataElementData(dataElementDataObj.getGroupDataElement(),
						dataElementDataObj.getSectionId(), repeatableGroupName, new ArrayList<String>(),
						new ArrayList<Integer>(), dataElementDataObj.isRepeatable());
				dataElementData.add(de);
			}
			if (isUniqueRGName(repeatableGroupData, repeatableGroupName)) {
				repeatableGroupData.add(new DataImportRepeatableGroupData(repeatableGroupName, "", 0));
			}
		}
		csv = "record";

		for (DataImportDataElementData groupDataElement : dataElementData) {
			csv = csv + "," + groupDataElement.getGroupDataElement();
		}

		logger.info("This is the CSV header: " + csv);
		csv = csv.concat("\n");

		for (DataImportDataElementData de : dataElementDataList) {
			String columnResultsData = de.getValue().get(0);
			if (columnResultsData != null) {
				String currentColumnName = de.getGroupDataElement();
				int currentColumnArrayIndex = isInArray(dataElementData, currentColumnName);
				String sectionId = de.getSectionId();
				boolean isRepeatable = de.isRepeatable();
				int returnIndex = returnIndex(dataElementData.get(currentColumnArrayIndex).getId(), sectionId);
				DataImportDataElementData workingDe = dataElementData.get(currentColumnArrayIndex);
				if (multiSelects.contains(currentColumnName)) {
					if (returnIndex > -1) {
						dataElementData.get(currentColumnArrayIndex).addStringToValues(returnIndex, columnResultsData);

					} else if (returnIndex == -1) {
						if (isRepeatable) {
							incrementRepeatableGroup(repeatableGroupData, dataElementData.get(currentColumnArrayIndex),
									sectionId);
							workingDe = addNullToBlankSections(repeatableGroupData, dataElementData.get(currentColumnArrayIndex));
						}
						workingDe.getValue().add(columnResultsData);
						workingDe.getId().add(Integer.parseInt(sectionId));
						dataElementData.set(currentColumnArrayIndex, workingDe);

					}
				} else {
					if (isRepeatable) {
						incrementRepeatableGroup(repeatableGroupData, dataElementData.get(currentColumnArrayIndex),
								sectionId);
						workingDe = addNullToBlankSections(repeatableGroupData, dataElementData.get(currentColumnArrayIndex));
					}
					workingDe.getValue().add(columnResultsData);
					workingDe.getId().add(Integer.parseInt(sectionId));
					dataElementData.set(currentColumnArrayIndex, workingDe);
				}
			}
		}

		csv = csv.concat("x");

		int greatest = 0;// largestGroupSize
		for (DataImportDataElementData groupDataElement : dataElementData) {
			if (groupDataElement.getValue().size() > greatest) {
				greatest = groupDataElement.getValue().size();
			}
		}

		for (int i = 0; i < greatest; i++) {
			for (DataImportDataElementData groupDataElement : dataElementData) {
				if (groupDataElement.getValue().isEmpty() || groupDataElement.getValue().size() <= i || groupDataElement.getValue().get(i) == null) {
					csv = csv + ",";
				} else {
					csv = csv + ",\"" + groupDataElement.getValue().get(i) + "\"";
				}
			}
			csv = csv + "\n";
		}

		String copyOfCSV = csv;
		List<String> csvList = Arrays.asList(copyOfCSV.split("\n"));
		List<String> csvHeaderList = Arrays.asList(csvList.get(0).split(","));

		csvList.set(0, String.join(",", csvHeaderList));

		csv = String.join("\n", csvList);

		return csv;
	}

	/**
	 * Searches repeatableGroupList to see if repeatableGroupName exists in the
	 * list.
	 * 
	 * @param repeatableGroupList
	 * @param repeatableGroupName
	 * @return boolean value of whether repeatableGroupName exists in
	 *         repeatableGroupList
	 */
	protected boolean isUniqueRGName(List<DataImportRepeatableGroupData> repeatableGroupList,
			String repeatableGroupName) {
		for (DataImportRepeatableGroupData rg : repeatableGroupList) {
			if (rg.getRepeatableGroupName().equals(repeatableGroupName)) {
				return false;
			}
		}
		return true;
	}

	protected int isInArray(List<DataImportDataElementData> dataList, String deName) {
		for (DataImportDataElementData data : dataList) {
			if (deName.equals(data.getGroupDataElement())) {
				return dataList.indexOf(data);
			}
		}
		return -1;
	}

	protected int returnIndex(List<Integer> idList, String id) {
		int idAsInt = Integer.valueOf(id);
		for (int idObject : idList) {
			if (idAsInt == idObject) {
				return idList.indexOf(idAsInt);
			}
		}
		return -1;
	}

	/**
	 * Increments the sectionIterator value of a repeatableGroup if the groupName
	 * and sectionId the found
	 * 
	 * @param repeatableGroup
	 * @param groupDataElement
	 * @param sectionId
	 */
	protected void incrementRepeatableGroup(List<DataImportRepeatableGroupData> repeatableGroup,
			DataImportDataElementData groupDataElement, String sectionId) {
		String workingRGName = groupDataElement.getRepeatableGroupName();

		for (DataImportRepeatableGroupData repeatableGroupName : repeatableGroup) {
			if (repeatableGroupName.getRepeatableGroupName().equals(workingRGName)) {
				if (!repeatableGroupName.getCurrentSectionid().equals(sectionId)) {
					repeatableGroupName.setCurrentSectionid(sectionId);
					repeatableGroupName.setSectionIterator(repeatableGroupName.getSectionIterator() + 1);
				}
			}
		}
	}

	/**
	 * Fills in null values to sections that are blank
	 * 
	 * @param repeatableGroup
	 * @param groupDataElement
	 */
	protected DataImportDataElementData addNullToBlankSections(List<DataImportRepeatableGroupData> repeatableGroup,
			DataImportDataElementData groupDataElement) {
		String workingRGName = groupDataElement.getRepeatableGroupName();

		for (DataImportRepeatableGroupData repeatableGroupName : repeatableGroup) {
			if (repeatableGroupName.getRepeatableGroupName().equals(workingRGName)) {
				if (repeatableGroupName.getSectionIterator() > groupDataElement.getValue().size() + 1) {
					for (int i = groupDataElement.getValue().size(); i < repeatableGroupName.getSectionIterator()
							- 1; i++) {
						groupDataElement.getValue().add(null);
						groupDataElement.getId().add(repeatableGroupName.getSectionIterator());

					}
				}
			}
		}
		return groupDataElement;
	}

	/**
	 * Returns a string representation of a list with the opening/closing braces
	 * replaced with brackets
	 * 
	 * @param list
	 * @return The formatted string
	 */
	protected String formattedListToString(List<String> list) {
		String result = list.toString();
		result = result.replace("[", "");
		result = result.replace("]", "");
		return result;
	}

	protected String fixUTCDate(String finalLockDate) {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

		Calendar calendar = new GregorianCalendar();
		TimeZone timeZone = calendar.getTimeZone();
		int offset = timeZone.getOffset(calendar.getTimeInMillis()) / 1000 / 60 * -1;

		try {
			Date lockDate = f.parse(finalLockDate);
			calendar.setTime(lockDate);
			calendar.add(Calendar.MINUTE, offset);
		} catch (ParseException e) {
			logger.error("Unable to parse lock date");
			e.printStackTrace();
		}

		return f.format(calendar.getTime());
	}

	/**
	 * Submits the fully compiled message (CSV and headers) to import-RESTful for
	 * processing. It will return a Success or Failure depending on the import and
	 * validation within import-RESTful
	 * 
	 * @param messages
	 * @return The response from the rest service call
	 * @throws MalformedURLException
	 * @throws ProtocolException
	 * @throws IOException
	 */
	protected String submitDataToImportRestful(List<String> messages)
			throws MalformedURLException, ProtocolException, IOException {
		BufferedReader resp = null;

		try {
			String data = formattedListToString(messages);
			String importRestfulUrl = modulesConstants.getImportRestfulUrl();
			URL url = new URL(importRestfulUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "text/plain");
			conn.setRequestProperty("charset", "utf-8");
			conn.setUseCaches(false);
			conn.setRequestProperty("Content-Length", String.valueOf(messages.toString().getBytes().length));

			OutputStream outStream = conn.getOutputStream();
			OutputStreamWriter outWriter = new OutputStreamWriter(outStream, "UTF-8");
			outWriter.write(data);
			outWriter.flush();
			outWriter.close();

			InputStream inputStream = conn.getInputStream();
			InputStreamReader streamRead = new InputStreamReader(inputStream);
			resp = new BufferedReader(streamRead);

			StringBuffer rData = new StringBuffer();
			String rDataLine = null;

			while ((rDataLine = resp.readLine()) != null) {
				rData.append(rDataLine);
				logger.info("response is: " + rDataLine);
			}

			logger.info("Web service to FITBIR import-restful has been completed. Waiting for response.");

			return rData.toString();
		} finally {
			resp.close();
		}

	}

}