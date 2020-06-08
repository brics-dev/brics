package gov.nih.tbi.export.json;

import com.google.gson.JsonObject;

import gov.nih.tbi.repository.model.InstancedRecord;

public interface JsonRecordSerializer {
	/**
	 * Serialize the given record into a single string
	 * 
	 * @param record
	 * @param visibleColumns
	 * @return
	 */
	public JsonObject serializeRecord(InstancedRecord record);

	/**
	 * Serialize the next record by first advancing the record iterator, then
	 * calling serialize record.
	 * 
	 * @return
	 */
	public JsonObject serializeNext();

	/**
	 * Returns true if there are more records.
	 * 
	 * @return
	 */
	public boolean hasNext();
}
