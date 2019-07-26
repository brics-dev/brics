package gov.nih.tbi.commons.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by amakar on 9/7/2016.
 */
public enum JobStatus {

    NEW(0),
    PREPARING(1),
    READY(2),
    STARTED(3),
    RESUMED(4),
    COMPLETED(5),
    ABORTED(6),
    ERROR(7);

    private static final Map<Integer, JobStatus> dbIdLookup = new HashMap<>();

    static {
        for (JobStatus type : EnumSet.allOf(JobStatus.class)) {
            int databaseID = type.getId();
            dbIdLookup.put(databaseID, type);
        }
    }

    private int id;

    JobStatus(int _id) {

        this.id = _id;
    }

    public int getId() {
        return this.id;
    }

    public static JobStatus getFromDatabaseID(int _databaseID) {

        JobStatus result = dbIdLookup.get(_databaseID);

        return result;
    }
}
