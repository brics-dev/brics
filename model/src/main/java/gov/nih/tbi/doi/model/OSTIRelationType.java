package gov.nih.tbi.doi.model;

import java.util.Hashtable;
import java.util.Map;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum(value = String.class)
public enum OSTIRelationType {
	@XmlEnumValue(value = "IsCitedBy")
	CITED_BY("IsCitedBy"),

	@XmlEnumValue(value = "Cites")
	CITIES("Cites"),

	@XmlEnumValue(value = "IsSupplementTo")
	SUPPLEMENT_TO("IsSupplementTo"),

	@XmlEnumValue(value = "IsSupplementedBy")
	SUPPLEMENTED_BY("IsSupplementedBy"),

	@XmlEnumValue(value = "IsContinuedBy")
	CONTINUED_BY("IsContinuedBy"),

	@XmlEnumValue(value = "Continues")
	CONTINUES("Continues"),

	@XmlEnumValue(value = "HasMetadata")
	HAS_METADATA("HasMetadata"),

	@XmlEnumValue(value = "IsMetadataFor")
	METADATA_FOR("IsMetadataFor"),

	@XmlEnumValue(value = "IsNewVersionOf")
	NEW_VERSION_OF("IsNewVersionOf"),

	@XmlEnumValue(value = "IsPreviousVersionOf")
	PREVIOUS_VERSION_OF("IsPreviousVersionOf"),

	@XmlEnumValue(value = "IsPartOf")
	PART_OF("IsPartOf"),

	@XmlEnumValue(value = "HasPart")
	HAS_PART("HasPart"),

	@XmlEnumValue(value = "IsReferencedBy")
	REFERENCED_BY("IsReferencedBy"),

	@XmlEnumValue(value = "References")
	REFERENCES("References"),

	@XmlEnumValue(value = "IsDocumentedBy")
	DOCUMENTED_BY("IsDocumentedBy"),

	@XmlEnumValue(value = "Documents")
	DOCUMENTS("Documents"),

	@XmlEnumValue(value = "IsCompiledBy")
	COMPILED_BY("IsCompiledBy"),

	@XmlEnumValue(value = "Compiles")
	COMPILES("Compiles"),

	@XmlEnumValue(value = "IsVariantFormOf")
	VARIANT_FORM_OF("IsVariantFormOf"),

	@XmlEnumValue(value = "IsOriginalFormOf")
	ORIGINAL_FORM_OF("IsOriginalFormOf"),

	@XmlEnumValue(value = "IsIdenticalTo")
	IDENTICAL_TO("IsIdenticalTo"),

	@XmlEnumValue(value = "IsReviewedBy")
	REVIEWED_BY("IsReviewedBy"),

	@XmlEnumValue(value = "Reviews")
	REVIEWS("Reviews"),

	@XmlEnumValue(value = "IsDerivedFrom")
	DERIVED_FROM("IsDerivedFrom"),

	@XmlEnumValue(value = "IsSourceOf")
	SOURCE_OF("IsSourceOf");

	private static final Map<String, OSTIRelationType> lookup = new Hashtable<String, OSTIRelationType>(25);

	static {
		for (OSTIRelationType type : OSTIRelationType.values()) {
			lookup.put(type.getTerm(), type);
		}
	}

	private String term;

	OSTIRelationType(String term) {
		this.term = term;
	}

	public String getTerm() {
		return term;
	}

	/**
	 * Retrieves an OSTIRelationType enum by its vocabulary term.
	 * 
	 * @param targetTerm - The vocabulary term to search for.
	 * @return The OSTIRelationType enum that matches the given vocabulary term, or null of no such term exists.
	 */
	public static synchronized OSTIRelationType getByVocabTerm(String targetTerm) {
		return lookup.get(targetTerm);
	}
}
