package gov.nih.tbi.commons.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum AnswerType {

	STRING(1, "String", "^\\w+$"), 
	NUMERIC(2, "Numeric", "^-?[0-9]*\\.?[0-9]*$"), 
	DATE(3, "Date", "yyyy-MM-dd"), 
	DATETIME(4, "Date-Time", "yyyy-MM-dd HH:mm");

	private static final Map<String, AnswerType> nameLookup = new HashMap<String, AnswerType>();
	private static final Map<Integer, AnswerType> valueLookup = new HashMap<Integer, AnswerType>();

	static {
		for (AnswerType s : EnumSet.allOf(AnswerType.class)) {
			nameLookup.put(s.getName().toLowerCase(), s);
		}
	}

	static {
		for (AnswerType s : EnumSet.allOf(AnswerType.class)) {
			valueLookup.put(s.getValue(), s);
		}
	}

	private int value;
	private String name;
	private String regex;

	AnswerType(int value, String name, String regex) {

		this.value = value;
		this.name = name;
		this.regex = regex;
	}

	public int getValue() {

		return value;
	}

	public String getName() {

		return name;
	}

	public String getRegex() {

		return regex;
	}

	public static AnswerType getByName(String name) {
		if (name != null) {
			name = name.toLowerCase();
		}
		return nameLookup.get(name);
	}

	public static AnswerType getByValue(Integer value) {
		return valueLookup.get(value);
	}

}
