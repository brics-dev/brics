
package gov.nih.tbi.ordermanager.model;

public enum BioRepositoryEnum
{
    CORIELL(345, "NINDS BIOREPOSITORY"), HARVARD(1, "HARVARD BIOREPOSITORY");

    private Integer id;
    private String value;

    private BioRepositoryEnum(Integer id, String value)
    {

        this.id = id;
        this.value = value;
    }

    public Integer getId()
    {

        return this.id;
    }

    public String getValue()
    {

        return this.value;
    }
}
