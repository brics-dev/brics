package gov.nih.tbi.service;

import gov.nih.tbi.pojo.FacetItem;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.service.model.PermissionModel;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;

public interface SearchManager {

	public List<StudyResult> getStudiesFromCache(PermissionModel permissionModel);

	public List<FormResult> getFormsFromCache();

	public List<FacetItem> getDeFacetItems();

	public List<StudyResult> searchStudies(String textValue, PermissionModel permissionModel);

	public List<FormResult> searchForms(String textValue);

	public List<FormResult> searchDeForms(String textValue, List<String> deUris);

	/**
	 * Returns a multimap of data element names to a list of form structures it is attached to, but only for the data
	 * elements mentioned in the given list of data element variable names.
	 * 
	 * @param deNames
	 * @return
	 */
	public Multimap<String, FormResult> searchFormsByDeNames(List<String> deNames);

	/**
	 * Returns a multimap of data element names to its seeAlso values for only the data elements mentioned in the given
	 * list data element variable names.
	 * 
	 * @param deNames
	 * @return
	 */
	public Multimap<String, String> getSeeAlso(List<String> deNames);

	/**
	 * Return a map of data element name to its title
	 * 
	 * @param deNames
	 * @return
	 */
	public Map<String, String> getDeTitles(List<String> deNames);
}
