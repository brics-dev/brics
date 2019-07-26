package gov.nih.tbi.commons.model;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Enum that represents a BRICS instance. The name of each enum should correspond to the "modules.org.name" property in
 * the modules.properties file. So that calling the {@link #valueOf(String)} method with what is returned by
 * {@link gov.nih.tbi.ModulesConstants#getModulesOrgName()} would return the correct enum.
 * 
 * @author jeng
 *
 */
@XmlType
@XmlEnum(value = String.class)
public enum BricsInstanceType {
	@XmlEnumValue(value = "PDBP")
	PDBP("PDBP"),

	@XmlEnumValue(value = "FITBIR")
	FITBIR("FITBIR"),

	@XmlEnumValue(value = "NEI_BRICS")
	NEI_BRICS("NEI BRICS"),

	@XmlEnumValue(value = "CISTAR")
	CISTAR("CISTAR"),

	@XmlEnumValue(value = "CNRM")
	CNRM("CNRM"),

	@XmlEnumValue(value = "CDRNS")
	NINR("CDRNS"),
	
	@XmlEnumValue(value = "NINDS")
	NINDS("NINDS"),
	
	@XmlEnumValue(value = "NIA")
	NIA("NIA"),
	
	@XmlEnumValue(value = "NTRR")
	NTRR("NTRR"),
	
	@XmlEnumValue(value = "GRDR")
	GRDR("GRDR");

	private static final Map<String, BricsInstanceType> lookup = new HashMap<String, BricsInstanceType>();

	static {
		for (BricsInstanceType type : BricsInstanceType.values()) {
			lookup.put(type.getName(), type);
		}
	}

	private String name;

	BricsInstanceType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * Retrieves a BricsInstanceType enum by its instance name (i.e. "FITBIR").
	 * 
	 * @param targetName - The instance name to search for.
	 * @return The BricsInstanceType enum that matches the target name, or null if no match exists.
	 */
	public static synchronized BricsInstanceType getByInstanceName(String targetName) {
		return lookup.get(targetName);
	}
}
