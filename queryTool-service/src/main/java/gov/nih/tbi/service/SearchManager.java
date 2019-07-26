package gov.nih.tbi.service;

import gov.nih.tbi.pojo.FacetItem;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.service.model.PermissionModel;

import java.util.List;

public interface SearchManager {

	public List<StudyResult> getStudiesFromCache(PermissionModel permissionModel);
	
	public List<FormResult> getFormsFromCache();
	
	public List<FacetItem> getDeFacetItems();
	
	public List<StudyResult> searchStudies(String textValue, PermissionModel permissionModel);
	
	public List<FormResult> searchForms(String textValue);
	
	public List<FormResult> searchDeForms(String textValue, List<String> deUris);
}
