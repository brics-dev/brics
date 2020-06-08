package gov.nih.tbi.dictionary.dao.eform;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;

public interface BasicEformDao extends GenericDao<BasicEform, Long> {
	
	public List<BasicEform> list(Set<Long> eformIds);
	
	public List<BasicEform> searchEformWithFormStructureTitle(List<Long> eformIds);

	public List<BasicEform> basicEformSearch(Set<Long> eformIds, List<StatusType> eformStatus, String formStructureName, String createdBy, Boolean isShared);
	
	public boolean isEformShortNameUnique(String shortName);
	
	public List<BasicEform> getBasicEForms(Collection<String> shortNames);
}