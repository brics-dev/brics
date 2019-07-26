package gov.nih.tbi;

public class ModelConstants {

    /************************ ID PREFIXES **********************************************/
    public static final String SYSTEM_TIME_ZONE = "EST";
    public static final String PREFIX_STUDY = "-STUDY";
    public static final String PREFIX_META_STUDY = "-META-STUDY";
    public static final String PREFIX_DATASET = "-DATA";
    public static final String PREFIX_LEAD_NUM = "0";
    public static final String PREFIX_REGEX = "[" + PREFIX_LEAD_NUM + "]*(\\d+)";
    public static final String SURFIX_PERFORM_BM = "_DD";

    public static final String STUDY_REGEX = PREFIX_STUDY + PREFIX_REGEX;
    public static final String DATASET_REGEX = PREFIX_DATASET + PREFIX_REGEX;

    public static final String PRINCIPAL_INVESTIGATOR = "Principal Investigator";
    public static final String USER_NAME_MASK = "System Admin";
    public static final String EMPTY_STRING = "";
    public static final String RECORD_STRING = "record";
    public static final String MAIN_STRING = "Main";

    // Valid time formats, in order of precision
    public static final String ISO_DATE_FORMAT= "yyyy-MM-dd";
    public static final String UNIVERSAL_DATE_FORMAT = "dd-MMM-yyyy";
    public static final String[] UNIVERSAL_DATE_FORMATS = { "yyyy-MM-dd HH:mm:ss.SSSZ", "yyyy-MM-dd HH:mm:ss.SSS",
        "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd", "dd-MMM-yyyy hh:mm:ss a z",
        "dd/MMM/yyyy hh:mm:ss a z", "dd-MMM-yyyy hh:mm:ss a", "dd/MMM/yyyy hh:mm:ss a", "dd-MMM-yyyy hh:mm a z",
        "dd/MMM/yyyy hh:mm a z", "dd-MMM-yyyy hh:mm a", "dd/MMM/yyyy hh:mm a", "dd-MMM-yyyy HH:mm:ss z",
        "dd/MMM/yyyy HH:mm:ss z", "dd-MMM-yyyy HH:mm:ss", "dd/MMM/yyyy HH:mm:ss", "dd-MMM-yyyy HH:mm z",
        "dd/MMM/yyyy HH:mm z", "dd-MMM-yyyy HH:mm", "dd/MMM/yyyy HH:mm", "dd-MMM-yyyy z", "dd/MMM/yyyy z",
        "dd-MMM-yyyy", "dd/MMM/yyyy" };

    public static final int MD5_HASH_SIZE = 1000;
    public static final String WHITESPACE = " ";
    public static final String LEFT_PAREN = "(";
    public static final String RIGHT_PAREN = ")";
    
    public static final int DEFAULT_NUM_JOINS = 5;
    public final static String HASHCODE_METHOD_PREFIX = "getHashCode";

	/********************* EFORM CONSTANTS ************************************/
	public static final int MAX_COL_Q_IN_SECTION = 12;
	public static final String ID = "id";
	public static final String EFROM_NAME = "name";
	public static final String EFROM_DESCRIPTION = "description";
	public static final String EFROM_QUESTIONID = "questionId";
	public static final String EFROM_SECTIONID = "sectionId";
	public static final String EFROM_SECTION_PATTERN = "S_";
	public static final String EFROM_ROW = "row";
	public static final String EFROM_COLUMN = "col";
	public static final String EFROM_MINUS_ONE = "-1";
	public static final String NULL = "null";
	public static final String EFORM_CREATE = "create";
	public static final String EFORM_EDIT = "edit";
	public static final String OTHER_OPTION = "Other, please specify | null | ";
	public static final String OTHER_OPTION_DISPLAY = "Other, please specify";

	/********************* DOI CONSTANTS ************************************/
	public static final String DOI_PUBLISHER_VAL = "Center for Information Technology (CIT)";

}
