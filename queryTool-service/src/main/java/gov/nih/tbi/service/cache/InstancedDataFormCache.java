package gov.nih.tbi.service.cache;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;

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

	// This is the cache of all unique NonRepeatingCellValues. Should be able to save significant amount of memory when
	// we have a lot of cells with the same data.
	// This is a weak hashmap of weak references to cell values because we want the garbage collect to be able to clean
	// this up when the cell value is no longer being referenced anymore.
	private Map<NonRepeatingCellValue, WeakReference<NonRepeatingCellValue>> cellValueCache =
			new WeakHashMap<NonRepeatingCellValue, WeakReference<NonRepeatingCellValue>>();
	
	public InstancedDataFormCache() {
		
	}
	
	public InstancedDataFormCache(InstancedDataFormCache og) {
		if (og.rowUriMap != null) {
			this.rowUriMap = new HashMap<String, InstancedRow>();
			this.rowUriMap.putAll(og.rowUriMap);
		}

		if (og.guidMap != null) {
			this.guidMap = ArrayListMultimap.create();
			this.guidMap.putAll(og.guidMap);
		}
	}

	/**
	 * This method will take the given cellValue and either cache and return the same object or return the identical
	 * object from the cache. See https://stackoverflow.com/questions/3323807/generic-internpoolt-in-java
	 * 
	 * @param cellValue
	 * @return
	 */
	public synchronized NonRepeatingCellValue internCell(NonRepeatingCellValue cellValue) {
		WeakReference<NonRepeatingCellValue> cellRef = cellValueCache.get(cellValue);
		NonRepeatingCellValue output = null;

		// The loop is needed to deal with race
		// conditions where the GC runs while we are
		// accessing the cellValueCache
		do {
			if (cellRef == null) {
				cellRef = new WeakReference<NonRepeatingCellValue>(cellValue);
				cellValueCache.put(cellValue, cellRef);
				output = cellValue;
			} else {
				output = cellRef.get();
			}
		} while (output == null);

		return output;
	}

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

	public Map<String, InstancedRow> getRowUriMap() {
		return rowUriMap;
	}

	public void setRowUriMap(Map<String, InstancedRow> rowUriMap) {
		this.rowUriMap = rowUriMap;
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
