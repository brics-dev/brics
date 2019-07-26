
package gov.nih.tbi.dictionary.model;

public class ClassificationFacetValue
{

    private String subgroup;
    private String classification;

    public ClassificationFacetValue(String classification, String subgroup)
    {

        super();
        this.subgroup = subgroup;
        this.classification = classification;
    }

    public String getSubgroup()
    {

        return subgroup;
    }

    public void setSubgroup(String subgroup)
    {

        this.subgroup = subgroup;
    }

    public String getClassification()
    {

        return classification;
    }

    public void setClassification(String classification)
    {

        this.classification = classification;
    }

    @Override
    public int hashCode()
    {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((classification == null) ? 0 : classification.hashCode());
        result = prime * result + ((subgroup == null) ? 0 : subgroup.hashCode());
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
        ClassificationFacetValue other = (ClassificationFacetValue) obj;
        if (classification == null)
        {
            if (other.classification != null)
                return false;
        }
        else if (!classification.equals(other.classification))
            return false;
        if (subgroup == null)
        {
            if (other.subgroup != null)
                return false;
        }
        else if (!subgroup.equals(other.subgroup))
            return false;
        return true;
    }

    @Override
    public String toString()
    {

        return "ClassificationFacetValue [subgroup=" + subgroup + ", classification=" + classification + "]";
    }

}
