
package gov.nih.tbi.dictionary.model;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.commons.service.hibernate.StaticReferenceManagerImpl;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import au.com.bytecode.opencsv.bean.HeaderColumnNameMappingStrategy;

public class TBIMappingStrategy extends HeaderColumnNameMappingStrategy<DataElement>
{

    @Autowired
    StaticReferenceManager staticManager;

    private static Map<String, String> csvColumnMapping;

    /**
     * returns a mapping of readable column headers to bean fields.
     * 
     * @return
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     */
    public static Map<String, String> getCsvColumnMapping() throws MalformedURLException, UnsupportedEncodingException
    {

        StaticReferenceManager staticManager = new StaticReferenceManagerImpl();

        if (csvColumnMapping == null)
        {
            csvColumnMapping = new HashMap<String, String>();

            csvColumnMapping.put(ServiceConstants.NAME_READABLE.toLowerCase(), CoreConstants.NAME);
            csvColumnMapping.put(ServiceConstants.TITLE_READABLE.toLowerCase(), ServiceConstants.TITLE);
            csvColumnMapping.put(ServiceConstants.SHORT_DESCRIPTION_READABLE.toLowerCase(),
                    ServiceConstants.SHORT_DESCRIPTION);
            csvColumnMapping.put(ServiceConstants.DEFINITION_READABLE.toLowerCase(), CoreConstants.DESCRIPTION);
            csvColumnMapping.put(ServiceConstants.FORMAT_READABLE.toLowerCase(), ServiceConstants.FORMAT);
            csvColumnMapping.put(ServiceConstants.SIZE_READABLE.toLowerCase(), ServiceConstants.SIZE);
            csvColumnMapping.put(ServiceConstants.PERMISSIBLE_VALUES_READABLE.toLowerCase(),
                    ServiceConstants.VALUE_RANGES);
            csvColumnMapping.put(ServiceConstants.PERMISSIBLE_VALUES_DESCRIPTION_READABLE.toLowerCase(),
                    ServiceConstants.VALUE_RANGE_DESCRIPTIONS);
           csvColumnMapping.put(ServiceConstants.PERMISSIBLE_VALUES_OUTPUT_CODES_READABLE.toLowerCase(),
                    ServiceConstants.VALUE_RANGE_OUTPUT_CODES);
            csvColumnMapping.put(ServiceConstants.GUIDELINES_READABLE.toLowerCase(), ServiceConstants.GUIDELINES);
            csvColumnMapping.put(ServiceConstants.HISTORICAL_NOTES_READABLE.toLowerCase(),
                    ServiceConstants.HISTORICAL_NOTES);
            csvColumnMapping.put(ServiceConstants.TYPE_READABLE.toLowerCase(), ServiceConstants.TYPE);
            csvColumnMapping.put(ServiceConstants.REFERENCES_READABLE.toLowerCase(), ServiceConstants.REFERENCES);
            csvColumnMapping.put(ServiceConstants.CLASSIFICATION_READABLE.toLowerCase(),
                    ServiceConstants.CLASSIFICATION);
            csvColumnMapping.put(ServiceConstants.POPULATION_READABLE.toLowerCase(), ServiceConstants.POPULATION);
            csvColumnMapping.put(ServiceConstants.KEYWORD_LIST_READABLE.toLowerCase(), ServiceConstants.KEYWORD_LIST);
            csvColumnMapping.put(ServiceConstants.REQUIRED_TYPE_READABLE.toLowerCase(), ServiceConstants.REQUIRED_TYPE);
            csvColumnMapping.put(ServiceConstants.SECTION_READABLE.toLowerCase(), ServiceConstants.SECTION);
            csvColumnMapping.put(ServiceConstants.ALIASES_READABLE.toLowerCase(), ServiceConstants.ALIASES);
            csvColumnMapping.put(ServiceConstants.RESTRICTIONS_READABLE.toLowerCase(), ServiceConstants.RESTRICTIONS);
            csvColumnMapping.put(ServiceConstants.CATEGORY_READABLE.toLowerCase(), ServiceConstants.CATEGORY);
            /* these fields have been removed and separated out into a different CSV process. commenting out just in case we add it back
             * csvColumnMapping.put(ServiceConstants.LOINC_READABLE.toLowerCase(), ServiceConstants.LOINC);
            csvColumnMapping.put(ServiceConstants.SNOMED_READABLE.toLowerCase(), ServiceConstants.SNOMED);
            csvColumnMapping.put(ServiceConstants.CADSR_READABLE.toLowerCase(), ServiceConstants.CADSR);
            csvColumnMapping.put(ServiceConstants.CDISC_READABLE.toLowerCase(), ServiceConstants.CDISC);
            csvColumnMapping.put(ServiceConstants.NINDS_READABLE.toLowerCase(), ServiceConstants.NINDS);*/
            csvColumnMapping.put(ServiceConstants.MEASUREMENT_TYPE_READABLE.toLowerCase(),
                    ServiceConstants.MEASUREMENT_TYPE);
            csvColumnMapping.put(ServiceConstants.UNIT_OF_MEASURE_READABLE.toLowerCase(),
                    ServiceConstants.MEASUREMENT_UNIT);
            csvColumnMapping.put(ServiceConstants.MINIMUM_VALUE_READABLE.toLowerCase(), ServiceConstants.MINIMUM_VALUE);
            csvColumnMapping.put(ServiceConstants.MAXIMUM_VALUE_READABLE.toLowerCase(), ServiceConstants.MAXIMUM_VALUE);
            csvColumnMapping.put(ServiceConstants.NOTES_READABLE.toLowerCase(), ServiceConstants.NOTES);
            csvColumnMapping.put(ServiceConstants.PREFERRED_QUESTION_TEXT_READABLE.toLowerCase(),
                    ServiceConstants.QUESTION_TEXT);
            csvColumnMapping.put(ServiceConstants.VERSION_READABLE.toLowerCase(), ServiceConstants.VERSION_READABLE);
            csvColumnMapping.put(ServiceConstants.LABEL_READABLE.toLowerCase(), ServiceConstants.LABEL_LIST);
            csvColumnMapping.put(ServiceConstants.SEE_ALSO_READABLE, ServiceConstants.SEE_ALSO);
            csvColumnMapping.put(ServiceConstants.SUBMITTING_ORGANIZATION_NAME_READABLE,
                    ServiceConstants.SUBMITTING_ORGANIZATION_NAME);
            csvColumnMapping.put(ServiceConstants.SUBMITTING_CONTACT_NAME_READABLE,
                    ServiceConstants.SUBMITTING_CONTACT_NAME);
            csvColumnMapping.put(ServiceConstants.SUBMITTING_CONTACT_INFORMATION_READABLE,
                    ServiceConstants.SUBMITTING_CONTACT_INFORMATION);
            csvColumnMapping.put(ServiceConstants.EFFECTIVE_DATE_READABLE.toLowerCase(),
                    ServiceConstants.EFFECTIVE_DATE);
            csvColumnMapping.put(ServiceConstants.UNTIL_DATE_READABLE, ServiceConstants.UNTIL_DATE);
            csvColumnMapping.put(ServiceConstants.STEWARD_ORGANIZATION_NAME_READABLE,
                    ServiceConstants.STEWARD_ORGANIZATION_NAME);
            csvColumnMapping.put(ServiceConstants.STEWARD_CONTACT_NAME_READABLE, ServiceConstants.STEWARD_CONTACT_NAME);
            csvColumnMapping.put(ServiceConstants.STEWARD_CONTACT_INFORMATION_READABLE,
                    ServiceConstants.STEWARD_CONTACT_INFORMATION);
            // added by Ching-Heng
            csvColumnMapping.put(ServiceConstants.CAT_OID_READABLE.toLowerCase(), ServiceConstants.CAT_OID);
            csvColumnMapping.put(ServiceConstants.FORM_ITEM_OID_READABLE.toLowerCase(), ServiceConstants.FORM_ITEM_OID);
            csvColumnMapping.put(ServiceConstants.ITEM_RESPONSE_OID_READABLE.toLowerCase(), ServiceConstants.ITEM_RESPONSE_OID);
            csvColumnMapping.put(ServiceConstants.ELEMENT_OID_READABLE.toLowerCase(), ServiceConstants.ELEMENT_OID);
            
            
            List<Subgroup> subgroups = staticManager.getSubgroupList();

            for (Subgroup subgroup : subgroups)
            {
                csvColumnMapping.put("classification." + subgroup.getSubgroupName().toLowerCase(), "Classification."
                        + subgroup.getSubgroupName());
            }

            // This will allow domain.<disease> headers to be recongnized as valid columns
            List<Disease> diseases = staticManager.getDiseaseList();
            for (Disease d : diseases)
            {
                csvColumnMapping.put("domain." + d.getName().toLowerCase(), "Domain." + d.getName());
            }
        }

        return csvColumnMapping;
    }

    public int getColumnCount()
    {

        return header.length;
    }

    public String getColumnName(String col) throws MalformedURLException, UnsupportedEncodingException
    {

        return TBIMappingStrategy.getCsvColumnMapping().get(col.toLowerCase().trim());
    }

    public String getColumnName(int col)
    {

        if (null != header && col < header.length)
        {
            try
            {
                String column = header[col].trim().toLowerCase();
                String toReturn = TBIMappingStrategy.getCsvColumnMapping().get(column);
                return toReturn;
            }
            catch (MalformedURLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (UnsupportedEncodingException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else
        {
            return null;
        }
        return null;
    }

    /**
     * Validates that all the required fields are in the header.
     * 
     * @return
     */
    public String validateColumns(List<Subgroup> subgroups)
    {

        List<String> headerList = new ArrayList<String>();

        // trim and change cases to match our constants
        for (String column : header)
        {
            column = column.trim();
            column = column.toLowerCase();
            headerList.add(column);
        }

        // Add any new required fields here in the same fashion
        if (!headerList.contains(ServiceConstants.CATEGORY_READABLE.toLowerCase()))
        {
            return ServiceConstants.CATEGORY_READABLE;
        }
        else
            if (!headerList.contains(ServiceConstants.NAME_READABLE.toLowerCase()))
            {
                return ServiceConstants.NAME_READABLE;
            }
            else
                if (!headerList.contains(ServiceConstants.TITLE_READABLE.toLowerCase()))
                {
                    return ServiceConstants.TITLE_READABLE;
                }
                else
                    if (!headerList.contains(ServiceConstants.SHORT_DESCRIPTION_READABLE.toLowerCase()))
                    {
                        return ServiceConstants.SHORT_DESCRIPTION_READABLE;
                    }
                    else
                        if (!headerList.contains(ServiceConstants.SIZE_READABLE.toLowerCase()))
                        {
                            return ServiceConstants.SIZE_READABLE;
                        }
                        else
                            if (!headerList.contains(ServiceConstants.PERMISSIBLE_VALUES_READABLE.toLowerCase()))
                            {
                                return ServiceConstants.PERMISSIBLE_VALUES_READABLE;
                            }
                            else
                                if (!headerList.contains(ServiceConstants.PERMISSIBLE_VALUES_READABLE.toLowerCase()))
                                {
                                    return ServiceConstants.PERMISSIBLE_VALUES_DESCRIPTION_READABLE;
                                }
                                else
                                    if (!headerList.contains(ServiceConstants.TYPE_READABLE.toLowerCase()))
                                    {
                                        return ServiceConstants.TYPE_READABLE;
                                    }
                                    else
                                        if (!headerList.contains(ServiceConstants.POPULATION_READABLE.toLowerCase()))
                                        {
                                            return ServiceConstants.POPULATION_READABLE;
                                        }
                                        else
                                            if (!headerList.contains(ServiceConstants.RESTRICTIONS_READABLE
                                                    .toLowerCase()))
                                            {
                                                return ServiceConstants.RESTRICTIONS_READABLE;
                                            }

        return null;
    }

}
