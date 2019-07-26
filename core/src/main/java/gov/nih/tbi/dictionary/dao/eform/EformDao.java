package gov.nih.tbi.dictionary.dao.eform;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Repository;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.CalculationRule;
import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;

@Repository
public interface EformDao extends GenericDao<Eform, Long> { 

	public Eform getEformNoLazyLoad(long eformId);
	
	public Eform getEformNoLazyLoad(String eformShortName);

	/**
	 * Gets the a list of full Eform objects from the database based on the given collection of eForm short names. The
	 * returned Eform objects will contain data that would otherwise have been lazy loaded. So the execution time of
	 * this method could get quite expensive. So please use with great care.
	 * 
	 * @param eFormShortNames - A collection of eForm short names used to query the list of Eform objects.
	 * @return A list of full Eform objects that corresponds to the collection of given eForm short names.
	 * @throws HibernateException When there is an error getting the Eform objects from the database.
	 */
	public List<Eform> getEformNoLazyLoad(Collection<String> eFormShortNames);

	public void deleteEform(long eFormId);
	
	public void saveOrUpdate(Eform eform);
	
	public List<CalculationRule> getAllCalculationRules();
	
}
