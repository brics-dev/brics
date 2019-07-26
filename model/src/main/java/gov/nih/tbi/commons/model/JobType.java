package gov.nih.tbi.commons.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by amakar on 9/7/2016.
 */
public enum JobType {

    DATASET_DATA_DELETION("DSDRM"),
    DATASTORE_DELETION("DSTRM"),
    SUBMISSION_JOIN_DELETION("SJNRM");

    private static final Map<String, JobType> dbIdLookup = new HashMap<>();

    static {
        for (JobType type : EnumSet.allOf(JobType.class)) {
            String databaseID = type.getId();
            dbIdLookup.put(databaseID, type);
        }
    }

    private String id;

    JobType(String _id) {

        this.id = _id;
    }

    public String getId() {
        return this.id;
    }

    public static JobType getFromDatabaseID(String _databaseID) {

        JobType result = dbIdLookup.get(_databaseID);

        return result;
    }
}
