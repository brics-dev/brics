package gov.nih.tbi.service.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
	private List<InstancedRecord> joinResult;

	// stores info about the sorting state. This is mainly used to track when the sorting gets changed so we know when
	// we will need to re-sort the join result.
	private DataTableColumn currentSortColumn;
	private String currentSortOrder;
	private boolean hasMatchingGuid;

	public boolean hasMatchingGuid() {

		// empty or null automatically tells us theres no matching guid
		if (joinResult == null || joinResult.isEmpty()) {
			return false;
		}

		return hasMatchingGuid;
	}

	public void setHasMatchingGuid(boolean hasMatchingGuid) {
		this.hasMatchingGuid = hasMatchingGuid;
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

	public List<InstancedRecord> getJoinResult() {
		return joinResult;
	}

	public void setJoinResult(List<InstancedRecord> joinResult) {
		this.joinResult = joinResult;
	}

	public int getJoinRowCount() {
		if (!isJoined()) {
			return 0;
		}

		return joinResult.size();
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

		if (joinResult != null) {
			joinResult.clear();
		}
	}

	public boolean isEmpty() {
		return (cacheMap == null ? true : cacheMap.isEmpty()) && (joinResult == null ? true : joinResult.isEmpty());
	}

	public boolean isJoined() {
		return joinResult == null ? false : !joinResult.isEmpty();
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
