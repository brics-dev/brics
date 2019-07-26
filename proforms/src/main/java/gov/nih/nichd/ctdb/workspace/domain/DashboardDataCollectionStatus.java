package gov.nih.nichd.ctdb.workspace.domain;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import gov.nih.nichd.ctdb.common.CtdbConstants;

public enum DashboardDataCollectionStatus {
	NOTSTARTED(0, CtdbConstants.DATACOLLECTION_STATUS_NOTSTARTED, CtdbConstants.DATACOLLECTION_STATUS_NOTSTARTED_WHITE),
	INPROGRESS(1, CtdbConstants.DATACOLLECTION_STATUS_INPROGRESS, CtdbConstants.DATACOLLECTION_STATUS_INPROGRESS_RED),
	COMPLETED(2, CtdbConstants.DATACOLLECTION_STATUS_COMPLETED, CtdbConstants.DATACOLLECTION_STATUS_COMPLETED_YELLOW), 
	LOCKED(3, CtdbConstants.DATACOLLECTION_STATUS_LOCKED, CtdbConstants.DATACOLLECTION_STATUS_LOCKED_GREEN);

    private static final Map<Integer, DashboardDataCollectionStatus> lookup = new HashMap<Integer, DashboardDataCollectionStatus>();
    private static final Map<String, DashboardDataCollectionStatus> lookupName = new HashMap<String, DashboardDataCollectionStatus>();

    static {
        for (DashboardDataCollectionStatus s : EnumSet.allOf(DashboardDataCollectionStatus.class)) {
            lookup.put(s.getId(), s);
            lookupName.put(s.getName(), s);
        }
    }

    private Integer id;
    private String name;
    private String assoColor;

    DashboardDataCollectionStatus(Integer id, String name, String assoColor) {
        this.id = id;
        this.name = name;
        this.assoColor = assoColor;
    }

    public Integer getId(){
        return id;
    }

    public String getName(){
        return name;
    }
    
    public String getAssoColor() {
    	return assoColor;
    }

    public static DashboardDataCollectionStatus getById(Integer id){
        return lookup.get(id);
    }

    public static DashboardDataCollectionStatus getByName(String name){
        return lookupName.get(name);
    }
}
