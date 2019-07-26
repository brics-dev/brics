package gov.nih.tbi.service.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import gov.nih.tbi.repository.model.InstancedRow;

/**
 * This object is used to cache data for a particular form in session.
 *
 */
public class InstancedDataFormCache implements Serializable {
	private static final long serialVersionUID = 8009578804858082732L;

	// index the instanced row by row URI
	private Map<String, InstancedRow> rowUriMap;

	// index of the row by GUID. This is important for joins.
	private ListMultimap<String, InstancedRow> guidMap;

	public void putRow(InstancedRow row) {
		if (rowUriMap == null) {
			rowUriMap = new HashMap<>();
		}

		if (guidMap == null) {
			guidMap = ArrayListMultimap.create();
		}

		rowUriMap.put(row.getRowUri(), row);

		if (row.getGuid() != null && !row.getGuid().isEmpty()) {
			guidMap.put(row.getGuid(), row);
		}
	}

	public Set<String> getAllGuids() {
		Set<String> guids = new HashSet<>();

		if (guidMap == null) {
			return guids;
		}

		return guidMap.keySet();
	}

	public List<InstancedRow> getByGuid(String guid) {
		if (guidMap == null) {
			return new ArrayList<InstancedRow>();
		}

		return guidMap.get(guid);
	}

	/**
	 * Returns true if the given GUID exists in our form cache.
	 * 
	 * @param guid
	 * @return
	 */
	public boolean hasGuid(String guid) {
		return guidMap != null ? guidMap.containsKey(guid) : false;
	}

	public InstancedRow getByRowUri(String rowUri) {
		if (rowUriMap == null) {
			return null;
		}

		return rowUriMap.get(rowUri);
	}

	public Map<String, InstancedRow> getAllRows(Map<String, String> unselectedRowUriFormMap) {
		if (rowUriMap == null) {
			return null;
		}
		Map<String, InstancedRow> collect =
				rowUriMap.entrySet().stream().filter(x -> unselectedRowUriFormMap.get(x.getKey()) == null)
						.collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
		return collect;
	}
}
