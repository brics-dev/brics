
package gov.nih.tbi.dictionary.model;

import java.util.Date;

public class DateFacet extends BaseDictionaryFacet
{

    private FacetType type;
    private Date date;

    public DateFacet(FacetType type, Date date)
    {

        super();
        this.type = type;
        this.date = date;
    }

    public Date getDate()
    {

        return date;
    }

    public void setDate(Date date)
    {

        this.date = date;
    }

    public FacetType getType()
    {

        return type;
    }

    public void setType(FacetType type)
    {

        this.type = type;
    }

    @Override
    public int hashCode()
    {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {

        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        DateFacet other = (DateFacet) obj;
        if (date == null)
        {
            if (other.date != null)
                return false;
        }
        else if (!date.equals(other.date))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString()
    {

        return "DateFacetValue [type=" + type + ", date=" + date + "]";
    }

}
