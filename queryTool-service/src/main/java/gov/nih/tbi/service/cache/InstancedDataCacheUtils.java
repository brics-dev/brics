package gov.nih.tbi.service.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import gov.nih.tbi.filter.Filter;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.InstancedRecord;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.util.InstancedRecordComparator;

/**
 * This class includes a bunch of utilities that help manipulate data in the instanced data cache.
 *
 */
public class InstancedDataCacheUtils {
	private static final Logger log = Logger.getLogger(InstancedDataCacheUtils.class);

	/**
	 * Apply all the filters in the forms against the data in cache by removing all the records that should get filtered
	 * out.
	 * 
	 * @param forms
	 * @param cache
	 */
	public static void applyFilter(List<FormResult> forms, InstancedDataCache cache) {
		long startTime = System.nanoTime();

		List<List<Filter>> filterList = new ArrayList<>();

		boolean hasFilter = false;

		// reconstruct the filters into a 2D arraylist where the index of the nested array correspond to the position of
		// the form being filtered.
		for (FormResult form : forms) {
			List<Filter> subList = new ArrayList<>();

			subList.addAll(form.getFilters());

			if (!subList.isEmpty()) {
				hasFilter = true;
			}
			filterList.add(subList);
		}

		if (hasFilter) {
			applyFilter(filterList, cache.getJoinResult());
		}

		long endTime = System.nanoTime();
		log.info("Time to do filter: " + ((endTime - startTime) / 1000000) + "ms");
	}

	/**
	 * Applies the given filters against the collection of records by removing the records that should get filtered out.
	 * 
	 * @param filterList
	 * @param records
	 */
	private static void applyFilter(List<List<Filter>> filterList, List<InstancedRecord> records) {
		// start a synchronized list to store our final result. This is needed because we will be adding to the list in
		// parallel.
		List<InstancedRecord> finalList = Collections.synchronizedList(new ArrayList<InstancedRecord>());

		records.stream().parallel().forEach(record -> applyFilter(filterList, record, finalList));

		// replace records with elements from final list.
		records.clear();
		records.addAll(finalList);
	}

	/**
	 * Apply filters to the given record. If the record does not get filtered out, add the record to finalList
	 * 
	 * @param filterList
	 * @param record
	 * @param finalList
	 */
	private static void applyFilter(List<List<Filter>> filterList, InstancedRecord record,
			List<InstancedRecord> finalList) {
		boolean evaluationResult = true;

		// short circuit the evaluation if the filter comes back as false.
		for (int i = 0; i < filterList.size() && evaluationResult; i++) {
			List<Filter> currentFilterList = filterList.get(i);

			if (!currentFilterList.isEmpty()) {
				evaluationResult = evaluateFilters(currentFilterList, record.getSelectedRows().get(i));
			}
		}

		if (evaluationResult) {
			finalList.add(record);
		}
	}

	/**
	 * Evaluate the filters against a given row of data
	 * 
	 * @param filterList
	 * @param row
	 * @return
	 */
	private static boolean evaluateFilters(List<Filter> filterList, InstancedRow row) {

		for (Filter filter : filterList) {
			// short-circuit the evaluation if it comes back false.
			if (!filter.evaluate(row)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns paginated data using the cached join result. Sorting is also done in this method.
	 * 
	 * @param offset
	 * @param limit
	 * @param sortColumn
	 * @param sortOrder
	 * @return
	 */
	public static List<InstancedRecord> getPageData(InstancedDataCache cache, int offset, int limit,
			DataTableColumn sortColumn, String sortOrder) {
		if (!cache.isJoined()) {
			return new ArrayList<InstancedRecord>();
		}

		List<InstancedRecord> joinResult = cache.getJoinResult();

		// SORTING
		// only do sorting here if the sort order/column changes
		if (sortColumn != null && (!sortColumn.equals(cache.getCurrentSortColumn())
				|| !sortOrder.equals(cache.getCurrentSortOrder()))) {
			long startTime = System.nanoTime();
			Collections.sort(joinResult, new InstancedRecordComparator(sortColumn, sortOrder));
			long endTime = System.nanoTime();
			log.info("Time to do sort: " + ((endTime - startTime) / 1000000) + "ms");

			// store the column and sort order, so we don't re-sort the result using the same parameters again.
			cache.setCurrentSortColumn(sortColumn);
			cache.setCurrentSortOrder(sortOrder);
		}

		// PAGINATE
		int toIndex = joinResult.size() <= offset + limit ? joinResult.size() : offset + limit;
		List<InstancedRecord> output = joinResult.subList(offset, toIndex);

		return output;
	}
}
