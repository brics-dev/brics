
package gov.nih.tbi.commons.model;

public enum PermissionGroupStatus
{
    ACTIVE(0L, "Active"), PENDING(1L, "Pending"), INACTIVE(2L, "Inactive");

    private Long id;
    private String name;

    PermissionGroupStatus(Long id, String name)
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
