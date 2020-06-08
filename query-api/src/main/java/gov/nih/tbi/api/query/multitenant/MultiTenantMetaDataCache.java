package gov.nih.tbi.api.query.multitenant;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.pojo.ResultType;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.service.model.MetaDataCache;

public class MultiTenantMetaDataCache implements MetaDataCache, Serializable {
	final Logger logger = LoggerFactory.getLogger(MultiTenantMetaDataCache.class);
	private static final long serialVersionUID = -3400877129539832814L;

	private Map<String, MetaDataCache> targetMetaDataCache;

	public void putMetaDataCache(String tenantName, MetaDataCache cache) {
		if (targetMetaDataCache == null) {
			targetMetaDataCache = new HashMap<>();
		}

		targetMetaDataCache.put(tenantName, cache);
	}

	protected String currentTenant() {
		return TenantContext.getCurrentTenant();
	}

	protected MetaDataCache getMetaDataCache() {
		return targetMetaDataCache.get(currentTenant());
	}

	@Override
	public Integer getRgDePosition(RepeatableGroup rg, DataElement de) {
		return getMetaDataCache().getRgDePosition(rg, de);
	}

	@Override
	public void setRgDePosition(RepeatableGroup rg, DataElement de, Integer position) {
		getMetaDataCache().setRgDePosition(rg, de, position);
	}

	@Override
	public void putFormResult(String uri, FormResult formResult) {
		getMetaDataCache().putFormResult(uri, formResult);
	}

	@Override
	public void putStudyResult(String uri, StudyResult studyResult) {
		getMetaDataCache().putStudyResult(uri, studyResult);
	}

	@Override
	public StudyResult getStudyResult(String uri) {
		return getMetaDataCache().getStudyResult(uri);
	}

	@Override
	public FormResult getFormResult(String uri) {
		return getMetaDataCache().getFormResult(uri);
	}

	@Override
	public void clearCache() {
		logger.info("Clearing cache for: " + currentTenant());
		getMetaDataCache().clearCache();
	}

	@Override
	public Map<String, FormResult> getFormResultCacheMap() {
		return getMetaDataCache().getFormResultCacheMap();
	}

	@Override
	public Map<String, StudyResult> getStudyResultCacheMap() {
		return getMetaDataCache().getStudyResultCacheMap();
	}

	@Override
	public boolean isResultCacheEmpty(ResultType type) {
		return getMetaDataCache().isResultCacheEmpty(type);
	}

}
