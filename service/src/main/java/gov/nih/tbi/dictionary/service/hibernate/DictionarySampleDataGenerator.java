
package gov.nih.tbi.dictionary.service.hibernate;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.commons.model.RepeatableType;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.dictionary.dao.FormStructureDao;
import gov.nih.tbi.dictionary.model.DictionaryData;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.repository.ws.RestRepositoryProvider;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This class can be used to generate sample data for any data structure in the system
 * 
 * @author Andrew Johnson
 */
@Service
public class DictionarySampleDataGenerator
{

    /**************************************************************************************************/

    private static final String AVAILABLE_CHARACTERS = "abcdefghijklmnopqrstuvwxyz             ";
    private static final String X_STRING = "x";
    private static final String BIOSAMPLE_PREFIX = "BSID";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(ModelConstants.UNIVERSAL_DATE_FORMAT);
    private static Random randomCounter = new Random();

    /**************************************************************************************************/

    /*    @Autowired
        DataStructureDao dataStructureDao;*/

    @Autowired
    FormStructureDao formStructureDao;

    @Autowired
    ModulesConstants modulesConstants;

    /**************************************************************************************************/

    String invalidSampleGuid;

    /**************************************************************************************************/

    /**************************************************************************************************/

    /**
     * The initial entry point for generating data for the entire data structure
     * 
     * @param account
     * 
     * @param tempStructure
     *            (AbstractDataStructure) - data Structure to generate data for
     * 
     * @return container for generated data
     */
    public DictionaryData generateDataStructureData(RestRepositoryProvider restRepositoryProvider,
            FormStructure tempStructure, Long diseaseId)
    {

        // Prep-work

        // Going to use the Repository provider to get the list -> RepositoryWebservice -> RepositoryManager ->
        // InvSbjDao

        // ZG542YHV
        // ABC12XYZ

        String guidInvPrefix = modulesConstants.getGuidInvPrefix(diseaseId);
        invalidSampleGuid = guidInvPrefix + "ABC12XYZ";
        /* try
         {
             sampleSubjectsList = restRepositoryProvider.getInvalidSubjects();
         }
         catch (UnsupportedEncodingException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }*/

        FormStructure dataStructure = null;

        DictionaryData data = new DictionaryData();

        if (tempStructure instanceof FormStructure)
        {
            dataStructure = formStructureDao.get(tempStructure.getShortName(), tempStructure.getVersion());
        }
        else
        {
            dataStructure = (FormStructure) tempStructure;
        }

        RepeatableGroup main = null;
        data.addRow(); // Add initial line

        for (RepeatableGroup rg : dataStructure.getRepeatableGroups())
        {

            int threshold = rg.getThreshold();

            // In order to enforce a limit on the number of rows generated for a RG with a threshold of 0
            // assign threshold integer a random whole number 1-3 and continue as normal
            if (threshold == 0)
            {
                int minNumber = 1;
                int maxNumber = 4;
                // This will generate a random number 1-3
                threshold = minNumber + (int) (Math.random() * ((maxNumber - minNumber) + 1));
            }

            if (rg.getName().equalsIgnoreCase(ModelConstants.MAIN_STRING))
            {
                main = rg;
            }

            // Increase the size of the array until it can hold the max of any repeatable group
            if (threshold > data.getRowCount())
            {
                int test = threshold - (data.getRowCount() - 1);

                for (int i = 0; i < test; i++)
                {
                    data.addRow();
                }
            }
        }

        // add record column as the first column
        data.addColumn(ModelConstants.RECORD_STRING);
        // All rows of data start with an 'x' identifier, fill in the rest of the rows with an empty spot
        data.set(0, ModelConstants.RECORD_STRING, X_STRING);

        // generate data for Main repeatable group first
        generateRepeatableGroupData(data, main);

        // generate data for each other group afterwards
        for (RepeatableGroup rg : dataStructure.getRepeatableGroups())
        {
            if (!rg.getName().equalsIgnoreCase(ModelConstants.MAIN_STRING))
            {
                generateRepeatableGroupData(data, rg);
            }
        }

        return data;
    }

    /**
     * Generates data for a repeatable group, including multi-row data for repeating groups
     * 
     * @param data
     *            (DictionaryData) - container for holding data
     * @param rg
     *            (RepeatableGroup) - object that contains the validation rules for the repeatable group
     */
    private void generateRepeatableGroupData(DictionaryData data, RepeatableGroup rg)
    {

        int maxRows = data.getRowCount();
        int threshold = rg.getThreshold();

        if (threshold == 0)
        {
            // random number assigned in the generateDataStructureData method
            // only used if the threshold is 0
            threshold = 1 + (int) (Math.random() * ((data.getRowCount() - 1) + 1));
        }

        if (RepeatableType.LESSTHAN.equals(rg.getType()))
        {
            // random ( 0 to (thresh - 1) ) + 1
            maxRows = generateNumber(1, threshold);
        }
        else
            if (RepeatableType.MORETHAN.equals(rg.getType()))
            {
                // random ( 0 to (max - thresh) ) + thresh
                maxRows = generateNumber(threshold, maxRows);
            }
            else
            {
                // Otherwise use exactly the threshold
                maxRows = threshold;
            }

        // for each element in this group
        for (MapElement me : rg.getMapElements())
        {
            String columnName = rg.getName() + ServiceConstants.PERIOD + me.getStructuralDataElement().getName();
            data.addColumn(columnName);

            // for each of our current rows add data for this element
            for (int currentRow = 0; currentRow < maxRows; currentRow++)
            {
                String val = generateMapElementData(me);
                data.set(currentRow, columnName, val);
            }

            // NOTE: this shouldn't be necessary
            // For each other row add an extra white space for this element
            for (int currentRow = maxRows; currentRow < data.getRowCount(); currentRow++)
            {
                data.set(currentRow, columnName, null);
            }
        }
    }

    /**
     * Generate Data for a Data Element
     * 
     * @param me
     *            (MapElement) - used to know what rules to generate for
     * @return a string that matches the validation rules
     */
    private String generateMapElementData(MapElement me)
    {

        StructuralDataElement de = me.getStructuralDataElement();
        if (InputRestrictions.FREE_FORM.equals(de.getRestrictions()))
        {
            if (DataType.ALPHANUMERIC.equals(de.getType()))
            {
                String val = ModelConstants.EMPTY_STRING;

                int beginWith = 3;
                if (de.getSize() < 3) // If the map element data size is less than 3, we need to use that instead
                {
                    beginWith = de.getSize();
                }

                // generate at least n characters before a number
                for (int i = 0; i < beginWith; i++)
                {
                    // generate a random index into the available characters
                    int charNum = generateNumber(0, 25); // But don't include the whitespace character in the first few

                    if (i == 0) // Capitalize the first letter
                        val += Character.toUpperCase(AVAILABLE_CHARACTERS.charAt(charNum));
                    else
                        val += AVAILABLE_CHARACTERS.charAt(charNum);
                }

                if (beginWith < de.getSize())
                {
                    // generate a random number of characters
                    for (int i = beginWith; i < generateNumber(beginWith, de.getSize()); i++)
                    {
                        // generate a random index into the available characters
                        int charNum = generateNumber(0, AVAILABLE_CHARACTERS.length());

                        // append the selected character
                        val += AVAILABLE_CHARACTERS.charAt(charNum);
                    }
                }

                return val;
            }
            else
                if (DataType.NUMERIC.equals(de.getType()))
                {
                    double min = -100;
                    double max = 99999;

                    if (de.getMinimumValue() != null)
                    {
                        min = de.getMinimumValue().doubleValue();
                    }

                    if (de.getMaximumValue() != null)
                    {
                        max = de.getMaximumValue().doubleValue();
                    }

                    // generate the free form number
                    double val = generateNumber(min, max);
                    DecimalFormat twoPlaces = new DecimalFormat("#####.##");

                    return twoPlaces.format(val);
                }
                else
                    if (DataType.GUID.equals(de.getType()))
                    {
                        String guid = null;

                        // only do this if we have possible values
                        if (invalidSampleGuid != null && !invalidSampleGuid.isEmpty())
                        {
                            /* // generate an index into the possible list of subjects
                             int index = generateNumber(0, sampleSubjectsList.size() - 1);

                             InvalidSubject sample = sampleSubjectsList.get(index);*/

                            guid = invalidSampleGuid;
                        }

                        return guid;
                    }
                    else
                        if (DataType.DATE.equals(de.getType()))
                        {
                            String date = null;

                            Calendar cal = Calendar.getInstance();
                            cal.setLenient(false);

                            // keep generating dates until one actually works.
                            while (date == null)
                            {
                                try
                                {
                                    // generate a date
                                    cal.set(generateNumber(1920, 2012), generateNumber(1, 12), generateNumber(1, 31));

                                    // format it for use
                                    date = dateFormat.format(cal.getTime());
                                }
                                catch (Exception e)
                                {
                                    // ignore this exception because we will just attempt to generate a different date
                                    // e.printStackTrace();
                                }
                            }

                            return date;
                        }
                        else
                            if (DataType.BIOSAMPLE.equals(de.getType()))
                            {
                                /*
                                 * Biosample ids are created by appending the Biosample Id Prefix (BSID) with a randomly gernerated number.
                                 * ex: BSID345
                                 * 
                                 */
                                String val = ModelConstants.EMPTY_STRING;
                                int id = generateNumber(0, 2999);
                                val = BIOSAMPLE_PREFIX + id;
                                return val;
                            }

                            else
                            {

                                // anything else
                                return ModelConstants.EMPTY_STRING;
                            }

            // FILE
            // THUMBNAIL
        }
        else
        {
            int valCount = 1;
            int maxPossibilities = de.getValueRangeList().size();

            // If its multiple then it can be between 1 and max number of possibilities
            if (InputRestrictions.MULTIPLE.equals(de.getRestrictions()))
            {
                valCount = generateNumber(1, maxPossibilities - 1);
            }
            // XXX: if it is single entry, it could possibly be no input, but we assume we always want at least one

            // create a list of possible values to be used
            List<String> possibleIndex = new ArrayList<String>();
            for (ValueRange vr : de.getValueRangeList())
            {
                possibleIndex.add(vr.getValueRange());
            }

            String output = ModelConstants.EMPTY_STRING;

            // If there are no permissible values, just leave the field blank.
            if (!possibleIndex.isEmpty())
            {
                for (int i = 0; i < valCount; i++)
                {
                    // if this is not the first one add a comma
                    if (i > 0)
                    {
                        output += ServiceConstants.CSV_LIST_SEPARATER;
                    }

                    // generate a random index into possibleIndexes
                    int nextVal = generateNumber(0, possibleIndex.size() - 1);

                    // get the value from possibleIndex
                    String val = possibleIndex.get(nextVal);
                    // remove the value from the list of possibles
                    possibleIndex.remove(possibleIndex.get(nextVal));

                    // add the value to the string
                    output += val;
                }
            }

            return output;
        }

    }

    /**************************************************************************************************/

    /**
     * Helper method to generate a number between min and max. If min and max are the same, then return the exact value.
     * 
     * @param min
     *            (int) - min value
     * @param max
     *            (int) - max value
     * @return a number between min and max
     */
    private int generateNumber(int min, int max)
    {

        if (min >= max)
        {
            return min;
        }

        return randomCounter.nextInt(max - min) + min;
    }

    /**
     * Helper method to generate a number between min and max (double). If min and max are the same, then return the
     * exact value.
     * 
     * @param min
     *            (double) - min value
     * @param max
     *            (double) - max value
     * @return a number between min and max
     */
    private double generateNumber(double min, double max)
    {

        return min + ((randomCounter.nextDouble() * (max - min)));
    }

}
