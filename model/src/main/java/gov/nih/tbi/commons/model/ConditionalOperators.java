
package gov.nih.tbi.commons.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum ConditionalOperators
{
	EQ(1L, "="), NEQ(2L, "!="), GT(3L, ">"), LT(4L, "<"), GTE(5L, ">="), LTE(6L, "<=");

	private static final Map<Long, ConditionalOperators> idLookup = new HashMap<Long, ConditionalOperators>();
	private static final Map<String, ConditionalOperators> nameLookup = new HashMap<String, ConditionalOperators>();

	static {
		for (ConditionalOperators co : EnumSet.allOf(ConditionalOperators.class)) {
			idLookup.put(co.getId(), co);
			nameLookup.put(co.getName(), co);
		}
	}


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

	public static ConditionalOperators getById(Long id) {
		return idLookup.get(id);
	}

	public static ConditionalOperators getByName(String name) {
		return nameLookup.get(name);
	}
}
