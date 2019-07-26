package gov.nih.tbi.repository.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.Downloadable;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.UserFile;

public class RepositoryRestServiceModel {

	@XmlRootElement(name = "Studies")
	public static class StudyWrapper {
		@XmlElement(name = "Study")
		List<Study> studies;

		public StudyWrapper() {
			studies = new ArrayList<Study>();
		}

		public StudyWrapper(Collection<Study> sc) {
			studies = new ArrayList<Study>(sc);
		}

		public void addAll(Collection<Study> l) {
			studies.addAll(l);
		}

		public List<Study> getList() {

			return studies;
		}

	}

	@XmlRootElement(name = "Datasets")
	public static class DatasetWrapper {
		Set<Dataset> datasets = new HashSet<Dataset>();

		public DatasetWrapper() {}

		@XmlElementWrapper(name = "Dataset")
		@XmlElement
		public List<Dataset> getDatasetList() {
			return new ArrayList<Dataset>(datasets);
		}

		public void addAll(Collection<Dataset> l) {
			datasets.addAll(l);
		}

		public Set<Dataset> getSet() {
			return datasets;
		}
	}

	@XmlRootElement(name = "DatasetFiles")
	public static class DatasetFileWrapper {

		@XmlElement(name = "DatasetFile")
		Set<DatasetFile> datasetFiles = new HashSet<DatasetFile>();

		public DatasetFileWrapper() {}

		public Set<DatasetFile> getDatasetFiles() {
			return datasetFiles;
		}

		public void addAll(Collection<DatasetFile> datasetFiles) {
			this.datasetFiles.addAll(datasetFiles);
		}

	}

	@XmlRootElement(name = "UserFiles")
	public static class UserFileWrapper {

		@XmlElement(name = "UserFile")
		List<UserFile> userFileList = new ArrayList<UserFile>();

		public UserFileWrapper() {}

		public List<UserFile> getUserFileList() {
			return userFileList;
		}

		public void addAll(Collection<UserFile> userFileList) {
			userFileList.addAll(userFileList);
		}

	}

	/**
	 * This object is to simplify sending the parameters of the proccessSubmissionTicket
	 * 
	 * @author mgree1
	 * 
	 */
	@XmlRootElement(name = "submissionProccessTicket", namespace = "")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class ProccessTickeWrapper implements Serializable {
		private static final long serialVersionUID = -2308056798366536361L;

		@XmlAttribute
		private String localPath;

		public ProccessTickeWrapper() {}

		public String getLocalPath() {
			return localPath;
		}

		public void setLocalPath(String localPath) {
			this.localPath = localPath;
		}
	}
	
	@XmlRootElement(name = "Download_files")
	public static class DownloadableWrapper {

		List<Downloadable> downloadables = new ArrayList<Downloadable>();

		public DownloadableWrapper() {}
		
		public DownloadableWrapper(List<Downloadable> downloadableList) {
			downloadables.addAll(downloadableList);
		}

		public void addAll(Collection<Downloadable> l) {
			downloadables.addAll(l);
		}
		
		@XmlElementWrapper(name = "Download_file")
		@XmlElement
		public List<Downloadable> getList() {
			return downloadables;
		}
	}
}
