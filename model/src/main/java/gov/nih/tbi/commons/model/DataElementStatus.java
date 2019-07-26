
package gov.nih.tbi.commons.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum DataElementStatus
{
    // The status MULTIPLE refers to the Multiple statuses, usually the combination of Awaiting Publication and
    // Published.
    DRAFT(0L, "Draft"), AWAITING(1L, "Awaiting Publication"), PUBLISHED(2L, "Published"), DEPRECATED(3L, "Deprecated"), RETIRED(4L, "Retired");

    private static final Map<Long, DataElementStatus> lookup = new HashMap<Long, DataElementStatus>();
    private static final Map<String, DataElementStatus> lookupName = new HashMap<String, DataElementStatus>();

    static
    {
        for (DataElementStatus s : EnumSet.allOf(DataElementStatus.class))
        {
            lookup.put(s.getId(), s);
            lookupName.put(s.getName(), s);
        }
    }

    private long id;
    private String name;

    DataElementStatus(long id, String name)
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

    public static DataElementStatus getById(Long id)
    {

        return lookup.get(id);
    }

    public static DataElementStatus getByName(String name)
    {

        return lookupName.get(name);
    }
}
