package gov.nih.tbi.commons.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the author database table.
 * 
 */
@Entity
@Table(name="Author")
public class Author implements Serializable {

	private static final long serialVersionUID = -8558099753457165701L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AUTHOR_SEQ")
	@SequenceGenerator(name = "AUTHOR_SEQ", sequenceName = "AUTHOR_SEQ", allocationSize = 1)
	private Long id;

	@Column(name="first_name")
	private String firstName;

	@Column(name="last_name")
	private String lastName;

	@Column(name="mi")
	private String mi;

	@Column(name="email")
	private String email;

	@Column(name="org_name")
	private String orgName;

	public Author() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMi() {
		return this.mi;
	}

	public void setMi(String mi) {
		this.mi = mi;
	}

	public String getOrgName() {
		return this.orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result + ((mi == null) ? 0 : mi.hashCode());
		result = prime * result + ((orgName == null) ? 0 : orgName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Author other = (Author) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		if (mi == null) {
			if (other.mi != null)
				return false;
		} else if (!mi.equals(other.mi))
			return false;
		if (orgName == null) {
			if (other.orgName != null)
				return false;
		} else if (!orgName.equals(other.orgName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Author [id=" + id + ", email=" + email + ", firstName=" + firstName + ", lastName=" + lastName + ", mi="
				+ mi + ", orgName=" + orgName + "]";
	}

}