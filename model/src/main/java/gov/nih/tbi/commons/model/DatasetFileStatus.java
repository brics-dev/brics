
package gov.nih.tbi.commons.model;

public enum DatasetFileStatus
{
    PENDING(0L, "Pending"), COMPLETE(1L, "Complete");

    Long id;
    String name;

    private DatasetFileStatus(Long id, String name)
    {

        this.id = id;
        this.name = name;
    }

    public Long getId()
    {

        return id;
    }

    public String getName()
    {

        return name;
    }
}
