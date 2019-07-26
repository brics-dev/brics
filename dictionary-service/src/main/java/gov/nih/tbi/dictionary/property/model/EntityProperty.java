package gov.nih.tbi.dictionary.property.model;

public abstract class EntityProperty {

	public static final int RDF_PROPERTY = 0;
	public static final int SQL_PROPERTY = 1;

	private String fieldsValue;

	private int datastore;

	public String getFieldsValue() {
		return fieldsValue;
	}

	public void setFieldsValue(String fieldsValue) {
		this.fieldsValue = fieldsValue;
	}

	public int getDatastore() {
		return datastore;
	}

	public void setDatastore(int datastore) {
		this.datastore = datastore;
	}



}
