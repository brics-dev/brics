package gov.nih.tbi.dictionary.model;

import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.commons.model.RequiredType;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.dictionary.dao.KeywordSparqlDao;
import gov.nih.tbi.dictionary.dao.LabelSparqlDao;
import gov.nih.tbi.dictionary.dao.ValidationPluginDao;
import gov.nih.tbi.dictionary.model.hibernate.Alias;
import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.Classification;
import gov.nih.tbi.dictionary.model.hibernate.ClassificationElement;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseElement;
import gov.nih.tbi.dictionary.model.hibernate.Domain;
import gov.nih.tbi.dictionary.model.hibernate.ExternalId;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.MeasuringUnit;
import gov.nih.tbi.dictionary.model.hibernate.Population;
import gov.nih.tbi.dictionary.model.hibernate.SubDomain;
import gov.nih.tbi.dictionary.model.hibernate.SubDomainElement;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.CsvToBean;

/**
 * This extends the default CsvToBean class so it can handle special fields.
 * 
 * @author Francis Chen
 * 
 */
@Component
public class CsvToDataElement extends CsvToBean<MapElement> {

	static Logger logger = Logger.getLogger(CsvToDataElement.class);

	@Autowired
	KeywordSparqlDao keywordDao;

	@Autowired
	LabelSparqlDao labelDao;

	@Autowired
	ValidationPluginDao validationPluginDao;

	@Autowired
	StaticReferenceManager staticManager;

	List<Keyword> allKeywords;
	List<Keyword> allLabels;
	
	ArrayList<String> errMsgs;
	String ioExAttrName;

	public List<DataElement> parse(TBIMappingStrategy mapper, CSVReader csv,
			HashMap<String, ArrayList<String>> pvValidateMap) throws IOException, InstantiationException,
			IllegalAccessException, IntrospectionException, InvocationTargetException {

		mapper.captureHeader(csv); // processes the first line (header).

		// Check for all mandatory headers
		if (mapper.validateColumns(staticManager.getSubgroupList()) != null) {
			throw new RuntimeException(String.format(ServiceConstants.ERROR_COLUMN_MISSING,
					mapper.validateColumns(staticManager.getSubgroupList())));
		}

		/*
		 * This is a quick fix to a preexisting problem with the TBIMappingStrategy.
		 * 
		 * In the getCsvColumnMapping(), TBIMappingStrategy.java:
		 * 
		 * StaticReferenceManager staticManager = new StaticReferenceManagerImpl();
		 * 
		 * 
		 * This creates a Static Reference Manager obj with null DAO objects. When
		 * 
		 * List<Disease> diseases = staticManager.getDiseaseList(); (line 92)
		 * 
		 * is called, a NPE is thrown.
		 * 
		 * I think the spring Autowiring of the StaticRefernceManager is the reason why it works in this class
		 * 
		 * @Autowired StaticReferenceManager staticManager;
		 */
		staticManager.getDiseaseList();

		// Check for duplicate columns //MATT'S NOTE: There is something in this for loop that causes a
		// java.lang.NullPointerException which makes the import to fail
		// fast and creates the white box of nothingness
		int columns = mapper.getColumnCount();
		for (int i = 0; i < columns; i++) {
			for (int j = 0; j < columns; j++) {
				if ((j != i) && mapper.getColumnName(i) != null
						&& (mapper.getColumnName(i).equalsIgnoreCase(mapper.getColumnName(j)))) {
					throw new UnsupportedOperationException(ServiceConstants.DUPLICATE_COLUMN + mapper.getColumnName(i));
				}
			}
		}

		String[] line;
		List<DataElement> list = new ArrayList<DataElement>();
		// sets a master list of all keywords so processing the keyword would not need to query the database
		// multiple times
		allKeywords = keywordDao.getAll();
		allLabels = labelDao.getAll();

		errMsgs = new ArrayList<String>();
		while (null != (line = csv.readNext())) {

			// This will ignore a blank line and continue processing
			// blank rows (no commas) are set to the line[] with the first value as null
			// this creates a null string in the array at space 0

			if (!emptyStringDetected(line)) {

				DataElement obj = processLine(mapper, line, pvValidateMap);
				obj.setStatus(DataElementStatus.DRAFT);

				list.add(obj);
				// Commenting this out means that blank lines (all commas) will cause problems for the import. This
				// check should be done when parsing the document, however, not when validating the data element.
				// This should be done in the if statement a few lines up.
				// if (obj.dataElementHasData(obj))
				// {
				// list.add(obj);
				// }
				if (!ioExAttrName.equals("")){
					errMsgs.add(ioExAttrName + " in Data Element "+obj.getName());
				}
			}
		}

		csv.close();
		if (!errMsgs.isEmpty()){
			String errorMessage = "Error in parsing Data Elements. Un-terminated quoted field exists in following attribute(s): " + StringUtils.join(errMsgs.toArray(), "; ") + ".";
			logger.error("CSVToDataElement.parse() throw IOException: "+errorMessage);
			throw new IOException(errorMessage);
		}

		return list;

	}

	/**
	 * The original code from CsvToBean has issues with Java 8's enum valueOf method.
	 */
	@Override
	protected Object convertValue(String value, PropertyDescriptor prop) throws InstantiationException,
			IllegalAccessException {
		PropertyEditor editor = getPropertyEditor(prop);
		Object obj = value;
		if (null != editor) {
			editor.setAsText(value);
			obj = editor.getValue();
		}
		return obj;
	}

	/**
	 * Processes a line of csv. Sets the appropriate fields of a Map Element.
	 * 
	 * @param mapper
	 * @param line
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IntrospectionException
	 * @throws IOException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws Exception
	 */
	protected DataElement processLine(TBIMappingStrategy mapper, String[] line,
			HashMap<String, ArrayList<String>> pvValidateMap) throws InstantiationException, IllegalAccessException,
			IntrospectionException, IOException, IllegalArgumentException, InvocationTargetException {

		DataElement bean = mapper.createBean();
		String[] normalizedRangeList = null;
		String[] valueRangeDescriptionList = null;
		String[] valueRangeOutputCodeStringList = null;
		Integer[] valueRangeOutputCodeList = null;
		// added by Ching-Heng
		String[] elementOidList = null;
		String[] itemResponseOidList = null;
		
		ArrayList<String> ioExAttrNameList = new ArrayList<String>();
		
		// for each column in the csv line
		for (int col = 0; col < line.length; col++) {
			PropertyDescriptor prop = mapper.findDescriptor(col);
			if (null != prop) {
				String value = checkForTrim(line[col], prop);
				//replace '\"' with '"' and then replace '""' with '"' in the field if it contains two '\"'s
				if(value.indexOf("\\\"") >= 0 ){
					value = value.replaceAll("\\\\\"", "\"");
					if(value.indexOf("\"\"") >= 0 ){
						value = value.replaceAll("\\\"\\\"", "\"");
					}
				}
				String obj;


				if (value == null || value.equals(ServiceConstants.EMPTY_STRING)) {
					obj = ServiceConstants.EMPTY_STRING;
				} else {
					if (ServiceConstants.TYPE.equals(prop.getName())
							|| ServiceConstants.RESTRICTIONS.equals(prop.getName())) { // cannot call convert value on
																						// enum
						// fields. Something to do with
						// spring upgrade or java 8 update
						// caused this
						obj = value;
					} else if (convertValue(value, prop) instanceof Long) {
						obj = ((Long) convertValue(value, prop)).toString();
					} else {
						obj = convertValue(value, prop).toString();
					}
				}

				// This part handles special object types (not strings)
				if (ServiceConstants.KEYWORD_LIST.equals(prop.getName())) { // if column is keywordList
					try {
						bean.setKeywords(parseKeywords(obj));
					} catch(IOException e){
						ioExAttrNameList.add(prop.getName());      //get attribute name when catching the exception throw from CsvParser.parse() called in parseGenericList()
					}
				} else if (ServiceConstants.LABEL_LIST.equals(prop.getName())) {
					try {
						bean.setLabels(parseLabels(obj));
					} catch(IOException e){
						ioExAttrNameList.add(prop.getName());     
					}
				} else if (ServiceConstants.TYPE.equals(prop.getName())) {
					bean.setType(parseType(obj));

				} else if (ServiceConstants.VERSION_READABLE.equals(prop.getName())) {
					String version = obj;
					bean.setVersion(version);
				} else if (ServiceConstants.SIZE.equals(prop.getName())) { // if column is size
					try {
						bean.setSize(Integer.valueOf(obj));
					} catch (NumberFormatException e) {
						bean.setSize(null);
					}
				} else if (ServiceConstants.MAXIMUM_VALUE.equals(prop.getName())) {
					try {
						// The constructor here will make sure that obj is a valid numerical string
						bean.setMaximumValue(new BigDecimal(obj));
					} catch (NumberFormatException e) {
						bean.setMaximumValue(null);
					}
				} else if (ServiceConstants.MINIMUM_VALUE.equals(prop.getName())) {
					try {
						bean.setMinimumValue(new BigDecimal(obj));
					} catch (NumberFormatException e) {
						bean.setMaximumValue(null);
					}
				} else if (ServiceConstants.ALIASES.equals(prop.getName())) { // if column is aliases
					try {
						bean.setAliasList(parseAliasList(obj, bean));
					} catch(IOException e){
						ioExAttrNameList.add(prop.getName());         
					}
				} else if (ServiceConstants.CLASSIFICATION.equals(prop.getName().split("\\.")[0])) {
					ClassificationElement ce = parseClassification(obj, prop.getName(), bean);
					if (ce != null) {

						bean.addClassificationElement(ce);
					}
				} else if (ServiceConstants.MEASUREMENT_UNIT.equals(prop.getName())) {
					bean.setMeasuringUnit(parseMeasuringUnit(obj));
				} else if (ServiceConstants.DOMAIN.equals(prop.getName().split("\\.")[0])) {
					parseDomainColumn(bean, prop.getName(), obj);
				} else if (ServiceConstants.POPULATION.equals(prop.getName())) {
					bean.setPopulation(parsePopulation(obj));
				} else if (ServiceConstants.CATEGORY.equals(prop.getName())) {
					bean.setCategory(parseCategory(obj));
				} else if (ServiceConstants.RESTRICTIONS.equals(prop.getName())) {
					bean.setRestrictions(parseRestrictions(obj, bean));
				} /*  these fields have been removed and separated out into a different CSV process. commenting out just in case we add it back
					else if (ServiceConstants.LOINC.equals(prop.getName())) {
					try {
						bean.updateExternalId(ExternalType.LOINC, obj);
					} catch (NumberFormatException e) {

					}
				} else if (ServiceConstants.CDISC.equals(prop.getName())) {
					try {
						bean.updateExternalId(ExternalType.CDISC, obj);
					} catch (NumberFormatException e) {

					}
				} else if (ServiceConstants.NINDS.equals(prop.getName())) {
					try {
						bean.updateExternalId(ExternalType.NINDS, obj);
					} catch (NumberFormatException e) {

					}
				} else if (ServiceConstants.SNOMED.equals(prop.getName())) {
					try {
						bean.updateExternalId(ExternalType.SNOMED, obj);
					} catch (NumberFormatException e) {

					}
				} else if (ServiceConstants.CADSR.equals(prop.getName())) {
					try {
						bean.updateExternalId(ExternalType.CADSR, obj);
					} catch (NumberFormatException e) {

					}
				}*/ else if (ServiceConstants.NOTES.equals(prop.getName())) {
					try {
						String notesString = obj;
						bean.setNotes(truncateString(StringEscapeUtils.escapeHtml(notesString)));
					} catch (NumberFormatException e) {

					}
				} else if (ServiceConstants.GUIDELINES.equals(prop.getName())) {
					try {
						String guidelinesString = obj;
						bean.setGuidelines(truncateString(StringEscapeUtils.escapeHtml(guidelinesString)));
					} catch (NumberFormatException e) {

					}
				} else if (ServiceConstants.HISTORICAL_NOTES.equals(prop.getName())) {
					try {
						String historicalNotesString = obj;
						bean.setHistoricalNotes(truncateString(StringEscapeUtils.escapeHtml(historicalNotesString)));
					} catch (NumberFormatException e) {

					}
				} else if (ServiceConstants.REFERENCES.equals(prop.getName())) {
					try {
						String referencesString = obj;
						bean.setReferences(truncateString(StringEscapeUtils.escapeHtml(referencesString)));
					} catch (NumberFormatException e) {

					}
				} else if (ServiceConstants.QUESTION_TEXT.equals(prop.getName())) {
					bean.setSuggestedQuestion(truncateString(StringEscapeUtils.escapeHtml(obj)));

				} else if (ServiceConstants.LABEL_LIST.equals(prop.getName())) {
					try {
						bean.setLabels(parseKeywords(obj));
					} catch(IOException e){
						ioExAttrNameList.add(prop.getName());
					}
				} else if (ServiceConstants.SEE_ALSO.equals(prop.getName())) {
					bean.setSeeAlso(obj);
				} else if (ServiceConstants.SUBMITTING_ORGANIZATION_NAME.equals(prop.getName())) {
					bean.setSubmittingOrgName(obj);
				} else if (ServiceConstants.SUBMITTING_CONTACT_NAME.equals(prop.getName())) {
					bean.setSubmittingContactName(obj);
				} else if (ServiceConstants.SUBMITTING_CONTACT_INFORMATION.equals(prop.getName())) {
					bean.setSubmittingContactInfo(obj);
				} else if (ServiceConstants.STEWARD_ORGANIZATION_NAME.equals(prop.getName())) {
					bean.setStewardOrgName(obj);
				} else if (ServiceConstants.STEWARD_CONTACT_NAME.equals(prop.getName())) {
					bean.setStewardContactName(obj);
				} else if (ServiceConstants.STEWARD_CONTACT_INFORMATION.equals(prop.getName())) {
					bean.setStewardContactInfo(obj);
				} else if (ServiceConstants.EFFECTIVE_DATE.equals(prop.getName())) {
					bean.setEffectiveDate(parseDate(obj));
				} else if (ServiceConstants.UNTIL_DATE.equals(prop.getName())) {
					bean.setUntilDate(parseDate(obj));
				} else {
					// default
					// case,
					// MapElement
					// field
					// is
					/*
					 * If the Property String doesnt match any of the previous conditionals, the value is written to the
					 * Data Element object. The following fields fall under this category: *Name *Title *Description
					 * *Short Description *Suggested Questions
					 * EscapeHtml() is to escape some special characters like great than, less than, mdash, copy sign, Umlaut and so on
					 */
					if (!ServiceConstants.EMPTY_STRING.equals(obj)) {
						if (prop.getName().equalsIgnoreCase("suggestedQuestion")) {
							String suggestedString = obj;
							prop.getWriteMethod().invoke((DataElement) bean,
									truncateString(StringEscapeUtils.escapeHtml(suggestedString)));
						} else {
							prop.getWriteMethod().invoke((DataElement) bean, StringEscapeUtils.escapeHtml(obj.trim()));
						}
					}
				}
			} else { // handles the cases where we do not directly store into bean fields
				String value = checkForTrim(line[col]);
				
				if (value.indexOf("\"") >=0){
					value = value.replace("\"", "\"\"\"");
				}
				
				if (ServiceConstants.VALUE_RANGES.equals(mapper.getColumnName(col))) { // if column is valueRanges
					try {
						normalizedRangeList = parseGenericList(value);
					} catch(IOException e){
						ioExAttrNameList.add(mapper.getColumnName(col));
					}
				} else if (ServiceConstants.VALUE_RANGE_DESCRIPTIONS.equals(mapper.getColumnName(col))) { // if column																											// is
					try {																						// valueRangeDescriptions
						valueRangeDescriptionList = parseGenericList(value);
					} catch(IOException e){
						ioExAttrNameList.add(mapper.getColumnName(col));
					}
				} else if (ServiceConstants.VALUE_RANGE_OUTPUT_CODES.equals(mapper.getColumnName(col))) { // if column
																											// is
																											// valueRangeOutputCodes
					if (normalizedRangeList != null && value.equals("")) {
						// this means that no output codes were given...so make valueRangeOutputCodeList size of
						// normalizedRangeList and make them all null
						valueRangeOutputCodeStringList = new String[normalizedRangeList.length];
					} else {
						try {
							valueRangeOutputCodeStringList = parseGenericList(value);
						} catch(IOException e){
							ioExAttrNameList.add(mapper.getColumnName(col));
						}
					}
				} else if(ServiceConstants.ELEMENT_OID.equals(mapper.getColumnName(col))) {
					try {																						// valueRangeDescriptions
						elementOidList = parseGenericList(value);
					} catch(IOException e){
						ioExAttrNameList.add(mapper.getColumnName(col));
					}
				} else if(ServiceConstants.ITEM_RESPONSE_OID.equals(mapper.getColumnName(col))) {
					try {																						// valueRangeDescriptions
						itemResponseOidList = parseGenericList(value);
					} catch(IOException e){
						ioExAttrNameList.add(mapper.getColumnName(col));
					}
				} else if (mapper.getColumnName(col) != null
						&& ServiceConstants.CLASSIFICATION.equalsIgnoreCase(mapper.getColumnName(col).split("\\.")[0])) { // if
																															// column
																															// is
																															// a
																															// classification
					ClassificationElement ce = parseClassification(value, mapper.getColumnName(col), bean);
					if (ce != null) {
						bean.getClassificationElementList().add(ce);
					}
				} else if (mapper.getColumnName(col) != null
						&& ServiceConstants.DOMAIN.equalsIgnoreCase(mapper.getColumnName(col).split("\\.")[0])) {
					parseDomainColumn(bean, mapper.getColumnName(col), value);
				}
			}
			ioExAttrName = "";
			if(!ioExAttrNameList.isEmpty()){
				ioExAttrName = StringUtils.join(ioExAttrNameList.toArray(), ", ");
			}
		}
		cleanUpExternalIdSet(bean.getExternalIdSet());


		// since we are checking that there is same number of permissible values and descriptions at this point, lets do
		// all the permissible value validation checking
		if (normalizedRangeList != null && valueRangeDescriptionList != null ) {
			permissibleValueValidation(bean, pvValidateMap, normalizedRangeList, valueRangeDescriptionList,
				valueRangeOutputCodeStringList, valueRangeOutputCodeList, elementOidList, itemResponseOidList);
		}

		return bean;
	}

	// permissible value/description/output code validation checking
	private void permissibleValueValidation(DataElement bean, HashMap<String, ArrayList<String>> pvValidateMap,
			String[] normalizedRangeList, String[] valueRangeDescriptionList, String[] valueRangeOutputCodeStringList,
			Integer[] valueRangeOutputCodeList, String[] elementOidList, String[] itemResponseOidList) {
		ArrayList<String> pvValidateList = new ArrayList<String>();

		if (normalizedRangeList != null && valueRangeDescriptionList != null && normalizedRangeList.length != valueRangeDescriptionList.length) {

			pvValidateList.add(ServiceConstants.PER_VALUE_RANGE_MISMATCH + bean.getName());
			// bean.setCategory(null);
			// throw new RuntimeException(String.format(ServiceConstants.PER_VALUE_RANGE_MISMATCH + bean.getName()));
		}


		if (valueRangeOutputCodeStringList == null) {  // this can happen in case of imported files that dont even have
														// the column header
			valueRangeOutputCodeStringList = new String[normalizedRangeList.length];
		}

		if (normalizedRangeList.length != valueRangeOutputCodeStringList.length) {
			// bean.setCategory(null);
			pvValidateList.add(ServiceConstants.PER_VALUE_RANGE_OUTOUT_CODES_MISMATCH + bean.getName());
			// throw new RuntimeException(String.format(ServiceConstants.PER_VALUE_RANGE_OUTOUT_CODES_MISMATCH +
			// bean.getName()));
		}



		// set up size of valueRangeOutputCodeList array (Integer array).
		valueRangeOutputCodeList = new Integer[valueRangeOutputCodeStringList.length];

		// try and populate valueRangeOutputCodeList (Integer array) based on valueRangeOutputCodeStringList. Should be
		// all Integers or null or "". If not throw error
		// check that all are integers, check that any numbers in valueRangeOutputCodeList are between -10000 and 10000
		for (int i = 0; i < valueRangeOutputCodeStringList.length; i++) {
			String outputCodeString = valueRangeOutputCodeStringList[i];
			if (outputCodeString == null || outputCodeString.equals("")) {
				valueRangeOutputCodeList[i] = null;
			} else {
				Integer outputCodeInteger = null;
				try {
					// checking for integer
					outputCodeInteger = new Integer(outputCodeString);
					// checking for between -10000 and 10000
					if (outputCodeInteger < -10000 || outputCodeInteger > 10000) {
						// bean.setCategory(null);
						pvValidateList.add(ServiceConstants.PER_VALUE_RANGE_OUTOUT_CODES_BADINTEGERANGE
								+ bean.getName());
						// throw new
						// RuntimeException(String.format(ServiceConstants.PER_VALUE_RANGE_OUTOUT_CODES_BADINTEGERANGE +
						// bean.getName()));
					}
				} catch (NumberFormatException nfe) {
					// bean.setCategory(null);
					pvValidateList.add(ServiceConstants.PER_VALUE_RANGE_OUTOUT_CODES_BADINTEGERANGE + bean.getName());
					// throw new
					// RuntimeException(String.format(ServiceConstants.PER_VALUE_RANGE_OUTOUT_CODES_BADINTEGERANGE +
					// bean.getName()));
				}

				valueRangeOutputCodeList[i] = outputCodeInteger;
			}
		}

		// check that output codes are unique
		boolean isDuplicates = false;
		outerLoop: for (int i = 0; i < valueRangeOutputCodeList.length; i++) {
			Integer outputCodeInteger1 = valueRangeOutputCodeList[i];
			if (outputCodeInteger1 != null) {
				for (int j = 0; j < valueRangeOutputCodeList.length; j++) {
					Integer outputCodeInteger2 = valueRangeOutputCodeList[j];
					if (outputCodeInteger2 != null && outputCodeInteger1.intValue() == outputCodeInteger2.intValue()
							&& i != j) {
						isDuplicates = true;
						break outerLoop;
					}
				}
			}
		}
		if (isDuplicates) {
			// bean.setCategory(null);
			pvValidateList.add(ServiceConstants.PER_VALUE_RANGE_OUTOUT_CODES_DUPLICATES + bean.getName());
			// throw new RuntimeException(String.format(ServiceConstants.PER_VALUE_RANGE_OUTOUT_CODES_DUPLICATES +
			// "   DataElement: " + bean.getName()));
		}

		// all is good so set value range list in bean
		if (pvValidateList.size() == 0) {
			bean.setValueRangeList(finalizeValueRangeList(normalizedRangeList, valueRangeDescriptionList,
					valueRangeOutputCodeList, elementOidList, itemResponseOidList, bean));
		} else {
			pvValidateMap.put(bean.getName(), pvValidateList);
			// doing the following just tp prevent error saying permissible value is required in case of when user
			// actually did put value but there are other errors
			if (normalizedRangeList.length > 0) {
				bean.setValueRangeList(finalizeValueRangeList(normalizedRangeList,
						new String[normalizedRangeList.length], new Integer[normalizedRangeList.length],(elementOidList==null)?null:new String[elementOidList.length], 
								(itemResponseOidList==null)?null:new String[itemResponseOidList.length], bean));
			}
		}
	}



	/**
	 * Turns String of a input restriction into InputRestriction object
	 * 
	 * @param obj
	 * @param bean
	 * @return
	 */
	private InputRestrictions parseRestrictions(String obj, DataElement bean) {

		for (InputRestrictions restriction : InputRestrictions.values()) {
			if (restriction.getValue().compareToIgnoreCase(obj) == 0) {
				return restriction;
			}
		}

		return null;
	}

	/**
	 * Parse a column header with the format "Domain.<Disease>" and cell contents "<Domain>.<Sub-Domain>". Only process
	 * the first instance that is come across that has cell contents. This function does NOT check for valid
	 * subdomain/domain/disease pairs. That needs to be done in the validation phase of import.
	 * 
	 * A DiseaseElement object will be created, as long as the Disease is valid. An invalid disease (bad column header)
	 * will result in a diseaseElement object not being created. (A warning will be thrown to indicate a bad column
	 * header). DomainPairs are not garunteed to be attached (Set may be empty), and any domainpairs in the set are not
	 * garunteed to have a domain and/or subdomain. All of this needs to be checked in validation.
	 * 
	 * @param de : The dataElement being processed.
	 * @param header : Domain.<Disease>
	 * @param cell : <Domain>.<Sub-Domain>
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 * @throws Exception
	 */
	private void parseDomainColumn(DataElement de, String header, String cell) throws MalformedURLException,
			UnsupportedEncodingException {

		// If the cell is blank then this disease should be skipped for this data element (another column may have valid
		// domain/sub-domain pair
		if (cell == null || cell.equals(ServiceConstants.EMPTY_STRING)) {
			return;
		}

		// Retrieve the disease from the header
		String headerArray[] = header.split("\\.", 2);
		String diseaseString;
		Disease disease = null;

		if (headerArray.length < 2) {
			// User entered a domain header without a disease in it (no ".")
			diseaseString = ServiceConstants.EMPTY_STRING;
		} else {
			diseaseString = headerArray[1];
		}

		// Add the disease (if found) to the DiseaseElement object
		for (Disease d : staticManager.getDiseaseList()) {
			if (d.getName().compareToIgnoreCase(diseaseString) == 0) { // if the two disease names are equal
				disease = d;
			}
		}

		// If the disease is valid then add to data element and continue. Otherwise do not add this disease to data
		// element.
		if (disease == null) {
			return;
		}

		// Create the domain pairs from the contents of the cell.
		String domainPairStrings[] = cell.split(";");
		for (String dpString : domainPairStrings) {

			// Get the Domain and Subdomains in String form
			String cellArray[] = dpString.split("\\.", 2);
			String domainString = cellArray[0];
			String subDomainString;
			if (cellArray.length < 2) {
				// user enters cell without a period
				subDomainString = ServiceConstants.EMPTY_STRING;
			} else {
				subDomainString = cellArray[1];
			}
			SubDomainElement subdomainElement =
					new SubDomainElement(disease, parseDomain(domainString), parseSubdomain(subDomainString));
			de.addSubDomainElement(subdomainElement);
		}
	}

	/**
	 * Turns domain string into domain object
	 * 
	 * @param obj
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	private Domain parseDomain(String unparsedString) throws MalformedURLException, UnsupportedEncodingException {

		for (Domain domain : staticManager.getDomainList()) {
			if (domain.getName().compareToIgnoreCase(unparsedString) == 0) {
				return domain;
			}
		}

		return null;
	}

	/**
	 * Turns subdomain string into subdomain object
	 * 
	 * @param obj
	 * @return
	 */
	private SubDomain parseSubdomain(String unparsedString) {

		for (SubDomain subdomain : staticManager.getSubDomainList()) {
			if (subdomain.getName().compareToIgnoreCase(unparsedString) == 0) {
				return subdomain;
			}
		}

		return null;
	}

	/**
	 * Turn classification string into classification object Returns null if no match is found.
	 * 
	 * @param obj
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	private ClassificationElement parseClassification(String classificationString, String rawSubgroupString,
			DataElement dataElement) throws MalformedURLException, UnsupportedEncodingException {

		ClassificationElement classificationElement = new ClassificationElement();
		String subgroupArray[] = rawSubgroupString.split("\\.", 2);

		// The case where there are no subgroups, then the data element should set the 'Classification' subgroup
		if (subgroupArray.length < 2) {
			for (Subgroup subgroup : staticManager.getSubgroupList()) {
				if (subgroup.getSubgroupName().compareTo(subgroupArray[0]) == 0) {
					classificationElement.setSubgroup(subgroup);
					Disease subgroupDisease = staticManager.getDiseaseBySubgroup(subgroup);
					classificationElement.setDisease(subgroupDisease);

					break;
				}
			}

		} else {
			for (Subgroup s : staticManager.getSubgroupList()) {
				if (s.getSubgroupName().compareToIgnoreCase(subgroupArray[1]) == 0) {
					classificationElement.setSubgroup(s);
					Disease subgroupDisease = staticManager.getDiseaseBySubgroup(s);
					classificationElement.setDisease(subgroupDisease);
					break;
				}
			}
		}

		for (Classification c : staticManager.getClassificationList(true)) {
			if (c.getName().compareToIgnoreCase(classificationString) == 0) {
				classificationElement.setClassification(c);
				break;
			}
		}

		if (classificationElement.getSubgroup() == null
				|| (classificationElement.getClassification() == null && (classificationString == null || ""
						.equals(classificationString)))) {
			return null;
		}

		return classificationElement;
	}

	/**
	 * Turn population string into population object
	 * 
	 * @param obj
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	private Population parsePopulation(String unparsedString) throws MalformedURLException,
			UnsupportedEncodingException {

		for (Population population : staticManager.getPopulationList()) {
			if (population.getName().equalsIgnoreCase("Adult and Pediatric")) {
				if (("Adult;Pediatric").equalsIgnoreCase(unparsedString)
						|| population.getName().equalsIgnoreCase(unparsedString))
					return population;
			} else if (population.getName().compareToIgnoreCase(unparsedString) == 0) {
				return population;
			}
		}

		return null;
	}

	/**
	 * Turn category string into category object
	 * 
	 * @param obj
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	private Category parseCategory(String unparsedString) throws MalformedURLException, UnsupportedEncodingException {

		for (Category category : staticManager.getCategoryList()) {
			if (category.getName().compareToIgnoreCase(unparsedString) == 0) {
				return category;
			}
		}

		return null;
	}

	/**
	 * Parses the string of a required type name into RequiredType enum, returns null otherwise.
	 * 
	 * @param typeString
	 * @return RequiredType
	 */
	private RequiredType parseRequiredType(String typeString) {

		for (RequiredType type : RequiredType.values()) {
			if (type.getValue().toLowerCase().equals(typeString.toLowerCase())) {
				return type;
			}
		}

		return null;
	}

	/**
	 * Parses the string and transforms it into a Measuring Unit, returns null otherwise.
	 * 
	 * @param measuringUnit
	 * @return
	 */

	private MeasuringUnit parseMeasuringUnit(String measuringUnit) {

		for (MeasuringUnit mu : staticManager.getMeasuringUnitList()) {
			if (mu.getName().toLowerCase().equals(measuringUnit.toLowerCase())) {
				return mu;
			}
		}
		return null;
	}

	/**
	 * Combine a list of normalized value range and list of value range descriptions into a set of ValueRange.
	 * Acceptable value list not expected in the csv so that is empty.
	 * 
	 * @param normalizedRangeList
	 * @param valueRangeDescriptionList
	 * @param bean
	 * @return Set<ValueRange>
	 */
	// private Set<ValueRange> finalizeValueRangeList(String[] normalizedRangeList, String[] valueRangeDescriptionList,
	// DataElement bean)
	// {
	//
	// Set<ValueRange> valueRangeList = new HashSet<ValueRange>();
	//
	// if (normalizedRangeList != null
	// && Array.getLength(normalizedRangeList) == Array.getLength(valueRangeDescriptionList))
	// {
	// for (int i = 0; i < Array.getLength(normalizedRangeList); i++)
	// {
	// if (!ServiceConstants.EMPTY_STRING.equals(normalizedRangeList[i]))
	// {
	// // sets the new ValueRange and add it to the list
	// ValueRange newValueRange = new ValueRange();
	// if (normalizedRangeList[i] != null)
	// {
	// newValueRange.setValueRange(normalizedRangeList[i]);
	// }
	// if (valueRangeDescriptionList[i] != null)
	// {
	// newValueRange.setDescription(valueRangeDescriptionList[i]);
	// }
	// newValueRange.setDataElement(bean);
	// valueRangeList.add(newValueRange);
	// }
	// }
	// }
	// else
	// {
	// for (int i = 0; i < Array.getLength(normalizedRangeList); i++)
	// {
	// if (!ServiceConstants.EMPTY_STRING.equals(normalizedRangeList[i]))
	// {
	// ValueRange newValueRange = new ValueRange();
	// if (normalizedRangeList[i] != null)
	// {
	// newValueRange.setValueRange(normalizedRangeList[i]);
	// }
	// if (i < valueRangeDescriptionList.length)
	// {
	// if (valueRangeDescriptionList[i] != null)
	// {
	// newValueRange.setDescription(valueRangeDescriptionList[i]);
	// }
	// else
	// {
	// newValueRange.setDescription(null);
	// }
	// }
	// else
	// {
	// newValueRange.setDescription(null);
	// }
	// newValueRange.setDataElement(bean);
	// valueRangeList.add(newValueRange);
	// }
	// }
	// }
	//
	// return valueRangeList;
	// }

	private Set<ValueRange> finalizeValueRangeList(String[] normalizedRangeList, String[] valueRangeDescriptionList,
			Integer[] valueRangeOutputCodeList, String[] elementOidList, String[] itemResponseOidList, DataElement bean) {

		Set<ValueRange> valueRangeList = new HashSet<ValueRange>();
		int nrlLen;
		int vrdlLen;
		int i;

		if (normalizedRangeList == null && valueRangeDescriptionList == null) {
			return valueRangeList;
		}

		if (normalizedRangeList != null)
			nrlLen = Array.getLength(normalizedRangeList);
		else
			nrlLen = 0;
		if (valueRangeDescriptionList != null)
			vrdlLen = Array.getLength(valueRangeDescriptionList);
		else
			vrdlLen = 0;

		// If the lengths are the same, then we dont have to worry about which array to iterate over.
		if (nrlLen == vrdlLen) {
			for (i = 0; i < nrlLen; i++) {
				ValueRange newValueRange = new ValueRange();
				if (normalizedRangeList[i] != null) {
					if (!ServiceConstants.EMPTY_STRING.equals(normalizedRangeList[i])) {
						newValueRange.setValueRange(normalizedRangeList[i]);
					} else {
						newValueRange.setValueRange(null);
					}
				}
				if (valueRangeDescriptionList[i] != null) {
					if (!ServiceConstants.EMPTY_STRING.equals(valueRangeDescriptionList[i])) {
						newValueRange.setDescription(valueRangeDescriptionList[i]);
					} else {
						newValueRange.setDescription("");
					}
				}
				newValueRange.setOutputCode(valueRangeOutputCodeList[i]);
				// added by Ching-Heng
				if(elementOidList != null && itemResponseOidList != null) {
					if(elementOidList.length == nrlLen && itemResponseOidList.length == nrlLen) {
						newValueRange.setElementOid(elementOidList[i]);
						newValueRange.setItemResponseOid(itemResponseOidList[i]);	
					}
				}
				if (newValueRange.getValueRange() != null) {
					newValueRange.setDataElement(bean.getStructuralObject());
					valueRangeList.add(newValueRange);
				}

			}
		} else { // Now we have to select which array to iterate over
			if (nrlLen > vrdlLen) {// If the normalizedRangeList is bigger, we'll iterate over that
				for (i = 0; i < nrlLen; i++) {
					ValueRange newValueRange = new ValueRange();
					if (normalizedRangeList[i] != null) {
						if (!ServiceConstants.EMPTY_STRING.equals(normalizedRangeList[i])) {
							newValueRange.setValueRange(normalizedRangeList[i]);
						} else {
							newValueRange.setValueRange(null);
						}
					}
					if (i < vrdlLen) {
						if (valueRangeDescriptionList[i] != null) {
							if (!ServiceConstants.EMPTY_STRING.equals(valueRangeDescriptionList[i])) {
								newValueRange.setDescription(valueRangeDescriptionList[i]);
							} else {
								newValueRange.setDescription("");
							}
						}
					} else {
						newValueRange.setDescription(null);
					}
					newValueRange.setOutputCode(valueRangeOutputCodeList[i]);
					// added by Ching-Heng
					if(elementOidList[i] != null && itemResponseOidList[i] != null) {
						newValueRange.setElementOid(elementOidList[i]);
						newValueRange.setItemResponseOid(itemResponseOidList[i]);
					}
					if (newValueRange.getValueRange() != null) {
						newValueRange.setDataElement(bean.getStructuralObject());
						valueRangeList.add(newValueRange);
					}
				}
			} else {

				for (i = 0; i < vrdlLen; i++) {
					ValueRange newValueRange = new ValueRange();
					if (i < nrlLen) {
						if (normalizedRangeList[i] != null) {
							if (!ServiceConstants.EMPTY_STRING.equals(normalizedRangeList[i])) {
								newValueRange.setValueRange(normalizedRangeList[i]);
							} else {
								newValueRange.setValueRange(null);
							}
						}
					} else {
						newValueRange.setValueRange(null);
					}
					if (valueRangeDescriptionList[i] != null) {
						if (!ServiceConstants.EMPTY_STRING.equals(valueRangeDescriptionList[i])) {
							newValueRange.setDescription(valueRangeDescriptionList[i]);
						} else {
							newValueRange.setDescription("");
						}
					}
					newValueRange.setOutputCode(valueRangeOutputCodeList[i]);
					// added by Ching-Heng
					newValueRange.setElementOid(elementOidList[i]);
					newValueRange.setItemResponseOid(itemResponseOidList[i]);
					if (newValueRange.getValueRange() != null) {
						newValueRange.setDataElement(bean.getStructuralObject());
						valueRangeList.add(newValueRange);
					}
				}
			}
		}

		return valueRangeList;
	}

	/**
	 * Parses a semi-colon delimited list into a set of diseaseElements
	 * 
	 * @param bean
	 * @param unparsedList
	 * @return Set<Alias>
	 * @throws Exception
	 * @deprecated Diseases are no longer provided in a list, but instead are extracted from column headers
	 */
	private Set<DiseaseElement> parseDiseaseList(String unparsedList, DataElement bean) throws Exception {

		Set<DiseaseElement> diseaseList = new HashSet<DiseaseElement>();

		try {
			String[] parsedDiseaseArray = parseGenericList(unparsedList);

			for (String diseaseString : parsedDiseaseArray) {
				for (Disease disease : staticManager.getDiseaseList()) {
					if (disease.getName().compareToIgnoreCase(diseaseString) == 0) { // if the two disease names are
																						// equal
						DiseaseElement newDiseaseElement = new DiseaseElement();
						newDiseaseElement.setDisease(disease);
						newDiseaseElement.setSemanticDataElement(bean.getSemanticObject());
						diseaseList.add(newDiseaseElement);
					}
				}
			}
		} catch (Exception e) {
			throw new Exception(e);
		}

		return diseaseList;
	}

	/**
	 * Parses a semi-colon delimited list into a set of aliases
	 * 
	 * @param bean
	 * @param unparsedList
	 * @return Set<Alias>
	 * @throws IOException
	 * @throws Exception
	 */
	private Set<Alias> parseAliasList(String unparsedList, DataElement bean) throws IOException {

		Set<Alias> aliasList = new HashSet<Alias>();

		String[] parsedAliasArray = parseGenericList(unparsedList);

		for (String alias : parsedAliasArray) {
			Alias newAlias = new Alias();
			newAlias.setName(alias);
			newAlias.setDataElement(bean.getStructuralObject());
			aliasList.add(newAlias);
		}

		return aliasList;
	}

	/**
	 * Parses a generic semi-colon delimited String list
	 * 
	 * @param unparsedList
	 * @return
	 * @throws IOException
	 * @throwsException
	 */
	private String[] parseGenericList(String unparsedList) throws IOException {

		// sets the delimitor in constructor
		unparsedList = unparsedList.replace("\n", "");
		CSVParser myParser = new CSVParser(';');
		String[] parsedList;
		List<String> result = new LinkedList<String>();
		// Dev's Note: I used a linked list in order to safely check and edit the parsed list for . MG
		parsedList = myParser.parseLine(unparsedList);
		// trim all the values in the list
		for (String str : parsedList) {
			if (!str.equals("")) {			
				if (str.indexOf("\"\"") >=0){
					str = str.replace("\"\"", "\"");
				}
				result.add(str.trim());
			} else {
				result.add("");
			}
		}

		return result.toArray(parsedList);
	}

	/**
	 * Parses a string into DataType enum, returns null if string does not match any of the values.
	 * 
	 * @param typeString
	 * @return DataType
	 */
	private DataType parseType(String typeString) {

		for (DataType type : DataType.values()) {
			if (type.getValue().equalsIgnoreCase(typeString)) {
				return type;
			}
		}

		return null;
	}

	/**
	 * Parses a list of keywords by searching for them in the database, makes new instance if keyword is not found.
	 * 
	 * @param unparsedList
	 * @param bean
	 * @return Set<KeywordElement>
	 * @throws IOException
	 * @throws Exception
	 */
	private Set<Keyword> parseKeywords(String unparsedList) throws IOException {

		Set<Keyword> keywordList = new HashSet<Keyword>();

		if (!ServiceConstants.EMPTY_STRING.equals(unparsedList)) {
			String[] parsedList = parseGenericList(unparsedList);

			// Create a KeywordElement object for each string in the list
			for (String parsedElement : parsedList) {
				// Search through existing keywords
				boolean keywordExists = false;
				for (Keyword keyword : this.allKeywords) {
					if (parsedElement.equalsIgnoreCase(keyword.getKeyword())) {
						keywordList.add(keyword);
						keywordExists = true;
						break;
					}
				}
				// A currently existing keyword match was not found.
				if (!keywordExists) {
					Keyword newKeyword = new Keyword();
					newKeyword.setKeyword(parsedElement);
					newKeyword.setCount(0L);
					keywordList.add(newKeyword);
				}
			}
		}

		return keywordList;
	}

	private Date parseDate(String unparsedDate) {

		if (unparsedDate.equals("")) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern("MM/dd/yyyy");
		try {
			return sdf.parse(unparsedDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Parses a list of labels by searching for them in the database, makes new instance if label is not found.
	 * 
	 * @param unparsedList
	 * @param bean
	 * @return Set<KeywordElement>
	 * @throws IOException
	 * @throws Exception
	 */
	private Set<Keyword> parseLabels(String unparsedList) throws IOException {

		Set<Keyword> labelsList = new HashSet<Keyword>();

		if (!ServiceConstants.EMPTY_STRING.equals(unparsedList)) {
			String[] parsedList = parseGenericList(unparsedList);

			// Create a KeywordElement object for each string in the list
			for (String parsedElement : parsedList) {
				// Search through existing keywords
				boolean labelExists = false;
				for (Keyword label : this.allLabels) {
					if (parsedElement.equalsIgnoreCase(label.getKeyword())) {
						labelsList.add(label);
						labelExists = true;
						break;
					}
				}
				// A currently existing keyword match was not found.
				if (!labelExists) {
					Keyword newLabel = new Keyword();
					newLabel.setKeyword(parsedElement);
					newLabel.setCount(0L);
					labelsList.add(newLabel);
				}
			}
		}

		return labelsList;
	}

	/**
	 * Trims the string if property is trimmable
	 * 
	 * @param s
	 * @param prop
	 * @return
	 */
	private String checkForTrim(String s, PropertyDescriptor prop) {

		return trimmableProperty(prop) ? s.trim() : s;
	}

	/**
	 * Checks if property is trimmable
	 * 
	 * @param s
	 * @param prop
	 * @return
	 */
	private boolean trimmableProperty(PropertyDescriptor prop) {

		return !prop.getPropertyType().getName().contains("String");
	}

	/**
	 * Overload of checkfortrim that does not check for trimmability
	 * 
	 * @param s
	 * @return
	 */
	private String checkForTrim(String s) {

		return s.trim();
	}

	/**
	 * Truncates the string to the guideline notes references limits length
	 * 
	 * @param in
	 * @return
	 */
	private String truncateString(String in) // TODO Destroy (Deprecate) This Method
	{

		if (in.length() > ServiceConstants.GUIDELINE_NOTES_REFERENCES_IMPORT_LENGTH_LIMIT) {

			logger.info("Field must not be longer than "
					+ ServiceConstants.GUIDELINE_NOTES_REFERENCES_IMPORT_LENGTH_LIMIT
					+ " characters... truncating string.");
			in = in.substring(0, ServiceConstants.GUIDELINE_NOTES_REFERENCES_IMPORT_LENGTH_LIMIT - 1);
		}

		return in;
	}

	/**
	 * 
	 * This method detects empty string arrays that are passed in from the CSV
	 * 
	 * @param lineArr
	 * @return
	 */
	private boolean emptyStringDetected(String[] lineArr) {

		int index = 0;
		boolean emptyString = true;
		while (index < lineArr.length && emptyString) {
			if (!lineArr[index].matches("^$")) {
				emptyString = false;
			}
			index++;
		}
		return emptyString;
	}

	private void cleanUpExternalIdSet(Set<ExternalId> listOfExternalIds) {

		Iterator listItr = listOfExternalIds.iterator();
		while (listItr.hasNext()) {
			ExternalId externalId = (ExternalId) listItr.next();
			if (externalId.getValue().equals("")) {
				listItr.remove();
			}
		}
	}
}
