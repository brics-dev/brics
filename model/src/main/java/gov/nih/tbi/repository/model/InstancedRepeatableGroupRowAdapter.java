package gov.nih.tbi.repository.model;

import gov.nih.tbi.ModelConstants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter to marshal/unmarshal InstancedRepeatableGroupRow
 * 
 * @author fchen
 * 
 */
public class InstancedRepeatableGroupRowAdapter extends XmlAdapter<InstancedRepeatableGroupRowAdapter.InstancedRepeatableGroupRowElements, LinkedHashMap<DataTableColumn, CellValueCode>> {

	protected static class InstancedRepeatableGroupRowElements {

		public final List<InstancedRepeatableGroupRowElement> elements =
				new ArrayList<InstancedRepeatableGroupRowElement>();

	}

	protected static class InstancedRepeatableGroupRowElement {

		public InstancedRepeatableGroupRowElement() {

		}

		public InstancedRepeatableGroupRowElement(DataTableColumn column, CellValueCode value) {

			this.column = column;

			if (value.getValue() != null) {
				// remove 0x3 control value that somehow appeared in production data
				value.setValue(value.getValue().replaceAll("\u0003", ModelConstants.EMPTY_STRING));
			}
		}

		@XmlElement(name = "dataTableColumn")
		public DataTableColumn column;

		@XmlElement(name = "cellValueCode", type = CellValueCode.class)
		public CellValueCode value;
	}

	@Override
	public LinkedHashMap<DataTableColumn, CellValueCode> unmarshal(InstancedRepeatableGroupRowElements v) throws Exception {

		LinkedHashMap<DataTableColumn, CellValueCode> output = new LinkedHashMap<DataTableColumn, CellValueCode>();

		for (InstancedRepeatableGroupRowElement element : v.elements) {
			output.put(element.column, element.value);
		}

		return output;
	}

	@Override
	public InstancedRepeatableGroupRowElements marshal(LinkedHashMap<DataTableColumn, CellValueCode> v) throws Exception {

		InstancedRepeatableGroupRowElements output = new InstancedRepeatableGroupRowElements();

		if (v != null) {
			for (Entry<DataTableColumn, CellValueCode> entry : v.entrySet()) {
				output.elements.add(new InstancedRepeatableGroupRowElement(entry.getKey(), entry.getValue()));
			}
		}

		return output;
	}
}
