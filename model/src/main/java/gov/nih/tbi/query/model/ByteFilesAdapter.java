package gov.nih.tbi.query.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;


public class ByteFilesAdapter extends XmlAdapter<ByteFilesAdapter.ByteFileElements, Map<String, byte[]>> {
	// public class AttachedFilesAdapter extends XmlAdapter<AttachedFilesAdapter.AttachedFilesElements,
	// ListMultimap<Long, String>> {

	protected static class ByteFileElements {
		@XmlElement
		public List<ByteFileElement> elements = new ArrayList<ByteFileElement>();

		void addEntry(ByteFileElement entry) {
			elements.add(entry);
		}
	}

	protected static class ByteFileElement {
		@XmlAttribute
		public String name;

		@XmlElement
		public byte[] fileByte;

		public ByteFileElement() {}

		public ByteFileElement(String name, byte[] bytes) {
			this.name = name;
			fileByte = bytes;
		}
	}

	@Override
	public Map<String, byte[]> unmarshal(ByteFileElements v) throws Exception {
		Map<String, byte[]> output = new HashMap<>();
		for (ByteFileElement element : v.elements) {
			output.put(element.name, element.fileByte);
		}

		return output;
	}

	@Override
	public ByteFileElements marshal(Map<String, byte[]> v) throws Exception {

			ByteFileElements output = new ByteFileElements();

		for (Map.Entry<String, byte[]> entry : v.entrySet()) {
			output.addEntry(new ByteFileElement(entry.getKey(), entry.getValue()));
			}
			
			return output;
		}
}
