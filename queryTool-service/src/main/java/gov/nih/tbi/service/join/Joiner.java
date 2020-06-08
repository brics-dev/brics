package gov.nih.tbi.service.join;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.repository.model.InstancedRecord;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.service.cache.InstancedDataCache;

/**
 * This class is responsible to join the data in the given cache by GUID.
 *
 */
public class Joiner {

	private static final Logger log = Logger.getLogger(Joiner.class);

	private InstancedDataCache cache;
	private String[] formList;

	public Joiner(List<FormResult> formResults, InstancedDataCache cache) {
		this.setFormList(formResults);
		this.cache = cache;
	}

	public Joiner(String[] formList, InstancedDataCache cache) {
		this.formList = formList;
		this.cache = cache;
	}

	public String[] getFormList() {
		return formList;
	}

	public void setFormList(String[] formList) {
		this.formList = formList;
	}

	public void setFormList(List<FormResult> formResults) {
		formList = new String[formResults.size()];

		for (int i = 0; i < formResults.size(); i++) {
			formList[i] = formResults.get(i).getShortNameAndVersion();
		}
	}

	public InstancedDataCache getInstancedDataCache() {
		return cache;
	}

	public void setInstancedDataCache(InstancedDataCache cache) {
		this.cache = cache;
	}

	/**
	 * Prints the generated combo list (for debugging outputs)
	 * 
	 * @param comboList
	 */
	private void printCombo(List<Integer[]> comboList) {
		log.trace("Combinations " + comboList.size());
		for (int i = 0; i < comboList.size(); ++i) {
			String txt = "[ ";
			Integer[] combo = comboList.get(i);
			for (int j = 0; j < combo.length - 1; j++) {
				txt += (combo[j] + ", ");
			}
			txt += (combo[combo.length - 1] + " ]");
			log.trace(txt);
		}
	}

	/**
	 * Generate all the possible permutations based on the list of form indexes. The permutations are also sorted
	 * descending order based on length. E.g. Given [ 0, 1 ], the output should be [ [ 0, 1 ] , [ 0 ], [ 1 ] ]
	 * 
	 * @param list
	 * @return
	 */
	public List<Integer[]> getComboList(int[] list) {


		List<Integer[]> join = combine(list);

		// sort combinations so the longest are listed first:
		// combinations are processed longest -> shortest to ensure
		// the most restricted use of each guid is tested first
		List<Integer[]> sortedComboList = new ArrayList<Integer[]>();
		for (int i = list.length; i > 0; i--) {
			for (int j = 0; j < join.size(); j++) {
				if (join.get(j).length == i) {
					sortedComboList.add(join.get(j));
				}
			}
		}

		return sortedComboList;
	}

	/**
	 * Aux method that generates the unsorted permutations.
	 * 
	 * @param list
	 * @return
	 */
	private List<Integer[]> combine(int[] list) {
		if (list.length == 2) {
			List<Integer[]> combineList = new ArrayList<Integer[]>();
			combineList.add(new Integer[] {list[0], list[1]});
			combineList.add(new Integer[] {list[0]});
			combineList.add(new Integer[] {list[1]});
			return combineList;
		} else {
			int[] subList = new int[list.length - 1];
			for (int i = 0; i < subList.length; i++) {
				subList[i] = list[i + 1];
			}
			List<Integer[]> subCombo = combine(subList);
			List<Integer[]> joinList = new ArrayList<Integer[]>();
			for (int i = 0; i < subCombo.size(); ++i) {
				Integer[] array = subCombo.get(i);
				Integer[] join = new Integer[array.length + 1];
				join[0] = list[0];
				for (int j = 0; j < array.length; j++) {
					join[j + 1] = array[j];
				}
				joinList.add(join);
			}
			for (int i = 0; i < subCombo.size(); ++i) {
				joinList.add(subCombo.get(i));
			}
			joinList.add(new Integer[] {list[0]});
			return joinList;
		}
	}

	/**
	 * Call this to trigger the join process. Returns the result of the join as a list of InstancedRecords.
	 * 
	 * @return
	 */
	public List<InstancedRecord> doJoin() {
		int[] indexList = new int[formList.length];
		for (int i = 0; i < formList.length; ++i) {
			indexList[i] = i;
		}

		List<Integer[]> comboList = getComboList(indexList);

		if (log.isTraceEnabled()) {
			printCombo(comboList);
		}

		Set<String> guidList = cache.getAllGuids();

		if (log.isDebugEnabled()) {
			log.debug("Number of unique GUIDs: " + guidList.size());
		}

		List<InstancedRecord> records = new ArrayList<InstancedRecord>();

		for (String guid : guidList) {
			for (Integer[] combo : comboList) {
				boolean match = true;

				for (Integer formIndex : combo) {
					String form = formList[formIndex];
					if (!cache.hasGuid(form, guid)) {
						match = false;
						break;
					}
				}

				if (match) {
					records.addAll(createRecords(guid, combo));
					break;
				}
			}
		}

		return records;
	}

	/**
	 * Given a guid and a combination, returns a list of all permutations of the data from each form with the given
	 * guid.
	 * 
	 * @param guid
	 * @param combo
	 * @return
	 */
	private List<InstancedRecord> createRecords(String guid, Integer[] combo) {
		// uncomment this to bring back left outer join.
		// if (!formList[combo[0]].equals(formList[0])) {
		// return new ArrayList<InstancedRecord>();
		// }

		// gather all the data with the given guid into a 2D array. Linked list is used here because we will need the
		// poll method later.
		LinkedList<List<InstancedRow>> allData = new LinkedList<>();

		for (Integer i : combo) {
			String formName = formList[i];
			List<InstancedRow> currentData = cache.getByFormName(formName).getByGuid(guid);
			allData.add(currentData);
		}

		List<InstancedRecord> records = createRecordsAux(allData, guid, combo);

		return records;
	}

	/**
	 * The recursive helper method that is used by createRecords
	 * 
	 * @param allData
	 * @param guid
	 * @param combo
	 * @return
	 */
	private List<InstancedRecord> createRecordsAux(LinkedList<List<InstancedRow>> allData, String guid,
			Integer[] combo) {
		if (allData.isEmpty()) {
			return null;
		}

		List<InstancedRecord> records = new ArrayList<>();
		List<InstancedRow> currentRows = allData.poll();
		Integer currentCombo = combo[0];

		if (allData.size() >= 1) {

			List<InstancedRecord> restOfRecords =
					createRecordsAux(allData, guid, Arrays.copyOfRange(combo, 1, combo.length));

			for (InstancedRow currentRow : currentRows) {
				for (InstancedRecord currentRestOfRecord : restOfRecords) {
					InstancedRecord newRecord = initializeRecord(guid);
					newRecord.setSelectedRow(currentCombo, currentRow);
					mergeRecords(newRecord, currentRestOfRecord);
					records.add(newRecord);
				}
			}
		} else {
			for (InstancedRow currentRow : currentRows) {
				InstancedRecord newRecord = initializeRecord(guid);
				newRecord.setSelectedRow(currentCombo, currentRow);
				records.add(newRecord);
			}
		}

		return records;
	}

	/**
	 * Merge the second record (r2) into r1. Index must be retained. For example, merging r1 [ f1, null, null ] and r2 [
	 * null, f2, null ] turns r1 into [ f1, f2, null ].
	 * 
	 * @param r1
	 * @param r2
	 */
	private void mergeRecords(InstancedRecord r1, InstancedRecord r2) {
		for (int i = 0; i < r2.getSelectedRows().size(); i++) {
			InstancedRow currentRow = r2.getSelectedRows().get(i);
			if (currentRow != null) {
				r1.setSelectedRow(i, currentRow);
			}
		}
	}

	/**
	 * Initialize the record with the same number of nulls as we have forms being joined. For example, if joining 5
	 * forms, this record should look like [null,null,null,null,null]
	 * 
	 * @param guid
	 * @return
	 */
	private InstancedRecord initializeRecord(String guid) {
		InstancedRecord record = new InstancedRecord(guid);
		for (int i = 0; i < formList.length; i++) {
			record.addSelectedRow(null);
		}

		return record;
	}
}
