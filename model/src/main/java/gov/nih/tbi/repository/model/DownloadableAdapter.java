package gov.nih.tbi.repository.model;

import gov.nih.tbi.repository.model.hibernate.AccountDownloadFile;
import gov.nih.tbi.repository.model.hibernate.DatasetDownloadFile;
import gov.nih.tbi.repository.model.hibernate.DownloadFileDataset;
import gov.nih.tbi.repository.model.hibernate.Downloadable;
import gov.nih.tbi.repository.model.hibernate.QueryToolDownloadFile;
import gov.nih.tbi.repository.model.hibernate.UserFile;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Since downloadable is an abstract class, we will need an adapter to...'adapt' the downloadable object to its
 * subclasses, otherwise jaxb will just ignore the fields that only exists in the implementing classes
 * 
 * @author Francis Chen
 *
 */
public class DownloadableAdapter extends XmlAdapter<DownloadableAdapter.DownloadableElements, Set<Downloadable>> {

	protected static class DownloadableElements {
		@XmlElement(name = "item")
		public Set<DownloadableElement> elements = new HashSet<DownloadableElement>();
	}

	protected static class DownloadableElement {
		@XmlAttribute
		public Long id;

		@XmlElement
		public Set<DownloadFileDataset> datasets = new HashSet<DownloadFileDataset>();

		@XmlElement
		public UserFile userFile;

		@XmlAttribute
		public DownloadableOrigin fileOrigin;

		@XmlAttribute
		public SubmissionType fileType;
	}

	@Override
	public Set<Downloadable> unmarshal(DownloadableElements v) throws Exception {
		Set<Downloadable> downloadables = new HashSet<Downloadable>();

		for (DownloadableElement element : v.elements) {
			switch (element.fileOrigin) {
				case ACCOUNT:
					AccountDownloadFile accountFile = new AccountDownloadFile();
					accountFile.setId(element.id);
					accountFile.setUserFile(element.userFile);
					accountFile.setType(SubmissionType.ACCOUNT_FILE);
					downloadables.add(accountFile);
					break;
				case DATASET:
					DatasetDownloadFile datasetFile = new DatasetDownloadFile();
					datasetFile.setId(element.id);
					datasetFile.setUserFile(element.userFile);
					Iterator<DownloadFileDataset> dsIterator = element.datasets.iterator();
					if (dsIterator.hasNext()) {
						datasetFile.setDataset(dsIterator.next());
					}
					datasetFile.setType(element.fileType);
					downloadables.add(datasetFile);
					break;
				case QUERY_TOOL:
					QueryToolDownloadFile qtFile = new QueryToolDownloadFile();
					qtFile.setId(element.id);
					qtFile.setUserFile(element.userFile);
					qtFile.setType(element.fileType);
					qtFile.setDatasets(element.datasets);
					downloadables.add(qtFile);
					break;
				default:
					break;
			}
		}

		return downloadables;
	}

	@Override
	public DownloadableElements marshal(Set<Downloadable> v) throws Exception {
		DownloadableElements output = new DownloadableElements();

		for (Downloadable downloadable : v) {
			DownloadableElement element = new DownloadableElement();
			output.elements.add(element);
			element.id = downloadable.getId();
			element.fileOrigin = downloadable.getOrigin();
			element.userFile = downloadable.getUserFile();
			switch (downloadable.getOrigin()) {
				case DATASET:
					DatasetDownloadFile datasetDownloadFile = (DatasetDownloadFile) downloadable;
					if (datasetDownloadFile.getDataset() != null)
						element.datasets.add(datasetDownloadFile.getDataset());
					if (datasetDownloadFile.getType() != null){
						element.fileType = datasetDownloadFile.getType();
					}
					else{
						element.fileType = SubmissionType.UNKNOWN;
					}
					break;
				case QUERY_TOOL:
					// can't load the datasets here because we are lazy loading the datasets.
					element.fileType = SubmissionType.DATA_FILE;
					break;
				default:
					break;
			}
			
		}
		return output;
	}


}
