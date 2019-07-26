
package gov.nih.tbi.repository.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.hibernate.DatasetDataStructure;

public interface DatasetDataStructureDao extends GenericDao<DatasetDataStructure, Long> {

	public Boolean isFormInAnyDataset(Long formId);


	/**
	 * Get all of the dsds objects that are part of the dataset with the given id.
	 * 
	 * @param id : of the desired dataset
	 * @return
	 */
	public List<DatasetDataStructure> getByDatasetId(Long id);

	/**
	 * Given a form structure ID, return all DatasetDataStructure objects with that ID
	 * 
	 * @param id
	 * @return
	 */
	public List<DatasetDataStructure> getByFormStructureId(Long id);
}
