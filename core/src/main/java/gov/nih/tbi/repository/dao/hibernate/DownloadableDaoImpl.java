package gov.nih.tbi.repository.dao.hibernate;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.repository.dao.DownloadPackageDao;
import gov.nih.tbi.repository.dao.DownloadableDao;
import gov.nih.tbi.repository.model.DownloadableOrigin;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;
import gov.nih.tbi.repository.model.hibernate.Downloadable;

@Transactional("metaTransactionManager")
@Repository
public class DownloadableDaoImpl extends GenericDaoImpl<Downloadable, Long> implements DownloadableDao {

	Logger logger = Logger.getLogger(this.getClass());

	@Autowired
	DownloadPackageDao downloadPackageDao;

	@Autowired
	public DownloadableDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {
		super(Downloadable.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void deleteByIds(List<Long> ids) {
		if (ids == null | ids.isEmpty()) {
			return;
		}

		List<Downloadable> downloadablesToDelete = getByIds(ids);
		DownloadPackage downloadPackage = null;
		if (downloadablesToDelete != null && !downloadablesToDelete.isEmpty()) {
			for (ListIterator<Downloadable> it = downloadablesToDelete.listIterator(); it.hasNext();) {
				Downloadable downloadable = it.next();
				downloadPackage = downloadable.getDownloadPackage();

				if (downloadPackage != null) {
					Set<Downloadable> newDownloadables = new HashSet<Downloadable>();
					for(Downloadable da : downloadPackage.getDownloadables()){
						if(!da.equals(downloadable)){
							newDownloadables.add(da);
						}						
					}
					/*An alternative way to remove. Set.remove() was not working*/
					downloadPackage.getDownloadables().clear();
					downloadPackage.getDownloadables().addAll(newDownloadables);
					downloadPackageDao.save(downloadPackage);
				}				
			}
		}
	}

	public List<Downloadable> getByIds(List<Long> ids) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Downloadable> query = cb.createQuery(Downloadable.class);

		Root<Downloadable> root = query.from(persistentClass);
		query.where(root.get("id").in(ids)).distinct(true);

		return createQuery(query).getResultList();
	}

	public List<Downloadable> getByTypeAndUser(DownloadableOrigin type, User user) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Downloadable> query = cb.createQuery(Downloadable.class);

		Root<Downloadable> root = query.from(persistentClass);
		query.where(cb.and(cb.equal(root.get("user"), user), cb.equal(root.get("type"), type))).distinct(true);
		
		return createQuery(query).getResultList();
	}

}
