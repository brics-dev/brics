
package gov.nih.tbi.dictionary.dao.hibernate;

import java.util.Collection;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.MapElementDao;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;

@Transactional("dictionaryTransactionManager")
@Repository
public class MapElementDaoImpl extends GenericDictDaoImpl<MapElement, Long> implements MapElementDao {
	private final String QUERY_UPDATE_FS_DE =
			"update map_element set data_element_id = %d where id in (select me.id from map_element me join "
					+ "data_element de on me.data_element_id = de.id where de.element_name = '%s');";

	@Autowired
	public MapElementDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(MapElement.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	public void deleteAll(Collection<MapElement> deletionList) {

		this.deleteAll(deletionList);
	}

	/*
	 * This method will change every form structure to point to the latest data element in the system
	 */
	public void updateFormStructuresWithLatestDataElement(String elementName, Long newDataElementID) {
		/*
		 * Used a query here because Hibernate does not have great support for updates This will be a much quicker way
		 * to update the DE id instead of pulling a list updating the status and saving the same list.
		 */
		String sqlUpdate = String.format(QUERY_UPDATE_FS_DE, newDataElementID, elementName);
		getSession().createNativeQuery(sqlUpdate).executeUpdate();
	}
	
}
