package gov.nih.tbi.query.dao;

import java.util.List;

import com.google.common.collect.Multimap;

import gov.nih.tbi.commons.dao.GenericSparqlDao;
import gov.nih.tbi.repository.model.PublicSubmittedForm;
import gov.nih.tbi.repository.model.StudySubmittedForm;


public interface StudySubmittedFormsSparqlDao extends GenericSparqlDao<List<StudySubmittedForm>> {

	public Multimap<Long, StudySubmittedForm> getAllStudySubmittedForms();
	
	public List<PublicSubmittedForm> getAllPublicSubmittedForms();
	
	public List<String> getStudyTitleListByFSShortName(String fsShortName);
	
	public Integer getRowCountByStudy(String studyId);
	
	public Integer getSubjectCountByStudy(String studyId);
	
	public Integer getFormCountByStudy(String studyId);
	
	public String getLastSubmitDateByStudy(String studyId);
}
