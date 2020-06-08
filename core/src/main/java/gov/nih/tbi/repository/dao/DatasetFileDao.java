package gov.nih.tbi.repository.dao;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.SubmissionType;
public interface DatasetFileDao extends GenericDao<DatasetFile, Long> {
	/**
	 * Returns the dataset file that has the exact same dataset as the parameter but will only try to match the fileName
	 * to the end of local_locations
	 * 
	 * @param datasetId
	 * @param fileName
	 * @return
	 */
	public DatasetFile getByDatasetIdAndFileName(Long datasetId, String fileName);

	/**
	 * Returns a list of datasets that have the exact same dataset as the parameter.
	 * 
	 * @param datasetId
	 * @return
	 */
	public List<DatasetFile> getByDatasetId(Long datasetId);
	
	public BigInteger getDatasetPendingFileCount(Long datasetId);

	public BigDecimal getTotalFilesSizeByDatasetId(Long datasetId);
}
