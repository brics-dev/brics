
package gov.nih.tbi.dictionary.model;


import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.query.model.hibernate.SavedQuery;

public class QueryToolRestServiceModel {

	@XmlRootElement(name = "SavedQueries")
	public static class SavedQueryList {

		@XmlElement(name = "SavedQuery")
		List<SavedQuery> sqList;

		public SavedQueryList() {
			sqList = new ArrayList<SavedQuery>();
		}

		public void addAll(List<SavedQuery> s) {
			sqList.clear();

			if (s != null) {
				sqList.addAll(s);
			}
		}

		public void add(SavedQuery s) {
			sqList.add(s);
		}

		public List<SavedQuery> getList() {
			return sqList;
		}
	}

	@XmlRootElement(name = "MetaStudies")
	public static class MetaStudyList {

		@XmlElement(name = "MetaStudy")
		List<MetaStudy> msList;

		public MetaStudyList() {
			msList = new ArrayList<MetaStudy>();
		}

		public void addAll(List<MetaStudy> m) {
			msList.clear();

			if (m != null) {
				msList.addAll(m);
			}
		}

		public void add(MetaStudy m) {
			msList.add(m);
		}

		public List<MetaStudy> getList() {
			return msList;
		}
	}
}
