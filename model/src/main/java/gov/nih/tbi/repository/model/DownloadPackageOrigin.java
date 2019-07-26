package gov.nih.tbi.repository.model;

public enum DownloadPackageOrigin {
	QUERY_TOOL("Query Tool"), DATASET("Dataset"), ACCOUNT("User File");

	private String name;

	DownloadPackageOrigin(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
