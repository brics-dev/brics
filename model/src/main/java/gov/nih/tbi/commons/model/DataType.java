package gov.nih.tbi.commons.model;

import gov.nih.tbi.PostgreConstants;
import gov.nih.tbi.dictionary.model.NativeTypeConverter;
import gov.nih.tbi.dictionary.model.conversion.DoubleConverter;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum DataType {
	// new LongConverter()
	ALPHANUMERIC(0L, "Alphanumeric", "unrestricted text", PostgreConstants.CREATE_CHARACTER_VARYING, null),
	NUMERIC(1L, "Numeric Values", "", PostgreConstants.CREATE_NUMERIC, new DoubleConverter()),
	DATE(2L, "Date or Date & Time", "", PostgreConstants.CREATE_TIMESTAMP, null),
	GUID(3L, "GUID", "Globally Unique Patient Identifier", PostgreConstants.CREATE_GUID, null),
	FILE(4L, "File", "", PostgreConstants.CREATE_FILE, null),
	THUMBNAIL(5L, "Thumbnail", "Image files only.", PostgreConstants.CREATE_THUMBNAIL, null),
	BIOSAMPLE(6L, "Biosample", "", PostgreConstants.CREATE_BIOSAMPLE, null),
	TRIPLANAR(7L, "Tri-Planar", "", PostgreConstants.CREATE_TRIPLANAR, null);

	private Long id;
	private String value;
	private String specialInstructions;
	private String sqlFormatString; // Used by Formatter to generate SQL to create a column for this type
	// private SortingType sortType;
	private NativeTypeConverter typeConverter;

	private static final Map<Long, DataType> lookup = new HashMap<Long, DataType>();
	private static final Map<String, DataType> lookupValue = new HashMap<String, DataType>();

	static {
		for (DataType s : EnumSet.allOf(DataType.class)) {
			lookup.put(s.getId(), s);
			lookupValue.put(s.getValue(), s);
		}
	}

	DataType(Long id, String value, String specialInstructions, String sqlFormatString,
			/* SortingType sortType, */ NativeTypeConverter converter) {

		this.id = id;
		this.value = value;
		this.specialInstructions = specialInstructions;
		this.sqlFormatString = sqlFormatString;
		// this.sortType = sortType;
		this.typeConverter = converter;
	}

	public Long getId() {

		return id;
	}

	public String getValue() {

		return value;
	}

	public String getSpecialInstructions() {

		return specialInstructions;
	}

	public String getSqlFormatString() {

		return sqlFormatString;
	}

	public static DataType getById(Long id) {

		return lookup.get(id);
	}

	/*
	 * public SortingType getSortingType() { return this.sortType; }
	 */

	public NativeTypeConverter getTypeConverter() {
		return this.typeConverter;
	}

	public static DataType getByValue(String value) {
		return lookupValue.get(value);
	}
}
