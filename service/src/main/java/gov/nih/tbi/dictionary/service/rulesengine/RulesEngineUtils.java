
package gov.nih.tbi.dictionary.service.rulesengine;

import gov.nih.tbi.commons.model.RepeatableType;
import gov.nih.tbi.commons.model.SeverityLevel;
import gov.nih.tbi.dictionary.model.SeverityRecord;
import gov.nih.tbi.dictionary.service.rulesengine.model.ThresholdProperty;

/**
 * 
 * The purpose of the Rules Engine String
 * 
 * @author mgree1
 * 
 */
public class RulesEngineUtils
{

    public static StringBuilder messageBuilder;

    /**************************************************************************************
     * 
     * Field Value Text Strings
     * 
     *************************************************************************************/

    /* Data Dictionary Object Strings*/
    public final static String FORM_STRUCTURE = "Form Structure";
    public final static String DATA_ELEMENT = "Data Element";

    /*Prefixes*/
    public final static String YOUR = "Your";
    public final static String YOUR_CHANGE = YOUR + " change ";
    public final static String YOUR_CHANGE_RESULTED = YOUR_CHANGE + "resulted in a ";
    public final static String YOUR_CHANGE_THRESHOLD = YOUR_CHANGE + "to the threshold of Data Element group ";
    public final static String INC_FREQ = "Increasing Frequency: ";
    public final static String DEC_FREQ = "Decreasing Frequency: ";
    public final static String NC_FREQ = "No Change to Frequency: ";

    /*Suffixes*/
    public final static String TO_FORM_STRUCTURE = " to the " + FORM_STRUCTURE + ".";
    public final static String TO_DATA_ELEMENT = " to the " + DATA_ELEMENT + ".";
    public final static String CREATION_OF_NEW = " requires the creation of a new ";

    /*Cases for Strings*/
    public final static String BASE_CASE = "Base:";
    public final static String LIST_CASE = "List:";
    public final static String LIST_ITEM_CASE = "List Item:";
    public final static String SUB_CASE = "Subfield:";
    public final static String ID = "ID:";
    public final static String EXTERNAL_ID = "External ID:";
    public final static String RG_DISPLAY_NAME = "Element Group";
    public final static String TYPE_THRESHOLD = "Type/Threshold";
    public final static String THRESHOLD_FREQUENCY = "Threshold and Frequency";

    public final static String PERMISSIBLE_VALUE_DESCRIPTION = "Value Range Description";
    public final static String PERMISSIBLE_VALUE_OUTPUT_CODE = "Value Range Output Code";

    /*Special Case  Strings*/
    public final static String DE_OPT = "Data Element Optionality: ";
    public final static String DE_REQ_TO_OPTREC = DE_OPT
            + "Your change of data element optionality from required to recommended/optional resulted in a major version change to the form structure.";
    public final static String DE_OPTREC_TO_REQ = DE_OPT
            + "Your change of data element optionality from recommended/optional to required requires a new form structure.";
    public final static String DE_OPT_TO_REC = DE_OPT
            + "Your change of data element optionality from optional to recommended requires a minor version change.";
    public final static String DE_REC_TO_OPT = DE_OPT
            + "Your change of data element optionality from recommended to optional requires a minor version change.";

    public final static String FIELD_HAS_A_SUBFIELD = " has a Subfield called ";
    public final static String FIELD_HAS_BEEN = " has been ";
    public final static String FIELD_AND_SEVERITY = " and the severity is ";
    public final static String FIELD_VALUE_CHANGED = FIELD_HAS_BEEN + "changed" + FIELD_AND_SEVERITY;
    public final static String SUBFIELD_VALUE_CHANGED = " that" + FIELD_VALUE_CHANGED;

    public final static String ADDED = "Added";
    public final static String REMOVED = "Removed";
    public final static String SEQUENCE = "Sequence";
    
    public final static String PRE_DEFINED_VALUE_DESCRIPTION ="Pre defined value description";
    public final static String PRE_DEFINED_OUTPUT_CODE ="Pre defined output code of permissible value ";
    public final static String TO ="\" to \"";
    public final static String FROM =" from \"";
    public final static String EDITED="Edited the ";
    public final static String CHANGED_TO =" changed to ";
    public final static String SET_TO=" set to \"";
    public final static String KEYWORD ="Keyword";
    public final static String PROGRAM_FORM ="Required Program Form";
    public final static String DISEASE ="Disease";
    public final static String TYPE ="Type/Threshold";
    public final static String FORM_TYPE = "Form Type";
    public final static String REPEATABLE_GROUP =" of the repeatable group ";
    public final static String DE_OPTIONALITY ="Data Element Optionality";
    public final static String OPTIONALITY_CHANGE ="'s optionality ";
    public final static String DATAELEMENT="Data Element ";
    public final static String LABEL ="Label";
    public final static String CATEGORY ="Category";
    public final static String CLASSIFICATION ="Classification";
    public final static String EXTERNAL_ID_FIELD ="External Id";
    public final static String CLASSIFICATION_CHANGE ="Edited Classification(s):-";
    public final static String CATEGORY_GROUP ="Edited Category group (s):-";
    public final static String ADD_S =" (s)";
    public final static String ADDED_REMOVED =" added/removed: ";
    public final static String ADDED_LIST =" added:";
    public final static String LIST =" list";
    public final static String SPACEQUOTATION =" \"";
    public final static String SPACE =" ";
    public final static String NEW_DE =" New data element created from changes on already existing data element.";
    public final static String NEW_FS =" New form structure created from changes on already existing form structure.";
    public final static String DE_NO_VERSIONING_CHANGE =" Edited Data Element with no minor or major change. E.g. Added/removed documentation. ";
    public final static String FS_NO_VERSIONING_CHANGE = " Edited Form Structure with no minor or major change. E.g. Added/removed documentation. ";
    public final static String DATE ="Date";
    public final static String ADDEDFIELD ="\" added. ";
    public final static String REMOVEDFIELD ="\" removed. ";
    public final static String COLON =":";
    public final static String FULLSTOP="\". ";
    public final static char COMMA =',';
    public final static char DOT ='.';
    public final static String CLINICAL ="Clinical Assessment";
    public final static String OMICS = "Omics";
    public final static String IMAGING = "Imaging";
    public final static String PRECLINICAL = "Preclinical";
    public final static String STANDARDIZATION = "Standardization";


    /**************************************************************************************
     * 
     * Change String Messaging
     * 
     *************************************************************************************/
    public final static String RULES_ENGINE_CHANGES = " has a number changes:\n";

    /**
     * This method takes in a severity record and generates a string that represent a human readable messages.
     * 
     * @param sr
     * @return
     */
    public static String generateSeverityRecordString(SeverityRecord sr)
    {

        getMessageBuilder();
        if (sr.getFieldName().contains(BASE_CASE))
        {
            baseCaseStringGeneration(sr);
        }
        else

            if (sr.getFieldName().startsWith(LIST_CASE))
            {
                listCaseStringGeneration(sr);
            }
        String toRtn = messageBuilder.toString();
        messageBuilder.setLength(0);
        return toRtn;
    }

	private static void listCaseStringGeneration(SeverityRecord sr) {

		String fieldName = sr.getFieldName();
		if (!fieldName.contains(RulesEngineConstants.RULES_ENGINE_OPERAND_SEPARATOR)) {
			if (fieldName.contains(TYPE_THRESHOLD)) {
				thresholdFrequencyStringGeneration(sr);
			} else if (fieldName.contains(EXTERNAL_ID)) {
				externalIdStringGeneration(sr);
			} else if (sr.getFieldName().contains(SUB_CASE)) {
				subCaseStringGeneration(sr);
			}

		} else {
			if (fieldName.contains(ADDED)) {
				String fNParse = locateDisplayField(fieldName, ADDED);

				messageBuilder.append(fNParse + "Addition: " + YOUR + " addition of " + removeFieldS(fNParse.trim())
						+ "(s)");
				appendSeverityChange(sr);
			} else if (fieldName.contains(REMOVED)) {
				// Split the string into smaller pieces
				// Find the instance of the removed name and then spilt further
				String fNParse = locateDisplayField(fieldName, REMOVED);

				messageBuilder.append(fNParse + "Removal: " + YOUR + " removal of " + removeFieldS(fNParse.trim())
						+ "(s)");
				appendSeverityChange(sr);

			} else if (fieldName.contains(SEQUENCE)) {

				String fNParse = locateDisplayField(fieldName, SEQUENCE);

				messageBuilder.append(fNParse + "Sequence: " + YOUR + " sequence change of "
						+ removeFieldS(fNParse.trim()) + "(s)");
				appendSeverityChange(sr);

			}
		}
	}

    private static String parseForList(String fieldName)
    {

        int indexOfColon = fieldName.lastIndexOf(':') + 1;
        int indexOfHypen = fieldName.lastIndexOf('-');
        return fieldName.substring(indexOfColon, indexOfHypen);

    }

    public static String locateDisplayField(String displayName, String toLocate)
    {

        String[] displayNameArr = displayName.split("\\|");

        for (String displayPiece : displayNameArr)
        {
            if (displayPiece.contains(toLocate))
            {
                return parseForList(displayPiece);
            }
        }
        return null;
    }

    //
    // fieldLocation.append(" has a item with a " + fieldList.getKey() + " of "
    // + orgValue.toString() + " that contains a ");

    private static void thresholdFrequencyStringGeneration(SeverityRecord sr)
    {

        ThresholdProperty original = (ThresholdProperty) sr.getOriginalValue();
        ThresholdProperty incoming = (ThresholdProperty) sr.getChangedValue();

        int indexOfIdea = sr.getFieldName().indexOf(ID);
        String iDSubString = sr.getFieldName().substring(indexOfIdea + ID.length());
        String idString = iDSubString.substring(0, iDSubString.indexOf('|'));

        if (original.getRepeatableType().compareTo(incoming.getRepeatableType()) == 0)
        { // if repeatable group is the same and
            equalThreshDiffFreqStringGenerator(sr, original, incoming, idString);
        }
        else
        { // We know the the threshold are different
            diffThreshFreqStringGenerator(sr, original, incoming, idString);
        }
        // Cases
        // 1 threshold Changes
        // 2 Frequency changes
        // 3 Both changes

        // Find the name and add to message prompt
    }

    /**
     * @param sr
     * @param original
     * @param incoming
     * @param idString
     */
    private static void diffThreshFreqStringGenerator(SeverityRecord sr, ThresholdProperty original,
            ThresholdProperty incoming, String idString)
    {

        if ((original.getRepeatableType().compareTo(RepeatableType.MORETHAN) == 0)
                || (original.getRepeatableType().compareTo(RepeatableType.LESSTHAN) == 0))
        { // At least //Up To

            if (original.getThreshold() > incoming.getThreshold())
            { // If the frequency decreases
                messageBuilder.append(DEC_FREQ + YOUR_CHANGE_THRESHOLD + idString.trim());
            }
            else
                if (original.getThreshold() < incoming.getThreshold())
                {
                    messageBuilder.append(INC_FREQ + YOUR_CHANGE_THRESHOLD + idString.trim());
                }
                else
                {
                    messageBuilder.append(NC_FREQ + YOUR_CHANGE_THRESHOLD + idString.trim());
                }

        }
        else
            if (original.getRepeatableType().compareTo(RepeatableType.EXACTLY) == 0)
            { // Exactly
                if (incoming.getRepeatableType().compareTo(RepeatableType.MORETHAN) == 0)
                {// At Least
                    if (original.getThreshold() > incoming.getThreshold())
                    { // If the frequency decreases
                        messageBuilder.append(DEC_FREQ + YOUR_CHANGE_THRESHOLD + idString.trim());
                    }
                    else
                        if (original.getThreshold() < incoming.getThreshold())
                        {
                            messageBuilder.append(INC_FREQ + YOUR_CHANGE_THRESHOLD + idString.trim());
                        }
                        else
                        {
                            messageBuilder.append(NC_FREQ + YOUR_CHANGE_THRESHOLD + idString.trim());
                        }
                }
                else
                    if (incoming.getRepeatableType().compareTo(RepeatableType.LESSTHAN) == 0)
                    {
                        if (original.getThreshold() > incoming.getThreshold())
                        { // If the frequency decreases
                            messageBuilder.append(DEC_FREQ + YOUR_CHANGE_THRESHOLD + idString);
                        }
                        else
                            if (original.getThreshold() < incoming.getThreshold())
                            {
                                messageBuilder.append(INC_FREQ + YOUR_CHANGE_THRESHOLD + idString);
                            }
                            else
                            {
                                messageBuilder.append(NC_FREQ + YOUR_CHANGE_THRESHOLD + idString);
                            }
                    }
            }
        if (sr.getSeverityLevel().compareTo(SeverityLevel.NEW) == 0)
        {
            messageBuilder.append(" resulted in a new " + FORM_STRUCTURE + ".");
        }
        else
        {
            messageBuilder.append(" resulted in a " + sr.getSeverityLevel().toString() + " version change to the "
                    + FORM_STRUCTURE + ".");
        }
    }

    /**
     * @param sr
     * @param orginal
     * @param incoming
     * @param idString
     */
    private static void equalThreshDiffFreqStringGenerator(SeverityRecord sr, ThresholdProperty orginal,
            ThresholdProperty incoming, String idString)
    {

        if (sr.getSeverityLevel().compareTo(SeverityLevel.NEW) == 0)        	
        { 
        	
        	// CRIT--2891 if the original threshold is 0(infinite) and we make new one to 1 or more we make in decreasing 
        	if(orginal.getThreshold()==0 && orginal.getThreshold() < incoming.getThreshold()){
        		  messageBuilder.append(DEC_FREQ + YOUR_CHANGE + "to the frequency of Data Element group " + idString
                          + "  " + CREATION_OF_NEW + FORM_STRUCTURE + ".");
        	}
        	// CRIT--2891 if the incoming threshold is 0(infinite) and old one was some no 1 or more then we make in increasing
        	else if(orginal.getThreshold() > incoming.getThreshold() && incoming.getThreshold()==0 ){
        		 messageBuilder.append(INC_FREQ + YOUR_CHANGE + "to the frequency of Data Element group " + idString
                         + " " + CREATION_OF_NEW + FORM_STRUCTURE + ".");
        	}
        	// If the threshold is the same but the change is new
        	else if (orginal.getThreshold() > incoming.getThreshold())
            { // If the frequency decreases
                messageBuilder.append(DEC_FREQ + YOUR_CHANGE + "to the frequency of Data Element group " + idString
                        + "  " + CREATION_OF_NEW + FORM_STRUCTURE + ".");
            }
            else
            {
                messageBuilder.append(INC_FREQ + YOUR_CHANGE + "to the frequency of Data Element group " + idString
                        + " " + CREATION_OF_NEW + FORM_STRUCTURE + ".");
            }
        }
        else
        {
            if (orginal.getThreshold() > incoming.getThreshold())
            { // If the frequency decreases
                messageBuilder.append(DEC_FREQ + YOUR_CHANGE + "to the frequency of Data Element group " + idString
                        + " resulted in a " + sr.getSeverityLevel().toString() + " version change to the "
                        + FORM_STRUCTURE + ".");
            }
            else
            {
                messageBuilder.append(INC_FREQ + YOUR_CHANGE + "to the frequency of Data Element group " + idString
                        + " resulted in a " + sr.getSeverityLevel().toString() + " version change to the "
                        + FORM_STRUCTURE + ".");
            }
        }
    }

    private static void externalIdStringGeneration(SeverityRecord sr)
    {

        // List:External Id | List Item:ExternalId External ID: caDSR | Subfield:External Id Value
        String fieldName = sr.getFieldName();
        int subCase = fieldName.indexOf(SUB_CASE);
        String subField = fieldName.substring(subCase + SUB_CASE.length());
        messageBuilder.append(subField + ": ");
        int externalId = fieldName.indexOf(EXTERNAL_ID);
        int externalLen = externalId + EXTERNAL_ID.length();
        int secondPipe = fieldName.indexOf('|', externalLen);
        String externalIdType = fieldName.substring(externalLen, secondPipe);
        messageBuilder.append(YOUR_CHANGE + "to " + externalIdType.trim() + " resulted in a "
                + sr.getSeverityLevel().name() + " version change");
        appendSeverityReccomendation(sr);
    }

    private static void permissibleValueDescriptionStringGeneration(SeverityRecord sr)
    {

        int indexOfIdea = sr.getFieldName().indexOf(ID);
        String iDSubString = sr.getFieldName().substring(indexOfIdea + ID.length());
        String idString = iDSubString.substring(0, iDSubString.indexOf('|'));

        messageBuilder.append(YOUR_CHANGE + "to a Permisible value \"" + idString.trim() + "\"" + CREATION_OF_NEW
                + DATA_ELEMENT + ".");
    }

    
	private static void permissibleValueOutputCodeStringGeneration(SeverityRecord sr) {

		int indexOfIdea = sr.getFieldName().indexOf(ID);
		String iDSubString = sr.getFieldName().substring(indexOfIdea + ID.length());
		String idString = iDSubString.substring(0, iDSubString.indexOf('|'));

		messageBuilder.append(YOUR_CHANGE + "to the Output Code of Permisible Value \"" + idString.trim()
				+ "\" resulted in a " + sr.getSeverityLevel().name() + " version change");
		appendSeverityReccomendation(sr);
	}

	
    private static void subCaseStringGeneration(SeverityRecord sr)
    {

        String fieldName = sr.getFieldName();
        int subCase = fieldName.indexOf(SUB_CASE);
        String subField = fieldName.substring(subCase + SUB_CASE.length());
        messageBuilder.append(subField + ": ");
        buildBaseMessage(sr);
    }

    /**
     * Handles Base Case message generation. Checks the severity level and Data Dictionary Object to construct the
     * message
     * 
     * @param sr
     */
    private static void baseCaseStringGeneration(SeverityRecord sr)
    {

        String fieldName = sr.getFieldName().substring(BASE_CASE.length()); // This is to remove the Base: prefix
        messageBuilder.append(fieldName + ": ");
        buildBaseMessage(sr);
    }

    /**
     * @param sr
     */
    private static void buildBaseMessage(SeverityRecord sr)
    {

        SeverityLevel severityLevel = sr.getSeverityLevel();
        if (severityLevel.compareTo(SeverityLevel.NEW) == 0)
        {
            messageBuilder.append(YOUR_CHANGE + CREATION_OF_NEW);
            if (sr.getDataDictionaryObject().equals(DATA_ELEMENT))
            {
                messageBuilder.append(DATA_ELEMENT);

            }
            else
            {
                messageBuilder.append(FORM_STRUCTURE);
            }
        }
        else
        {
            messageBuilder.append(YOUR_CHANGE_RESULTED + severityLevel.getSeverityLevel() + " version change");
            appendSeverityReccomendation(sr);
        }
    }

    private static void appendSeverityChange(SeverityRecord sr)
    {

        if (sr.getSeverityLevel().compareTo(SeverityLevel.NEW) == 0)
        {
            messageBuilder.append(CREATION_OF_NEW);
            if (sr.getDataDictionaryObject().equals(DATA_ELEMENT))
            {
                messageBuilder.append(DATA_ELEMENT);

            }
            else
            {
                messageBuilder.append(FORM_STRUCTURE);
            }
        }
        else
        {
            messageBuilder.append(" resulted in a " + sr.getSeverityLevel().toString().toLowerCase()
                    + " version change");
            appendSeverityReccomendation(sr);

        }
    }

    /**
     * @param sr
     */
    private static void appendSeverityReccomendation(SeverityRecord sr)
    {

        if (sr.getDataDictionaryObject().equals(DATA_ELEMENT))
        {

            messageBuilder.append(TO_DATA_ELEMENT);
        }
        else
        {
            messageBuilder.append(TO_FORM_STRUCTURE);
        }
    }

    /**
     * @return the messageBuilder
     */
    public static StringBuilder getMessageBuilder()
    {

        if (messageBuilder == null)
        {
            messageBuilder = new StringBuilder();
        }
        return messageBuilder;
    }

    /**
     * @param messageBuilder
     *            the messageBuilder to set
     */
    public void setMessageBuilder(StringBuilder messageBuilder)
    {

        this.messageBuilder = messageBuilder;
    }

    public static String removeFieldS(String fieldName)
    {

        int lastFieldInt = fieldName.length() - 1;
        char lastFieldChar = fieldName.charAt(lastFieldInt);
        if (lastFieldChar == 's')
        {
            return fieldName.substring(0, lastFieldInt);
        }
        return fieldName;
    }
    
    public static String parseFieldName(String fieldName)
    {

        int indexOfColon = fieldName.indexOf(':')+1;
        int indexOfHypen = fieldName.indexOf('-');
        return fieldName.substring(indexOfColon, indexOfHypen);

    }
    
    public static String parseMapId(String fieldName)
    {
    	
        int indexOfHypen = fieldName.lastIndexOf('|');
    	fieldName = fieldName.substring(0,indexOfHypen);
    	
    	int indexOfColon = fieldName.lastIndexOf(':')+1;	
        return fieldName.substring(indexOfColon);
    }
    
    public static String parseListFieldName(String fieldName)
    {
    	  int indexOfHypen = fieldName.lastIndexOf('|');
      	  fieldName = fieldName.substring(0,indexOfHypen);
      	
      	  int indexOfColon = fieldName.lastIndexOf(':')+1;
      	  return fieldName.substring(indexOfColon);
    }

}
