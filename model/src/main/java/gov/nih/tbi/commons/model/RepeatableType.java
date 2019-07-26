
package gov.nih.tbi.commons.model;

/**
 * An enum type that maps a key/value pair to a comparison type
 * 
 * @author mvalei
 */
public enum RepeatableType
{
    EXACTLY(0L, "Exactly"), MORETHAN(1L, "At Least"), LESSTHAN(2L, "Up To");

    private Long id;
    private String value;

    RepeatableType(Long id, String value)
    {

        this.id = id;
        this.value = value;
    }

    public Long getId()
    {

        return id;
    }

    public String getValue()
    {

        return value;
    }

    public static final RepeatableType getDefault()
    {

        return RepeatableType.EXACTLY;
    }
}
