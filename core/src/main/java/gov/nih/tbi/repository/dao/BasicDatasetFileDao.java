package gov.nih.tbi.repository.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.hibernate.BasicDatasetFile;

public interface BasicDatasetFileDao extends GenericDao<BasicDatasetFile, Long> {
	
	public List<BasicDatasetFile> getUserFileByDatasetId(Long datasetId);
}
