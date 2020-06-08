
package gov.nih.tbi.dictionary.model;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.SeverityLevel;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SessionDataStructure implements Serializable
{

    /**
	 * 
	 */
    private static final long serialVersionUID = -4049660844623863748L;

    /******************************************************************************************************/

    private FormStructure dataStructure;

    private RepeatableGroup repeatableGroup;

    private Boolean newStructure;

    private Boolean draftCopy;

    // These three variables are used to keep track of how many new repeatable groups, map elements and
    // data elements have been created this session. The number is used to give each new data a unique id
    // and is decremented for each new data. The negative ids let the dao know the id is temporary.
    private Integer newRepeatableGroups;

    private Integer newMappedElements;

    private MapElement mapElement;

    // private Integer newDataElements;

    // Permission Stuff
    private List<EntityMap> entityMapList;
    private List<EntityMap> removedMapList;
    private List<String> entityMapAuthNameList;

    private PermissionType sessionDataStructureUserPermissionType;
    
    private List<SeverityRecord> severityRecords = new ArrayList<SeverityRecord>();
    private SeverityLevel fSChangeSeverity;
    private Long originalFSId;
    private Long changedFsId;
    /******************************************************************************************************/

    public SessionDataStructure()
    {

        super();
        newRepeatableGroups = -1;
        newMappedElements = -1;
        // newDataElements = -1;
        newStructure = false;
        draftCopy = false;
    }

    public FormStructure getDataStructure()
    {

        return dataStructure;
    }

    public void setDataStructure(FormStructure dataStructure)
    {

        this.dataStructure = dataStructure;
    }

    /**
     * @return the repeatableGroup
     */
    public RepeatableGroup getRepeatableGroup()
    {

        return repeatableGroup;
    }

    /**
     * @param repeatableGroup2
     *            the repeatableGroup to set
     */
    public void setRepeatableGroup(RepeatableGroup repeatableGroup2)
    {

        this.repeatableGroup = repeatableGroup2;
    }

    public Boolean getNewStructure()
    {

        return newStructure;
    }

    public void setNewStructure(Boolean newStructure)
    {

        this.newStructure = newStructure;
    }

    public Boolean getDraftCopy()
    {

        return draftCopy;
    }

    public void setDraftCopy(Boolean draftCopy)
    {

        this.draftCopy = draftCopy;
    }

    /**
     * @return the newRepeatableGroups
     */
    public Integer getNewRepeatableGroups()
    {

        return newRepeatableGroups;
    }

    /**
     * @param newRepeatableGroups
     *            the newRepeatableGroups to set
     */
    public void setNewRepeatableGroups(Integer newRepeatableGroups)
    {

        this.newRepeatableGroups = newRepeatableGroups;
    }

    /**
     * @return the newMappedElements
     */
    public Integer getNewMappedElements()
    {

        return newMappedElements;
    }

    /**
     * @param newMappedElements
     *            the newMappedElements to set
     */
    public void setNewMappedElements(Integer newMappedElements)
    {

        this.newMappedElements = newMappedElements;
    }

    /**
     * Resets all the properties of the session data structure
     */
    public void clear()
    {

        dataStructure = null;
        repeatableGroup = null;
        newRepeatableGroups = -1;
        newMappedElements = -1;
        newStructure = false;
        draftCopy = false;

        entityMapList = null;
        removedMapList = null;
        entityMapAuthNameList = null;
        sessionDataStructureUserPermissionType =null;
        
        severityRecords = new ArrayList<SeverityRecord>();
    	fSChangeSeverity =null;
    	originalFSId =null;
    	changedFsId =null;
    }
    
    public void clearPermission()
    {
    	sessionDataStructureUserPermissionType =null;
    }
    
    public void clearSeverityRecords()
    {
    	severityRecords = new ArrayList<SeverityRecord>();
    	fSChangeSeverity =null;
    	originalFSId =null;
    	changedFsId =null;
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

    /**
     * @return the mapElement
     */
    public MapElement getMapElement()
    {

        return mapElement;
    }

    /**
     * @param mapElement
     *            the mapElement to set
     */
    public void setMapElement(MapElement mapElement)
    {

        this.mapElement = mapElement;
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

	public PermissionType getSessionDataStructureUserPermissionType() {
		return sessionDataStructureUserPermissionType;
	}

	public void setSessionDataStructureUserPermissionType(PermissionType sessionDataStructureUserPermissionType) {
		this.sessionDataStructureUserPermissionType = sessionDataStructureUserPermissionType;
	}

	public List<SeverityRecord> getSeverityRecords() {
		return severityRecords;
	}

	public void setSeverityRecords(List<SeverityRecord> severityRecords) {
		this.severityRecords = severityRecords;
	}

	public SeverityLevel getfSChangeSeverity() {
		return fSChangeSeverity;
	}

	public void setfSChangeSeverity(SeverityLevel fSChangeSeverity) {
		this.fSChangeSeverity = fSChangeSeverity;
	}

	public Long getOriginalFSId() {
		return originalFSId;
	}

	public void setOriginalFSId(Long originalFSId) {
		this.originalFSId = originalFSId;
	}

	public Long getChangedFsId() {
		return changedFsId;
	}

	public void setChangedFsId(Long changedFsId) {
		this.changedFsId = changedFsId;
	}

}
