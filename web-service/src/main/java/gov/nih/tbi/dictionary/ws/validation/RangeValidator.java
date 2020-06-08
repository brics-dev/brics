package gov.nih.tbi.dictionary.ws.validation;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

public class RangeValidator extends CellValidator {

	public RangeValidator() {

		super();
	}

	public RangeValidator(List<StructuralFormStructure> dictionary) {

		super(dictionary);
	}

	@Override
	// Will only check against the base value range and it's translations
	// Does not handle references
	public boolean validate(MapElement iElement, String data) {

		// Gets base value range ignoring Conditionals
		// Vector<String> range = ValidationUtil.tokenizeRange(iElement.getValueRange());
		// if (!range.isEmpty()){
		// for (ITranslationRule trans : iElement.getTranslationRules()){
		// //range.add(trans.getKey()); new style
		// for (ITranslationValue value: trans.getValueList()){
		// range.add(value.getValue());
		// }
		// }
		// }

		Vector<String> range = new Vector<String>();

		return inRange(data, iElement);
	}

	// public boolean inRange(String data, AbstractDataElement iElement){
	// return inRange(data, new HashSet<String>(valueRange), type, null, iElement);
	// }
	//
	// public boolean inRange(String data, Vector<String> valueRange, String type, String format, AbstractDataElement
	// iElement){
	// return inRange(data, new HashSet<String>(valueRange), type, format, iElement);
	// }
	//
	// public boolean inRange(String data, HashSet<String> valueRange, String type, AbstractDataElement iElement){
	// return inRange(data, valueRange, type, null, iElement);
	// }

	/**
	 * This method is replaced with our implementation
	 * 
	 * @param data
	 * @param valueRange
	 * @param type
	 * @param format
	 * @return
	 */
	public boolean inRange(String data, MapElement mapElement) {

		boolean result;
		StructuralDataElement iElement = mapElement.getStructuralDataElement();

		switch (iElement.getType()) {
			case ALPHANUMERIC: {
				if (InputRestrictions.FREE_FORM.equals(iElement.getRestrictions())) {
					return true;
				}
				result = stringInRange(data, null, iElement);
				break;
			}
			case NUMERIC: {
				result = floatInRange(data, null, iElement);
				break;
			}
			case FILE:
			case TRIPLANAR:
			case THUMBNAIL: {
				result = true;
				break;
			}
			case DATE: {
				result = dateInRange(data, null, ModelConstants.UNIVERSAL_DATE_FORMAT);
				break;
			}
			case BIOSAMPLE: {
				if (InputRestrictions.FREE_FORM.equals(iElement.getRestrictions())) {
					return true;
				}

				result = stringInRange(data, null, iElement);
				break;
			}
			case GUID: {
				result = true;
				break;
			}
			default: {
				result = false;
				break;
			}
		}

		return result;
	}

	private boolean floatInRange(String data, HashSet<String> valueRange, StructuralDataElement iElement) {

		String[] dataArr = data.split(";");

		for (String value : dataArr) {
			BigDecimal num;
			try {
				num = new BigDecimal(value);
			} catch (NumberFormatException e) {
				return false;
			}

			if (iElement.getMinimumValue() != null && num.compareTo(iElement.getMinimumValue()) < 0) {
				return false;
			}

			if (iElement.getMaximumValue() != null && num.compareTo(iElement.getMaximumValue()) > 0) {
				return false;
			}

			if (!InputRestrictions.FREE_FORM.equals(iElement.getRestrictions())) {
				boolean found = false;

				for (ValueRange vr : iElement.getValueRangeList()) {
					if (vr.getValueRange().equalsIgnoreCase(value.trim())) {
						found = true;
						break;
					}
				}

				if (!found) {
					return false;
				}
			}

		}

		return true;

	}

	private boolean dateInRange(String data, HashSet<String> valueRange, String format) {

		Date date = ValidationUtil.parseDate(data);

		// Makes sure date is not in the future (future dates are currently disallowed)
		if (date.after(new Date()) || date == null) {
			return false;
		}

		return true;
	}

	private boolean stringInRange(String data, HashSet<String> valueRange, StructuralDataElement iElement) {

		String[] dataArr = data.split(";");

		for (String value : dataArr) {
			boolean found = false;

			for (ValueRange vr : iElement.getValueRangeList()) {
				if (vr.getValueRange().equalsIgnoreCase(value.trim())) {
					found = true;
					break;
				}
			}

			if (!found) {
				return false;
			}
		}

		return true;
	}
	
	/**
	 * This method is to check if range data contains duplicates
	 * 
	 * @param data
	 * @return List<String> list of duplicate values for displaying in error message
	 */
	public List<String> hasDuplicates(String data){

		List<String> noDuplicatesList = new ArrayList<String>();
		List<String> duplicatesList = new ArrayList<String>();
		String[] dataArr = data.split(";");
		List<String> dataList = Arrays.asList(dataArr);
				
		for(String str : dataList){
			if(!noDuplicatesList.contains(str)){
				noDuplicatesList.add(str);				
			} else {
				duplicatesList.add(str);
			}			
		}
		return duplicatesList;
	}
	
	/**
	 * This method is used to check which values out of permissible values are outside of the numeric range
	 * 
	 * @param data , a semicolon separated list of values 
	 * @param iElement
	 * @return String that contains the message details for values that aren't within the ValueRange
	 */
	public String pvOutsideRange(String data, StructuralDataElement iElement) {

		List<String> dataList = new ArrayList<String>(Arrays.asList(data.split(";")));
		String valuesOutsideRange = "";
		for (String value : dataList) {
			
			if (!InputRestrictions.FREE_FORM.equals(iElement.getRestrictions())) {
				boolean foundInData = false;

				for (ValueRange vr : iElement.getValueRangeList()) {
					if (vr.getValueRange().equalsIgnoreCase(value.trim())) {
						foundInData = true;
						break;
					}
				}

				if (!foundInData) {
					valuesOutsideRange = valuesOutsideRange.concat(", "+ value);
				}
			}

		}

		if(!valuesOutsideRange.isEmpty()) {
			if(valuesOutsideRange.indexOf(',') == valuesOutsideRange.lastIndexOf(',')) {
				return "The value " + valuesOutsideRange.substring(valuesOutsideRange.indexOf(',')) + " ";
			} else {
				return "The set of values" + valuesOutsideRange + " ";
			}
		} else {
			return "No values within the set ";
		}
	}

}
