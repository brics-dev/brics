package gov.nih.nichd.ctdb.publication.domain;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * Created by IntelliJ IDEA.
 * User: matt
 * Date: Apr 18, 2011
 * Time: 9:57:39 AM
 * 
 * Edited by CIT
 * User: Ching Heng and J. Eng
 */
public class Publication extends CtdbDomainObject {
    private static final long serialVersionUID = 5558722263707099612L;
    
	private CtdbLookup publicationType;
    private String title;
    private String authors;
    private String description;
    private int protocolId;
    private int documentId;
    private String url;
    private String pubmedId;

    public int getDocumentId() {
		return documentId;
	}

	public void setDocumentId(int documentId) {
		this.documentId = documentId;
	}
	
	public CtdbLookup getPublicationType() {
        return publicationType;
    }

    public void setPublicationType(CtdbLookup publicationType) {
        this.publicationType = publicationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public int getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId;
    }

	public Document toXML() throws TransformationException, UnsupportedOperationException {
		throw new UnsupportedOperationException("Not Implemented in Publication.");
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPubmedId() {
		return pubmedId;
	}

	public void setPubmedId(String pubmedId) {
		this.pubmedId = pubmedId;
	}

}
