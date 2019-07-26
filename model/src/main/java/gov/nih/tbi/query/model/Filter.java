
package gov.nih.tbi.query.model;

import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Model that represents a single filter. Contains a element and repeatable group to filter by.
 * 
 * @author Francis Chen
 * 
 */
public class Filter implements Serializable
{

    private static final long serialVersionUID = 8316978995099712048L;

    private DataElement element;
    private RepeatableGroup group;
    private ArrayList<String> permissibleValues;
    private Double maximum;
    private Double minimum;
    private String freeFormValue;
    private Date dateMin;
    private Date dateMax;

    public Filter(DataElement element)
    {

        this.element = element;
    }

    public Filter(RepeatableGroup group, DataElement element)
    {

        this.group = group;
        this.element = element;
    }

    public DataElement getElement()
    {

        return element;
    }

    public void setElement(DataElement element)
    {

        this.element = element;
    }

    public Double getMaximum()
    {

        return maximum;
    }

    public void setMaximum(Double maximum)
    {

        this.maximum = maximum;
    }

    public Double getMinimum()
    {

        return minimum;
    }

    public void setMinimum(Double minimum)
    {

        this.minimum = minimum;
    }

    public boolean isNumeric()
    {

        if (minimum != null && maximum != null)
        {
            return true;
        }

        return false;
    }

    public boolean isDate()
    {

        if (dateMin != null && dateMax != null)
        {
            return true;
        }

        return false;
    }

    public boolean isEmpty()
    {

        if (minimum == null && maximum == null && permissibleValues == null && freeFormValue == null && dateMin == null
                && dateMax == null)
        {
            return true;
        }

        return false;
    }

    public ArrayList<String> getPermissibleValues()
    {

        return permissibleValues;
    }

    public void setPermissibleValues(ArrayList<String> permissibleValues)
    {

        if (this.permissibleValues == null)
        {
            this.permissibleValues = new ArrayList<String>();
        }

        this.permissibleValues.clear();

        if (permissibleValues != null)
        {
            this.permissibleValues.addAll(permissibleValues);
        }
    }

    public String getFreeFormValue()
    {

        return freeFormValue;
    }

    public void setFreeFormValue(String freeFormValue)
    {

        this.freeFormValue = freeFormValue;
    }

    public Date getDateMin()
    {

        return dateMin;
    }

    public void setDateMin(Date dateMin)
    {

        this.dateMin = dateMin;
    }

    public Date getDateMax()
    {

        return dateMax;
    }

    public void setDateMax(Date dateMax)
    {

        this.dateMax = dateMax;
    }

    public void clear()
    {

        this.dateMax = null;
        this.dateMin = null;
        this.element = null;
        this.maximum = null;
        this.minimum = null;
        this.permissibleValues = null;
        this.freeFormValue = null;
    }

    public RepeatableGroup getGroup()
    {

        return group;
    }

    public void setGroup(RepeatableGroup group)
    {

        this.group = group;
    }

    /**
     * Returns true if this filter has a value to filter on, false otherwise.
     * @return
     */
    public boolean hasValue()
    {

        if (this.dateMin == null && this.dateMax == null
                && (this.permissibleValues == null || this.permissibleValues.isEmpty()) && this.freeFormValue == null
                && this.minimum == null && this.maximum == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}
