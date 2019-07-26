
package gov.nih.tbi.commons.model;

public enum ConditionalOperators
{
    EQ(1L, "="), NEQ(2L, "!="), GT(3L, ">"), LT(4L, "<"), GTE(5L, ">="), LTE(6L, "<=");

    Long id;
    String name;

    private ConditionalOperators(Long id, String name)
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

    public static ConditionalOperators[] getEquals()
    {

        ConditionalOperators[] array = { EQ, NEQ };
        return array;
    }
}
