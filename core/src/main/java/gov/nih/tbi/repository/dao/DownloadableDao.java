package gov.nih.tbi.repository.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;
import gov.nih.tbi.repository.model.hibernate.DatasetDownloadFile;
import gov.nih.tbi.repository.model.hibernate.Downloadable;

public interface DownloadableDao extends GenericDao<Downloadable, Long> {

	public void deleteByIds(List<Long> ids);
}
