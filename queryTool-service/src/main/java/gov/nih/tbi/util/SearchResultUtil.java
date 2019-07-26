package gov.nih.tbi.util;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.exceptions.ResultSetTranslationException;
import gov.nih.tbi.pojo.BeanField;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.service.ResultManager;
import gov.nih.tbi.service.model.MetaDataCache;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class SearchResultUtil {

	private static final Logger log = LogManager.getLogger(SearchResultUtil.class);

	public static void cacheStudyResults(ResultManager resultManager) {
		
		try {
			log.info("Caching study Results.");
			List<StudyResult> studyResults = resultManager.runStudyQueryForCaching();
			insertFormDetails(resultManager, studyResults);
			
			for (StudyResult studyResult : studyResults) {
				MetaDataCache.putStudyResult(studyResult.getUri(), studyResult);
			}
		} catch (ResultSetTranslationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void cacheFormResults(ResultManager resultManager) {
		
		try {
			List<FormResult> formResults = resultManager.runFormQueryForCaching();
			insertStudyDetails(resultManager, formResults);
			
			for (FormResult formResult : formResults) {
				MetaDataCache.putFormResult(formResult.getUri(), formResult);
			}
		} catch (ResultSetTranslationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	// Inserts the details of the forms into each study
	private static void insertFormDetails(ResultManager resultManager, List<StudyResult> studies) {

		if (studies == null || studies.isEmpty()) {
			return;
		}

		try {
			List<FormResult> formResults = resultManager.getFormDetailsInfo(studies);

			for (StudyResult study : studies) {
				for (FormResult formInStudy : study.getForms()) {
					for (FormResult formResult : formResults) {
						if (formInStudy.getUri().equals(formResult.getUri())) {
							for (BeanField field : QueryToolConstants.FORM_FIELDS) {
								if (!"url".equals(field.getName()) && !"studies".equals(field.getName())) {
									Method setMethod = getSetMethod(FormResult.class, field.getName(), field.getType());
									Method getMethod = getGetMethod(FormResult.class, field.getName());
									setMethod.invoke(formInStudy, getMethod.invoke(formResult));
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	// Insert details of the studies for each form
	private static void insertStudyDetails(ResultManager resultManager, List<FormResult> forms) {

		if (forms == null || forms.isEmpty()) {
			return;
		}

		try {
			List<StudyResult> studyResults = resultManager.getStudyDetailsInfo(forms);

			for (FormResult form : forms) {
				List<StudyResult> studies = form.getStudies();
				if (studies != null) {
					for (StudyResult studyInForm : studies) {
						for (StudyResult studyResult : studyResults) {
							if (studyInForm.getUri().equals(studyResult.getUri())) {
								for (BeanField field : QueryToolConstants.STUDY_FIELDS) {
									if (!"url".equals(field.getName()) && !"forms".equals(field.getName())) {
										Method setMethod =
												getSetMethod(StudyResult.class, field.getName(), field.getType());
										Method getMethod = getGetMethod(StudyResult.class, field.getName());
										setMethod.invoke(studyInForm, getMethod.invoke(studyResult));
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private static Method getSetMethod(Class<?> type, String field, Class<?> methodType) throws SecurityException,
			NoSuchMethodException {

		String setMethodName = "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
		return type.getMethod(setMethodName, methodType);
	}

	private static Method getGetMethod(Class<?> type, String field) throws SecurityException, NoSuchMethodException {

		String getMethodName = "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
		return type.getMethod(getMethodName);
	}

 	/**
	 * Sorts the studies list in alphabetical order by title (ignoring case). Also sorts the list of forms in each study
	 * in alphabetical order by short name (ignoring case).
	 */
	public static void sortStudyResults(List<StudyResult> studyResults) {

		if (studyResults != null) {
			Collections.sort(studyResults);

			// We also need to sort the forms in each study, which will also be sorted alphabetically by short name
			// while ignoring case.
			for (StudyResult study : studyResults) {
				if (study != null) {
					List<FormResult> formResultList = study.getForms();

					if (formResultList != null) {
						Collections.sort(formResultList);
					}
				}
			}
		}
	}

	/**
	 * Sorts list of form results by title (ignoring case). The list of studies that are in each form will also be
	 * sorted by its title (ignoring case).
	 */
	public static void sortFormResults(List<FormResult> formResults) {

		// sorts the forms alphabetically by title
		if (formResults != null) {
			Collections.sort(formResults);

			// also sort the studies in each form by title
			for (FormResult fr : formResults) {
				List<StudyResult> studyResultList = fr.getStudies();

				if (studyResultList != null) {
					Collections.sort(studyResultList);
				}
			}
		}
	}
}
