package gov.nih.tbi.service.model;

import java.util.Map;

import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.pojo.ResultType;
import gov.nih.tbi.pojo.StudyResult;


public interface MetaDataCache {

	/**
	 * Returns the position based on rg or de. Will return null if the rg de combination has never been set.
	 * 
	 * @param rg
	 * @param de
	 * @return
	 */
	Integer getRgDePosition(RepeatableGroup rg, DataElement de);

	/**
	 * Sets the position for the rg de combination.
	 * 
	 * @param rg
	 * @param de
	 * @param position
	 */
	void setRgDePosition(RepeatableGroup rg, DataElement de, Integer position);

	void putFormResult(String uri, FormResult formResult);

	void putStudyResult(String uri, StudyResult studyResult);

	StudyResult getStudyResult(String uri);

	FormResult getFormResult(String uri);

	void clearCache();

	Map<String, FormResult> getFormResultCacheMap();

	Map<String, StudyResult> getStudyResultCacheMap();

	boolean isResultCacheEmpty(ResultType type);

}
