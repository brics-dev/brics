
package gov.nih.tbi.dictionary.model;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.SeverityLevel;
import gov.nih.tbi.dictionary.model.hibernate.Alias;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SessionDataElement implements Serializable
{

    private static final long serialVersionUID = 1149002349218274099L;

    /********************************************************************************/

    private DataElement dataElement;

    private MapElement mapElement;

    private List<Keyword> currentKeywords;
    private List<Keyword> currentLabels;

    private String prevPage;

    // Id for the next created keyword in this session. Each new keyword decrements
    // this value.
    private Integer newKeywordId;
    private Integer newLabelId;

    // A list of keywords created this session.
    private Set<Keyword> newKeywords;
    private Set<Keyword> newLabels;

    // Permission Stuff
    private List<EntityMap> entityMapList;
    private List<EntityMap> removedMapList;
    private List<String> entityMapAuthNameList;
    
    private PermissionType sessionDataElementUserPermissionType;
    
    private List<SeverityRecord> severityRecords = new ArrayList<SeverityRecord>();
    private SeverityLevel dataElementChangeSeverity;
    private Long originalDEId;
    private Long changedDEId;
    
    

    /********************************************************************************/

    public SessionDataElement()
    {

        super();
        newKeywordId = -1;
        newLabelId = -1;
    }

    public DataElement getDataElement()
    {

        return dataElement;
    }

    public MapElement getMapElement()
    {

        return mapElement;
    }

    public String getPrevPage()
    {

        return prevPage;
    }

    public void setPrevPage(String prevPage)
    {

        this.prevPage = prevPage;
    }

    public void setDataElement(DataElement dataElement)
    {

        this.dataElement = dataElement;
    }

    public void setMapElement(MapElement currentMapElement)
    {

        this.mapElement = currentMapElement;
    }

    public void setNewKeywordId(Integer newKeywordId)
    {

        this.newKeywordId = newKeywordId;
    }

    public void setNewLabelId(Integer newLabelId)
    {

        this.newLabelId = newLabelId;
    }

    public Integer getKeywordId()
    {

        return newKeywordId;
    }

    public Integer getLabelId()
    {

        return newLabelId;
    }

    public Set<Keyword> getNewKeywords()
    {

        if (newKeywords == null)
        {
            newKeywords = new LinkedHashSet<Keyword>();
        }
        return newKeywords;
    }

    public Set<Keyword> getNewLabels()
    {

        if (newLabels == null)
        {
            newLabels = new LinkedHashSet<Keyword>();
        }
        return newLabels;
    }

    public void addNewKeyword(Keyword keyword)
    {

        if (newKeywords == null)
        {
            newKeywords = new LinkedHashSet<Keyword>();
        }
        newKeywords.add(keyword);
    }

    public void addNewLabel(Keyword label)
    {

        if (newLabels == null)
        {
            newLabels = new LinkedHashSet<Keyword>();
        }
        newLabels.add(label);
    }

    public List<Keyword> getCurrentKeywords()
    {

        if (currentKeywords == null)
        {
            currentKeywords = new ArrayList<Keyword>();
        }

        return currentKeywords;
    }

    public List<Keyword> getCurrentLabels()
    {

        if (currentLabels == null)
        {
            currentLabels = new ArrayList<Keyword>();
        }
        return currentLabels;
    }

    public void setCurrentKeywords(List<Keyword> currentKeywords)
    {

        this.currentKeywords = currentKeywords;
    }

    public void setCurrentLables(List<Keyword> currentLabels)
    {

        this.currentLabels = currentLabels;
    }

    /**
     * This method removes the alias with the name of param.
     * 
     * @param name
     */
    public void removeAliasByName(String name)
    {

        Set<Alias> aliasList = null;
        aliasList = dataElement.getAliasList();

        Alias toBeRemoved = null;

        for (Alias alias : aliasList)
        {
            if (alias.getName().equals(name))
            {
                toBeRemoved = alias;
            }
        }

        if (toBeRemoved != null)
        {
            aliasList.remove(toBeRemoved);
        }
    }

    public List<EntityMap> getEntityMapList()
    {

        return entityMapList;
    }

    public List<EntityMap> getRemovedMapList()
    {

        if (removedMapList == null)
        {
            removedMapList = new ArrayList<EntityMap>();
        }

        return removedMapList;
    }

    public void setEntityMapList(List<EntityMap> entityMapList)
    {

        this.entityMapList = entityMapList;
    }
    
    public List<String> getEntityMapAuthNameList()
    {
        if (entityMapAuthNameList == null)
        {
        	entityMapAuthNameList = new ArrayList<String>();
        }

        return entityMapAuthNameList;
    }

    public void setEntityMapAuthNameList(List<String> entityMapAuthNameList)
    {

        this.entityMapAuthNameList = entityMapAuthNameList;
    }

    /**
     * Remove all the properties of the session
     */
    public void clear()
    {

        // clear() no longer removes dataElements. This prevents a bug when editing documentation and
        // the dataElement is over written later in the edit/create function anyway.
        // dataElement = null;
        currentKeywords = null;
        prevPage = "";
        newKeywordId = -1;
        newKeywords = null;
        newLabelId = -1;
        newLabels = null;

        entityMapList = null;
        removedMapList = null;
        entityMapAuthNameList = null;
        sessionDataElementUserPermissionType = null;
        
        severityRecords = new ArrayList<SeverityRecord>();
        dataElementChangeSeverity = null;
        originalDEId = null;
        changedDEId = null;
    }
    
    public void clearPermission()
    {
    	sessionDataElementUserPermissionType = null;
    }
    
    public void clearSeverityRecords()
    {
    	severityRecords = new ArrayList<SeverityRecord>();
    	dataElementChangeSeverity =null;
    	originalDEId = null;
        changedDEId = null;
    }

	public PermissionType getSessionDataElementUserPermissionType() {
		return sessionDataElementUserPermissionType;
	}

	public void setSessionDataElementUserPermissionType(PermissionType sessionDataElementUserPermissionType) {
		this.sessionDataElementUserPermissionType = sessionDataElementUserPermissionType;
	}

	public List<SeverityRecord> getSeverityRecords() {
		return severityRecords;
	}

	public void setSeverityRecords(List<SeverityRecord> severityRecords) {
		this.severityRecords = severityRecords;
	}

	public SeverityLevel getDataElementChangeSeverity() {
		return dataElementChangeSeverity;
	}

	public void setDataElementChangeSeverity(SeverityLevel dataElementChangeSeverity) {
		this.dataElementChangeSeverity = dataElementChangeSeverity;
	}

	public Long getOriginalDEId() {
		return originalDEId;
	}

	public void setOriginalDEId(Long originalDEId) {
		this.originalDEId = originalDEId;
	}

	public Long getChangedDEId() {
		return changedDEId;
	}

	public void setChangedDEId(Long changedDEId) {
		this.changedDEId = changedDEId;
	}
	
}