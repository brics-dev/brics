package gov.nih.tbi.commons.service;



public interface BioRepositoryManager {
	
	public void checkIUStatusAndRetrieveManifest();

	/*
	 * retrieve Indiana U. biosample catalogs from 2 repositories of NINDS and biofind, and transfer the data files to
	 * file server
	 */
	public void retrieveIUBiosampleCatalog();
}
