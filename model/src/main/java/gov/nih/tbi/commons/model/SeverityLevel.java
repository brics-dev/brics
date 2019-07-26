
package gov.nih.tbi.commons.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum SeverityLevel
{
    MINOR(0l, "Minor"), MAJOR(1l, "Major"), NEW(2l, "New");

    private static final Map<String, SeverityLevel> lookup = new HashMap<String, SeverityLevel>();

    static
    {
        for (SeverityLevel s : EnumSet.allOf(SeverityLevel.class))
        {
            lookup.put(s.getSeverityLevel(), s);
        }
    }

    private long id;
    private String severityLevel;

    SeverityLevel(long id, String severityLevel)
    {

        this.id = id;
        this.severityLevel = severityLevel;
    }

    public long getId()
    {

        return id;
    }

    public void setId(long id)
    {

        this.id = id;
    }

    public String getSeverityLevel()
    {

        return severityLevel;
    }

    public void setSeverityLevel(String severityLevel)
    {

        this.severityLevel = severityLevel;
    }

    public static SeverityLevel[] getMainTSeverityLevels()
    {

        SeverityLevel[] out = { MINOR, MAJOR, NEW };
        return out;
    }

    public static SeverityLevel getByName(String s)
    {

        return lookup.get(s);
    }
}
