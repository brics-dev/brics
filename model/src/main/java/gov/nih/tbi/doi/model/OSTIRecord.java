package gov.nih.tbi.doi.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import gov.nih.tbi.commons.model.BricsInstanceType;

@XmlRootElement(name = "record")
@XmlAccessorType(XmlAccessType.FIELD)
public class OSTIRecord implements Serializable {
	private static final long serialVersionUID = -1608087173794463940L;
	public static final int DOI_DESCRIPTION_MAX_LENGTH = 5000;
	public static final String DOI_DATE_REGEX = "(0[1-9]|1[012])/(0[1-9]|[12][0-9]|3[01])/(19|20)\\d{2}";
	public static final String DOI_DATE_FORMAT = "MM/dd/yyyy";

	// Required fields
	@XmlElement(name = "osti_id")
	private Long ostiId;

	@XmlElement(name = "title", required = true)
	private String title;

	@XmlElement(name = "creators")
	private String creators;

	@XmlElement(name = "publisher", required = true)
	private String publisher;

	@XmlElement(name = "publication_date", required = true)
	private String publicationDate;

	@XmlElement(name = "site_url", required = true)
	private String landingPageUrl;

	@XmlElement(name = "product_type", required = true)
	private OSTIProductType productType;

	@XmlElement(name = "product_type_specific", required = true)
	private String productTypeSpecific;

	@XmlElement(name = "contact_name")
	private String contactName;

	@XmlElement(name = "contact_org")
	private String contactOrganization;

	@XmlElement(name = "contact_email")
	private String contactEmail;

	@XmlElement(name = "contact_phone")
	private String contactPhone;

	// Optional fields
	@XmlElement(name = "product_nos")
	private String projectNumbers;

	@XmlElement(name = "contract_nos")
	private String contractNumbers;

	@XmlElement(name = "sponsor_org")
	private String sponsoringOrganization;

	@XmlElement(name = "doi")
	private String doi;

	@XmlElement(name = "doi_infix")
	private BricsInstanceType doiInfix; // Required only during DOI creation.

	@XmlElement(name = "description")
	private String description;

	@XmlElement(name = "related_resource")
	private String releatedResource;

	@XmlElement(name = "availability")
	private String availability;

	@XmlElement(name = "contributor_organizations")
	private String contributorOrganizations;

	@XmlElement(name = "other_identifying_nos")
	private String otherIdentifyingNumbers;

	@XmlElement(name = "keywords")
	private String keywords;

	@XmlElement(name = "file_extension")
	private String fileExtension;

	/**
	 * Set a value <b>ONLY</b> when reserving a DOI. Leave this property as null for all other requests. If a value is
	 * set, the site/landing page URL must be null.
	 */
	@XmlElement(name = "set_reserved")
	private String setReserved;

	@XmlElementWrapper(name = "relatedidentifiersblock")
	@XmlElement(name = "relatedidentifers_detail")
	private List<OSTIRelatedIdentifierDetail> relatedIdentifiers;

	public OSTIRecord() {}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCreators() {
		return creators;
	}

	public void setCreators(String creators) {
		this.creators = creators;
	}

	public String getProjectNumbers() {
		return projectNumbers;
	}

	public void setProjectNumbers(String projectNumbers) {
		this.projectNumbers = projectNumbers;
	}

	public String getContractNumbers() {
		return contractNumbers;
	}

	public void setContractNumbers(String contractNumbers) {
		this.contractNumbers = contractNumbers;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getPublicationDate() {
		return publicationDate;
	}

	/**
	 * Sets the publication date that is formatted as {@value #DOI_DATE_FORMAT}. An exception is thrown if the passed in
	 * string does not represent a date and is not formatted correctly.
	 * 
	 * @param publicationDate - The new publication date.
	 * @throws IllegalArgumentException When the given string is found to not be a date and/or is not formatted
	 *         correctly.
	 */
	public void setPublicationDate(String publicationDate) throws IllegalArgumentException {
		if ((publicationDate != null) && !publicationDate.matches(OSTIRecord.DOI_DATE_REGEX)) {
			throw new IllegalArgumentException(
					"The new publication date is not formatted as " + OSTIRecord.DOI_DATE_FORMAT + ".");
		}

		this.publicationDate = publicationDate;
	}

	public String getSponsoringOrganization() {
		return sponsoringOrganization;
	}

	public void setSponsoringOrganization(String sponsoringOrganization) {
		this.sponsoringOrganization = sponsoringOrganization;
	}

	public String getLandingPageUrl() {
		return landingPageUrl;
	}

	public void setLandingPageUrl(String landingPageUrl) {
		this.landingPageUrl = landingPageUrl;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactOrganization() {
		return contactOrganization;
	}

	public void setContactOrganization(String contactOrganization) {
		this.contactOrganization = contactOrganization;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	public Long getOstiId() {
		return ostiId;
	}

	public void setOstiId(Long ostiId) {
		this.ostiId = ostiId;
	}

	public String getDoi() {
		return doi;
	}

	public void setDoi(String doiUrl) {
		this.doi = doiUrl;
	}

	public BricsInstanceType getDoiInfix() {
		return doiInfix;
	}

	public void setDoiInfix(BricsInstanceType infix) {
		this.doiInfix = infix;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Sets the DOI's description if it is with {@value #DOI_DESCRIPTION_MAX_LENGTH} characters in length, otherwise an
	 * exception is thrown.
	 * 
	 * @param descr - The value to set as the DOI's description
	 * @throws IllegalArgumentException When the given description value is greater than
	 *         {@value #DOI_DESCRIPTION_MAX_LENGTH} characters in length.
	 */
	public void setDescription(String descr) throws IllegalArgumentException {
		if ((descr != null) && (descr.length() > OSTIRecord.DOI_DESCRIPTION_MAX_LENGTH)) {
			throw new IllegalArgumentException("The DOI's description cannot be more than "
					+ OSTIRecord.DOI_DESCRIPTION_MAX_LENGTH + " characters in length.");
		}

		this.description = descr;
	}

	public String getReleatedResource() {
		return releatedResource;
	}

	public void setReleatedResource(String releatedResource) {
		this.releatedResource = releatedResource;
	}

	public String getAvailability() {
		return availability;
	}

	public void setAvailability(String availability) {
		this.availability = availability;
	}

	public String getContributorOrganizations() {
		return contributorOrganizations;
	}

	public void setContributorOrganizations(String contributorOrganizations) {
		this.contributorOrganizations = contributorOrganizations;
	}

	public String getOtherIdentifyingNumbers() {
		return otherIdentifyingNumbers;
	}

	public void setOtherIdentifyingNumbers(String otherIdentifyingNumbers) {
		this.otherIdentifyingNumbers = otherIdentifyingNumbers;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public String getSetReserved() {
		return setReserved;
	}

	public void setSetReserved(String setReserved) {
		this.setReserved = setReserved;
	}

	public List<OSTIRelatedIdentifierDetail> getRelatedIdentifiers() {
		return relatedIdentifiers;
	}

	public void setRelatedIdentifiers(List<OSTIRelatedIdentifierDetail> relatedIdentifiers) {
		this.relatedIdentifiers = relatedIdentifiers;
	}

	public OSTIProductType getProductType() {
		return productType;
	}

	public void setProductType(OSTIProductType productType) {
		this.productType = productType;
	}

	public String getProductTypeSpecific() {
		return productTypeSpecific;
	}

	public void setProductTypeSpecific(String productTypeSpecific) {
		this.productTypeSpecific = productTypeSpecific;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((availability == null) ? 0 : availability.hashCode());
		result = prime * result + ((contactEmail == null) ? 0 : contactEmail.hashCode());
		result = prime * result + ((contactName == null) ? 0 : contactName.hashCode());
		result = prime * result + ((contactOrganization == null) ? 0 : contactOrganization.hashCode());
		result = prime * result + ((contactPhone == null) ? 0 : contactPhone.hashCode());
		result = prime * result + ((contractNumbers == null) ? 0 : contractNumbers.hashCode());
		result = prime * result + ((contributorOrganizations == null) ? 0 : contributorOrganizations.hashCode());
		result = prime * result + ((creators == null) ? 0 : creators.hashCode());
		result = prime * result + ((projectNumbers == null) ? 0 : projectNumbers.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((doiInfix == null) ? 0 : doiInfix.hashCode());
		result = prime * result + ((doi == null) ? 0 : doi.hashCode());
		result = prime * result + ((fileExtension == null) ? 0 : fileExtension.hashCode());
		result = prime * result + ((keywords == null) ? 0 : keywords.hashCode());
		result = prime * result + ((landingPageUrl == null) ? 0 : landingPageUrl.hashCode());
		result = prime * result
				+ ((publisher == null) ? 0 : publisher.hashCode());
		result = prime * result + ((ostiId == null) ? 0 : ostiId.hashCode());
		result = prime * result + ((otherIdentifyingNumbers == null) ? 0 : otherIdentifyingNumbers.hashCode());
		result = prime * result + ((productType == null) ? 0 : productType.hashCode());
		result = prime * result + ((productTypeSpecific == null) ? 0 : productTypeSpecific.hashCode());
		result = prime * result + ((publicationDate == null) ? 0 : publicationDate.hashCode());
		result = prime * result + ((relatedIdentifiers == null) ? 0 : relatedIdentifiers.hashCode());
		result = prime * result + ((releatedResource == null) ? 0 : releatedResource.hashCode());
		result = prime * result + ((setReserved == null) ? 0 : setReserved.hashCode());
		result = prime * result + ((sponsoringOrganization == null) ? 0 : sponsoringOrganization.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof OSTIRecord)) {
			return false;
		}
		OSTIRecord other = (OSTIRecord) obj;
		if (availability == null) {
			if (other.availability != null) {
				return false;
			}
		} else if (!availability.equals(other.availability)) {
			return false;
		}
		if (contactEmail == null) {
			if (other.contactEmail != null) {
				return false;
			}
		} else if (!contactEmail.equals(other.contactEmail)) {
			return false;
		}
		if (contactName == null) {
			if (other.contactName != null) {
				return false;
			}
		} else if (!contactName.equals(other.contactName)) {
			return false;
		}
		if (contactOrganization == null) {
			if (other.contactOrganization != null) {
				return false;
			}
		} else if (!contactOrganization.equals(other.contactOrganization)) {
			return false;
		}
		if (contactPhone == null) {
			if (other.contactPhone != null) {
				return false;
			}
		} else if (!contactPhone.equals(other.contactPhone)) {
			return false;
		}
		if (contractNumbers == null) {
			if (other.contractNumbers != null) {
				return false;
			}
		} else if (!contractNumbers.equals(other.contractNumbers)) {
			return false;
		}
		if (contributorOrganizations == null) {
			if (other.contributorOrganizations != null) {
				return false;
			}
		} else if (!contributorOrganizations.equals(other.contributorOrganizations)) {
			return false;
		}
		if (creators == null) {
			if (other.creators != null) {
				return false;
			}
		} else if (!creators.equals(other.creators)) {
			return false;
		}
		if (projectNumbers == null) {
			if (other.projectNumbers != null) {
				return false;
			}
		} else if (!projectNumbers.equals(other.projectNumbers)) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (doiInfix == null) {
			if (other.doiInfix != null) {
				return false;
			}
		} else if (!doiInfix.equals(other.doiInfix)) {
			return false;
		}
		if (doi == null) {
			if (other.doi != null) {
				return false;
			}
		} else if (!doi.equals(other.doi)) {
			return false;
		}
		if (fileExtension == null) {
			if (other.fileExtension != null) {
				return false;
			}
		} else if (!fileExtension.equals(other.fileExtension)) {
			return false;
		}
		if (keywords == null) {
			if (other.keywords != null) {
				return false;
			}
		} else if (!keywords.equals(other.keywords)) {
			return false;
		}
		if (landingPageUrl == null) {
			if (other.landingPageUrl != null) {
				return false;
			}
		} else if (!landingPageUrl.equals(other.landingPageUrl)) {
			return false;
		}
		if (publisher == null) {
			if (other.publisher != null) {
				return false;
			}
		} else if (!publisher.equals(other.publisher)) {
			return false;
		}
		if (ostiId == null) {
			if (other.ostiId != null) {
				return false;
			}
		} else if (!ostiId.equals(other.ostiId)) {
			return false;
		}
		if (otherIdentifyingNumbers == null) {
			if (other.otherIdentifyingNumbers != null) {
				return false;
			}
		} else if (!otherIdentifyingNumbers.equals(other.otherIdentifyingNumbers)) {
			return false;
		}
		if (productType != other.productType) {
			return false;
		}
		if (productTypeSpecific == null) {
			if (other.productTypeSpecific != null) {
				return false;
			}
		} else if (!productTypeSpecific.equals(other.productTypeSpecific)) {
			return false;
		}
		if (publicationDate == null) {
			if (other.publicationDate != null) {
				return false;
			}
		} else if (!publicationDate.equals(other.publicationDate)) {
			return false;
		}
		if (relatedIdentifiers == null) {
			if (other.relatedIdentifiers != null) {
				return false;
			}
		} else if (!relatedIdentifiers.equals(other.relatedIdentifiers)) {
			return false;
		}
		if (releatedResource == null) {
			if (other.releatedResource != null) {
				return false;
			}
		} else if (!releatedResource.equals(other.releatedResource)) {
			return false;
		}
		if (setReserved == null) {
			if (other.setReserved != null) {
				return false;
			}
		} else if (!setReserved.equals(other.setReserved)) {
			return false;
		}
		if (sponsoringOrganization == null) {
			if (other.sponsoringOrganization != null) {
				return false;
			}
		} else if (!sponsoringOrganization.equals(other.sponsoringOrganization)) {
			return false;
		}
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		return true;
	}

}
