
package gov.nih.tbi.commons.dao;

import java.util.List;
import java.util.Set;

import gov.nih.tbi.commons.model.hibernate.User;

public interface UserDao extends GenericDao<User, Long> {

	/**
	 * Get a user record based on the email address
	 * 
	 * @param email
	 * @return
	 */
	public User getByEmail(String email);

	/**
	 * Gets all of the emails
	 * 
	 * @return
	 */
	public List<String> getAllEmails();

	/**
	 * Returns a user with the given first and last name. We are only returning one User object because first and last
	 * name should be unique within the system.
	 * 
	 * @param firstName
	 * @param lastName
	 * @return
	 */
	public User getByName(String firstName, String lastName);
	
	public List<User> getUserDetailsByIds(Set<Long> userIds);
}
