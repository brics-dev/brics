package gov.nih.tbi.pojo;

import java.io.Serializable;

/**
 * Model object that represents a single row in QT downloading mapping file.
 * 
 * @author jim3
 */
public class DownloadPVMappingRow implements Serializable {

	private static final long serialVersionUID = 3698273104391308737L;

	private String deName;
	private String deTitle;
	private String deDescription;
	private String pvValue;
	private String pvCode;
	private String pvDesciption;
	private String schemaDeId;
	private String schemaValue;

	public String getDeName() {
		return deName;
	}

	public void setDeName(String deName) {
		this.deName = deName;
	}

	public String getDeTitle() {
		return deTitle;
	}

	public void setDeTitle(String deTitle) {
		this.deTitle = deTitle;
	}

	public String getDeDescription() {
		return deDescription;
	}

	public void setDeDescription(String deDescription) {
		this.deDescription = deDescription;
	}

	public String getPvValue() {
		return pvValue;
	}

	public void setPvValue(String pvValue) {
		this.pvValue = pvValue;
	}

	public String getPvCode() {
		return pvCode;
	}

	public void setPvCode(String pvCode) {
		this.pvCode = pvCode;
	}

	public String getPvDesciption() {
		return pvDesciption;
	}

	public void setPvDesciption(String pvDesciption) {
		this.pvDesciption = pvDesciption;
	}

	public String getSchemaDeId() {
		return schemaDeId;
	}

	public void setSchemaDeId(String schemaDeId) {
		this.schemaDeId = schemaDeId;
	}

	public String getSchemaValue() {
		return schemaValue;
	}

	public void setSchemaValue(String schemaValue) {
		this.schemaValue = schemaValue;
	}

}
