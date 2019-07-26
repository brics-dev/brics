package gov.nih.tbi.dictionary.portal.util;

import gov.nih.tbi.commons.service.SchemaMappingManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class SchemaMappingUtil {
	private SchemaMappingManager schemaMappingManager;
	private List<String> allSchemas = new ArrayList<String>();
	private Map<String, Integer> headersMap;
	// used for validation case where we check that no two rows are the same (based on
	// criteria listed in validateImportDataRow
	private HashMap<String, String> enteredRows;

	// This is no longer needed (6/26/2017)
	// private HashMap<String, String> schemaDeIdMappings;

	public SchemaMappingUtil(SchemaMappingManager smm, String[] headers) {
		schemaMappingManager = smm;

		// initialize
		enteredRows = new HashMap<String, String>();
		// schemaDeIdMappings = new HashMap<String, String>();
		getListOfSchemas();
		initializeHeaders(headers);
	}

	/**
	 * Checks the input file headers to ensure they match the list of headers we accept
	 * 
	 * @param headers array of headers to validate
	 * @return empty list or filled list of error strings
	 */
	public List<String> validateImportHeaderGetErrors(String[] headers) {
		List<String> errors = new ArrayList<String>();
		for (String header : headers) {
			String headerFormatted = header.trim();
			if (!headerFormatted.equalsIgnoreCase(ServiceConstants.SCHEMA_MAPPING_BRICS_DE_PV)
					&& !headerFormatted.equalsIgnoreCase(ServiceConstants.SCHEMA_MAPPING_BRICS_DE_SHORTNAME)
					&& !headerFormatted.equalsIgnoreCase(ServiceConstants.SCHEMA_MAPPING_SCHEMA_DE_ID)
					&& !headerFormatted.equalsIgnoreCase(ServiceConstants.SCHEMA_MAPPING_SCHEMA_PV_ID)
					&& !headerFormatted.equalsIgnoreCase(ServiceConstants.SCHEMA_MAPPING_SCHEMA_DE_NAME)
					&& !headerFormatted.equalsIgnoreCase(ServiceConstants.SCHEMA_MAPPING_SCHEMA_SYSTEM)
					&& !headerFormatted.equalsIgnoreCase(ServiceConstants.SCHEMA_MAPPING_SCHEMA_PV_VALUE)) {

				errors.add("header " + header + " is not a part of the template");
			}
		}
		return errors;
	}

	/**
	 * Performs validations on an individual row of data from the schema mapping file
	 * 
	 * validations: ensure schema system is in the list of schemas ensure a data element only has one scheme DE ID per
	 * schema per DE ensure a ValueRange exists for a given data element and PV value Each schema permissible value must
	 * be unique for each DE Permissible Value After discussion with Erica, this means: The same BRIC-side PV must not
	 * ever have two different Schema PVs. However, it is okay for the same Schema PV to be mapped to two different
	 * BRICS PVs. For example: BRICS DE PV Schema PV Male 0 Female 1 Unknown 5 Unreported 5
	 *
	 * The above is okay because the same Schema PV has two BRICS PVs. The below is NOT okay because one BRICS PV is
	 * mapped to multiple Schema PVs Male 0 Male 1 Female 1 Unknown 5 Unreported 5
	 * 
	 * @param headers the leaders row
	 * @param rowElements variables in the row
	 * @return list of errors or empty list if no errors
	 */
	public List<String> validateImportDataRow(String[] rowElements) {
		List<String> errors = new ArrayList<String>();
		String lineSchemaName = getValueFromMappingByHeader(rowElements, ServiceConstants.SCHEMA_MAPPING_SCHEMA_SYSTEM);

		// check the schema system
		if (!allSchemas.contains(lineSchemaName)) {
			errors.add(String.format(ServiceConstants.ERROR_INVALID_SCHEMA_SYSTEM, lineSchemaName));
		}

		// check that a data element and valuerange exists
		String lineDeName =
				getValueFromMappingByHeader(rowElements, ServiceConstants.SCHEMA_MAPPING_BRICS_DE_SHORTNAME);
		String linePvValue = getValueFromMappingByHeader(rowElements, ServiceConstants.SCHEMA_MAPPING_BRICS_DE_PV);
		String lineSchemaDeName =
				getValueFromMappingByHeader(rowElements, ServiceConstants.SCHEMA_MAPPING_SCHEMA_DE_NAME);
		String lineSchemaPv = getValueFromMappingByHeader(rowElements, ServiceConstants.SCHEMA_MAPPING_SCHEMA_PV_VALUE);
		String lineSchemaDeId = getValueFromMappingByHeader(rowElements, ServiceConstants.SCHEMA_MAPPING_SCHEMA_DE_ID);

		DataElement latestDe = schemaMappingManager.getLatestDeByName(lineDeName);
		if (latestDe == null) {
			errors.add(String.format(ServiceConstants.ERROR_MISSING_DE, lineDeName));
		} else {
			// permissible values arn't required, but if one exists, we must check if it is a legit permissible value
			if (!StringUtils.isBlank(linePvValue)) {
				ValueRange vr = schemaMappingManager.getValueRangeByDeAndPv(latestDe, linePvValue);
				if (vr == null) {
					errors.add(String.format(ServiceConstants.ERROR_INVALID_PV, lineDeName, linePvValue));
				}
			}

			// Schema DE Name is a required field
			if (StringUtils.isBlank(lineSchemaDeName)) {
				errors.add(String.format(ServiceConstants.ERROR_SCHEMA_DE_NAME_MISSING, lineDeName));

				// Schema DE Name must not exceed 50 characters
			} else if (lineSchemaDeName.length() > ServiceConstants.SCHEMA_DE_NAME_LIMIT) {
				errors.add(String.format(ServiceConstants.ERROR_SCHEMA_DE_CHARACTER_LIMIT, lineDeName));
			}

			// (6/26/2017) removed this check at the request of olga
			// check the schema system DE ID
			// String schemaDeIdHash = generateSchemaDeIdHash(rowElements);
			// if (schemaDeIdMappings.containsKey(schemaDeIdHash)) {
			// String storedDeSchemaId = schemaDeIdMappings.get(schemaDeIdHash);
			// if (!storedDeSchemaId.equals(lineSchemaDeId)) {
			// errors.add("The Schema Data Element ID " + lineSchemaDeId + " for data element " + lineDeName
			// + " does not match the one already used by the system: " + storedDeSchemaId
			// + ". These must be consistent.");
			// }
			// } else {
			// // don't get the version from the db here - because we may update the DE later
			// schemaDeIdMappings.put(schemaDeIdHash, lineSchemaDeId);
			// }
			/////////////////////////////////////////////////////////////////////////////////
			
			
			
			// Each schema permissible value must be unique for each DE Permissible Value
			// We add these to a HashMap because it has a high probability of O(1) search time
			// AND can provide the previous mapped PV.
			// this only checks the current upload operation
			String rowHash = generateRowHash(rowElements);
			if (enteredRows.containsKey(rowHash)) {
				String previousPv = enteredRows.get(rowHash);
				if (!StringUtils.isBlank(previousPv)) {
					errors.add(String.format(ServiceConstants.ERROR_DUPLICATE_SCHEMA_ID_WITH_PV, lineDeName,
							linePvValue, lineSchemaName, previousPv, lineSchemaDeId));
				} else {
					errors.add(String.format(ServiceConstants.ERROR_DUPLICATE_SCHEMA_ID_WITHOUT_PV, lineDeName,
							linePvValue, lineSchemaName, lineSchemaDeId));
				}
			} else {
				// already exists, don't bother adding again
				enteredRows.put(rowHash, lineSchemaPv);
			}
		}
		return errors;
	}

	private void getListOfSchemas() {
		List<Schema> allSchemaObjs = schemaMappingManager.getAllSchemas();
		for (Schema scheme : allSchemaObjs) {
			allSchemas.add(scheme.getName());
		}
	}

	private void initializeHeaders(String[] headers) {
		// the headers could be out of order but still valid, so handle that
		headersMap = new HashMap<String, Integer>();
		for (int i = 0; i < headers.length; i++) {
			// I do toLowerCase here so we don't have to worry with
			// case-sensitive matching later
			// when we look for headers by string name
			headersMap.put(headers[i].toLowerCase().trim(), i);
		}
	}

	public String getValueFromMappingByHeader(String[] line, String headerName) {
		// The different cases in header should not affect
		Integer mappingIndex = headersMap.get(headerName.toLowerCase());

		if (mappingIndex == null || line.length <= mappingIndex) {
			return null;
		} else {
			return line[mappingIndex];
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

	private String generateSchemaDeIdHash(String[] rowElements) {
		String lineDeName =
				getValueFromMappingByHeader(rowElements, ServiceConstants.SCHEMA_MAPPING_BRICS_DE_SHORTNAME);
		String lineSchemaSystem =
				getValueFromMappingByHeader(rowElements, ServiceConstants.SCHEMA_MAPPING_SCHEMA_SYSTEM);
		return lineDeName + "_" + lineSchemaSystem;
	}
}
