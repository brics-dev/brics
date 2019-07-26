
package gov.nih.tbi.query.model.hibernate;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Model for storing Saved Queries
 * 
 * ID (integer)
 * Name (character varying 255)
 * Description (character varying 4000)
 * Owner (integer)
 * XML (blob/text)
 * LastUpdate (timestamp without timezone)
 * 
 * @author Ryan Stewart
 */
@Entity
@Table(name = "Saved_Query")
@XmlRootElement(name = "savedQuery")
@XmlAccessorType(XmlAccessType.FIELD)
public class SavedQuery implements Serializable {
    private static final long serialVersionUID = -4355534373965146780L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SAVED_QUERY_SEQ")
    @SequenceGenerator(name = "SAVED_QUERY_SEQ", sequenceName = "SAVED_QUERY_SEQ", allocationSize = 1)
	@XmlElement(nillable = true)
    private Long id;

    @Column(name = "NAME")
    private String name;
    
    @Column(name = "DESCRIPTION")
    private String description;
    

	@Column(name = "XML")
    private String xml;
    
	@Column(name = "LAST_UPDATED")
	@XmlElement(nillable = true)
    private Date lastUpdated;
	
	@Column(name = "COPY_FLAG")
	private Boolean copyFlag;
	
	@Column(name = "QUERY_DATA")
	private String queryData;
   
	public SavedQuery() {
		name = "";
		description = "";
		xml = "";
		copyFlag = Boolean.FALSE;
		queryData = "";
	}
	 
	public SavedQuery(String name) {
		this.name = name;
		description = "";
		xml = "";
		copyFlag = Boolean.FALSE;
		queryData = "";
	}
	 
	 /* 
	  * Copy Constructor
	  */
	public SavedQuery(SavedQuery originalQuery) {
		this.id = originalQuery.getId();
		this.name = originalQuery.getName();
		this.description = originalQuery.getDescription();
		this.xml = originalQuery.getXml();
		this.lastUpdated = originalQuery.getLastUpdated();
		this.copyFlag = originalQuery.getCopyFlag();
		this.queryData = originalQuery.getQueryData();
	}

	public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public String getName()
    {

        return name;
    }

    public void setName(String name)
    {

        this.name = name;
    }

    public String getDescription()
    {

        return description;
    }

    public void setDescription(String description)
    {

        this.description = description;
    }

	
	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}
	
	public String getQueryData() {
		return queryData;
	}

	public void setQueryData(String queryData) {
		this.queryData = queryData;
	}

	public Date getLastUpdated() {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.format(lastUpdated);
		
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Boolean getCopyFlag() {
		return copyFlag;
	}

	public void setCopyFlag(Boolean copyFlag) {
		this.copyFlag = copyFlag;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		SavedQuery other = (SavedQuery) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;

		return true;
	}

	@Override
	public String toString() {
		return "SavedQuery [id=" + id + ", name=" + name + ", description="
				+ description + "]";
	}
}
