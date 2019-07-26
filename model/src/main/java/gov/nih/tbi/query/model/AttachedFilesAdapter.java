package gov.nih.tbi.query.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class AttachedFilesAdapter extends XmlAdapter<AttachedFilesAdapter.AttachedFilesElements, ListMultimap<Long, String>> {

	protected static class AttachedFilesElements {
		@XmlElement
		public List<AttachedFilesElement> elements = new ArrayList<AttachedFilesElement>();
	}

	protected static class AttachedFilesElement {
		@XmlAttribute
		public Long datasetId;

		@XmlElement
		public List<String> fileNames;
	}



	@Override
	public ListMultimap<Long, String> unmarshal(AttachedFilesElements v) throws Exception {

		ListMultimap<Long, String> output = ArrayListMultimap.create();

		for (AttachedFilesElement element : v.elements) {
			output.putAll(element.datasetId, element.fileNames);
		}

		return output;
	}

	@Override
	public AttachedFilesElements marshal(ListMultimap<Long, String> v) throws Exception {

		AttachedFilesElements output = new AttachedFilesElements();

		for (Entry<Long, Collection<String>> entry : v.asMap().entrySet()) {
			AttachedFilesElement element = new AttachedFilesElement();
			element.datasetId = entry.getKey();
			element.fileNames = new ArrayList<String>(entry.getValue());
			output.elements.add(element);
		}
		
		return output;
	}

}
