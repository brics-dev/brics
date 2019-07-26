package gov.nih.tbi.dictionary.util;

public class XSDConstants {

	public static final String SCHEMA_TAG = "xs:schema";
	public static final String ELEMENT_TAG = "xs:element";
	public static final String COMPLEX_TYPE_TAG = "xs:complexType";
	public static final String COMPLEX_CONTENT_TAG = "xs:complexContent";
	public static final String EXTENSION_TAG = "xs:extension";
	public static final String SIMPLE_TYPE_TAG = "xs:simpleType";
	public static final String SEQUENCE_TAG = "xs:sequence";
	public static final String RESTRICTION_TAG = "xs:restriction";
	public static final String INCLUDE_TAG = "xs:include";
	public static final String ENUMERATION_TAG = "xs:enumeration";
	public static final String MIN_TAG = "xs:minInclusive";
	public static final String MAX_TAG = "xs:maxInclusive";
	public static final String MIN_LENGTH_TAG = "xs:minLength";
	public static final String MAX_LENGTH_TAG = "xs:maxLength";
	public static final String PATTERN_TAG = "xs:pattern";
	public static final String WHITESPACE_TAG = "xs:whiteSpace";
	public static final String ATTR_TAG = "xs:attribute";
	public static final String UNIQUE_TAG = "xs:unique";
	public static final String SELECTOR_TAG = "xs:selector";
	public static final String FIELD_TAG = "xs:field";
	public static final String 	MULTI_SELECT_TAG = "value";

	public static final String SOURCE_ATTR = "xmlns:xs";
	public static final String NAME_ATTR = "name";
	public static final String MAX_ATTR = "maxOccurs";
	public static final String MIN_ATTR = "minOccurs";
	public static final String SCHEMA_ATTR = "schemaLocation";
	public static final String BASE_ATTR = "base";
	public static final String VALUE_ATTR = "value";
	public static final String REF_ATTR = "ref";
	public static final String TYPE_ATTR = "type";
	public static final String SUB_ATTR = "substitutionGroup";
	public static final String XPATH_ATTR = "xpath";

	public static final String SOURCE_VAL = "http://www.w3.org/2001/XMLSchema";
	public static final String FRAMEWORK_SOURCE_VAL = "BricsCore.xsd";
	public static final String UNBOUNDED_VAL = "unbounded";
	public static final String DATASET_VAL = "Dataset";
	public static final String METADATA_VAL = "Metadata";
	public static final String ADMIN_FORM_ID = "AdminFormId";
	public static final String FORM_ID_VAL = "AdminFormId";
	public static final String STRING_TYPE_VAL = "xs:string";
	public static final String INT_TYPE_VAL = "xs:integer";
	public static final String DECIMAL_TYPE_VAL = "xs:decimal";
	public static final String DATE_TIME_TYPE_VAL = "xs:dateTime";
	public static final String DATE_TYPE_VAL = "xs:date";
	public static final String TIME_TYPE_VAL = "xs:time";
	public static final String COLLECTION_VAL = "Collection";
	public static final String STRUCTURE_TYPE_VAL = "FormStructureType";
	public static final String URI_VAL = "URI";

	public static final String NUMERIC_PATTERN = "[0-9]*";
	public static final String STRING_PATTERN = "[a-zA-Z0-9 ]*";
	public static final String GUID_PATTERN = "[A-Z0-9_]*";
	public static final String FILE_PATTERN = "(/.*)*(\\.)(\\w)*";
	public static final String THUMB_PATTERN = "(/.*)*(\\.)(\\w)*";
	public static final String COLLAPSE_PATTERN = "collapse";
	public static final String PRESERVE_PATTERN = "preserve";

	public static final int MAX_LENGTH_DEFAULT = 255;
	public static final int MAX_BIOSAMPLE_LENGTH = 100;
	public static final int MIN_BIOSAMPLE_LENGTH = 1;
}
