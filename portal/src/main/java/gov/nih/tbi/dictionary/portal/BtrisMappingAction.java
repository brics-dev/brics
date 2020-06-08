package gov.nih.tbi.dictionary.portal;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.bytecode.opencsv.CSVReader;
import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.service.BtrisMappingManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.dictionary.model.hibernate.BtrisMapping;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

public class BtrisMappingAction extends BaseDataElementSearchAction {

	private static final long serialVersionUID = 5381872918271110943L;

	static Logger logger = Logger.getLogger(BtrisMappingAction.class);

	@Autowired
	protected BtrisMappingManager btrisMappingManager;

	public String deShortName;
	public DataElement de;

	private File upload;

	private String uploadContentType;

	private Map<String, Integer> headersMap;

	private HashMap<String, String> enteredRows = new HashMap<String, String>();

	public String adminBtrisMappingImport() {
		return PortalConstants.ACTION_IMPORT_BTRIS_MAPPING;
	}

	public String adminUploadBtrisMapping() throws Exception {
		if (ServiceConstants.CSV_FILE.equalsIgnoreCase(uploadContentType)
				|| "text/csv".equalsIgnoreCase(uploadContentType)
				|| "application/csv".equalsIgnoreCase(uploadContentType)) {
			// file itself is in File upload
			CSVReader reader = null;

			try {
				reader = new CSVReader(new FileReader(upload));
				List<String[]> lines = reader.readAll();

				if (lines.size() > 0) {
					String[] headers = lines.get(0);
					this.initializeHeaders(headers);

					List<String> headerErrors = this.validateImportHeaderGetErrors(headers);
					if (!headerErrors.isEmpty()) {
						for (String headerError : headerErrors) {
							addActionError(headerError);
						}
						return PortalConstants.ACTION_IMPORT_BTRIS_MAPPING;
					}

					// the headers could be out of order but still valid, so handle that
					Map<String, Integer> headersMap = new HashMap<String, Integer>();
					for (int i = 0; i < headers.length; i++) {
						headersMap.put(headers[i].toLowerCase().trim(), i);
					}

					// validate all rows
					List<String> validationErrors = this.validateBtrisMappingFile(lines);
					for (String error : validationErrors) {
						addActionError(error);
					}

					if (!hasActionErrors()) {
						List<BtrisMapping> btrisMappingList = new ArrayList<BtrisMapping>();
						// load the rows
						for (int i = 1; i < lines.size(); i++) {
							String[] line = lines.get(i);

							String lineDeName = this.getValueFromMappingByHeader(line,
									ServiceConstants.BTRIS_MAPPING_BRICS_DE_SHORTNAME);
							String linePvValue = this.getValueFromMappingByHeader(line,
									ServiceConstants.BTRIS_MAPPING_BRICS_PV_VALUE);
							String lineObservationName = this.getValueFromMappingByHeader(line,
									ServiceConstants.BTRIS_MAPPING_OBSERVATION_NAME);
							String lineRedCode = this.getValueFromMappingByHeader(line,
									ServiceConstants.BTRIS_MAPPING_RED_CONCEPT_CODE);
							String lineSpecimenType = this.getValueFromMappingByHeader(line,
									ServiceConstants.BTRIS_MAPPING_SPECIMEN_TYPE);
							String lineTable = this.getValueFromMappingByHeader(line,
									ServiceConstants.BTRIS_MAPPING_TABLE);

							// the above fail gracefully to a null, so check here
							// where we can handle it with messaging
							if (lineDeName == null || lineDeName.equals("")) {
								addActionError(
										"The BRICS Data Element Short Name header was missing.  Please check your input file and try again.");
							} else if (lineObservationName == null || lineObservationName.equals("")) {
								addActionError(
										"The BTRIS Observation Name header was missing.  Please check your input file and try again.");
							} else if (lineTable == null || (lineTable != null && StringUtil.isBlank(lineTable))) {
								if (lineSpecimenType == null || lineSpecimenType.equals("")) {
									addActionError(
											"The BTRIS Specimen Type header was missing.  Please check your input file and try again.");
								} 									
							} else {
								
								// does this mapping already exist? If so, replace it
								DataElement de = getLatestDeByName(lineDeName);

								if (de != null) {

									BtrisMapping bm = new BtrisMapping();
									BtrisMapping alreadyMapped = null;
									if (lineTable == null || (lineTable != null && StringUtil.isBlank(lineTable))) {
										alreadyMapped =	btrisMappingManager.getBtrisMappingByName(de, lineObservationName,
														lineSpecimenType);
									} else {
										if (linePvValue != null && !StringUtil.isBlank(linePvValue)) {
											alreadyMapped = btrisMappingManager.getBtrisMappingByDeNameAndPv(de,
													linePvValue, lineObservationName, lineTable);
										} else {
											alreadyMapped = btrisMappingManager.getBtrisMappingSubjectByName(de,
													lineObservationName, lineTable);
										}
									}
									if (alreadyMapped != null) {
										bm = alreadyMapped;
									}

									bm.setBricsDataElement(de.getStructuralObject());
									bm.setBricsDataElementName(de.getName());
									ValueRange vr = btrisMappingManager.getValueRangeByDeAndPv(de, linePvValue);
									bm.setBricsValueRange(vr);
									bm.setBtrisObservationName(lineObservationName);
									bm.setBtrisRedCode(lineRedCode);
									bm.setBtrisSpecimenType(lineSpecimenType);
									bm.setBtrisTable(lineTable);
									btrisMappingList.add(bm);
								} else {
									addActionError("Could not find the data element with name " + lineDeName);
								}
							}
						}
						btrisMappingManager.saveBtrisMappingList(btrisMappingList);
					}
				}
			} catch (Exception e) {
				logger.error("Username: " + getAccount().getUserName()
						+ ".  Exception occurred while performing a BTRIS mapping");
				e.printStackTrace();
				addActionError(
						"Creating the mapping failed because of an error.  Please check your input file and try again.");
			} finally {
				try {
					if (reader != null)
						reader.close();
				} catch (IOException e) {
					logger.error("Username: " + getAccount().getUserName() + ".  Failed to close CSVReader");
					throw new IOException(e); // cant handle this, throw up
				}
			}
		}

		else {
			addActionError(
					"Invalid file type. If the file is open in another application please close it and try again.");
		}

		// if there are no errors, send up a success
		if (!this.hasActionErrors()) {
			this.addActionMessage("The BTRIS Mapping has been uploaded successfully");
		}
		return PortalConstants.ACTION_IMPORT_BTRIS_MAPPING;
	}

	private DataElement getLatestDeByName(String name) {
		de = btrisMappingManager.getLatestDeByName(name);
		return de;
	}

	private List<String> validateBtrisMappingFile(List<String[]> lines) {
		List<String> validationErrors = new ArrayList<String>();
		for (int i = 1; i < lines.size(); i++) {
			String[] line = lines.get(i);
			validationErrors.addAll(this.validateImportDataRow(line));
			if (validationErrors.size() > 20) {
				validationErrors.add(
						"More than 20 errors exist in your input document.  Please correct the above errors before continuing");
				break;
			}
		}
		return validationErrors;
	}

	public List<String> validateImportHeaderGetErrors(String[] headers) {
		List<String> errors = new ArrayList<String>();
		for (String header : headers) {
			String headerFormatted = header.trim();
			if (!headerFormatted.equalsIgnoreCase(ServiceConstants.BTRIS_MAPPING_BRICS_DE_SHORTNAME)
					&& !headerFormatted.equalsIgnoreCase(ServiceConstants.BTRIS_MAPPING_BRICS_PV_VALUE)
					&& !headerFormatted.equalsIgnoreCase(ServiceConstants.BTRIS_MAPPING_OBSERVATION_NAME)
					&& !headerFormatted.equalsIgnoreCase(ServiceConstants.BTRIS_MAPPING_RED_CONCEPT_CODE)
					&& !headerFormatted.equalsIgnoreCase(ServiceConstants.BTRIS_MAPPING_SPECIMEN_TYPE)
					&& !headerFormatted.equalsIgnoreCase(ServiceConstants.BTRIS_MAPPING_TABLE)) {

				errors.add("header " + header + " is not a part of the template");
			}
		}
		return errors;
	}

	public List<String> validateImportDataRow(String[] rowElements) {
		List<String> errors = new ArrayList<String>();

		// check that a data element and valuerange exists
		String lineDeName =
				getValueFromMappingByHeader(rowElements, ServiceConstants.BTRIS_MAPPING_BRICS_DE_SHORTNAME).trim();
		String linePvValue = getValueFromMappingByHeader(rowElements, ServiceConstants.BTRIS_MAPPING_BRICS_PV_VALUE).trim();
		String lineObservationName =
				getValueFromMappingByHeader(rowElements, ServiceConstants.BTRIS_MAPPING_OBSERVATION_NAME).trim();
		String lineRedCode = getValueFromMappingByHeader(rowElements, ServiceConstants.BTRIS_MAPPING_RED_CONCEPT_CODE).trim();
		String lineSpecimenType =
				getValueFromMappingByHeader(rowElements, ServiceConstants.BTRIS_MAPPING_SPECIMEN_TYPE).trim();
	
		String lineTable = getValueFromMappingByHeader(rowElements, ServiceConstants.BTRIS_MAPPING_TABLE).trim();

		DataElement latestDe = btrisMappingManager.getLatestDeByName(lineDeName);
		if (latestDe == null) {
			errors.add(String.format(ServiceConstants.ERROR_MISSING_DE, lineDeName));
		} else {
			// permissible values arn't required, but if one exists, we must check if it is a legit permissible value
			if (!StringUtils.isBlank(linePvValue)) {
				ValueRange vr = btrisMappingManager.getValueRangeByDeAndPv(latestDe, linePvValue);
				if (vr == null) {
					errors.add(String.format(ServiceConstants.ERROR_INVALID_PV, lineDeName, linePvValue));
				}
			}
			// Observation name is a required field
			if (StringUtils.isBlank(lineObservationName)) {
				errors.add(String.format(ServiceConstants.ERROR_BTRIS_OBSERVATION_NAME_MISSING));
			}
			// Specimen Type, Unit of Measue and Range are required fields for non-demographic data element in BTRIS
			if (StringUtils.isBlank(lineTable) || !lineTable.equalsIgnoreCase("Subject")) {
				if (StringUtils.isBlank(lineSpecimenType)) {
					errors.add(String.format(ServiceConstants.ERROR_BTRIS_SPECIMEN_TYPE));
				}

			}
			/* check duplicates */
			String rowHash = generateRowHash(rowElements);
			if (enteredRows.containsKey(rowHash)) {
				errors.add(String.format(ServiceConstants.ERROR_DUPLICATE_OBSERVATION_NAME, lineDeName,
						lineObservationName));
			} else {
				// already exists, don't bother adding again
				enteredRows.put(rowHash, lineObservationName);
			}
		}
		return errors;
	}


	private void initializeHeaders(String[] headers) {
		// the headers could be out of order but still valid, so handle that
		headersMap = new HashMap<String, Integer>();
		for (int i = 0; i < headers.length; i++) {
			headersMap.put(headers[i].toLowerCase().trim(), i);
		}
	}

	public String getValueFromMappingByHeader(String[] line, String headerName) {
		// The different cases in header should not affect
		Integer mappingIndex = headersMap.get(headerName.toLowerCase());

		if (mappingIndex == null || line.length <= mappingIndex) {
			return null;
		} else {
			return line[mappingIndex].trim();
		}
	}

	private String generateRowHash(String[] rowElements) {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < rowElements.length; i++) {
			String currentElement = rowElements[i];
			sb.append(currentElement).append("_");
		}

		if (sb.length() > 0) {
			sb.replace(sb.length() - 1, sb.length(), ServiceConstants.EMPTY_STRING);
		}

		return sb.toString();
	}

	public String getDeShortName() {
		return deShortName;
	}

	public void setDeShortName(String deShortName) {
		this.deShortName = deShortName;
	}

	public DataElement getDe() {
		return de;
	}

	public void setDe(DataElement de) {
		this.de = de;
	}

	public File getUpload() {
		return upload;
	}

	public void setUpload(File upload) {
		this.upload = upload;
	}

	public String getUploadContentType() {
		return uploadContentType;
	}

	public void setUploadContentType(String uploadContentType) {
		this.uploadContentType = uploadContentType;
	}
}
