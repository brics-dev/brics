package gov.nih.tbi.export.csv;

import gov.nih.tbi.repository.model.InstancedRecord;

public interface CsvRecordSerializer {
	/**
	 * Serialize the header for CSV
	 * 
	 * @return
	 */
	public String serializeHeader();
	
	/**
	 * Serialize the given record into a single string
	 * @param record
	 * @return
	 */
	public String serializeRecord(InstancedRecord record);

	/**
	 * Serialize the next record by first advancing the record iterator, then calling serialize record.
	 * 
	 * @return
	 */
	public String serializeNext();

	/**
	 * Returns true if there are more records.
	 * 
	 * @return
	 */
	public boolean hasNext();
}
