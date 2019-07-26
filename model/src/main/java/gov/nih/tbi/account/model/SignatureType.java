package gov.nih.tbi.account.model;

public enum SignatureType {

	BRICS_ELECTRONIC_SIGNATURE(0L, "BRICS Electronic Signature"),
	PDBP_DMR_DUC(1L, "PDBP DMR DUC");
	
    private Long id;
    private String type;
    
	SignatureType(Long id, String type) {
		this.id = id;
		this.type = type;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
