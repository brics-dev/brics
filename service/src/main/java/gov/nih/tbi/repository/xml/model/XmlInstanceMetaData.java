package gov.nih.tbi.repository.xml.model;

import java.util.HashSet;

public class XmlInstanceMetaData {

	private String fsName;
	private HashSet<String> guidElements;
	private HashSet<String> dateElements;

	/**
	 * @return the fsName
	 */
	public String getFsName() {
		return fsName;
	}

	/**
	 * @param fsName the fsName to set
	 */
	public void setFsName(String fsName) {
		this.fsName = fsName;
	}

	/**
	 * @return the guidElements
	 */
	public HashSet<String> getGuidElements() {
		return guidElements;
	}

	/**
	 * @param guidElements the guidElements to set
	 */
	public void setGuidElements(HashSet<String> guidElements) {
		this.guidElements = guidElements;
	}

	/**
	 * @return the dateElements
	 */
	public HashSet<String> getDateElements() {
		return dateElements;
	}

	/**
	 * @param dateElements the dateElements to set
	 */
	public void setDateElements(HashSet<String> dateElements) {
		this.dateElements = dateElements;
	}

}
