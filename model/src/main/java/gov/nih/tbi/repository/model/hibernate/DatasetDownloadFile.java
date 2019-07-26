package gov.nih.tbi.repository.model.hibernate;

import gov.nih.tbi.repository.model.DownloadableOrigin;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@DiscriminatorValue(value = "DATASET")
@XmlRootElement(name = "download_file")
@XmlAccessorType(XmlAccessType.FIELD)
public class DatasetDownloadFile extends Downloadable {

	private static final long serialVersionUID = 8390509696988006235L;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinTable(name = "download_file_dataset", joinColumns = {@JoinColumn(name = "download_file_id")}, inverseJoinColumns = {@JoinColumn(name = "dataset_id")})
	private DownloadFileDataset dataset;
	
	public DownloadFileDataset getDataset() {
		return dataset;
	}

	public void setDataset(DownloadFileDataset dataset) {
		this.dataset = dataset;
	}

	@Override
	public DownloadableOrigin getOrigin() {
		return DownloadableOrigin.DATASET;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((dataset == null) ? 0 : dataset.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DatasetDownloadFile other = (DatasetDownloadFile) obj;
		if (dataset == null) {
			if (other.dataset != null)
				return false;
		} else if (!dataset.equals(other.dataset))
			return false;
		return true;
	}

	@Override
	public String getDownloadSubdirectory() {
		return this.dataset.getName();
	}

}
