package gov.nih.tbi.commons.model.hibernate;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.util.BRICSStringUtils;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name = "TBI_USER")
public class User implements Serializable {

	private static final long serialVersionUID = -2525276531296178501L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TBI_USER_SEQ")
	@SequenceGenerator(name = "TBI_USER_SEQ", sequenceName = "TBI_USER_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "FIRST_NAME")
	private String firstName;
	
	@Column(name = "MIDDLE_NAME")
	private String middleName;	

	@Column(name = "LAST_NAME")
	private String lastName;

	@Column(name = "EMAIL")
	private String email;

	/*******************************************************************/

	public Long getId() {

		return id;
	}

	public void setId(Long id) {

		this.id = id;
	}

	public String getFirstName() {

		return firstName;
	}

	public void setFirstName(String firstName) {
		if(!StringUtils.isBlank(firstName)) {
			firstName = firstName.trim();
		}
		
		this.firstName = firstName;
	}

	public String getMiddleName() {

		return middleName;
	}

	public void setMiddleName(String middleName) {
		if(!StringUtils.isBlank(middleName)) {
			middleName = middleName.trim();
		}
		
		this.middleName = middleName;
	}

	public String getLastName() {
		
		return lastName;
	}

	public void setLastName(String lastName) {
		if(!StringUtils.isBlank(lastName)) {
			lastName = lastName.trim();
		}
		
		this.lastName = lastName;
	}

	public String getEmail() {

		return email;
	}

	public void setEmail(String email) {

		this.email = email;
	}

	/*******************************************************************/

	/**
	 * Returns the full name of the user in the format: First Last
	 * 
	 * @return String
	 */
	public String getFullName() {
		// it's possible these to be null when fullname is called. Probably due to some lazy loading mechanism
		if (firstName == null || lastName == null) {
			return ModelConstants.EMPTY_STRING;
		}

		// capitalize the first characters in the first and last names
		String first = BRICSStringUtils.capitalizeFirstCharacter(this.getFirstName().trim());
		String last = BRICSStringUtils.capitalizeFirstCharacter(this.getLastName().trim());

		if (StringUtils.isBlank(middleName)) {
			return last + ", " + first;
		} else {
			String middle = BRICSStringUtils.capitalizeFirstCharacter(this.getMiddleName().trim());
			return last + ", " + first + " " + middle;
		}
	}

	@Override
	public String toString() {
		return "User [firstName=" + getFirstName() + ", id=" + id + ", lastName=" + getLastName() + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		if (obj instanceof User) {
			User other = (User) obj;

			// Getter methods are needed here to ensure that Hibernate/JPA retrieves the proper values.
			return getFirstName().equals(other.getFirstName()) && getLastName().equals(other.getLastName())
					&& getEmail().equals(other.getEmail());
		}

		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		// The usage of getter methods below will ensure that Hibernate/JPA will fetch the proper values.
		result = prime * result + ((getFirstName() == null) ? 0 : getFirstName().hashCode());
		result = prime * result + ((getLastName() == null) ? 0 : getLastName().hashCode());
		result = prime * result + ((getEmail() == null) ? 0 : getEmail().hashCode());

		return result;
	}
}
