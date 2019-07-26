
package gov.nih.tbi.repository.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.hibernate.SupportingDocumentation;

public interface SupportingDocumentationDao extends GenericDao<SupportingDocumentation, Long> {
	
	public List<SupportingDocumentation> getStudyPublicationDocumentation(Long studyId);
	
	public List<SupportingDocumentation> getMetastudyPublicationDocumentation(Long metaStudyId);
	
	public SupportingDocumentation getPublicationDocumentation(Long supportingDocId);
	
	public SupportingDocumentation getPublicationDocumentation(Long studyId, Long publicationId);
}
