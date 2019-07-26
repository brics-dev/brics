
package gov.nih.tbi;

public class ApplicationsConstants
{

    // Error locations
    public static final String LOC_ENTRY = "The record that begins on row %d ";
    public static final String LOC_ROW_COLUMN = "The cell located at row %d, column %d ";
    public static final String LOC_ROW = "Row %d ";
    public static final String LOC_COLUMN = "Column %d ";
    public static final String LOC_NAME_COLUMN = "The data element \"%s\", located at column %d ";
    public static final String LOC_NAME_GROUP = "The data element \"%s\" in repeatable group \"%s\" ";
    public static final String LOC_ROW_COLUMN_NAME = "The data entry at row %d, column %d, for the data element \"%s.%s\" ";
    public static final String LOC_GUID_ROW_COLUMN_NAME = "The data entry at guid %s, row %d, column %d, for the data element \"%s.%s\" ";
    public static final String LOC_DATA_ROW_COLUMN_NAME = "The data '%s' at row %d, column %d, for the data element \"%s.%s\" ";
    public static final String LOC_FILE = "This file, containing the structure \"%s\" ";

    // Errors
    public static final String ERR_MANY_RGS = "must contain more than %d instances of the repeatable group: \"%s\".";
    public static final String ERR_FEW_RGS = "must contain less than %d instances of the repeatable group: \"%s\".";
    public static final String ERR_EXACTLY_RGS = "must contain exactly %d instances of the repeatable group: \"%s\".";
    public static final String ERR_EMPTY_REPEATABLE_GROUP = "repeatable group %d is empty";
    public static final String ERR_EMPTY_ONE_ELEMENT_REPEATABLE_GROUP =
    		"repeatable group with only one element of %s is empty";

    public static final String ERR_RECORD_MISSING = "is missing the 'record' column which distinguishes new rows of data.";
    public static final String ERR_RECORD_LOCATION = "contains the 'record' column in the wrong place.";

    public static final String ERR_MISSING_REQUIRED = "is missing. Data structure %s identifies this element as REQUIRED.";
    public static final String ERR_MISSING_RECOMMENDED = "is missing. Data structure %s identifies this element as RECOMMENDED.";
    public static final String ERR_MISSING_CONDITIONAL = "references a conditional data element \"%s\" but that data element does not exist.";
    public static final String ERR_BROKEN_CONDITIONAL = "has broken conditional logic.  Please contact adminstration.";

    public static final String ERR_COLUMN_REQUIRED = "is empty. Data structure %s identifies this element as REQUIRED.";
    public static final String ERR_COLUMN_PROHIBITED = "is not empty. Data structure %s identifies this element as PROHIBITED.";
    public static final String ERR_COLUMN_RECOMMENDED = "is empty. Data structure %s identifies this element as RECOMMENDED.";
    public static final String ERR_CELL_WITH_NO_HEADER = "There is data in a cell that does not have a column header.";

    public static final String ERR_TYPE_THUMBNAIL = "is of type Thumbnail. The file is not a image file.";
    public static final String ERR_TYPE_FILE_PERMISSION = "is of type File or Thumbnail. User does not have read permissions to the file.";
    public static final String ERR_TYPE_FILE_EXCLUDED = "is of type File or Thumbnail. The file must be included.";
    public static final String ERR_TYPE_FILE_CASE_SENSE = "is of type File or Thumbnail. A file was found with non-matching case, file names are case sensitive.";
    public static final String ERR_TYPE_FILE_MISSING = "is of type File or Thumbnail. The file could not be found.";
    public static final String ERR_TYPE_FILE_NAME_DUPLICATE = "is a duplicate file name. Please make a unique file name.";

    public static final String ERR_TYPE_STRING_SIZE = "is of type String and should be no longer than %d characters per element definition.";

    public static final String ERR_TYPE_BIOSAMPLE_SIZE = "is of type Biosample and should be no longer than %d characters.";

    public static final String ERR_RESTRICT_ONLY_SINGLE = "has a SINGLE input restriction, but more than one input has been provided.";

    public static final String ERR_TYPE_INCORRECT = "is not the correct type. The element must have a type %s.";
    public static final String ERR_RANGE_INCORRECT = "is not within element's value range - %s";
    public static final String ERR_DUPLICATE_RANGE_VALUE = "contains duplicate values - %s";
    public static final String ERR_RANGE_DATE = "is an invalid date in the future.";
    public static final String ERR_INVALID_CHAR = "contains an invalid special character.";
	public static final String WARN_INVALID_DATA_FORMAT =
			"A record identifier was not found in column 1 row %d for form structure %s. Please make make sure your data starts on row 3 with a identifier in column 1.";
    public static final String ERR_UNKNOWN_FILE = "The file \"%s\" is unknown. This file must be excluded or associated with a data file to build a submission package.";
    
    public static final String ERR_BLANK_REQUIRED = "is %s instead of the required blank";
    public static final String ERR_UNKNOWN_REQUIRED = "is %s instead of the required Unknown";
    public static final String ERR_UNTESTABLE_REQUIRED = "is %s instead of the required Untestable";
    public static final String ERR_INCORRECT_SUM = "is %s instead of the correct sum = %s";
    public static final String ERR_INCORRECT_CALCULATION = "is %s instead of the correct calculation = %s";
    public static final String ERR_NOT_INTEGER = "is %s instead of being the required integer";
    public static final String ERR_NOT_PERMISSIBLE_VALUE = "is %s instead of being a permissible value in %s";
    public static final String WARNING_INCORRECT_CALCULATION = "is %s instead of the correct calculation = %s";
    public static final String ERR_INCORRECT_MINIMUM = "is %s instead of the correct minimum = %s";
    public static final String WARNING_INCORRECT_AGE = "is %s instead of the required %s";
    public static final String WARNING_BLANK_FOUND = "data has not been provided";
    public static final String WARNING_MISSING_REQUIRED_VALUE =
    		"has been calculated with 1 missing required value.";
    public static final String WARNING_MISSING_REQUIRED_VALUES =
    		"has been calculated with %d missing required values.";
    public static final String ERR_BLANK_FOUND = "data has not been provided";
    public static final String WARNING_FEWER3_PHONEMIC = "data only submitted for %s phonemic trials instead of all 3";
    public static final String WARNING_FEWER3_SEMANTIC = "data only submitted for %s semantic trials instead of all 3";
    public static final String ERR_TWO_NOT_EQUAL = "%s value of %s should equal %s value of %s";
    public static final String ERR_ILLEGAL_GROUNDSURFTYP = "GroundSurfTyp is an illegal %s instead of the required Firm or Foam";
    public static final String ERR_TOO_FEW_TRIALS = "has been calculated with only %d of the required %d trials";

    // Other
    public static final int DATA_START_LINE = 3;
    public static final char ESCAPE_CHAR = 0; // Used for CSVReader
    public static final char QUOTE_CHAR = '"'; // Used for CSVReader

    // XML Attributes
    public static final String SHORT_NAME = "shortName";
    public static final String VERSION = "version";
    public static final String DATA_STRUCTURE = "dataStructure";
    public static final String REPEATABLE_GROUP = "repeatableGroup";
    public static final String NAME = "name";
    public static final String GROUP = "group";
    public static final String ALIAS = "alias";
    public static final String VALUE = "value";
    public static final String DATA = "data";
    public static final String DATA_FILE = "dataFile";

    public static final String SEMI_COLON = ";";

    /******************************
     * Webstart Icons
     * 
     ******************************/

    public static final String TOOLS_ICON = "images/tools.jpg";
    public static final String FITBIR_ICON = "TBI-Favicon-orange.jpg";
    public static final String PDBP_ICON = "PDBPFavicon.jpg";

    /******************************
     * Shared EULA Agreement Constants
     * 
     ******************************/
    public static final String AGREEMENT_TEXT_1 = "<html><b>Data Privacy</b><br /> This system is a collaborative environment with privacy rules that pertain to the collection and display of imaging data. Before accessing and using this systems, please ensure you familiarize yourself with our privacy rules available through the Access Request and supporting documentation.<br /><br /> Collection of this information is authorized under 42 U.S.C. 241, 242, 248, 281(a)(b)(1)(P) and 44 U.S.C. 3101. The primary use of this information is to facilitate medical research. This information may be disclosed to researchers for research purposes, and to system administrators for evaluation and data normalization.<br /><br /> Rules governing submission of this information are based on the data sharing rules defined in the Notice of Grant Award (NOGA). If you do not have a grant defining data sharing requirements, data submission is voluntary. Data entered into the system will be used solely for scientific and research purposes and is designed to further the understanding the disease. Modification of information may be addressed by contacting your system administrator at ";
    public static final String AGREEMENT_TEXT_2 = ". Significant system update information may be posted on the site as required.<br /><br /></html>";

    /******************************
     * Upload Manager Constants
     * 
     ******************************/

    public static final String FRAME_ICON = TOOLS_ICON;
    public static final String APPLICATION_TITLE = "Upload Tool";
    public static final int MAXIMUM_CONCURRENT_UPLOAD = 1;
    public static final int MAX_DATASET_NAME_LENGTH = 55;
    public static final String COMBO_BOX_VALUE = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
    public static final String UPLOAD_THREADGROUP_NAME = "MULTI_THREAD_UPLOADING";

    // public static final String POLICY_EMAIL = getProperty("webstart.email");
    // public static final String UPLOAD_AGREEMENT_TEXT = getProperty("webstart.uploadTool.EULAArgreement");

    /******************************
     * Validation Tool Constants
     * 
     ******************************/

    public static final String VALUE_RANGE_DELIMITER = "::";
    public static final String VALUE_RANGE_BOTTOM_BOUND = "+";

    public static final String VALUE_COLUMN_REFERENCE = "$";
    public static final String VALUE_ROW_REFERENCE = "#";
    public static final String VALUE_REFERENCE_DIVIDER = ".";

    public static final String VALUE_DELIMITER = ";";

    public static String CONSTRAINT_AND = "&&";
    public static String CONSTRAINT_OR = "||";
    public static String CONSTRAINT_NEGATION = "!";
    public static String[] CONSTRAINT_LINKS = { "&&", "||" }; // this is currently "AND", "OR"
    public static String[] CONSTRAINT_OPERATORS = { "<", ">", "=", "!=", "<=", ">=", "~", "!~" };// ~ currently "IN"
    public static String[] CONSTRAINT_NUMBER_OPERATORS = { "<", ">", "<=", ">=" };
    public static String[] CONSTRAINT_RANGE_OPERATORS = { "~", "!~" };

    public static final String VALIDATION_SUCCESS = "PASSED";
    public static final String VALIDATION_FAILURE = "FAILED";

    public static final String FIELD_REQUIRED_STR = "Required";
    public static final String FIELD_RECOMMENDED_STR = "Recommended";
    public static final String FIELD_OPTIONAL_STR = "Optional";
    public static final String FIELD_CONDITIONAL_STR = "Conditional";

    public static final String VAL_FRAME_ICON = TOOLS_ICON;

    public static final long FILE_MAX_SIZE = 1000000;

    public static final String APP_TITLE = "Validation Tool";

    /**********************************
     * Download Tool Constants
     * 
     * ********************************/

    public static final String APPLICATION_HEADER = "Download Manager";
}
