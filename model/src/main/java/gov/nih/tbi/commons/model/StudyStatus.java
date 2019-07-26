
package gov.nih.tbi.commons.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum StudyStatus
{
    PUBLIC(0L, "Public"), PRIVATE(1L, "Private"), REQUESTED(2L, "Requested"), REJECTED(3L, "Rejected");

    private static final Map<Long, StudyStatus> lookup = new HashMap<Long, StudyStatus>();

    static
    {
        for (StudyStatus s : EnumSet.allOf(StudyStatus.class))
            lookup.put(s.getId(), s);
    }

    private Long id;
    private String name;

    StudyStatus(Long id, String name)
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

    public static StudyStatus getById(Long id)
    {

        return lookup.get(id);
    }
}
