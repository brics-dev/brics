package gov.nih.tbi.service.cache;

import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.service.model.MetaDataCache;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.pojo.ResultType;

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
public class QueryMetaDataCache implements Serializable, MetaDataCache {
	private static final long serialVersionUID = -3002424891599263336L;
	private static final Logger log = LogManager.getLogger(QueryMetaDataCache.class);

	// Key:
	private final Map<String, Map<String, Integer>> rgDePositionMap =
			new ConcurrentHashMap<String, Map<String, Integer>>();
	private final Map<String, FormResult> formResultCacheMap = new ConcurrentHashMap<String, FormResult>();
	private final Map<String, StudyResult> studyResultCacheMap = new ConcurrentHashMap<String, StudyResult>();

	public QueryMetaDataCache() {}

	/**
	 * Returns the position based on rg or de. Will return null if the rg de combination has never been set.
	 * 
	 * @param rg
	 * @param de
	 * @return
	 */
	@Override
	public synchronized Integer getRgDePosition(RepeatableGroup rg, DataElement de) {

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
	@Override
	public synchronized void setRgDePosition(RepeatableGroup rg, DataElement de, Integer position) {

		Map<String, Integer> dePositionMap = rgDePositionMap.get(rg.getUri());

		if (dePositionMap == null) {
			dePositionMap = new HashMap<String, Integer>();
		}

		dePositionMap.put(de.getUri(), position);

		rgDePositionMap.put(rg.getUri(), dePositionMap);
	}

	@Override
	public void putFormResult(String uri, FormResult formResult) {
		formResultCacheMap.put(uri, formResult);
	}

	@Override
	public void putStudyResult(String uri, StudyResult studyResult) {
		studyResultCacheMap.put(uri, studyResult);
	}

	@Override
	public StudyResult getStudyResult(String uri) {

		return studyResultCacheMap.get(uri);
	}

	@Override
	public FormResult getFormResult(String uri) {

		return formResultCacheMap.get(uri);
	}

	@Override
	public synchronized void clearCache() {

		rgDePositionMap.clear();
		formResultCacheMap.clear();
		studyResultCacheMap.clear();
		log.info("The cache has been cleared");
	}

	@Override
	public Map<String, FormResult> getFormResultCacheMap() {

		return formResultCacheMap;
	}

	@Override
	public Map<String, StudyResult> getStudyResultCacheMap() {

		return studyResultCacheMap;
	}

	@Override
	public synchronized boolean isResultCacheEmpty(ResultType type) {
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
