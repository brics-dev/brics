
package gov.nih.tbi.dictionary.model;

import java.util.List;

public class StringFacet extends BaseDictionaryFacet
{

    private FacetType type;
    private List<String> values;

    public StringFacet(FacetType type, List<String> values)
    {

        this.type = type;
        this.values = values;
    }

    public List<String> getValues()
    {

        return values;
    }

    public void setValues(List<String> values)
    {

        this.values = values;
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
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StringFacet other = (StringFacet) obj;
        if (type != other.type)
            return false;
        if (values == null)
        {
            if (other.values != null)
                return false;
        }
        else if (!values.equals(other.values))
            return false;
        return true;
    }

    @Override
    public String toString()
    {

        return "StringFacetValues [type=" + type + ", values=" + values + "]";
    }

}
