package gov.nih.tbi.filter;

public enum FilterMode {
	EXACT("exact"), INCLUSIVE("inclusive");

	private String name;

	private FilterMode(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
