package gov.nih.brics.cas.model;

/**
 * Convenience class for storing basic account/user information.
 * This allows us to make database calls a little cleaner. 
 * 
 * @author Joshua Park(jospark)
 *
 */
public class LimitedUser {
	
	private String username;
	private String first_name;
	private String last_name;
	private String email;
	private Long id;
	
	public String getFullName() {
		return last_name + ", " + first_name;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirst_name() {
		return first_name;
	}
	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}
	public String getLast_name() {
		return last_name;
	}
	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String toString() {
		return "Username: " + username +
				", First Name: " + first_name + 
				", Last Name: " + last_name +
				", Email: " + email +
				", Account Id: " + id;
	}
}
