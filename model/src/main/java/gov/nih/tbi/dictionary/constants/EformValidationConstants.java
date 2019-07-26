package gov.nih.tbi.dictionary.constants;

public class EformValidationConstants {
	
	//FORM
	//form structure
	public static final String EFORM_NULL_FORM_STRUCTURE = "Form Structure is required";
	public static final String EFORM_SIZE_FORM_STRUCTURE = "Font Size must be fewer than 50 characters long";
	public static String FORM_STRUCTURE_NULL = "The associated form structure was not found in the system.";
	public static String RETIRED_DEPRICATED_DATA_ELEMENTS = "This form structure contains either deprecated and/or retired data element(s).";
	//name or title
	public static final String EFORM_NULL_TITLE = "Name/Title is required";
	public static final String EFORM_SIZE_TITLE = "Name/Title must be fewer than 100 characters long";
	//short name
	public static final String EFORM_NULL_SHORT_NAME = "Short Name is required";
	public static final String EFORM_SIZE_SHORT_NAME = "Short Name must be fewer than 50 characters long";
	public static final String EFORM_UNIQUE_SHORT_NAME = "Short Name must be unique";
	public static final String EFORM_PATTERN_SHORT_NAME = "Short Name must start with an alphabetic character and not contain any special characters or spaces";
	//description
	public static final String EFORM_SIZE_DESCRIPTION = "Description must be fewer than 1000 characters long";
	//is legacy
	public static final String EFORM_IS_LEGACY = "Is_Legacy flag must be false";
	//is shared
	public static final String EFORM_IS_SHARED = "Is_Shared flag must be false";
	//form name font
	public static final String EFORM_NULL_FORM_NAME_FONT = "Form Name Font is required";
	//form name color
	public static final String EFORM_NULL_FORM_NAME_COLOR = "Form Name Color is required";
	//section name font
	public static final String EFORM_NULL_SECTION_NAME_FONT = "Section Name Font is required";
	//section name color
	public static final String EFORM_NULL_SECTION_NAME_COLOR = "Section Name Color is required";
	//form header
	public static final String EFORM_SIZE_FORM_HEADER = "Form Header must be fewer than 500 characters long";
	//form footer
	public static final String EFORM_SIZE_FORM_FOOTER = "Form Footer must be fewer than 500 characters long";
	//font size
	public static final String EFORM_SIZE_FONT = "Font Size must be between 8 and 18";
	//cell padding
	public static final String EFORM_SIZE_CELL_PADDING = "Font Size must be between 1 and 20";
	
	
	
	
	//SECTION
	//name
	public static final String EFORM_NULL_SECTION_NAME = "Section Name is required";
	public static final String EFORM_SIZE_SECTION_NAME = "Section Name must be fewer than 128 characters long";
	//description
	public static final String EFORM_SIZE_SECTION_DESCRIPTION = "Section Text must be fewer than 4,000 characters long";
	//order val
	public static final String EFORM_NULL_SECTION_ORDER_VAL = "Order Val is required";
	//form row
	public static final String EFORM_NULL_SECTION_FORM_ROW = "Form Row is required";
	//form col
	public static final String EFORM_NULL_SECTION_FORM_COL = "Form Col is required";
	//initial repeated sections
	public static final String EFORM_NULL_INITIAL_REPEATED_SECTIONS = "Initial Repeated Sections is required";
	//max repeated sections
	public static final String EFORM_NULL_MAX_REPEATED_SECTIONS = "Max Repeated Sections is required";
	public static final String EFORM_SIZE_MAX_REPEATED_SECTIONS = "Max Repeated Sections must be fewer than 45";
	public static final String EFORM_SIZE_INIT_MAX_REPEATED_SECTIONS_RANGE = "Max Repeated Sections must be greater than Initial Repeated Sections";
	//group name
	public static final String EFORM_SIZE_SECTION_GROUP_NAME = "Group Name must be fewer than 75 characters long";
	
	
	
	
	//QUESTION
	//name
	public static final String EFORM_NULL_QUESTION_NAME = "Question Name is required";
	public static final String EFORM_SIZE_QUESTION_NAME = "Question Name must be fewer than 40 characters long";
	//text
	public static final String EFORM_NULL_QUESTION_TEXT = "Question Text is required";
	public static final String EFORM_SIZE_QUESTION_TEXT = "Question Text must be fewer than 4000 characters long";
	//default value
	public static final String EFORM_SIZE_DEFAULT_VALUE = "Default value must be fewer than 4000 characters long";
	//unanswered value
	public static final String EFORM_SIZE_UNANSWERED_VALUE = "Unanswered value must be fewer than 4000 characters long";
	//description up
	public static final String EFORM_SIZE_QUESTION_DESCRIPTION_UP = "Description Above must be fewer than 4000 characters long";
	//description down
	public static final String EFORM_SIZE_QUESTION_DESCRIPTION_DOWN = "Description Below must be fewer than 4000 characters long";
	//required
	public static String REQUIRED_QUESTIONS_DATA_ELEMENTS = "The eForm does not require all the necessary questions.";
	
	//QUESTION ANSWER OPTION
	//display
	public static final String EFORM_SIZE_QUESTION_ANSWER_OPTION_DISPLAY = "Question Option Display must be fewer than 400 characters long";
	//display
	public static final String EFORM_SIZE_QUESTION_ANSWER_OPTION_SUBMITTED_VALUE = "Question Option Submitted Value must be fewer than 400 characters long";
	//score
	public static final String EFORM_REQUIRED_QUESTION_ANSWER_OPTION_SCORE = "Question Option Score is required because question is used in a calculation rule";
	// question option type
	public static String QUESTION_ANSWER_OPTION_VALIDATION = "Question options do not apply to the correct question type.";
		
	
	//QUESTION ATTRIBUTES
	//skip rule equals
	public static final String EFORM_SIZE_SKIP_RULE_EQUALS = "Skip Rule Equals must be fewer than 50 characters long";
	//skip rule type
	public static final String EFORM_SKIP_RULE_TYPE = "Skip Rule Type must be 1 or 0";
	//skip rule type
	public static final String EFORM_SKIP_RULE_OPERATOR_TYPE = "Skip Rule Operator Type must be 1 or 0";
	//height align
	public static final String EFORM_NULL_HEIGHT_ALIGN = "Height Align is required";
	public static final String EFORM_SIZE_HEIGHT_ALIGN = "Height Align must be fewer than 20 characters long";
	//vertical align
	public static final String EFORM_NULL_VERTICAL_ALIGN = "Vertical Align is required";
	public static final String EFORM_SIZE_VERTICAL_ALIGN = "Vertical Align must be fewer than 20 characters long";
	//text color
	public static final String EFORM_NULL_TEXT_COLOR = "Text Color is required";
	public static final String EFORM_SIZE_TEXT_COLOR = "Text Color must be fewer than 20 characters long";
	//font face
	public static final String EFORM_NULL_FONT_FACE = "Font Face is required";
	public static final String EFORM_SIZE_FONT_FACE = "Font Face must be fewer than 20 characters long";
	//font size
	public static final String EFORM_SIZE_FONT_SIZE = "Font Size must be fewer than 3 characters long";
	//indent
	public static final String EFORM_SIZE_INDENT = "Indent must be fewer than 50";
	//xhtml text
	public static final String EFORM_SIZE_XHTML_TEXT = "Xhtml Text must be fewer than 4000 characters long";
	//data element name
	public static final String EFORM_SIZE_DATA_ELEMENT_NAME = "Data Element Name must be fewer than 50 characters long";
	//prepopulation value
	public static final String EFORM_SIZE_PREPOPULATION_VALUE = "Prepopulation Value must be fewer than 50 characters long";
	//conversion factor
	public static final String EFORM_SIZE_CONVERSION_FACTOR = "Conversion Factor must be fewer than 4000 characters long";
	//group name
	public static final String EFORM_SIZE_GROUP_NAME = "Group Name must be fewer than 75 characters long";
	//range operator
	public static final String EFORM_SIZE_RANGE_OPERATOR = "Range Operator must be fewer than 4000 characters long";
	//range value 1 
	public static final String EFORM_SIZE_RANGE_VALUE1 = "Range Value 1 must be fewer than 30 characters long";
	//range value 2
	public static final String EFORM_SIZE_RANGE_VALUE2 = "Range Value 2 must be fewer than 30 characters long";
	//answwer type
	public static final String EFORM_ANSWER_TYPE = "Answer Type must be 0 or 1";
	//text area width
	public static final String EFORM_SIZE_TEXTAREA_WIDTH = "Text Area Width must be less than 100";
	//text area width
	public static final String EFORM_SIZE_TEXTAREA_HEIGHT = "Text Area Height must be less than 60";
	//text box length
	public static final String EFORM_SIZE_TEXTBOX_LENGTH = "Text Box Length must be less than 20";
	//table header type
	public static final String EFORM_TABLE_HEADER_TYPE = "Table Header Type must be 0 or 1";
	//draft status
	public static final String EFORM_STATUS_DRAFT = "Status should not be Draft";
	//data element invalid
	public static final String EFORM_DE_INVALID = "Data Element in the eform must match with Data Element in form structure";
	
	//QUESTION DOCUMENT
	//file name
	public static final String EFORM_SIZE_QUESTION_IMAGE_FILE_NAME = "Question Image Filename must be fewer than 300 characters long";
	
	
	
	//SECTION QUESTION
	//calculation
	public static final String EFORM_SIZE_CALCULATION = "Calculation must be fewer than 4000 characters long";
	
		
		
	
	//EMAIL TRIGGER
	//to email address
	public static final String EFORM_SIZE_TO_EMAIL_ADDRESS = "To Email Address must be fewer than 255 characters long";
	//cc email address
	public static final String EFORM_SIZE_CC_EMAIL_ADDRESS = "CC Email Address must be fewer than 255 characters long";
	//email subject
	public static final String EFORM_SIZE_EMAIL_SUBJECT = "Email Subject must be fewer than 255 characters long";
	//email body
	public static final String EFORM_SIZE_EMAIL_BODY = "Email Body must be fewer than 4000 characters long";
	//email trigger answer
	public static final String EFORM_SIZE_EMAIL_TRIGGER_ANSWER = "Email Trigger Answer must be fewer than 255 characters long";
	
	
	
	//VISUAL SCALE
	//width
	public static final String EFORM_NULL_VISUAL_SCALE_WIDTH = "Visual Scale Width is required";
	//min
	public static final String EFORM_NULL_VISUAL_SCALE_MIN = "Visual Scale Minimum is required";
	//max
	public static final String EFORM_NULL_VISUAL_SCALE_MAX = "Visual Scale Maximum is required";
	//minmax
	public static final String EFORM_SIZE_VISUAL_SCALE_MINMAX_RANGE = "Visual Scale Minimum must be less than Visual Scale Maximum";
	//left text
	public static final String EFORM_SIZE_VISUAL_SCALE_LEFT_TEXT = "Visual Scale Left Text must be fewer than 255 characters long";
	//left text
	public static final String EFORM_SIZE_VISUAL_SCALE_RIGHT_TEXT = "Visual Scale Right Text must be fewer than 255 characters long";
	//center text
	public static final String EFORM_SIZE_VISUAL_SCALE_CENTER_TEXT = "Visual Scale Center Text must be fewer than 2000 characters long";
	
	
	
	
	
	
}
