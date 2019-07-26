package main.java.dataimport;

import gov.nih.tbi.commons.WebstartRestProvider;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.ordermanager.model.DerivedBiosampleConfigurations;
import gov.nih.tbi.ordermanager.model.DerivedBiosampleFormConfiguration;
import gov.nih.tbi.ordermanager.model.DerivedBiosampleRepository;
import gov.nih.tbi.query.model.DerivedDataContainer;
import gov.nih.tbi.query.model.DerivedDataKey;
import gov.nih.tbi.query.model.DerivedDataRow;
import gov.nih.tbi.query.model.RepeatableGroupDataElement;
import gov.nih.tbi.query.ws.RestQueryToolProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.ConfigurationException;
import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import main.java.dataimport.exception.BiosampleRepositoryNotFound;
import main.java.dataimport.exception.DerivedDataMapperException;
import main.java.dataimport.exception.DerivedDataNotFoundException;
import main.java.dataimport.utils.DerivedDataUtils;

public class DerivedDataMapper {
	private static final Logger logger = Logger.getLogger(DerivedDataMapper.class);

	public final static String DERIVED_DATA_DIRECTORY_NAME = "derived";
	private String submissionLocation;
	private int FORM_NAME_LINE = 0;
	private int COLUMN_HEADER_LINE = 1;
	private RestQueryToolProvider queryToolProvider;
	private String repositoryName;
	private List<String> messages = new ArrayList<String>();
	private DerivedBiosampleConfigurations configurations;
	private WebstartRestProvider dictionaryProvider;
	private String bricsUrl;

	public DerivedDataMapper(DerivedBiosampleConfigurations configurations, String queryToolUrl,
			WebstartRestProvider dictionaryProvider, String submissionLocation, String repositoryName,
			String bricsUrl) {
		super();
		this.dictionaryProvider = dictionaryProvider;
		this.queryToolProvider = new RestQueryToolProvider(queryToolUrl, null);
		this.submissionLocation = submissionLocation;
		this.repositoryName = repositoryName;
		this.configurations = configurations;
		this.bricsUrl = bricsUrl;
	}

	/**
	 * Return the list of permissible values for Biosample's neurological diagnosis data element. We need this to
	 * properly map other,specify in NeurologicalExamination to Other in the Catalog form structure.
	 * 
	 * @param catalogDiagnosisDataElementName - name of catalog's diagnosis data element
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private List<String> getDiagnosisPermissibleValues(String catalogDiagnosisDataElementName)
			throws UnsupportedEncodingException {
		DataElement neuroDiagnosisElement =
				dictionaryProvider.getDataElementByName(bricsUrl, catalogDiagnosisDataElementName);

		List<String> diagnosises = new ArrayList<String>();
		for (ValueRange vr : neuroDiagnosisElement.getValueRangeList()) {
			diagnosises.add(vr.getValueRange().toLowerCase());
		}

		return diagnosises;
	}

	/**
	 * Returns the list of headers, including the headers for derived data
	 * 
	 * @return
	 * @throws BiosampleRepositoryNotFound
	 */
	private List<String> getHeaders() throws BiosampleRepositoryNotFound {
		File file = new File(submissionLocation);
		CSVReader reader = null;

		List<String> columnHeaders = new ArrayList<String>();

		try {
			reader = new CSVReader(new BufferedReader(new FileReader(file)));

			int rowIndex = 0;
			for (String[] line; (line = reader.readNext()) != null && rowIndex <= COLUMN_HEADER_LINE;) {

				if (rowIndex == COLUMN_HEADER_LINE) { // reading column headers

					for (int i = 0; i < line.length; i++) {
						String currentHeader = line[i];

						if (!currentHeader.isEmpty()) {
							columnHeaders.add(currentHeader);
						}
					}

					List<String> derivedDataColumns =
							DerivedDataUtils.getDerivedDataColumns(configurations, repositoryName);

					columnHeaders.addAll(derivedDataColumns);
					return columnHeaders;
				}

				rowIndex++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		throw new DerivedDataMapperException("Catalog data file is missing column headers!");
	}

	/**
	 * Returns a set of all the GUIDs that are in the original catalog
	 * 
	 * @param columnIndexMap
	 * @return
	 * @throws ConfigurationException
	 */
	private Set<String> getGuids(List<String> columnHeaders) throws ConfigurationException {
		File file = new File(submissionLocation);
		CSVReader reader = null;
		Set<String> guids = new HashSet<String>();

		String guidColumnName = configurations.getGuidColumnName();

		int guidColumnIndex = columnHeaders.indexOf(guidColumnName);

		if (guidColumnIndex == -1) {
			throw new ConfigurationException(
					"GUID column defined in derived data configuration does not match any of the columns in the catalog.");
		}

		try {
			reader = new CSVReader(new BufferedReader(new FileReader(file)));

			int rowIndex = 0;
			for (String[] line; (line = reader.readNext()) != null;) {

				if (rowIndex > COLUMN_HEADER_LINE) { // reading data that comes after the column header line
					if (guidColumnIndex < line.length) {
						String currentGuid = line[guidColumnIndex];
						if (!currentGuid.isEmpty()) { // yes, it's valid to have a biosample without a GUID.
							guids.add(currentGuid);
						}
					}
				}

				rowIndex++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return guids;
	}

	/**
	 * Call the query tool derivedDataRestService. Return a map of form name and version to its the derived data map.
	 * 
	 * @param guids
	 * @return
	 * @throws BiosampleRepositoryNotFound
	 */
	public Map<NameAndVersion, Map<DerivedDataKey, DerivedDataRow>> queryDerivedData(Set<String> guids)
			throws BiosampleRepositoryNotFound {
		DerivedBiosampleRepository repositoryConfiguration =
				DerivedDataUtils.getBiosampleRepositoryConfiguration(configurations, repositoryName);
		Map<NameAndVersion, Map<DerivedDataKey, DerivedDataRow>> formDerivedDataMap =
				new HashMap<NameAndVersion, Map<DerivedDataKey, DerivedDataRow>>();

		for (DerivedBiosampleFormConfiguration formConfiguration : repositoryConfiguration.getFormConfiguration()) {
			NameAndVersion formNameAndVersion = formConfiguration.getFormNameAndVersion();
			List<RepeatableGroupDataElement> repeatableGroupDataElements =
					formConfiguration.getRepeatableGroupDataElements();
			try {
				DerivedDataContainer derivedDataContainer =
						queryToolProvider.getDerivedData(formNameAndVersion, repeatableGroupDataElements, guids);
				formDerivedDataMap.put(formNameAndVersion, derivedDataContainer.getDataMap());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw new WebServiceException("Error occured while calling the derived data query tool webservice");
			}
		}

		return formDerivedDataMap;
	}

	/**
	 * Writes a new catalog file with derived data mapped to it. New catalog will be written to /derived/...
	 * 
	 * @return
	 * @throws BiosampleRepositoryNotFound
	 * @throws ConfigurationException
	 * @throws DerivedDataNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public List<String> mapDerivedData() throws BiosampleRepositoryNotFound, ConfigurationException,
			DerivedDataNotFoundException, UnsupportedEncodingException {
		List<String> columnHeaders = getHeaders();
		Set<String> guids = getGuids(columnHeaders);

		// map of form name and version to a derived data map, we get this from the query tool
		Map<NameAndVersion, Map<DerivedDataKey, DerivedDataRow>> formDerivedDataMap = queryDerivedData(guids);

		// write a copy of csv with derived data
		writeCsvWithDerivedData(columnHeaders, formDerivedDataMap);

		// return messages to be sent to OPs
		return messages;
	}

	/**
	 * Writes the derived copy to .../derived/
	 * 
	 * @param columnHeaders
	 * @param formDerivedDataMap
	 * @throws BiosampleRepositoryNotFound
	 * @throws DerivedDataNotFoundException
	 * @throws ConfigurationException
	 * @throws UnsupportedEncodingException
	 */
	public void writeCsvWithDerivedData(List<String> columnHeaders,
			Map<NameAndVersion, Map<DerivedDataKey, DerivedDataRow>> formDerivedDataMap)
			throws BiosampleRepositoryNotFound, DerivedDataNotFoundException, ConfigurationException,
			UnsupportedEncodingException {
		File originalFile = new File(submissionLocation);
		// derived data needs to be a new directory
		String derivedFilePath = originalFile.getParent() + File.separator + DERIVED_DATA_DIRECTORY_NAME
				+ File.separator + originalFile.getName();
		logger.info("Derived data path: " + derivedFilePath);
		File derivedFile = new File(derivedFilePath);

		CSVReader reader = null;
		CSVWriter csvWriter = null;

		// get the column name and index for GUID and VisitTyp
		String guidColumnName = configurations.getGuidColumnName();
		String visitTypeColumnName = configurations.getVisitTypeColumnName();
		String sampleTypeColumnName = configurations.getSampleTypeColumnName();
		int guidIndex = columnHeaders.indexOf(guidColumnName);
		int visitTypeIndex = columnHeaders.indexOf(visitTypeColumnName);
		int sampleTypeIndex = columnHeaders.indexOf(sampleTypeColumnName);

		DerivedBiosampleRepository repositoryConfiguration =
				DerivedDataUtils.getBiosampleRepositoryConfiguration(configurations, repositoryName);
		Map<String, String> dataElementColumnMapping = repositoryConfiguration.getDataElementColumnMapping();

		// get the necessary values used for mapping diagnosis values
		String diagnosisDataElementName =
				repositoryConfiguration.getDiagnosisConfiguration().getCatalogDataElementName();
		String otherPv = repositoryConfiguration.getDiagnosisConfiguration().getCatalogOtherPermissibleValue();
		String noDiagnosisPv = repositoryConfiguration.getDiagnosisConfiguration().getNoDiagnosisPermissibleValue();
		String derivedDiagnosisDataElementName =
				repositoryConfiguration.getDiagnosisConfiguration().getDeriveDataElementName();
		String caseOrControl = repositoryConfiguration.getDiagnosisConfiguration().getCaseOrControl();

		// get the list of permissible values of the neurological diagnosis data element in biosample catalog
		List<String> diagnosisPvList = getDiagnosisPermissibleValues(diagnosisDataElementName);

		try {
			if (!derivedFile.exists()) { // need to create the new file and directories
				if (derivedFile.getParentFile() != null) {
					derivedFile.getParentFile().mkdirs();
				}
				derivedFile.createNewFile();
			}

			reader = new CSVReader(new BufferedReader(new FileReader(originalFile)));
			csvWriter = new CSVWriter(new FileWriter(derivedFile), CSVWriter.DEFAULT_SEPARATOR,
					CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER);

			int rowIndex = 0;
			for (String[] line; (line = reader.readNext()) != null;) {
				if (line.length == 0) {
					logger.info("Line empty, skipping...");
					continue;
				}

				// go ahead and just copy over the form name
				if (rowIndex == FORM_NAME_LINE) {
					csvWriter.writeNext(DerivedDataUtils.escapeLine(line));

					// write the new column headers
				} else if (rowIndex == COLUMN_HEADER_LINE) {
					String[] headerLine = columnHeaders.toArray(new String[columnHeaders.size()]);
					csvWriter.writeNext(DerivedDataUtils.escapeLine(headerLine));
				} else {
					String currentGuid = line[guidIndex];
					String currentVisitType = line[visitTypeIndex];
					String sampleType = line[sampleTypeIndex];

					// empty guid is valid, just copy the line over without deriving data
					if (currentGuid.isEmpty()) {
						csvWriter.writeNext(DerivedDataUtils.escapeLine(line));
					} else {
						// start deriving the current line
						String[] derivedLine = new String[columnHeaders.size()];

						// need to copy the original line array into a derived line array so we can fit all of the data
						for (int i = 0; i < line.length; i++) {
							derivedLine[i] = line[i];
						}

						// this flag tells us if any of the derive data is missing for the current guid+visit type
						boolean missingDerivedData = false;

						// iterate through all the forms we need to derive data from
						for (DerivedBiosampleFormConfiguration formConfiguration : repositoryConfiguration
								.getFormConfiguration()) {

							NameAndVersion currentForm = formConfiguration.getFormNameAndVersion();

							// get the derived data for the current form
							Map<DerivedDataKey, DerivedDataRow> currentDerivedDataMap =
									formDerivedDataMap.get(currentForm);

							// get the current visit type mapping
							Map<String, String> currentVisitTypeMapping = formConfiguration.getVisitTypeMapping();

							// this is the actual visit type we want to use to derive data from
							String visitTypeToDeriveFrom = null;

							// no visit type mapping to use, set the visit type key to empty string
							if (currentVisitTypeMapping == null || currentVisitTypeMapping.isEmpty()) {
								visitTypeToDeriveFrom = ServiceConstants.EMPTY_STRING;
							} else {
								if (!currentVisitType.isEmpty()) {
									visitTypeToDeriveFrom = currentVisitTypeMapping.get(currentVisitType);
								} else { // it is possible for visit type to be empty. If empty, just derive and an
										 // empty
										 // visit type.
									visitTypeToDeriveFrom = ServiceConstants.EMPTY_STRING;
								}
							}

							if (visitTypeToDeriveFrom == null) {
								String error = "ERROR: Missing visit type mapping for: " + currentVisitType
										+ " in form, " + currentForm;
								logger.error(error);
								throw new ConfigurationException(error);
							}

							if (currentDerivedDataMap == null) {
								String error = "ERROR: Missing derived data from form: " + currentForm;
								logger.error(error);
								throw new DerivedDataNotFoundException(error);
							}

							DerivedDataKey currentKey = new DerivedDataKey(currentGuid, visitTypeToDeriveFrom);

							DerivedDataRow currentRow = currentDerivedDataMap.get(currentKey);

							if (currentRow != null) {
								for (RepeatableGroupDataElement rgDe : formConfiguration
										.getRepeatableGroupDataElements()) {
									// get the current data element name
									String dataElementName = rgDe.getDataElementName();
									// get the value that we derived for this data element
									String currentDerivedValue = currentRow.getRow().get(dataElementName);

									// ******************** MAP NEUROLOGICAL DIAGNOSIS *******************
									// if data element is neurological exam, we will need to handle other, specify here
									if (dataElementName.equals(derivedDiagnosisDataElementName)) {

										// if diagnosis is empty, the value needs to become 'No Neurological Diagnosis'
										if (currentDerivedValue.isEmpty()) {
											currentDerivedValue = noDiagnosisPv;

											// if the derived neurological exam is not with in the permissible value,
											// the value needs to become 'Other'
										} else if (!diagnosisPvList.contains(currentDerivedValue.toLowerCase())) { //
											currentDerivedValue = otherPv;
										}
									}
									// ******************* END MAP NEUROLOGICAL DIAGNOSIS ****************


									// get the column name of the current data element
									String currentColumn = dataElementColumnMapping.get(dataElementName);
									// get the column index from the column name
									Integer currentColumnIndex = columnHeaders.indexOf(currentColumn);

									// write the derived value to the derived line
									derivedLine[currentColumnIndex] = currentDerivedValue;
								}

								// if currentRow is null, it means there are no derived data found for the guid and
								// visit
								// type combination. we dont do anything because we do not want that row of data to
								// appear
								// in catalog
							} else {
								String missingDataMessage = currentGuid + ";" + currentVisitType + ";" + currentForm
										+ ";Missing derived data;;" + sampleType;
								messages.add(missingDataMessage);
								missingDerivedData = true;
							}
						}

						// only write this line if there are no missing derived data
						if (!missingDerivedData) {

							// we noticed that some records in neurological exam form had a diagnosis even though its
							// also a control.
							// the routine below will set the diagnosis to no diagnosis when the subject is a control
							String caseOrControlColumn = dataElementColumnMapping.get(caseOrControl);
							Integer caseOrControlColumnIndex = columnHeaders.indexOf(caseOrControlColumn);

							String caseOrControlValue = derivedLine[caseOrControlColumnIndex];

							if (caseOrControlValue.equalsIgnoreCase("Control")) {
								String diagnosisColumn = dataElementColumnMapping.get(derivedDiagnosisDataElementName);
								Integer diagnosisColumnIndex = columnHeaders.indexOf(diagnosisColumn);
								String diagnosisValue = derivedLine[diagnosisColumnIndex];

								if (!diagnosisValue.equals(noDiagnosisPv)) {
									derivedLine[diagnosisColumnIndex] = noDiagnosisPv;
								}
							}
							// END remove control's diagnosis

							csvWriter.writeNext(DerivedDataUtils.escapeLine(derivedLine));
						}
					}
				}

				rowIndex++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (csvWriter != null) {
					csvWriter.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
