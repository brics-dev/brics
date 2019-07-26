package gov.nih.tbi.doi.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

public class OSTIRestServiceModel {

	@XmlRootElement(name = "records")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class DoiRecordList {
		@XmlElement(name = "record")
		private List<OSTIRecord> recordList;

		public DoiRecordList() {
			recordList = new ArrayList<OSTIRecord>();
		}

		public void addAll(Collection<OSTIRecord> col) {
			recordList.clear();

			if (col != null) {
				recordList.addAll(col);
			}
		}

		public void add(OSTIRecord record) {
			recordList.add(record);
		}

		public List<OSTIRecord> getList() {
			return recordList;
		}
	}

	@XmlRootElement(name = "records")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class DoiResponseList {
		@XmlElement(name = "record")
		private List<OSTIResponse> responseList;

		public DoiResponseList() {
			responseList = new ArrayList<OSTIResponse>();
		}

		public void addAll(Collection<OSTIResponse> col) {
			responseList.clear();

			if (col != null) {
				responseList.addAll(col);
			}
		}

		public void add(OSTIResponse r) {
			responseList.add(r);
		}

		public List<OSTIResponse> getList() {
			return responseList;
		}
	}

	public OSTIRestServiceModel() {}

}
