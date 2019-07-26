package gov.nih.tbi.query.model;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.collect.ListMultimap;

/**
 * This class extends from previous QueryToolDownloadPackage class. Now it can handle data and PV code mapping files
 * created from multiple forms
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class QTDownloadPackage implements Serializable {

	private static final long serialVersionUID = 1907322709557555253L;

	@XmlElement
	private Set<Long> datasetIds;

	@XmlElement
	private String username;

	@XmlElement
	private String directoryName;

	@XmlElement
	private Date dateAdded;

	@XmlElement
	private boolean isCartDownload;
	
	@XmlElement
	private List<String> dataFiles;

	@XmlJavaTypeAdapter(ByteFilesAdapter.class)
	private Map<String, byte[]> byteFileMap;


	// I wanted to use Multimap here, but Jaxb doesn't play nicely with it
	@XmlJavaTypeAdapter(AttachedFilesAdapter.class)
	private ListMultimap<Long, String> attachedFilesMap;

	public List<String> getDataFiles() {
		return dataFiles;
	}

	public void setDataFiles(List<String> dataFiles) {
		this.dataFiles = dataFiles;
	}

	public String getDirectoryName() {
		return directoryName;
	}

	public void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
	}

	public Date getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(Date dateAdded) {
		this.dateAdded = dateAdded;
	}

	public ListMultimap<Long, String> getAttachedFilesMap() {
		return attachedFilesMap;
	}

	public void setAttachedFilesMap(ListMultimap<Long, String> attachedFilesMap) {
		this.attachedFilesMap = attachedFilesMap;
	}

	public boolean isCartDownload() {
		return isCartDownload;
	}

	public void setCartDownload(boolean isCartDownload) {
		this.isCartDownload = isCartDownload;
	}

	public Set<Long> getDatasetIds() {

		return datasetIds;
	}

	public void setDatasetIds(Set<Long> datasetIds) {

		this.datasetIds = datasetIds;
	}

	public String getUsername() {

		return username;
	}

	public void setUsername(String username) {

		this.username = username;
	}

	public QTDownloadPackage() {

	}

	public QTDownloadPackage(Set<Long> datasetIds,
 String username, String description, boolean isCartDownload,
			ListMultimap<Long, String> attachedFilesMap, Date dateAdded, Map<String, byte[]> byteFileMap, List<String> dataFiles
	) {
		super();
		this.datasetIds = datasetIds;
		this.username = username;
		this.directoryName = description;
		this.isCartDownload = isCartDownload;
		this.attachedFilesMap = attachedFilesMap;
		this.dateAdded = dateAdded;
		this.byteFileMap = byteFileMap;
		this.dataFiles = dataFiles;
	}

	public Map<String, byte[]> getByteFileMap() {
		return byteFileMap;
	}

	public void setByteFileMap(Map<String, byte[]> byteFileMap) {
		this.byteFileMap = byteFileMap;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attachedFilesMap == null) ? 0 : attachedFilesMap.hashCode());
		result = prime * result + ((byteFileMap == null) ? 0 : byteFileMap.hashCode());
		result = prime * result + ((datasetIds == null) ? 0 : datasetIds.hashCode());
		result = prime * result + ((dateAdded == null) ? 0 : dateAdded.hashCode());
		result = prime * result + ((directoryName == null) ? 0 : directoryName.hashCode());
		result = prime * result + (isCartDownload ? 1231 : 1237);
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QTDownloadPackage other = (QTDownloadPackage) obj;
		if (attachedFilesMap == null) {
			if (other.attachedFilesMap != null)
				return false;
		} else if (!attachedFilesMap.equals(other.attachedFilesMap))
			return false;
		if (datasetIds == null) {
			if (other.datasetIds != null)
				return false;
		} else if (!datasetIds.equals(other.datasetIds))
			return false;
		if (dateAdded == null) {
			if (other.dateAdded != null)
				return false;
		} else if (!dateAdded.equals(other.dateAdded))
			return false;
		if (directoryName == null) {
			if (other.directoryName != null)
				return false;
		} else if (!directoryName.equals(other.directoryName))
			return false;
		if (isCartDownload != other.isCartDownload)
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;

		if (byteFileMap == null) {
			if (other.byteFileMap != null)
				return false;
		} else if (!byteFileMap.equals(other.byteFileMap))
			return false;

		return true;
	}

	@Override
	public String toString() {
		return "QTDownloadPackage [datasetIds="
				+ datasetIds + ", username=" + username + ", description=" + directoryName + ", isCartDownload="
				+ isCartDownload + "]";
	}
}