
package gov.nih.tbi.dictionary.model.hibernate;

import java.io.Serializable;

/**
 * Model for classification_element
 * 
 * @author Michael Valeiras
 * 
 */
public class ClassificationElement implements Serializable, Comparable<ClassificationElement>
{

    private static final long serialVersionUID = 3384613692285967810L;

    /**********************************************************************/
    private String uri;

    private Long id;

    private Classification classification;

    private Disease disease;

    private Subgroup subgroup;

    public ClassificationElement()
    {

    }

    public ClassificationElement(Disease disease, Classification classification, Subgroup subgroup)
    {

        this.disease = disease;
        this.classification = classification;
        this.subgroup = subgroup;
    }

    /**********************************************************************/

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public Disease getDisease()
    {

        return disease;
    }

    public void setDisease(Disease disease)
    {

        this.disease = disease;
    }

    public Classification getClassification()
    {

        return classification;
    }

    public void setClassification(Classification classification)
    {

        this.classification = classification;
    }

    public Subgroup getSubgroup()
    {

        return subgroup;
    }

    public void setSubgroup(Subgroup subgroup)
    {

        this.subgroup = subgroup;
    }

    public String toString()
    {

        return "[Classification: " + classification + ", Subgroup: " + subgroup + "]";
    }

    public String getUri()
    {

        return uri;
    }

    public void setUri(String uri)
    {

        this.uri = uri;
    }

    /**
     * 
     * The getMultiFields method gather the string representation of the Disease, Subgroup and Classification
     * 
     * The string will be "{Disease},{SubGroup},{Classification}"
     * 
     * @return
     */
    public String getMultiFields()
    {

        StringBuilder multipleField = new StringBuilder();
        multipleField.append(getDisease().getName() + "," + getSubgroup().getSubgroupName() + ","
                + getClassification().getName());
        return multipleField.toString();
    }

    /**********************************************************************/

    @Override
    public int compareTo(ClassificationElement o)
    {

        return this.getSubgroup().getSubgroupName().compareTo(o.getSubgroup().getSubgroupName());
    }
}
