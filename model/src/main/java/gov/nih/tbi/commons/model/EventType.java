package gov.nih.tbi.commons.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by amakar on 8/31/2016.
 */
public enum EventType {

    DATASET_DELETION("DSDEL","Dataset Deleted"),
	REQUESTED_ARCHIVED_APPROVED("RARAP","Requested Archive Approved"),
	REQUESTED_SHARED_APPROVED("RSHAP","Requested Share Approved"),
	REQUESTED_DELETE_APPROVED("RDELA","Requested Delete Approved"),
	REQUESTED_ARHIVED_REJECTED("RARRE","Requested Archive Rejected"),
	REQUESTED_SHARED_REJECTED("RSHRE","Requested Share Rejected"),
	REQUESTED_DELETE_REJECTED("RDELR","Requested Delete Rejected"),
	STATUS_CHANGE_TO_SHARED("SCSHD","Status Changed to Shared"),
	STATUS_CHANGE_TO_ARCHIVE("SCARD","Status Changed to Archived"),
	STATUS_CHANGE_TO_ERROR("SCERR","Status Changed to Error During Load"),
	STATUS_CHANGE_TO_PUBLISHED("SCPUB","Status Changed to Published"),
	REQUESTED_PUBLISH_APPROVED("RPUAP","Requested Publish Approved"),
	REQUESTED_PUBLISH_REJECTED("RPURE","Requested Publish Rejected"),
	STATUS_CHANGE_SHARED_DRAFT("SCSDR","Status Changed to Shared Draft"),
	STATUS_CHANGE_REVERT_DRAFT("SCRDR","Reverted Shared Draft"),
	STATUS_CHANGE_UNARCHIVED("SCUAR","Status Changed to UnArchived"),
	STATUS_CHANGE_RETIRED("SCRTD","Status Changed to Retired"),
	STATUS_CHANGE_DEPRECATED("SCDEP","Status Changed to Deprecated"),
	MINOR_MAJOR_CHANGE("MIMAC","Minor Major change");
	

    private static final Map<String, EventType> dbIdLookup = new HashMap<>();

    static {
        for (EventType type : EnumSet.allOf(EventType.class)) {
            String databaseID = type.getId();
            dbIdLookup.put(databaseID, type);
        }
    }

    private String id;
    private String description;

    EventType(String id, String description){
    	this.id=id;
    	this.description=description;
    }

    public String getId() {
        return this.id;
    }

    public String getDescription() {
		return description;
	}
    
	public static EventType getFromDatabaseID(String _databaseID) {

        EventType result = dbIdLookup.get(_databaseID);

        return result;
    }
}


