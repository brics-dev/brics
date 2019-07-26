package gov.nih.tbi.commons.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum SkipRuleOperatorType {
	
	EQUALS(1, "Equals"),
	IS_BLANK(2, "Is Blank"),
	HAS_ANY_VALUE(3, "Has Any Value"),
	CONTAINS(4, "Contains");
	
	private static final Map<String, SkipRuleOperatorType> valueLookup = new HashMap<String, SkipRuleOperatorType>();
	private static final Map<String, SkipRuleOperatorType> nameLookup = new HashMap<String, SkipRuleOperatorType>();

	static {
		for (SkipRuleOperatorType s : EnumSet.allOf(SkipRuleOperatorType.class)) {
			valueLookup.put(Integer.toString(s.getValue()), s);
		}
	}
	
	static {
		for (SkipRuleOperatorType s : EnumSet.allOf(SkipRuleOperatorType.class)) {
			nameLookup.put(s.getName().toLowerCase(), s);
		}
	}
	
	private int value;
	private String name;
	
	
	SkipRuleOperatorType(int value, String name){
		this.value = value;
		this.name = name;
	}

	
	public int getValue(){
		return value;	
	}
	
	public String getName(){
		return name;
	}
	
	public static SkipRuleOperatorType getByName(String name) {
		if (name != null) {
			name = name.toLowerCase();
		}
		return nameLookup.get(name);
	}
	
	public static SkipRuleOperatorType getByDisplay(int value) {
		return valueLookup.get(Integer.toString(value));
	}
}
