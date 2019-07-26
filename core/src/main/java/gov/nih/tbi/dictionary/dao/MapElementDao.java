
package gov.nih.tbi.dictionary.dao;

import java.util.Collection;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;

public interface MapElementDao extends GenericDao<MapElement, Long> {

	/**
	 * This method is used to delete everything in a data structure.
	 * 
	 * This method is deprecated because hibernate mappings should handle this issue
	 * 
	 * @param deletionList
	 */
	@Deprecated
	public void deleteAll(Collection<MapElement> deletionList);

	/*
	 * This method will change every form structure to point to the latest data element in the system
	 */
	public void updateFormStructuresWithLatestDataElement(String elementName, Long newDataElementID);
}
