
package gov.nih.tbi.commons.model;

public enum AuditType
{
    DELETE(0L, "Deleted"), UPDATE(1L, "Updated"), INSERT(2L, "Created");

    private Long id;
    private String value;

    AuditType(Long id, String value)
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
}
