package gov.nih.tbi.dictionary.ws.validation;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ValidationConstants {

	private static Properties p;
	public static final String VALUE_RANGE_DELIMITER = "::"; 
	public static final String VALUE_RANGE_BOTTOM_BOUND = "+";
	
	public static final String VALUE_COLUMN_REFERENCE = "$";
	public static final String VALUE_ROW_REFERENCE = "#";
	public static final String VALUE_REFERENCE_DIVIDER = ".";
	
	public static final String VALUE_DELIMITER = ";";
	
	public static String CONSTRAINT_AND = "&&";
	public static String CONSTRAINT_OR = "||";
	public static String CONSTRAINT_NEGATION = "!"; 
	public static String[] CONSTRAINT_LINKS = {"&&","||"}; //this is currently "AND", "OR"
	public static String[] CONSTRAINT_OPERATORS= {"<",">","=","!=","<=",">=","~", "!~"};// ~ currently "IN"
	public static String[] CONSTRAINT_NUMBER_OPERATORS = {"<",">","<=",">="};
	public static String[] CONSTRAINT_RANGE_OPERATORS = {"~", "!~"};
	
	public static final String VALIDATION_SUCCESS = "PASSED";
	public static final String VALIDATION_FAILURE = "FAILED";
	
	public static final String FIELD_REQUIRED_STR	    = "Required";
	public static final String FIELD_RECOMMENDED_STR    = "Recommended";
	public static final String FIELD_OPTIONAL_STR 	    = "Optional";
	public static final String FIELD_CONDITIONAL_STR 	= "Conditional";
	
	public static final String FRAME_ICON = retriveIcon();
	
	public static final long FILE_MAX_SIZE = 1000000;
	
	public static final String	APP_TITLE	= "Validation Tool";
	
	
	//The icon needs to be changed to suit the particluar Disease, change the properties file and the changes will be reflected
    private static String retriveIcon(){
		p = new Properties();
		String agreeText = null;
		File file = new File ("C://BRICs//webstarts.properties"); //This will have to probably changed to uploadTools.Properties
		try{
			FileInputStream myStream = new FileInputStream(file);
			try {
				p.load(myStream);
				agreeText = p.getProperty("webstart.validationTool.icon");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}
		return agreeText;
	}
    
	
}
