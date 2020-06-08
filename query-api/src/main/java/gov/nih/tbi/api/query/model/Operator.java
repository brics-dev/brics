package gov.nih.tbi.api.query.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets Operator
 */
public enum Operator {
	AND("AND", "&&"), OR("OR", "||");

	private String value;
	private String booleanValue;

	Operator(String value, String booleanValue) {
		this.value = value;
		this.booleanValue = booleanValue;
	}

	public String getBooleanValue() {
		return booleanValue;
	}

	@Override
	@JsonValue
	public String toString() {
		return String.valueOf(value);
	}

	@JsonCreator
	public static Operator fromValue(String text) {
		for (Operator b : Operator.values()) {
			if (String.valueOf(b.value).equals(text)) {
				return b;
			}
		}
		return null;
	}
}
