package gov.nih.tbi.service.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.InstancedRecord;

/**
 * This object caches the result of the join in session.
 *
 */
public class InstancedDataCache implements Serializable {
	private static final long serialVersionUID = 145475423319237987L;

	private static final Logger log = Logger.getLogger(InstancedDataCache.class);

	// This is a map of form short name -> data cache for the form
	private Map<String, InstancedDataFormCache> cacheMap;

	// The cached result of the join
	private List<InstancedRecord> resultCache;

	private Map<String, DataTableColumn> columnCache;

	// stores info about the sorting state. This is mainly used to track when the sorting gets changed so we know when
	// we will need to re-sort the join result.
	private DataTableColumn currentSortColumn;
	private String currentSortOrder;

	public InstancedDataCache() {
	
	}
	
	public InstancedDataCache(InstancedDataCache og) {
		if (og.cacheMap != null) {
			this.cacheMap = new HashMap<String, InstancedDataFormCache>();
			for (Entry<String, InstancedDataFormCache> formCacheEntry : og.cacheMap.entrySet()) {
				InstancedDataFormCache formCacheCopy = new InstancedDataFormCache(formCacheEntry.getValue());
				this.cacheMap.put(formCacheEntry.getKey(), formCacheCopy);
			}
		}

		if (og.resultCache != null) {
			this.resultCache = new ArrayList<InstancedRecord>();
			this.resultCache.addAll(og.resultCache);
		}

		if (og.columnCache != null) {
			this.columnCache = new HashMap<String, DataTableColumn>();
			this.columnCache.putAll(og.columnCache);
		}
	}

	public DataTableColumn getCurrentSortColumn() {
		return currentSortColumn;
	}

	public void setCurrentSortColumn(DataTableColumn currentSortColumn) {
		this.currentSortColumn = currentSortColumn;
	}

	public String getCurrentSortOrder() {
		return currentSortOrder;
	}

	public void setCurrentSortOrder(String currentSortOrder) {
		this.currentSortOrder = currentSortOrder;
	}

	public List<InstancedRecord> getResultCache() {
		return resultCache;
	}

	public void setResultCache(List<InstancedRecord> resultCache) {
		this.resultCache = resultCache;
	}

	public int getCachedRowCount() {
		if (!isResultCached()) {
			return 0;
		}

		return resultCache.size();
	}

	public void putFormCache(String formName, InstancedDataFormCache formCache) {
		if (cacheMap == null) {
			cacheMap = new HashMap<>();
		}

		cacheMap.put(formName, formCache);
	}

	public Set<String> getAllGuids() {
		Set<String> guids = new HashSet<String>();
		if (cacheMap == null) {
			return guids;
		}

		for (InstancedDataFormCache formCache : cacheMap.values()) {
			guids.addAll(formCache.getAllGuids());
		}

		return guids;
	}

	public Map<String, InstancedDataFormCache> getCacheMap() {
		return cacheMap;
	}

	public void setCacheMap(Map<String, InstancedDataFormCache> cacheMap) {
		this.cacheMap = cacheMap;
	}

	public InstancedDataFormCache getByFormName(String formName) {

		if (cacheMap == null) {
			return null;
		}

		return cacheMap.get(formName);
	}

	public void clear() {
		if (cacheMap != null) {
			cacheMap.clear();
		}

		if (resultCache != null) {
			resultCache.clear();
		}

		if (columnCache != null) {
			columnCache.clear();
		}
	}

	public boolean isEmpty() {
		return (cacheMap == null ? true : cacheMap.isEmpty()) && (resultCache == null ? true : resultCache.isEmpty());
	}

	public boolean isResultCached() {
		return resultCache == null ? false : !resultCache.isEmpty();
	}

	public boolean hasGuid(String formName, String guid) {
		if (cacheMap == null) {
			return false;
		}

		InstancedDataFormCache formCache = cacheMap.get(formName);

		if (formCache == null) {
			return false;
		}

		return formCache.hasGuid(guid);
	}
}
