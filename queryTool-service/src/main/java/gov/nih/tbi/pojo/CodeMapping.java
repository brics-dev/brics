package gov.nih.tbi.pojo;

import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.exceptions.CodeMappingException;
import gov.nih.tbi.repository.model.CellValueCode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CodeMapping implements Serializable {
	private static final long serialVersionUID = -4530444775608769445L;

	// map of data element -> map of permissible value name -> value range object
	// Map<Data Element Name, Map<Permissible Value Name, ValueRange>>
	private Map<String, Map<String, ValueRange>> deValueRangeMap;

	public Map<String, Map<String, ValueRange>> getDeValueRangeMap() {
		return deValueRangeMap;
	}

	public void setDeValueRangeMap(Map<String, Map<String, ValueRange>> deValueRangeMap) {
		this.deValueRangeMap = deValueRangeMap;
	}

	public CodeMapping() {

	}

	public CodeMapping(Map<String, Map<String, ValueRange>> deValueRangeMap) {
		super();
		this.deValueRangeMap = deValueRangeMap;
	}

	/**
	 * Return the output code in string of the given permissible value from the given data element
	 * 
	 * @param dataElementName
	 * @param deValue - a single data element value
	 * @return - Returns an output code or empty string. Will never return null.
	 */
	private String getSingleSelectOutputCode(String dataElementName, String deValue) {
		Map<String, ValueRange> currentPermissibleValueMap = deValueRangeMap.get(dataElementName);

		Integer outputCode = null;

		if (currentPermissibleValueMap != null) {
			ValueRange currentValueRange = currentPermissibleValueMap.get(deValue.toUpperCase());

			if (currentValueRange != null) {
				outputCode = currentValueRange.getOutputCode();
			}
		}

		// we want the output code to be an empty string if it does not exist
		return outputCode != null ? outputCode.toString() : QueryToolConstants.EMPTY_STRING;
	}

	/**
	 * Return a semi-colon delimited list of the output codes for the given data element and data element values
	 * 
	 * @param dataElementName
	 * @param deValues - semi-colon delimited list of values of the data element. (e.g. "pv1;pv2;...;pvN")
	 * @return Returns output code or empty string. Will never return null.
	 */
	private String getMultiSelectOutputCode(String dataElementName, String deValues) {
		List<String> permissibleValueList = BRICSStringUtils.delimitedStringToList(deValues, ";");

		StringBuilder sb = new StringBuilder();

		for (String currentPermissibleValue : permissibleValueList) {
			String currentOutputCode = getSingleSelectOutputCode(dataElementName, currentPermissibleValue);

			if (!currentOutputCode.isEmpty()) {
				sb.append(currentOutputCode).append(";");
			}
		}


		if (sb.length() > 0) { // if string builder has content, we will need to remove the trailing delimiter character
			return sb.substring(0, sb.length() - 1);
		} else {
			return QueryToolConstants.EMPTY_STRING;
		}
	}

	/**
	 * Given a data element and a data element value of a semi-colon delimited list of data element values, return the
	 * output code(s) of the data
	 * 
	 * @param dataElement
	 * @param permissibleValue - This can be a single data element value or a semi-colon delimited list of data element
	 *        values for multiple pre-select data elements
	 * @return Returns output code or empty string. Will never return null.
	 */
	public String getOutputCode(DataElement dataElement, String permissibleValue) {
		String dataElementName = dataElement.getName();
		InputRestrictions inputRestriction = dataElement.getInputRestrictions();

		switch (inputRestriction) {
			case SINGLE:
				return getSingleSelectOutputCode(dataElementName, permissibleValue);
			case MULTIPLE:
				return getMultiSelectOutputCode(dataElementName, permissibleValue);
			case FREE_FORM:
				return getSingleSelectOutputCode(dataElementName, permissibleValue);
			default:
				throw new CodeMappingException("Data element has an unhandled input restriction");

		}
	}

	/**
	 * Given a data element and a semi-colon delimited list of values, return a hash map of schema's to its permissible
	 * value for the schema
	 * 
	 * @param dataElement
	 * @param deValues - semi-colon delimited list of values of the data element. (e.g. "pv1;pv2;...;pvN")
	 * @return
	 */
	private Map<String, String> buildMultiSelectSchemaPvMap(DataElement dataElement, String deValues) {
		List<String> permissibleValueList = BRICSStringUtils.delimitedStringToList(deValues, ";");
		Map<String, String> multiSchemaPvMap = new HashMap<String, String>();


		for (String currentPermissibleValue : permissibleValueList) {
			Map<String, String> singleSchemaPvMap = buildSingleSelectSchemaPvMap(dataElement, currentPermissibleValue);

			for (Entry<String, String> schemaPvEntry : singleSchemaPvMap.entrySet()) {
				String schemaName = schemaPvEntry.getKey();
				String schemaPv = schemaPvEntry.getValue();
				String currentMultipleSchemaPv = multiSchemaPvMap.get(schemaName);

				if (currentMultipleSchemaPv == null) {
					currentMultipleSchemaPv = QueryToolConstants.EMPTY_STRING;
				}

				if (schemaPv != null) {
					currentMultipleSchemaPv += schemaPv + ";";
					multiSchemaPvMap.put(schemaName, currentMultipleSchemaPv);
				}
			}
		}

		// if currentSchemaPv has content, we will need to remove the trailing delimiter character
		for (Entry<String, String> currentSchemaPvEntry : multiSchemaPvMap.entrySet()) {
			String schemaName = currentSchemaPvEntry.getKey();
			String schemeaPv = currentSchemaPvEntry.getValue();

			if (!schemeaPv.isEmpty()) {
				schemeaPv = schemeaPv.substring(0, schemeaPv.length() - 1);
				multiSchemaPvMap.put(schemaName, schemeaPv);
			}
		}

		return multiSchemaPvMap;
	}

	/**
	 * Given a data element and a single permissible value, return a hash map of schema's to its permissible value for
	 * the schema
	 * 
	 * @param dataElement
	 * @param permissibleValue - a single permissible value
	 * @return (e.g. ["CDISC" -> "PV1", "LOINC" -> "PV2", ...]
	 */
	private Map<String, String> buildSingleSelectSchemaPvMap(DataElement dataElement, String permissibleValue) {
		Map<String, ValueRange> valueRangeMap = deValueRangeMap.get(dataElement.getName());

		if (valueRangeMap == null) {
			return new HashMap<String, String>();
		}

		ValueRange valueRange = valueRangeMap.get(permissibleValue.toUpperCase());

		if (valueRange == null) {
			return new HashMap<String, String>();
		}

		return valueRange.getSchemaPvMap();
	}

	/**
	 * Given a data element and a data element value, return a hash map of schema's to its permissible value for the
	 * schema
	 * 
	 * @param dataElement
	 * @param deValue - This can be a single data element value or a semi-colon delimited list of data element values
	 *        for multiple pre-select data elements
	 * @return
	 */
	private Map<String, String> getSchemaPvMap(DataElement dataElement, String deValue) {
		switch (dataElement.getInputRestrictions()) {
			case SINGLE:
				return buildSingleSelectSchemaPvMap(dataElement, deValue);
			case MULTIPLE:
				return buildMultiSelectSchemaPvMap(dataElement, deValue);
			case FREE_FORM:
				// there can be multi-select free-form data elements. In this case, we want to treat the data element
				// as a multi-select.
				if (dataElement.getPermissibleValues() != null && !dataElement.getPermissibleValues().isEmpty()) {
					return buildMultiSelectSchemaPvMap(dataElement, deValue);
				} else {
					return buildSingleSelectSchemaPvMap(dataElement, deValue);
				}
			default:
				throw new CodeMappingException("Data element has an unhandled input restriction");
		}
	}

	/**
	 * This method returns CellValueCode object for the given data element.
	 * 
	 * @param de - data element
	 * @param deValue - data element value, it could be concatenated for multiple selection
	 * @return CellValueCode object for the given data element.
	 */
	public CellValueCode getCellValueCode(DataElement de, String deValue) {
		boolean hasValueRange = de.hasPermissibleValues();

		if (deValueRangeMap == null || !hasValueRange) {
			return new CellValueCode(deValue, QueryToolConstants.EMPTY_STRING, hasValueRange,
					new HashMap<String, String>());
		}

		String outputCode = getOutputCode(de, deValue);

		// map of schema name to it's permissible value
		Map<String, String> schemaOutputCodeMap = getSchemaPvMap(de, deValue);

		return new CellValueCode(deValue, outputCode, hasValueRange, schemaOutputCodeMap);
	}
}
