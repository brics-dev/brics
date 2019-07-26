package gov.nih.tbi.commons.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public enum QuestionType {

	TEXTBOX(1, "Textbox"),
	TEXTAREA(2, "Textarea"),
	SELECT(3, "Select"),
	RADIO(4, "Radio"),
	MULTI_SELECT(5, "Multi-Select"),
	CHECKBOX(6, "Checkbox"),
	CALCULATED(7, "Calculated"),
	PATIENT_CALENDAR(8, "Patient Calendar"),
	IMAGE_MAP(9, "Image Map"),
	VISUAL_SCALE(10, "Visual Scale"),
	FILE(11,"File"),
	TEXT_BLOCK(12, "Textblock");
	
	private static final Map<Integer, QuestionType> valueLookup = new HashMap<Integer, QuestionType>();
	private static final Map<String, QuestionType> nameLookup = new HashMap<String, QuestionType>();

	static {
		for (QuestionType s : EnumSet.allOf(QuestionType.class)) {
			valueLookup.put(s.getValue(), s);
		}
	}
	
	static {
		for (QuestionType s : EnumSet.allOf(QuestionType.class)) {
			nameLookup.put(s.getName().toLowerCase(), s);
		}
	}
 

	private Integer value;
	private String name;

	     QuestionType(Integer value, String name){
	        this.value = value;
	        this.name = name;
	    }
	    
	    public Integer getValue(){
	    	return this.value;
	    }
	    
	    public String getName(){
	    	return this.name;
	    }

		public static QuestionType getByName(String name) {
			if (name != null) {
				name = name.toLowerCase();
			}
			return nameLookup.get(name);
		}
		
		public static QuestionType getByValue(Integer value) {
			return valueLookup.get(value);
		}

	}
