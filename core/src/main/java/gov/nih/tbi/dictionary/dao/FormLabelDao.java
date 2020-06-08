package gov.nih.tbi.dictionary.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.FormLabel;

public interface FormLabelDao extends GenericDao<FormLabel, Long> {

	public List<FormLabel> getAllFormLabels();
	
	public boolean isFormLabelUnique(String formLabel);
}
