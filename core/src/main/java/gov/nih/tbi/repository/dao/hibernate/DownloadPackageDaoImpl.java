package gov.nih.tbi.repository.dao.hibernate;


import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.repository.dao.DownloadPackageDao;
import gov.nih.tbi.repository.model.DownloadPackageOrigin;
import gov.nih.tbi.repository.model.hibernate.DatasetDownloadFile;
import gov.nih.tbi.repository.model.hibernate.DownloadFileDataset;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;
import gov.nih.tbi.repository.model.hibernate.Downloadable;

@Transactional("metaTransactionManager")
@Repository
public class DownloadPackageDaoImpl extends GenericDaoImpl<DownloadPackage, Long> implements DownloadPackageDao {

	@Autowired
	public DownloadPackageDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {
		super(DownloadPackage.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	public List<DownloadPackage> getByUser(User user) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<DownloadPackage> query = cb.createQuery(DownloadPackage.class);

		Root<DownloadPackage> root = query.from(DownloadPackage.class);
		query.where(cb.equal(root.get("user"), user)).distinct(true);

		return createQuery(query).getResultList();
	}

	/**
	 * @inheritDoc
	 */
	public List<DownloadPackage> getByUserId(Long userId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<DownloadPackage> query = cb.createQuery(DownloadPackage.class);

		Root<DownloadPackage> root = query.from(DownloadPackage.class);
		query.where(cb.equal(root.join("user").get("id"), userId)).distinct(true);

		return createQuery(query).getResultList();
	}


	public List<DownloadPackage> getByIds(List<Long> ids) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<DownloadPackage> query = cb.createQuery(DownloadPackage.class);

		Root<DownloadPackage> root = query.from(DownloadPackage.class);
		query.where(root.get("id").in(ids)).distinct(true);

		return createQuery(query).getResultList();
	}

	public void deleteByIds(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return;
		}

		this.removeAll(getByIds(ids));
	}

	private List<DownloadPackage> getEmptyPackages() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<DownloadPackage> query = cb.createQuery(DownloadPackage.class);

		Root<DownloadPackage> root = query.from(DownloadPackage.class);
		query.where(cb.isEmpty(root.get("downloadables"))).distinct(true);

		return createQuery(query).getResultList();
	}

	/**
	 * @inheritDoc
	 */
	public void removeEmptyPackages() {
		List<DownloadPackage> emptyPackages = getEmptyPackages();
		this.removeAll(emptyPackages);
	}

	/**
	 * @inheritDoc
	 */
	public DownloadPackage getDownloadPackageByDatasetAndUser(DownloadFileDataset dataset, User user) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<DownloadPackage> query = cb.createQuery(DownloadPackage.class);

		Root<DownloadPackage> root = query.from(DownloadPackage.class);
		query.where(
				cb.and(cb.equal(root.get("user"), user), cb.equal(root.get("origin"), DownloadPackageOrigin.DATASET)))
				.distinct(true);

		List<DownloadPackage> downloadPackages = createQuery(query).getResultList();

		for (DownloadPackage downloadPackage : downloadPackages) {
			for (Downloadable downloadable : downloadPackage.getDownloadables()) {
				if (downloadable instanceof DatasetDownloadFile) {
					DatasetDownloadFile dsDownloadFile = (DatasetDownloadFile) downloadable;
					if (dsDownloadFile.getDataset() != null
							&& dataset.getName().equals(dsDownloadFile.getDataset().getName())) {
						return downloadPackage;
					}
				}
			}
		}

		return null;
	}
	
	public int deleteDatasetsOlderThan(int daysOlderThan){
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaDelete<DownloadPackage> delete = cb.createCriteriaDelete(DownloadPackage.class);
		//CriteriaQuery<DownloadPackage> query  = cb.createQuery(DownloadPackage.class);
		Calendar cal = Calendar.getInstance();
		
		cal.add(Calendar.DATE, daysOlderThan * -1);
		
		Date numDaysAgo = cal.getTime();
		
		Root<DownloadPackage> root = delete.from(DownloadPackage.class);

		delete.where(cb.lessThan(root.get("dateAdded"), numDaysAgo));
		
		int rows = getSession().createQuery(delete).executeUpdate();
		
		
		return rows;
		
	}

}
