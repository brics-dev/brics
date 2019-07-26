package gov.nih.tbi.commons.dao;

import org.apache.log4j.Logger;
import org.hibernate.Cache;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import gov.nih.tbi.CoreConstants;

@Service
public class PersistanceSessionUtils {
	private static Logger logger = Logger.getLogger(PersistanceSessionUtils.class);

	private SessionFactory sessionFactory;

	public PersistanceSessionUtils(@Qualifier(CoreConstants.COMMONS_FACTORY) SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}


	public void clearMetaHibernateCache() {
		Cache cache = sessionFactory.getCache();

		if (cache != null) {
			logger.info("Cleared hibernate cache");
			cache.evictAllRegions(); // Evict data from all query regions.
		}
	}
}
