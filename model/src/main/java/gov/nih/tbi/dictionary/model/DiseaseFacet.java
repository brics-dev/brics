
package gov.nih.tbi.dictionary.model;

import java.util.List;

public class DiseaseFacet extends BaseDictionaryFacet
{

    private List<DiseaseFacetValue> values;

    public DiseaseFacet(List<DiseaseFacetValue> values)
    {
        super();
        this.values = values;
    }

    public List<DiseaseFacetValue> getValues()
    {

        return values;
    }

    public void setValues(List<DiseaseFacetValue> values)
    {

        this.values = values;
    }
    
    public FacetType getType()
    {
        return FacetType.DISEASE;
    }

    @Override
    public int hashCode()
    {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((values == null) ? 0 : values.hashCode());
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
        DiseaseFacet other = (DiseaseFacet) obj;
        if (values == null)
        {
            if (other.values != null)
                return false;
        }
        else if (!values.equals(other.values))
            return false;
        return true;
    }
}
