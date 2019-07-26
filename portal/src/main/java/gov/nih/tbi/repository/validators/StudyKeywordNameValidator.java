package gov.nih.tbi.repository.validators;

import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.repository.model.hibernate.StudyKeyword;
import gov.nih.tbi.repository.portal.StudyAction;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class StudyKeywordNameValidator extends FieldValidatorSupport {

	@Autowired
	RepositoryManager repoManager;

	/**
	 * Method called by struts2 validation process
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void validate(Object object) throws ValidationException {

		String fieldName = this.getFieldName();
		String keyword = (String) this.getFieldValue(this.getFieldName(), object);

		// If the name is not valid or it already exists then throw an error
		if (keyword == null || validateKeywordName(keyword)) {
			addFieldError(fieldName, "Invalid Keyword Name");
			return;
		}

		StudyAction action = (StudyAction) object;
		Set<StudyKeyword> sessionKeywords = action.getSessionStudy().getNewKeywords();

		// Check against the fields that have already been added this session
		if (sessionKeywords != null) {
			for (StudyKeyword sessionKeyword : sessionKeywords) {
				if (sessionKeyword.getKeyword().equals(keyword)) {
					addFieldError(fieldName, "Invalid Keyword Name");
				}
			}
		}
	}

	public Boolean validateKeywordName(String keywordName) {

		List<StudyKeyword> keyword = repoManager.retrieveAllStudyKeywords();

		for (StudyKeyword key : keyword) {
			if (key.getKeyword().equals(keywordName)) {
				return true;
			}
		}
		return false;
	}
}
