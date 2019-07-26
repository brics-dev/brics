package gov.nih.tbi.repository.service;

import java.io.Serializable;
import java.sql.SQLException;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;

/**
 * This class handles the building of tables in the repos schema for storing instanced data.
 * 
 * @author Francis Chen
 *
 */
public interface RepositoryTableBuilder extends Serializable {
	
	/**
	 * Using the given form structure, create the repository tables and sequences required to store instanced data.
	 * 
	 * @param datastructure - The form structure in question.
	 * @param errorToEmail - In the case of an error occurring, this is the email address we will be sending the error text to.
	 * @param account - Account of the user in session invoking this method.
	 * @return - true if successful, false otherwise.
	 * @throws UserPermissionException 
	 * @throws SQLException 
	 */
	public boolean createRepositoryStore(StructuralFormStructure datastructure, String errorToEmail, Account account) throws SQLException, UserPermissionException;
}
