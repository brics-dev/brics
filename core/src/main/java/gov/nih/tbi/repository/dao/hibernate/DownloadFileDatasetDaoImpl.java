package gov.nih.tbi.repository.dao.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.repository.dao.DownloadFileDatasetDao;
import gov.nih.tbi.repository.model.hibernate.DownloadFileDataset;

@Transactional("metaTransactionManager")
@Repository
public class DownloadFileDatasetDaoImpl extends GenericDaoImpl<DownloadFileDataset, Long> implements DownloadFileDatasetDao {

	@Autowired
	public DownloadFileDatasetDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {
		super(DownloadFileDataset.class, sessionFactory);
	}

	public List<DownloadFileDataset> getByIds(Set<Long> ids) {
		
		if(ids == null || ids.isEmpty()) {
			return new ArrayList<DownloadFileDataset> ();
		}
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<DownloadFileDataset> query = cb.createQuery(DownloadFileDataset.class);

		Root<DownloadFileDataset> root = query.from(DownloadFileDataset.class);
		query.where(root.get("id").in(ids)).distinct(true);

		return createQuery(query).getResultList();
	}
}
