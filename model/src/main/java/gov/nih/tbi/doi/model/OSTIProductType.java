package gov.nih.tbi.doi.model;

import java.util.Hashtable;
import java.util.Map;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum(value = String.class)
public enum OSTIProductType {
	@XmlEnumValue(value = "Dataset")
	DATASET("Dataset"),

	@XmlEnumValue(value = "Text")
	TEXT("Text"),

	@XmlEnumValue(value = "Collection")
	COLLECTION("Collection");

	private static final Map<String, OSTIProductType> lookup = new Hashtable<String, OSTIProductType>();

	static {
		for (OSTIProductType type : OSTIProductType.values()) {
			lookup.put(type.getTerm(), type);
		}
	}

	private String term;

	OSTIProductType(String term) {
		this.term = term;
	}

	public String getTerm() {
		return term;
	}

	/**
	 * Retrieves an OSTIProductType enum by its vocabulary term.
	 * 
	 * @param targetTerm - The vocabulary term to search for.
	 * @return The OSTIProductType enum that matches the given vocabulary term, or null of no such term exists.
	 */
	public static synchronized OSTIProductType getByVocabTerm(String targetTerm) {
		return lookup.get(targetTerm);
	}
}
