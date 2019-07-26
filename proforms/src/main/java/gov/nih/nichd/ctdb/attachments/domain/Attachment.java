package gov.nih.nichd.ctdb.attachments.domain;

import java.io.File;
import java.io.InputStream;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Dec 13, 2006
 * Time: 11:18:27 AM
 * Represents an attachment of any sort
 */
public class Attachment extends CtdbDomainObject {
    
	private static final long serialVersionUID = -5599914945932244817L;
	private String fileName = "";
    private String description = "";
    private byte[] data = null;
    private File file = null;
    private File attachFile = null;
    private InputStream attachFileContent = null;  

	/**
     * associated Id is the Id of the associated object realized through type
     * if the type is protocol, it's a protocolid
     * if the type is patient, it's a patientid
     * if the type is form, it's an administeredformid
     */
    private int associatedId = Integer.MIN_VALUE;
    private AttachmentCategory category = null;
    private CtdbLookup type = null;
    private String name = "";
    private String changeReason = "";
    private String attachmentActionFlag;
	private String sampleName = "";
	private String storagePath = "";
	private CtdbLookup publicationType = null;
	private String authors = "";
	private String url = "";
	private String pubMedId = "";
	private boolean updated = false;
	private boolean deleted = false;
    
    public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

    public String getAttachmentActionFlag() {
		return attachmentActionFlag;
	}

	public void setAttachmentActionFlag(String attachmentActionFlag) {
		this.attachmentActionFlag = attachmentActionFlag;
	}

	public Attachment () {
        super();
    }
   
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getAssociatedId() {
        return associatedId;
    }

    public void setAssociatedId(int associatedId) {
        this.associatedId = associatedId;
    }

    public AttachmentCategory getCategory() {
    	if( category == null ) {
    		category = new AttachmentCategory(); 
    	}
    	
        return category;
    }

    public void setCategory(AttachmentCategory category) {
        this.category = category;
    }

    public CtdbLookup getType() {
        return type;
    }

    public void setType(CtdbLookup type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }


    public Document toXML() throws TransformationException, UnsupportedOperationException {
	    throw new UnsupportedOperationException("toXML() is not supported in Attachment.");
    }
    
    public String getSampleName() {
		return sampleName;
	}

	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	/**
	 * @return the storagePath
	 */
	public String getStoragePath() {
		return storagePath;
	}

	/**
	 * @param storagePath the storagePath to set
	 */
	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}

	/**
	 * @return the publicationType
	 */
	public CtdbLookup getPublicationType() {
		return publicationType;
	}

	/**
	 * @param publicationType the publicationType to set
	 */
	public void setPublicationType(CtdbLookup publicationType) {
		this.publicationType = publicationType;
	}

	/**
	 * @return the authors
	 */
	public String getAuthors() {
		return authors;
	}

	/**
	 * @param authors the authors to set
	 */
	public void setAuthors(String authors) {
		this.authors = authors;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the pubMedId
	 */
	public String getPubMedId() {
		return pubMedId;
	}

	/**
	 * @param pubMedId the pubMedId to set
	 */
	public void setPubMedId(String pubMedId) {
		this.pubMedId = pubMedId;
	}
	
    public File getAttachFile() {
		return attachFile;
	}

	public void setAttachFile(File attachFile) {
		this.attachFile = attachFile;
	}
	
	//These two fields were added for Struts2 work around
	private String attachFileContentType = null;
    private String attachFileFileName = null;
	public String getAttachFileContentType() {
		return attachFileContentType;
	}
	public void setAttachFileContentType(String attachFileContentType) {
		this.attachFileContentType = attachFileContentType;
	}
	public String getAttachFileFileName() {
		return attachFileFileName;
	}
	public void setAttachFileFileName(String attachFileFileName) {
		this.attachFileFileName = attachFileFileName;
	}

	public InputStream getAttachFileContent() {
		return attachFileContent;
	}

	public void setAttachFileContent(InputStream attachFileContent) {
		this.attachFileContent = attachFileContent;
	}

	/**
	 * @return the updated
	 */
	public boolean isUpdated() {
		return updated;
	}

	/**
	 * @param updated the updated to set
	 */
	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	/**
	 * @return the deleted
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * @param deleted the deleted to set
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + associatedId;
		result = prime * result + ((attachFileContentType == null) ? 0 : attachFileContentType.hashCode());
		result = prime * result + ((attachFileFileName == null) ? 0 : attachFileFileName.hashCode());
		result = prime * result + ((authors == null) ? 0 : authors.hashCode());
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((pubMedId == null) ? 0 : pubMedId.hashCode());
		result = prime * result + ((publicationType == null) ? 0 : publicationType.hashCode());
		result = prime * result + ((sampleName == null) ? 0 : sampleName.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + (updated ? 1231 : 1237);
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		Attachment other = (Attachment) obj;
		if (associatedId != other.associatedId)
			return false;
		if (attachFileContentType == null) {
			if (other.attachFileContentType != null)
				return false;
		} else if (!attachFileContentType.equals(other.attachFileContentType))
			return false;
		if (attachFileFileName == null) {
			if (other.attachFileFileName != null)
				return false;
		} else if (!attachFileFileName.equals(other.attachFileFileName))
			return false;
		if (authors == null) {
			if (other.authors != null)
				return false;
		} else if (!authors.equals(other.authors))
			return false;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (pubMedId == null) {
			if (other.pubMedId != null)
				return false;
		} else if (!pubMedId.equals(other.pubMedId))
			return false;
		if (publicationType == null) {
			if (other.publicationType != null)
				return false;
		} else if (!publicationType.equals(other.publicationType))
			return false;
		if (sampleName == null) {
			if (other.sampleName != null)
				return false;
		} else if (!sampleName.equals(other.sampleName))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (updated != other.updated)
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

}
