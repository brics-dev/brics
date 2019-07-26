package gov.nih.tbi.dictionary.service;



import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.SeverityLevel;
import gov.nih.tbi.dictionary.dao.DataElementDao;
import gov.nih.tbi.dictionary.dao.SchemaDao;
import gov.nih.tbi.dictionary.model.FormStructureStandardization;
import gov.nih.tbi.dictionary.model.InstanceRequiredFor;
import gov.nih.tbi.dictionary.model.SeverityRecord;
import gov.nih.tbi.dictionary.model.hibernate.ClassificationElement;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseStructure;
import gov.nih.tbi.dictionary.model.hibernate.ExternalId;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.dictionary.model.hibernate.SubDomainElement;
import gov.nih.tbi.dictionary.service.rulesengine.RulesEngineUtils;
import gov.nih.tbi.dictionary.service.rulesengine.model.ThresholdProperty;

@Service
@Scope("singleton")
public class DictionaryEventLogService {

	@Autowired
	private DataElementDao dataElementDao;
	
	@Autowired
	private SchemaDao schemaDao;
	
	

	public String getMinorMajorChangeLog(List<SeverityRecord> severityRecords, SeverityLevel highestSeverityLevel,
			EntityType entityType) {

		String change = "";
		// used set<String> here instead of string builder to avoid any repetitive changes
		Set<String> historyRecord = new HashSet<String>();

		switch (entityType) {
			case DATA_ELEMENT:
				if (severityRecords.isEmpty())
					change = RulesEngineUtils.DE_NO_VERSIONING_CHANGE;
				else if (SeverityLevel.NEW.equals(highestSeverityLevel))
					change = RulesEngineUtils.NEW_DE;
				else
					change = getDateElementChange(severityRecords, historyRecord);
				break;

			case DATA_STRUCTURE:
				if (severityRecords.isEmpty())
					change = RulesEngineUtils.FS_NO_VERSIONING_CHANGE;
				else if (SeverityLevel.NEW.equals(highestSeverityLevel))
					change = RulesEngineUtils.NEW_FS;
				else
					change = getFormStructureChange(severityRecords, historyRecord);
				break;

			default:
				break;
		}
		return change;
	}


	private String getDateElementChange(List<SeverityRecord> severityRecords, Set<String> historyRecord) {

		for (SeverityRecord severityRecord : severityRecords) {

			String fieldName = severityRecord.getFieldName();

			if (fieldName.contains(RulesEngineUtils.BASE_CASE)) {
				historyRecord.add(getBaseCaseChange(severityRecord, fieldName));
			} else if (fieldName.contains(RulesEngineUtils.PERMISSIBLE_VALUE_DESCRIPTION)) {
				historyRecord.add(changedFromBuilder(RulesEngineUtils.PRE_DEFINED_VALUE_DESCRIPTION,
						severityRecord.getOriginalValue(), severityRecord.getChangedValue()));
			} else if (fieldName.contains(RulesEngineUtils.PERMISSIBLE_VALUE_OUTPUT_CODE)) {

				String pValue = RulesEngineUtils.parseListFieldName(fieldName);

				historyRecord.add(changedFromBuilder(RulesEngineUtils.PRE_DEFINED_OUTPUT_CODE + pValue,
						severityRecord.getOriginalValue(), severityRecord.getChangedValue()));
			} else if (fieldName.contains(RulesEngineUtils.KEYWORD) || fieldName.contains(RulesEngineUtils.LABEL)) {
				historyRecord.add(getKeyWordLabelChange(severityRecord, fieldName));
			} else if (fieldName.contains(RulesEngineUtils.CATEGORY)) {
				historyRecord.add(getCategoryChange(severityRecord));
			} else if (fieldName.contains(RulesEngineUtils.CLASSIFICATION)) {
				historyRecord.add(getClassificationChange(severityRecord));
			} else if (fieldName.contains(RulesEngineUtils.EXTERNAL_ID_FIELD)) {
				historyRecord.add(getExternalIdChange(severityRecord, fieldName));
			}
		}
		return StringUtils.join(historyRecord.toArray(), RulesEngineUtils.SPACE);
	}

	private String getFormStructureChange(List<SeverityRecord> severityRecords, Set<String> historyRecord) {

		for (SeverityRecord severityRecord : severityRecords) {

			String fieldName = severityRecord.getFieldName();

			// handle standardization change separately because the displayed and saved standardization names are
			// different
			// CRIT-6167: Change History- Incorrect standardization information recorded upon change
			if (fieldName.contains(RulesEngineUtils.BASE_CASE)
					&& !fieldName.contains(RulesEngineUtils.STANDARDIZATION)) {
				historyRecord.add(getBaseCaseChange(severityRecord, fieldName));
			} else if (fieldName.contains(RulesEngineUtils.STANDARDIZATION)) {
				historyRecord.add(getStandardizationChange(severityRecord, fieldName));
			} else if (fieldName.contains(RulesEngineUtils.PROGRAM_FORM)) {
				historyRecord.add(getProgramFormChange(severityRecord, fieldName));
			} else if (fieldName.contains(RulesEngineUtils.DISEASE)) {
				historyRecord.add(getDiseaseChange(severityRecord, fieldName));
			} else if (fieldName.contains(RulesEngineUtils.DE_OPTIONALITY)) {
				historyRecord.add(getDEOptionalityChange(severityRecord, fieldName));
			} else if (fieldName.contains(RulesEngineUtils.TYPE)) {
				historyRecord.add(getTypeThresholdChange(severityRecord, fieldName));
			}
		}
		return StringUtils.join(historyRecord.toArray(), RulesEngineUtils.SPACE);
	}

	private String getBaseCaseChange(SeverityRecord severityRecord, String fieldName) {

		String originalFieldName = fieldName.substring(RulesEngineUtils.BASE_CASE.length());

		if (fieldName.contains(RulesEngineUtils.DATE)) {
			return getDateChange(severityRecord, originalFieldName);
		} else if (fieldName.contains(RulesEngineUtils.FORM_TYPE)) {
			return getFormTypeChange(severityRecord, originalFieldName);
		} else {
			return changeStringBuilder(originalFieldName, severityRecord.getOriginalValue(),
					severityRecord.getChangedValue());
		}
	}

	private String getFormTypeChange(SeverityRecord severityRecord, String fieldName) {

		long originalNu = ((long) severityRecord.getOriginalValue());
		long changedValueNu = (long) severityRecord.getChangedValue();

		String original = "";
		switch ((int) originalNu) {
			case 0:
				original = RulesEngineUtils.CLINICAL;
				break;
			case 1:
				original = RulesEngineUtils.OMICS;
				break;
			case 2:
				original = RulesEngineUtils.IMAGING;
				break;
			case 6:
				original = RulesEngineUtils.PRECLINICAL;
				break;
		}

		String changedValue = "";
		switch ((int) changedValueNu) {
			case 0:
				changedValue = RulesEngineUtils.CLINICAL;
				break;
			case 1:
				changedValue = RulesEngineUtils.OMICS;
				break;
			case 2:
				changedValue = RulesEngineUtils.IMAGING;
				break;
			case 6:
				changedValue = RulesEngineUtils.PRECLINICAL;
				break;
		}

		return changeStringBuilder(fieldName, original, changedValue);

	}

	private String getDateChange(SeverityRecord severityRecord, String originalFieldName) {

		Date originalDate = (Date) severityRecord.getOriginalValue();
		String originalDateStr = BRICSTimeDateUtil.formatDate(originalDate);

		Date changedValueDate = (Date) severityRecord.getChangedValue();
		String changedValueStr = BRICSTimeDateUtil.formatDate(changedValueDate);

		return changeStringBuilder(originalFieldName, originalDateStr, changedValueStr);
	}


	@SuppressWarnings("unchecked")
	private String getKeyWordLabelChange(SeverityRecord severityRecord, String fieldName) {

		Set<String> originalKeyword = new HashSet<String>();
		Set<String> changedValueKeyword = new HashSet<String>();

		Set<Keyword> original = (Set<Keyword>) severityRecord.getOriginalValue();
		for (Keyword keyword : original) {
			originalKeyword.add(keyword.getKeyword());
		}

		Set<Keyword> changedValue = (Set<Keyword>) severityRecord.getChangedValue();
		for (Keyword keyword : changedValue) {
			changedValueKeyword.add(keyword.getKeyword());
		}

		return changeStringBuilder(fieldName, originalKeyword, changedValueKeyword);
	}

	@SuppressWarnings("unchecked")
	private String getCategoryChange(SeverityRecord severityRecord) {

		Set<String> originalElement = new HashSet<String>();
		Set<String> changedValueElement = new HashSet<String>();


		Set<SubDomainElement> original = (Set<SubDomainElement>) severityRecord.getOriginalValue();
		for (SubDomainElement element : original) {
			originalElement.add(element.getMultiFieldsWithDes());
		}

		Set<SubDomainElement> changedValue = (Set<SubDomainElement>) severityRecord.getChangedValue();
		for (SubDomainElement element : changedValue) {
			changedValueElement.add(element.getMultiFieldsWithDes());
		}

		return RulesEngineUtils.CATEGORY_GROUP + getChangedValue(originalElement, changedValueElement);
	}

	@SuppressWarnings("unchecked")
	private String getClassificationChange(SeverityRecord severityRecord) {

		Set<String> originalElement = new HashSet<String>();
		Set<String> changedValueElement = new HashSet<String>();

		Set<ClassificationElement> original = (Set<ClassificationElement>) severityRecord.getOriginalValue();
		for (ClassificationElement element : original) {
			originalElement.add(element.getSubgroup().getSubgroupName() + RulesEngineUtils.COLON
					+ element.getClassification().getName());
		}

		Set<ClassificationElement> changedValue = (Set<ClassificationElement>) severityRecord.getChangedValue();
		for (ClassificationElement element : changedValue) {
			changedValueElement.add(element.getSubgroup().getSubgroupName() + RulesEngineUtils.COLON
					+ element.getClassification().getName());
		}

		return RulesEngineUtils.CLASSIFICATION_CHANGE + getChangedValue(originalElement, changedValueElement);
	}

	@SuppressWarnings("unchecked")
	private String getExternalIdChange(SeverityRecord severityRecord, String fieldName) {

		Set<String> originalId = new HashSet<String>();
		Set<String> changedValueId = new HashSet<String>();

		if (severityRecord.getOriginalValue() instanceof Set<?> && severityRecord.getChangedValue() instanceof Set<?>) {
			Set<ExternalId> original = (Set<ExternalId>) severityRecord.getOriginalValue();
			for (ExternalId externalId : original) {
				originalId.add(externalId.getSchema().getName());
			}

			Set<ExternalId> changedValue = (Set<ExternalId>) severityRecord.getChangedValue();
			for (ExternalId externalId : changedValue) {
				changedValueId.add(externalId.getSchema().getName());
			}

			return changeStringBuilder(fieldName, originalId, changedValueId);
		} else if (severityRecord.getOriginalValue() instanceof String && severityRecord.getChangedValue() instanceof String) {
			String original = (String) severityRecord.getOriginalValue();
			String changedValue = (String) severityRecord.getChangedValue();
			
			List<Schema> schemas = schemaDao.getAll();

			String schemaName = "unknown schema";
			
			for(Schema schema:schemas) {
				if(fieldName.toLowerCase().contains(schema.getName().toLowerCase())) {
					schemaName = schema.getName() + " external id";
					break;
				}
			}
			
			return changeStringBuilder(schemaName, original, changedValue);
		}

		throw new UnsupportedOperationException(
				"Cannot generate change history with the given severity record.  Is it null?");
	}

	@SuppressWarnings("unchecked")
	private String getProgramFormChange(SeverityRecord severityRecord, String fieldName) {

		Set<String> original = new HashSet<String>();
		Set<String> changedValue = new HashSet<String>();

		List<InstanceRequiredFor> originalObject = (List<InstanceRequiredFor>) severityRecord.getOriginalValue();
		for (InstanceRequiredFor instanceRequiredFor : originalObject) {
			original.add(instanceRequiredFor.getName());
		}

		List<InstanceRequiredFor> changedObject = (List<InstanceRequiredFor>) severityRecord.getChangedValue();
		for (InstanceRequiredFor instanceRequiredFor : changedObject) {
			changedValue.add(instanceRequiredFor.getName());
		}

		return changeStringBuilder(fieldName, original, changedValue);
	}

	@SuppressWarnings("unchecked")
	private String getDiseaseChange(SeverityRecord severityRecord, String fieldName) {

		Set<String> original = new HashSet<String>();
		Set<String> changedValue = new HashSet<String>();

		Set<DiseaseStructure> originalObject = (Set<DiseaseStructure>) severityRecord.getOriginalValue();
		for (DiseaseStructure diseaseStructure : originalObject) {
			original.add(diseaseStructure.getDisease().getName());
		}

		Set<DiseaseStructure> changedObject = (Set<DiseaseStructure>) severityRecord.getChangedValue();
		for (DiseaseStructure diseaseStructure : changedObject) {
			changedValue.add(diseaseStructure.getDisease().getName());
		}

		return changeStringBuilder(fieldName, original, changedValue);
	}

	private String getDEOptionalityChange(SeverityRecord severityRecord, String fieldName) {

		String id = RulesEngineUtils.parseMapId(fieldName);
		Long idLong = Long.parseLong(id.trim());

		DataElement dataElement = dataElementDao.getByMapElementId(idLong);
		String change = RulesEngineUtils.DATAELEMENT + dataElement.getTitle() + RulesEngineUtils.OPTIONALITY_CHANGE;

		return changeStringBuilder(change, severityRecord.getOriginalValue(), severityRecord.getChangedValue());
	}

	private String getTypeThresholdChange(SeverityRecord severityRecord, String fieldName) {

		String groupName = RulesEngineUtils.parseListFieldName(fieldName);

		ThresholdProperty original = (ThresholdProperty) severityRecord.getOriginalValue();
		ThresholdProperty changed = (ThresholdProperty) severityRecord.getChangedValue();

		String originalRT = original.getRepeatableType().getValue();
		String changedRT = changed.getRepeatableType().getValue();

		Integer originalTR = original.getThreshold();
		Integer changedTR = original.getThreshold();

		return changeStringBuilder(
				RulesEngineUtils.TYPE_THRESHOLD + RulesEngineUtils.REPEATABLE_GROUP + groupName
						+ RulesEngineUtils.SPACE,
				originalRT + RulesEngineUtils.COMMA + originalTR, changedRT + RulesEngineUtils.COMMA + changedTR);
	}

	private String getStandardizationChange(SeverityRecord severityRecord, String fieldName) {

		String originalFieldName = fieldName.substring(RulesEngineUtils.BASE_CASE.length());

		String originalValue = (String) severityRecord.getOriginalValue();
		String displayedOriginalValue = FormStructureStandardization.getByName(originalValue).getDisplay();

		String changedValue = (String) severityRecord.getChangedValue();
		String displayedChangedValue = FormStructureStandardization.getByName(changedValue).getDisplay();

		return changeStringBuilder(originalFieldName, displayedOriginalValue, displayedChangedValue);
	}

	private String getChangedValue(Set<String> original, Set<String> changed) {

		Set<String> originalValue = new HashSet<String>();
		Set<String> changedValue = new HashSet<String>();

		originalValue.addAll(original);
		changedValue.addAll(changed);

		changedValue.removeAll(originalValue);

		if (changedValue.isEmpty()) {
			original.removeAll(changed);
			return RulesEngineUtils.SPACEQUOTATION + StringUtils.join(original.toArray(), RulesEngineUtils.COMMA)
					+ RulesEngineUtils.REMOVEDFIELD;
		} else {
			return RulesEngineUtils.SPACEQUOTATION + StringUtils.join(changedValue.toArray(), RulesEngineUtils.COMMA)
					+ RulesEngineUtils.ADDEDFIELD;
		}
	}

	private String changeStringBuilder(String fieldName, Set<String> original, Set<String> changedValue) {

		String changeString;

		String originalString = StringUtils.join(original.toArray(), RulesEngineUtils.COMMA);
		String changedValueString = StringUtils.join(changedValue.toArray(), RulesEngineUtils.COMMA);

		String parsedFieldName = RulesEngineUtils.parseFieldName(fieldName);

		if (originalString.length() != 0 && changedValueString.length() != 0) {
			return changedFromBuilder(parsedFieldName + RulesEngineUtils.LIST, originalString, changedValueString);
		} else if (changedValue.size() == 0) {
			changeString = StringUtils.join(original.toArray(), RulesEngineUtils.COMMA);
			return removedFieldBuilder(parsedFieldName, changeString);
		} else {
			changeString = StringUtils.join(changedValue.toArray(), RulesEngineUtils.COMMA);
			return addedFieldBuilder(parsedFieldName, changeString);
		}
	}

	private String changeStringBuilder(String fieldName, Object original, Object changedValue) {

		if (original != null && !original.equals(ModelConstants.EMPTY_STRING) && changedValue != null
				&& !changedValue.equals(ModelConstants.EMPTY_STRING)) {
			return changedFromBuilder(fieldName, original, changedValue);
		} else if (original == null || original.equals(ModelConstants.EMPTY_STRING)) {
			return setToBuilder(fieldName, changedValue);
		} else {
			return removedFieldBuilder(fieldName, original);
		}
	}

	private String addedFieldBuilder(String fieldName, Object changedValue) {

		if (fieldName.contains(RulesEngineUtils.BASE_CASE)) {
			return setToBuilder(fieldName, changedValue);
		} else {
			return fieldName + RulesEngineUtils.SPACEQUOTATION + changedValue + RulesEngineUtils.ADDEDFIELD;
		}
	}

	private String changedFromBuilder(String fieldName, Object originalString, Object changedValueString) {

		if (fieldName.contains(RulesEngineUtils.DATAELEMENT) || fieldName.contains(RulesEngineUtils.REPEATABLE_GROUP)) {
			return RulesEngineUtils.EDITED + fieldName + RulesEngineUtils.FROM + originalString.toString().toLowerCase()
					+ RulesEngineUtils.TO + changedValueString.toString().toLowerCase() + RulesEngineUtils.FULLSTOP;
		} else {
			return RulesEngineUtils.EDITED + fieldName.toLowerCase() + RulesEngineUtils.FROM + originalString
					+ RulesEngineUtils.TO + changedValueString + RulesEngineUtils.FULLSTOP;
		}
	}

	private String setToBuilder(String fieldName, Object changedValue) {

		return fieldName + RulesEngineUtils.SET_TO + changedValue + RulesEngineUtils.FULLSTOP;
	}

	private String removedFieldBuilder(String listName, Object changedValue) {

		return listName + RulesEngineUtils.SPACEQUOTATION + changedValue + RulesEngineUtils.REMOVEDFIELD;
	}

}
