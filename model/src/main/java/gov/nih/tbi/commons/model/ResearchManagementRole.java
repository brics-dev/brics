package gov.nih.tbi.commons.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

@XmlRootElement(name = "researchManagementRole")
@XmlAccessorType(XmlAccessType.FIELD)
public enum ResearchManagementRole {

	@SerializedName("0")
	PRIMARY_PRINCIPAL_INVESTIGATOR(0L, "Primary Principal Investigator"), 
	
	@SerializedName("1")
	PRINCIPAL_INVESTIGATOR(1L, "Principal Investigator"), 
	
	@SerializedName("2")
	ASSOCIATE_PRINCIPAL_INVESTIGATOR(2L, "Associate Principal Investigator"), 
	
	@SerializedName("3")
	DATA_MANAGER(3L, "Data Manager"),
	
	@SerializedName("4")
	RESEARCHER(4L, "Researcher"), 
	
	@SerializedName("5")
	OTHER(5L, "Other"); 
	
    private long id;
    private String name;

    ResearchManagementRole(long id, String name) {
        this.id = id;
        this.name = name;
    }

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static ResearchManagementRole roleOf(long roleId) {
		for (ResearchManagementRole role : ResearchManagementRole.values()) {
			if (role.getId() == roleId) {
				return role;
			}
		}
		return null;
	}
}
