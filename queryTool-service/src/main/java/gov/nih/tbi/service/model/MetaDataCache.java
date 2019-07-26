package gov.nih.tbi.service.model;

import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.pojo.ResultType;
import gov.nih.tbi.pojo.StudyResult;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Application scoped class that will hopefully end up cache all of our static meta data we no longer will have to
 * redundantly query every time for them for each session
 * 
 * @author Francis Chen
 */
@Component
@Scope("application")
public class MetaDataCache implements Serializable {
	private static final long serialVersionUID = -3002424891599263336L;
	private static final Logger log = LogManager.getLogger(MetaDataCache.class);

	// Key:
	private static final Map<String, Map<String, Integer>> rgDePositionMap =
			new ConcurrentHashMap<String, Map<String, Integer>>();
	private static final Map<String, FormResult> formResultCacheMap = new ConcurrentHashMap<String, FormResult>();
	private static final Map<String, StudyResult> studyResultCacheMap = new ConcurrentHashMap<String, StudyResult>();

	private MetaDataCache() {}

	/**
	 * Returns the position based on rg or de. Will return null if the rg de combination has never been set.
	 * 
	 * @param rg
	 * @param de
	 * @return
	 */
	public static synchronized Integer getRgDePosition(RepeatableGroup rg, DataElement de) {

		if (de == null) {
			return null;
		}

		Map<String, Integer> dePositionMap = rgDePositionMap.get(rg.getUri());

		if (dePositionMap == null) {
			return null;
		}

		return dePositionMap.get(de.getUri());
	}

	/**
	 * Sets the position for the rg de combination.
	 * 
	 * @param rg
	 * @param de
	 * @param position
	 */
	public static synchronized void setRgDePosition(RepeatableGroup rg, DataElement de, Integer position) {

		Map<String, Integer> dePositionMap = rgDePositionMap.get(rg.getUri());

		if (dePositionMap == null) {
			dePositionMap = new HashMap<String, Integer>();
		}

		dePositionMap.put(de.getUri(), position);

		rgDePositionMap.put(rg.getUri(), dePositionMap);
	}

	public static void putFormResult(String uri, FormResult formResult) {
		formResultCacheMap.put(uri, formResult);
	}

	public static void putStudyResult(String uri, StudyResult studyResult) {
		studyResultCacheMap.put(uri, studyResult);
	}

	public static StudyResult getStudyResult(String uri) {

		return studyResultCacheMap.get(uri);
	}

	public static FormResult getFormResult(String uri) {

		return formResultCacheMap.get(uri);
	}

	public static synchronized void clearCache() {

		rgDePositionMap.clear();
		formResultCacheMap.clear();
		studyResultCacheMap.clear();
		log.info("The cache has been cleared");
	}

	public static Map<String, FormResult> getFormResultCacheMap() {

		return formResultCacheMap;
	}

	public static Map<String, StudyResult> getStudyResultCacheMap() {

		return studyResultCacheMap;
	}

	public static synchronized boolean isResultCacheEmpty(ResultType type) {
		switch (type) {
			case FORM_STRUCTURE:
				return formResultCacheMap.isEmpty();
			case STUDY:
				return studyResultCacheMap.isEmpty();
			default:
				return true;
		}
	}
}
