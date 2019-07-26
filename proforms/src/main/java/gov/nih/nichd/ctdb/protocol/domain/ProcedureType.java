package gov.nih.nichd.ctdb.protocol.domain;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum ProcedureType {
    // The status MULTIPLE refers to the Multiple statuses, usually the combination of Awaiting Publication and
    // Published.
    ASSESSMENTS(0L, "Assessment"), IMAGING(1L, "Imaging"), BIOSAMPLE(2L, "Biosample Collection"), Other(3L, "Other");

    private static final Map<Long, ProcedureType> lookup = new HashMap<Long, ProcedureType>();
    private static final Map<String, ProcedureType> lookupName = new HashMap<String, ProcedureType>();

    static {
        for (ProcedureType s : EnumSet.allOf(ProcedureType.class)) {
            lookup.put(s.getId(), s);
            lookupName.put(s.getName(), s);
        }
    }

    private long id;
    private String name;

    ProcedureType(long id, String name) {

        this.id = id;
        this.name = name;
    }

    public Long getId(){

        return id;
    }

    public String getName(){

        return name;
    }

    public static ProcedureType getById(Long id){

        return lookup.get(id);
    }

    public static ProcedureType getByName(String name){

        return lookupName.get(name);
    }
}
