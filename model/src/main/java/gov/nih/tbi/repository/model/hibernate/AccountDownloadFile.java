package gov.nih.tbi.repository.model.hibernate;

import gov.nih.tbi.repository.model.DownloadableOrigin;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@DiscriminatorValue(value = "ACCOUNT")
@XmlRootElement(name = "download_file")
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountDownloadFile extends Downloadable {

	private static final long serialVersionUID = 9110280791661648973L;

	@Override
	public DownloadableOrigin getOrigin() {
		return DownloadableOrigin.ACCOUNT;
	}

	@Override
	public String getDownloadSubdirectory() {
		return "";
	}

}
