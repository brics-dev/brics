package gov.nih.tbi.dictionary.service.rulesengine;

import gov.nih.tbi.commons.model.RepeatableType;
import gov.nih.tbi.commons.model.RequiredType;
import gov.nih.tbi.commons.model.SeverityLevel;
import gov.nih.tbi.dictionary.model.SeverityRecord;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.service.rulesengine.model.ChangeSeverity;
import gov.nih.tbi.dictionary.service.rulesengine.model.DataElementOptionality;
import gov.nih.tbi.dictionary.service.rulesengine.model.FieldList;
import gov.nih.tbi.dictionary.service.rulesengine.model.IncomingOptionality;
import gov.nih.tbi.dictionary.service.rulesengine.model.InvalidOperationException;
import gov.nih.tbi.dictionary.service.rulesengine.model.Operation;
import gov.nih.tbi.dictionary.service.rulesengine.model.OriginalOptionality;
import gov.nih.tbi.dictionary.service.rulesengine.model.Rule;
import gov.nih.tbi.dictionary.service.rulesengine.model.RulesEngineException;
import gov.nih.tbi.dictionary.service.rulesengine.model.Threshold;
import gov.nih.tbi.dictionary.service.rulesengine.model.ThresholdChange;
import gov.nih.tbi.dictionary.service.rulesengine.model.ThresholdProperty;
import gov.nih.tbi.dictionary.service.rulesengine.model.ThresholdPropertyRules;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * The RulesEngine Operation Handler handles all the compare-based operations the Rules Engine relies on in order to
 * determine the equivalence of two objects.
 * 
 * Much like the Rules Engine itself, the RulesEngine Operation will be a black box to the Rules Engine. The Rules
 * Engine should be able to pass in two object regardless of their object and the operation that needs to performed, and
 * the Rules Engine Operation Handler should be able to return a Severity Record.
 * 
 * 
 * Objects to be compared
 * 
 * Data Element
 * 
 * String - Title Description Short Description Notes Suggested Question Text Guidelines Historical Notes
 * 
 * 
 * Long - Size Maximum Value Minimum Value
 * 
 * Data Type
 * 
 * Input Restrictions
 * 
 * Range Values
 * 
 * Measuring Unit
 * 
 * ExternalID
 * 
 * SubDomain & Domain (DiseaseElement)
 * 
 * Classification (ClassificationElement)
 * 
 * Population
 * 
 * 
 * Form Structure
 * 
 * String - Title, Description, Organization
 * 
 * Submission
 * 
 * Disease
 * 
 * Repeatable Group - Property , Sequence , Data Element (Short Name)
 * 
 * @author mgree1
 * 
 */
public class RulesEngineOperationHandler implements OperationVisitor {

	public static final String EVALUATESTRING = "evaluateString";
	public static final String EVALUATEINTEGER = "evaluateInteger";
	public static final String EVALUATELONG = "evaluateLong";
	public static final String EVALUATEBIGDECIMAL = "evaluateBigDecimal";
	public static final String EVALUATELIST = "evaluateList";
	public static final String EVALUATETHRESHOLDPROPERTY = "evaluateThresholdProperty";

	/**
	 * 
	 * @param original
	 * @param incoming
	 * @param fieldName
	 * @param rule
	 * @param dataDictionaryObject
	 * @return
	 * @throws RulesEngineException
	 * @throws InvalidOperationException
	 */
	public SeverityRecord evaluateString(String original, String incoming, String fieldName, Rule rule,
			String dataDictionaryObject) throws RulesEngineException, InvalidOperationException {

		SeverityRecord sr = null;
		if (original != null && incoming != null) {
			Operation stringOperand = rule.getOperation();
			switch (stringOperand) {
				case NOT_EQUAL:
					if (!original.equals(incoming)) {
						sr =
								generateSeverityRecord(fieldName, original, incoming, rule.getSeverity(),
										dataDictionaryObject);
					}
					break;
				case LESS_THAN:
				case GREATER_THAN:
				case ADDED:
				case REMOVE:
				case SEQUENCE:
				default:
					throw new InvalidOperationException(RulesEngineConstants.INVALID_OPERATOR + " " + fieldName);
			}
		} else {
			// specific case where empty strings or null

			if ((original == null || original.isEmpty()) && (incoming != null && !incoming.isEmpty())) {
				sr = generateSeverityRecord(fieldName, original, incoming, rule.getSeverity(), dataDictionaryObject);

			} else if ((incoming == null || incoming.isEmpty()) && (original != null && !original.isEmpty()))

			{
				sr = generateSeverityRecord(fieldName, original, incoming, rule.getSeverity(), dataDictionaryObject);
			}
		}
		return sr;

	}

	/**
	 * 
	 * @param original
	 * @param incoming
	 * @param fieldName
	 * @param rule
	 * @param dataDictionaryObject
	 * @return
	 * @throws RulesEngineException
	 * @throws InvalidOperationException
	 */
	public SeverityRecord evaluateInteger(Integer original, Integer incoming, String fieldName, Rule rule,
			String dataDictionaryObject) throws RulesEngineException, InvalidOperationException {

		SeverityRecord sr = null;
		if (original != null && incoming != null) {
			Operation stringOperand = rule.getOperation();
			switch (stringOperand) {
				case NOT_EQUAL:
					if (original.compareTo(incoming) != 0) {
						sr =
								generateSeverityRecord(fieldName, original, incoming, rule.getSeverity(),
										dataDictionaryObject);
					}
					break;
				case LESS_THAN:
					if (original.compareTo(incoming) == -1) {
						sr =
								generateSeverityRecord(fieldName, original, incoming, rule.getSeverity(),
										dataDictionaryObject);
					}
					break;
				case GREATER_THAN:
					if (original.compareTo(incoming) == 1) {
						sr =
								generateSeverityRecord(fieldName, original, incoming, rule.getSeverity(),
										dataDictionaryObject);
					}
					break;
				case ADDED:
				case REMOVE:
				case SEQUENCE:
				default:

					throw new InvalidOperationException(RulesEngineConstants.INVALID_OPERATOR + " " + fieldName);
			}
		} else {
			if ((original == null && incoming != null) || (incoming == null && original != null)) {
				sr = generateSeverityRecord(fieldName, original, incoming, rule.getSeverity(), dataDictionaryObject);

			}
		}
		return sr;
	}

	public SeverityRecord evaluateLong(Long original, Long incoming, String fieldName, Rule rule,
			String dataDictionaryObject) throws RulesEngineException, InvalidOperationException {

		SeverityRecord sr = null;
		if (original != null && incoming != null) {
			Operation stringOperand = rule.getOperation();
			switch (stringOperand) {
				case NOT_EQUAL:
					if (original.compareTo(incoming) != 0) {
						sr =
								generateSeverityRecord(fieldName, original, incoming, rule.getSeverity(),
										dataDictionaryObject);
					}
					break;
				case LESS_THAN:
					if (original < incoming) {
						sr =
								generateSeverityRecord(fieldName, original, incoming, rule.getSeverity(),
										dataDictionaryObject);
					}
					break;
				case GREATER_THAN:
					if (original > incoming) {
						sr =
								generateSeverityRecord(fieldName, original, incoming, rule.getSeverity(),
										dataDictionaryObject);
					}
					break;
				case ADDED:
				case REMOVE:
				case SEQUENCE:
				default:

					throw new InvalidOperationException(RulesEngineConstants.INVALID_OPERATOR + " " + fieldName);
			}
		} else {
			if ((original == null && incoming != null) || (incoming == null && original != null)) {
				sr = generateSeverityRecord(fieldName, original, incoming, rule.getSeverity(), dataDictionaryObject);

			}
		}
		return sr;
	}

	public SeverityRecord evaluateDate(Date original, Date incoming, String fieldName, Rule rule,
			String dataDictionaryObject) throws RulesEngineException, InvalidOperationException {

		SeverityRecord sr = null;
		if (original != null && incoming != null) {
			Operation stringOperand = rule.getOperation();
			switch (stringOperand) {
				case NOT_EQUAL:
					Calendar originalCal = Calendar.getInstance();
					Calendar incomingCal = Calendar.getInstance();
					originalCal.setTime(original);
					incomingCal.setTime(incoming);
					if (!compareDates(originalCal, incomingCal)) {
						sr =
								generateSeverityRecord(fieldName, original, incoming, rule.getSeverity(),
										dataDictionaryObject);
					}
					break;
				case LESS_THAN:
				case GREATER_THAN:
				case ADDED:
				case REMOVE:
				case SEQUENCE:
				default:

					throw new InvalidOperationException(RulesEngineConstants.INVALID_OPERATOR + " " + fieldName);
			}
		} else {
			if ((original == null && incoming != null) || (incoming == null && original != null)) {
				sr = generateSeverityRecord(fieldName, original, incoming, rule.getSeverity(), dataDictionaryObject);

			}
		}
		return sr;
	}

	/**
	 * @param originalCal
	 * @param incomingCal
	 */
	private boolean compareDates(Calendar originalCal, Calendar incomingCal) {

		int originalDay = originalCal.get(Calendar.DAY_OF_MONTH);
		int incomingDay = incomingCal.get(Calendar.DAY_OF_MONTH);
		if (originalDay != incomingDay) {
			return false;
		}

		int originalMonth = originalCal.get(Calendar.MONTH);
		int incomingMonth = incomingCal.get(Calendar.MONTH);
		if (originalMonth != incomingMonth) {
			return false;
		}

		int originalYear = originalCal.get(Calendar.YEAR);
		int incomingYear = incomingCal.get(Calendar.YEAR);
		if (originalYear != incomingYear) {
			return false;
		}
		return true;
	}

	public SeverityRecord evaluateBigDecimal(BigDecimal original, BigDecimal incoming, String fieldName, Rule rule,
			String dataDictionaryObject) throws RulesEngineException, InvalidOperationException {

		SeverityRecord sr = null;
		if (original != null && incoming != null) {
			Operation stringOperand = rule.getOperation();
			switch (stringOperand) {
				case NOT_EQUAL:
					if (original.compareTo(incoming) != 0) {
						sr =
								generateSeverityRecord(fieldName, original, incoming, rule.getSeverity(),
										dataDictionaryObject);
					}
					break;
				case LESS_THAN:
					if (original.compareTo(incoming) == -1) {
						sr =
								generateSeverityRecord(fieldName, original, incoming, rule.getSeverity(),
										dataDictionaryObject);
					}
					break;
				case GREATER_THAN:
					if (original.compareTo(incoming) == 1) {
						sr =
								generateSeverityRecord(fieldName, original, incoming, rule.getSeverity(),
										dataDictionaryObject);
					}
					break;
				case ADDED:
				case REMOVE:
				case SEQUENCE:
				default:

					throw new InvalidOperationException(RulesEngineConstants.INVALID_OPERATOR + " " + fieldName);
			}
		} else {
			if ((original == null && incoming != null) || (incoming == null && original != null)) {
				sr = generateSeverityRecord(fieldName, original, incoming, rule.getSeverity(), dataDictionaryObject);

			}
		}
		return sr;

	}

	@SuppressWarnings("unchecked")
	public SeverityRecord evaluateList(Collection<?> original, Collection<?> incoming, String fieldName, Rule rule,
			FieldList fieldList, String dataDictionaryObject) throws InvalidOperationException, RulesEngineException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		SeverityRecord sr = null;
		StringBuilder fieldNameMessageConstructor = new StringBuilder();
		if (original != null && incoming != null) {
			Operation stringOperand = rule.getOperation();
			try {
				switch (stringOperand) {
					case ADDED:
						if (evaluateCollectionAddition(original, incoming, fieldList)) {
							fieldNameMessageConstructor.append(fieldName
									+ RulesEngineConstants.RULES_ENGINE_OPERAND_SEPARATOR + RulesEngineUtils.ADDED);
							sr =
									generateSeverityRecord(fieldNameMessageConstructor.toString(), original, incoming,
											rule.getSeverity(), dataDictionaryObject);
						}
						break;
					case REMOVE:
						if (evaluateCollectionDeletion(original, incoming, fieldList)) {
							fieldNameMessageConstructor.append(fieldName
									+ RulesEngineConstants.RULES_ENGINE_OPERAND_SEPARATOR + RulesEngineUtils.REMOVED);
							sr =
									generateSeverityRecord(fieldNameMessageConstructor.toString(), original, incoming,
											rule.getSeverity(), dataDictionaryObject);
						}
						break;
					case SEQUENCE:
						boolean badSequence = false;
						String typeOfField = fieldList.getType().substring((fieldList.getType().lastIndexOf('.') + 1));
						if (typeOfField.equalsIgnoreCase("repeatablegroup")) {
							Collection<RepeatableGroup> originalRGColl = (Collection<RepeatableGroup>) original;
							Collection<RepeatableGroup> incomingRGColl = (Collection<RepeatableGroup>) incoming;

							if (!evaluateRepeatableGroupSequence(originalRGColl, incomingRGColl, fieldList)) {
								badSequence = true;
							}
						} else {
							if (!evaluateCollectionSequence(original, incoming, fieldList)) {
								badSequence = true;
							}
						}
						if (badSequence) {
							fieldNameMessageConstructor.append(fieldName
									+ RulesEngineConstants.RULES_ENGINE_OPERAND_SEPARATOR + RulesEngineUtils.SEQUENCE);
							sr =
									generateSeverityRecord(fieldNameMessageConstructor.toString(), original, incoming,
											rule.getSeverity(), dataDictionaryObject);
						}

						break;
					case NOT_EQUAL:
					case LESS_THAN:
					case GREATER_THAN:
					default:

						throw new InvalidOperationException(RulesEngineConstants.INVALID_OPERATOR + " " + fieldName);
				}
			} catch (ClassNotFoundException e) {
				throw new RulesEngineException(RulesEngineConstants.INVALID_LIST_OBJECT_KEY + "Field:" + fieldName
						+ " Field Key: " + fieldList.getKey());
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				throw new RulesEngineException(RulesEngineConstants.INVALID_LIST_OBJECT_KEY + "Field:" + fieldName
						+ " Field: " + fieldList.getKey());
			}
		} else {
			sr = generateSeverityRecord(fieldName, original, incoming, rule.getSeverity(), null);
		}
		return sr;
	}

	/**
	 * 
	 * @param original
	 * @param incoming
	 * @param subField
	 * @param rule
	 * @param dataDictionaryObject
	 * @return
	 * @throws InvalidOperationException
	 * @throws RulesEngineException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public SeverityRecord evaluateThresholdProperty(ThresholdProperty original, ThresholdProperty incoming,
			String fieldName, Rule rule, String dataDictionaryObject) throws RulesEngineException {

		SeverityRecord sr = null;
		ThresholdPropertyRules thresholdPropertyRules = rule.getThresholdPropertiesRules();
		if (thresholdPropertyRules == null) {
			throw new RulesEngineException(RulesEngineConstants.REPEATABLE_GROUP_THRESHOLD_FAILURE);
		}
		if (original.getRepeatableType().compareTo(incoming.getRepeatableType()) == 0) {
			if (original.getThreshold().compareTo(incoming.getThreshold()) == 0) {
				return sr;
			}
		}
		for (Threshold threshold : thresholdPropertyRules.getThreshold()) {
			if (threshold.getName().equalsIgnoreCase(original.getRepeatableType().getValue())) {
				for (ThresholdChange changeToThreshold : threshold.getThresholdChange()) {
					if (changeToThreshold.getName().equalsIgnoreCase(incoming.getRepeatableType().getValue())) {
						ChangeSeverity changeSeverity = changeToThreshold.getChangeSeverity();
						if (original.getThreshold().compareTo(incoming.getThreshold()) == 0) {
							sr =
									generateSeverityRecord(fieldName, original, incoming, changeSeverity.getNoChange(),
											dataDictionaryObject);
						} else if (original.getThreshold().compareTo(incoming.getThreshold()) == 1) {

							sr =
									generateSeverityRecord(fieldName, original, incoming, changeSeverity.getDecrease(),
											dataDictionaryObject);
						} else if (original.getThreshold().compareTo(incoming.getThreshold()) == -1) {
							sr =
									generateSeverityRecord(fieldName, original, incoming, changeSeverity.getIncrease(),
											dataDictionaryObject);
						}
					}
				}
			}
		}

		return sr;
	}

	public SeverityRecord evaluateRequiredType(RequiredType original, RequiredType incoming, String fieldName,
			Rule rule, String dataDictionaryObject) throws RulesEngineException {

		SeverityRecord sr = null;
		DataElementOptionality dataElementRules = rule.getDataElementOptionality();
		if (dataElementRules == null) {
			throw new RulesEngineException(RulesEngineConstants.DE_OPTIONALITY_FAILURE);
		}
		if (original.compareTo(incoming) == 0) {
			return sr;
		}
		for (OriginalOptionality oo : dataElementRules.getOriginalOptionality()) {
			if (oo.getName().equals(original.getValue())) {
				for (IncomingOptionality io : oo.getIncomingOptionality()) {
					if (io.getName().equals(incoming.getValue())) {
						sr =
								generateSeverityRecord(fieldName, original, incoming, io.getSeverity(),
										dataDictionaryObject);
					}
				}
			}
		}

		return sr;
	}

	@Override
	/***
	 * 
	 * The evaluate method is responsible for calling the other evaluate methods in the Rules Engine Operation Handler.
	 * 
	 * The FieldList will be used to determine whether or not evaluateList needs to call for list
	 * 
	 */
	public SeverityRecord evaluate(Object original, Object incoming, StringBuilder fieldName, Rule rule,
			FieldList fieldList, String dataDictionaryObject) throws InvalidOperationException, RulesEngineException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		SeverityRecord sr = null;
		if (fieldList != null) {
			Collection<?> orginalList = (Collection<?>) original;
			Collection<?> incomingList = (Collection<?>) incoming;

			return evaluateList(orginalList, incomingList, fieldName.toString(), rule, fieldList, dataDictionaryObject);
		}
		Object objectToEvaluate = null;
		if (original != null) {
			objectToEvaluate = original;
		} else {
			objectToEvaluate = incoming;
		}

		String className = objectToEvaluate.getClass().getName();

		// TODO Remove reflection use switch or if else

		if (className.equalsIgnoreCase(String.class.getName())) {
			sr = evaluateString((String) original, (String) incoming, fieldName.toString(), rule, dataDictionaryObject);
		} else if (className.equalsIgnoreCase(Integer.class.getName())) {
			sr =
					evaluateInteger((Integer) original, (Integer) incoming, fieldName.toString(), rule,
							dataDictionaryObject);
		} else if (className.equalsIgnoreCase(Long.class.getName())) {
			sr = evaluateLong((Long) original, (Long) incoming, fieldName.toString(), rule, dataDictionaryObject);
		} else if (className.equalsIgnoreCase(BigDecimal.class.getName())) {
			sr =
					evaluateBigDecimal((BigDecimal) original, (BigDecimal) incoming, fieldName.toString(), rule,
							dataDictionaryObject);
		} else if (className.equalsIgnoreCase(ThresholdProperty.class.getName())) {
			sr =
					evaluateThresholdProperty((ThresholdProperty) original, (ThresholdProperty) incoming,
							fieldName.toString(), rule, dataDictionaryObject);
		} else if (className.equalsIgnoreCase(RequiredType.class.getName())) {
			sr =
					evaluateRequiredType((RequiredType) original, (RequiredType) incoming, fieldName.toString(), rule,
							dataDictionaryObject);
		} else if (className.equalsIgnoreCase(Date.class.getName())) {
			sr = evaluateDate((Date) original, (Date) incoming, fieldName.toString(), rule, dataDictionaryObject);
		} else {
			throw new RulesEngineException(RulesEngineConstants.RULES_ENGINE_EVALUATE_ERROR + fieldName.toString());
		}

		return sr;
	}

	/******************************************************************************************************************************************
	 * 
	 * 
	 * Helper Methods
	 * 
	 * 
	 * These methods assist in performing redundant tasks
	 * 
	 * 
	 * 
	 * 
	 *******************************************************************************************************************************************/

	/**
	 * The purpose of this function is to determine whether the Incoming added a new element. The Field List object
	 * provides the key value which the comparison between collection objects is performed on.
	 * 
	 * Returns True if there has been addition made
	 * 
	 * @param original
	 * @param incoming
	 * @param fl
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws RulesEngineException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	public Boolean evaluateCollectionAddition(Collection<?> original, Collection<?> incoming, FieldList fieldList)
			throws ClassNotFoundException, NoSuchMethodException, RulesEngineException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {

		for (Object inc : incoming) {
			Boolean found = false;
			Iterator<Object> it = (Iterator<Object>) original.iterator();
			while (!found && it.hasNext()) {
				Object org = it.next();

				Object orgValue = null;
				Object incValue = null;
				
				//if the key is null or empty there is no wrapper class
				if(fieldList.getKey() != null || !fieldList.getKey().isEmpty()){
					orgValue = retrieveFieldObjectForComparison(org, fieldList.getKey());
					incValue = retrieveFieldObjectForComparison(inc, fieldList.getKey());
				} else {
					orgValue = org;
					incValue = inc;
				}

				if (visitCompare(orgValue, incValue)) {
					found = true;
				}
			}
			if (!found) {
				return true;
			}
		}

		return false;
	}

	/**
	 * The purpose of this method is to determine whether or not something has been deleted. The Field List object
	 * provides the key value which the comparison between collection objects is performed on.
	 * 
	 * Returns true if an object is deleted
	 * 
	 * @param original
	 * @param incoming
	 * @param fl
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws RulesEngineException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	public Boolean evaluateCollectionDeletion(Collection<?> original, Collection<?> incoming, FieldList fieldList)
			throws ClassNotFoundException, SecurityException, NoSuchMethodException, RulesEngineException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		// it.
		for (Object org : original) {
			Boolean found = false;
			Iterator<Object> it = (Iterator<Object>) incoming.iterator();
			while (!found && it.hasNext()) {
				Object inc = it.next();
				Object orgValue = retrieveFieldObjectForComparison(org, fieldList.getKey());
				Object incValue = retrieveFieldObjectForComparison(inc, fieldList.getKey());

				if (visitCompare(orgValue, incValue)) {
					found = true;
				}
			}
			if (!found) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The purpose of this method is to determine whether or not the sequence of object between two list is the same.The
	 * Field List object provides the key value which the comparison between collection objects is performed on.
	 * 
	 * 
	 * Returns True if both lists are in the same sequence
	 * 
	 * Returns False if list are out of Sequence
	 * 
	 * @param original
	 * @param incoming
	 * @param fl
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws RulesEngineException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public Boolean evaluateCollectionSequence(Collection<?> original, Collection<?> incoming, FieldList fieldList)
			throws ClassNotFoundException, SecurityException, NoSuchMethodException, RulesEngineException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		// it.
		if (original.size() != incoming.size()) {
			return false;
		}
		Iterator<Object> orgIt = (Iterator<Object>) original.iterator();
		Iterator<Object> incIt = (Iterator<Object>) incoming.iterator();

		while (orgIt.hasNext() && incIt.hasNext()) {
			Object org = orgIt.next();
			Object inc = incIt.next();

			Object orgValue = retrieveFieldObjectForComparison(org, fieldList.getKey());
			Object incValue = retrieveFieldObjectForComparison(inc, fieldList.getKey());

			if (!visitCompare(orgValue, incValue)) {
				return false;
			}

		}

		return true;
	}

	public Boolean evaluateRepeatableGroupSequence(Collection<RepeatableGroup> original,
			Collection<RepeatableGroup> incoming, FieldList fieldList) throws IllegalArgumentException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException, RulesEngineException {

		if (original.size() != incoming.size()) {
			return false;
		}
		for (RepeatableGroup originalRG : original) {
			Boolean sequence = false;
			Iterator<RepeatableGroup> it = (Iterator<RepeatableGroup>) incoming.iterator();
			while (!sequence && it.hasNext()) {
				RepeatableGroup incomingRG = it.next();
				Object orgValue = retrieveFieldObjectForComparison(originalRG, fieldList.getKey());
				Object incValue = retrieveFieldObjectForComparison(incomingRG, fieldList.getKey());

				if (visitCompare(orgValue, incValue)) { // Finding Matching id's to find RGs

					if (!incomingRG.getPosition().equals(originalRG.getPosition())) {// If an id matches then we know
																						// that it
																						// is the same RG
																						// If the position is the same
																						// then the sequence is good for
																						// that particular RG

						return false; // If the po
					} else {
						sequence = true;
					}
				}
			}
			if (!sequence) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Visitor much like the evaluate() method, visitCompare uses reflection to call the correct compare method
	 * 
	 * @param original
	 * @param incoming
	 * @return
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws RulesEngineException
	 */
	public boolean visitCompare(Object original, Object incoming) throws NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException, RulesEngineException {

		String className = original.getClass().getName();
		Boolean compare = null;

		if (className.equalsIgnoreCase(Integer.class.getName())) // TODO: Change Method name to class name and match
																	// base on Class Name
		{
			compare = compareInteger((Integer) original, (Integer) incoming);
		} else if (className.equalsIgnoreCase(Long.class.getName())) {
			compare = compareLong((Long) original, (Long) incoming);
		} else if (className.equalsIgnoreCase(String.class.getName())) {
			compare = compareString((String) original, (String) incoming);
		} else {
			throw new RulesEngineException(RulesEngineConstants.RULES_ENGINE_COMPARE_ERROR + original.getClass());
		}

		/*
		 * Method compareMethod = getClass().getMethod(methodName, new Class[] { original.getClass(),
		 * original.getClass() });
		 * 
		 * compare = (Boolean) compareMethod.invoke(this, new Object[] { original, incoming });
		 */

		return compare;
	}

	public boolean compareInteger(Integer originalKey, Integer incomingKey) {

		return (originalKey.equals(incomingKey));
	}

	public boolean compareLong(Long orignalKey, Long incomingKey) {

		return (orignalKey.compareTo(incomingKey) == 0);

	}

	public boolean compareString(String originalKey, String incomingKey) {

		return (originalKey.equals(incomingKey));

	}

	/**
	 * Retrieves an object based on the passed in object class and path.
	 * 
	 * @param object
	 * @param accessorPath
	 * @return
	 * @throws NoSuchMethodException
	 * @throws RulesEngineException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public Object retrieveFieldObjectForComparison(Object object, String accessorPath) throws NoSuchMethodException,
			RulesEngineException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		if (accessorPath.equalsIgnoreCase("Type/Threshold")) {
			if (object instanceof RepeatableGroup)
				return createThresholdProprtyFromField((RepeatableGroup) object, accessorPath);
			else
				throw new RulesEngineException(RulesEngineConstants.INVALID_LIST_OBJECT_KEY); // TODO: Remove if
																								// else
		}
		String[] accessorPathParts = accessorPath.split("\\.");
		for (int i = 0; i < accessorPathParts.length; i++) {
			if (object == null)
				return object;
			try {
				object = extractGetMethod(accessorPathParts[i], object.getClass()).invoke(object);
			} catch (NullPointerException e) {
				System.out.println(accessorPath);
				throw new RulesEngineException(e.getMessage());
			}

		}
		return object;
	}

	public ThresholdProperty createThresholdProprtyFromField(RepeatableGroup object, String accessorPath)
			throws NoSuchMethodException, RulesEngineException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {

		String[] forwardSlashParts = accessorPath.split("\\/");
		ThresholdProperty thresholdProperty = new ThresholdProperty();
		thresholdProperty.setRepeatableType((RepeatableType) extractGetMethod(forwardSlashParts[0], object.getClass())
				.invoke(object));
		thresholdProperty.setThreshold((Integer) extractGetMethod(forwardSlashParts[1], object.getClass()).invoke(
				object));

		return thresholdProperty;
	}

	/**
	 * Retrieves the a getMethod that will be use to return key values (Shortname, Id, etc) from objects that reside in
	 * a list
	 * 
	 * @param fl
	 * @param classType
	 * @return
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 */
	public Method extractGetMethod(String key, Class objectClass) throws NoSuchMethodException {

		// TODO Pass in the Class not the string

		String getMethodName = "get" + key;
		Method getMethod = objectClass.getMethod(getMethodName);
		return getMethod;
	}

	/**
	 * Generates a Severity Record and stores values to its fields, including the Field being changed, the original
	 * value and the incoming value.
	 * 
	 * @param fieldName
	 * @param originalDEFieldValue
	 * @param incomingDEFieldValue
	 * @param dataDictionaryObject TODO
	 * @throws RulesEngineException
	 */
	public SeverityRecord generateSeverityRecord(String fieldName, Object originalDEFieldValue,
			Object incomingDEFieldValue, String sl, String dataDictionaryObject) throws RulesEngineException {

		SeverityRecord sr = new SeverityRecord();
		sr.setFieldName(fieldName);
		sr.setOriginalValue(originalDEFieldValue);
		sr.setChangedValue(incomingDEFieldValue);
		sr.setSeverityLevel(stringToSeverityLevel(sl, fieldName));
		sr.setDataDictionaryObject(dataDictionaryObject);
		return sr;
	}

	/**
	 * Takes in a string that represents a Severity Level and matches it to a Severity Level. Will return an exception
	 * if the Severity Level string doesn't have a match.
	 * 
	 * @param severityLevel
	 * @param field
	 * @return
	 * @throws RulesEngineException
	 */
	public SeverityLevel stringToSeverityLevel(String severityLevel, String field) throws RulesEngineException {

		for (SeverityLevel s : SeverityLevel.values()) {
			if (s.name().equalsIgnoreCase(severityLevel))

				return s;
		}
		throw new RulesEngineException(RulesEngineConstants.INVALID_SEVERITY_LEVEL + "Field: " + field + " Severity: "
				+ severityLevel);
	}
}
