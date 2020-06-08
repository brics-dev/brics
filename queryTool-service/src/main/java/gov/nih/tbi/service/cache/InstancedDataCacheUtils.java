package gov.nih.tbi.service.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import gov.nih.tbi.exceptions.FilterEvaluatorException;
import gov.nih.tbi.filter.FilterEvaluator;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.InstancedRecord;
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
	 * @throws FilterEvaluatorException
	 */
	public static void applyFilter(String filterExpression, List<FormResult> forms, InstancedDataCache cache)
			throws FilterEvaluatorException {
		long startTime = System.nanoTime();

		if (filterExpression != null && !filterExpression.isEmpty()) {
			FilterEvaluator filterEvaluator = new FilterEvaluator(filterExpression, forms);

			if (filterEvaluator.hasFilters()) {
				List<InstancedRecord> records = cache.getResultCache();

				// start a synchronized list to store our final result. This is needed because we will be adding to the
				// list
				// in parallel.
				List<InstancedRecord> finalList = Collections.synchronizedList(new ArrayList<InstancedRecord>());

				records.stream().parallel().forEach(record -> {
					applyFilterAux(filterEvaluator, record, finalList);
				});

				// replace records with elements from final list.
				records.clear();
				records.addAll(finalList);
			}
		}

		long endTime = System.nanoTime();
		log.info("Time to do filter: " + ((endTime - startTime) / 1000000) + "ms");
	}

	/**
	 * Apply filters to the given record. If the record does not get filtered out, add the record to finalList
	 * 
	 * @param filterEvaluators
	 * @param record
	 * @param finalList
	 * @throws FilterEvaluatorException
	 */
	private static void applyFilterAux(FilterEvaluator filterEvaluator, InstancedRecord record,
			List<InstancedRecord> finalList) {
		boolean evaluationResult = filterEvaluator.evaluate(record);

		if (evaluationResult) {
			finalList.add(record);
		}
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
		if (!cache.isResultCached()) {
			return new ArrayList<InstancedRecord>();
		}

		List<InstancedRecord> joinResult = cache.getResultCache();

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
