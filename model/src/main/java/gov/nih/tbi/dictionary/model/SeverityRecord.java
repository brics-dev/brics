
package gov.nih.tbi.dictionary.model;

import gov.nih.tbi.commons.model.SeverityLevel;

public class SeverityRecord
{

    private String fieldName; // Name of field being compared.
    private SeverityLevel severityLevel; // determined severity level
    private Object originalValue; // original value of field
    private Object changedValue; // altered value of field
    private String dataDictionaryObject;

    public String getFieldName()
    {

        return fieldName;
    }

    public void setFieldName(String fieldName)
    {

        this.fieldName = fieldName;
    }

    public SeverityLevel getSeverityLevel()
    {

        return severityLevel;
    }

    public void setSeverityLevel(SeverityLevel severityLevel)
    {

        this.severityLevel = severityLevel;
    }

    public Object getOriginalValue()
    {

        return originalValue;
    }

    public void setOriginalValue(Object originalValue)
    {

        this.originalValue = originalValue;
    }

    public Object getChangedValue()
    {

        return changedValue;
    }

    public void setChangedValue(Object changedValue)
    {

        this.changedValue = changedValue;
    }

    /**
     * @return the dataDictionaryObject
     */
    public String getDataDictionaryObject()
    {

        return dataDictionaryObject;
    }

    /**
     * @param dataDictionaryObject
     *            the dataDictionaryObject to set
     */
    public void setDataDictionaryObject(String dataDictionaryObject)
    {

        this.dataDictionaryObject = dataDictionaryObject;
    }
}