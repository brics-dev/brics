package gov.nih.tbi.export.json;

import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonObject;

import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.repository.model.FormHeader;
import gov.nih.tbi.repository.model.InstancedRecord;

public abstract class AbstractJsonRecordSerializer implements JsonRecordSerializer {
	protected InstancedDataTable instancedDataTable;
	protected List<FormHeader> headers;
	protected String displayOption;
	protected Iterator<InstancedRecord> recordIterator;

	public AbstractJsonRecordSerializer(InstancedDataTable instancedDataTable) {
		this.instancedDataTable = instancedDataTable;
		this.headers = instancedDataTable.getHeaders();
		this.displayOption = instancedDataTable.getDisplayOption();
		this.recordIterator = instancedDataTable.getInstancedRecords().iterator();
	}

	@Override
	public JsonObject serializeNext() {
		InstancedRecord currentRecord = recordIterator.next();
		return serializeRecord(currentRecord);
	}

	@Override
	public boolean hasNext() {
		return recordIterator.hasNext();
	}
}
