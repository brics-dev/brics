package gov.nih.tbi.commons.service.hibernate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import gov.nih.tbi.commons.dao.PersistanceSessionUtils;
import gov.nih.tbi.commons.service.HibernateManager;

@Service
@Scope("singleton")
public class HibernateManagerImpl implements HibernateManager {
	@Autowired
	private PersistanceSessionUtils persistanceSessionUtils;
	
	public void clearMetaHibernateCache() {
		persistanceSessionUtils.clearMetaHibernateCache();
	}
}
