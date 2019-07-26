package gov.nih.tbi.dictionary.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum FormStructureStandardization {
	
	APPENDIX("Appendix", "Appendix"), MODIFIED("Modified", "Standard Modified"), UNIQUE("Unique", "Unique"),
	NINDSSTANDARD("Standard NINDS", "Standard NINDS CDE"), NINDSNOTSTANDARD("Standard not NINDS", "Standard"),
	OTHERNINDS("Other NINDS", "Other NINDS");

	private static final Map<String, FormStructureStandardization> nameLookup = new HashMap<String, FormStructureStandardization>();
	private static final Map<String, FormStructureStandardization> displayLookup = new HashMap<String, FormStructureStandardization>();

	static {
		for (FormStructureStandardization s : EnumSet.allOf(FormStructureStandardization.class)) {
			nameLookup.put(s.getName().toLowerCase(), s);
		}
	}
	
	static {
		for (FormStructureStandardization s : EnumSet.allOf(FormStructureStandardization.class)) {
			displayLookup.put(s.getDisplay().toLowerCase(), s);
		}
	}

	private String name;
	private String display;

	FormStructureStandardization(String name, String display) {

		this.name = name;
		this.display = display;
	}

	public String getName() {

		return name;
	}

	public String getDisplay() {

		return display;
	}

	public static FormStructureStandardization getByName(String name) {
		if (name != null) {
			name = name.toLowerCase();
		}
		return nameLookup.get(name);
	}
	
	public static FormStructureStandardization getByDisplay(String display) {
		if (display != null) {
			display = display.toLowerCase();
		}
		return displayLookup.get(display);
	}
	
	public static FormStructureStandardization[] getMainStandardizationTypes(){
		FormStructureStandardization[] out = {NINDSSTANDARD, NINDSNOTSTANDARD, MODIFIED, APPENDIX, UNIQUE};
        return out;
    }
	
	public static FormStructureStandardization[] getMainStandardizationTypesPublicSite(){
		FormStructureStandardization[] out = {NINDSSTANDARD, NINDSNOTSTANDARD};
        return out;
    }
	
}
