package gov.nih.tbi.commons.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum SkipRuleType {

	REQUIRE(1, "Require"),
	DISABLE(2, "Disable");
	
	private static final Map<String, SkipRuleType> valueLookup = new HashMap<String, SkipRuleType>();
	private static final Map<String, SkipRuleType> nameLookup = new HashMap<String, SkipRuleType>();

	static {
		for (SkipRuleType s : EnumSet.allOf(SkipRuleType.class)) {
			valueLookup.put(Integer.toString(s.getValue()), s);
		}
	}
	
	static {
		for (SkipRuleType s : EnumSet.allOf(SkipRuleType.class)) {
			nameLookup.put(s.getName().toLowerCase(), s);
		}
	}
	
	private int value;
	private String name;
	
	
	SkipRuleType(int value, String name){
		this.value = value;
		this.name = name;
	}

	
	public int getValue(){
		return value;	
	}
	
	public String getName(){
		return name;
	}
	
	public static SkipRuleType getByName(String name) {
		if (name != null) {
			name = name.toLowerCase();
		}
		return nameLookup.get(name);
	}
	
	public static SkipRuleType getByDisplay(int value) {
		return valueLookup.get(Integer.toString(value));
	}
}
