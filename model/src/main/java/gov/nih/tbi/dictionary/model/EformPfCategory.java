package gov.nih.tbi.dictionary.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EformPfCategory {
	NORMAL(0L, "Normal"), ADVERSE_EVENT_REPORT(1L, "Adverse Event Report");
	private static final Map<Long, EformPfCategory> lookupId = new HashMap<Long, EformPfCategory>();
	private static final Map<String, EformPfCategory> lookupName = new HashMap<String, EformPfCategory>();

    static {
        for (EformPfCategory s : EnumSet.allOf(EformPfCategory.class)) {
        	lookupId.put(s.getId(), s);
        	lookupName.put(s.getName(), s);
        }
    }

    private Long id;
    private String name;

    EformPfCategory(Long id, String name) {

        this.id = id;
        this.name = name;
    }

    public Long getId() {

        return id;
    }

    public String getName() {

        return name;
    }

    public static EformPfCategory getById(Long id)  {
        return lookupId.get(id);
    }
    
    public static EformPfCategory getByName(String name)  {
        return lookupName.get(name);
    }
}
