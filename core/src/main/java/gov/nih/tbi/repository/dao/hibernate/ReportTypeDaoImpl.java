package gov.nih.tbi.repository.dao.hibernate;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.repository.dao.ReportTypeDao;
import gov.nih.tbi.repository.model.hibernate.ReportType;

@Repository
public class ReportTypeDaoImpl extends GenericDaoImpl<ReportType, Long> implements ReportTypeDao {

	@Autowired
	public ReportTypeDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(ReportType.class, sessionFactory);
	}

}
