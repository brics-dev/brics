
package gov.nih.tbi.dictionary.model;

public enum FormStructureFacet
{
	// @formatter:off
    URI(0L, "uri"),
    OWNERSHIP(1L, "ownership"),
    STATUS(2L, "status"),
    // TYPE(3L, "type"), // Removed because this is an unused copy of SUBMISSION_TYPE
    DISEASE(4L, "disease"),
    TITLE(5L, "title"),
    SHORT_NAME(6L, "shortName"),
    VERSION(7L, "version"),
    CREATED_DATE(8L, "createdDate"),
    MODIFIED_DATE(9L, "modifiedDate"),
    SUBMISSION_TYPE(10L, "submissionType"),
    REQUIRED(11L, "required"),
    STANDARDIZATION(12L, "standardization"),
    IS_COPYRIGHTED(13L, "isCopyrighted"),
    FORM_LABEL(14L, "formLabel");
	// @formatter:on
    private long id;
    private String name;

    FormStructureFacet(long id, String name)
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
