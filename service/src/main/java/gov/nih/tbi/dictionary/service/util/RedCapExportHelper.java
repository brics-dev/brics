package gov.nih.tbi.dictionary.service.util;

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import au.com.bytecode.opencsv.CSVWriter;

public class RedCapExportHelper {

	// The first Original Name column is deleted before importing into RECAP
	public static final String ORIGINAL_NAME = "Original Name (Delete before importing into REDCAP)";
	public static final String VARIABLE_NAME = "Variable / Field Name";
	public static final String FORM_NAME = "Form Name";
	public static final String SECTION_HEADER = "Section Header";
	public static final String FIELD_TYPE = "Field Type";
	public static final String FIELD_LABEL = "Field Label";
	public static final String CHOICES_OR_CALCULATIONS = "Choices OR Calculations";
	public static final String FIELD_NOTE = "Field Note";
	public static final String TEXT_VALIDATION = "Text Validation";
	public static final String TEXT_VALIDATION_MIN = "Text Validation Min";
	public static final String TEXT_VALIDATION_MAX = "Text Validation Max";
	public static final String IDENTIFIER = "Identifier?";
	public static final String BRANCHING_LOGIC = "Branching Logic";
	public static final String REQUIRED_FIELD = "Required Field?";
	public static final String CUSTOM_ALIGNMENT = "Custom Alignment";
	public static final String QUESTION_NUMBER = "Question Number";
	public static final String MATRIX_GROUP_NAME = "Matrix Group Name";
	public static final String MATRIX_RANKING = "Matrix Ranking?";
	public static final String FIELD_ANNOTATION = "Field Annotation";

	// This map stores the REDCap CSV Column headers and the column indexes.
	public static final Map<String, Integer> REDCAP_CSV_HEADERS;
	static {
		REDCAP_CSV_HEADERS = new LinkedHashMap<String, Integer>();

		REDCAP_CSV_HEADERS.put(VARIABLE_NAME, 0);
		REDCAP_CSV_HEADERS.put(FORM_NAME, 1);
		REDCAP_CSV_HEADERS.put(SECTION_HEADER, 2);
		REDCAP_CSV_HEADERS.put(FIELD_TYPE, 3);
		REDCAP_CSV_HEADERS.put(FIELD_LABEL, 4);
		REDCAP_CSV_HEADERS.put(CHOICES_OR_CALCULATIONS, 5);
		REDCAP_CSV_HEADERS.put(FIELD_NOTE, 6);
		REDCAP_CSV_HEADERS.put(TEXT_VALIDATION, 7);
		REDCAP_CSV_HEADERS.put(TEXT_VALIDATION_MIN, 8);
		REDCAP_CSV_HEADERS.put(TEXT_VALIDATION_MAX, 9);
		REDCAP_CSV_HEADERS.put(IDENTIFIER, 10);
		REDCAP_CSV_HEADERS.put(BRANCHING_LOGIC, 11);
		REDCAP_CSV_HEADERS.put(REQUIRED_FIELD, 12);
		REDCAP_CSV_HEADERS.put(CUSTOM_ALIGNMENT, 13);
		REDCAP_CSV_HEADERS.put(QUESTION_NUMBER, 14);
		REDCAP_CSV_HEADERS.put(MATRIX_GROUP_NAME, 15);
		REDCAP_CSV_HEADERS.put(MATRIX_RANKING, 16);
		REDCAP_CSV_HEADERS.put(FIELD_ANNOTATION, 17);
	}


	/**
	 * This method is called from Search DataElement page, it converts a list of DataElement objects to REDCap format
	 * and writes it to ByteArrayOutputStream.
	 * 
	 * @param elementList - a list of DataElement objects
	 * @return ByteArrayOutputStream
	 * @throws IOException
	 */
	public static ByteArrayOutputStream exportDEListToRedCapCsv(List<DataElement> elementList) throws IOException {

		if (elementList == null) {
			throw new NullPointerException("Data Element List cannot be null");
		}

		// create new writer
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(baos));

		// write headers
		String[] redCapHeaderArr = new String[REDCAP_CSV_HEADERS.size()];
		writer.writeNext(REDCAP_CSV_HEADERS.keySet().toArray(redCapHeaderArr));

		// write each DE into a row
		for (DataElement de : elementList) {
			if (de != null) {
				String[] row = new String[REDCAP_CSV_HEADERS.size()];
				String endString = "0";
				writeDataElementToRedCap(de, row, writer, endString);
			}
		}

		writer.close();
		return baos;
	}


	/**
	 * This method is called from Form Structure Data Element Report page, it converts a map of DataElement objects to
	 * REDCap format and writes it to ByteArrayOutputStream.
	 * 
	 * @param formName - name of the form structure
	 * @param groupDEStringList - a list of group names
	 * @param groupDEDataElementList - a list of data elements
	 * @param groupDEDataRequiredList - a list of required status for data elements
	 * @param groupDEThresholdList - a list of the number of repeats for each group
	 * @return ByteArrayOutputStream
	 * @throws IOException
	 */
	public static ByteArrayOutputStream exportDEDetailsToRedCapCsv(String formName,
			List<String> groupDEStringList,
			List<List<DataElement>> groupDEDataElementList,
			List<List<Boolean>> groupDEDataRequiredList,
			List<Integer> groupDEThresholdList) throws IOException {

		if (groupDEStringList == null) {
			throw new NullPointerException("groupDEStringList cannot be null");
		}
		
		if (groupDEDataElementList == null) {
			throw new NullPointerException("groupDEDataElementList cannot be null");
		}
		
		if (groupDEDataRequiredList == null) {
			throw new NullPointerException("groupDEDataRequiredList cannot be null");
		}
		
		if (groupDEThresholdList == null) {
			throw new NullPointerException("groupDEThresholdList cannot be null");
		}

		// create new writer
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(baos));

		// write headers
		String[] redCapHeaderArr = new String[REDCAP_CSV_HEADERS.size()];
		writer.writeNext(REDCAP_CSV_HEADERS.keySet().toArray(redCapHeaderArr));
		
		// write each DE into a row
		for (int i = 0; i < groupDEStringList.size(); i++) {
			String group = groupDEStringList.get(i);
			int requestedRepeats = groupDEThresholdList.get(i);
			int maxRepeats;
			if (group.trim().equalsIgnoreCase("Form Administration")) {
				maxRepeats = 1;
			}
			else {
				maxRepeats = 10;
			}
			if (requestedRepeats == 0) {
				requestedRepeats = maxRepeats;
			}
			int actualRepeats = Math.max(1, Math.min(maxRepeats, requestedRepeats));
			List<DataElement> deList = groupDEDataElementList.get(i);
			List<Boolean> deRequiredList = groupDEDataRequiredList.get(i);
			for (int j = 0; j < actualRepeats; j++) {
				for (int dataElementNumber = 1; dataElementNumber <= deList.size(); dataElementNumber++) {
					DataElement de = deList.get(dataElementNumber-1);
					String endString;
					if (actualRepeats == 1) {
						endString = "0";
					}
					else {
						endString = String.valueOf(j+1);
					}
					
					boolean deRequired = deRequiredList.get(dataElementNumber-1);
					if (de != null) {
						String[] row = new String[REDCAP_CSV_HEADERS.size()];
						row[REDCAP_CSV_HEADERS.get(FORM_NAME)] = formName;
						if ((dataElementNumber == 1) && ((!group.trim().equalsIgnoreCase("Main")) &&
								(!group.trim().equalsIgnoreCase("Main Group")))) {
							if (!endString.equals("0")) {
								row[REDCAP_CSV_HEADERS.get(SECTION_HEADER)] = group + endString;	
							}
							else {
								row[REDCAP_CSV_HEADERS.get(SECTION_HEADER)] = group;	
							}
						}
						else if ((dataElementNumber == 2) && ((group.trim().equalsIgnoreCase("Main")) || 
							(group.trim().equalsIgnoreCase("Main Group")))) {
							if (!endString.equals("0")) {
								row[REDCAP_CSV_HEADERS.get(SECTION_HEADER)] = group + endString;	
							}
							else {
								row[REDCAP_CSV_HEADERS.get(SECTION_HEADER)] = group;	
							}
						}
						else {
							row[REDCAP_CSV_HEADERS.get(SECTION_HEADER)] = " ";
						}
						if (deRequired) {
							row[REDCAP_CSV_HEADERS.get(REQUIRED_FIELD)] = "Y";
						}
						writeDataElementToRedCap(de, row, writer, endString);
					}
				}
			}
		}

		writer.close();
		return baos;
	}


	/**
	 * This method converts one data element to REDCap row and writes to output stream.
	 * 
	 * @param de - DataElement object
	 * @param row - array of the strings that map to excel cell values.
	 * @param writer - CSVWriter
	 * @param endString - number at end if repeated group
	 */
	public static void writeDataElementToRedCap(DataElement de, String[] row, CSVWriter writer, String endString) {
        int i;
        int key;
        String value;
		row[REDCAP_CSV_HEADERS.get(VARIABLE_NAME)] = adjustName(de.getName(), endString);
		row[REDCAP_CSV_HEADERS.get(FIELD_LABEL)] = de.getTitle();

		ArrayList<keyValueItem> redcapPVList = parsePermissbleValues(de.getValueRangeList());

		if (redcapPVList != null && !redcapPVList.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (i = 0; i < redcapPVList.size(); i++) {
				key = redcapPVList.get(i).getKey();
				value = redcapPVList.get(i).getValue();
				sb.append(key).append(", ").append(value).append(" | ");
			}
			row[REDCAP_CSV_HEADERS.get(CHOICES_OR_CALCULATIONS)] = sb.toString().substring(0, sb.length() - 2);
		}

		row[REDCAP_CSV_HEADERS.get(FIELD_NOTE)] = de.getMeasuringUnit() != null ? de.getMeasuringUnit().getName() : "";

		// Check if we need to create a branch row for Other Specify permissible value
		ValueRange branchingPV = getBranchingValueRange(de);
		boolean branchingNeeded = (branchingPV != null);

		if (branchingNeeded) {
			row[REDCAP_CSV_HEADERS.get(FIELD_TYPE)] = "dropdown";

		} else {
			row[REDCAP_CSV_HEADERS.get(FIELD_TYPE)] = calculateFieldType(de);

			// Calculate Text Validation
			if (de.getType() == DataType.DATE) {
				if (de.getName().contains("DateTime")) {
					row[REDCAP_CSV_HEADERS.get(FIELD_NOTE)] = "YYYY-MM-DD + HH:MD Military Time";
					row[REDCAP_CSV_HEADERS.get(TEXT_VALIDATION)] = "datetime_ymd";
				} else {
					row[REDCAP_CSV_HEADERS.get(FIELD_NOTE)] = "YYYY-MM-DD";
					row[REDCAP_CSV_HEADERS.get(TEXT_VALIDATION)] = "date_ymd";
				}

			} else if (de.getType() == DataType.NUMERIC && de.getRestrictions() == InputRestrictions.FREE_FORM) {
				row[REDCAP_CSV_HEADERS.get(TEXT_VALIDATION)] = "number";

			}

			row[REDCAP_CSV_HEADERS.get(TEXT_VALIDATION_MIN)] =
					de.getMinimumValue() != null ? de.getMinimumValue().toString() : "";
			row[REDCAP_CSV_HEADERS.get(TEXT_VALIDATION_MAX)] =
					de.getMaximumValue() != null ? de.getMaximumValue().toString() : "";
		}

		writer.writeNext(row);

		// Create another row for Other, specify permissible value with field type = text.
		if (branchingNeeded) {
			String[] brow = new String[REDCAP_CSV_HEADERS.size()];

			brow[REDCAP_CSV_HEADERS.get(VARIABLE_NAME)] = adjustBranchingName(de.getName(), endString);
			brow[REDCAP_CSV_HEADERS.get(FIELD_TYPE)] = "text";
			brow[REDCAP_CSV_HEADERS.get(FIELD_NOTE)] =
					de.getMeasuringUnit() != null ? de.getMeasuringUnit().getName() : "";

			// If permissible value of Other, specify is not an integer, we will get the REDCap key of the
			int otherKey = -1;
			String otherValue = branchingPV.getValueRange();
			boolean isOtherValueInteger = true;
			try {
				otherKey = Integer.parseInt(otherValue);
			} catch (NumberFormatException e) {
				isOtherValueInteger = false;
			}

			if (!isOtherValueInteger) {
				for (i = 0; i < redcapPVList.size(); i++) {
					key = redcapPVList.get(i).getKey();
					value = redcapPVList.get(i).getValue();
					if (value.contains(otherValue)) {
					    otherKey = key;
					    break;
					}
				}
			}

			brow[REDCAP_CSV_HEADERS.get(BRANCHING_LOGIC)] = calculateBranchingLogic(de, otherKey, endString);

			writer.writeNext(brow);
		}
	}

	/**
	 * Variable names must be truncated to 26 characters in the REDCap format. Also, the variable names must be in all
	 * lower case.
	 * 
	 * @param name
	 * @param number at end if repeatable group
	 * @return adjusted name for display
	 */
	private static String adjustName(String name, String endString) {

		String output = name.trim().toLowerCase();
		if (!endString.equals("0")) {
			if (output.length() > 26 - endString.length()) {
				output = output.substring(0, 26 - endString.length());
			}
			output = output + endString;
		}
		else {
			if (output.length() > 26) {
				output = output.substring(0, 26);
			}	
		}

		return output;
	}

	/**
	 * Variable names must be truncated to 26 characters in the REDCap format. Also, the variable names must be in all
	 * lower case.
	 * 
	 * @param name
	 * @param endString number at end if repeatable group
	 * @return adjusted name for display
	 */
	private static String adjustBranchingName(String name, String endString) {

		String output = name.trim().toLowerCase();
		if (!endString.equals("0")) {
			if (output.length() > 23 - endString.length()) {
				output = output.substring(0, 23 - endString.length());
			}
	
			return output + "oth" + endString;
		}
		else {
			if (output.length() > 23) {
				output = output.substring(0, 23);
			}
	
			return output + "oth";
			
		}
	}

	/**
	 * Branching is needed if a field labeled as a Free-form Entry is present with an option for
	 * Other, specify, in which case the field is split into a dropdown and a text field.
	 * 
	 * @param de - current DataElement
	 * @return - VCaluerange object of the permissible value that we need to create a split row
	 */
	private static ValueRange getBranchingValueRange(DataElement de) {

		if (de.getRestrictions() != InputRestrictions.FREE_FORM) {
			return null;
		}

		Set<ValueRange> permissibleValues = de.getValueRangeList();
		if (permissibleValues != null && !permissibleValues.isEmpty()) {

			for (ValueRange pv : permissibleValues) {
				if (pv.getDescription() != null
						&& (pv.getDescription().toLowerCase().contains("other, specify") || pv.getValueRange()
								.toLowerCase().contains("other, specify"))) {
					return pv;
				}
			}
		}

		return null;
	}


	/**
	 * Set column Branching Logic to [Variable Name] = "REDCap key of Other, specify permissible value"
	 * 
	 * @param de - current DataElement
	 * @param value - REDCap key of Other, specify
	 * @param endString number at end if repeatable group
	 * 
	 * @return value for Branching Logic column
	 */
	private static String calculateBranchingLogic(DataElement de, int otherKey, String endString) {

		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(adjustName(de.getName(), endString));
		sb.append("] = \"");
		sb.append(otherKey);
		sb.append("\"");

		return sb.toString();
	}


	/**
	 * Based on what format the variable is in the CDE, choose a REDCap format.
	 * 
	 * @param dataElement - current DataElement
	 * @return value of Field Type column
	 */
	private static String calculateFieldType(DataElement dataElement) {

		String fieldType = "N/A";
		InputRestrictions restrictions = dataElement.getRestrictions();

		if (restrictions == InputRestrictions.FREE_FORM) {
			Integer maxSize = dataElement.getSize();
			fieldType = (maxSize == null || maxSize == 255) ? "text" : "notes";
		}

		else if (restrictions == InputRestrictions.SINGLE) {
			Set<ValueRange> permissibleValues = dataElement.getValueRangeList();

			// dropdown: Mapped to Single Pre-Defined Value Selected
			// radio: Mapped to Single Pre-Defined Value Selected, but only when not
			// every single permissible value has an associated description label.

			if (permissibleValues != null && !permissibleValues.isEmpty()) {
				fieldType = "dropdown";

				for (ValueRange pv : permissibleValues) {
					if (pv.getDescription() == null || pv.getDescription().isEmpty()) {
						fieldType = "radio";
						break;
					}
				}
			}
		} else if (restrictions == InputRestrictions.MULTIPLE) {
			fieldType = "checkbox";
		}

		return fieldType;
	}


	/**
	 * Parse through the Permissible Values and covert into REDCap friendly form. Also makes a few consistency
	 * modifications for Yes/No selections
	 * 
	 * @param de - current DataElement
	 * @param row - String array that maps to the current csv row
	 * @return TreeMap of REDCap key to permissible value
	 */
	private static ArrayList<keyValueItem> parsePermissbleValues(Set<ValueRange> pvs) {

		if (pvs == null || pvs.isEmpty()) {
			return null;
		}

		ArrayList<keyValueItem> redcapPVList = new ArrayList<keyValueItem>();

		int size = pvs.size();
		String values[] = new String[size];
		String descs[] = new String[size];
		Integer outputCodes[] = new Integer[size];
		boolean valuesSameAsDescriptions = true;

		boolean allIntegers = true;
		int numpvNotNull = 0;
		for (ValueRange pv : pvs) {
			if (pv != null) {
				values[numpvNotNull] = pv.getValueRange();
				descs[numpvNotNull] = pv.getDescription();
				if (!values[numpvNotNull].equals(descs[numpvNotNull])) {
					valuesSameAsDescriptions = false;
				}
				outputCodes[numpvNotNull] = pv.getOutputCode();

				if (allIntegers) {
					try {
						Integer.parseInt(pv.getValueRange());
					} catch (NumberFormatException e) {
						allIntegers = false;
					}
				}
				numpvNotNull++;
			} // if (pv != null)
		}
		
		
	    int k = 1;
		for (int j = 0; j < numpvNotNull; j++) {
	        if ((outputCodes[j] != null)  && (outputCodes[j] >= -10000) && (outputCodes[j] <= 10000)) {
	        	if (valuesSameAsDescriptions || (descs[j] == null) || (descs[j].isEmpty())) {
	        		redcapPVList.add(new keyValueItem(outputCodes[j], values[j]));   	
	        	} // if (valuesSameAsDescriptions || (descs[j] == null) || (descs[j].isEmpty()))
	        	else {
	        		redcapPVList.add(new keyValueItem(outputCodes[j], values[j] + " (" + descs[j] + ")"));		
	        	}
	        } // if ((outputCodes[j] != null)  && (outputCodes[j] >= -10000) && (outputCodes[j] <= 10000))
	        else if (allIntegers) {
	        	redcapPVList.add(new keyValueItem(Integer.parseInt(values[j]), descs[j]));	
	        } // else if (allIntegers)
	        else if (valuesSameAsDescriptions || (descs[j] == null) || (descs[j].isEmpty())) {
	        	redcapPVList.add(new keyValueItem((j+1), values[j])); 	
	        } // else if (valuesSameAsDescriptions || (descs[j] == null) || (descs[j].isEmpty()))
	        else {
	        	try {
	        		redcapPVList.add(new keyValueItem(Integer.parseInt(values[j]), descs[j]));	
	        	}
	        	catch (NumberFormatException e) {
	        		redcapPVList.add(new keyValueItem(k, values[j] + " (" + descs[j] + ")"));	
	        		k++;
	        	}
	        		
	        }
	    } // for (int j = 0; j < numpvNotNull; j++)
		

		// If Yes/No is not already attached to numeric values, make sure Yes = 1, No = 0
		// Also check for Left/Right and Normal/Abnormal are consistent
		/*else {
			if (values.length > 1
					&& ((values[0].equalsIgnoreCase("Yes") && values[1].equalsIgnoreCase("No"))
							|| (values[0].equalsIgnoreCase("y") && values[1].equalsIgnoreCase("n"))
							|| (values[0].equalsIgnoreCase("Right") && values[1].equalsIgnoreCase("Left"))
							|| (values[0].equalsIgnoreCase("r") && values[1].equalsIgnoreCase("l")) || (values[0]
							.equalsIgnoreCase("Normal") && values[1].equalsIgnoreCase("Abnormal")))) {
				String temp = values[1];
				values[1] = values[0];
				values[0] = temp;

				for (int j = 0; j < size; j++) {
					redcapPVMap.put(j, values[j]);
				}
			}

			// Otherwise, just label in increasing order, and show both value and label (minus overlap)
			else {
				for (int j = 0; j < size; j++) {
					if (descs[j] != null && descs[j].startsWith(values[j])) {
						redcapPVMap.put(j, descs[j]);
					} else {
						if (descs[j] == null || descs[j].isEmpty()) {
							redcapPVMap.put(j, values[j]);
						} else {
							redcapPVMap.put(j, values[j] + " (" + descs[j] + ")");
						}
					}
				}
			}
		}*/

		return redcapPVList;
	}
	
	private static class keyValueItem {

        /** DOCUMENT ME! */
        private final int key;

        /** DOCUMENT ME! */
        private final String value;

        /**
         * Creates a new keyValueItem object.
         * 
         * @param key
         * @param value
         */
        public keyValueItem(final int key, final String value) {
            this.key = key;
            this.value = value;
        }

        /**
         * DOCUMENT ME!
         * 
         * @return DOCUMENT ME!
         */
        public int getKey() {
            return key;
        }

        /**
         * DOCUMENT ME!
         * 
         * @return DOCUMENT ME!
         */
        public String getValue() {
            return value;
        }

    }
}
