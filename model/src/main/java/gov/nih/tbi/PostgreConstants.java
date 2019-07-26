
package gov.nih.tbi;

/**
 * Class to store constant values specific to PostgreSql.
 * 
 * @author dhollo
 * 
 */
public class PostgreConstants
{

    public static final String[] SYSTEM_COLUMNS = new String[] { "oid", "tableoid", "xmin", "cmin", "xmax", "cmax",
            "ctid" };
    public static final String CREATE_CHARACTER_VARYING = "character varying(%1$d)";
    public static final String CREATE_NUMERIC = "numeric";
    public static final String CREATE_TIMESTAMP = "timestamp with time zone";
    public static final String CREATE_GUID = "character varying(30)";
    public static final String CREATE_FILE = "character varying(300)";
    public static final String CREATE_THUMBNAIL = "character varying(300)";
    public static final String CREATE_BIOSAMPLE = "character varying(100)";
    public static final String CREATE_TRIPLANAR = "character varying(300)";
}