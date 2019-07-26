
package gov.nih.tbi.dictionary.model;

public enum FacetType
{
	OWNERSHIP(1L, "ownership"), STATUS(2L, "status"), DATA_TYPE(3L, "dataType"), DISEASE(4L, "disease"),
	DOMAIN(5L, "domain"), SUBDOMAIN(6L, "subdomain"), CLASSIFICATION(7L, "classification"),
	POPULATION(8L, "population"), TITLE(9L, "title"), SHORTNAME(10L, "shortName"), PERMISSIONS(10L, "uri"),
	VERSION(11L, "version"), CATEGORY(12L, "category"), KEYWORDS(13L, "keyword"), DESCRIPTION(14L, "description"),
	PERMISSIBLEVALUE(15L, "permissibleValue"), ALIAS(16L, "alias"), CREATED_DATE(17L, "dateCreated"),
	MODIFIED_DATE(18L, "modifiedDate"), SUBGROUP(19L, "subgroup"), LABELS(20L, "label"), EXTERNALID(21L, "externalId"),
	CREATEDBY(22L, "createdBy");

    private long id;
    private String name;

    FacetType(long id, String name)
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
