package gov.nih.tbi.api.query.utils;

import gov.nih.tbi.api.query.model.Study;
import gov.nih.tbi.api.query.model.Study.StatusEnum;
import gov.nih.tbi.pojo.StudyResult;

/**
 * This class converts a StudyResult object used by the query tool into a study object that gets returned by this API.
 * Yes, this could have been a constructor of study, but since it is auto-generated, we don't want to have to rewrite
 * the constructor every time the Study object gets updated.
 * 
 * @author Francis Chen
 *
 */
public class StudyAdapter implements ObjectAdapter<Study> {

	StudyResult studyResult;

	public StudyAdapter(StudyResult studyResult) {
		this.studyResult = studyResult;
	}

	/**
	 * {@inheritDoc}
	 */
	public Study adapt() {
		if (studyResult == null) {
			return null;
		}

		Study study = new Study();
		study.setPi(studyResult.getPi());
		study.setTitle(studyResult.getTitle());
		study.setStatus(StatusEnum.fromValue(studyResult.getStatus()));
		study.setAbstract(studyResult.getAbstractText());
		study.setId(studyResult.getPrefixedId());
		return study;
	}
}
