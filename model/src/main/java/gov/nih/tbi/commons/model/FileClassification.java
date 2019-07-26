
package gov.nih.tbi.commons.model;

public enum FileClassification
{
    ADMIN(0L, "Admin"), SUPPORTING_DOCUMENT(1L, "Supporting Document"), DATASET_DATA(2L, "Dataset File"),
    META_STUDY_SUPPORTING_DOCUMENT(3L, "Meta Study Supporting Document"), META_STUDY_DATA(4L, "Meta Study Data"),
    SAVED_QUERY(5L, "Saved Query"), PICTURE(6L, "Picture"), ELECTRONIC_SIGNATURE(7L, "Electronic Signature");

    private Long id;
    private String name;

    FileClassification(Long id, String name)
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
