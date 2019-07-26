
package gov.nih.tbi.dictionary.model;

import java.util.List;

public class ClassificationFacet extends BaseDictionaryFacet
{

    private List<ClassificationFacetValue> values;

    public ClassificationFacet(List<ClassificationFacetValue> values)
    {

        super();
        this.values = values;
    }

    public List<ClassificationFacetValue> getValues()
    {

        return values;
    }

    public void setValues(List<ClassificationFacetValue> values)
    {

        this.values = values;
    }

    @Override
    public FacetType getType()
    {

        return FacetType.CLASSIFICATION;
    }

    @Override
    public int hashCode()
    {

        final int prime = 31;
        int result = 1;
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
        ClassificationFacet other = (ClassificationFacet) obj;
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

        return "ClassificationFacetValues [values=" + values + "]";
    }

}
