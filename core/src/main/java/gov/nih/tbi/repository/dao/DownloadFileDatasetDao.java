package gov.nih.tbi.repository.dao;

import java.util.List;
import java.util.Set;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.hibernate.DownloadFileDataset;

public interface DownloadFileDatasetDao extends GenericDao<DownloadFileDataset, Long> {
	public List<DownloadFileDataset> getByIds(Set<Long> ids);
}
