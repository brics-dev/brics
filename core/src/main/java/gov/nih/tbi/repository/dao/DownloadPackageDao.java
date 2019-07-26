package gov.nih.tbi.repository.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.repository.model.hibernate.DownloadFileDataset;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;

public interface DownloadPackageDao extends GenericDao<DownloadPackage, Long> {
	/**
	 * Returns all of the download packages owned by the given user
	 * @param user
	 * @return
	 */
	public List<DownloadPackage> getByUser(User user);
	
	/**
	 * Returns all of the download packages owned by the given user ID
	 * @param userId
	 * @return
	 */
	public List<DownloadPackage> getByUserId(Long userId);

	/**
	 * Deletes all of the download packages by IDs
	 */
	public void deleteByIds(List<Long> ids);
	
	/**
	 * Delete all download packages that do not have any downloadables
	 */
	public void removeEmptyPackages();
	
	public DownloadPackage getDownloadPackageByDatasetAndUser(DownloadFileDataset dataset, User user);
	
	public int deleteDatasetsOlderThan(int daysOlderThan);
}
