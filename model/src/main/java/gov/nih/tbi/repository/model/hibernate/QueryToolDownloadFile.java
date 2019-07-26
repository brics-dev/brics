package gov.nih.tbi.repository.model.hibernate;

import gov.nih.tbi.repository.model.DownloadableOrigin;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@DiscriminatorValue(value = "QUERY_TOOL")
@XmlRootElement(name = "download_file")
@XmlAccessorType(XmlAccessType.FIELD)
public class QueryToolDownloadFile extends Downloadable {

	private static final long serialVersionUID = 8533442459432774968L;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "download_file_dataset", joinColumns = {@JoinColumn(name = "download_file_id")}, inverseJoinColumns = {@JoinColumn(name = "dataset_id")})
	@XmlElement
	private Set<DownloadFileDataset> datasets;

	public QueryToolDownloadFile() {
		this.datasets = new HashSet<DownloadFileDataset>();
	}

	public Set<DownloadFileDataset> getDatasets() {
		return datasets;
	}

	public void setDatasets(Set<DownloadFileDataset> datasets) {
		this.datasets = datasets;
	}

	public void addDataset(DownloadFileDataset dataset) {
		this.datasets.add(dataset);
	}

	@Override
	public DownloadableOrigin getOrigin() {
		return DownloadableOrigin.QUERY_TOOL;
	}

	@Override
	public String getDownloadSubdirectory() {
		return "";
	}
}
