package gov.nih.tbi.repository.portal;

import java.util.Comparator;

import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.repository.model.hibernate.Dataset;

public class DatasetComparatorFactory {

	private static final String PREFIXED_ID = "prefixId";
	private static final String NAME = "name";
	private static final String SUBMIT_DATE = "submitDate";
	private static final String TYPE = "type";
	private static final String STATUS = "status";
	private static final String RECORD_COUNT = "recordCount";

	/**
	 * Returns a comparator that sorts by the given field name in <b>ascending</b> order.
	 * 
	 * @param fieldName
	 * @return
	 * @throws IdtSortingException
	 */
	public static Comparator<Dataset> getComparator(String fieldName) throws InvalidColumnException {
		switch (fieldName) {
			case PREFIXED_ID:
				return new Comparator<Dataset>() {
					public int compare(Dataset one, Dataset two) {
						return one.getPrefixedId().compareTo(two.getPrefixedId());
					}
				};
			case NAME:
				return new Comparator<Dataset>() {
					public int compare(Dataset one, Dataset two) {
						return one.getName().compareTo(two.getName());
					}
				};
			case SUBMIT_DATE:
				return new Comparator<Dataset>() {
					public int compare(Dataset one, Dataset two) {
						if (two.getSubmitDate() == null) {
							return (one.getSubmitDate() == null) ? 0 : 1;
						}
						if (one.getSubmitDate() == null) {
							return -1;
						}

						return one.getSubmitDate().compareTo(two.getSubmitDate());
					}
				};
			case TYPE:
				return new Comparator<Dataset>() {
					public int compare(Dataset one, Dataset two) {
						return one.getFileTypeString().compareTo(two.getFileTypeString());
					}
				};
			case STATUS:
				return new Comparator<Dataset>() {
					public int compare(Dataset one, Dataset two) {
						return one.getDatasetStatus().getName().compareTo(two.getDatasetStatus().getName());
					}
				};
			case RECORD_COUNT:
				return new Comparator<Dataset>() {
					public int compare(Dataset one, Dataset two) {
						if (two.getRecordCount() == null) {
							return (one.getRecordCount() == null) ? 0 : 1;
						}
						if (one.getRecordCount() == null) {
							return -1;
						}

						return one.getRecordCount().compareTo(two.getRecordCount());
					}
				};
			default:
				throw new InvalidColumnException("Attempting to sort by an invalid column.");
		}
	}
}
