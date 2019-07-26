package gov.nih.tbi.dao;

import java.util.List;


public interface QueryAccountDao{
	
	/**
	 * Creates a new node representing the current user and link the node to all of the datasets the user has access to.
	 * 
	 * @param username
	 */
	public void addGraphAccount(String username, List<Long> datasetIds);
	
	/**
	 * Removes the node associated to the current user and all the links from said node
	 * 
	 * @param username
	 */
	public void removeGraphAccount(String username);
	
}
